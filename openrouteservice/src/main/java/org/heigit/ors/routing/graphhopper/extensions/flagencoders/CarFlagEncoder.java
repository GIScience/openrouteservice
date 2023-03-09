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
package org.heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.reader.osm.conditional.ConditionalOSMSpeedInspector;
import com.graphhopper.reader.osm.conditional.ConditionalParser;
import com.graphhopper.reader.osm.conditional.DateRangeParser;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TransportationMode;
import com.graphhopper.util.PMap;

import java.util.Arrays;

/**
 * Defines bit layout for cars. (speed, access, ferries, ...)
 * <p>
 *
 * @author Peter Karich
 * @author Nop
 */
public class CarFlagEncoder extends VehicleFlagEncoder {

    private static final String KEY_IMPASSABLE = "impassable";

    // Mean speed for isochrone reach_factor
    private static final int MEAN_SPEED = 100;

    public CarFlagEncoder(PMap properties) {
        this(properties.getInt("speed_bits", 5),
                properties.getDouble("speed_factor", 5),
                properties.getBool("turn_costs", false) ? 1 : 0);

        setProperties(properties);
    }

    public CarFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
        super(speedBits, speedFactor, maxTurnCosts);

        restrictedValues.add("agricultural");
        restrictedValues.add("forestry");
        restrictedValues.add("delivery");
        restrictedValues.add("emergency");

        blockByDefaultBarriers.add("bus_trap");
        blockByDefaultBarriers.add("sump_buster");

        initSpeedLimitHandler(this.toString());
    }

    @Override
    protected void init(DateRangeParser dateRangeParser) {
        super.init(dateRangeParser);
        ConditionalOSMSpeedInspector conditionalOSMSpeedInspector = new ConditionalOSMSpeedInspector(Arrays.asList("maxspeed"));
        conditionalOSMSpeedInspector.addValueParser(ConditionalParser.createDateTimeParser());
        setConditionalSpeedInspector(conditionalOSMSpeedInspector);
    }

    @Override
    public EncodingManager.Access getAccess(ReaderWay way) {
        // TODO: Ferries have conditionals, like opening hours or are closed during some time in the year
        String highwayValue = way.getTag("highway");
        String [] restrictionValues = way.getFirstPriorityTagValues(restrictions);
        if (highwayValue == null) {
            if (way.hasTag("route", ferries)) {
                for (String restrictionValue: restrictionValues) {
                    if (restrictedValues.contains(restrictionValue))
                        return EncodingManager.Access.CAN_SKIP;
                    if (intendedValues.contains(restrictionValue) ||
                            // implied default is allowed only if foot and bicycle is not specified:
                            restrictionValue.isEmpty() && !way.hasTag("foot") && !way.hasTag("bicycle"))
                        return EncodingManager.Access.FERRY;
                }
            }
            return EncodingManager.Access.CAN_SKIP;
        }

        if ("track".equals(highwayValue)) {
            String tt = way.getTag("tracktype");
            if (tt != null) {
            	int grade = getTrackGradeLevel(tt);
            	if (grade > maxTrackGradeLevel)
                    return EncodingManager.Access.CAN_SKIP;
            }
        }

        if (!speedLimitHandler.hasSpeedValue(highwayValue))
            return EncodingManager.Access.CAN_SKIP;

        if (way.hasTag(KEY_IMPASSABLE, "yes") || way.hasTag("status", KEY_IMPASSABLE) || way.hasTag("smoothness", KEY_IMPASSABLE))
            return EncodingManager.Access.CAN_SKIP;

        // multiple restrictions needs special handling compared to foot and bike, see also motorcycle
        for (String restrictionValue: restrictionValues) {
            if (!restrictionValue.isEmpty()) {
                if (restrictedValues.contains(restrictionValue))
                    return isRestrictedWayConditionallyPermitted(way);
                if (intendedValues.contains(restrictionValue))
                    return EncodingManager.Access.WAY;
            }
        }

        // do not drive street cars into fords
        if (isBlockFords() && ("ford".equals(highwayValue) || way.hasTag("ford")))
            return EncodingManager.Access.CAN_SKIP;
        
        
        String maxwidth = way.getTag("maxwidth"); // Runge added on 23.02.2016
        if (maxwidth != null) {
            try {
                double mwv = Double.parseDouble(maxwidth);
                if (mwv < 2.0)
                    return EncodingManager.Access.CAN_SKIP;
            } catch (Exception ex) {
                // ignore
            }
        }

        return isPermittedWayConditionallyRestricted(way);
    }

    public double getMeanSpeed() {
        return MEAN_SPEED;
    }

    @Override
    public String toString() {
        return FlagEncoderNames.CAR_ORS;
    }

    @Override
    public TransportationMode getTransportationMode() {
        return TransportationMode.CAR;
    }
}
