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
