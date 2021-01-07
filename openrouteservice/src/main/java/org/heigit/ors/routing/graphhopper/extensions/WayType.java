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

public enum WayType implements PropertyType {
	UNKNOWN,
	STATE_ROAD,
	ROAD,
	STREET,
	PATH,
	TRACK,
	CYCLEWAY,
	FOOTWAY,
	STEPS,
	FERRY,
	CONSTRUCTION;

	public static WayType getFromString(String highway) {
		if ("primary".equalsIgnoreCase(highway)
				|| "primary_link".equalsIgnoreCase(highway)
				|| "motorway".equalsIgnoreCase(highway)
				|| "motorway_link".equalsIgnoreCase(highway)
				|| "trunk".equalsIgnoreCase(highway)
				|| "trunk_link".equalsIgnoreCase(highway)) {
			return WayType.STATE_ROAD;
		} else if ("secondary".equalsIgnoreCase(highway)
				|| "secondary_link".equalsIgnoreCase(highway)
				|| "tertiary".equalsIgnoreCase(highway)
				|| "tertiary_link".equalsIgnoreCase(highway)
				|| "road".equalsIgnoreCase(highway)
				|| "unclassified".equalsIgnoreCase(highway)) {
			return WayType.ROAD;
		} else if ("residential".equalsIgnoreCase(highway)
				|| "service".equalsIgnoreCase(highway)
				|| "living_street".equalsIgnoreCase(highway)) {
			return WayType.STREET;
		} else if ("path".equalsIgnoreCase(highway)) {
			return WayType.PATH;
		} else if ("track".equalsIgnoreCase(highway)) {
			return WayType.TRACK;
		} else if ("cycleway".equalsIgnoreCase(highway)) {
			return WayType.CYCLEWAY;
		} else if ("footway".equalsIgnoreCase(highway)
				|| "pedestrian".equalsIgnoreCase(highway)
				|| "crossing".equalsIgnoreCase(highway)) {
			return WayType.FOOTWAY;
		} else if ("steps".equalsIgnoreCase(highway)) {
			return WayType.STEPS;
		} else if ("construction".equalsIgnoreCase(highway)) {
			return WayType.CONSTRUCTION;
		}
		return WayType.UNKNOWN;
	}

	@Override
	public int getOrdinal() {
		return this.ordinal();
	}
}
