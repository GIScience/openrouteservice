package heigit.ors.partitioning;

import java.util.HashMap;
import java.util.Map;

public class PartitioningData {
    //Edge data
    FlowEdgeDataPair[] flowEdgeDataPairs;
    FlowNodeData[] flowNodeData;

    //node data
    //every node has one dummy edge. Map nodeid to DummyEdge
    FlowEdge[] dummyEdges;

    public PartitioningData() {

    }

    public void createEdgeDataStructures(int size){
//        float loadFactor = 0.75F;
        flowEdgeDataPairs = new FlowEdgeDataPair[size];

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
    //Breaks parallelization
    public void clearActiveEdges(){
        for(FlowEdgeDataPair data : flowEdgeDataPairs){
            if (data == null) continue;
//            FlowEdgeData flowEdgeData = entry.getValue();
            if(data.flowEdgeData0 != null){
                data.flowEdgeData0.active = false;
                data.flowEdgeData0.flow = 0;
            }
            if(data.flowEdgeData1 != null){
                data.flowEdgeData1.active = false;
                data.flowEdgeData1.flow = 0;
            }
//            data.active = false;
//            data.flow = 0;
//            flowEdgeDataMap.put(entry.getKey(), flowEdgeData);
        }
    }
    //Breaks parallelization
    public void clearVisitedNodes(){
        for(FlowNodeData data : flowNodeData){
            if (data == null) continue;
//            FlowNodeData flowNodeData = entry.getValue();
            data.visited = 0;
            data.minCut = false;
//            flowNodeDataMap.put(entry.getKey(), flowNodeData);
        }
    }

    public void setFlowEdgeData(int edgeId, int baseNode, FlowEdgeData data){
        if(flowEdgeDataPairs[edgeId] == null){
            FlowEdgeDataPair flowEdgeDataPair = new FlowEdgeDataPair();
            flowEdgeDataPair.setFlowEdgeData(baseNode, data);
            flowEdgeDataPairs[edgeId] = flowEdgeDataPair;
        }
        else
            flowEdgeDataPairs[edgeId].setFlowEdgeData(baseNode, data);
    }

    public FlowEdgeData getFlowEdgeData(int edgeId, int baseNode){
        return flowEdgeDataPairs[edgeId].getFlowEdgeData(baseNode);
    }

    public FlowEdgeData getFlowEdgeData(int edgeId){
        return flowEdgeDataPairs[edgeId].getFlowEdgeData();
    }

    public void replaceBaseNodeFlowEdgeData(int edgeId, int newBaseNode){
        flowEdgeDataPairs[edgeId].replaceBaseNode(newBaseNode);
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


    class FlowEdgeDataPair {
        int baseNode0 = -1;
        int baseNode1 = -1;
        FlowEdgeData flowEdgeData0;
        FlowEdgeData flowEdgeData1;

        public FlowEdgeDataPair setFlowEdgeData(int baseNode, FlowEdgeData flowEdgeData){
            if (baseNode0 == -1 || baseNode0 == baseNode){
                baseNode0 = baseNode;
                flowEdgeData0 = flowEdgeData;
                return this;
            }
            if (baseNode1 == -1 || baseNode1 == baseNode){
                baseNode1 = baseNode;
                flowEdgeData1 = flowEdgeData;
                return this;
            }
            throw new IllegalStateException("Edge and node do not belong together?");
        }

        public void replaceBaseNode(int newBaseNode){
            if ((baseNode0 == -1 && baseNode1 == -1)
                || (baseNode0 != -1 && baseNode1 != -1))
                throw new IllegalStateException("Original basenode not found");
            if (baseNode0 == -1)
                baseNode1 = newBaseNode;
            else
                baseNode0 = newBaseNode;
        }

        public FlowEdgeData getFlowEdgeData(int baseNode){
            if(baseNode0 == baseNode)
                return flowEdgeData0;
            if(baseNode1 == baseNode)
                return flowEdgeData1;
            throw new IllegalStateException("Edge and node do not belong together?");
        }

        public FlowEdgeData getFlowEdgeData(){
            if(baseNode0 == -1)
                return flowEdgeData1;
            if(baseNode1 == -1)
                return flowEdgeData0;
            throw new IllegalStateException("Operation only allowed on dummy edges");
        }
    }

}
