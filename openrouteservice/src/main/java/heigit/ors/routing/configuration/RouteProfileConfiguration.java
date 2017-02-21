package heigit.ors.routing.configuration;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.routing.RoutingProfileType;

public class RouteProfileConfiguration
{
	public Boolean Enabled = true;

	public String Profiles; // comma separated
	public String GraphPath;
	
	public String ExtStorages;
	
	public Double MaximumDistance = 0.0;
	public Double MinimumDistance = 0.0; 
	public Boolean UseTrafficInformation = false;
	
	public Boolean Instructions = true;
	
	public int EncoderFlagsSize = 4;
	public String EncoderOptions = null;
	public String CHWeighting = null;
	
	public String ElevationCachPath = null;
	public String ElevationProvider = null;
	
	public Envelope BBox;
	
	public Integer[] GetProfilesTypes()
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
	
	public RouteProfileConfiguration clone()
	{
		RouteProfileConfiguration rpc = new RouteProfileConfiguration();
		
		rpc.Enabled = this.Enabled;
		rpc.Profiles = this.Profiles;
			
		rpc.MaximumDistance = this.MaximumDistance;
		rpc.MinimumDistance = this.MinimumDistance; 
		rpc.UseTrafficInformation = this.UseTrafficInformation;
		
		rpc.Instructions = this.Instructions;
		
		rpc.EncoderFlagsSize = this.EncoderFlagsSize;
		rpc.EncoderOptions = this.EncoderOptions;
		rpc.CHWeighting = this.CHWeighting;

		rpc.ExtStorages = this.ExtStorages;

		rpc.ElevationCachPath = this.ElevationCachPath;
		rpc.ElevationProvider = this.ElevationProvider;
		
		rpc.BBox = this.BBox;
		
		return rpc;
	}
}