/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.graphhopper.extensions.storages;

/**
 * Shortest Path Tree with multiple fields for containing multiple source information.
 * The weight is the average of all non-infinite entries for order in priority queue.
 * Average weight can result in better performance than minimum weight.
 * @author Hendrik Leuschner
 *
 */
public class AveragedMultiTreeSPEntry extends MultiTreeSPEntry {
	private double weight = Double.POSITIVE_INFINITY;

	public AveragedMultiTreeSPEntry(int adjNode, int edgeId, double edgeWeight, boolean updated, AveragedMultiTreeSPEntry parent, int numTrees) {
		super(adjNode,edgeId, edgeWeight, updated, parent, numTrees);
		updateWeights();
	}

	public void setSubItemOriginalEdgeIds(int newOriginalEdgeId) {
		for (int i = 0; i < getSize(); i++) {
			getItem(i).setOriginalEdge(newOriginalEdgeId);
		}
	}


	@Override
	public void updateWeights() {
		double averageWeight = 0;
		int numNonInfiniteWeights = 0;
		for (int i = 0; i < getSize(); i++) {
			MultiTreeSPEntryItem item = getItem(i);
			double itemWeight = item.getWeight();
			if(itemWeight != Double.POSITIVE_INFINITY) {
				averageWeight += itemWeight;
				numNonInfiniteWeights++;
			}

		}
		if(numNonInfiniteWeights == 0)
			weight = Double.POSITIVE_INFINITY;
		else
			weight = averageWeight / numNonInfiniteWeights;
	}

	@Override
	public int compareTo(MultiTreeSPEntry other) {
		AveragedMultiTreeSPEntry o = (AveragedMultiTreeSPEntry) other;
		if (weight < o.weight)
			return -1;

		// assumption no NaN and no -0
		return weight > o.weight ? 1 : 0;
	}

	@Override
	public String toString() {
		return "adjNode: " + getAdjNode() + ",  average weight: " + weight;
	}
}
