package org.heigit.ors.partitioning;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.heigit.ors.partitioning.FastIsochroneParameters.*;


public class MaxFlowMinCutImpl extends MaxFlowMinCut {

    private Graph _graph;
    private EdgeExplorer _edgeExpl;
    private EdgeIterator _edgeIter;

    private int maxEdgeId = -1;


    MaxFlowMinCutImpl(GraphHopperStorage ghStorage) {
        this._graph = ghStorage.getBaseGraph();
        this._edgeExpl = _graph.createEdgeExplorer();

        initStatics();
    }

    public void run(){
        _dummyEdgeId = _graph.getAllEdges().getMaxId() + 1;
        _dummyNodeId = _graph.getNodes() + 1;
        //Need entries for all edges + one dummy edge for all nodes
        pData.createEdgeDataStructures(2 * _dummyEdgeId + 2 * _dummyNodeId);
        pData.createNodeDataStructures(_dummyNodeId);
        buildStaticNetwork();
//        pairEdges();
    }


    public void initStatics() {
        this.nodes = _graph.getNodes();
//        this.flowEdgeMap = new HashMap<>();
        _dummyEdgeId = _graph.getAllEdges().getMaxId() + 1;
        _dummyNodeId = _graph.getNodes() + 1;
    }

    public void buildStaticNetwork() {
        Set<Integer> targSet = new HashSet<>();

        for (int nodeId = 0; nodeId < nodes; nodeId++)
            addDummyEdgePair(nodeId);

        for (int baseId = 0; baseId < nodes; baseId++) {
            targSet.clear();
            _edgeIter = _edgeExpl.setBaseNode(baseId);
            while (_edgeIter.next()) {
                int targId = _edgeIter.getAdjNode();
                if(!acceptForPartitioning(_edgeIter))
                    continue;
                //>> eliminate Loops and MultiEdges
                if ((baseId != targId) && (!targSet.contains(targId))) {
                    targSet.add(targId);

//                    if(shouldBeLowCapacity(_edgeIter))
//                        addEdge(_edgeIter.getEdge(), baseId, INFL__LOW_GRAPH_EDGE_CAPACITY);
//                    else
                    addEdge(_edgeIter.getEdge(), baseId, targId, INFL__GRAPH_EDGE_CAPACITY);
                }
            }
        }
    }

    private int addEdge(int edgeId, int baseNode, int targNode, byte capacity) {
        pData.setFlowEdgeData(edgeId, baseNode,
                new FlowEdgeData((short)0, capacity, edgeId, false));
        pData.setFlowEdgeData(edgeId, targNode,
                new FlowEdgeData((short)0, capacity, edgeId, false));
        if(maxEdgeId < edgeId)
            maxEdgeId = edgeId;
        return edgeId;
    }


//    private void pairEdges() {
//        for (Map.Entry<String, Integer> entry : flowEdgeMap.entrySet()) {
//            String[] ids = entry.getKey().split(",");
//            int targId = Integer.parseInt(ids[0]);
//            int baseId = Integer.parseInt(ids[1]);
//
//            FlowEdgeData edgeData = pData.getFlowEdgeData(entry.getValue(), baseId);
//            if (edgeData.inverse == -1) {
//                int invEdge = flowEdgeMap.getOrDefault(baseId + "," + targId, -1);
//                if (invEdge == -1) {
//                    maxEdgeId++;
////                    invEdge = addEdge(getDummyEdgeId(), 0);
////                    invEdge = getDummyEdgeId();
//                    edgeData.inverse = entry.getValue();
//                    FlowEdgeData invEdgeData = new FlowEdgeData((short)0, (short)0, entry.getValue(), false);
//
////                    pData.setFlowEdgeData(entry.getValue(), baseId, edgeData);
//                    pData.setFlowEdgeData(entry.getValue(), targId, invEdgeData);
//                }
//                else {
//                    edgeData.inverse = invEdge;
//                    FlowEdgeData invEdgeData = pData.getFlowEdgeData(invEdge, targId);
//                    invEdgeData.inverse = entry.getValue();
//                    pData.setFlowEdgeData(invEdge, targId, invEdgeData);
//                }
//
//            }
//            pData.setFlowEdgeData(entry.getValue(), baseId, edgeData);
//        }
//    }

    public void addDummyEdgePair(int node) {
        FlowEdge forwEdge = new FlowEdge(getDummyEdgeId(), node, -1);
        int backEdgeId = getDummyEdgeId();// = new FlowEdge(getDummyEdgeId(), -1, node);
        forwEdge.inverse = backEdgeId;
        pData.setDummyEdge(node, forwEdge);

        FlowEdgeData flowEdgeData = new FlowEdgeData((short)0, (short)0, backEdgeId, false);
        pData.setFlowEdgeData(forwEdge.id, node, flowEdgeData);

        FlowEdgeData invFlowEdgeData = new FlowEdgeData((short)0, (short)0, forwEdge.id, false);
        pData.setFlowEdgeData(backEdgeId, -2, invFlowEdgeData);
    }


//    public void freeMemory() {
//        this.flowEdgeMap = null;
//    }

}
