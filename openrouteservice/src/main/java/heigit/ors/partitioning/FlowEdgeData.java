package heigit.ors.partitioning;

public class FlowEdgeData {
    public int flow = 0;
    public int capacity = 0;
    public int inverse = -1;
    public boolean active = false;

    public FlowEdgeData(int flow, int capacity, int inverse, boolean active) {
        this.flow = flow;
        this.capacity = capacity;
        this.inverse = inverse;
        this.active = active;
    }

}
