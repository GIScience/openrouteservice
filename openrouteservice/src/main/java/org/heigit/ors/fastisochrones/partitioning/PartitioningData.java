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
    //Edge ids are the same for forward and reverse edges, but we need different data for each, so each edgeId has an entry at x and x+1
    int[] flowEdgeBaseNode;
    boolean[] flow;
    //Node data
    int[] visited;

    public PartitioningData() {
    }

    public PartitioningData(int[] flowEdgeBaseNode, boolean[] flow, int[] visited) {
        this.flowEdgeBaseNode = flowEdgeBaseNode;
        this.flow = flow;
        this.visited = visited;
    }

    public void createEdgeDataStructures(int size) {
        if (size < 1 || size > Integer.MAX_VALUE / 2)
            throw new IllegalArgumentException("Bad size.");
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

        if (size < 1 || size > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Bad size.");
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
        if (dataPosition > flow.length - 1 || dataPosition < 0)
            throw new IllegalArgumentException("Index " + dataPosition + " out of bounds for flow with length " + flow.length);
        flow[dataPosition] = data.isFlow();
    }

    public void setVisited(int nodeId, int newVisited) {
        if (nodeId > visited.length - 1 || nodeId < 0)
            throw new IllegalArgumentException("Index " + nodeId + " out of bounds for visited with length " + visited.length);
        visited[nodeId] = newVisited;
    }

    public int getVisited(int nodeId) {
        return visited[nodeId];
    }
}
