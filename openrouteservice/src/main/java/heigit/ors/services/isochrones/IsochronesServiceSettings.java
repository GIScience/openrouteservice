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
import heigit.ors.isochrones.IsochronesRangeType;

public class IsochronesServiceSettings {
	private static boolean enabled = true;
	private static int maximumLocations = 1;
	private static int maximumRangeDistance = 100000; //  in meters
	private static int maximumRangeTime = 3600; // in seconds
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
		value = AppConfig.Global().getServiceParameter("isochrones", "maximum_range_time");
		if (value != null)
			maximumRangeTime = Integer.parseInt(value);
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
	
	public static boolean getEnabled() {
		return enabled;
	}

	public static boolean getAllowComputeArea() {
		return allowComputeArea;
	}

	public static int getMaximumLocations() {
		return maximumLocations;
	}

	public static int getMaximumRange(IsochronesRangeType range) {
		switch(range)
		{
		case Distance:
			return maximumRangeDistance;
		case Time:
			return maximumRangeTime;
		}

		return 0;
	}

	public static int getMaximumIntervals()	{
		return maximumIntervals;
	}

	public static String getAttribution() {
		return attribution;
	}	
}
