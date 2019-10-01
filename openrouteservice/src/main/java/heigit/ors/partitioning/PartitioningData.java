package heigit.ors.partitioning;

import java.util.HashMap;
import java.util.Map;

public class PartitioningData {
    //Edge data
    FlowEdgeData[] flowEdgeData;
    FlowNodeData[] flowNodeData;

    //node data
    //every node has one dummy edge. Map nodeid to DummyEdge
    FlowEdge[] dummyEdges;

    public PartitioningData() {

    }

    public void createEdgeDataStructures(int size){
//        float loadFactor = 0.75F;
        flowEdgeData = new FlowEdgeData[size];

    }

    public void createNodeDataStructures(int size){
//        float loadFactor = 0.75F;
        //size should be num of nodes. Each node has one dummy edge.
        //There is data for every node + data for each source/sink node. 2 * numNodes means support for
        //a minimum cell size of 2 nodes per cell, so plenty enough.
        dummyEdges = new FlowEdge[size];
        flowNodeData = new FlowNodeData[2 * size];
//        flowNodeDataMap = new HashMap<>(size, loadFactor);
    }

    public void clearActiveEdges(){
        for(FlowEdgeData data : flowEdgeData){
            if (data == null) continue;
//            FlowEdgeData flowEdgeData = entry.getValue();
            data.active = false;
//            flowEdgeDataMap.put(entry.getKey(), flowEdgeData);
        }
    }

    public void clearVisitedNodes(){
        for(FlowNodeData data : flowNodeData){
            if (data == null) continue;
//            FlowNodeData flowNodeData = entry.getValue();
            data.visited = 0;
//            flowNodeDataMap.put(entry.getKey(), flowNodeData);
        }
    }

    public void setFlowEdgeData(int edgeId, FlowEdgeData data){
        flowEdgeData[edgeId] = data;
    }

    public FlowEdgeData getFlowEdgeData(int edgeId){
        return flowEdgeData[edgeId];
    }

    public void setDummyEdge(int nodeId, FlowEdge data){
        dummyEdges[nodeId] = data;
    }

    public FlowEdge getDummyEdge(int nodeId){
        return dummyEdges[nodeId];
    }

    public void setFlowNodeData(int nodeId, FlowNodeData data){
        flowNodeData[nodeId] = data;
    }

    public FlowNodeData getFlowNodeData(int nodeId){
        return flowNodeData[nodeId];
    }

    public FlowNodeData getFlowNodeDataOrDefault(int nodeId, FlowNodeData data){
        return flowNodeData[nodeId] == null ? data : flowNodeData[nodeId];
    }


}
