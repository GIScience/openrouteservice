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

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

public class UpwardSearchEdgeFilter extends CHLevelEdgeFilter {

	public UpwardSearchEdgeFilter(CHGraph g, FlagEncoder encoder) {
		super(g, encoder);
	}
	
	@Override
	public boolean accept(EdgeIteratorState edgeIterState) {
		int adj = edgeIterState.getAdjNode(); 
		// always accept virtual edges, see #288
		if (baseNode >= maxNodes || adj >= maxNodes)
			return true;
 
		if (baseNodeLevel <= graph.getLevel(adj))
			return edgeIterState.isForward(encoder);
		else
			return false;
	}
}
