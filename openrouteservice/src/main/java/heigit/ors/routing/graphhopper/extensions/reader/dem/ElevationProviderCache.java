/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.routing.graphhopper.extensions.reader.dem;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.graphhopper.reader.dem.CGIARProvider;
import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.reader.dem.SRTMProvider;
import com.graphhopper.storage.DAType;
import com.graphhopper.util.Helper;

import heigit.ors.util.HashUtility;

public class ElevationProviderCache 
{
	private Map<Integer, ElevationProvider> _cache;
	private Object _lockObj;

	public ElevationProviderCache()
	{
		_cache = new HashMap<Integer, ElevationProvider>();
		_lockObj = new Object();
	}

	public ElevationProvider getProvider(String name, String cacheDir, int threads, String dataAccessType, boolean clearCache)
	{
		ElevationProvider provider = ElevationProvider.NOOP;

		synchronized (_lockObj) 
		{
			int hash = HashUtility.getHashCode(name, cacheDir);
			
			if (!Helper.isEmpty(dataAccessType))
				hash = HashUtility.getHashCode(hash, dataAccessType);

			if (!_cache.containsKey(hash))
			{
				if (name.equalsIgnoreCase("srtm"))
				{
					provider = new SRTMProvider();
				} 
				else if (name.equalsIgnoreCase("cgiar"))
				{
					CGIARProvider cgiarProvider = new CGIARProvider();
					cgiarProvider.setAutoRemoveTemporaryFiles(clearCache);
					provider = cgiarProvider;
				}

				provider.setCacheDir(new File(cacheDir));
				if (!Helper.isEmpty(dataAccessType))
					provider.setDAType(DAType.fromString(dataAccessType));
				
				//if (threads > 1)
				// always wrap provider with SyncronizedElevationProvider, otherwise an exception will be thrown because OSMReader calls release method after completing first profile.
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
