package org.heigit.ors.fastisochrones.partitioning;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates the data necessary for running a max flow min cut algorithm.
 *
 * @author Hendrik Leuschner
 */
public class PartitioningDataBuilder {
    private Graph graph;
    private EdgeExplorer edgeExplorer;
    private PartitioningData pData;
    private EdgeFilter edgeFilter;
    private int maxEdgeId = -1;

    PartitioningDataBuilder(Graph graph, PartitioningData pData) {
        this.graph = graph;
        this.edgeExplorer = this.graph.createEdgeExplorer();
        this.pData = pData;
    }

    public void run() {
        //Need entries for all edges + one dummy edge for all nodes
        pData.createEdgeDataStructures(graph.getAllEdges().length() + 1);
        pData.fillFlowEdgeBaseNodes(graph);
        pData.createNodeDataStructures(graph.getNodes() + 1);
        buildStaticNetwork();
    }

    public void buildStaticNetwork() {
        Set<Integer> targSet = new HashSet<>();
        EdgeIterator edgeIter;

        for (int baseId = 0; baseId < graph.getNodes(); baseId++) {
            targSet.clear();
            edgeIter = edgeExplorer.setBaseNode(baseId);
            while (edgeIter.next()) {
                int targId = edgeIter.getAdjNode();
                if (!acceptForPartitioning(edgeIter))
                    continue;
                //>> eliminate Loops and MultiEdges
                if ((baseId != targId) && (!targSet.contains(targId))) {
                    targSet.add(targId);
                    addEdge(edgeIter.getEdge(), baseId, targId);
                }
            }
        }
    }

    private int addEdge(int edgeId, int baseNode, int targNode) {
        pData.setFlowEdgeData(edgeId, baseNode,
                new FlowEdgeData(false, edgeId));
        pData.setFlowEdgeData(edgeId, targNode,
                new FlowEdgeData(false, edgeId));
        if (maxEdgeId < edgeId)
            maxEdgeId = edgeId;
        return edgeId;
    }

    public void setAdditionalEdgeFilter(EdgeFilter edgeFilter) {
        this.edgeFilter = edgeFilter;
    }

    private boolean acceptForPartitioning(EdgeIterator edgeIterator) {
        return edgeFilter == null ? true : edgeFilter.accept(edgeIterator);
    }
}
