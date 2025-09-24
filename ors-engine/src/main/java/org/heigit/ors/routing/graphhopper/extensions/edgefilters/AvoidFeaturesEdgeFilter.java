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

import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.AvoidFeatureFlags;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.RoutingProfileCategory;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.TollwaysGraphStorage;
import org.heigit.ors.routing.pathprocessors.TollwayExtractor;

public class AvoidFeaturesEdgeFilter implements EdgeFilter {
    private TollwayExtractor tollwayExtractor;
    private final int avoidFeatureType;
    private BooleanEncodedValue highwayEnc = null;
    private BooleanEncodedValue fordEnc = null;
    private EnumEncodedValue<WayType> wayTypeEnc;


    public AvoidFeaturesEdgeFilter(int profileType, RouteSearchParameters searchParams, GraphHopperStorage graphStorage) {
        handleEncodedValues(graphStorage);

        int profileCategory = RoutingProfileCategory.getFromRouteProfile(profileType);
        this.avoidFeatureType = searchParams.getAvoidFeatureTypes() & AvoidFeatureFlags.getProfileFlags(profileCategory);

        TollwaysGraphStorage extTollways = GraphStorageUtils.getGraphExtension(graphStorage, TollwaysGraphStorage.class);
        if (extTollways != null)
            tollwayExtractor = new TollwayExtractor(extTollways, searchParams.getProfileType(), searchParams.getProfileParameters());
    }

    public AvoidFeaturesEdgeFilter(int avoidFeatureType, GraphHopperStorage graphStorage) {
        if (avoidFeatureType == AvoidFeatureFlags.TOLLWAYS)
            throw new IllegalArgumentException("Invalid constructor for use with feature type: " + AvoidFeatureFlags.TOLLWAYS);
        handleEncodedValues(graphStorage);

        this.avoidFeatureType = avoidFeatureType;
    }

    private void handleEncodedValues(GraphHopperStorage graphStorage) {
        var encodingManager = graphStorage.getEncodingManager();
        if (encodingManager.hasEncodedValue(Highway.KEY))
            highwayEnc = encodingManager.getBooleanEncodedValue(Highway.KEY);
        if (encodingManager.hasEncodedValue(Ford.KEY))
            fordEnc = encodingManager.getBooleanEncodedValue(Ford.KEY);
        if (encodingManager.hasEncodedValue(WayType.KEY))
            wayTypeEnc = encodingManager.getEnumEncodedValue(WayType.KEY, WayType.class);
    }

    @Override
    public final boolean accept(EdgeIteratorState iter) {
        return avoidFeatureType == 0
            || (acceptHighways(iter)
                    && acceptFords(iter)
                    && acceptSteps(iter)
                    && acceptFerries(iter)
                    && acceptTollways(iter)
                );
    }

    private boolean acceptFerries(EdgeIteratorState iter) {
        return wayTypeEnc == null
                || (avoidFeatureType & AvoidFeatureFlags.FERRIES) == 0
                || iter.get(wayTypeEnc) != WayType.FERRY;
    }

    private boolean acceptSteps(EdgeIteratorState iter) {
        return wayTypeEnc == null
                || (avoidFeatureType & AvoidFeatureFlags.STEPS) == 0
                || iter.get(wayTypeEnc) != WayType.STEPS;
    }

    private boolean acceptFords(EdgeIteratorState iter) {
        return fordEnc == null
                || (avoidFeatureType & AvoidFeatureFlags.FORDS) == 0
                || !iter.get(fordEnc);
    }

    private boolean acceptHighways(EdgeIteratorState iter) {
        return highwayEnc == null
                || (avoidFeatureType & AvoidFeatureFlags.HIGHWAYS) == 0
                || !iter.get(highwayEnc);
    }

    private boolean acceptTollways(EdgeIteratorState iter) {
        return tollwayExtractor == null
                || (avoidFeatureType & AvoidFeatureFlags.TOLLWAYS) == 0
                || !tollwayExtractor.isProfileSpecificTollway(iter.getEdge());
    }
}
