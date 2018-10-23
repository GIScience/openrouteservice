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
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import heigit.ors.routing.graphhopper.extensions.flagencoders.tomove.BikeCommonFlagEncoder;

public class BikeFlagEncoder extends BikeCommonFlagEncoder {
    public BikeFlagEncoder() {
        this(4, 2, 0, false);
    }

    public BikeFlagEncoder(String propertiesString) {
        this(new PMap(propertiesString));
    }

    public BikeFlagEncoder(PMap properties) {
        this((int) properties.getLong("speed_bits", 4) + (properties.getBool("consider_elevation", false) ? 1 : 0),
                properties.getLong("speed_factor", 2),
                properties.getBool("turn_costs", false) ? 1 : 0, properties.getBool("consider_elevation", false));
        this.properties = properties;
        this.setBlockFords(properties.getBool("block_fords", true));
    }
    
    public BikeFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
        this(speedBits, speedFactor, maxTurnCosts, false);
    }
    
    public BikeFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts, boolean considerElevation) {
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
    public int getVersion() {
        return 2;
    }

    @Override
	protected
    boolean isPushingSection(ReaderWay way) {
        String highway = way.getTag("highway");
        String trackType = way.getTag("tracktype");
        return super.isPushingSection(way) || "track".equals(highway) && trackType != null 
            	&&  !("grade1".equals(trackType) || "grade2".equals(trackType) || "grade3".equals(trackType)); // Runge
    }

    @Override
	protected double getDownhillMaxSpeed()
	{
		return 50;
	}

    @Override
    public String toString() {
        return FlagEncoderNames.BIKE_ORS_OLD;
        //return FlagEncoderNames.BIKE_ORS;
    }
}
