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
package heigit.ors.services.accessibility;

import heigit.ors.config.AppConfig;

public class AccessibilityServiceSettings 
{
	private static int  responseLimit = 50; 
	private static String attribution = "";
	private static boolean enabled = true;
	
	static 
	{
		String value = AppConfig.Global().getServiceParameter("accessibility", "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);		
		value = AppConfig.Global().getServiceParameter("accessibility", "response_limit");
		if (value != null)
			responseLimit = Integer.parseInt(value);
		value = AppConfig.Global().getServiceParameter("accessibility", "attribution");
		if (value != null)
			attribution = value;
	}
	
	public static Boolean getEnabled() {
		return enabled;
	}
	
	public static int getResponseLimit() {
		return responseLimit;
	}
	
	public static String getAttribution() {
		return attribution;
	}
}
