package org.heigit.ors.partitioning;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;

import static org.heigit.ors.partitioning.FastIsochroneParameters.*;
import java.util.*;

public class MaxFlowMinCut {

    protected FlowNode srcNode, snkNode;
    protected boolean flooded;
    protected int nodes, visitedToken, maxFlow, maxFlowLimit;

    protected static Set<Integer> _skippedEdgeSet;
    protected static int _dummyNodeId, _dummyEdgeId;
    public static Map<Integer, FlowNode> _nodeMap;
    protected static final int BACKW_EDGE_ID = Integer.MAX_VALUE;

    protected Graph _graph;
    protected EdgeExplorer _edgeExpl;
    protected GraphHopperStorage _ghStorage;

    protected double limit;
    protected Set<Integer> nodeIdSet, edgeIdSet;

    MaxFlowMinCut(GraphHopperStorage ghStorage) {
        this._ghStorage = ghStorage;
        this._graph = ghStorage.getBaseGraph();
        this._edgeExpl = _graph.createEdgeExplorer();
        init();
        MaxFlowMinCutImpl maxFlowMinCut = new MaxFlowMinCutImpl(ghStorage);
        maxFlowMinCut.run();
    }

    MaxFlowMinCut() {
    }

    private void init() {
        this.nodeIdSet = new HashSet<>();
        this.edgeIdSet = new HashSet<>();
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
        this.snkNode = new FlowNode(getDummyNodeId());
    }

    private void identifySrcSnkEdges(double a, double b, List<Integer> sortedNodes) {
        FlowEdge dummForw, dummBack;

        this.nodes = sortedNodes.size();
        this.nodeIdSet.addAll(sortedNodes);

        for (int i = 0; i < nodes; i++) {
            int nodeId = sortedNodes.get(i);
            FlowNode node = _nodeMap.get(nodeId);

            //>> reset Values and set eligible outGoingEdges of Node
            node.reset();
            for (FlowEdge outEdge : node.outEdgesBckp) {
                outEdge.reset();
                if (outEdge.inverse==null) {
                    System.out.println(outEdge.id);
//                    outEdge.inverse.reset();
                }
                if (nodeIdSet.contains(outEdge.targNode.id))
                    node.outEdges.add(outEdge);
            }

            //>> handle Dummy-Edges of Node
            if ((int) (a * nodes) <= i && i < (int) (b * nodes)) {
                //>> bring DummySourceEdges to Life
                dummForw = node.dummyOutEdge.inverse;
                dummBack = node.dummyOutEdge;

                dummForw.capacity = INFL__DUMMY_EDGE_CAPACITY;
                dummBack.capacity = 0;
                dummForw.baseNode = dummBack.targNode = srcNode;
                node.outEdges.add(dummBack);
                srcNode.outEdges.add(dummForw);
//                GeoJSON.addNode3(nodeId);
            } else if ((int) ((1 - b) * nodes) < i && i <= (int) ((1 - a) * nodes)) {
                //>> bring DummySinkEdges to Life
                dummForw = node.dummyOutEdge;
                dummBack = node.dummyOutEdge.inverse;

                dummForw.capacity = INFL__DUMMY_EDGE_CAPACITY;
                dummBack.capacity = 0;
                dummForw.targNode = dummBack.baseNode = snkNode;
                node.outEdges.add(dummForw);
                snkNode.outEdges.add(dummBack);
//                GeoJSON.addNode3(nodeId);
            } else {
                //>> let all other DummyEdges sleep
                node.dummyOutEdge.capacity = node.dummyOutEdge.inverse.capacity = -1;
                node.dummyOutEdge.targNode = node.dummyOutEdge.inverse.baseNode = null;
            }
        }
    }

    private synchronized int getDummyNodeId() {
        return --_dummyNodeId;
    }

    protected synchronized int getDummyEdgeId() {
        return --_dummyEdgeId;
    }
}
