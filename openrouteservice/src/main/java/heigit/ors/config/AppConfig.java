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
package heigit.ors.config;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import heigit.ors.services.shortenlink.ShortenLinkServlet;
import heigit.ors.util.StringUtility;

public class AppConfig {

	private Config _config;
	private static AppConfig _global;
	
	public AppConfig(String path)
	{
		File file = new File(path);
		_config = ConfigFactory.parseFile(file);
	}
	
	public AppConfig()	{
    	URL url = ShortenLinkServlet.class.getClassLoader().getResource("../app.config");
    	
    	File file = new File(url.getPath());
		_config = ConfigFactory.parseFile(file);
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
	

	public Map<String,Object> getServiceParametersMap(String serviceName, String paramName)
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
					Map<String,Object> map = getServiceParametersMap(serviceName, paramName + "." + key);
					value = map;
					break;
				case LIST:
					value = paramValue.unwrapped();
					break;
				case STRING:
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
