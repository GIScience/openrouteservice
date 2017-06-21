package com.graphhopper.routing.phast;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIterator;

public class DistanceExtractorRPHAST {
	private CHGraph g;
	private IntObjectMap<SPTEntry> targets;
	private int[] intTargetMap;
	private float[] distances;

	public DistanceExtractorRPHAST(CHGraph graph, IntObjectMap<SPTEntry> targets) {
		this.g = graph;
		this.targets = targets;
		distances = new float[targets.size()];
	}

	public DistanceExtractorRPHAST(CHGraph graph) {
		this.g = graph;
	}

	public float[] extractDistances() {
		if (targets == null)
			throw new IllegalStateException("Target destinations not set");
		int i = 0;
		for (IntObjectCursor<SPTEntry> c : targets) {

			SPTEntry goalEdge = c.value;
			float dist = 0;
			System.out.println(goalEdge.adjNode + " is target node");

			while (EdgeIterator.Edge.isValid(goalEdge.edge)) {
				CHEdgeIteratorState iter = (CHEdgeIteratorState) g.getEdgeIteratorState(goalEdge.edge,
						goalEdge.adjNode);
				dist += iter.getDistance();
				goalEdge = goalEdge.parent;
			}
			distances[i] = dist;
			i++;

		}
		return distances;
	}

	public void extractDistances(float[] distances, int pos) {
		if (targets == null)
			throw new IllegalStateException("Target destinations not set");
		int i = 0;
		for (int target : intTargetMap) {

			SPTEntry goalEdge = targets.get(target);
			float dist = 0;
			if (goalEdge != null) {
				// System.out.println(goalEdge.adjNode + " is target node");

				while (EdgeIterator.Edge.isValid(goalEdge.edge)) {
					// System.out.println("edge to node " + goalEdge.adjNode +
					// ", level: " + g.getLevel(goalEdge.adjNode));

					CHEdgeIteratorState iter = (CHEdgeIteratorState) g.getEdgeIteratorState(goalEdge.edge,
							goalEdge.adjNode);
					// System.out.println(
					// "Additional distance " + iter.getDistance() + ", edge is
					// shortcut: " + iter.isShortcut());
					dist += iter.getDistance();
					goalEdge = goalEdge.parent;
				}
			} else
				dist = -1;
			distances[pos + i] = dist;
			i++;

		}

	}

	public void setMatrix(IntObjectMap<SPTEntry> targets, int[] targetMap) {
		this.targets = targets;
		this.intTargetMap = targetMap;
		this.distances = new float[targets.size()];
	}

}
