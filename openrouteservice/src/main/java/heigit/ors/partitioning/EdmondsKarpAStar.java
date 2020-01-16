package heigit.ors.partitioning;


import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;

import java.util.PriorityQueue;
import java.util.Queue;

import static heigit.ors.partitioning.FastIsochroneParameters.FLOW__SET_SPLIT_VALUE;
import static heigit.ors.partitioning.FastIsochroneParameters.INFL__GRAPH_EDGE_CAPACITY;


public class EdmondsKarpAStar extends AbstractMaxFlowMinCutAlgorithm {

    private IntObjectHashMap<EdgeInfo> prevMap;

    public EdmondsKarpAStar(GraphHopperStorage ghStorage, PartitioningData pData, EdgeFilter edgeFilter, boolean init) {
        super(ghStorage, pData, edgeFilter, init);
    }

    public EdmondsKarpAStar() {
    }

    public boolean getRemainingCapacity(int edgeId, int nodeId) {
        FlowEdgeData flowEdgeData = pData.getFlowEdgeData(edgeId, nodeId);
        return !flowEdgeData.flow;
    }

    public void augment(int edgeId, int baseId, int adjId) {
        FlowEdgeData flowEdgeData = pData.getFlowEdgeData(edgeId, baseId);
        flowEdgeData.flow = true;
        FlowEdgeData inverseFlowEdgeData = pData.getFlowEdgeData(flowEdgeData.inverse, adjId);
        inverseFlowEdgeData.flow = false;
        pData.setFlowEdgeData(edgeId, baseId, flowEdgeData);
        pData.setFlowEdgeData(flowEdgeData.inverse, adjId, inverseFlowEdgeData);
    }

    @Override
    public void flood() {
        int flow;
        prevMap = new IntObjectHashMap((int)Math.ceil(FLOW__SET_SPLIT_VALUE * nodes));
        do {
            prevMap.clear();
            setUnvisitedAll();
            flow = bfs();
            maxFlow += flow;

            if ((maxFlow > maxFlowLimit)) {
                maxFlow = Integer.MAX_VALUE;
                break;
            }
        } while (flow > 0);

        prevMap = null;
    }

    private int bfs() {
        PriorityQueue<EKEdgeEntry> queue = new PriorityQueue<EKEdgeEntry>(nodes);
        EKEdgeEntry srcNodeEntry = new EKEdgeEntry(srcNode.id, 0);
        queue.offer(srcNodeEntry);
        int node;
        IntHashSet targSet = new IntHashSet();

        int calls = 0;
        double maxBFSCalls = _graph.getBaseGraph().getAllEdges().getMaxId() * 2;
        double sizeFactor = ((double)nodeIdSet.size()) / _graph.getBaseGraph().getNodes();
        maxBFSCalls = (int)Math.ceil(maxBFSCalls * sizeFactor);
        maxBFSCalls += nodeIdSet.size() * 2;

        while (!queue.isEmpty()) {
            if(calls > maxBFSCalls)
                return 0;
            EKEdgeEntry entry = queue.poll();
            node = entry.node;
            targSet.clear();

            if (node == snkNodeId)
                break;
            if (node == srcNode.id){
                calls += bfsSrcNode(queue);
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
                if ((getRemainingCapacity(edge, base))
                        && !isVisited(pData.getFlowNodeData(adj).visited)) {
                    setVisited(adj);
                    prevMap.put(adj, new EdgeInfo(edge, base, adj));
                    queue.offer(new EKEdgeEntry(adj, this.explorationPreference.get(adj)));
                }
            }

            calls++;
            //do the same for the dummyedge of the node
            FlowEdge dummyEdge = pData.getDummyEdge(node);
            if(dummyEdge.targNode != snkNodeId)
                continue;
            prevMap.put(dummyEdge.targNode, new EdgeInfo(dummyEdge.id, node, dummyEdge.targNode));
            queue.offer(new EKEdgeEntry(dummyEdge.targNode, this.explorationPreference.get(dummyEdge.targNode)));
            //Early stop
            break;

        }

        if (prevMap.getOrDefault(snkNodeId, null) == null)
            return 0;
        int bottleNeck = Integer.MAX_VALUE;

        EdgeInfo edge = prevMap.getOrDefault(snkNodeId, null);
        edge = prevMap.getOrDefault(edge.baseNode, null);
        while (edge != null){
            if(edge.baseNode == srcNode.id)
                break;
            bottleNeck = Math.min(bottleNeck, getRemainingCapacity(edge.edge, edge.baseNode) ? 1 : 0);
            if(bottleNeck == 0)
                return 0;
            edge = prevMap.getOrDefault(edge.baseNode, null);
        }

        edge = prevMap.getOrDefault(snkNodeId, null);
        edge = prevMap.getOrDefault(edge.baseNode, null);
        while (edge != null){
            if(edge.baseNode == srcNode.id)
                break;
            augment(edge.getEdge(), edge.getBaseNode(), edge.getAdjNode());
            edge = prevMap.getOrDefault(edge.getBaseNode(), null);
        }
        return bottleNeck;
    }

    private int bfsSrcNode(Queue queue) {
        final int[] outEdges = srcNode.outNodes.buffer;
        int size = srcNode.outNodes.size();
        for (int i = 0; i < size; i++){
            FlowEdge flowEdge = pData.getInverseDummyEdge(outEdges[i]);
            setVisited(flowEdge.targNode);
            if(flowEdge.targNode == srcNode.id) {
                continue;
            }
            prevMap.put(flowEdge.targNode, new EdgeInfo(flowEdge.id, srcNode.id, flowEdge.targNode));
            queue.offer(new EKEdgeEntry(flowEdge.targNode, this.explorationPreference.get(flowEdge.targNode)));
        }
        return srcNode.outNodes.size();
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

