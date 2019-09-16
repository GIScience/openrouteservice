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
package org.heigit.ors.routing;

public class AvoidFeatureFlags {
	public static final int HIGHWAYS = 1;
	public static final int TOLLWAYS = 2;
	public static final int STEPS = 4;
	public static final int FERRIES = 8;
	public static final int FORDS = 16;

	private static final int DRIVING_FEATURES = HIGHWAYS | TOLLWAYS | FERRIES | FORDS;
	private static final int CYCLING_FEATURES = STEPS | FERRIES | FORDS;
	private static final int WALKING_FEATURES =  STEPS | FERRIES | FORDS;
	private static final int WHEELCHAIR_FEATURES = WALKING_FEATURES;

	private AvoidFeatureFlags() {}

	public static int getFromString(String value) {
		switch(value.toLowerCase()) {
			case "highways":
				return HIGHWAYS;
			case "tollways":
				return TOLLWAYS;
			case "ferries":
				return FERRIES;
			case "steps":
				return STEPS;
			case "fords":
				return FORDS;
			default:
				return 0;
		}
	}

	public static int getProfileFlags(int profileCategory) {
		switch(profileCategory) {
			case RoutingProfileCategory.DRIVING:
				return DRIVING_FEATURES;
			case RoutingProfileCategory.CYCLING:
				return CYCLING_FEATURES;
			case RoutingProfileCategory.WALKING:
				return WALKING_FEATURES;
			case RoutingProfileCategory.WHEELCHAIR:
				return WHEELCHAIR_FEATURES;
			default:
				return RoutingProfileCategory.UNKNOWN;
		}
	}

	public static boolean isValid(int profileType, int value) {
		int profileCategory = RoutingProfileCategory.getFromRouteProfile(profileType);
		int nonProfileFlags = ~ getProfileFlags(profileCategory);
		return (nonProfileFlags & value) == 0;
	}
}
