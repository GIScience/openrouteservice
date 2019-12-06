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

import static heigit.ors.partitioning.FastIsochroneParameters.*;
import static heigit.ors.partitioning.Sort.sortByValueReturnList;

public class InertialFlow extends PartitioningBase {

    private enum Projection {  // Sortier-Projektionen der Koordinaten
        Line_p90
                // Projektion auf 90°
                {
                    public double sortValue(double lat, double lon) {
                        return lat;
                    }
                },
//        Line_p75
//                // Projektion auf 75°
//                {
//                    public double sortValue(double lat, double lon) {
//                        return lat + Math.tan(Math.toRadians(75)) * lon;
//                    }
//                },
        Line_p60
                // Projektion auf 60°: v.lat+tan(60°)*v.lon
                {
                    public double sortValue(double lat, double lon) {
                        return lat + Math.tan(Math.toRadians(60)) * lon;
                    }
                },
//        Line_p45
//                // Projektion auf 30°
//                {
//                    public double sortValue(double lat, double lon) {
//                        return lat + Math.tan(Math.toRadians(45)) * lon;
//                    }
//                },
        Line_p30
                // Projektion auf 30°
                {
                    public double sortValue(double lat, double lon) {
                        return lat + Math.tan(Math.toRadians(30)) * lon;
                    }
                },
//        Line_p15
//                // Projektion auf 30°
//                {
//                    public double sortValue(double lat, double lon) {
//                        return lat + Math.tan(Math.toRadians(15)) * lon;
//                    }
//                },
        Line_m00
                // Projektion auf 0°
                {
                    public double sortValue(double lat, double lon) {
                        return lon;
                    }
                },
//        Line_m15
//                // Projektion auf 30°
//                {
//                    public double sortValue(double lat, double lon) {
//                        return lat - Math.tan(Math.toRadians(15)) * lon;
//                    }
//                },
        Line_m30
                // Projektion auf -30°
                {
                    public double sortValue(double lat, double lon) {
                        return lat - Math.tan(Math.toRadians(30)) * lon;
                    }
                },
//        Line_m45
//                // Projektion auf -30°
//                {
//                    public double sortValue(double lat, double lon) {
//                        return lat - Math.tan(Math.toRadians(45)) * lon;
//                    }
//                },
        Line_m60
                // Projektion auf -60°
                {
                    public double sortValue(double lat, double lon) {
                        return lat - Math.tan(Math.toRadians(60)) * lon;
                    }
                };
//        Line_m75
//                // Projektion auf -30°
//                {
//                    public double sortValue(double lat, double lon) {
//                        return lat - Math.tan(Math.toRadians(75)) * lon;
//                    }
//                };

        abstract double sortValue(double lat, double lon);
    }

    private Map<Integer, IntHashSet> splitNodeSet;

    private static double[] bArray = new double[]{0.4};//, 0.27, 0.3, 0.33, 0.36, 0.39, 0.42, 0.45}; // somewhat between 0.25 and 0.45


    public InertialFlow(GraphHopperStorage ghStorage, EdgeFilterSequence edgeFilters) {
        super(ghStorage, edgeFilters);
        //Start cellId 1 so that bitshifting it causes no zeros at the front
        this.cellId = 1;
        this.splitNodeSet = new HashMap<>();
        this.ghGraph = ghStorage.getBaseGraph();

        initNodes();
        initAlgo();
    }

    private InertialFlow(int cellId, IntHashSet nodeSet, EdgeFilter edgeFilter) {
        this.cellId = cellId;
        this.nodeIdSet = new IntHashSet();

        for(IntCursor node : nodeSet){
            nodeIdSet.add(node.value);
        }
        this.splitNodeSet = new HashMap<>();
        this.edgeFilter = edgeFilter;
        this.ghGraph = ghStorage.getBaseGraph();

//        graphBiSplit();
//        saveResults();
//        recursion();



    }

    public void compute() {
        setAlgo();
        graphBiSplit();
        saveResults();
        recursion();
    }


    private void graphBiSplit() {
        int mincutScore = Integer.MAX_VALUE;
        IntHashSet mincutSrcSet;
        IntHashSet mincutSnkSet;

        //>> Loop through Projections and project each Node
        for (Projection proj : Projection.values()) {
            //>> sort projected Nodes
            Map<Integer, Double> tmpNodeProjMap = new HashMap<>();
            for (IntCursor nodeId : nodeIdSet) {
                tmpNodeProjMap.put(nodeId.value, proj.sortValue(ghStorage.getNodeAccess().getLatitude(nodeId.value), ghStorage.getNodeAccess().getLongitude(nodeId.value)));
            }
            IntArrayList tmpNodeList = sortByValueReturnList(tmpNodeProjMap, true);
//            tmpNodeProjMap = null;
            //>> loop through b-percentage Values to fetch Source and Sink Nodes
//            double aTmp = 0.0;
//            for (double bTmp : bArray) {
//            mincutAlgo.setMaxFlowLimit(mincutScore).initSubNetwork(aTmp, bTmp, tmpNodeList);
            mincutAlgo.setMaxFlowLimit(mincutScore).initSubNetwork(0d, FLOW__SET_SPLIT_VALUE, tmpNodeList);
            int cutScore = mincutAlgo.getMaxFlow();

//                if ((0 < cutScore) && (cutScore < mincutScore)) {
            if (cutScore < mincutScore) {
                //>> store Results
                mincutScore = cutScore;
                mincutSrcSet = mincutAlgo.getSrcPartition();
                mincutSnkSet = mincutAlgo.getSnkPartition();
//                    mincutEdgBaseSet = mincutAlgo.getMinCut();

                //>> get Data for next Recursion-Step
                splitNodeSet.put(0, mincutSrcSet);
                splitNodeSet.put(1, mincutSnkSet);
            }

//                aTmp = bTmp;
//            }
        }
        this.mincutAlgo = null;   //>> free Memory
    }

    private void saveResults() {
        //>> saving iteration results
        for (Map.Entry<Integer, IntHashSet> entry : splitNodeSet.entrySet()) {
            for(IntCursor node : entry.getValue())
                nodeToCellArr[node.value] = cellId << 1 | entry.getKey();
//            for (int node : entry.getValue())
//                nodeToCellArr[node] = cellId << 1 | entry.getKey();
        }
    }

    private void saveMultiCells(Set<IntHashSet> cells, int motherId) {
        //>> saving iteration results
//        System.out.println("Savind data after disconnecting cell " + motherId);
        Iterator<IntHashSet> iterator = cells.iterator();
        while (iterator.hasNext()){
            if(motherId << 1 > PART__MAX_SPLITTING_ITERATION)
                break;
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
        for (Map.Entry<Integer, IntHashSet> entry : splitNodeSet.entrySet()) {
            boolean nextRecursionLevel = false;

            //>> Condition
            if ((cellId < PART__MAX_SPLITTING_ITERATION) && (entry.getValue().size() > PART__MAX_CELL_NODES_NUMBER)){
                nextRecursionLevel = true;
            }
            if ((cellId < PART__MIN_SPLITTING_ITERATION))
                nextRecursionLevel = true;
            if (nextRecursionLevel == false && PART__SEPARATECONNECTED && (cellId < PART__MAX_SPLITTING_ITERATION)) {
//                System.out.println("Disconnecting cell " + (cellId << 1 | entry.getKey()));
                Set<IntHashSet> disconnectedCells = separateConnected(entry.getValue(), cellId << 1 | entry.getKey());
                saveMultiCells(disconnectedCells, cellId << 1 | entry.getKey());

            }
            //>> Execution
            if (nextRecursionLevel) {
                invokeNext[entry.getKey()] = true;
//                list.add(entry.getKey());
            }
        }
        if (invokeNext[0] == true && invokeNext[1] == true) {
            invokeAll(new InertialFlow(cellId << 1 | 0, splitNodeSet.get(0), this.edgeFilter),
                    new InertialFlow(cellId << 1 | 1, splitNodeSet.get(1), this.edgeFilter));
        }
        else if (invokeNext[0]) {
            invokeAll(new InertialFlow(cellId << 1 | 0, splitNodeSet.get(0), this.edgeFilter));
        }
        else if (invokeNext[1]) {
                invokeAll(new InertialFlow(cellId << 1 | 1, splitNodeSet.get(1), this.edgeFilter));
        }
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
        return disconnectedCells;
    }

//    public Geometry concHullOfNodes(Set<Integer> pointSet) {
//        NodeAccess nodeAccess = ghStorage.getNodeAccess();
//        GeometryFactory _geomFactory = new GeometryFactory();
//        Geometry[] geometries = new Geometry[pointSet.size()];
//        int g = 0;
//        for (int point : pointSet) {
//            Coordinate c = new Coordinate(nodeAccess.getLon(point), nodeAccess.getLat(point));
//            geometries[g++] = _geomFactory.createPoint(c);
//        }
//
//        GeometryCollection points = new GeometryCollection(geometries, _geomFactory);
//        ConcaveHull ch = new ConcaveHull(points, CONCAVEHULL_THRESHOLD, false);
//        Geometry geom = ch.getConcaveHull();
//
//        return geom;
//    }
//    /*
//Calculates the distance between two coordinates in meters
// */
//    public static double distance(double lat1, double lat2, double lon1,
//                                  double lon2) {
//
//        final int R = 6371; // Radius of the earth
//
//        double latDistance = Math.toRadians(lat2 - lat1);
//        double lonDistance = Math.toRadians(lon2 - lon1);
//        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
//                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        double distance = R * c * 1000; // convert to meters
//
//        distance = Math.pow(distance, 2);
//
//        return Math.sqrt(distance);
//    }

}
