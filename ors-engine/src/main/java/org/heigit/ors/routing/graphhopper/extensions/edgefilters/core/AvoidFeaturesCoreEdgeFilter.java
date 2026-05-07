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

import com.graphhopper.routing.ev.Toll;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RoutingCHEdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.AvoidFeatureFlags;
import org.heigit.ors.routing.RoutingProfileCategory;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.FormerWayCategory;
import org.heigit.ors.routing.pathprocessors.TollwayExtractor;

public class AvoidFeaturesCoreEdgeFilter implements EdgeFilter {
    private final int avoidFeatures;
    private static final String TYPE = "avoid_features";
    private final FormerWayCategory formerWayCategory;
    private final TollwayExtractor tollwayExtractor;


    public AvoidFeaturesCoreEdgeFilter(GraphHopperStorage graphStorage, int profileType) {
        this(graphStorage, profileType, AvoidFeatureFlags.getProfileFlags(RoutingProfileCategory.getFromRouteProfile(profileType)));
    }

    public AvoidFeaturesCoreEdgeFilter(GraphHopperStorage graphStorage, int profileType, int avoidFeatures) {
        this.avoidFeatures = avoidFeatures;
        formerWayCategory = new FormerWayCategory(graphStorage, avoidFeatures);
        EncodingManager encodingManager = graphStorage.getEncodingManager();
        tollwayExtractor = encodingManager.hasEncodedValue(Toll.KEY) ?
                new TollwayExtractor(encodingManager.getEnumEncodedValue(Toll.KEY, Toll.class), profileType) :
                null;
    }

    @Override
    public final boolean accept(EdgeIteratorState iter) {
        if (iter instanceof RoutingCHEdgeIterator iterator && iterator.isShortcut())
            return true;
        return formerWayCategory.accept(iter)
            && acceptGenericTollways(iter);
    }

    private boolean acceptGenericTollways(EdgeIteratorState iter) {
        return tollwayExtractor == null
                || (avoidFeatures & AvoidFeatureFlags.TOLLWAYS) == 0
                || !tollwayExtractor.isProfileSpecificTollway(iter);
    }

    public String getType() {
        return TYPE;
    }

    public int getAvoidFeatures() {
        return avoidFeatures;
    }
}
