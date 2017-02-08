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

public class SphericalCosinesFunction extends AbstractDistanceFunction {

	public double calcDistance(double lon0, double lat0, double lon1, double lat1) {
		lat0 = DEG_TO_RAD * lat0;
		lat1 = DEG_TO_RAD * lat1;

		return R
				* Math.acos(Math.sin(lat0) * Math.sin(lat1) + Math.cos(lat0) * Math.cos(lat1)
						* Math.cos(DEG_TO_RAD * (lon1 - lon0)));
	}
}