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
package heigit.ors.routing.graphhopper.extensions.storages;

/**
 * This class is used to create the shortest-path-tree from linked entities.
 * <p>
 *
 */
public class MultiTreeSPEntry implements Cloneable, Comparable<MultiTreeSPEntry> {
	public int adjNode;
	public boolean visited = false;
	private MultiTreeSPEntryItem[] items;
	private double totalWeight = 0.0;

	public MultiTreeSPEntry(int adjNode, int edgeId, double edgeWeight, boolean updated, MultiTreeSPEntry parent, int numTrees) {
		this.adjNode = adjNode;
		this.items = new MultiTreeSPEntryItem[numTrees];
		double entryWeight;
		
		for (int i = 0; i < numTrees; ++i)
		{
			MultiTreeSPEntryItem item = new MultiTreeSPEntryItem();
			items[i] = item;

			entryWeight = parent == null ? Double.POSITIVE_INFINITY : parent.items[i].weight;
			if (entryWeight == Double.POSITIVE_INFINITY && parent != null)
				continue;

			item.weight = edgeWeight + entryWeight;
			item.parent = parent;
			item.edge = edgeId;
			item.update = updated;
			totalWeight += item.weight;
		}
	}
	
	private MultiTreeSPEntry(MultiTreeSPEntry entry) {
		int numTrees = entry.items.length;
		this.items = new MultiTreeSPEntryItem[numTrees];
		
		for (int i = 0; i < numTrees; ++i)
		{
			MultiTreeSPEntryItem item = new MultiTreeSPEntryItem();
			entry.items[i].assignFrom(item);
			items[i] = item;
		}
		
		totalWeight = entry.totalWeight;
	}

	public int getSize()
	{
		return items.length;
	}

	public MultiTreeSPEntryItem getItem(int index)
	{
		return items[index];
	}

	public void resetUpdate(boolean value)
	{
		for (int i = 0; i < items.length; i++) {
			items[i].update = value;
		}
	}

	@Override
	public MultiTreeSPEntry clone() {
		MultiTreeSPEntry res = new MultiTreeSPEntry(this);
		return res;
	}

	public MultiTreeSPEntry cloneFull() {
		throw new UnsupportedOperationException("cloneFull not supported");
	}
	
	public void updateWeights()
	{
		totalWeight = 0.0;
		
		for (int i = 0; i < items.length; i++) {
			if(items[i].weight == Double.POSITIVE_INFINITY) continue;
			totalWeight += items[i].weight;
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
		return adjNode + " (" + 0 + ") weights: " + totalWeight; // TODO
	}
}
