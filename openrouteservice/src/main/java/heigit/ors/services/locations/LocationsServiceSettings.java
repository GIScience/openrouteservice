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
package heigit.ors.services.locations;

import java.util.Map;

import heigit.ors.config.AppConfig;

public class LocationsServiceSettings
{
	private static String providerName = null;
	private static Map<String, Object> providerParameters;
	private static int responseLimit = 100;
	// maximum allowed length of a linestring, measured in meters
    private static double maximumFeatureLength = -1.0;	
    // maximum allowed area of a polygon, measured in square meters
    private static double  maximumFeatureArea = -1.0; 
    // maximum allowed search radius
    private static double maximumSearchRadius = 200;
	private static String attribution = "";
	private static boolean enabled = true;

	static 
	{
		String value = AppConfig.Global().getServiceParameter("locations", "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);		
		
		value = AppConfig.Global().getServiceParameter("locations", "provider_name");
		if (value != null)
			providerName = value;
		
		providerParameters = AppConfig.Global().getServiceParametersMap("locations", "provider_parameters");

		value = AppConfig.Global().getServiceParameter("locations", "maximum_feature_length");
		if (value != null)
			maximumFeatureLength = Double.parseDouble(value);

		value = AppConfig.Global().getServiceParameter("locations", "maximum_feature_area");
		if (value != null)
			maximumFeatureArea = Double.parseDouble(value);
		
		value = AppConfig.Global().getServiceParameter("locations", "maximum_search_radius");
		if (value != null)
			maximumSearchRadius = Double.parseDouble(value);

		value = AppConfig.Global().getServiceParameter("locations", "response_limit");
		if (value != null)
			responseLimit = Integer.parseInt(value);
		
		value = AppConfig.Global().getServiceParameter("locations", "attribution");
		if (value != null)
			attribution = value;
	}

	public static Boolean getEnabled() 
	{
		return enabled;
	}

	public static String getProviderName() 
	{
		return providerName;
	}
	
	public static Map<String, Object> getProviderParameters() 
	{
		return providerParameters;
	}
	
	public static double getMaximumFeatureLength()
	{
		return maximumFeatureLength;
	}
	
	public static double getMaximumFeatureArea()
	{
		return maximumFeatureArea;
	}
	
	public static double getMaximumSearchRadius()
	{
		return maximumSearchRadius;
	}

	public static int getResponseLimit()
	{
		return responseLimit;
	}

	public static String getAttribution()
	{
		return attribution;
	}
}
