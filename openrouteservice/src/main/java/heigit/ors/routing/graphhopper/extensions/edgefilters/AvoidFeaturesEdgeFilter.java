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

import heigit.ors.routing.AvoidFeatureFlags;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.RoutingProfileCategory;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.graphhopper.extensions.storages.*;
import heigit.ors.routing.pathprocessors.TollwayExtractor;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public class AvoidFeaturesEdgeFilter implements EdgeFilter {
	private byte[] _buffer;
	private WayCategoryGraphStorage _storage;
	private TollwayExtractor _tollwayExtractor;
	private int _avoidFeatureType;
	private int _profileCategory;

	private static final int NOT_TOLLWAYS = ~AvoidFeatureFlags.Tollways;

	public AvoidFeaturesEdgeFilter(int profileType, RouteSearchParameters searchParams, GraphStorage graphStorage) throws Exception {
		this._buffer = new byte[10];

		_profileCategory = RoutingProfileCategory.getFromRouteProfile(profileType);

		this._avoidFeatureType = searchParams.getAvoidFeatureTypes() & AvoidFeatureFlags.getProfileFlags(_profileCategory);

		_storage = GraphStorageUtils.getGraphExtension(graphStorage, WayCategoryGraphStorage.class);
		if (_storage == null)
			throw new Exception("ExtendedGraphStorage for avoid features was not found.");

		TollwaysGraphStorage extTollways = GraphStorageUtils.getGraphExtension(graphStorage, TollwaysGraphStorage.class);
		if (extTollways != null)
			_tollwayExtractor = new TollwayExtractor(extTollways, searchParams.getProfileType(), searchParams.getProfileParameters());
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {

		if (_avoidFeatureType != 0) {
			int edge = iter.getEdge();
			int edgeFeatType = _storage.getEdgeValue(edge, _buffer);

			if (edgeFeatType != 0) {
				int avoidEdgeFeatureType = _avoidFeatureType & edgeFeatType;

				if (avoidEdgeFeatureType != 0) {

					if ((avoidEdgeFeatureType & NOT_TOLLWAYS) != 0) {
						// restrictions other than tollways are present
						return false;
					}
					else if (_tollwayExtractor != null) {
						// false when there is a toll for the given profile
						return _tollwayExtractor.getValue(edge) == 0;
					}

				}
			}
		}

		return true;
	}
}
