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

import com.graphhopper.routing.util.CHEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

public abstract class CHLevelEdgeFilter implements CHEdgeFilter {
	protected final FlagEncoder encoder;
	protected final CHGraph graph;
	protected final int maxNodes;
	protected int highestNode = -1;
	protected int highestNodeLevel = -1;
	protected int baseNode;
	protected int baseNodeLevel = -1;

	public CHLevelEdgeFilter(CHGraph g, FlagEncoder encoder) {
		graph = g;
		maxNodes = g.getNodes(); 
		this.encoder = encoder;
	}

	@Override
	public boolean accept(EdgeIteratorState edgeIterState) {
		return false;
	}
	
	public int getHighestNode() {
		return highestNode;
	}

	public void setBaseNode(int nodeId) {
		baseNode = nodeId;
		if (nodeId < maxNodes)
		  baseNodeLevel = graph.getLevel(nodeId);
	}

	public void updateHighestNode(EdgeIteratorState edgeIterState) {
		int adjNode = edgeIterState.getAdjNode();
		
		if (adjNode < maxNodes)
		{
			if (highestNode == -1 || highestNodeLevel < graph.getLevel(adjNode))
			{
				highestNode =  adjNode;
				highestNodeLevel = graph.getLevel(highestNode);
			}
		}
		else
		{
			if (highestNode == -1)
				highestNode =  adjNode;
		}
	}
}
