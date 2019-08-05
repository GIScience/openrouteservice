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
package heigit.ors.services.shortenlink;

import heigit.ors.config.AppConfig;

public class ShortenLinkServiceSettings {
	private static String userName = "";
	private static String userPassword = "";
	private static String apiKey = "";
	private static String attribution = "";
	private static Boolean enabled = true;
	
	static 
	{
		String value = AppConfig.Global().getServiceParameter("shortenlink", "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);		
		value = AppConfig.Global().getServiceParameter("shortenlink", "user_name");
		if (value != null)
			userName = value;
		value = AppConfig.Global().getServiceParameter("shortenlink", "user_password");
		if (value != null)
			userPassword = value;
		value = AppConfig.Global().getServiceParameter("shortenlink", "api_key");
		if (value != null)
			apiKey = value;
		value = AppConfig.Global().getServiceParameter("shortenlink", "attribution");
		if (value != null)
			attribution = value;
	}
	
	public static Boolean getEnabled()
	{
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
