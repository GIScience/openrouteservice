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

public class AvoidFeatureFlags {
	public static final int Highways = 1; // 1 << 0;
	public static final int Tollways = 2; // 1 << 1;
	public static final int Steps = 4; // 1 << 2;
	public static final int Ferries = 8; // 1 << 3;
	public static final int Fords = 16; // 1 << 4;

	public static final int DrivingFeatures = Highways | Tollways | Ferries | Fords;
	public static final int CyclingFeatures = Steps | Ferries | Fords;
	public static final int WalkingFeatures =  Steps | Ferries | Fords;
	public static final int WheelchairFeatures = WalkingFeatures;

	public static int getFromString(String value)
	{
		switch(value.toLowerCase())
		{
			case "highways":
				return Highways;
			case "tollways":
				return Tollways;
			case "ferries":
				return Ferries;
			case "steps":
				return Steps;
			case "fords":
				return Fords;
		}

		return 0;
	}

	public static int getProfileFlags(int profileCategory) {
		switch(profileCategory) {
			case RoutingProfileCategory.DRIVING:
				return DrivingFeatures;
			case RoutingProfileCategory.CYCLING:
				return CyclingFeatures;
			case RoutingProfileCategory.WALKING:
				return WalkingFeatures;
			case RoutingProfileCategory.WHEELCHAIR:
				return WheelchairFeatures;
			default:
				return RoutingProfileCategory.UNKNOWN;
		}
	}

	public static boolean isValid(int profileType, int value)
	{
		int profileCategory = RoutingProfileCategory.getFromRouteProfile(profileType);
		int nonProfileFlags = ~ getProfileFlags(profileCategory);

		return (nonProfileFlags & value) == 0;
	}
}
