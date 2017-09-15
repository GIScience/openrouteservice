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
