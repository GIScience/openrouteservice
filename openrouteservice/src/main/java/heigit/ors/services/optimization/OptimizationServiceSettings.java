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
