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

public class VehicleDimensionRestrictions {
	public static final int MAX_HEIGHT = 0;
	public static final int MAX_WEIGHT = 1;
	public static final int MAX_WIDTH = 2;
	public static final int MAX_LENGTH = 3;
	public static final int MAX_AXLE_LOAD = 4;
	public static final int COUNT = 5;

	private VehicleDimensionRestrictions() {}
}
