/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
