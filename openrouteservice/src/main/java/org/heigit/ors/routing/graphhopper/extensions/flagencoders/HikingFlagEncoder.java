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

import static com.graphhopper.routing.util.PriorityCode.BEST;
import static com.graphhopper.routing.util.PriorityCode.VERY_NICE;

public class HikingFlagEncoder extends FootFlagEncoder {
    public HikingFlagEncoder(PMap properties) {
        this((int) properties.getLong("speedBits", 4),
                properties.getDouble("speedFactor", 1));
        this.properties = properties;
        this.setBlockFords(properties.getBool("block_fords", false));
    }

    private HikingFlagEncoder(int speedBits, double speedFactor) {
        super(speedBits, speedFactor);

        hikingNetworkToCode.put("iwn", BEST.getValue());
        hikingNetworkToCode.put("nwn", BEST.getValue());
        hikingNetworkToCode.put("rwn", VERY_NICE.getValue());
        hikingNetworkToCode.put("lwn", VERY_NICE.getValue());

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

        init();
    }

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public String toString() {
        return FlagEncoderNames.HIKING_ORS;
    }
}
