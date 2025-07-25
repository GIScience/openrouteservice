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
package org.heigit.ors.routing.graphhopper.extensions;

public enum SurfaceType {

    //Keep in sync with documentation: surface.md

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

    private static final SurfaceType[] values = values();

    private SurfaceType(int value) {
        this.value = (byte) value;
    }

    public byte value() {
        return value;
    }

    public static SurfaceType getFromId(int id) {
        return values[id];
    }

    public static SurfaceType getFromString(String surface) {

        if (surface.contains(";"))
            surface = surface.split(";")[0];
        if (surface.contains(":"))
            surface = surface.split(":")[0];

        return switch (surface.toLowerCase()) {
            case "paved" -> SurfaceType.PAVED;
            case "unpaved", "woodchips", "rock", "rocks", "stone", "shells", "salt" -> SurfaceType.UNPAVED;
            case "asphalt", "chipseal", "bitmac", "tarmac" -> SurfaceType.ASPHALT;
            case "concrete", "cement" -> SurfaceType.CONCRETE;
            case "paving_stones", "paved_stones", "sett", "cobblestone", "unhewn_cobblestone", "bricks", "brick" -> SurfaceType.PAVING_STONES;
            case "metal" -> SurfaceType.METAL;
            case "wood" -> SurfaceType.WOOD;
            case "compacted", "pebblestone" -> SurfaceType.COMPACTED_GRAVEL;
            case "gravel", "fine_gravel" -> SurfaceType.GRAVEL;
            case "dirt", "earth", "soil" -> SurfaceType.DIRT;
            case "ground", "mud" -> SurfaceType.GROUND;
            case "ice", "snow" -> SurfaceType.ICE;
            case "sand" -> SurfaceType.SAND;
            case "grass" -> SurfaceType.GRASS;
            case "grass_paver" -> SurfaceType.GRASS_PAVER;
            default -> SurfaceType.UNKNOWN;
        };
    }
}
