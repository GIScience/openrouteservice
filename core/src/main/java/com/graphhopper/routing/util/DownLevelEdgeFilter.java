package com.graphhopper.routing.util;

import com.graphhopper.storage.CHGraph;
import com.graphhopper.util.EdgeIteratorState;

public class DownLevelEdgeFilter implements CHEdgeFilter {
	private final CHGraph graph;
	private final int maxNodes;

	public DownLevelEdgeFilter(CHGraph g, FlagEncoder encoder) {
		graph = g;
		maxNodes = g.getNodes();
	}

	@Override
	public boolean accept(EdgeIteratorState edgeIterState) {
		int base = edgeIterState.getBaseNode();
		int adj = edgeIterState.getAdjNode();
		// always accept virtual edges, see #288
		if (base >= maxNodes || adj >= maxNodes) {
			return true;
		}
		// if (highestNode == -1)
		// highestNode = adj;
		// if (graph.getLevel(base) > graph.getLevel(adj)) {
		// if (graph.getLevel(highestNode) > graph.getLevel(adj)) {
		// if (adj > 0)
		// highestNode = adj;
		// }
		// }
		// if (edgeIterState.isShortcut())
		// return !(graph.getLevel(base) > graph.getLevel(adj)) ? false : true;
		// return !(graph.getLevel(base) > graph.getLevel(adj)) ? false :
		// edgeIterState.isForward(encoder);
		return !(graph.getLevel(base) > graph.getLevel(adj)) ? false : true;
	}
}
