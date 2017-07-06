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
package heigit.ors.services.optimization;

import heigit.ors.config.AppConfig;

public class OptimizationServiceSettings {
	private static Boolean enabled  = true;
	private static int maximumLocations = 100;
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
		
		value = config.getServiceParameter("optimization", "attribution");
		if (value != null)
			attribution = value;
	}
	
	public static Boolean getEnabled()
	{
		return enabled;
	}

	public static int getMaximumLocations() {
		return maximumLocations;
	}
	
 	public static String getAttribution() {
		return attribution;
	}
}
