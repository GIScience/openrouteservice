package heigit.ors.routing.configuration;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.routing.RoutingProfileType;

public class RouteProfileConfiguration
{
	public String Profiles; // comma separated
	public String ConfigPath;   // path to Graphhopper's configuration file.
	public String GraphPath;
	public Boolean DynamicWeighting = false; // to store additional fields such as max_width, max_height, max_weight, etc.
	public Boolean SurfaceInformation = false;// to store way and surface type information
	public Boolean HillIndex = false;// to store way and surface type information
	public Double MaximumDistance;
	public Double MinimumDistance; 
	public Boolean Enabled = true;
	public Boolean UseTrafficInformation = false;
	public Envelope BBox;
	
	public Integer[] GetProfiles()
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		String[] elements = Profiles.split("\\s*,\\s*");
		
		for (int i = 0; i< elements.length; i++)
		{
			int profileType = (int)RoutingProfileType.getFromString(elements[i]);
			
			if (profileType != (int)RoutingProfileType.UNKNOWN)
			{
				list.add(profileType);
			}
		}
		
		return (Integer[])list.toArray(new Integer[list.size()]);
	}
}