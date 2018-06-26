package com.graphhopper.routing.weighting;

import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphEdgeIdFinder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.Parameters.Routing;

public class DefaultWeightingFactory implements WeightingFactory {
	
	public Weighting createWeighting(HintsMap hintsMap, TraversalMode tMode, FlagEncoder encoder, Graph gh, LocationIndex locationIndex, GraphHopperStorage graphStorage)
    {
        String weightingStr = hintsMap.getWeighting().toLowerCase();
        Weighting weighting = null;

        if (encoder.supports(GenericWeighting.class)) {
            weighting = new GenericWeighting((com.graphhopper.routing.util.DataFlagEncoder) encoder, hintsMap);
        } else if ("shortest".equalsIgnoreCase(weightingStr)) {
            weighting = new ShortestWeighting(encoder, hintsMap);
        } else if ("fastest".equalsIgnoreCase(weightingStr) || weightingStr.isEmpty()) {
            if (encoder.supports(PriorityWeighting.class))
                weighting = new PriorityWeighting(encoder, hintsMap);
            else
                weighting = new FastestWeighting(encoder, hintsMap);
        } else if ("curvature".equalsIgnoreCase(weightingStr)) {
            if (encoder.supports(CurvatureWeighting.class))
                weighting = new CurvatureWeighting(encoder, hintsMap);

        } else if ("short_fastest".equalsIgnoreCase(weightingStr)) {
            weighting = new ShortFastestWeighting(encoder, hintsMap);
        }

        if (weighting == null)
            throw new IllegalArgumentException("weighting " + weighting + " not supported");

        if (hintsMap.has(Routing.BLOCK_AREA)) {
            String blockAreaStr = hintsMap.get(Routing.BLOCK_AREA, "");
            GraphEdgeIdFinder.BlockArea blockArea = new GraphEdgeIdFinder(gh, locationIndex).
                    // MARQ24 MOD START
                    // ORG ORS0.9IMPL
                    //parseBlockArea(blockAreaStr, new DefaultEdgeFilter(encoder));
                    // copied the new code from the com.graphhopper.GraphHopper class
                    // see GraphHopper.createWeighting(...)
                    parseBlockArea(blockAreaStr, new DefaultEdgeFilter(encoder), hintsMap.getDouble("block_area.edge_id_max_area", 1000 * 1000));
                    // MARQ24 MOD END
            return new BlockAreaWeighting(weighting, blockArea);
        }
        return weighting;
    }
}
