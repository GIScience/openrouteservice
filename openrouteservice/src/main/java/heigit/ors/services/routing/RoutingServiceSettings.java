/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
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
	private static int maximumWayPoints = 30;
	private static double dynamicWeightingMaximumDistance = 0.0;
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
		
		value = config.getServiceParameter("routing", "maximum_way_points");
		if (value != null)
			maximumWayPoints = Integer.parseInt(value);
		
		value = config.getServiceParameter("routing", "maximum_distance_with_dynamic_weights");
		if (value != null)
			dynamicWeightingMaximumDistance = Double.parseDouble(value);
		
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
	
	public static int getMaximumWayPoints() {
		return maximumWayPoints;
	}
	
	public static double getDynamicWeightingMaximumDistance() {
		return dynamicWeightingMaximumDistance;
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
	
	public static Map<String, Object> getParametersMap(String paramName) 
	{
	   return _config.getServiceParametersMap("routing", paramName);	
	}
	
 	public static String getAttribution() {
		return attribution;
	}
}
