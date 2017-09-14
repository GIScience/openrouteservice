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
			res = maximumRangeDistance;
			
			if (profileMaxRangeDistances != null && profileMaxRangeDistances.containsKey(profileType))
			{
				res = profileMaxRangeDistances.get(profileType);
			}
			break;
		case Time:
			res = maximumRangeTime;
			
			if (profileMaxRangeTimes != null && profileMaxRangeTimes.containsKey(profileType))
			{
				res = profileMaxRangeTimes.get(profileType);
			}
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
