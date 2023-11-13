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
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.BikeCommonFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TransportationMode;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.storage.ConditionalEdges;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Translation;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.util.PriorityCode;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

import static com.graphhopper.routing.ev.RouteNetwork.*;
import static com.graphhopper.routing.util.EncodingManager.getKey;
import static org.heigit.ors.routing.graphhopper.extensions.util.PriorityCode.*;

/**
 * Defines bit layout of bicycles (not motorcycles) for speed, access and relations (network).
 * <p>
 *
 * @author Peter Karich
 * @author Nop
 * @author ratrun
 */
public abstract class CommonBikeFlagEncoder extends BikeCommonFlagEncoder {
    /**
     * Reports whether this edge is unpaved.
     */
    protected static final int PUSHING_SECTION_SPEED = 4;
    public static final String KEY_BICYCLE = "bicycle";
    public static final String KEY_DESIGNATED = "designated";
    public static final String KEY_OFFICIAL = "official";
    public static final String KEY_UNPAVED = "unpaved";
    public static final String KEY_LIVING_STREET = "living_street";
    public static final String KEY_SERVICE = "service";
    public static final String KEY_STEPS = "steps";
    public static final String KEY_CYCLEWAY = "cycleway";
    public static final String KEY_TRACK = "track";
    public static final String KEY_MOTORWAY = "motorway";
    public static final String KEY_MOTORWAY_LINK = "motorway_link";
    public static final String KEY_HIGHWAY = "highway";
    public static final String KEY_ROUTE = "route";
    public static final String KEY_RAILWAY = "railway";
    public static final String KEY_BICYCLE_ROAD = "bicycle_road";
    public static final String KEY_JUNCTION = "junction";
    public static final String KEY_SEGREGATED = "segregated";
    public static final String KEY_ONEWAY_BICYCLE = "oneway:bicycle";
    public static final String KEY_BRIDLEWAY = "bridleway";

    // Pushing section highways are parts where you need to get off your bike and push it (German: Schiebestrecke)
    protected final HashSet<String> pushingSectionsHighways = new HashSet<>();
    protected final HashSet<String> oppositeLanes = new HashSet<>();
    protected final Set<String> preferHighwayTags = new HashSet<>();
    protected final Set<String> avoidHighwayTags = new HashSet<>();
    protected final Set<String> unpavedSurfaceTags = new HashSet<>();
    private final Map<String, SpeedValue> trackTypeSpeeds = new HashMap<>();
    private final Map<String, SpeedValue> surfaceSpeeds = new HashMap<>();
    private final Set<String> roadValues = new HashSet<>();
    private final Map<String, SpeedValue> highwaySpeeds = new HashMap<>();
    // convert network tag of bicycle routes into a way route code
    DecimalEncodedValue priorityWayEncoder;
    BooleanEncodedValue unpavedEncoder;
    private IntEncodedValue wayTypeEncoder;
    // Car speed limit which switches the preference from UNCHANGED to AVOID_IF_POSSIBLE
    private int avoidSpeedLimit;
    EnumEncodedValue<RouteNetwork> bikeRouteEnc;
    Map<RouteNetwork, Integer> routeMap = new HashMap<>();
    protected boolean conditionalAccess = false;
    // This is the specific bicycle class
    private String classBicycleKey;

    private BooleanEncodedValue conditionalAccessEncoder;

    private static final boolean DEBUG_OUTPUT = false;
    FileWriter logWriter;

    // MARQ24 MOD START
    // MARQ24 ADDON in the case of the RoadBike Encoder we want to skip some
    // conditions...
    private final boolean isRoadBikeEncoder = this instanceof RoadBikeFlagEncoder; // TODO: design: parent class should not need to know of child
    protected static final Logger LOGGER = Logger.getLogger(CommonBikeFlagEncoder.class.getName());
    // MARQ24 MOD END

    // MARQ24 MOD START
    protected CommonBikeFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
        this(speedBits, speedFactor, maxTurnCosts, false);
    }
    // MARQ24 MOD END

    protected void setProperties(PMap properties) {
        blockFords(properties.getBool("block_fords", true));
        conditionalAccess = properties.getBool(ConditionalEdges.ACCESS, false);
    }

    // MARQ24 MOD START
    protected CommonBikeFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts, boolean considerElevation) {
        // MARQ24 MOD END
        super(speedBits, speedFactor, maxTurnCosts);
        // strict set, usually vehicle and agricultural/forestry are ignored by cyclists
        restrictions.addAll(Arrays.asList(KEY_BICYCLE, "vehicle", "access"));
        restrictedValues.add("private");
        restrictedValues.add("no");
        restrictedValues.add("restricted");
        restrictedValues.add("military");
        restrictedValues.add("emergency");

        intendedValues.add("yes");
        intendedValues.add(KEY_DESIGNATED);
        intendedValues.add(KEY_OFFICIAL);
        intendedValues.add("permissive");

        oppositeLanes.add("opposite");
        oppositeLanes.add("opposite_lane");
        oppositeLanes.add("opposite_track");

        passByDefaultBarriers.add("gate");
        passByDefaultBarriers.add("swing_gate");

        blockByDefaultBarriers.add("stile");
        blockByDefaultBarriers.add("turnstile");

        unpavedSurfaceTags.add(KEY_UNPAVED);
        unpavedSurfaceTags.add("gravel");
        unpavedSurfaceTags.add("ground");
        unpavedSurfaceTags.add("dirt");
        unpavedSurfaceTags.add("grass");
        unpavedSurfaceTags.add("compacted");
        unpavedSurfaceTags.add("earth");
        unpavedSurfaceTags.add("fine_gravel");
        unpavedSurfaceTags.add("grass_paver");
        unpavedSurfaceTags.add("ice");
        unpavedSurfaceTags.add("mud");
        unpavedSurfaceTags.add("salt");
        unpavedSurfaceTags.add("sand");
        unpavedSurfaceTags.add("wood");

        roadValues.add(KEY_LIVING_STREET);
        roadValues.add("road");
        roadValues.add(KEY_SERVICE);
        roadValues.add("unclassified");
        roadValues.add("residential");
        roadValues.add("trunk");
        roadValues.add("trunk_link");
        roadValues.add("primary");
        roadValues.add("primary_link");
        roadValues.add("secondary");
        roadValues.add("secondary_link");
        roadValues.add("tertiary");
        roadValues.add("tertiary_link");

        maxPossibleSpeed = 30;

        // MARQ24 MOD START
        // we have to check, WHAT this really does - since when 'enabled' we get 'cracy' detours like this one
        // http://localhost:3035/directions?n1=51.563406&n2=8.713585&n3=16&a=51.566454,8.705764,51.559224,8.707244&b=1a&c=0&g1=-1&g2=0&h2=3&k1=en-US&k2=km
        if (considerElevation) {
            maxPossibleSpeed = (int) getDownhillMaxSpeed();
        }
        // MARQ24 MOD END

        setTrackTypeSpeed("grade1", 18); // paved
        setTrackTypeSpeed("grade2", 12); // now unpaved ...
        setTrackTypeSpeed("grade3", 8);
        setTrackTypeSpeed("grade4", 6);
        setTrackTypeSpeed("grade5", 4); // like sand/grass

        setSurfaceSpeed("paved", 18);
        setSurfaceSpeed("asphalt", 18);
        setSurfaceSpeed("cobblestone", 8);
        setSurfaceSpeed("cobblestone:flattened", 10);
        setSurfaceSpeed("sett", 10);
        setSurfaceSpeed("concrete", 18);
        setSurfaceSpeed("concrete:lanes", 16);
        setSurfaceSpeed("concrete:plates", 16);
        setSurfaceSpeed("paving_stones", 12);
        setSurfaceSpeed("paving_stones:30", 12);
        setSurfaceSpeed(KEY_UNPAVED, 14);
        setSurfaceSpeed("compacted", 16);
        setSurfaceSpeed("dirt", 10);
        setSurfaceSpeed("earth", 12);
        setSurfaceSpeed("fine_gravel", 18);
        setSurfaceSpeed("grass", 8);
        setSurfaceSpeed("grass_paver", 8);
        setSurfaceSpeed("gravel", 12);
        setSurfaceSpeed("ground", 12);
        setSurfaceSpeed("ice", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("metal", 10);
        setSurfaceSpeed("mud", 10);
        setSurfaceSpeed("pebblestone", 16);
        setSurfaceSpeed("salt", 6);
        setSurfaceSpeed("sand", 6);
        setSurfaceSpeed("wood", 6);

        setHighwaySpeed(KEY_LIVING_STREET, 6);
        setHighwaySpeed(KEY_STEPS, PUSHING_SECTION_SPEED / 2);

        final int CYCLEWAY_SPEED = 18;  // Make sure cycleway and path use same speed value, see #634
        setHighwaySpeed(KEY_CYCLEWAY, CYCLEWAY_SPEED);
        setHighwaySpeed("path", 10);
        setHighwaySpeed("footway", 6);
        setHighwaySpeed("pedestrian", 6);
        setHighwaySpeed(KEY_TRACK, 12);
        setHighwaySpeed(KEY_SERVICE, 14);
        setHighwaySpeed("residential", 18);
        // no other highway applies:
        setHighwaySpeed("unclassified", 16);
        // unknown road:
        setHighwaySpeed("road", 12);

        setHighwaySpeed("trunk", 18);
        setHighwaySpeed("trunk_link", 18);
        setHighwaySpeed("primary", 18);
        setHighwaySpeed("primary_link", 18);
        setHighwaySpeed("secondary", 18);
        setHighwaySpeed("secondary_link", 18);
        setHighwaySpeed("tertiary", 18);
        setHighwaySpeed("tertiary_link", 18);

        // special case see tests and #191
        setHighwaySpeed(KEY_MOTORWAY, 18);
        setHighwaySpeed(KEY_MOTORWAY_LINK, 18);
        avoidHighwayTags.add(KEY_MOTORWAY);
        avoidHighwayTags.add(KEY_MOTORWAY_LINK);

        // bridleways are allowed to ride over in some cases
        setHighwaySpeed(KEY_BRIDLEWAY, 6);
        avoidHighwayTags.add(KEY_BRIDLEWAY);

        routeMap.put(INTERNATIONAL, BEST.getValue());
        routeMap.put(NATIONAL, BEST.getValue());
        routeMap.put(REGIONAL, VERY_NICE.getValue());
        routeMap.put(LOCAL, PREFER.getValue());
        routeMap.put(DEPRECATED, REACH_DEST.getValue());
        routeMap.put(MTB, UNCHANGED.getValue());
        routeMap.put(FERRY, AVOID_IF_POSSIBLE.getValue());

        setAvoidSpeedLimit(71);

        if (DEBUG_OUTPUT) {
            try {
                File file = new File("CommonBikeFlagEncoder.log");
                logWriter = new FileWriter(file);
            } catch (Exception ex) {
                LOGGER.warn("Failed to write log file.");
            }
        }
    }

    @Override
    public TransportationMode getTransportationMode() {
        return TransportationMode.BIKE;
    }

    @Override
    public void createEncodedValues(List<EncodedValue> registerNewEncodedValue, String prefix, int index) {
        super.createEncodedValues(registerNewEncodedValue, prefix, index);
        registerNewEncodedValue.add(avgSpeedEnc = new UnsignedDecimalEncodedValue(getKey(prefix, "average_speed"), speedBits, speedFactor, false));
        unpavedEncoder = new SimpleBooleanEncodedValue(getKey(prefix, "paved"), false);
        registerNewEncodedValue.add(unpavedEncoder);
        wayTypeEncoder = new UnsignedIntEncodedValue(getKey(prefix, "waytype"), 2, false);
        registerNewEncodedValue.add(wayTypeEncoder);
        priorityWayEncoder = new UnsignedDecimalEncodedValue(getKey(prefix, "priority"), 4, PriorityCode.getFactor(1), false);
        registerNewEncodedValue.add(priorityWayEncoder);
        if (conditionalAccess) {
            conditionalAccessEncoder = new SimpleBooleanEncodedValue(EncodingManager.getKey(prefix, ConditionalEdges.ACCESS), true);
            registerNewEncodedValue.add(conditionalAccessEncoder);
        }
        bikeRouteEnc = getEnumEncodedValue(RouteNetwork.key("bike"), RouteNetwork.class);
    }

    @Override
    public EncodingManager.Access getAccess(ReaderWay way) {
        String highwayValue = way.getTag(KEY_HIGHWAY);
        if (highwayValue == null) {
            EncodingManager.Access acceptPotentially = EncodingManager.Access.CAN_SKIP;

            if (way.hasTag(KEY_ROUTE, ferries)) {
                // if bike is NOT explicitly tagged allow bike but only if foot is not specified
                String bikeTag = way.getTag(KEY_BICYCLE);
                if (bikeTag == null && !way.hasTag("foot") || "yes".equals(bikeTag)) {
                    acceptPotentially = EncodingManager.Access.FERRY;
                }
            }

            // special case not for all acceptedRailways, only platform
            if (way.hasTag(KEY_RAILWAY, "platform")) {
                acceptPotentially = EncodingManager.Access.WAY;
            }

            if (way.hasTag("man_made", "pier")) {
                acceptPotentially = EncodingManager.Access.WAY;
            }

            if (!acceptPotentially.canSkip()) {
                if (way.hasTag(restrictions, restrictedValues))
                    acceptPotentially = isRestrictedWayConditionallyPermitted(way, acceptPotentially);
                return acceptPotentially;
            }

            return EncodingManager.Access.CAN_SKIP;
        }

        if (!highwaySpeeds.containsKey(highwayValue)) {
            return EncodingManager.Access.CAN_SKIP;
        }

        String sacScale = way.getTag("sac_scale");
        if (sacScale != null) {
            if ((way.hasTag(KEY_HIGHWAY, KEY_CYCLEWAY)) && (way.hasTag("sac_scale", "hiking"))) {
                return EncodingManager.Access.WAY;
            }
            if (!isSacScaleAllowed(sacScale)) {
                return EncodingManager.Access.CAN_SKIP;
            }
        }

        // use the way if it is tagged for bikes
        if (way.hasTag(KEY_BICYCLE, intendedValues)
                || way.hasTag(KEY_BICYCLE, "dismount")
                || way.hasTag(KEY_HIGHWAY, KEY_CYCLEWAY)
                // MARQ24 MOD START
                // Runge: http://www.openstreetmap.org/way/1700503
                || way.hasTag(KEY_BICYCLE_ROAD, "yes")
            // MARQ24 MOD END
        ) {
            return isPermittedWayConditionallyRestricted(way);
        }

        // accept only if explicitly tagged for bike usage
        if (KEY_MOTORWAY.equals(highwayValue) || KEY_MOTORWAY_LINK.equals(highwayValue) || KEY_BRIDLEWAY.equals(highwayValue)) {
            return EncodingManager.Access.CAN_SKIP;
        }

        if (way.hasTag("motorroad", "yes")) {
            return EncodingManager.Access.CAN_SKIP;
        }

        // do not use fords with normal bikes, flagged fords are in included above
        if (isBlockFords() && (way.hasTag(KEY_HIGHWAY, "ford") || way.hasTag("ford"))) {
            return EncodingManager.Access.CAN_SKIP;
        }

        // check access restrictions
        if (way.hasTag(restrictions, restrictedValues))
            return isRestrictedWayConditionallyPermitted(way);

        return isPermittedWayConditionallyRestricted(way);
    }

    boolean isSacScaleAllowed(String sacScale) {
        // other scales are nearly impossible by an ordinary bike, see http://wiki.openstreetmap.org/wiki/Key:sac_scale
        return "hiking".equals(sacScale);
    }

    /**
     * Apply maxspeed: In contrast to the implementation of the AbstractFlagEncoder, we assume that
     * we can reach the maxspeed for bicycles in case that the road type speed is higher and not
     * just only 90%.
     * <p>
     *
     * @param way   needed to retrieve tags
     * @param speed speed guessed e.g. from the road type or other tags
     * @return The assumed average speed.
     */
    @Override
    protected double applyMaxSpeed(ReaderWay way, double speed) {
        double maxSpeed = getMaxSpeed(way);
        if (isValidSpeed(maxSpeed) && maxSpeed < speed) {
            return maxSpeed;
        }
        return speed;
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, EncodingManager.Access access) {
        if (access.canSkip()) {
            return edgeFlags;
        }

        Integer priorityFromRelationInt = routeMap.get(bikeRouteEnc.getEnum(false, edgeFlags));
        int priorityFromRelation = priorityFromRelationInt == null ? 0 : priorityFromRelationInt;

        double wayTypeSpeed = getSpeed(way);
        if (!access.isFerry()) {
            wayTypeSpeed = applyMaxSpeed(way, wayTypeSpeed);
            handleSpeed(edgeFlags, way, wayTypeSpeed);
            handleBikeRelated(edgeFlags, way, priorityFromRelation > UNCHANGED.getValue());
            if (access.isConditional() && conditionalAccessEncoder != null)
                conditionalAccessEncoder.setBool(false, edgeFlags, true);
            boolean isRoundabout = way.hasTag(KEY_JUNCTION, "roundabout") || way.hasTag(KEY_JUNCTION, "circular");
            if (isRoundabout) {
                roundaboutEnc.setBool(false, edgeFlags, true);
            }
        } else {
            double ferrySpeed = ferrySpeedCalc.getSpeed(way);
            handleSpeed(edgeFlags, way, ferrySpeed);
        }

        int priority = handlePriority(way, wayTypeSpeed, priorityFromRelation);
        if (DEBUG_OUTPUT) {
            try {
                logWriter.write("WayID %d RelationPrio %d FinalPrio %d %n".formatted(way.getId(), priorityFromRelation, priority));
                logWriter.flush();
            } catch (Exception ex) {
                LOGGER.warn("Failed to write log file.");
            }
        }
        priorityWayEncoder.setDecimal(false, edgeFlags, PriorityCode.getFactor(priority));
        return edgeFlags;
    }

    int getSpeed(ReaderWay way) {
        int speed = Integer.MIN_VALUE;
        String highwayTag = way.getTag(KEY_HIGHWAY);
        SpeedValue highwaySpeed = highwaySpeeds.get(highwayTag);

        boolean isPushingWay = isPushingSection(way);
        boolean isCyclewayLikeWay = false;
        boolean isSteps = way.hasTag(KEY_HIGHWAY, KEY_STEPS);

        // Under certain conditions we need to increase the speed of pushing sections to the speed of a "highway=cycleway"
        // MARQ24 - so if this is a pushing section (like path, track or footway) BUT have additional bicycle attributes
        // then we treat this way as it was tagged as "cycleway"...
        if (isPushingWay &&
                (
                        (way.hasTag("foot", "yes") && way.hasTag(KEY_SEGREGATED, "yes"))
                                || way.hasTag(KEY_BICYCLE, "yes")
                                || way.hasTag(KEY_BICYCLE, KEY_DESIGNATED)
                                || way.hasTag(KEY_BICYCLE, KEY_OFFICIAL)
                )
                && !isSteps
        ) {
            isCyclewayLikeWay = true;
            highwaySpeed = getHighwaySpeed(KEY_CYCLEWAY);
        }

        String s = way.getTag("surface");
        if (!Helper.isEmpty(s)) {
            SpeedValue surfaceSpeed = surfaceSpeeds.get(s);
            if (surfaceSpeed != null && (!isPushingWay || isCyclewayLikeWay)) {
                // ok if no specific highway speed is set we will use the surface speed...
                if (highwaySpeed == null) {
                    speed = surfaceSpeed.speed;
                } else {
                    speed = calcHighwaySpeedBasedOnSurface(highwaySpeed, surfaceSpeed);
                }
            }
        } else {
            // no SURFACE TAG present...
            String tt = way.getTag("tracktype");
            if (!Helper.isEmpty(tt)) {
                SpeedValue tracktypeSpeed = trackTypeSpeeds.get(tt);
                if (tracktypeSpeed != null && (!isPushingWay || isCyclewayLikeWay)) {
                    if (highwaySpeed == null) {
                        speed = tracktypeSpeed.speed;
                    } else {
                        speed = calcHighwaySpeedBasedOnSurface(highwaySpeed, tracktypeSpeed);
                    }
                }
            }
        }

        // if the speed have not been set yet...
        if (speed == Integer.MIN_VALUE) {
            if (highwaySpeed != null) {
                if (!way.hasTag(KEY_SERVICE)) {
                    speed = highwaySpeed.speed;
                } else {
                    // MARQ24: with other words any 'service' tagged ways will be
                    // considered as min "living street" speed (or slower)...
                    speed = Math.min(highwaySpeeds.get(KEY_LIVING_STREET).speed, highwaySpeed.speed);
                }
            } else {
                speed = PUSHING_SECTION_SPEED;
            }
        }

        // MARQ24 MOD START
        if (speed <= PUSHING_SECTION_SPEED) {
            if (!isSteps) {
                // MARQ24 if we are still on pushing section speed, then we at least double the speed in the case
                // that the way is 'segregated' (but of course we sill ignore steps)...
                // MARQ24 MOD END
                // Increase speed in case of segregated
                if (way.hasTag(KEY_SEGREGATED, "yes")) {
                    speed = PUSHING_SECTION_SPEED * 2;
                }
                // MARQ24 MOD START
            } else {
                // MARQ24: make sure that steps will always get a very slow speed...
                speed = PUSHING_SECTION_SPEED / 2;
            }
        }
        // MARQ24 MOD END

        return speed;
    }

    /*
    The order of speed arguments MAKE a difference !!! When highway & surface SpeedValue have the UpdateType=BOTH
    then the return value will be always the speed of the last speed argument (surface.speed)!!!
    */
    public int calcHighwaySpeedBasedOnSurface(SpeedValue highway, SpeedValue surface) {
        if (highway.speed.equals(surface.speed)) {
            return highway.speed;
        } else if (highway.speed > surface.speed) {
            // highway = 18 (residential)
            // surface = 4 (gravel)
            switch (highway.type) {
                case UPGRADE_ONLY:
                    return highway.speed;

                case DOWNGRADE_ONLY:
                case BOTH:
                default:
                    return switch (surface.type) {
                        case UPGRADE_ONLY -> highway.speed;
                        case DOWNGRADE_ONLY, BOTH -> surface.speed;
                        default -> surface.speed;
                    };
            }
        } else {
            // highway = 8 (cycleway)
            // surface = 18 (asphalt)
            switch (highway.type) {
                case DOWNGRADE_ONLY:
                    return highway.speed;
                case UPGRADE_ONLY:
                case BOTH:
                default:
                    return switch (surface.type) {
                        case DOWNGRADE_ONLY -> highway.speed;
                        case UPGRADE_ONLY, BOTH -> surface.speed;
                        default -> surface.speed;
                    };
            }
        }
    }

    String getWayName(int pavementType, int wayType, Translation tr) {
        String pavementName = "";
        if (pavementType == 1)
            pavementName = tr.tr(KEY_UNPAVED);

        String wayTypeName = switch (wayType) {
            case 0 -> "";
            case 1 -> tr.tr("off_bike");
            case 2 -> tr.tr(KEY_CYCLEWAY);
            case 3 -> tr.tr("small_way");
            default -> "";
        };

        if (pavementName.isEmpty()) {
            if (wayType == 0 || wayType == 3)
                return "";
            return wayTypeName;
        } else if (wayTypeName.isEmpty())
            return pavementName;
        else
            return wayTypeName + ", " + pavementName;
    }

    /**
     * In this method we prefer cycleways or roads with designated bike access and avoid big roads
     * or roads with trams or pedestrian.
     * <p>
     *
     * @return new priority based on priorityFromRelation and on the tags in ReaderWay.
     */
    protected int handlePriority(ReaderWay way, double wayTypeSpeed, int priorityFromRelation) {
        TreeMap<Double, Integer> weightToPrioMap = new TreeMap<>();
        if (priorityFromRelation == 0)
            weightToPrioMap.put(0d, UNCHANGED.getValue());
        else
            weightToPrioMap.put(110d, priorityFromRelation);

        collect(way, wayTypeSpeed, weightToPrioMap);

        // pick priority with biggest order value
        return weightToPrioMap.lastEntry().getValue();
    }

    // Conversion of class value to priority. See http://wiki.openstreetmap.org/wiki/Class:bicycle
    private PriorityCode convertClassValueToPriority(String tagvalue) {
        int classvalue;
        try {
            classvalue = Integer.parseInt(tagvalue);
        } catch (NumberFormatException e) {
            return UNCHANGED;
        }

        return switch (classvalue) {
            case 3 -> BEST;
            case 2 -> VERY_NICE;
            case 1 -> PREFER;
            case 0 -> UNCHANGED;
            case -1 -> AVOID_IF_POSSIBLE;
            case -2 -> REACH_DEST;
            case -3 -> AVOID_AT_ALL_COSTS;
            default -> UNCHANGED;
        };
    }

    /**
     * @param weightToPrioMap associate a weight with every priority. This sorted map allows
     *                        subclasses to 'insert' more important priorities as well as overwrite determined priorities.
     */
    void collect(ReaderWay way, double wayTypeSpeed, TreeMap<Double, Integer> weightToPrioMap) {
        String service = way.getTag(KEY_SERVICE);
        String highway = way.getTag(KEY_HIGHWAY);
        // MARQ24 MOD START
        if (!isRoadBikeEncoder) {
            // MARQ24 MOD END
            // MARQ24 MOD START
            if (way.hasTag(KEY_BICYCLE, KEY_DESIGNATED) || way.hasTag(KEY_BICYCLE, KEY_OFFICIAL) || way.hasTag(KEY_BICYCLE_ROAD, "yes")) {
                // MARQ24 MOD END
                if ("path".equals(highway)) {
                    weightToPrioMap.put(100d, VERY_NICE.getValue());
                } else {
                    weightToPrioMap.put(100d, PREFER.getValue());
                }
            }
            if (KEY_CYCLEWAY.equals(highway)) {
                if (way.hasTag("foot", intendedValues) && !way.hasTag(KEY_SEGREGATED, "yes")) {
                    weightToPrioMap.put(100d, PREFER.getValue());
                } else {
                    weightToPrioMap.put(100d, VERY_NICE.getValue());
                }
            }
            // MARQ24 MOD START
        }
        // MARQ24 MOD END

        double maxSpeed = getMaxSpeed(way);
        if (preferHighwayTags.contains(highway) || this.isValidSpeed(maxSpeed) && maxSpeed <= 30) {
            if (!this.isValidSpeed(maxSpeed) || maxSpeed < avoidSpeedLimit) {
                weightToPrioMap.put(40d, PREFER.getValue());
                if (way.hasTag("tunnel", intendedValues)) {
                    weightToPrioMap.put(40d, UNCHANGED.getValue());
                }
            }
        } else if (avoidHighwayTags.contains(highway) || maxSpeed >= avoidSpeedLimit && !KEY_TRACK.equals(highway)) {
            weightToPrioMap.put(50d, REACH_DEST.getValue());
            if (way.hasTag("tunnel", intendedValues)) {
                weightToPrioMap.put(50d, AVOID_AT_ALL_COSTS.getValue());
            }
        }

        if (pushingSectionsHighways.contains(highway)
                || "parking_aisle".equals(service)) {
            int pushingSectionPrio = AVOID_IF_POSSIBLE.getValue();
            // MARQ24 MOD START
            if (!isRoadBikeEncoder) {
                // MARQ24 MOD END
                if (way.hasTag(KEY_BICYCLE, "use_sidepath") || way.hasTag(KEY_BICYCLE, "yes") || way.hasTag(KEY_BICYCLE, "permissive")) {
                    pushingSectionPrio = PREFER.getValue();
                }
                if (way.hasTag(KEY_BICYCLE, KEY_DESIGNATED) || way.hasTag(KEY_BICYCLE, KEY_OFFICIAL)) {
                    pushingSectionPrio = VERY_NICE.getValue();
                }
                // MARQ24 MOD START
            }
            // MARQ24 MOD END

            if (way.hasTag("foot", "yes")) {
                pushingSectionPrio = Math.max(pushingSectionPrio - 1, WORST.getValue());
                if (!isRoadBikeEncoder && way.hasTag(KEY_SEGREGATED, "yes")) {
                    pushingSectionPrio = Math.min(pushingSectionPrio + 1, BEST.getValue());
                }
            }
            weightToPrioMap.put(100d, pushingSectionPrio);
        }

        if (way.hasTag(KEY_RAILWAY, "tram")) {
            weightToPrioMap.put(50d, AVOID_AT_ALL_COSTS.getValue());
        }

        String classBicycleValue = way.getTag(classBicycleKey);
        if (classBicycleValue != null) {
            // We assume that humans are better in classifying preferences compared to our algorithm above -> weight = 100
            weightToPrioMap.put(100d, convertClassValueToPriority(classBicycleValue).getValue());
        } else {
            String classBicycle = way.getTag("class:bicycle");
            if (classBicycle != null) {
                weightToPrioMap.put(100d, convertClassValueToPriority(classBicycle).getValue());
            }
        }

        // Increase the priority for scenic routes or in case that maxspeed limits our average speed as compensation. See #630
        if ((way.hasTag("scenic", "yes") || (maxSpeed > 0 && maxSpeed < wayTypeSpeed)) && weightToPrioMap.lastEntry().getValue() < BEST.getValue()) {
            // Increase the prio by one step
            weightToPrioMap.put(110d, weightToPrioMap.lastEntry().getValue() + 1);
        }
    }

    /**
     * Handle surface and wayType encoding
     */
    void handleBikeRelated(IntsRef edgeFlags, ReaderWay way, boolean partOfCycleRelation) {
        String surfaceTag = way.getTag("surface");
        String highway = way.getTag(KEY_HIGHWAY);
        String trackType = way.getTag("tracktype");

        // Populate unpavedBit
        if (KEY_TRACK.equals(highway) && (!"grade1".equals(trackType))
                || "path".equals(highway) && surfaceTag == null
                || unpavedSurfaceTags.contains(surfaceTag)) {
            unpavedEncoder.setBool(false, edgeFlags, true);
        }

        WayType wayType;
        if (roadValues.contains(highway)) {
            wayType = WayType.ROAD;
        } else {
            wayType = WayType.OTHER_SMALL_WAY;
        }

        boolean isPushingSection = isPushingSection(way);
        if (isPushingSection || KEY_STEPS.equals(highway)) {
            wayType = WayType.PUSHING_SECTION;
        } else {
            // boost "none identified" partOfCycleRelation
            if (!isRoadBikeEncoder && partOfCycleRelation) {
                wayType = WayType.CYCLEWAY;
            }

            if (way.hasTag(KEY_BICYCLE, intendedValues) || KEY_CYCLEWAY.equals(highway)) {
                wayType = WayType.CYCLEWAY;
            }
        }
        wayTypeEncoder.setInt(false, edgeFlags, wayType.getValue());
    }

    boolean isPushingSection(ReaderWay way) {
        // MARQ24 MOD START
        return way.hasTag(KEY_HIGHWAY, pushingSectionsHighways) || way.hasTag(KEY_RAILWAY, "platform") || way.hasTag(KEY_BICYCLE, "dismount") || way.hasTag(KEY_ROUTE, ferries); // Runge
        // MARQ24 MOD END
    }

    protected void handleSpeed(IntsRef edgeFlags, ReaderWay way, double speed) {
        String bicycleForwardTag = "bicycle:forward";
        avgSpeedEnc.setDecimal(false, edgeFlags, speed);
        // handle oneways
        boolean isOneway = way.hasTag("oneway", oneways)
                || way.hasTag(KEY_ONEWAY_BICYCLE, oneways)
                || way.hasTag("vehicle:backward")
                || way.hasTag("vehicle:forward")
                || way.hasTag(bicycleForwardTag, "yes")
                || way.hasTag(bicycleForwardTag, "no");
        //MARQ24 MOD START
        if (!way.hasTag(KEY_BICYCLE_ROAD, "yes") && (isOneway || way.hasTag(KEY_JUNCTION, "roundabout"))
                //MARQ24 MOD END
                && !way.hasTag(KEY_ONEWAY_BICYCLE, "no")
                && !way.hasTag("bicycle:backward")
                && !way.hasTag(KEY_CYCLEWAY, oppositeLanes)
                && !way.hasTag("cycleway:left", oppositeLanes)
                && !way.hasTag("cycleway:right", oppositeLanes)) {
            boolean isBackward = way.hasTag("oneway", "-1")
                    || way.hasTag(KEY_ONEWAY_BICYCLE, "-1")
                    || way.hasTag("vehicle:forward", "no")
                    || way.hasTag(bicycleForwardTag, "no");
            accessEnc.setBool(isBackward, edgeFlags, true);
        } else {
            accessEnc.setBool(false, edgeFlags, true);
            accessEnc.setBool(true, edgeFlags, true);
        }
    }

    protected void setHighwaySpeed(String highway, int speed) {
        highwaySpeeds.put(highway, new SpeedValue(speed));
    }

    protected void setHighwaySpeed(String highway, SpeedValue speed) {
        highwaySpeeds.put(highway, speed);
    }

    SpeedValue getHighwaySpeed(String key) {
        return highwaySpeeds.get(key);
    }

    protected void setTrackTypeSpeed(String tracktype, int speed) {
        trackTypeSpeeds.put(tracktype, new SpeedValue(speed));
    }

    protected void setTrackTypeSpeed(String tracktype, SpeedValue speed) {
        trackTypeSpeeds.put(tracktype, speed);
    }

    protected void setSurfaceSpeed(String surface, int speed) {
        surfaceSpeeds.put(surface, new SpeedValue(speed));
    }

    protected void setSurfaceSpeed(String surface, SpeedValue speed) {
        surfaceSpeeds.put(surface, speed);
    }

    SpeedValue getSurfaceSpeed(String key) {
        return surfaceSpeeds.get(key);
    }

    void addPushingSection(String highway) {
        pushingSectionsHighways.add(highway);
    }

    @Override
    public boolean supports(Class<?> feature) {
        if (super.supports(feature)) {
            return true;
        }
        return PriorityWeighting.class.isAssignableFrom(feature);
    }

    public void setAvoidSpeedLimit(int limit) {
        avoidSpeedLimit = limit;
    }

    protected void setSpecificClassBicycle(String subkey) {
        classBicycleKey = "class:bicycle:" + subkey;
    }

    private enum WayType {
        ROAD(0),
        PUSHING_SECTION(1),
        CYCLEWAY(2),
        OTHER_SMALL_WAY(3);

        private final int value;

        WayType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    protected enum UpdateType {
        UPGRADE_ONLY,
        DOWNGRADE_ONLY,
        BOTH
    }

    protected static class SpeedValue {
        private final Integer speed;
        private UpdateType type = UpdateType.BOTH;

        private SpeedValue(Integer speed) {
            this.speed = speed;
        }

        protected SpeedValue(Integer speed, UpdateType type) {
            this.speed = speed;
            this.type = type;
        }

        public String toString() {
            return switch (type) {
                case BOTH -> speed + " [BOTH]";
                case UPGRADE_ONLY -> speed + " [UP]";
                case DOWNGRADE_ONLY -> speed + " [DOWN]";
            };
        }
    }

    // MARQ24 MOD START
    protected abstract double getDownhillMaxSpeed();
    // MARQ24 MOD END

    @Override
    public int hashCode() {
        return ("CommonBikeFlagEnc" + this).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CommonBikeFlagEncoder cast = (CommonBikeFlagEncoder) obj;
        return toString().equals(cast.toString());
    }

    public abstract double getMeanSpeed();
}
