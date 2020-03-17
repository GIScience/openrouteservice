package org.heigit.ors.fastisochrones;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import org.heigit.ors.isochrones.builders.concaveballs.PointItemVisitor;
import org.heigit.ors.partitioning.CellStorage;
import org.heigit.ors.partitioning.IsochroneNodeStorage;
import org.opensphere.geometry.algorithm.ConcaveHull;

import java.util.*;
import java.util.stream.Collectors;

import static org.heigit.ors.partitioning.FastIsochroneParameters.*;

/**
 * Calculates Outlines (Contour) of cells.
 * Contours are concave hulls of a given set of points.
 * Additionally, contours for supercells can be created.
 * <p>
 *
 * @author Hendrik Leuschner
 */

public class Contour {
    private IsochroneNodeStorage isochroneNodeStorage;
    private CellStorage cellStorage;
    protected NodeAccess nodeAccess;
    protected GraphHopperStorage ghStorage;
    private int minEdgeLengthLimit = 400;
    private int maxEdgeLengthLimit = Integer.MAX_VALUE;

    public Contour(GraphHopperStorage ghStorage, NodeAccess nodeAccess, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage){
        this.ghStorage = ghStorage;
        this.nodeAccess = nodeAccess;
        this.isochroneNodeStorage = isochroneNodeStorage;
        this.cellStorage = cellStorage;
    }

    public void calcCellContourPre() {
        for (IntCursor cellId : isochroneNodeStorage.getCellIds()) {
            LineString ring = createContour(createCoordinates(cellId.value));
            if (ring == null || ring.getNumPoints() < 2) {
                cellStorage.setCellContourOrder(cellId.value, new ArrayList<>(), new ArrayList<>());
                continue;
            }
            storeContour(cellId.value, ring);
        }

        cellStorage.flush();
        IntObjectMap<IntHashSet> superCells;
        if(CONTOUR__USE_SUPERCELLS) {
            //Create supercells for better querytime performance
            //Current implementation supports 2 levels of supercells. Calculated individually
            //For each super(super)cell, we need to know the corresponding basecells (to get the contour from storage)
            //and the corresponding subcells (these are supercells for supersupercells)
            superCells = identifySuperCells(isochroneNodeStorage.getCellIds(), PART_SUPERCELL_HIERARCHY_LEVEL, true);
            IntHashSet superCellIds = new IntHashSet();
            superCellIds.addAll(superCells.keys());
            IntObjectMap<IntHashSet> superSuperCells = identifySuperCells(superCellIds, 2, false);
            IntObjectMap<IntHashSet> superSuperCellsToBaseCells = new IntObjectHashMap<>();
            for(IntObjectCursor<IntHashSet> superSuperCell : superSuperCells){
                IntHashSet newSuperCell = new IntHashSet();
                for (IntCursor cell : superSuperCell.value)
                    newSuperCell.addAll(superCells.get(cell.value));
                superSuperCellsToBaseCells.put(superSuperCell.key, newSuperCell);
            }
            superSuperCellsToBaseCells.putAll(superCells);
            superCells.putAll(superSuperCells);

            //Calculate the concave hull for all super(super)cells
            for (IntObjectCursor<IntHashSet> superCell : superSuperCellsToBaseCells) {

                List<Coordinate> superCellCoordinates = createSuperCellCoordinates(superCell.value);

                LineString ring = createContour(superCellCoordinates);
                if (ring == null || ring.getNumPoints() < 2) {
                    cellStorage.setCellContourOrder(superCell.key, new ArrayList<>(), new ArrayList<>());
                    continue;
                }

                storeContour(superCell.key, ring);

//                List<Double> superCellContourLats = new ArrayList<>();
//                List<Double> superCellContourLongs = new ArrayList<>();
//                for (int i = 0; i < ring.getNumPoints(); i++) {
//                    //COORDINATE OF POLYGON BASED
//                    superCellContourLats.add(ring.getPointN(i).getY());
//                    superCellContourLongs.add(ring.getPointN(i).getX());
//
//                    if (i < ring.getNumPoints() - 1) {
//                        splitEdge(ring.getPointN(i).getY(),
//                                ring.getPointN(i + 1).getY(),
//                                ring.getPointN(i).getX(),
//                                ring.getPointN(i + 1).getX(),
//                                superCellContourLats,
//                                superCellContourLongs,
//                                minEdgeLengthLimit,
//                                maxEdgeLengthLimit);
//                    }
//                }
//                cellStorage.setCellContourOrder(superCell.key, new ArrayList<>(superCellContourLats), new ArrayList<>(superCellContourLongs));
            }
        }

        cellStorage.storeContourPointerMap();

        //Store the supercell->cellIds map
        if(CONTOUR__USE_SUPERCELLS)
            cellStorage.storeSuperCells(superCells);
        cellStorage.setContourPrepared(true);
        cellStorage.flush();
    }

    private List<Coordinate> createSuperCellCoordinates(IntHashSet superCell) {
        List<Coordinate> superCellCoordinates = new ArrayList<>(superCell.size() * 10);
        for (IntCursor subcell : superCell) {
            List<Double> subCellContour = cellStorage.getCellContourOrder(subcell.value);
            int j = 0;
            while (j < subCellContour.size()) {
                double lat = subCellContour.get(j);
                j++;
                double lon = subCellContour.get(j);
                j++;
                superCellCoordinates.add(new Coordinate(lon, lat));
            }
        }
        return  superCellCoordinates;
    }

    public  Geometry concHullOfNodes(List<Coordinate> coordinates) {
        int j = 0;
        double defaultVisitorThreshold = 0.0035;
        double defaultSearchWidth = 0.0008;
        double defaulPointWidth = 0.005;

        List<Coordinate> points = new ArrayList<>(1/20 * coordinates.size());
        PointItemVisitor visitor = new PointItemVisitor(0, 0, defaultVisitorThreshold);
        Quadtree qtree = new Quadtree();
        Envelope searchEnv = new Envelope();
        TreeSet<Coordinate> treeSet = new TreeSet<>();

        while (j < coordinates.size()){
            double latitude = coordinates.get(j).y;
            double longitude = coordinates.get(j).x;
            j++;
            addPoint(visitor, points, qtree, searchEnv, treeSet, longitude, latitude, defaultSearchWidth, defaulPointWidth, true);
        }

        GeometryFactory _geomFactory = new GeometryFactory();
        int size = points.size();
        Geometry[] geometries = new Geometry[size];
        int g = 0;
        for (int i = 0; i < size; i++)
            geometries[g++] = _geomFactory.createPoint(points.get(i));
        GeometryCollection treePoints = new GeometryCollection(geometries, _geomFactory);

//        if(PART__DEBUG) System.out.println("Coordinates from geometry " + coordinates.size() + ", reduced input coordinates to conchull " + points.size());
        ConcaveHull ch = new ConcaveHull(treePoints, CONCAVEHULL_THRESHOLD, false);
        Geometry geom = ch.getConcaveHull();

        return geom;
    }

    public Boolean addPoint(PointItemVisitor visitor, List<Coordinate> points, Quadtree tree, Envelope searchEnv, TreeSet treeSet, double lon, double lat, double searchWidth, double pointWidth, boolean checkNeighbours) {
        if (checkNeighbours)
        {
            visitor.setPoint(lon, lat);
            searchEnv.init(lon - searchWidth, lon + searchWidth, lat - searchWidth, lat + searchWidth);
            tree.query(searchEnv, visitor);
            if (!visitor.isNeighbourFound())
            {
                Coordinate p = new Coordinate(lon, lat);

                if (!treeSet.contains(p))
                {
                    Envelope env = new Envelope(lon - pointWidth, lon + pointWidth, lat - pointWidth, lat + pointWidth);
                    tree.insert(env, p);
                    points.add(p);
                    treeSet.add(p);

                    return true;
                }
            }
        }
        else
        {
            Coordinate p = new Coordinate(lon, lat);
            if (!treeSet.contains(p))
            {
                Envelope env = new Envelope(lon - pointWidth, lon + pointWidth, lat - pointWidth, lat + pointWidth);
                tree.insert(env, p);
                points.add(p);
                treeSet.add(p);

                return true;
            }
        }

        return false;
    }

    IntObjectMap<IntHashSet> identifySuperCells(IntSet cellIds, int hierarchyLevel, boolean isPrimary){
        //Account for the subcell division in InertialFlow final step
        //hierarchyLevel += 1;
//        IntSet cellIds = isochroneNodeStorage.getCellIds();
        int maxId = -1;
        for(IntCursor cellId : cellIds)
            if(cellId.value > maxId)
                maxId = cellId.value;

        IntHashSet visitedCells = new IntHashSet();
        IntObjectMap<IntHashSet> superCells = new IntObjectHashMap<>();
        List<Integer> orderedCellIds = Arrays.stream(cellIds.toArray()).boxed().collect(Collectors.toList());
//        List<Integer> orderedCellIds = (List<Integer>) Arrays.asList(cellIds.toArray());
        Collections.sort(orderedCellIds);
        for(int cellId : orderedCellIds){
            if (visitedCells.contains(cellId))
                continue;
            //These checks are only needed and possible for supercells built from baseCells and not built from supercells
            if(isPrimary) {
                //Check if it is part of a separated cell: Has daughter?
                if (cellIds.contains(cellId << 1))
                    continue;
                //If it has sister, check if their combined size is smaller than minimum cell size -> disconnected
                if (cellIds.contains(cellId ^ 1)) {
                    if (cellStorage.getNodesOfCell(cellId).size()
                            + cellStorage.getNodesOfCell(cellId ^ 1).size()
                            < PART__MAX_CELL_NODES_NUMBER)
                        continue;
                }
            }
            int motherId = cellId >> hierarchyLevel;
            //This cell is too high up in the hierarchy
            while(motherId == 0){
                hierarchyLevel -= 1;
                motherId = cellId >> hierarchyLevel;
            }

            IntHashSet superCell = new IntHashSet();

            createSuperCell(cellIds, visitedCells, superCell, maxId, motherId, hierarchyLevel, isPrimary);
            for(IntCursor cell :  superCell)
                visitedCells.add(cell.value);
            if(superCell.size() > 0)
                superCells.put(motherId, superCell);
        }
        return superCells;
    }

    void createSuperCell(IntSet cellIds, IntHashSet visitedCells, IntHashSet superCell, int maxId, int currentCell, int level, boolean isPrimary){
        if(currentCell > maxId)
            return;
        //Is it already part of a supercell?
        if(visitedCells.contains(currentCell))
            return;
        if(isPrimary) {
            //Is a disconnected cell?
            if (cellIds.contains(currentCell) && cellIds.contains(currentCell << 1))
                return;
            //If it has sister, check if their combined size is smaller than minimum cell size -> disconnected
            if (cellIds.contains(currentCell ^ 1) && cellIds.contains(currentCell)) {
                if (cellStorage.getNodesOfCell(currentCell).size()
                        + cellStorage.getNodesOfCell(currentCell ^ 1).size()
                        < PART__MAX_CELL_NODES_NUMBER)
                    return;
            }
        }

        if(!cellIds.contains(currentCell)){
            createSuperCell(cellIds, visitedCells, superCell, maxId, currentCell << 1, level, isPrimary);
            createSuperCell(cellIds, visitedCells, superCell, maxId, currentCell << 1 | 1, level, isPrimary);
        }
        else {
            superCell.add(currentCell);
            return;
        }
    }

    private List<Coordinate> createCoordinates(int cellId){
        IntHashSet cellNodes = cellStorage.getNodesOfCell(cellId);
        int initialSize = cellNodes.size();
        List<Coordinate> coordinates =  new ArrayList<>(initialSize);

        EdgeExplorer explorer = ghStorage.getBaseGraph().createEdgeExplorer(EdgeFilter.ALL_EDGES);
        EdgeIterator iter;

        IntHashSet visitedEdges = new IntHashSet();
        for (int node : cellNodes.keys){
            iter = explorer.setBaseNode(node);
            while (iter.next()){
                if(!cellNodes.contains(iter.getAdjNode()))
                    continue;
                if(visitedEdges.contains(iter.getEdge()))
                    continue;
                visitedEdges.add(iter.getEdge());
                //Add all base nodes + geometry of edge
                checkAddLatLon(iter.fetchWayGeometry(1), coordinates);
            }
        }
        return coordinates;
    }

    private LineString createContour(List<Coordinate> coordinates){
        try {
            Geometry geom = concHullOfNodes(coordinates);
            Polygon poly = (Polygon) geom;
            poly.normalize();
            return poly.getExteriorRing();
        }
        catch (Exception e){
            return null;
        }
    }

    private void storeContour(int cellId, LineString ring){
        List<Double> hullLatitudes = new ArrayList<>(ring.getNumPoints());
        List<Double> hullLongitudes = new ArrayList<>(ring.getNumPoints());
        for (int i = 0; i < ring.getNumPoints(); i++) {
            // Add coordinates to storage, but make sure there are enough on long edges by splitting
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
        cellStorage.setCellContourOrder(cellId, hullLatitudes, hullLongitudes);
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
     splits a distance between two coordinates if theyre above a certain limit
     */
    private void splitEdge(double lat0, double lat1, double lon0, double lon1, List<Double> latitudes, List<Double> longitudes, double minlim, double maxlim){
        double dist = distance(lat0, lat1, lon0, lon1);

        if(dist > minlim && dist < maxlim){
            int n = (int) Math.ceil(dist / minlim);
            for (int i = 1; i < n; i++){
                latitudes.add(lat0 + i * (lat1 - lat0) / n);
                longitudes.add(lon0 + i * (lon1 - lon0) / n);
            }
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

    private void checkAddLatLon(PointList newCoordinates, List<Coordinate> existingCoordinates){
        for (int i = 0; i < newCoordinates.size(); i++) {
            existingCoordinates.add(new Coordinate(newCoordinates.getLon(i), newCoordinates.getLat(i)));
//            existingLats.add(newCoordinates.getLat(i));
//            existingLons.add(newCoordinates.getLon(i));
//            int latIndex = existingLats.indexOf(newCoordinates.getLat(i));
//            //point is not yet in list
//            if(latIndex == -1){
//                existingLats.add(newCoordinates.getLat(i));
//                existingLons.add(newCoordinates.getLon(i));
//                countNonExisting++;
//                continue;
//            }
//            //The coordinate is already added
//            if(newCoordinates.getLon(i) == existingLons.get(latIndex)) {
//                countExisting++;
//                continue;
//            }
//
//
//            existingLats.add(newCoordinates.getLat(i));
//            existingLons.add(newCoordinates.getLon(i));
//            countNonExisting++;

        }
    }
}
