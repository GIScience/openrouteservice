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
package heigit.ors.routing.graphhopper.extensions.storages;

import java.util.Arrays;

/**
 * This class is used to create the shortest-path-tree from linked entities.
 * <p>
 *
 * @author Peter Karich
 */
public class MultiTreeSPEntry implements Cloneable, Comparable<MultiTreeSPEntry> {
	public int[] edge;
	public int adjNode;
	public double[] weights;
	public MultiTreeSPEntry[] parent;
	public boolean visited = false;

	public MultiTreeSPEntry(int[] edgeId, int adjNode, double[] weights) {
		this.edge = edgeId;
		this.adjNode = adjNode;
		this.weights = weights;
		this.parent = new MultiTreeSPEntry[weights.length];
	}

	public MultiTreeSPEntry(int[] edgeId, int adjNode, int numTrees) {
		this.edge = edgeId;
		this.adjNode = adjNode;
		this.weights = new double[numTrees];
		this.parent = new MultiTreeSPEntry[numTrees];
		Arrays.fill(this.weights, -1);
	}

	/**
	 * This method returns the weight to the origin e.g. to the start for the
	 * forward SPT and to the destination for the backward SPT. Where the
	 * variable 'weight' is used to let heap select smallest *full* weight (from
	 * start to destination).
	 */
	public double[] getWeightOfVisitedPath() {
		return weights;
	}

	@Override
	public MultiTreeSPEntry clone() {
		return new MultiTreeSPEntry(edge, adjNode, weights);
	}

	public MultiTreeSPEntry cloneFull() {
		throw new UnsupportedOperationException("cloneFull not supported");
	}

	@Override
	public int compareTo(MultiTreeSPEntry o) {
		double s1 = 0;
		double s2 = 0;
		for (int i = 0; i < weights.length; i++) {
			s1 += weights[i];
			s2 += o.weights[i];
		}
		if (s1 < s2)
			return -1;

		// assumption no NaN and no -0
		return s1 > s2 ? 1 : 0;
	}

	@Override
	public String toString() {
		return adjNode + " (" + edge + ") weight: " + weights;
	}
}
