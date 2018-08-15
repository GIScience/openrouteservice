/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
