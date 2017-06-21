package com.graphhopper.routing.phast;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIterator;

public class PropertyExtractorRPHAST {
	private CHGraph g;
	private IntObjectMap<SPTEntry> targets;
	private int[] intTargetMap;
	private float[] prop;
	private int propertyFlag;

	public PropertyExtractorRPHAST(CHGraph graph, IntObjectMap<SPTEntry> targets, int propertyFlag) {
		this.g = graph;
		this.targets = targets;
		prop = new float[targets.size()];
		this.propertyFlag = propertyFlag;
	}

	public PropertyExtractorRPHAST(CHGraph graph, int propertyFlag) {
		this.g = graph;
		this.propertyFlag = propertyFlag;
	}

	public float[] extractProperty() {
		if (propertyFlag == MatrixService.DISTANCE) {

			if (targets == null)
				throw new IllegalStateException("Target destinations not set");
			int i = 0;
			for (IntObjectCursor<SPTEntry> c : targets) {

				SPTEntry goalEdge = c.value;
				float tempProp = 0;
				System.out.println(goalEdge.adjNode + " is target node");

				while (EdgeIterator.Edge.isValid(goalEdge.edge)) {
					CHEdgeIteratorState iter = (CHEdgeIteratorState) g.getEdgeIteratorState(goalEdge.edge,
							goalEdge.adjNode);
					tempProp += iter.getDistance();
					goalEdge = goalEdge.parent;
				}
				prop[i] = tempProp;
				i++;

			}
			return prop;
		}
		if (propertyFlag == MatrixService.DISTANCE) {

			if (targets == null)
				throw new IllegalStateException("Target destinations not set");
			int i = 0;
			for (IntObjectCursor<SPTEntry> c : targets) {

				SPTEntry goalEdge = c.value;
				float tempProp = 0;
				System.out.println(goalEdge.adjNode + " is target node");

				while (EdgeIterator.Edge.isValid(goalEdge.edge)) {
					CHEdgeIteratorState iter = (CHEdgeIteratorState) g.getEdgeIteratorState(goalEdge.edge,
							goalEdge.adjNode);
					tempProp += iter.getAdditionalField();
					goalEdge = goalEdge.parent;
				}
				prop[i] = tempProp;
				i++;

			}
			return prop;
		}
		throw new IllegalStateException("Unsupported flag: " + this.propertyFlag);

	}

	public void extractProperty(float[] prop, int pos) {
		if (propertyFlag == MatrixService.DISTANCE) {
			if (targets == null)
				throw new IllegalStateException("Target destinations not set");
			int i = 0;
			for (int target : intTargetMap) {

				SPTEntry goalEdge = targets.get(target);
				float tempProp = 0;
				if (goalEdge != null) {
					while (EdgeIterator.Edge.isValid(goalEdge.edge)) {
						CHEdgeIteratorState iter = (CHEdgeIteratorState) g.getEdgeIteratorState(goalEdge.edge,
								goalEdge.adjNode);
						tempProp += iter.getDistance();
						goalEdge = goalEdge.parent;
					}
				} else
					tempProp = -1;
				prop[pos + i] = tempProp;
				i++;

			}
			return;
		}
		if (propertyFlag == MatrixService.WEIGHT) {
			if (targets == null)
				throw new IllegalStateException("Target destinations not set");
			int i = 0;
			for (int target : intTargetMap) {

				SPTEntry goalEdge = targets.get(target);
				float tempProp = 0;
				if (goalEdge != null) {
					while (EdgeIterator.Edge.isValid(goalEdge.edge)) {
						CHEdgeIteratorState iter = (CHEdgeIteratorState) g.getEdgeIteratorState(goalEdge.edge,
								goalEdge.adjNode);
						tempProp += iter.getAdditionalField();
						goalEdge = goalEdge.parent;
					}
				} else
					tempProp = -1;
				prop[pos + i] = tempProp;
				i++;

			}
			return;
		}
		throw new IllegalStateException("Unsupported flag: " + this.propertyFlag);
	}

	public void setMatrix(IntObjectMap<SPTEntry> targets, int[] targetMap) {
		this.targets = targets;
		this.intTargetMap = targetMap;
		this.prop = new float[targets.size()];
	}

}
