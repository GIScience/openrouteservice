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
    // Keep in sync with ORS documentation: surface.md
    UNKNOWN,
    PAVED,
    UNPAVED,
    ASPHALT,
    CONCRETE,
    METAL,
    WOOD,
    COMPACTED_GRAVEL,
    GRAVEL,
    DIRT,
    GROUND,
    ICE,
    PAVING_STONES,
    SAND,
    GRASS,
    GRASS_PAVER;

    public static final String KEY = "ors_surface";
}
