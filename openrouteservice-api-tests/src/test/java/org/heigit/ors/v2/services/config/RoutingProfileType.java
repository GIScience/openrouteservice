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
package heigit.ors.v2.services.config;


public class RoutingProfileType {
    public static final int UNKNOWN = 0;

    // DRIVING STUFF
    public static final int DRIVING_CAR = 1;
    public static final int DRIVING_HGV = 2;
    public static final int DRIVING_EMERGENCY = 3;
    public static final int DRIVING_ELECTRIC_CAR = 6;
    public static final int DRIVING_MOTORCYCLE = 7;
    public static final int DRIVING_TRAFFIC = 8;

    // CYCLING STUFF
    public static final int CYCLING_REGULAR = 10;
    public static final int CYCLING_MOUNTAIN = 11;
    public static final int CYCLING_ROAD = 12;
    public static final int CYCLING_ELECTRIC = 17;

    public static final int CYCLING_MOTOR = 15;

    // WALKING STUFF
    public static final int FOOT_WALKING = 20;
    public static final int FOOT_HIKING = 21;
    public static final int FOOT_JOGGING = 24;

    // OTHER STUFF
    public static final int WHEELCHAIR = 30;

    // GH default FlagEncoders...
    public static final int GH_CAR = 40;
    public static final int GH_CAR4WD = 41;

    public static final int GH_BIKE = 42;
    public static final int GH_BIKE2 = 43;
    public static final int GH_BIKE_MTB = 44;
    public static final int GH_BIKE_ROAD = 45;

    public static final int GH_FOOT = 46;
    public static final int GH_HIKE = 47;


    public static boolean isDriving(int routePref) {
        return routePref == DRIVING_CAR
                || routePref == DRIVING_HGV
                || routePref == DRIVING_ELECTRIC_CAR
                || routePref == DRIVING_EMERGENCY
                || routePref == DRIVING_MOTORCYCLE
                || routePref == DRIVING_TRAFFIC
                || routePref == GH_CAR
                || routePref == GH_CAR4WD;
    }

    public static boolean isWalking(int routePref) {
        return (routePref == FOOT_WALKING || routePref == FOOT_HIKING || routePref == FOOT_JOGGING || routePref == GH_FOOT || routePref == GH_HIKE);
    }

    public static boolean isWheelchair(int routePref) {
        return routePref == WHEELCHAIR;
    }

    public static boolean isCycling(int routePref) {
        return routePref == CYCLING_REGULAR
                || routePref == CYCLING_MOUNTAIN
                || routePref == CYCLING_ROAD
                || routePref == GH_BIKE
                || routePref == GH_BIKE2
                || routePref == GH_BIKE_MTB
                || routePref == GH_BIKE_ROAD
                || routePref == CYCLING_ELECTRIC;
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
            case DRIVING_TRAFFIC:
                return "driving-traffic";
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
            case "driving-traffic":
                return DRIVING_TRAFFIC;
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
}