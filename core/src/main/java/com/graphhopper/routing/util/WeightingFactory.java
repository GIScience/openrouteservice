package com.graphhopper.routing.util;


public interface WeightingFactory {
	
	public Weighting createWeighting(String weighting, double maxSpeed, FlagEncoder encoder, Object userState);
}
