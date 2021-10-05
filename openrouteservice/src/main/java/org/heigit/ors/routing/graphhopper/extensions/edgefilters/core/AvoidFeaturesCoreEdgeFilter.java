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
package org.heigit.ors.routing.graphhopper.extensions.edgefilters.core;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RoutingCHEdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.AvoidFeatureFlags;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.WayCategoryGraphStorage;

public class AvoidFeaturesCoreEdgeFilter implements EdgeFilter {
	private final byte[] buffer;
	private final WayCategoryGraphStorage storage;
	private int avoidFeatures;
	private static final String TYPE = "avoid_features";

	public AvoidFeaturesCoreEdgeFilter(GraphHopperStorage graphStorage, int profileCategory) {
		buffer = new byte[10];
		avoidFeatures = AvoidFeatureFlags.getProfileFlags(profileCategory);
		storage = GraphStorageUtils.getGraphExtension(graphStorage, WayCategoryGraphStorage.class);
	}

	public AvoidFeaturesCoreEdgeFilter(GraphHopperStorage graphStorage, int profileCategory, int overrideClass) {
		this(graphStorage, profileCategory);
		avoidFeatures = overrideClass;
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {
		if(iter instanceof RoutingCHEdgeIterator && ((RoutingCHEdgeIterator)iter).isShortcut())
			return true;
		return (storage.getEdgeValue(iter.getEdge(), buffer) & avoidFeatures) == 0;
	}

	public String getType() {
		return TYPE;
	}

	public int getAvoidFeatures() {
		return avoidFeatures;
	}
}
