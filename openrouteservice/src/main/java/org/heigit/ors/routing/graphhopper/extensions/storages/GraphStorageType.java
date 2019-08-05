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
package heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.util.Helper;

public class GraphStorageType {
	public static final int VehicleType = 1;
	public static final int Restrictions = 2;
	public static final int WayCategory = 4;
	public static final int WaySurfaceType = 8;
	public static final int HillIndex = 16;
	
	public static boolean isSet(int type, int value)
	{
		return (type & value) == value;
	}

	public static int getFomString(String value)
	{
		if (Helper.isEmpty(value))
			return 0;

		int res = 0;

		String[] values = value.split("\\|");
		for (int i = 0; i < values.length; ++i) {
			switch (values[i].toLowerCase()) {
			case "vehicletype":
				res |= VehicleType;
				break;
			case "restrictions":
				res |= Restrictions;
				break;
			case "waycategory":
				res |= WayCategory;
				break;
			case "waysurfacetype":
				res |= WaySurfaceType;
				break;
			case "hillindex":
				res |= HillIndex;
				break;
			}
		}

		return res;
	}
}
