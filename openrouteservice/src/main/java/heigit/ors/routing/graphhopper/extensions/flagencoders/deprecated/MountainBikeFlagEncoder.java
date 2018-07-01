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

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.flagencoders.SpeedLimitHandler;
import heigit.ors.routing.graphhopper.extensions.flagencoders.tomove.BikeCommonFlagEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.graphhopper.routing.util.PriorityCode.*;

/**
 * Specifies the settings for mountain biking
 * <p>
 *
 * @author ratrun
 * @author Peter Karich
 */
public class MountainBikeFlagEncoder extends BikeCommonFlagEncoder {
    public MountainBikeFlagEncoder() {
        this(4, 2, 0, false);
    }

    public MountainBikeFlagEncoder(PMap properties) {
        this(
                properties.getInt("speed_bits", 4) + (properties.getBool("consider_elevation", false) ? 1 : 0),
                properties.getDouble("speed_factor", 2),
                properties.getBool("turn_costs", false) ? 1 : 0,
                properties.getBool("consider_elevation", false)
        );
        this.properties = properties;
        this.setBlockFords(properties.getBool("block_fords", true));
    }

    public MountainBikeFlagEncoder(String propertiesStr) {
        this(new PMap(propertiesStr));
    }

    public MountainBikeFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts, boolean considerElevation) {
        super(speedBits, speedFactor, maxTurnCosts, considerElevation);
        
		Map<String, Integer> trackTypeSpeedMap = new HashMap<String, Integer>();
		trackTypeSpeedMap.put("grade1", 18); // paved
		trackTypeSpeedMap.put("grade2", 12); // now unpaved ...
		trackTypeSpeedMap.put("grade3", 8);
		trackTypeSpeedMap.put("grade4", 6);
		trackTypeSpeedMap.put("grade5", 4); // like sand/grass    

		Map<String, Integer> badSurfaceSpeedMap = new HashMap<String, Integer>();
        badSurfaceSpeedMap.put("paved", 18);
        badSurfaceSpeedMap.put("asphalt", 18);
        badSurfaceSpeedMap.put("cobblestone", 10);
        badSurfaceSpeedMap.put("cobblestone:flattened", 10);
        badSurfaceSpeedMap.put("sett", 10);
        badSurfaceSpeedMap.put("concrete", 14);
        badSurfaceSpeedMap.put("concrete:lanes", 16);
        badSurfaceSpeedMap.put("concrete:plates", 16);
        badSurfaceSpeedMap.put("paving_stones", 16);
        badSurfaceSpeedMap.put("paving_stones:30", 16);
        badSurfaceSpeedMap.put("unpaved", 14);
        badSurfaceSpeedMap.put("compacted", 14);
        badSurfaceSpeedMap.put("dirt", 14);
        badSurfaceSpeedMap.put("earth", 14);
        badSurfaceSpeedMap.put("fine_gravel", 18);
        badSurfaceSpeedMap.put("grass", 14);
        badSurfaceSpeedMap.put("grass_paver", 14);
        badSurfaceSpeedMap.put("gravel", 16);
        badSurfaceSpeedMap.put("ground", 16);
        badSurfaceSpeedMap.put("ice", PUSHING_SECTION_SPEED / 2);
        badSurfaceSpeedMap.put("metal", 10);
        badSurfaceSpeedMap.put("mud", 12);
        badSurfaceSpeedMap.put("pebblestone", 12);
        badSurfaceSpeedMap.put("salt", 12);
        badSurfaceSpeedMap.put("sand", 10);
        badSurfaceSpeedMap.put("wood", 10);

        Map<String, Integer> highwaySpeeds = new HashMap<String, Integer>();
        highwaySpeeds.put("living_street", 6);
        highwaySpeeds.put("steps", PUSHING_SECTION_SPEED);

        highwaySpeeds.put("cycleway", 18);
        highwaySpeeds.put("path", 18);
        highwaySpeeds.put("footway", 6);
        highwaySpeeds.put("pedestrian", 6);
        highwaySpeeds.put("road", 12);
        highwaySpeeds.put("track", 18);
        highwaySpeeds.put("service", 14);
        highwaySpeeds.put("unclassified", 16);
        highwaySpeeds.put("residential", 16);

        highwaySpeeds.put("trunk", 18);
        highwaySpeeds.put("trunk_link", 18);
        highwaySpeeds.put("primary", 18);
        highwaySpeeds.put("primary_link", 18);
        highwaySpeeds.put("secondary", 18);
        highwaySpeeds.put("secondary_link", 18);
        highwaySpeeds.put("tertiary", 18);
        highwaySpeeds.put("tertiary_link", 18);
        
        _speedLimitHandler = new SpeedLimitHandler(this.toString(), highwaySpeeds, badSurfaceSpeedMap, trackTypeSpeedMap);

        addPushingSection("footway");
        addPushingSection("pedestrian");
        addPushingSection("steps");

        setCyclingNetworkPreference("icn", PREFER.getValue());
        setCyclingNetworkPreference("ncn", PREFER.getValue());
        setCyclingNetworkPreference("rcn", PREFER.getValue());
        setCyclingNetworkPreference("lcn", PREFER.getValue());
        setCyclingNetworkPreference("mtb", BEST.getValue());

        avoidHighwayTags.add("primary");
        avoidHighwayTags.add("primary_link");
        avoidHighwayTags.add("secondary");
        avoidHighwayTags.add("secondary_link");

        preferHighwayTags.add("road");
        preferHighwayTags.add("track");
        preferHighwayTags.add("path");
        preferHighwayTags.add("service");
        preferHighwayTags.add("tertiary");
        preferHighwayTags.add("tertiary_link");
        preferHighwayTags.add("residential");
        preferHighwayTags.add("unclassified");

        potentialBarriers.add("kissing_gate");
        setSpecificClassBicycle("mtb");

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
        if ("track".equals(highway)) {
            String trackType = way.getTag("tracktype");
            if ("grade1".equals(trackType))
                weightToPrioMap.put(50d, UNCHANGED.getValue());
            else if (trackType == null)
                weightToPrioMap.put(90d, PREFER.getValue());
            else if (trackType.startsWith("grade"))
                weightToPrioMap.put(100d, VERY_NICE.getValue());
        }
    }

    @Override
    public long handleRelationTags(ReaderRelation relation, long oldRelationFlags) {
        oldRelationFlags = super.handleRelationTags(relation, oldRelationFlags);
        int code = 0;
        if (relation.hasTag("route", "mtb"))
            code = PREFER.getValue();

        int oldCode = (int) relationCodeEncoder.getValue(oldRelationFlags);
        if (oldCode < code)
            return relationCodeEncoder.setValue(0, code);
        return oldRelationFlags;
    }

    // MARQ24 removed @Override
    boolean isSacScaleAllowed(String sacScale) {
        // other scales are too dangerous even for MTB, see http://wiki.openstreetmap.org/wiki/Key:sac_scale
        return "hiking".equals(sacScale) || "mountain_hiking".equals(sacScale)
                || "demanding_mountain_hiking".equals(sacScale) || "alpine_hiking".equals(sacScale);
    }

    @Override
	protected double getDownhillMaxSpeed()
	{
		return 60;
	}

    @Override
    public String toString() {
        return "mtb";
    }
}
