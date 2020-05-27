package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntIntHashMap;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

/**
 * Abstract MaxFlowMinCut implementation.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public abstract class MaxFlowMinCut {
    protected static int _dummyNodeId = -2;
    protected static int _dummyEdgeId = -2;
    protected boolean flooded;
    protected int nodes, visitedToken, maxFlow, maxFlowLimit, snkNodeId;
    protected double limit;
    protected Graph graph;
    protected EdgeExplorer edgeExplorer;
    protected EdgeIterator edgeIterator;
    protected IntIntHashMap nodeOrder;
    protected IntArrayList orderedNodes;
    protected EdgeFilter edgeFilter;
    PartitioningData pData;

    MaxFlowMinCut(Graph graph, PartitioningData pData, EdgeFilter edgeFilter) {
        this.graph = graph;
        this.edgeExplorer = this.graph.createEdgeExplorer();
        this.pData = pData;

        setAdditionalEdgeFilter(edgeFilter);
    }

    protected void initSubNetwork() {
        reset();
        this.snkNodeId = getDummyNodeId();
        identifySrcSnkEdges();
    }

    protected MaxFlowMinCut setMaxFlowLimit(int prevMaxFlow) {
        this.maxFlowLimit = prevMaxFlow;
        return this;
    }

    protected void reset() {
        this.nodes = 0;
        this.flooded = false;
        this.maxFlow = 0;
        this.visitedToken = 1;
    }

    public void setVisited(int node) {
        pData.setVisited(node, visitedToken);
    }

    public boolean isVisited(int visited) {
        return (visited == visitedToken);
    }

    public void setUnvisitedAll() {
        ++this.visitedToken;
    }

    public int getMaxFlow() {
        execute();
        return maxFlow;
    }

    /**
     * Execute the flooding of the flow graph and determine source sink sets from visited attribute.
     *
     * @return the bi partition
     */
    public BiPartition calcNodePartition() {
        IntHashSet srcSet = new IntHashSet();
        IntHashSet snkSet = new IntHashSet();

        execute();
        for (int nodeId : nodeOrder.keys) {
            if (isVisited(pData.getVisited(nodeId)))
                srcSet.add(nodeId);
            else
                snkSet.add(nodeId);
        }
        return new BiPartition(srcSet, snkSet);
    }

    private void execute() {
        if (flooded)
            return;

        this.flooded = true;
        flood();
    }

    public abstract void flood();

    /**
     * Set flow data entries for given nodes
     */
    private void identifySrcSnkEdges() {
        this.nodes = orderedNodes.size();
        for (int i = 0; i < nodes; i++) {
            IntHashSet targSet = new IntHashSet();
            int nodeId = orderedNodes.get(i);

            pData.setVisited(nodeId, 0);

            edgeIterator = edgeExplorer.setBaseNode(nodeId);
            while (edgeIterator.next()) {
                if (targSet.contains(edgeIterator.getAdjNode())
                        || edgeIterator.getAdjNode() == edgeIterator.getBaseNode())
                    continue;
                if (!acceptForPartitioning(edgeIterator))
                    continue;
                targSet.add(edgeIterator.getAdjNode());
                //reset
                FlowEdgeData flowEdgeData = pData.getFlowEdgeData(edgeIterator.getEdge(), edgeIterator.getBaseNode());
                flowEdgeData.flow = false;
                pData.setFlowEdgeData(edgeIterator.getEdge(), edgeIterator.getBaseNode(), flowEdgeData);
            }
        }
    }

    public void setNodeOrder() {
        this.nodeOrder = new IntIntHashMap();
        for (int i = 0; i < orderedNodes.size(); i++)
            nodeOrder.put(orderedNodes.get(i), i);
    }

    public void setOrderedNodes(IntArrayList orderedNodes) {
        this.orderedNodes = orderedNodes;
    }

    protected boolean acceptForPartitioning(EdgeIterator edgeIterator) {
        return edgeFilter.accept(edgeIterator);
    }

    public void setAdditionalEdgeFilter(EdgeFilter edgeFilter) {
        this.edgeFilter = edgeFilter;
    }

    private synchronized int getDummyNodeId() {
        return ++_dummyNodeId;
    }

    protected synchronized int getDummyEdgeId() {
        return ++_dummyEdgeId;
    }
}
