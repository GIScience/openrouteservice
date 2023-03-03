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
package org.heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;

import java.util.ArrayList;

public class EdgeFilterSequence implements EdgeFilter {

	protected ArrayList<EdgeFilter> filters = new ArrayList<>();
	private String name = "";

	@Override
	public boolean accept(EdgeIteratorState eis) {
		for (EdgeFilter edgeFilter: filters) {
			if (!edgeFilter.accept(eis)) {
				return false;
			}
		}
		return true;
	}

	public void appendName(String name){
		if (this.name.isEmpty())
			this.name = name;
		else
			this.name += ("_" + name);
	}

	public String getName() {
		return name;
	}

	public void add(EdgeFilter o) {
		filters.add(o);
	}

	@Override
	public String toString() {
		return "EdgeFilter Sequence: " + name + " (" + filters.size() + ")";
	}
}
