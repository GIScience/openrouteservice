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
package heigit.ors.routing;

public class RoutingProfileCategory {
	public static final int UNKNOWN = 0;
	public static final int DRIVING = 1;
	public static final int CYCLING  =2;
	public static final int WALKING = 3;
	public static final int WHEELCHAIR = 4;
	
	public static int getFromRouteProfile(int profileType)
	{
		if (RoutingProfileType.isDriving(profileType))
			return RoutingProfileCategory.DRIVING;

		if (RoutingProfileType.isCycling(profileType))
			return RoutingProfileCategory.CYCLING;
		
		if (RoutingProfileType.isWalking(profileType))
			return RoutingProfileCategory.WALKING;
		
		if (RoutingProfileType.WHEELCHAIR == profileType)
			return RoutingProfileCategory.WHEELCHAIR;
		
		return RoutingProfileCategory.UNKNOWN;
	}
}
