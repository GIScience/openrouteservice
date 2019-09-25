package heigit.ors.partitioning;

public class FlowNodeData {
    public boolean minCut = false;
    public int visited = 0;

    public FlowNodeData(boolean minCut, int visited) {
        this.minCut = minCut;
        this.visited = visited;
    }

    public void reset() {
        this.visited = 0;
        this.minCut = false;
    }

}
