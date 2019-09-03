package org.heigit.ors.partitioning;


import com.graphhopper.storage.GraphHopperStorage;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;


public class EdmondsKarp extends AbstractMaxFlowMinCutAlgorithm {

    private Map<FlowNode, FlowEdge> prevMap;


    public EdmondsKarp(GraphHopperStorage ghStorage) {
        super(ghStorage);
    }

    public EdmondsKarp() {
    }

    @Override
    public void flood() {
        int flow;
        prevMap = new HashMap<>();

        do {
            setUnvisitedAll();
            flow = bfs();
            maxFlow += flow;
            if ((maxFlow > maxFlowLimit)) {
                maxFlow = Integer.MAX_VALUE;
                break;
            }
        } while (flow > 0);

        for (int nodeId : nodeIdSet) {
            FlowNode node = _nodeMap.get(nodeId);
            node.minCut = isVisited(node);
        }

        prevMap.clear();
    }

    private int bfs() {
        Queue<FlowNode> queue = new ArrayDeque<>(nodes);
        setVisited(srcNode);
        queue.offer(srcNode);

        while (!queue.isEmpty()) {
            FlowNode node = queue.poll();
            if (node == snkNode)
                break;

            for (FlowEdge edge : node.getOutEdges()) {
                if ((edge.getRemainingCapacity() > 0) && (!isVisited(edge.targNode))) {
                    setVisited(edge.targNode);
                    prevMap.put(edge.targNode, edge);
                    queue.offer(edge.targNode);
                } else {
                }
            }
        }

        if (prevMap.get(snkNode) == null)
            return 0;
        int bottleNeck = Integer.MAX_VALUE;

        for (FlowEdge edge = prevMap.get(snkNode); edge != null; edge = prevMap.get(edge.baseNode))
            bottleNeck = Math.min(bottleNeck, edge.getRemainingCapacity());

        for (FlowEdge edge = prevMap.get(snkNode); edge != null; edge = prevMap.get(edge.baseNode))
            edge.augment(bottleNeck);

        return bottleNeck;
    }
}

