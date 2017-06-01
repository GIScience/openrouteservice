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
package heigit.ors.services.accessibility;

import heigit.ors.common.TravelRangeType;
import heigit.ors.config.AppConfig;

public class AccessibilityServiceSettings 
{
	private static int maximumLocations = 1;
	private static int maximumRangeDistance = 100000; //  in meters
	private static int maximumRangeTime = 3600; // in seconds
	private static boolean routeDetailsAllowed = false;
	private static int responseLimit = 50; 
	private static String attribution = "";
	private static boolean enabled = true;
	
	static 
	{
		String value = AppConfig.Global().getServiceParameter("accessibility", "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);		
		value = AppConfig.Global().getServiceParameter("accessibility", "maximum_locations");
		if (value != null)
			maximumLocations = Math.max(1, Integer.parseInt(value));
		value = AppConfig.Global().getServiceParameter("accessibility", "maximum_range_distance");
		if (value != null)
			maximumRangeDistance = Integer.parseInt(value);
		value = AppConfig.Global().getServiceParameter("accessibility", "maximum_range_time");
		if (value != null)
			maximumRangeTime = Integer.parseInt(value);
		value = AppConfig.Global().getServiceParameter("accessibility", "route_details_allowed");
		if (value != null)
			routeDetailsAllowed = Boolean.parseBoolean(value);
		value = AppConfig.Global().getServiceParameter("accessibility", "response_limit");
		if (value != null)
			responseLimit = Integer.parseInt(value);
		value = AppConfig.Global().getServiceParameter("accessibility", "attribution");
		if (value != null)
			attribution = value;
	}
	
	public static Boolean getEnabled() {
		return enabled;
	}
	
	public static int getResponseLimit() {
		return responseLimit;
	}
	
	public static int getMaximumLocations() {
		return maximumLocations;
	}
	
	public static int getMaximumRange(TravelRangeType range) {
		switch(range)
		{
		case Distance:
			return maximumRangeDistance;
		case Time:
			return maximumRangeTime;
		}

		return 0;
	}
	
	public static boolean getRouteDetailsAllowed()
	{
		return routeDetailsAllowed;
	}
	
	public static String getAttribution() {
		return attribution;
	}
}
