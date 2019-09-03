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

    private Map<String, FlowEdge> flowEdgeMap;


    MaxFlowMinCutImpl(GraphHopperStorage ghStorage) {
        this._graph = ghStorage.getBaseGraph();
        this._edgeExpl = _graph.createEdgeExplorer();

        initStatics();
    }

    public void run(){
        buildStaticNetwork();
        pairEdges();
        freeMemory();
    }


    public void initStatics() {
        this.nodes = _graph.getNodes();
        this.flowEdgeMap = new HashMap<>();


        _nodeMap = new HashMap<>();
        _skippedEdgeSet = new HashSet<>();
        _dummyNodeId = _dummyEdgeId = 0;
    }

    public void buildStaticNetwork() {
        Set<Integer> targSet = new HashSet<>();

        for (int nodeId = 0; nodeId < nodes; nodeId++)
            addDummyEdgePair(new FlowNode(nodeId));

        for (int baseId = 0; baseId < nodes; baseId++) {
            targSet.clear();
            _edgeIter = _edgeExpl.setBaseNode(baseId);
            while (_edgeIter.next()) {
                int targId = _edgeIter.getAdjNode();
                //>> eliminate Loops and MultiEdges
                if ((baseId != targId) && (!targSet.contains(targId))) {
                    targSet.add(targId);

                    int capa = INFL__GRAPH_EDGE_CAPACITY;
                    addEdge(_edgeIter.getEdge(), baseId, targId, capa);
                }
            }
        }
    }

    private FlowEdge addEdge(int id, int baseId, int targId, int capacity) {
        FlowNode baseNode = _nodeMap.get(baseId);
        FlowNode targNode = _nodeMap.get(targId);
        FlowEdge forwEdge = new FlowEdge(id, baseNode, targNode, capacity);
        this.flowEdgeMap.put(targId + "," + baseId, forwEdge);
        baseNode.outEdgesBckp.add(forwEdge);
        return forwEdge;
    }

    private void pairEdges() {
        for (Map.Entry<String, FlowEdge> entry : flowEdgeMap.entrySet()) {
            if (entry.getValue().inverse == null) {
                String[] ids = entry.getKey().split(",");
                int baseId = Integer.parseInt(ids[0]);
                int targId = Integer.parseInt(ids[1]);

                FlowEdge invEdge = flowEdgeMap.getOrDefault(targId + "," + baseId, null);
                if (invEdge == null)
                    invEdge = addEdge(BACKW_EDGE_ID, targId, baseId, 0);
                entry.getValue().inverse = invEdge;
                invEdge.inverse = entry.getValue();
            }
        }
    }

    public void addDummyEdgePair(FlowNode node) {
        FlowEdge forwEdge = new FlowEdge(getDummyEdgeId(), node, null, 0);
        FlowEdge backEdge = new FlowEdge(BACKW_EDGE_ID, null, node, 0);
        forwEdge.inverse = backEdge;
        backEdge.inverse = forwEdge;
        node.dummyOutEdge = forwEdge;
    }

    public void freeMemory() {
        this.flowEdgeMap = null;
    }

}
