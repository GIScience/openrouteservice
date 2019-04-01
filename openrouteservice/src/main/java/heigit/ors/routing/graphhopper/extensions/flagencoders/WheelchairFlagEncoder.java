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

import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.AbstractFlagEncoder;
import com.graphhopper.routing.util.EncodedDoubleValue;
import com.graphhopper.routing.util.EncodedValue;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

import java.util.*;

import static com.graphhopper.routing.util.PriorityCode.*;

public class WheelchairFlagEncoder extends AbstractFlagEncoder 
{
	static final int SLOW_SPEED = 2;
    public static final int MEAN_SPEED = 4;
    static final int FERRY_SPEED = 10;
    static final int MAX_SPEED = 15;
    
    private EncodedValue priorityWayEncoder;
    private EncodedValue relationCodeEncoder;

    private final Set<String> usableSidewalkValues = new HashSet<String>();
    private final Set<String> noSidewalkValues = new HashSet<String>();
    // convert network tag of hiking routes into a way route code
    private final Map<String, Integer> hikingNetworkToCode = new HashMap<String, Integer>();
    
    protected HashSet<String> acceptedPublicTransport = new HashSet<String>(5);
    
    /**
     * Fully suitable for wheelchair users
     */
    private final Set<String> fullyWheelchairAccessibleHighways = new HashSet<String>();
    
    /**
     * Suitable for wheelchair users. However highways falling into this category that explicitly indicate a sidewalk is available will be prefered 
     */
    private final Set<String> assumedWheelchairAccessibleHighways = new HashSet<String>();
    
    /**
     * Highways that fall into this category will only be considered if further information about surface/smoothness is available
     */
    private final Set<String> limitedWheelchairAccessibleHighways = new HashSet<String>();
    
    /**
     * Highways that fall into this category will only be considered if further information about surface/smoothness is available
     */
    private final Set<String> restrictedWheelchairHighways = new HashSet<String>();
    
    /**
     * Highways that fall into this category cannot be accessed by Wheelchair users (e.g. steps)
     */
    private final Set<String> nonWheelchairAccessibleHighways = new HashSet<String>();
    
    /**
     * Barriers (nodes) that are not accessible. Routes that would these nodes are not possible.
     */
    private HashSet<String> inaccessibleBarriers = new HashSet<String>(5);
    
    private final Set<String> accessibilityRelatedAttributes = new HashSet<String>();

  	public WheelchairFlagEncoder(PMap configuration)
    {
		 this(configuration.getInt("speed_bits", 4),
			  configuration.getDouble("speed_factor", 1));
    }

    /**
     * Should be only instantiated via EncodingManager
     */
    public WheelchairFlagEncoder()
    {
        this(4, 1);
    }

    public WheelchairFlagEncoder( int speedBits, double speedFactor )
    {
        super(speedBits, speedFactor, 0);
        // test for the following restriction keys
        restrictions.addAll(Arrays.asList("foot", "access", "wheelchair"));
        
        // for nodes: these values make the node impassable for any value of restrictions
        // for ways: these values make the way impassable for any value of restrictions
        restrictedValues.add("private");
        restrictedValues.add("no");
        restrictedValues.add("restricted");

        intendedValues.add("yes");
        intendedValues.add("designated");
        intendedValues.add("official");
        intendedValues.add("permissive");
        // TODO: include limited here or not? maybe make this is an parameter, selectable via client UI?
        intendedValues.add("limited");

        usableSidewalkValues.add("yes");
        usableSidewalkValues.add("both");
        usableSidewalkValues.add("left");
        usableSidewalkValues.add("right");
        
        noSidewalkValues.add("no");
        noSidewalkValues.add("none");
        noSidewalkValues.add("separate");
        noSidewalkValues.add("seperate");
        noSidewalkValues.add("detached");
        

        // http://wiki.openstreetmap.org/wiki/Key:barrier
        // http://taginfo.openstreetmap.org/keys/?key=barrier#values
        absoluteBarriers.add("fence");
        absoluteBarriers.add("wall");
        absoluteBarriers.add("hedge");
        absoluteBarriers.add("retaining_wall");
        absoluteBarriers.add("city_wall");
        absoluteBarriers.add("ditch");
        absoluteBarriers.add("hedge_bank");
        absoluteBarriers.add("guard_rail");
        absoluteBarriers.add("wire_fence");
        absoluteBarriers.add("embankment");

        // specify whether potential barriers block a route if no further information is available
        setBlockByDefault(false);
        
        // http://wiki.openstreetmap.org/wiki/Key:barrier
        // http://taginfo.openstreetmap.org/keys/?key=barrier#values
        // potential barriers do not block, if no further information is available
        potentialBarriers.add("gate");
        potentialBarriers.add("bollard");
        potentialBarriers.add("lift_gate");
        potentialBarriers.add("cycle_barrier");
        potentialBarriers.add("entrance");
        potentialBarriers.add("cattle_grid");
        potentialBarriers.add("swing_gate");
        potentialBarriers.add("chain");
        potentialBarriers.add("bump_gate");
        // potentialBarriers.add("kerb");
        
        // add these to absolute barriers
        inaccessibleBarriers.add("stile");
        inaccessibleBarriers.add("block");
        inaccessibleBarriers.add("kissing_gate");
        inaccessibleBarriers.add("turnstile");
        inaccessibleBarriers.add("hampshire_gate");
        
        acceptedPublicTransport.add("platform");
        // acceptedPublicTransport.add("halt"); --> usually describes a building, not a platform
        // acceptedPublicTransport.add("station"); --> usually describes a building, not a platform
        // acceptedPublicTransport.add("subway_entrance");  --> usually describes a sub entrance (building), not a platform
        // acceptedPublicTransport.add("tram_stop"); --> usually describes the stop itself, not the platform
        
        // include funicular, rail, light_rail, subway, narrow_gauge, aerialway=cablecar (tram is already included via AbstractFlagEncoder) with high costs?
        // --> this would be multi-modal routing, which is currently discouraged for ORS
        
        // fully wheelchair accessible, needs double check with tracktype, surface, smoothness
        fullyWheelchairAccessibleHighways.add("footway"); // fußweg, separat modelliert
        fullyWheelchairAccessibleHighways.add("pedestrian"); // fußgängerzone
        fullyWheelchairAccessibleHighways.add("living_street"); //spielstraße
        fullyWheelchairAccessibleHighways.add("residential"); // Straße im Wohngebiet
        fullyWheelchairAccessibleHighways.add("unclassified"); // unklassifizierter Fahrweg, meistens schmal
        fullyWheelchairAccessibleHighways.add("service"); // Zufahrtsweg
        
        assumedWheelchairAccessibleHighways.add("trunk"); // Schnellstraße 
        assumedWheelchairAccessibleHighways.add("trunk_link"); // Schnellstraßenabfahrt
        assumedWheelchairAccessibleHighways.add("primary"); // Bundesstraße
        assumedWheelchairAccessibleHighways.add("primary_link"); //Bundessstraßenabfahrt
        assumedWheelchairAccessibleHighways.add("secondary"); // Staatsstraße
        assumedWheelchairAccessibleHighways.add("secondary_link"); // Staatsstraßenabfahrt
        assumedWheelchairAccessibleHighways.add("tertiary"); // Kreisstraße
        assumedWheelchairAccessibleHighways.add("tertiary_link"); // Kreisstraßenabfahrt
        assumedWheelchairAccessibleHighways.add("road"); // neue Straße, Klassifizierung bisher unklar
        
        // potentially not suitable for wheelchair users
        limitedWheelchairAccessibleHighways.add("path"); // Wanderweg
        limitedWheelchairAccessibleHighways.add("track"); // Feldweg  
        
        // potentially not allowed for wheelchair users
        restrictedWheelchairHighways.add("bridleway"); // disallowed in some countries - needs to be doublechecked with foot=yes
        restrictedWheelchairHighways.add("cycleway"); // disallowed in some countries - needs to be doublechecked with foot=yes or wheelchair=yes
        
        // highways that are not suitable for wheelchair users
        nonWheelchairAccessibleHighways.add("steps"); // Treppen
        
        // attributes to be checked for limited wheelchair accessible highways
        accessibilityRelatedAttributes.add("surface");
        accessibilityRelatedAttributes.add("smoothness");
        accessibilityRelatedAttributes.add("tracktype");
        accessibilityRelatedAttributes.add("incline");
        accessibilityRelatedAttributes.add("sloped_curb");
        accessibilityRelatedAttributes.add("sloped_kerb");

        // prefer international, national, regional or local hiking routes
        hikingNetworkToCode.put("iwn", BEST.getValue());
        hikingNetworkToCode.put("nwn", BEST.getValue());
        hikingNetworkToCode.put("rwn", VERY_NICE.getValue());
        hikingNetworkToCode.put("lwn", VERY_NICE.getValue());
        
        init();
    }
    
    public double getDefaultMaxSpeed()
	{
		return 4;
	}

    @Override
    public int defineWayBits( int index, int shift )
    {
        // first 3 bits are reserved for route handling in superclass
        shift = super.defineWayBits(index, shift);
        // larger value required - ferries are faster than pedestrians (4 bits)
        speedEncoder = new EncodedDoubleValue("Speed", shift, speedBits, speedFactor, MEAN_SPEED, MAX_SPEED);
        shift += speedEncoder.getBits();

        priorityWayEncoder = new EncodedValue("PreferWay", shift, 3, 1, 0, 7);
        shift += priorityWayEncoder.getBits();

        return shift;
    }

    @Override
    public int defineNodeBits(int index, int shift) {
        shift = super.defineNodeBits(index, shift);

        return shift;
    }
    
    
    @Override
    public double getMaxSpeed() {
        return speedEncoder.getMaxValue();
    }
    
    @Override
    public double getSpeed( long flags )
    {
        double speedVal = speedEncoder.getDoubleValue(flags);
        if (speedVal < 0)
            throw new IllegalStateException("Speed was negative!? " + speedVal);

        return speedVal;
    }
    
    //@Override
    public long adaptSpeed(long flags, double factor)
    {
    	
    	double speedValue = getSpeed(flags);
    	double adaptedSpeed = speedValue * factor;
    	if (adaptedSpeed > MAX_SPEED) adaptedSpeed = MAX_SPEED;
    	flags = speedEncoder.setDoubleValue(flags, 0);
    	flags = speedEncoder.setDoubleValue(flags, adaptedSpeed);
    	
        return flags;
    }
    
    //@Override
    public long setSidewalkSpeed(long flags)
    {
    	// return flags;
    	return adaptSpeed(flags, 1.25d);
    	// return adaptSpeed(flags, 1d);
    }
    
    //@Override
    public long setNonSidewalkSpeed(long flags)
    {
    	// return flags;
    	return adaptSpeed(flags, 0.5d);
    	// return adaptSpeed(flags, 1d);
    }

    @Override
    public int defineRelationBits( int index, int shift )
    {
        relationCodeEncoder = new EncodedValue("RelationCode", shift, 3, 1, 0, 7);
        return shift + relationCodeEncoder.getBits();
    }

    /**
     * Wheelchair flag encoder does not provide any turn cost / restrictions
     */
    @Override
    public int defineTurnBits( int index, int shift )
    {
        return shift;
    }

    /**
     * Wheelchair flag encoder does not provide any turn cost / restrictions
     * <p>
     * @return <code>false</code>
     */
    @Override
    public boolean isTurnRestricted( long flag )
    {
        return false;
    }
    
    /**
     * Foot flag encoder does not provide any turn cost / restrictions
     * <p>
     * @return 0
     */
    @Override
    public double getTurnCost( long flag )
    {
        return 0;
    }

    @Override
    public long getTurnFlags( boolean restricted, double costs )
    {
        return 0;
    }

    /**
     * Some ways are okay but not separate for pedestrians.
     * <p/>
     * @param way
     */
    @Override
    public long acceptWay(ReaderWay way )
    {
    	// check access restrictions
        if (way.hasTag(restrictions, restrictedValues) && !(way.hasTag(restrictions, intendedValues) || way.hasTag("sidewalk", usableSidewalkValues)))
            return 0;
        
    	String highwayValue = way.getTag("highway");
        if (highwayValue == null) {
        	
        	// ferries and shuttle_trains
            if (way.hasTag("route", ferries)) {
            	// check whether information on wheelchair accessbility is available
            	if (way.hasTag("wheelchair")) {
            		// wheelchair=yes, designated, official, permissive, limited
                	if (way.hasTag("wheelchair", intendedValues)) {
                		return acceptBit | ferryBit;
                	}
                	// wheelchair=no, restricted, private
                	if (way.hasTag("wheelchair", restrictedValues)) {
                		return 0;
                	}
                }
            	if (way.hasTag("foot")) {
            		// foot=yes, designated, official, permissive, limited
                	if (way.hasTag("foot", intendedValues)) {
                		return acceptBit | ferryBit;
                	}
                	// foot=no, restricted, private
                	if (way.hasTag("foot", restrictedValues)) {
                		return 0;
                	}
            	}
            	return acceptBit;
            }
            
            // public transport in general
            // railways (platform, station)
            if (way.hasTag("public_transport", acceptedPublicTransport) || way.hasTag("railway", acceptedPublicTransport)) {
            	// check whether information on wheelchair accessbility is available
            	if (way.hasTag("wheelchair")) {
            		// wheelchair=yes, designated, official, permissive, limited
                	if (way.hasTag("wheelchair", intendedValues)) {
                		return acceptBit;
                	}
                	// wheelchair=no, restricted, private
                	if (way.hasTag("wheelchair", restrictedValues)) {
                		return 0;
                	}
                }
            	if (way.hasTag("foot")) {
            		// foot=yes, designated, official, permissive, limited
                	if (way.hasTag("foot", intendedValues)) {
                		return acceptBit;
                	}
                	// foot=no, restricted, private
                	if (way.hasTag("foot", restrictedValues)) {
                		return 0;
                	}
            	}
                return acceptBit;
            }
            // no highway, no ferry, no railway? --> do not accept way
            return 0;
        }
        // highway != null
        else {
        	/*
        	// we allow all tracktype values
        	if (way.hasTag("tracktype"))
        	{
        		String trackType = way.getTag("tracktype");
        		if (!("grade1".equalsIgnoreCase(trackType) || "grade2".equalsIgnoreCase(trackType)))
        				return 0;
        	}
        	*/
        	// wheelchair=yes, designated, official, permissive, limited
        	if (way.hasTag("wheelchair", intendedValues)) {
        		return acceptBit;
        	}
        	// wheelchair=no, restricted, private
        	if (way.hasTag("wheelchair", restrictedValues)) {
        		return 0;
        	}
        	
        	// do not include nonWheelchairAccessibleHighways
            if (nonWheelchairAccessibleHighways.contains(highwayValue)) {
            	// check for wheelchair accessibility
            	return 0;
            }
        	
        	// foot=yes, designated, official, permissive, limited
        	if (way.hasTag("foot", intendedValues)) {
        		return acceptBit;
        	}
        	
        	// foot=no, restricted, private
        	if (way.hasTag("foot", restrictedValues)) {
        		return 0;
        	}
        	
            // http://wiki.openstreetmap.org/wiki/DE:Key:sac_scale
            String sacScale = way.getTag("sac_scale");
            if (sacScale != null) {
            	// even "hiking" is probably not possible for wheelchair user 
                return 0;
            }

            if (way.hasTag("sidewalk", usableSidewalkValues)) {
            	return acceptBit;
            }
            
            // Runge
            if (way.hasTag("sidewalk", noSidewalkValues) && assumedWheelchairAccessibleHighways.contains(highwayValue))
           		return 0;

            // explicit motorroads are not usable
            if (way.hasTag("motorroad", "yes"))
                return 0;

            // do not get our feet wet, "yes" is already included above
            if (isBlockFords() && (way.hasTag("highway", "ford") || way.hasTag("ford")))
                return 0;
            
            boolean bicycleOrHorseOnlyWay = (way.hasTag("bicycle", "designated") || way.hasTag("bicycle", "official") || way.hasTag("horse", "designated") || way.hasTag("horse", "official")) && !way.hasTag(restrictions, intendedValues);
            if (bicycleOrHorseOnlyWay)
                return 0;
            
            if (restrictedWheelchairHighways.contains(highwayValue) && !bicycleOrHorseOnlyWay) {
            	// In some countries bridleways cannot be travelled by anything other than a horse, so we should check if they have been explicitly allowed for foot or pedestrian
                if (highwayValue.equals("bridleway") && !(intendedValues.contains(way.getTag("foot", "no")) || intendedValues.contains(way.getTag("wheelchair", "no")))) {
                    return 0;
                }
           		return acceptBit;
            }
            
            if (fullyWheelchairAccessibleHighways.contains(highwayValue) || assumedWheelchairAccessibleHighways.contains(highwayValue) || limitedWheelchairAccessibleHighways.contains(highwayValue)) {
            	// check whether information on wheelchair accessbility is available
            	return acceptBit;
            }
            
            // anything else
            return 0;
        }
    }

    @Override
    public long handleRelationTags(ReaderRelation relation, long oldRelationFlags )
    {
        int code = 0;
        if (relation.hasTag("route", "hiking") || relation.hasTag("route", "foot")) {
            Integer val = hikingNetworkToCode.get(relation.getTag("network"));
            if (val != null)
                code = val;
        } 
        else if (relation.hasTag("route", "ferry")) {
            code = PriorityCode.AVOID_IF_POSSIBLE.getValue();
        }

        int oldCode = (int) relationCodeEncoder.getValue(oldRelationFlags);
        if (oldCode < code)
            return relationCodeEncoder.setValue(0, code);
        return oldRelationFlags;
    }

    @Override
    public long handleWayTags(ReaderWay way, long allowed, long relationFlags )
    {
        if (!isAccept(allowed))
            return 0;

        long encoded = 0;
        if (!isFerry(allowed))
        {
        	// TODO: Depending on availability of sidewalk, surface, smoothness, tracktype and incline MEAN_SPEED or SLOW_SPEED should be encoded
        	// TODO: Maybe also implement AvoidFeaturesWeighting for Wheelchairs
        	
        	// *****************************************  Runge
        	// This is a trick, where we try to underrate the speed for highways that do not have tagged sidewalks.
        	// TODO: this actually affects travel time estimation (might be a good or negative side effect depending on context)
        	double speed = MEAN_SPEED;
        	if (way.hasTag("highway"))
        	{
        		
        		String highway = way.getTag("highway");
        		if (assumedWheelchairAccessibleHighways.contains(highway))
        		{
        			if (!way.hasTag("sidewalk", usableSidewalkValues)) {
        				speed *= 0.8d;
        			}
        		}
        		if (fullyWheelchairAccessibleHighways.contains(highway)) {
        			if (highway.equalsIgnoreCase("footway") || highway.equalsIgnoreCase("pedestrian") || highway.equalsIgnoreCase("living_street")) {
        				speed *= 1.25d;
        				if (way.hasTag("footway", "crossing") || way.hasTag("highway", "crossing")) {
        					speed *= 2d; // should not exceed 10 in total due to encoding restrictions
        				}
        			}
        			// residential, unclassified
        			else if (!way.hasTag("sidewalk", usableSidewalkValues)) {
        				speed *= 0.9d;
        			}
        		}
        		if (restrictedWheelchairHighways.contains(highway) && (way.hasTag("foot", intendedValues) || way.hasTag("wheelchair", intendedValues))) {
        			speed *= 1.25d;
    				if (way.hasTag("cycleway", "crossing") || way.hasTag("bridleway", "crossing") || way.hasTag("highway", "crossing")) {
    					speed *= 2d; // should not exceed 10 in total due to encoding restrictions
    				}
        		}
        		
        	}
        	// *****************************************
        	
            encoded = speedEncoder.setDoubleValue(0, speed);
     
         	// assumption: all ways are usable for wheelchair users in both direction
            encoded |= directionBitMask;
            
            int priorityFromRelation = 0;
            if (relationFlags != 0)
                priorityFromRelation = (int) relationCodeEncoder.getValue(relationFlags);

            encoded = setLong(encoded, PriorityWeighting.KEY, handlePriority(way, priorityFromRelation));
        } 
        else {
            double ferrySpeed = getFerrySpeed(way, SLOW_SPEED, MEAN_SPEED, FERRY_SPEED);
            encoded = setSpeed(encoded, ferrySpeed);
            encoded |= directionBitMask;
        }

        return encoded;
    }

    /**
     * Parse tags on nodes. Node tags can add to speed (like traffic_signals) where the value is
     * strict negative or block access (like a barrier), then the value is strict positive. This
     * method is called in the second parsing step.
     */
    @Override
    public long handleNodeTags(ReaderNode node)
    {
    	long encoded = 0;

        // absolute barriers always block
        if (node.hasTag("barrier", absoluteBarriers)) {
        	// barrier is not passable
        	// set barrier flag for incoming/outgoing edges of the node
        	encoded |= directionBitMask;
        }
        
        // movable barriers block if they are not marked as passable
        if (node.hasTag("barrier", potentialBarriers))
        {
            boolean isLocked = false;
            if (node.hasTag("locked", "yes")) {
            	isLocked = true;
            }

            for (String restriction : restrictions)
            {
            	// a node is passable if it is not explicitly locked and if it is explicitly intended to be used by this restriction
            	// a node is not set to passable if only "foot" is allowed as the potential Barrier might even then still be a barrier for wheelchair user
            	if (!isLocked && node.hasTag(restriction, intendedValues)) {
            		// barrier is passable, no barrier flag
            		encoded |= 0;
            	}
            }

            if (isBlockByDefault()) {
            	// depending on configuration barrier is (not) passable if no further information is available
            	// set barrier flag for incoming/outgoing edges of the node
            	encoded |= directionBitMask;
            }
        }

        // block fords defaults to yes
        // the following logic results in the vast majority of fords not being passable for wheelchair users
        if (isBlockFords()) {
        	if ((node.hasTag("highway", "ford") || node.hasTag("ford"))) {
        		for (String restriction : restrictions)
                {
                	// a ford is passable if it is explicitly intended to be used by this restriction
                	// a node is not set to passable if only "foot" is allowed as the ford will probably still then be a barrier for wheelchair users 
                	if (!restriction.equals("foot") && node.hasTag(restriction, intendedValues)) {
                		// ford is passable, no barrier flag
                		encoded |= 0;
                	}
                }
        		// ford is not passable
        		// set barrier flag for incoming/outgoing edges of the node
        		encoded |= directionBitMask;
        	}
        }

        /*if(node.hasTag("kerb") || node.hasTag("kerb:height")) {
            // We do not know if the kerb is a barrier as this is defined by the user
            encoded |= directionBitMask;
        }*/
      
        /*
        // https://github.com/species/osrm-wheelchair-profiles/blob/master/wheelchair-normal.lua
        // http://mm.linuxtage.at/osm/routing/wheelchair-normal/?z=19&center=47.074193%2C15.449503&loc=47.074336%2C15.449848&loc=47.074871%2C15.451916&hl=en&ly=&alt=&df=&srv=
        if (node.containsTag("kerb") || node.containsTag("curb") || node.containsTag("sloped_curb")) {
    		// kerb is not passable
    		// set barrier flag for incoming/outgoing edges of the node
    	//	encoded |= kerbBit;
    		double doubleValue = getKerbHeight(node);
    		// System.out.println("WheelchairFlagEncoder.handleNodeTags(), nodeId="+node.getId()+", kerb="+doubleValue);
    		// set value
    		//encoded |= slopedCurbEncoder.setDoubleValue(0, doubleValue);    		
    	}
        
        if (node.containsTag("crossing") || node.hasTag("highway", "crossing")) {
    		// set flag for crossings
    	//	encoded |= crossingBit;
    	}
		
*/
        // node is passable if nothing else applies
        return encoded;
    }
    

    /**
     * Second parsing step. Invoked after splitting the edges. Currently used to offer a hook to
     * calculate precise speed values based on elevation data stored in the specified edge.
     */
    @Override
    public void applyWayTags(ReaderWay way, EdgeIteratorState edge )
    {
    	/*
        PointList pl = edge.fetchWayGeometry(3);
        if (!pl.is3D())
            throw new IllegalStateException("To support speed calculation based on elevation data it is necessary to enable import of it.");

        long flags = edge.getFlags();

        if (way.hasTag("sidewalk", "both"))
        {
        	way.getNodes();
        	
        }
        */
    }
    
    

    
    
    @Override
    public double getDouble(long flags, int key)
    {
        switch (key)
        {
            case PriorityWeighting.KEY:
                double prio = priorityWayEncoder.getValue(flags);
                if (prio == 0)
                    return (double) UNCHANGED.getValue() / (double) BEST.getValue();

                return prio / (double) BEST.getValue();
            default:
                return super.getDouble(flags, key);
        }
    }

    @Override
    // currently unused
    public long getLong(long flags, int key)
    {
        switch (key)
        {
            case PriorityWeighting.KEY:
                return priorityWayEncoder.getValue(flags);
            default:
                return super.getLong(flags, key);
        }
    }

    @Override
    public long setLong(long flags, int key, long value)
    {
        switch (key)
        {
            case PriorityWeighting.KEY:
                return priorityWayEncoder.setValue(flags, value);
            default:
                return super.setLong(flags, key, value);
        }
    }
    
    protected int handlePriority(ReaderWay way, int priorityFromRelation)
    {
        TreeMap<Double, Integer> weightToPrioMap = new TreeMap<Double, Integer>();
        if (priorityFromRelation == 0)
            weightToPrioMap.put(1d, UNCHANGED.getValue());
        else
            weightToPrioMap.put(1d, priorityFromRelation);

        collect(way, weightToPrioMap);
        
        // pick priority with biggest order value
        return weightToPrioMap.lastEntry().getValue();
    }

    /**
     * @param weightToPrioMap associate a weight with every priority. This sorted map allows
     * subclasses to 'insert' more important priorities as well as overwrite determined priorities.
     */
    public void collect(ReaderWay way, TreeMap<Double, Integer> weightToPrioMap)
    { 
    	int positiveFeatures = 0;
    	int negativeFeatures = 0;
    	
    	// http://wiki.openstreetmap.org/wiki/DE:Key:traffic_calming
        String highwayValue = way.getTag("highway");
        double maxSpeed = getMaxSpeed(way);
        
        if (maxSpeed > 0) {
        	 if (maxSpeed > 50) {
             	negativeFeatures++;
             	if (maxSpeed > 60) {
             		negativeFeatures++;
             		if (maxSpeed > 80) {
             			negativeFeatures++;
             		}
             	}
             }
             
             if (maxSpeed > 0 && maxSpeed <= 20) {
             	positiveFeatures+=1;
             }
        }
        
        if (way.hasTag("tunnel", intendedValues)) {
        	negativeFeatures+=4;
        }
        
        if (way.hasTag("bicycle", "official")) {
        	negativeFeatures+=2;
        }
        
        // put penalty on these ways if no further information is available
        if (limitedWheelchairAccessibleHighways.contains(highwayValue)) {
        	boolean hasAccessibilityRelatedAttributes = false;
        	for (String key : accessibilityRelatedAttributes) {
        		hasAccessibilityRelatedAttributes |= way.hasTag(key);
			}
        	if (!hasAccessibilityRelatedAttributes) {
        		negativeFeatures+=2;
        	}
        }
        
        if (assumedWheelchairAccessibleHighways.contains(highwayValue)) {
        	if (highwayValue.equalsIgnoreCase("trunk") || highwayValue.equalsIgnoreCase("trunk_link")) {
        		negativeFeatures+=5;
        	}
        	else if (highwayValue.equalsIgnoreCase("primary") || highwayValue.equalsIgnoreCase("primary_link")) {
        		negativeFeatures+=3;
        	}
        	else { // secondary, tertiary, road, service
        		negativeFeatures+=1;
        	}
        }
        
        // do not rate foot features twice
        boolean isFootEvaluated = false;
        if (fullyWheelchairAccessibleHighways.contains(highwayValue)) {
        	// positiveFeatures++;
       		if (highwayValue.equalsIgnoreCase("footway") || highwayValue.equalsIgnoreCase("pedestrian") || highwayValue.equalsIgnoreCase("living_street")) {
        		positiveFeatures+=5;
        		isFootEvaluated = true;
        	}
        	else {
        		// residential, unclassified
        		negativeFeatures++;
        	}
        }
        
        if (!isFootEvaluated) {
        	// key=sidewalk
        	if (way.hasTag("sidewalk", usableSidewalkValues)) {
            	positiveFeatures+=5;
            }
        	// key=foot
        	else if (way.hasTag("foot", "designated")) {
    			positiveFeatures += 5;
    		}
    		else if (way.hasTag("foot", intendedValues) || way.hasTag("bicycle", "designated")) {
    			positiveFeatures += 2;
    		}
        }
        
        
        int sum = positiveFeatures - negativeFeatures;
        
       	// System.out.println("WheelchairFlagEncoder.collect(), sum="+sum+", wayId="+way.getId());
        
        if (sum <= -6) weightToPrioMap.put(2d, AVOID_AT_ALL_COSTS.getValue());
        else if (sum >= -5 && sum <= -3) weightToPrioMap.put(2d, REACH_DEST.getValue());
        else if (sum >= -2 && sum <= -1) weightToPrioMap.put(2d, AVOID_IF_POSSIBLE.getValue());
        else if (sum == 0) weightToPrioMap.put(2d, UNCHANGED.getValue());
        else if (sum >= 1 && sum <= 2) weightToPrioMap.put(2d, PREFER.getValue());
        else if (sum >= 3 && sum <= 5) weightToPrioMap.put(2d, VERY_NICE.getValue());
        else if (sum >= 6) weightToPrioMap.put(2d, BEST.getValue());
    }
    

    @Override
    public boolean supports( Class<?> feature )
    {
        if (super.supports(feature))
            return true;

        return PriorityWeighting.class.isAssignableFrom(feature);
    }

    @Override
    public String toString()
    {
        return FlagEncoderNames.WHEELCHAIR;
    }

	@Override
	public int getVersion() {
		return 2;
	}


}
