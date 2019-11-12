package com.graphhopper.routing.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;

// ORS-GH MOD - Modification by Maxim Rylov: Added a new class.
public interface WeightingFactory {
	
	public Weighting createWeighting(HintsMap hintsMap, FlagEncoder encoder, GraphHopperStorage graphStorage);
}
