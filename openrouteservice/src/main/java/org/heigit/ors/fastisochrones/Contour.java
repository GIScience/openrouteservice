package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.*;
import org.heigit.ors.partitioning.CellStorage;
import org.heigit.ors.partitioning.IsochroneNodeStorage;
import org.opensphere.geometry.algorithm.ConcaveHull;

import java.util.*;

import static org.heigit.ors.partitioning.FastIsochroneParameters.CONCAVEHULL_THRESHOLD;

public class Contour {

    private Map<String, Set<Coordinate>> bufferCoordsMap = new HashMap<>();
    public enum ContourCalc {hull_v1}

    private IsochroneNodeStorage isochroneNodeStorage;
    private CellStorage cellStorage;
    protected NodeAccess nodeAccess;
    protected GraphHopperStorage ghStorage;
    private int minEdgeLengthLimit = 300;
    private int maxEdgeLengthLimit = Integer.MAX_VALUE;
    private int approxLowNodeCountLimit = 10;

    public Contour(GraphHopperStorage ghStorage, NodeAccess nodeAccess, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage){
        this.ghStorage = ghStorage;
        this.nodeAccess = nodeAccess;
        this.isochroneNodeStorage = isochroneNodeStorage;
        this.cellStorage = cellStorage;
    }


    public void calcCellContourPre() {

        Set<Integer> cellNodes = new HashSet<>();
        List<Integer> contourOrder = new ArrayList<>();
        List<Double> hullLatitudes = new ArrayList<>();
        List<Double> hullLongitudes = new ArrayList<>();

        for (int cellId : isochroneNodeStorage.getCellIds()) {
            LineString ring;
            contourOrder.clear();
            hullLatitudes.clear();
            hullLongitudes.clear();
            cellNodes = cellStorage.getNodesOfCell(cellId);

            EdgeExplorer explorer = ghStorage.getBaseGraph().createEdgeExplorer(EdgeFilter.ALL_EDGES);
            EdgeIterator iter;
            List<Double> lats = new ArrayList<>(cellNodes.size());
            List<Double> longs = new ArrayList<>(cellNodes.size());

            Set<Integer> visitedEdges = new HashSet<>();
            for (int node : cellNodes){
                iter = explorer.setBaseNode(node);
                while (iter.next()){
                    if(!cellNodes.contains(iter.getAdjNode()))
                        continue;
                    if(visitedEdges.contains(iter.getEdge()))
                        continue;
                    visitedEdges.add(iter.getEdge());
                    checkAddLatLon(iter.fetchWayGeometry(3), lats, longs);
//                    allNodes.add(iter.fetchWayGeometry(1));
                }

            }


//            Geometry geom = lats.size() < approxLowNodeCountLimit ? concHullOfNodes(cellNodes) : concHullOfNodes(lats, longs);
            Geometry geom;
            try {
                geom = concHullOfNodes(lats, longs);
//                if (geom.getNumPoints() > 2) {
                    Polygon poly = (Polygon) geom;
                    ring = poly.getExteriorRing();
                    poly.normalize();
//                }
            }
            catch (Exception e){
//                System.out.println("Failed to create concave hull for cell " + cellId);
                cellStorage.setCellContourOrder(cellId, new ArrayList<>(), new ArrayList<>());
                continue;
            }
//            if (geom.getNumPoints() > 2 && !(geom instanceof MultiPoint)) {
//                Polygon poly = (Polygon) geom;
//                ring = poly.getExteriorRing();
//                poly.normalize();
//            } else {
//                cellStorage.setCellContourOrder(cellId, new ArrayList<>(), new ArrayList<>());
//                continue;
//            }
            for (int i = 0; i < ring.getNumPoints(); i++) {
                //COORDINATE OF POLYGON BASED
                hullLatitudes.add(ring.getPointN(i).getY());
                hullLongitudes.add(ring.getPointN(i).getX());

                if(i < ring.getNumPoints() -1) {
                    splitEdge(ring.getPointN(i).getY(),
                            ring.getPointN(i + 1).getY(),
                            ring.getPointN(i).getX(),
                            ring.getPointN(i + 1).getX(),
                            hullLatitudes,
                            hullLongitudes,
                            minEdgeLengthLimit,
                            maxEdgeLengthLimit);
                }
            }

            cellStorage.setCellContourOrder(cellId, new ArrayList<>(hullLatitudes), new ArrayList<>(hullLongitudes));


        }
        cellStorage.storeContourPointerMap();
        cellStorage.flush();
    }


    public  Geometry concHullOfNodes(Set<Integer> pointSet) {

        NodeAccess nodeAccess = ghStorage.getNodeAccess();
        GeometryFactory _geomFactory = new GeometryFactory();
        Geometry[] geometries = new Geometry[pointSet.size()];

        if(pointSet.size() <10 ){
            Coordinate[] coordinates = new Coordinate[pointSet.size()];
            int i = 0;
            for(int point : pointSet) {
                coordinates[i] = new Coordinate(nodeAccess.getLon(point), nodeAccess.getLat(point));
                i++;
            }
            Geometry geom = _geomFactory.createMultiPoint(coordinates);
            return geom;
        }

        int g = 0;
        for (int point : pointSet) {
            Coordinate c = new Coordinate(nodeAccess.getLon(point), nodeAccess.getLat(point));
            geometries[g++] = _geomFactory.createPoint(c);
        }

        GeometryCollection points = new GeometryCollection(geometries, _geomFactory);
        ConcaveHull ch = new ConcaveHull(points, CONCAVEHULL_THRESHOLD, false);
        Geometry geom = ch.getConcaveHull();

        return geom;
    }

    public  Geometry concHullOfNodes(List<Double> lats, List<Double> lons) {
        if(lats.size() != lons.size())
            throw new IllegalStateException("Latitudes and Longitudes must be same size");
        GeometryFactory _geomFactory = new GeometryFactory();
        int size = lats.size();
        Geometry[] geometries = new Geometry[lats.size()];
        int g = 0;
        for (int i = 0; i < size; i++) {
            Coordinate c = new Coordinate(lons.get(i), lats.get(i));
            geometries[g++] = _geomFactory.createPoint(c);
        }

        GeometryCollection points = new GeometryCollection(geometries, _geomFactory);
        ConcaveHull ch = new ConcaveHull(points, CONCAVEHULL_THRESHOLD, false);
        Geometry geom = ch.getConcaveHull();

        return geom;
    }


    public Contour setGhStorage(GraphHopperStorage ghStorage) {
        this.ghStorage = ghStorage;
        return this;
    }

    /*
    Calculates the distance between two coordinates in meters
     */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }

    /*
    iteratively splits a distance between two coordinates if theyre above a certain limit
     */
    private void splitEdge(double lat0, double lat1, double lon0, double lon1, List<Double> latitudes, List<Double> longitudes, double minlim, double maxlim){
        double dist = distance(lat0, lat1, lon0, lon1);
        if (dist > minlim && dist < maxlim) {
            latitudes.add((lat0 + lat1) / 2);
            longitudes.add((lon0 + lon1) / 2);

            splitEdge(lat0, (lat0 + lat1) / 2, lon0, (lon0 + lon1) / 2, latitudes, longitudes, minlim, maxlim);
            splitEdge((lat0 + lat1) / 2, lat1, (lon0 + lon1) / 2, lon1, latitudes, longitudes, minlim, maxlim);
        }
    }

    /**
     * G-E-T
     **/
    protected double getLat(int nodeId){
        return nodeAccess.getLat(nodeId);
    }

    protected double getLon(int nodeId){
        return nodeAccess.getLon(nodeId);
    }

    private void checkAddLatLon(PointList newCoordinates, List<Double> existingLats, List<Double> existingLons){
        for (int i = 0; i < newCoordinates.size(); i++) {
            int latIndex = existingLats.indexOf(newCoordinates.getLat(i));
            //point is not yet in list
            if(latIndex == -1){
                existingLats.add(newCoordinates.getLat(i));
                existingLons.add(newCoordinates.getLon(i));
                continue;
            }
            //The coordinate is already added
            if(newCoordinates.getLon(i) == existingLons.get(latIndex))
                continue;

            existingLats.add(newCoordinates.getLat(i));
            existingLons.add(newCoordinates.getLon(i));
        }
    }
}
