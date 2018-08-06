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
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;
import heigit.ors.routing.parameters.WheelchairParameters;

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
		_buffer = new byte[WheelchairAttributesGraphStorage.BYTE_COUNT];
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

				if (_params.getMaximumIncline() > (Float.MAX_VALUE * -1.0f))
				{
					if (_params.getMaximumIncline() < _attributes.getIncline())
						return false;
				}

				if (_params.getMaximumSlopedKerb() >= 0.0)
				{
					if (_params.getMaximumSlopedKerb() < _attributes.getSlopedKerbHeight())
						return false;
				}

				if (_params.getMinimumWidth() > 0.0) {
					// if the attribute value is 0, this signifies that no data is available
					if(_attributes.getWidth() > 0.0) {
						if(_params.getMinimumWidth() > _attributes.getWidth()) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}
}
