package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.TreeSet;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.getSplitValue;

/**
 * EdmondsKarp implementation of the maxflow algorithm using a deque.
 * Finds the maximum number of possible paths from the source region to the sink region.
 * The visit order is determined by the projection order. Nodes closer to the sink region are expanded first.
 *
 * @author Hendrik Leuschner
 */
public class EdmondsKarpAStar extends MaxFlowMinCut {
    private int srcLimit;
    private int snkLimit;

    public EdmondsKarpAStar(Graph graph, PartitioningData pData, EdgeFilter edgeFilter) {
        super(graph, pData, edgeFilter);
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

    /**
     * Iterate the search until no more connections can be found.
     */
    @Override
    public void flood() {
        int flow;
        srcLimit = (int) (getSplitValue() * nodes);
        snkLimit = (int) ((1 - getSplitValue()) * nodes);
        Deque<Integer> deque = new ArrayDeque<>(nodes / 2);
        addSrcNodesToDeque(deque);
        do {
            setUnvisitedAll();
            flow = search(deque);
            maxFlow += flow;

            if ((maxFlow > maxFlowLimit)) {
                maxFlow = Integer.MAX_VALUE;
                break;
            }
        } while (flow > 0);
    }

    /**
     * Search for a connection between source and sink set. Order of node expansion given by distance to sink set.
     * @param initialDeque Deque for source set. Invariable and thus only calculated once.
     * @return 1 while connection found. 0 otherwise.
     */
    private int search(Deque<Integer> initialDeque) {
        IntObjectHashMap<EdgeInfo> prevMap = new IntObjectHashMap((int) Math.ceil(0.1 * nodes));
        Deque<Integer> deque = copyInitialDeque(initialDeque);
        int calls = srcLimit;
        int node;

        double maxBFSCalls = graph.getBaseGraph().getAllEdges().length() * 2;
        double sizeFactor = ((double) nodeOrder.size()) / graph.getBaseGraph().getNodes();
        maxBFSCalls = (int) Math.ceil(maxBFSCalls * sizeFactor) + nodeOrder.size() * 2;

        while (!deque.isEmpty()) {
            if (calls > maxBFSCalls)
                return 0;
            node = deque.pop();

            if (snkLimit < nodeOrder.get(node)) {
                prevMap.put(snkNodeId, new EdgeInfo(getDummyEdgeId(), node, snkNodeId));
                //Early stop
                break;
            }

            edgeIterator = edgeExplorer.setBaseNode(node);
            TreeSet<EKEdgeEntry> set = new TreeSet<>(EKEdgeEntry::compareTo);
            while (edgeIterator.next()) {
                if (!edgeFilter.accept(edgeIterator))
                    continue;
                calls++;
                int adj = edgeIterator.getAdjNode();
                int edge = edgeIterator.getEdge();

                if (adj == node)
                    continue;
                if (!nodeOrder.containsKey(adj))
                    continue;
                if ((getRemainingCapacity(edge, node))
                        && !isVisited(pData.getVisited(adj))) {
                    setVisited(adj);
                    prevMap.put(adj, new EdgeInfo(edge, node, adj));
                    set.add(new EKEdgeEntry(adj, this.nodeOrder.get(adj)));
                }
            }
            for (EKEdgeEntry ekEdgeEntry : set)
                deque.push(ekEdgeEntry.node);
            calls++;
        }

        if (prevMap.getOrDefault(snkNodeId, null) == null)
            return 0;
        int bottleNeck = Integer.MAX_VALUE;

        EdgeInfo edge = prevMap.getOrDefault(snkNodeId, null);
        edge = prevMap.getOrDefault(edge.baseNode, null);
        while (edge != null) {
            if (nodeOrder.get(edge.baseNode) < srcLimit)
                break;
            bottleNeck = Math.min(bottleNeck, getRemainingCapacity(edge.edge, edge.baseNode) ? 1 : 0);
            if (bottleNeck == 0)
                return 0;
            augment(edge.getEdge(), edge.getBaseNode(), edge.getAdjNode());
            edge = prevMap.getOrDefault(edge.baseNode, null);
        }
        return bottleNeck;
    }

    /**
     * Create source deque.
     * @param deque The deque to which source nodes are to be added.
     */
    private void addSrcNodesToDeque(Deque<Integer> deque) {
        //Reverse insertion order to maximize offer performance
        int nodeNumber = 0;
        while (nodeNumber < srcLimit) {
            int node = orderedNodes.get(nodeNumber);
            deque.push(node);
            setVisited(node);
            nodeNumber++;
        }
    }

    private Deque<Integer> copyInitialDeque(Deque<Integer> initialDeque) {
        Deque<Integer> deque = new ArrayDeque<>(initialDeque);
        //Reverse insertion order to maximize offer performance
        int nodeNumber = srcLimit - 1;
        while (nodeNumber > 0) {
            int node = orderedNodes.get(nodeNumber);
            setVisited(node);
            nodeNumber--;
        }
        return deque;
    }

    private class EdgeInfo {
        int edge;
        int baseNode;
        int adjNode;

        EdgeInfo(int edge, int baseNode, int adjNode) {
            this.edge = edge;
            this.baseNode = baseNode;
            this.adjNode = adjNode;
        }

        public int getEdge() {
            return edge;
        }

        public int getBaseNode() {
            return baseNode;
        }

        public int getAdjNode() {
            return adjNode;
        }
    }
}

