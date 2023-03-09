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

/**
 * Specifies the settings for cycletouring/trekking
 * <p>
 *
 * @author ratrun
 * @author Peter Karich
 */
public class RegularBikeFlagEncoder extends CommonBikeFlagEncoder {
    private static final int MEAN_SPEED = 15;

    public RegularBikeFlagEncoder(PMap properties) {
        this(
            // MARQ24 MOD START
            properties.getInt("speed_bits", 4 + (properties.getBool("consider_elevation", false) ? 1 : 0)),
            // MARQ24 MOD END
            properties.getDouble("speed_factor", 2),
            properties.getBool("turn_costs", false) ? 1 : 0
            // MARQ24 MOD START
            ,properties.getBool("consider_elevation", false)
            // MARQ24 MOD END
        );
        setProperties(properties);
    }

    public RegularBikeFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts, boolean considerElevation) {
        super(speedBits, speedFactor, maxTurnCosts, considerElevation);
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

        preferHighwayTags.add("service");
        preferHighwayTags.add("tertiary");
        preferHighwayTags.add("tertiary_link");
        preferHighwayTags.add("residential");
        preferHighwayTags.add("unclassified");

        blockByDefaultBarriers.add("kissing_gate");
        setSpecificClassBicycle("touring");
    }

    public double getMeanSpeed() {
        return MEAN_SPEED;
    }

    @Override
    boolean isPushingSection(ReaderWay way) {
        String highway = way.getTag("highway");
        String trackType = way.getTag("tracktype");
        return super.isPushingSection(way) || "track".equals(highway) && trackType != null
        // MARQ24 MOD START - by Runge
                && !("grade1".equals(trackType) || "grade2".equals(trackType) || "grade3".equals(trackType));
        // MARQ24 MOD END
    }

    @Override
    public String toString() {
        // MARQ24 MOD START
        return FlagEncoderNames.BIKE_ORS;
        // MARQ24 MOD END
    }

    // MARQ24 MOD START
    @Override
    protected double getDownhillMaxSpeed()
    {
        return 50;
    }
    // MARQ24 MOD END
}
