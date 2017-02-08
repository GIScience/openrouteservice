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
package heigit.ors.services.geocoding;

import heigit.ors.config.AppConfig;

public class GeocodingServiceSettings {
	private static String geocoderName = "photon";
	private static String geocodingUrl = "";
	private static String reverseGeocodingUrl = "";
	private static String userAgent = "ors";
	private static String attribution = "";
	private static boolean enabled = true;
	
	static 
	{
		String value = AppConfig.Global().getServiceParameter("geocoding", "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);		
		value = AppConfig.Global().getServiceParameter("geocoding", "geocoder_name");
		if (value != null)
			geocoderName = value;
		value = AppConfig.Global().getServiceParameter("geocoding", "geocoding_url");
		if (value != null)
			geocodingUrl = value;
		value = AppConfig.Global().getServiceParameter("geocoding", "reverse_geocoding_url");
		if (value != null)
			reverseGeocodingUrl = value;
		value = AppConfig.Global().getServiceParameter("geocoding", "user_agent");
		if (value != null)
			userAgent = value;
		value = AppConfig.Global().getServiceParameter("geocoding", "attribution");
		if (value != null)
			attribution = value;
	}
	
	public static Boolean getEnabled() {
		return enabled;
	}

	public static String getGeocoderName() {
		return geocoderName;
	}
	
	public static String getGeocodingURL() {
		return geocodingUrl;
	}
	
	public static String getReverseGeocodingURL() {
		return reverseGeocodingUrl;
	}

	public static String getUserAgent() {
		return userAgent;
	}
	
	public static String getAttribution() {
		return attribution;
	}
}
