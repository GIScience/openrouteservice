package heigit.ors.partitioning;

import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.storage.Graph;

import java.util.Arrays;

import static heigit.ors.partitioning.FastIsochroneParameters.INFL__GRAPH_EDGE_CAPACITY;

public class PartitioningData {
    //Edge data
    int[] flowEdgeBaseNode;
    boolean[] flow;
    boolean[] active;
    int[] visited;

    //node data
    //every node has one dummy edge. Map nodeid to DummyEdge
    int[] dummyEdgeIds, dummyEdgeTargNodes;

    public PartitioningData() {

    }

    public void createEdgeDataStructures(int size){
        flowEdgeBaseNode = new int[2 * size];
        flow = new boolean[2 * size];
        active = new boolean[2 * size];
        Arrays.fill(flowEdgeBaseNode, -1);
    }

    public void fillFlowEdgeBaseNodes(Graph graph) {
        AllEdgesIterator iter = graph.getAllEdges();
        while (iter.next()){
            flowEdgeBaseNode[2 * iter.getEdge()] = iter.getBaseNode();
            flowEdgeBaseNode[2 * iter.getEdge() + 1] = iter.getAdjNode();
        }
    }

    public void createNodeDataStructures(int size){
//        float loadFactor = 0.75F;
        //size should be num of nodes. Each node has one dummy edge.
        //There is data for every node + data for each source/sink node. 2 * numNodes means support for
        //a minimum cell size of 2 nodes per cell, so plenty enough.
//        dummyEdges = new FlowEdge[size];
        dummyEdgeIds = new int[size];
        dummyEdgeTargNodes = new int[size];

        visited = new int[size];
        Arrays.fill(visited, -1);
    }


    public void setFlowEdgeData(int edgeId, int baseNode, FlowEdgeData data){
        if(flowEdgeBaseNode[2 * edgeId] == baseNode) {
            setFlowEdgeData(2 * edgeId, data);
            return;
        }
        else if (flowEdgeBaseNode[2 * edgeId + 1] == baseNode) {
            setFlowEdgeData(2 * edgeId + 1, data);
            return;
        }
        throw new IllegalStateException("edgeId " + edgeId + " and basenode " + baseNode + " do not belong together");
    }

    public FlowEdgeData getFlowEdgeData(int edgeId, int baseNode){
        int pointer = -1;
        if(flowEdgeBaseNode[2 * edgeId] == baseNode)
            pointer = 2 * edgeId;
        if(flowEdgeBaseNode[2 * edgeId + 1] == baseNode)
            pointer = 2 * edgeId + 1;
        if (pointer == -1)
            throw new IllegalStateException("Edge and node do not belong together?");
        return new FlowEdgeData(flow[pointer], edgeId, active[pointer]);
    }


    private void setFlowEdgeData(int dataPosition, FlowEdgeData data){
        flow[dataPosition] = data.flow;
        active[dataPosition] = data.active;
    }

    /*
    DUMMY
     */

    public void setDummyEdge(int nodeId, FlowEdge data){
        dummyEdgeIds[nodeId] = data.id;
        dummyEdgeTargNodes[nodeId] = data.targNode;

    }

    public FlowEdge getDummyEdge(int nodeId){
        return new FlowEdge(
                dummyEdgeIds[nodeId],
                dummyEdgeIds[nodeId] + 1,
                nodeId,
                dummyEdgeTargNodes[nodeId]);
    }

    public FlowEdge getInverseDummyEdge(int nodeId){
        return new FlowEdge(
                dummyEdgeIds[nodeId] + 1,
                dummyEdgeIds[nodeId],
                dummyEdgeTargNodes[nodeId],
                nodeId);
    }

        public void setDummyTargNode(int nodeId, int newTargNode){
        dummyEdgeTargNodes[nodeId] = newTargNode;
    }



    public void setFlowNodeData(int nodeId, FlowNodeData data){
        visited[nodeId] = data.visited;
    }

    public FlowNodeData getFlowNodeData(int nodeId){
        return new FlowNodeData(visited[nodeId]);
    }

    public FlowNodeData getFlowNodeDataOrDefault(int nodeId, FlowNodeData data){
        return visited[nodeId] == -1 ? data : new FlowNodeData(visited[nodeId]);
    }


}
