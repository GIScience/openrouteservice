package org.heigit.ors.fastisochrones.partitioning;

import com.graphhopper.storage.Graph;

/**
 * Creates the data necessary for running a max flow min cut algorithm.
 *
 * @author Hendrik Leuschner
 */
public class PartitioningDataBuilder {
    private final Graph graph;
    private final PartitioningData pData;

    PartitioningDataBuilder(Graph graph, PartitioningData pData) {
        this.graph = graph;
        this.pData = pData;
    }

    public void run() {
        pData.createEdgeDataStructures(graph.getAllEdges().length() + 1);
        pData.fillFlowEdgeBaseNodes(graph);
        pData.createNodeDataStructures(graph.getNodes() + 1);
    }
}
