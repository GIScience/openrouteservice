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

public class AvoidFeatureFlags {
	public static final int Highways = 1; // 1 << 0;
	public static final int Tollways = 2; // 1 << 1;
	public static final int Steps = 2; // 1 << 1;
	public static final int Ferries = 4; // 1 << 2;
	public static final int UnpavedRoads = 8; // 1 << 3;
	public static final int Tracks = 16; // 1 << 4;
	public static final int Tunnels = 32; // 1 << 5;
	public static final int PavedRoads = 64; // 1 << 6;
	public static final int Fords = 128; // 1 << 7;
	
	public static final int Bridges = 256; // does not work as it is greater than byte limit of 255.
	public static final int Borders = 512; 
	public static final int Hills = 1024;
	
	public static int getFromString(String value)
	{
		switch(value.toLowerCase())
		{
			case "highways":
				return Highways;
			case "tollways":
				return Tollways;
			case "ferries":
				return Ferries;
			case "unpavedroads":
				return UnpavedRoads;
			case "steps":
				return Steps;
			case "tracks":
				return Tracks;
			case "tunnels":
				return Tunnels;
			case "pavedroads":
				return PavedRoads;
			case "fords":
				return Fords;
			case "bridges":
				return Bridges;
			case "borders":
				return Borders;
			case "hills":
				return Hills;
		}
		
		return 0;
	}
	
	public static boolean isValid(int profileType, int value)
	{
		if (RoutingProfileType.isDriving(profileType))
		{
			if (value == Steps)
				return false;
		}
		else if (RoutingProfileType.isCycling(profileType) || RoutingProfileType.isWalking(profileType))
		{
			if (value == Highways || value == Tollways || value == Tunnels)
				return false;
		}
		
		return true;
	}
}
