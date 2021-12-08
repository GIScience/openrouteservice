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
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.HereTrafficGraphStorage;

/**
 * This class includes in the core all edges which have traffic speed assigned.
 *
 * @author Andrzej Oles
 */

public class TrafficSpeedCoreEdgeFilter implements EdgeFilter {
    private HereTrafficGraphStorage hereTrafficGraphStorage;

    public TrafficSpeedCoreEdgeFilter(GraphHopperStorage graphHopperStorage) {
        hereTrafficGraphStorage = GraphStorageUtils.getGraphExtension(graphHopperStorage, HereTrafficGraphStorage.class);
    }

    @Override
    public boolean accept(EdgeIteratorState edge) {
        if (hereTrafficGraphStorage == null)
            return true;

        return !hereTrafficGraphStorage.hasTrafficSpeed(edge.getEdge(), edge.getBaseNode(), edge.getAdjNode());
    }

    public static boolean hasTrafficGraphStorage(GraphHopperStorage graphHopperStorage) {
        return GraphStorageUtils.getGraphExtension(graphHopperStorage, HereTrafficGraphStorage.class) != null;
    }
}

