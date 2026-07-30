package com.graphhopper.routing.querygraph;

import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;


public class QueryGraphHelper {

    private QueryGraphHelper() {
        // prevent instantiation
    }

    public static EdgeIteratorState getOriginalEdgeFromVirtualEdge(VirtualEdgeIteratorState virtualEdgeIteratorState, Graph graph) {
        return graph.getEdgeIteratorStateForKey(virtualEdgeIteratorState.getOriginalEdgeKey());
    }
}
