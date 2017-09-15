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
import java.util.concurrent.locks.StampedLock;

import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.reader.dem.HeightTile;
import com.graphhopper.storage.DAType;

public class SyncronizedElevationProvider implements ElevationProvider 
{
	private final StampedLock _lock = new StampedLock();
	private ElevationProvider _elevProvider;

	public SyncronizedElevationProvider(ElevationProvider elevProvider)
	{
		_elevProvider = elevProvider;
	}

	@Override
	public HeightTile loadTile(double lat, double lon) {
		HeightTile dem = null;
		
		long stamp = _lock.writeLock();
		
        try
        {
        	dem = _elevProvider.loadTile(lat, lon);
        }
        finally
        {
        	_lock.unlockWrite(stamp);
        }
		
		return dem;
	}

	@Override
	public double getEle(double lat, double lon) {
		HeightTile dem = null;
		int tileKey;
		
		long stamp = _lock.readLock();
		
		try
		{
			tileKey = getTileKey(lat, lon);
			dem = getTile(tileKey);
        }
		finally
		{
            _lock.unlockRead(stamp);
        }
		
		if (dem == null)
			dem = loadTile(lat, lon);
		
		if (dem == null)
			return 0;

		if (dem.isSeaLevel())
			return 0;

		return dem.getHeight(lat, lon);
	}

	@Override
	public ElevationProvider setBaseURL(String baseURL) {
		return _elevProvider.setBaseURL(baseURL);
	}

	@Override
	public ElevationProvider setCacheDir(File cacheDir) {

		return _elevProvider.setCacheDir(cacheDir);
	}

	@Override
	public ElevationProvider setDAType(DAType daType) {
		return _elevProvider.setDAType(daType);
	}

	@Override
	public void setCalcMean(boolean calcMean) {
		_elevProvider.setCalcMean(calcMean);
	}

	public void release(boolean disposeInternal) {
	  if (disposeInternal)
		  _elevProvider.release();
	}
	
	@Override
	public void release() {
		// omit calling release method of the internal provider since it is used by OSMReader
		//_elevProvider.release();		
	}

	@Override
	public int getTileKey(double lat, double lon) {
		return _elevProvider.getTileKey(lat, lon);
	}

	@Override
	public HeightTile getTile(int key) {
		return _elevProvider.getTile(key);
	}
}
