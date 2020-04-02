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
package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.AvoidFeatureFlags;
import org.heigit.ors.routing.graphhopper.extensions.storages.WayCategoryGraphStorage;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

public class WayCategoryGraphStorageBuilder extends AbstractGraphStorageBuilder {
	private WayCategoryGraphStorage storage;
	protected final HashSet<String> ferries;
	private int wayType = 0;
	
	public WayCategoryGraphStorageBuilder() {
		ferries = new HashSet<>(5);
		ferries.add("shuttle_train");
		ferries.add("ferry");
	}
	
	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");
		
		storage = new WayCategoryGraphStorage();
		
		return storage;
	}

	public void processWay(ReaderWay way) {
		wayType = 0;

		boolean hasHighway = way.hasTag("highway");
		boolean isFerryRoute = way.hasTag("route", ferries);

		if (hasHighway || isFerryRoute) {
			java.util.Iterator<Entry<String, Object>> it = way.getProperties();

			while (it.hasNext()) {
				Map.Entry<String, Object> pairs = it.next();
				String key = pairs.getKey();
				String value = pairs.getValue().toString();

				if (key.equals("highway")) {
					if (value.equals("motorway") || value.equals("motorway_link")) {
						wayType |= AvoidFeatureFlags.HIGHWAYS;
					} else if (value.equals("steps")) {
						wayType |= AvoidFeatureFlags.STEPS;
					}
				} else if (value.equals("yes") && key.startsWith("toll")) {
					wayType |= AvoidFeatureFlags.TOLLWAYS;
				} else if (key.equals("route") && isFerryRoute) {
					wayType |= AvoidFeatureFlags.FERRIES;
				} else if (("ford".equals(key) && value.equals("yes"))) {
					wayType |= AvoidFeatureFlags.FORDS;
				}
			}
		}
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge) {
		storage.setEdgeValue(edge.getEdge(), wayType);
	}
	
	@Override
	public String getName() {
		return "WayCategory";
	}
}
