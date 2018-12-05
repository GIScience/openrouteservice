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
package heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodedDoubleValue;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines bit layout for cars. (speed, access, ferries, ...)
 * <p>
 *
 * @author Peter Karich
 * @author Nop
 */
public class CarFlagEncoder extends ORSAbstractFlagEncoder {

    private static Logger LOGGER = Logger.getLogger(CarFlagEncoder.class);

    // This value determines the maximal possible on roads with bad surfaces
    protected int badSurfaceSpeed;

    // This value determines the speed for roads with access=destination
    protected int destinationSpeed;
    /**
     * A map which associates string to speed. Get some impression:
     * http://www.itoworld.com/map/124#fullscreen
     * http://wiki.openstreetmap.org/wiki/OSM_tags_for_routing/Maxspeed
     */
    protected int maxTrackGradeLevel = 3;

    // Take into account acceleration calculations when determining travel speed
    protected boolean useAcceleration = false;

    public CarFlagEncoder() {
        this(5, 5, 0);
    }

    public CarFlagEncoder(PMap properties) {
        this((int) properties.getLong("speed_bits", 5),
                properties.getDouble("speed_factor", 5),
                properties.getBool("turn_costs", false) ? 1 : 0);
        this.properties = properties;
        this.setBlockFords(properties.getBool("block_fords", true));
        this.setBlockByDefault(properties.getBool("block_barriers", true));

        this.useAcceleration = properties.getBool("use_acceleration", false);

        maxTrackGradeLevel = properties.getInt("maximum_grade_level", 3);
    }

    public CarFlagEncoder(String propertiesStr) {
        this(new PMap(propertiesStr));
    }

    public CarFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
        super(speedBits, speedFactor, maxTurnCosts);
        restrictions.addAll(Arrays.asList("motorcar", "motor_vehicle", "vehicle", "access"));
        restrictedValues.add("private");
        restrictedValues.add("agricultural");
        restrictedValues.add("forestry");
        restrictedValues.add("no");
        restrictedValues.add("restricted");
        restrictedValues.add("delivery");
        restrictedValues.add("military");
        restrictedValues.add("emergency");

        intendedValues.add("yes");
        intendedValues.add("permissive");
        intendedValues.add("destination");  // This is needed to allow the passing of barriers that are marked as destination

        potentialBarriers.add("gate");
        potentialBarriers.add("lift_gate");
        potentialBarriers.add("kissing_gate");
        potentialBarriers.add("swing_gate");

        absoluteBarriers.add("bollard");
        absoluteBarriers.add("stile");
        absoluteBarriers.add("turnstile");
        absoluteBarriers.add("cycle_barrier");
        absoluteBarriers.add("motorcycle_barrier");
        absoluteBarriers.add("block");
        absoluteBarriers.add("bus_trap");
        absoluteBarriers.add("sump_buster");
        
        Map<String, Integer> trackTypeSpeedMap = new HashMap<String, Integer>();

        trackTypeSpeedMap.put("grade1", 40); // paved
        trackTypeSpeedMap.put("grade2", 30); // now unpaved - gravel mixed with ...
        trackTypeSpeedMap.put("grade3", 20); // ... hard and soft materials
        trackTypeSpeedMap.put("grade4", 15);
        trackTypeSpeedMap.put("grade5", 10);

        Map<String, Integer> badSurfaceSpeedMap = new HashMap<String, Integer>();

        // limit speed on bad surfaces to 30 km/h
        badSurfaceSpeed = 30;

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

        destinationSpeed = 5;

        maxPossibleSpeed = 140;

        Map<String, Integer> defaultSpeedMap = new HashMap<String, Integer>();
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
        
        _speedLimitHandler = new SpeedLimitHandler(this.toString(), defaultSpeedMap, badSurfaceSpeedMap, trackTypeSpeedMap);

        init();
    }

    @Override
    public int getVersion() {
        return 1;
    }

    /**
     * Define the place of the speedBits in the edge flags for car.
     */
    @Override
    public int defineWayBits(int index, int shift) {
        // first two bits are reserved for route handling in superclass
        shift = super.defineWayBits(index, shift);
        speedEncoder = new EncodedDoubleValue("Speed", shift, speedBits, speedFactor, _speedLimitHandler.getSpeed("secondary"),
                maxPossibleSpeed);
        return shift + speedEncoder.getBits();
    }

    @Override
    double averageSecondsTo100KmpH() {
        return 10;
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

    @Override
    public long acceptWay(ReaderWay way) {
        // TODO: Ferries have conditionals, like opening hours or are closed during some time in the year
        String highwayValue = way.getTag("highway");
        String firstValue = way.getFirstPriorityTag(restrictions);
        if (highwayValue == null) {
            if (way.hasTag("route", ferries)) {
                if (restrictedValues.contains(firstValue))
                    return 0;
                if (intendedValues.contains(firstValue) ||
                        // implied default is allowed only if foot and bicycle is not specified:
                        firstValue.isEmpty() && !way.hasTag("foot") && !way.hasTag("bicycle"))
                    return acceptBit | ferryBit;
            }
            return 0;
        }

        if ("track".equals(highwayValue)) {
            String tt = way.getTag("tracktype");
            //if (tt != null && !tt.equals("grade1") && !tt.equals("grade2") && !tt.equals("grade3"))
            if (tt != null)
            {
            	int grade = getTrackGradeLevel(tt);
            	if (grade > maxTrackGradeLevel)
            		return 0;
            }
        }

        if (!_speedLimitHandler.hasSpeedValue(highwayValue))
            return 0;

        if (way.hasTag("impassable", "yes") || way.hasTag("status", "impassable") || way.hasTag("smoothness", "impassable"))
            return 0;

        // multiple restrictions needs special handling compared to foot and bike, see also motorcycle
        if (!firstValue.isEmpty()) {
            if (restrictedValues.contains(firstValue) && !getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way))
                return 0;
            if (intendedValues.contains(firstValue))
                return acceptBit;
        }

        // do not drive street cars into fords
        if (isBlockFords() && ("ford".equals(highwayValue) || way.hasTag("ford")))
            return 0;
        
        
        String maxwidth = way.getTag("maxwidth"); // Runge added on 23.02.2016
        if (maxwidth != null)
        {
        	try
            {
        		double mwv = Double.parseDouble(maxwidth);
        		if (mwv < 2.0)
        			return 0;
            }
        	catch(Exception ex)
            {
            	
            }
        }

        if (getConditionalTagInspector().isPermittedWayConditionallyRestricted(way))
            return 0;
        else
            return acceptBit;
    }
    
    protected int getTrackGradeLevel(String grade)
    {
    	if (grade == null)
    		return 0; 
    	 
    	if (grade.contains(";")) // grade3;grade2
    	{
    		int maxGrade = 0; 
    		
    		try
    		{
    			String[] values = grade.split(";"); 
    			for(String v : values)
    			{
    		       int iv = Integer.parseInt(v.replace("grade","").trim());
    		       if (iv > maxGrade)
    		    	   maxGrade = iv;
    			}
    			
    			return maxGrade;
    		}
    		catch(Exception ex)
    		{}
    	}

    	switch(grade)
    	{
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

    @Override
    public long handleRelationTags(ReaderRelation relation, long oldRelationFlags) {
        return oldRelationFlags;
    }

    @Override
    public long handleWayTags(ReaderWay way, long allowed, long relationFlags) {
        if (!isAccept(allowed))
            return 0;

        long flags = 0;
        if (!isFerry(allowed)) {
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

            if (isRoundabout) // Runge
            {
            	//http://www.sidrasolutions.com/Documents/OArndt_Speed%20Control%20at%20Roundabouts_23rdARRBConf.pdf
            	if (way.hasTag("highway", "mini_roundabout"))
            		speed = speed < 25 ? speed : 25;
            	
            	if (way.hasTag("lanes"))
            	{
            		try
            		{
            			// The following line throws exceptions when it tries to parse a value "3; 2"
            			int lanes = Integer.parseInt(way.getTag("lanes"));
            			if (lanes >= 2)
            				speed  = speed < 40 ? speed : 40;
            			else
            				speed  = speed < 35 ? speed : 35;
            		}
            		catch(Exception ex)
            		{}
            	}
            }

            flags = setSpeed(flags, speed);

            if (isRoundabout)
            {
                flags = setBool(flags, K_ROUNDABOUT, true);
            }

            if (isOneway(way) || isRoundabout) {
                if (isBackwardOneway(way))
                    flags |= backwardBit;

                if (isForwardOneway(way))
                    flags |= forwardBit;
            } else
                flags |= directionBitMask;

        } else {
            double ferrySpeed = getFerrySpeed(way, _speedLimitHandler.getSpeed("living_street"), _speedLimitHandler.getSpeed("service"), _speedLimitHandler.getSpeed("residential"));
            flags = setSpeed(flags, ferrySpeed);
            flags |= directionBitMask;
        }

        for (String restriction : restrictions) {
            if (way.hasTag(restriction, "destination")) {
                // This is problematic as Speed != Time
                flags = this.speedEncoder.setDoubleValue(flags, destinationSpeed);
            }
        }

        return flags;
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

    @Override
    public String toString() {
        return FlagEncoderNames.CAR_ORS;
    }
}
