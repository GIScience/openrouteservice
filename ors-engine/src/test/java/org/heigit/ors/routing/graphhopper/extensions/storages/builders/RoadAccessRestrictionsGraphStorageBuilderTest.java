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

package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.reader.ReaderWay;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.graphhopper.extensions.AccessRestrictionType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoadAccessRestrictionsGraphStorageBuilderTest {

    private RoadAccessRestrictionsGraphStorageBuilder builder;

    public RoadAccessRestrictionsGraphStorageBuilderTest() throws Exception {
    }

    @Test
    void testCarWayCreation() throws Exception {
        initBuilder(RoutingProfileType.DRIVING_CAR);
        ReaderWay way = new ReaderWay(1);
        way.setTag("access", "no");
        builder.processWay(way);
        assertEquals(AccessRestrictionType.NO, builder.getRestrictions());

        way = new ReaderWay(1);
        way.setTag("motorcar", "destination");
        builder.processWay(way);
        assertEquals(AccessRestrictionType.DESTINATION, builder.getRestrictions());

        way = new ReaderWay(1);
        way.setTag("motorcar", "yes");
        builder.processWay(way);
        assertEquals(AccessRestrictionType.NONE, builder.getRestrictions());
    }

    @Test
    void testBikeWayCreation() throws Exception {
        initBuilder(RoutingProfileType.CYCLING_REGULAR);
        ReaderWay way = new ReaderWay(1);
        way.setTag("access", "no");
        builder.processWay(way);
        assertEquals(AccessRestrictionType.NO, builder.getRestrictions());

        way = new ReaderWay(1);
        way.setTag("bicycle", "destination");
        builder.processWay(way);
        assertEquals(AccessRestrictionType.DESTINATION, builder.getRestrictions());

        way = new ReaderWay(1);
        way.setTag("bicycle", "yes");
        builder.processWay(way);
        assertEquals(AccessRestrictionType.NONE, builder.getRestrictions());
    }

    @Test
    void testFootWayCreation() throws Exception {
        initBuilder(RoutingProfileType.FOOT_WALKING);
        ReaderWay way = new ReaderWay(1);
        way.setTag("access", "no");
        builder.processWay(way);
        assertEquals(AccessRestrictionType.NO, builder.getRestrictions());

        way = new ReaderWay(1);
        way.setTag("foot", "destination");
        builder.processWay(way);
        assertEquals(AccessRestrictionType.DESTINATION, builder.getRestrictions());

        way = new ReaderWay(1);
        way.setTag("foot", "yes");
        builder.processWay(way);
        assertEquals(AccessRestrictionType.NONE, builder.getRestrictions());
    }

    private void initBuilder(int profileType) throws Exception {
        builder = new RoadAccessRestrictionsGraphStorageBuilder();
        builder.parameters = new HashMap<>();
        builder.parameters.put("use_for_warnings", "true");
        builder.init(null, profileType);
    }
}
