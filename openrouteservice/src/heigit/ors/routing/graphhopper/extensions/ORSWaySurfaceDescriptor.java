package org.freeopenls.routeservice.graphhopper.extensions;

import org.freeopenls.routeservice.graphhopper.extensions.storages.WaySurfaceTypeGraphStorage;

import com.graphhopper.routing.util.EdgeWaySurfaceDescriptor;
import com.graphhopper.routing.util.WaySurfaceDescription;

public class ORSWaySurfaceDescriptor implements EdgeWaySurfaceDescriptor {

	private WaySurfaceTypeGraphStorage storage;
	
	public ORSWaySurfaceDescriptor(WaySurfaceTypeGraphStorage storage)
	{
	   this.storage = storage;	
	}
	
	public WaySurfaceDescription getDescription(int edgeId)
	{
		byte[] buffer = new byte[1];
		return storage.getEdgeValue(edgeId, buffer);
	}
}
