package org.freeopenls.routeservice.routing.configuration;

import java.util.ArrayList;

import org.freeopenls.routeservice.routing.RoutePreferenceType;

import com.vividsolutions.jts.geom.Envelope;

public class RouteProfileConfiguration
{
	public String Preferences; // comma separated
	public String ConfigFileName;   // path to Graphhopper's configuration file.
	public String GraphLocation;
	public Boolean DynamicWeighting = false; // to store additional fields such as max_width, max_height, max_weight, etc.
	public Boolean StoreSurfaceInformation = false;// to store way and surface type information
	public Boolean StoreHillIndex = false;// to store way and surface type information
	public Double MaximumDistance;
	public Double MinimumDistance; 
	public Boolean Enabled = true;
	public Boolean UseTrafficInformation = false;
	public Envelope BBox;
	
	public Integer[] GetPreferences()
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		String[] elements = Preferences.split("\\s*,\\s*");
		
		for (int i = 0; i< elements.length; i++)
		{
			int prefType = (int)RoutePreferenceType.getFromString(elements[i]);
			
			if (prefType != (int)RoutePreferenceType.UNKNOWN)
			{
				list.add(prefType);
			}
		}
		
		return (Integer[])list.toArray(new Integer[list.size()]);
	}
}