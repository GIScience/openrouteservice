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

import com.graphhopper.routing.ev.HillIndex;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

/**
 * Special weighting for down/uphills
 * <p>
 *
 * @author Maxim Rylov
 */
public class AvoidHillsWeighting extends FastestWeighting {
    private final IntEncodedValue hillIndexEnc;
    private static final double[] PENALTY_FACTOR = {1.0, 1.0, 1.1, 1.5, 1.7, 1.8, 2.0, 2.2, 2.4, 2.6, 2.8, 3.2, 3.5, 3.7, 3.9, 4.2};

    public AvoidHillsWeighting(FlagEncoder encoder, PMap map, GraphHopperStorage graphStorage) {
        super(encoder, map);
        EncodingManager encodingManager = graphStorage.getEncodingManager();
        hillIndexEnc = encodingManager.hasEncodedValue(HillIndex.KEY) ? encodingManager.getIntEncodedValue(HillIndex.KEY) : null;
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
        if (hillIndexEnc != null) {
            int hillIndex = reverse ? edgeState.getReverse(hillIndexEnc) : edgeState.get(hillIndexEnc);

            if (hillIndex > PENALTY_FACTOR.length - 1) {
                return 100;
            }

            return PENALTY_FACTOR[hillIndex];
        }
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final AvoidHillsWeighting other = (AvoidHillsWeighting) obj;
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return ("AvoidHillsWeighting" + this).hashCode();
    }
}
