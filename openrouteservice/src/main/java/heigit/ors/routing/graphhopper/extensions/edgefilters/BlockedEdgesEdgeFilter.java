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

import java.util.List;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.EdgeIteratorState;

import heigit.ors.routing.graphhopper.extensions.flagencoders.HeavyVehicleFlagEncoder;

public class BlockedEdgesEdgeFilter implements EdgeFilter {
	private FlagEncoder encoder;
	private List<Integer> blockedEdges;
	private List<Integer> blockedEdges_hv;

	/**
	 * edges (blockedEdges) for cars and heavy vehicles
	 * edges_hv only for heavy vehicles 
	 **/
	public BlockedEdgesEdgeFilter(FlagEncoder encoder, List<Integer> edges, List<Integer> edges_hv) {
		this.encoder = encoder;
		this.blockedEdges = edges;
		this.blockedEdges_hv = edges_hv;
	}

	@Override
	public boolean accept(EdgeIteratorState iter) {

		if (blockedEdges != null && blockedEdges.contains(iter.getOriginalEdge())) {
			return false;
		}

		if (blockedEdges_hv.size() != 0 && encoder instanceof HeavyVehicleFlagEncoder && blockedEdges_hv.contains(iter.getOriginalEdge())) {
			return false;
		}

		return true;

	}

}