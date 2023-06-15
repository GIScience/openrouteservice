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
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;

public final class WheelchairCoreEdgeFilter implements EdgeFilter {
	private final byte[] buffer;
	private final WheelchairAttributesGraphStorage storage;
	private final WheelchairAttributes attributes;

	public WheelchairCoreEdgeFilter(GraphHopperStorage graphStorage) {
        buffer = new byte[WheelchairAttributesGraphStorage.BYTE_COUNT];
		attributes = new WheelchairAttributes();
		storage = GraphStorageUtils.getGraphExtension(graphStorage, WheelchairAttributesGraphStorage.class);
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {

		storage.getEdgeValues(iter.getEdge(), attributes, buffer);

		return !attributes.hasValues();

	}
}
