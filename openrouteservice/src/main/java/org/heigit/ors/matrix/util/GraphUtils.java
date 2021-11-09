package org.heigit.ors.matrix.util;

import com.graphhopper.storage.CHGraph;

import java.util.ArrayList;
import java.util.List;

public class GraphUtils {
    public static boolean isCoreNode(CHGraph chGraph, int nodeId, int nodeCount, int coreNodeLevel) {
        if (isVirtualNode(nodeId, nodeCount))
            return false;
        return chGraph.getLevel(nodeId) >= coreNodeLevel;
    }

    private static boolean isVirtualNode(int node, int nodeCount) {
        return node >= nodeCount;
    }

    public static int[] getValidNodeIds(int[] nodeIds) {
        List<Integer> nodeList = new ArrayList<>();
        for (int dst : nodeIds) {
            if (dst != -1)
                nodeList.add(dst);
        }

        int[] res = new int[nodeList.size()];
        for (int i = 0; i < nodeList.size(); i++)
            res[i] = nodeList.get(i);

        return res;
    }
}
