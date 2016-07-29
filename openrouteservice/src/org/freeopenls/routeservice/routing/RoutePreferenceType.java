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

public class RoutePreferenceType {
	public static final int CAR = 1;

	// Field descriptor #68 I
	public static final int NONMOTORIZED = 2;

	// Field descriptor #68 I
	public static final int MOTORBIKE = 3;

	// Field descriptor #68 I
	public static final int X_4_WD = 4;

	// Field descriptor #68 I
	public static final int HEAVY_VEHICLE = 5;

	// Field descriptor #68 I
	public static final int TRUCKTRAILER = 6;

	// Field descriptor #68 I
	public static final int PEDESTRIAN = 7;

	// Field descriptor #68 I
	public static final int BICYCLE = 8;

	// Field descriptor #68 I
	public static final int BICYCLE_MTB = 9;

	// Field descriptor #68 I
	public static final int BICYCLE_RACER = 10;

	// Field descriptor #68 I
	public static final int BICYCLE_SAFETY = 11;

	// Field descriptor #68 I
	public static final int BICYCLE_TOUR = 12;

	public static final int WHEELCHAIR = 13;

	public static final int ELECTRO_VEHICLE = 14;
	
	public static final int CAR_TMC = 15;

	public static final int UNKNOWN = 16;


	public static boolean isCar(int routePref) {
		if (routePref == RoutePreferenceType.CAR
				|| routePref == RoutePreferenceType.HEAVY_VEHICLE || routePref == RoutePreferenceType.ELECTRO_VEHICLE
				|| routePref == RoutePreferenceType.X_4_WD || routePref == RoutePreferenceType.CAR_TMC)
			return true;
		else
			return false;
	}

	public static boolean isHeavyVehicle(int routePref) {
		if (routePref == RoutePreferenceType.HEAVY_VEHICLE || routePref == RoutePreferenceType.X_4_WD)
			return true;
		else
			return false;
	}
	
	public static boolean isBicycle(int routePref) {
		if (routePref == RoutePreferenceType.BICYCLE || routePref == RoutePreferenceType.BICYCLE_MTB
				|| routePref == RoutePreferenceType.BICYCLE_RACER || routePref == RoutePreferenceType.BICYCLE_TOUR || routePref == RoutePreferenceType.BICYCLE_SAFETY)
			return true;
		else
			return false;
	}
	
	public static boolean supportMessages(int routePref)
	{
		return isCar(routePref);
	}
	
	public static String getName(int pref)
	{
		switch (pref)
		{
			case CAR:
				return "Car";
			case PEDESTRIAN:
				return "Pedestrian";
			case BICYCLE:
				return "Bicycle";
			case BICYCLE_MTB:
				return "BicycleMTB";
			case BICYCLE_RACER:
				return "BicycleRacer";
			case BICYCLE_SAFETY:
				return "BicycleSafety";
			case BICYCLE_TOUR:
				return "BicycleTour"; 
			case WHEELCHAIR:
				return "Wheelchair";
			case HEAVY_VEHICLE:
				return "HeavyVehicle";
			default:
				return "Unknown";
		}
	}

	public static int getFromString(String prefType) {
		if ("Car".equalsIgnoreCase(prefType)) {
			return RoutePreferenceType.CAR;
		}  else if ("Pedestrian".equalsIgnoreCase(prefType)) {
			return RoutePreferenceType.PEDESTRIAN;
		} else if ("Bicycle".equalsIgnoreCase(prefType)) {
			return RoutePreferenceType.BICYCLE;
		} else if ("BicycleMTB".equalsIgnoreCase(prefType)) {
			return RoutePreferenceType.BICYCLE_MTB;
		} else if ("BicycleRacer".equalsIgnoreCase(prefType)) {
			return RoutePreferenceType.BICYCLE_RACER;
		} else if ("BicycleSafety".equalsIgnoreCase(prefType)) {
			return RoutePreferenceType.BICYCLE_SAFETY;
		} else if ("BicycleTour".equalsIgnoreCase(prefType)) {
			return RoutePreferenceType.BICYCLE_TOUR;
		} else if ("MotorBike".equalsIgnoreCase(prefType)) {
			return RoutePreferenceType.MOTORBIKE;
		} else if ("Wheelchair".equalsIgnoreCase(prefType)) {
			return RoutePreferenceType.WHEELCHAIR;
		} else if ("X_4_WD".equalsIgnoreCase(prefType)) {
			return RoutePreferenceType.X_4_WD;
		} else if ("HeavyVehicle".equalsIgnoreCase(prefType)) {
			return RoutePreferenceType.HEAVY_VEHICLE;
	    }
		else if  ("Car_TMC".equalsIgnoreCase(prefType)) {
			return RoutePreferenceType.CAR_TMC;
		}

		return RoutePreferenceType.UNKNOWN;
	}
}
