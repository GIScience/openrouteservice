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
