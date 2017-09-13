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
package heigit.ors.matrix;

import com.graphhopper.util.Helper;

public class MatrixMetricsType {
	public static final int Unknown = 0;
	public static final int Duration = 1;
	public static final int Distance = 2;
	public static final int Weight = 4;
	
    public static boolean isSet(int metrics, int value) {
        return (metrics & value) == value;
    }
    
    public int getMetricsCount(int metrics)
    {
    	int res = 0;
    	
    	if (isSet(metrics, Duration))
    		res++;

    	if (isSet(metrics, Distance))
    		res++;

    	if (isSet(metrics, Weight))
    		res++;

    	return res;
    }

	public static int getFromString(String value)
	{
        if (Helper.isEmpty(value))
            return 0;
        
        String[] values = value.toLowerCase().split("\\|");
        int res = Unknown;
        
        for(String str : values)
        {
        	switch(str)
        	{
        	case "duration":
        		res |= Duration;
        		break;
        	case "distance":
        		res |= Distance;
        		break;
        	case "weight":
        		res |= Weight;
        		break;
       		default:
        		return Unknown;
        	}
        }
        
		return res;
	}
}
