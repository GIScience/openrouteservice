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
package heigit.ors.logging;

import heigit.ors.config.AppConfig;

public class LoggingSettings {
	private static boolean enabled;
	private static String levelFile = "DEFAULT_LOGGING.properties";
	private static String location = "logs/output/ors.log";
	private static boolean stdout = false;
	
	static 
	{
		String value = AppConfig.Global().getParameter("logging", "level_file");
		if (value != null)
			levelFile = value;
		value = AppConfig.Global().getParameter("logging", "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);	
		value = AppConfig.Global().getParameter("logging", "location");
		if (value != null)
			location = value;
		value = AppConfig.Global().getParameter("logging", "stdout");
		if (value != null)
			stdout = Boolean.parseBoolean(value);
	}
	
	public static Boolean getEnabled() {
		return enabled;
	}
	
	public static String getLevelFile() {
		return levelFile;
	}

	public static String getLocation() {
		return location;
	}
	
	public static Boolean getStdOut() {
		return stdout;
	}
}
