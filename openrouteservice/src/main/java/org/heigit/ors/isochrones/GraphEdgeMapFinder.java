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
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.TimeDependentFastestWeighting;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.HelperORS;
import com.graphhopper.util.PMap;
import com.graphhopper.util.shapes.GHPoint3D;
import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.algorithms.DijkstraCostCondition;
import org.heigit.ors.routing.algorithms.TDDijkstraCostCondition;
import org.heigit.ors.routing.graphhopper.extensions.AccessibilityMap;
import org.heigit.ors.routing.graphhopper.extensions.ORSEdgeFilterFactory;
import org.heigit.ors.routing.graphhopper.extensions.weighting.DistanceWeighting;
import org.heigit.ors.routing.traffic.TrafficSpeedCalculator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;

public class GraphEdgeMapFinder {
    private GraphEdgeMapFinder() {
    }

    public static AccessibilityMap findEdgeMap(RouteSearchContext searchCntx, IsochroneSearchParameters parameters) throws Exception {
        GraphHopper gh = searchCntx.getGraphHopper();
        FlagEncoder encoder = searchCntx.getEncoder();
        GraphHopperStorage graph = gh.getGraphHopperStorage();

        ORSEdgeFilterFactory edgeFilterFactory = new ORSEdgeFilterFactory();
        EdgeFilter edgeFilter = edgeFilterFactory.createEdgeFilter(searchCntx.getProperties(), encoder, graph);

        Coordinate loc = parameters.getLocation();
        QueryResult res = gh.getLocationIndex().findClosest(loc.y, loc.x, edgeFilter);

        GHPoint3D snappedPosition = res.getSnappedPoint();

        int fromId = res.getClosestNode();

        if (fromId == -1)
            throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "The closest node is null.");
        //TODO make dependent on parameters
        Weighting weighting = createWeighting(parameters, encoder, graph);

        if (parameters.isTimeDependent()) {
            //Time-dependent means traffic dependent for isochrones (for now)
            TrafficSpeedCalculator trafficSpeedCalculator = new TrafficSpeedCalculator();
            trafficSpeedCalculator.init(graph, encoder);
            ((TimeDependentFastestWeighting) weighting).setSpeedCalculator(trafficSpeedCalculator);
            if (HelperORS.getTurnCostExtensions(graph.getExtension()) != null)
                weighting = new TurnWeighting(weighting, HelperORS.getTurnCostExtensions(graph.getExtension()));
            TDDijkstraCostCondition tdDijkstraCostCondition = new TDDijkstraCostCondition(graph, weighting, parameters.getMaximumRange(), parameters.getReverseDirection(),
                    TraversalMode.NODE_BASED);
            tdDijkstraCostCondition.setEdgeFilter(edgeFilter);
            //Time is defined to be in UTC + 1 because original implementation was for German traffic data
            //If changed, this needs to be adapted in the traffic storage, too
            ZonedDateTime zdt = parameters.getRouteParameters().getDeparture().atZone(ZoneId.of("Europe/Berlin"));
            trafficSpeedCalculator.setZonedDateTime(zdt);
            tdDijkstraCostCondition.calcPath(fromId, Integer.MIN_VALUE, zdt.toInstant().toEpochMilli());

            IntObjectMap<SPTEntry> edgeMap = tdDijkstraCostCondition.getMap();
            int sumEntries = 0;
            double sumPercentages = 0;
            for (Map.Entry<Double, Double> entry : trafficSpeedCalculator.changedSpeedCount.entrySet()) {
                sumEntries += entry.getValue();
                sumPercentages += (trafficSpeedCalculator.changedSpeed.get(entry.getKey()) / entry.getValue()) / entry.getKey() * entry.getValue();
//				System.out.println("Speed " + entry.getKey() + " replaced by average traffic: " + trafficSpeedCalculator.changedSpeed.get(entry.getKey()) / entry.getValue() + " with number of entries " + entry.getValue());
            }
            trafficSpeedCalculator.changedSpeedCount.entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .forEach(entry -> System.out.println("Speed " + entry.getKey() + " replaced by average traffic: " + trafficSpeedCalculator.changedSpeed.get(entry.getKey()) / entry.getValue() + " over total distance of " + (int) entry.getValue().doubleValue() / 1000 + "km"));
            System.out.println("Average traffic speed as percentage of normal speed " + sumPercentages / sumEntries);
            System.out.println("Total distance replaced: " + (int) sumEntries / 1000 + "km");
            return new AccessibilityMap(edgeMap, tdDijkstraCostCondition.getCurrentEdge(), snappedPosition);
        } else {
            // IMPORTANT: It only works with TraversalMode.NODE_BASED.
            DijkstraCostCondition dijkstraAlg = new DijkstraCostCondition(graph, weighting, parameters.getMaximumRange(), parameters.getReverseDirection(),
                    TraversalMode.NODE_BASED);
            dijkstraAlg.setEdgeFilter(edgeFilter);
            dijkstraAlg.calcPath(fromId, Integer.MIN_VALUE);

            IntObjectMap<SPTEntry> edgeMap = dijkstraAlg.getMap();
            return new AccessibilityMap(edgeMap, dijkstraAlg.getCurrentEdge(), snappedPosition);
        }
    }

    private static Weighting createWeighting(IsochroneSearchParameters parameters, FlagEncoder encoder, GraphHopperStorage graph) {
        Weighting weighting = parameters.getRangeType() == TravelRangeType.TIME ?
                parameters.isTimeDependent() ? new TimeDependentFastestWeighting(encoder, new PMap()) : new FastestWeighting(encoder)
                : new DistanceWeighting(encoder);

        return weighting;
    }
}
