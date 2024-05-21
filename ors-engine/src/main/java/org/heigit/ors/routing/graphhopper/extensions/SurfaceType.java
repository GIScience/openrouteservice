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
    PAVING_STONE(14),
    SAND(15),
    GRASS(17),
    GRASS_PAVER(18);

    private final byte value;

    private static final SurfaceType values[] = values();

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

        switch (surface.toLowerCase()) {
            case "paved":
                return SurfaceType.PAVED;
            case "unpaved", "woodchips":
                return SurfaceType.UNPAVED;
            case "asphalt":
                return SurfaceType.ASPHALT;
            case "concrete":
            case "concrete:lanes":
            case "concrete:plates":
                return SurfaceType.CONCRETE;
            case "paving_stones":
            case "paving_stones:20":
            case "paving_stones:30":
            case "paving_stones:50":
            case "paved_stones":
            case "cobblestone:flattened":
            case "sett":
            case "cobblestone":
                return SurfaceType.PAVING_STONE;
            case "metal":
                return SurfaceType.METAL;
            case "wood":
                return SurfaceType.WOOD;
            case "compacted":
            case "pebblestone":
                return SurfaceType.COMPACTED_GRAVEL;
            case "fine_gravel":
            case "gravel":
                return SurfaceType.GRAVEL;
            case "dirt":
                return SurfaceType.DIRT;
            case "ground":
            case "earth":
            case "mud":
                return SurfaceType.GROUND;
            case "ice":
            case "snow":
                return SurfaceType.ICE;
            case "sand":
                return SurfaceType.SAND;
            case "grass":
                return SurfaceType.GRASS;
            case "grass_paver":
                return SurfaceType.GRASS_PAVER;
        }

        return SurfaceType.UNKNOWN;
    }
}
