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

public enum WayType {
    // Keep in sync with documentation: waytype.md
    UNKNOWN(0),
    STATE_ROAD(1),
    ROAD(2),
    STREET(3),
    PATH(4),
    TRACK(5),
    CYCLEWAY(6),
    FOOTWAY(7),
    STEPS(8),
    FERRY(9),
    CONSTRUCTION(10);

    private final int value;

    private static final WayType[] values = values();

    private WayType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static WayType getFromId(int id) {
        return values[id];
    }

    public static final String KEY = "way_type";
}
