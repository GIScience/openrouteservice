package com.graphhopper.storage;

import com.graphhopper.util.EdgeIterator;

public class GraphHelper {

    private GraphHelper() {
        // prevent instantiation
    }

    public static boolean isReversed(EdgeIterator iter) {
        try {
            return ((BaseGraph.EdgeIteratorImpl) iter).reverse;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
