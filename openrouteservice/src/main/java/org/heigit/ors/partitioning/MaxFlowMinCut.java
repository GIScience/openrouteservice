package org.heigit.ors.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntIntHashMap;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;


public class MaxFlowMinCut {

    PartitioningData pData;

    protected FlowNode srcNode;
    protected boolean flooded;
    protected int nodes, visitedToken, maxFlow, maxFlowLimit;
    protected int snkNodeId;

    protected static int _dummyNodeId = -2;
    protected static int _dummyEdgeId = -2;

    protected Graph _graph;
    protected EdgeExplorer _edgeExpl;
    protected EdgeIterator _edgeIter;

    protected GraphHopperStorage _ghStorage;

    protected double limit;
    protected IntHashSet nodeIdSet;
    protected IntIntHashMap explorationPreference;

    protected EdgeFilter edgeFilter;

    MaxFlowMinCut(GraphHopperStorage ghStorage, PartitioningData pData, EdgeFilter edgeFilter, boolean init) {
        this._ghStorage = ghStorage;
        this._graph = ghStorage.getBaseGraph();
        this._edgeExpl = _graph.createEdgeExplorer();
        this.pData = pData;

        setAdditionalEdgeFilter(edgeFilter);
        if(init) {
            init();
            MaxFlowMinCutImpl maxFlowMinCut = new MaxFlowMinCutImpl(ghStorage, pData);
            maxFlowMinCut.setAdditionalEdgeFilter(edgeFilter);
            maxFlowMinCut.setGHStorage(ghStorage);
            maxFlowMinCut.run();
        }
    }

    MaxFlowMinCut() {
    }

    private void init() {
        this.nodeIdSet = new IntHashSet();
    }

    protected void setGHStorage(GraphHopperStorage ghStorage){
        this._ghStorage = ghStorage;
    }


    protected void initSubNetwork(double a, double b, IntArrayList sortedNodes) {
        reset();
        initDynamics();
        buildSrcSnkNodes();
        identifySrcSnkEdges(b, sortedNodes);
    }

    protected MaxFlowMinCut setMaxFlowLimit(int prevMaxFlow) {
        this.maxFlowLimit = prevMaxFlow;
        return this;
    }

    /*###############################################################################################################*/
    //>> query call to build individual network

    protected void reset() {
        this.nodes = 0;
        this.flooded = false;
        this.maxFlow = 0;
        this.visitedToken = 1;
    }

    private void initDynamics() {
        this.nodeIdSet = new IntHashSet();
    }

    private void buildSrcSnkNodes() {
        this.srcNode = new FlowNode(getDummyNodeId());
        this.snkNodeId = getDummyNodeId();
    }

    private void identifySrcSnkEdges(double b, IntArrayList sortedNodes) {
        this.nodes = sortedNodes.size();
        int b1 = (int) (b * nodes);
        int b2 = (int) ((1 - b) * nodes);
        this.nodeIdSet.addAll(sortedNodes);

        for (int i = 0; i < nodes; i++) {
            IntHashSet targSet = new IntHashSet();
            int nodeId = sortedNodes.get(i);
            FlowNodeData flowNodeData = pData.getFlowNodeDataOrDefault(nodeId, new FlowNodeData(0));
            flowNodeData.visited = 0;
            pData.setFlowNodeData(nodeId, flowNodeData);

            _edgeIter = _edgeExpl.setBaseNode(nodeId);
            pData.setDummyTargNode(nodeId, -1);

            while (_edgeIter.next()) {
                if(targSet.contains(_edgeIter.getAdjNode())
                        || _edgeIter.getAdjNode() == _edgeIter.getBaseNode())
                    continue;
                if(!acceptForPartitioning(_edgeIter))
                    continue;
                targSet.add(_edgeIter.getAdjNode());
                //reset
                FlowEdgeData flowEdgeData = pData.getFlowEdgeData(_edgeIter.getEdge(), _edgeIter.getBaseNode());
                flowEdgeData.flow = false;
                if (nodeIdSet.contains(_edgeIter.getAdjNode()))
                    flowEdgeData.active = true;
                else flowEdgeData.active = false;
                pData.setFlowEdgeData(_edgeIter.getEdge(), _edgeIter.getBaseNode(), flowEdgeData);

            }

//          handle Dummy-Edges of Node
            if (i < b1) {
                //>> bring DummySourceEdges to Life
                pData.setDummyTargNode(nodeId, srcNode.id);
                srcNode.outNodes.add(nodeId);

            } else if (b2 < i) {
                //>> bring DummySinkEdges to Life
                pData.setDummyTargNode(nodeId, snkNodeId);
            }
        }
    }

    public void setExplorationPreference(IntArrayList nodes){
        this.explorationPreference = new IntIntHashMap();
        for(int i = 0; i < nodes.size(); i++)
            explorationPreference.put(nodes.get(i), i);
    }

    protected boolean acceptForPartitioning(EdgeIterator edgeIterator){
        return true;
//        return !(edgeIterator.getDistance() > 3000);
//            return false;
//        return edgeFilter.accept(edgeIterator);
    }

    protected boolean shouldBeLowCapacity(EdgeIterator edgeIterator){
        return false;
//        if (storage == null)
//            storage = GraphStorageUtils.getGraphExtension(_ghStorage, WayCategoryGraphStorage.class);
//        if ((storage.getEdgeValue(edgeIterator.getEdge(), buffer) & AvoidFeatureFlags.Ferries) == 0)
//            return false;
//        else
//            return true;
//        return (edgeIterator.getDistance() > 2000);
    }

    public void setAdditionalEdgeFilter(EdgeFilter edgeFilter){
        this.edgeFilter = edgeFilter;
    }


    private synchronized int getDummyNodeId() {
        return ++_dummyNodeId;
    }

    protected synchronized int getDummyEdgeId() {
        return ++_dummyEdgeId;
    }

}
