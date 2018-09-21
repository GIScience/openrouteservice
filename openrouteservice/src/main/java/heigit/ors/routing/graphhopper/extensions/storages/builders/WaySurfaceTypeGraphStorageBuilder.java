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
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.WaySurfaceDescription;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;

import heigit.ors.routing.graphhopper.extensions.SurfaceType;
import heigit.ors.routing.graphhopper.extensions.WayType;
import heigit.ors.routing.graphhopper.extensions.storages.WaySurfaceTypeGraphStorage;

public class WaySurfaceTypeGraphStorageBuilder extends AbstractGraphStorageBuilder
{
	private WaySurfaceTypeGraphStorage _storage;
	private final WaySurfaceDescription waySurfaceDesc = new WaySurfaceDescription();
	protected final HashSet<String> ferries;
	
	public WaySurfaceTypeGraphStorageBuilder()
	{
		ferries = new HashSet<String>(5);
		ferries.add("shuttle_train");
		ferries.add("ferry");
	}
	
	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");
		
		_storage = new WaySurfaceTypeGraphStorage();
		return _storage;
	}

	public void processWay(ReaderWay way) {
		waySurfaceDesc.Reset();

		boolean hasHighway = way.hasTag("highway");
		boolean isFerryRoute = way.hasTag("route", ferries);

		java.util.Iterator<Entry<String, Object>> it = way.getProperties();

		while (it.hasNext()) {
			Map.Entry<String, Object> pairs = it.next();
			String key = pairs.getKey();
			String value = pairs.getValue().toString();

			if (hasHighway || isFerryRoute) {
				if (key.equals("highway")) {
					byte wayType = (isFerryRoute) ? WayType.Ferry : (byte)WayType.getFromString(value);

					if (waySurfaceDesc.SurfaceType == 0)
					{
						if (wayType == WayType.Road ||  wayType == WayType.StateRoad || wayType == WayType.Street)
							waySurfaceDesc.SurfaceType = (byte)SurfaceType.Asphalt;
						else if (wayType == WayType.Path)
							waySurfaceDesc.SurfaceType = (byte)SurfaceType.Unpaved;
					}

					waySurfaceDesc.WayType = wayType;
				}
				else if (key.equals("surface")) {
					waySurfaceDesc.SurfaceType = (byte)SurfaceType.getFromString(value);
				}
			}
		}
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge) 
	{
		_storage.setEdgeValue(edge.getEdge(), waySurfaceDesc);
	}

	@Override
	public String getName() {
		return "WaySurfaceType";
	}
}
