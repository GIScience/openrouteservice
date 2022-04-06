package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.routing.DefaultWeightingFactory;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.ProfileWeighting;
import org.heigit.ors.routing.graphhopper.extensions.weighting.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is a preliminary adaptation of ORSWeightingFactory to the new
 * interface of GH's WeightingFactory. ORSWeightingFactory was copy-pasted
 * and modified from GH's DefaultWeightingFactory. OrsWeightingFactoryGh4
 * is meant to handle these extensions to DefaultWeightingFactory across the
 * two code bases more cleanly. The modifications in ORSWeightingFactory
 * should be transferred into OrsWeightingFactoryGh4 on need, in order
 * to figure out, which parts of ORSWeightingFactory are still needed and which
 * ones are remnants of unmaintained features.
 */
public class OrsWeightingFactoryGh4 extends DefaultWeightingFactory {
    private GraphHopperStorage graphStorage;

    public OrsWeightingFactoryGh4(GraphHopperStorage ghStorage, EncodingManager encodingManager) {
        super(ghStorage, encodingManager);
        graphStorage = ghStorage; // TODO: cleanup - this references the same storage as in super
    }

    @Override
    protected Weighting handleExternalOrsWeightings(String weightingStr, PMap hints, FlagEncoder encoder, TurnCostProvider turnCostProvider) {
        Weighting weighting = null;
        if ("priority".equalsIgnoreCase(weightingStr)) {
            weighting = new PreferencePriorityWeighting(encoder, hints);
        } else if ("recommended_pref".equalsIgnoreCase(weightingStr)) {
            if (encoder.supports(PriorityWeighting.class)) {
                weighting = new PreferencePriorityWeighting(encoder, hints, turnCostProvider);
            } else {
                weighting = new FastestWeighting(encoder, hints, turnCostProvider);
            }
        } else if ("recommended".equalsIgnoreCase(weightingStr)) {
            if (encoder.supports(PriorityWeighting.class)) {
                weighting = new OptimizedPriorityWeighting(encoder, hints, turnCostProvider);
            } else {
                weighting = new FastestWeighting(encoder, hints, turnCostProvider);
            }
        } else {
            if (encoder.supports(PriorityWeighting.class)) {
                weighting = new FastestSafeWeighting(encoder, hints); // TODO: do we need turnCostProvider here?
            } else {
                weighting = new FastestWeighting(encoder, hints); // TODO: do we need turnCostProvider here?
            }
        }

        weighting = applySoftWeightings(hints, encoder, weighting);

        return weighting;
    }

    private Weighting applySoftWeightings(PMap hints, FlagEncoder encoder, Weighting weighting) {
        // TODO (cleanup): The term "custom_weighting" is easily confused with GH's custom
        //                 weighting and should be renamed.
        if (hints.getBool("custom_weightings", false))
        {
            Map<String, Object> map = hints.toMap();

            List<String> weightingNames = new ArrayList<>();
            for (Map.Entry<String, Object> kv : map.entrySet())
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
                weighting = new AdditionWeighting(softWeightings, weighting);
            }
        }
        return weighting;
    }

    private PMap getWeightingProps(String weightingName, Map<String, Object> map)
    {
        PMap res = new PMap();

        String prefix = "weighting_#" + weightingName;
        int n = prefix.length();

        for (Map.Entry<String, Object> kv : map.entrySet())
        {
            String name = kv.getKey();
            int p = name.indexOf(prefix);
            if (p >= 0)
                res.putObject(name.substring(p + n + 1), kv.getValue());
        }

        return res;
    }
}
