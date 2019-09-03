package org.heigit.ors.partitioning;

import java.util.ArrayList;
import java.util.List;

public class FlowNode {
    public boolean minCut;
    public int id, visited, level, next;
    protected FlowEdge dummyOutEdge;
    protected List<FlowEdge> outEdges;
    protected List<FlowEdge> outEdgesBckp;


    FlowNode(int id) {
        this.id = id;
        this.level = 0;
        this.outEdges = new ArrayList<>();
        this.outEdgesBckp = new ArrayList<>();

        MaxFlowMinCut._nodeMap.put(id, this);
    }


    public List<FlowEdge> getOutEdges() {
        return outEdges;
    }

    public void reset() {
        this.next = 0;
        this.level = 0;
        this.visited = 0;
        this.minCut = false;
        this.outEdges.clear();
    }

}
