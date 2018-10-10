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
package heigit.ors.services.optimization;

import java.util.Map;

import heigit.ors.config.AppConfig;

public class OptimizationServiceSettings {
	private static boolean enabled  = true;
	private static int maximumLocations = 100;
	private static String solverName = "default";
	private static Map<String, Object> solverOptions;
	private static String attribution = "";
	private static AppConfig _config;
	
	static 
	{
		_config = AppConfig.Global();
		init(_config);
	}
	
	public static void loadFromFile(String path)
	{
		_config = new AppConfig(path);
		
		init(_config);
	}
	
	private static void init(AppConfig config)
	{
		String value = config.getServiceParameter("optimization", "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);		
		
		value = AppConfig.Global().getServiceParameter("optimization", "maximum_locations");
		if (value != null)
			maximumLocations = Math.max(1, Integer.parseInt(value));
		
		value = AppConfig.Global().getServiceParameter("optimization", "solver_name");
		if (value != null)
			solverName = value;
		
		value = AppConfig.Global().getServiceParameter("optimization", "solver_options");
		if (value != null)
			solverOptions = _config.getServiceParametersMap("optimization", "solver_options", false);
		
		value = config.getServiceParameter("optimization", "attribution");
		if (value != null)
			attribution = value;
	}
	
	public static boolean getEnabled()
	{
		return enabled;
	}

	public static String getSolverName() {
		return solverName;
	}
	
	public static Map<String, Object> getSolverOptions()
	{
		return solverOptions;
	}
	
	public static int getMaximumLocations() {
		return maximumLocations;
	}
	
 	public static String getAttribution() {
		return attribution;
	}
}
