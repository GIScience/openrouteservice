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

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;
import com.graphhopper.routing.ev.UnsignedDecimalEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.ConditionalEdges;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.BitUtil;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.graphhopper.routing.util.EncodingManager.getKey;

public abstract class VehicleFlagEncoder extends ORSAbstractFlagEncoder {
    public static final String KEY_ESTIMATED_DISTANCE = "estimated_distance";
    public static final String KEY_HIGHWAY = "highway";
    public static final String KEY_ONEWAY = "oneway";
    public static final String KEY_VEHICLE_FORWARD = "vehicle:forward";
    public static final String KEY_MOTOR_VEHICLE_FORWARD = "motor_vehicle:forward";
    public static final String KEY_MOTORROAD = "motorroad";
    private static final double ACCELERATION_SPEED_CUTOFF_MAX = 80.0;
    private static final double ACCELERATION_SPEED_CUTOFF_MIN = 20.0;
    public static final int AVERAGE_SECS_TO_100_KMPH = 10;
    public static final String KEY_MOTORWAY_LINK = "motorway_link";
    public static final String KEY_RESIDENTIAL = "residential";
    protected SpeedLimitHandler speedLimitHandler;

    private double accelerationModifier = 0.0;

    protected boolean speedTwoDirections = false;

    protected int maxTrackGradeLevel = 3;

    // Take into account acceleration calculations when determining travel speed
    protected boolean useAcceleration = false;

    // This value determines the maximal possible on roads with bad surfaces
    protected int badSurfaceSpeed;

    // This value determines the speed for roads with access=destination
    protected int destinationSpeed;

    protected final double minPossibleSpeed;

    protected Map<String, Integer> trackTypeSpeedMap;
    protected Map<String, Integer> badSurfaceSpeedMap;
    protected Map<String, Integer> defaultSpeedMap;

    private boolean hasConditionalAccess;
    private boolean hasConditionalSpeed;
    private BooleanEncodedValue conditionalAccessEncoder;
    private BooleanEncodedValue conditionalSpeedEncoder;

    protected void setProperties(PMap properties) {
        hasConditionalAccess = properties.getBool(ConditionalEdges.ACCESS, false);
        hasConditionalSpeed = properties.getBool(ConditionalEdges.SPEED, false);
        this.blockFords(properties.getBool("block_fords", true));
        this.blockBarriers(properties.getBool("block_barriers", true));
        speedTwoDirections = properties.getBool("speed_two_directions", true);
        useAcceleration = properties.getBool("use_acceleration", false);
        maxTrackGradeLevel = properties.getInt("maximum_grade_level", maxTrackGradeLevel);
    }

    VehicleFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
        super(speedBits, speedFactor, maxTurnCosts);

        minPossibleSpeed = this.speedFactor;

        restrictions.addAll(Arrays.asList("motorcar", "motor_vehicle", "vehicle", "access"));

        restrictedValues.add("private");
        restrictedValues.add("no");
        restrictedValues.add("restricted");
        restrictedValues.add("military");

        intendedValues.add("yes");
        intendedValues.add("permissive");
        intendedValues.add("destination");  // This is needed to allow the passing of barriers that are marked as destination

        passByDefaultBarriers.add("gate");
        passByDefaultBarriers.add("lift_gate");
        passByDefaultBarriers.add("kissing_gate");
        passByDefaultBarriers.add("swing_gate");

        blockByDefaultBarriers.add("bollard");
        blockByDefaultBarriers.add("stile");
        blockByDefaultBarriers.add("turnstile");
        blockByDefaultBarriers.add("cycle_barrier");
        blockByDefaultBarriers.add("motorcycle_barrier");
        blockByDefaultBarriers.add("block");

        trackTypeSpeedMap = new HashMap<>();

        trackTypeSpeedMap.put("grade1", 40); // paved
        trackTypeSpeedMap.put("grade2", 30); // now unpaved - gravel mixed with ...
        trackTypeSpeedMap.put("grade3", 20); // ... hard and soft materials
        trackTypeSpeedMap.put("grade4", 15);
        trackTypeSpeedMap.put("grade5", 10);

        badSurfaceSpeedMap = new HashMap<>();

        badSurfaceSpeedMap.put("asphalt", -1);
        badSurfaceSpeedMap.put("concrete", -1);
        badSurfaceSpeedMap.put("concrete:plates", -1);
        badSurfaceSpeedMap.put("concrete:lanes", -1);
        badSurfaceSpeedMap.put("paved", -1);
        badSurfaceSpeedMap.put("cement", 80);
        badSurfaceSpeedMap.put("compacted", 80);
        badSurfaceSpeedMap.put("fine_gravel", 60);
        badSurfaceSpeedMap.put("paving_stones", 40);
        badSurfaceSpeedMap.put("metal", 40);
        badSurfaceSpeedMap.put("bricks", 40);
        badSurfaceSpeedMap.put("grass", 30);
        badSurfaceSpeedMap.put("wood", 30);
        badSurfaceSpeedMap.put("sett", 30);
        badSurfaceSpeedMap.put("grass_paver", 30);
        badSurfaceSpeedMap.put("gravel", 30);
        badSurfaceSpeedMap.put("unpaved", 30);
        badSurfaceSpeedMap.put("ground", 30);
        badSurfaceSpeedMap.put("dirt", 30);
        badSurfaceSpeedMap.put("pebblestone", 30);
        badSurfaceSpeedMap.put("tartan", 30);
        badSurfaceSpeedMap.put("cobblestone", 20);
        badSurfaceSpeedMap.put("clay", 20);
        badSurfaceSpeedMap.put("earth", 15);
        badSurfaceSpeedMap.put("stone", 15);
        badSurfaceSpeedMap.put("rocky", 15);
        badSurfaceSpeedMap.put("sand", 15);
        badSurfaceSpeedMap.put("mud", 10);
        badSurfaceSpeedMap.put("unknown", 30);

        // limit speed on bad surfaces to 30 km/h
        badSurfaceSpeed = 30;
        destinationSpeed = 5;
        maxPossibleSpeed = 140;

        defaultSpeedMap = new HashMap<>();
        // autobahn
        defaultSpeedMap.put("motorway", 100);
        defaultSpeedMap.put(KEY_MOTORWAY_LINK, 60);
        defaultSpeedMap.put(KEY_MOTORROAD, 90);
        // bundesstraße
        defaultSpeedMap.put("trunk", 85);
        defaultSpeedMap.put("trunk_link", 60);
        // linking bigger town
        defaultSpeedMap.put("primary", 65);
        defaultSpeedMap.put("primary_link", 50);
        // linking towns + villages
        defaultSpeedMap.put("secondary", 60);
        defaultSpeedMap.put("secondary_link", 50);
        // streets without middle line separation
        defaultSpeedMap.put("tertiary", 50);
        defaultSpeedMap.put("tertiary_link", 40);
        defaultSpeedMap.put("unclassified", 30);
        defaultSpeedMap.put(KEY_RESIDENTIAL, 30);
        // spielstraße
        defaultSpeedMap.put("living_street", 10);
        defaultSpeedMap.put("service", 20);
        // unknown road
        defaultSpeedMap.put("road", 20);
        // forestry stuff
        defaultSpeedMap.put("track", 15);
    }

    @Override
    public void createEncodedValues(List<EncodedValue> registerNewEncodedValue, String prefix, int index) {
        // first two bits are reserved for route handling in superclass
        super.createEncodedValues(registerNewEncodedValue, prefix, index);
        avgSpeedEnc = new UnsignedDecimalEncodedValue(getKey(prefix, "average_speed"), speedBits, speedFactor, speedTwoDirections);
        registerNewEncodedValue.add(avgSpeedEnc);
        if (hasConditionalAccess)
            registerNewEncodedValue.add(conditionalAccessEncoder = new SimpleBooleanEncodedValue(EncodingManager.getKey(prefix, ConditionalEdges.ACCESS), true));
        if (hasConditionalSpeed)
            registerNewEncodedValue.add(conditionalSpeedEncoder = new SimpleBooleanEncodedValue(EncodingManager.getKey(prefix, ConditionalEdges.SPEED), false));

    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, EncodingManager.Access access) {
        return handleWayTags(edgeFlags,way,access,0);
    }

    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, EncodingManager.Access access, long relationFlags) {
        if (access.canSkip())
            return edgeFlags;

        if (!access.isFerry()) {
            // get assumed speed from highway type
            double speed = getSpeed(way);
            speed = applyMaxSpeed(way, speed);

            // TODO: save conditional speeds only if their value is different from the default speed
            if (getConditionalSpeedInspector()!=null && getConditionalSpeedInspector().hasConditionalSpeed(way))
                if (getConditionalSpeedInspector().hasLazyEvaluatedConditions() && conditionalSpeedEncoder!=null) {
                    conditionalSpeedEncoder.setBool(false, edgeFlags, true);
                } else {
                    // conditional maxspeed overrides unconditional one
                    speed = applyConditionalSpeed(getConditionalSpeedInspector().getTagValue(), speed);
                }

            speed = getSurfaceSpeed(way, speed);

            if (way.hasTag(KEY_ESTIMATED_DISTANCE)) {
                if (way.hasTag(KEY_HIGHWAY, KEY_RESIDENTIAL)) {
                    speed = addResedentialPenalty(speed, way);
                }
                else if (this.useAcceleration) {
                    double estDist = way.getTag(KEY_ESTIMATED_DISTANCE, Double.MAX_VALUE);
                    speed = adjustSpeedForAcceleration(estDist, speed);
                }
            }

            boolean isRoundabout = way.hasTag("junction", "roundabout");

            if (isRoundabout) { // Runge
                roundaboutEnc.setBool(false, edgeFlags, true);
                //http://www.sidrasolutions.com/Documents/OArndt_Speed%20Control%20at%20Roundabouts_23rdARRBConf.pdf
                if (way.hasTag(KEY_HIGHWAY, "mini_roundabout"))
                    speed = speed < 25 ? speed : 25;

                if (way.hasTag("lanes")) {
                    try {
                        // The following line throws exceptions when it tries to parse a value "3; 2"
                        int lanes = Integer.parseInt(way.getTag("lanes"));
                        if (lanes >= 2)
                            speed  = speed < 40 ? speed : 40;
                        else
                            speed  = speed < 35 ? speed : 35;
                    } catch(Exception ex) {
                        // do nothing
                    }
                }
            }

            setSpeed(false, edgeFlags, speed);
            setSpeed(true, edgeFlags, speed);

            if (isOneway(way) || isRoundabout) {
                if (isForwardOneway(way))
                    setAccess(access, edgeFlags, true, false);
                if (isBackwardOneway(way))
                    setAccess(access, edgeFlags, false, true);
            } else {
                setAccess(access, edgeFlags, true, true);
            }

            if (access.isConditional() && conditionalAccessEncoder!=null)
                conditionalAccessEncoder.setBool(false, edgeFlags, true);
        } else {
            double ferrySpeed = ferrySpeedCalc.getSpeed(way);
            accessEnc.setBool(false, edgeFlags, true);
            accessEnc.setBool(true, edgeFlags, true);
            setSpeed(false, edgeFlags, ferrySpeed);
            setSpeed(true, edgeFlags, ferrySpeed);
        }

        if (destinationSpeed != -1) {
            for (String restriction : restrictions) {
                if (way.hasTag(restriction, "destination")) {
                    // This is problematic as Speed != Time
                    avgSpeedEnc.setDecimal(false, edgeFlags, destinationSpeed);
                    avgSpeedEnc.setDecimal(true, edgeFlags, destinationSpeed);
                }
            }
        }

        return edgeFlags;
    }

    // Override this method in order to set minimum speed rather than disabling access
    @Override
    protected void setSpeed(boolean reverse, IntsRef edgeFlags, double speed) {
        if (speed >= 0.0D && !Double.isNaN(speed)) {
            if (speed < minPossibleSpeed)
                speed = minPossibleSpeed;
            else if (speed > this.getMaxSpeed())
                speed = this.getMaxSpeed();

            this.avgSpeedEnc.setDecimal(reverse, edgeFlags, speed);
        } else {
            throw new IllegalArgumentException("Speed cannot be negative or NaN: " + speed + ", flags:" + BitUtil.LITTLE.toBitString(edgeFlags));
        }
    }

    private void setAccess(EncodingManager.Access access, IntsRef edgeFlags, boolean fwd, boolean bwd) {
        if (fwd)
            accessEnc.setBool(false, edgeFlags, true);
        if (bwd)
            accessEnc.setBool(true, edgeFlags, true);

        if (access.isConditional() && conditionalAccessEncoder!=null) {
            if (fwd)
                conditionalAccessEncoder.setBool(false, edgeFlags, true);
            if (bwd)
                conditionalAccessEncoder.setBool(true, edgeFlags, true);
        }
    }

    /**
     * make sure that isOneway is called before
     */
    protected boolean isBackwardOneway(ReaderWay way) {
        return way.hasTag(KEY_ONEWAY, "-1")
                || way.hasTag(KEY_VEHICLE_FORWARD, "no")
                || way.hasTag(KEY_MOTOR_VEHICLE_FORWARD, "no");
    }

    /**
     * make sure that isOneway is called before
     */
    protected boolean isForwardOneway(ReaderWay way) {
        return !way.hasTag(KEY_ONEWAY, "-1")
                && !way.hasTag(KEY_VEHICLE_FORWARD, "no")
                && !way.hasTag(KEY_MOTOR_VEHICLE_FORWARD, "no");
    }

    protected boolean isOneway(ReaderWay way) {
        return way.hasTag(KEY_ONEWAY, oneways)
                || way.hasTag("vehicle:backward")
                || way.hasTag(KEY_VEHICLE_FORWARD)
                || way.hasTag("motor_vehicle:backward")
                || way.hasTag(KEY_MOTOR_VEHICLE_FORWARD);
    }

    protected double getSpeed(ReaderWay way) {
        String highwayValue = getHighway(way);
        Integer speed = speedLimitHandler.getSpeed(highwayValue);

        // Note that Math.round(NaN) == 0
        int maxSpeed = (int) Math.round(getMaxSpeed(way));
        if (maxSpeed <= 0)
            maxSpeed = speedLimitHandler.getMaxSpeed(way);
        if (maxSpeed > 0)
            speed = maxSpeed;

        if (speed == null)
            throw new IllegalStateException(this + ", no speed found for: " + highwayValue + ", tags: " + way);

        if (highwayValue.equals("track")) {
            String tt = way.getTag("tracktype");
            if (!Helper.isEmpty(tt)) {
                Integer tInt = speedLimitHandler.getTrackTypeSpeed(tt);
                if (tInt != null && tInt != -1)
                    speed = tInt;
            }
        }

        return speed;
    }

    protected String getHighway(ReaderWay way) {
        String highwayValue = way.getTag(KEY_HIGHWAY);
        if (!Helper.isEmpty(highwayValue) && way.hasTag(KEY_MOTORROAD, "yes")
                && !highwayValue.equals("motorway") && !highwayValue.equals(KEY_MOTORWAY_LINK)) {
            highwayValue = KEY_MOTORROAD;
        }
        return highwayValue;
    }

    @Override
    protected double applyMaxSpeed(ReaderWay way, double speed) {
        double maxSpeed = this.getMaxSpeed(way);
        return isValidSpeed(maxSpeed) ? maxSpeed * 0.9D : speed;
    }

    /**
     * @param way:   needed to retrieve tags
     * @param speed: speed guessed e.g. from the road type or other tags
     * @return The assumed speed
     */
    protected double getSurfaceSpeed(ReaderWay way, double speed) {
        // limit speed if bad surface
        String surface = way.getTag("surface");
        if (surface != null)
        {
            Integer surfaceSpeed = speedLimitHandler.getSurfaceSpeed(surface);
            if (speed > surfaceSpeed && surfaceSpeed != -1)
                return surfaceSpeed;
        }

        return speed;
    }

    public String getWayInfo(ReaderWay way) {
        StringBuilder str = new StringBuilder();
        String highwayValue = way.getTag(KEY_HIGHWAY);
        // for now only motorway links
        if (KEY_MOTORWAY_LINK.equals(highwayValue)) {
            String destination = way.getTag("destination");
            if (!Helper.isEmpty(destination)) {
                int counter = 0;
                for (String d : destination.split(";")) {
                    if (d.trim().isEmpty())
                        continue;

                    if (counter > 0)
                        str.append(", ");

                    str.append(d.trim());
                    counter++;
                }
            }
        }
        if (str.length() == 0)
            return str.toString();
        // I18N
        if (str.toString().contains(","))
            return "destinations: " + str;
        else
            return "destination: " + str;
    }

    protected int getTrackGradeLevel(String grade) {
        if (grade == null)
            return 0;

        if (grade.contains(";")) {
            int maxGrade = 0;
            try {
                String[] values = grade.split(";");
                for(String v : values)                 {
                    int iv = Integer.parseInt(v.replace("grade","").trim());
                    if (iv > maxGrade)
                        maxGrade = iv;
                }

                return maxGrade;
            } catch(Exception ex) {
                // do nothing
            }
        }

        switch(grade) {
            case "grade":
            case "grade1":
                return 1;
            case "grade2":
                return 2;
            case "grade3":
                return 3;
            case "grade4":
                return 4;
            case "grade5":
                return 5;
            case "grade6":
                return 6;
            default:
        }
        return 10;
    }

    double addResedentialPenalty(double baseSpeed, ReaderWay way) {
        if (baseSpeed == 0)
            return 0;
        double speed = baseSpeed;

        if(way.hasTag(KEY_HIGHWAY, KEY_RESIDENTIAL)) {
            double estDist = way.getTag(KEY_ESTIMATED_DISTANCE, Double.MAX_VALUE);
            // take into account number of nodes to get an average distance between nodes
            double interimDistance = estDist;
            int interimNodes = way.getNodes().size() - 2;
            if(interimNodes > 0) {
                interimDistance = estDist/(interimNodes+1);
            }
            if(interimDistance < 100) {
                speed = speed * 0.5;
            }
        }

        return speed;
    }

    /**
     * Returns how many seconds it is assumed that this vehicle would reach 100 km/h taking into acocunt the acceleration modifier
     *
     * @return
     */
    double secondsTo100KmpH() {
        return AVERAGE_SECS_TO_100_KMPH + (accelerationModifier * AVERAGE_SECS_TO_100_KMPH);
    }

    /**
     * Returns the acceleration in KM per hour per second.
     *
     * @return
     */
    double accelerationKmpHpS() {
        return 100.0 / secondsTo100KmpH();
    }

    /**
     * Adjust the maximum speed taking into account supposed acceleration on the segment. The method looks at acceleration
     * along the way (assuming starting from 0km/h) and then uses the length to travel and the supposed maximum speed
     * to determine an average speed for travelling along the whole segment.
     *
     * @param distance				How long the segment to travel along is
     * @param maximumSpeedInKmph	The maximum speed that a vehicle can travel along this segment (usually the speed limit)
     * @return
     */
    public double adjustSpeedForAcceleration(double distance, double maximumSpeedInKmph) {
        // We only want to perform the adjustment if the road is a slower speed - main roads shouldnt be affected as much due to less junctions and changes in direction
        if(maximumSpeedInKmph < ACCELERATION_SPEED_CUTOFF_MAX) {
            if (distance <= 0) {
                return maximumSpeedInKmph;
            }

            // slower roads can be assumed to have slower acceleration...

            double normalisedSpeed = maximumSpeedInKmph;
            if(normalisedSpeed < ACCELERATION_SPEED_CUTOFF_MIN)
                normalisedSpeed = ACCELERATION_SPEED_CUTOFF_MIN;

            normalisedSpeed = (normalisedSpeed -ACCELERATION_SPEED_CUTOFF_MIN) / (ACCELERATION_SPEED_CUTOFF_MAX-ACCELERATION_SPEED_CUTOFF_MIN);

            accelerationModifier = Math.pow(0.01, normalisedSpeed);

            double timeToMaxSpeed = durationToMaxSpeed(0, maximumSpeedInKmph);

            // We need to calculate how much distance is travelled in acceleration/deceleration phases
            double accelerationDistance = distanceTravelledInDuration(0, maximumSpeedInKmph, timeToMaxSpeed);

            double distanceAtMaxSpeed = distance - (accelerationDistance * 2); // Double the distance because of deceleration aswell


            if (distanceAtMaxSpeed < 0) {
                double duration = durationToTravelDistance(0, maximumSpeedInKmph, distance / 2);
                if (duration == 0) {
                    duration = 1;
                }
                return convertMpsToKmph(distance / (duration * 2));
            } else {
                double timeAtMaxSpeed = distanceAtMaxSpeed / convertKmphToMps(maximumSpeedInKmph);
                double averageSpeedMps = distance / (timeToMaxSpeed*2 + timeAtMaxSpeed);

                return convertMpsToKmph(averageSpeedMps);
            }
        } else {
            return maximumSpeedInKmph;
        }
    }

    /**
     * How many seconds does it take to reach maximum speed based on initial speed and acceleration.
     *
     * @param initialSpeedInKmph	How fast the vehicle is travelling at the start of the calculation
     * @param maxSpeedInKmph		The target speed to be reached
     * @return						How long it takes to reach the speed in seconds
     */
    private double durationToMaxSpeed(double initialSpeedInKmph, double maxSpeedInKmph) {
        return  (maxSpeedInKmph - initialSpeedInKmph) / accelerationKmpHpS();
    }

    /**
     * How long in seconds does it take to reach the intended distance based on the initial travelling speed and the
     * maximum speed that can be travelled.
     *
     * @param initialSpeedInKmph	The speed of the vehicle when starting the calculation
     * @param maxSpeedInKmph		The maximum speed the vehicle can travel at
     * @param distanceInM			The target distance to be travelled
     * @return						How long it takes in seconds to reach the target distance
     */
    private double durationToTravelDistance(double initialSpeedInKmph, double maxSpeedInKmph, double distanceInM) {
        double secondsTravelled = 0;
        double distanceTravelled = 0;

        double currentSpeed = initialSpeedInKmph;

        while(currentSpeed < maxSpeedInKmph && distanceTravelled < distanceInM) {
            currentSpeed += accelerationKmpHpS();
            secondsTravelled += 1;
            distanceTravelled += convertKmphToMps(currentSpeed);
        }

        double distanceRemaining = distanceInM - distanceTravelled;

        if(distanceRemaining > 0) {
            secondsTravelled += (distanceRemaining / convertKmphToMps(maxSpeedInKmph));
        }

        return secondsTravelled;
    }

    /**
     * How far can the vehicle travel in the specified time frame
     *
     * @param initialSpeedInKmph	The starting speed of the vehicle
     * @param maxSpeedInKmph		The maximum travel speed
     * @param duration				How long is the vehicle travelling for
     * @return						The distance in metres that the vehicle travels in the specified time
     */
    private double distanceTravelledInDuration(double initialSpeedInKmph, double maxSpeedInKmph, double duration) {
        double secondsTravelled = 0;
        double distanceTravelled = 0;
        double currentSpeed = initialSpeedInKmph;

        while(currentSpeed < maxSpeedInKmph && secondsTravelled < duration) {
            currentSpeed += accelerationKmpHpS();
            secondsTravelled += 1;
            distanceTravelled += convertKmphToMps(currentSpeed);
        }

        double secondsRemaining = duration - secondsTravelled;

        if(secondsRemaining > 0 ) {
            distanceTravelled += (secondsRemaining * convertKmphToMps(maxSpeedInKmph));
        }

        return distanceTravelled;
    }

    /**
     * Convert kilometres per hour to metres per second
     *
     * @param speedInKmph	The speed to be converted in km per hour
     * @return				The speed in metres per second
     */
    private double convertKmphToMps(double speedInKmph) {
        return (speedInKmph * 1000) / 3600;
    }

    /**
     * Convert metres per second to kilometres per hour
     *
     * @param speedInMps	The speed in metres per second
     * @return				The speed in kilometres per hour
     */
    private double convertMpsToKmph(double speedInMps) {
        return (3600 * speedInMps) / 1000;
    }

    protected void initSpeedLimitHandler(String profile) {
        speedLimitHandler = new SpeedLimitHandler(profile, defaultSpeedMap, badSurfaceSpeedMap, trackTypeSpeedMap);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final VehicleFlagEncoder other = (VehicleFlagEncoder) obj;
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return ("VehicleFlagEncoder" + this).hashCode();
    }
}
