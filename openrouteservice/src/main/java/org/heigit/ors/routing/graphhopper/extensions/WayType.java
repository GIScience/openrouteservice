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
		switch(highway.toLowerCase()) {
			case "primary":
			case "primary_link":
			case "motorway":
			case "motorway_link":
			case "trunk":
			case "trunk_link":
				return WayType.STATE_ROAD;
			case "secondary":
			case "secondary_link":
			case "tertiary":
			case "tertiary_link":
			case "road":
			case "unclassified":
				return WayType.ROAD;
			case "residential":
			case "service":
			case "living_street":
				return WayType.STREET;
			case "path":
				return WayType.PATH;
			case "track":
				return WayType.TRACK;
			case "cycleway":
				return WayType.CYCLEWAY;
			case "footway":
			case "pedestrian":
			case "crossing":
				return WayType.FOOTWAY;
			case "steps":
				return WayType.STEPS;
			case "construction":
				return WayType.CONSTRUCTION;
			default:
				return WayType.UNKNOWN;
		}
	}

	@Override
	public int getOrdinal() {
		return this.ordinal();
	}
}
