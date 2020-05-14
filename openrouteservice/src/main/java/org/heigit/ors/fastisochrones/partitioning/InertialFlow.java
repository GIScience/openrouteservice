package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.*;
import java.util.concurrent.ExecutorService;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.*;

import org.heigit.ors.fastisochrones.partitioning.Projector.*;

/**
 * Recursive implementation of InertialFlow algorithm for partitioning a graph.
 *
 * @author Hendrik Leuschner
 */
public class InertialFlow extends PartitioningBase {
    private static Projector projector;
    private final int MIN_SPLITTING_ITERATION = 0;
    private final int MAX_SPLITTING_ITERATION = 268435456; //==2^28
    private final int MAX_SUBCELL_NUMBER = 10;
    private final boolean SEPARATEDISCONNECTED = true;
    private final int CONSIDERED_PROJECTIONS = 3;
    private PreparePartition.InverseSemaphore inverseSemaphore;
    private int partitionNodeCount;

    public InertialFlow(int[] nodeToCellArray, GraphHopperStorage ghStorage, PartitioningData pData, EdgeFilterSequence edgeFilters, ExecutorService executorService, PreparePartition.InverseSemaphore inverseSemaphore) {
        super(nodeToCellArray, ghStorage, pData, edgeFilters, executorService);
        //Start cellId 1 so that bitshifting it causes no zeros at the front
        this.cellId = 1;
        this.ghGraph = ghStorage.getBaseGraph();
        this.partitionNodeCount = this.ghGraph.getNodes();
        if (isLogEnabled()) System.out.println("Number of nodes: " + ghGraph.getNodes());
        if (isLogEnabled()) System.out.println("Number of edges: " + ghGraph.getAllEdges().length());
        this.inverseSemaphore = inverseSemaphore;

        initAlgo();
        projector = new Projector(ghStorage);
        projector.prepareProjectionMaps();
        this.projections = projector.calculateProjections();
    }

    private InertialFlow(int[] nodeToCellArr, int cellId, GraphHopperStorage ghStorage, PartitioningData pData, int partitionNodeCount, Map<Projection, IntArrayList> projections, EdgeFilter edgeFilter, ExecutorService executorService, PreparePartition.InverseSemaphore inverseSemaphore) {
        setExecutorService(executorService);
        this.pData = pData;
        this.partitionNodeCount = partitionNodeCount;
        this.cellId = cellId;
        this.ghStorage = ghStorage;
        this.ghGraph = ghStorage.getBaseGraph();
        this.nodeToCellArr = nodeToCellArr;
        this.inverseSemaphore = inverseSemaphore;
        this.edgeFilter = edgeFilter;
        this.projections = projections;
    }

    public void run() {
        try {
            setAlgo();
            BiPartition biPartition = graphBiSplit(this.projections);
            saveResults(biPartition);
            BiPartitionProjection biPartitionProjection = projector.prepareProjections(this.projections, biPartition);
            this.projections = null;
            recursion(getInvokeNextAndSave(biPartition), biPartition.getPartition0().size(), biPartition.getPartition1().size(), biPartitionProjection);
        } finally {
            inverseSemaphore.taskCompleted();
        }
    }

    private BiPartition graphBiSplit(Map<Projection, IntArrayList> projections) {
        //Estimated maximum iterations
        int mincutScore = ghGraph.getBaseGraph().getAllEdges().length();
        double sizeFactor = ((double) partitionNodeCount) / ghGraph.getBaseGraph().getNodes();
        mincutScore = (int) Math.ceil(mincutScore * sizeFactor);
        BiPartition biPartition = new BiPartition();
        MaxFlowMinCut maxFlowMinCut = this.cellId == 1 ? initAlgo() : setAlgo();
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
            maxFlowMinCut.setMaxFlowLimit(mincutScore).initSubNetwork();
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

    private void saveResults(BiPartition biPartition) {
        //>> saving iteration results
        for (IntCursor node : biPartition.getPartition0())
            nodeToCellArr[node.value] = cellId << 1;
        for (IntCursor node : biPartition.getPartition1())
            nodeToCellArr[node.value] = cellId << 1 | 1;
    }

    private void saveMultiCells(Set<IntHashSet> cells, int motherId) {
        //>> saving iteration results
        Iterator<IntHashSet> iterator = cells.iterator();
        while (iterator.hasNext()) {
            IntHashSet cell = iterator.next();
            for (IntCursor node : cell) {
                nodeToCellArr[node.value] = motherId << 1;
            }
            if (iterator.hasNext()) {
                cell = iterator.next();
                for (IntCursor node : cell) {
                    nodeToCellArr[node.value] = motherId << 1 | 1;
                }
            }
            motherId = motherId << 1;
        }
    }

    private void recursion(boolean[] invokeNext, int part0Count, int part1Count, BiPartitionProjection biPartitionProjection) {
        if (invokeNext[0] == true && invokeNext[1] == true) {
            if (partitionNodeCount > getMaxCellNodesNumber() * 4) {
                inverseSemaphore.beforeSubmit();
                executorService.execute(new InertialFlow(nodeToCellArr, cellId << 1 | 0, ghStorage, pData, part0Count, biPartitionProjection.getProjection0(), this.edgeFilter, executorService, inverseSemaphore));
                inverseSemaphore.beforeSubmit();
                executorService.execute(new InertialFlow(nodeToCellArr, cellId << 1 | 1, ghStorage, pData, part1Count, biPartitionProjection.getProjection1(), this.edgeFilter, executorService, inverseSemaphore));
            } else {
                inverseSemaphore.beforeSubmit();
                InertialFlow inertialFlow = new InertialFlow(nodeToCellArr, cellId << 1 | 0, ghStorage, pData, part0Count, biPartitionProjection.getProjection0(), this.edgeFilter, executorService, inverseSemaphore);
                inertialFlow.run();
                inverseSemaphore.beforeSubmit();
                inertialFlow = new InertialFlow(nodeToCellArr, cellId << 1 | 1, ghStorage, pData, part1Count, biPartitionProjection.getProjection1(), this.edgeFilter, executorService, inverseSemaphore);
                inertialFlow.run();
            }
        } else if (invokeNext[0]) {
            if (partitionNodeCount > getMaxCellNodesNumber() * 4) {
                inverseSemaphore.beforeSubmit();
                executorService.execute(new InertialFlow(nodeToCellArr, cellId << 1 | 0, ghStorage, pData, part0Count, biPartitionProjection.getProjection0(), this.edgeFilter, executorService, inverseSemaphore));
            } else {
                inverseSemaphore.beforeSubmit();
                InertialFlow inertialFlow = new InertialFlow(nodeToCellArr, cellId << 1 | 0, ghStorage, pData, part0Count, biPartitionProjection.getProjection0(), this.edgeFilter, executorService, inverseSemaphore);
                inertialFlow.run();
            }
        } else if (invokeNext[1]) {
            if (partitionNodeCount > getMaxCellNodesNumber() * 4) {
                inverseSemaphore.beforeSubmit();
                executorService.execute(new InertialFlow(nodeToCellArr, cellId << 1 | 1, ghStorage, pData, part1Count, biPartitionProjection.getProjection1(), this.edgeFilter, executorService, inverseSemaphore));
            } else {
                inverseSemaphore.beforeSubmit();
                InertialFlow inertialFlow = new InertialFlow(nodeToCellArr, cellId << 1 | 1, ghStorage, pData, part1Count, biPartitionProjection.getProjection1(), this.edgeFilter, executorService, inverseSemaphore);
                inertialFlow.run();
            }
        }
    }

    private boolean[] getInvokeNextAndSave(BiPartition biPartition) {
        boolean[] invokeNext = new boolean[2];
        boolean nextRecursionLevel = false;
        int part0Count = biPartition.getPartition0().size();
        int part1Count = biPartition.getPartition1().size();

        if ((cellId < MAX_SPLITTING_ITERATION) && (part0Count > getMaxCellNodesNumber())) {
            nextRecursionLevel = true;
        }
        if ((cellId < MIN_SPLITTING_ITERATION))
            nextRecursionLevel = true;
        if (nextRecursionLevel == false && SEPARATEDISCONNECTED && (cellId < MAX_SPLITTING_ITERATION)) {
            Set<IntHashSet> disconnectedCells = separateDisconnected(biPartition.getPartition0(), cellId << 1);
            saveMultiCells(disconnectedCells, cellId << 1);
        }
        if (nextRecursionLevel) {
            invokeNext[0] = true;
        }

        nextRecursionLevel = false;

        if ((cellId < MAX_SPLITTING_ITERATION) && (part1Count > getMaxCellNodesNumber())) {
            nextRecursionLevel = true;
        }
        if ((cellId < MIN_SPLITTING_ITERATION))
            nextRecursionLevel = true;
        if (nextRecursionLevel == false && SEPARATEDISCONNECTED && (cellId < MAX_SPLITTING_ITERATION)) {
            Set<IntHashSet> disconnectedCells = separateDisconnected(biPartition.getPartition1(), cellId << 1 | 1);
            saveMultiCells(disconnectedCells, cellId << 1 | 1);
        }
        if (nextRecursionLevel) {
            invokeNext[1] = true;
        }
        return invokeNext;
    }

    /*
    Identify disconnected parts of a cell so that they can be split
     */
    private Set<IntHashSet> separateDisconnected(IntHashSet nodeSet, int cellId) {
        Set<IntHashSet> disconnectedCells = new HashSet<>();
        EdgeExplorer edgeExplorer = ghGraph.createEdgeExplorer(EdgeFilter.ALL_EDGES);
        Queue<Integer> queue = new ArrayDeque<>();
        IntHashSet connectedCell = new IntHashSet(nodeSet.size());
        Iterator<IntCursor> iter;
        EdgeIterator edgeIterator;
//        byte[] buffer = new byte[10];
        while (!nodeSet.isEmpty()) {
            iter = nodeSet.iterator();
            int startNode = iter.next().value;
            queue.offer(startNode);
//            if(disconnectedCells.size() > PART__MAX_SUBCELL_NUMBER)
//                break;
            if (connectedCell.size() > getMinCellNodesNumber() && disconnectedCells.size() < MAX_SUBCELL_NUMBER) {
                connectedCell = new IntHashSet();
                disconnectedCells.add(connectedCell);
            }
            connectedCell.add(startNode);

            while (!queue.isEmpty()) {
                int currentNode = queue.poll();
//                connectedCell.add(currentNode);
                edgeIterator = edgeExplorer.setBaseNode(currentNode);

                while (edgeIterator.next()) {
//                    if (!((storage.getEdgeValue(edgeIterator.getEdge(), buffer) & AvoidFeatureFlags.Ferries) == 0))
//                        continue;
                    if (!edgeFilter.accept(edgeIterator))
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
//        if(disconnectedCells.size() > 1)
//            System.out.println("Separated cell " + cellId + " into number of subcells: " + disconnectedCells.size());
        return disconnectedCells;
    }
}
