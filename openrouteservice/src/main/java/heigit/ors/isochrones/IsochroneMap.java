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

// Authors: M. Rylov 

package heigit.ors.isochrones;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class IsochroneMap {
	private Envelope envelope;
	private List<Isochrone> isochrones;
	private Coordinate center;
	
	public IsochroneMap(Coordinate center)
	{
		this.center = center;
		this.isochrones = new ArrayList<Isochrone>();
		this.envelope = new Envelope();
	}

	public boolean isEmpty()
	{
		return isochrones.size() == 0;
	}

	public Coordinate getCenter() 
	{
		return center;
	}

	public Iterable<Isochrone> getIsochrones()
	{
		return isochrones;
	}

	public Isochrone getIsochrone(int index)
	{
		return isochrones.get(index);
	}

	public void addIsochrone(Isochrone isochrone)
	{
		isochrones.add(isochrone);
		envelope.expandToInclude(isochrone.getGeometry().getEnvelopeInternal());
	}

	public Envelope getEnvelope()
	{
		return envelope;
	}
}
