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

public class OrsWeightingFactory extends DefaultWeightingFactory {
    public OrsWeightingFactory(GraphHopperStorage ghStorage, EncodingManager encodingManager) {
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
