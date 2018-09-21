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
package heigit.ors.routing.graphhopper.extensions.flagencoders.deprecated;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import heigit.ors.routing.graphhopper.extensions.flagencoders.tomove.BikeCommonFlagEncoder;

import java.util.TreeMap;

import static com.graphhopper.routing.util.PriorityCode.*;

public class CycleTourBikeFlagEncoder extends BikeCommonFlagEncoder {
	public CycleTourBikeFlagEncoder() {
		super(4, 2, 0, false);
	}

	public CycleTourBikeFlagEncoder(PMap configuration) {
		super(configuration.getInt("speed_bits", 4) + (configuration.getBool("consider_elevation", false) ? 1 : 0),
				configuration.getDouble("speed_factor", 2),
				configuration.getBool("turn_costs", false) ? 3 : 0, 
						configuration.getBool("consider_elevation", false));

		setBlockFords(false);
		this.setBlockFords(configuration.getBool("block_fords", true));

		setCyclingNetworkPreference("icn", BEST.getValue());
		setCyclingNetworkPreference("ncn", BEST.getValue());
		setCyclingNetworkPreference("rcn", BEST.getValue());
		setCyclingNetworkPreference("lcn", BEST.getValue());

		// addPushingSection("path"); // Runge Assume that paths are suitable
		// for cycle tours.
		addPushingSection("footway");
		addPushingSection("pedestrian");
		addPushingSection("steps");

		avoidHighwayTags.clear();
		avoidHighwayTags.add("motorway");
		avoidHighwayTags.add("motorway_link");
		avoidHighwayTags.add("trunk");
		avoidHighwayTags.add("trunk_link");
		avoidHighwayTags.add("primary");
		avoidHighwayTags.add("primary_link");
		avoidHighwayTags.add("secondary");
		avoidHighwayTags.add("secondary_link");

		// preferHighwayTags.add("road");
		preferHighwayTags.add("path");
		preferHighwayTags.add("service");
		preferHighwayTags.add("residential");
		preferHighwayTags.add("unclassified");
		preferHighwayTags.add("tertiary");
		preferHighwayTags.add("tertiary_link");

		setAvoidSpeedLimit(61);

		setSpecificClassBicycle("touring");

		init();
	}

	@Override
	protected boolean isPushingSection(ReaderWay way) {
		String highway = way.getTag("highway");
		String trackType = way.getTag("tracktype");
		return way.hasTag("highway", pushingSectionsHighways) || way.hasTag("railway", "platform")  || way.hasTag("route", ferries) 
				|| "track".equals(highway) && trackType != null
				&&  !("grade1".equals(trackType) || "grade2".equals(trackType) || "grade3".equals(trackType));
	}

	protected void collect(ReaderWay way, double wayTypeSpeed, TreeMap<Double, Integer> weightToPrioMap) { // Runge
		String service = way.getTag("service");
		String highway = way.getTag("highway");
		if (way.hasTag("bicycle", "designated"))
			weightToPrioMap.put(100d, VERY_NICE.getValue());
		if ("cycleway".equals(highway))
			weightToPrioMap.put(100d, BEST.getValue());

		double maxSpeed = getMaxSpeed(way);

		String cycleway = getCycleway(way); // Runge
		if (!Helper.isEmpty(cycleway) && (cycleway.equals("track") || cycleway.equals("lane")))
		{
			if (maxSpeed <= 50)
				weightToPrioMap.put(90d, VERY_NICE.getValue());
			else if (maxSpeed > 50 && maxSpeed < avoidSpeedLimit)
				weightToPrioMap.put(50d, AVOID_IF_POSSIBLE.getValue());
			else if (maxSpeed >= AVOID_AT_ALL_COSTS.getValue())
				weightToPrioMap.put(50d, REACH_DEST.getValue());
		}

		if (preferHighwayTags.contains(highway) || maxSpeed > 0 && maxSpeed <= 30) {
			if (maxSpeed >= avoidSpeedLimit) // Runge
				weightToPrioMap.put(55d, AVOID_AT_ALL_COSTS.getValue());
			else if (maxSpeed >= 50 && avoidSpeedLimit <= 70)
				weightToPrioMap.put(40d, AVOID_IF_POSSIBLE.getValue());
			else {
				// special case for highway=path
				if ("path".equals(highway))
					weightToPrioMap.put(40d, AVOID_IF_POSSIBLE.getValue());
				else
				{
					if (maxSpeed >= 50)
						weightToPrioMap.put(40d, UNCHANGED.getValue());
					else
						weightToPrioMap.put(40d, PREFER.getValue());
				}
			}

			if (way.hasTag("tunnel", intendedValues))
				weightToPrioMap.put(40d, AVOID_IF_POSSIBLE.getValue());
		} else {
			if ("track".equals(highway)) {
				String trackType = way.getTag("tracktype");

				if (trackType == null || "grade1".equals(trackType) || "grade2".equals(trackType)) {
					weightToPrioMap.put(40d, UNCHANGED.getValue());
				} else if ("grade2".equals(trackType) || "grade3".equals(trackType)) {
					weightToPrioMap.put(40d, AVOID_IF_POSSIBLE.getValue());
				} else {
					weightToPrioMap.put(40d, REACH_DEST.getValue());
				}
			}
		}

		if (pushingSectionsHighways.contains(highway) || way.hasTag("bicycle", "use_sidepath")
				|| "parking_aisle".equals(service)) {
			weightToPrioMap.put(50d, AVOID_IF_POSSIBLE.getValue());
		}
		if (avoidHighwayTags.contains(highway) || ((maxSpeed >= avoidSpeedLimit) && (highway != "track"))) {
			weightToPrioMap.put(50d, REACH_DEST.getValue());
			if (way.hasTag("tunnel", intendedValues))
				weightToPrioMap.put(50d, AVOID_AT_ALL_COSTS.getValue());
		}
		if (way.hasTag("railway", "tram"))
			weightToPrioMap.put(50d, AVOID_AT_ALL_COSTS.getValue());

		String classBicycleSpecific = way.getTag(classBicycleKey);
		if (classBicycleSpecific != null) {
			// We assume that humans are better in classifying preferences
			// compared to our algorithm above -> weight = 100
			weightToPrioMap.put(100d, convertClassValueToPriority(classBicycleSpecific).getValue());
		} else {
			String classBicycle = way.getTag("class:bicycle");
			if (classBicycle != null) {
				weightToPrioMap.put(100d, convertClassValueToPriority(classBicycle).getValue());
			}
		}
	}

	@Override
	public int getVersion() {
		return 2;
	}

	@Override
	protected double getDownhillMaxSpeed()
	{
		return 50;
	}

	@Override
	public String toString() {
		return FlagEncoderNames.BIKE_TOUR;
	}
}
