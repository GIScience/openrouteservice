package heigit.ors.partitioning;

import java.util.HashMap;
import java.util.Map;

public class PartitioningData {
    //Edge data
    static Map<Integer, FlowEdgeData> flowEdgeDataMap;
    static Map<Integer, FlowNodeData> flowNodeDataMap;

    //node data
    //every node has one dummy edge. Map nodeid to DummyEdge
    static Map<Integer, FlowEdge> dummyEdges;

    public PartitioningData() {

    }

    public static void createEdgeDataStructures(int size){
        float loadFactor = 0.75F;
        flowEdgeDataMap = new HashMap<>(size, loadFactor);

    }

    public static void createNodeDataStructures(int size){
        float loadFactor = 0.75F;
        dummyEdges = new HashMap<>(size, loadFactor);
        flowNodeDataMap = new HashMap<>(size, loadFactor);
    }

    public static void clearActiveEdges(){
        for(Map.Entry<Integer, FlowEdgeData> entry : flowEdgeDataMap.entrySet()){
            FlowEdgeData flowEdgeData = entry.getValue();
            flowEdgeData.active = false;
            flowEdgeDataMap.put(entry.getKey(), flowEdgeData);
        }
    }

    public static void clearVisitedNodes(){
        for(Map.Entry<Integer, FlowNodeData> entry : flowNodeDataMap.entrySet()){
            FlowNodeData flowNodeData = entry.getValue();
            flowNodeData.visited = 0;
            flowNodeDataMap.put(entry.getKey(), flowNodeData);
        }
    }


}
