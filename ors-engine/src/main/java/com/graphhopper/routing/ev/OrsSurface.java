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
package com.graphhopper.routing.ev;

public enum OrsSurface {
    // Keep in sync with documentation: surface.md
    UNKNOWN(0),
    PAVED(1),
    UNPAVED(2),
    ASPHALT(3),
    CONCRETE(4),
    METAL(6),
    WOOD(7),
    COMPACTED_GRAVEL(8),
    GRAVEL(10),
    DIRT(11),
    GROUND(12),
    ICE(13),
    PAVING_STONES(14),
    SAND(15),
    GRASS(17),
    GRASS_PAVER(18);

    private final byte value;

    private OrsSurface(int value) {
        this.value = (byte) value;
    }

    public byte value() {
        return value;
    }

    public static final String KEY = "ors_surface";
}
