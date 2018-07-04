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
package heigit.ors.routing.graphhopper.extensions.edgefilters.core;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.AvoidFeatureFlags;
import heigit.ors.routing.RoutingProfileCategory;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.WayCategoryGraphStorage;

public class AvoidFeaturesCoreEdgeFilter implements EdgeFilter {
	private byte[] _buffer;
	private WayCategoryGraphStorage _storage;
	private int _avoidFeatures;

	public AvoidFeaturesCoreEdgeFilter(GraphStorage graphStorage, int profileType) {
		_buffer = new byte[10];
		_avoidFeatures = AvoidFeatureFlags.getProfileFlags(profileType);
		_storage = GraphStorageUtils.getGraphExtension(graphStorage, WayCategoryGraphStorage.class);
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {

		return (_storage.getEdgeValue(iter.getEdge(), _buffer) & _avoidFeatures) == 0;

	}

}
