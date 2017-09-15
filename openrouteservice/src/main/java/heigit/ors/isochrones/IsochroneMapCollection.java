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
package heigit.ors.isochrones;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class IsochroneMapCollection {
	private int _nIsochrones = 0;
	private List<IsochroneMap> _isochroneMaps = new ArrayList<IsochroneMap>();
	
	public IsochroneMapCollection()
	{}
	
	public void add(IsochroneMap map)
	{
		_isochroneMaps.add(map);
		_nIsochrones += map.getIsochronesCount();
	}
	
	public Iterable<IsochroneMap> getIsochroneMaps()
	{
		return _isochroneMaps;
	}
	
	public IsochroneMap getIsochrone(int index)
	{
		return _isochroneMaps.get(index);
	}
	
	public int getIsochronesCount()
	{
		return _nIsochrones;
	}
	
	public int size()
	{
		return _isochroneMaps.size();
	}
	
	public Geometry computeIntersection()
	{
		if (_isochroneMaps.size() == 0)
			return null;
		
		if (_isochroneMaps.size() == 1)
			return _isochroneMaps.get(0).getIsochrone(0).getGeometry();
		
		Isochrone iso = _isochroneMaps.get(0).getIsochrone(0);
		Geometry geomIntersection = iso.getGeometry();
		Envelope envIntersection = iso.getEnvelope();
		
		for (int i = 1; i < _isochroneMaps.size(); ++i)
		{
			iso = _isochroneMaps.get(i).getIsochrone(0);
			if (envIntersection.intersects(iso.getEnvelope()))
			{
				geomIntersection = geomIntersection.intersection(iso.getGeometry());
				if (geomIntersection == null || geomIntersection.isEmpty())
					return null;
			}
			else
				return null;
		}
		
		return geomIntersection;
	}
}
