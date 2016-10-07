package com.graphhopper.routing.util;


public interface WeightingFactory {
	
	public Weighting createWeighting(WeightingMap weightingMap, double maxSpeed, FlagEncoder encoder, Object userState);
}
