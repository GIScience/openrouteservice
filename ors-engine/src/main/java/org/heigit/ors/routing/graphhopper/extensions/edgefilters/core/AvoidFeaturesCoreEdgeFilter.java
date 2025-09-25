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
import org.heigit.ors.routing.graphhopper.extensions.TollwayType;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.FormerWayCategory;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.TollwaysGraphStorage;

public class AvoidFeaturesCoreEdgeFilter implements EdgeFilter {
    private int avoidFeatures;
    private static final String TYPE = "avoid_features";
    private final FormerWayCategory formerWayCategory;
    private final TollwaysGraphStorage tollwaysStorage;


    public AvoidFeaturesCoreEdgeFilter(GraphHopperStorage graphStorage, int profileCategory) {
        avoidFeatures = AvoidFeatureFlags.getProfileFlags(profileCategory);
        formerWayCategory = new FormerWayCategory(graphStorage, avoidFeatures);
        tollwaysStorage = GraphStorageUtils.getGraphExtension(graphStorage, TollwaysGraphStorage.class);
    }

    public AvoidFeaturesCoreEdgeFilter(GraphHopperStorage graphStorage, int profileCategory, int overrideClass) {
        avoidFeatures = overrideClass;
        formerWayCategory = new FormerWayCategory(graphStorage, avoidFeatures);
        tollwaysStorage = null;
    }

    @Override
    public final boolean accept(EdgeIteratorState iter) {
        if (iter instanceof RoutingCHEdgeIterator iterator && iterator.isShortcut())
            return true;
        return formerWayCategory.accept(iter)
            && acceptGenericTollways(iter);
    }

    private boolean acceptGenericTollways(EdgeIteratorState iter) {
        return tollwaysStorage == null
                || (avoidFeatures & AvoidFeatureFlags.TOLLWAYS) == 0
                || tollwaysStorage.getEdgeValue(iter.getEdge()) == TollwayType.NONE;
    }

    public String getType() {
        return TYPE;
    }

    public int getAvoidFeatures() {
        return avoidFeatures;
    }
}
