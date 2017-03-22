package heigit.ors.routing.traffic;

import heigit.ors.routing.RoutingProfileType;

public class TmcMode {
	
	// currently only car and heavy vehicle are used
	public static final int CAR = RoutingProfileType.DRIVING_CAR;
	
	public static final int BUS = 2;
	
	public static final int LORRIES = 3;
	
	public static final int HIGH_SIDED_VEHICLE = 4;
	
	public static final int HEAVY_VEHICLE = RoutingProfileType.DRIVING_HGV;
	
	public static final int VEHICLE_WITH_TRAILER =  6;	
	
	// maybe more modes in the tmc messages
	
}