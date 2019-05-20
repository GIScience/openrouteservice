/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodedDoubleValue;
import com.graphhopper.routing.util.EncodedValue;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.weighting.PriorityWeighting;
import heigit.ors.routing.graphhopper.extensions.OSMTags;

import java.util.*;

import static com.graphhopper.routing.util.PriorityCode.*;

/**
 * This code has been adapted from the original GraphHopper FootFlagEncoder found at
 * https://github.com/graphhopper/graphhopper/blob/master/core/src/main/java/com/graphhopper/routing/util/FootFlagEncoder.java
 *
 * @author Adam Rousell
 * @author Peter Karich
 */
public abstract class FootFlagEncoder extends ORSAbstractFlagEncoder {
    static final int SLOW_SPEED = 2;
    private static final int MEAN_SPEED = 5;
    static final int FERRY_SPEED = 15;

    private final Set<String> safeHighwayTags = new HashSet<>();
    private final Set<String> allowedHighwayTags = new HashSet<>();
    private final Set<String> avoidHighwayTags = new HashSet<>();

    Set<String> preferredWayTags = new HashSet<>();

    private final Set<String> avoidUnlessSidewalkTags = new HashSet<>();

    Set<String> suitableSacScales = new HashSet<>();
    // convert network tag of hiking routes into a way route code
    final Map<String, Integer> hikingNetworkToCode = new HashMap<>();
    Set<String> usableSidewalkValues = new HashSet<>(5);
    Set<String> noSidewalkValues = new HashSet<>(5);

    protected EncodedValue priorityWayEncoder;
    protected EncodedValue relationCodeEncoder;

    FootFlagEncoder(int speedBits, double speedFactor) {
        super(speedBits, speedFactor, 0);
        restrictions.addAll(Arrays.asList("foot", "access"));

        restrictedValues.addAll(Arrays.asList(
                "private",
                "no",
                "restricted",
                "military",
                "emergency"
        ));

        intendedValues.addAll(Arrays.asList(
                "yes",
                "designated",
                "official",
                "permissive"
        ));

        noSidewalkValues.addAll(Arrays.asList(
                "no",
                "none",
                "separate",
                "separate"
        ));

        usableSidewalkValues.addAll(Arrays.asList(
                "yes",
                "both",
                "left",
                "right"
        ));

        setBlockByDefault(false);
        potentialBarriers.add("gate");

        safeHighwayTags.addAll(Arrays.asList(
                "footway",
                "path",
                "steps",
                "pedestrian",
                "living_street",
                "track",
                "residential",
                "service"
        ));

        avoidHighwayTags.addAll(Arrays.asList(
                "secondary",
                "secondary_link",
                "tertiary",
                "tertiary_link"
        ));

        avoidUnlessSidewalkTags.addAll(Arrays.asList(
                "trunk",
                "trunk_link",
                "primary",
                "primary_link"
        ));

        allowedHighwayTags.addAll(safeHighwayTags);
        allowedHighwayTags.addAll(avoidHighwayTags);
        allowedHighwayTags.addAll(avoidUnlessSidewalkTags);
        allowedHighwayTags.addAll(Arrays.asList(
                "cycleway",
                "unclassified",
                "road"
        ));

        hikingNetworkToCode.put("iwn", UNCHANGED.getValue());
        hikingNetworkToCode.put("nwn", UNCHANGED.getValue());
        hikingNetworkToCode.put("rwn", UNCHANGED.getValue());
        hikingNetworkToCode.put("lwn", UNCHANGED.getValue());

        maxPossibleSpeed = FERRY_SPEED;

        init();
    }

    public double getMeanSpeed() {
        return MEAN_SPEED;
    }

    @Override
    public int defineWayBits(int index, int shift) {
        // first two bits are reserved for route handling in superclass
        shift = super.defineWayBits(index, shift);
        // larger value required - ferries are faster than pedestrians
        speedEncoder = new EncodedDoubleValue("Speed", shift, speedBits, speedFactor, MEAN_SPEED, maxPossibleSpeed);
        shift += speedEncoder.getBits();

        priorityWayEncoder = new EncodedValue("PreferWay", shift, 3, 1, 0, 7);
        shift += priorityWayEncoder.getBits();
        return shift;
    }

    @Override
    public int defineRelationBits(int index, int shift) {
        relationCodeEncoder = new EncodedValue("RelationCode", shift, 3, 1, 0, 7);
        return shift + relationCodeEncoder.getBits();
    }

    /**
     * Foot flag encoder does not provide any turn cost / restrictions
     */
    @Override
    public int defineTurnBits(int index, int shift) {
        return shift;
    }

    /**
     * Foot flag encoder does not provide any turn cost / restrictions
     * <p>
     *
     * @return <code>false</code>
     */
    @Override
    public boolean isTurnRestricted(long flag) {
        return false;
    }

    /**
     * Foot flag encoder does not provide any turn cost / restrictions
     * <p>
     *
     * @return 0
     */
    @Override
    public double getTurnCost(long flag) {
        return 0;
    }

    @Override
    public long getTurnFlags(boolean restricted, double costs) {
        return 0;
    }

    @Override
    public long handleRelationTags(ReaderRelation relation, long oldRelationFlags) {
        int code = 0;
        if (relation.hasTag(OSMTags.Keys.ROUTE, "hiking") || relation.hasTag(OSMTags.Keys.ROUTE, "foot")) {
            Integer val = hikingNetworkToCode.get(relation.getTag("network"));
            if (val != null)
                code = val;
            else
                code = hikingNetworkToCode.get("lwn");
        } else if (relation.hasTag(OSMTags.Keys.ROUTE, "ferry")) {
            code = PriorityCode.AVOID_IF_POSSIBLE.getValue();
        }

        int oldCode = (int) relationCodeEncoder.getValue(oldRelationFlags);
        if (oldCode < code)
            return relationCodeEncoder.setValue(0, code);
        return oldRelationFlags;
    }

    @Override
    public long handleWayTags(ReaderWay way, long allowed, long relationFlags) {
        if (!isAccept(allowed))
            return 0;

        long flags = 0;
        if (!isFerry(allowed)) {
            String sacScale = way.getTag(OSMTags.Keys.SAC_SCALE);
            if (sacScale != null && !"hiking".equals(sacScale)) {
                flags = speedEncoder.setDoubleValue(flags, SLOW_SPEED);
            } else {
                flags = speedEncoder.setDoubleValue(flags, MEAN_SPEED);
            }
            flags |= directionBitMask;

            boolean isRoundabout = way.hasTag(OSMTags.Keys.JUNCTION, "roundabout") || way.hasTag(OSMTags.Keys.JUNCTION, "circular");
            if (isRoundabout)
                flags = setBool(flags, K_ROUNDABOUT, true);

        } else {
            double ferrySpeed = getFerrySpeed(way);
            flags = setSpeed(flags, ferrySpeed);
            flags |= directionBitMask;
        }

        int priorityFromRelation = 0;
        if (relationFlags != 0)
            priorityFromRelation = (int) relationCodeEncoder.getValue(relationFlags);

        flags = priorityWayEncoder.setValue(flags, handlePriority(way, priorityFromRelation));

        return flags;
    }

    @Override
    public double getDouble(long flags, int key) {
        if (key == PriorityWeighting.KEY)
            return (double) priorityWayEncoder.getValue(flags) / BEST.getValue();
        else
            return super.getDouble(flags, key);
    }

    @Override
    public long acceptWay(ReaderWay way) {
        String highwayValue = way.getTag(OSMTags.Keys.HIGHWAY);

        if (highwayValue == null)
            return handleNonHighways(way);

        if (hasTooDifficultSacScale(way))
            return 0;

        // no need to evaluate ferries or fords - already included here
        if (way.hasTag(OSMTags.Keys.FOOT, intendedValues))
            return acceptBit;

        // check access restrictions
        if (way.hasTag(restrictions, restrictedValues) && !getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way))
            return 0;

        if (way.hasTag(OSMTags.Keys.SIDEWALK, usableSidewalkValues))
            return acceptBit;

        if (!allowedHighwayTags.contains(highwayValue))
            return 0;

        if (way.hasTag(OSMTags.Keys.MOTOR_ROAD, "yes"))
            return 0;

        // do not get our feet wet, "yes" is already included above
        if (isBlockFords() && (way.hasTag(OSMTags.Keys.HIGHWAY, "ford") || way.hasTag(OSMTags.Keys.FORD)))
            return 0;

        if (getConditionalTagInspector().isPermittedWayConditionallyRestricted(way))
            return 0;

        return acceptBit;
    }

    /**
     * Method which generates the acceptance flag for ways that are not seen as being highways (such as ferry routes)
     *
     * @param way   The way that is to be assessed
     * @return      The acceptance flag for the way
     */
    private long handleNonHighways(ReaderWay way) {
        long acceptPotentially = 0;

        if (way.hasTag(OSMTags.Keys.ROUTE, ferries)) {
            String footTag = way.getTag(OSMTags.Keys.FOOT);
            if (footTag == null || "yes".equals(footTag))
                acceptPotentially = acceptBit | ferryBit;
        }

        // special case not for all acceptedRailways, only platform
        if (way.hasTag(OSMTags.Keys.RAILWAY, "platform"))
            acceptPotentially = acceptBit;

        if (way.hasTag(OSMTags.Keys.MAN_MADE, "pier"))
            acceptPotentially = acceptBit;

        if (acceptPotentially != 0) {
            if (way.hasTag(restrictions, restrictedValues) && !getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way))
                return 0;
            return acceptPotentially;
        }

        return 0;
    }

    /**
     * Determine if the way is seen as being too difficult based on any sac_scale tags and the information provided in
     * the setup of the object (suitableSacScales)
     *
     * @param way   The way to be assessed
     * @return      Whether the way is too difficult or not
     */
    private boolean hasTooDifficultSacScale(ReaderWay way) {
        String sacScale = way.getTag(OSMTags.Keys.SAC_SCALE);
        return sacScale != null && !suitableSacScales.contains(sacScale);
    }

    /**
     * Assign priorities based on relations and values stored against the way. This is the top level method that calls
     * other priority assessment methods
     *
     * @param way                   The way to be assessed
     * @param priorityFromRelation  The priority obtained from any relations
     * @return                      The overall priority value for the way
     */
    private int handlePriority(ReaderWay way, int priorityFromRelation) {
        TreeMap<Double, Integer> weightToPrioMap = new TreeMap<>();
        if (priorityFromRelation == 0)
            weightToPrioMap.put(0d, UNCHANGED.getValue());
        else
            weightToPrioMap.put(110d, priorityFromRelation);

        assignPriorities(way, weightToPrioMap);

        // pick priority with biggest order value
        return weightToPrioMap.lastEntry().getValue();
    }

    /**
     * @param weightToPrioMap associate a weight with every priority. This sorted map allows
     *                        subclasses to 'insert' more important priorities as well as overwrite determined priorities.
     */
    private void assignPriorities(ReaderWay way, TreeMap<Double, Integer> weightToPrioMap) {
        if (way.hasTag(OSMTags.Keys.FOOT, "designated"))
            weightToPrioMap.put(100d, PREFER.getValue());

        assignSafeHighwayPriority(way, weightToPrioMap);

        assignAvoidHighwayPriority(way, weightToPrioMap);

        assignAvoidUnlessSidewalkPresentPriority(way, weightToPrioMap);

        assignBicycleWayPriority(way, weightToPrioMap);

    }

    /**
     * Update the weight priority map based on values relating to highway types that are identified as being "safe" or
     * with low speeds
     *
     * @param way               The way containing the tag information
     * @param weightToPrioMap   The priority weight map that will have the weightings updated
     */
    void assignSafeHighwayPriority(ReaderWay way, TreeMap<Double, Integer> weightToPrioMap) {
        String highway = way.getTag(OSMTags.Keys.HIGHWAY);
        double maxSpeed = getMaxSpeed(way);

        if (safeHighwayTags.contains(highway) || maxSpeed > 0 && maxSpeed <= 20) {
            if (preferredWayTags.contains(highway))
                weightToPrioMap.put(40d, VERY_NICE.getValue());
            else {
                weightToPrioMap.put(40d, PREFER.getValue());
            }
            assignTunnelPriority(way, weightToPrioMap);
        }
    }

    /**
     * Update the weight priority map based on tunnel information
     *
     * @param way               The way containing the tag information
     * @param weightToPrioMap   The priority weight map that will have the weightings updated
     */
    void assignTunnelPriority(ReaderWay way, TreeMap<Double, Integer> weightToPrioMap) {
        if (way.hasTag(OSMTags.Keys.TUNNEL, intendedValues)) {
            if (way.hasTag(OSMTags.Keys.SIDEWALK, noSidewalkValues))
                weightToPrioMap.put(40d, AVOID_IF_POSSIBLE.getValue());
            else
                weightToPrioMap.put(40d, UNCHANGED.getValue());
        }
    }

    /**
     * Update the weight priority map based on values relating to avoiding highways
     *
     * @param way               The way containing the tag information
     * @param weightToPrioMap   The priority weight map that will have the weightings updated
     */
    private void assignAvoidHighwayPriority(ReaderWay way, TreeMap<Double, Integer> weightToPrioMap) {
        String highway = way.getTag(OSMTags.Keys.HIGHWAY);
        double maxSpeed = getMaxSpeed(way);

        if ((maxSpeed > 50 || avoidHighwayTags.contains(highway))
                && !way.hasTag(OSMTags.Keys.SIDEWALK, usableSidewalkValues)) {
            weightToPrioMap.put(45d, REACH_DEST.getValue());
        }
    }

    /**
     * Mark the way as to be avoided if there is no sidewalk present on highway types identified as needing a sidewalk
     * to be traversed
     *
     * @param way               The way containing the tag information
     * @param weightToPrioMap   The priority weight map that will have the weightings updated
     */
    private void assignAvoidUnlessSidewalkPresentPriority(ReaderWay way, TreeMap<Double, Integer> weightToPrioMap) {
        String highway = way.getTag(OSMTags.Keys.HIGHWAY);
        if (avoidUnlessSidewalkTags.contains(highway) && !way.hasTag(OSMTags.Keys.SIDEWALK, usableSidewalkValues))
            weightToPrioMap.put(45d, AVOID_AT_ALL_COSTS.getValue());
    }

    /**
     * Update the weight priority map based on values relating to bicycle ways.
     *
     * @param way               The way containing the tag information
     * @param weightToPrioMap   The priority weight map that will have the weightings updated
     */
    private void assignBicycleWayPriority(ReaderWay way, TreeMap<Double, Integer> weightToPrioMap) {
        if (way.hasTag(OSMTags.Keys.BICYCLE, "official") || way.hasTag(OSMTags.Keys.BICYCLE, "designated"))
            weightToPrioMap.put(44d, AVOID_IF_POSSIBLE.getValue());
    }

    @Override
    public boolean supports(Class<?> feature) {
        if (super.supports(feature)) {
            return true;
        }

        return PriorityWeighting.class.isAssignableFrom(feature);
    }

    /*
     * This method is a current hack, to allow ferries to be actually faster than our current storable maxSpeed.
     */
    @Override
    public double getSpeed(long flags) {
        double speed = super.getSpeed(flags);
        if (speed == getMaxSpeed()) {
            // We cannot be sure if it was a long or a short trip
            return SHORT_TRIP_FERRY_SPEED;
        }

        return speed;
    }
}
