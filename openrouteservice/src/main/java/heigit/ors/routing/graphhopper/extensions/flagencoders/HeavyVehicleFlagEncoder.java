package heigit.ors.routing.graphhopper.extensions.flagencoders;

import static com.graphhopper.routing.util.PriorityCode.BEST;
import static com.graphhopper.routing.util.PriorityCode.UNCHANGED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.AbstractFlagEncoder;
import com.graphhopper.routing.util.EncodedDoubleValue;
import com.graphhopper.routing.util.EncodedValue;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;

import java.util.*;

import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;

public class HeavyVehicleFlagEncoder extends AbstractFlagEncoder
{
	private class HeavyVehicleEncodedValue extends EncodedValue
	{
		private int vehicleType = -1;
		
		public HeavyVehicleEncodedValue(int shift) {
			super("VehicleDirection", shift, 2*HeavyVehicleAttributes.getVehiclesCount(), 1, 0, 1);
		}
		
		public long setValue(int vehicleType, long flags, long direction)
		{
			checkValue(direction);
	        // scale value
	        //value /= factor; factor is always 1
			// vehicle mask contains of pairs such as  00 00 00 00 00 00 where each group defines forbidden directions for each vehicle type (bf bf bf bf bf bf).

			long value = 1 << ((shift + ((vehicleType - HeavyVehicleAttributes.GOODS) >> 1)) + direction);

	        // clear value bits
	        flags &= ~mask;

	        // set value
	        return flags | value;
		}
		
		public long getValue(int vehicleType, long flags, long direction)
	    {
	        flags &= mask;
	        return ((flags >> ((shift + ((vehicleType - HeavyVehicleAttributes.GOODS) >> 1)) + direction)) & 1); 
	    }
		
		public long reverseFlags(long flags)
		{
			if (vehicleType == -1)
			{
				// reverse flags for all vehicle types.
				long bit = (shift + ((0) >> 1));
				flags ^= 1 << bit;
				flags ^= 1 << (bit+1);
				
				bit += 2;
				flags ^= 1 << bit;
				flags ^= 1 << (bit+1);
				
				bit += 2;
				flags ^= 1 << bit;
				flags ^= 1 << (bit+1);
				
				bit += 2;
				flags ^= 1 << bit;
				flags ^= 1 << (bit+1);
				
				bit += 2;
				flags ^= 1 << bit;
				flags ^= 1 << (bit+1);
				
				bit += 2;
				flags ^= 1 << bit;
				flags ^= 1 << (bit+1);
			}
			else
			{
				long bit = (shift + ((vehicleType) >> 1));
				flags ^= 1 << bit;
				flags ^= 1 << (bit+1);
			}
			
			return flags;
		}
		
		public void setVehicleType(int vt)
		{
			vehicleType = vt - HeavyVehicleAttributes.GOODS;
		}
	}
	
    protected final Map<String, Integer> trackTypeSpeedMap = new HashMap<String, Integer>();
    protected final Set<String> badSurfaceSpeedMap = new HashSet<String>();
    
    protected final HashSet<String> forwardKeys = new HashSet<String>(5);
    protected final HashSet<String> backwardKeys = new HashSet<String>(5);
    protected final HashSet<String> noValues = new HashSet<String>(5);
    protected final HashSet<String> yesValues = new HashSet<String>(5);
    protected final List<String> hgvAccess = new ArrayList<String>(5);
    
    /**
     * A map which associates string to speed. Get some impression:
     * http://www.itoworld.com/map/124#fullscreen
     * http://wiki.openstreetmap.org/wiki/OSM_tags_for_routing/Maxspeed
     */
    protected final Map<String, Integer> defaultSpeedMap = new HashMap<String, Integer>();
	private EncodedValue preferWayEncoder;
	private HeavyVehicleEncodedValue vehicleDirectionEncoder; // hgv:forward
	
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
    }

    public HeavyVehicleFlagEncoder( int speedBits, double speedFactor, int maxTurnCosts )
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

        trackTypeSpeedMap.put("grade1", 20); // paved
        trackTypeSpeedMap.put("grade2", 15); // now unpaved - gravel mixed with ...
        trackTypeSpeedMap.put("grade3", 10); // ... hard and soft materials
        trackTypeSpeedMap.put("grade4", 5); // ... some hard or compressed materials
        trackTypeSpeedMap.put("grade5", 5); // ... no hard materials. soil/sand/grass

        badSurfaceSpeedMap.add("cobblestone");
        badSurfaceSpeedMap.add("grass_paver");
        badSurfaceSpeedMap.add("gravel");
        badSurfaceSpeedMap.add("sand");
        badSurfaceSpeedMap.add("paving_stones");
        badSurfaceSpeedMap.add("dirt");
        badSurfaceSpeedMap.add("ground");
        badSurfaceSpeedMap.add("grass");

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
        speedEncoder = new EncodedDoubleValue("Speed", shift, speedBits, speedFactor, defaultSpeedMap.get("secondary"), defaultSpeedMap.get("motorway"));
        shift += speedEncoder.getBits();

        preferWayEncoder = new EncodedValue("PreferWay", shift, 3, 1, 0, 7);
		shift += preferWayEncoder.getBits();
		
		//vehicleDirectionEncoder = new HeavyVehicleEncodedValue(shift);
		//shift += vehicleDirectionEncoder.getBits();
		
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
	protected double getMaxSpeed( ReaderWay way ) // runge
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
			double defaultSpeed = defaultSpeedMap.get(way.getTag("highway"));
			if (defaultSpeed < maxSpeed)
				maxSpeed = defaultSpeed;
		}

		return maxSpeed;
	}
	
    protected double getSpeed(ReaderWay way )
    {
        String highwayValue = way.getTag("highway");
        Integer speed = defaultSpeedMap.get(highwayValue);
        if (speed == null)
            throw new IllegalStateException(toString() + ", no speed found for:" + highwayValue);

        if (highwayValue.equals("track"))
        {
            String tt = way.getTag("tracktype");
            if (!Helper.isEmpty(tt))
            {
                Integer tInt = trackTypeSpeedMap.get(tt);
                if (tInt != null)
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
        
        if ("track".equals(highwayValue))
        {
            String tt = way.getTag("tracktype");
            if (tt != null && !tt.equals("grade1")) // TODO allow higher grade values for forestry and agriculture
            	return 0;
            	
            if (tt != null && !trackTypeSpeedMap.containsKey(tt))
                return 0;
        }

        if (!defaultSpeedMap.containsKey(highwayValue))
            return 0;

        if (way.hasTag("impassable", "yes") || way.hasTag("status", "impassable"))
            return 0;

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

        long encoded = 0;
        if (!isFerry(allowed))
        {
        	encoded = setLong(encoded, PriorityWeighting.KEY, handlePriority(way));
        	
            // get assumed speed from highway type
            double speed = getSpeed(way);
            
            // runge
            // every road type except motorways and trunks might have traffic lights, so we make an actual speed a bit lower 
            /*String highway = way.getTag("highway");
            if ("motorway".equals(highway) || "motorway_link".equals(highway) || "trunk".equals(highway) || "trunk_link".equals(highway))
				speed = applyMaxSpeed(way, speed, false);
            else
            	speed = applyMaxSpeed(way, speed, true);
            */
            speed = applyMaxSpeed(way, speed);
            // limit speed to max 30 km/h if bad surface
            if (speed > 30 && way.hasTag("surface", badSurfaceSpeedMap))
                speed = 30;
            
			encoded = handleSpeed(way, speed, encoded);

        } else
        {
        	double ferrySpeed = getFerrySpeed(way, defaultSpeedMap.get("living_street"), defaultSpeedMap.get("service"), defaultSpeedMap.get("residential"));
        	encoded = setSpeed(encoded, ferrySpeed);
        	encoded |= directionBitMask;
        }

        return encoded;
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
    
    private long handleSpeed(ReaderWay way, double speed, long encoded) 
    {
    	encoded = setSpeed(encoded, speed);

        boolean isRoundabout = way.hasTag("junction", "roundabout");
        if (isRoundabout)
            encoded = setBool(encoded, K_ROUNDABOUT, true);

        if (way.hasTag("oneway", oneways) || isRoundabout)
        {
            if (way.hasTag("oneway", "-1"))
                encoded |= backwardBit;
            else
                encoded |= forwardBit;
        } else
        {
        	/*
        	for(Entry<String, Object> entry : way.getTags().entrySet())
        	{
        		if (backwardKeys.contains(entry.getKey()))
        		{
        			//if (noValues.contains(entry.getValue()))
        			//	encoded = setVehicleDirection(entry.getKey(), encoded, FlagEncoder.K_BACKWARD); // 1 is backward
        			//else if (yesValues.contains(entry.getValue()))
        			//	encoded = setVehicleDirection(entry.getKey(), encoded, 1);
        		}
        		else if (forwardKeys.contains(entry.getKey()))
        		{
        			//if (noValues.contains(entry.getValue()))
        			//	encoded = setVehicleDirection(entry.getKey(), encoded, FlagEncoder.K_FORWARD);
        			//else if (yesValues.contains(entry.getValue()))
        			//	encoded = setVehicleDirection(entry.getKey(), encoded, 1);
        		}
        	}*/
        		
       		encoded |= directionBitMask;
        }
        
        return encoded;
    }
    
    private long setVehicleDirection(String hvKey, long flags, long direction)
    {
    	if (vehicleDirectionEncoder != null)
    	{
    		int hvType = HeavyVehicleAttributes.getFromString(hvKey.split(":")[0]);
    	
    		long res = vehicleDirectionEncoder.setValue(hvType, flags, direction);
    	
    		long v = vehicleDirectionEncoder.getValue(hvType, res, direction);
    	
    		return res;
    	}
    	else
    		return flags;
    }
    
    public boolean isBool(int vehicleType, long flags, int direction)
    {
    	if (vehicleDirectionEncoder != null)
    	{
    		long value = vehicleDirectionEncoder.getValue(vehicleType, flags, direction);
    	
    		return value == 0;
    	}
    	else
    		return false;
    }
    
    @Override
    public long reverseFlags( long flags )
    {
        // swap access
        flags = super.reverseFlags(flags);

        if (vehicleDirectionEncoder != null)
        	return vehicleDirectionEncoder.reverseFlags(flags);
        else
        	return flags;
    }
    
    public void setVehicleType(int vt)
	{
    	if (vehicleDirectionEncoder != null)
    		vehicleDirectionEncoder.setVehicleType(vt);
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

    public boolean supports(Class<?> feature) {
		if (super.supports(feature))
			return true;
		return PriorityWeighting.class.isAssignableFrom(feature);
	}
    
    @Override
    public String toString()
    {
        return "heavyvehicle";
    }

	@Override
	public int getVersion() {
		// TODO Auto-generated method stub
		return 1;
	}
}
