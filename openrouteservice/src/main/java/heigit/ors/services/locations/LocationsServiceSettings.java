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
	private static int maximumCategories = 5;
	// maximum allowed length of a linestring, measured in meters
    private static double maximumFeatureLength = -1.0;	
    // maximum allowed area of a polygon, measured in square meters
    private static double  maximumFeatureArea = -1.0; 
    // maximum allowed search radius, measured in meters
    private static double maximumSearchRadiusForPoints = 20000;
    private static double maximumSearchRadiusForLinestrings = 500;
    private static double maximumSearchRadiusForPolygons = 500;
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

		value = AppConfig.Global().getServiceParameter("locations", "maximum_categories");
		if (value != null)
			maximumCategories = Integer.parseInt(value);
		
		value = AppConfig.Global().getServiceParameter("locations", "maximum_feature_length");
		if (value != null)
			maximumFeatureLength = Double.parseDouble(value);

		value = AppConfig.Global().getServiceParameter("locations", "maximum_feature_area");
		if (value != null)
			maximumFeatureArea = Double.parseDouble(value);
		
		value = AppConfig.Global().getServiceParameter("locations", "maximum_search_radius_for_points");
		if (value != null)
			maximumSearchRadiusForPoints = Double.parseDouble(value);
		
		value = AppConfig.Global().getServiceParameter("locations", "maximum_search_radius_for_linestrings");
		if (value != null)
			maximumSearchRadiusForLinestrings = Double.parseDouble(value);
		
		value = AppConfig.Global().getServiceParameter("locations", "maximum_search_radius_for_polygons");
		if (value != null)
			maximumSearchRadiusForPolygons = Double.parseDouble(value);		

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
	
	public static int getMaximumCategories()
	{
		return maximumCategories;
	}
	
	public static double getMaximumFeatureLength()
	{
		return maximumFeatureLength;
	}
	
	public static double getMaximumFeatureArea()
	{
		return maximumFeatureArea;
	}
	
	public static double getMaximumSearchRadiusForPoints()
	{
		return maximumSearchRadiusForPoints;
	}

	public static double getMaximumSearchRadiusForLinestrings()
	{
		return maximumSearchRadiusForLinestrings;
	}
	
	public static double getMaximumSearchRadiusForPolygons()
	{
		return maximumSearchRadiusForPolygons;
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
