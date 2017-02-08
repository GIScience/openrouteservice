/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.util.distancefunctions;

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.util.distancefunctions.DistanceFunction;

public abstract class AbstractDistanceFunction implements DistanceFunction {

	protected final static double R = 6372797.560856;
	protected final static double R2 = 2 * R;
	protected final static double DEG_TO_RAD = 0.017453292519943295769236907684886;
	protected final static double DEG_TO_RAD_HALF = 0.017453292519943295769236907684886 / 2.0;

	public abstract double calcDistance(double lon0, double lat0, double lon1, double lat1);

	public double calcDistance(Coordinate c0, Coordinate c1) {
		return calcDistance(c0.x, c0.y, c1.x, c1.y);
	}
}
