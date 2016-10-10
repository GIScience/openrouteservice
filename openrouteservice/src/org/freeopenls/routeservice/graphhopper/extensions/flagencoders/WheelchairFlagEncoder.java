/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

package org.freeopenls.routeservice.graphhopper.extensions.flagencoders;

import static com.graphhopper.routing.util.PriorityCode.AVOID_AT_ALL_COSTS;
import static com.graphhopper.routing.util.PriorityCode.AVOID_IF_POSSIBLE;
import static com.graphhopper.routing.util.PriorityCode.BEST;
import static com.graphhopper.routing.util.PriorityCode.PREFER;
import static com.graphhopper.routing.util.PriorityCode.REACH_DEST;
import static com.graphhopper.routing.util.PriorityCode.UNCHANGED;
import static com.graphhopper.routing.util.PriorityCode.VERY_NICE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.freeopenls.routeservice.graphhopper.extensions.util.WheelchairRestrictionCodes;
import org.freeopenls.routeservice.routing.RouteProfileManager;

import com.graphhopper.reader.OSMElement;
import com.graphhopper.reader.OSMNode;
import com.graphhopper.reader.OSMRelation;
import com.graphhopper.reader.OSMWay;
import com.graphhopper.routing.util.AbstractFlagEncoder;
import com.graphhopper.routing.util.EncodedDoubleValue;
import com.graphhopper.routing.util.EncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.util.PriorityWeighting;
import com.graphhopper.util.EdgeIteratorState;

public class WheelchairFlagEncoder extends AbstractFlagEncoder {
	
	public static void main(String[] args) {
		TreeMap<Double, Integer> weightToPrioMap = new TreeMap<Double, Integer>();
		weightToPrioMap.put(40d, UNCHANGED.getValue());
		weightToPrioMap.put(40d, BEST.getValue());
		weightToPrioMap.put(30d, BEST.getValue());
		// pick priority with biggest order value
        // System.out.println("WheelchairFlagEncoder.main(), weightToPrioMap.lastEntry().getValue()="+weightToPrioMap.lastEntry().getValue());
	}
	
	private final static Logger logger = Logger.getLogger(RouteProfileManager.class.getName());
	
	static final int SLOW_SPEED = 2;
    static final int MEAN_SPEED = 4;
    static final int FERRY_SPEED = 10;
    static final int MAX_SPEED = 15;
    private EncodedValue preferWayEncoder;
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

    public final static String ENCODER_NAME_PREFER_WAY = "prefer_way";
    public final static String ENCODER_NAME_SURFACE = "surface";
    public final static String ENCODER_NAME_SMOOTHNESS = "smoothness";
    public final static String ENCODER_NAME_SLOPED_CURB = "sloped_curb";
    public final static String ENCODER_NAME_TRACKTYPE = "tracktype";
    public final static String ENCODER_NAME_INCLINE = "inline";
    
    /*
    // 26 distinct values > 5 Bits
 	protected EncodedValue surfaceEncoder;
 	// 8 distinct values > 3 Bits
 	protected EncodedValue smoothnessEncoder;
 	*/
 	// maximum value 0.15 > 4 Bits
 	protected EncodedDoubleValue slopedCurbEncoder;
 	/*
 	// 5 distinct values > 3 Bits
 	protected EncodedValue tracktypeEncoder;
 	// maximum value 15% > 4 Bits
 	protected EncodedDoubleValue inclineEncoder;
 	*/
 	
 	
 	// 1 Bit
  	protected long sidewalkBit;
 	// 1 Bit
 	protected long crossingBit;
 	// 1 Bit
  	protected long kerbBit;
	
  	public WheelchairFlagEncoder( String propertiesStr )
    {
		 this((int) parseLong(propertiesStr, "speedBits", 4),
	                parseDouble(propertiesStr, "speedFactor", 1));
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

        // prefer international, national, regional or local hiking routes
        hikingNetworkToCode.put("iwn", BEST.getValue());
        hikingNetworkToCode.put("nwn", BEST.getValue());
        hikingNetworkToCode.put("rwn", VERY_NICE.getValue());
        hikingNetworkToCode.put("lwn", VERY_NICE.getValue());
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
        
        // System.out.println("WheelchairFlagEncoder.defineWayBits(), shift="+shift);

        // 8 distinct values => 3 Bits sufficient
        int preferenceBits = 3;
        preferWayEncoder = new EncodedValue(ENCODER_NAME_PREFER_WAY, shift, preferenceBits, 1d, PriorityCode.WORST.getValue(), PriorityCode.BEST.getValue());
        shift += preferWayEncoder.getBits();
        
        /*
        // 26 distinct values => 5 Bits sufficient
        int surfaceBits = 5;
     	surfaceEncoder = new EncodedValue(ENCODER_NAME_SURFACE, shift, surfaceBits, 1d, WheelchairRestrictionCodes.SURFACE_PAVED, WheelchairRestrictionCodes.SURFACE_UNPAVED_WOODCHIPS);
     	shift += surfaceBits;
     	// 8 distinct values => 3 Bits sufficient
     	int smoothnessBits = 3;
     	smoothnessEncoder = new EncodedValue(ENCODER_NAME_SMOOTHNESS, shift, smoothnessBits, 1d, WheelchairRestrictionCodes.SMOOTHNESS_EXCELLENT, WheelchairRestrictionCodes.SMOOTHNESS_IMPASSABLE);
     	shift += smoothnessBits;
     	*/
     	// maximum value = 0.15cm => 4 Bits sufficient
     	int slopedCurbBits = 4;
     	slopedCurbEncoder = new EncodedDoubleValue(ENCODER_NAME_SLOPED_CURB, shift, slopedCurbBits, 0.01d, 0, (int)WheelchairRestrictionCodes.SLOPED_CURB_MAXIMUM);
     	shift += slopedCurbBits;
     	/*
     	// 5 distinct values => 3 Bits sufficient
     	int tracktypeBits = 3;
     	tracktypeEncoder = new EncodedValue(ENCODER_NAME_TRACKTYPE, shift, tracktypeBits, 1d, WheelchairRestrictionCodes.TRACKTYPE_GRADE1, WheelchairRestrictionCodes.TRACKTYPE_GRADE5);
     	shift += tracktypeBits;
     	// maximum value 15% => 4 Bits sufficient
     	int inclineBits = 4;
     	inclineEncoder = new EncodedDoubleValue(ENCODER_NAME_INCLINE, shift, inclineBits, 1d, 0, WheelchairRestrictionCodes.INCLINE_MAXIMUM);
     	shift += inclineBits;
     	*/
        
     	// 1 Bit for sidewalks
     	sidewalkBit = 1L << shift++;
     	// 1 Bit for crossings
     	crossingBit = 1L << shift++;
     	// 1 Bit for kerbs
     	kerbBit = 1L << shift++;
     	
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
    
    @Override
    public long adaptSpeed(long flags, double factor)
    {
    	
    	double speedValue = getSpeed(flags);
    	double adaptedSpeed = speedValue * factor;
    	if (adaptedSpeed > MAX_SPEED) adaptedSpeed = MAX_SPEED;
    	flags = speedEncoder.setDoubleValue(flags, 0);
    	flags = speedEncoder.setDoubleValue(flags, adaptedSpeed);
    	
        return flags;
    }
    
    @Override
    public long setSidewalkSpeed(long flags)
    {
    	// return flags;
    	return adaptSpeed(flags, 1.25d);
    	// return adaptSpeed(flags, 1d);
    }
    
    @Override
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
    
    @Override
    public boolean isBool( long flags, int key )
    {
        switch (key)
        {
        	case K_SIDEWALK:
        		return (flags & sidewalkBit) != 0;
            case K_CROSSING:
            	// System.out.println("WheelchairFlagEncoder.isBool(), flags & crossingBit="+((flags & crossingBit)!=0));
            	// System.out.println("WheelchairFlagEncoder.isBool(), flags=      "+Long.toBinaryString(flags));
        		// System.out.println("WheelchairFlagEncoder.isBool(), crossingBit="+Long.toBinaryString(crossingBit));
            	return (flags & crossingBit) != 0;
            case K_KERB:
                return (flags & kerbBit) != 0;
            default:
                return super.isBool(flags, key);
        }
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
    public long acceptWay( OSMWay way )
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
            	// only disallow cycleways/bridleways if they are only designated to either of this mode of traveling
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
    public long handleRelationTags( OSMRelation relation, long oldRelationFlags )
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
    public long handleWayTags( OSMWay way, long allowed, long relationFlags )
    {
        if (!isAccept(allowed))
            return 0;

        long encoded;
        if (!isFerry(allowed))
        {
        	// TODO: Depending on availability of sidewalk, surface, smoothness, tracktype and incline MEAN_SPEED or SLOW_SPEED should be encoded
        	// TODO: Maybe also implement AvoidFeaturesWeighting for Wheelchairs
        	
        	// *****************************************  Runge
        	// This is a trick, where we try to underrate the speed for highways that do not have tagged sidewalks.
        	// TODO: this actually affects travel time estimation (might be a good or negative side effect depending on context)
        	double speed = MEAN_SPEED;
        	if (way.containsTag("highway"))
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
            
            /*
        	// default value for surface (paved=0)
         	encoded |= surfaceEncoder.setDefaultValue(WheelchairRestrictionCodes.SURFACE_PAVED);
         	
         	// surface value
         	// extract value for surface
         	if (way.hasTag("sidewalk")) {
         		
         	}
         	// if no separate sidewalk surface is available try to use surface from the highway
         	else {
         		if (way.hasTag("surface")) {
    				String tagValue = way.getTag("surface").toLowerCase();
    				if (WheelchairRestrictionCodes.SURFACE_MAP.containsKey(tagValue)) {
    					// set value
    					encoded |= surfaceEncoder.setValue(0, WheelchairRestrictionCodes.SURFACE_MAP.get(tagValue));
    				}
         		}
         	}
         	
         	// default value for smoothness (excellent=0)
         	encoded |= smoothnessEncoder.setDefaultValue(WheelchairRestrictionCodes.SMOOTHNESS_EXCELLENT);
         	
         	// smoothness value
         	// extract value for surface
         	if (way.hasTag("sidewalk")) {
         		
         	}
         	// if no separate sidewalk smoothness is available try to use smoothness directly from the highway
         	else {
         		if (way.hasTag("smoothness")) {
    				String tagValue = way.getTag("smoothness").toLowerCase();
    				if (WheelchairRestrictionCodes.SMOOTHNESS_MAP.containsKey(tagValue)) {
    					// set value
    					encoded |= smoothnessEncoder.setValue(0, WheelchairRestrictionCodes.SMOOTHNESS_MAP.get(tagValue));
    				}
         		}
         	}
         	*/
         	
         	// sloped_curb
			// ===========
			// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Curb_heights
			// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#B.C3.BCrgersteige
			// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#B.C3.BCrgersteige_und_Eigenschaften
			// http://wiki.openstreetmap.org/wiki/Key:sloped_curb
         	
         	// only use sloped_curb|kerb|curb values on ways that are crossing. there are cases (e.g. platform) where these tags are also used but in fact indicate wheelchair accessibility (e.g. platform=yes, kerb=raised)
         	if ((way.hasTag("sloped_curb") || way.hasTag("kerb") || way.hasTag("curb")) && (way.hasTag("footway", "crossing") || way.hasTag("cycleway", "crossing") || way.hasTag("highway", "crossing") || way.hasTag("crossing"))) {
         		double doubleValue = getKerbHeight(way);
				
				// set value
				encoded |= slopedCurbEncoder.setDoubleValue(0, doubleValue);
         	}

         	
         	/*
         	// default value for tracktype (tracktype1=0)
         	encoded |= tracktypeEncoder.setDefaultValue(WheelchairRestrictionCodes.TRACKTYPE_GRADE1);
         	
         	// tracktype value
         	// extract value for tracktype
         	// if no separate sidewalk tracktype is available try to use tracktype from the highway
     		if (way.hasTag("tracktype")) {
				String tagValue = way.getTag("tracktype").toLowerCase();
				if (WheelchairRestrictionCodes.TRACKTYPE_MAP.containsKey(tagValue)) {
					// set value
					encoded |= tracktypeEncoder.setValue(0, WheelchairRestrictionCodes.TRACKTYPE_MAP.get(tagValue));
				}
     		}
         	
         	
         	// default value for incline
         	encoded |= inclineEncoder.setDefaultValue(0);
         	
         	// incline
			// =======
			// http://wiki.openstreetmap.org/wiki/Key:incline
			// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Path_properties.2C_in_general
			// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#Weg_Eigenschaften_allgemein
     		String inclineValue = way.getTag("incline");
         	if (inclineValue != null) {
				double v = 0d;
				boolean isDegree = false;
				try {
					inclineValue = inclineValue.replace("%", "");
					inclineValue = inclineValue.replace(",", ".");
					if (inclineValue.contains("°")) {
						inclineValue = inclineValue.replace("°", "");
						isDegree = true;
					}
					// TODO: the following lines are assumptions - can they be validated?
					inclineValue = inclineValue.replace("up", "10");
					inclineValue = inclineValue.replace("down", "10");
					inclineValue = inclineValue.replace("yes", "10");
					inclineValue = inclineValue.replace("steep", "15");
					inclineValue = inclineValue.replace("no", "0");
					inclineValue = inclineValue.replace("+/-0", "0");
					v = Double.parseDouble(inclineValue);
					if (isDegree) {
						v = Math.tan(v) * 100;
					}
				}
				catch (Exception ex) {
					logger.warning("Error parsing value for Tag incline from this String: " + inclineValue);
				}
				// Fist check if the value makes sense
				// http://wiki.openstreetmap.org/wiki/DE:Key:incline
				// TODO: deal with negative incline (indicates the direction of the incline => might not be important for use wheelchair user as too much incline is an exclusion criterion in both directions?)
				if (-50 < v && v < 50) {
					// value seems to be okay
				}
				else {
					// v = Double.NaN;
					v = 15;
				}
				if (Math.abs(v) > 15) {
					v = 15;
				}
				encoded |= inclineEncoder.setDoubleValue(0, Math.abs(v));
         	}
         	*/
         	
         	// test whether way has sidewalks
         	HashSet<String> sidewalkValues = new HashSet<String>();
         	sidewalkValues.add("left");
         	sidewalkValues.add("right");
         	sidewalkValues.add("both");
         	sidewalkValues.add("yes");
         	if (way.hasTag("sidewalk", sidewalkValues)) {
         		encoded |= sidewalkBit;
         	}
         	
         	// assumption: all ways are usable for wheelchair users in both direction
            encoded |= directionBitMask;
            
            int priorityFromRelation = 0;
            if (relationFlags != 0)
                priorityFromRelation = (int) relationCodeEncoder.getValue(relationFlags);

            encoded = setLong(encoded, PriorityWeighting.KEY, handlePriority(way, priorityFromRelation));
        } 
        else {
            encoded = handleFerryTags(way, SLOW_SPEED, MEAN_SPEED, FERRY_SPEED);
            encoded |= directionBitMask;
        }

        return encoded;
    }

	private double getKerbHeight(OSMElement osmElement) {
		// http://taginfo.openstreetmap.org/keys/kerb#overview: 80% nodes, 20% ways
		// http://taginfo.openstreetmap.org/keys/kerb#values
		double doubleValue = 0d;
 		String stringValue = null;
		// http://taginfo.openstreetmap.org/keys/sloped_curb#overview: 90% nodes, 10% ways
		// http://taginfo.openstreetmap.org/keys/sloped_curb#values
		if (osmElement.hasTag("sloped_curb")) {
			stringValue = osmElement.getTag("sloped_curb").toLowerCase();
			stringValue = stringValue.replace("yes", "0.03");
			stringValue = stringValue.replace("both", "0.03");
			stringValue = stringValue.replace("no", "0.15");
			stringValue = stringValue.replace("one", "0.15");
			stringValue = stringValue.replace("at_grade", "0.0");
			stringValue = stringValue.replace("flush", "0.0");
			stringValue = stringValue.replace("low", "0.03");
		}
		
		if (osmElement.hasTag("kerb")) {
			if (osmElement.hasTag("kerb:height")) {
				stringValue = osmElement.getTag("kerb:height").toLowerCase();
			}
			else {
				stringValue = osmElement.getTag("kerb").toLowerCase();
				stringValue = stringValue.replace("lowered", "0.03");
				stringValue = stringValue.replace("raised", "0.15");
				stringValue = stringValue.replace("yes", "0.03");
				stringValue = stringValue.replace("flush", "0.0");
				stringValue = stringValue.replace("unknown", "0.03");
				stringValue = stringValue.replace("no", "0.15");
				stringValue = stringValue.replace("dropped", "0.03");
				stringValue = stringValue.replace("rolled", "0.03");
				stringValue = stringValue.replace("none", "0.15");
			}
		}
		
		// http://taginfo.openstreetmap.org/keys/curb#overview: 70% nodes, 30% ways
		// http://taginfo.openstreetmap.org/keys/curb#values
		if (osmElement.hasTag("curb")) {
			stringValue = osmElement.getTag("curb").toLowerCase();
			stringValue = stringValue.replace("lowered", "0.03");
			stringValue = stringValue.replace("regular", "0.15");
			stringValue = stringValue.replace("flush;lowered", "0.0");
			stringValue = stringValue.replace("sloped", "0.03");
			stringValue = stringValue.replace("lowered_and_sloped", "0.03");
			stringValue = stringValue.replace("flush", "0.0");
			stringValue = stringValue.replace("none", "0.15");
			stringValue = stringValue.replace("flush_and_lowered", "0.0");
		}
		
		if (stringValue != null) {
			boolean isCm = false;
			try {
				if (stringValue.contains("c")) {
					isCm = true;
				}
				doubleValue = Double.parseDouble(stringValue.replace("%", "").replace(",", ".").replace("m", "").replace("c", ""));
				if (isCm) {
					doubleValue /= 100d;
				}
			}
			catch (Exception ex) {
				logger.warning("Error parsing value for Tag kerb from this String: " + stringValue + ". Exception:" + ex.getMessage());
			}
		}

		// check if the value makes sense (i.e. maximum 0.3m/30cm)
		if (-0.15 < doubleValue && doubleValue < 0.15) {
			doubleValue = Math.abs(doubleValue);
		}
		else {
			// doubleValue = Double.NaN;
			doubleValue = 0.15;
		}
		return doubleValue;
	}

    /**
     * Parse tags on nodes. Node tags can add to speed (like traffic_signals) where the value is
     * strict negative or block access (like a barrier), then the value is strict positive. This
     * method is called in the second parsing step.
     */
    @Override
    public long handleNodeTags(OSMNode node)
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
        
        // https://github.com/species/osrm-wheelchair-profiles/blob/master/wheelchair-normal.lua
        // http://mm.linuxtage.at/osm/routing/wheelchair-normal/?z=19&center=47.074193%2C15.449503&loc=47.074336%2C15.449848&loc=47.074871%2C15.451916&hl=en&ly=&alt=&df=&srv=
        if (node.containsTag("kerb") || node.containsTag("curb") || node.containsTag("sloped_curb")) {
    		// kerb is not passable
    		// set barrier flag for incoming/outgoing edges of the node
    		encoded |= kerbBit;
    		double doubleValue = getKerbHeight(node);
    		// System.out.println("WheelchairFlagEncoder.handleNodeTags(), nodeId="+node.getId()+", kerb="+doubleValue);
    		// set value
    		encoded |= slopedCurbEncoder.setDoubleValue(0, doubleValue);    		
    	}
        
        if (node.containsTag("crossing") || node.hasTag("highway", "crossing")) {
    		// set flag for crossings
    		encoded |= crossingBit;
    		// System.out.println("WheelchairFlagEncoder.handleNodeTags(), node.getId()="+node.getId());
    		// System.out.println("WheelchairFlagEncoder.handleNodeTags(), encoded=    "+String.format("%64s", Long.toBinaryString(encoded)).replace(" ", "0"));
    		// System.out.println("WheelchairFlagEncoder.handleNodeTags(), crossingBit="+String.format("%64s", Long.toBinaryString(crossingBit)).replace(" ", "0"));
    	}
		

        // node is passable if nothing else applies
        return encoded;
    }
    

    /**
     * Second parsing step. Invoked after splitting the edges. Currently used to offer a hook to
     * calculate precise speed values based on elevation data stored in the specified edge.
     */
    @Override
    public void applyWayTags( OSMWay way, EdgeIteratorState edge )
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
    
    private EncodedDoubleValue getDoubleValueEncoder(String encoderName) throws IllegalArgumentException {
    	if (encoderName.equals(ENCODER_NAME_SLOPED_CURB)) {
    		return slopedCurbEncoder;
    	}
    	/*
    	if (encoderName.equals(ENCODER_NAME_INCLINE)) {
    		return inclineEncoder;
    	}
    	*/
    	throw new IllegalArgumentException("Unknown encoder name " + encoderName + ".");
    }
    /*
    private EncodedValue getValueEncoder(String encoderName) throws IllegalArgumentException {
    	if (encoderName.equals(ENCODER_NAME_SURFACE)) {
    		return surfaceEncoder;
    	}
    	if (encoderName.equals(ENCODER_NAME_SMOOTHNESS)) {
    		return smoothnessEncoder;
    	}
    	if (encoderName.equals(ENCODER_NAME_TRACKTYPE)) {
    		return tracktypeEncoder;
    	}
    	throw new IllegalArgumentException("Unknown encoder name " + encoderName + ".");
    }
    */
    
    /**
     * Checks whether the value encoded by <code>flags</code> and <code>encoderName</code> <= <code>value</code> 
     * 
     * @param flags
     * @param encoderName
     * @param restriction a restriction value to be checked
     * @return <code>true</code> if <code>value</code> >= encoded value, <code>false</code> otherwise
     */
    /*
    public boolean checkRestriction(long flags, String encoderName, int restriction) throws IllegalArgumentException {
    	return getValueEncoder(encoderName).getValue(flags) <= restriction;
    }
    */
    
    
    /**
     * Checks whether the value encoded by <code>flags</code> and <code>encoderName</code> <= <code>value</code> 
     * 
     * @param flags
     * @param encoderName
     * @param restriction a restriction value to be checked
     * @return <code>true</code> if <code>value</code> >= encoded value, <code>false</code> otherwise
     */
    
    public boolean checkRestriction(long flags, String encoderName, double restriction) throws IllegalArgumentException {
    	return getDoubleValueEncoder(encoderName).getDoubleValue(flags) <= restriction;
    }

    
    @Override
    public double getDouble(long flags, int key)
    {
        switch (key)
        {
            case PriorityWeighting.KEY:
                double prio = preferWayEncoder.getValue(flags);
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
                return preferWayEncoder.getValue(flags);
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
                return preferWayEncoder.setValue(flags, value);
            default:
                return super.setLong(flags, key, value);
        }
    }
    
    @Override
    public long setBool( long flags, int key, boolean value )
    {
        switch (key)
        {
        	case K_SIDEWALK:
        		return value ? flags | sidewalkBit : flags & ~sidewalkBit;
        }
        return super.setBool(flags, key, value);
    }

    protected int handlePriority(OSMWay way, int priorityFromRelation)
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
    public void collect(OSMWay way, TreeMap<Double, Integer> weightToPrioMap)
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
        		hasAccessibilityRelatedAttributes |= way.containsTag(key);
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
        return "wheelchair";
    }

	@Override
	public int getVersion() {
		return 0;
	}


}
