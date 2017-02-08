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
package org.freeopenls.routeservice.traffic;

public class TrafficEventCategory {
	public static final int UNDEFINED = 0;
	public static final int NORMAL_TRAFFIC = 1;
	public static final int WARNING = 2;
	public static final int ROADWORKS = 3;
	public static final int PARTIALLY_CLOSED  = 4;
	public static final int SLOW_TRAFFIC = 5;
	public static final int STATIONARY_TRAFFIC = 6;
	public static final int COMPLETELY_CLOSED  = 7;
	
	public static String toString(int category)
	{
		switch(category)
		{
			case UNDEFINED:
				return "UNDEFINED";
			case WARNING:
				return "WARNING";
			case STATIONARY_TRAFFIC:
				return "STATIONARY_TRAFFIC";
			case SLOW_TRAFFIC:
				return "SLOW_TRAFFIC";
			case NORMAL_TRAFFIC:
				return "NORMAL_TRAFFIC";
			case ROADWORKS:
				return "ROADWORKS";
			case PARTIALLY_CLOSED:
				return "PARTIALLY_CLOSED";
			case COMPLETELY_CLOSED:
				return "COMPLETELY_CLOSED";
		}
		
		return null;
	}
}
