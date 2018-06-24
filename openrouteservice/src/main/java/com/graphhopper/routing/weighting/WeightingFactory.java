package com.graphhopper.routing.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;

// MARQ24 MOD ADDED CLASS
public interface WeightingFactory {
	
	public Weighting createWeighting(HintsMap hintsMap, TraversalMode tMode, FlagEncoder encoder, Graph graph, LocationIndex index, GraphHopperStorage graphStorage);
}
