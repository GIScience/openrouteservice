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

import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.profiles.EncodedValue;
import com.graphhopper.routing.profiles.IntEncodedValue;
import com.graphhopper.routing.profiles.UnsignedIntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

import java.util.*;

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
        this((int) properties.getLong("speed_bits", 5),
                properties.getDouble("speed_factor", 5),
                properties.getBool("turn_costs", false) ? 1 : 0);
        this.properties = properties;
        speedTwoDirections = properties.getBool("speed_two_directions", true);
        this.setBlockFords(properties.getBool("block_fords", true));
        this.setBlockByDefault(properties.getBool("block_barriers", true));

        useAcceleration = properties.getBool("use_acceleration", false);

        maxTrackGradeLevel = properties.getInt("maximum_grade_level", 3);
    }

    public CarFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
        super(speedBits, speedFactor, maxTurnCosts);

        restrictedValues.add("agricultural");
        restrictedValues.add("forestry");
        restrictedValues.add("delivery");
        restrictedValues.add("emergency");

        absoluteBarriers.add("bus_trap");
        absoluteBarriers.add("sump_buster");

        initSpeedLimitHandler(this.toString());

        // MARQ24 added in order to support transfer from Node values over to the ways...
        //see https://wiki.openstreetmap.org/wiki/DE:Key:crossing
        crossing_with_trafficLight.add("traffic_signals");
        crossing_with_trafficLight.add("traffic_lights");
        crossing_with_trafficLight.add("toucan");
        crossing_with_trafficLight.add("pegasus");
        crossing_without.add("uncontrolled");
        crossing_without.add("zebra");
        crossing_without.add("island");

        init();
    }

    @Override
    public EncodingManager.Access getAccess(ReaderWay way) {
        // TODO: Ferries have conditionals, like opening hours or are closed during some time in the year
        String highwayValue = way.getTag("highway");
        String firstValue = way.getFirstPriorityTag(restrictions);
        if (highwayValue == null) {
            if (way.hasTag("route", ferries)) {
                if (restrictedValues.contains(firstValue))
                    return EncodingManager.Access.CAN_SKIP;
                if (intendedValues.contains(firstValue) ||
                        // implied default is allowed only if foot and bicycle is not specified:
                        firstValue.isEmpty() && !way.hasTag("foot") && !way.hasTag("bicycle"))
                    return EncodingManager.Access.FERRY;
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
        if (!firstValue.isEmpty()) {
            if (restrictedValues.contains(firstValue) && !getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way))
                return EncodingManager.Access.CAN_SKIP;
            if (intendedValues.contains(firstValue))
                return EncodingManager.Access.WAY;
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

        if (getConditionalTagInspector().isPermittedWayConditionallyRestricted(way))
            return EncodingManager.Access.CAN_SKIP;
        else
            return EncodingManager.Access.WAY;
    }

    public double getMeanSpeed() {
        return MEAN_SPEED;
    }

    @Override
    public String toString() {
        return FlagEncoderNames.CAR_ORS;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    // ADDONs for supporting TrafficLight & Crossing counts in order to support more reasonable routing times
    // for car's in cities... [initial created by marq24 - so if you have questions - please contact me]
    // These additional values will be used by the
    // org.heigit.ors.routing.graphhopper.extensions.weighting.StreetCrossingWeighting
    private Set<String> crossing_with_trafficLight = new HashSet(4);
    private Set<String> crossing_without = new HashSet(3);
    private long trafficLightCountEncBit;
    private long crossingCountEncBit;
    private IntEncodedValue trafficLightCountEnc;
    private IntEncodedValue crossingCountEnc;

    // handleNodeTags will collect information from each OSM-Note, that then will stored in a map (per nodeId) that
    // then will/can be processed in the OSMReader during EdgeCreationTime.
    public long handleNodeTags(ReaderNode node) {
        // just extract out traffic light & crossing info...
        if (node.hasTag("highway", "traffic_signals") || node.hasTag("crossing", crossing_with_trafficLight)){
            return trafficLightCountEncBit | super.handleNodeTags(node);
        } else if(node.hasTag("highway", "crossing") && node.hasTag("crossing", crossing_without)) {
            return crossingCountEncBit | super.handleNodeTags(node);
        }
        return super.handleNodeTags(node);
    }

    @Override
    public void createEncodedValues(List<EncodedValue> registerNewEncodedValue, String prefix, int index) {
        // first two bits are reserved for route handling in superclass
        super.createEncodedValues(registerNewEncodedValue, prefix, index);
        trafficLightCountEnc = new UnsignedIntEncodedValue("trafficlights", 4, false);
        registerNewEncodedValue.add(trafficLightCountEnc);
        crossingCountEnc = new UnsignedIntEncodedValue("crossings", 3, false);
        registerNewEncodedValue.add(crossingCountEnc);
        trafficLightCountEncBit = 1L << index + 1;
        crossingCountEncBit = 1L << index + 2;
    }

    @Override
    public void applyWayTags(ReaderWay way, EdgeIteratorState edgeState) {
        int addVal = edgeState.getAdditionalField();
        if(addVal > -1){
            // ok we have some values collected per edge!
            // see OSMReader adjustment for the mask shifting... (Yes all that is a just quick hack...)
            int trafficLights = addVal & 0xFF;
            if(trafficLights > 0){
                edgeState.set(trafficLightCountEnc, Math.min(trafficLights, 15));
            }
            int crossingCount = addVal >> 8;
            if(crossingCount > 0){
                edgeState.set(crossingCountEnc, Math.min(crossingCount, 7));
            }
        }
    }

    public IntEncodedValue getTrafficLightCountEnc() {
        if (trafficLightCountEnc == null) {
            throw new NullPointerException("FlagEncoder " + toString() + " not yet initialized");
        }
        return trafficLightCountEnc;
    }

    public IntEncodedValue getCrossingCountEnc() {
        if (crossingCountEnc == null) {
            throw new NullPointerException("FlagEncoder " + toString() + " not yet initialized");
        }
        return crossingCountEnc;
    }
}
