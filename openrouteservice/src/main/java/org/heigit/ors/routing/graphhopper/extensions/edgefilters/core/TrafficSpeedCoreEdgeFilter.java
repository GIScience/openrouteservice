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
import org.heigit.ors.routing.graphhopper.extensions.storages.TrafficGraphStorage;

/**
 * This class includes in the core all edges which have traffic speed assigned.
 *
 * @author Andrzej Oles
 */

public class TrafficSpeedCoreEdgeFilter implements EdgeFilter {
    private TrafficGraphStorage trafficGraphStorage;

    public TrafficSpeedCoreEdgeFilter(GraphHopperStorage graphHopperStorage) {
        trafficGraphStorage = GraphStorageUtils.getGraphExtension(graphHopperStorage, TrafficGraphStorage.class);
    }

    @Override
    public boolean accept(EdgeIteratorState edge) {
        if (trafficGraphStorage == null)
            return true;

        return !trafficGraphStorage.hasTrafficSpeed(edge.getEdge(), edge.getBaseNode(), edge.getAdjNode());
    }

    public static boolean hasTrafficGraphStorage(GraphHopperStorage graphHopperStorage) {
        return GraphStorageUtils.getGraphExtension(graphHopperStorage, TrafficGraphStorage.class) != null;
    }
}

