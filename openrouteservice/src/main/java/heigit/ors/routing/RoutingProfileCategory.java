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
package heigit.ors.routing;

public class RoutingProfileCategory {
	public static final int UNKNOWN = 0;
	public static final int DRIVING = 1;
	public static final int CYCLING  =2;
	public static final int WALKING = 3;
	public static final int WHEELCHAIR = 4;
	
	public static int getFromRouteProfile(int profileType)
	{
		if (RoutingProfileType.isDriving(profileType))
			return RoutingProfileCategory.DRIVING;

		if (RoutingProfileType.isCycling(profileType))
			return RoutingProfileCategory.CYCLING;
		
		if (RoutingProfileType.isWalking(profileType))
			return RoutingProfileCategory.WALKING;
		
		if (RoutingProfileType.WHEELCHAIR == profileType)
			return RoutingProfileCategory.WHEELCHAIR;
		
		return RoutingProfileCategory.UNKNOWN;
	}
}
