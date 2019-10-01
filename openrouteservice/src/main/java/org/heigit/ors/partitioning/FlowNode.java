package org.heigit.ors.partitioning;

import java.util.ArrayList;
import java.util.List;

public class FlowNode {
    public int id = -1;
    protected List<FlowEdge> outEdges;

    FlowNode(int id) {
        this.id = id;
        this.outEdges = new ArrayList<>();
    }
}
