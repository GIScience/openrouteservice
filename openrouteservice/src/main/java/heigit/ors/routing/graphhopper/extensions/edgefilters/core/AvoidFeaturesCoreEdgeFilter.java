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
package heigit.ors.routing.graphhopper.extensions.edgefilters.core;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.CHEdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.AvoidFeatureFlags;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.WayCategoryGraphStorage;

public class AvoidFeaturesCoreEdgeFilter implements EdgeFilter {
	private byte[] buffer;
	private WayCategoryGraphStorage storage;
	private int avoidFeatures;
	private final String type = "avoid_features";

	public AvoidFeaturesCoreEdgeFilter(GraphStorage graphStorage, int profileCategory) {
		buffer = new byte[10];
		avoidFeatures = AvoidFeatureFlags.getProfileFlags(profileCategory);
		storage = GraphStorageUtils.getGraphExtension(graphStorage, WayCategoryGraphStorage.class);
	}
	public AvoidFeaturesCoreEdgeFilter(GraphStorage graphStorage, int profileCategory, int overrideClass) {
		this(graphStorage, -1);
		avoidFeatures = overrideClass;
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {
		if(iter instanceof CHEdgeIterator)
			if(((CHEdgeIterator)iter).isShortcut()) return true;

		return (storage.getEdgeValue(iter.getEdge(), buffer) & avoidFeatures) == 0;

	}

	public String getType() {
		return type;
	}

	public int getAvoidFeatures() {
		return avoidFeatures;
	}
}
