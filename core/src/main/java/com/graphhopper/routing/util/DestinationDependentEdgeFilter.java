package com.graphhopper.routing.util;

import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;

public interface DestinationDependentEdgeFilter extends EdgeFilter
{
    void setDestinationEdge(EdgeIteratorState edge, Graph graph, FlagEncoder encoder, TraversalMode tMode);
}