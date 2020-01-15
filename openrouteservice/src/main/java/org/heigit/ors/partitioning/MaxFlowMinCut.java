package org.heigit.ors.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntIntHashMap;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import heigit.ors.routing.AvoidFeatureFlags;
import heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.WayCategoryGraphStorage;
import io.swagger.annotations.OAuth2Definition;

import static org.heigit.ors.partitioning.FastIsochroneParameters.*;
import java.util.*;

public class MaxFlowMinCut {

    private WayCategoryGraphStorage storage;
    PartitioningData pData;
    private byte[] buffer = new byte[10];

    protected FlowNode srcNode, snkNode;
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
        storage = GraphStorageUtils.getGraphExtension(_ghStorage, WayCategoryGraphStorage.class);

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
        storage = GraphStorageUtils.getGraphExtension(_ghStorage, WayCategoryGraphStorage.class);
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
        pData.setFlowNodeData(this.srcNode.id, new FlowNodeData(false, 0));
        this.snkNodeId = getDummyNodeId();
        pData.setFlowNodeData(this.snkNodeId, new FlowNodeData(false, 0));
    }

    private void identifySrcSnkEdges(double b, IntArrayList sortedNodes) {
        this.nodes = sortedNodes.size();
        int b1 = (int) (b * nodes);
        int b2 = (int) ((1 - b) * nodes);
        this.nodeIdSet.addAll(sortedNodes);

        for (int i = 0; i < nodes; i++) {
            IntHashSet targSet = new IntHashSet();
            int nodeId = sortedNodes.get(i);
            FlowNodeData flowNodeData = pData.getFlowNodeDataOrDefault(nodeId, new FlowNodeData(false, 0));
            flowNodeData.visited = 0;
            flowNodeData.minCut = false;
            pData.setFlowNodeData(nodeId, flowNodeData);

            _edgeIter = _edgeExpl.setBaseNode(nodeId);

            FlowEdge dummy = pData.getDummyEdge(nodeId);
            FlowEdge dummyInv = pData.getInverseDummyEdge(nodeId);
            pData.setDummyBaseNode(nodeId, nodeId);
            pData.setDummyTargNode(nodeId, -1);
            FlowEdgeData dummyData = pData.getFlowEdgeData(dummy.id);
            FlowEdgeData dummyInvData = pData.getFlowEdgeData(dummyInv.id);
            dummyData.flow = dummyInvData.flow = 0;
            dummyData.active = dummyInvData.active = false;
            dummyData.capacity = dummyInvData.capacity = 0;
            pData.setDummyFlowEdgeData(dummy.id,  dummyData);
            pData.setDummyFlowEdgeData(dummyInv.id, dummyInvData);

            while (_edgeIter.next()) {
                if(targSet.contains(_edgeIter.getAdjNode())
                        || _edgeIter.getAdjNode() == _edgeIter.getBaseNode())
                    continue;
                if(!acceptForPartitioning(_edgeIter))
                    continue;
                targSet.add(_edgeIter.getAdjNode());
                //reset
                FlowEdgeData flowEdgeData = pData.getFlowEdgeData(_edgeIter.getEdge(), _edgeIter.getBaseNode());
                flowEdgeData.flow = 0;
                if (nodeIdSet.contains(_edgeIter.getAdjNode()))
                    flowEdgeData.active = true;
                else flowEdgeData.active = false;
                pData.setFlowEdgeData(_edgeIter.getEdge(), _edgeIter.getBaseNode(), flowEdgeData);


            }

//          handle Dummy-Edges of Node
            if (i < b1) {
                //>> bring DummySourceEdges to Life

                FlowEdge dummBack = pData.getDummyEdge(nodeId);
                FlowEdge dummForw = pData.getInverseDummyEdge(nodeId);

                FlowEdgeData dummyBackData = pData.getFlowEdgeData(dummBack.id);
                FlowEdgeData dummyForwData = pData.getFlowEdgeData(dummForw.id);

//                dummForw.baseNode = dummBack.targNode = srcNode.id;
                dummForw.baseNode = srcNode.id;
                pData.setDummyTargNode(nodeId, srcNode.id);
//                srcNode.outEdges.add(dummForw.id);
                srcNode.outNodes.add(nodeId);
                dummyForwData.capacity = INFL__DUMMY_EDGE_CAPACITY;
                dummyBackData.capacity = 0;
                dummyBackData.active = true;
                pData.setDummyFlowEdgeData(dummBack.id,  dummyBackData);
                pData.setDummyFlowEdgeData(dummForw.id, dummyForwData);
                pData.replaceBaseNodeFlowEdgeData(dummForw.id, srcNode.id);
//                pData.overwriteFlowEdgeData(dummForw.id, nodeId, srcNode.id, dummyForwData);

            } else if (b2 < i) {
                //>> bring DummySinkEdges to Life
                FlowEdge dummForw = pData.getDummyEdge(nodeId);
                FlowEdge dummBack = pData.getInverseDummyEdge(nodeId);
                FlowEdgeData dummyBackData = pData.getFlowEdgeData(dummBack.id);
                FlowEdgeData dummyForwData = pData.getFlowEdgeData(dummForw.id);

//                dummForw.targNode = dummBack.baseNode = snkNode.id;
                dummBack.baseNode = snkNodeId;
                pData.setDummyTargNode(nodeId, snkNodeId);
//                snkNode.outEdges.add(dummBack.id);
                dummyForwData.capacity = INFL__DUMMY_EDGE_CAPACITY;
                dummyBackData.capacity = 0;
                dummyForwData.active = true;
                pData.setDummyFlowEdgeData(dummBack.id,  dummyBackData);
                pData.setDummyFlowEdgeData(dummForw.id, dummyForwData);
                pData.replaceBaseNodeFlowEdgeData(dummBack.id, snkNodeId);
//                pData.overwriteFlowEdgeData(dummBack.id, nodeId, snkNode.id, dummyBackData);

            } else {
                //>> let all other DummyEdges sleep
                FlowEdge dummyEdge = pData.getDummyEdge(nodeId);
                FlowEdge inverseDummyEdge = pData.getInverseDummyEdge(nodeId);
                pData.setDummyTargNode(nodeId, -1);
//                dummyEdge.targNode = inverseDummyEdge.baseNode = -1;
                pData.replaceBaseNodeFlowEdgeData(inverseDummyEdge.id, -2);


                FlowEdgeData flowEdgeData = pData.getFlowEdgeData(dummyEdge.id);
                FlowEdgeData invFlowEdgeData = pData.getFlowEdgeData(inverseDummyEdge.id);
                flowEdgeData.capacity = invFlowEdgeData.capacity = -1;
                pData.setDummyFlowEdgeData(dummyEdge.id,  flowEdgeData);
                pData.setDummyFlowEdgeData(inverseDummyEdge.id, invFlowEdgeData);
//                flowEdgeData.active = invFlowEdgeData.active = false;
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
