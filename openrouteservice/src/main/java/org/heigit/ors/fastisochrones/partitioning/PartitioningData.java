package org.heigit.ors.fastisochrones.partitioning;

import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.storage.Graph;

import java.util.Arrays;

/**
 * Temporary preprocessing time storage for max flow min cut data.
 *
 * @author Hendrik Leuschner
 */
public class PartitioningData {
    //Edge data
    int[] flowEdgeBaseNode;
    boolean[] flow;
    //Node data
    int[] visited;

    public PartitioningData() {    }

    public PartitioningData(int[] flowEdgeBaseNode, boolean[] flow, int[] visited) {
        this.flowEdgeBaseNode = flowEdgeBaseNode;
        this.flow = flow;
        this.visited = visited;
    }

    public void createEdgeDataStructures(int size) {
        flowEdgeBaseNode = new int[2 * size];
        flow = new boolean[2 * size];
        Arrays.fill(flowEdgeBaseNode, -1);
    }

    public void fillFlowEdgeBaseNodes(Graph graph) {
        AllEdgesIterator iter = graph.getAllEdges();
        while (iter.next()) {
            flowEdgeBaseNode[2 * iter.getEdge()] = iter.getBaseNode();
            flowEdgeBaseNode[2 * iter.getEdge() + 1] = iter.getAdjNode();
        }
    }

    public void createNodeDataStructures(int size) {
        //size should be num of nodes. Each node has one dummy edge.
        //There is data for every node + data for each source/sink node. 2 * numNodes means support for
        //a minimum cell size of 2 nodes per cell, so plenty enough.
        visited = new int[size];
    }


    public void setFlowEdgeData(int edgeId, int baseNode, FlowEdgeData data) {
        if (flowEdgeBaseNode[2 * edgeId] == baseNode) {
            setFlowEdgeData(2 * edgeId, data);
            return;
        } else if (flowEdgeBaseNode[2 * edgeId + 1] == baseNode) {
            setFlowEdgeData(2 * edgeId + 1, data);
            return;
        }
        throw new IllegalStateException("edgeId " + edgeId + " and basenode " + baseNode + " do not belong together");
    }

    public FlowEdgeData getFlowEdgeData(int edgeId, int baseNode) {
        int pointer = -1;
        if (flowEdgeBaseNode[2 * edgeId] == baseNode)
            pointer = 2 * edgeId;
        if (flowEdgeBaseNode[2 * edgeId + 1] == baseNode)
            pointer = 2 * edgeId + 1;
        if (pointer == -1)
            throw new IllegalStateException("Edge " + edgeId + " and node " + baseNode + " do not belong together?");
        return new FlowEdgeData(flow[pointer], edgeId);
    }


    private void setFlowEdgeData(int dataPosition, FlowEdgeData data) {
        flow[dataPosition] = data.flow;
    }

    public void setVisited(int nodeId, int newVisited) {
        visited[nodeId] = newVisited;
    }

    public int getVisited(int nodeId) {
        return visited[nodeId];
    }
}
