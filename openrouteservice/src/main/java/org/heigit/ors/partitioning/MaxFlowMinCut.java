package org.heigit.ors.partitioning;

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

    protected static PartitioningData pData = new PartitioningData();

    private WayCategoryGraphStorage storage;
    private byte[] buffer = new byte[10];

    protected FlowNode srcNode, snkNode;
    protected boolean flooded;
    protected int nodes, visitedToken, maxFlow, maxFlowLimit;

    protected static int _dummyNodeId = -2;
    protected static int _dummyEdgeId = -2;

    protected Graph _graph;
    protected EdgeExplorer _edgeExpl;
    protected EdgeIterator _edgeIter;

    protected GraphHopperStorage _ghStorage;

    protected double limit;
    protected Set<Integer> nodeIdSet;

    protected EdgeFilter edgeFilter;

    MaxFlowMinCut(GraphHopperStorage ghStorage, EdgeFilter edgeFilter, boolean init) {
        this._ghStorage = ghStorage;
        this._graph = ghStorage.getBaseGraph();
        this._edgeExpl = _graph.createEdgeExplorer();
        storage = GraphStorageUtils.getGraphExtension(_ghStorage, WayCategoryGraphStorage.class);

        setAdditionalEdgeFilter(edgeFilter);
        if(init) {
            init();
            MaxFlowMinCutImpl maxFlowMinCut = new MaxFlowMinCutImpl(ghStorage);
            maxFlowMinCut.setAdditionalEdgeFilter(edgeFilter);
            maxFlowMinCut.setGHStorage(ghStorage);
            maxFlowMinCut.run();
        }
    }

    MaxFlowMinCut() {
    }

    private void init() {
        this.nodeIdSet = new HashSet<>();
    }

    protected void setGHStorage(GraphHopperStorage ghStorage){
        this._ghStorage = ghStorage;
        storage = GraphStorageUtils.getGraphExtension(_ghStorage, WayCategoryGraphStorage.class);
    }


    protected void initSubNetwork(double a, double b, List<Integer> sortedNodes) {
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
        this.nodeIdSet = new HashSet<>();
    }

    private void buildSrcSnkNodes() {
        this.srcNode = new FlowNode(getDummyNodeId());
        pData.setFlowNodeData(this.srcNode.id, new FlowNodeData(false, 0));
        this.snkNode = new FlowNode(getDummyNodeId());
        pData.setFlowNodeData(this.snkNode.id, new FlowNodeData(false, 0));
    }

    private void identifySrcSnkEdges(double b, List<Integer> sortedNodes) {
        this.nodes = sortedNodes.size();
        int b1 = (int) (b * nodes);
        int b2 = (int) ((1 - b) * nodes);
        this.nodeIdSet.addAll(sortedNodes);
//        pData.clearActiveEdges();
//        pData.clearVisitedNodes();

        for (int i = 0; i < nodes; i++) {
            Set<Integer> targSet = new HashSet<>();
            int nodeId = sortedNodes.get(i);
            FlowNodeData flowNodeData = pData.getFlowNodeDataOrDefault(nodeId, new FlowNodeData(false, 0));
            flowNodeData.visited = 0;
            flowNodeData.minCut = false;
            pData.setFlowNodeData(nodeId, flowNodeData);

            _edgeIter = _edgeExpl.setBaseNode(nodeId);
            FlowEdge dummy = pData.getDummyEdge(nodeId);
            FlowEdge dummyInv = dummy.inverse;
            dummy.baseNode = nodeId;
            dummy.targNode = -1;
            dummyInv.baseNode = -1;
            dummyInv.targNode = nodeId;

            FlowEdgeData dummyData = pData.getFlowEdgeData(dummy.id);
            FlowEdgeData dummyInvData = pData.getFlowEdgeData(dummyInv.id);
            dummyData.flow = dummyInvData.flow = 0;
            dummyData.active = dummyInvData.active = false;
            dummyData.capacity = dummyInvData.capacity = 0;
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


            }

//          handle Dummy-Edges of Node
            if (i < b1) {
                //>> bring DummySourceEdges to Life

                FlowEdge dummBack = pData.getDummyEdge(nodeId);
                FlowEdge dummForw = pData.getDummyEdge(nodeId).inverse;

                FlowEdgeData dummyBackData = pData.getFlowEdgeData(dummBack.id);
                FlowEdgeData dummyForwData = pData.getFlowEdgeData(dummForw.id);

                dummForw.baseNode = dummBack.targNode = srcNode.id;
                srcNode.outEdges.add(dummForw);
                dummyForwData.capacity = INFL__DUMMY_EDGE_CAPACITY;
                dummyBackData.capacity = 0;
                dummyBackData.active = true;
                pData.replaceBaseNodeFlowEdgeData(dummForw.id, srcNode.id);
//                pData.overwriteFlowEdgeData(dummForw.id, nodeId, srcNode.id, dummyForwData);

            } else if (b2 < i) {
                //>> bring DummySinkEdges to Life
                FlowEdge dummBack = pData.getDummyEdge(nodeId).inverse;
                FlowEdge dummForw = pData.getDummyEdge(nodeId);

                FlowEdgeData dummyBackData = pData.getFlowEdgeData(dummBack.id);
                FlowEdgeData dummyForwData = pData.getFlowEdgeData(dummForw.id);

                dummForw.targNode = dummBack.baseNode = snkNode.id;
                snkNode.outEdges.add(dummBack);
                dummyForwData.capacity = INFL__DUMMY_EDGE_CAPACITY;
                dummyBackData.capacity = 0;
                dummyForwData.active = true;
                pData.replaceBaseNodeFlowEdgeData(dummBack.id, snkNode.id);
//                pData.overwriteFlowEdgeData(dummBack.id, nodeId, snkNode.id, dummyBackData);

            } else {
                //>> let all other DummyEdges sleep
                FlowEdge dummyEdge = pData.getDummyEdge(nodeId);
                FlowEdge inverseDummyEdge = pData.getDummyEdge(nodeId).inverse;
                dummyEdge.targNode = inverseDummyEdge.baseNode = -1;
                pData.replaceBaseNodeFlowEdgeData(inverseDummyEdge.id, -2);


                FlowEdgeData flowEdgeData = pData.getFlowEdgeData(dummyEdge.id);
                FlowEdgeData invFlowEdgeData = pData.getFlowEdgeData(inverseDummyEdge.id);
                flowEdgeData.capacity = invFlowEdgeData.capacity = -1;
//                flowEdgeData.active = invFlowEdgeData.active = false;
            }
        }
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
