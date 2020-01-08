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
package org.heigit.ors.routing.graphhopper.extensions;

public class HeavyVehicleAttributes {
	public static final int UNKNOWN = 0;
	// Vehicle type and
	public static final int GOODS = 1;
	public static final int HGV = 2;
	public static final int BUS = 4;
	public static final int AGRICULTURE = 8;
	public static final int FORESTRY = 16;
	public static final int DELIVERY = 32;
	public static final int ANY = GOODS | HGV | BUS | AGRICULTURE | FORESTRY | DELIVERY;
	// Load characteristics
	public static final int HAZMAT = 128;

	private HeavyVehicleAttributes() {}

	public static int getFromString(String value) {
		if ("goods".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.GOODS;
		} else if ("hgv".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.HGV;
		} else if ("bus".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.BUS;
		} else if ("agricultural".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.AGRICULTURE;
		} else if ("forestry".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.FORESTRY;
		} else if ("delivery".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.DELIVERY;
	    }
		return HeavyVehicleAttributes.UNKNOWN;
	}
}