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

public class PedestrianFlagEncoder extends FootFlagEncoder {

    public PedestrianFlagEncoder(PMap properties) {
        this(properties.getInt("speed_bits", 4), properties.getDouble("speed_factor", 1), properties.getBool("speed_two_directions", false));
        setProperties(properties);
    }

    private PedestrianFlagEncoder(int speedBits, double speedFactor, boolean speedTwoDirections) {
        super(speedBits, speedFactor, speedTwoDirections);

        suitableSacScales.add("hiking");
    }

    @Override
    public String getName() {
        return FlagEncoderNames.PEDESTRIAN_ORS;
    }
}
