package heigit.ors.isochrones;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
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
import heigit.ors.routing.graphhopper.extensions.AccessibilityMap;
import heigit.ors.routing.graphhopper.extensions.weighting.DistanceWeighting;

public class GraphEdgeMapFinder {
	
   public static AccessibilityMap findEdgeMap(GraphHopper graphHopper, String encoderName, EdgeFilter edgeFilter, IsochroneSearchParameters parameters) throws Exception {
		FlagEncoder encoder = graphHopper.getEncodingManager().getEncoder(encoderName);
		GraphHopperStorage graph = graphHopper.getGraphHopperStorage();

		Coordinate loc = parameters.getLocation();
		QueryResult res = graphHopper.getLocationIndex().findClosest(loc.y, loc.x, edgeFilter);
		int fromId = res.getClosestNode();

		if (fromId == -1)
			throw new Exception("The closest node is null.");

		Weighting weighting = (parameters.getRangeType() == IsochronesRangeType.Time) ? new FastestWeighting(-1, encoder) : new DistanceWeighting(encoder); 
		// IMPORTANT: It only works with TraversalMode.NODE_BASED.
		DijkstraCostCondition dijkstraAlg = new DijkstraCostCondition(graph, encoder, weighting, parameters.getMaximumRange(), parameters.getReverseDirection(),
				TraversalMode.NODE_BASED);
		dijkstraAlg.setEdgeFilter(edgeFilter);
		dijkstraAlg.computePath(fromId, Integer.MIN_VALUE);

		TIntObjectMap<EdgeEntry> edgeMap = dijkstraAlg.getMap();

		return new AccessibilityMap(edgeMap, dijkstraAlg.getCurrentEdge());
	}
}
