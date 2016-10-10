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

package org.freeopenls.routeservice.routing;

public class AvoidFeatureFlags {
	public static final int Highway = 1; // 1 << 0;
	public static final int Tollway = 2; // 1 << 1;
	public static final int Ferries = 4; // 1 << 2;
	public static final int UnpavedRoads = 8; // 1 << 3;
	public static final int Steps = 16; // 1 << 4;
	public static final int Tracks = 16; // 1 << 4;
	public static final int Tunnels = 32; // 1 << 5;
	public static final int PavedRoads = 64; // 1 << 6;
	public static final int Fords = 128; // 1 << 7;
	public static final int Bridges = 256; // does not work as it is greater than byte limit of 255.
	public static final int Borders = 512; 
	public static final int Hills = 1024;
}
