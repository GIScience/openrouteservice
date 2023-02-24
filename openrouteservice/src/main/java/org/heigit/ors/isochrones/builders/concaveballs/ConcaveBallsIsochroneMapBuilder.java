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
package org.heigit.ors.isochrones.builders.concaveballs;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HikeFlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint3D;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.apache.log4j.Logger;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.isochrones.GraphEdgeMapFinder;
import org.heigit.ors.isochrones.Isochrone;
import org.heigit.ors.isochrones.IsochroneMap;
import org.heigit.ors.isochrones.IsochroneSearchParameters;
import org.heigit.ors.isochrones.builders.IsochroneMapBuilder;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.graphhopper.extensions.AccessibilityMap;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FootFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.ORSAbstractFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.bike.CommonBikeFlagEncoder;
import org.heigit.ors.util.DebugUtility;
import org.heigit.ors.util.GeomUtility;
import org.opensphere.geometry.algorithm.ConcaveHullOpenSphere;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class ConcaveBallsIsochroneMapBuilder implements IsochroneMapBuilder {
    private static final Logger LOGGER = Logger.getLogger(ConcaveBallsIsochroneMapBuilder.class.getName());
    private static final boolean BUFFERED_OUTPUT = true;
    private static DistanceCalc dcFast = new DistancePlaneProjection();
    private double searchWidth = 0.0007;
    private double pointWidth = 0.0005;
    private double visitorThreshold = 0.0013;
    private Envelope searchEnv = new Envelope();
    private GeometryFactory geometryFactory;
    private PointItemVisitor visitor = null;
    private List<Coordinate> prevIsoPoints = null;
    private TreeSet<Coordinate> treeSet;

    private RouteSearchContext searchContext;

    public void initialize(RouteSearchContext searchContext) {
        geometryFactory = new GeometryFactory();
        this.searchContext = searchContext;
    }

    public IsochroneMap compute(IsochroneSearchParameters parameters) throws Exception {
        StopWatch swTotal = null;
        StopWatch sw = null;
        if (LOGGER.isDebugEnabled()) {
            swTotal = new StopWatch();
            swTotal.start();
            sw = new StopWatch();
            sw.start();
        }

        GraphHopperStorage graph = searchContext.getGraphHopper().getGraphHopperStorage();
        String graphdate = graph.getProperties().get("datareader.import.date");

        // 1. Find all graph edges for a given cost.
        FlagEncoder encoder = searchContext.getEncoder();
        double maxSpeed = encoder.getMaxSpeed();

        if (encoder instanceof FootFlagEncoder || encoder instanceof HikeFlagEncoder) {
            // in the GH FootFlagEncoder, the maximum speed is set to 15km/h which is way too high
            maxSpeed = 4;
        }

        if (encoder instanceof WheelchairFlagEncoder) {
            maxSpeed = WheelchairFlagEncoder.MEAN_SPEED;
        }

        double meanSpeed = maxSpeed;
        if (encoder instanceof ORSAbstractFlagEncoder) {
            meanSpeed = ((ORSAbstractFlagEncoder) encoder).getMeanSpeed();
        }
        if (encoder instanceof CommonBikeFlagEncoder) {
            meanSpeed = ((CommonBikeFlagEncoder) encoder).getMeanSpeed();
        }

        AccessibilityMap edgeMap = GraphEdgeMapFinder.findEdgeMap(searchContext, parameters);

        GHPoint3D point = edgeMap.getSnappedPosition();

        Coordinate loc = (point == null) ? parameters.getLocation() : new Coordinate(point.lon, point.lat);

        IsochroneMap isochroneMap = new IsochroneMap(parameters.getTravellerId(), loc);

        isochroneMap.setGraphDate(graphdate);

        if (LOGGER.isDebugEnabled()) {
            sw.stop();

            LOGGER.debug("Find edges: " + sw.getSeconds());
        }

        if (edgeMap.isEmpty())
            return isochroneMap;

        treeSet = new TreeSet<>();

        List<Coordinate> isoPoints = new ArrayList<>((int) (1.2 * edgeMap.getMap().size()));

        if (LOGGER.isDebugEnabled()) {
            sw = new StopWatch();
            sw.start();
        }

        markDeadEndEdges(edgeMap);

        if (LOGGER.isDebugEnabled()) {
            sw.stop();
            LOGGER.debug("Mark dead ends: " + sw.getSeconds());
        }

        int nRanges = parameters.getRanges().length;

        double metersPerSecond = maxSpeed / 3.6;
        // only needed for reachfactor property
        double meanMetersPerSecond = meanSpeed / 3.6;

        double prevCost = 0;
        for (int i = 0; i < nRanges; i++) {
            double isoValue = parameters.getRanges()[i];
            double isochronesDifference = parameters.getRanges()[i];
            if (i > 0)
                isochronesDifference = isochronesDifference - parameters.getRanges()[i - 1];

            float smoothingFactor = parameters.getSmoothingFactor();
            TravelRangeType isochroneType = parameters.getRangeType();

            if (LOGGER.isDebugEnabled()) {
                sw = new StopWatch();
                sw.start();
            }

            double maxRadius;
            double meanRadius;
            if (isochroneType == TravelRangeType.DISTANCE) {
                maxRadius = isoValue;
                meanRadius = isoValue;
            } else {
                maxRadius = metersPerSecond * isoValue;
                meanRadius = meanMetersPerSecond * isoValue;
                isochronesDifference = metersPerSecond * isochronesDifference;
            }

            Coordinate[] points = buildIsochrone(edgeMap, isoPoints, loc.x, loc.y, isoValue, prevCost, isochronesDifference, 0.85);

            if (LOGGER.isDebugEnabled()) {
                sw.stop();
                LOGGER.debug(i + " Find points: " + sw.getSeconds() + " " + points.length);

                sw = new StopWatch();
                sw.start();
            }

            addIsochrone(isochroneMap, points, isoValue, maxRadius, meanRadius, smoothingFactor);

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Build concave hull total: " + sw.stop().getSeconds());

            prevCost = isoValue;
        }

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Total time: " + swTotal.stop().getSeconds());

        return isochroneMap;
    }

    /**
     * Converts the smoothing factor into a distance (which can be used in algorithms for generating isochrone polygons).
     * The distance value returned is dependent on the radius and smoothing factor.
     *
     * @param smoothingFactor A factor that should be used in the smoothing process. Lower numbers produce a smaller
     *                        distance (and so likely a more detailed polygon)
     * @param maxRadius       The maximum radius of the isochrone (in metres)
     * @return
     */
    private double convertSmoothingFactorToDistance(float smoothingFactor, double maxRadius) {
        final double MINIMUM_DISTANCE = 0.006;

        if (smoothingFactor == -1) {
            // No user defined smoothing factor, so use defaults

            // For shorter isochrones, we want to use a smaller minimum distance else we get inaccurate results
            if (maxRadius < 5000)
                return MINIMUM_DISTANCE;

            // Use a default length (~1333m)
            return 0.012;
        }

        double intervalDegrees = GeomUtility.metresToDegrees(maxRadius);
        double maxLength = (intervalDegrees / 100f) * smoothingFactor;

        if (maxLength < MINIMUM_DISTANCE)
            maxLength = MINIMUM_DISTANCE;
        return maxLength;
    }

    private void addIsochrone(IsochroneMap isochroneMap, Coordinate[] points, double isoValue, double maxRadius, double meanRadius, float smoothingFactor) {
        Geometry[] geometries = new Geometry[points.length];
        for (int i = 0; i < points.length; ++i) {
            Coordinate c = points[i];
            geometries[i] = geometryFactory.createPoint(c);
        }
        GeometryCollection geometry = new GeometryCollection(geometries, geometryFactory);

        if (points.length == 0)
            return;
        StopWatch sw = new StopWatch();
        if (LOGGER.isDebugEnabled()) {
            sw = new StopWatch();
            sw.start();
        }
        ConcaveHullOpenSphere concaveHullShell = new ConcaveHullOpenSphere(geometry, convertSmoothingFactorToDistance(smoothingFactor, maxRadius), false);
        Geometry shellGeometry = concaveHullShell.getConcaveHull();
        if (shellGeometry instanceof GeometryCollection) {
            GeometryCollection geomColl = (GeometryCollection) shellGeometry;
            if (geomColl.isEmpty())
                return;
        }
        Polygon polyShell = (Polygon) shellGeometry;
        copyConvexHullPoints(polyShell);

        if (LOGGER.isDebugEnabled()) {
            sw.stop();
            LOGGER.debug("Build shell concave hull " + sw.getSeconds());

            sw = new StopWatch();
            sw.start();
        }
        isochroneMap.addIsochrone(new Isochrone(polyShell, isoValue, meanRadius));

        if (LOGGER.isDebugEnabled()) {
            sw.stop();
            LOGGER.debug("Adding holes " + sw.getSeconds());

            sw = new StopWatch();
            sw.start();
        }
    }

    private void markDeadEndEdges(AccessibilityMap edgeMap) {
        IntObjectMap<SPTEntry> map = edgeMap.getMap();
        IntObjectMap<Integer> result = new GHIntObjectHashMap<>(map.size() / 20);

        for (IntObjectCursor<SPTEntry> entry : map) {
            SPTEntry edge = entry.value;
            if (edge.originalEdge == -1)
                continue;

            result.put(edge.parent.originalEdge, 1);
        }

        for (IntObjectCursor<SPTEntry> entry : map) {
            SPTEntry edge = entry.value;
            if (edge.originalEdge == -1)
                continue;

            if (!result.containsKey(edge.originalEdge))
                edge.edge = -2;
        }
    }

    public Boolean addPoint(List<Coordinate> points, Quadtree tree, double lon, double lat, boolean checkNeighbours) {
        if (checkNeighbours) {
            visitor.setPoint(lon, lat);
            searchEnv.init(lon - searchWidth, lon + searchWidth, lat - searchWidth, lat + searchWidth);
            tree.query(searchEnv, visitor);
            if (!visitor.isNeighbourFound()) {
                Coordinate p = new Coordinate(lon, lat);

                if (!treeSet.contains(p)) {
                    Envelope env = new Envelope(lon - pointWidth, lon + pointWidth, lat - pointWidth, lat + pointWidth);
                    tree.insert(env, p);
                    points.add(p);
                    treeSet.add(p);

                    return true;
                }
            }
        } else {
            Coordinate p = new Coordinate(lon, lat);
            if (!treeSet.contains(p)) {
                Envelope env = new Envelope(lon - pointWidth, lon + pointWidth, lat - pointWidth, lat + pointWidth);
                tree.insert(env, p);
                points.add(p);
                treeSet.add(p);

                return true;
            }
        }

        return false;
    }

    private void addBufferPoints(List<Coordinate> points, Quadtree tree, double lon0, double lat0, double lon1,
                                 double lat1, boolean addLast, boolean checkNeighbours, double bufferSize) {
        double dx = (lon0 - lon1);
        double dy = (lat0 - lat1);
        double normLength = Math.sqrt((dx * dx) + (dy * dy));
        double scale = bufferSize / normLength;

        double dx2 = -dy * scale;
        double dy2 = dx * scale;

        addPoint(points, tree, lon0 + dx2, lat0 + dy2, checkNeighbours);
        addPoint(points, tree, lon0 - dx2, lat0 - dy2, checkNeighbours);

        // add a middle point if two points are too far from each other
        if (normLength > 2 * bufferSize) {
            addPoint(points, tree, (lon0 + lon1) / 2.0 + dx2, (lat0 + lat1) / 2.0 + dy2, checkNeighbours);
            addPoint(points, tree, (lon0 + lon1) / 2.0 - dx2, (lat0 + lat1) / 2.0 - dy2, checkNeighbours);
        }

        if (addLast) {
            addPoint(points, tree, lon1 + dx2, lat1 + dy2, checkNeighbours);
            addPoint(points, tree, lon1 - dx2, lat1 - dy2, checkNeighbours);
        }
    }

    private Coordinate[] buildIsochrone(AccessibilityMap edgeMap, List<Coordinate> points, double lon, double lat,
                                        double isolineCost, double prevCost, double isochronesDifference, double detailedGeomFactor) {
        IntObjectMap<SPTEntry> map = edgeMap.getMap();

        points.clear();
        treeSet.clear();

        if (prevIsoPoints != null)
            points.addAll(prevIsoPoints);

        GraphHopperStorage graph = searchContext.getGraphHopper().getGraphHopperStorage();
        NodeAccess nodeAccess = graph.getNodeAccess();
        int maxNodeId = graph.getNodes() - 1;
        int maxEdgeId = graph.getEdges() - 1;

        double bufferSize = 0.0018;
        Quadtree qtree = new Quadtree();
        visitor = new PointItemVisitor(lon, lat, visitorThreshold);
        double detailedZone = isolineCost * detailedGeomFactor;

        double defaultSearchWidth = 0.0008;
        double defaulPointWidth = 0.005;
        double defaultVisitorThreshold = 0.0040;

        // make results a bit more precise for regions with low data density
        if (map.size() < 10000) {
            defaultSearchWidth = 0.0008;
            defaulPointWidth = 0.005;
            defaultVisitorThreshold = 0.0025;
        }

        boolean useHighDetail = map.size() < 1000 || isochronesDifference < 1000;

        if (useHighDetail) {
            bufferSize = 0.00018;
            defaultVisitorThreshold = 0.000005;
        }

        int nodeId;
        int edgeId;

        int minSplitLength = 20;
        int maxSplitLength = 20000;
        StopWatch sw = new StopWatch();

        for (IntObjectCursor<SPTEntry> entry : map) {
            SPTEntry goalEdge = entry.value;
            edgeId = goalEdge.originalEdge;
            nodeId = goalEdge.adjNode;

            if (edgeId == -1 || nodeId == -1 || nodeId > maxNodeId || edgeId > maxEdgeId)
                continue;

            float maxCost = (float) goalEdge.weight;
            float minCost = (float) goalEdge.parent.weight;

            // ignore all edges that have been considered in the previous step. We do not want to do this for small
            // isochrones as the edge may have more than one range on it in that case
            if (minCost < prevCost && isochronesDifference > 1000)
                continue;

            searchWidth = defaultSearchWidth;
            visitorThreshold = defaultVisitorThreshold;
            pointWidth = defaulPointWidth;

            visitor.setThreshold(visitorThreshold);

            EdgeIteratorState iter = graph.getEdgeIteratorState(edgeId, nodeId);

            // edges that are fully inside the isochrone
            if (isolineCost >= maxCost) {
                // This checks for dead end edges, but we need to include those in small areas to provide realistic
                // results
                if (goalEdge.edge != -2 || useHighDetail) {
                    double edgeDist = iter.getDistance();
                    if (((maxCost >= detailedZone && maxCost <= isolineCost) || edgeDist > 200)) {
                        boolean detailedShape = (edgeDist > 200);
                        // always use mode=3, since other ones do not provide correct results
                        PointList pl = iter.fetchWayGeometry(FetchMode.ALL);

                        if (LOGGER.isDebugEnabled()) {
                            sw.start();
                        }
                        PointList expandedPoints = new PointList(pl.size(), pl.is3D());

                        for (int i = 0; i < pl.size() - 1; i++)
                            splitEdge(pl.get(i), pl.get(i + 1), expandedPoints, minSplitLength, maxSplitLength);
                        pl.add(expandedPoints);
                        if (LOGGER.isDebugEnabled()) {
                            sw.stop();
                        }
                        int size = pl.size();
                        if (size > 0) {
                            double lat0 = pl.getLat(0);
                            double lon0 = pl.getLon(0);
                            double lat1;
                            double lon1;

                            if (detailedShape && BUFFERED_OUTPUT) {
                                for (int i = 1; i < size; ++i) {
                                    lat1 = pl.getLat(i);
                                    lon1 = pl.getLon(i);

                                    addBufferPoints(points, qtree, lon0, lat0, lon1, lat1, goalEdge.edge < 0 && i == size - 1, true, bufferSize);

                                    lon0 = lon1;
                                    lat0 = lat1;
                                }
                            } else {
                                for (int i = 1; i < size; ++i) {
                                    lat1 = pl.getLat(i);
                                    lon1 = pl.getLon(i);

                                    addPoint(points, qtree, lon0, lat0, true);
                                    if (i == size - 1)
                                        addPoint(points, qtree, lon1, lat1, true);

                                    lon0 = lon1;
                                    lat0 = lat1;
                                }
                            }
                        }
                    } else {
                        addPoint(points, qtree, nodeAccess.getLon(nodeId), nodeAccess.getLat(nodeId), true);
                    }
                }
            } else {
                if ((minCost < isolineCost && maxCost >= isolineCost)) {

                    PointList pl = iter.fetchWayGeometry(FetchMode.ALL);

                    PointList expandedPoints = new PointList(pl.size(), pl.is3D());
                    if (LOGGER.isDebugEnabled()) {
                        sw.start();
                    }
                    for (int i = 0; i < pl.size() - 1; i++)
                        splitEdge(pl.get(i), pl.get(i + 1), expandedPoints, minSplitLength, maxSplitLength);
                    pl.add(expandedPoints);
                    if (LOGGER.isDebugEnabled()) {
                        sw.stop();
                    }
                    int size = pl.size();

                    if (size > 0) {
                        double edgeCost = maxCost - minCost;
                        double edgeDist = iter.getDistance();
                        double costPerMeter = edgeCost / edgeDist;
                        double distPolyline = 0.0;

                        double lat0 = pl.getLat(0);
                        double lon0 = pl.getLon(0);
                        double lat1;
                        double lon1;

                        for (int i = 1; i < size; ++i) {
                            lat1 = pl.getLat(i);
                            lon1 = pl.getLon(i);

                            distPolyline += dcFast.calcDist(lat0, lon0, lat1, lon1);

                            if (BUFFERED_OUTPUT) {
                                double distCost = minCost + distPolyline * costPerMeter;
                                if (distCost >= isolineCost) {
                                    double segLength = (1 - (distCost - isolineCost) / edgeCost);
                                    double lon2 = lon0 + segLength * (lon1 - lon0);
                                    double lat2 = lat0 + segLength * (lat1 - lat0);

                                    addBufferPoints(points, qtree, lon0, lat0, lon2, lat2, true, false, bufferSize);

                                    break;
                                } else {
                                    addBufferPoints(points, qtree, lon0, lat0, lon1, lat1, false, true, bufferSize);
                                }
                            } else {
                                addPoint(points, qtree, lon0, lat0, true);
                            }

                            lat0 = lat1;
                            lon0 = lon1;
                        }
                    }
                }
            }
        }
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Expanding edges " + sw.getSeconds());

        Coordinate[] coordinates = new Coordinate[points.size()];

        for (int i = 0; i < points.size(); ++i) {
            Coordinate c = points.get(i);
            coordinates[i] = c;
        }
        return coordinates;
    }

    private void copyConvexHullPoints(Polygon poly) {
        LineString ring = poly.getExteriorRing();
        if (prevIsoPoints == null)
            prevIsoPoints = new ArrayList<>(ring.getNumPoints());
        else
            prevIsoPoints.clear();
        for (int i = 0; i < ring.getNumPoints(); ++i) {
            Point p = ring.getPointN(i);
            prevIsoPoints.add(new Coordinate(p.getX(), p.getY()));
        }
    }

    /**
     * Splits a line between two points of the distance is longer than a limit
     *
     * @param point0 point0 of line
     * @param point1 point1 of line
     * @param minlim limit above which the edge will be split (in meters)
     * @param maxlim limit above which the edge will NOT be split anymore (in meters)
     */
    private void splitEdge(GHPoint3D point0, GHPoint3D point1, PointList pointList, double minlim, double maxlim) {
        //No need to consider elevation
        double lat0 = point0.getLat();
        double lon0 = point0.getLon();
        double lat1 = point1.getLat();
        double lon1 = point1.getLon();
        double dist = dcFast.calcDist(lat0, lon0, lat1, lon1);
        boolean is3D = pointList.is3D();

        if (dist > minlim && dist < maxlim) {
            int n = (int) Math.ceil(dist / minlim);
            for (int i = 1; i < n; i++) {
                if (is3D)
                    pointList.add(lat0 + i * (lat1 - lat0) / n, lon0 + i * (lon1 - lon0) / n, 0);
                else
                    pointList.add(lat0 + i * (lat1 - lat0) / n, lon0 + i * (lon1 - lon0) / n);
            }
        }
    }
}
