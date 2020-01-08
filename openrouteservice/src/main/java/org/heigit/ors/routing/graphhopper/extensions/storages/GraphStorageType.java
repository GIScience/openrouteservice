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
package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.util.Helper;

public class GraphStorageType {
	private static final int VEHICLE_TYPE = 1;
	private static final int RESTRICTIONS = 2;
	private static final int WAY_CATEGORY = 4;
	private static final int WAY_SURFACE_TYPE = 8;
	private static final int HILL_INDEX = 16;

	private GraphStorageType() {}

	public static boolean isSet(int type, int value)
	{
		return (type & value) == value;
	}

	public static int getFomString(String value) {
		if (Helper.isEmpty(value))
			return 0;

		int res = 0;
		String[] values = value.split("\\|");
		for (int i = 0; i < values.length; ++i) {
			switch (values[i].toLowerCase()) {
				case "vehicletype":
					res |= VEHICLE_TYPE;
					break;
				case "restrictions":
					res |= RESTRICTIONS;
					break;
				case "waycategory":
					res |= WAY_CATEGORY;
					break;
				case "waysurfacetype":
					res |= WAY_SURFACE_TYPE;
					break;
				case "hillindex":
					res |= HILL_INDEX;
					break;
				default:
			}
		}
		return res;
	}
}
