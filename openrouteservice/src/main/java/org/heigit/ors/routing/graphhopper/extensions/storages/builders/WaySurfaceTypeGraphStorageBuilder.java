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
import org.heigit.ors.routing.graphhopper.extensions.SurfaceType;
import org.heigit.ors.routing.graphhopper.extensions.WayType;
import org.heigit.ors.routing.graphhopper.extensions.storages.WaySurfaceTypeGraphStorage;
import org.heigit.ors.routing.util.WaySurfaceDescription;

import java.util.HashSet;

public class WaySurfaceTypeGraphStorageBuilder extends AbstractGraphStorageBuilder {
	public static final String TAG_HIGHWAY = "highway";
	public static final String TAG_SURFACE = "surface";
	public static final String TAG_ROUTE = "route";
	private WaySurfaceTypeGraphStorage storage;
	private final WaySurfaceDescription waySurfaceDesc = new WaySurfaceDescription();
	protected final HashSet<String> ferries;
	
	public WaySurfaceTypeGraphStorageBuilder() {
		ferries = new HashSet<>(5);
		ferries.add("shuttle_train");
		ferries.add("ferry");
	}
	
	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");
		
		storage = new WaySurfaceTypeGraphStorage();
		return storage;
	}

	public void processWay(ReaderWay way) {
		waySurfaceDesc.reset();

		int wayType;
		if (way.hasTag(TAG_ROUTE, ferries)) {
			wayType = WayType.FERRY;
		} else if (way.hasTag(TAG_HIGHWAY)) {
			wayType = WayType.getFromString(way.getTag(TAG_HIGHWAY));
		} else {
			return;
		}
		waySurfaceDesc.setWayType(wayType);

		int surfaceType = way.hasTag(TAG_SURFACE) ? SurfaceType.getFromString(way.getTag(TAG_SURFACE)) : SurfaceType.UNKNOWN;
		if (surfaceType == SurfaceType.UNKNOWN) {
			if (wayType == WayType.ROAD || wayType == WayType.STATE_ROAD || wayType == WayType.STREET) {
				surfaceType = SurfaceType.PAVED;
			} else if (wayType == WayType.PATH) {
				surfaceType = SurfaceType.UNPAVED;
			}
		}
		waySurfaceDesc.setSurfaceType(surfaceType);

	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge) {
		storage.setEdgeValue(edge.getEdge(), waySurfaceDesc);
	}

	@Override
	public String getName() {
		return "WaySurfaceType";
	}
}
