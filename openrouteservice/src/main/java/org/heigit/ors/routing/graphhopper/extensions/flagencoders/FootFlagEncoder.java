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

package org.heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TransportationMode;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.storage.ConditionalEdges;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.OSMTags;
import org.heigit.ors.routing.graphhopper.extensions.util.PriorityCode;

import java.util.*;

import static com.graphhopper.routing.ev.RouteNetwork.*;
import static com.graphhopper.routing.util.EncodingManager.getKey;
import static org.heigit.ors.routing.graphhopper.extensions.util.PriorityCode.*;

/**
 * This code has been adapted from the original GraphHopper FootFlagEncoder found at
 * https://github.com/graphhopper/graphhopper/blob/master/core/src/main/java/com/graphhopper/routing/util/FootFlagEncoder.java
 *
 * @author Adam Rousell
 * @author Peter Karich
 * @author Nop
 * @author Karl Hübner
 */
public abstract class FootFlagEncoder extends com.graphhopper.routing.util.FootFlagEncoder {
    static final int SLOW_SPEED = 2;
    private static final int MEAN_SPEED = 5;
    static final int FERRY_SPEED = 15;
    public static final String KEY_DESIGNATED = "designated";

    private final Set<String> safeHighwayTags = new HashSet<>();
    private final Set<String> allowedHighwayTags = new HashSet<>();
    private final Set<String> avoidHighwayTags = new HashSet<>();
    Set<String> preferredWayTags = new HashSet<>();
    private final Set<String> avoidUnlessSidewalkTags = new HashSet<>();
    Set<String> suitableSacScales = new HashSet<>();
    // convert network tag of hiking routes into a way route code
    Set<String> usableSidewalkValues = new HashSet<>(5);
    Set<String> noSidewalkValues = new HashSet<>(5);
    protected DecimalEncodedValue priorityWayEncoder;
    protected EnumEncodedValue<RouteNetwork> footRouteEnc;
    Map<RouteNetwork, Integer> routeMap = new HashMap<>();
    private BooleanEncodedValue conditionalAccessEncoder;

    protected void setProperties(PMap properties) {
        this.setProperties(properties, true);
    }

    protected void setProperties(PMap properties, boolean blockFords) {
        this.properties = properties;
        this.blockFords(properties.getBool("block_fords", blockFords));
    }


    FootFlagEncoder(int speedBits, double speedFactor) {
        super(speedBits, speedFactor);
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
                KEY_DESIGNATED,
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

        blockByDefaultBarriers.add("fence");
        passByDefaultBarriers.add("gate");
        passByDefaultBarriers.add("cattle_grid");

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

        routeMap.put(INTERNATIONAL, UNCHANGED.getValue());
        routeMap.put(NATIONAL, UNCHANGED.getValue());
        routeMap.put(REGIONAL, UNCHANGED.getValue());
        routeMap.put(LOCAL, UNCHANGED.getValue());
        routeMap.put(FERRY, AVOID_IF_POSSIBLE.getValue());

        maxPossibleSpeed = FERRY_SPEED;
    }

    public double getMeanSpeed() {
        return MEAN_SPEED;
    }

    @Override
    public void createEncodedValues(List<EncodedValue> registerNewEncodedValue, String prefix, int index) {
        // first two bits are reserved for route handling in superclass
        super.createEncodedValues(registerNewEncodedValue, prefix, index);
        // larger value required - ferries are faster than pedestrians
        registerNewEncodedValue.add(avgSpeedEnc = new UnsignedDecimalEncodedValue(getKey(prefix, "average_speed"), speedBits, speedFactor, false));
        priorityWayEncoder = new UnsignedDecimalEncodedValue(getKey(prefix, FlagEncoderKeys.PRIORITY_KEY), 4, PriorityCode.getFactor(1), false);
        registerNewEncodedValue.add(priorityWayEncoder);
        if (properties.getBool(ConditionalEdges.ACCESS, false)) {
            conditionalAccessEncoder = new SimpleBooleanEncodedValue(EncodingManager.getKey(prefix, ConditionalEdges.ACCESS), true);
            registerNewEncodedValue.add(conditionalAccessEncoder);
        }
        footRouteEnc = getEnumEncodedValue(RouteNetwork.key("foot"), RouteNetwork.class);
    }

    @Override
    public EncodingManager.Access getAccess(ReaderWay way) {
        String highwayValue = way.getTag(OSMTags.Keys.HIGHWAY);

        if (highwayValue == null)
            return handleNonHighways(way);

        if (hasTooDifficultSacScale(way))
            return EncodingManager.Access.CAN_SKIP;

        // no need to evaluate ferries or fords - already included here
        if (way.hasTag(OSMTags.Keys.FOOT, intendedValues))
            return isPermittedWayConditionallyRestricted(way);

        // check access restrictions
        if (way.hasTag(restrictions, restrictedValues))
            return isRestrictedWayConditionallyPermitted(way);

        if (way.hasTag(OSMTags.Keys.SIDEWALK, usableSidewalkValues))
            return isPermittedWayConditionallyRestricted(way);

        if (!allowedHighwayTags.contains(highwayValue))
            return EncodingManager.Access.CAN_SKIP;

        if (way.hasTag(OSMTags.Keys.MOTOR_ROAD, "yes"))
            return EncodingManager.Access.CAN_SKIP;

        // do not get our feet wet, "yes" is already included above
        if (isBlockFords() && (way.hasTag(OSMTags.Keys.HIGHWAY, "ford") || way.hasTag(OSMTags.Keys.FORD)))
            return EncodingManager.Access.CAN_SKIP;

        if (getConditionalTagInspector().isPermittedWayConditionallyRestricted(way))
            return EncodingManager.Access.CAN_SKIP;

        return isPermittedWayConditionallyRestricted(way);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, EncodingManager.Access access) {
        return handleWayTags(edgeFlags, way, access, null);
    }

    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, EncodingManager.Access access, IntsRef relationFlags) {
        if (access.canSkip())
            return edgeFlags;

        Integer priorityFromRelation = routeMap.get(footRouteEnc.getEnum(false, edgeFlags));
        if (!access.isFerry()) {
            String sacScale = way.getTag(OSMTags.Keys.SAC_SCALE);
            if (sacScale != null && !"hiking".equals(sacScale)) {
                avgSpeedEnc.setDecimal(false, edgeFlags, SLOW_SPEED);
            } else {
                avgSpeedEnc.setDecimal(false, edgeFlags, MEAN_SPEED);
            }
            accessEnc.setBool(false, edgeFlags, true);
            accessEnc.setBool(true, edgeFlags, true);
            if (access.isConditional() && conditionalAccessEncoder!=null)
                conditionalAccessEncoder.setBool(false, edgeFlags, true);
        } else {
            double ferrySpeed = ferrySpeedCalc.getSpeed(way);
            setSpeed(false, edgeFlags, ferrySpeed);
        }
        accessEnc.setBool(false, edgeFlags, true);
        accessEnc.setBool(true, edgeFlags, true);

        priorityWayEncoder.setDecimal(false, edgeFlags, PriorityCode.getFactor(handlePriority(way, priorityFromRelation != null ? priorityFromRelation.intValue() : 0)));
        return edgeFlags;
    }


    /**
     * Method which generates the acceptance flag for ways that are not seen as being highways (such as ferry routes)
     *
     * @param way   The way that is to be assessed
     * @return      The acceptance flag for the way
     */
    private EncodingManager.Access handleNonHighways(ReaderWay way) {
        EncodingManager.Access acceptPotentially = EncodingManager.Access.CAN_SKIP;

        if (way.hasTag(OSMTags.Keys.ROUTE, ferries)) {
            String footTag = way.getTag(OSMTags.Keys.FOOT);
            if (footTag == null || intendedValues.contains(footTag))
                acceptPotentially = EncodingManager.Access.FERRY;
        }

        // special case not for all acceptedRailways, only platform
        if (way.hasTag(OSMTags.Keys.RAILWAY, "platform"))
            acceptPotentially = EncodingManager.Access.WAY;

        if (way.hasTag(OSMTags.Keys.MAN_MADE, "pier"))
            acceptPotentially = EncodingManager.Access.WAY;


        // only route via lock_gate if foot-tag allows for it.
        if (way.hasTag(OSMTags.Keys.WATERWAY, "lock_gate")) {
            if (way.hasTag(OSMTags.Keys.FOOT, intendedValues)) {
                acceptPotentially = EncodingManager.Access.WAY;
            }
        }


        if (!acceptPotentially.canSkip()) {
            if (way.hasTag(restrictions, restrictedValues))
                return isRestrictedWayConditionallyPermitted(way, acceptPotentially);
            return isPermittedWayConditionallyRestricted(way, acceptPotentially);
        }

        return EncodingManager.Access.CAN_SKIP;
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
    protected int handlePriority(ReaderWay way, int priorityFromRelation) {
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
        if (way.hasTag(OSMTags.Keys.FOOT, KEY_DESIGNATED))
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

        if (safeHighwayTags.contains(highway) || isValidSpeed(maxSpeed) && maxSpeed <= 20) {
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
        if (way.hasTag(OSMTags.Keys.BICYCLE, "official") || way.hasTag(OSMTags.Keys.BICYCLE, KEY_DESIGNATED))
            weightToPrioMap.put(44d, AVOID_IF_POSSIBLE.getValue());
    }

    @Override
    public boolean supports(Class<?> feature) {
        if (super.supports(feature)) {
            return true;
        }

        return PriorityWeighting.class.isAssignableFrom(feature);
    }

    @Override
    public TransportationMode getTransportationMode() {
        return TransportationMode.FOOT;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final FootFlagEncoder other = (FootFlagEncoder) obj;
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return ("FootFlagEncoder" + this).hashCode();
    }
}
