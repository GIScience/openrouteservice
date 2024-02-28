/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.isochrones.builders.fast;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.GraphHopper;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint3D;
import org.heigit.ors.isochrones.builders.AbstractIsochroneMapBuilder;
import org.heigit.ors.util.ProfileTools;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.apache.log4j.Logger;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.fastisochrones.FastIsochroneAlgorithm;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.isochrones.IsochroneMap;
import org.heigit.ors.isochrones.IsochroneSearchParameters;
import org.heigit.ors.isochrones.IsochronesErrorCodes;
import org.heigit.ors.routing.AvoidFeatureFlags;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.graphhopper.extensions.AccessibilityMap;
import org.heigit.ors.routing.graphhopper.extensions.ORSEdgeFilterFactory;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.AvoidFeaturesEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.heigit.ors.util.DebugUtility;
import org.heigit.ors.util.GeomUtility;

import java.util.*;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.*;
import static org.locationtech.jts.algorithm.hull.ConcaveHull.concaveHullByLength;

/**
 * Calculates isochrone polygons using fast isochrone algorithm.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class FastIsochroneMapBuilder extends AbstractIsochroneMapBuilder {
    private CellStorage cellStorage;
    private IsochroneNodeStorage isochroneNodeStorage;
    private QueryGraph queryGraph;
    private static final int MAX_EDGE_LENGTH_LIMIT = Integer.MAX_VALUE;
    private static final double ACTIVE_CELL_APPROXIMATION_FACTOR = 0.99;
    private static final Logger LOGGER = Logger.getLogger(FastIsochroneMapBuilder.class.getName());

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void initialize(RouteSearchContext searchContext) {
        super.initialize(searchContext);
        defaultSmoothingDistance = 0.010;// Use a default length of ~1000m
        var fastIsochroneFactory = ((ORSGraphHopper) searchContext.getGraphHopper()).getFastIsochroneFactory();
        this.cellStorage = fastIsochroneFactory.getCellStorage();
        this.isochroneNodeStorage = fastIsochroneFactory.getIsochroneNodeStorage();
    }

    public IsochroneMap compute(IsochroneSearchParameters parameters) throws Exception {
        StopWatch swTotal = null;
        StopWatch sw = null;
        if (DebugUtility.isDebug()) {
            swTotal = new StopWatch();
            swTotal.start();
            sw = new StopWatch();
            sw.start();
        }
        double maxSpeed = determineMaxSpeed();

        double meanSpeed = determineMeanSpeed(maxSpeed);

        double metersPerSecond = maxSpeed / 3.6;
        // only needed for reachfactor property
        double meanMetersPerSecond = meanSpeed / 3.6;

        Weighting weighting = ORSWeightingFactory.createIsochroneWeighting(searchContext, parameters.getRangeType());

        Coordinate loc = parameters.getLocation();

        FlagEncoder encoder = searchContext.getEncoder();
        String profileName = ProfileTools.makeProfileName(encoder.toString(), weighting.getName(), false);
        GraphHopper gh = searchContext.getGraphHopper();
        GraphHopperStorage graphHopperStorage = gh.getGraphHopperStorage();
        EdgeFilter defaultSnapFilter = new DefaultSnapFilter(weighting, graphHopperStorage.getEncodingManager().getBooleanEncodedValue(Subnetwork.key(profileName)));

        ORSEdgeFilterFactory edgeFilterFactory = new ORSEdgeFilterFactory();
        EdgeFilterSequence edgeFilterSequence = getEdgeFilterSequence(edgeFilterFactory, defaultSnapFilter);
        Snap res = searchContext.getGraphHopper().getLocationIndex().findClosest(loc.y, loc.x, edgeFilterSequence);
        List<Snap> snaps = new ArrayList<>(1);
        snaps.add(res);
        //Needed to get the cell of the start point (preprocessed information, so no info on virtual nodes)
        int nonvirtualClosestNode = res.getClosestNode();
        if (nonvirtualClosestNode == -1)
            throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "The closest node is null.");

        Graph graph = searchContext.getGraphHopper().getGraphHopperStorage().getBaseGraph();
        queryGraph = QueryGraph.create(graph, snaps);
        int from = res.getClosestNode();

        //This calculates the nodes that are within the limit
        //Currently only support for Node based
        if (!(searchContext.getGraphHopper() instanceof ORSGraphHopper))
            throw new IllegalStateException("Unable to run fast isochrones without ORSGraphhopper");

        int nRanges = parameters.getRanges().length;
        IsochroneMap isochroneMap = null;

        for (int i = 0; i < nRanges; i++) {
            FastIsochroneAlgorithm fastIsochroneAlgorithm = new FastIsochroneAlgorithm(
                    queryGraph,
                    weighting,
                    TraversalMode.NODE_BASED,
                    cellStorage,
                    isochroneNodeStorage,
                    ((ORSGraphHopper) searchContext.getGraphHopper()).getEccentricity().getEccentricityStorage(weighting),
                    ((ORSGraphHopper) searchContext.getGraphHopper()).getEccentricity().getBorderNodeDistanceStorage(weighting),
                    edgeFilterSequence);
            //Account for snapping distance
            double isolimit = parameters.getRanges()[i] - weighting.getMinWeight(res.getQueryDistance());
            if (isolimit <= 0)
                throw new IllegalStateException("Distance of query to snapped position is greater than isochrone limit!");

            fastIsochroneAlgorithm.calcIsochroneNodes(from, nonvirtualClosestNode, isolimit);

            Set<Geometry> isochroneGeometries = new HashSet<>();

            if (DebugUtility.isDebug()) {
                LOGGER.debug("Find edges: " + sw.stop().getSeconds());
                sw = new StopWatch();
                sw.start();
            }

            fastIsochroneAlgorithm.approximateActiveCells(ACTIVE_CELL_APPROXIMATION_FACTOR);

            if (DebugUtility.isDebug()) {
                LOGGER.debug("Approximate active cells: " + sw.stop().getSeconds());
                sw = new StopWatch();
                sw.start();
            }
            //Add all fully reachable cell geometries
            handleFullyReachableCells(isochroneGeometries, fastIsochroneAlgorithm.getFullyReachableCells());

            if (DebugUtility.isDebug()) {
                LOGGER.debug("Handle " + fastIsochroneAlgorithm.getFullyReachableCells().size() + " fully reachable cells: " + sw.stop().getSeconds());
            }

            GHPoint3D snappedPosition = res.getSnappedPoint();

            AccessibilityMap edgeMap = new AccessibilityMap(fastIsochroneAlgorithm.getStartCellMap(), snappedPosition);

            final Coordinate snappedLoc = (snappedPosition == null) ? parameters.getLocation() : new Coordinate(snappedPosition.lon, snappedPosition.lat);

            if (isochroneMap == null) isochroneMap = new IsochroneMap(parameters.getTravellerId(), snappedLoc);

            if (edgeMap.isEmpty())
                return isochroneMap;

            List<Coordinate> isoPoints = new ArrayList<>((int) (1.2 * edgeMap.getMap().size()));

            double isoValue = parameters.getRanges()[i];

            TravelRangeType isochroneType = parameters.getRangeType();

            final double maxRadius;
            double meanRadius;
            if (isochroneType == TravelRangeType.TIME) {
                maxRadius = metersPerSecond * isoValue;
                meanRadius = meanMetersPerSecond * isoValue;
            } else {
                maxRadius = isoValue;
                meanRadius = isoValue;
            }

            float smoothingFactor = parameters.getSmoothingFactor();
            var smoothingDistance = convertSmoothingFactorToDistance(smoothingFactor, maxRadius);
            var smoothingDistanceMeter = GeomUtility.degreesToMetres(smoothingDistance);

            //Add previous isochrone interval polygon
            addPreviousIsochronePolygon(isochroneGeometries);
            buildActiveCellsConcaveHulls(fastIsochroneAlgorithm, isochroneGeometries, snappedLoc, snappedPosition, isoValue, smoothingDistance, smoothingDistanceMeter);

            if (!isochroneGeometries.isEmpty()) {
                //Make a union of all now existing polygons to reduce coordinate list
                //Uncomment to see all geometries in response
                //for(Geometry poly : isochroneGeometries)
                //  isochroneMap.addIsochrone(new Isochrone(poly, isoValue, meanRadius));
                Geometry preprocessedGeometry = combineGeometries(isochroneGeometries);

                StopWatch finalConcaveHullStopWatch = new StopWatch();
                if (DebugUtility.isDebug())
                    finalConcaveHullStopWatch.start();
                isoPoints.addAll(createCoordinateListFromGeometry(preprocessedGeometry, smoothingDistanceMeter));
                GeometryCollection points = buildIsochrone(new AccessibilityMap(new GHIntObjectHashMap<>(0), snappedPosition), isoPoints, isoValue, smoothingDistanceMeter);
                addIsochrone(isochroneMap, points, isoValue, meanRadius, smoothingDistance);
                if (DebugUtility.isDebug()) {
                    LOGGER.debug("Build final concave hull from " + points.getNumGeometries() + " points: " + finalConcaveHullStopWatch.stop().getSeconds());
                }
            }
        }

        if (DebugUtility.isDebug())
            LOGGER.debug("Total time: " + swTotal.stop().getSeconds());

        return isochroneMap;
    }

    private EdgeFilterSequence getEdgeFilterSequence(ORSEdgeFilterFactory edgeFilterFactory, EdgeFilter prependFilter) throws Exception {
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        EdgeFilter edgeFilter = edgeFilterFactory.createEdgeFilter(searchContext.getProperties(), searchContext.getEncoder(), searchContext.getGraphHopper().getGraphHopperStorage(), prependFilter);
        edgeFilterSequence.add(edgeFilter);
        edgeFilterSequence.add(new AvoidFeaturesEdgeFilter(AvoidFeatureFlags.FERRIES, searchContext.getGraphHopper().getGraphHopperStorage()));
        return edgeFilterSequence;
    }

    private void buildActiveCellsConcaveHulls(FastIsochroneAlgorithm fastIsochroneAlgorithm, Set<Geometry> isochroneGeometries, Coordinate snappedLoc, GHPoint3D snappedPosition, double isoValue, double smoothingDistance, double minSplitLength) {
        //Build concave hulls of all active cells individually
        StopWatch swActiveCellSeparate = new StopWatch();
        StopWatch swActiveCellBuild = new StopWatch();

        for (Map.Entry<Integer, IntObjectMap<SPTEntry>> activeCell : fastIsochroneAlgorithm.getActiveCellMaps().entrySet()) {
            swActiveCellSeparate.start();
            //Find disconnected sub-cells of active cells to avoid geometric problems
            List<GHIntObjectHashMap<SPTEntry>> disconnectedActiveCells = separateDisconnected(activeCell.getValue());

            swActiveCellSeparate.stop();
            swActiveCellBuild.start();
            boolean largestSubCellProcessed = false;
            for (GHIntObjectHashMap<SPTEntry> splitMap : disconnectedActiveCells) {
                if (largestSubCellProcessed && splitMap.size() < getMinCellNodesNumber())
                    continue;
                largestSubCellProcessed = true;
                GeometryCollection points = buildIsochrone(new AccessibilityMap(splitMap, snappedPosition), new ArrayList<>(), isoValue, minSplitLength);
                createPolyFromPoints(isochroneGeometries, points, smoothingDistance, minSplitLength);
            }
            swActiveCellBuild.stop();
        }
        if (DebugUtility.isDebug()) {
            LOGGER.debug("Separate disconnected: " + swActiveCellSeparate.stop().getSeconds());
            LOGGER.debug("Build " + fastIsochroneAlgorithm.getActiveCellMaps().size() + " active cells: " + swActiveCellBuild.stop().getSeconds());
        }
    }

    private List<Coordinate> createCoordinateListFromGeometry(Geometry preprocessedGeometry, double minSplitLength) {
        List<Coordinate> contourCoords = new ArrayList<>();

        for (int j = 0; j < preprocessedGeometry.getNumGeometries(); j++) {
            Geometry geometry = preprocessedGeometry.getGeometryN(j);

            if (geometry instanceof Polygon poly) {
                List<Coordinate> ringCoords = createCoordinateListFromPolygon(poly);
                contourCoords.addAll(ringCoords);

                double lat0 = ringCoords.get(ringCoords.size() - 1).y;
                double lon0 = ringCoords.get(ringCoords.size() - 1).x;
                double lat1;
                double lon1;
                for (int i = 0; i < ringCoords.size(); i++) {
                    lat1 = ringCoords.get(i).y;
                    lon1 = ringCoords.get(i).x;
                    splitLineSegment(lat0, lon0, lat1, lon1, contourCoords, minSplitLength / 2, MAX_EDGE_LENGTH_LIMIT);
                    lon0 = lon1;
                    lat0 = lat1;
                }
            }
            else {
                contourCoords.addAll(Arrays.asList(geometry.getCoordinates()));
            }
        }

        return contourCoords;
    }

    private Geometry combineGeometries(Set<Geometry> isochroneGeometries) {
        StopWatch unaryUnionStopWatch = new StopWatch();
        if (DebugUtility.isDebug())
            unaryUnionStopWatch.start();
        Geometry preprocessedGeometry = UnaryUnionOp.union(isochroneGeometries);
        if (DebugUtility.isDebug()) {
            LOGGER.debug("Union of geometries: " + unaryUnionStopWatch.stop().getSeconds());
        }
        return preprocessedGeometry;
    }

    private void addPreviousIsochronePolygon(Set<Geometry> isochroneGeometries) {
        if (previousIsochronePolygon != null)
            isochroneGeometries.add(previousIsochronePolygon);
    }

    private void createPolyFromPoints(Set<Geometry> isochroneGeometries, GeometryCollection points, double smoothingDistance, double minSplitLength) {
        if (points.isEmpty())
            return;

        try {
            Geometry concaveHull = concaveHullByLength(points, smoothingDistance);
            if (concaveHull instanceof Polygon && concaveHull.isValid() && !concaveHull.isEmpty())
                isochroneGeometries.add(concaveHull);
        } catch (Exception e) {
            if (isLogEnabled()) LOGGER.debug(e.getMessage());
        }
    }

    private GeometryCollection buildIsochrone(AccessibilityMap edgeMap, List<Coordinate> points,
                                              double isolineCost, double minSplitLength) {
        IntObjectMap<SPTEntry> map = edgeMap.getMap();

        GraphHopperStorage graphHopperStorage = searchContext.getGraphHopper().getGraphHopperStorage();

        int maxNodeId = graphHopperStorage.getNodes() - 1;
        int maxEdgeId = graphHopperStorage.getEdges() - 1;

        SPTEntry goalEdge;

        double bufferSize = 0.0018;

        boolean useHighDetail = map.size() < 1000;

        if (useHighDetail) {
            bufferSize = 0.0009;
        }

        for (IntObjectCursor<SPTEntry> entry : map) {
            goalEdge = entry.value;
            int edgeId = goalEdge.originalEdge;
            int nodeId = goalEdge.adjNode;

            if (edgeId == -1 || nodeId == -1 || nodeId > maxNodeId || edgeId > maxEdgeId)
                continue;

            float maxCost = (float) goalEdge.weight;
            float minCost = (float) goalEdge.parent.weight;

            EdgeIteratorState iter = graphHopperStorage.getBaseGraph().getEdgeIteratorState(edgeId, nodeId);

            // edges that are fully inside of the isochrone
            if (isolineCost >= maxCost) {
                // This checks for dead end edges, but we need to include those in small areas to provide realistic
                // results
                if (goalEdge.edge != -2 || useHighDetail) {
                    addBufferedEdgeGeometry(points, minSplitLength, iter, true, goalEdge, bufferSize);
                }
            } else {
                if ((minCost < isolineCost && maxCost >= isolineCost)) {
                    addBorderEdgeGeometry(points, isolineCost, minSplitLength, iter, maxCost, minCost, bufferSize);
                }
            }
        }
        Geometry[] geometries = new Geometry[points.size()];

        for (int i = 0; i < points.size(); ++i) {
            Coordinate c = points.get(i);
            geometries[i] = geometryFactory.createPoint(c);
        }

        return new GeometryCollection(geometries, geometryFactory);
    }

    private void handleFullyReachableCells(Set<Geometry> isochroneGeometries, Set<Integer> fullyReachableCells) {
        //printing for debug
//        StringBuilder cellsPrintStatement = new StringBuilder();
//
//        if (DebugUtility.isDebug()) {
//            cellsPrintStatement.append(System.lineSeparator());
//            cellsPrintStatement.append("{" +
//                    "  \"type\": \"FeatureCollection\"," +
//                    "  \"features\": [");
//            cellsPrintStatement.append(System.lineSeparator());
//        }
        Set<Integer> reachableCellsAndSuperCells = isSupercellsEnabled() ? handleSuperCells(fullyReachableCells) : fullyReachableCells;

        for (int cellId : reachableCellsAndSuperCells) {
            addCellPolygon(cellId, isochroneGeometries);
//            if (DebugUtility.isDebug())
//                cellsPrintStatement.append(printCell(cellStorage.getCellContourOrder(cellId), cellId));
        }
//        if (DebugUtility.isDebug()) {
//            cellsPrintStatement.deleteCharAt(cellsPrintStatement.length() - 2);
//            cellsPrintStatement.append("]}");
//            cellsPrintStatement.append(System.lineSeparator());
//        }
//        LOGGER.debug(cellsPrintStatement.toString());
    }

    private Set<Integer> handleSuperCells(Set<Integer> fullyReachableCells) {
        Set<Integer> reachableCellsAndSuperCells = new HashSet<>();
        Set<Integer> reachableSuperCells = new HashSet<>();
        for (int cellId : fullyReachableCells) {
            int superCell = cellStorage.getSuperCellOfCell(cellId);
            if (superCell != -1 && fullyReachableCells.containsAll(cellStorage.getCellsOfSuperCellAsList(superCell)))
                reachableSuperCells.add(superCell);
            else {
                reachableCellsAndSuperCells.add(cellId);
            }
        }
        for (int cellId : reachableSuperCells) {
            int superCell = cellStorage.getSuperCellOfCell(cellId);
            if (superCell != -1 && reachableSuperCells.containsAll(cellStorage.getCellsOfSuperCellAsList(superCell))) {
                reachableCellsAndSuperCells.add(superCell);
            } else {
                reachableCellsAndSuperCells.add(cellId);
            }
        }
        return reachableCellsAndSuperCells;
    }

    private void addCellPolygon(int cellId, Set<Geometry> isochronePolygons) {
        List<Double> coordinates = cellStorage.getCellContourOrder(cellId);
        if (coordinates.size() % 2 != 0)
            throw new IllegalArgumentException("Coordinate list must contain equal number of lats and lons but has odd numbered size.");
        Coordinate[] cArray = new Coordinate[coordinates.size() / 2];
        //Convert list of doubles (lat0,lon0,lat1,lon1,...) to array of coordinates
        for (int n = cArray.length - 1; n >= 0; n--) {
            cArray[cArray.length - 1 - n] = new Coordinate(coordinates.get(2 * n + 1).floatValue(), coordinates.get(2 * n).floatValue());
        }
        Polygon polygon = geometryFactory.createPolygon(cArray);
        if (polygon.isValid() && !polygon.isEmpty()) {
            isochronePolygons.add(polygon);
        } else
            LOGGER.debug("Poly of cell " + cellId + " is invalid at size " + cArray.length);
    }

    //DEBUG
    private String printCell(List<Double> coordinates, int cellId) {
        if (coordinates.size() < 3)
            return "";
        StringBuilder statement = new StringBuilder();
        statement.append("{\"type\": \"Feature\",\"properties\": {\"name\": \"").append(cellId).append("\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[");
        int i;
        for (i = coordinates.size() - 2; i > 0; i -= 2) {
            statement.append("[").append(String.valueOf(coordinates.get(i + 1)), 0, Math.min(8, String.valueOf(coordinates.get(i + 1)).length())).append(",").append(String.valueOf(coordinates.get(i)), 0, Math.min(8, String.valueOf(coordinates.get(i)).length())).append("],");
        }
        statement.append("[").append(String.valueOf(coordinates.get(coordinates.size() - 1)), 0, Math.min(8, String.valueOf(coordinates.get(coordinates.size() - 1)).length())).append(",").append(String.valueOf(coordinates.get(coordinates.size() - 2)), 0, Math.min(8, String.valueOf(coordinates.get(coordinates.size() - 2)).length())).append("]");

        statement.append("]]}},");
        statement.append(System.lineSeparator());
        return statement.toString();
    }

    private List<GHIntObjectHashMap<SPTEntry>> separateDisconnected(IntObjectMap<SPTEntry> map) {
        List<GHIntObjectHashMap<SPTEntry>> disconnectedCells = new ArrayList<>();
        EdgeExplorer edgeExplorer = queryGraph.createEdgeExplorer();
        Queue<Integer> queue = new ArrayDeque<>();
        IntHashSet visitedNodes = new IntHashSet(map.size());
        for (IntObjectCursor<SPTEntry> entry : map) {
            if (visitedNodes.contains(entry.key))
                continue;
            visitedNodes.add(entry.key);
            queue.offer(entry.key);

            GHIntObjectHashMap<SPTEntry> connectedCell = new GHIntObjectHashMap<>();

            while (!queue.isEmpty()) {
                int currentNode = queue.poll();
                connectedCell.put(currentNode, map.get(currentNode));
                EdgeIterator edgeIterator = edgeExplorer.setBaseNode(currentNode);

                while (edgeIterator.next()) {
                    int nextNode = edgeIterator.getAdjNode();
                    if (visitedNodes.contains(nextNode) || !map.containsKey(nextNode))
                        continue;
                    queue.offer(nextNode);
                    connectedCell.put(nextNode, map.get(nextNode));
                    visitedNodes.add(nextNode);
                }
            }
            disconnectedCells.add(connectedCell);
        }
        disconnectedCells.sort((a1, a2) -> a2.size() - a1.size());
        return disconnectedCells;
    }
}
