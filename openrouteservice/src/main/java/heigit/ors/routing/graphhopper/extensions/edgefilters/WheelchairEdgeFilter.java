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
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;
import heigit.ors.routing.parameters.WheelchairParameters;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public class WheelchairEdgeFilter implements EdgeFilter 
{
	private byte[] _buffer;
	private WheelchairAttributesGraphStorage _storage;
	private WheelchairAttributes _attributes;
	private WheelchairParameters _params;
	
	public WheelchairEdgeFilter(WheelchairParameters params, GraphStorage graphStorage) throws Exception {

		_storage = GraphStorageUtils.getGraphExtension(graphStorage, WheelchairAttributesGraphStorage.class);

		if (_storage ==  null)
			throw new Exception("ExtendedGraphStorage for wheelchair attributes was not found.");
		
		_params = params;
		_attributes = new WheelchairAttributes();
		_buffer = new byte[WheelchairAttributesGraphStorage.BYTE_COUNT];
	}

	@Override
	public boolean accept(EdgeIteratorState iter) {

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

		return true;
	}
}
