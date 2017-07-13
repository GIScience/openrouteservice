/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

package heigit.ors.routing;

import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;

import heigit.ors.util.StringUtility;

public class ProfileWeighting {
	private String _name;
	private PMap _params;

	public ProfileWeighting(String name) throws Exception
	{
		if (Helper.isEmpty(name))
			throw new Exception("'name' cann't be null or empty");

		_name =  name;
	}

	public String getName()
	{
		return _name;
	}

	public void addParameter(String name, String value)
	{
		if (_params == null)
			_params = new PMap();

		_params.put(name, value);
	}

	public PMap getParameters()
	{
		return _params;
	}
	public static String encodeName(String name)
	{
		return "weighting_#" + name + "#";
	}

	public static String decodeName(String value)
	{
		if (value.startsWith("weighting_#"))
			return StringUtility.substring(value, '#');
		else
			return null;
	}
}
