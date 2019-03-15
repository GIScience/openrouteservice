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
import com.graphhopper.routing.util.EncodedValue;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;

import java.util.*;

import static com.graphhopper.routing.util.PriorityCode.BEST;
import static com.graphhopper.routing.util.PriorityCode.UNCHANGED;

public class HeavyVehicleFlagEncoder extends ORSAbstractFlagEncoder
{
    protected final HashSet<String> forwardKeys = new HashSet<String>(5);
    protected final HashSet<String> backwardKeys = new HashSet<String>(5);
    protected final List<String> hgvAccess = new ArrayList<String>(5);

    // Take into account acceleration calculations when determining travel speed
    protected boolean useAcceleration = false;
    
    protected int maxTrackGradeLevel = 3;
    
    /**
     * A map which associates string to speed. Get some impression:
     * http://www.itoworld.com/map/124#fullscreen
     * http://wiki.openstreetmap.org/wiki/OSM_tags_for_routing/Maxspeed
     */
	private EncodedValue preferWayEncoder;
	
    /**
     * Should be only instantied via EncodingManager
     */
    public HeavyVehicleFlagEncoder()
    {
        this(5, 5, 0);
    }

    public HeavyVehicleFlagEncoder(PMap properties)
    {
        this(properties.getInt("speed_bits", 5),
        		properties.getDouble("speed_factor", 5),
        		properties.getBool("turn_costs", false) ? 3 : 0);
        
        setBlockFords(false);
        
        maxTrackGradeLevel = properties.getInt("maximum_grade_level", 1);

        this.useAcceleration = properties.getBool("use_acceleration", false);
    }

    public HeavyVehicleFlagEncoder( int speedBits, double speedFactor, int maxTurnCosts )
    {
        super(speedBits, speedFactor, maxTurnCosts);
        restrictions.addAll(Arrays.asList("motorcar", "motor_vehicle", "vehicle", "access"));
        restrictedValues.add("private");
        restrictedValues.add("no");
        restrictedValues.add("restricted");
        restrictedValues.add("military");

        intendedValues.add("yes");
        intendedValues.add("permissive");
        intendedValues.add("designated");
        intendedValues.add("destination");  // This is needed to allow the passing of barriers that are marked as destination

        intendedValues.add("agricultural");
        intendedValues.add("forestry");
        intendedValues.add("delivery");
        intendedValues.add("bus");
        intendedValues.add("hgv");
        intendedValues.add("goods");

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
        trackTypeSpeedMap.put("grade1", 20); // paved
        trackTypeSpeedMap.put("grade2", 15); // now unpaved - gravel mixed with ...
        trackTypeSpeedMap.put("grade3", 10); // ... hard and soft materials
        trackTypeSpeedMap.put("grade4", 5); // ... some hard or compressed materials
        trackTypeSpeedMap.put("grade5", 5); // ... no hard materials. soil/sand/grass

        Map<String, Integer> badSurfaceSpeedMap =  new HashMap<String, Integer>();

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

        Map<String, Integer> defaultSpeedMap = new HashMap<String, Integer>();
        // autobahn
        defaultSpeedMap.put("motorway", 80);
        defaultSpeedMap.put("motorway_link", 50);
        defaultSpeedMap.put("motorroad", 80);
        // bundesstraße
        defaultSpeedMap.put("trunk", 80);
        defaultSpeedMap.put("trunk_link", 50);
        // linking bigger town
        defaultSpeedMap.put("primary", 60);  
        defaultSpeedMap.put("primary_link", 50);
        // linking towns + villages
        defaultSpeedMap.put("secondary", 60);
        defaultSpeedMap.put("secondary_link", 50);
        // streets without middle line separation
        defaultSpeedMap.put("tertiary", 60);
        defaultSpeedMap.put("tertiary_link", 50);
        defaultSpeedMap.put("unclassified", 60);
        defaultSpeedMap.put("residential", 60);
        // spielstraße
        defaultSpeedMap.put("living_street", 10);
        defaultSpeedMap.put("service", 20);
        // unknown road
        defaultSpeedMap.put("road", 20);
        // forestry stuff
        defaultSpeedMap.put("track", 15);
        
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
        speedEncoder = new EncodedDoubleValue("Speed", shift, speedBits, speedFactor, _speedLimitHandler.getSpeed("secondary"), _speedLimitHandler.getSpeed("motorway"));
        shift += speedEncoder.getBits();

        preferWayEncoder = new EncodedValue("PreferWay", shift, 3, 1, 0, 7);
		shift += preferWayEncoder.getBits();

		return shift;
    }

	@Override
	public double getDouble(long flags, int key) {
		switch (key) {
		case PriorityWeighting.KEY:
			double prio = preferWayEncoder.getValue(flags);
			if (prio == 0)
				return (double) UNCHANGED.getValue() / BEST.getValue();
			return prio / BEST.getValue();
		default:
			return super.getDouble(flags, key);
		}
	}
	
	@Override
	public double getMaxSpeed( ReaderWay way ) // runge
	{
		boolean bCheckMaxSpeed = false;
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

		if (bCheckMaxSpeed)
		{
			double defaultSpeed = _speedLimitHandler.getSpeed(way.getTag("highway"));
			if (defaultSpeed < maxSpeed)
				maxSpeed = defaultSpeed;
		}

		return maxSpeed;
	}

    @Override
    double averageSecondsTo100KmpH() {
        return 10;
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
    protected double getSpeed(ReaderWay way )
    {
        String highwayValue = way.getTag("highway");
        Integer speed = _speedLimitHandler.getSpeed(highwayValue);
        if (speed == null)
            throw new IllegalStateException(toString() + ", no speed found for:" + highwayValue);

        if (highwayValue.equals("track"))
        {
            String tt = way.getTag("tracktype");
            if (!Helper.isEmpty(tt))
            {
                Integer tInt = _speedLimitHandler.getTrackTypeSpeed(tt);
                if (tInt != null && tInt != -1)
                    speed = tInt;
            }
        }
        
        String hgvSpeed = way.getTag("maxspeed:hgv");
        if (!Helper.isEmpty(hgvSpeed))
        {
        	try
        	{
        		if ("walk".equals(hgvSpeed))
        			speed = 10;
        		else
        	        speed = Integer.parseInt(hgvSpeed);
        	}
        	catch(Exception ex)
        	{
        		// TODO
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
        
        String firstValue = way.getFirstPriorityTag(restrictions);
        if (highwayValue == null)
        {
            if (way.hasTag("route", ferries))
            {
            	 if (restrictedValues.contains(firstValue))
                     return 0;
                 if (intendedValues.contains(firstValue) ||
                         // implied default is allowed only if foot and bicycle is not specified:
                         firstValue.isEmpty() && !way.hasTag("foot") && !way.hasTag("bicycle"))
                     return acceptBit | ferryBit;
            }
            return 0;
        }
        
        if ("track".equals(highwayValue))
        {
            String tt = way.getTag("tracktype");
            int grade = getTrackGradeLevel(tt);
            if (grade > maxTrackGradeLevel)
                return 0;
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
        boolean carsAllowed = way.hasTag(restrictions, intendedValues);
        if (isBlockFords() && ("ford".equals(highwayValue) || way.hasTag("ford")) && !carsAllowed)
            return 0;

        // check access restrictions
        if (way.hasTag(restrictions, restrictedValues) && !carsAllowed)
        {
        	// filter special type of access for hgv
        	if (!way.hasTag(hgvAccess, intendedValues))
        		return 0;
        }
        
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

    @Override
    public long handleRelationTags( ReaderRelation relation, long oldRelationFlags )
    {
        return oldRelationFlags;
    }

	@Override
	public long getLong(long flags, int key) {
		switch (key) {
		case PriorityWeighting.KEY:
			return preferWayEncoder.getValue(flags);
		default:
			return super.getLong(flags, key);
		}
	}

	@Override
	public long setLong(long flags, int key, long value) {
		switch (key) {
		case PriorityWeighting.KEY:
			return preferWayEncoder.setValue(flags, value);
		default:
			return super.setLong(flags, key, value);
		}
	}

    @Override
    public long handleWayTags( ReaderWay way, long allowed, long relationFlags )
    {
        if (!isAccept(allowed))
            return 0;

        long flags = 0;
        if (!isFerry(allowed))
        {
            double speed = getSpeed(way);
            speed = applyMaxSpeed(way, speed);
            
            String surface = way.getTag("surface");
        	if (surface != null)
        	{
        		Integer surfaceSpeed = _speedLimitHandler.getSurfaceSpeed(surface);
        		if (speed > surfaceSpeed && surfaceSpeed != -1)
        			speed = surfaceSpeed;
        	}

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
        } else
        {
        	 double ferrySpeed = getFerrySpeed(way, _speedLimitHandler.getSpeed("living_street"), _speedLimitHandler.getSpeed("service"), _speedLimitHandler.getSpeed("residential"));
             flags = setSpeed(flags, ferrySpeed);
             flags |= directionBitMask;
        }

        return flags;
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
		else
		{
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
    public long reverseFlags( long flags )
    {
        // swap access
        flags = super.reverseFlags(flags);
       	return flags;
    }
 
    public String getWayInfo( ReaderWay way )
    {
        String str = "";
        String highwayValue = way.getTag("highway");
        // for now only motorway links
        if ("motorway_link".equals(highwayValue))
        {
            String destination = way.getTag("destination");
            if (!Helper.isEmpty(destination))
            {
                int counter = 0;
                for (String d : destination.split(";"))
                {
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


    public boolean supports(Class<?> feature) {
		if (super.supports(feature))
			return true;
		return PriorityWeighting.class.isAssignableFrom(feature);
	}
    
    @Override
    public String toString()
    {
        return FlagEncoderNames.HEAVYVEHICLE;
    }

	@Override
	public int getVersion() {
		// TODO Auto-generated method stub
		return 1;
	}
}
