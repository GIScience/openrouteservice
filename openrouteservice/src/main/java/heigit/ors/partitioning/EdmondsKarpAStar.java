package heigit.ors.partitioning;


import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;

import java.util.PriorityQueue;
import java.util.Queue;

import static heigit.ors.partitioning.FastIsochroneParameters.FLOW__SET_SPLIT_VALUE;


public class EdmondsKarpAStar extends AbstractMaxFlowMinCutAlgorithm {

    private IntObjectHashMap<EdgeInfo> prevMap;
    private int srcLimit;
    private int snkLimit;

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
        srcLimit = (int) (FLOW__SET_SPLIT_VALUE * nodes);
        snkLimit = (int) ((1 - FLOW__SET_SPLIT_VALUE) * nodes);
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
        int calls = addSrcNodesToQueue(queue);
        int node;
        IntHashSet targSet = new IntHashSet();

        double maxBFSCalls = _graph.getBaseGraph().getAllEdges().getMaxId() * 2;
        double sizeFactor = ((double) nodeOrder.size()) / _graph.getBaseGraph().getNodes();
        maxBFSCalls = (int)Math.ceil(maxBFSCalls * sizeFactor) + nodeOrder.size() * 2;

        while (!queue.isEmpty()) {
            if(calls > maxBFSCalls)
                return 0;
            EKEdgeEntry entry = queue.poll();
            node = entry.node;
            targSet.clear();

            if(snkLimit < nodeOrder.get(node)){
                prevMap.put(snkNodeId, new EdgeInfo(getDummyEdgeId(), node, snkNodeId));
                //Early stop
                break;
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
                if(!nodeOrder.containsKey(adj))
                    continue;
                if ((getRemainingCapacity(edge, base))
                        && !isVisited(pData.getVisited(adj))) {
                    setVisited(adj);
                    prevMap.put(adj, new EdgeInfo(edge, base, adj));
                    queue.offer(new EKEdgeEntry(adj, this.nodeOrder.get(adj)));
                }
            }
            calls++;
        }

        if (prevMap.getOrDefault(snkNodeId, null) == null)
            return 0;
        int bottleNeck = Integer.MAX_VALUE;

        EdgeInfo edge = prevMap.getOrDefault(snkNodeId, null);
        edge = prevMap.getOrDefault(edge.baseNode, null);
        while (edge != null){
            if(nodeOrder.get(edge.baseNode) < srcLimit)
                break;
            bottleNeck = Math.min(bottleNeck, getRemainingCapacity(edge.edge, edge.baseNode) ? 1 : 0);
            if(bottleNeck == 0)
                return 0;
            augment(edge.getEdge(), edge.getBaseNode(), edge.getAdjNode());
            edge = prevMap.getOrDefault(edge.baseNode, null);
        }
        return bottleNeck;
    }

    private int addSrcNodesToQueue(Queue queue){
        int nodeNumber = 0;
        while(nodeNumber < srcLimit) {
            queue.offer(new EKEdgeEntry(orderedNodes.get(nodeNumber), nodeNumber));
            nodeNumber++;
        }
        return srcLimit;
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

