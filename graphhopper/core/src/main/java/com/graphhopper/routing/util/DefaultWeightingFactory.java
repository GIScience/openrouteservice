package com.graphhopper.routing.util;

public class DefaultWeightingFactory implements WeightingFactory {
	
	public Weighting createWeighting(String weighting, double maxSpeed, FlagEncoder encoder, Object userState)
    {
		 weighting = weighting.toLowerCase();
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
