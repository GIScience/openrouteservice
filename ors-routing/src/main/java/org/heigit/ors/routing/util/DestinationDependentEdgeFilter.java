package org.heigit.ors.routing.util;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;

public interface DestinationDependentEdgeFilter extends EdgeFilter {
    void setDestinationEdge(EdgeIteratorState edge, Graph graph, FlagEncoder encoder, TraversalMode tMode);
}