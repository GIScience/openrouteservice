package heigit.ors.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntDoubleHashMap;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.vividsolutions.jts.geom.*;
import heigit.ors.fastisochrones.Contour;
import heigit.ors.routing.AvoidFeatureFlags;
import heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.opensphere.geometry.algorithm.ConcaveHull;

import java.util.*;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

import static heigit.ors.partitioning.FastIsochroneParameters.*;
import static heigit.ors.partitioning.InertialFlow.Projection.*;
import static heigit.ors.partitioning.Sort.sortByValueReturnList;

public class InertialFlow extends PartitioningBase {

    enum Projection {  // Sortier-Projektionen der Koordinaten
        Line_p90
                // Projektion auf 90°
                {
                    public double sortValue(double lat, double lon) {
                        return lat;
                    }
                },
        Line_p75
                // Projektion auf 75°
                {
                    public double sortValue(double lat, double lon) {
                        return lat + Math.tan(Math.toRadians(75)) * lon;
                    }
                },
        Line_p60
                // Projektion auf 60°: v.lat+tan(60°)*v.lon
                {
                    public double sortValue(double lat, double lon) {
                        return lat + Math.tan(Math.toRadians(60)) * lon;
                    }
                },
        Line_p45
                // Projektion auf 30°
                {
                    public double sortValue(double lat, double lon) {
                        return lat + Math.tan(Math.toRadians(45)) * lon;
                    }
                },
        Line_p30
                // Projektion auf 30°
                {
                    public double sortValue(double lat, double lon) {
                        return lat + Math.tan(Math.toRadians(30)) * lon;
                    }
                },
        Line_p15
                // Projektion auf 30°
                {
                    public double sortValue(double lat, double lon) {
                        return lat + Math.tan(Math.toRadians(15)) * lon;
                    }
                },
        Line_m00
                // Projektion auf 0°
                {
                    public double sortValue(double lat, double lon) {
                        return lon;
                    }
                },
        Line_m15
                // Projektion auf 30°
                {
                    public double sortValue(double lat, double lon) {
                        return lat - Math.tan(Math.toRadians(15)) * lon;
                    }
                },
        Line_m30
                // Projektion auf -30°
                {
                    public double sortValue(double lat, double lon) {
                        return lat - Math.tan(Math.toRadians(30)) * lon;
                    }
                },
        Line_m45
                // Projektion auf -30°
                {
                    public double sortValue(double lat, double lon) {
                        return lat - Math.tan(Math.toRadians(45)) * lon;
                    }
                },
        Line_m60
                // Projektion auf -60°
                {
                    public double sortValue(double lat, double lon) {
                        return lat - Math.tan(Math.toRadians(60)) * lon;
                    }
                },
        Line_m75
                // Projektion auf -30°
                {
                    public double sortValue(double lat, double lon) {
                        return lat - Math.tan(Math.toRadians(75)) * lon;
                    }
                };

        abstract double sortValue(double lat, double lon);
    }

    private Map<Projection, Projection> correspondingProjMap = new HashMap<>();


    //    private Map<Integer, IntHashSet> splitNodeSet;
    private IntHashSet partition0, partition1;

    private Map<Projection, IntArrayList> nodeListProjMap;
    private List<Projection> projOrder;
    private Map<Projection, Double> squareRangeProjMap;
    private Map<Projection, Double> orthogonalDiffProjMap;


    private static double[] bArray = new double[]{0.4};//, 0.27, 0.3, 0.33, 0.36, 0.39, 0.42, 0.45}; // somewhat between 0.25 and 0.45

    private PreparePartition.InverseSemaphore inverseSemaphore;

    public InertialFlow(GraphHopperStorage ghStorage, EdgeFilterSequence edgeFilters, ExecutorService executorService, PreparePartition.InverseSemaphore inverseSemaphore) {
        super(ghStorage, edgeFilters, executorService);
        //Start cellId 1 so that bitshifting it causes no zeros at the front
        this.cellId = 1;

        this.correspondingProjMap = new HashMap<>();
        this.correspondingProjMap.put(Line_p90, Line_m00);
        this.correspondingProjMap.put(Line_p75, Line_m15);
        this.correspondingProjMap.put(Line_p60, Line_m30);
        this.correspondingProjMap.put(Line_p45, Line_m45);
        this.correspondingProjMap.put(Line_p30, Line_m60);
        this.correspondingProjMap.put(Line_p15, Line_m75);
        this.correspondingProjMap.put(Line_m00, Line_p90);
        this.correspondingProjMap.put(Line_m15, Line_p75);
        this.correspondingProjMap.put(Line_m30, Line_p60);
        this.correspondingProjMap.put(Line_m45, Line_p45);
        this.correspondingProjMap.put(Line_m60, Line_p30);
        this.correspondingProjMap.put(Line_m75, Line_p15);

//        this.splitNodeSet = new HashMap<>();
        this.partition0 = new IntHashSet();
        this.partition1 = new IntHashSet();
        this.nodeListProjMap = new HashMap<>();
        this.projOrder = new ArrayList<>();
        this.squareRangeProjMap = new HashMap<>();
        this.orthogonalDiffProjMap = new HashMap<>();
        this.ghGraph = ghStorage.getBaseGraph();
        if(PART__DEBUG) System.out.println("Number of nodes: "+ghGraph.getNodes());
        if(PART__DEBUG) System.out.println("Number of edges: "+ghGraph.getAllEdges().getMaxId());
        this.inverseSemaphore = inverseSemaphore;

        initNodes();
        initAlgo();
    }

    private InertialFlow(int cellId, IntHashSet nodeSet, EdgeFilter edgeFilter, ExecutorService executorService, PreparePartition.InverseSemaphore inverseSemaphore) {
        setExecutorService(executorService);
        this.cellId = cellId;
        this.correspondingProjMap = new HashMap<>();
        this.correspondingProjMap.put(Line_p90, Line_m00);
        this.correspondingProjMap.put(Line_p75, Line_m15);
        this.correspondingProjMap.put(Line_p60, Line_m30);
        this.correspondingProjMap.put(Line_p45, Line_m45);
        this.correspondingProjMap.put(Line_p30, Line_m60);
        this.correspondingProjMap.put(Line_p15, Line_m75);
        this.correspondingProjMap.put(Line_m00, Line_p90);
        this.correspondingProjMap.put(Line_m15, Line_p75);
        this.correspondingProjMap.put(Line_m30, Line_p60);
        this.correspondingProjMap.put(Line_m45, Line_p45);
        this.correspondingProjMap.put(Line_m60, Line_p30);
        this.correspondingProjMap.put(Line_m75, Line_p15);

        this.nodeIdSet = new IntHashSet();
//        this.nodeIdSet = nodeSet;
        for(IntCursor node : nodeSet){
            nodeIdSet.add(node.value);
        }
        nodeSet = null;
//        this.splitNodeSet = new HashMap<>();
        this.edgeFilter = edgeFilter;
        this.partition0 = new IntHashSet();
        this.partition1 = new IntHashSet();
        this.nodeListProjMap = new HashMap<>();
        this.projOrder = new ArrayList<>();
        this.squareRangeProjMap = new HashMap<>();
        this.orthogonalDiffProjMap = new HashMap<>();
        this.ghGraph = ghStorage.getBaseGraph();
        this.inverseSemaphore = inverseSemaphore;

//        graphBiSplit();
//        saveResults();
//        recursion();



    }

    public void run() {
        try {
            setAlgo();
            prepareProjections();
            graphBiSplit();
            saveResults();
            recursion();
        }
        finally {
//            System.out.println("Ending task for cell " + (cellId));
            inverseSemaphore.taskCompleted();
        }

    }

    private void prepareProjections() {
        //>> Loop through linear combinations and project each Node
        for (Projection proj : Projection.values()) {
            //>> sort projected Nodes
            Double[] values = new Double[nodeIdSet.size()];
            Integer[] ids = IntStream.of( nodeIdSet.toArray() ).boxed().toArray( Integer[]::new );
            for(int i = 0; i < ids.length; i++)
                values[i] = proj.sortValue(ghStorage.getNodeAccess().getLatitude(ids[i]), ghStorage.getNodeAccess().getLongitude(ids[i]));
            nodeListProjMap.put(proj, sortByValueReturnList(ids, values));
            projOrder.add(proj);
        }

        //>> calculate Projection-Distances
        for (Projection proj : projOrder) {
            int idx = (int) (nodeListProjMap.get(proj).size() * FLOW__SET_SPLIT_VALUE);
            squareRangeProjMap.put(proj, projIndividualValue(proj, idx));
        }

        //>> combine inverse Projection-Distances
        for (Projection proj : projOrder) {
            orthogonalDiffProjMap.put(proj, projCombinedValue(proj));
        }

        //>> order Projections by Projection-Value
        projOrder = sortByValueReturnList(orthogonalDiffProjMap, false);
//            Info.debug(sortByValueReturnMap(orthogonalDiffProjMap, false) + " (" + cellId + ")");
    }


    private void graphBiSplit() {
        int mincutScore = nodeIdSet.size();
//        IntHashSet mincutSrcSet;
//        IntHashSet mincutSnkSet;

        //>> Loop through Projections and project each Node
        int i = 0;
        for (Projection proj : Projection.values()) {
            if(i == 3)
                break;
            //>> sort projected Nodes
            mincutAlgo.setMaxFlowLimit(mincutScore).initSubNetwork(0d, FLOW__SET_SPLIT_VALUE, nodeListProjMap.get(proj));
            mincutAlgo.setExplorationPreference(nodeListProjMap.get(proj));
            int cutScore = mincutAlgo.getMaxFlow();

            if (cutScore < mincutScore) {
                //>> store Results
                mincutScore = cutScore;
                //>> get Data for next Recursion-Step
                partition0 = mincutAlgo.getSrcPartition();
                partition1 = mincutAlgo.getSnkPartition();
            }

            i++;
        }
        this.mincutAlgo = null;   //>> free Memory
    }

    private void saveResults() {
        //>> saving iteration results
        for(IntCursor node : partition0)
            nodeToCellArr[node.value] = cellId << 1;
        for(IntCursor node : partition1)
            nodeToCellArr[node.value] = cellId << 1 | 1;
//        }
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

    private void recursion() {
        boolean[] invokeNext = new boolean[2];
//        for (Map.Entry<Integer, IntHashSet> entry : splitNodeSet.entrySet()) {
        boolean nextRecursionLevel = false;

        if ((cellId < PART__MAX_SPLITTING_ITERATION) && (partition0.size() > PART__MAX_CELL_NODES_NUMBER)){
            nextRecursionLevel = true;
        }
        if ((cellId < PART__MIN_SPLITTING_ITERATION))
            nextRecursionLevel = true;
        if (nextRecursionLevel == false && PART__SEPARATECONNECTED && (cellId < PART__MAX_SPLITTING_ITERATION)) {
            Set<IntHashSet> disconnectedCells = separateConnected(partition0, cellId << 1);
            saveMultiCells(disconnectedCells, cellId << 1);
        }
        if (nextRecursionLevel) {
            invokeNext[0] = true;
        }

        nextRecursionLevel = false;

        if ((cellId < PART__MAX_SPLITTING_ITERATION) && (partition1.size() > PART__MAX_CELL_NODES_NUMBER)){
            nextRecursionLevel = true;
        }
        if ((cellId < PART__MIN_SPLITTING_ITERATION))
            nextRecursionLevel = true;
        if (nextRecursionLevel == false && PART__SEPARATECONNECTED && (cellId < PART__MAX_SPLITTING_ITERATION)) {
            Set<IntHashSet> disconnectedCells = separateConnected(partition1, cellId << 1 | 1);
            saveMultiCells(disconnectedCells, cellId << 1 | 1);
        }
        if (nextRecursionLevel) {
            invokeNext[1] = true;
        }
//        }
        if (invokeNext[0] == true && invokeNext[1] == true) {
            if(nodeIdSet.size() > PART__MAX_CELL_NODES_NUMBER * 10) {
//                System.out.println("Submitting task for cell " + (cellId << 1 | 0));
                inverseSemaphore.beforeSubmit();
                executorService.execute(new InertialFlow(cellId << 1 | 0, partition0, this.edgeFilter, executorService, inverseSemaphore));
//                System.out.println("Submitting task for cell " + (cellId << 1 | 1));

                inverseSemaphore.beforeSubmit();
                executorService.execute(new InertialFlow(cellId << 1 | 1, partition1, this.edgeFilter, executorService, inverseSemaphore));
            }
            else{
                inverseSemaphore.beforeSubmit();
                InertialFlow inertialFlow = new InertialFlow(cellId << 1 | 0, partition0, this.edgeFilter, executorService, inverseSemaphore);
                inertialFlow.run();
                inverseSemaphore.beforeSubmit();
                inertialFlow = new InertialFlow(cellId << 1 | 1, partition1, this.edgeFilter, executorService, inverseSemaphore);
                inertialFlow.run();
            }
        }
        else if (invokeNext[0]) {
            if(nodeIdSet.size() > PART__MAX_CELL_NODES_NUMBER * 10) {
                inverseSemaphore.beforeSubmit();
//                System.out.println("Submitting task for cell " + (cellId << 1 | 0));

                executorService.execute(new InertialFlow(cellId << 1 | 0, partition0, this.edgeFilter, executorService, inverseSemaphore));
            }
            else {
                inverseSemaphore.beforeSubmit();
                InertialFlow inertialFlow = new InertialFlow(cellId << 1 | 0, partition0, this.edgeFilter, executorService, inverseSemaphore);
                inertialFlow.run();
            }
        }
        else if (invokeNext[1]) {
            if(nodeIdSet.size() > PART__MAX_CELL_NODES_NUMBER * 10) {

                inverseSemaphore.beforeSubmit();
                executorService.execute(new InertialFlow(cellId << 1 | 1, partition1, this.edgeFilter, executorService, inverseSemaphore));
            }
            else {
                inverseSemaphore.beforeSubmit();
                InertialFlow inertialFlow = new InertialFlow(cellId << 1 | 1, partition1, this.edgeFilter, executorService, inverseSemaphore);
                inertialFlow.run();
            }
        }
    }

    private double projIndividualValue(Projection proj, int idx) {
        IntArrayList  tmpNodeList;
        double fromLat, fromLon, toLat, toLon;

        tmpNodeList = nodeListProjMap.get(proj);
        toLat = ghStorage.getNodeAccess().getLatitude(tmpNodeList.get(idx));
        toLon = ghStorage.getNodeAccess().getLongitude(tmpNodeList.get(idx));
        fromLat = ghStorage.getNodeAccess().getLatitude(tmpNodeList.get(tmpNodeList.size() - idx - 1));
        fromLon = ghStorage.getNodeAccess().getLongitude(tmpNodeList.get(tmpNodeList.size() - idx - 1));

        return Contour.distance(fromLat, toLat, fromLon, toLon);
    }

    private double projCombinedValue(Projection proj) {
        return squareRangeProjMap.get(proj) * squareRangeProjMap.get(proj) / squareRangeProjMap.get(correspondingProjMap.get(proj));
    }


    private boolean isMalformed(Set<Integer> nodeSet){

//        Geometry geom = concHullOfNodes(nodeSet);
//        if (geom.getNumPoints() < 3) {
//            return false;
//        }
//
//            Polygon poly = (Polygon) geom;
//        LineString ring = poly.getExteriorRing();
//
//        for (int i = 0; i < ring.getNumPoints() - 1; i++) {
//            //COORDINATE OF POLYGON BASED
//            double dist = distance(ring.getPointN(i).getY(),
//                    ring.getPointN(i + 1).getY(),
//                    ring.getPointN(i).getX(),
//                    ring.getPointN(i + 1).getX());
//            if (dist > 3000){
//                return true;
//            }
//        }

        return false;
    }
    /*
    Identify disconnected parts of a cell so that they can be split
     */
    private Set<IntHashSet> separateConnected(IntHashSet nodeSet, int cellId){
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

            if (connectedCell.size() > PART__MIN_CELL_NODES_NUMBER)
                connectedCell = new IntHashSet();
            connectedCell.add(startNode);

            while (!queue.isEmpty()) {
                int currentNode = queue.poll();
//                connectedCell.add(currentNode);
                edgeIterator = edgeExplorer.setBaseNode(currentNode);

                while (edgeIterator.next()) {
//                    if (!((storage.getEdgeValue(edgeIterator.getEdge(), buffer) & AvoidFeatureFlags.Ferries) == 0))
//                        continue;
                    int nextNode = edgeIterator.getAdjNode();
                    if (connectedCell.contains(nextNode)
                            || !nodeSet.contains(nextNode))
                        continue;
                    queue.offer(nextNode);
                    connectedCell.add(nextNode);
                }
            }
            disconnectedCells.add(connectedCell);

            nodeSet.removeAll(connectedCell);
        }
//        if(disconnectedCells.size() > 1)
//            System.out.println("Separated cell " + cellId + " into number of subcells: " + disconnectedCells.size());
        return disconnectedCells;
    }

}
