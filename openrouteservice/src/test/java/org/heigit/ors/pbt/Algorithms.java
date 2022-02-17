package org.heigit.ors.pbt;

import java.util.*;

import com.graphhopper.routing.*;
import com.graphhopper.routing.weighting.*;
import com.graphhopper.storage.*;
import org.heigit.ors.routing.graphhopper.extensions.core.*;

class Algorithms {


	static CoreALT coreALT(GraphHopperStorage graphHopperStorage, Weighting weighting) {
		QueryGraph queryGraph = new QueryGraph(graphHopperStorage.getCHGraph());
		queryGraph.lookup(Collections.emptyList());
		CoreALT coreAlgorithm = new CoreALT(queryGraph, weighting);
		CoreDijkstraFilter levelFilter = new CoreDijkstraFilter(graphHopperStorage.getCHGraph());
		coreAlgorithm.setEdgeFilter(levelFilter);
		return coreAlgorithm;
	}

}
