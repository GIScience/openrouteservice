/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
