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

package org.freeopenls.routeservice.graphhopper.extensions.util;

public class ConvertUtils {

	public static int getTrackValue(String trackType) {
		if ("grade1".equals(trackType))
			return 1;
		else if ("grade2".equals(trackType))
			return 2;
		else if ("grade3".equals(trackType))
			return 3;
		else if ("grade4".equals(trackType))
			return 4;
		else if ("grade5".equals(trackType))
			return 5;
		else if ("grade6".equals(trackType))
			return 6;
		else if ("grade7".equals(trackType))
			return 7;
		else if ("grade8".equals(trackType))
			return 8;
		else
			return 0;
	}

	public static int getSmoothnessValue(String smoothness) {
		if ("excellent".equals(smoothness))
			return 1;
		else if ("good".equals(smoothness))
			return 2;
		else if ("intermediate".equals(smoothness))
			return 3;
		else if ("bad".equals(smoothness))
			return 4;
		else if ("very_bad".equals(smoothness))
			return 5;
		else if ("horrible".equals(smoothness))
			return 6;
		else if ("very_horrible".equals(smoothness))
			return 7;
		else if ("impassable".equals(smoothness))
			return 8;
		else
			return 0;
	}
	
	public static int getSurfaceValue(String surface) {
		if ("paved".equals(surface))
			return 1;
		else if ("asphalt".equals(surface))
			return 2;
		else if ("cobblestone".equals(surface))
			return 3;
		else if ("cobblestone:flattened".equals(surface))
			return 4;
		else if ("concrete".equals(surface))
			return 5;
		else if ("concrete:lanes".equals(surface))
			return 6;
		else if ("concrete:plates".equals(surface))
			return 7;
		else if ("paving_stones".equals(surface))
			return 8;
		else
			return 0;
	}
}
