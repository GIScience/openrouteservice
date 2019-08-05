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

package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.reader.ReaderWay;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.graphhopper.extensions.AccessRestrictionType;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class RoadAccessRestrictionsGraphStorageBuilderTest {

    private RoadAccessRestrictionsGraphStorageBuilder builder;

    public RoadAccessRestrictionsGraphStorageBuilderTest() throws Exception {
    }

    @Test
    public void testCarWayCreation() throws Exception {
        initBuilder(RoutingProfileType.DRIVING_CAR);
        ReaderWay way = new ReaderWay(1);
        way.setTag("access", "no");
        builder.processWay(way);
        Assert.assertEquals(AccessRestrictionType.No, builder.getRestrictions());

        way = new ReaderWay(1);
        way.setTag("motorcar", "destination");
        builder.processWay(way);
        Assert.assertEquals(AccessRestrictionType.Destination, builder.getRestrictions());

        way = new ReaderWay(1);
        way.setTag("motorcar", "yes");
        builder.processWay(way);
        Assert.assertEquals(AccessRestrictionType.None, builder.getRestrictions());
    }

    @Test
    public void testBikeWayCreation() throws Exception {
        initBuilder(RoutingProfileType.CYCLING_REGULAR);
        ReaderWay way = new ReaderWay(1);
        way.setTag("access", "no");
        builder.processWay(way);
        Assert.assertEquals(AccessRestrictionType.No, builder.getRestrictions());

        way = new ReaderWay(1);
        way.setTag("bicycle", "destination");
        builder.processWay(way);
        Assert.assertEquals(AccessRestrictionType.Destination, builder.getRestrictions());

        way = new ReaderWay(1);
        way.setTag("bicycle", "yes");
        builder.processWay(way);
        Assert.assertEquals(AccessRestrictionType.None, builder.getRestrictions());
    }

    @Test
    public void testFootWayCreation() throws Exception {
        initBuilder(RoutingProfileType.FOOT_WALKING);
        ReaderWay way = new ReaderWay(1);
        way.setTag("access", "no");
        builder.processWay(way);
        Assert.assertEquals(AccessRestrictionType.No, builder.getRestrictions());

        way = new ReaderWay(1);
        way.setTag("foot", "destination");
        builder.processWay(way);
        Assert.assertEquals(AccessRestrictionType.Destination, builder.getRestrictions());

        way = new ReaderWay(1);
        way.setTag("foot", "yes");
        builder.processWay(way);
        Assert.assertEquals(AccessRestrictionType.None, builder.getRestrictions());
    }

    private void initBuilder(int profileType) throws Exception {
        builder = new RoadAccessRestrictionsGraphStorageBuilder();
        builder._parameters = new HashMap<>();
        builder._parameters.put("use_for_warnings", "true");
        builder.init(null, profileType);
    }
}
