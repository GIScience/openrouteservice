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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import heigit.ors.util.FormatUtility;
import heigit.ors.util.GeomUtility;
import heigit.ors.util.UnitsConverter;

public class Isochrone {
	private Geometry geometry;
	private double value;
	private double area = 0.0;
	private double maxRadius;
	private Envelope envelope;

	public Isochrone(Geometry geometry, double value, double maxRadius) {
		this.geometry = geometry;
		this.value = value;
		this.maxRadius = maxRadius;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public double getValue() {
		return value;
	}

	public double getMaxRadius(String units)
	{
		if (units != null)
		{
			switch(units)
			{
			case "m":
				return maxRadius;
			case "mi":
				return UnitsConverter.SqMetersToSqMiles(maxRadius);
			case "km":
				return UnitsConverter.SqMetersToSqKilometers(maxRadius); 
			}
		}

		return maxRadius;
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
				return UnitsConverter.SqMetersToSqMiles(area);
			case "km":
				return UnitsConverter.SqMetersToSqKilometers(area); 
			}
		}

		return area;
	}

	public double getArea(Boolean inMeters) throws Exception {
		if (area == 0.0) {
			area = FormatUtility.roundToDecimals(GeomUtility.getArea(geometry, inMeters), 2);
		}

		return area;
	}

	public Envelope getEnvelope() {
		if (envelope == null)
			envelope = geometry.getEnvelopeInternal();

		return envelope;
	}
}
