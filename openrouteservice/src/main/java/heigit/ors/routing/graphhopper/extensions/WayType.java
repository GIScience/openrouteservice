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
package heigit.ors.routing.graphhopper.extensions;

public class WayType {
	public static final int Unknown = 0;
	public static final int StateRoad = 1;
	public static final int Road = 2;
	public static final int Street = 3;
	public static final int Path = 4;
	public static final int Track = 5;
	public static final int Cycleway = 6;
	public static final int Footway = 7;
	public static final int Steps = 8;
	public static final int Ferry = 9;
	public static final int Construction = 10;
	
	public static int getFromString(String highway) {
		if ("primary".equalsIgnoreCase(highway) || "primary_link".equalsIgnoreCase(highway)
				|| "motorway".equalsIgnoreCase(highway) || "motorway_link".equalsIgnoreCase(highway)
				|| "trunk".equalsIgnoreCase(highway) || "trunk_link".equalsIgnoreCase(highway)) {
			return WayType.StateRoad;
		} else if ("secondary".equalsIgnoreCase(highway) || "secondary_link".equalsIgnoreCase(highway)
				|| "tertiary".equalsIgnoreCase(highway) || "tertiary_link".equalsIgnoreCase(highway)
				|| "road".equalsIgnoreCase(highway) || "unclassified".equalsIgnoreCase(highway)) {
			return WayType.Road;
		} else if ("residential".equalsIgnoreCase(highway) || "service".equalsIgnoreCase(highway)
				|| "living_street".equalsIgnoreCase(highway) || "living_street".equalsIgnoreCase(highway)) {
			return WayType.Street;
		} else if ("path".equalsIgnoreCase(highway)) {
			return WayType.Path;
		} else if ("track".equalsIgnoreCase(highway)) {
			return WayType.Track;
		} else if ("cycleway".equalsIgnoreCase(highway)) {
			return WayType.Cycleway;
		} else if ("footway".equalsIgnoreCase(highway) || "pedestrian".equalsIgnoreCase(highway)
				|| "crossing".equalsIgnoreCase(highway)) {
			return WayType.Footway;
		} else if ("steps".equalsIgnoreCase(highway)) {
			return WayType.Steps;
		} else if ("construction".equalsIgnoreCase(highway)) {
			return WayType.Construction;
		}

		return WayType.Unknown;
	}
}
