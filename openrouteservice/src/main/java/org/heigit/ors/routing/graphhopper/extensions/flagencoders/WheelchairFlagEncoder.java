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

import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.PMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.graphhopper.routing.util.PriorityCode.*;

public class WheelchairFlagEncoder extends FootFlagEncoder {
    public static final int MEAN_SPEED = 4;
    public static final String KEY_WHEELCHAIR = "wheelchair";
    public static final String KEY_FOOTWAY = "footway";
    public static final String KEY_PEDESTRIAN = "pedestrian";
    public static final String KEY_LIVING_STREET = "living_street";
    public static final String KEY_BRIDLEWAY = "bridleway";
    public static final String KEY_SIDEWALK = "sidewalk";
    public static final String KEY_HIGHWAY = "highway";
    public static final String KEY_ROUTE = "route";
    public static final String KEY_BICYCLE = "bicycle";
    public static final String KEY_DESIGNATED = "designated";
    public static final String KEY_OFFICIAL = "official";
    public static final String KEY_CROSSING = "crossing";

    protected Set<String> acceptedPublicTransport = new HashSet<>(5);
    
    /**
     * Fully suitable for wheelchair users
     */
    private final Set<String> fullyWheelchairAccessibleHighways = new HashSet<>();
    
    /**
     * Suitable for wheelchair users. However highways falling into this category that explicitly indicate a sidewalk is available will be prefered 
     */
    private final Set<String> assumedWheelchairAccessibleHighways = new HashSet<>();
    
    /**
     * Highways that fall into this category will only be considered if further information about surface/smoothness is available
     */
    private final Set<String> limitedWheelchairAccessibleHighways = new HashSet<>();
    
    /**
     * Highways that fall into this category will only be considered if further information about surface/smoothness is available
     */
    private final Set<String> restrictedWheelchairHighways = new HashSet<>();
    
    /**
     * Highways that fall into this category cannot be accessed by Wheelchair users (e.g. steps)
     */
    private final Set<String> nonWheelchairAccessibleHighways = new HashSet<>();
    
    /**
     * Barriers (nodes) that are not accessible. Routes that would these nodes are not possible.
     */
    private Set<String> inaccessibleBarriers = new HashSet<>(5);
    
    private final Set<String> accessibilityRelatedAttributes = new HashSet<>();

  	public WheelchairFlagEncoder(PMap configuration) {
		 this(configuration.getInt("speed_bits", 4),
			  configuration.getDouble("speed_factor", 1));
    }

    /**
     * Should be only instantiated via EncodingManager
     */
    public WheelchairFlagEncoder() {
        this(4, 1);
    }

    public WheelchairFlagEncoder( int speedBits, double speedFactor ) {
        super(speedBits, speedFactor);
        // test for the following restriction keys
        restrictions.add(KEY_WHEELCHAIR);

        intendedValues.add("limited");


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
        fullyWheelchairAccessibleHighways.add(KEY_FOOTWAY); // fußweg, separat modelliert
        fullyWheelchairAccessibleHighways.add(KEY_PEDESTRIAN); // fußgängerzone
        fullyWheelchairAccessibleHighways.add(KEY_LIVING_STREET); //spielstraße
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
        restrictedWheelchairHighways.add(KEY_BRIDLEWAY); // disallowed in some countries - needs to be doublechecked with foot=yes
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
        
        init();
    }

    @Override
    public double getMeanSpeed() {
        return MEAN_SPEED;
    }
    
    @Override
    public int defineNodeBits(int index, int shift) {
        shift = super.defineNodeBits(index, shift);

        return shift;
    }


    /**
     * Some ways are okay but not separate for pedestrians.
     * <p/>
     * @param way
     */
    @Override
    public EncodingManager.Access getAccess(ReaderWay way ) {
    	// check access restrictions
        if (way.hasTag(restrictions, restrictedValues) && !(way.hasTag(restrictions, intendedValues) || way.hasTag(KEY_SIDEWALK, usableSidewalkValues)))
            return EncodingManager.Access.CAN_SKIP;
        
    	String highwayValue = way.getTag(KEY_HIGHWAY);
        if (highwayValue == null) {
        	
        	// ferries and shuttle_trains
            if (way.hasTag(KEY_ROUTE, ferries)) {
            	// check whether information on wheelchair accessbility is available
            	if (way.hasTag(KEY_WHEELCHAIR)) {
            		// wheelchair=yes, designated, official, permissive, limited
                	if (way.hasTag(KEY_WHEELCHAIR, intendedValues)) {
                		return EncodingManager.Access.FERRY;
                	}
                	// wheelchair=no, restricted, private
                	if (way.hasTag(KEY_WHEELCHAIR, restrictedValues)) {
                		return EncodingManager.Access.CAN_SKIP;
                	}
                }
            	if (way.hasTag("foot")) {
            		// foot=yes, designated, official, permissive, limited
                	if (way.hasTag("foot", intendedValues)) {
                        return EncodingManager.Access.FERRY;
                	}
                	// foot=no, restricted, private
                	if (way.hasTag("foot", restrictedValues)) {
                		return EncodingManager.Access.CAN_SKIP;
                	}
            	}
            	return EncodingManager.Access.WAY;
            }
            
            // public transport in general
            // railways (platform, station)
            if (way.hasTag("public_transport", acceptedPublicTransport) || way.hasTag("railway", acceptedPublicTransport)) {
            	// check whether information on wheelchair accessbility is available
            	if (way.hasTag(KEY_WHEELCHAIR)) {
            		// wheelchair=yes, designated, official, permissive, limited
                	if (way.hasTag(KEY_WHEELCHAIR, intendedValues)) {
                		return EncodingManager.Access.WAY;
                	}
                	// wheelchair=no, restricted, private
                	if (way.hasTag(KEY_WHEELCHAIR, restrictedValues)) {
                		return EncodingManager.Access.CAN_SKIP;
                	}
                }
            	if (way.hasTag("foot")) {
            		// foot=yes, designated, official, permissive, limited
                	if (way.hasTag("foot", intendedValues)) {
                		return EncodingManager.Access.WAY;
                	}
                	// foot=no, restricted, private
                	if (way.hasTag("foot", restrictedValues)) {
                		return EncodingManager.Access.CAN_SKIP;
                	}
            	}
                return EncodingManager.Access.WAY;
            }
            // no highway, no ferry, no railway? --> do not accept way
            return EncodingManager.Access.CAN_SKIP;
        }
        // highway != null
        else {
        	// wheelchair=yes, designated, official, permissive, limited
        	if (way.hasTag(KEY_WHEELCHAIR, intendedValues)) {
        		return EncodingManager.Access.WAY;
        	}
        	// wheelchair=no, restricted, private
        	if (way.hasTag(KEY_WHEELCHAIR, restrictedValues)) {
        		return EncodingManager.Access.CAN_SKIP;
        	}
        	
        	// do not include nonWheelchairAccessibleHighways
            if (nonWheelchairAccessibleHighways.contains(highwayValue)) {
            	// check for wheelchair accessibility
            	return EncodingManager.Access.CAN_SKIP;
            }
        	
        	// foot=yes, designated, official, permissive, limited
        	if (way.hasTag("foot", intendedValues)) {
        		return EncodingManager.Access.WAY;
        	}
        	
        	// foot=no, restricted, private
        	if (way.hasTag("foot", restrictedValues)) {
        		return EncodingManager.Access.CAN_SKIP;
        	}
        	
            // http://wiki.openstreetmap.org/wiki/DE:Key:sac_scale
            String sacScale = way.getTag("sac_scale");
            if (sacScale != null) {
            	// even "hiking" is probably not possible for wheelchair user 
                return EncodingManager.Access.CAN_SKIP;
            }

            if (way.hasTag(KEY_SIDEWALK, usableSidewalkValues)) {
            	return EncodingManager.Access.WAY;
            }
            
            // Runge
            if (way.hasTag(KEY_SIDEWALK, noSidewalkValues) && assumedWheelchairAccessibleHighways.contains(highwayValue))
           		return EncodingManager.Access.CAN_SKIP;

            // explicit motorroads are not usable
            if (way.hasTag("motorroad", "yes"))
                return EncodingManager.Access.CAN_SKIP;

            // do not get our feet wet, "yes" is already included above
            if (isBlockFords() && (way.hasTag(KEY_HIGHWAY, "ford") || way.hasTag("ford")))
                return EncodingManager.Access.CAN_SKIP;
            
            boolean bicycleOrHorseOnlyWay = (way.hasTag(KEY_BICYCLE, KEY_DESIGNATED) || way.hasTag(KEY_BICYCLE, KEY_OFFICIAL) || way.hasTag("horse", KEY_DESIGNATED) || way.hasTag("horse", KEY_OFFICIAL)) && !way.hasTag(restrictions, intendedValues);
            if (bicycleOrHorseOnlyWay)
                return EncodingManager.Access.CAN_SKIP;
            
            if (restrictedWheelchairHighways.contains(highwayValue)) {
            	// In some countries bridleways cannot be travelled by anything other than a horse, so we should check if they have been explicitly allowed for foot or pedestrian
                if (highwayValue.equals(KEY_BRIDLEWAY) && !(intendedValues.contains(way.getTag("foot", "no")) || intendedValues.contains(way.getTag(KEY_WHEELCHAIR, "no")))) {
                    return EncodingManager.Access.CAN_SKIP;
                }
           		return EncodingManager.Access.WAY;
            }
            
            if (fullyWheelchairAccessibleHighways.contains(highwayValue) || assumedWheelchairAccessibleHighways.contains(highwayValue) || limitedWheelchairAccessibleHighways.contains(highwayValue)) {
            	// check whether information on wheelchair accessbility is available
            	return EncodingManager.Access.WAY;
            }
            
            // anything else
            return EncodingManager.Access.CAN_SKIP;
        }
    }

    @Override
    public long handleRelationTags(long oldRelationFlags, ReaderRelation relation) {
        int code = 0;
        if (relation.hasTag(KEY_ROUTE, "hiking") || relation.hasTag(KEY_ROUTE, "foot")) {
            Integer val = hikingNetworkToCode.get(relation.getTag("network"));
            if (val != null)
                code = val;
        } 
        else if (relation.hasTag(KEY_ROUTE, "ferry")) {
            code = PriorityCode.AVOID_IF_POSSIBLE.getValue();
        }

        int oldCode = (int) relationCodeEncoder.getValue(oldRelationFlags);
        if (oldCode < code)
            return relationCodeEncoder.setValue(0, code);
        return oldRelationFlags;
    }

    //public long handleWayTags(ReaderWay way, long allowed, long relationFlags )

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, EncodingManager.Access access, long relationFlags) {
        if (access.canSkip())
            return edgeFlags;

        if (!access.isFerry()) {
        	// TODO: Depending on availability of sidewalk, surface, smoothness, tracktype and incline MEAN_SPEED or SLOW_SPEED should be encoded
        	// TODO: Maybe also implement AvoidFeaturesWeighting for Wheelchairs
        	
        	// *****************************************  Runge
        	// This is a trick, where we try to underrate the speed for highways that do not have tagged sidewalks.
        	// TODO: this actually affects travel time estimation (might be a good or negative side effect depending on context)
        	double speed = MEAN_SPEED;
        	if (way.hasTag(KEY_HIGHWAY))
        	{
        		
        		String highway = way.getTag(KEY_HIGHWAY);
        		if (assumedWheelchairAccessibleHighways.contains(highway) && !way.hasTag(KEY_SIDEWALK, usableSidewalkValues)) {
                    speed *= 0.8d;
        		}
        		if (fullyWheelchairAccessibleHighways.contains(highway)) {
        			if (highway.equalsIgnoreCase(KEY_FOOTWAY) || highway.equalsIgnoreCase(KEY_PEDESTRIAN) || highway.equalsIgnoreCase(KEY_LIVING_STREET)) {
        				speed *= 1.25d;
        				if (way.hasTag(KEY_FOOTWAY, KEY_CROSSING) || way.hasTag(KEY_HIGHWAY, KEY_CROSSING)) {
        					speed *= 2d; // should not exceed 10 in total due to encoding restrictions
        				}
        			}
        			// residential, unclassified
        			else if (!way.hasTag(KEY_SIDEWALK, usableSidewalkValues)) {
        				speed *= 0.9d;
        			}
        		}
        		if (restrictedWheelchairHighways.contains(highway) && (way.hasTag("foot", intendedValues) || way.hasTag(KEY_WHEELCHAIR, intendedValues))) {
        			speed *= 1.25d;
    				if (way.hasTag("cycleway", KEY_CROSSING) || way.hasTag(KEY_BRIDLEWAY, KEY_CROSSING) || way.hasTag(KEY_HIGHWAY, KEY_CROSSING)) {
    					speed *= 2d; // should not exceed 10 in total due to encoding restrictions
    				}
        		}
        		
        	}
        	// *****************************************
        	
            speedEncoder.setDecimal(false, edgeFlags, speed);

            accessEnc.setBool(false, edgeFlags, true);
            accessEnc.setBool(true, edgeFlags, true);
            
            int priorityFromRelation = 0;
            if (relationFlags != 0)
                priorityFromRelation = (int) relationCodeEncoder.getValue(relationFlags);

            priorityWayEncoder.setDecimal(false, edgeFlags, PriorityCode.getFactor(handlePriority(way, priorityFromRelation)));
        } 
        else {
            double ferrySpeed = getFerrySpeed(way);
            setSpeed(false, edgeFlags, ferrySpeed);
            accessEnc.setBool(false, edgeFlags, true);
            accessEnc.setBool(true, edgeFlags, true);
        }

        return edgeFlags;
    }

    /**
     * Parse tags on nodes. Node tags can add to speed (like traffic_signals) where the value is
     * strict negative or block access (like a barrier), then the value is strict positive. This
     * method is called in the second parsing step.
     */
    @Override
    public long handleNodeTags(ReaderNode node) {
        long encoded = super.handleNodeTags(node);
        // We want to be more strict with fords, as only if it is declared as wheelchair accessible do we want to cross it
        if (isBlockFords() && (node.hasTag(KEY_HIGHWAY, "ford") || node.hasTag("ford")) && !node.hasTag(KEY_WHEELCHAIR, intendedValues)) {
            encoded = getEncoderBit();
        }
        return encoded;
    }

    @Override
    protected int handlePriority(ReaderWay way, int priorityFromRelation) {
        TreeMap<Double, Integer> weightToPrioMap = new TreeMap<>();
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
    public void collect(ReaderWay way, Map<Double, Integer> weightToPrioMap) {
    	int positiveFeatures = 0;
    	int negativeFeatures = 0;
    	
    	// http://wiki.openstreetmap.org/wiki/DE:Key:traffic_calming
        String highwayValue = way.getTag(KEY_HIGHWAY);
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
        
        if (way.hasTag(KEY_BICYCLE, KEY_OFFICIAL)) {
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
       		if (highwayValue.equalsIgnoreCase(KEY_FOOTWAY) || highwayValue.equalsIgnoreCase(KEY_PEDESTRIAN) || highwayValue.equalsIgnoreCase(KEY_LIVING_STREET)) {
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
        	if (way.hasTag(KEY_SIDEWALK, usableSidewalkValues)) {
            	positiveFeatures+=5;
            }
        	// key=foot
        	else if (way.hasTag("foot", KEY_DESIGNATED)) {
    			positiveFeatures += 5;
    		}
    		else if (way.hasTag("foot", intendedValues) || way.hasTag(KEY_BICYCLE, KEY_DESIGNATED)) {
    			positiveFeatures += 2;
    		}
        }
        
        
        int sum = positiveFeatures - negativeFeatures;
        
        if (sum <= -6) weightToPrioMap.put(2d, AVOID_AT_ALL_COSTS.getValue());
        else if (sum >= -5 && sum <= -3) weightToPrioMap.put(2d, REACH_DEST.getValue());
        else if (sum >= -2 && sum <= -1) weightToPrioMap.put(2d, AVOID_IF_POSSIBLE.getValue());
        else if (sum == 0) weightToPrioMap.put(2d, UNCHANGED.getValue());
        else if (sum >= 1 && sum <= 2) weightToPrioMap.put(2d, PREFER.getValue());
        else if (sum >= 3 && sum <= 5) weightToPrioMap.put(2d, VERY_NICE.getValue());
        else if (sum >= 6) weightToPrioMap.put(2d, BEST.getValue());
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final WheelchairFlagEncoder other = (WheelchairFlagEncoder) obj;
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return ("WheelchairFlagEncoder" + toString()).hashCode();
    }
}
