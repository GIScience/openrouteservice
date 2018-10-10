/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper GmbH licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions.flagencoders.deprecated;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import heigit.ors.routing.graphhopper.extensions.flagencoders.SpeedLimitHandler;
import heigit.ors.routing.graphhopper.extensions.flagencoders.tomove.BikeCommonFlagEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.graphhopper.routing.util.PriorityCode.*;

/**
 * Specifies the settings for race biking
 * <p>
 *
 * @author ratrun
 * @author Peter Karich
 */
public class RacingBikeFlagEncoder extends BikeCommonFlagEncoder {
    public RacingBikeFlagEncoder() {
        this(4, 2, 0, false);
    }

    public RacingBikeFlagEncoder(PMap properties) {
        this(
                (int) properties.getLong("speed_bits", 4),
                properties.getDouble("speed_factor", 2),
                properties.getBool("turn_costs", false) ? 1 : 0, 
                properties.getBool("consider_elevation", false)
        );
        this.properties = properties;
        this.setBlockFords(properties.getBool("block_fords", true));
    }

    public RacingBikeFlagEncoder(String propertiesStr) {
        this(new PMap(propertiesStr));
    }

    public RacingBikeFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts, boolean considerElevation) {
        super(speedBits, speedFactor, maxTurnCosts, considerElevation);
        preferHighwayTags.add("road");
        preferHighwayTags.add("secondary");
        preferHighwayTags.add("secondary_link");
        preferHighwayTags.add("tertiary");
        preferHighwayTags.add("tertiary_link");
        preferHighwayTags.add("residential");

		Map<String, Integer> trackTypeSpeedMap = new HashMap<String, Integer>();
		trackTypeSpeedMap.put("grade1", 20); // paved
		trackTypeSpeedMap.put("grade2", 10); // now unpaved ...
		trackTypeSpeedMap.put("grade3", PUSHING_SECTION_SPEED);
		trackTypeSpeedMap.put("grade4", PUSHING_SECTION_SPEED);
		trackTypeSpeedMap.put("grade5", PUSHING_SECTION_SPEED);    
		
		Map<String, Integer> surfaceSpeedMap = new HashMap<String, Integer>();
		surfaceSpeedMap.put("paved", 20);
		surfaceSpeedMap.put("asphalt", 20);
		surfaceSpeedMap.put("cobblestone", 10);
		surfaceSpeedMap.put("cobblestone:flattened", 10);
		surfaceSpeedMap.put("sett", 10);
		surfaceSpeedMap.put("concrete", 20);
		surfaceSpeedMap.put("concrete:lanes", 16);
		surfaceSpeedMap.put("concrete:plates", 16);
		surfaceSpeedMap.put("paving_stones", 10);
		surfaceSpeedMap.put("paving_stones:30", 10);
	    surfaceSpeedMap.put("unpaved", PUSHING_SECTION_SPEED / 2);
	    surfaceSpeedMap.put("compacted", PUSHING_SECTION_SPEED / 2);
	    surfaceSpeedMap.put("dirt", PUSHING_SECTION_SPEED / 2);
	    surfaceSpeedMap.put("earth", PUSHING_SECTION_SPEED / 2);
	    surfaceSpeedMap.put("fine_gravel", PUSHING_SECTION_SPEED);
	    surfaceSpeedMap.put("grass", PUSHING_SECTION_SPEED / 2);
	    surfaceSpeedMap.put("grass_paver", PUSHING_SECTION_SPEED / 2);
        surfaceSpeedMap.put("gravel", PUSHING_SECTION_SPEED / 2);
        surfaceSpeedMap.put("ground", PUSHING_SECTION_SPEED / 2);
        surfaceSpeedMap.put("ice", PUSHING_SECTION_SPEED / 2);
        surfaceSpeedMap.put("metal", PUSHING_SECTION_SPEED / 2);
        surfaceSpeedMap.put("mud", PUSHING_SECTION_SPEED / 2);
        surfaceSpeedMap.put("pebblestone", PUSHING_SECTION_SPEED);
        surfaceSpeedMap.put("salt", PUSHING_SECTION_SPEED / 2);
        surfaceSpeedMap.put("sand", PUSHING_SECTION_SPEED / 2);
        surfaceSpeedMap.put("wood", PUSHING_SECTION_SPEED / 2);
        
		Map<String, Integer> highwaySpeeds = new HashMap<String, Integer>();
        highwaySpeeds.put("cycleway", 18);
        highwaySpeeds.put("path", 8);
        highwaySpeeds.put("footway", 6);
        highwaySpeeds.put("pedestrian", 6);
        highwaySpeeds.put("road", 12);
        highwaySpeeds.put("track", PUSHING_SECTION_SPEED / 2); // assume unpaved
        highwaySpeeds.put("service", 12);
        highwaySpeeds.put("unclassified", 16);
        highwaySpeeds.put("residential", 16);
        highwaySpeeds.put("living_street", 8);

        highwaySpeeds.put("trunk", 20);
        highwaySpeeds.put("trunk_link", 20);
        highwaySpeeds.put("primary", 20);
        highwaySpeeds.put("primary_link", 20);
        highwaySpeeds.put("secondary", 20);
        highwaySpeeds.put("secondary_link", 20);
        highwaySpeeds.put("tertiary", 20);
        highwaySpeeds.put("tertiary_link", 20);

        _speedLimitHandler = new SpeedLimitHandler(this.toString(), highwaySpeeds, surfaceSpeedMap, trackTypeSpeedMap);
        
        addPushingSection("path");
        addPushingSection("footway");
        addPushingSection("pedestrian");
        addPushingSection("steps");

        setCyclingNetworkPreference("icn", PriorityCode.BEST.getValue());
        setCyclingNetworkPreference("ncn", PriorityCode.BEST.getValue());
        setCyclingNetworkPreference("rcn", PriorityCode.VERY_NICE.getValue());
        setCyclingNetworkPreference("lcn", PriorityCode.UNCHANGED.getValue());
        setCyclingNetworkPreference("mtb", PriorityCode.UNCHANGED.getValue());

        absoluteBarriers.add("kissing_gate");

        setAvoidSpeedLimit(81);
        setSpecificClassBicycle("roadcycling");

        init();
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    protected void collect(ReaderWay way, double wayTypeSpeed, TreeMap<Double, Integer> weightToPrioMap) {
        super.collect(way, wayTypeSpeed, weightToPrioMap);

        String highway = way.getTag("highway");
        if ("service".equals(highway)) {
            weightToPrioMap.put(40d, UNCHANGED.getValue());
        }  else if ("track".equals(highway) || "path".equals(highway) ) // Runge, see http://www.openstreetmap.org/way/157482832 
        {
            String trackType = way.getTag("tracktype");
            if ("grade1".equals(trackType))
                weightToPrioMap.put(110d, PREFER.getValue());
            else if (trackType == null || trackType.startsWith("grade"))
                weightToPrioMap.put(110d, AVOID_AT_ALL_COSTS.getValue());
        }
    }

    @Override
    protected boolean isPushingSection(ReaderWay way) {
        String highway = way.getTag("highway");
        String trackType = way.getTag("tracktype");
        
        // Runge
        boolean isPushing =  way.hasTag("highway", pushingSectionsHighways)
                || way.hasTag("railway", "platform")  || way.hasTag("route", ferries)   || way.hasTag("bicycle", "dismount")
                || "track".equals(highway) && trackType != null && !"grade1".equals(trackType);
        
        if (isPushing)
        {
        	if ("track".equals(highway) && trackType != null && "grade1".equals(trackType))
        	{
        		String surface = way.getTag("surface"); // Runge
        	    if (!Helper.isEmpty(surface))
        	    {
        	    	Integer surfaceSpeed = _speedLimitHandler.getSurfaceSpeed(surface);
        	    	if (surfaceSpeed != null && surfaceSpeed != -1)
        	    	{
        	    		if (surfaceSpeed >= PUSHING_SECTION_SPEED)
        	    			isPushing = false;
        	    	}
        	    }
        	}
        }
        
        return isPushing;
    }

    // MARQ24 removed @Override
    boolean isSacScaleAllowed(String sacScale) {
        // for racing bike it is only allowed if empty
        return false;
    }

    @Override
    public String toString() {
        return FlagEncoderNames.RACINGBIKE_ORS;
    }
}
