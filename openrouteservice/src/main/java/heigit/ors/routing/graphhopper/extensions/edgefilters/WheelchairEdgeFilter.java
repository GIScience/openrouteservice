package heigit.ors.routing.graphhopper.extensions.edgefilters;

import heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;
import heigit.ors.routing.WheelchairParameters;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public class WheelchairEdgeFilter implements EdgeFilter 
{
	private final boolean in;
	private final boolean out;
	private FlagEncoder encoder;
	private byte[] _buffer;
	private WheelchairAttributesGraphStorage _storage;
	private WheelchairAttributes _attributes;
	private WheelchairParameters _params;
	
	public WheelchairEdgeFilter(WheelchairParameters params, FlagEncoder encoder, GraphStorage graphStorage) throws Exception
	{
		this(params, encoder, true, true, graphStorage);
	}

	public WheelchairEdgeFilter(WheelchairParameters params, FlagEncoder encoder, boolean in, boolean out, GraphStorage graphStorage) throws Exception
	{
		this.encoder = encoder;
		this.in = in;
		this.out = out;

		_storage = GraphStorageUtils.getGraphExtension(graphStorage, WheelchairAttributesGraphStorage.class);

		if (_storage ==  null)
			throw new Exception("ExtendedGraphStorage for wheelchair attributes was not found.");
		
		_params = params;
		_attributes = new WheelchairAttributes();
		_buffer = new byte[3];
	}

	@Override
	public boolean accept(EdgeIteratorState iter) 
	{
		if (out && iter.isForward(encoder) || in && iter.isBackward(encoder))
		{
			_storage.getEdgeValues(iter.getEdge(), _attributes, _buffer);
			
			if (_attributes.hasValues())
			{
				if (_params.getSurfaceType() > 0)
				{
					if (_params.getSurfaceType() < _attributes.getSurfaceType())
						return false;
				}
				
				if (_params.getSmoothnessType() > 0)
				{
					if (_params.getSmoothnessType() < _attributes.getSmoothnessType())
						return false;
				}
				
				if (_params.getTrackType() > 0 && _attributes.getTrackType() != 0)
				{
					if ( _params.getTrackType() <= _attributes.getTrackType())
						return false;
				}

				if (_params.getMaximumIncline() != 0.0)
				{
					if (_params.getMaximumIncline() < _attributes.getIncline())
						return false;
				}

				if (_params.getMaximumSlopedCurb() > 0.0)
				{
					if (_params.getMaximumSlopedCurb() < _attributes.getSlopedCurbHeight())
						return false;
				}
			}
		}

		return true;
	}
}
