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

import com.graphhopper.util.EdgeIterator;

public class MultiTreeSPEntryItem {
	private int edge = EdgeIterator.NO_EDGE;
	private int originalEdge = EdgeIterator.NO_EDGE;
	private int incEdge = EdgeIterator.NO_EDGE;
	private double weight = Double.POSITIVE_INFINITY;
	private boolean update =  false;
	private MultiTreeSPEntry parent = null;

	public int getEdge() {
		return edge;
	}

	public void setEdge(int edge) {
		this.edge = edge;
	}

	public int getOriginalEdge() {
		return originalEdge;
	}

	public void setOriginalEdge(int originalEdge) {
		this.originalEdge = originalEdge;
	}

	public int getIncEdge() {
		return incEdge;
	}

	public void setIncEdge(int incEdge) {
		this.incEdge = incEdge;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	public MultiTreeSPEntry getParent() {
		return parent;
	}

	public void setParent(MultiTreeSPEntry parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return String.valueOf(weight);
	}

}
