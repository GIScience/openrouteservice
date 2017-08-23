/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
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
            }
        }

        return res;
    }
}
