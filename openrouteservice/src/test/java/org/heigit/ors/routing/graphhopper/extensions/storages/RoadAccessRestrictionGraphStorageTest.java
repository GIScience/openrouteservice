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

package org.heigit.ors.routing.graphhopper.extensions.storages;

import org.heigit.ors.routing.graphhopper.extensions.AccessRestrictionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoadAccessRestrictionGraphStorageTest {
    private final RoadAccessRestrictionsGraphStorage storage;
    byte[] buffer;

    public RoadAccessRestrictionGraphStorageTest() {
        storage = new RoadAccessRestrictionsGraphStorage();
        storage.init();
        storage.create(3);
        buffer = new byte[1];
    }

    @Test
    void testItemCreation() {
        storage.setEdgeValue(0, AccessRestrictionType.DESTINATION);
        storage.setEdgeValue(1, AccessRestrictionType.NONE);
        storage.setEdgeValue(2, AccessRestrictionType.PERMISSIVE);

        storage.getEdgeValue(0, buffer);
        assertEquals(AccessRestrictionType.DESTINATION, buffer[0]);
        storage.getEdgeValue(1, buffer);
        assertEquals(AccessRestrictionType.NONE, buffer[0]);
        storage.getEdgeValue(2, buffer);
        assertEquals(AccessRestrictionType.PERMISSIVE, buffer[0]);
    }
}
