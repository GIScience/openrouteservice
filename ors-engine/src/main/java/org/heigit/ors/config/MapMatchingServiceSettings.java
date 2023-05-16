/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package org.heigit.ors.config;

public class MapMatchingServiceSettings {
	private static boolean enabled  = true;
	private static int maximumLocations = 100;
	private static double maximumSearchRadius = 200;
	private static int maximumVisitedNodes = 10000;
	private static String attribution = "";

	public static final String PARAM_MAPMATCHING = "mapmatching";

	static {
		String value = AppConfig.getGlobal().getServiceParameter(PARAM_MAPMATCHING, "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);		
		value = AppConfig.getGlobal().getServiceParameter(PARAM_MAPMATCHING, "maximum_locations");
		if (value != null)
			maximumLocations = Math.max(1, Integer.parseInt(value));
		value = AppConfig.getGlobal().getServiceParameter(PARAM_MAPMATCHING, "maximum_search_radius");
		if (value != null)
			maximumSearchRadius = Math.max(1, Double.parseDouble(value));
		value = AppConfig.getGlobal().getServiceParameter(PARAM_MAPMATCHING, "maximum_visited_nodes");
		if (value != null)
			maximumVisitedNodes = Math.max(1, Integer.parseInt(value));
		value = AppConfig.getGlobal().getServiceParameter(PARAM_MAPMATCHING, "attribution");
		if (value != null)
			attribution = value;
	}

	private  MapMatchingServiceSettings() {}
	
	public static boolean getEnabled() {
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
