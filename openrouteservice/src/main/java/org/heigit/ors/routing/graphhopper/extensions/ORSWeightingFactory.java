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
package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.*;
import com.graphhopper.storage.ConditionalEdges;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.TurnCostExtension;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.routing.ProfileWeighting;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.TrafficGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.weighting.*;
import org.heigit.ors.routing.traffic.RoutingTrafficSpeedCalculator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ORSWeightingFactory implements WeightingFactory {
	private Map<Object, TurnCostExtension> turnCostExtensionMap;

	public ORSWeightingFactory()
	{
		turnCostExtensionMap = new HashMap<>();
	}

	public Weighting createWeighting(HintsMap hintsMap, FlagEncoder encoder, GraphHopperStorage graphStorage) {

		TraversalMode tMode = encoder.supports(TurnWeighting.class) ? TraversalMode.EDGE_BASED : TraversalMode.NODE_BASED;
		if (hintsMap.has(Parameters.Routing.EDGE_BASED))
			tMode = hintsMap.getBool(Parameters.Routing.EDGE_BASED, false) ? TraversalMode.EDGE_BASED : TraversalMode.NODE_BASED;
		if (tMode.isEdgeBased() && !encoder.supports(TurnWeighting.class)) {
			throw new IllegalArgumentException("You need a turn cost extension to make use of edge_based=true, e.g. use car|turn_costs=true");
		}

		String strWeighting = hintsMap.get("weighting_method", "").toLowerCase();
		if (Helper.isEmpty(strWeighting))
			strWeighting = hintsMap.getWeighting();

		Weighting result = null;

        if("true".equalsIgnoreCase(hintsMap.get("isochroneWeighting", "false")))
            return createIsochroneWeighting(hintsMap, encoder);

		if ("shortest".equalsIgnoreCase(strWeighting))
		{
			result = new ShortestWeighting(encoder); 
		}
		else if ("fastest".equalsIgnoreCase(strWeighting)) 
		{
			if (encoder.supports(PriorityWeighting.class) && !encoder.toString().equals(FlagEncoderNames.HEAVYVEHICLE))
				result = new PriorityWeighting(encoder, hintsMap);
	         else
	        	 result = new FastestWeighting(encoder, hintsMap);
		}
		else  if ("priority".equalsIgnoreCase(strWeighting))
		{
			result = new PreferencePriorityWeighting(encoder, hintsMap);
		} 
		else 
		{
			if (encoder.supports(PriorityWeighting.class))
			{
				if ("recommended_pref".equalsIgnoreCase(strWeighting))
				{
					result = new PreferencePriorityWeighting(encoder, hintsMap);
				}
				else if ("recommended".equalsIgnoreCase(strWeighting))
					result = new OptimizedPriorityWeighting(encoder, hintsMap);
				else
					result = new FastestSafeWeighting(encoder, hintsMap);
			}
			else
				result = new FastestWeighting(encoder, hintsMap);
		}

		if (hasTimeDependentSpeed(hintsMap)) {
			if (hasConditionalSpeed(encoder, graphStorage))
				result.setSpeedCalculator(new ConditionalSpeedCalculator(result.getSpeedCalculator(), graphStorage, encoder));

			String time = hintsMap.get(hintsMap.has(RouteRequest.PARAM_DEPARTURE) ? RouteRequest.PARAM_DEPARTURE : RouteRequest.PARAM_ARRIVAL, "");
			addTrafficSpeedCalculator(result, graphStorage, time);
		}

		//FIXME: turn cost weighting should probably be enabled only at query time as in GH
		/*
		if (encoder.supports(TurnWeighting.class) && !isFootBasedFlagEncoder(encoder) && graphStorage != null && !tMode.equals(TraversalMode.NODE_BASED)) {
			Path path = Paths.get(graphStorage.getDirectory().getLocation(), "turn_costs");
			File file = path.toFile();
			if (file.exists()) {
				TurnCostExtension turnCostExt = null;
				synchronized (turnCostExtensionMap) {
					turnCostExt = turnCostExtensionMap.get(graphStorage);
					if (turnCostExt == null) {
						turnCostExt = new TurnCostExtension();
						turnCostExt.init(graphStorage, graphStorage.getDirectory());
						turnCostExtensionMap.put(graphStorage, turnCostExt);
					}
				}

				result = new TurnWeighting(result, turnCostExt);
			}
		}
		*/
		// Apply soft weightings
		if (hintsMap.getBool("custom_weightings", false))
		{
			Map<String, String> map = hintsMap.toMap();

			List<String> weightingNames = new ArrayList<>();
			for (Map.Entry<String, String> kv : map.entrySet())
			{
				String name = ProfileWeighting.decodeName(kv.getKey());
				if (name != null && !weightingNames.contains(name))
					weightingNames.add(name);
			}
	
			List<Weighting> softWeightings = new ArrayList<>();

			for (String weightingName : weightingNames) {
				switch (weightingName) {
					case "steepness_difficulty":
						softWeightings.add(new SteepnessDifficultyWeighting(encoder, getWeightingProps(weightingName, map), graphStorage));
						break;
					case "avoid_hills":
						softWeightings.add(new AvoidHillsWeighting(encoder, getWeightingProps(weightingName, map), graphStorage));
						break;
					case "green":
						softWeightings.add(new GreenWeighting(encoder, getWeightingProps(weightingName, map), graphStorage));
						break;
					case "quiet":
						softWeightings.add(new QuietWeighting(encoder, getWeightingProps(weightingName, map), graphStorage));
						break;
					case "acceleration":
						softWeightings.add(new AccelerationWeighting(encoder, getWeightingProps(weightingName, map), graphStorage));
						break;
					case "csv":
						softWeightings.add(new HeatStressWeighting(encoder, getWeightingProps(weightingName, map), graphStorage));
						break;
					case "shadow":
						softWeightings.add(new ShadowWeighting(encoder, getWeightingProps(weightingName, map), graphStorage));
						break;
					default:
						break;
				}
			}

			if (!softWeightings.isEmpty()) {
				Weighting[] arrWeightings = new Weighting[softWeightings.size()];
				arrWeightings = softWeightings.toArray(arrWeightings);
				result = new AdditionWeighting(arrWeightings, result);
			}
		}
		return result;
	}

	private boolean hasTimeDependentSpeed(HintsMap hintsMap) {
		return hintsMap.getBool(ORSParameters.Weighting.TIME_DEPENDENT_SPEED_OR_ACCESS, false);
	}

	private boolean hasConditionalSpeed(FlagEncoder encoder, GraphHopperStorage graphStorage) {
		return graphStorage.getEncodingManager().hasEncodedValue(EncodingManager.getKey(encoder, ConditionalEdges.SPEED));
	}

	public Weighting createIsochroneWeighting(HintsMap hintsMap, FlagEncoder encoder) {
        String strWeighting = hintsMap.get("weighting_method", "").toLowerCase();
        if (Helper.isEmpty(strWeighting))
            strWeighting = hintsMap.getWeighting();

        Weighting result = null;

        //Isochrones only support fastest or shortest as no path is found.
        //CalcWeight must be directly comparable to the isochrone limit

        if ("shortest".equalsIgnoreCase(strWeighting))
        {
            result = new ShortestWeighting(encoder);
        }
        else if ("fastest".equalsIgnoreCase(strWeighting)
                || "priority".equalsIgnoreCase(strWeighting)
                || "recommended_pref".equalsIgnoreCase(strWeighting)
                || "recommended".equalsIgnoreCase(strWeighting))
        {
            result = new FastestWeighting(encoder, hintsMap);
        }

        return result;
    }

	private boolean isFootBasedFlagEncoder(FlagEncoder encoder){
		return encoder instanceof FootFlagEncoder;
	}

	private PMap getWeightingProps(String weightingName, Map<String, String> map)
	{
		PMap res = new PMap();

		String prefix = "weighting_#" + weightingName;
		int n = prefix.length();
		
		for (Map.Entry<String, String> kv : map.entrySet())
		{
			String name = kv.getKey();
		    int p = name.indexOf(prefix);
		    if (p >= 0)
		    	res.put(name.substring(p + n + 1, name.length()), kv.getValue());
		}
		
		return res;
	}

	public static void addTrafficSpeedCalculator(List<Weighting> weightings, GraphHopperStorage ghStorage) {
        for (Weighting weighting : weightings)
            addTrafficSpeedCalculator(weighting, ghStorage, "");
    }

    private static void addTrafficSpeedCalculator(Weighting weighting, GraphHopperStorage ghStorage, String time) {
        TrafficGraphStorage trafficGraphStorage = GraphStorageUtils.getGraphExtension(ghStorage, TrafficGraphStorage.class);

        if (trafficGraphStorage != null) {
            RoutingTrafficSpeedCalculator routingTrafficSpeedCalculator = new RoutingTrafficSpeedCalculator(weighting.getSpeedCalculator(), ghStorage, weighting.getFlagEncoder());

            if (!time.isEmpty()) {
                //Use fixed time zone because original implementation was for German traffic data
                ZonedDateTime zdt = LocalDateTime.parse(time).atZone(ZoneId.of("Europe/Berlin"));
                routingTrafficSpeedCalculator.setZonedDateTime(zdt);
            }

            weighting.setSpeedCalculator(routingTrafficSpeedCalculator);
        }
    }
}
