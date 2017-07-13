/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.routing.graphhopper.extensions;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heigit.ors.routing.ProfileWeighting;
import heigit.ors.routing.graphhopper.extensions.weighting.*;
import heigit.ors.routing.traffic.RealTrafficDataProvider;

import com.graphhopper.routing.weighting.DefaultWeightingFactory;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.TurnCostExtension;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;

public class ORSWeightingFactory extends DefaultWeightingFactory {

	private RealTrafficDataProvider m_trafficDataProvider;
	private Map<Object, TurnCostExtension> m_turnCostExtensions;

	public ORSWeightingFactory(RealTrafficDataProvider trafficProvider)
	{
		m_trafficDataProvider = trafficProvider;
		m_turnCostExtensions = new HashMap<Object, TurnCostExtension>();
	}

	public Weighting createWeighting(HintsMap hintsMap, FlagEncoder encoder, Graph graph, LocationIndex locationIndex, GraphHopperStorage graphStorage) {
		String weighting = hintsMap.get("weighting_method", "").toLowerCase();
		if (Helper.isEmpty(weighting))
			weighting = hintsMap.getWeighting();

		Weighting result = null;

		if ("shortest".equalsIgnoreCase(weighting))
		{
			result = new ShortestWeighting(encoder); 
		}
		else if ("fastest".equalsIgnoreCase(weighting))
		{
			result = new FastestWeighting(encoder, hintsMap);
		}
		else  if ("priority".equalsIgnoreCase(weighting))
		{
			result = new PreferencePriorityWeighting(encoder, hintsMap);
		} 
		else 
		{
			if (encoder.supports(PriorityWeighting.class))
			{
				if ("recommended_pref".equalsIgnoreCase(weighting))
				{
					result = new PreferencePriorityWeighting(encoder, hintsMap);
				}
				else if ("recommended".equalsIgnoreCase(weighting))

					result = new OptimizedPriorityWeighting(encoder, hintsMap);
				else
					result = new FastestSafeWeighting(encoder, hintsMap);
			}
			else
				result = new FastestWeighting(encoder, hintsMap);
		} 

		if (hintsMap.getBool("weighting_traffic_block", false))
		{
			//String strPref = weighting.substring(weighting.indexOf("-") + 1);
			result = new TrafficAvoidWeighting(result, encoder, m_trafficDataProvider.getAvoidEdges(graphStorage));
		}

		if (encoder.supports(TurnWeighting.class) && !(encoder instanceof FootFlagEncoder) && graphStorage != null) {
			Path path = Paths.get(graphStorage.getDirectory().getLocation(), "turn_costs");
			File file = path.toFile();
			if (file.exists()) {
				TurnCostExtension turnCostExt = null;
				synchronized (m_turnCostExtensions) {
					turnCostExt = m_turnCostExtensions.get(graphStorage);
					if (turnCostExt == null) {
						turnCostExt = new TurnCostExtension();
						turnCostExt.init(graphStorage, graphStorage.getDirectory());
						m_turnCostExtensions.put(graphStorage, turnCostExt);
					}
				}

				result = new TurnWeighting(result, turnCostExt);
			}
		}

		// Apply soft weightings
		if (hintsMap.getBool("custom_weightings", false))
		{
			Map<String, String> map = hintsMap.getMap();

			List<String> weightingNames = new ArrayList<String>();
			for (Map.Entry<String, String> kv : map.entrySet())
			{
				String name = ProfileWeighting.decodeName(kv.getKey());
				if (name != null && !weightingNames.contains(name))
					weightingNames.add(name);
			}

			List<Weighting> softWeightings = new ArrayList<Weighting>();

			for (int i = 0; i < weightingNames.size(); i++)
			{
				String weightingName = weightingNames.get(i);

				switch(weightingName)
				{
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
				}
			}

			if (softWeightings.size() > 0)
			{
				Weighting[] arrWeightings = new Weighting[softWeightings.size()];
				arrWeightings = softWeightings.toArray(arrWeightings);
				result = new AdditionWeighting(arrWeightings, result, encoder, hintsMap, graphStorage);
			}
		}

		return result;
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
