package heigit.ors.routing;

import com.graphhopper.reader.dem.ElevationProvider;

import heigit.ors.routing.graphhopper.extensions.reader.dem.ElevationProviderCache;

public class RoutingProfileLoadContext 
{
	// add here any other shared resources
	private int _threads = 1;

	private ElevationProviderCache _elevProviders;
	
	public RoutingProfileLoadContext()
	{
		this(1);
	}
	
	public RoutingProfileLoadContext(int threads)
	{
		_threads = threads;
		_elevProviders = new ElevationProviderCache();
	}
	
	public ElevationProvider getElevationProvider(String name, String location, String dataAccessType, boolean clearCache)
	{
		return _elevProviders.getProvider(name, location, _threads, dataAccessType, clearCache);
	}
	
	public void release()
	{
		_elevProviders.release();
	}
}
