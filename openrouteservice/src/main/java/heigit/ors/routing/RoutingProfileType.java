/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing;

public class RoutingProfileType {
	public static final int UNKNOWN = 0;

	public static final int DRIVING_CAR = 1;
	public static final int DRIVING_ELECTRIC_CAR = 2;
	public static final int DRIVING_HGV = 3;
	public static final int DRIVING_MOTORCYCLE = 4;
	public static final int DRIVING_CAROFFROAD = 5; // not supported
	public static final int DRIVING_SEGWAY = 6; // not supported
	public static final int DRIVING_EMERGENCY = 7; 
	
	public static final int DRIVING_TRAFFIC = 9;

	public static final int CYCLING_REGULAR = 10;
	public static final int CYCLING_ROAD = 11;
	public static final int CYCLING_MOUNTAIN = 12;
	public static final int CYCLING_ELECTRIC = 13;
	public static final int CYCLING_SAFE = 14;
	public static final int CYCLING_TOUR = 15;
	public static final int CYCLING_MOTOR = 16;

	public static final int FOOT_WALKING = 20;
	public static final int FOOT_HIKING = 21;
	public static final int FOOT_JOGGING = 22; // not supported

	public static final int WHEELCHAIR = 30;
	
	

	public static boolean isDriving(int routePref) {
		if (routePref == DRIVING_CAR
				|| routePref == DRIVING_HGV || routePref == DRIVING_ELECTRIC_CAR || routePref == DRIVING_EMERGENCY
				|| routePref == DRIVING_MOTORCYCLE || routePref == DRIVING_CAROFFROAD)
			return true;
		else
			return false;
	}

	public static boolean isHeavyVehicle(int routePref) {
		if (routePref == DRIVING_HGV || routePref == DRIVING_CAROFFROAD || routePref  == DRIVING_EMERGENCY)
			return true;
		else
			return false;
	}

	public static boolean isWalking(int routePref) {
	  return (routePref == FOOT_WALKING || routePref == FOOT_HIKING || routePref == FOOT_JOGGING);
	}
	
	public static boolean isCycling(int routePref) {
		if (routePref == CYCLING_REGULAR || routePref == CYCLING_MOUNTAIN
				|| routePref == CYCLING_ROAD || routePref == CYCLING_TOUR || routePref == CYCLING_SAFE
				|| routePref == CYCLING_ELECTRIC)
			return true;
		else
			return false;
	}

	public static boolean supportMessages(int profileType)
	{
		return isDriving(profileType);
	}

	public static String getName(int profileType)
	{
		switch (profileType)
		{
		case DRIVING_CAR:
			return "driving-car";
		case DRIVING_HGV:
			return "driving-hgv";
		case DRIVING_ELECTRIC_CAR:
			return "driving-ecar";
		case DRIVING_MOTORCYCLE:
			return "driving-motorcycle";
		case DRIVING_EMERGENCY:
			return "driving-emergency";

		case CYCLING_REGULAR:
			return "cycling-regular";
		case CYCLING_ROAD:
			return "cycling-road";
		case CYCLING_MOUNTAIN:
			return "cycling-mountain";
		case CYCLING_ELECTRIC:
			return "cycling-electric";
		case CYCLING_TOUR:
			return "cycling-tour";
		case CYCLING_SAFE:
			return "cycling-safe";

		case FOOT_WALKING:
			return "foot-walking";
		case FOOT_HIKING:
			return "foot-hiking";
		case FOOT_JOGGING:
			return "foot-jogging";

		case WHEELCHAIR:
			return "wheelchair";
		default:
			return "Unknown";
		}
	}

	public static int getFromString(String profileType) {
		if ("driving-car".equalsIgnoreCase(profileType)) 
			return DRIVING_CAR;
		if ("driving-ecar".equalsIgnoreCase(profileType)) 
			return DRIVING_ELECTRIC_CAR;
		else if ("driving-hgv".equalsIgnoreCase(profileType)) 
			return DRIVING_HGV;
		else if ("driving-motorcycle".equalsIgnoreCase(profileType)) 
			return DRIVING_MOTORCYCLE;
		else if ("driving-traffic".equalsIgnoreCase(profileType)) 
			return DRIVING_TRAFFIC;
		else if ("driving-emergency".equalsIgnoreCase(profileType)) 
			return DRIVING_EMERGENCY;

		else if ("cycling-regular".equalsIgnoreCase(profileType)) 
			return CYCLING_REGULAR;
		else if ("cycling-road".equalsIgnoreCase(profileType)) 
			return CYCLING_ROAD;
		else if ("cycling-mountain".equalsIgnoreCase(profileType)) 
			return CYCLING_MOUNTAIN;
		else if ("cycling-safe".equalsIgnoreCase(profileType)) 
			return CYCLING_SAFE;	
		else if ("cycling-tour".equalsIgnoreCase(profileType)) 
			return CYCLING_TOUR;	
		else if ("cycling-electric".equalsIgnoreCase(profileType)) 
			return CYCLING_ELECTRIC;

		else if ("foot-walking".equalsIgnoreCase(profileType)) 
			return FOOT_WALKING;
		else if ("foot-hiking".equalsIgnoreCase(profileType)) 
			return FOOT_HIKING;		
		else if ("foot-jogging".equalsIgnoreCase(profileType)) 
			return FOOT_JOGGING;
		
		else if ("wheelchair".equalsIgnoreCase(profileType)) 
				return WHEELCHAIR;
		return UNKNOWN;
	} 

	public static String getEncoderName(int routePref) {
		if (routePref == RoutingProfileType.DRIVING_CAR)
			return "CAR";
		else if (routePref == RoutingProfileType.DRIVING_TRAFFIC)
			return "CARTMC";
		else if (routePref == RoutingProfileType.DRIVING_MOTORCYCLE) 
			return "MOTORCYCLE";
		else if (routePref == RoutingProfileType.DRIVING_ELECTRIC_CAR)  
			return "EVEHICLE";
		else if (routePref == RoutingProfileType.DRIVING_HGV)  
			return "HEAVYVEHICLE";
		else if (routePref == RoutingProfileType.DRIVING_CAROFFROAD)  
			return "CAROFFROAD";
		else if (routePref == RoutingProfileType.DRIVING_EMERGENCY)  
			return "EMERGENCY";
		else if (routePref == RoutingProfileType.FOOT_WALKING)
			return "FOOT";
		else if (routePref == RoutingProfileType.FOOT_HIKING)
			return "HIKING";
		else if (routePref == RoutingProfileType.FOOT_HIKING)
			return "RUNNING";
		else if (routePref == RoutingProfileType.CYCLING_REGULAR)
			return "BIKE";
		else if (routePref == RoutingProfileType.CYCLING_MOUNTAIN)
			return "MTB";
		else if (routePref == RoutingProfileType.CYCLING_ROAD)
			return "RACINGBIKE";
		else if (routePref == RoutingProfileType.CYCLING_TOUR) // custom
			return "CYCLETOURBIKE";
		else if (routePref == RoutingProfileType.CYCLING_SAFE) // custom
			return "SAFETYBIKE";
		else if (routePref == RoutingProfileType.CYCLING_ELECTRIC) // custom
			return "ELECTROBIKE";
		else if (routePref == RoutingProfileType.WHEELCHAIR) // custom
			return "WHEELCHAIR";

		return "UNKNOWN";
	}
	
	public static int getFromEncoderName(String encoder) {
		if ("CAR".equalsIgnoreCase(encoder))
			return RoutingProfileType.DRIVING_CAR;
		else if ("CARTMC".equalsIgnoreCase(encoder))
			return RoutingProfileType.DRIVING_TRAFFIC;
		else if ("MOTORCYCLE".equalsIgnoreCase(encoder))
			return RoutingProfileType.DRIVING_MOTORCYCLE;
		else if ("EVEHICLE".equalsIgnoreCase(encoder))
			return RoutingProfileType.DRIVING_ELECTRIC_CAR;
		else if ("HEAVYVEHICLE".equalsIgnoreCase(encoder))
			return RoutingProfileType.DRIVING_HGV;
		else if ("CAROFFROAD".equalsIgnoreCase(encoder))
			return RoutingProfileType.DRIVING_CAROFFROAD;
		else if ("FOOT".equalsIgnoreCase(encoder))
			return RoutingProfileType.FOOT_WALKING;
		else if ("HIKING".equalsIgnoreCase(encoder))
			return RoutingProfileType.FOOT_HIKING;
		else if ("RUNNING".equalsIgnoreCase(encoder))
			return RoutingProfileType.FOOT_JOGGING;
		else if ("BIKE".equalsIgnoreCase(encoder))
			return RoutingProfileType.CYCLING_REGULAR;
		else if ("MTB".equalsIgnoreCase(encoder))
			return RoutingProfileType.CYCLING_MOUNTAIN;
		else if ("RACINGBIKE".equalsIgnoreCase(encoder))
			return RoutingProfileType.CYCLING_ROAD;
		else if ("CYCLETOURBIKE".equalsIgnoreCase(encoder))
			return RoutingProfileType.CYCLING_TOUR;
		else if ("SAFETYBIKE".equalsIgnoreCase(encoder))
			return RoutingProfileType.CYCLING_SAFE;
		else if ("ELECTROBIKE".equalsIgnoreCase(encoder))
			return RoutingProfileType.CYCLING_ELECTRIC;
		else if ("WHEELCHAIR".equalsIgnoreCase(encoder))
			return RoutingProfileType.WHEELCHAIR;

		return RoutingProfileType.UNKNOWN;
	}
}
