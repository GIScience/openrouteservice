package org.heigit.ors.matrix.util;

import com.graphhopper.storage.CHGraph;

public class GraphUtils {
    public static boolean isCoreNode(CHGraph chGraph, int nodeId, int nodeCount, int coreNodeLevel) {
        if (isVirtualNode(nodeId, nodeCount))
            return false;
        return chGraph.getLevel(nodeId) >= coreNodeLevel;
    }

    private static boolean isVirtualNode(int node, int nodeCount){
        return node >= nodeCount;
    }
}
