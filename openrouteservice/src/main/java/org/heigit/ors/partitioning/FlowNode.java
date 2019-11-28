package org.heigit.ors.partitioning;

import com.carrotsearch.hppc.IntArrayList;

import java.util.ArrayList;
import java.util.List;

public class FlowNode {
    private int EXPECTEDEDGECOUNT = 100;
    public int id = -1;
//    protected List<FlowEdge> outEdges;
    protected IntArrayList outNodes;

    FlowNode(int id) {
        this.id = id;
        this.outNodes = new IntArrayList(EXPECTEDEDGECOUNT);
    }
}
