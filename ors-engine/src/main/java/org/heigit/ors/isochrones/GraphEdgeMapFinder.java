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
package org.heigit.ors.isochrones;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.shapes.GHPoint3D;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.algorithms.DijkstraCostCondition;
import org.heigit.ors.routing.algorithms.TDDijkstraCostCondition;
import org.heigit.ors.routing.graphhopper.extensions.AccessibilityMap;
import org.heigit.ors.routing.graphhopper.extensions.ORSEdgeFilterFactory;
import org.heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import org.heigit.ors.routing.traffic.TrafficSpeedCalculator;
import org.heigit.ors.util.ProfileTools;
import org.locationtech.jts.geom.Coordinate;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class GraphEdgeMapFinder {
    private GraphEdgeMapFinder() {
    }

    public static AccessibilityMap findEdgeMap(RouteSearchContext searchCntx, IsochroneSearchParameters parameters) throws Exception {
        GraphHopper gh = searchCntx.getGraphHopper();
        FlagEncoder encoder = searchCntx.getEncoder();
        GraphHopperStorage graph = gh.getGraphHopperStorage();
        EncodingManager encodingManager = gh.getEncodingManager();
        Weighting weighting = new ORSWeightingFactory(graph, encodingManager).createIsochroneWeighting(searchCntx, parameters.getRangeType());
        String profileName = ProfileTools.makeProfileName(encoder.toString(), weighting.getName(), false);
        EdgeFilter defaultSnapFilter = new DefaultSnapFilter(weighting, encodingManager.getBooleanEncodedValue(Subnetwork.key(profileName)));
        ORSEdgeFilterFactory edgeFilterFactory = new ORSEdgeFilterFactory();
        EdgeFilter edgeFilter = edgeFilterFactory.createEdgeFilter(searchCntx.getProperties(), encoder, graph, defaultSnapFilter);

        Coordinate loc = parameters.getLocation();
        Snap res = gh.getLocationIndex().findClosest(loc.y, loc.x, edgeFilter);
        List<Snap> snaps = new ArrayList<>(1);
        snaps.add(res);
        QueryGraph queryGraph = QueryGraph.create(graph, snaps);

        GHPoint3D snappedPosition = res.getSnappedPoint();

        int fromId = res.getClosestNode();

        if (fromId == -1)
            throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "The closest node is null.");

        if (parameters.isTimeDependent()) {
            return calculateTimeDependentAccessibilityMap(parameters, encoder, graph, edgeFilter, queryGraph, snappedPosition, fromId, weighting);
        } else {
            // IMPORTANT: It only works with TraversalMode.NODE_BASED.
            DijkstraCostCondition dijkstraAlg = new DijkstraCostCondition(queryGraph, weighting, parameters.getMaximumRange(), parameters.getReverseDirection(),
                    TraversalMode.NODE_BASED);
            dijkstraAlg.setEdgeFilter(edgeFilter);
            dijkstraAlg.calcPath(fromId, Integer.MIN_VALUE);

            IntObjectMap<SPTEntry> edgeMap = dijkstraAlg.getMap();
            return new AccessibilityMap(edgeMap, dijkstraAlg.getCurrentEdge(), snappedPosition);
        }
    }

    /**
     * Calculate all nodes that are within the reach of the maximum range and return a map of them.
     *
     * @param parameters      IsochroneSearchParameters
     * @param encoder         FlagEncoder
     * @param graph           GraphHopperStorage
     * @param edgeFilter      The EdgeFilter to be used for finding the nodes
     * @param queryGraph      Graph containing all normal nodes and virtual node of the queried location
     * @param snappedPosition Position the query has been snapped to on the querygraph
     * @param fromId          origin of query
     * @param weighting       weighting to be used
     * @return accessibility map containing all reachable nodes
     */
    private static AccessibilityMap calculateTimeDependentAccessibilityMap(IsochroneSearchParameters parameters, FlagEncoder encoder, GraphHopperStorage graph, EdgeFilter edgeFilter, QueryGraph queryGraph, GHPoint3D snappedPosition, int fromId, Weighting weighting) {
        //Time-dependent means traffic dependent for isochrones (for now)
        TrafficSpeedCalculator trafficSpeedCalculator = new TrafficSpeedCalculator(weighting.getSpeedCalculator());
        trafficSpeedCalculator.init(graph, encoder);
        weighting.setSpeedCalculator(trafficSpeedCalculator);
        TDDijkstraCostCondition tdDijkstraCostCondition = new TDDijkstraCostCondition(queryGraph, weighting, parameters.getMaximumRange(), parameters.getReverseDirection(),
                TraversalMode.NODE_BASED);
        tdDijkstraCostCondition.setEdgeFilter(edgeFilter);
        //Time is defined to be in UTC + 1 because original implementation was for German traffic data
        //If changed, this needs to be adapted in the traffic storage, too
        ZonedDateTime zdt = parameters.getRouteParameters().getDeparture().atZone(trafficSpeedCalculator.getZoneId());
        trafficSpeedCalculator.setZonedDateTime(zdt);
        int toId = parameters.getReverseDirection() ? fromId : Integer.MIN_VALUE;
        fromId = parameters.getReverseDirection() ? Integer.MIN_VALUE : fromId;
        tdDijkstraCostCondition.calcPath(fromId, toId, zdt.toInstant().toEpochMilli());
        IntObjectMap<SPTEntry> edgeMap = tdDijkstraCostCondition.getMap();
        return new AccessibilityMap(edgeMap, tdDijkstraCostCondition.getCurrentEdge(), snappedPosition);
    }
}
