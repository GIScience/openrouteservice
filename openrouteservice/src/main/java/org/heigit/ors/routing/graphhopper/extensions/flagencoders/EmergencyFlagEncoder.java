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
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.parsers.helpers.OSMValueExtractor;
import org.heigit.ors.routing.graphhopper.extensions.util.PriorityCode;
import com.graphhopper.routing.util.TransportationMode;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;

import java.util.*;

public class EmergencyFlagEncoder extends VehicleFlagEncoder {
    private static final double MEAN_SPEED = 80;
    public static final String KEY_AGRICULTURAL = "agricultural";
    public static final String KEY_MOTORWAY_LINK = "motorway_link";
    public static final String KEY_FORESTRY = "forestry";
    public static final String KEY_MOTORWAY = "motorway";
    public static final String KEY_MOTORROAD = "motorroad";
    public static final String KEY_SERVICE = "service";
    public static final String KEY_TRACK = "track";
    public static final String KEY_HIGHWAY = "highway";
    protected final HashSet<String> forwardKeys = new HashSet<>(5);
    protected final HashSet<String> backwardKeys = new HashSet<>(5);
    protected final HashSet<String> noValues = new HashSet<>(5);
    protected final HashSet<String> yesValues = new HashSet<>(5);
    protected final List<String> hgvAccess = new ArrayList<>(5);

    public double getMeanSpeed() { return MEAN_SPEED; }

    public EmergencyFlagEncoder(PMap properties) {
        this(properties.getInt("speed_bits", 5),
        		properties.getDouble("speed_factor", 5),
        		properties.getBool("turn_costs", false) ? 3 : 0);
        blockFords(false);
    }

    public EmergencyFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
        super(speedBits, speedFactor, maxTurnCosts);
        restrictions.addAll(Arrays.asList("motorcar", "motor_vehicle", "vehicle", "access"));
        restrictedValues.add("private");
        restrictedValues.add(KEY_AGRICULTURAL);
        restrictedValues.add(KEY_FORESTRY);
        restrictedValues.add("no");
        restrictedValues.add("restricted");
        restrictedValues.add("delivery");

        intendedValues.add("yes");
        intendedValues.add("permissive");
        
        hgvAccess.addAll(Arrays.asList("hgv", "goods", "bus", KEY_AGRICULTURAL, KEY_FORESTRY, "delivery"));

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

        Map<String, Integer> trackTypeSpeedMap = new HashMap<>();
        trackTypeSpeedMap.put("grade1", 25); // paved
        trackTypeSpeedMap.put("grade2", 15); // now unpaved - gravel mixed with ...
        trackTypeSpeedMap.put("grade3", 15); // ... hard and soft materials
        trackTypeSpeedMap.put("grade4", 10); // ... some hard or compressed materials
        trackTypeSpeedMap.put("grade5", 5); // ... no hard materials. soil/sand/grass

        Map<String, Integer> badSurfaceSpeedMap = new HashMap<>();
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

        Map<String, Integer> defaultSpeedMap = new HashMap<>();
        // autobahn
        defaultSpeedMap.put(KEY_MOTORWAY, 130);
        defaultSpeedMap.put(KEY_MOTORWAY_LINK, 50);
        defaultSpeedMap.put(KEY_MOTORROAD, 130);
        // bundesstraße
        defaultSpeedMap.put("trunk", 120);
        defaultSpeedMap.put("trunk_link", 50);
        // linking bigger town
        defaultSpeedMap.put("primary", 120);  
        defaultSpeedMap.put("primary_link", 50);
        // linking towns + villages
        defaultSpeedMap.put("secondary", 120);
        defaultSpeedMap.put("secondary_link", 50);
        // streets without middle line separation
        defaultSpeedMap.put("tertiary", 110);
        defaultSpeedMap.put("tertiary_link", 50);
        defaultSpeedMap.put("unclassified", 60);
        defaultSpeedMap.put("residential", 50);
        // spielstraße
        defaultSpeedMap.put("living_street", 20);
        defaultSpeedMap.put(KEY_SERVICE, 20);
        // unknown road
        defaultSpeedMap.put("road", 20);
        // forestry stuff
        defaultSpeedMap.put(KEY_TRACK, 15);
        // additional available for emergency
        defaultSpeedMap.put("raceway", 100);
        defaultSpeedMap.put("cycleway", 10);
        // how to declare this ?
        defaultSpeedMap.put("aeroway=runway", 100);
        defaultSpeedMap.put("aeroway=taxilane", 100);
        
        // FIXME: allow highway=footway, pedestrian
        
        speedLimitHandler = new SpeedLimitHandler(this.toString(), defaultSpeedMap, badSurfaceSpeedMap, trackTypeSpeedMap);

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
        
        noValues.add("no");
        noValues.add("-1");
        
        yesValues.add("yes");
        yesValues.add("1");
    }
    
    @Override
    protected double applyMaxSpeed(ReaderWay way, double speed) {
        double maxSpeed = getMaxSpeed(way);
        // We obay speed limits
        if (maxSpeed >= 0) {
            return maxSpeed;
        }
        return speed;
    }

	
	@Override
	public double getMaxSpeed(ReaderWay way) { // runge
		String maxspeedTag = way.getTag("maxspeed:hgv");
		if (Helper.isEmpty(maxspeedTag))
			maxspeedTag = way.getTag("maxspeed");
		double maxSpeed = OSMValueExtractor.stringToKmh(maxspeedTag);
		
        String highway = way.getTag(KEY_HIGHWAY);
        double defaultSpeed = speedLimitHandler.getSpeed(highway);
        if (defaultSpeed < maxSpeed) // TODO
            maxSpeed = defaultSpeed;

		return maxSpeed;
	}

	@Override
    protected double getSpeed(ReaderWay way) {
    	 String highwayValue = way.getTag(KEY_HIGHWAY);
         if (!Helper.isEmpty(highwayValue) && way.hasTag(KEY_MOTORROAD, "yes")
                 && !highwayValue.equals(KEY_MOTORWAY) && !highwayValue.equals(KEY_MOTORWAY_LINK) ) {
             highwayValue = KEY_MOTORROAD;
         }
         Integer speed = speedLimitHandler.getSpeed(highwayValue);
         if (speed == null)
             throw new IllegalStateException(this + ", no speed found for: " + highwayValue + ", tags: " + way);

         if (highwayValue.equals(KEY_TRACK)) {
             String tt = way.getTag("tracktype");
             if (!Helper.isEmpty(tt)) {
                 Integer tInt = speedLimitHandler.getTrackTypeSpeed(tt); // FIXME
                 if (tInt != null && tInt != -1)
                     speed = tInt;
             }
         }

        return speed;
    }

    @Override
    public EncodingManager.Access getAccess(ReaderWay way)
    {
        String highwayValue = way.getTag(KEY_HIGHWAY);
        
        if (highwayValue == null)
        {
            if (way.hasTag("route", ferries))
            {
                String motorcarTag = way.getTag("motorcar");
                if (motorcarTag == null)
                    motorcarTag = way.getTag("motor_vehicle");

                if (motorcarTag == null && !way.hasTag("foot") && !way.hasTag("bicycle") || "yes".equals(motorcarTag))
                    return EncodingManager.Access.FERRY;
            }
            return EncodingManager.Access.CAN_SKIP;
        }
        
        if (!speedLimitHandler.hasSpeedValue(highwayValue))
            return EncodingManager.Access.CAN_SKIP;

        if (way.hasTag("impassable", "yes") || way.hasTag("status", "impassable"))
            return EncodingManager.Access.CAN_SKIP;

        // do not drive street cars into fords
        if (isBlockFords() && ("ford".equals(highwayValue) || way.hasTag("ford")))
            return EncodingManager.Access.CAN_SKIP;

        // check access restrictions
        // Amandus
        if (way.hasTag("lanes:psv") || way.hasTag("lanes:bus") || way.hasTag("lanes:taxi") || way.hasTag("busway, lane") || way.hasTag("busway:left, lane") || way.hasTag("busway:right, lane"))
            return EncodingManager.Access.WAY; // TODO: this result is equal to the final return; can the if be removed?

        return EncodingManager.Access.WAY;
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

            boolean isRoundabout = way.hasTag("junction", "roundabout");
            if (isRoundabout)
                roundaboutEnc.setBool(false, edgeFlags, true);

            setSpeed(false, edgeFlags, speed);
            setSpeed(true, edgeFlags, speed);

            if ((isOneway(way) || isRoundabout) && speed > 80) {
                if (isForwardOneway(way))
                    accessEnc.setBool(false, edgeFlags, true);
                if (isBackwardOneway(way))
                    accessEnc.setBool(true, edgeFlags, true);
            } else {
                accessEnc.setBool(false, edgeFlags, true);
                accessEnc.setBool(true, edgeFlags, true);
            }

        } else {
            double ferrySpeed = ferrySpeedCalc.getSpeed(way);
            accessEnc.setBool(false, edgeFlags, true);
            accessEnc.setBool(true, edgeFlags, true);
            setSpeed(false, edgeFlags, ferrySpeed);
            setSpeed(true, edgeFlags, ferrySpeed);
        }

        return edgeFlags;
    }

    
    /**
	 * @param weightToPrioMap
	 *            associate a weight with every priority. This sorted map allows
	 *            subclasses to 'insert' more important priorities as well as
	 *            overwrite determined priorities.
	 */
	protected void collect(ReaderWay way, TreeMap<Double, Integer> weightToPrioMap) { // Runge
		if (way.hasTag("hgv", "designated") || (way.hasTag("access", "designated") && (way.hasTag("goods", "yes") || way.hasTag("hgv", "yes") || way.hasTag("bus", "yes") || way.hasTag(KEY_AGRICULTURAL, "yes") || way.hasTag(KEY_FORESTRY, "yes") )))
			weightToPrioMap.put(100d, PriorityCode.BEST.getValue());
		// Amandus
        else if (way.hasTag(KEY_HIGHWAY, KEY_SERVICE) && way.hasTag(KEY_SERVICE, "emergency_access"))
            weightToPrioMap.put(100d, PriorityCode.BEST.getValue());
        else {
            // Amandus
			String busway = way.getTag("busway");// FIXME || way.getTag("busway:right") || way.getTag("busway:left")
            if (!Helper.isEmpty(busway) && "lane".equals(busway))
                weightToPrioMap.put(10d, PriorityCode.PREFER.getValue());

            String highway = way.getTag(KEY_HIGHWAY);
			double maxSpeed = getMaxSpeed(way);
			
			if (!Helper.isEmpty(highway)) {
				if (KEY_MOTORWAY.equals(highway) || KEY_MOTORWAY_LINK.equals(highway) || "trunk".equals(highway) || "trunk_link".equals(highway))
					weightToPrioMap.put(100d,  PriorityCode.BEST.getValue());
				else if ("primary".equals(highway) || "primary_link".equals(highway))
					weightToPrioMap.put(100d,  PriorityCode.PREFER.getValue());
				else if ("secondary".equals(highway) || "secondary_link".equals(highway))
					weightToPrioMap.put(100d,  PriorityCode.PREFER.getValue());
				else if ("tertiary".equals(highway) || "tertiary_link".equals(highway))
					weightToPrioMap.put(100d,  PriorityCode.UNCHANGED.getValue());
				else if ("residential".equals(highway) || KEY_SERVICE.equals(highway) || "road".equals(highway) || "unclassified".equals(highway)) {
					 if (maxSpeed > 0 && maxSpeed <= 30)
						 weightToPrioMap.put(120d,  PriorityCode.REACH_DEST.getValue());
					 else
						 weightToPrioMap.put(100d,  PriorityCode.AVOID_IF_POSSIBLE.getValue());
				}
				else if ("living_street".equals(highway))
					 weightToPrioMap.put(100d,  PriorityCode.AVOID_IF_POSSIBLE.getValue());
				else if (KEY_TRACK.equals(highway))
					 weightToPrioMap.put(100d,  PriorityCode.REACH_DEST.getValue());
				else 
					weightToPrioMap.put(40d, PriorityCode.AVOID_IF_POSSIBLE.getValue());
			}
			else	
				weightToPrioMap.put(100d, PriorityCode.UNCHANGED.getValue());
			
			if (maxSpeed > 0) {
				// We assume that the given road segment goes through a settlement.
				if (maxSpeed <= 40)
					weightToPrioMap.put(110d, PriorityCode.AVOID_IF_POSSIBLE.getValue());
				else if (maxSpeed <= 50)
					weightToPrioMap.put(110d, PriorityCode.UNCHANGED.getValue());
			}
		}
	}

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final EmergencyFlagEncoder other = (EmergencyFlagEncoder) obj;
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return ("EmergencyFlagEncoder" + this).hashCode();
    }

    @Override
    public String toString()
    {
        return FlagEncoderNames.EMERGENCY;
    }

    @Override
    public TransportationMode getTransportationMode() {
        return TransportationMode.PSV;
    }
}
