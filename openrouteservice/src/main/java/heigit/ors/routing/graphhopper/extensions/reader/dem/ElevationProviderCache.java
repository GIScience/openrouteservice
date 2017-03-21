package heigit.ors.routing.graphhopper.extensions.reader.dem;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.graphhopper.reader.dem.CGIARProvider;
import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.reader.dem.SRTMProvider;

import heigit.ors.util.HashUtility;

public class ElevationProviderCache {
	private Map<Integer, ElevationProvider> _cache;
	private Object _lockObj;

	public ElevationProviderCache()
	{
		_cache = new HashMap<Integer, ElevationProvider>();
		_lockObj = new Object();
	}

	public ElevationProvider getProvider(String name, String cacheDir, String[] args)
	{
		ElevationProvider provider = ElevationProvider.NOOP;

		synchronized (_lockObj) 
		{
			int hash = HashUtility.getHashCode(name, cacheDir);

			if (!_cache.containsKey(hash))
			{
				if (name.equalsIgnoreCase("srtm"))
				{
					provider = new SRTMProvider();
				} 
				else if (name.equalsIgnoreCase("cgiar"))
				{
					CGIARProvider cgiarProvider = new CGIARProvider();
					// TODO args
					//cgiarProvider.setAutoRemoveTemporaryFiles(args.getBool("graph.elevation.cgiar.clear", true));
					provider = cgiarProvider;
				}
				
				provider.setCacheDir(new File(cacheDir));
				provider = new SyncronizedElevationProvider(provider);
				_cache.put(hash, provider);
			}
			else
				provider = _cache.get(hash);
		}

		return provider;
	}

	public void release()
	{
		for(Map.Entry<Integer, ElevationProvider> entry : _cache.entrySet())
		{
			ElevationProvider provider = entry.getValue();
			if (provider instanceof SyncronizedElevationProvider)
				((SyncronizedElevationProvider)provider).release(true);
			else
				provider.release();
		}

		_cache.clear();
	}
}
