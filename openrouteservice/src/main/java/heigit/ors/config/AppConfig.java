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
import java.util.List;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

import heigit.ors.services.shortenlink.ShortenLinkServlet;

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
	
	public List<String> getServiceParameters(String serviceName, String paramName)
	{
		try
		{
			return _config.getStringList("ors.services." + serviceName + "." + paramName);
		}
		catch(Exception ex)
		{}
		
		return null;
	}
}
