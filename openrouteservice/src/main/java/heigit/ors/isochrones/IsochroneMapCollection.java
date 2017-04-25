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
}
