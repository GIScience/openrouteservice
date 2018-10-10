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
package heigit.ors.services.matrix;

import heigit.ors.config.AppConfig;

public class MatrixServiceSettings 
{
	private static int maximumLocations = 100;
	private static int maximumLocationsFlexible = 25;
	private static int maximumVisitedNodes = 100000;
	private static double maximumSearchRadius = 2000;
	private static boolean allowResolveLocations = true;
	private static String attribution = "";
	private static boolean enabled = true;
	
	static 
	{
		String value = AppConfig.Global().getServiceParameter("matrix", "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);		
		value = AppConfig.Global().getServiceParameter("matrix", "maximum_locations");
		if (value != null)
			maximumLocations = Math.max(1, Integer.parseInt(value));
		value = AppConfig.Global().getServiceParameter("matrix", "maximum_locations_flexible");
		if (value != null)
			maximumLocationsFlexible = Math.max(1, Integer.parseInt(value));
		value = AppConfig.Global().getServiceParameter("matrix", "maximum_search_radius");
		if (value != null)
			maximumSearchRadius = Math.max(1, Double.parseDouble(value));
		value = AppConfig.Global().getServiceParameter("matrix", "maximum_visited_nodes");
		if (value != null)
			maximumVisitedNodes = Math.max(1, Integer.parseInt(value));
		value = AppConfig.Global().getServiceParameter("matrix", " allow_resolve_locations");
		if (value != null)
			allowResolveLocations = Boolean.parseBoolean(value);
		value = AppConfig.Global().getServiceParameter("matrix", "attribution");
		if (value != null)
			attribution = value;
	}
	
	public static Boolean getEnabled() {
		return enabled;
	}

	public static boolean getAllowResolveLocations() {
		return allowResolveLocations;
	}
	
	public static int getMaximumVisitedNodes() {
		return maximumVisitedNodes;
	}
	
	public static int getMaximumLocations(boolean flexible) {
		return (flexible? maximumLocationsFlexible : maximumLocations);
	}
	
	public static double getMaximumSearchRadius() {
		return maximumSearchRadius;
	}
	
	public static String getAttribution() {
		return attribution;
	}
}
