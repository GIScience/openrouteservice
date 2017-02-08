package org.freeopenls.routeservice.routing;

public class WeightingMethod {
	public static final int FASTEST = 1;
	public static final int SHORTEST = 2;
	public static final int RECOMMENDED = 3;
	
	public static int getFromString(String prefType) {
		if ("Fastest".equalsIgnoreCase(prefType)) {
			return WeightingMethod.FASTEST;
		} else if ("Shortest".equalsIgnoreCase(prefType)) {
			return WeightingMethod.SHORTEST;
		} else if ("Recommended".equalsIgnoreCase(prefType)) {
			return WeightingMethod.RECOMMENDED; 
		} 
		
		return WeightingMethod.FASTEST;
	}
}
