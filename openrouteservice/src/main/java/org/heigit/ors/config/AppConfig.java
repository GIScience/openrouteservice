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
package heigit.ors.config;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import org.apache.log4j.Logger;

import heigit.ors.util.StringUtility;
import heigit.ors.util.FileUtility;
import org.springframework.core.io.ClassPathResource;

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
		File file;

		String appConfigName = "app.config";
		if(System.getenv("ORS_APP_CONFIG") != null)
			appConfigName = System.getenv("ORS_APP_CONFIG");
    	try {

    		ClassPathResource rs = new ClassPathResource(appConfigName);
			file = rs.getFile();
			_config = ConfigFactory.parseFile(file);
		} catch (IOException ioe) {
    		LOGGER.error(ioe);
		}

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
