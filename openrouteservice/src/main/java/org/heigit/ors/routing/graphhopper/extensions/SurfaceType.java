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

        if ("paved".equalsIgnoreCase(surface)) {
            return SurfaceType.PAVED;
        } else if ("unpaved".equalsIgnoreCase(surface)) {
            return SurfaceType.UNPAVED;
        } else if ("asphalt".equalsIgnoreCase(surface)) {
            return SurfaceType.ASPHALT;
        } else if ("concrete".equalsIgnoreCase(surface) || "concrete:lanes".equalsIgnoreCase(surface)
                || "concrete:plates".equalsIgnoreCase(surface)) {
            return SurfaceType.CONCRETE;
        } else if ("paving_stones".equalsIgnoreCase(surface) || "paving_stones:20".equalsIgnoreCase(surface) || "paving_stones:30".equalsIgnoreCase(surface) || "paving_stones:50".equalsIgnoreCase(surface) || "paved_stones".equalsIgnoreCase(surface)) {
            return SurfaceType.PAVING_STONE;
        } else if ("cobblestone:flattened".equalsIgnoreCase(surface)
                || "sett".equalsIgnoreCase(surface)) {
            return SurfaceType.PAVING_STONE;
        } else if ("cobblestone".equalsIgnoreCase(surface)) {
            return SurfaceType.COBBLESTONE;
        } else if ("metal".equalsIgnoreCase(surface)) {
            return SurfaceType.METAL;
        } else if ("wood".equalsIgnoreCase(surface)) {
            return SurfaceType.WOOD;
        } else if ("compacted".equalsIgnoreCase(surface) || "pebblestone".equalsIgnoreCase(surface)) {
            return SurfaceType.COMPACTED_GRAVEL;
        } else if ("fine_gravel".equalsIgnoreCase(surface)) {
            return SurfaceType.FINE_GRAVEL;
        } else if ("gravel".equalsIgnoreCase(surface)) {
            return SurfaceType.GRAVEL;
        } else if ("dirt".equalsIgnoreCase(surface)) {
            return SurfaceType.DIRT;
        } else if ("ground".equalsIgnoreCase(surface) || "earth".equalsIgnoreCase(surface)
                || "mud".equalsIgnoreCase(surface)) {
            return SurfaceType.GROUND;
        } else if ("ice".equalsIgnoreCase(surface) || "snow".equalsIgnoreCase(surface)) {
            return SurfaceType.ICE;
        } else if ("sand".equalsIgnoreCase(surface)) {
            return SurfaceType.SAND;
        } else if ("woodchips".equalsIgnoreCase(surface)) {
            return SurfaceType.WOODCHIPS;
        } else if ("grass".equalsIgnoreCase(surface)) {
            return SurfaceType.GRASS;
        } else if ("grass_paver".equalsIgnoreCase(surface)) {
            return SurfaceType.GRASS_PAVER;
        }

        return SurfaceType.UNKNOWN;
    }

    @Override
    public int getOrdinal() {
        return this.ordinal();
    }
}
