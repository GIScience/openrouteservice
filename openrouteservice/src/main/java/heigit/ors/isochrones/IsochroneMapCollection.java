/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
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
