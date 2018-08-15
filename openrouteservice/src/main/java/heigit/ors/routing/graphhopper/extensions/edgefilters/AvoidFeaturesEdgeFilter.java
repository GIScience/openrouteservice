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
			_tollwayExtractor = new TollwayExtractor(extTollways, searchParams.getVehicleType(), searchParams.getProfileParameters());
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {

		if (_avoidFeatureType != 0) {
			int edge = iter.getEdge();
			int edgeFeatType = _storage.getEdgeValue(edge, _buffer);

			if (edgeFeatType != 0) {
				int avoidEdgeFeatureType = _avoidFeatureType & edgeFeatType;

				if (avoidEdgeFeatureType != 0) {

					// needs special handling as "tollways" which are valid only for driving share flag with "steps"
					if (_profileCategory == RoutingProfileCategory.DRIVING) {

						if ((avoidEdgeFeatureType & NOT_TOLLWAYS) != 0) {
							// restrictions other than tollways are present
							return false;
						}
						else if (_tollwayExtractor != null) {
							// false when there is a toll for the given profile
							return _tollwayExtractor.getValue(edge) == 0;
						}

					} else {

						return false;
					}
				}
			}
		}

		return true;
	}
}
