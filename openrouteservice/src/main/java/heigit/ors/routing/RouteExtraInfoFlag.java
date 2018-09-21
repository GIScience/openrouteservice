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

import com.graphhopper.util.Helper;

public class RouteExtraInfoFlag {
    public static final int Steepness = 1;
    public static final int Surface = 2;
    public static final int WayType = 4;
    public static final int WayCategory = 8;
    public static final int Suitability = 16;
    public static final int Green = 32;
    public static final int Noise = 64;
    public static final int AvgSpeed = 128;
    public static final int Tollways = 256;
    public static final int TrailDifficulty = 512;
    public static final int OsmId = 1024;

    public static boolean isSet(int extraInfo, int value) {
        return (extraInfo & value) == value;
    }

    public static int getFromString(String value) {
        if (Helper.isEmpty(value))
            return 0;

        int res = 0;

        String[] values = value.split("\\|");
        for (int i = 0; i < values.length; ++i) {
            switch (values[i].toLowerCase()) {
                case "steepness":
                    res |= Steepness;
                    break;
                case "surface":
                    res |= Surface;
                    break;
                case "waytype":
                    res |= WayType;
                    break;
                case "waycategory":
                    res |= WayCategory;
                    break;
                case "suitability":
                    res |= Suitability;
                    break;
                case "green":
                    res |= Green;
                    break;
                case "noise":
                    res |= Noise;
                    break;
                case "avgspeed":
                    res |= AvgSpeed;
                    break;
                case "tollways":
                    res |= Tollways;
                    break;
                case "traildifficulty":
                	res |= TrailDifficulty;
                	break;
                case "osmid":
                    res |= OsmId;
                    break;
            }
        }

        return res;
    }
}
