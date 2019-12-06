package heigit.ors.partitioning;

import java.util.Arrays;

public class PartitioningData {
    //Edge data
    int[] flowEdgeBaseNode, flowEdgeInverse;
    int[] flow, capacity;
    boolean[] active;
    boolean[] minCut;
    int[] visited;

    //node data
    //every node has one dummy edge. Map nodeid to DummyEdge
    int[] inverseDummyEdgeIds, dummyEdgeIds, dummyEdgeBaseNodes, dummyEdgeTargNodes;

    public PartitioningData() {

    }

    public void createEdgeDataStructures(int size){
        flowEdgeBaseNode = new int[2 * size];
        flowEdgeInverse = new int[2 * size];
        flow = new int[2 * size];
        capacity = new int[2 * size];
        active = new boolean[2 * size];
        Arrays.fill(flowEdgeBaseNode, -1);
    }

    public void createNodeDataStructures(int size){
//        float loadFactor = 0.75F;
        //size should be num of nodes. Each node has one dummy edge.
        //There is data for every node + data for each source/sink node. 2 * numNodes means support for
        //a minimum cell size of 2 nodes per cell, so plenty enough.
//        dummyEdges = new FlowEdge[size];
        inverseDummyEdgeIds = new int[size];
        dummyEdgeIds = new int[size];
        dummyEdgeBaseNodes = new int[size];
        dummyEdgeTargNodes = new int[size];

        minCut = new boolean[2 * size];
        visited = new int[2 * size];
        Arrays.fill(visited, -1);
    }


    public void setFlowEdgeData(int edgeId, int baseNode, FlowEdgeData data){
        if(flowEdgeBaseNode[2 * edgeId] == -1 || flowEdgeBaseNode[2 * edgeId] == baseNode) {
            setFlowEdgeData(2 * edgeId, data);
            flowEdgeBaseNode[2 * edgeId] = baseNode;
        }
        else if (flowEdgeBaseNode[2 * edgeId + 1] == -1 || flowEdgeBaseNode[2 * edgeId + 1] == baseNode) {
            setFlowEdgeData(2 * edgeId + 1, data);
            flowEdgeBaseNode[2 * edgeId + 1] = baseNode;
        }
    }

    public FlowEdgeData getFlowEdgeData(int edgeId, int baseNode){
        int pointer = -1;
        if(flowEdgeBaseNode[2 * edgeId] == baseNode)
            pointer = 2 * edgeId;
        if(flowEdgeBaseNode[2 * edgeId + 1] == baseNode)
            pointer = 2 * edgeId + 1;
        if (pointer == -1)
            throw new IllegalStateException("Edge and node do not belong together?");
        return new FlowEdgeData(flow[pointer], capacity[pointer], flowEdgeInverse[pointer], active[pointer]);
    }

    public FlowEdgeData getFlowEdgeData(int edgeId){
        if(flowEdgeBaseNode[2* edgeId + 1] == -1)
            return new FlowEdgeData(flow[2* edgeId], capacity[2* edgeId], flowEdgeInverse[2* edgeId], active[2* edgeId]);
        if(flowEdgeBaseNode[2* edgeId] == -1)
            return new FlowEdgeData(flow[2* edgeId + 1], capacity[2* edgeId + 1], flowEdgeInverse[2* edgeId + 1], active[2* edgeId + 1]);
        throw new IllegalStateException("Operation only allowed on dummy edges");
//        return flowEdgeDataPairs[edgeId].getFlowEdgeData();
    }

    public void replaceBaseNodeFlowEdgeData(int edgeId, int newBaseNode){
        int baseNode0 = flowEdgeBaseNode[2 * edgeId];
        int baseNode1 = flowEdgeBaseNode[2 * edgeId + 1];
        if ((baseNode0 == -1 && baseNode1 == -1)
                || (baseNode0 != -1 && baseNode1 != -1))
            throw new IllegalStateException("Original basenode not found");
        if (baseNode0 == -1)
            flowEdgeBaseNode[2 * edgeId + 1] = newBaseNode;
        else
            flowEdgeBaseNode[2 * edgeId] = newBaseNode;
    }

    private void setFlowEdgeData(int dataPosition, FlowEdgeData data){
        flowEdgeInverse[dataPosition] = data.inverse;
        flow[dataPosition] = data.flow;
        capacity[dataPosition] = data.capacity;
        active[dataPosition] = data.active;
    }

    public void setDummyFlowEdgeData(int dataPosition, FlowEdgeData data){
        flowEdgeInverse[2* dataPosition] = data.inverse;
        flow[2 * dataPosition] = data.flow;
        capacity[2 * dataPosition] = data.capacity;
        active[2 * dataPosition] = data.active;
    }


    /*
    DUMMY
     */

    public void setDummyEdge(int nodeId, FlowEdge data){
        dummyEdgeIds[nodeId] = data.id;
        inverseDummyEdgeIds[nodeId] = data.inverse;
        dummyEdgeBaseNodes[nodeId] = data.baseNode;
        dummyEdgeTargNodes[nodeId] = data.targNode;

    }

    public FlowEdge getDummyEdge(int nodeId){
        return new FlowEdge(
                dummyEdgeIds[nodeId],
                inverseDummyEdgeIds[nodeId],
                dummyEdgeBaseNodes[nodeId],
                dummyEdgeTargNodes[nodeId]);
    }

    public FlowEdge getInverseDummyEdge(int nodeId){
        return new FlowEdge(
                inverseDummyEdgeIds[nodeId],
                dummyEdgeIds[nodeId],
                dummyEdgeTargNodes[nodeId],
                dummyEdgeBaseNodes[nodeId]);
    }


    public void setDummyBaseNode(int nodeId, int newBaseNode){
        dummyEdgeBaseNodes[nodeId] = newBaseNode;
    }

    public void setDummyTargNode(int nodeId, int newTargNode){
        dummyEdgeTargNodes[nodeId] = newTargNode;
    }



    public void setFlowNodeData(int nodeId, FlowNodeData data){
        minCut[nodeId] = data.minCut;
        visited[nodeId] = data.visited;
    }

    public FlowNodeData getFlowNodeData(int nodeId){
        return new FlowNodeData(minCut[nodeId], visited[nodeId]);
    }

    public FlowNodeData getFlowNodeDataOrDefault(int nodeId, FlowNodeData data){
        return visited[nodeId] == -1 ? data : new FlowNodeData(minCut[nodeId], visited[nodeId]);
    }


}
