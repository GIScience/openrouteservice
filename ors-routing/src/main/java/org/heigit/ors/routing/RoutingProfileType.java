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

    private RoutingProfileType() {}

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
            || routePref == GH_BIKE
            || routePref == GH_BIKE2
            || routePref == GH_BIKE_MTB
            || routePref == GH_BIKE_ROAD;
    }

    public static boolean supportMessages(int profileType) {
        return isDriving(profileType);
    }

    public static String getName(int profileType) {
        switch (profileType) {
            case DRIVING_CAR:
                return "driving-car";
            case DRIVING_ELECTRIC_CAR:
                return "driving-ecar";
            case DRIVING_HGV:
                return "driving-hgv";
            case DRIVING_MOTORCYCLE:
                return "driving-motorcycle";
            case DRIVING_EMERGENCY:
                return "driving-emergency";

            case CYCLING_REGULAR:
                return "cycling-regular";
            case CYCLING_MOUNTAIN:
                return "cycling-mountain";
            case CYCLING_ROAD:
                return "cycling-road";
            case CYCLING_ELECTRIC:
                return "cycling-electric";

            case FOOT_WALKING:
                return "foot-walking";
            case FOOT_HIKING:
                return "foot-hiking";
            case FOOT_JOGGING:
                return "foot-jogging";

            case WHEELCHAIR:
                return "wheelchair";

            // GH DEFAULTS:
            case GH_CAR:
                return "gh-car";
            case GH_CAR4WD:
                return "gh-car4wd";
            case GH_BIKE:
                return "gh-bike";
            case GH_BIKE2:
                return "gh-bike2";
            case GH_BIKE_MTB:
                return "gh-mtb";
            case GH_BIKE_ROAD:
                return "gh-racingbike";
            case GH_FOOT:
                return "gh-foot";
            case GH_HIKE:
                return "gh-hike";

            default:
                return "unknown";
        }
    }

    public static int getFromString(String profileType) {
        switch (profileType.toLowerCase()) {
            case "driving-car":
                return DRIVING_CAR;
            case "driving-ecar":
                return DRIVING_ELECTRIC_CAR;
            case "driving-hgv":
                return DRIVING_HGV;
            case "driving-motorcycle":
                return DRIVING_MOTORCYCLE;
            case "driving-emergency":
                return DRIVING_EMERGENCY;

            case "cycling-regular":
                return CYCLING_REGULAR;
            case "cycling-mountain":
                return CYCLING_MOUNTAIN;
            case "cycling-road":
                return CYCLING_ROAD;
            case "cycling-electric":
                return CYCLING_ELECTRIC;

            case "foot-walking":
                return FOOT_WALKING;
            case "foot-hiking":
                return FOOT_HIKING;
            case "foot-jogging":
                return FOOT_JOGGING;

            case "wheelchair":
                return WHEELCHAIR;

            case "public-transport":
                return PUBLIC_TRANSPORT;

            // GH DEFAULTS:
            case "gh-car":
                return GH_CAR;
            case "gh-car4wd":
                return GH_CAR4WD;
            case "gh-bike":
                return GH_BIKE;
            case "gh-bike2":
                return GH_BIKE2;
            case "gh-mtb":
                return GH_BIKE_MTB;
            case "gh-racingbike":
                return GH_BIKE_ROAD;
            case "gh-foot":
                return GH_FOOT;
            case "gh-hike":
                return GH_HIKE;

            default:
                return UNKNOWN;
        }
    }

    public static String getEncoderName(int routePref) {
        switch (routePref) {
            case RoutingProfileType.DRIVING_CAR:
                return FlagEncoderNames.CAR_ORS;

            case RoutingProfileType.DRIVING_HGV:
                return FlagEncoderNames.HEAVYVEHICLE;

            case RoutingProfileType.DRIVING_EMERGENCY:
                return FlagEncoderNames.EMERGENCY;

            case RoutingProfileType.DRIVING_MOTORCYCLE:
                return FlagEncoderNames.GH_MOTOCYCLE;

            case RoutingProfileType.DRIVING_ELECTRIC_CAR:
                return FlagEncoderNames.EVEHICLE;

            case RoutingProfileType.FOOT_JOGGING:
                return FlagEncoderNames.RUNNING;

            case RoutingProfileType.CYCLING_REGULAR:
                return FlagEncoderNames.BIKE_ORS;

            case RoutingProfileType.CYCLING_MOUNTAIN:
                return FlagEncoderNames.MTB_ORS;

            case RoutingProfileType.CYCLING_ROAD:
                return FlagEncoderNames.ROADBIKE_ORS;


            case RoutingProfileType.FOOT_WALKING:
                return FlagEncoderNames.PEDESTRIAN_ORS;

            case RoutingProfileType.FOOT_HIKING:
                return FlagEncoderNames.HIKING_ORS;


            case RoutingProfileType.WHEELCHAIR:
                return FlagEncoderNames.WHEELCHAIR;

            case RoutingProfileType.PUBLIC_TRANSPORT:
                return FlagEncoderNames.GH_FOOT;

            case RoutingProfileType.GH_CAR:
                return FlagEncoderNames.GH_CAR;

            case RoutingProfileType.GH_CAR4WD:
                return FlagEncoderNames.GH_CAR4WD;

            case RoutingProfileType.GH_BIKE:
                return FlagEncoderNames.GH_BIKE;

            case RoutingProfileType.GH_BIKE2:
                return FlagEncoderNames.GH_BIKE2;

            case RoutingProfileType.GH_BIKE_MTB:
                return FlagEncoderNames.GH_MTB;

            case RoutingProfileType.GH_BIKE_ROAD:
                return FlagEncoderNames.GH_RACINGBIKE;

            case RoutingProfileType.GH_FOOT:
                return FlagEncoderNames.GH_FOOT;

            case RoutingProfileType.GH_HIKE:
                return FlagEncoderNames.GH_HIKE;

            case RoutingProfileType.CYCLING_ELECTRIC:
                return FlagEncoderNames.BIKE_ELECTRO;

            default:
                return FlagEncoderNames.UNKNOWN;
        }
    }

    // these are the names of the toString Method of each FlagEncoder implementation!
    public static int getFromEncoderName(String encoder) {
        switch (encoder.toLowerCase()) {
            case FlagEncoderNames.CAR_ORS:
                return RoutingProfileType.DRIVING_CAR;

            /* a ors self implemented flagencoder */
            case FlagEncoderNames.HEAVYVEHICLE:
                return RoutingProfileType.DRIVING_HGV;

            /* not in use */
            case FlagEncoderNames.EVEHICLE:
                return RoutingProfileType.DRIVING_ELECTRIC_CAR;

            /* currently not in use */
            case FlagEncoderNames.GH_MOTOCYCLE:
                return RoutingProfileType.DRIVING_MOTORCYCLE;

            case FlagEncoderNames.BIKE_ORS:
                return RoutingProfileType.CYCLING_REGULAR;

            case FlagEncoderNames.MTB_ORS:
            //case FlagEncoderNames.MTB_ORS_OLD:
                return RoutingProfileType.CYCLING_MOUNTAIN;

            case FlagEncoderNames.ROADBIKE_ORS:
                return RoutingProfileType.CYCLING_ROAD;


            /* not in use */
            case FlagEncoderNames.RUNNING:
                return RoutingProfileType.FOOT_JOGGING;


            case FlagEncoderNames.WHEELCHAIR:
                return RoutingProfileType.WHEELCHAIR;


            case FlagEncoderNames.GH_CAR:
                return RoutingProfileType.GH_CAR;

            case FlagEncoderNames.GH_CAR4WD:
                return RoutingProfileType.GH_CAR4WD;

            case FlagEncoderNames.GH_BIKE:
                return RoutingProfileType.GH_BIKE;

            case FlagEncoderNames.GH_BIKE2:
                return RoutingProfileType.GH_BIKE2;

            case FlagEncoderNames.GH_MTB:
                return RoutingProfileType.GH_BIKE_MTB;

            case FlagEncoderNames.GH_RACINGBIKE:
                return RoutingProfileType.GH_BIKE_ROAD;

            case FlagEncoderNames.GH_FOOT:
                return RoutingProfileType.FOOT_WALKING;

            case FlagEncoderNames.GH_HIKE:
                return RoutingProfileType.FOOT_HIKING;

            case FlagEncoderNames.BIKE_ELECTRO:
                return RoutingProfileType.CYCLING_ELECTRIC;

            default:
                return RoutingProfileType.UNKNOWN;
        }
    }
}