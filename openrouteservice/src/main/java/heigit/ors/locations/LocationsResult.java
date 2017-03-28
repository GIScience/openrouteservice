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
package heigit.ors.locations;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

public class LocationsResult 
{
	private Geometry _geometry;
	private Map<String, String> _properties;

	public LocationsResult()
	{
		_properties = new LinkedHashMap<String, String>();
	}

	public Geometry getGeometry() 
	{
		return _geometry;
	}

	public void setGeometry(Geometry geometry) 
	{
		_geometry = geometry;
	}
	
	public Map<String, String> getProperties()
	{
		return _properties;
	}	
	
	public void addProperty(String name, String value)
	{
		_properties.put(name, value);
	}
}
