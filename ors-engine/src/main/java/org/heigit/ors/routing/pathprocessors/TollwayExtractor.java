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
package org.heigit.ors.routing.pathprocessors;

import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.Toll;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.RoutingProfileType;

public class TollwayExtractor {
    private final int profileType;
    private final EnumEncodedValue<Toll> tollEnc;

    public TollwayExtractor(EnumEncodedValue<Toll> tollEnc, int profileType) {
        this.tollEnc = tollEnc;
        this.profileType = profileType;
    }

    /**
     * return whether a way is a tollway for the configured vehicle.
     *
     * @param edge the edge to check
     */
    public boolean isProfileSpecificTollway(EdgeIteratorState edge) {
        Toll value = edge.get(tollEnc);

        return switch (value) {
            case ALL -> true;
            case HGV -> profileType == RoutingProfileType.DRIVING_HGV;
            default -> false;
        };
    }

}
