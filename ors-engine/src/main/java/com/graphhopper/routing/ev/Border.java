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

public enum Border {
    NONE(0),
    CONTROLLED(1),
    OPEN(2);

    private final int value;

    private static final Border[] values = values();

    private Border(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static Border getFromId(int id) {
        return values[id];
    }

    public static EnumEncodedValue<Border> create() {
        return new EnumEncodedValue<>(Border.KEY, Border.class);
    }

    public static final String KEY = "border";
}
