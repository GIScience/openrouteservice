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
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint3D;
import org.apache.log4j.Logger;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.isochrones.GraphEdgeMapFinder;
import org.heigit.ors.isochrones.IsochroneMap;
import org.heigit.ors.isochrones.IsochroneSearchParameters;
import org.heigit.ors.isochrones.builders.AbstractIsochroneMapBuilder;
import org.heigit.ors.routing.graphhopper.extensions.AccessibilityMap;
import org.heigit.ors.util.GeomUtility;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

public class ConcaveBallsIsochroneMapBuilder extends AbstractIsochroneMapBuilder {
    private static final Logger LOGGER = Logger.getLogger(ConcaveBallsIsochroneMapBuilder.class.getName());

    @Override
    public Logger getLogger() {
        return LOGGER;
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

        double maxSpeed = determineMaxSpeed();
        double meanSpeed = determineMeanSpeed(maxSpeed);

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

            float smoothingFactor = parameters.getSmoothingFactor();
            var smoothingDistance = convertSmoothingFactorToDistance(smoothingFactor, maxRadius);
            var smoothingDistanceMeter = GeomUtility.degreesToMetres(smoothingDistance);

            GeometryCollection points = buildIsochrone(edgeMap, isoPoints, isoValue, prevCost, isochronesDifference, 0.85, smoothingDistanceMeter);

            if (LOGGER.isDebugEnabled()) {
                sw.stop();
                LOGGER.debug(i + " Find points: " + sw.getSeconds() + " " + points.getNumPoints());
                sw = new StopWatch();
                sw.start();
            }

            addIsochrone(isochroneMap, points, isoValue, meanRadius, smoothingDistance);

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Build concave hull total: " + sw.stop().getSeconds());

            prevCost = isoValue;
        }

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Total time: " + swTotal.stop().getSeconds());

        return isochroneMap;
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

    private GeometryCollection buildIsochrone(AccessibilityMap edgeMap, List<Coordinate> points,
                                        double isolineCost, double prevCost, double isochronesDifference,
                                        double detailedGeomFactor,
                                        double minSplitLength) {
        IntObjectMap<SPTEntry> map = edgeMap.getMap();

        points.clear();

        if (previousIsochronePolygon != null)
            points.addAll(createCoordinateListFromPolygon(previousIsochronePolygon));

        GraphHopperStorage graph = searchContext.getGraphHopper().getGraphHopperStorage();
        NodeAccess nodeAccess = graph.getNodeAccess();
        int maxNodeId = graph.getNodes() - 1;
        int maxEdgeId = graph.getEdges() - 1;

        double bufferSize = 0.0018;
        double detailedZone = isolineCost * detailedGeomFactor;

        boolean useHighDetail = map.size() < 1000 || isochronesDifference < 1000;

        if (useHighDetail) {
            bufferSize = 0.00018;
        }

        int nodeId;
        int edgeId;

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

            EdgeIteratorState iter = graph.getEdgeIteratorState(edgeId, nodeId);

            // edges that are fully inside the isochrone
            if (isolineCost >= maxCost) {
                // This checks for dead end edges, but we need to include those in small areas to provide realistic
                // results
                if (goalEdge.edge != -2 || useHighDetail) {
                    double edgeDist = iter.getDistance();
                    boolean detailedShape = (edgeDist > 200);
                    if (maxCost >= detailedZone || detailedShape) {
                        if (LOGGER.isDebugEnabled())
                            sw.start();
                        addBufferedEdgeGeometry(points, minSplitLength, iter, detailedShape, goalEdge, bufferSize);
                        if (LOGGER.isDebugEnabled())
                            sw.stop();
                    } else {
                        addPoint(points, nodeAccess.getLon(nodeId), nodeAccess.getLat(nodeId));
                    }
                }
            } else {
                if ((minCost < isolineCost && maxCost >= isolineCost)) {
                    if (LOGGER.isDebugEnabled())
                        sw.start();
                    addBorderEdgeGeometry(points, isolineCost, minSplitLength, iter, maxCost, minCost, bufferSize);
                    if (LOGGER.isDebugEnabled())
                        sw.stop();
                }
            }
        }
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Expanding edges " + sw.getSeconds());

        Geometry[] geometries = new Geometry[points.size()];

        for (int i = 0; i < points.size(); ++i) {
            Coordinate c = points.get(i);
            geometries[i] = geometryFactory.createPoint(c);
        }

        return new GeometryCollection(geometries, geometryFactory);
    }
}
