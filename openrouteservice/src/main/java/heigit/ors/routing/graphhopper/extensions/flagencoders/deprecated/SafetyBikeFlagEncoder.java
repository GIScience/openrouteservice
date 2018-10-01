/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package heigit.ors.routing.graphhopper.extensions.flagencoders.deprecated;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import heigit.ors.routing.graphhopper.extensions.flagencoders.deprecated.exghoverwrite.ExGhORSBikeCommonFlagEncoder;

import java.util.TreeMap;

import static com.graphhopper.routing.util.PriorityCode.*;

public class SafetyBikeFlagEncoder extends ExGhORSBikeCommonFlagEncoder {
	public SafetyBikeFlagEncoder()
    {
        this(4, 2, 0, false);
    }
	
	public SafetyBikeFlagEncoder(PMap configuration)
    {
	      this(configuration.getInt("speed_bits", 4) + (configuration.getBool("consider_elevation", false) ? 1 : 0),
	    		  configuration.getDouble("speed_factor", 2),
	              configuration.getBool("turn_costs", false) ? 3 : 0,
	              configuration.getBool("consider_elevation", false));
	      
	      setBlockFords(false);
    }

    public SafetyBikeFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts, boolean considerElevation)
    {
    	super(speedBits, speedFactor, maxTurnCosts, considerElevation);
    	
		addPushingSection("path");
		addPushingSection("footway");
		addPushingSection("pedestrian");
		addPushingSection("steps");
	        
		preferHighwayTags.add("service");
		preferHighwayTags.add("road");
		preferHighwayTags.add("tertiary");
		preferHighwayTags.add("tertiary_link");
		preferHighwayTags.add("residential");
		preferHighwayTags.add("unclassified");
		
		avoidHighwayTags.clear();
		avoidHighwayTags.add("motorway");
		avoidHighwayTags.add("motorway_link");
		avoidHighwayTags.add("trunk");
		avoidHighwayTags.add("trunk_link");
		avoidHighwayTags.add("primary");
		avoidHighwayTags.add("primary_link");
	    avoidHighwayTags.add("secondary");
        avoidHighwayTags.add("secondary_link");

		setHighwaySpeed("unclassified", 14);
		 
		setHighwaySpeed("trunk", 14);
		setHighwaySpeed("trunk_link", 14);
		setHighwaySpeed("primary", 14);
		setHighwaySpeed("primary_link", 14);
		setHighwaySpeed("secondary", 14);
		setHighwaySpeed("secondary_link", 14);
		setHighwaySpeed("tertiary", 14);
		setHighwaySpeed("tertiary_link", 14);
		
		init();
	}

    @Override
    public boolean isPushingSection(ReaderWay way )
    {
        String highway = way.getTag("highway");
        String trackType = way.getTag("tracktype");
        return way.hasTag("highway", pushingSectionsHighways)  || way.hasTag("railway", "platform")  || way.hasTag("route", ferries)
                || "track".equals(highway) && trackType != null && !"grade1".equals(trackType);
    }
    

	@Override
	protected void collect(ReaderWay way, double wayTypeSpeed, TreeMap<Double, Integer> weightToPrioMap) {
		String service = way.getTag("service");
		String highway = way.getTag("highway");
		
		double maxSpeed = getMaxSpeed(way);

		if (way.hasTag("bicycle", "designated"))
			weightToPrioMap.put(100d, PriorityCode.PREFER.getValue());
		if ("cycleway".equals(highway))
			weightToPrioMap.put(100d, PriorityCode.VERY_NICE.getValue());

		String cycleway = getCycleway(way); // Runge
		if (!Helper.isEmpty(cycleway) && (cycleway.equals("track") || cycleway.equals("lane")))
		{
			if (maxSpeed <= 30)
				weightToPrioMap.put(40d, PriorityCode.PREFER.getValue());
			else if (maxSpeed > 30 && avoidSpeedLimit < 50)
				weightToPrioMap.put(40d, PriorityCode.UNCHANGED.getValue());
			else if (maxSpeed > 50 && maxSpeed < avoidSpeedLimit)
				weightToPrioMap.put(50d, AVOID_IF_POSSIBLE.getValue());
			else if (maxSpeed >= AVOID_AT_ALL_COSTS.getValue())
				weightToPrioMap.put(50d, REACH_DEST.getValue());
		}
		
		if (preferHighwayTags.contains(highway)) {
			if (!way.hasTag("cycleway", "opposite") || way.hasTag("hgv", "no") || maxSpeed <= 30)
			{
				if (maxSpeed >= avoidSpeedLimit) // Runge
					weightToPrioMap.put(55d, PriorityCode.AVOID_AT_ALL_COSTS.getValue());
				else if (maxSpeed >= 50 && avoidSpeedLimit <= 70)
					weightToPrioMap.put(40d, PriorityCode.AVOID_IF_POSSIBLE.getValue());
				else  if (maxSpeed > 30 && avoidSpeedLimit < 50)
					weightToPrioMap.put(40d, PriorityCode.UNCHANGED.getValue());
				else
					weightToPrioMap.put(40d, PREFER.getValue());
			}
			else
  			  weightToPrioMap.put(40d, PriorityCode.UNCHANGED.getValue());
						
			if (way.hasTag("tunnel", intendedValues))
				weightToPrioMap.put(40d, PriorityCode.UNCHANGED.getValue());
		}

		if (pushingSectionsHighways.contains(highway) || "parking_aisle".equals(service))
			weightToPrioMap.put(30d, PriorityCode.AVOID_IF_POSSIBLE.getValue());

		if (avoidHighwayTags.contains(highway) || maxSpeed > 50) {
			  weightToPrioMap.put(30d, PriorityCode.REACH_DEST.getValue());
			
			if (way.hasTag("tunnel", intendedValues))
				weightToPrioMap.put(30d, PriorityCode.AVOID_AT_ALL_COSTS.getValue());
		}

		if (way.hasTag("railway", "tram"))
			weightToPrioMap.put(30d, PriorityCode.AVOID_AT_ALL_COSTS.getValue());
	}

    @Override
	protected double getDownhillMaxSpeed()
	{
		return 30;
	}

	@Override
	public String toString() {
		return FlagEncoderNames.BIKE_SAFTY;
	}
}
