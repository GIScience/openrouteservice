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
package heigit.ors.config;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import org.apache.log4j.Logger;

import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.util.StringUtility;
import heigit.ors.util.FileUtility;

public class AppConfig {

	private Config _config;
	private static AppConfig _global;
	private static String osm_md5_hash = null;
	private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());

	public AppConfig(String path)
	{
		File file = new File(path);
		_config = ConfigFactory.parseFile(file);
	}
	
	public AppConfig()	{
    	URL url = RoutingProfileManager.class.getClassLoader().getResource("../app.config");
		if(System.getenv("ORS_APP_CONFIG") != null)
			url = RoutingProfileManager.class.getClassLoader().getResource("../" + System.getenv("ORS_APP_CONFIG"));
    	
    	File file = new File(url.getPath());
		_config = ConfigFactory.parseFile(file);

		//Modification by H Leuschner: Save md5 hash of map file in static String for access with every request
		File graphsDir = new File(getServiceParameter("routing.profiles.default_params", "graphs_root_path"));
		File[] md5Files = graphsDir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".md5");
			}
		});
		if (md5Files != null && md5Files.length == 1){
			try{
				osm_md5_hash = FileUtility.readFile(md5Files[0].toString()).trim();
			}
			catch (IOException e)
			{LOGGER.error(e);}
		}
	}
	
	public static AppConfig Global()
	{
		if (_global == null)
			_global = new AppConfig();
		
		return _global;
	}
	
	public String getParameter(String section, String paramName)
	{
		try
		{
			return _config.getString("ors." + section + "." + paramName);
		}
		catch(ConfigException ex)
		{}
		
		return null;
	}
	
	public String getServiceParameter(String serviceName, String paramName)
	{
		try
		{
			return _config.getString("ors.services." + serviceName + "." + paramName);
		}
		catch(ConfigException ex)
		{}
		
		return null;
	}
	
	public List<? extends ConfigObject> getObjectList(String serviceName, String paramName)
	{
		try
		{
			return _config.getObjectList("ors.services." + serviceName + "." + paramName);
		}
		catch(ConfigException ex)
		{}
		
		return null;
	}
	
	public List<Double> getDoubleList(String serviceName, String paramName) 
	{
		try
		{
			return _config.getDoubleList("ors.services." + serviceName + "." + paramName);
		}
		catch(Exception ex)
		{}
		
		return null;
	}
	
	public List<String> getServiceParametersList(String serviceName, String paramName)
	{
		try
		{
			return _config.getStringList("ors.services." + serviceName + "." + paramName);
		}
		catch(Exception ex)
		{}
		
		return null;
	}

	public static boolean hasValidMD5Hash()
	{
		return osm_md5_hash != null;
	}

	public static String getMD5Hash()
	{
		return osm_md5_hash;
	}

	public Map<String,Object> getServiceParametersMap(String serviceName, String paramName, boolean quotedStrings)
	{
		Map<String,Object> result = null;
		
		try
		{
			String rootPath = "ors.services." + serviceName + "." + paramName;
			ConfigObject configObj = _config.getObject(rootPath);
			
			result = new HashMap<String, Object>();
			
			for(String key : configObj.keySet())
			{
				Object value = null;
				ConfigValue paramValue = _config.getValue(rootPath + "." + key);

				switch(paramValue.valueType())
				{
				case NUMBER:
					value = paramValue.unwrapped();
					break;
				case OBJECT:
					Map<String,Object> map = getServiceParametersMap(serviceName, paramName + "." + key, quotedStrings);
					value = map;
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
				default:
					break;
				}
				
				result.put(key, value);
			}
		}
		catch(Exception ex)
		{}
		
		return result;
	}
}
