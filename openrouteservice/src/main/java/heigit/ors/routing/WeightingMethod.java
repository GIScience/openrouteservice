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
package heigit.ors.routing;

public class WeightingMethod {
	public static final int UNKNOWN = 0;
	public static final int FASTEST = 1;
	public static final int SHORTEST = 2;
	public static final int RECOMMENDED = 3;
	
	public static int getFromString(String method) {
		if ("fastest".equalsIgnoreCase(method)) {
			return WeightingMethod.FASTEST;
		} else if ("shortest".equalsIgnoreCase(method)) {
			return WeightingMethod.SHORTEST;
		} else if ("recommended".equalsIgnoreCase(method)) {
			return WeightingMethod.RECOMMENDED; 
		} 
		
		return WeightingMethod.UNKNOWN;
	}
	
	public static String getName(int profileType)
	{
		switch (profileType)
		{
		case FASTEST:
			return "fastest";
		case SHORTEST:
			return "shortest";
		case RECOMMENDED:
			return "recommended";
		}
		
		return "";
	}
}
