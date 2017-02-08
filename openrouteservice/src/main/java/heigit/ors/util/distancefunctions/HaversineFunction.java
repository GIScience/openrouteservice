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

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class HaversineFunction extends AbstractDistanceFunction {

	public double calcDistance(double lon0, double lat0, double lon1, double lat1) {
		// return CoordTools.calculateLengthWGS84(lon0, lat0, lon1, lat1);
		double sinDLat = sin(DEG_TO_RAD_HALF * (lat1 - lat0));
		double sinDLon = sin(DEG_TO_RAD_HALF * (lon1 - lon0));
		double c = sinDLat * sinDLat + sinDLon * sinDLon * cos(DEG_TO_RAD * (lat0)) * cos(DEG_TO_RAD * (lat1));

		return R2 * asin(sqrt(c));
	}
}