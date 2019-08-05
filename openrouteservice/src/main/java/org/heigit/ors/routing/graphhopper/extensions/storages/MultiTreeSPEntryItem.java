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

import com.graphhopper.util.EdgeIterator;

public class MultiTreeSPEntryItem {
	public int edge = EdgeIterator.NO_EDGE;
	public double weight = Double.POSITIVE_INFINITY;
	public boolean update =  false;
	public MultiTreeSPEntry parent = null;
	
	public void assignFrom(MultiTreeSPEntryItem item)
	{
		edge = item.edge;
		weight = item.weight;
		update = item.update;
		parent = item.parent;
	}
}
