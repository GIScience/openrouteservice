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
		
		providerParameters = AppConfig.Global().getServiceParametersMap("locations", "provider_parameters", false);

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
