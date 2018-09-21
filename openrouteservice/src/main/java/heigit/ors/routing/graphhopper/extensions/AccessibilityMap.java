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
package heigit.ors.routing.graphhopper.extensions;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.storage.SPTEntry;

public class AccessibilityMap {
	private IntObjectMap<SPTEntry> map;
	private SPTEntry edgeEntry;
	
	public AccessibilityMap(IntObjectMap<SPTEntry> map, SPTEntry edgeEntry)
	{
		this.map = map;
		this.edgeEntry = edgeEntry;
	}
	
	public boolean isEmpty()
	{
		return map.size() == 0;
	}
	
	public IntObjectMap<SPTEntry> getMap()
	{
		return map;
	}
	
	public SPTEntry getEdgeEntry()
	{
		return edgeEntry;
	}
}
