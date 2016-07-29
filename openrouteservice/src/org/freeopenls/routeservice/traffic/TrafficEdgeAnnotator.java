package org.freeopenls.routeservice.traffic;

import com.graphhopper.routing.util.EdgeAnnotator;
import com.graphhopper.storage.GraphStorage;

public class TrafficEdgeAnnotator implements EdgeAnnotator {
	private GraphStorage storage;
	
	public TrafficEdgeAnnotator(GraphStorage gs)
	{
		storage = gs;
	}
	
	public String getAnnotation(int edgeId)
	{
		return RealTrafficDataProvider.getInstance().getEdgeMessage(storage, edgeId);
	}
}
