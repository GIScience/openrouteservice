package heigit.ors.isochrones;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FastestWeighting;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.storage.EdgeEntry;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.QueryResult;
import com.vividsolutions.jts.geom.Coordinate;

import gnu.trove.map.TIntObjectMap;

import heigit.ors.isochrones.IsochronesRangeType;
import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.routing.graphhopper.extensions.DijkstraCostCondition;
import heigit.ors.routing.RouteSearchContext;
import heigit.ors.routing.RouteSearchParameters;
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
			throw new Exception("The closest node is null.");
	
		Weighting weighting = null;
		
		if (parameters.getRangeType() == IsochronesRangeType.Time)
		{
			double maxSpeed = -1;
			RouteSearchParameters routeParams = parameters.getRouteParameters();
			if (routeParams != null)
				maxSpeed = routeParams.getMaximumSpeed();
		    weighting = new FastestWeighting(maxSpeed, encoder) ;	
		}
		else
		{
			weighting  =new DistanceWeighting(encoder);
		}
		// IMPORTANT: It only works with TraversalMode.NODE_BASED.
		DijkstraCostCondition dijkstraAlg = new DijkstraCostCondition(graph, encoder, weighting, parameters.getMaximumRange(), parameters.getReverseDirection(),
				TraversalMode.NODE_BASED);
		dijkstraAlg.setEdgeFilter(searchCntx.getEdgeFilter());
		dijkstraAlg.computePath(fromId, Integer.MIN_VALUE);

		TIntObjectMap<EdgeEntry> edgeMap = dijkstraAlg.getMap();

		return new AccessibilityMap(edgeMap, dijkstraAlg.getCurrentEdge());
	}
}
