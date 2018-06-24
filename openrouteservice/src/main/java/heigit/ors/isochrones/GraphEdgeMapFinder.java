/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.isochrones;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.storage.index.QueryResult;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.common.TravelRangeType;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.routing.RouteSearchContext;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.algorithms.DijkstraCostCondition;
import heigit.ors.routing.graphhopper.extensions.AccessibilityMap;
import heigit.ors.routing.graphhopper.extensions.weighting.DistanceWeighting;

public class GraphEdgeMapFinder {
	
   public static AccessibilityMap findEdgeMap(RouteSearchContext searchCntx, IsochroneSearchParameters parameters) throws Exception {
		GraphHopper gh = searchCntx.getGraphHopper();
	    FlagEncoder encoder = searchCntx.getEncoder();
		GraphHopperStorage graph = gh.getGraphHopperStorage();

		Coordinate loc = parameters.getLocation();
		QueryResult res = gh.getLocationIndex().findClosest(loc.y, loc.x, searchCntx.getEdgeFilter());
		int fromId = res.getClosestNode();

		if (fromId == -1)
			throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "The closest node is null.");
	
		Weighting weighting = null;
		
		if (parameters.getRangeType() == TravelRangeType.Time)
		{
			double maxSpeed = -1;
			RouteSearchParameters routeParams = parameters.getRouteParameters();
			if (routeParams != null)
				maxSpeed = routeParams.getMaximumSpeed();
			
			HintsMap hints = new HintsMap();
			hints.put("max_speed", maxSpeed);
		    weighting = new FastestWeighting(encoder, hints);
			/*Weighting[] weightings = new Weighting[] {new AccelerationWeighting(encoder, hints, graph)};
			weighting = new AdditionWeighting(weightings,  new FastestWeighting(encoder, hints), encoder, hints, graph);*/
		}
		else
		{
			weighting  = new DistanceWeighting(encoder);
		}
		// IMPORTANT: It only works with TraversalMode.NODE_BASED.
		DijkstraCostCondition dijkstraAlg = new DijkstraCostCondition(graph, weighting, parameters.getMaximumRange(), parameters.getReverseDirection(),
				TraversalMode.NODE_BASED);
		dijkstraAlg.setEdgeFilter(searchCntx.getEdgeFilter());
		dijkstraAlg.calcPath(fromId, Integer.MIN_VALUE);

		IntObjectMap<SPTEntry> edgeMap = dijkstraAlg.getMap();

		return new AccessibilityMap(edgeMap, dijkstraAlg.getCurrentEdge());
	}
}
