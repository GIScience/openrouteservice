package com.graphhopper.storage;

import com.graphhopper.util.EdgeIteratorState;


public class GraphHelper {

    private GraphHelper() {
        // prevent instantiation
    }

    public static boolean isReversed(EdgeIteratorState edgeIteratorState) {
        if (edgeIteratorState instanceof BaseGraph.EdgeIteratorStateImpl edgeIteratorStateImpl) {
            return edgeIteratorStateImpl.reverse;
        }
        else {
            throw new IllegalArgumentException("EdgeIteratorState is not an instance of BaseGraph.EdgeIteratorStateImpl");
        }
    }
}
