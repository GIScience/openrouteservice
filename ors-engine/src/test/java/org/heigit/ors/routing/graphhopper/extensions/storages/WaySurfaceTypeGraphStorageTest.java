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
package org.heigit.ors.routing.graphhopper.extensions.storages;

import org.heigit.ors.routing.graphhopper.extensions.SurfaceType;
import org.heigit.ors.routing.util.WaySurfaceDescription;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaySurfaceTypeGraphStorageTest {
    private final WaySurfaceTypeGraphStorage _storage;

    public WaySurfaceTypeGraphStorageTest() {
        _storage = new WaySurfaceTypeGraphStorage();
        _storage.init();
        _storage.create(1);
    }

    @Test
    void TestWaySurfaceStorage() {
        WaySurfaceDescription waySurfaceDescription = new WaySurfaceDescription();
        byte [] buffer = new byte[1];

        for(SurfaceType surface: SurfaceType.values()) {
            waySurfaceDescription.setSurfaceType(surface);
            _storage.setEdgeValue(1, waySurfaceDescription);
            assertEquals(surface, _storage.getEdgeValue(1, buffer).getSurfaceType());
        }
    }
}
