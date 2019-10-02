package org.heigit.ors.partitioning;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import io.swagger.annotations.OAuth2Definition;

import static org.heigit.ors.partitioning.FastIsochroneParameters.*;
import java.util.*;

public class MaxFlowMinCut {

    protected static PartitioningData pData = new PartitioningData();

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

    MaxFlowMinCut(GraphHopperStorage ghStorage, boolean init) {
        this._ghStorage = ghStorage;
        this._graph = ghStorage.getBaseGraph();
        this._edgeExpl = _graph.createEdgeExplorer();
        if(init) {
            init();
            MaxFlowMinCutImpl maxFlowMinCut = new MaxFlowMinCutImpl(ghStorage);
            maxFlowMinCut.run();
        }
    }

    MaxFlowMinCut() {
    }

    private void init() {
        this.nodeIdSet = new HashSet<>();
    }


    protected void initSubNetwork(double a, double b, List<Integer> sortedNodes) {
        reset();
        initDynamics();
        buildSrcSnkNodes();
        identifySrcSnkEdges(a, b, sortedNodes);
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

    private void identifySrcSnkEdges(double a, double b, List<Integer> sortedNodes) {
        this.nodes = sortedNodes.size();
        this.nodeIdSet.addAll(sortedNodes);
        pData.clearActiveEdges();
        pData.clearVisitedNodes();

        for (int i = 0; i < nodes; i++) {
            Set<Integer> targSet = new HashSet<>();
            int nodeId = sortedNodes.get(i);
            FlowNodeData flowNodeData = pData.getFlowNodeDataOrDefault(nodeId, new FlowNodeData(false, 0));
            flowNodeData.visited = 0;
            flowNodeData.minCut = false;
            pData.setFlowNodeData(nodeId, flowNodeData);

            _edgeIter = _edgeExpl.setBaseNode(nodeId);
            while (_edgeIter.next()) {
                if(targSet.contains(_edgeIter.getAdjNode())
                        || _edgeIter.getAdjNode() == _edgeIter.getBaseNode())
                    continue;
                if(!acceptForPartitioning(_edgeIter))
                    continue;
                targSet.add(_edgeIter.getAdjNode());
                //reset
                FlowEdgeData flowEdgeData = pData.getFlowEdgeData(_edgeIter.getEdge());
                flowEdgeData.flow = 0;
                if (nodeIdSet.contains(_edgeIter.getAdjNode()))
                    flowEdgeData.active = true;
                else flowEdgeData.active = false;
            }

//          handle Dummy-Edges of Node
            if ((int) (a * nodes) <= i && i < (int) (b * nodes)) {
                //>> bring DummySourceEdges to Life

                FlowEdge dummBack = pData.getDummyEdge(nodeId);
                FlowEdge dummForw = pData.getDummyEdge(nodeId).inverse;

                FlowEdgeData dummyBackData = pData.getFlowEdgeData(dummBack.id);
                FlowEdgeData dummyForwData = pData.getFlowEdgeData(dummForw.id);

                dummForw.baseNode = dummBack.targNode = srcNode.id;
//                PartitioningData.dummyEdges.put(nodeId, dummBack);

                srcNode.outEdges.add(dummForw);
                dummyBackData.capacity = 0;
                dummyBackData.active = true;
                dummyForwData.capacity = INFL__DUMMY_EDGE_CAPACITY;

            } else if ((int) ((1 - b) * nodes) < i && i <= (int) ((1 - a) * nodes)) {
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
            } else {
                //>> let all other DummyEdges sleep
                FlowEdge dummyEdge = pData.getDummyEdge(nodeId);
                FlowEdge inverseDummyEdge = pData.getDummyEdge(nodeId).inverse;
                dummyEdge.targNode = inverseDummyEdge.baseNode = -1;
                dummyEdge.inverse = inverseDummyEdge;
                inverseDummyEdge.inverse = dummyEdge;

                FlowEdgeData flowEdgeData = pData.getFlowEdgeData(dummyEdge.id);
                FlowEdgeData invFlowEdgeData = pData.getFlowEdgeData(inverseDummyEdge.id);
                flowEdgeData.capacity = invFlowEdgeData.capacity = -1;
            }
        }
    }

    protected boolean acceptForPartitioning(EdgeIterator edgeIterator){
        if(edgeIterator.getDistance() > 5000)
            return false;
        return true;
    }


    private synchronized int getDummyNodeId() {
        return ++_dummyNodeId;
    }

    protected synchronized int getDummyEdgeId() {
        return ++_dummyEdgeId;
    }

}
