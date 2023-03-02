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
package org.heigit.ors.routing.graphhopper.extensions.flagencoders.bike;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;

import java.util.TreeMap;

import static com.graphhopper.routing.ev.RouteNetwork.*;
import static org.heigit.ors.routing.graphhopper.extensions.util.PriorityCode.*;

/**
 * Specifies the settings for mountain biking
 * <p>
 *
 * @author ratrun
 * @author Peter Karich
 */
public class MountainBikeFlagEncoder extends CommonBikeFlagEncoder {
    private static final int MEAN_SPEED = 14;
    public static final String KEY_TRACK = "track";

    public MountainBikeFlagEncoder(PMap properties) {
        this(
            properties.getInt("speed_bits", 4 + (properties.getBool("consider_elevation", false) ? 1 : 0)),
            properties.getDouble("speed_factor", 2),
            properties.getBool("turn_costs", false) ? 1 : 0,
            properties.getBool("consider_elevation", false)
        );
        setProperties(properties);
    }

    public MountainBikeFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts, boolean considerElevation) {
        super(speedBits, speedFactor, maxTurnCosts, considerElevation);
        setTrackTypeSpeed("grade1", 18); // paved
        setTrackTypeSpeed("grade2", 16); // now unpaved ...
        setTrackTypeSpeed("grade3", 12);
        setTrackTypeSpeed("grade4", 8);
        setTrackTypeSpeed("grade5", 6); // like sand/grass     

        setSurfaceSpeed("paved", 18);
        setSurfaceSpeed("asphalt", 18);
        setSurfaceSpeed("cobblestone", 10);
        setSurfaceSpeed("cobblestone:flattened", 10);
        setSurfaceSpeed("sett", 10);
        setSurfaceSpeed("concrete", 14);
        setSurfaceSpeed("concrete:lanes", 16);
        setSurfaceSpeed("concrete:plates", 16);
        setSurfaceSpeed("paving_stones", 16);
        setSurfaceSpeed("paving_stones:30", 16);
        setSurfaceSpeed("unpaved", 14);
        setSurfaceSpeed("compacted", 14);
        setSurfaceSpeed("dirt", 14);
        setSurfaceSpeed("earth", 14);
        setSurfaceSpeed("fine_gravel", 18);
        setSurfaceSpeed("grass", 14);
        setSurfaceSpeed("grass_paver", 14);
        setSurfaceSpeed("gravel", 16);
        setSurfaceSpeed("ground", 16);
        setSurfaceSpeed("ice", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("metal", 10);
        setSurfaceSpeed("mud", 12);
        setSurfaceSpeed("pebblestone", 12);
        setSurfaceSpeed("salt", 12);
        setSurfaceSpeed("sand", 10);
        setSurfaceSpeed("wood", 10);

        setHighwaySpeed("living_street", 6);
        setHighwaySpeed("steps", PUSHING_SECTION_SPEED);

        setHighwaySpeed("cycleway", 18);
        setHighwaySpeed("path", 18);
        setHighwaySpeed("footway", 6);
        setHighwaySpeed("pedestrian", 6);
        setHighwaySpeed("road", 12);
        setHighwaySpeed(KEY_TRACK, 18);
        setHighwaySpeed("service", 14);
        setHighwaySpeed("unclassified", 16);
        setHighwaySpeed("residential", 16);

        setHighwaySpeed("trunk", 18);
        setHighwaySpeed("trunk_link", 18);
        setHighwaySpeed("primary", 18);
        setHighwaySpeed("primary_link", 18);
        setHighwaySpeed("secondary", 18);
        setHighwaySpeed("secondary_link", 18);
        setHighwaySpeed("tertiary", 18);
        setHighwaySpeed("tertiary_link", 18);

        addPushingSection("footway");
        addPushingSection("pedestrian");
        addPushingSection("steps");

        avoidHighwayTags.add("primary");
        avoidHighwayTags.add("primary_link");
        avoidHighwayTags.add("secondary");
        avoidHighwayTags.add("secondary_link");

        preferHighwayTags.add("road");
        preferHighwayTags.add(KEY_TRACK);
        preferHighwayTags.add("path");
        preferHighwayTags.add("service");
        preferHighwayTags.add("tertiary");
        preferHighwayTags.add("tertiary_link");
        preferHighwayTags.add("residential");
        preferHighwayTags.add("unclassified");

        passByDefaultBarriers.add("kissing_gate");
        setSpecificClassBicycle("mtb");

        routeMap.put(INTERNATIONAL, PREFER.getValue());
        routeMap.put(NATIONAL, PREFER.getValue());
        routeMap.put(REGIONAL, PREFER.getValue());
        routeMap.put(LOCAL, PREFER.getValue());
        routeMap.put(MTB, BEST.getValue());
        routeMap.put(OTHER, PREFER.getValue());
    }

    public double getMeanSpeed() {
        return MEAN_SPEED;
    }

    @Override
    void collect(ReaderWay way, double wayTypeSpeed, TreeMap<Double, Integer> weightToPrioMap) {
        super.collect(way, wayTypeSpeed, weightToPrioMap);

        String highway = way.getTag("highway");
        if (KEY_TRACK.equals(highway)) {
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
    boolean isSacScaleAllowed(String sacScale) {
        // other scales are too dangerous even for MTB, see http://wiki.openstreetmap.org/wiki/Key:sac_scale
        return "hiking".equals(sacScale) || "mountain_hiking".equals(sacScale)
                || "demanding_mountain_hiking".equals(sacScale) || "alpine_hiking".equals(sacScale);
    }

    @Override
    public String toString() {
        return FlagEncoderNames.MTB_ORS;
    }

    @Override
    protected double getDownhillMaxSpeed() {
        return 60;
    }
}