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
import com.graphhopper.util.shapes.GHPoint3D;
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

       GHPoint3D snappedPosition = res.getSnappedPoint();

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
       return new AccessibilityMap(edgeMap, dijkstraAlg.getCurrentEdge(), snappedPosition);
	}
}
