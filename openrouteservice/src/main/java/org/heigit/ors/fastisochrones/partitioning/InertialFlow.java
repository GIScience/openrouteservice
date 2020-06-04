package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import org.heigit.ors.fastisochrones.partitioning.Projector.Projection;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.*;
import java.util.concurrent.ExecutorService;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.*;

/**
 * Recursive implementation of InertialFlow algorithm for partitioning a graph.
 *
 * @author Hendrik Leuschner
 */
public class InertialFlow implements Runnable {
    private static Projector projector;
    private final int MIN_SPLITTING_ITERATION = 0;
    private final int MAX_SPLITTING_ITERATION = 268435456; //==2^28
    private final int MAX_SUBCELL_NUMBER = 10;
    private final boolean SEPARATEDISCONNECTED = true;
    private final int CONSIDERED_PROJECTIONS = 3;
    protected Map<Projection, IntArrayList> projections;
    private int cellId;
    private Graph ghGraph;
    private EdgeFilter edgeFilter;
    private PartitioningData pData;
    private int[] nodeToCellArr;
    private ExecutorService executorService;
    private InverseSemaphore inverseSemaphore;

    public InertialFlow(int[] nodeToCellArray, GraphHopperStorage ghStorage, EdgeFilterSequence edgeFilters, ExecutorService executorService, InverseSemaphore inverseSemaphore) {
        //Start cellId 1 so that bitshifting it causes no zeros at the front
        this(nodeToCellArray, 1, ghStorage.getBaseGraph(), new PartitioningData(), null, edgeFilters, executorService, inverseSemaphore);
        if (isLogEnabled()) System.out.println("Number of nodes: " + ghGraph.getNodes());
        if (isLogEnabled()) System.out.println("Number of edges: " + ghGraph.getAllEdges().length());
        PartitioningDataBuilder partitioningDataBuilder = new PartitioningDataBuilder(ghStorage.getBaseGraph(), pData);
        partitioningDataBuilder.setAdditionalEdgeFilter(edgeFilter);
        partitioningDataBuilder.run();
        projector = new Projector(ghStorage);
        this.projections = projector.calculateProjections();
    }

    private InertialFlow(int[] nodeToCellArr, int cellId, Graph graph, PartitioningData pData, Map<Projection, IntArrayList> projections, EdgeFilter edgeFilter, ExecutorService executorService, InverseSemaphore inverseSemaphore) {
        this.executorService = executorService;
        this.pData = pData;
        this.cellId = cellId;
        this.ghGraph = graph;
        this.nodeToCellArr = nodeToCellArr;
        this.inverseSemaphore = inverseSemaphore;
        this.edgeFilter = edgeFilter;
        this.projections = projections;
    }

    /**
     * Split the graph. Order the projections by this split. Run the recursion.
     */
    public void run() {
        try {
            BiPartition biPartition = graphBiSplit(this.projections);
//            saveResults(biPartition);
            BiPartitionProjection biPartitionProjection = projector.partitionProjections(this.projections, biPartition);
            this.projections = null;
            recursion(getInvokeNextOrSaveResult(biPartition), biPartition.getPartition(0).size(), biPartition.getPartition(1).size(), biPartitionProjection);
        } finally {
            inverseSemaphore.taskCompleted();
        }
    }

    /**
     * Splits a set of nodes into two sets of nodes according to the maxflowmincut algorithm.
     *
     * @param projections projections of the nodes to be split.
     * @return
     */
    private BiPartition graphBiSplit(Map<Projection, IntArrayList> projections) {
        //Estimated maximum iterations
        int mincutScore = ghGraph.getBaseGraph().getAllEdges().length();
        double sizeFactor = ((double) projections.get(Projection.Line_m00).size()) / ghGraph.getBaseGraph().getNodes();
        mincutScore = (int) Math.ceil(mincutScore * sizeFactor);
        BiPartition biPartition = new BiPartition();
        MaxFlowMinCut maxFlowMinCut = createEdmondsKarp();
        List<Projection> projOrder = projector.calculateProjectionOrder(projections);
        //>> Loop through Projections and project each Node
        int i = 0;
        for (Projection proj : projOrder) {
            //Try only best projections
            if (i == CONSIDERED_PROJECTIONS)
                break;
            //>> sort projected Nodes
            maxFlowMinCut.setOrderedNodes(projections.get(proj));
            maxFlowMinCut.setNodeOrder();
            maxFlowMinCut.setMaxFlowLimit(mincutScore);
            maxFlowMinCut.reset();
            int cutScore = maxFlowMinCut.getMaxFlow();
            if (cutScore < mincutScore) {
                //>> store Results
                mincutScore = cutScore;
                //>> get Data for next Recursion-Step
                biPartition = maxFlowMinCut.calcNodePartition();
            }
            i++;
        }
        return biPartition;
    }

    /**
     * Save multiple cells (from separated disconnected) by iteratively assigning bit shifted cellIds
     *
     * @param cells     set of cells to be stored
     * @param newCellId original cell ids
     */
    private void saveMultiCells(Set<IntHashSet> cells, int newCellId) {
        // Store first cell with given cellId. If there are more subcells to be stored, create subcellId.
        Iterator<IntHashSet> iterator = cells.iterator();
        IntHashSet cell = iterator.next();
        for (IntCursor node : cell) {
            nodeToCellArr[node.value] = newCellId;
        }
        newCellId = newCellId << 1;

        while (iterator.hasNext()) {
            cell = iterator.next();
            for (IntCursor node : cell) {
                nodeToCellArr[node.value] = newCellId;
            }
            if (iterator.hasNext()) {
                cell = iterator.next();
                for (IntCursor node : cell) {
                    nodeToCellArr[node.value] = newCellId | 1;
                }
            }
            newCellId = newCellId << 1;
        }
    }

    /**
     * Recursively invoke InertialFlow for partitioning areas of the graph further.
     * Either invoke a further partition on both partitions or just one.
     *
     * @param invokeNext            which partitions to further divide
     * @param nodesPartition0       size of partition 0
     * @param nodesPartition1       size of partition 1
     * @param biPartitionProjection reference to projection
     */
    private void recursion(boolean[] invokeNext, int nodesPartition0, int nodesPartition1, BiPartitionProjection biPartitionProjection) {
        //Partition both areas
        int totalNodes = nodesPartition0 + nodesPartition1;
        if (invokeNext[0] && invokeNext[1]) {
            if (totalNodes > getMaxCellNodesNumber() * 4) {
                executorService.execute(createInertialFlow(0, biPartitionProjection));
                executorService.execute(createInertialFlow(1, biPartitionProjection));
            } else {
                InertialFlow inertialFlow0 = createInertialFlow(0, biPartitionProjection);
                inertialFlow0.run();
                InertialFlow inertialFlow1 = createInertialFlow(1, biPartitionProjection);
                inertialFlow1.run();
            }
        }
        //Partition only area 0
        else if (invokeNext[0]) {
            if (totalNodes > getMaxCellNodesNumber() * 4) {
                executorService.execute(createInertialFlow(0, biPartitionProjection));
            } else {
                InertialFlow inertialFlow = createInertialFlow(0, biPartitionProjection);
                inertialFlow.run();
            }
        }
        //Partition only area 1
        else if (invokeNext[1]) {
            if (totalNodes > getMaxCellNodesNumber() * 4) {
                executorService.execute(createInertialFlow(1, biPartitionProjection));
            } else {
                InertialFlow inertialFlow = createInertialFlow(1, biPartitionProjection);
                inertialFlow.run();
            }
        }
    }

    /**
     * Create new InertialFlow object
     *
     * @param partitionNumber       determines whether partition is the 0th or 1st
     * @param biPartitionProjection
     * @return
     */
    private InertialFlow createInertialFlow(int partitionNumber, BiPartitionProjection biPartitionProjection) {
        inverseSemaphore.beforeSubmit();
        return new InertialFlow(nodeToCellArr, cellId << 1 | partitionNumber, ghGraph, pData, biPartitionProjection.getProjection(partitionNumber), this.edgeFilter, executorService, inverseSemaphore);
    }

    /**
     * Determine whether a partition should be split up further.
     * If no further splitting, save the current results
     *
     * @param biPartition
     * @return size 2 boolean array determining which of the partitions should be split
     */
    private boolean[] getInvokeNextOrSaveResult(BiPartition biPartition) {
        boolean[] invokeNext = new boolean[]{false, false};

        for (int i = 0; i < 2; i++) {
            boolean invokeRecursion = false;
            if ((cellId < MAX_SPLITTING_ITERATION) && (biPartition.getPartition(i).size() > getMaxCellNodesNumber()))
                invokeRecursion = true;

            if ((cellId < MIN_SPLITTING_ITERATION))
                invokeRecursion = true;

            if (invokeRecursion)
                invokeNext[i] = true;

            else {
                Set<IntHashSet> cellsToStore = new HashSet<>();
                if (SEPARATEDISCONNECTED && cellId < MAX_SPLITTING_ITERATION)
                    cellsToStore = separateDisconnected(biPartition.getPartition(i));
                else
                    cellsToStore.add(biPartition.getPartition(i));
                saveMultiCells(cellsToStore, cellId << 1 | i);
            }
        }
        return invokeNext;
    }

    /**
     * Identify disconnected parts of a cell so that they can be split.
     * This is necessary if there are nodes (or node groups) that belong to one cell without an actual edge connection.
     * This is applied to cells that would not be split by normal InertialFlow anymore due to small size.
     * Separation is done by dijkstra edge exploration.
     *
     * @param nodeSet set of nodes to be split
     * @return Set of disconnected subcells of the original nodeSet
     */
    public Set<IntHashSet> separateDisconnected(IntHashSet nodeSet) {
        Set<IntHashSet> disconnectedCells = new HashSet<>();
        EdgeExplorer edgeExplorer = ghGraph.createEdgeExplorer(EdgeFilter.ALL_EDGES);
        Queue<Integer> queue = new ArrayDeque<>();
        IntHashSet connectedCell = new IntHashSet(nodeSet.size());
        disconnectedCells.add(connectedCell);
        Iterator<IntCursor> iter;
        EdgeIterator edgeIterator;
        while (!nodeSet.isEmpty()) {
            if (connectedCell.size() >= getMinCellNodesNumber() && disconnectedCells.size() < MAX_SUBCELL_NUMBER) {
                connectedCell = new IntHashSet();
                disconnectedCells.add(connectedCell);
            }
            iter = nodeSet.iterator();
            int startNode = iter.next().value;
            queue.offer(startNode);
            connectedCell.add(startNode);

            while (!queue.isEmpty()) {
                int currentNode = queue.poll();
                edgeIterator = edgeExplorer.setBaseNode(currentNode);

                while (edgeIterator.next()) {
                    if (!accept(edgeIterator))
                        continue;
                    int nextNode = edgeIterator.getAdjNode();
                    if (connectedCell.contains(nextNode)
                            || !nodeSet.contains(nextNode))
                        continue;
                    queue.offer(nextNode);
                    connectedCell.add(nextNode);
                }
            }
            nodeSet.removeAll(connectedCell);
        }
        return disconnectedCells;
    }

    /**
     * Init algo max flow min cut.
     *
     * @return the max flow min cut
     */
    public MaxFlowMinCut createEdmondsKarp() {
        return new EdmondsKarpAStar(ghGraph, pData, this.edgeFilter);
    }

    private boolean accept(EdgeIterator edgeIterator) {
        return edgeFilter == null ? true : edgeFilter.accept(edgeIterator);
    }
}
