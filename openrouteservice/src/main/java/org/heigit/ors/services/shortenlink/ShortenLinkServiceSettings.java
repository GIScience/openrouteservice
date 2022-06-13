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
package org.heigit.ors.services.shortenlink;

import org.heigit.ors.config.AppConfig;

public class ShortenLinkServiceSettings {
	private static String userName = "";
	private static String userPassword = "";
	private static String apiKey = "";
	private static String attribution = "";
	private static boolean enabled = true;

	public static final String SERVICE_SHORTENLINK = "shortenlink";

	static  {
		String value = AppConfig.getGlobal().getServiceParameter(SERVICE_SHORTENLINK, "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);		
		value = AppConfig.getGlobal().getServiceParameter(SERVICE_SHORTENLINK, "user_name");
		if (value != null)
			userName = value;
		value = AppConfig.getGlobal().getServiceParameter(SERVICE_SHORTENLINK, "user_password");
		if (value != null)
			userPassword = value;
		value = AppConfig.getGlobal().getServiceParameter(SERVICE_SHORTENLINK, "api_key");
		if (value != null)
			apiKey = value;
		value = AppConfig.getGlobal().getServiceParameter(SERVICE_SHORTENLINK, "attribution");
		if (value != null)
			attribution = value;
	}

	private ShortenLinkServiceSettings() {}
	
	public static boolean getEnabled() {
		return enabled;
	}

	public static String getUserName() {
		return userName;
	}	
	
	public static String getUserPassword() {
		return userPassword;
	}
	
	public static String getApiKey() {
		return apiKey;
	}	

	public static String getAttribution() {
		return attribution;
	}	
}
