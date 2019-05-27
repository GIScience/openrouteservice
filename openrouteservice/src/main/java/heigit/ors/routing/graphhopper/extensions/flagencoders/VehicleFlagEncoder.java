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
import com.graphhopper.routing.profiles.EncodedValue;
import com.graphhopper.routing.profiles.FactorizedDecimalEncodedValue;
import com.graphhopper.routing.util.EncodedValueOld;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.Helper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class VehicleFlagEncoder extends ORSAbstractFlagEncoder {
    private final double ACCELERATION_SPEED_CUTOFF_MAX = 80.0;
    private final double ACCELERATION_SPEED_CUTOFF_MIN = 20.0;
    protected SpeedLimitHandler _speedLimitHandler;

    protected EncodedValueOld relationCodeEncoder;

    private double accelerationModifier = 0.0;

    protected boolean speedTwoDirections;

    protected int maxTrackGradeLevel = 3;

    // Take into account acceleration calculations when determining travel speed
    protected boolean useAcceleration = false;

    // This value determines the maximal possible on roads with bad surfaces
    protected int badSurfaceSpeed;

    // This value determines the speed for roads with access=destination
    protected int destinationSpeed;

    protected Map<String, Integer> trackTypeSpeedMap;
    protected Map<String, Integer> badSurfaceSpeedMap;
    protected Map<String, Integer> defaultSpeedMap;


    VehicleFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
        super(speedBits, speedFactor, maxTurnCosts);

        restrictions.addAll(Arrays.asList("motorcar", "motor_vehicle", "vehicle", "access"));

        restrictedValues.add("private");
        restrictedValues.add("no");
        restrictedValues.add("restricted");
        restrictedValues.add("military");

        intendedValues.add("yes");
        intendedValues.add("permissive");
        intendedValues.add("destination");  // This is needed to allow the passing of barriers that are marked as destination

        absoluteBarriers.add("bollard");
        absoluteBarriers.add("stile");
        absoluteBarriers.add("turnstile");
        absoluteBarriers.add("cycle_barrier");
        absoluteBarriers.add("motorcycle_barrier");
        absoluteBarriers.add("block");

        trackTypeSpeedMap = new HashMap<String, Integer>();

        trackTypeSpeedMap.put("grade1", 40); // paved
        trackTypeSpeedMap.put("grade2", 30); // now unpaved - gravel mixed with ...
        trackTypeSpeedMap.put("grade3", 20); // ... hard and soft materials
        trackTypeSpeedMap.put("grade4", 15);
        trackTypeSpeedMap.put("grade5", 10);

        badSurfaceSpeedMap = new HashMap<String, Integer>();

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

        defaultSpeedMap = new HashMap<String, Integer>();
        // autobahn
        defaultSpeedMap.put("motorway", 100);
        defaultSpeedMap.put("motorway_link", 60);
        defaultSpeedMap.put("motorroad", 90);
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
        defaultSpeedMap.put("residential", 30);
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
        registerNewEncodedValue.add(speedEncoder = new FactorizedDecimalEncodedValue("average_speed", speedBits, speedFactor, true));
    }

    @Override
    public int defineRelationBits(int index, int shift) {
        relationCodeEncoder = new EncodedValueOld("RelationCode", shift, 3, 1, 0, 7);
        return shift + relationCodeEncoder.getBits();
    }

    @Override
    public long handleRelationTags(long oldRelationFlags, ReaderRelation relation) {
        return oldRelationFlags;
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, EncodingManager.Access access, long relationFlags) {
        if (access.canSkip())
            return edgeFlags;

        if (!access.isFerry()) {
            // get assumed speed from highway type
            double speed = getSpeed(way);
            speed = applyMaxSpeed(way, speed);

            speed = getSurfaceSpeed(way, speed);

            if(way.hasTag("estimated_distance")) {
                if(this.useAcceleration) {
                    double estDist = way.getTag("estimated_distance", Double.MAX_VALUE);
                    if(way.hasTag("highway","residential")) {
                        speed = addResedentialPenalty(speed, way);
                    } else {
                        speed = Math.max(adjustSpeedForAcceleration(estDist, speed), speedFactor);
                    }
                } else {
                    if(way.hasTag("highway","residential")) {
                        speed = addResedentialPenalty(speed, way);
                    }
                }
            }

            boolean isRoundabout = way.hasTag("junction", "roundabout");

            if (isRoundabout) { // Runge
                roundaboutEnc.setBool(true, edgeFlags, true);
                //http://www.sidrasolutions.com/Documents/OArndt_Speed%20Control%20at%20Roundabouts_23rdARRBConf.pdf
                if (way.hasTag("highway", "mini_roundabout"))
                    speed = speed < 25 ? speed : 25;

                if (way.hasTag("lanes")) {
                    try {
                        // The following line throws exceptions when it tries to parse a value "3; 2"
                        int lanes = Integer.parseInt(way.getTag("lanes"));
                        if (lanes >= 2)
                            speed  = speed < 40 ? speed : 40;
                        else
                            speed  = speed < 35 ? speed : 35;
                    } catch(Exception ex)
                    {}
                }
            }

            setSpeed(false, edgeFlags, speed);
            setSpeed(true, edgeFlags, speed);

            if (isOneway(way) || isRoundabout) {
                if (isForwardOneway(way))
                    accessEnc.setBool(false, edgeFlags, true);
                if (isBackwardOneway(way))
                    accessEnc.setBool(true, edgeFlags, true);
            } else {
                accessEnc.setBool(false, edgeFlags, true);
                accessEnc.setBool(true, edgeFlags, true);
            }
        } else {
            double ferrySpeed = getFerrySpeed(way);
            accessEnc.setBool(false, edgeFlags, true);
            accessEnc.setBool(true, edgeFlags, true);
            setSpeed(false, edgeFlags, ferrySpeed);
            setSpeed(true, edgeFlags, ferrySpeed);
        }

        for (String restriction : restrictions) {
            if (way.hasTag(restriction, "destination")) {
                // This is problematic as Speed != Time
                speedEncoder.setDecimal(false, edgeFlags, destinationSpeed);
                speedEncoder.setDecimal(true, edgeFlags, destinationSpeed);
            }
        }

        return edgeFlags;
    }

    /**
     * make sure that isOneway is called before
     */
    protected boolean isBackwardOneway(ReaderWay way) {
        return way.hasTag("oneway", "-1")
                || way.hasTag("vehicle:forward", "no")
                || way.hasTag("motor_vehicle:forward", "no");
    }

    /**
     * make sure that isOneway is called before
     */
    protected boolean isForwardOneway(ReaderWay way) {
        return !way.hasTag("oneway", "-1")
                && !way.hasTag("vehicle:forward", "no")
                && !way.hasTag("motor_vehicle:forward", "no");
    }

    protected boolean isOneway(ReaderWay way) {
        return way.hasTag("oneway", oneways)
                || way.hasTag("vehicle:backward")
                || way.hasTag("vehicle:forward")
                || way.hasTag("motor_vehicle:backward")
                || way.hasTag("motor_vehicle:forward");
    }

    protected double getSpeed(ReaderWay way) {
        String highwayValue = way.getTag("highway");
        if (!Helper.isEmpty(highwayValue) && way.hasTag("motorroad", "yes")
                && highwayValue != "motorway" && highwayValue != "motorway_link") {
            highwayValue = "motorroad";
        }
        Integer speed = _speedLimitHandler.getSpeed(highwayValue);
        int maxSpeed = (int) Math.round(getMaxSpeed(way)); // Runge
        if (maxSpeed > 0)
            speed = maxSpeed;
        else
        {
            maxSpeed = _speedLimitHandler.getMaxSpeed(way); // Runge
            if (maxSpeed > 0)
                speed = maxSpeed;
        }

        if (speed == null)
            throw new IllegalStateException(toString() + ", no speed found for: " + highwayValue + ", tags: " + way);

        if (highwayValue.equals("track")) {
            String tt = way.getTag("tracktype");
            if (!Helper.isEmpty(tt)) {
                Integer tInt = _speedLimitHandler.getTrackTypeSpeed(tt);
                if (tInt != null && tInt != -1)
                    speed = tInt;
            }
        }

        if (way.hasTag("access")) // Runge  //https://www.openstreetmap.org/way/132312559
        {
            String accessTag = way.getTag("access");
            if ("destination".equals(accessTag))
                return 1;
        }

        return speed;
    }

    /**
     * @param way:   needed to retrieve tags
     * @param speed: speed guessed e.g. from the road type or other tags
     * @return The assumed speed
     */
    protected double getSurfaceSpeed(ReaderWay way, double speed) {
        // limit speed if bad surface
        //if (badSurfaceSpeed > 0 && speed > badSurfaceSpeed && way.hasTag("surface", badSurfaceSpeedMap))
        //    speed = badSurfaceSpeed;
        String surface = way.getTag("surface");
        if (surface != null)
        {
            Integer surfaceSpeed = _speedLimitHandler.getSurfaceSpeed(surface);
            if (speed > surfaceSpeed && surfaceSpeed != -1)
                return surfaceSpeed;
        }

        return speed;
    }

    public String getWayInfo(ReaderWay way) {
        String str = "";
        String highwayValue = way.getTag("highway");
        // for now only motorway links
        if ("motorway_link".equals(highwayValue)) {
            String destination = way.getTag("destination");
            if (!Helper.isEmpty(destination)) {
                int counter = 0;
                for (String d : destination.split(";")) {
                    if (d.trim().isEmpty())
                        continue;

                    if (counter > 0)
                        str += ", ";

                    str += d.trim();
                    counter++;
                }
            }
        }
        if (str.isEmpty())
            return str;
        // I18N
        if (str.contains(","))
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
            }
            catch(Exception ex)
            {}
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
        }

        return 10;
    }

    double addResedentialPenalty(double baseSpeed, ReaderWay way) {
        if (baseSpeed == 0)
            return 0;
        double speed = baseSpeed;

        if(way.hasTag("highway","residential")) {
            double estDist = way.getTag("estimated_distance", Double.MAX_VALUE);
            // take into account number of nodes to get an average distance between nodes
            double interimDistance = estDist;
            int interimNodes = way.getNodes().size() - 2;
            if(interimNodes > 0) {
                interimDistance = estDist/(interimNodes+1);
            }
            if(interimDistance < 100) {
                speed = speed * 0.5;
            }
            //Don't go below 2.5 because it will be stored as 0 later
            if(speed < 5)
                speed = 5;
        }

        return speed;
    }

    double averageSecondsTo100KmpH() { return 10; }

    /**
     * Returns how many seconds it is assumed that this vehicle would reach 100 km/h taking into acocunt the acceleration modifier
     *
     * @return
     */
    double secondsTo100KmpH() {
        return averageSecondsTo100KmpH() + (accelerationModifier * averageSecondsTo100KmpH());
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
    double adjustSpeedForAcceleration(double distance, double maximumSpeedInKmph) {
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

            double averageSpeed;

            if (distanceAtMaxSpeed < 0) {
                averageSpeed = convertMpsToKmph(distance / (durationToTravelDistance(0, maximumSpeedInKmph, distance / 2) * 2));
            } else {
                double timeAtMaxSpeed = distanceAtMaxSpeed / convertKmphToMps(maximumSpeedInKmph);
                double averageSpeedMps = distance / (timeToMaxSpeed*2 + timeAtMaxSpeed);

                averageSpeed = convertMpsToKmph(averageSpeedMps);
            }

            return averageSpeed;
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
        _speedLimitHandler = new SpeedLimitHandler(profile, defaultSpeedMap, badSurfaceSpeedMap, trackTypeSpeedMap);
    }
}
