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

import com.graphhopper.util.Helper;

public class RouteExtraInfoFlag {
    public static final int STEEPNESS = 1;
    public static final int SURFACE = 2;
    public static final int WAY_TYPE = 4;
    public static final int WAY_CATEGORY = 8;
    public static final int SUITABILITY = 16;
    public static final int GREEN = 32;
    public static final int NOISE = 64;
    public static final int AVG_SPEED = 128;
    public static final int TOLLWAYS = 256;
    public static final int TRAIL_DIFFICULTY = 512;
    public static final int OSM_ID = 1024;
    public static final int ROAD_ACCESS_RESTRICTIONS = 2048;
    public static final int COUNTRY_INFO = 4096;
    public static final int SHADOW = 8192;
    public static final int CSV = 16384;

    private RouteExtraInfoFlag() {}

    public static boolean isSet(int extraInfo, int value) {
        return (extraInfo & value) == value;
    }

    public static int getFromString(String value) {
        if (Helper.isEmpty(value))
            return 0;

        int res = 0;

        String[] values = value.split("\\|");
        for (String s : values) {
            switch (s.toLowerCase()) {
                case "steepness":
                    res |= STEEPNESS;
                    break;
                case "surface":
                    res |= SURFACE;
                    break;
                case "waytype":
                    res |= WAY_TYPE;
                    break;
                case "waycategory":
                    res |= WAY_CATEGORY;
                    break;
                case "suitability":
                    res |= SUITABILITY;
                    break;
                case "green":
                    res |= GREEN;
                    break;
                case "noise":
                    res |= NOISE;
                    break;
                case "avgspeed":
                    res |= AVG_SPEED;
                    break;
                case "tollways":
                    res |= TOLLWAYS;
                    break;
                case "traildifficulty":
                    res |= TRAIL_DIFFICULTY;
                    break;
                case "osmid":
                    res |= OSM_ID;
                    break;
                case "roadaccessrestrictions":
                    res |= ROAD_ACCESS_RESTRICTIONS;
                    break;
                case "countryinfo":
                    res |= COUNTRY_INFO;
                    break;
                case "csv":
                    res |= CSV;
                    break;
                case "shadow":
                    res |= SHADOW;
                    break;
                default:
            }
        }
        return res;
    }
}
