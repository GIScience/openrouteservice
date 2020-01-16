package org.heigit.ors.partitioning;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

import java.util.HashSet;
import java.util.Set;

import static org.heigit.ors.partitioning.FastIsochroneParameters.*;


public class MaxFlowMinCutImpl extends MaxFlowMinCut {

    private Graph _graph;
    private EdgeExplorer _edgeExpl;
    private EdgeIterator _edgeIter;

    private int maxEdgeId = -1;


    MaxFlowMinCutImpl(GraphHopperStorage ghStorage, PartitioningData pData) {
        this._graph = ghStorage.getBaseGraph();
        this._edgeExpl = _graph.createEdgeExplorer();
        this.pData = pData;

        initStatics();
    }

    public void run(){
        _dummyEdgeId = _graph.getAllEdges().getMaxId() + 1;
        _dummyNodeId = _graph.getNodes() + 1;
        //Need entries for all edges + one dummy edge for all nodes
        pData.createEdgeDataStructures(_dummyEdgeId);
        pData.fillFlowEdgeBaseNodes(_graph);
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
                    addEdge(_edgeIter.getEdge(), baseId, targId);
                }
            }
        }
    }

    private int addEdge(int edgeId, int baseNode, int targNode) {
        pData.setFlowEdgeData(edgeId, baseNode,
                new FlowEdgeData(false, edgeId, false));
        pData.setFlowEdgeData(edgeId, targNode,
                new FlowEdgeData(false, edgeId, false));
        if(maxEdgeId < edgeId)
            maxEdgeId = edgeId;
        return edgeId;
    }


    public void addDummyEdgePair(int node) {
        FlowEdge forwEdge = new FlowEdge(getDummyEdgeId(), node, -1);
        int backEdgeId = getDummyEdgeId();// = new FlowEdge(getDummyEdgeId(), -1, node);
        forwEdge.inverse = backEdgeId;
        pData.setDummyEdge(node, forwEdge);
    }


}
