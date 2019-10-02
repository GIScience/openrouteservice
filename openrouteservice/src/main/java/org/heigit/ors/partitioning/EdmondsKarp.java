package org.heigit.ors.partitioning;


import com.graphhopper.storage.GraphHopperStorage;

import java.util.*;


public class EdmondsKarp extends AbstractMaxFlowMinCutAlgorithm {

    private Map<Integer, Integer> prevMap;
    private Map<Integer, Integer> prevBaseNodeMap;


    public EdmondsKarp(GraphHopperStorage ghStorage, boolean init) {
        super(ghStorage, init);
    }

    public EdmondsKarp() {
    }

    public int getRemainingCapacity(int edgeId) {
        FlowEdgeData flowEdgeData = pData.getFlowEdgeData(edgeId);
        return flowEdgeData.capacity - flowEdgeData.flow;
    }

    public void augment(int edge, int bottleNeck) {
        FlowEdgeData flowEdgeData = pData.getFlowEdgeData(edge);
        flowEdgeData.flow += bottleNeck;
        FlowEdgeData inverseFlowEdgeData = pData.getFlowEdgeData(flowEdgeData.inverse);
        inverseFlowEdgeData.flow -= bottleNeck;
    }

    @Override
    public void flood() {
        int flow;
        prevMap = new HashMap<>();
        prevBaseNodeMap = new HashMap<>();

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
            FlowNodeData flowNodeData = pData.getFlowNodeData(nodeId);
            flowNodeData.minCut = isVisited(flowNodeData.visited);
        }

        prevMap = null;
        prevBaseNodeMap = null;
    }

    private int bfs() {
        Queue<Integer> queue = new ArrayDeque<>(nodes);
        setVisited(srcNode.id);
        queue.offer(srcNode.id);
        int node;


        while (!queue.isEmpty()) {
            Set<Integer> targSet = new HashSet<>();
            node = queue.poll();

            if (node == snkNode.id)
                break;
            if (node == srcNode.id){
                bfsSrcNode(queue);
                continue;
            }
            _edgeIter = _edgeExpl.setBaseNode(node);
            //Iterate over normal edges
            while(_edgeIter.next()){
                if(targSet.contains(_edgeIter.getAdjNode())
                        || _edgeIter.getAdjNode() == _edgeIter.getBaseNode())
                    continue;
                if(!acceptForPartitioning(_edgeIter))
                    continue;
                targSet.add(_edgeIter.getAdjNode());

                FlowEdgeData flowEdgeData = pData.getFlowEdgeData(_edgeIter.getEdge());
                if(flowEdgeData.active != true)
                    continue;
                if ((getRemainingCapacity(_edgeIter.getEdge()) > 0)
                        && !isVisited(pData.getFlowNodeData(_edgeIter.getAdjNode()).visited)) {
                    setVisited(_edgeIter.getAdjNode());
                    prevMap.put(_edgeIter.getAdjNode(), _edgeIter.getEdge());
                    prevBaseNodeMap.put(_edgeIter.getEdge(), _edgeIter.getBaseNode());
                    queue.offer(_edgeIter.getAdjNode());
                }
            }

            //do the same for the dummyedge of the node
            FlowEdge dummyEdge = pData.getDummyEdge(node);
            if(pData.getFlowEdgeData(dummyEdge.id).active != true)
                continue;
            if ((getRemainingCapacity(dummyEdge.id) > 0)
                    && !isVisited(pData.getFlowNodeData(dummyEdge.targNode).visited)
                    ) {
                setVisited(dummyEdge.targNode);
                prevMap.put(dummyEdge.targNode, dummyEdge.id);
                prevBaseNodeMap.put(dummyEdge.id, dummyEdge.baseNode);
                queue.offer(dummyEdge.targNode);
            }

        }

        if (prevMap.getOrDefault(snkNode.id, -1) == -1)
            return 0;
        int bottleNeck = Integer.MAX_VALUE;

        for (int edge = prevMap.getOrDefault(snkNode.id, -1); edge != -1; edge = prevMap.getOrDefault(prevBaseNodeMap.get(edge), -1))
            bottleNeck = Math.min(bottleNeck, getRemainingCapacity(edge));

        for (int edge = prevMap.getOrDefault(snkNode.id, -1); edge != -1; edge = prevMap.getOrDefault(prevBaseNodeMap.get(edge), -1))
            augment(edge, bottleNeck);

        return bottleNeck;
    }

    private void bfsSrcNode(Queue queue) {
        Set<Integer> targSet = new HashSet<>();

        for(FlowEdge flowEdge : srcNode.outEdges){
            if(targSet.contains(flowEdge.targNode)
                    || flowEdge.targNode == flowEdge.baseNode)
                continue;
            targSet.add(flowEdge.targNode);

            if ((getRemainingCapacity(flowEdge.id) > 0)
                    && !isVisited(pData.getFlowNodeData(flowEdge.targNode).visited)) {
                setVisited(flowEdge.targNode);
                prevMap.put(flowEdge.targNode, flowEdge.id);
                prevBaseNodeMap.put(flowEdge.id, flowEdge.baseNode);
                queue.offer(flowEdge.targNode);
            }
        }
    }
}

