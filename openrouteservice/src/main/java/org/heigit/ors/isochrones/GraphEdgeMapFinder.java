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
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.shapes.GHPoint3D;
import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.algorithms.DijkstraCostCondition;
import org.heigit.ors.routing.graphhopper.extensions.AccessibilityMap;
import org.heigit.ors.routing.graphhopper.extensions.ORSEdgeFilterFactory;
import org.heigit.ors.routing.graphhopper.extensions.weighting.DistanceWeighting;

public class GraphEdgeMapFinder {
	private  GraphEdgeMapFinder() {}
	
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
	
		Weighting weighting =  parameters.getRangeType() == TravelRangeType.TIME ?  new FastestWeighting(encoder) : new DistanceWeighting(encoder);

		// IMPORTANT: It only works with TraversalMode.NODE_BASED.
		DijkstraCostCondition dijkstraAlg = new DijkstraCostCondition(graph, weighting, parameters.getMaximumRange(), parameters.getReverseDirection(),
				TraversalMode.NODE_BASED);
		dijkstraAlg.setEdgeFilter(edgeFilter);
		dijkstraAlg.calcPath(fromId, Integer.MIN_VALUE);

		IntObjectMap<SPTEntry> edgeMap = dijkstraAlg.getMap();
       return new AccessibilityMap(edgeMap, dijkstraAlg.getCurrentEdge(), snappedPosition);
	}
}
