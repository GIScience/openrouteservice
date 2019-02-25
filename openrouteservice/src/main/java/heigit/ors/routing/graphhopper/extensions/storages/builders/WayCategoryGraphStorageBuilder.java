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
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;

import heigit.ors.routing.AvoidFeatureFlags;
import heigit.ors.routing.graphhopper.extensions.storages.WayCategoryGraphStorage;

public class WayCategoryGraphStorageBuilder extends AbstractGraphStorageBuilder
{
	private WayCategoryGraphStorage _storage;
	protected final HashSet<String> ferries;
	private int _wayType = 0;
	
	public WayCategoryGraphStorageBuilder()
	{
		ferries = new HashSet<String>(5);
		ferries.add("shuttle_train");
		ferries.add("ferry");
	}
	
	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");
		
		_storage = new WayCategoryGraphStorage();
		
		return _storage;
	}

	public void processWay(ReaderWay way) {
		_wayType = 0;
		
		boolean hasHighway = way.hasTag("highway");
		boolean isFerryRoute = isFerryRoute(way);
		
		java.util.Iterator<Entry<String, Object>> it = way.getProperties();

		while (it.hasNext()) {
			Map.Entry<String, Object> pairs = it.next();
			String key = pairs.getKey();
			String value = pairs.getValue().toString();

			if (hasHighway || isFerryRoute) {
				if (key.equals("highway")) {
					if (value.equals("motorway") || value.equals("motorway_link"))
					{
						_wayType |= AvoidFeatureFlags.Highways;
					}
					else if (value.equals("steps"))
					{
						_wayType |= AvoidFeatureFlags.Steps;
					}
				} else if (value.equals("yes") && key.startsWith("toll")) 
					_wayType |= AvoidFeatureFlags.Tollways;
				else if (key.equals("route") && isFerryRoute) 
					_wayType |= AvoidFeatureFlags.Ferries;
				else if (("ford".equals(key) && value.equals("yes")))
					_wayType |= AvoidFeatureFlags.Fords;
			}
		}
	}
	
	public void processEdge(ReaderWay way, EdgeIteratorState edge)
	{
		if (_wayType > 0) 
			_storage.setEdgeValue(edge.getEdge(), _wayType);
	}

	private boolean isFerryRoute(ReaderWay way) {
		if (way.hasTag("route", ferries)) {

				String motorcarTag = way.getTag("motorcar");
				if (motorcarTag == null)
					motorcarTag = way.getTag("motor_vehicle");

				if (motorcarTag == null && !way.hasTag("foot") && !way.hasTag("bicycle") || "yes".equals(motorcarTag))
					return true;
			
				//return way.hasTag("bicycle", "yes");
				String bikeTag = way.getTag("bicycle");
				if (bikeTag == null && !way.hasTag("foot") || "yes".equals(bikeTag))
					return true;
		}

		return false;
	}
	
	@Override
	public String getName() {
		return "WayCategory";
	}
}
