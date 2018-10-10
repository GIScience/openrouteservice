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
package heigit.ors.routing.graphhopper.extensions.flagencoders.tomove;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.util.*;
import heigit.ors.routing.graphhopper.extensions.flagencoders.ORSAbstractFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.flagencoders.SpeedLimitHandler;

import java.util.*;

import static com.graphhopper.routing.util.PriorityCode.*;

/**
 * Defines bit layout of bicycles (not motorcycles) for speed, access and relations (network).
 * <p>
 *
 * @author Peter Karich
 * @author Nop
 * @author ratrun
 */
abstract public class BikeCommonFlagEncoder extends ORSAbstractFlagEncoder {
	/**
	 * Reports whether this edge is unpaved.
	 */
	public static final int K_UNPAVED = 100;
	protected static final int PUSHING_SECTION_SPEED = 4;
	// Pushing section heighways are parts where you need to get off your bike and push it (German: Schiebestrecke)
	protected final HashSet<String> pushingSectionsHighways = new HashSet<String>();
	protected final HashSet<String> oppositeLanes = new HashSet<String>();
	protected final Set<String> preferHighwayTags = new HashSet<String>();
	protected final Set<String> avoidHighwayTags = new HashSet<String>();
	protected final Set<String> unpavedSurfaceTags = new HashSet<String>();
	private final Set<String> roadValues = new HashSet<String>();
	// convert network tag of bicycle routes into a way route code
	private final Map<String, Integer> bikeNetworkToCode = new HashMap<String, Integer>();
	protected EncodedValue relationCodeEncoder;
	EncodedValue priorityWayEncoder;
	private long unpavedBit = 0;
	private EncodedValue wayTypeEncoder;
	// Car speed limit which switches the preference from UNCHANGED to AVOID_IF_POSSIBLE
	protected int avoidSpeedLimit;

	// This is the specific bicycle class
	protected String classBicycleKey;

	//Runge
	private DistanceCalc distCalc = new DistanceCalc3D();
	private List<RouteSplit> splits = new ArrayList<RouteSplit>();
	private int prevEdgeId = Integer.MAX_VALUE;
	public static boolean SKIP_WAY_TYPE_INFO = false;

	// MARQ24 REMOVED -> see comment near line 635
	//private heigit.ors.routing.graphhopper.extensions.flagencoders.CarFlagEncoder _carFlagEncoder = new CarFlagEncoder();

	protected BikeCommonFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts, boolean considerElevation) {
		super(speedBits, speedFactor, maxTurnCosts);
		// strict set, usually vehicle and agricultural/forestry are ignored by cyclists
		restrictions.addAll(Arrays.asList("bicycle", "vehicle", "access"));
		restrictedValues.add("private");
		restrictedValues.add("no");
		restrictedValues.add("restricted");
		restrictedValues.add("military");
		restrictedValues.add("emergency");

		intendedValues.add("yes");
		intendedValues.add("designated");
		intendedValues.add("official");
		intendedValues.add("permissive");

		oppositeLanes.add("opposite");
		oppositeLanes.add("opposite_lane");
		oppositeLanes.add("opposite_track");

		setBlockByDefault(false);
		potentialBarriers.add("gate");
		// potentialBarriers.add("lift_gate");
		potentialBarriers.add("swing_gate");

		absoluteBarriers.add("stile");
		absoluteBarriers.add("turnstile");

		unpavedSurfaceTags.add("unpaved");
		unpavedSurfaceTags.add("gravel");
		unpavedSurfaceTags.add("ground");
		unpavedSurfaceTags.add("dirt");
		unpavedSurfaceTags.add("grass");
		unpavedSurfaceTags.add("compacted");
		unpavedSurfaceTags.add("earth");
		unpavedSurfaceTags.add("fine_gravel");
		unpavedSurfaceTags.add("grass_paver");
		unpavedSurfaceTags.add("ice");
		unpavedSurfaceTags.add("mud");
		unpavedSurfaceTags.add("salt");
		unpavedSurfaceTags.add("sand");
		unpavedSurfaceTags.add("wood");

		roadValues.add("living_street");
		roadValues.add("road");
		roadValues.add("service");
		roadValues.add("unclassified");
		roadValues.add("residential");
		roadValues.add("trunk");
		roadValues.add("trunk_link");
		roadValues.add("primary");
		roadValues.add("primary_link");
		roadValues.add("secondary");
		roadValues.add("secondary_link");
		roadValues.add("tertiary");
		roadValues.add("tertiary_link");

		setConsiderElevation(considerElevation);
		maxPossibleSpeed = 30;

		if (considerElevation)
			maxPossibleSpeed = (int)getDownhillMaxSpeed();

		Map<String, Integer> trackTypeSpeedMap = new HashMap<String, Integer>();
		trackTypeSpeedMap.put("grade1", 18); // paved
		trackTypeSpeedMap.put("grade2", 12); // now unpaved ...
		trackTypeSpeedMap.put("grade3", 8);
		trackTypeSpeedMap.put("grade4", 6);
		trackTypeSpeedMap.put("grade5", 4); // like sand/grass     

		Map<String, Integer> badSurfaceSpeedMap = new HashMap<String, Integer>();
		badSurfaceSpeedMap.put("paved", 18);
		badSurfaceSpeedMap.put("asphalt", 18);
		badSurfaceSpeedMap.put("cobblestone", 8);
		badSurfaceSpeedMap.put("cobblestone:flattened", 10);
		badSurfaceSpeedMap.put("sett", 10);
		badSurfaceSpeedMap.put("concrete", 18);
		badSurfaceSpeedMap.put("concrete:lanes", 16);
		badSurfaceSpeedMap.put("concrete:plates", 16);
		badSurfaceSpeedMap.put("paving_stones", 12);
		badSurfaceSpeedMap.put("paving_stones:30", 12);
		badSurfaceSpeedMap.put("unpaved", 14);
		badSurfaceSpeedMap.put("compacted", 16);
		badSurfaceSpeedMap.put("dirt", 10);
		badSurfaceSpeedMap.put("earth", 12);
		badSurfaceSpeedMap.put("fine_gravel", 18);
		badSurfaceSpeedMap.put("grass", 8);
		badSurfaceSpeedMap.put("grass_paver", 8);
		badSurfaceSpeedMap.put("gravel", 12);
		badSurfaceSpeedMap.put("ground", 12);
		badSurfaceSpeedMap.put("ice", PUSHING_SECTION_SPEED / 2);
		badSurfaceSpeedMap.put("metal", 10);
		badSurfaceSpeedMap.put("mud", 10);
		badSurfaceSpeedMap.put("pebblestone", 16);
		badSurfaceSpeedMap.put("salt", 6);
		badSurfaceSpeedMap.put("sand", 6);
		badSurfaceSpeedMap.put("wood", 6);

		Map<String, Integer> highwaySpeeds = new HashMap<String, Integer>();
		highwaySpeeds.put("living_street", 12);
		highwaySpeeds.put("steps", PUSHING_SECTION_SPEED / 2);

		final int CYCLEWAY_SPEED = 18;  // Make sure cycleway and path use same speed value, see #634
		highwaySpeeds.put("cycleway", CYCLEWAY_SPEED);
		highwaySpeeds.put("path", 10);
		highwaySpeeds.put("footway", 6);
		highwaySpeeds.put("pedestrian", 6);
		highwaySpeeds.put("track", 12);
		highwaySpeeds.put("service", 14);
		highwaySpeeds.put("residential", 18);
		// no other highway applies:
		highwaySpeeds.put("unclassified", 16);
		// unknown road:
		highwaySpeeds.put("road", 12);

		highwaySpeeds.put("trunk", 18);
		highwaySpeeds.put("trunk_link", 18);
		highwaySpeeds.put("primary", 18);
		highwaySpeeds.put("primary_link", 18);
		highwaySpeeds.put("secondary", 18);
		highwaySpeeds.put("secondary_link", 18);
		highwaySpeeds.put("tertiary", 18);
		highwaySpeeds.put("tertiary_link", 18);

		// special case see tests and #191
		highwaySpeeds.put("motorway", 18);
		highwaySpeeds.put("motorway_link", 18);
		avoidHighwayTags.add("motorway");
		avoidHighwayTags.add("motorway_link");

		_speedLimitHandler = new SpeedLimitHandler(this.toString(), highwaySpeeds, badSurfaceSpeedMap, trackTypeSpeedMap);

		setCyclingNetworkPreference("icn", BEST.getValue());
		setCyclingNetworkPreference("ncn", BEST.getValue());
		setCyclingNetworkPreference("rcn", VERY_NICE.getValue());
		setCyclingNetworkPreference("lcn", PREFER.getValue());
		setCyclingNetworkPreference("mtb", UNCHANGED.getValue());

		setCyclingNetworkPreference("deprecated", AVOID_AT_ALL_COSTS.getValue());

		setAvoidSpeedLimit(71);
	}

	@Override
	public int getVersion() {
		return 2;
	}

	protected double getDownhillMaxSpeed()
	{
		return 30;
	}

	@Override
	public int defineWayBits(int index, int shift) {
		// first two bits are reserved for route handling in superclass
		shift = super.defineWayBits(index, shift);

		speedEncoder = new EncodedDoubleValue("Speed", shift, speedBits, speedFactor, _speedLimitHandler.getSpeed("cycleway"),
				maxPossibleSpeed);
		shift += speedEncoder.getBits();

		if (isConsiderElevation())
		{
			reverseSpeedEncoder = new EncodedDoubleValue("Reverse Speed", shift, speedBits, speedFactor,
					_speedLimitHandler.getSpeed("cycleway"), maxPossibleSpeed);
			shift += reverseSpeedEncoder.getBits();
		}

		unpavedBit = 1L << shift++;
		if (!SKIP_WAY_TYPE_INFO)
		{
			// 2 bits
			wayTypeEncoder = new EncodedValue("WayType", shift, 2, 1, 0, 3, true);
			shift += wayTypeEncoder.getBits();
		}

		priorityWayEncoder = new EncodedValue("PreferWay", shift, 3, 1, 0, 7);
		shift += priorityWayEncoder.getBits();

		return shift;
	}

	@Override
	public int defineRelationBits(int index, int shift) {
		relationCodeEncoder = new EncodedValue("RelationCode", shift, 3, 1, 0, 7);
		return shift + relationCodeEncoder.getBits();
	}

	@Override
	public long acceptWay(ReaderWay way) {
		String highwayValue = way.getTag("highway");
		if (highwayValue == null) {
			if (way.hasTag("route", ferries)) {
				// if bike is NOT explicitly tagged allow bike but only if foot is not specified
				String bikeTag = way.getTag("bicycle");
				if (bikeTag == null && !way.hasTag("foot") || "yes".equals(bikeTag))
					return acceptBit | ferryBit;
			}

			// special case not for all acceptedRailways, only platform
			if (way.hasTag("railway", "platform"))
				return acceptBit;

			return 0;
		}

		if (!_speedLimitHandler.hasSpeedValue(highwayValue) && !"bridleway".equals(highwayValue)) // Runge: exclude bridleways, see http://www.openstreetmap.org/way/24064837
			return 0;

		String sacScale = way.getTag("sac_scale");
		if (sacScale != null) {
			if ((way.hasTag("highway", "cycleway"))
					&& (way.hasTag("sac_scale", "hiking")))
				return acceptBit;
			if (!isSacScaleAllowed(sacScale))
				return 0;
		}

		// use the way if it is tagged for bikes
		if (way.hasTag("bicycle", intendedValues) ||
				way.hasTag("bicycle", "dismount") ||
				way.hasTag("highway", "cycleway") ||
				// Runge: http://www.openstreetmap.org/way/1700503
				way.hasTag("bicycle_road", "yes")
				)
			return acceptBit;

		// accept only if explicitly tagged for bike usage
		if ("motorway".equals(highwayValue) || "motorway_link".equals(highwayValue))
			return 0;

		if (way.hasTag("motorroad", "yes"))
			return 0;

		// do not use fords with normal bikes, flagged fords are in included above
		if (isBlockFords() && (way.hasTag("highway", "ford") || way.hasTag("ford")))
			return 0;

		// check access restrictions
		if (way.hasTag(restrictions, restrictedValues) && !getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way))
			return 0;

		if (getConditionalTagInspector().isPermittedWayConditionallyRestricted(way))
			return 0;
		else
			return acceptBit;
	}

	boolean isSacScaleAllowed(String sacScale) {
		// other scales are nearly impossible by an ordinary bike, see http://wiki.openstreetmap.org/wiki/Key:sac_scale
		return "hiking".equals(sacScale);
	}

	@Override
	public long handleRelationTags(ReaderRelation relation, long oldRelationFlags) {
		int code = 0;
		if (relation.hasTag("route", "bicycle")) {
			Integer val = bikeNetworkToCode.get(relation.getTag("network"));
			if (val != null)
				code = val;
			else
				code = PriorityCode.PREFER.getValue();  // Assume priority of network "lcn" as bicycle route default
		} else if (relation.hasTag("route", "ferry")) {
			code = AVOID_IF_POSSIBLE.getValue();
		}

		int oldCode = (int) relationCodeEncoder.getValue(oldRelationFlags);
		if (oldCode < code)
			return relationCodeEncoder.setValue(0, code);
		return oldRelationFlags;
	}

	/**
	 * Apply maxspeed: In contrast to the implementation of the AbstractFlagEncoder, we assume that
	 * we can reach the maxspeed for bicycles in case that the road type speed is higher and not
	 * just only 90%.
	 * <p>
	 *
	 * @param way:   needed to retrieve tags
	 * @param speed: speed guessed e.g. from the road type or other tags
	 * @return The assumed average speed.
	 */
	@Override
	protected double applyMaxSpeed(ReaderWay way, double speed) {
		double maxSpeed = getMaxSpeed(way);
		if (maxSpeed >= 0) {
			// We strictly obay speed limits, see #600
			if (maxSpeed < speed) {
				return maxSpeed;
			}
		}
		return speed;
	}

	@Override
	public long handleWayTags(ReaderWay way, long allowed, long relationFlags) {
		if (!isAccept(allowed))
			return 0;

		long flags = 0;
		double wayTypeSpeed = getSpeed(way);
		if (!isFerry(allowed)) {
			wayTypeSpeed = applyMaxSpeed(way, wayTypeSpeed);
			flags = handleSpeed(way, wayTypeSpeed, flags);
			flags = handleBikeRelated(way, flags, relationFlags > UNCHANGED.getValue());

			boolean isRoundabout = way.hasTag("junction", "roundabout");
			if (isRoundabout) {
				flags = setBool(flags, K_ROUNDABOUT, true);
			}

		} else {
			double ferrySpeed = getFerrySpeed(way,
					_speedLimitHandler.getSpeed("living_street"),
					_speedLimitHandler.getSpeed("track"),
					_speedLimitHandler.getSpeed("primary"));
			flags = handleSpeed(way, ferrySpeed, flags);
			flags |= directionBitMask;
		}
		int priorityFromRelation = 0;
		if (relationFlags != 0)
			priorityFromRelation = (int) relationCodeEncoder.getValue(relationFlags);

		flags = priorityWayEncoder.setValue(flags, handlePriority(way, wayTypeSpeed, priorityFromRelation));
		return flags;
	}

	int getSpeed(ReaderWay way) {
		int speed = PUSHING_SECTION_SPEED;
		String highwayTag = way.getTag("highway");
		Integer highwaySpeed = _speedLimitHandler.getSpeed(highwayTag);

		// Under certain conditions we need to increase the speed of pushing sections to the speed of a "highway=cycleway"
		if (way.hasTag("highway", pushingSectionsHighways)
				&& ((way.hasTag("foot", "yes") && way.hasTag("segregated", "yes"))
						|| way.hasTag("bicycle", "designated") || way.hasTag("bicycle", "official") || way.hasTag("bicycle", "yes")))
			highwaySpeed = _speedLimitHandler.getSpeed("cycleway");

		// Until now we assumed that the way is no pushing section
		// Now we check that, but only in case that our speed is bigger compared to the PUSHING_SECTION_SPEED
		if (speed > PUSHING_SECTION_SPEED
				&& (way.hasTag("highway", pushingSectionsHighways) || way.hasTag("bicycle", "dismount"))) {
			if (!way.hasTag("bicycle", intendedValues)) {
				// Here we set the speed for pushing sections and set speed for steps as even lower:
				if (way.hasTag("highway", "steps"))
					speed = PUSHING_SECTION_SPEED / 2;
				else
					speed = PUSHING_SECTION_SPEED;
			} else if (way.hasTag("bicycle", "designated") || way.hasTag("bicycle", "official") || way.hasTag("bicycle", "yes")) {
				// Here we handle the cases where the OSM tagging results in something similar to "highway=cycleway"
				speed = _speedLimitHandler.getSpeed("cycleway");
			} else {
				speed = PUSHING_SECTION_SPEED;
			}
			// Increase speed in case of segregated
			if (speed <= PUSHING_SECTION_SPEED && way.hasTag("segregated", "yes"))
				speed = PUSHING_SECTION_SPEED * 2;
		}

		String s = way.getTag("surface");
		if (!Helper.isEmpty(s)) {
			Integer surfaceSpeed = _speedLimitHandler.getSurfaceSpeed(s);
			if (surfaceSpeed != null && surfaceSpeed != -1) {
				speed = surfaceSpeed;
				// Boost handling for good surfaces
				if (highwaySpeed != null)
				{
					if (surfaceSpeed > highwaySpeed)
					{
						// Avoid boosting if pushing section
						if (pushingSectionsHighways.contains(highwayTag) && /* Runge */!highwayTag.equals("track"))
							speed = highwaySpeed;
						else
							speed = surfaceSpeed;
					}
					else  // runge
					{
						String cyclewayTag = way.getTag("cycleway");
						if (cyclewayTag != null && "track".equals(cyclewayTag))
						{
							// http://www.openstreetmap.org/way/28310994#map=19/51.44178/7.01691&layers=D
							// do not use speed taken according to surface type
							speed =  _speedLimitHandler.getSpeed("cycleway");
						}
					}
				}

			}
		} else {
			String tt = way.getTag("tracktype");
			if (!Helper.isEmpty(tt)) {
				Integer tInt = _speedLimitHandler.getTrackTypeSpeed(tt);
				if (tInt != null && tInt != -1)
					speed = tInt;
			} else if (highwaySpeed != null) {
				if (!way.hasTag("service"))
					speed = highwaySpeed;
				else
					speed = _speedLimitHandler.getSpeed("living_street");
			}
		}
		return speed;
	}

	@Override
	public InstructionAnnotation getAnnotation(long flags, Translation tr) {
		int paveType = 0; // paved
		if (isBool(flags, K_UNPAVED))
			paveType = 1; // unpaved        

		if (SKIP_WAY_TYPE_INFO)  // Runge. We don't use this information
			return new InstructionAnnotation(0, "");
		else
		{
			int wayType = (int) wayTypeEncoder.getValue(flags);
			String wayName = getWayName(paveType, wayType, tr);
			return new InstructionAnnotation(0, wayName);
		}
	}

	String getWayName(int pavementType, int wayType, Translation tr) {
		String pavementName = "";
		if (pavementType == 1)
			pavementName = tr.tr("unpaved");

		String wayTypeName = "";
		switch (wayType) {
		case 0:
			wayTypeName = "";
			break;
		case 1:
			wayTypeName = tr.tr("off_bike");
			break;
		case 2:
			wayTypeName = tr.tr("cycleway");
			break;
		case 3:
			wayTypeName = tr.tr("small_way");
			break;
		}

		if (pavementName.isEmpty()) {
			if (wayType == 0 || wayType == 3)
				return "";
			return wayTypeName;
		} else if (wayTypeName.isEmpty())
			return pavementName;
		else
			return wayTypeName + ", " + pavementName;
	}

	/**
	 * In this method we prefer cycleways or roads with designated bike access and avoid big roads
	 * or roads with trams or pedestrian.
	 * <p>
	 *
	 * @return new priority based on priorityFromRelation and on the tags in ReaderWay.
	 */
	protected int handlePriority(ReaderWay way, double wayTypeSpeed, int priorityFromRelation) {
		TreeMap<Double, Integer> weightToPrioMap = new TreeMap<Double, Integer>();
		if (priorityFromRelation == 0)
			weightToPrioMap.put(0d, UNCHANGED.getValue());
		else
			weightToPrioMap.put(110d, priorityFromRelation);

		collect(way, wayTypeSpeed, weightToPrioMap);

		// pick priority with biggest order value
		return weightToPrioMap.lastEntry().getValue();
	}

	// Conversion of class value to priority. See http://wiki.openstreetmap.org/wiki/Class:bicycle
	protected PriorityCode convertClassValueToPriority(String tagvalue) {
		int classvalue;
		try {
			classvalue = Integer.parseInt(tagvalue);
		} catch (NumberFormatException e) {
			return UNCHANGED;
		}

		switch (classvalue) {
		case 3:
			return BEST;
		case 2:
			return VERY_NICE;
		case 1:
			return PREFER;
		case 0:
			return UNCHANGED;
		case -1:
			return AVOID_IF_POSSIBLE;
		case -2:
			return REACH_DEST;
		case -3:
			return AVOID_AT_ALL_COSTS;
		default:
			return UNCHANGED;
		}
	}

	/**
	 * @param weightToPrioMap associate a weight with every priority. This sorted map allows
	 *                        subclasses to 'insert' more important priorities as well as overwrite determined priorities.
	 */
	protected void collect(ReaderWay way, double wayTypeSpeed, TreeMap<Double, Integer> weightToPrioMap) {
		String service = way.getTag("service");
		String highway = way.getTag("highway");
		if (way.hasTag("bicycle", "designated") || way.hasTag("bicycle", "official") || way.hasTag("bicycle_road", "yes")) {
			if ("path".equals(highway))
				weightToPrioMap.put(100d, VERY_NICE.getValue());
			else
				weightToPrioMap.put(100d, PREFER.getValue());
		}

		if ("cycleway".equals(highway)) {
			if (way.hasTag("foot", intendedValues) && !way.hasTag("segregated", "yes"))
				weightToPrioMap.put(100d, PREFER.getValue());
			else
				weightToPrioMap.put(100d, VERY_NICE.getValue());
		}

		double maxSpeed = getMaxSpeed(way);

		String cycleway = getCycleway(way); // Runge

		if (!Helper.isEmpty(cycleway) && (cycleway.equals("track") || cycleway.equals("lane")))
		{
			// http://www.openstreetmap.org/way/30606187 cycleway=track
			//http://www.openstreetmap.org/way/182932159 bicycle=yes and cycleway:right=track
			//http://www.openstreetmap.org/way/133845943 cycleway=lane
			if (maxSpeed <= 50)
				weightToPrioMap.put(90d, VERY_NICE.getValue());
			else if (maxSpeed > 50 && maxSpeed < avoidSpeedLimit)
				weightToPrioMap.put(50d, AVOID_IF_POSSIBLE.getValue());
			else if (maxSpeed >= AVOID_AT_ALL_COSTS.getValue())
				weightToPrioMap.put(50d, REACH_DEST.getValue());
		} 

		if (preferHighwayTags.contains(highway) || (maxSpeed > 0 && maxSpeed <= 30)) {
			if (maxSpeed >= avoidSpeedLimit) // Runge
				weightToPrioMap.put(55d, PriorityCode.REACH_DEST.getValue());
			else if (maxSpeed >= 50 && avoidSpeedLimit <= 70) // Runge racingbike
				weightToPrioMap.put(40d, PriorityCode.AVOID_IF_POSSIBLE.getValue());
			else
			{
				if (maxSpeed >= 50)
					weightToPrioMap.put(40d, UNCHANGED.getValue());
				else
					weightToPrioMap.put(40d, PREFER.getValue());
			}

			if (way.hasTag("tunnel", intendedValues))
				weightToPrioMap.put(40d, UNCHANGED.getValue());
		} else if (avoidHighwayTags.contains(highway) || (maxSpeed >= avoidSpeedLimit && !"track".equals(highway))) {
			Integer value = REACH_DEST.getValue();

			String surface = way.getTag("surface");
			// check the surface on motorways and trunks http://www.openstreetmap.org/way/37390463
			// set higher priority if car speed is low
			if (surface != null)
			{
				// MARQ24: WHY the heck a BikeFlagEncoder should use ANY SPEED from a CarFlagEncoder???
				// this makes NO SENSE to me!
				// SO I will comment this section out
				/* REMOVED BY MARQ24 (2018.06.30)
				double maxCarSpeed = _carFlagEncoder.getMaxSpeed(way);
				if (maxCarSpeed <= 40)
				{
					double carSpeed = _carFlagEncoder.getSpeed(way);
					carSpeed = _carFlagEncoder.getSurfaceSpeed(way, carSpeed);
					if (carSpeed != -1 && carSpeed <= 40)
					{
						value = UNCHANGED.getValue();
					}
				} REMOVE END*/
			}

			weightToPrioMap.put(50d, value);
			if (way.hasTag("tunnel", intendedValues) || maxSpeed >= avoidSpeedLimit + 10) // Runge
				weightToPrioMap.put(50d, AVOID_AT_ALL_COSTS.getValue());
		}

		if (pushingSectionsHighways.contains(highway)
				|| way.hasTag("bicycle", "use_sidepath")
				|| "parking_aisle".equals(service)) {
			int pushingSectionPrio = AVOID_IF_POSSIBLE.getValue();
			if (way.hasTag("bicycle", "yes") || way.hasTag("bicycle", "permissive"))
				pushingSectionPrio = PREFER.getValue();
			if (way.hasTag("bicycle", "designated") || way.hasTag("bicycle", "official"))
				pushingSectionPrio = VERY_NICE.getValue();
			if (way.hasTag("foot", "yes")) {
				pushingSectionPrio = Math.max(pushingSectionPrio - 1, WORST.getValue());
				if (way.hasTag("segregated", "yes"))
					pushingSectionPrio = Math.min(pushingSectionPrio + 1, BEST.getValue());
			}
			weightToPrioMap.put(100d, pushingSectionPrio);
		}

		if (way.hasTag("railway", "tram"))
			weightToPrioMap.put(50d, AVOID_AT_ALL_COSTS.getValue());

		String classBicycleValue = way.getTag(classBicycleKey);
		if (classBicycleValue != null) {
			// We assume that humans are better in classifying preferences compared to our algorithm above -> weight = 100
			weightToPrioMap.put(100d, convertClassValueToPriority(classBicycleValue).getValue());
		} else {
			String classBicycle = way.getTag("class:bicycle");
			if (classBicycle != null)
				weightToPrioMap.put(100d, convertClassValueToPriority(classBicycle).getValue());
		}

		// Increase the priority for scenic routes or in case that maxspeed limits our average speed as compensation. See #630
		if (way.hasTag("scenic", "yes") || maxSpeed > 0 && maxSpeed < wayTypeSpeed) {
			if (weightToPrioMap.lastEntry().getValue() < BEST.getValue())
				// Increase the prio by one step
				weightToPrioMap.put(110d, weightToPrioMap.lastEntry().getValue() + 1);
		}
	}

	/**
	 * Handle surface and wayType encoding
	 */
	long handleBikeRelated(ReaderWay way, long encoded, boolean partOfCycleRelation) {
		String surfaceTag = way.getTag("surface");
		String highway = way.getTag("highway");
		String trackType = way.getTag("tracktype");

		// Populate unpavedBit
		if ("track".equals(highway) && (trackType == null || !"grade1".equals(trackType))
				|| "path".equals(highway) && surfaceTag == null
				|| unpavedSurfaceTags.contains(surfaceTag)) {
			encoded = setBool(encoded, K_UNPAVED, true);
		}

		WayType wayType;
		if (roadValues.contains(highway))
			wayType = WayType.ROAD;
		else
			wayType = WayType.OTHER_SMALL_WAY;

		boolean isPushingSection = isPushingSection(way);
		if (isPushingSection && !partOfCycleRelation || "steps".equals(highway))
			wayType = WayType.PUSHING_SECTION;

		if (way.hasTag("bicycle", intendedValues)) {
			if (isPushingSection && !way.hasTag("bicycle", "designated"))
				wayType = WayType.OTHER_SMALL_WAY;
			else if (wayType == WayType.OTHER_SMALL_WAY || wayType == WayType.PUSHING_SECTION)
				wayType = WayType.CYCLEWAY;
		} else if ("cycleway".equals(highway))
			wayType = WayType.CYCLEWAY;

		if (SKIP_WAY_TYPE_INFO)
			return encoded;
		else
			return wayTypeEncoder.setValue(encoded, wayType.getValue());
	}

	@Override
	public void applyWayTags(ReaderWay way, EdgeIteratorState edge )
	{
		// Runge
		if (isConsiderElevation())
		{
			PointList pl = edge.fetchWayGeometry(3);
			if (!pl.is3D())
				throw new IllegalStateException("To support speed calculation based on elevation data it is necessary to enable import of it.");

			long flags = edge.getFlags();

			if (way.hasTag("tunnel", "yes") || way.hasTag("bridge", "yes") || way.hasTag("highway", "steps"))
			{
				// do not change speed
				// note: although tunnel can have a difference in elevation it is very unlikely that the elevation data is correct for a tunnel
			} else
			{
				double fullDist2D = edge.getDistance();

				if (Double.isInfinite(fullDist2D))
				{
					System.err.println("infinity distance? for way:" + way.getId());
					return;
				}

				// for short edges an incline makes no sense and for 0 distances could lead to NaN values for speed, see #432
				if (fullDist2D < 1)
					return;

				double wayMaxSpeed = getMaxSpeed(way);
				double maxSpeed = getDownhillMaxSpeed(); // getHighwaySpeed("cycleway");
				if (wayMaxSpeed != -1)
					maxSpeed = Math.min(maxSpeed, wayMaxSpeed);

				// Formulas for the following calculations is taken from http://www.flacyclist.com/content/perf/science.html
				double gradient = 0.0;

				if (prevEdgeId != EdgeIteratorStateHelper.getOriginalEdge(edge))
				{
					String incline = way.getTag("incline"); 
					if (!Helper.isEmpty(incline))
					{
						incline = incline.replace("%", "").replace(",", ".");

						try
						{
							double v = Double.parseDouble(incline);

							splits.clear();
							RouteSplit split = new RouteSplit();
							split.Length = fullDist2D;
							split.Gradient = v;
						}
						catch(Exception ex)
						{
							SteepnessUtil.computeRouteSplits(pl, false, distCalc, splits);
						}
					}
					else
						SteepnessUtil.computeRouteSplits(pl, false, distCalc, splits);
 
					prevEdgeId = EdgeIteratorStateHelper.getOriginalEdge(edge);
				}


				double speed = 0;
				double speedReverse = 0;

				if (isForward(flags))
					speed =  getSpeed(flags);

				if (isBackward(flags))
					speedReverse = getReverseSpeed(flags);

				if (splits.size() == 1)
				{
					RouteSplit split = splits.get(0);
					gradient = split.Gradient;

					if (split.Length < 60)
					{
						if (Math.abs(gradient) > 6)
						{
							if (Math.abs(gradient) < 9)
								gradient /= 2.0;
							else
								gradient /= 4.0;
						}

					}

					if (Math.abs(gradient) > 1.5)
					{
						if (speed != 0)
							speed = getGradientSpeed(speed, (int)Math.round(gradient));

						if (speedReverse != 0)
							speedReverse = getGradientSpeed(speedReverse, (int)Math.round(-gradient));
					}
				}
				else
				{
					double distUphill = 0.0;
					double distDownhill = 0.0;
					double distUphillR = 0.0;
					double distDownhillR = 0.0;
					double distTotalEqFlat = 0.0; 
					double length = 0.0;

					for(RouteSplit split : splits)
					{
						gradient = split.Gradient;
						length = split.Length;

						if (Math.abs(gradient) < 1.5)
						{

						}
						else
						{
							if (speed != 0)
							{
								double Vc = getGradientSpeed(speed, (int)Math.round(gradient));

								if (gradient > 0)
									distUphill += (speed/Vc - 1) * length;
								else
									distDownhill += (speed/Vc - 1) * length;
							}

							if (speedReverse != 0)
							{
								gradient = -gradient;
								double Vc = getGradientSpeed(speedReverse, (int)Math.round(gradient));

								if (gradient > 0)
									distUphillR += (speedReverse/Vc - 1) * length;
								else
									distDownhillR += (speedReverse/Vc - 1) * length;
							}
						}
					}

					if (speed != 0)
					{
						distTotalEqFlat = fullDist2D + distUphill + distDownhill;
						speed *= fullDist2D/distTotalEqFlat;
					}

					if (speedReverse != 0)
					{
						distTotalEqFlat = fullDist2D + distUphillR + distDownhillR;
						speedReverse *= fullDist2D/distTotalEqFlat;
					} 
				}

				flags = this.setSpeed(flags, Helper.keepIn(speed, PUSHING_SECTION_SPEED / 2, maxSpeed));
				flags = this.setReverseSpeed(flags, Helper.keepIn(speedReverse, PUSHING_SECTION_SPEED / 2, maxSpeed));
			}

			edge.setFlags(flags);
		}
	}

	protected double getGradientSpeed(double speed, int gradient)
	{
		if (gradient < -18)
		{
			if (speed > 10)
				return getDownhillMaxSpeed();
			else
				return speed;
		}
		else
		{
			if (speed > 10)
				return speed * getGradientSpeedFactor(gradient);
			else
			{
				double result = speed * getGradientSpeedFactor(gradient);

				// forbid high downhill speeds on surfaces with low speeds
				if (result > speed)
					return speed;
				else
					return result;
			}
		}
	}

	private double getGradientSpeedFactor(int gradient)
	{
		if (gradient < -18)
			return 3.5;
		else if (gradient > 17)
			return 0.1;
		else
		{
			switch(gradient)
			{
			case -18:
				return 	3.332978723;
			case -17:
				return	3.241489362;
			case -16:
				return	3.14751773;
			case -15:
				return	3.05070922;
			case -14:
				return	2.95106383;
			case -13:
				return	2.84822695;
			case -12:
				return	2.741843972;
			case -11:
				return	2.631560284;
			case -10:
				return	2.517021277;
			case -9:
				return	2.39787234;
			case -8:
				return	2.273049645;
			case -7:
				return	2.142553191;
			case -6:
				return	2.004964539;
			case -5:
				return	1.859574468;
			case -4:
				return	1.705673759;
			case -3:
				return	1.542198582;
			case -2:
				return	1.368439716;
			case -1:
				return	1.186524823;
			case 0:
				return	1;
			case 1:
				return	0.820567376;
			case 2:
				return	0.663120567;
			case 3:
				return	0.537234043;
			case 4:
				return	0.442553191;
			case 5:
				return	0.372695035;
			case 6:
				return	0.319858156;
			case 7:
				return	0.279787234;
			case 8:
				return	0.24822695;
			case 9:
				return	0.222695035;
			case 10:
				return	0.20177305;
			case 11:
				return	0.184751773;
			case 12:
				return	0.170212766;
			case 13:
				return	0.157446809;
			case 14:
				return	0.146808511;
			case 15:
				return	0.137234043;
			case 16:
				return	0.129078014;
			case 17:
				return	0.121631206;
			}
		}

		return 1;
	}

	@Override
	public long setBool(long flags, int key, boolean value) {
		switch (key) {
		case K_UNPAVED:
			return value ? flags | unpavedBit : flags & ~unpavedBit;
		default:
			return super.setBool(flags, key, value);
		}
	}

	@Override
	public boolean isBool(long flags, int key) {
		switch (key) {
		case K_UNPAVED:
			return (flags & unpavedBit) != 0;
		default:
			return super.isBool(flags, key);
		}
	}

	@Override
	public double getDouble(long flags, int key) {
		switch (key) {
		case PriorityWeighting.KEY:
			return (double) priorityWayEncoder.getValue(flags) / BEST.getValue();
		default:
			return super.getDouble(flags, key);
		}
	}

	protected boolean isPushingSection(ReaderWay way) {
		return way.hasTag("highway", pushingSectionsHighways) || way.hasTag("railway", "platform") || way.hasTag("bicycle", "dismount") || way.hasTag("route", ferries); // Runge
	}

	protected long handleSpeed(ReaderWay way, double speed, long encoded) {
		encoded = setSpeed(encoded, speed);

		// Runge
		if (isConsiderElevation())
			encoded = setReverseSpeed(encoded, speed);

		// handle oneways        
		boolean isOneway = way.hasTag("oneway", oneways)
				|| way.hasTag("oneway:bicycle", oneways)
				|| way.hasTag("vehicle:backward")
				|| way.hasTag("vehicle:forward")
				|| way.hasTag("bicycle:forward");

		if (!way.hasTag("bicycle_road", "yes") && (isOneway || way.hasTag("junction", "roundabout"))
				&& !way.hasTag("oneway:bicycle", "no")
				&& !way.hasTag("bicycle:backward")
				&& !way.hasTag("cycleway", oppositeLanes)
				&& !way.hasTag("cycleway:left", oppositeLanes)
				&& !way.hasTag("cycleway:right", oppositeLanes)) {
			boolean isBackward = way.hasTag("oneway", "-1")
					|| way.hasTag("oneway:bicycle", "-1")
					|| way.hasTag("vehicle:forward", "no")
					|| way.hasTag("bicycle:forward", "no");
			if (isBackward)
				encoded |= backwardBit;
			else
				encoded |= forwardBit;

		} else {
			encoded |= directionBitMask;
		}
		return encoded;
	}

	protected void setCyclingNetworkPreference(String network, int code) {
		bikeNetworkToCode.put(network, code);
	}

	protected void addPushingSection(String highway) {
		pushingSectionsHighways.add(highway);
	}

	protected  String getCycleway(ReaderWay way)
	{
		String cycleway = way.getTag("cycleway");
		if (Helper.isEmpty(cycleway))
		{
			cycleway = way.getTag("cycleway:right");
			if (Helper.isEmpty(cycleway))
			{
				cycleway = way.getTag("cycleway:left");
			}
		}

		return cycleway;
	}

	@Override
	public boolean supports(Class<?> feature) {
		if (super.supports(feature))
			return true;

		return PriorityWeighting.class.isAssignableFrom(feature);
	}

	public void setAvoidSpeedLimit(int limit) {
		avoidSpeedLimit = limit;
	}

	protected void setSpecificClassBicycle(String subkey) {
		classBicycleKey = "class:bicycle:" + subkey;
	}

	private enum WayType {
		ROAD(0),
		PUSHING_SECTION(1),
		CYCLEWAY(2),
		OTHER_SMALL_WAY(3);

		private final int value;

		private WayType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
}
