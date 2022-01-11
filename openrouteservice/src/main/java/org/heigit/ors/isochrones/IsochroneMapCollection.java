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
package org.heigit.ors.isochrones;

import java.util.ArrayList;
import java.util.List;

public class IsochroneMapCollection {
	private int nIsochrones = 0;
	private final List<IsochroneMap> isochroneMaps = new ArrayList<>();
	
	public void add(IsochroneMap map) {
		isochroneMaps.add(map);
		nIsochrones += map.getIsochronesCount();
	}
	
	public Iterable<IsochroneMap> getIsochroneMaps()
	{
		return isochroneMaps;
	}
	
	public IsochroneMap getIsochrone(int index)
	{
		return isochroneMaps.get(index);
	}
	
	public int getIsochronesCount()
	{
		return nIsochrones;
	}
	
	public int size()
	{
		return isochroneMaps.size();
	}
}
