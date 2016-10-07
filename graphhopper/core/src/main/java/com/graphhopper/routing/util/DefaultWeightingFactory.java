package com.graphhopper.routing.util;

public class DefaultWeightingFactory implements WeightingFactory {
	
	public Weighting createWeighting(WeightingMap weightingMap, double maxSpeed, FlagEncoder encoder, Object userState)
    {
	    String weighting = weightingMap.getWeighting().toLowerCase();

	     if ("fastest".equals(weighting))
	     {
	    	 if (encoder instanceof BikeCommonFlagEncoder)
	             return new PriorityWeighting(maxSpeed, (BikeCommonFlagEncoder) encoder);
	         else
	             return new FastestWeighting(maxSpeed, encoder);
	     }
	     return new ShortestWeighting(encoder);
    }
}
