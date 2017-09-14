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
