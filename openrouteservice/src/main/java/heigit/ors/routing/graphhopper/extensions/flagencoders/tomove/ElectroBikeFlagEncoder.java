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
package heigit.ors.routing.graphhopper.extensions.flagencoders.tomove;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import heigit.ors.routing.graphhopper.extensions.flagencoders.SpeedLimitHandler;

import java.util.HashMap;
import java.util.Map;

public class ElectroBikeFlagEncoder extends BikeCommonFlagEncoder
{
	  
    public ElectroBikeFlagEncoder()
    {
        this(4, 2, 0, false);
    }

    public ElectroBikeFlagEncoder( String propertiesString )
    {
        this(new PMap(propertiesString));
    }

    public ElectroBikeFlagEncoder(PMap properties )
    {
        this((int) properties.getLong("speed_bits", 4) + (properties.getBool("consider_elevation", false) ? 1 : 0),
                properties.getLong("speed_factor", 2),
                properties.getBool("turn_costs", false) ? 1 : 0, properties.getBool("consider_elevation", false));
        this.properties = properties;
        this.setBlockFords(properties.getBool("block_fords", true));
    }
    
    public ElectroBikeFlagEncoder( int speedBits, double speedFactor, int maxTurnCosts)
    {
      this(speedBits, speedFactor, maxTurnCosts, false);
    }

    public ElectroBikeFlagEncoder( int speedBits, double speedFactor, int maxTurnCosts, boolean considerElevation)
    {
        super(speedBits, speedFactor, maxTurnCosts,considerElevation);
        
        Map<String, Integer> trackTypeSpeedMap = new HashMap<String, Integer>();
		trackTypeSpeedMap.put("grade1", 21); // paved
		trackTypeSpeedMap.put("grade2", 15); // now unpaved ...
		trackTypeSpeedMap.put("grade3", 9);
		trackTypeSpeedMap.put("grade4", 7);
		trackTypeSpeedMap.put("grade5", 4); // like sand/grass     

		Map<String, Integer> surfaceSpeedMap = new HashMap<String, Integer>();
		surfaceSpeedMap.put("paved", 21);
		surfaceSpeedMap.put("asphalt", 21);
		surfaceSpeedMap.put("cobblestone", 9);
		surfaceSpeedMap.put("cobblestone:flattened", 11);
		surfaceSpeedMap.put("sett", 11);
		surfaceSpeedMap.put("concrete", 21);
		surfaceSpeedMap.put("concrete:lanes", 18);
		surfaceSpeedMap.put("concrete:plates", 18);
		surfaceSpeedMap.put("paving_stones", 13);
		surfaceSpeedMap.put("paving_stones:30", 13);
		surfaceSpeedMap.put("unpaved", 15);
		surfaceSpeedMap.put("compacted", 17);
		surfaceSpeedMap.put("dirt", 11);
		surfaceSpeedMap.put("earth", 13);
		surfaceSpeedMap.put("fine_gravel", 19);
		surfaceSpeedMap.put("grass", 9);
		surfaceSpeedMap.put("grass_paver", 9);
		surfaceSpeedMap.put("gravel", 13);
		surfaceSpeedMap.put("ground", 13);
		surfaceSpeedMap.put("ice", PUSHING_SECTION_SPEED / 2);
		surfaceSpeedMap.put("metal", 11);
		surfaceSpeedMap.put("mud", 11);
		surfaceSpeedMap.put("pebblestone", 18);
		surfaceSpeedMap.put("salt", 7);
		surfaceSpeedMap.put("sand", 7);
		surfaceSpeedMap.put("wood", 7);

        Map<String, Integer> highwaySpeeds = new HashMap<String, Integer>();
        highwaySpeeds.put("living_street", 9);
        highwaySpeeds.put("steps", PUSHING_SECTION_SPEED/2);

        highwaySpeeds.put("cycleway", 21);
        highwaySpeeds.put("path", 13);
        highwaySpeeds.put("footway", 7);
        highwaySpeeds.put("pedestrian", 7);
        highwaySpeeds.put("road", 14);
        highwaySpeeds.put("track", 13);
        highwaySpeeds.put("service", 15);
        highwaySpeeds.put("unclassified", 18);
        highwaySpeeds.put("residential", 21);

        highwaySpeeds.put("trunk", 20);
        highwaySpeeds.put("trunk_link", 20);
        highwaySpeeds.put("primary", 21);
        highwaySpeeds.put("primary_link", 21);
        highwaySpeeds.put("secondary", 21);
        highwaySpeeds.put("secondary_link", 21);
        highwaySpeeds.put("tertiary", 21);
        highwaySpeeds.put("tertiary_link", 21);
        
        _speedLimitHandler = new SpeedLimitHandler(this.toString(), highwaySpeeds, surfaceSpeedMap, trackTypeSpeedMap);
        
        addPushingSection("path");
        addPushingSection("footway");
        addPushingSection("pedestrian");
        addPushingSection("steps");

        avoidHighwayTags.add("trunk");
        avoidHighwayTags.add("trunk_link");
        avoidHighwayTags.add("primary");
        avoidHighwayTags.add("primary_link");
        avoidHighwayTags.add("secondary");
        avoidHighwayTags.add("secondary_link");

        // preferHighwayTags.add("road");
        preferHighwayTags.add("service");
        preferHighwayTags.add("tertiary");
        preferHighwayTags.add("tertiary_link");
        preferHighwayTags.add("residential");
        preferHighwayTags.add("unclassified");

        absoluteBarriers.add("kissing_gate");
        setSpecificClassBicycle("touring");
        
        init();
    }

    @Override
    public int getVersion()
    {
        return 2;
    }

    @Override
    protected boolean isPushingSection(ReaderWay way )
    {
        String highway = way.getTag("highway");
        String trackType = way.getTag("tracktype");
        return way.hasTag("highway", pushingSectionsHighways)
                || way.hasTag("railway", "platform")  || way.hasTag("route", ferries)
                || "track".equals(highway) && trackType != null 
            	&&  !("grade1".equals(trackType) || "grade2".equals(trackType) || "grade3".equals(trackType)); // Runge
    }
    
    @Override
	protected double getDownhillMaxSpeed()
	{
		return 30;
	}
    
    protected double getGradientSpeed(double speed, int gradient)
	{
    	if (speed > 10)
    		return speed + getGradientSpeedChange(gradient);
    	else
    	{
    		double result = speed + getGradientSpeedChange(gradient);

    		// forbid high downhill speeds on surfaces with low speeds
    		if (result > speed)
    			return speed;
    		else
    			return result;
    	}
	}	
    
    private double getGradientSpeedChange(int gradient)
    {
    	if (gradient > 12)
    		gradient = 12;
    	else if (gradient < -12)
    		gradient = -12;
    	
    	return -0.28*gradient;
    }

    @Override
    public String toString()
    {
        return FlagEncoderNames.BIKE_ELECTRO;
    }
}
