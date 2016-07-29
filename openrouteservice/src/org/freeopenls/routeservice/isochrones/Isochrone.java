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

package org.freeopenls.routeservice.isochrones;

import org.freeopenls.tools.FormatUtility;
import org.freeopenls.tools.GeomUtility;
import com.vividsolutions.jts.geom.Geometry;

public class Isochrone {
	private Geometry geometry;
	private double value;
	private double area = 0.0;

	public Isochrone(Geometry geometry, double value) {
		this.geometry = geometry;
		this.value = value;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public double getValue() {
		return value;
	}

	public double getArea(Boolean inMeters) throws Exception {
		if (area == 0.0) {
			area = FormatUtility.roundToDecimals(GeomUtility.getArea(geometry, inMeters), 2);
		}

		return area;
	}
}
