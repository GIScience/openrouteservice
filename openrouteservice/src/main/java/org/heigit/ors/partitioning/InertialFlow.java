package org.heigit.ors.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import org.heigit.ors.fastisochrones.Contour;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

import static org.heigit.ors.partitioning.FastIsochroneParameters.*;
import static org.heigit.ors.partitioning.Sort.sortByValueReturnList;

/**
 * Recursive implementation of InertialFlow algorithm for partitioning a graph.
 * <p>
 *
 * @author Hendrik Leuschner
 */

public class InertialFlow extends PartitioningBase {
    private PreparePartition.InverseSemaphore inverseSemaphore;

    public InertialFlow(GraphHopperStorage ghStorage, PartitioningData pData, EdgeFilterSequence edgeFilters, ExecutorService executorService, PreparePartition.InverseSemaphore inverseSemaphore) {
        super(ghStorage, pData, edgeFilters, executorService);
        //Start cellId 1 so that bitshifting it causes no zeros at the front
        this.cellId = 1;
        prepareProjectionMaps();
        this.projOrder = new ArrayList<>();
        this.ghGraph = ghStorage.getBaseGraph();
        if(PART__DEBUG) System.out.println("Number of nodes: "+ghGraph.getNodes());
        if(PART__DEBUG) System.out.println("Number of edges: "+ghGraph.getAllEdges().length());
        this.inverseSemaphore = inverseSemaphore;

        initNodes();
        initAlgo();
        this.projections = calculateProjections();
        this.projOrder = calculateProjectionOrder(this.projections);
    }

    private InertialFlow(int cellId, GraphHopperStorage ghStorage, int[] nodeToCellArr, PartitioningData pData, IntHashSet nodeSet, Map<Projection, IntArrayList> projections, EdgeFilter edgeFilter, ExecutorService executorService, PreparePartition.InverseSemaphore inverseSemaphore) {
        setExecutorService(executorService);
        this.pData = pData;
        this.cellId = cellId;
        this.ghStorage = ghStorage;
        this.ghGraph = ghStorage.getBaseGraph();
        this.nodeToCellArr = nodeToCellArr;
        this.inverseSemaphore = inverseSemaphore;
        this.edgeFilter = edgeFilter;
        this.projOrder = new ArrayList<>();
        this.projections = projections;

        prepareProjectionMaps();

        this.nodeIdSet = new IntHashSet();
        for(IntCursor node : nodeSet){
            nodeIdSet.add(node.value);
        }

        this.projOrder = calculateProjectionOrder(projections);

    }

    public void run() {
        try {
            setAlgo();
            BiPartition biPartition = graphBiSplit(this.projections);
            saveResults(biPartition);
            BiPartitionProjection biPartitionProjection = prepareProjections(this.projections, biPartition);
            this.projections = null;
            recursion(biPartition, biPartitionProjection);
        }
        finally {
            inverseSemaphore.taskCompleted();
        }

    }

    private Map<Projection, IntArrayList> calculateProjections() {
        //>> Loop through linear combinations and project each Node
        Map<Projection, IntArrayList> nodeListProjMap = new HashMap<>(Projection.values().length);

        for (Projection proj : Projection.values()) {
            //>> sort projected Nodes
            Double[] values = new Double[nodeIdSet.size()];
            Integer[] ids = IntStream.of( nodeIdSet.toArray() ).boxed().toArray( Integer[]::new );

            for(int i = 0; i < ids.length; i++) {
                values[i] = proj.sortValue(ghStorage.getNodeAccess().getLatitude(ids[i]), ghStorage.getNodeAccess().getLongitude(ids[i]));
            }
            nodeListProjMap.put(proj, sortByValueReturnList(ids, values, this.cellId));

            projOrder.add(proj);
        }
        return nodeListProjMap;
    }

    private BiPartitionProjection prepareProjections(Map<Projection, IntArrayList> originalProjections, BiPartition biPartition) {
        IntHashSet part0 = biPartition.getPartition0();
        Map<Projection, IntArrayList> projections0 = new HashMap<>(Projection.values().length);
        Map<Projection, IntArrayList> projections1 = new HashMap<>(Projection.values().length);
        int origNodeCount = originalProjections.get(Projection.values()[0]).size();
        //Add initial lists
        for(Projection proj : Projection.values()){
            projections0.put(proj, new IntArrayList(origNodeCount / 3));
            projections1.put(proj, new IntArrayList(origNodeCount / 3));
        }

        //Go through the original projections and separate each into two projections for the subsets, maintaining order
        for(int i = 0; i < origNodeCount; i++){
            for(Projection proj : originalProjections.keySet()){
                int node = originalProjections.get(proj).get(i);
                if(part0.contains(node))
                    projections0.get(proj).add(node);
                else
                    projections1.get(proj).add(node);
            }
        }

        return new BiPartitionProjection(projections0, projections1);
    }

    private List<Projection> calculateProjectionOrder(Map<Projection, IntArrayList> projections){
        List<Projection> order;
        Map<Projection, Double> squareRangeProjMap =  new HashMap<>();
        Map<Projection, Double> orthogonalDiffProjMap = new HashMap<>();
        //>> calculate Projection-Distances
        for (Projection proj : projections.keySet()) {
            int idx = (int) (projections.get(proj).size() * FLOW__SET_SPLIT_VALUE);
            squareRangeProjMap.put(proj, projIndividualValue(projections, proj, idx));
        }

        //>> combine inverse Projection-Distances
        for (Projection proj : projections.keySet()) {
            orthogonalDiffProjMap.put(proj, projCombinedValue(squareRangeProjMap, proj));
        }

        //>> order Projections by Projection-Value
        order = sortByValueReturnList(orthogonalDiffProjMap, false);
        return order;
    }


    private BiPartition graphBiSplit(Map<Projection, IntArrayList> nodeListProjMap) {
        //Estimated maximum iterations
        int mincutScore = ghGraph.getBaseGraph().getAllEdges().length();
        double sizeFactor = ((double)nodeIdSet.size()) / ghGraph.getBaseGraph().getNodes();
        mincutScore = (int)Math.ceil(mincutScore * sizeFactor);
        IntHashSet part0 = new IntHashSet(nodeIdSet.size() / 3);
        IntHashSet part1 = new IntHashSet(nodeIdSet.size() / 3);
        MaxFlowMinCut maxFlowMinCut = this.cellId == 1 ? initAlgo() : setAlgo();
        //>> Loop through Projections and project each Node
        int i = 0;
        for (Projection proj : projOrder) {
            //Try only best projections
            if(i == FLOW__CONSIDERED_PROJECTIONS)
                break;
            //>> sort projected Nodes
            maxFlowMinCut.setOrderedNodes(nodeListProjMap.get(proj));
            maxFlowMinCut.setNodeOrder();
            maxFlowMinCut.setMaxFlowLimit(mincutScore).initSubNetwork();
            int cutScore = maxFlowMinCut.getMaxFlow();
            if (cutScore < mincutScore) {
                //>> store Results
                mincutScore = cutScore;
                //>> get Data for next Recursion-Step
                part0 = maxFlowMinCut.getSrcPartition();
                part1 = maxFlowMinCut.getSnkPartition();
            }
            i++;
        }
        return new BiPartition(part0, part1);
    }

    private void saveResults(BiPartition biPartition) {
        //>> saving iteration results
        for(IntCursor node : biPartition.getPartition0())
            nodeToCellArr[node.value] = cellId << 1;
        for(IntCursor node : biPartition.getPartition1())
            nodeToCellArr[node.value] = cellId << 1 | 1;
    }

    private void saveMultiCells(Set<IntHashSet> cells, int motherId) {
        //>> saving iteration results
        Iterator<IntHashSet> iterator = cells.iterator();
        while (iterator.hasNext()){
            IntHashSet cell = iterator.next();
            for (IntCursor node : cell){
                nodeToCellArr[node.value] = motherId << 1;
            }
            if (iterator.hasNext()){
                cell = iterator.next();
                for (IntCursor node : cell){
                    nodeToCellArr[node.value] = motherId << 1 | 1;
                }
            }
            motherId = motherId << 1;
        }
    }

    private void recursion(BiPartition biPartition, BiPartitionProjection biPartitionProjection) {
        boolean[] invokeNext = new boolean[2];
        boolean nextRecursionLevel = false;

        if ((cellId < PART__MAX_SPLITTING_ITERATION) && (biPartition.getPartition0().size() > PART__MAX_CELL_NODES_NUMBER)){
            nextRecursionLevel = true;
        }
        if ((cellId < PART__MIN_SPLITTING_ITERATION))
            nextRecursionLevel = true;
        if (nextRecursionLevel == false && PART__SEPARATEDISCONNECTED && (cellId < PART__MAX_SPLITTING_ITERATION)) {
            Set<IntHashSet> disconnectedCells = separateDisconnected(biPartition.getPartition0(), cellId << 1);
            saveMultiCells(disconnectedCells, cellId << 1);
        }
        if (nextRecursionLevel) {
            invokeNext[0] = true;
        }

        nextRecursionLevel = false;

        if ((cellId < PART__MAX_SPLITTING_ITERATION) && (biPartition.getPartition1().size() > PART__MAX_CELL_NODES_NUMBER)){
            nextRecursionLevel = true;
        }
        if ((cellId < PART__MIN_SPLITTING_ITERATION))
            nextRecursionLevel = true;
        if (nextRecursionLevel == false && PART__SEPARATEDISCONNECTED && (cellId < PART__MAX_SPLITTING_ITERATION)) {
            Set<IntHashSet> disconnectedCells = separateDisconnected(biPartition.getPartition1(), cellId << 1 | 1);
            saveMultiCells(disconnectedCells, cellId << 1 | 1);
        }
        if (nextRecursionLevel) {
            invokeNext[1] = true;
        }
//        }
        if (invokeNext[0] == true && invokeNext[1] == true) {
            if(nodeIdSet.size() > PART__MAX_CELL_NODES_NUMBER * 4) {
//                System.out.println("Submitting task for cell " + (cellId << 1 | 0));
                inverseSemaphore.beforeSubmit();
                executorService.execute(new InertialFlow(cellId << 1 | 0, ghStorage, nodeToCellArr, pData, biPartition.getPartition0(), biPartitionProjection.getProjection0(), this.edgeFilter, executorService, inverseSemaphore));
//                System.out.println("Submitting task for cell " + (cellId << 1 | 1));

                inverseSemaphore.beforeSubmit();
                executorService.execute(new InertialFlow(cellId << 1 | 1, ghStorage, nodeToCellArr, pData, biPartition.getPartition1(), biPartitionProjection.getProjection1(), this.edgeFilter, executorService, inverseSemaphore));
            }
            else{
                inverseSemaphore.beforeSubmit();
                InertialFlow inertialFlow = new InertialFlow(cellId << 1 | 0, ghStorage, nodeToCellArr, pData, biPartition.getPartition0(), biPartitionProjection.getProjection0(), this.edgeFilter, executorService, inverseSemaphore);
                inertialFlow.run();
                inverseSemaphore.beforeSubmit();
                inertialFlow = new InertialFlow(cellId << 1 | 1, ghStorage, nodeToCellArr, pData, biPartition.getPartition1(), biPartitionProjection.getProjection1(), this.edgeFilter, executorService, inverseSemaphore);
                inertialFlow.run();
            }
        }
        else if (invokeNext[0]) {
            if(nodeIdSet.size() > PART__MAX_CELL_NODES_NUMBER * 4) {
                inverseSemaphore.beforeSubmit();
//                System.out.println("Submitting task for cell " + (cellId << 1 | 0));

                executorService.execute(new InertialFlow(cellId << 1 | 0, ghStorage, nodeToCellArr, pData, biPartition.getPartition0(), biPartitionProjection.getProjection0(), this.edgeFilter, executorService, inverseSemaphore));
            }
            else {
                inverseSemaphore.beforeSubmit();
                InertialFlow inertialFlow = new InertialFlow(cellId << 1 | 0, ghStorage, nodeToCellArr, pData, biPartition.getPartition0(), biPartitionProjection.getProjection0(), this.edgeFilter, executorService, inverseSemaphore);
                inertialFlow.run();
            }
        }
        else if (invokeNext[1]) {
            if(nodeIdSet.size() > PART__MAX_CELL_NODES_NUMBER * 4) {

                inverseSemaphore.beforeSubmit();
                executorService.execute(new InertialFlow(cellId << 1 | 1, ghStorage, nodeToCellArr, pData, biPartition.getPartition1(), biPartitionProjection.getProjection1(), this.edgeFilter, executorService, inverseSemaphore));
            }
            else {
                inverseSemaphore.beforeSubmit();
                InertialFlow inertialFlow = new InertialFlow(cellId << 1 | 1, ghStorage, nodeToCellArr, pData, biPartition.getPartition1(), biPartitionProjection.getProjection1(), this.edgeFilter, executorService, inverseSemaphore);
                inertialFlow.run();
            }
        }
    }

    private double projIndividualValue(Map<Projection, IntArrayList> projMap, Projection proj, int idx) {
        IntArrayList  tmpNodeList;
        double fromLat, fromLon, toLat, toLon;

        tmpNodeList = projMap.get(proj);
        toLat = ghStorage.getNodeAccess().getLatitude(tmpNodeList.get(idx));
        toLon = ghStorage.getNodeAccess().getLongitude(tmpNodeList.get(idx));
        fromLat = ghStorage.getNodeAccess().getLatitude(tmpNodeList.get(tmpNodeList.size() - idx - 1));
        fromLon = ghStorage.getNodeAccess().getLongitude(tmpNodeList.get(tmpNodeList.size() - idx - 1));

        return Contour.distance(fromLat, toLat, fromLon, toLon);
    }

    private double projCombinedValue(Map<Projection, Double> squareRangeProjMap, Projection proj) {
        return squareRangeProjMap.get(proj) * squareRangeProjMap.get(proj) / squareRangeProjMap.get(correspondingProjMap.get(proj));
    }

    /*
    Identify disconnected parts of a cell so that they can be split
     */
    private Set<IntHashSet> separateDisconnected(IntHashSet nodeSet, int cellId){
        Set<IntHashSet> disconnectedCells = new HashSet<>();
        EdgeExplorer edgeExplorer = ghGraph.createEdgeExplorer(EdgeFilter.ALL_EDGES);
        Queue<Integer> queue = new ArrayDeque<>();
        IntHashSet connectedCell = new IntHashSet(nodeSet.size());
        Iterator<IntCursor> iter;
        EdgeIterator edgeIterator;
//        byte[] buffer = new byte[10];
        while(!nodeSet.isEmpty()) {
            iter = nodeSet.iterator();
            int startNode = iter.next().value;
            queue.offer(startNode);
//            if(disconnectedCells.size() > PART__MAX_SUBCELL_NUMBER)
//                break;
            if (connectedCell.size() > PART__MIN_CELL_NODES_NUMBER && disconnectedCells.size() < PART__MAX_SUBCELL_NUMBER) {
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
                    if(!edgeFilter.accept(edgeIterator))
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
