package org.heigit.ors.partitioning;


import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;

import java.util.*;


public class EdmondsKarp extends AbstractMaxFlowMinCutAlgorithm {

    private IntIntHashMap prevMap;
    private IntIntHashMap prevBaseNodeMap;
    private IntIntHashMap prevAdjNodeMap;



    public EdmondsKarp(GraphHopperStorage ghStorage, EdgeFilter edgeFilter, boolean init) {
        super(ghStorage, edgeFilter, init);
    }

    public EdmondsKarp() {
    }

    public int getRemainingCapacity(int edgeId, int nodeId) {
        FlowEdgeData flowEdgeData = pData.getFlowEdgeData(edgeId, nodeId);
        return flowEdgeData.capacity - flowEdgeData.flow;
    }

    public int getRemainingCapacity(int edgeId) {
        FlowEdgeData flowEdgeData = pData.getFlowEdgeData(edgeId);
        return flowEdgeData.capacity - flowEdgeData.flow;
    }

    public void augment(int edgeId, int baseId, int adjId, int bottleNeck) {
        FlowEdgeData flowEdgeData = pData.getFlowEdgeData(edgeId, baseId);
        flowEdgeData.flow += bottleNeck;
        FlowEdgeData inverseFlowEdgeData = pData.getFlowEdgeData(flowEdgeData.inverse, adjId);
        inverseFlowEdgeData.flow -= bottleNeck;
    }

    @Override
    public void flood() {
        int flow;
        prevMap = new IntIntHashMap();
        prevBaseNodeMap = new IntIntHashMap();
        prevAdjNodeMap = new IntIntHashMap();

        do {
            setUnvisitedAll();
            flow = bfs();
            maxFlow += flow;
            if ((maxFlow > maxFlowLimit)) {
                maxFlow = Integer.MAX_VALUE;
                break;
            }
        } while (flow > 0);

        for (IntCursor nodeId : nodeIdSet) {
            FlowNodeData flowNodeData = pData.getFlowNodeData(nodeId.value);
            flowNodeData.minCut = isVisited(flowNodeData.visited);
        }

        prevMap = null;
        prevBaseNodeMap = null;
        prevAdjNodeMap = null;
    }

    private int bfs() {
        Queue<Integer> queue = new ArrayDeque<>(nodes);
        setVisited(srcNode.id);
        queue.offer(srcNode.id);
        int node;


        while (!queue.isEmpty()) {
            IntHashSet targSet = new IntHashSet();
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

                FlowEdgeData flowEdgeData = pData.getFlowEdgeData(_edgeIter.getEdge(), _edgeIter.getBaseNode());
                if(flowEdgeData.active != true)
                    continue;
                if ((getRemainingCapacity(_edgeIter.getEdge(), _edgeIter.getBaseNode()) > 0)
                        && !isVisited(pData.getFlowNodeData(_edgeIter.getAdjNode()).visited)) {
                    setVisited(_edgeIter.getAdjNode());
                    prevMap.put(_edgeIter.getAdjNode(), _edgeIter.getEdge());
                    prevBaseNodeMap.put(_edgeIter.getEdge(), _edgeIter.getBaseNode());
                    prevAdjNodeMap.put(_edgeIter.getEdge(), _edgeIter.getAdjNode());
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
                prevAdjNodeMap.put(dummyEdge.id, dummyEdge.targNode);
                queue.offer(dummyEdge.targNode);
            }

        }

        if (prevMap.getOrDefault(snkNode.id, -1) == -1)
            return 0;
        int bottleNeck = Integer.MAX_VALUE;

        int edge = prevMap.getOrDefault(snkNode.id, -1);
        while (edge != -1){
            int baseNode = prevBaseNodeMap.get(edge);
            bottleNeck = Math.min(bottleNeck, getRemainingCapacity(edge, baseNode));
            edge = prevMap.getOrDefault(prevBaseNodeMap.get(edge), -1);
        }

        edge = prevMap.getOrDefault(snkNode.id, -1);
        while (edge != -1){
            int baseNode = prevBaseNodeMap.get(edge);
            int adjNode = prevAdjNodeMap.get(edge);
            augment(edge, baseNode, adjNode, bottleNeck);
            edge = prevMap.getOrDefault(prevBaseNodeMap.get(edge), -1);
        }
//        for (int edge = prevMap.getOrDefault(snkNode.id, -1); edge != -1; edge = prevMap.getOrDefault(prevBaseNodeMap.get(edge), -1))
//            bottleNeck = Math.min(bottleNeck, getRemainingCapacity(edge, prevBaseNodeMap.get(edge)));

//        for (int edge = prevMap.getOrDefault(snkNode.id, -1); edge != -1; edge = prevMap.getOrDefault(prevBaseNodeMap.get(edge), -1))
//            augment(edge, bottleNeck);

        return bottleNeck;
    }

    private void bfsSrcNode(Queue queue) {

        for(FlowEdge flowEdge : srcNode.outEdges){
            if ((getRemainingCapacity(flowEdge.id) > 0)
                    && !isVisited(pData.getFlowNodeData(flowEdge.targNode).visited)) {
                setVisited(flowEdge.targNode);
                prevMap.put(flowEdge.targNode, flowEdge.id);
                prevBaseNodeMap.put(flowEdge.id, flowEdge.baseNode);
                prevAdjNodeMap.put(flowEdge.id, flowEdge.targNode);
                queue.offer(flowEdge.targNode);
            }
        }
    }
}

