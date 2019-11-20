package heigit.ors.partitioning;

public class FlowEdgeData {
    public short flow = 0;
    public short capacity = 0;
    public int inverse = -1;
    public boolean active = false;

    public FlowEdgeData(short flow, short capacity, int inverse, boolean active) {
        this.flow = flow;
        this.capacity = capacity;
        this.inverse = inverse;
        this.active = active;
    }

}
