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

import java.util.List;
import java.util.Map;

import com.graphhopper.util.Helper;

public class RoutingServiceSettings {
	private static final String SERVICE_NAME_ROUTING = "routing";
	private static boolean enabled  = true;
	private static String sourceFile = "";
	private static String workingMode = "Normal"; // Normal or PrepareGraphs
	private static int initializationThreads = 1;
	private static boolean distanceApproximation = false;
	private static String storageFormat = "Native";
	private static String attribution = "";
	private static String routingName = "openrouteservice directions";
	private static AppConfig config;
	
	static {
		config = AppConfig.getGlobal();
		init(config);
	}

	private RoutingServiceSettings() {}

	public static void loadFromFile(String path) {
		config = new AppConfig(path);
		init(config);
	}
	
	private static void init(AppConfig config) {
		String value = config.getServiceParameter(SERVICE_NAME_ROUTING, "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);

		sourceFile = RoutingServiceSettings.config.getServiceParametersList(SERVICE_NAME_ROUTING, "sources").get(0);

		workingMode = config.getServiceParameter(SERVICE_NAME_ROUTING, "mode");
		
		value = config.getServiceParameter(SERVICE_NAME_ROUTING, "init_threads");
		if (value != null)
			initializationThreads = Integer.parseInt(value);
		
		value = config.getServiceParameter(SERVICE_NAME_ROUTING, "distance_approximation");
		if (value != null)
			distanceApproximation = Boolean.parseBoolean(value);
		
		value = config.getServiceParameter(SERVICE_NAME_ROUTING, "storage_format");
		if (value != null)
			storageFormat = value;
		
		value = config.getServiceParameter(SERVICE_NAME_ROUTING, "attribution");
		if (value != null)
			attribution = value;

		value = config.getServiceParameter(SERVICE_NAME_ROUTING, "routing_name");
		if (value != null)
			routingName = value;
	}
	
	public static boolean getEnabled() {
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

	public static String getParameter(String paramName)  {
	   return config.getServiceParameter(SERVICE_NAME_ROUTING, paramName);
	}
	
	public static String getParameter(String paramName, boolean notNull) {
	   String value = config.getServiceParameter(SERVICE_NAME_ROUTING, paramName);
	   if (notNull && Helper.isEmpty(value))
		   throw new IllegalArgumentException("Parameter '" + paramName + "' must not be null or empty.");
	   
	   return value;
	}
	
	public static List<String> getParametersList(String paramName)  {
	   return config.getServiceParametersList(SERVICE_NAME_ROUTING, paramName);
	}
	
	public static List<Double> getDoubleList(String paramName)  {
	   return config.getDoubleList(SERVICE_NAME_ROUTING, paramName);
	}
	
	public static Map<String, Object> getParametersMap(String paramName, boolean quotedStrings) {
	   return config.getServiceParametersMap(SERVICE_NAME_ROUTING, paramName, quotedStrings);
	}
	
 	public static String getAttribution() {
		return attribution;
	}

	public static String getRoutingName() {
		return routingName;
	}
}
