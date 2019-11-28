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
	private DistanceUnitUtil() {}

	public static DistanceUnit getFromString(String value, DistanceUnit defaultValue) {
		switch (value) {
			case "m":
			case "meters":
				return DistanceUnit.METERS;
			case "km":
			case "kilometers":
				return DistanceUnit.KILOMETERS;
			case "mi":
			case "miles":
				return DistanceUnit.MILES;
			default:
				return defaultValue;
		}
	}
	
	public static String toString( DistanceUnit unit) {
		switch (unit) {
			case METERS:
				return "m";
			case KILOMETERS:
				return "km";
			case MILES:
				return "mi";
			default:
				return "";
		}
	}

	public static double convert(double value, DistanceUnit unitsFrom, DistanceUnit unitsTo) throws StatusCodeException {
		if (unitsFrom == DistanceUnit.METERS) {
			switch(unitsTo) {
				case KILOMETERS:
					return value / 1000.0;
				case MILES:
					return value * 0.000621371192;
				case METERS:
				default:
					return value;
			}
		}
		throw new StatusCodeException(501, "Conversion not implemented.");
	}
}
