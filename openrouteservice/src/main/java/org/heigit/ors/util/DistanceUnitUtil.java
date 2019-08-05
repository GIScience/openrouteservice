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
package heigit.ors.util;

import heigit.ors.common.DistanceUnit;
import heigit.ors.exceptions.StatusCodeException;

public class DistanceUnitUtil
{
	public static DistanceUnit getFromString(String value, DistanceUnit defaultValue)
	{
		switch (value)
		{
		case "meters":
			return DistanceUnit.Meters;
		case "m":
			return DistanceUnit.Meters;
		case "kilometers":
			return DistanceUnit.Kilometers;
		case "km":
			return DistanceUnit.Kilometers;
		case "miles":
			return DistanceUnit.Miles;
		case "mi":
			return DistanceUnit.Miles;
		}

		return defaultValue;
	}
	
	public static String toString( DistanceUnit unit)
	{
		switch (unit)
		{
		case Meters:
			return "m";
		case Kilometers:
			return "km";
		case Miles:
			return "mi";
		default:
			break;
		}

		return "";
	}


	public static double convert(double value, DistanceUnit unitsFrom, DistanceUnit unitsTo) throws StatusCodeException {
		if (unitsFrom == DistanceUnit.Meters)
		{
			switch(unitsTo)
			{
			case Meters:
				return value;
			case Kilometers:
				return value / 1000.0;
			case Miles:
				return value * 0.000621371192;
			default:
				break;
			}
			return value;
		}
		else
			throw new StatusCodeException(501, "Conversion not implemented.");
	}
}
