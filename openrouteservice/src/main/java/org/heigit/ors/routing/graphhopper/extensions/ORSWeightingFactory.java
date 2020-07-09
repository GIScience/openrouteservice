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

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.*;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.TurnCostExtension;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;
import org.heigit.ors.routing.ProfileWeighting;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.weighting.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
					default:
						break;
				}
			}

			if (!softWeightings.isEmpty()) {
				Weighting[] arrWeightings = new Weighting[softWeightings.size()];
				arrWeightings = softWeightings.toArray(arrWeightings);
				result = new AdditionWeighting(arrWeightings, result, encoder);
			}
		}
		return result;
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
}
