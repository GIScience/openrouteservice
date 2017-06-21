package com.graphhopper.routing.util;
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

import java.util.HashSet;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.CHEdgeIteratorState;

/**
 * Accepts nodes that are contained in the target tree and have the correct
 * level relation to the current node
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class RPHASTEdgeFilter implements CHEdgeFilter {
	private final CHGraph graph;
	private final int maxNodes;
	public int highestNode = -1;
	private FlagEncoder encoder;
	private HashSet<Integer> targetTree;

	public RPHASTEdgeFilter(CHGraph g, FlagEncoder encoder) {
		graph = g;
		maxNodes = g.getNodes();
		this.encoder = encoder;
	}

	@Override
	public boolean accept(CHEdgeIteratorState edgeIterState) {
		int base = edgeIterState.getBaseNode();
		int adj = edgeIterState.getAdjNode();
		// always accept virtual edges, see #288
		if (base >= maxNodes || adj >= maxNodes)
			return true;
		if (highestNode == -1)
			highestNode = adj;
		// if (graph.getLevel(base) < graph.getLevel(adj)) {
		// if (graph.getLevel(highestNode) < graph.getLevel(adj)) {
		// if (adj > 0) {
		// highestNode = adj;
		// System.out.println("New highest node: " + highestNode);
		// }
		// }
		// }
		if (!(graph.getLevel(base) > graph.getLevel(adj))) {
			return false;
		} else if (targetTree.contains(adj)) {
			return edgeIterState.isForward(encoder);
		} else
			return false;
	}

	@Override
	public int getHighestNode() {
		return highestNode;
	}

	@Override
	public void setHighestNode(int highestNode) {
		this.highestNode = highestNode;
	}

	public RPHASTEdgeFilter setTargetTree(IntObjectMap<SPTEntry> targets) {
		targetTree = new HashSet<Integer>(targets.size(), 1);
		for (IntObjectCursor<SPTEntry> target : targets) {
			targetTree.add(target.value.adjNode);
		}
		// System.out.println(targetTree.toString());
		return this;
	}
}
