package org.heigit.ors.fastisochrones;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.locationtech.jts.geom.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.getMaxCellNodesNumber;
import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.isSupercellsEnabled;
import static org.locationtech.jts.algorithm.hull.ConcaveHull.concaveHullByLength;

/**
 * Calculates Outlines (Contour) of cells.
 * Contours are concave hulls of a given set of points.
 * Additionally, contours for super cells can be created.
 * Super cells are again grouped into another set of super super cells.
 * Super cells contain a maximum amount of base cells given by the hierarchy level parameter.
 * Usually, there will be fewer base cells in a super cell, as some branches of the partitioning end earlier than others.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class Contour {
    //Length that a polygon edge has to have in m to be split into smaller subedges so that there are no artifacts from later concave hull calculations with it
    private static final int MIN_EDGE_LENGTH = 125;
    private static final int MAX_EDGE_LENGTH = Integer.MAX_VALUE;
    //This means that one supercell can contain at most 2^3 = 8 base cells.
    private static final int SUPER_CELL_HIERARCHY_LEVEL = 2;
    //This means that one supersupercell can contain at most 2^(3 + 2) = 32 base cells.
    private static final int SUPER_SUPER_CELL_HIERARCHY_LEVEL = 2; // level above super cell level
    private static final double CONCAVE_HULL_THRESHOLD = 0.006;
    private static final double BUFFER_SIZE = 0.0003;
    protected NodeAccess nodeAccess;
    protected GraphHopperStorage ghStorage;
    private final IsochroneNodeStorage isochroneNodeStorage;
    private final CellStorage cellStorage;

    public Contour(GraphHopperStorage ghStorage, NodeAccess nodeAccess, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage) {
        this.ghStorage = ghStorage;
        this.nodeAccess = nodeAccess;
        this.isochroneNodeStorage = isochroneNodeStorage;
        this.cellStorage = cellStorage;
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

    /**
     * Calculates Contours of base cells and (if enabled) supercells and stores the data in cellStorage
     */
    public void calculateContour() {
        handleBaseCells();
        cellStorage.flush();
        IntObjectMap<IntHashSet> superCells = handleSuperCells();
        cellStorage.storeContourPointerMap();
        if (isSupercellsEnabled())
            cellStorage.storeSuperCells(superCells);
        cellStorage.setContourPrepared(true);
        cellStorage.flush();
    }

    /**
     * Create contour for each base cell and store it
     */
    private void handleBaseCells() {
        for (IntCursor cellId : isochroneNodeStorage.getCellIds()) {
            List<Coordinate> coordinates = createCoordinates(cellId.value);
            LineString ring = createContour(coordinates);
            if (ring == null || ring.getNumPoints() < 2) {
                cellStorage.setCellContourOrder(cellId.value, new ArrayList<>(), new ArrayList<>());
                continue;
            }
            expandAndSaveContour(cellId.value, ring);
        }
    }

    /**
     * Create Contour for each supercell and store it
     * Create supercells for better querytime performance
     * Current implementation supports 2 levels of supercells. Calculated individually
     * For each super(super)cell, we need to know the corresponding basecells (to get the contour from storage)
     * and the corresponding subcells (these are supercells for supersupercells)
     *
     * @return Mapping of supercell Id -> Set of subcell ids
     */
    private IntObjectMap<IntHashSet> handleSuperCells() {
        IntObjectMap<IntHashSet> superCells = new IntObjectHashMap<>();
        if (isSupercellsEnabled()) {
            superCells = identifySuperCells(isochroneNodeStorage.getCellIds(), SUPER_CELL_HIERARCHY_LEVEL, true);
            IntObjectMap<IntHashSet> superSuperCells = identifySuperCells(new IntHashSet(superCells.keys()), SUPER_SUPER_CELL_HIERARCHY_LEVEL, false);

            IntObjectMap<IntHashSet> superCellsToBaseCells = getBaseCellsOfSuperSuperCells(superSuperCells, superCells);
            superCellsToBaseCells.putAll(superCells);
            superCells.putAll(superSuperCells);

            //Calculate the concave hull for all super cells and super super cells
            for (IntObjectCursor<IntHashSet> superCell : superCellsToBaseCells) {
                List<Coordinate> superCellCoordinates = createSuperCellCoordinates(superCell.value);
                LineString ring = createContour(superCellCoordinates);
                if (ring == null || ring.getNumPoints() < 2) {
                    cellStorage.setCellContourOrder(superCell.key, new ArrayList<>(), new ArrayList<>());
                    continue;
                }
                expandAndSaveContour(superCell.key, ring);
            }
        }
        return superCells;
    }

    /**
     * From the superCells Map get all the base cells for each super super cell
     *
     * @param superSuperCells the super super cells for which to get the base cells
     * @param superCells      the mapping of super cells to base cells
     * @return mapping of super super cells to base cells
     */
    private IntObjectMap<IntHashSet> getBaseCellsOfSuperSuperCells(IntObjectMap<IntHashSet> superSuperCells, IntObjectMap<IntHashSet> superCells) {
        IntObjectMap<IntHashSet> baseCellsOfSuperSuperCells = new IntObjectHashMap<>();
        for (IntObjectCursor<IntHashSet> superSuperCell : superSuperCells) {
            IntHashSet newSuperCell = new IntHashSet();
            for (IntCursor cell : superSuperCell.value)
                newSuperCell.addAll(superCells.get(cell.value));
            baseCellsOfSuperSuperCells.put(superSuperCell.key, newSuperCell);
        }
        return baseCellsOfSuperSuperCells;
    }

    /**
     * For a super cell: get the base cell coordinates from storage.
     * Create a sorted list of coordinates
     *
     * @param superCell super cell for which to create the coordinates
     * @return list of contour coordinates of base cells
     */
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
        //Need to sort the coordinates, because they will be added to a search tree
        //The order of insertion changes the search tree coordinates and we want consistency between runs
        Collections.sort(superCellCoordinates);
        return superCellCoordinates;
    }

    private Geometry concHullOfNodes(List<Coordinate> points) {
        var geomFactory = new GeometryFactory();
        var geometries = points.stream().map(geomFactory::createPoint).toArray(Geometry[]::new);
        var treePoints = geomFactory.createGeometryCollection(geometries);

        return concaveHullByLength(treePoints, CONCAVE_HULL_THRESHOLD);
    }

    /**
     * Find the supercells of base cells.
     * For a given cellId, find cells that are connected to it via cellId bitshifting
     * E.g. cells 100 and 101 are subcells of cell 10
     *
     * @param cellIds        cellIds whose super cells should be found
     * @param hierarchyLevel how many bits should be pruned from the id to find a supercell. Influences size of super cell
     * @param isPrimary      Whether super cells are found for base cells or for existing super cells
     * @return a map of supercellId to base cell ids
     */
    private IntObjectMap<IntHashSet> identifySuperCells(IntSet cellIds, int hierarchyLevel, boolean isPrimary) {
        //Account for the subcell division in InertialFlow final step
        int maxId = createMaxId(cellIds);

        IntHashSet visitedCells = new IntHashSet();
        IntObjectMap<IntHashSet> superCells = new IntObjectHashMap<>();
        //Sorting the ids creates better supercells as close Ids are also geographically closer
        List<Integer> orderedCellIds = Arrays.stream(cellIds.toArray()).boxed().sorted().collect(Collectors.toList());
        for (int cellId : orderedCellIds) {
            if (visitedCells.contains(cellId))
                continue;
            //These checks are only needed and possible for supercells built from baseCells and not built from supercells
            if (isPrimary && !isValidBaseCell(cellIds, cellId))
                continue;

            int motherId = cellId >> hierarchyLevel;
            //This cell is too high up in the hierarchy, so we need to adapt the hierarchy level and id
            while (motherId == 0) {
                hierarchyLevel -= 1;
                motherId = cellId >> hierarchyLevel;
            }

            IntHashSet superCell = new IntHashSet();

            createSuperCell(cellIds, visitedCells, superCell, maxId, motherId, isPrimary);
            for (IntCursor cell : superCell)
                visitedCells.add(cell.value);
            if (superCell.size() > 0)
                superCells.put(motherId, superCell);
        }
        return superCells;
    }

    private int createMaxId(IntSet cellIds) {
        int maxId = -1;
        for (IntCursor cellId : cellIds)
            if (cellId.value > maxId)
                maxId = cellId.value;
        return maxId;
    }

    /**
     * Check whether a base cell is valid. It is valid if no cells were disconnected from it and if it is no disconnected cell itself
     *
     * @param cellIds All existing cellIds
     * @param cellId  the cellId to check
     * @return isValid
     */
    private boolean isValidBaseCell(IntSet cellIds, int cellId) {
        //Check if it is part of a separated cell: Has daughter?
        if (cellIds.contains(cellId << 1))
            return false;
        return !isDisconnectedCell(cellIds, cellId);
    }

    private boolean isDisconnectedCell(IntSet cellIds, int cellId) {
        //If it has sister, check if their combined size is smaller than minimum cell size -> disconnected
        return (cellIds.contains(cellId ^ 1) && cellStorage.getNodesOfCell(cellId).size()
                + cellStorage.getNodesOfCell(cellId ^ 1).size()
                < getMaxCellNodesNumber());
    }

    /**
     * Recursively iterate over cellIds to collect all cells that belong to a super cell id
     *
     * @param cellIds      all cellIds
     * @param visitedCells cellIds that have already been added to a supercell
     * @param superCell    the super cell to which other cell ids are added
     * @param maxId        maximum possible search id
     * @param currentCell  the cell currently examined
     * @param isPrimary    true if super cell is built from base cells
     */
    private void createSuperCell(IntSet cellIds, IntHashSet visitedCells, IntHashSet superCell, int maxId, int currentCell, boolean isPrimary) {
        if (currentCell > maxId)
            return;
        //Cells should be only part of one supercell
        if (visitedCells.contains(currentCell))
            return;
        if (isPrimary && cellIds.contains(currentCell) && !isValidBaseCell(cellIds, currentCell))
            return;

        if (!cellIds.contains(currentCell)) {
            createSuperCell(cellIds, visitedCells, superCell, maxId, currentCell << 1, isPrimary);
            createSuperCell(cellIds, visitedCells, superCell, maxId, currentCell << 1 | 1, isPrimary);
        } else {
            superCell.add(currentCell);
        }
    }

    /**
     * Get all coordinates of nodes and edges in a cell
     *
     * @param cellId the cell id
     * @return list of coordinates that represent all edges and nodes in the cell
     */
    private List<Coordinate> createCoordinates(int cellId) {
        IntHashSet cellNodes = cellStorage.getNodesOfCell(cellId);
        int initialSize = cellNodes.size();
        List<Coordinate> coordinates = new ArrayList<>(initialSize);
        EdgeFilter edgeFilter = AccessFilter.allEdges(ghStorage.getEncodingManager().fetchEdgeEncoders().get(0).getAccessEnc()); // TODO Refactoring: cleanup method chain

        EdgeExplorer explorer = ghStorage.getBaseGraph().createEdgeExplorer(edgeFilter);
        EdgeIterator iter;

        IntHashSet visitedEdges = new IntHashSet();
        PointList towerCoordinates = new PointList(cellNodes.keys.length, false);
        for (IntCursor node : cellNodes) {
            towerCoordinates.add(ghStorage.getNodeAccess().getLat(node.value), ghStorage.getNodeAccess().getLon(node.value));
        }
        addLatLon(towerCoordinates, coordinates);
        for (IntCursor node : cellNodes) {
            iter = explorer.setBaseNode(node.value);
            while (iter.next()) {
                if (visitedEdges.contains(iter.getEdge())
                        || !cellNodes.contains(iter.getAdjNode())
                        || !edgeFilter.accept(iter))
                    continue;
                visitedEdges.add(iter.getEdge());
                splitAndAddLatLon(iter.fetchWayGeometry(FetchMode.ALL), coordinates, MIN_EDGE_LENGTH, MAX_EDGE_LENGTH);
            }
        }
        //Remove duplicates
        coordinates = coordinates.stream()
                .distinct()
                .collect(Collectors.toList());
        //Need to sort the coordinates, because they will be added to a search tree
        //The order of insertion changes the search tree coordinates and we want consistency between runs
        try {
            Collections.sort(coordinates);
        } catch (Exception e) {
            //This happens in less than 1% of the runs and I have not figured out why.
            //It has no real impact as sorting is only for consistency of outlines, not quality
        }
        return coordinates;
    }

    private LineString createContour(List<Coordinate> coordinates) {
        try {
            Geometry geom = concHullOfNodes(coordinates);
            Polygon poly = (Polygon) geom;
            poly.normalize();
            return poly.getExteriorRing();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Fill the edges of the polygon representing the contour so that even long straights are represented by regular points.
     * If these long edges were not split, it would lead to "holes" in the edge that can be misinterpreted when building the overall isochrone from multiple contours
     *
     * @param cellId cellId of the contour
     * @param ring   LineString representing the contour in order
     */
    private void expandAndSaveContour(int cellId, LineString ring) {
        List<Double> hullLatitudes = new ArrayList<>(ring.getNumPoints());
        List<Double> hullLongitudes = new ArrayList<>(ring.getNumPoints());
        for (int i = 0; i < ring.getNumPoints(); i++) {
            // Add coordinates to storage, but make sure there are enough on long edges by splitting
            hullLatitudes.add(ring.getPointN(i).getY());
            hullLongitudes.add(ring.getPointN(i).getX());
        }
        cellStorage.setCellContourOrder(cellId, hullLatitudes, hullLongitudes);
    }

    public Contour setGhStorage(GraphHopperStorage ghStorage) {
        this.ghStorage = ghStorage;
        return this;
    }

    private void splitAndAddLatLon(PointList newCoordinates, List<Coordinate> existingCoordinates, double minlim, double maxlim) {
        for (int i = 0; i < newCoordinates.size() - 1; i++) {
            double lat0 = newCoordinates.getLat(i);
            double lon0 = newCoordinates.getLon(i);
            double lat1 = newCoordinates.getLat(i + 1);
            double lon1 = newCoordinates.getLon(i + 1);
            double dist = distance(lat0, lat1, lon0, lon1);
            double dx = (lon0 - lon1);
            double dy = (lat0 - lat1);
            double normLength = Math.sqrt((dx * dx) + (dy * dy));

            int n = (int) Math.ceil(dist / minlim);
            double scale = BUFFER_SIZE / normLength;

            double dx2 = -dy * scale;
            double dy2 = dx * scale;
            if (i != 0) {
                existingCoordinates.add(new Coordinate(lon0 + dx2, lat0 + dy2));
                existingCoordinates.add(new Coordinate(lon0 - dx2, lat0 - dy2));
            }

            if (dist > minlim && dist < maxlim) {
                for (int j = 1; j < n; j++) {
                    existingCoordinates.add(new Coordinate(lon0 + j * (lon1 - lon0) / n + dx2, lat0 + j * (lat1 - lat0) / n + dy2));
                    existingCoordinates.add(new Coordinate(lon0 + j * (lon1 - lon0) / n - dx2, lat0 + j * (lat1 - lat0) / n - dy2));
                }
            }
        }
    }

    private void addLatLon(PointList newCoordinates, List<Coordinate> existingCoordinates) {
        if (newCoordinates.isEmpty())
            return;
        for (int i = 0; i < newCoordinates.size() - 1; i++) {
            double lat0 = newCoordinates.getLat(i);
            double lon0 = newCoordinates.getLon(i);
            existingCoordinates.add(new Coordinate(lon0 + BUFFER_SIZE, lat0 + BUFFER_SIZE));
            existingCoordinates.add(new Coordinate(lon0 - BUFFER_SIZE, lat0 - BUFFER_SIZE));
        }
    }
}
