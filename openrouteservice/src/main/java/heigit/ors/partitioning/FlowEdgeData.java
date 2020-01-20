package heigit.ors.partitioning;

public class FlowEdgeData {
    public boolean flow = false;
    public int inverse = -1;

    public FlowEdgeData(boolean flow, int inverse) {
        this.flow = flow;
        this.inverse = inverse;
    }

}
