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

import com.graphhopper.util.Helper;

import heigit.ors.config.AppConfig;

public class RoutingServiceSettings {
	private static Boolean enabled  = true;  
	private static String attribution = "";
	private static AppConfig _config;
	
	static 
	{
		_config = AppConfig.Global();
		String value = AppConfig.Global().getServiceParameter("routing", "attribution");
		if (value != null)
			attribution = value;
	}
	
	public static void loadFromFile(String path)
	{
		_config = new AppConfig(path);
		String value = AppConfig.Global().getServiceParameter("routing", "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);		
		value = AppConfig.Global().getServiceParameter("routing", "attribution");
		if (value != null)
			attribution = value;
	}
	
	public static Boolean getEnabled()
	{
		return enabled;
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
	
	public static List<String> getParameters(String paramName) 
	{
	   return _config.getServiceParameters("routing", paramName);	
	}
	
 	public static String getAttribution() {
		return attribution;
	}
}
