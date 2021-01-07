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

public enum SurfaceType implements PropertyType {
    UNKNOWN,
    PAVED,
    UNPAVED,
    ASPHALT,
    CONCRETE,
    COBBLESTONE,
    METAL,
    WOOD,
    COMPACTED_GRAVEL,
    FINE_GRAVEL,
    GRAVEL,
    DIRT,
    GROUND,
    ICE,
    PAVING_STONE,
    SAND,
    WOODCHIPS,
    GRASS,
    GRASS_PAVER;

    public static SurfaceType getFromString(String surface) {

        if (surface.contains(";"))
            surface = surface.split(";")[0];

        switch (surface.toLowerCase()) {
            case "paved":
                return SurfaceType.PAVED;
            case "unpaved":
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
                return SurfaceType.PAVING_STONE;
            case "cobblestone":
                return SurfaceType.COBBLESTONE;
            case "metal":
                return SurfaceType.METAL;
            case "wood":
                return SurfaceType.WOOD;
            case "compacted":
            case "pebblestone":
                return SurfaceType.COMPACTED_GRAVEL;
            case "fine_gravel":
                return SurfaceType.FINE_GRAVEL;
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
            case "woodchips":
                return SurfaceType.WOODCHIPS;
            case "grass":
                return SurfaceType.GRASS;
            case "grass_paver":
                return SurfaceType.GRASS_PAVER;
            default:
                return SurfaceType.UNKNOWN;
        }
    }

    @Override
    public int getOrdinal() {
        return this.ordinal();
    }
}
