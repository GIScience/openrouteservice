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
    public OrsWeightingFactoryGh4(GraphHopperStorage ghStorage, EncodingManager encodingManager) {
        super(ghStorage, encodingManager);
    }

    @Override
    protected Weighting handleExternalOrsWeightings(String weightingStr, PMap hints, FlagEncoder encoder, TurnCostProvider turnCostProvider) {
        Weighting weighting = null;
        if (weightingStr.equalsIgnoreCase("recommended")) {
            if (encoder.supports(PriorityWeighting.class)) {
                weighting = new PriorityWeighting(encoder, hints, turnCostProvider);
            } else {
                weighting = new FastestWeighting(encoder, hints, turnCostProvider);
            }
        }
        return weighting;
    }
}
