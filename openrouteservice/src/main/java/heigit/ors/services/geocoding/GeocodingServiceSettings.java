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
package heigit.ors.services.geocoding;

import heigit.ors.config.AppConfig;

public class GeocodingServiceSettings 
{
	private static String geocoderName = "photon";
	private static String geocodingUrl = "";
	private static String reverseGeocodingUrl = "";
	private static int responseLimit = 20;
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
		value = AppConfig.Global().getServiceParameter("geocoding", "response_limit");
		if (value != null)
			responseLimit = Integer.parseInt(value);
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
	
	public static int getResponseLimit() {
		return responseLimit;
	}

	public static String getUserAgent() {
		return userAgent;
	}
	
	public static String getAttribution() {
		return attribution;
	}
}
