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
package heigit.ors.routing.graphhopper.extensions.flagencoders.bike;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.util.*;
import org.apache.log4j.Logger;

import java.util.*;

import static com.graphhopper.routing.util.PriorityCode.*;
import static com.graphhopper.util.Helper.keepIn;

/**
 * Defines bit layout of bicycles (not motorcycles) for speed, access and relations (network).
 * <p>
 *
 * @author Peter Karich
 * @author Nop
 * @author ratrun
 */
abstract public class CommonBikeFlagEncoder extends AbstractFlagEncoder {
    /**
     * Reports whether this edge is unpaved.
     */
    public static final int K_UNPAVED = 100;
    protected static final int PUSHING_SECTION_SPEED = 4;
    // Pushing section heighways are parts where you need to get off your bike and push it (German: Schiebestrecke)
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
    private final Map<String, Integer> bikeNetworkToCode = new HashMap<>();
    protected EncodedValue relationCodeEncoder;
    EncodedValue priorityWayEncoder;
    private long unpavedBit = 0;
    private EncodedValue wayTypeEncoder;
    // Car speed limit which switches the preference from UNCHANGED to AVOID_IF_POSSIBLE
    private int avoidSpeedLimit;

    // This is the specific bicycle class
    private String classBicycleKey;

    // MARQ24 MOD START
    // MARQ24 ADDON in the case of the RoadBike Encoder we want to skip some
    // conditions...
    private boolean isRoadBikeEncoder = this instanceof RoadBikeFlagEncoder;
    protected static final Logger LOGGER = Logger.getLogger(CommonBikeFlagEncoder.class.getName());
    // MARQ24 MOD END

    // MARQ24 MOD START
    protected CommonBikeFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
        this(speedBits, speedFactor, maxTurnCosts, false);
    }
    // MARQ24 MOD END

    // MARQ24 MOD START
    protected CommonBikeFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts, boolean considerElevation) {
    // MARQ24 MOD END
        super(speedBits, speedFactor, maxTurnCosts);
        // strict set, usually vehicle and agricultural/forestry are ignored by cyclists
        restrictions.addAll(Arrays.asList("bicycle", "vehicle", "access"));
        restrictedValues.add("private");
        restrictedValues.add("no");
        restrictedValues.add("restricted");
        restrictedValues.add("military");
        restrictedValues.add("emergency");

        intendedValues.add("yes");
        intendedValues.add("designated");
        intendedValues.add("official");
        intendedValues.add("permissive");

        oppositeLanes.add("opposite");
        oppositeLanes.add("opposite_lane");
        oppositeLanes.add("opposite_track");

        setBlockByDefault(false);
        potentialBarriers.add("gate");
        // potentialBarriers.add("lift_gate");
        potentialBarriers.add("swing_gate");

        absoluteBarriers.add("stile");
        absoluteBarriers.add("turnstile");

        unpavedSurfaceTags.add("unpaved");
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

        roadValues.add("living_street");
        roadValues.add("road");
        roadValues.add("service");
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
        setConsiderElevation(considerElevation);
        //setConsiderElevation(false);
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
        setSurfaceSpeed("unpaved", 14);
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

        setHighwaySpeed("living_street", 6);
        setHighwaySpeed("steps", PUSHING_SECTION_SPEED / 2);

        final int CYCLEWAY_SPEED = 18;  // Make sure cycleway and path use same speed value, see #634
        setHighwaySpeed("cycleway", CYCLEWAY_SPEED);
        setHighwaySpeed("path", 10);
        setHighwaySpeed("footway", 6);
        setHighwaySpeed("pedestrian", 6);
        setHighwaySpeed("track", 12);
        setHighwaySpeed("service", 14);
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
        setHighwaySpeed("motorway", 18);
        setHighwaySpeed("motorway_link", 18);
        avoidHighwayTags.add("motorway");
        avoidHighwayTags.add("motorway_link");

        setCyclingNetworkPreference("icn", BEST.getValue());
        setCyclingNetworkPreference("ncn", BEST.getValue());
        setCyclingNetworkPreference("rcn", VERY_NICE.getValue());
        setCyclingNetworkPreference("lcn", PREFER.getValue());
        setCyclingNetworkPreference("mtb", UNCHANGED.getValue());

        setCyclingNetworkPreference("deprecated", AVOID_AT_ALL_COSTS.getValue());

        setAvoidSpeedLimit(71);
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public int defineWayBits(int index, int shift) {
        // first two bits are reserved for route handling in superclass
        shift = super.defineWayBits(index, shift);
        speedEncoder = new EncodedDoubleValue("Speed", shift, speedBits, speedFactor, highwaySpeeds.get("cycleway").speed, maxPossibleSpeed);
        shift += speedEncoder.getBits();

        unpavedBit = 1L << shift++;
        // 2 bits
        // MARQ24 2018/07/08 WayType-Encoder has NO IMPACT on the actual routing - the only place where it was/is used
        // is in the TurnInstructions (where an different annotations will be added once a different WAYTYPE are present
        wayTypeEncoder = new EncodedValue("WayType", shift, 2, 1, 0, 3, true);
        shift += wayTypeEncoder.getBits();

        priorityWayEncoder = new EncodedValue("PreferWay", shift, 3, 1, 0, 7);
        shift += priorityWayEncoder.getBits();

        // MARQ24 MOD START
        if (isConsiderElevation()) {
            reverseSpeedEncoder = new EncodedDoubleValue("Reverse Speed", shift, speedBits, speedFactor, getHighwaySpeed("cycleway").speed, maxPossibleSpeed);
            shift += reverseSpeedEncoder.getBits();
        }
        // MARQ24 MOD END
        return shift;
    }

    @Override
    public int defineRelationBits(int index, int shift) {
        relationCodeEncoder = new EncodedValue("RelationCode", shift, 3, 1, 0, 7);
        return shift + relationCodeEncoder.getBits();
    }

    @Override
    public long acceptWay(ReaderWay way) {
        String highwayValue = way.getTag("highway");
        if (highwayValue == null) {
            long acceptPotentially = 0;

            if (way.hasTag("route", ferries)) {
                // if bike is NOT explicitly tagged allow bike but only if foot is not specified
                String bikeTag = way.getTag("bicycle");
                if (bikeTag == null && !way.hasTag("foot") || "yes".equals(bikeTag)) {
                    acceptPotentially = acceptBit | ferryBit;
                }
            }

            // special case not for all acceptedRailways, only platform
            if (way.hasTag("railway", "platform")) {
                acceptPotentially = acceptBit;
            }

            if (way.hasTag("man_made", "pier")) {
                acceptPotentially = acceptBit;
            }

            if (acceptPotentially != 0) {
                if (way.hasTag(restrictions, restrictedValues) && !getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way)){
                    return 0;
                }
                return acceptPotentially;
            }

            return 0;
        }

        if (!highwaySpeeds.containsKey(highwayValue)) {
            return 0;
        }

        String sacScale = way.getTag("sac_scale");
        if (sacScale != null) {
            if ((way.hasTag("highway", "cycleway")) && (way.hasTag("sac_scale", "hiking"))) {
                return acceptBit;
            }
            if (!isSacScaleAllowed(sacScale)) {
                return 0;
            }
        }

        // use the way if it is tagged for bikes
        if (way.hasTag("bicycle", intendedValues)
                || way.hasTag("bicycle", "dismount")
                || way.hasTag("highway", "cycleway")
                // MARQ24 MOD START
                // Runge: http://www.openstreetmap.org/way/1700503
                || way.hasTag("bicycle_road", "yes")
                // MARQ24 MOD END
        ){
            return acceptBit;
        }

        // accept only if explicitly tagged for bike usage
        if ("motorway".equals(highwayValue) || "motorway_link".equals(highwayValue)) {
            return 0;
        }

        if (way.hasTag("motorroad", "yes")) {
            return 0;
        }

        // do not use fords with normal bikes, flagged fords are in included above
        if (isBlockFords() && (way.hasTag("highway", "ford") || way.hasTag("ford"))) {
            return 0;
        }

        // check access restrictions
        if (way.hasTag(restrictions, restrictedValues) && !getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way)) {
            return 0;
        }

        if (getConditionalTagInspector().isPermittedWayConditionallyRestricted(way)){
            return 0;
        }else {
            return acceptBit;
        }
    }

    boolean isSacScaleAllowed(String sacScale) {
        // other scales are nearly impossible by an ordinary bike, see http://wiki.openstreetmap.org/wiki/Key:sac_scale
        return "hiking".equals(sacScale);
    }

    @Override
    public long handleRelationTags(ReaderRelation relation, long oldRelationFlags) {
        int code = 0;
        if (relation.hasTag("route", "bicycle")) {
            Integer val = bikeNetworkToCode.get(relation.getTag("network"));
            if (val != null) {
                code = val;
            }else {
                code = PriorityCode.PREFER.getValue();  // Assume priority of network "lcn" as bicycle route default
            }
        } else if (relation.hasTag("route", "ferry")) {
            code = AVOID_IF_POSSIBLE.getValue();
        }

        int oldCode = (int) relationCodeEncoder.getValue(oldRelationFlags);
        if (oldCode < code) {
            return relationCodeEncoder.setValue(0, code);
        }
        return oldRelationFlags;
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
        if (maxSpeed >= 0) {
            // We strictly obay speed limits, see #600
            if (maxSpeed < speed) {
                return maxSpeed;
            }
        }
        return speed;
    }

    @Override
    public long handleWayTags(ReaderWay way, long allowed, long relationFlags) {
        if (!isAccept(allowed)) {
            return 0;
        }
        long flags = 0;
        double wayTypeSpeed = getSpeed(way);
        if (!isFerry(allowed)) {
            wayTypeSpeed = applyMaxSpeed(way, wayTypeSpeed);
            flags = handleSpeed(way, wayTypeSpeed, flags);
            flags = handleBikeRelated(way, flags, relationFlags > UNCHANGED.getValue());
            boolean isRoundabout = way.hasTag("junction", "roundabout") || way.hasTag("junction", "circular");
            if (isRoundabout) {
                flags = setBool(flags, K_ROUNDABOUT, true);
            }
        } else {
            double ferrySpeed = getFerrySpeed(way);
            flags = handleSpeed(way, ferrySpeed, flags);
            flags |= directionBitMask;
        }
        int priorityFromRelation = 0;
        if (relationFlags != 0) {
            priorityFromRelation = (int) relationCodeEncoder.getValue(relationFlags);
        }

        flags = priorityWayEncoder.setValue(flags, handlePriority(way, wayTypeSpeed, priorityFromRelation));
        return flags;
    }

    int getSpeed(ReaderWay way) {
        int speed = Integer.MIN_VALUE;
        String highwayTag = way.getTag("highway");
        SpeedValue highwaySpeed = highwaySpeeds.get(highwayTag);

        boolean isPushingWay = isPushingSection(way);
        boolean isCyclewayLikeWay = false;
        boolean isSteps = way.hasTag("highway", "steps");

        // Under certain conditions we need to increase the speed of pushing sections to the speed of a "highway=cycleway"
        // MARQ24 - so if this is a pushing section (like path, track or footway) BUT have additional bicycle attributes
        // then we treat this way as it was tagged as "cycleway"...
        if (isPushingWay &&
            (
                (way.hasTag("foot", "yes") && way.hasTag("segregated", "yes"))
                || way.hasTag("bicycle", "yes")
                || way.hasTag("bicycle", "designated")
                || way.hasTag("bicycle", "official")
            )
        ){
            // MARQ24 - but still ignoring STEPS!!!
            if (!isSteps) {
                isCyclewayLikeWay = true;
                highwaySpeed = getHighwaySpeed("cycleway");
            }
        }

        String s = way.getTag("surface");
        if (!Helper.isEmpty(s)) {
            SpeedValue surfaceSpeed = surfaceSpeeds.get(s);
            if (surfaceSpeed != null) {
                if(!isPushingWay || isCyclewayLikeWay) {
                    // ok if no specific highway speed is set we will use the surface speed...
                    if(highwaySpeed == null){
                        speed = surfaceSpeed.speed;
                    }else{
                        speed = calcHighwaySpeedBasedOnSurface(highwaySpeed, surfaceSpeed);
                    }
                }
            }
        } else {
            // no SURFACE TAG present...
            String tt = way.getTag("tracktype");
            if (!Helper.isEmpty(tt)) {
                SpeedValue tracktypeSpeed = trackTypeSpeeds.get(tt);
                if (tracktypeSpeed != null) {
                    if(!isPushingWay || isCyclewayLikeWay) {
                        if(highwaySpeed == null){
                            speed = tracktypeSpeed.speed;
                        }else{
                            speed = calcHighwaySpeedBasedOnSurface(highwaySpeed, tracktypeSpeed);
                        }
                    }
                }
            }
        }

        // if the speed have not been set yet...
        if (speed == Integer.MIN_VALUE){
            if(highwaySpeed != null) {
                if (!way.hasTag("service")) {
                    speed = highwaySpeed.speed;
                } else {
                    // MARQ24: with other words any 'service' tagged ways will be
                    // considered as min "living street" speed (or slower)...
                    speed = Math.min(highwaySpeeds.get("living_street").speed, highwaySpeed.speed);
                }
            }else {
                speed = PUSHING_SECTION_SPEED;
            }
        }

        // MARQ24 MOD START -> 2018/07/08 - REMOVED COMPLETE SECTION!
        // IMHO there is no need to check ONCE again, if we are faster then pushing speed, and decrease the speed
        // to PUSHING_SECTION_SPEED ['steps' handling will done later]
        /*
        // Until now we assumed that the way is no pushing section
        // Now we check that, but only in case that our speed is bigger compared to the PUSHING_SECTION_SPEED
        if (speed > PUSHING_SECTION_SPEED && (way.hasTag("highway", pushingSectionsHighways) || way.hasTag("bicycle", "dismount"))) {
            // MARQ24 MOD START
            // MARQ24 MOD END
            if (!way.hasTag("bicycle", intendedValues)) {
                // Here we set the speed for pushing sections and set speed for steps as even lower:
                if (isSteps) {
                    speed = PUSHING_SECTION_SPEED / 2;
                }else {
                    speed = PUSHING_SECTION_SPEED;
                }
            // MARQ24 MOD START
            //} else if (way.hasTag("bicycle", "designated") || way.hasTag("bicycle", "official")) {
            } else if (way.hasTag("bicycle", "designated") || way.hasTag("bicycle", "official") || way.hasTag("bicycle", "yes")) {
            // MARQ24 MOD END
                // Here we handle the cases where the OSM tagging results in something similar to "highway=cycleway"
                // MARQ24 - WHY the heck we should set the speed again to the specified 'cycleway' speed - even if
                // we are in a pushing section ???! - WHEN we would ignore "pushing speed" (in the case that the
                // highway have an osm bicycle attribute, then this should ne the previous calculated speed based on
                // surface and track type! - so I will remove the original line!
                // MARQ24 MOD START
                //speed = highwaySpeeds.get("cycleway").speed;
                // MARQ24 MOD END
            } else {
                speed = PUSHING_SECTION_SPEED;
            }
        }
        // MARQ24 MOD END */

        // MARQ24 MOD START
        if(speed <= PUSHING_SECTION_SPEED) {
            if (!isSteps) {
                // MARQ24 if we are still on pushing section speed, then we at least double the speed in the case
                // that the way is 'segregated' (but of course we sill ignore steps)...
        // MARQ24 MOD END
                // Increase speed in case of segregated
                if (way.hasTag("segregated", "yes")) {
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
    public int calcHighwaySpeedBasedOnSurface(SpeedValue highway, SpeedValue surface){
        if(highway.speed == surface.speed){
            return highway.speed;
        }else if(highway.speed > surface.speed){
            // highway = 18 (residential)
            // surface = 4 (gravel)
            switch (highway.type){
                case UPGRADE_ONLY:
                    return highway.speed;

                case DOWNGRADE_ONLY:
                case BOTH:
                default:
                    switch (surface.type){
                        case UPGRADE_ONLY:
                            return highway.speed;
                        case DOWNGRADE_ONLY:
                        case BOTH:
                        default:
                            return surface.speed;
                    }
            }
        }else /*if(highway.speed < surface.speed)*/ {
            // highway = 8 (cycleway)
            // surface = 18 (asphalt)
            switch (highway.type){
                case UPGRADE_ONLY:
                case BOTH:
                default:
                    switch (surface.type){
                        case UPGRADE_ONLY:
                        case BOTH:
                        default:
                            return surface.speed;
                        case DOWNGRADE_ONLY:
                            return highway.speed;
                    }

                case DOWNGRADE_ONLY:
                    return highway.speed;
            }
        }
    }

    @Override
    public InstructionAnnotation getAnnotation(long flags, Translation tr) {
        int paveType = 0; // paved
        if (isBool(flags, K_UNPAVED)) {
            paveType = 1; // unpaved
        }
        int wayType = (int) wayTypeEncoder.getValue(flags);
        String wayName = getWayName(paveType, wayType, tr);
        return new InstructionAnnotation(0, wayName);
    }

    String getWayName(int pavementType, int wayType, Translation tr) {
        String pavementName = "";
        if (pavementType == 1)
            pavementName = tr.tr("unpaved");

        String wayTypeName = "";
        switch (wayType) {
            case 0:
                wayTypeName = "";
                break;
            case 1:
                wayTypeName = tr.tr("off_bike");
                break;
            case 2:
                wayTypeName = tr.tr("cycleway");
                break;
            case 3:
                wayTypeName = tr.tr("small_way");
                break;
        }

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

        switch (classvalue) {
            case 3:
                return BEST;
            case 2:
                return VERY_NICE;
            case 1:
                return PREFER;
            case 0:
                return UNCHANGED;
            case -1:
                return AVOID_IF_POSSIBLE;
            case -2:
                return REACH_DEST;
            case -3:
                return AVOID_AT_ALL_COSTS;
            default:
                return UNCHANGED;
        }
    }

    /**
     * @param weightToPrioMap associate a weight with every priority. This sorted map allows
     *                        subclasses to 'insert' more important priorities as well as overwrite determined priorities.
     */
    void collect(ReaderWay way, double wayTypeSpeed, TreeMap<Double, Integer> weightToPrioMap) {
        String service = way.getTag("service");
        String highway = way.getTag("highway");
        // MARQ24 MOD START
        if(!isRoadBikeEncoder){
        // MARQ24 MOD END
            // MARQ24 MOD START
            //if (way.hasTag("bicycle", "designated") || way.hasTag("bicycle", "official")) {
            if (way.hasTag("bicycle", "designated") || way.hasTag("bicycle", "official") || way.hasTag("bicycle_road", "yes")) {
            // MARQ24 MOD END
                if ("path".equals(highway)) {
                    weightToPrioMap.put(100d, VERY_NICE.getValue());
                } else {
                    weightToPrioMap.put(100d, PREFER.getValue());
                }
            }
            if ("cycleway".equals(highway)) {
                if (way.hasTag("foot", intendedValues) && !way.hasTag("segregated", "yes")){
                    weightToPrioMap.put(100d, PREFER.getValue());
                } else {
                    weightToPrioMap.put(100d, VERY_NICE.getValue());
                }
            }
        // MARQ24 MOD START
        }
        // MARQ24 MOD END

        double maxSpeed = getMaxSpeed(way);
        if (preferHighwayTags.contains(highway) || maxSpeed > 0 && maxSpeed <= 30) {
            if (maxSpeed < avoidSpeedLimit) {
                weightToPrioMap.put(40d, PREFER.getValue());
                if (way.hasTag("tunnel", intendedValues)) {
                    weightToPrioMap.put(40d, UNCHANGED.getValue());
                }
            }
        } else if (avoidHighwayTags.contains(highway) || maxSpeed >= avoidSpeedLimit && !"track".equals(highway)) {
            weightToPrioMap.put(50d, REACH_DEST.getValue());
            if (way.hasTag("tunnel", intendedValues)) {
                weightToPrioMap.put(50d, AVOID_AT_ALL_COSTS.getValue());
            }
        }

        // MARQ24 MOD START
        //if (pushingSectionsHighways.contains(highway) || way.hasTag("bicycle", "use_sidepath") || "parking_aisle".equals(service)) {
        if (pushingSectionsHighways.contains(highway) || (!isRoadBikeEncoder && way.hasTag("bicycle", "use_sidepath")) || "parking_aisle".equals(service)) {
        // MARQ24 MOD END
            int pushingSectionPrio = AVOID_IF_POSSIBLE.getValue();
            // MARQ24 MOD START
            if(!isRoadBikeEncoder) {
            // MARQ24 MOD END
                if (way.hasTag("bicycle", "yes") || way.hasTag("bicycle", "permissive")) {
                    pushingSectionPrio = PREFER.getValue();
                }
                if (way.hasTag("bicycle", "designated") || way.hasTag("bicycle", "official")) {
                    pushingSectionPrio = VERY_NICE.getValue();
                }
            // MARQ24 MOD START
            }
            // MARQ24 MOD END

            if (way.hasTag("foot", "yes")) {
                pushingSectionPrio = Math.max(pushingSectionPrio - 1, WORST.getValue());
                // MARQ24 MOD START
                if(!isRoadBikeEncoder ) {
                // MARQ24 MOD END
                    if (way.hasTag("segregated", "yes")) {
                        pushingSectionPrio = Math.min(pushingSectionPrio + 1, BEST.getValue());
                    }
                // MARQ24 MOD START
                }
                // MARQ24 MOD END
            }
            weightToPrioMap.put(100d, pushingSectionPrio);
        }

        if (way.hasTag("railway", "tram")) {
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
        if (way.hasTag("scenic", "yes") || (maxSpeed > 0 && maxSpeed < wayTypeSpeed)) {
            if (weightToPrioMap.lastEntry().getValue() < BEST.getValue()) {
                // Increase the prio by one step
                weightToPrioMap.put(110d, weightToPrioMap.lastEntry().getValue() + 1);
            }
        }
    }

    /**
     * Handle surface and wayType encoding
     */
    long handleBikeRelated(ReaderWay way, long encoded, boolean partOfCycleRelation) {
        String surfaceTag = way.getTag("surface");
        String highway = way.getTag("highway");
        String trackType = way.getTag("tracktype");

        // Populate unpavedBit
        if ("track".equals(highway) && (trackType == null || !"grade1".equals(trackType))
                || "path".equals(highway) && surfaceTag == null
                || unpavedSurfaceTags.contains(surfaceTag)) {
            encoded = setBool(encoded, K_UNPAVED, true);
        }

        WayType wayType;
        if (roadValues.contains(highway)) {
            wayType = WayType.ROAD;
        }else {
            wayType = WayType.OTHER_SMALL_WAY;
        }

        boolean isPushingSection = isPushingSection(way);
        // MARQ24 MOD START
        /* ORG CODE START
        if (isPushingSection && !partOfCycleRelation || "steps".equals(highway))
            wayType = WayType.PUSHING_SECTION;

        if (way.hasTag("bicycle", intendedValues)) {
            if (isPushingSection && !way.hasTag("bicycle", "designated"))
                wayType = WayType.OTHER_SMALL_WAY;
            else if (wayType == WayType.OTHER_SMALL_WAY || wayType == WayType.PUSHING_SECTION)
                wayType = WayType.CYCLEWAY;
        } else if ("cycleway".equals(highway))
            wayType = WayType.CYCLEWAY;
        ORG CODE END */
        // MARQ24 MOD END
        if (isPushingSection || "steps".equals(highway)) {
            wayType = WayType.PUSHING_SECTION;
        } else{
            // boost "none identified" partOfCycleRelation
            if(!isRoadBikeEncoder && partOfCycleRelation){
                wayType = WayType.CYCLEWAY;
            }

            if (way.hasTag("bicycle", intendedValues)) {
                wayType = WayType.CYCLEWAY;
            } else if ("cycleway".equals(highway)) {
                wayType = WayType.CYCLEWAY;
            }
        }
        return wayTypeEncoder.setValue(encoded, wayType.getValue());
    }

    @Override
    public long setBool(long flags, int key, boolean value) {
        switch (key) {
            case K_UNPAVED:
                return value ? flags | unpavedBit : flags & ~unpavedBit;
            default:
                return super.setBool(flags, key, value);
        }
    }

    @Override
    public boolean isBool(long flags, int key) {
        switch (key) {
            case K_UNPAVED:
                return (flags & unpavedBit) != 0;
            default:
                return super.isBool(flags, key);
        }
    }

    @Override
    public double getDouble(long flags, int key) {
        switch (key) {
            case PriorityWeighting.KEY:
                return (double) priorityWayEncoder.getValue(flags) / BEST.getValue();
            default:
                return super.getDouble(flags, key);
        }
    }

    boolean isPushingSection(ReaderWay way) {
        // MARQ24 MOD START
        //return way.hasTag("highway", pushingSectionsHighways) || way.hasTag("railway", "platform") || way.hasTag("bicycle", "dismount");
        return way.hasTag("highway", pushingSectionsHighways) || way.hasTag("railway", "platform") || way.hasTag("bicycle", "dismount") || way.hasTag("route", ferries); // Runge
        // MARQ24 MOD END
    }

    //MARQ24 MOD START
    public long handleSpeed(ReaderWay way, double speed, long encoded) {
        // MARQ24: taken from GH Bike2WeightFlagEncoder.handleSpeed(...)

        // handle oneways
        encoded = handleSpeedInt(way, speed, encoded);
        if (isBackward(encoded)) {
            encoded = setReverseSpeed(encoded, speed);
        }
        if (isForward(encoded)) {
            encoded = setSpeed(encoded, speed);
        }
        return encoded;
    }
    //MARQ24 MOD END

    //MARQ24 MOD START
    //protected long handleSpeed(ReaderWay way, double speed, long encoded) {
    private long handleSpeedInt(ReaderWay way, double speed, long encoded) {
    //MARQ24 MOD END
        encoded = setSpeed(encoded, speed);
        // handle oneways
        boolean isOneway = way.hasTag("oneway", oneways)
                || way.hasTag("oneway:bicycle", oneways)
                || way.hasTag("vehicle:backward")
                || way.hasTag("vehicle:forward")
                || way.hasTag("bicycle:forward");
        //MARQ24 MOD START
        //if ((isOneway || way.hasTag("junction", "roundabout"))
        if (!way.hasTag("bicycle_road", "yes") && (isOneway || way.hasTag("junction", "roundabout"))
        //MARQ24 MOD END
                && !way.hasTag("oneway:bicycle", "no")
                && !way.hasTag("bicycle:backward")
                && !way.hasTag("cycleway", oppositeLanes)
                && !way.hasTag("cycleway:left", oppositeLanes)
                && !way.hasTag("cycleway:right", oppositeLanes)) {
            boolean isBackward = way.hasTag("oneway", "-1")
                    || way.hasTag("oneway:bicycle", "-1")
                    || way.hasTag("vehicle:forward", "no")
                    || way.hasTag("bicycle:forward", "no");
            if (isBackward)
                encoded |= backwardBit;
            else
                encoded |= forwardBit;

        } else {
            encoded |= directionBitMask;
        }
        return encoded;
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

    void setCyclingNetworkPreference(String network, int code) {
        bikeNetworkToCode.put(network, code);
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

        private WayType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    protected enum UpdateType {
        UPGRADE_ONLY,
        DOWNGRADE_ONLY,
        BOTH;
    }

    protected static class SpeedValue {
        private Integer speed;
        private  UpdateType type = UpdateType.BOTH;

        private SpeedValue(Integer speed){
            this.speed = speed;
        }

        protected SpeedValue(Integer speed, UpdateType type){
            this.speed = speed;
            this.type = type;
        }

        public String toString(){
            switch (type){
                default:
                case BOTH:
                    return speed +" [BOTH]";
                case UPGRADE_ONLY:
                    return speed +" [UP]";
                case DOWNGRADE_ONLY:
                    return speed +" [DOWN]";
            }
        }
    }

    // MARQ24 MOD START
    protected abstract double getDownhillMaxSpeed();
    // MARQ24 MOD END

    // MARQ24 08 July 2018
    // I have decided to remove all the ors reverse speed calculations and include here the "original"
    // gh speed calc (as it could be found in the Bike2WeightFlagEncoder...
    @Override
    public void applyWayTags(ReaderWay way, EdgeIteratorState edge) {
        // MARQ24 MOD START
        if (isConsiderElevation()) {
        // MARQ24 MOD END
            PointList pl = edge.fetchWayGeometry(3);
            if (!pl.is3D())
                throw new IllegalStateException("To support speed calculation based on elevation data it is necessary to enable import of it.");

            long flags = edge.getFlags();

            if (way.hasTag("tunnel", "yes") || way.hasTag("bridge", "yes") || way.hasTag("highway", "steps")) {
                // do not change speed
                // note: although tunnel can have a difference in elevation it is very unlikely that the elevation data is correct for a tunnel
            } else {
                // Decrease the speed for ele increase (incline), and decrease the speed for ele decrease (decline). The speed-decrease
                // has to be bigger (compared to the speed-increase) for the same elevation difference to simulate loosing energy and avoiding hills.
                // For the reverse speed this has to be the opposite but again keeping in mind that up+down difference.
                double incEleSum = 0, incDist2DSum = 0;
                double decEleSum = 0, decDist2DSum = 0;
                // double prevLat = pl.getLatitude(0), prevLon = pl.getLongitude(0);
                double prevEle = pl.getElevation(0);
                double fullDist2D = edge.getDistance();

                if (Double.isInfinite(fullDist2D))
                    throw new IllegalStateException("Infinite distance should not happen due to #435. way ID=" + way.getId());

                // for short edges an incline makes no sense and for 0 distances could lead to NaN values for speed, see #432
                if (fullDist2D < 1)
                    return;

                double eleDelta = pl.getElevation(pl.size() - 1) - prevEle;
                if (eleDelta > 0.1) {
                    incEleSum = eleDelta;
                    incDist2DSum = fullDist2D;
                } else if (eleDelta < -0.1) {
                    decEleSum = -eleDelta;
                    decDist2DSum = fullDist2D;
                }

//            // get a more detailed elevation information, but due to bad SRTM data this does not make sense now.
//            for (int i = 1; i < pl.size(); i++)
//            {
//                double lat = pl.getLatitude(i);
//                double lon = pl.getLongitude(i);
//                double ele = pl.getElevation(i);
//                double eleDelta = ele - prevEle;
//                double dist2D = distCalc.calcDist(prevLat, prevLon, lat, lon);
//                if (eleDelta > 0.1)
//                {
//                    incEleSum += eleDelta;
//                    incDist2DSum += dist2D;
//                } else if (eleDelta < -0.1)
//                {
//                    decEleSum += -eleDelta;
//                    decDist2DSum += dist2D;
//                }
//                fullDist2D += dist2D;
//                prevLat = lat;
//                prevLon = lon;
//                prevEle = ele;
//            }
                // Calculate slop via tan(asin(height/distance)) but for rather smallish angles where we can assume tan a=a and sin a=a.
                // Then calculate a factor which decreases or increases the speed.
                // Do this via a simple quadratic equation where y(0)=1 and y(0.3)=1/4 for incline and y(0.3)=2 for decline
                double fwdIncline = incDist2DSum > 1 ? incEleSum / incDist2DSum : 0;
                double fwdDecline = decDist2DSum > 1 ? decEleSum / decDist2DSum : 0;
                double restDist2D = fullDist2D - incDist2DSum - decDist2DSum;

                // MARQ24 MOD START
                //double maxSpeed = getHighwaySpeed("cycleway").speed;
                double wayMaxSpeed = getMaxSpeed(way);
                double maxSpeed = getDownhillMaxSpeed();
                if (wayMaxSpeed != -1) {
                    maxSpeed = Math.min(maxSpeed, wayMaxSpeed);
                }
                // MARQ24 MOD END

                if (isForward(flags)) {
                    // use weighted mean so that longer incline influences speed more than shorter
                    double speed = getSpeed(flags);
                    double fwdFaster = 1 + 2 * keepIn(fwdDecline, 0, 0.2);
                    fwdFaster = fwdFaster * fwdFaster;
                    double fwdSlower = 1 - 5 * keepIn(fwdIncline, 0, 0.2);
                    fwdSlower = fwdSlower * fwdSlower;
                    speed = speed * (fwdSlower * incDist2DSum + fwdFaster * decDist2DSum + 1 * restDist2D) / fullDist2D;
                    flags = this.setSpeed(flags, keepIn(speed, PUSHING_SECTION_SPEED / 2, maxSpeed));
                }

                if (isBackward(flags)) {
                    double speedReverse = getReverseSpeed(flags);
                    double bwFaster = 1 + 2 * keepIn(fwdIncline, 0, 0.2);
                    bwFaster = bwFaster * bwFaster;
                    double bwSlower = 1 - 5 * keepIn(fwdDecline, 0, 0.2);
                    bwSlower = bwSlower * bwSlower;
                    speedReverse = speedReverse * (bwFaster * incDist2DSum + bwSlower * decDist2DSum + 1 * restDist2D) / fullDist2D;
                    flags = this.setReverseSpeed(flags, keepIn(speedReverse, PUSHING_SECTION_SPEED / 2, maxSpeed));
                }
            }
            edge.setFlags(flags);

        // MARQ24 MOD START
        }
        // MARQ24 MOD END
    }

    /* REMOVED by MARQ24 (08 July 2018)
    // MARQ24 MOD START [SHOULD BE REVIEWED!!!]
    private List<RouteSplit> splits = new ArrayList<RouteSplit>();
    private int prevEdgeId = Integer.MAX_VALUE;
    private DistanceCalc distCalc = new DistanceCalc3D();

    @Override
    public void applyWayTags(ReaderWay way, EdgeIteratorState edge) {

        // Modification by Maxim Rylov
        if (isConsiderElevation()) {
            PointList pl = edge.fetchWayGeometry(3);
            if (!pl.is3D()) {
                throw new IllegalStateException("To support speed calculation based on elevation data it is necessary to enable import of it.");
            }

            long flags = edge.getFlags();

            if (way.hasTag("tunnel", "yes") || way.hasTag("bridge", "yes") || way.hasTag("highway", "steps")) {
                // do not change speed
                // note: although tunnel can have a difference in elevation it is very unlikely that the elevation data is correct for a tunnel
            } else {
                double fullDist2D = edge.getDistance();

                if (Double.isInfinite(fullDist2D)) {
                    System.err.println("infinity distance? for way:" + way.getId());
                    return;
                }

                // for short edges an incline makes no sense and for 0 distances could lead to NaN values for speed, see #432
                if (fullDist2D < 1)
                    return;

                double wayMaxSpeed = getMaxSpeed(way);
                double maxSpeed = getDownhillMaxSpeed(); // getHighwaySpeed("cycleway");
                if (wayMaxSpeed != -1) {
                    maxSpeed = Math.min(maxSpeed, wayMaxSpeed);
                }

                // Formulas for the following calculations is taken from http://www.flacyclist.com/content/perf/science.html
                double gradient = 0.0;

                if (prevEdgeId != edge.getOriginalEdge()) {
                    String incline = way.getTag("incline");
                    if (!Helper.isEmpty(incline)) {
                        incline = incline.replace("%", "").replace(",", ".");
                        try {
                            double v = Double.parseDouble(incline);
                            splits.clear();
                            RouteSplit split = new RouteSplit();
                            split.Length = fullDist2D;
                            split.Gradient = v;
                        } catch (Exception ex) {
                            SteepnessUtil.computeRouteSplits(pl, false, distCalc, splits);
                        }
                    } else
                        SteepnessUtil.computeRouteSplits(pl, false, distCalc, splits);

                    prevEdgeId = edge.getOriginalEdge();
                }

                double speed = 0;
                double speedReverse = 0;

                if (isForward(flags)) {
                    speed = getSpeed(flags);
                }

                if (isBackward(flags)) {
                    speedReverse = getReverseSpeed(flags);
                }

                if (splits.size() == 1) {
                    RouteSplit split = splits.get(0);
                    gradient = split.Gradient;

                    if (split.Length < 60) {
                        if (Math.abs(gradient) > 6) {
                            if (Math.abs(gradient) < 9)
                                gradient /= 2.0;
                            else
                                gradient /= 4.0;
                        }
                    }

                    if (Math.abs(gradient) > 1.5) {
                        if (speed != 0) {
                            speed = getGradientSpeed(speed, (int) Math.round(gradient));
                        }

                        if (speedReverse != 0) {
                            speedReverse = getGradientSpeed(speedReverse, (int) Math.round(-gradient));
                        }
                    }
                } else {
                    double distUphill = 0.0;
                    double distDownhill = 0.0;
                    double distUphillR = 0.0;
                    double distDownhillR = 0.0;
                    double distTotalEqFlat = 0.0;
                    double length = 0.0;

                    for (RouteSplit split : splits) {
                        gradient = split.Gradient;
                        length = split.Length;

                        if (Math.abs(gradient) < 1.5) {

                        } else {
                            if (speed != 0) {
                                double Vc = getGradientSpeed(speed, (int) Math.round(gradient));

                                if (gradient > 0) {
                                    distUphill += (speed / Vc - 1) * length;
                                }else {
                                    distDownhill += (speed / Vc - 1) * length;
                                }
                            }

                            if (speedReverse != 0) {
                                gradient = -gradient;
                                double Vc = getGradientSpeed(speedReverse, (int) Math.round(gradient));

                                if (gradient > 0) {
                                    distUphillR += (speedReverse / Vc - 1) * length;
                                }else {
                                    distDownhillR += (speedReverse / Vc - 1) * length;
                                }
                            }
                        }
                    }

                    if (speed != 0) {
                        distTotalEqFlat = fullDist2D + distUphill + distDownhill;
                        speed *= fullDist2D / distTotalEqFlat;
                    }

                    if (speedReverse != 0) {
                        distTotalEqFlat = fullDist2D + distUphillR + distDownhillR;
                        speedReverse *= fullDist2D / distTotalEqFlat;
                    }
                }

                flags = this.setSpeed(flags, Helper.keepIn(speed, PUSHING_SECTION_SPEED / 2, maxSpeed));
                flags = this.setReverseSpeed(flags, Helper.keepIn(speedReverse, PUSHING_SECTION_SPEED / 2, maxSpeed));
            }
            edge.setFlags(flags);
        }
    }

    protected double getGradientSpeed(double speed, int gradient) {
        if (gradient < -18) {
            if (speed > 10)
                return getDownhillMaxSpeed();
            else
                return speed;
        } else {
            if (speed > 10)
                return speed * getGradientSpeedFactor(gradient);
            else {
                double result = speed * getGradientSpeedFactor(gradient);

                // forbid high downhill speeds on surfaces with low speeds
                if (result > speed)
                    return speed;
                else
                    return result;
            }
        }
    }

    private double getGradientSpeedFactor(int gradient) {
        if (gradient < -18)
            return 3.5;
        else if (gradient > 17)
            return 0.1;
        else {
            switch (gradient) {
                case -18:
                    return 3.332978723;
                case -17:
                    return 3.241489362;
                case -16:
                    return 3.14751773;
                case -15:
                    return 3.05070922;
                case -14:
                    return 2.95106383;
                case -13:
                    return 2.84822695;
                case -12:
                    return 2.741843972;
                case -11:
                    return 2.631560284;
                case -10:
                    return 2.517021277;
                case -9:
                    return 2.39787234;
                case -8:
                    return 2.273049645;
                case -7:
                    return 2.142553191;
                case -6:
                    return 2.004964539;
                case -5:
                    return 1.859574468;
                case -4:
                    return 1.705673759;
                case -3:
                    return 1.542198582;
                case -2:
                    return 1.368439716;
                case -1:
                    return 1.186524823;
                case 0:
                    return 1;
                case 1:
                    return 0.820567376;
                case 2:
                    return 0.663120567;
                case 3:
                    return 0.537234043;
                case 4:
                    return 0.442553191;
                case 5:
                    return 0.372695035;
                case 6:
                    return 0.319858156;
                case 7:
                    return 0.279787234;
                case 8:
                    return 0.24822695;
                case 9:
                    return 0.222695035;
                case 10:
                    return 0.20177305;
                case 11:
                    return 0.184751773;
                case 12:
                    return 0.170212766;
                case 13:
                    return 0.157446809;
                case 14:
                    return 0.146808511;
                case 15:
                    return 0.137234043;
                case 16:
                    return 0.129078014;
                case 17:
                    return 0.121631206;
            }
        }

        return 1;
    }
    // MARQ24 MOD END
    */
}