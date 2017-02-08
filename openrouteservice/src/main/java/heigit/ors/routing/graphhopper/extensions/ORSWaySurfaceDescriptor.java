package heigit.ors.routing.graphhopper.extensions;

import heigit.ors.routing.graphhopper.extensions.storages.WaySurfaceTypeGraphStorage;

import com.graphhopper.routing.util.EdgeWaySurfaceDescriptor;
import com.graphhopper.routing.util.WaySurfaceDescription;

public class ORSWaySurfaceDescriptor implements EdgeWaySurfaceDescriptor {

	private WaySurfaceTypeGraphStorage _storage;
	private byte[] _buffer = new byte[1];
	
	public ORSWaySurfaceDescriptor(WaySurfaceTypeGraphStorage storage)
	{
	   this._storage = storage;	
	}
	
	public WaySurfaceDescription getDescription(int edgeId)
	{
		return _storage.getEdgeValue(edgeId, _buffer);
	}
}
