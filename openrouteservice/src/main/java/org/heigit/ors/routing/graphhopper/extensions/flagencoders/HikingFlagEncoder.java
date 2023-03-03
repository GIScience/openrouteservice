/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.util.PMap;

import java.util.Arrays;

import static com.graphhopper.routing.ev.RouteNetwork.*;
import static org.heigit.ors.routing.graphhopper.extensions.util.PriorityCode.BEST;
import static org.heigit.ors.routing.graphhopper.extensions.util.PriorityCode.VERY_NICE;

public class HikingFlagEncoder extends FootFlagEncoder {

    public HikingFlagEncoder(PMap properties) {
        this((int) properties.getLong("speedBits", 4),
                properties.getDouble("speedFactor", 1));
        setProperties(properties, false);
    }

    private HikingFlagEncoder(int speedBits, double speedFactor) {
        super(speedBits, speedFactor);

        routeMap.put(INTERNATIONAL, BEST.getValue());
        routeMap.put(NATIONAL, BEST.getValue());
        routeMap.put(REGIONAL, VERY_NICE.getValue());
        routeMap.put(LOCAL, VERY_NICE.getValue());

        suitableSacScales.addAll(Arrays.asList(
                "hiking",
                "mountain_hiking",
                "demanding_mountain_hiking",
                "alpine_hiking"
        ));

        preferredWayTags.addAll(Arrays.asList(
                "track",
                "path",
                "footway"
        ));
    }

    @Override
    public String toString() {
        return FlagEncoderNames.HIKING_ORS;
    }
}
