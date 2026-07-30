package com.graphhopper.routing.querygraph;

import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

public class QueryGraphHelper {

    private QueryGraphHelper() {
        // prevent instantiation
    }

    public static EdgeIteratorState getOriginalEdgeFromVirtualEdge(EdgeIterator iter, Graph graph) {
        if (iter instanceof VirtualEdgeIterator virtualEdge) {
            EdgeIteratorState currentEdge = graph.getEdgeIteratorStateForKey(virtualEdge.getEdgeKey());
            if (currentEdge instanceof VirtualEdgeIteratorState virtualEdgeState) {
              return graph.getEdgeIteratorStateForKey(virtualEdgeState.getOriginalEdgeKey());
            }
        }
        return null;
    }
}
