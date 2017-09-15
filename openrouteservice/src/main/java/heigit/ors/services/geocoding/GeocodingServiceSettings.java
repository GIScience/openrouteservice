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
