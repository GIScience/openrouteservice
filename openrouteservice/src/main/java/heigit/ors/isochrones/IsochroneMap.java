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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class IsochroneMap {
	private Envelope _envelope;
	private List<Isochrone> _isochrones;
	private Coordinate _center;
	
	public IsochroneMap(Coordinate center)
	{
		this._center = center;
		this._isochrones = new ArrayList<Isochrone>();
		this._envelope = new Envelope();
	}

	public boolean isEmpty()
	{
		return _isochrones.size() == 0;
	}

	public Coordinate getCenter() 
	{
		return _center;
	}

	public Iterable<Isochrone> getIsochrones()
	{
		return _isochrones;
	}
	
	public int getIsochronesCount()
	{
		return _isochrones.size();
	}

	public Isochrone getIsochrone(int index)
	{
		return _isochrones.get(index);
	}

	public void addIsochrone(Isochrone isochrone)
	{
		_isochrones.add(isochrone);
		_envelope.expandToInclude(isochrone.getGeometry().getEnvelopeInternal());
	}

	public Envelope getEnvelope()
	{
		return _envelope;
	}
}
