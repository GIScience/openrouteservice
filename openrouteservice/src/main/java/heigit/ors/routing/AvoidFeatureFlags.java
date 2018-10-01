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
	public static final int Steps = 2; // 1 << 1;
	public static final int Ferries = 4; // 1 << 2;
	public static final int UnpavedRoads = 8; // 1 << 3;
	public static final int Tracks = 16; // 1 << 4;
	public static final int Tunnels = 32; // 1 << 5;
	public static final int PavedRoads = 64; // 1 << 6;
	public static final int Fords = 128; // 1 << 7;
	
	public static final int Bridges = 256; // does not work as it is greater than byte limit of 255.
	public static final int Borders = 512; 
	public static final int Hills = 1024;

	public static final int DrivingFeatures = Highways | Tollways | Ferries | UnpavedRoads | Tracks | Tunnels | Fords | Bridges | Borders;
	public static final int CyclingFeatures = Steps | Ferries | UnpavedRoads | PavedRoads | Fords;
	public static final int WalkingFeatures =  Steps | Ferries | Fords;
	public static final int WheelchairFeatures = Ferries;

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
			case "unpavedroads":
				return UnpavedRoads;
			case "steps":
				return Steps;
			case "tracks":
				return Tracks;
			case "tunnels":
				return Tunnels;
			case "pavedroads":
				return PavedRoads;
			case "fords":
				return Fords;
			case "bridges":
				return Bridges;
			case "borders":
				return Borders;
			case "hills":
				return Hills;
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

	public static boolean isValid(int profileType, int value, String featName)
	{
		if (RoutingProfileType.isDriving(profileType))
		{
			if (value == Steps)
				return "steps".equalsIgnoreCase(featName) ? false : true;
		}
		else if (RoutingProfileType.isCycling(profileType) || RoutingProfileType.isWalking(profileType))
		{
			if (value == Highways || value == Tunnels)
				return false;
		}
		
		return true;
	}
}
