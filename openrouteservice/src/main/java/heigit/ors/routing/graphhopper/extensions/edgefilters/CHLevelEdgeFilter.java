package heigit.ors.routing.graphhopper.extensions.edgefilters;
/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper GmbH licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import com.graphhopper.routing.util.CHEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Only certain nodes are accepted and therefore the others are ignored.
 * <p>
 *
 * @author Peter Karich
 */
public abstract class CHLevelEdgeFilter implements CHEdgeFilter {
	protected final CHGraph graph;
	protected final int maxNodes;
	protected int highestNode = -1;
	protected FlagEncoder encoder;
	
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

	public void updateHighestNode(EdgeIteratorState edgeIterState) {
		
		int ajdNode = edgeIterState.getAdjNode();
		if (ajdNode < maxNodes)
		{
			if (highestNode == -1 || graph.getLevel(highestNode) < graph.getLevel(ajdNode))
				this.highestNode =  ajdNode;
		}
		else
		{
			if (highestNode == -1)
				this.highestNode =  ajdNode;
		}
	}
}
