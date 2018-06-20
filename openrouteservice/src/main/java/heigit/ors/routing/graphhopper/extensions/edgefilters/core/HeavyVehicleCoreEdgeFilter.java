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
import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.HeavyVehicleAttributesGraphStorage;


public class HeavyVehicleCoreEdgeFilter implements EdgeFilter {
	private HeavyVehicleAttributesGraphStorage gsHeavyVehicles;
	private final boolean in;
	private final boolean out;
	private FlagEncoder encoder;
	private byte[] buffer;

	public HeavyVehicleCoreEdgeFilter(FlagEncoder encoder, GraphStorage graphStorage) {
		this(encoder, true, true, graphStorage);
	}

	/**
	 * Creates an core edge filter which accepts both directions.
	 */
	public HeavyVehicleCoreEdgeFilter(FlagEncoder encoder, boolean in, boolean out, GraphStorage graphStorage) {
		this.encoder = encoder;
		this.in = in;
		this.out = out;
		this.buffer = new byte[10];
		this.gsHeavyVehicles = GraphStorageUtils.getGraphExtension(graphStorage, HeavyVehicleAttributesGraphStorage.class);
	}

	@Override
	public boolean accept(EdgeIteratorState iter) {
		if (out && iter.isForward(encoder) || in && iter.isBackward(encoder)) {
			int edgeId = iter.getOriginalEdge();

			int vt = gsHeavyVehicles.getEdgeVehicleType(edgeId, buffer);

			// true if edge has no restrictions
			return (vt == HeavyVehicleAttributes.UNKNOWN);
		}
		return false;
	}

}
