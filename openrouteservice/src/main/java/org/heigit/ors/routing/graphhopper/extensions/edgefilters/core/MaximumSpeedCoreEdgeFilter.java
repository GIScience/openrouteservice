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

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.EdgeIteratorState;

/**
 * This class includes in the core all edges with speed more than the one set in the ors-config.json file max_speed.
 *
 * @author Athanasios Kogios
 */

public class MaximumSpeedCoreEdgeFilter implements EdgeFilter {
    private final double maximumSpeedLowerBound;

    private final DecimalEncodedValue avSpeedEnc;

    public MaximumSpeedCoreEdgeFilter(FlagEncoder flagEncoder, double maximumSpeedLowerBound) {
        this.maximumSpeedLowerBound = maximumSpeedLowerBound;
        this.avSpeedEnc = flagEncoder.getAverageSpeedEnc();
    }

    @Override
    public boolean accept(EdgeIteratorState edge) {
        //If the max speed of the road is greater than that of the limit include it in the core.
        return edge.get(avSpeedEnc) <= maximumSpeedLowerBound && edge.getReverse(avSpeedEnc) <= maximumSpeedLowerBound;
    }
}
