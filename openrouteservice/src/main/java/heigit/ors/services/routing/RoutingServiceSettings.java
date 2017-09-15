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
package heigit.ors.services.routing;

import java.util.List;
import java.util.Map;

import com.graphhopper.util.Helper;

import heigit.ors.config.AppConfig;

public class RoutingServiceSettings {
	private static Boolean enabled  = true;
	private static String sourceFile = "";
	private static String workingMode = "Normal"; // Normal or PrepareGraphs
	private static int initializationThreads = 1;
	private static boolean distanceApproximation = false;
	private static String storageFormat = "Native";
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
		String value = config.getServiceParameter("routing", "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);		
		
		value =  _config.getServiceParametersList("routing", "sources").get(0);
		if (value != null)
			sourceFile = value;
		
		workingMode = config.getServiceParameter("routing", "mode");
		
		value = config.getServiceParameter("routing", "init_threads");
		if (value != null)
			initializationThreads = Integer.parseInt(value);
		
		value = config.getServiceParameter("routing", "distance_approximation");
		if (value != null)
			distanceApproximation = Boolean.parseBoolean(value);
		
		value = config.getServiceParameter("routing", "storage_format");
		if (value != null)
			storageFormat = value;
		
		value = config.getServiceParameter("routing", "attribution");
		if (value != null)
			attribution = value;
	}
	
	public static Boolean getEnabled()
	{
		return enabled;
	}
	
	public static String getSourceFile() {
		return sourceFile;
	}
	
	public static String getWorkingMode() {
		return workingMode;
	}
	
	public static int getInitializationThreads() {
		return initializationThreads;
	}
	
	public static boolean getDistanceApproximation()	{
		return distanceApproximation;
	}
	
	public static String getStorageFormat()	{
		return storageFormat;
	}

	public static String getParameter(String paramName) 
	{
	   return _config.getServiceParameter("routing", paramName);	
	}
	
	public static String getParameter(String paramName, boolean notNull) throws Exception 
	{
	   String value = _config.getServiceParameter("routing", paramName);
	   if (notNull && Helper.isEmpty(value))
		   throw new Exception("Parameter '" + paramName + "' must not be null or empty.");
	   
	   return value;
	}
	
	public static List<String> getParametersList(String paramName) 
	{
	   return _config.getServiceParametersList("routing", paramName);	
	}
	
	public static List<Double> getDoubleList(String paramName) 
	{
	   return _config.getDoubleList("routing", paramName);	
	}
	
	public static Map<String, Object> getParametersMap(String paramName, boolean quotedStrings) 
	{
	   return _config.getServiceParametersMap("routing", paramName, quotedStrings);	
	}
	
 	public static String getAttribution() {
		return attribution;
	}
}
