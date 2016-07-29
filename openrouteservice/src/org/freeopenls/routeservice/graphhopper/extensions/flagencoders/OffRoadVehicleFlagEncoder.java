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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.graphhopper.reader.OSMReader;
import com.graphhopper.reader.OSMRelation;
import com.graphhopper.reader.OSMTurnRelation;
import com.graphhopper.reader.OSMWay;
import com.graphhopper.reader.OSMTurnRelation.TurnCostTableEntry;
import com.graphhopper.routing.util.AbstractFlagEncoder;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EncodedDoubleValue;
import com.graphhopper.util.Helper;

public class OffRoadVehicleFlagEncoder extends AbstractFlagEncoder {
	protected final Map<String, Integer> trackTypeSpeedMap = new HashMap<String, Integer>();
	protected final Map<String, Integer> badSurfaceSpeedMap = new HashMap<String, Integer>();
	protected final HashSet<String> badSurfaceMap = new HashSet<String>();
	/**
	 * A map which associates string to speed. Get some impression:
	 * http://www.itoworld.com/map/124#fullscreen
	 * http://wiki.openstreetmap.org/wiki/OSM_tags_for_routing/Maxspeed
	 */
	protected final Map<String, Integer> defaultSpeedMap = new HashMap<String, Integer>();

	/**
	 * Should be only instantied via EncodingManager
	 */
	public OffRoadVehicleFlagEncoder() {
		this(5, 5, 0);
	}
	
	public OffRoadVehicleFlagEncoder( String propertiesStr )
    {
	     this((int) parseLong(propertiesStr, "speedBits", 5),
	                parseDouble(propertiesStr, "speedFactor", 5),
	                parseBoolean(propertiesStr, "turnCosts", false) ? 3 : 0);
	     
	     setBlockFords(false);
    }

	public OffRoadVehicleFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
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

		potentialBarriers.add("gate");
		potentialBarriers.add("lift_gate");
		potentialBarriers.add("kissing_gate");
		potentialBarriers.add("swing_gate");

		absoluteBarriers.add("bollard");
		absoluteBarriers.add("stile");
		absoluteBarriers.add("turnstile");
		absoluteBarriers.add("cycle_barrier");
		absoluteBarriers.add("block");

		trackTypeSpeedMap.put("grade1", 20); // paved
		trackTypeSpeedMap.put("grade2", 15); // now unpaved - gravel mixed with
												// ...
		trackTypeSpeedMap.put("grade3", 10); // ... hard and soft materials
		trackTypeSpeedMap.put("grade4", 5); // ... some hard or compressed
											// materials
		trackTypeSpeedMap.put("grade5", 5); // ... no hard materials.
											// soil/sand/grass
		trackTypeSpeedMap.put("grade6", 2);
		trackTypeSpeedMap.put("grade7", 2);
		trackTypeSpeedMap.put("grade8", 1); // madness

		badSurfaceMap.add("cobblestone");
		badSurfaceMap.add("grass_paver");
		badSurfaceMap.add("gravel");
		badSurfaceMap.add("sand");
		badSurfaceMap.add("paving_stones");
		badSurfaceMap.add("dirt");
		badSurfaceMap.add("ground");
		badSurfaceMap.add("earth");
		badSurfaceMap.add("grass");
		badSurfaceMap.add("compacted");
		badSurfaceMap.add("fine_gravel");
		badSurfaceMap.add("ice");
		badSurfaceMap.add("snow");
		badSurfaceMap.add("wood");
		badSurfaceMap.add("mud");

		badSurfaceSpeedMap.put("mud", 25); // TODO
		badSurfaceSpeedMap.put("ice", 20);

		// autobahn
		defaultSpeedMap.put("motorway", 100);
		defaultSpeedMap.put("motorway_link", 70);
		// bundesstraße
		defaultSpeedMap.put("trunk", 70);
		defaultSpeedMap.put("trunk_link", 65);
		// linking bigger town
		defaultSpeedMap.put("primary", 65);
		defaultSpeedMap.put("primary_link", 60);
		// linking towns + villages
		defaultSpeedMap.put("secondary", 60);
		defaultSpeedMap.put("secondary_link", 50);
		// streets without middle line separation
		defaultSpeedMap.put("tertiary", 50);
		defaultSpeedMap.put("tertiary_link", 40);
		defaultSpeedMap.put("unclassified", 30);
		defaultSpeedMap.put("residential", 30);
		// spielstraße
		defaultSpeedMap.put("living_street", 5);
		defaultSpeedMap.put("service", 20);
		// unknown road
		defaultSpeedMap.put("road", 20);
		// forestry stuff
		defaultSpeedMap.put("track", 15);
	}
	
	public double getDefaultMaxSpeed()
	{
		return 100;
	}

	/**
	 * Define the place of speedBits in the flags variable for car.
	 */
	@Override
	public int defineWayBits(int index, int shift) {
		// first two bits are reserved for route handling in superclass
		shift = super.defineWayBits(index, shift);
		speedEncoder = new EncodedDoubleValue("Speed", shift, speedBits, speedFactor, defaultSpeedMap.get("secondary"),
				defaultSpeedMap.get("motorway"));
		return shift + speedBits;
	}

	protected double getSpeed(OSMWay way) {
		String highwayValue = way.getTag("highway");
		Integer speed = defaultSpeedMap.get(highwayValue);
		if (speed == null)
			throw new IllegalStateException("car, no speed found for:" + highwayValue);

		if (highwayValue.equals("track")) {
			String tt = way.getTag("tracktype");
			if (!Helper.isEmpty(tt)) {
				Integer tInt = trackTypeSpeedMap.get(tt);
				if (tInt != null)
					speed = tInt;
			}
		}

		return speed;
	}

	@Override
	public long acceptWay(OSMWay way) {
		String highwayValue = way.getTag("highway");
		if (highwayValue == null) {
			if (way.hasTag("route", ferries)) {
				String motorcarTag = way.getTag("motorcar");
				if (motorcarTag == null)
					motorcarTag = way.getTag("motor_vehicle");

				if (motorcarTag == null && !way.hasTag("foot") && !way.hasTag("bicycle") || "yes".equals(motorcarTag))
					return acceptBit | ferryBit;
			}
			return 0;
		}

		String smoothnessValue = way.getTag("smoothness");

		if ("impassable".equals(smoothnessValue)) {
			return 0; // No wheeled vehicle (see also sac_scale=*)
		}

		if ("track".equals(highwayValue)) {
			String tt = way.getTag("tracktype");
			if (tt != null) // && !tt.equals("grade1")) 4wd supports all grades
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
			return 0;

		// do not drive cars over railways (sometimes incorrectly mapped!)
		if (way.hasTag("railway") && !way.hasTag("railway", acceptedRailways))
			return 0;

		return acceptBit;
	}

	@Override
	public long handleRelationTags(OSMRelation relation, long oldRelationFlags) {
		return oldRelationFlags;
	}

	@Override
	public long handleWayTags(OSMWay way, long allowed, long relationCode) {
		if (!isAccept(allowed))
			return 0;

		long encoded;
		if (!isFerry(allowed)) {
			// get assumed speed from highway type
			double speed = getSpeed(way);
			double maxSpeed = getMaxSpeed(way);
			if (maxSpeed > 0)
				// apply maxSpeed which can mean increase or decrease
				speed = maxSpeed * 0.9;

			// limit speed to max 30 km/h if bad surface
			if (speed > 30 && way.hasTag("surface", badSurfaceMap)) {
				String surfaceValue = way.getTag("surface");
				if (badSurfaceSpeedMap.containsKey(surfaceValue))
					speed = badSurfaceSpeedMap.get(surfaceValue);
				else
					speed = 30;
			}

			encoded = setSpeed(0, speed);

			boolean isRoundabout = way.hasTag("junction", "roundabout");
			if (isRoundabout)
				encoded = setBool(encoded, K_ROUNDABOUT, true);

			if (way.hasTag("oneway", oneways) || isRoundabout) {
				if (way.hasTag("oneway", "-1"))
					encoded |= backwardBit;
				else
					encoded |= forwardBit;
			} else
				encoded |= directionBitMask;

		} else {
			encoded = handleFerryTags(way, defaultSpeedMap.get("living_street"), defaultSpeedMap.get("service"),
					defaultSpeedMap.get("residential"));
			encoded |= directionBitMask;
		}

		return encoded;
	}

	public String getWayInfo(OSMWay way) {
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

	@Override
	public String toString() {
		return "offroadvehicle";
	}

	@Override
	public int getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}
}
