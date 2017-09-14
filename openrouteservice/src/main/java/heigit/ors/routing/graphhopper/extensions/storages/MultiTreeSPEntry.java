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

			entryWeight = parent == null ? 0.0 : parent.items[i].weight;
			if (entryWeight == 0.0 && parent != null)
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
