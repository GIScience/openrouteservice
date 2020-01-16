package heigit.ors.partitioning;

public class FlowEdgeData {
    public boolean flow = false;
    public int inverse = -1;
    public boolean active = false;

    public FlowEdgeData(boolean flow, int inverse, boolean active) {
        this.flow = flow;
        this.inverse = inverse;
        this.active = active;
    }

}
