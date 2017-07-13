/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.services.isochrones;

import heigit.ors.config.AppConfig;
import heigit.ors.routing.RoutingProfileType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.typesafe.config.ConfigObject;

import heigit.ors.common.TravelRangeType;

public class IsochronesServiceSettings {
	private static boolean enabled = true;
	private static int maximumLocations = 1;
	private static int maximumRangeDistance = 100000; //  in meters
	private static Map<Integer, Integer> profileMaxRangeDistances;
	private static int maximumRangeTime = 3600; // in seconds
	private static Map<Integer, Integer> profileMaxRangeTimes;
	private static int maximumIntervals = 1;
	private static boolean allowComputeArea = true;
	private static String attribution = "";

	static 
	{
		String value = AppConfig.Global().getServiceParameter("isochrones", "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);
		value = AppConfig.Global().getServiceParameter("isochrones", "maximum_locations");
		if (value != null)
			maximumLocations = Integer.parseInt(value);
		value = AppConfig.Global().getServiceParameter("isochrones", "maximum_range_distance");
		if (value != null)
			maximumRangeDistance = Integer.parseInt(value);
		else
		{
			List<? extends ConfigObject> params = AppConfig.Global().getObjectList("isochrones", "maximum_range_distance");
		    if (params != null)
		    {
		    	profileMaxRangeDistances = getParameters(params);
		    	if (profileMaxRangeDistances.containsKey(-1))
		    		maximumRangeDistance = profileMaxRangeDistances.get(-1);
		    }
		}
				
		value = AppConfig.Global().getServiceParameter("isochrones", "maximum_range_time");
		if (value != null)
			maximumRangeTime = Integer.parseInt(value);
		else
		{
			List<? extends ConfigObject> params = AppConfig.Global().getObjectList("isochrones", "maximum_range_time");
		    if (params != null)
		    {
		    	profileMaxRangeTimes = getParameters(params);
		    	if (profileMaxRangeTimes.containsKey(-1))
		    		maximumRangeTime = profileMaxRangeTimes.get(-1);
		    }
		}
		
		value = AppConfig.Global().getServiceParameter("isochrones", "maximum_intervals");
		if (value != null)
			maximumIntervals = Integer.parseInt(value);
		value = AppConfig.Global().getServiceParameter("isochrones", "allow_compute_area");
		if (value != null)
			allowComputeArea = Boolean.parseBoolean(value);
		value = AppConfig.Global().getServiceParameter("isochrones", "attribution");
		if (value != null)
			attribution = value;
	}
	
	private static Map<Integer, Integer> getParameters(List<? extends ConfigObject> params)
	{
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		
		for(ConfigObject cfgObj : params)
    	{
    		if (cfgObj.containsKey("profiles") && cfgObj.containsKey("value"))
    		{
    		   String[] profiles = cfgObj.toConfig().getString("profiles").split(",");
    		   for (String profileStr : profiles)
    		   {
    			   profileStr = profileStr.trim();
    			   Integer profile = ("any".equalsIgnoreCase(profileStr)) ? -1 : RoutingProfileType.getFromString(profileStr);
    			   if (profile != RoutingProfileType.UNKNOWN)
    				   result.put(profile, cfgObj.toConfig().getInt("value"));
    		   }
    		}
    	}
		
		return result;
	}
	
	public static boolean getEnabled() {
		return enabled;
	}

	public static boolean getAllowComputeArea() {
		return allowComputeArea;
	}

	public static int getMaximumLocations() {
		return maximumLocations;
	}

	public static int getMaximumRange(int profileType, TravelRangeType range) {
		Integer res = 0;
		
		switch(range)
		{
		case Distance:
			res = profileMaxRangeDistances.get(profileType);
			if (res == null)
				res = maximumRangeDistance;
			break;
		case Time:
			res = profileMaxRangeTimes.get(profileType);
			if (res == null)
				res = maximumRangeTime;
			 break;
		}

		return res;
	}

	public static int getMaximumIntervals()	{
		return maximumIntervals;
	}

	public static String getAttribution() {
		return attribution;
	}	
}
