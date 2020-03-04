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
package org.heigit.ors.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import org.apache.log4j.Logger;
import org.heigit.ors.util.FileUtility;
import org.heigit.ors.util.StringUtility;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppConfig {

	public static final String PREFIX_ORS_SERVICES = "ors.services.";
	private Config config;
	private static AppConfig global;
	private static String osmMd5Hash = null;
	private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());

	public AppConfig(String path) {
		File file = new File(path);
		config = ConfigFactory.parseFile(file);
	}

	public AppConfig() {
		try {
			File file;
			if (System.getProperty("ors_app_config") != null) {
				file = new FileSystemResource(System.getProperty("ors_app_config")).getFile();
			} else {
				String appConfigName = "app.config";
				if (System.getenv("ORS_APP_CONFIG") != null)
					appConfigName = System.getenv("ORS_APP_CONFIG");
				file = new ClassPathResource(appConfigName).getFile();
			}
			config = ConfigFactory.parseFile(file);
		} catch (IOException ioe) {
			LOGGER.error(ioe);
		}

		//Modification by H Leuschner: Save md5 hash of map file in static String for access with every request
		File graphsDir = new File(getServiceParameter("routing.profiles.default_params", "graphs_root_path"));
		File[] md5Files = graphsDir.listFiles(pathname -> pathname.getName().endsWith(".md5"));
		if (md5Files != null && md5Files.length == 1){
			try {
				osmMd5Hash = FileUtility.readFile(md5Files[0].toString()).trim();
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
	}

	public static AppConfig getGlobal() {
		if (global == null)
			global = new AppConfig();
		return global;
	}

	public String getParameter(String section, String paramName) {
		try {
			return config.getString("ors." + section + "." + paramName);
		} catch(Exception e) {
			// IGNORE
		}
		return null;
	}

	public String getServiceParameter(String serviceName, String paramName) {
		try {
			return config.getString(PREFIX_ORS_SERVICES + serviceName + "." + paramName);
		} catch(Exception e) {
			// IGNORE
		}
		return null;
	}

	public String getRoutingProfileParameter(String profile, String paramName) {
		try {
			String rootPath = PREFIX_ORS_SERVICES + "routing.profiles";
			ConfigObject configObj = config.getObject(rootPath);
			for(String key : configObj.keySet()) {
				if (key.startsWith("profile-")) {
					String profileName = getServiceParameter("routing", "profiles." + key + ".profiles");
					if (profile.equals(profileName)) {
						String profileValue = getServiceParameter("routing", "profiles." + key + ".parameters." + paramName);
						if (profileValue != null) {
							return profileValue;
						}
					}
				}
			}
			return config.getString(rootPath + ".default_params." + paramName);
		} catch(Exception e) {
			// IGNORE
		}
		return null;
	}

	public List<? extends ConfigObject> getObjectList(String serviceName, String paramName) {
		try {
			return config.getObjectList(PREFIX_ORS_SERVICES + serviceName + "." + paramName);
		} catch(Exception e) {
			// IGNORE
		}
		return new ArrayList<>() ;
	}

	public List<? extends ConfigObject> getObjectList(String paramName) {
		try {
			return config.getObjectList("ors." + paramName);
		} catch(Exception e) {
			// IGNORE
		}
		return new ArrayList<>() ;
	}

	public List<Double> getDoubleList(String serviceName, String paramName) {
		try {
			return config.getDoubleList(PREFIX_ORS_SERVICES + serviceName + "." + paramName);
		} catch(Exception e) {
			// IGNORE
		}
		return new ArrayList<>();
	}

	public List<String> getServiceParametersList(String serviceName, String paramName) {
		try {
			return config.getStringList(PREFIX_ORS_SERVICES + serviceName + "." + paramName);
		} catch(Exception e) {
			// IGNORE
		}
		return new ArrayList<>();
	}

	public static boolean hasValidMD5Hash() {
		return osmMd5Hash != null;
	}

	public static String getMD5Hash() {
		return osmMd5Hash;
	}

	public Map<String,Object> getServiceParametersMap(String serviceName, String paramName, boolean quotedStrings) {
		Map<String,Object> result = null;
		try {
			String rootPath = PREFIX_ORS_SERVICES + serviceName + "." + paramName;
			ConfigObject configObj = config.getObject(rootPath);
			result = new HashMap<>();
			for(String key : configObj.keySet()) {
				Object value = null;
				ConfigValue paramValue = config.getValue(rootPath + "." + key);
				switch(paramValue.valueType()) {
					case NUMBER:
						value = paramValue.unwrapped();
						break;
					case OBJECT:
						value = getServiceParametersMap(serviceName, paramName + "." + key, quotedStrings);
						break;
					case LIST:
						value = paramValue.unwrapped();
						break;
					case STRING:
						if (quotedStrings)
							value = paramValue.render();
						else
							value = StringUtility.trim(paramValue.render(), '"');
						break;
					case BOOLEAN:
						value = paramValue.unwrapped();
						break;
					default:
						break;
				}
				result.put(key, value);
			}
		} catch(Exception ex) {
			// IGNORE
		}

		return result;
	}
}
