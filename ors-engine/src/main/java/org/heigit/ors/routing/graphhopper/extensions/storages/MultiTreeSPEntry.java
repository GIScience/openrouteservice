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
 * This class is used to create the shortest-path-tree from linked entities.
 * <p>
 *
 */
public class MultiTreeSPEntry implements Comparable<MultiTreeSPEntry> {

	private int adjNode;
	protected int edge;
	private boolean visited = false;
	private final MultiTreeSPEntryItem[] items;
	private double totalWeight = 0.0;

	public MultiTreeSPEntry(int adjNode, int edgeId, double edgeWeight, boolean updated, MultiTreeSPEntry parent, int numTrees) {
		this.adjNode = adjNode;
		this.edge = edgeId;
		this.items = new MultiTreeSPEntryItem[numTrees];
		double entryWeight;
		
		for (int i = 0; i < numTrees; ++i) {
			MultiTreeSPEntryItem item = new MultiTreeSPEntryItem();
			items[i] = item;

			entryWeight = parent == null ? Double.POSITIVE_INFINITY : parent.items[i].getWeight();
			if (entryWeight == Double.POSITIVE_INFINITY && parent != null)
				continue;

			item.setWeight(edgeWeight + entryWeight);
			item.setParent(parent);
			item.setEdge(edgeId);
			item.setOriginalEdge(edgeId);
			item.setUpdate(updated);
			totalWeight += item.getWeight();
		}
	}

	public int getAdjNode() {
		return adjNode;
	}

	public void setAdjNode(int adjNode) {
		this.adjNode = adjNode;
	}

	public int getEdge() {return edge;}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public int getSize()
	{
		return items.length;
	}

	public MultiTreeSPEntryItem getItem(int index)
	{
		return items[index];
	}

	public void resetUpdate(boolean value) {
		for (int i = 0; i < items.length; i++) {
			items[i].setUpdate(value);
		}
	}

	public void updateWeights() {
		totalWeight = 0.0;
		
		for (int i = 0; i < items.length; i++) {
			if(items[i].getWeight() == Double.POSITIVE_INFINITY) continue;
			totalWeight += items[i].getWeight();
		}
	}

	@Override
	public int compareTo(MultiTreeSPEntry o) {
		if (totalWeight < o.totalWeight)
			return -1;

		// assumption no NaN and no -0
		return totalWeight > o.totalWeight ? 1 : 0;
	}

	@Override
	public String toString() {
		return "adjNode: " + adjNode +  ", totalWeight: " + totalWeight; // TODO
	}
}
