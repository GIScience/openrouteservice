package heigit.ors.routing;

import com.graphhopper.reader.dem.ElevationProvider;

import heigit.ors.routing.graphhopper.extensions.reader.dem.ElevationProviderCache;

public class RoutingProfileLoadContext 
{
	// add here any other shared resources

	private ElevationProviderCache _elevProviders;
	
	public RoutingProfileLoadContext()
	{
		_elevProviders = new ElevationProviderCache();
	}
	
	public ElevationProvider getElevationProvider(String name, String location)
	{
		return _elevProviders.getProvider(name, location, null);
	}
	
	public void release()
	{
		_elevProviders.release();
	}
}
