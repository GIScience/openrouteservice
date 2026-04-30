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

public enum AccessRestriction {
    // Keep in sync with documentation: road-access-restrictions.md
    NONE(0),
    NO(1),
    CUSTOMERS(2),
    DESTINATION(4),
    DELIVERY(8),
    PRIVATE(16),
    PERMISSIVE(32),
    PERMIT(64);

    private final byte value;

    private AccessRestriction(int value) {
        this.value = (byte) value;
    }

    public byte value() {
        return value;
    }

    public static AccessRestriction fromValue(int value) {
        for (AccessRestriction restriction : AccessRestriction.values()) {
            if (restriction.value == (byte) value) {
                return restriction;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    public static final String KEY = "access_restriction";
}
