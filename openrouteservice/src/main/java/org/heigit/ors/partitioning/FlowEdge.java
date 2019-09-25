package org.heigit.ors.partitioning;

public class FlowEdge {
    public heigit.ors.partitioning.FlowEdge inverse;
    public int id;
    public int baseNode, targNode;


    public FlowEdge(int id, int baseNode, int targNode) {
        this.id = id;
        this.baseNode = baseNode;
        this.targNode = targNode;
    }

}
