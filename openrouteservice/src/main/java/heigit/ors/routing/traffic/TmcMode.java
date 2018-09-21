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
package heigit.ors.routing.traffic;

import heigit.ors.routing.RoutingProfileType;

public class TmcMode {
	
	// currently only car and heavy vehicle are used
	public static final int CAR = RoutingProfileType.DRIVING_CAR;
	
	public static final int BUS = 2;
	
	public static final int LORRIES = 3;
	
	public static final int HIGH_SIDED_VEHICLE = 4;
	
	public static final int HEAVY_VEHICLE = RoutingProfileType.DRIVING_HGV;
	
	public static final int VEHICLE_WITH_TRAILER =  6;	
	
	// maybe more modes in the tmc messages
	
}