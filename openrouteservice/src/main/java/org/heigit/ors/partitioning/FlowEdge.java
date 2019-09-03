package org.heigit.ors.partitioning;

public class FlowEdge {
    public org.heigit.ors.partitioning.FlowEdge inverse;
    public FlowNode baseNode, targNode;
    public int id, flow, capacity;


    public FlowEdge(int id, FlowNode baseNode, FlowNode targNode, int capacity) {
        this.id = id;
        this.baseNode = baseNode;
        this.targNode = targNode;
        this.capacity = capacity;
    }


    public void reset() {
        this.flow = 0;
    }

    public boolean isResidual() {
        return (capacity == 0);
    }

    public int getRemainingCapacity() {
        return capacity - flow;
    }

    public void augment(int bottleNeck) {
        this.flow += bottleNeck;
        this.inverse.flow -= bottleNeck;
    }

    public String toString(FlowNode s, FlowNode t) {
        String u = (baseNode == s) ? "s" : ((baseNode == t) ? "t" : String.valueOf(baseNode.id));
        String v = (targNode == s) ? "s" : ((targNode == t) ? "t" : String.valueOf(targNode.id));
        return String.format("Edge %s :: %s -> %s | flow = %d | capacity = %d | is inverse: %s", id,
                u, v, flow, capacity, isResidual());

    }
}
