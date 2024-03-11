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

package org.heigit.ors.routing.graphhopper.extensions;

public class OSMTags {
    private OSMTags() {
    }

    public static class Keys {
        private Keys() {
        }

        public static final String HIGHWAY = "highway";
        public static final String SIDEWALK = "sidewalk";
        public static final String ROUTE = "route";
        public static final String FOOT = "foot";
        public static final String SAC_SCALE = "sac_scale";
        public static final String FORD = "ford";
        public static final String MOTOR_ROAD = "motorroad";
        public static final String JUNCTION = "junction";
        public static final String RAILWAY = "railway";
        public static final String MAN_MADE = "man_made";
        public static final String CYCLEWAY = "cycleway";
        public static final String TUNNEL = "tunnel";
        public static final String BICYCLE = "bicycle";
        public static final String WATERWAY = "waterway";
    }
}
