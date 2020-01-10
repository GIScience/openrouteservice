package heigit.ors.partitioning;


import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;

import java.util.PriorityQueue;
import java.util.Queue;

import static heigit.ors.partitioning.FastIsochroneParameters.FLOW__SET_SPLIT_VALUE;


public class EdmondsKarpAStar extends AbstractMaxFlowMinCutAlgorithm {

//    private HashMap<Integer, EdgeInfo> prevMap;
    private IntObjectHashMap<EdgeInfo> prevMap;
    int calls = 0;
    int maxCalls = 0;

    public EdmondsKarpAStar(GraphHopperStorage ghStorage, EdgeFilter edgeFilter, boolean init) {
        super(ghStorage, edgeFilter, init);
    }

    public EdmondsKarpAStar() {
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
        pData.setFlowEdgeData(edgeId, baseId, flowEdgeData);
        pData.setFlowEdgeData(flowEdgeData.inverse, adjId, inverseFlowEdgeData);
    }

    @Override
    public void flood() {
        int flow;
//        maxCalls = nodeIdSet.size() * 3;
//        maxCalls = Integer.MAX_VALUE;
//        prevMap = new HashMap();
        prevMap = new IntObjectHashMap((int)Math.ceil(FLOW__SET_SPLIT_VALUE * nodes));
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
            pData.setFlowNodeData(nodeId.value, flowNodeData);
        }

        prevMap = null;
    }

    private int bfs() {
        PriorityQueue<EKEdgeEntry> queue = new PriorityQueue<EKEdgeEntry>(nodes);
        setVisited(srcNode.id);
        EKEdgeEntry srcNodeEntry = new EKEdgeEntry(srcNode.id, 0);
        queue.offer(srcNodeEntry);
        int node;
        IntHashSet targSet = new IntHashSet();

        while (!queue.isEmpty()) {
            EKEdgeEntry entry = queue.poll();
            node = entry.node;
            targSet.clear();

            if (node == snkNodeId)
                break;
            if (node == srcNode.id){
                bfsSrcNode(queue);
                continue;
            }
            _edgeIter = _edgeExpl.setBaseNode(node);
            //Iterate over normal edges
            while(_edgeIter.next()){
                calls++;
                int adj = _edgeIter.getAdjNode();
                int base = _edgeIter.getBaseNode();
                int edge = _edgeIter.getEdge();

                if(targSet.contains(adj)
                        || adj == base)
                    continue;
                targSet.add(adj);
                if(pData.getFlowEdgeData(edge, base).active != true)
                    continue;
                if ((getRemainingCapacity(edge, base) > 0)
                        && !isVisited(pData.getFlowNodeData(adj).visited)) {
                    setVisited(adj);
                    prevMap.put(adj, new EdgeInfo(edge, base, adj));
                    queue.offer(new EKEdgeEntry(adj, this.explorationPreference.get(adj)));
                }
            }

            calls++;
            //do the same for the dummyedge of the node
            FlowEdge dummyEdge = pData.getDummyEdge(node);
            if(pData.getFlowEdgeData(dummyEdge.id).active != true)
                continue;
            if ((getRemainingCapacity(dummyEdge.id) > 0)
                    && !isVisited(pData.getFlowNodeData(dummyEdge.targNode).visited)
                    ) {
                setVisited(dummyEdge.targNode);
                prevMap.put(dummyEdge.targNode, new EdgeInfo(dummyEdge.id, dummyEdge.baseNode, dummyEdge.targNode));
                queue.offer(new EKEdgeEntry(dummyEdge.targNode, this.explorationPreference.get(dummyEdge.targNode)));
                //Early stop
                if(dummyEdge.targNode == snkNodeId)
                    break;
            }

        }

        if (prevMap.getOrDefault(snkNodeId, null) == null)
            return 0;
        int bottleNeck = Integer.MAX_VALUE;

        EdgeInfo edge = prevMap.getOrDefault(snkNodeId, null);

        while (edge != null){
            bottleNeck = Math.min(bottleNeck, getRemainingCapacity(edge.edge, edge.baseNode));
            if(bottleNeck == 0)
                return 0;
            edge = prevMap.getOrDefault(edge.baseNode, null);
        }

        edge = prevMap.getOrDefault(snkNodeId, null);
        while (edge != null){
            augment(edge.getEdge(), edge.getBaseNode(), edge.getAdjNode(), bottleNeck);
            edge = prevMap.getOrDefault(edge.getBaseNode(), null);
        }
        return bottleNeck;
    }

    private void bfsSrcNode(Queue queue) {

//        for(FlowEdge flowEdge : srcNode.outEdges){
        final int[] outEdges = srcNode.outNodes.buffer;
        int size = srcNode.outNodes.size();
        for (int i = 0; i < size; i++){
            FlowEdge flowEdge = pData.getInverseDummyEdge(outEdges[i]);
            if ((getRemainingCapacity(flowEdge.id) > 0)
                    && !isVisited(pData.getFlowNodeData(flowEdge.targNode).visited)) {
                setVisited(flowEdge.targNode);
                prevMap.put(flowEdge.targNode, new EdgeInfo(flowEdge.id, flowEdge.baseNode, flowEdge.targNode));
                queue.offer(new EKEdgeEntry(flowEdge.targNode, this.explorationPreference.get(flowEdge.targNode)));
            }
        }
    }

    private class EdgeInfo{
        int edge;
        int baseNode;
        int adjNode;
        EdgeInfo(int edge, int baseNode, int adjNode){
            this.edge = edge;
            this.baseNode = baseNode;
            this.adjNode = adjNode;
        }
        public int getEdge() {
            return edge;
        }
        public int getBaseNode(){
            return baseNode;
        }
        public int getAdjNode() {
            return adjNode;
        }
    }
}

