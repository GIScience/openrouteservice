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
package heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodedDoubleValue;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;

import java.util.*;

public class EmergencyFlagEncoder extends ORSAbstractFlagEncoder
{
    protected final HashSet<String> forwardKeys = new HashSet<String>(5);
    protected final HashSet<String> backwardKeys = new HashSet<String>(5);
    protected final HashSet<String> noValues = new HashSet<String>(5);
    protected final HashSet<String> yesValues = new HashSet<String>(5);
    protected final List<String> hgvAccess = new ArrayList<String>(5);
    
    // This value determines the maximal possible on roads with bad surfaces
    protected int badSurfaceSpeed;

    // This value determines the speed for roads with access=destination
    protected int destinationSpeed;
    
    /**
     * A map which associates string to speed. Get some impression:
     * http://www.itoworld.com/map/124#fullscreen
     * http://wiki.openstreetmap.org/wiki/OSM_tags_for_routing/Maxspeed
     */
	
    /**
     * Should be only instantied via EncodingManager
     */
    public EmergencyFlagEncoder()
    {
        this(5, 5, 0);
    }

    public EmergencyFlagEncoder(PMap properties)
    {
        this(properties.getInt("speed_bits", 5),
        		properties.getDouble("speed_factor", 5),
        		properties.getBool("turn_costs", false) ? 3 : 0);
        
        setBlockFords(false);
    }

    public EmergencyFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts )
    {
        super(speedBits, speedFactor, maxTurnCosts);
        restrictions.addAll(Arrays.asList("motorcar", "motor_vehicle", "vehicle", "access"));
        restrictedValues.add("private");
        restrictedValues.add("agricultural");
        restrictedValues.add("forestry");
        restrictedValues.add("no");
        restrictedValues.add("restricted");
        restrictedValues.add("delivery");

        intendedValues.add("yes");
        intendedValues.add("permissive");
        
        hgvAccess.addAll(Arrays.asList("hgv", "goods", "bus", "agricultural", "forestry", "delivery"));

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

        Map<String, Integer> trackTypeSpeedMap = new HashMap<String, Integer>();
        trackTypeSpeedMap.put("grade1", 25); // paved
        trackTypeSpeedMap.put("grade2", 15); // now unpaved - gravel mixed with ...
        trackTypeSpeedMap.put("grade3", 15); // ... hard and soft materials
        trackTypeSpeedMap.put("grade4", 10); // ... some hard or compressed materials
        trackTypeSpeedMap.put("grade5", 5); // ... no hard materials. soil/sand/grass

        Map<String, Integer> badSurfaceSpeedMap = new HashMap<String, Integer>();
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

        Map<String, Integer> defaultSpeedMap = new HashMap<String, Integer>();
        // autobahn
        defaultSpeedMap.put("motorway", 130);
        defaultSpeedMap.put("motorway_link", 50);
        defaultSpeedMap.put("motorroad", 130);
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
        defaultSpeedMap.put("service", 20);
        // unknown road
        defaultSpeedMap.put("road", 20);
        // forestry stuff
        defaultSpeedMap.put("track", 15);
        // additional available for emergency
        defaultSpeedMap.put("raceway", 100);
        defaultSpeedMap.put("cycleway", 10);
        // how to declare this ?
        defaultSpeedMap.put("aeroway=runway", 100);
        defaultSpeedMap.put("aeroway=taxilane", 100);
        
        // FIXME: allow highway=footway, pedestrian
        
        _speedLimitHandler = new SpeedLimitHandler(this.toString(), defaultSpeedMap, badSurfaceSpeedMap, trackTypeSpeedMap);

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

        init();
    }
    
	public double getDefaultMaxSpeed()
	{
		return 80;
	}

    /**
     * Define the place of the speedBits in the edge flags for car.
     */
    @Override
    public int defineWayBits( int index, int shift )
    {
        // first two bits are reserved for route handling in superclass
        shift = super.defineWayBits(index, shift);
        speedEncoder = new EncodedDoubleValue("Speed", shift, speedBits, speedFactor, _speedLimitHandler.getSpeed("secondary"), maxPossibleSpeed);
        shift += speedEncoder.getBits();
	
		return shift;
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
	public double getMaxSpeed(ReaderWay way ) // runge
	{
		boolean bCheckMaxSpeed = true;
/*
		String maxspeedTag = way.getTag("maxspeed:hgv");
		if (maxspeedTag == null)
		{
			maxspeedTag = way.getTag("maxspeed");
			bCheckMaxSpeed = true;
		}
		
		double maxSpeed = parseSpeed(maxspeedTag);

		double fwdSpeed = parseSpeed(way.getTag("maxspeed:forward"));
		if (fwdSpeed >= 0 && (maxSpeed < 0 || fwdSpeed < maxSpeed))
			maxSpeed = fwdSpeed;

		double backSpeed = parseSpeed(way.getTag("maxspeed:backward"));
		if (backSpeed >= 0 && (maxSpeed < 0 || backSpeed < maxSpeed))
			maxSpeed = backSpeed;
*/
		String maxspeedTag = way.getTag("maxspeed:hgv");
		if (Helper.isEmpty(maxspeedTag))
			maxspeedTag = way.getTag("maxspeed");
		double maxSpeed = parseSpeed(maxspeedTag);
		
		if (bCheckMaxSpeed)
		{
			String highway = way.getTag("highway");
			double defaultSpeed = _speedLimitHandler.getSpeed(highway);
			if (defaultSpeed < maxSpeed) // TODO
				maxSpeed = defaultSpeed;
		}
/**
 * 
        // Amandus
        if (speed == 30)
            speed = 50;// seems to be way to easy like that
        if (speed == 70)
            speed = 80;
 */
 
		return maxSpeed;
	}
	
    protected double getSpeed(ReaderWay way )
    {
    	 String highwayValue = way.getTag("highway");
         if (!Helper.isEmpty(highwayValue) && way.hasTag("motorroad", "yes")
                 && highwayValue != "motorway" && highwayValue != "motorway_link") {
             highwayValue = "motorroad";
         }
         Integer speed = _speedLimitHandler.getSpeed(highwayValue);
         if (speed == null)
             throw new IllegalStateException(toString() + ", no speed found for: " + highwayValue + ", tags: " + way);

         if (highwayValue.equals("track")) {
             String tt = way.getTag("tracktype");
             if (!Helper.isEmpty(tt)) {
                 Integer tInt = _speedLimitHandler.getTrackTypeSpeed(tt); // FIXME
                 if (tInt != null && tInt != -1)
                     speed = tInt;
             }
         }

     /*   if (way.hasTag("access")) // Runge  //https://www.openstreetmap.org/way/132312559
        {
        	String accessTag = way.getTag("access");
        	if ("destination".equals(accessTag))
        		return 1; 
        }*/

        return speed;
    }

    @Override
    public long acceptWay(ReaderWay way)
    {
        String highwayValue = way.getTag("highway");
        
        if (highwayValue == null)
        {
            if (way.hasTag("route", ferries))
            {
                String motorcarTag = way.getTag("motorcar");
                if (motorcarTag == null)
                    motorcarTag = way.getTag("motor_vehicle");

                if (motorcarTag == null && !way.hasTag("foot") && !way.hasTag("bicycle") || "yes".equals(motorcarTag))
                    return acceptBit | ferryBit;
            }
            return 0;
        }
        
        // if ("track".equals(highwayValue))
        // {
        //     String tt = way.getTag("tracktype");
        //     if (tt != null && !tt.equals("grade1")) // TODO allow higher grade values for forestry and agriculture
        //     	return 0;
            	
        //     if (tt != null && !trackTypeSpeedMap.containsKey(tt))
        //         return 0;
        // }

        if (!_speedLimitHandler.hasSpeedValue(highwayValue))
            return 0;

        if (way.hasTag("impassable", "yes") || way.hasTag("status", "impassable"))
            return 0;

        // do not drive street cars into fords
        // boolean carsAllowed = way.hasTag(restrictions, intendedValues);
        // if (isBlockFords() && ("ford".equals(highwayValue) || way.hasTag("ford")) && !carsAllowed)
        //     return 0;
        if (isBlockFords() && ("ford".equals(highwayValue) || way.hasTag("ford")))
            return 0;

        // check access restrictions
        // if (way.hasTag(restrictions, restrictedValues) && !carsAllowed)
        // {
        // 	// filter special type of access for hgv
        // 	if (!way.hasTag(hgvAccess, intendedValues))
        // 		return 0;
        // }

        // Amandus
        if (way.hasTag("lanes:psv") || way.hasTag("lanes:bus") || way.hasTag("lanes:taxi") || way.hasTag("busway, lane") || way.hasTag("busway:left, lane") || way.hasTag("busway:right, lane"))
            return acceptBit;
        // allow railway=tram where paved? no suitable exclusion criteria found yet

        // do not drive cars over railways (sometimes incorrectly mapped!)
    /*    if (way.hasTag("railway") && !way.hasTag("railway", acceptedRailways))
        {
      	  // Runge, see http://www.openstreetmap.org/way/36106092
      	    String motorcarTag = way.getTag("motorcar");
            if (motorcarTag == null)
                motorcarTag = way.getTag("motor_vehicle");

            if (motorcarTag == null || "no".equals(motorcarTag))
          	  return 0;
        }*/
        
        return acceptBit;
    }

    @Override
    public long handleRelationTags(ReaderRelation relation, long oldRelationFlags )
    {
        return oldRelationFlags;
    }

    @Override
    public long handleWayTags( ReaderWay way, long allowed, long relationFlags )
    {
    	if (!isAccept(allowed))
            return 0;

        long flags = 0;
        if (!isFerry(allowed)) {
            // get assumed speed from highway type
            double speed = getSpeed(way);
            speed = applyMaxSpeed(way, speed);

            speed = applyBadSurfaceSpeed(way, speed);

            flags = setSpeed(flags, speed);

            boolean isRoundabout = way.hasTag("junction", "roundabout");
            if (isRoundabout)
                flags = setBool(flags, K_ROUNDABOUT, true);

            if ((isOneway(way) || isRoundabout) && speed > 80) {
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

        /*
        for (String restriction : restrictions) {
            if (way.hasTag(restriction, "destination")) {
                // This is problematic as Speed != Time
                flags = this.speedEncoder.setDoubleValue(flags, destinationSpeed);
            }
        }
          */
        return flags;
    }
    
    /**
     * @param way:   needed to retrieve tags
     * @param speed: speed guessed e.g. from the road type or other tags
     * @return The assumed speed
     */
    protected double applyBadSurfaceSpeed(ReaderWay way, double speed) {
        // limit speed if bad surface
       // if (badSurfaceSpeed > 0 && speed > badSurfaceSpeed && way.hasTag("surface", badSurfaceSpeedMap))
       //     speed = badSurfaceSpeed;
    	String surface = way.getTag("surface");
    	if (surface != null)
    	{
    		Integer surfaceSpeed = _speedLimitHandler.getSurfaceSpeed(surface);
    		if (speed > surfaceSpeed && surfaceSpeed != -1)
    		   return surfaceSpeed;
    	}
        return speed;
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

    
    protected int handlePriority(ReaderWay way) {
		TreeMap<Double, Integer> weightToPrioMap = new TreeMap<Double, Integer>();
		
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
		if (way.hasTag("hgv", "designated") || (way.hasTag("access", "designated") && (way.hasTag("goods", "yes") || way.hasTag("hgv", "yes") || way.hasTag("bus", "yes") || way.hasTag("agricultural", "yes") || way.hasTag("forestry", "yes") )))
			weightToPrioMap.put(100d, PriorityCode.BEST.getValue());
		// Amandus
        else if (way.hasTag("highway", "service") && way.hasTag("service", "emergency_access"))
            weightToPrioMap.put(100d, PriorityCode.BEST.getValue());
        else
		{
            // Amandus
			String busway = way.getTag("busway");// FIXME || way.getTag("busway:right") || way.getTag("busway:left");
            if (!Helper.isEmpty(busway))
            {
                if ("lane".equals(busway))
                    weightToPrioMap.put(10d, PriorityCode.PREFER.getValue());
            }

            String highway = way.getTag("highway");
			double maxSpeed = getMaxSpeed(way);
			
			if (!Helper.isEmpty(highway))
			{
				if ("motorway".equals(highway) || "motorway_link".equals(highway) || "trunk".equals(highway) || "trunk_link".equals(highway))
					weightToPrioMap.put(100d,  PriorityCode.BEST.getValue());
				else if ("primary".equals(highway) || "primary_link".equals(highway))
					weightToPrioMap.put(100d,  PriorityCode.PREFER.getValue());
				else if ("secondary".equals(highway) || "secondary_link".equals(highway))
					weightToPrioMap.put(100d,  PriorityCode.PREFER.getValue());
				else if ("tertiary".equals(highway) || "tertiary_link".equals(highway))
					weightToPrioMap.put(100d,  PriorityCode.UNCHANGED.getValue());
				else if ("residential".equals(highway) || "service".equals(highway) || "road".equals(highway) || "unclassified".equals(highway))
				{
					 if (maxSpeed > 0 && maxSpeed <= 30)
						 weightToPrioMap.put(120d,  PriorityCode.REACH_DEST.getValue());
					 else
						 weightToPrioMap.put(100d,  PriorityCode.AVOID_IF_POSSIBLE.getValue());
				}
				else if ("living_street".equals(highway))
					 weightToPrioMap.put(100d,  PriorityCode.AVOID_IF_POSSIBLE.getValue());
				else if ("track".equals(highway))
					 weightToPrioMap.put(100d,  PriorityCode.REACH_DEST.getValue());
				else 
					weightToPrioMap.put(40d, PriorityCode.AVOID_IF_POSSIBLE.getValue());
			}
			else	
				weightToPrioMap.put(100d, PriorityCode.UNCHANGED.getValue());
			
			if (maxSpeed > 0)
			{
				// We assume that the given road segment goes through a settlement.
				if (maxSpeed <= 40)
					weightToPrioMap.put(110d, PriorityCode.AVOID_IF_POSSIBLE.getValue());
				else if (maxSpeed <= 50)
					weightToPrioMap.put(110d, PriorityCode.UNCHANGED.getValue());
			}
		}
	}
    
    @Override
    public String toString()
    {
        return FlagEncoderNames.EMERGENCY;
    }

	@Override
	public int getVersion() {
		// TODO Auto-generated method stub
		return 1;
	}
}
