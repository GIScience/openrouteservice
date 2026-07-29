package com.graphhopper.routing.querygraph;

import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

public class QueryGraphHelper {
    public static EdgeIteratorState getOriginalEdgeFromVirtualEdge(EdgeIterator iter, Graph graph) {
        if (iter instanceof VirtualEdgeIterator virtualEdge) {
            EdgeIteratorState currentEdge = graph.getEdgeIteratorStateForKey(virtualEdge.getEdgeKey());
            if (currentEdge instanceof VirtualEdgeIteratorState virtualEdgeState) {
                EdgeIteratorState originalEdgeIteratorState = graph.getEdgeIteratorStateForKey(virtualEdgeState.getOriginalEdgeKey());
                return originalEdgeIteratorState;
            }
        }
        return null;
    }
}
