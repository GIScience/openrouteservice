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
package heigit.ors.services.mapmatching;

import heigit.ors.config.AppConfig;

public class MapMatchingServiceSettings {
	private static Boolean enabled  = true;
	private static int maximumLocations = 100;
	private static double maximumSearchRadius = 200;
	private static int maximumVisitedNodes = 10000;
	private static String attribution = "";
		
	static 
	{
		String value = AppConfig.Global().getServiceParameter("mapmatching", "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);		
		value = AppConfig.Global().getServiceParameter("mapmatching", "maximum_locations");
		if (value != null)
			maximumLocations = Math.max(1, Integer.parseInt(value));
		value = AppConfig.Global().getServiceParameter("mapmatching", "maximum_search_radius");
		if (value != null)
			maximumSearchRadius = Math.max(1, Double.parseDouble(value));
		value = AppConfig.Global().getServiceParameter("mapmatching", "maximum_visited_nodes");
		if (value != null)
			maximumVisitedNodes = Math.max(1, Integer.parseInt(value));
		value = AppConfig.Global().getServiceParameter("mapmatching", "attribution");
		if (value != null)
			attribution = value;
	}
	
	public static Boolean getEnabled() {
		return enabled;
	}
	
	public static int getMaximumLocations() {
		return maximumLocations;
	}
	
 	public static String getAttribution() {
		return attribution;
	}

	public static double getMaximumSearchRadius() {
		return maximumSearchRadius;
	}

	public static int getMaximumVisitedNodes() {
		return maximumVisitedNodes;
	}
}
