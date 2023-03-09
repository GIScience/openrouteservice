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
package org.heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.UnsignedDecimalEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.parsers.helpers.OSMValueExtractor;
import org.heigit.ors.routing.graphhopper.extensions.util.PriorityCode;
import com.graphhopper.routing.util.TransportationMode;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;

import java.util.*;

import static com.graphhopper.routing.util.EncodingManager.getKey;

public class HeavyVehicleFlagEncoder extends VehicleFlagEncoder {
    public static final String VAL_DESIGNATED = "designated";
    public static final String VAL_AGRICULTURAL = "agricultural";
    public static final String VAL_FORESTRY = "forestry";
    public static final String VAL_GOODS = "goods";
    public static final String KEY_HIGHWAY = "highway";
    public static final String VAL_TRACK = "track";
    public static final String KEY_IMPASSABLE = "impassable";
    protected final HashSet<String> forwardKeys = new HashSet<>(5);
    protected final HashSet<String> backwardKeys = new HashSet<>(5);
    protected final List<String> hgvAccess = new ArrayList<>(5);

    private static final int MEAN_SPEED = 70;

    // Encoder for storing whether the edge is on a preferred way
    private DecimalEncodedValue priorityWayEncoder;

    /**
     * Should be only instantied via EncodingManager
     */
    public HeavyVehicleFlagEncoder()
    {
        this(5, 5, 0);
    }

    public HeavyVehicleFlagEncoder(PMap properties) {
        this(properties.getInt("speed_bits", 5),
                properties.getDouble("speed_factor", 5),
                properties.getBool("turn_costs", false) ? 3 : 0);

        setProperties(properties);

        maxTrackGradeLevel = properties.getInt("maximum_grade_level", 1);
    }

    public HeavyVehicleFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
        super(speedBits, speedFactor, maxTurnCosts);

        maxPossibleSpeed = 90;

        intendedValues.add(VAL_DESIGNATED);
        intendedValues.add(VAL_AGRICULTURAL);
        intendedValues.add(VAL_FORESTRY);
        intendedValues.add("delivery");
        intendedValues.add("bus");
        intendedValues.add("hgv");
        intendedValues.add(VAL_GOODS);

        hgvAccess.addAll(Arrays.asList("hgv", VAL_GOODS, "bus", VAL_AGRICULTURAL, VAL_FORESTRY, "delivery"));

        // Override default speeds with lower values
        trackTypeSpeedMap.put("grade1", 40); // paved
        trackTypeSpeedMap.put("grade2", 30); // now unpaved - gravel mixed with ...
        trackTypeSpeedMap.put("grade3", 20); // ... hard and soft materials
        trackTypeSpeedMap.put("grade4", 15); // ... some hard or compressed materials
        trackTypeSpeedMap.put("grade5", 10); // ... no hard materials. soil/sand/grass
        // autobahn
        defaultSpeedMap.put("motorway", 80);
        defaultSpeedMap.put("motorway_link", 50);
        defaultSpeedMap.put("motorroad", 80);
        // bundesstraße
        defaultSpeedMap.put("trunk", 80);
        defaultSpeedMap.put("trunk_link", 50);
        // linking bigger town
        defaultSpeedMap.put("primary", 60);

        initSpeedLimitHandler(this.toString());

        forwardKeys.add("goods:forward");
        forwardKeys.add("hgv:forward");
        forwardKeys.add("bus:forward");
        forwardKeys.add("agricultural:forward");
        forwardKeys.add("forestry:forward");
        forwardKeys.add("delivery:forward");

        backwardKeys.add("goods:backward");
        backwardKeys.add("hgv:backward");
        backwardKeys.add("bus:backward");
        backwardKeys.add("agricultural:backward");
        backwardKeys.add("forestry:backward");
        backwardKeys.add("delivery:backward");
    }

    @Override
    public void createEncodedValues(List<EncodedValue> registerNewEncodedValue, String prefix, int index) {
        super.createEncodedValues(registerNewEncodedValue, prefix, index);
        priorityWayEncoder = new UnsignedDecimalEncodedValue(getKey(prefix, "priority"), 4, PriorityCode.getFactor(1), false);
        registerNewEncodedValue.add(priorityWayEncoder);
    }

    @Override
    public double getMaxSpeed( ReaderWay way ) {
        double maxSpeed = OSMValueExtractor.stringToKmh(way.getTag("maxspeed:hgv"));

        double fwdSpeed = OSMValueExtractor.stringToKmh(way.getTag("maxspeed:hgv:forward"));
        if (isValidSpeed(fwdSpeed)  && (!isValidSpeed(maxSpeed) || fwdSpeed < maxSpeed)) {
            maxSpeed = fwdSpeed;
        }

        double backSpeed = OSMValueExtractor.stringToKmh(way.getTag("maxspeed:hgv:backward"));
        if (isValidSpeed(backSpeed) && (!isValidSpeed(maxSpeed) || backSpeed < maxSpeed)) {
            maxSpeed = backSpeed;
        }

        if (!isValidSpeed(maxSpeed)) {
            maxSpeed = super.getMaxSpeed(way);
            if (isValidSpeed(maxSpeed)) {
                String highway = way.getTag(KEY_HIGHWAY);
                if (!Helper.isEmpty(highway)) {
                    double defaultSpeed = speedLimitHandler.getSpeed(highway);
                    if (defaultSpeed < maxSpeed)
                        maxSpeed = defaultSpeed;
                }
            }
        }

        return maxSpeed;
    }

    @Override
    protected String getHighway(ReaderWay way) {
        return way.getTag(KEY_HIGHWAY);
    }

    @Override
    public EncodingManager.Access getAccess(ReaderWay way) {
        String highwayValue = way.getTag(KEY_HIGHWAY);
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

        if (VAL_TRACK.equals(highwayValue)) {
            String tt = way.getTag("tracktype");
            int grade = getTrackGradeLevel(tt);
            if (grade > maxTrackGradeLevel)
                return EncodingManager.Access.CAN_SKIP;
        }

        if (!speedLimitHandler.hasSpeedValue(highwayValue))
            return EncodingManager.Access.CAN_SKIP;

        if (way.hasTag(KEY_IMPASSABLE, "yes") || way.hasTag("status", KEY_IMPASSABLE) || way.hasTag("smoothness", KEY_IMPASSABLE))
            return EncodingManager.Access.CAN_SKIP;

        // multiple restrictions needs special handling compared to foot and bike, see also motorcycle
        for (String restrictionValue: restrictionValues) {
            if (!restrictionValue.isEmpty()) {
                if (restrictedValues.contains(restrictionValue) && !getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way))
                    return EncodingManager.Access.CAN_SKIP;
                if (intendedValues.contains(restrictionValue))
                    return EncodingManager.Access.WAY;
            }
        }

        // do not drive street cars into fords
        boolean carsAllowed = way.hasTag(restrictions, intendedValues);
        if (isBlockFords() && ("ford".equals(highwayValue) || way.hasTag("ford")) && !carsAllowed)
            return EncodingManager.Access.CAN_SKIP;

        // check access restrictions
        // filter special type of access for hgv
        if (way.hasTag(restrictions, restrictedValues) && !carsAllowed && !way.hasTag(hgvAccess, intendedValues)) {
            return EncodingManager.Access.CAN_SKIP;
        }

        String maxwidth = way.getTag("maxwidth"); // Runge added on 23.02.2016
        if (maxwidth != null) {
            try {
                double mwv = Double.parseDouble(maxwidth);
                if (mwv < 2.0)
                    return EncodingManager.Access.CAN_SKIP;
            } catch(Exception ex) {
                // do nothing
            }
        }

        if (getConditionalTagInspector().isPermittedWayConditionallyRestricted(way))
            return EncodingManager.Access.CAN_SKIP;
        else
            return EncodingManager.Access.WAY;
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, EncodingManager.Access access, long relationFlags) {
        super.handleWayTags(edgeFlags, way, access, relationFlags);

        priorityWayEncoder.setDecimal(false, edgeFlags, PriorityCode.getFactor(handlePriority(way)));
        return edgeFlags;
    }

    protected int handlePriority(ReaderWay way) {
        TreeMap<Double, Integer> weightToPrioMap = new TreeMap<>();

        collect(way, weightToPrioMap);

        // pick priority with biggest order value
        return weightToPrioMap.lastEntry().getValue();
    }

    /**
     * @param weightToPrioMap
     *            associate a weight with every priority. This sorted map allows
     *            subclasses to 'insert' more important priorities as well as
     *            overwrite determined priorities.
     */
    protected void collect(ReaderWay way, TreeMap<Double, Integer> weightToPrioMap) { // Runge
        if (way.hasTag("hgv", VAL_DESIGNATED) || (way.hasTag("access", VAL_DESIGNATED) && (way.hasTag(VAL_GOODS, "yes") || way.hasTag("hgv", "yes") || way.hasTag("bus", "yes") || way.hasTag(VAL_AGRICULTURAL, "yes") || way.hasTag(VAL_FORESTRY, "yes") )))
            weightToPrioMap.put(100d, PriorityCode.BEST.getValue());
        else {
            String highway = way.getTag(KEY_HIGHWAY);
            double maxSpeed = getMaxSpeed(way);

            if (!Helper.isEmpty(highway)) {
                switch (highway) {
                    case "motorway":
                    case "motorway_link":
                    case "trunk":
                    case "trunk_link":
                        weightToPrioMap.put(100d, PriorityCode.BEST.getValue());
                        break;
                    case "primary":
                    case "primary_link":
                    case "secondary":
                    case "secondary_link":
                        weightToPrioMap.put(100d, PriorityCode.PREFER.getValue());
                        break;
                    case "tertiary":
                    case "tertiary_link":
                        weightToPrioMap.put(100d, PriorityCode.UNCHANGED.getValue());
                        break;
                    case "residential":
                    case "service":
                    case "road":
                    case "unclassified":
                        if (isValidSpeed(maxSpeed) && maxSpeed <= 30) {
                            weightToPrioMap.put(120d, PriorityCode.REACH_DEST.getValue());
                        } else {
                            weightToPrioMap.put(100d, PriorityCode.AVOID_IF_POSSIBLE.getValue());
                        }
                        break;
                    case "living_street":
                        weightToPrioMap.put(100d, PriorityCode.AVOID_IF_POSSIBLE.getValue());
                        break;
                    case VAL_TRACK:
                        weightToPrioMap.put(100d, PriorityCode.REACH_DEST.getValue());
                        break;
                    default:
                        weightToPrioMap.put(40d, PriorityCode.AVOID_IF_POSSIBLE.getValue());
                        break;
                }
            } else {
                weightToPrioMap.put(100d, PriorityCode.UNCHANGED.getValue());
            }

            if (isValidSpeed(maxSpeed)) {
                // We assume that the given road segment goes through a settlement.
                if (maxSpeed <= 40)
                    weightToPrioMap.put(110d, PriorityCode.AVOID_IF_POSSIBLE.getValue());
                else if (maxSpeed <= 50)
                    weightToPrioMap.put(110d, PriorityCode.UNCHANGED.getValue());
            }
        }
    }

    @Override
    public boolean supports(Class<?> feature) {
        if (super.supports(feature))
            return true;
        return PriorityWeighting.class.isAssignableFrom(feature);
    }

    public double getMeanSpeed() {
        return MEAN_SPEED;
    }

    @Override
    public String toString() {
        return FlagEncoderNames.HEAVYVEHICLE;
    }

    @Override
    public TransportationMode getTransportationMode() {
        return TransportationMode.HGV;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final HeavyVehicleFlagEncoder other = (HeavyVehicleFlagEncoder) obj;
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return ("HeavyVehicleFlagEncoder" + this).hashCode();
    }
}
