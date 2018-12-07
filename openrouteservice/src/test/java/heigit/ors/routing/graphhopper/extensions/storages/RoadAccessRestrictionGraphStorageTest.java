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

package heigit.ors.routing.graphhopper.extensions.storages;

import heigit.ors.routing.graphhopper.extensions.AccessRestrictionType;
import org.junit.Assert;
import org.junit.Test;

public class RoadAccessRestrictionGraphStorageTest {
    private RoadAccessRestrictionsGraphStorage storage;
    byte[] buffer;

    public RoadAccessRestrictionGraphStorageTest() {
        storage = new RoadAccessRestrictionsGraphStorage();
        storage.init();
        storage.create(3);
        buffer = new byte[1];
    }

    @Test
    public void testItemCreation() {
        storage.setEdgeValue(0, AccessRestrictionType.Destination);
        storage.setEdgeValue(1, AccessRestrictionType.None);
        storage.setEdgeValue(2, AccessRestrictionType.Permissive);

        storage.getEdgeValue(0, buffer);
        Assert.assertEquals(AccessRestrictionType.Destination, buffer[0]);
        storage.getEdgeValue(1, buffer);
        Assert.assertEquals(AccessRestrictionType.None, buffer[0]);
        storage.getEdgeValue(2, buffer);
        Assert.assertEquals(AccessRestrictionType.Permissive, buffer[0]);
    }
}
