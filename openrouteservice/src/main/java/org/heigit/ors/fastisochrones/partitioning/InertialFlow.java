package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import org.heigit.ors.fastisochrones.partitioning.Projector.Projection;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.getMaxCellNodesNumber;
import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.getMinCellNodesNumber;

/**
 * Recursive implementation of InertialFlow algorithm for partitioning a graph.
 *
 * @author Hendrik Leuschner
 */
public class InertialFlow implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InertialFlow.class);
    private static final int MIN_SPLITTING_ITERATION = 0;
    private static final int MAX_SPLITTING_ITERATION = Integer.MAX_VALUE; //==2^32
    //Defines the number of disconnected cells a cell can be split into after the final InertialFlow step.
    //Using an estimate of 10 as most cells are disconnected into 1 - 5 independent cells.
    //If the value is too high and the graph has (e.g. by faulty data) many disconnected nodes, we do not want to create a separate cell for all of them
    private static final int MAX_SUBCELL_NUMBER = 10;
    private static final boolean SEPARATEDISCONNECTED = true;
    //The projections are evaluated before the max flow algorithm. Only the best CONSIDERED_PROJECTIONS are actually run through the algorithm, as MaxFlow is relatively costly
    private static final int CONSIDERED_PROJECTIONS = 3;
    private static final Projector projector = new Projector();
    private static FlagEncoder flagEncoder;
    protected Map<Projection, IntArrayList> projections;
    private int cellId;
    private Graph ghGraph;
    private GraphHopperStorage ghStorage;
    private EdgeFilter edgeFilter;
    private PartitioningData pData;
    private int[] nodeToCellArr;
    private ExecutorService executorService;
    private InverseSemaphore inverseSemaphore;

    public InertialFlow(int[] nodeToCellArray, GraphHopperStorage ghStorage, EdgeFilterSequence edgeFilters, ExecutorService executorService, InverseSemaphore inverseSemaphore) {
        //Start cellId 1 so that bitshifting it causes no zeros at the front
        setNodeToCellArr(nodeToCellArray);
        setCellId(1);
        setGraph(ghStorage.getBaseGraph());
        setGraphHopperStorage(ghStorage);
        setEdgeFilter(edgeFilters);
        setExecutorService(executorService);
        setInverseSemaphore(inverseSemaphore);
        setFlagEncoder(ghStorage.getEncodingManager().fetchEdgeEncoders().get(0));

        PartitioningData partitioningData = new PartitioningData();
        PartitioningDataBuilder partitioningDataBuilder = new PartitioningDataBuilder(ghStorage.getBaseGraph(), partitioningData);
        partitioningDataBuilder.run();
        setPartitioningData(partitioningData);
        projector.setGHStorage(ghStorage);
        setProjections(projector.calculateProjections());

        LOGGER.info("Number of nodes: {}", ghGraph.getNodes());
        LOGGER.info("Number of edges: {}", ghGraph.getAllEdges().length());
    }

    private InertialFlow() {
    }

    /**
     * Split the graph. Order the projections by this split. Run the recursion.
     */
    public void run() {
        try {
            BiPartition biPartition = graphBiSplit(this.projections);
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
        double sizeFactor = ((double) projections.get(Projection.LINE_M00).size()) / ghGraph.getBaseGraph().getNodes();
        mincutScore = Math.max((int) Math.ceil(mincutScore * sizeFactor), 5);
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
                mincutScore = cutScore;
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
     * This is a parallel computation. When the cells get too small, the overhead of creating new threads can be more costly
     * than just running the remaining InertialFlows in serial in the same thread. That's why there is a check of the node size.
     *
     * @param invokeNext            which partitions to further divide
     * @param nodesPartition0       size of partition 0
     * @param nodesPartition1       size of partition 1
     * @param biPartitionProjection reference to projection
     */
    private void recursion(boolean[] invokeNext, int nodesPartition0, int nodesPartition1, BiPartitionProjection biPartitionProjection) {
        int totalNodes = nodesPartition0 + nodesPartition1;
        for (int i : new int[]{0, 1}) {
            if (invokeNext[i]) {
                if (totalNodes > getMaxCellNodesNumber() * 4) {
                    executorService.execute(createInertialFlow(i, biPartitionProjection));
                } else {
                    InertialFlow inertialFlow = createInertialFlow(i, biPartitionProjection);
                    inertialFlow.run();
                }
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
        InertialFlow inertialFlow = new InertialFlow();
        inertialFlow.setCellId(cellId << 1 | partitionNumber);
        inertialFlow.setNodeToCellArr(nodeToCellArr);
        inertialFlow.setGraph(ghGraph);
        inertialFlow.setGraphHopperStorage(ghStorage);
        inertialFlow.setPartitioningData(pData);
        inertialFlow.setProjections(biPartitionProjection.getProjection(partitionNumber));
        inertialFlow.setEdgeFilter(edgeFilter);
        inertialFlow.setExecutorService(executorService);
        inertialFlow.setInverseSemaphore(inverseSemaphore);
        return inertialFlow;
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
            boolean invokeRecursion = (cellId < MAX_SPLITTING_ITERATION) && (biPartition.getPartition(i).size() > getMaxCellNodesNumber());

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
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        edgeFilterSequence.add(AccessFilter.allEdges(flagEncoder.getAccessEnc()));
        if(edgeFilter != null)
            edgeFilterSequence.add(edgeFilter);

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
                    int nextNode = edgeIterator.getAdjNode();
                    if (!edgeFilterSequence.accept(edgeIterator)
                            || connectedCell.contains(nextNode)
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
        return new EdmondsKarpAStar(ghGraph, pData, edgeFilter);
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public void setGraph(Graph ghGraph) {
        this.ghGraph = ghGraph;
    }

    public void setGraphHopperStorage(GraphHopperStorage graphHopperStorage) {
        this.ghStorage = graphHopperStorage;
    }

    public void setEdgeFilter(EdgeFilter edgeFilter) {
        this.edgeFilter = edgeFilter;
    }

    public void setPartitioningData(PartitioningData pData) {
        this.pData = pData;
    }

    public void setNodeToCellArr(int[] nodeToCellArr) {
        this.nodeToCellArr = nodeToCellArr;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setInverseSemaphore(InverseSemaphore inverseSemaphore) {
        this.inverseSemaphore = inverseSemaphore;
    }

    public static void setFlagEncoder(FlagEncoder newFlagEncoder) {
        flagEncoder = newFlagEncoder;
    }

    public void setProjections(Map<Projection, IntArrayList> projections) {
        this.projections = projections;
    }
}
