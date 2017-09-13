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
import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import heigit.ors.util.FormatUtility;
import heigit.ors.util.GeomUtility;
import heigit.ors.util.UnitsConverter;
import heigit.ors.common.Pair;

public class IsochronesIntersection {
	private Geometry _geometry;
	private Envelope _envelope;
	private double _area = 0.0;
	private List<Pair<Integer, Integer>> _contourRefs;

	public IsochronesIntersection(Geometry geometry)
	{
		_geometry = geometry;
		_contourRefs = new ArrayList<Pair<Integer, Integer>>();
	}

	public List<Pair<Integer, Integer>> getContourRefs()
	{
		return _contourRefs;
	}

	public void addContourRefs(Pair<Integer, Integer> ref)
	{
		_contourRefs.add(ref);
	}

	public void addContourRefs(Collection<Pair<Integer, Integer>> refs)
	{
		_contourRefs.addAll(refs);
	}

	public Geometry getGeometry()
	{
		return _geometry;
	}

	public double getArea(String units) throws Exception
	{
		double area = getArea(true);

		if (units != null)
		{
			switch(units)
			{
			case "m":
				return area;
			case "mi":
				return UnitsConverter.SqMetersToSqMiles(_area);
			case "km":
				return UnitsConverter.SqMetersToSqKilometers(_area); 
			}
		}

		return area;
	}

	public double getArea(Boolean inMeters) throws Exception {
		if (_area == 0.0) {
			_area = FormatUtility.roundToDecimals(GeomUtility.getArea(_geometry, inMeters), 2);
		}

		return _area;
	}

	public boolean intersects(IsochronesIntersection other)
	{
		if (!getEnvelope().intersects(other.getEnvelope()))
			return false;

		return _geometry.intersects(other._geometry);
	}

	public Envelope getEnvelope()
	{
		if(_envelope == null)
			_envelope = _geometry.getEnvelopeInternal();

		return _envelope;
	}
}
