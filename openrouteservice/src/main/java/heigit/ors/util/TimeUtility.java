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
package heigit.ors.util;

public final class TimeUtility {

	public static String getElapsedTime(long startTime, boolean addSeconds) {
		return getElapsedTime(startTime, System.currentTimeMillis(), addSeconds);
	}
	
	public static String getElapsedTime(long startTime, long endTime, boolean addSeconds) {
		long time = endTime - startTime;
		double handlingTimeSeconds = (double) time / 1000;

		String res = Double.toString(handlingTimeSeconds).replace(",", ".");

		if (addSeconds)
			res += "s";

		return res;
	}
}
