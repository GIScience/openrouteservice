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
package org.heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.AvoidFeatureFlags;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.TollwaysGraphStorage;
import org.heigit.ors.routing.pathprocessors.TollwayExtractor;

public class AvoidFeaturesEdgeFilter implements EdgeFilter {
    private final int avoidFeatureType;
    private TollwayExtractor tollwayExtractor = null;
    private final FormerWayCategory formerWayCategory;

    public AvoidFeaturesEdgeFilter(GraphHopperStorage graphStorage, int profileCategory, RouteSearchParameters searchParams) {
        this.avoidFeatureType = searchParams.getAvoidFeatureTypes() & AvoidFeatureFlags.getProfileFlags(profileCategory);

        formerWayCategory = new FormerWayCategory(graphStorage, avoidFeatureType);

        TollwaysGraphStorage extTollways = GraphStorageUtils.getGraphExtension(graphStorage, TollwaysGraphStorage.class);
        if (extTollways != null)
            tollwayExtractor = new TollwayExtractor(extTollways, searchParams.getProfileType(), searchParams.getProfileParameters());
    }

    public AvoidFeaturesEdgeFilter(GraphHopperStorage graphStorage, int avoidFeatureType) {
        if (avoidFeatureType == AvoidFeatureFlags.TOLLWAYS)
            throw new IllegalArgumentException("Invalid constructor for use with feature type: " + AvoidFeatureFlags.TOLLWAYS);
        formerWayCategory = new FormerWayCategory(graphStorage, avoidFeatureType);

        this.avoidFeatureType = avoidFeatureType;
    }

    @Override
    public final boolean accept(EdgeIteratorState iter) {
        return formerWayCategory.accept(iter)
            && acceptProfileSpecificTollways(iter);
    }

    private boolean acceptProfileSpecificTollways(EdgeIteratorState iter) {
        return tollwayExtractor == null
                || (avoidFeatureType & AvoidFeatureFlags.TOLLWAYS) == 0
                || !tollwayExtractor.isProfileSpecificTollway(iter.getEdge());
    }
}
