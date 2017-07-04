package com.graphhopper.routing.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;

public interface WeightingFactory {
	
	public Weighting createWeighting(HintsMap hintsMap, FlagEncoder encoder, Graph graph, LocationIndex index, GraphHopperStorage graphStorage);
}
