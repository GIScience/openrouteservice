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
package org.heigit.ors.util;

import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.exceptions.StatusCodeException;

public class DistanceUnitUtil {
    private DistanceUnitUtil() {
    }

    public static DistanceUnit getFromString(String value, DistanceUnit defaultValue) {
        return switch (value) {
            case "m", "meters" -> DistanceUnit.METERS;
            case "km", "kilometers" -> DistanceUnit.KILOMETERS;
            case "mi", "miles" -> DistanceUnit.MILES;
            default -> defaultValue;
        };
    }

    public static String toString(DistanceUnit unit) {
        return switch (unit) {
            case METERS -> "m";
            case KILOMETERS -> "km";
            case MILES -> "mi";
            default -> "";
        };
    }

    public static double convert(double value, DistanceUnit unitsFrom, DistanceUnit unitsTo) throws StatusCodeException {
        if (unitsFrom == DistanceUnit.METERS) {
            return switch (unitsTo) {
                case KILOMETERS -> value / 1000.0;
                case MILES -> value * 0.000621371192;
                case METERS -> value;
                default -> value;
            };
        }
        throw new StatusCodeException(501, "Conversion not implemented.");
    }
}
