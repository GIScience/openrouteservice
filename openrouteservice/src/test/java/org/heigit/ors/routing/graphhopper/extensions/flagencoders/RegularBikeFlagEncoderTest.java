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

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodingManager;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.bike.RegularBikeFlagEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegularBikeFlagEncoderTest {
    private final RegularBikeFlagEncoder flagEncoder;
    private ReaderWay way;

    public RegularBikeFlagEncoderTest() {
        EncodingManager encodingManager = EncodingManager.create(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.BIKE_ORS);
        flagEncoder = (RegularBikeFlagEncoder) encodingManager.getEncoder(FlagEncoderNames.BIKE_ORS);
    }

    @BeforeEach
    void initWay() {
        way = new ReaderWay(1);
    }

    @Test
    void acceptBridlewayOnlyWithBicycleTag() {
        way.setTag("highway", "bridleway");
        assertTrue(flagEncoder.getAccess(way).canSkip());

        way.setTag("bicycle", "yes");
        assertTrue(flagEncoder.getAccess(way).isWay());

        way.setTag("bicycle", "no");
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }
}
