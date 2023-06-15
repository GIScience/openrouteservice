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

import static com.graphhopper.routing.weighting.Weighting.INFINITE_U_TURN_COSTS;

public class MatrixServiceSettings {
	private static int maximumRoutes = 2500;
	private static int maximumRoutesFlexible = 25;
	private static int maximumVisitedNodes = 100000;
	private static double maximumSearchRadius = 2000;
	private static boolean allowResolveLocations = true;
	private static String attribution = "";
	private static boolean enabled = true;
	private static double uTurnCost = INFINITE_U_TURN_COSTS;

	public static final String PARAM_MATRIX = "matrix";

	static  {
		String value = AppConfig.getGlobal().getServiceParameter(PARAM_MATRIX, "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);		
		value = AppConfig.getGlobal().getServiceParameter(PARAM_MATRIX, "maximum_routes");
		if (value != null)
			maximumRoutes = Math.max(1, Integer.parseInt(value));
		value = AppConfig.getGlobal().getServiceParameter(PARAM_MATRIX, "maximum_routes_flexible");
		if (value != null)
			maximumRoutesFlexible = Math.max(1, Integer.parseInt(value));
		value = AppConfig.getGlobal().getServiceParameter(PARAM_MATRIX, "maximum_search_radius");
		if (value != null)
			maximumSearchRadius = Math.max(1, Double.parseDouble(value));
		value = AppConfig.getGlobal().getServiceParameter(PARAM_MATRIX, "maximum_visited_nodes");
		if (value != null)
			maximumVisitedNodes = Math.max(1, Integer.parseInt(value));
		value = AppConfig.getGlobal().getServiceParameter(PARAM_MATRIX, " allow_resolve_locations");
		if (value != null)
			allowResolveLocations = Boolean.parseBoolean(value);
		value = AppConfig.getGlobal().getServiceParameter(PARAM_MATRIX, "attribution");
		if (value != null)
			attribution = value;
		value = AppConfig.getGlobal().getServiceParameter(PARAM_MATRIX, "u_turn_cost");
		if (value != null && Double.parseDouble(value) != -1.0)
			uTurnCost = Double.parseDouble(value);
	}

	private MatrixServiceSettings() {}
	
	public static boolean getEnabled() {
		return enabled;
	}

	public static boolean getAllowResolveLocations() {
		return allowResolveLocations;
	}
	
	public static int getMaximumVisitedNodes() {
		return maximumVisitedNodes;
	}
	
	public static int getMaximumRoutes(boolean flexible) {
		return (flexible? maximumRoutesFlexible : maximumRoutes);
	}
	
	public static double getMaximumSearchRadius() {
		return maximumSearchRadius;
	}
	
	public static String getAttribution() {
		return attribution;
	}

	public static double getUTurnCost() { return uTurnCost;}
}
