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
package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.querygraph.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.ShadowIndexGraphStorage;

public class ShadowWeighting extends FastestWeighting {

    private static final Logger LOGGER = Logger.getLogger(ShadowWeighting.class.getName());
    private final ShadowIndexGraphStorage shadowIndexStorage;
    private final byte[] buffer = new byte[1];
    private final double userWeighting;

    public ShadowWeighting(FlagEncoder encoder, PMap map, GraphHopperStorage graphStorage) {
        super(encoder, map);
        userWeighting = map.getDouble("factor", 1);
        shadowIndexStorage = GraphStorageUtils.getGraphExtension(graphStorage, ShadowIndexGraphStorage.class);
        if (shadowIndexStorage == null) {
            LOGGER.error("ShadowIndexStorage not found.");
        }
    }

    private double calShadowWeighting(int shadowIndexValue) {
        double amplifier = 5.; // amplify influence of shadow
        return shadowIndexValue * 0.01 * amplifier * userWeighting;
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
        int shadowValue = shadowIndexStorage
                .getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edgeState), buffer);
        return calShadowWeighting(shadowValue);
    }

    @Override
    public String getName() {
        return "shadow";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ShadowWeighting other = (ShadowWeighting) obj;
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return ("ShadowWeighting" + this).hashCode();
    }
}
