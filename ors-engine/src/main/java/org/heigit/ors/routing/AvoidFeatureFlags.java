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

    //Keep in sync with documentation: waycategory.md

    public static final int HIGHWAYS = 1;
    public static final int TOLLWAYS = 2;
    public static final int STEPS = 4;
    public static final int FERRIES = 8;
    public static final int FORDS = 16;
    public static final int JUNCTION = 32;

    private static final int DRIVING_FEATURES = HIGHWAYS | TOLLWAYS | FERRIES | FORDS;
    private static final int CYCLING_FEATURES = STEPS | FERRIES | FORDS | JUNCTION;
    private static final int WALKING_FEATURES = STEPS | FERRIES | FORDS;
    private static final int WHEELCHAIR_FEATURES = WALKING_FEATURES;

    private AvoidFeatureFlags() {
    }

    public static int getFromString(String value) {
        return switch (value.toLowerCase()) {
            case "highways" -> HIGHWAYS;
            case "tollways" -> TOLLWAYS;
            case "ferries" -> FERRIES;
            case "steps" -> STEPS;
            case "fords" -> FORDS;
            case "junction" -> JUNCTION;
            default -> 0;
        };
    }

    public static int getProfileFlags(int profileCategory) {
        return switch (profileCategory) {
            case RoutingProfileCategory.DRIVING -> DRIVING_FEATURES;
            case RoutingProfileCategory.CYCLING -> CYCLING_FEATURES;
            case RoutingProfileCategory.WALKING -> WALKING_FEATURES;
            case RoutingProfileCategory.WHEELCHAIR -> WHEELCHAIR_FEATURES;
            default -> RoutingProfileCategory.UNKNOWN;
        };
    }

    public static boolean isValid(int profileType, int value) {
        int profileCategory = RoutingProfileCategory.getFromRouteProfile(profileType);
        int nonProfileFlags = ~getProfileFlags(profileCategory);
        return (nonProfileFlags & value) == 0;
    }
}
