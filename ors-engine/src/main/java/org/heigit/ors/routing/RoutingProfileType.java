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

import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;

public class RoutingProfileType {
    public static final int UNKNOWN = 0;

    // DRIVING STUFF
    public static final int DRIVING_CAR = 1;
    public static final int DRIVING_HGV = 2;
    public static final int DRIVING_EMERGENCY = 3; // not supported
    public static final int DRIVING_CAROFFROAD = 4; // not supported
    public static final int DRIVING_SEGWAY = 5; // not implemented
    public static final int DRIVING_ELECTRIC_CAR = 6;
    public static final int DRIVING_MOTORCYCLE = 7;
    public static final int DRIVING_TRAFFIC = 8;

    // CYCLING STUFF
    public static final int CYCLING_REGULAR = 10;
    public static final int CYCLING_MOUNTAIN = 11;
    public static final int CYCLING_ROAD = 12;
    public static final int CYCLING_ELECTRIC = 17;
    public static final int CYCLING_CARGO = 18;

    // WALKING STUFF
    public static final int FOOT_WALKING = 20;
    public static final int FOOT_HIKING = 21;
    public static final int FOOT_JOGGING = 24;

    // OTHER STUFF
    public static final int WHEELCHAIR = 30;

    public static final int PUBLIC_TRANSPORT = 31;

    // GH default FlagEncoders...
    public static final int GH_CAR = 40;
    public static final int GH_CAR4WD = 41;

    public static final int GH_BIKE = 42;
    public static final int GH_BIKE2 = 43;
    public static final int GH_BIKE_MTB = 44;
    public static final int GH_BIKE_ROAD = 45;

    public static final int GH_FOOT = 46;
    public static final int GH_HIKE = 47;

    private RoutingProfileType() {
    }

    public static boolean isDriving(int routePref) {
        return routePref == DRIVING_CAR
                || routePref == DRIVING_HGV
                || routePref == DRIVING_ELECTRIC_CAR
                || routePref == DRIVING_EMERGENCY
                || routePref == DRIVING_MOTORCYCLE
                || routePref == DRIVING_CAROFFROAD
                || routePref == DRIVING_TRAFFIC
                || routePref == GH_CAR
                || routePref == GH_CAR4WD;
    }

    public static boolean isHeavyVehicle(int routePref) {
        return routePref == DRIVING_HGV
                || routePref == DRIVING_CAROFFROAD
                || routePref == DRIVING_EMERGENCY;
    }

    public static boolean isWalking(int routePref) {
        return routePref == FOOT_WALKING
                || routePref == FOOT_HIKING
                || routePref == FOOT_JOGGING
                || routePref == GH_FOOT
                || routePref == GH_HIKE;
    }

    public static boolean isPedestrian(int routePref) {
        return isWalking(routePref) || routePref == WHEELCHAIR;
    }

    public static boolean isWheelchair(int routePref) {
        return routePref == WHEELCHAIR;
    }

    public static boolean isCycling(int routePref) {
        return routePref == CYCLING_REGULAR
                || routePref == CYCLING_MOUNTAIN
                || routePref == CYCLING_ROAD
                || routePref == CYCLING_ELECTRIC
                || routePref == CYCLING_CARGO
                || routePref == GH_BIKE
                || routePref == GH_BIKE2
                || routePref == GH_BIKE_MTB
                || routePref == GH_BIKE_ROAD;
    }

    public static boolean supportMessages(int profileType) {
        return isDriving(profileType);
    }

    public static String getName(int profileType) {
        return switch (profileType) {
            case DRIVING_CAR -> "driving-car";
            case DRIVING_ELECTRIC_CAR -> "driving-ecar";
            case DRIVING_HGV -> "driving-hgv";
            case DRIVING_MOTORCYCLE -> "driving-motorcycle";
            case DRIVING_EMERGENCY -> "driving-emergency";
            case CYCLING_REGULAR -> "cycling-regular";
            case CYCLING_MOUNTAIN -> "cycling-mountain";
            case CYCLING_ROAD -> "cycling-road";
            case CYCLING_ELECTRIC -> "cycling-electric";
            case CYCLING_CARGO -> "cycling-cargo"; 
            case FOOT_WALKING -> "foot-walking";
            case FOOT_HIKING -> "foot-hiking";
            case FOOT_JOGGING -> "foot-jogging";
            case WHEELCHAIR -> "wheelchair";

            // GH DEFAULTS:
            case GH_CAR -> "gh-car";
            case GH_CAR4WD -> "gh-car4wd";
            case GH_BIKE -> "gh-bike";
            case GH_BIKE2 -> "gh-bike2";
            case GH_BIKE_MTB -> "gh-mtb";
            case GH_BIKE_ROAD -> "gh-racingbike";
            case GH_FOOT -> "gh-foot";
            case GH_HIKE -> "gh-hike";
            default -> "unknown";
        };
    }

    public static int getFromString(String profileType) {
        return switch (profileType.toLowerCase()) {
            case "driving-car" -> DRIVING_CAR;
            case "driving-ecar" -> DRIVING_ELECTRIC_CAR;
            case "driving-hgv" -> DRIVING_HGV;
            case "driving-motorcycle" -> DRIVING_MOTORCYCLE;
            case "driving-emergency" -> DRIVING_EMERGENCY;
            case "cycling-regular" -> CYCLING_REGULAR;
            case "cycling-mountain" -> CYCLING_MOUNTAIN;
            case "cycling-road" -> CYCLING_ROAD;
            case "cycling-electric" -> CYCLING_ELECTRIC;
            case "cycling-cargo" -> CYCLING_CARGO;
            case "foot-walking" -> FOOT_WALKING;
            case "foot-hiking" -> FOOT_HIKING;
            case "foot-jogging" -> FOOT_JOGGING;
            case "wheelchair" -> WHEELCHAIR;
            case "public-transport" -> PUBLIC_TRANSPORT;

            // GH DEFAULTS:
            case "gh-car" -> GH_CAR;
            case "gh-car4wd" -> GH_CAR4WD;
            case "gh-bike" -> GH_BIKE;
            case "gh-bike2" -> GH_BIKE2;
            case "gh-mtb" -> GH_BIKE_MTB;
            case "gh-racingbike" -> GH_BIKE_ROAD;
            case "gh-foot" -> GH_FOOT;
            case "gh-hike" -> GH_HIKE;
            default -> UNKNOWN;
        };
    }

    public static String getEncoderName(int routePref) {
        return switch (routePref) {
            case RoutingProfileType.DRIVING_CAR -> FlagEncoderNames.CAR_ORS;
            case RoutingProfileType.DRIVING_HGV -> FlagEncoderNames.HEAVYVEHICLE;
            case RoutingProfileType.DRIVING_EMERGENCY -> FlagEncoderNames.EMERGENCY;
            case RoutingProfileType.DRIVING_MOTORCYCLE -> FlagEncoderNames.GH_MOTOCYCLE;
            case RoutingProfileType.DRIVING_ELECTRIC_CAR -> FlagEncoderNames.EVEHICLE;
            case RoutingProfileType.FOOT_JOGGING -> FlagEncoderNames.RUNNING;
            case RoutingProfileType.CYCLING_REGULAR -> FlagEncoderNames.BIKE_ORS;
            case RoutingProfileType.CYCLING_MOUNTAIN -> FlagEncoderNames.MTB_ORS;
            case RoutingProfileType.CYCLING_ROAD -> FlagEncoderNames.ROADBIKE_ORS;
            case RoutingProfileType.FOOT_WALKING -> FlagEncoderNames.PEDESTRIAN_ORS;
            case RoutingProfileType.FOOT_HIKING -> FlagEncoderNames.HIKING_ORS;
            case RoutingProfileType.WHEELCHAIR -> FlagEncoderNames.WHEELCHAIR;
            case RoutingProfileType.PUBLIC_TRANSPORT -> FlagEncoderNames.GH_FOOT;
            case RoutingProfileType.GH_CAR -> FlagEncoderNames.GH_CAR;
            case RoutingProfileType.GH_CAR4WD -> FlagEncoderNames.GH_CAR4WD;
            case RoutingProfileType.GH_BIKE -> FlagEncoderNames.GH_BIKE;
            case RoutingProfileType.GH_BIKE2 -> FlagEncoderNames.GH_BIKE2;
            case RoutingProfileType.GH_BIKE_MTB -> FlagEncoderNames.GH_MTB;
            case RoutingProfileType.GH_BIKE_ROAD -> FlagEncoderNames.GH_RACINGBIKE;
            case RoutingProfileType.GH_FOOT -> FlagEncoderNames.GH_FOOT;
            case RoutingProfileType.GH_HIKE -> FlagEncoderNames.GH_HIKE;
            case RoutingProfileType.CYCLING_ELECTRIC -> FlagEncoderNames.BIKE_ELECTRO;
            case RoutingProfileType.CYCLING_CARGO-> FlagEncoderNames.BIKE_CARGO; 
            default -> FlagEncoderNames.UNKNOWN;
        };
    }

    // these are the names of the toString Method of each FlagEncoder implementation!
    public static int getFromEncoderName(String encoder) {
        return switch (encoder.toLowerCase()) {
            case FlagEncoderNames.CAR_ORS -> RoutingProfileType.DRIVING_CAR;

            /* a ors self implemented flagencoder */
            case FlagEncoderNames.HEAVYVEHICLE -> RoutingProfileType.DRIVING_HGV;

            /* not in use */
            case FlagEncoderNames.EVEHICLE -> RoutingProfileType.DRIVING_ELECTRIC_CAR;

            /* currently not in use */
            case FlagEncoderNames.GH_MOTOCYCLE -> RoutingProfileType.DRIVING_MOTORCYCLE;
            case FlagEncoderNames.BIKE_ORS -> RoutingProfileType.CYCLING_REGULAR;
            case FlagEncoderNames.MTB_ORS ->
                //case FlagEncoderNames.MTB_ORS_OLD:
                    RoutingProfileType.CYCLING_MOUNTAIN;
            case FlagEncoderNames.ROADBIKE_ORS -> RoutingProfileType.CYCLING_ROAD;


            /* not in use */
            case FlagEncoderNames.RUNNING -> RoutingProfileType.FOOT_JOGGING;
            case FlagEncoderNames.WHEELCHAIR -> RoutingProfileType.WHEELCHAIR;
            case FlagEncoderNames.GH_CAR -> RoutingProfileType.GH_CAR;
            case FlagEncoderNames.GH_CAR4WD -> RoutingProfileType.GH_CAR4WD;
            case FlagEncoderNames.GH_BIKE -> RoutingProfileType.GH_BIKE;
            case FlagEncoderNames.GH_BIKE2 -> RoutingProfileType.GH_BIKE2;
            case FlagEncoderNames.GH_MTB -> RoutingProfileType.GH_BIKE_MTB;
            case FlagEncoderNames.GH_RACINGBIKE -> RoutingProfileType.GH_BIKE_ROAD;
            case FlagEncoderNames.GH_FOOT -> RoutingProfileType.FOOT_WALKING;
            case FlagEncoderNames.GH_HIKE -> RoutingProfileType.FOOT_HIKING;
            case FlagEncoderNames.BIKE_ELECTRO -> RoutingProfileType.CYCLING_ELECTRIC;
            case FlagEncoderNames.BIKE_CARGO -> RoutingProfileType.CYCLING_CARGO;
            default -> RoutingProfileType.UNKNOWN;
        };
    }
}
