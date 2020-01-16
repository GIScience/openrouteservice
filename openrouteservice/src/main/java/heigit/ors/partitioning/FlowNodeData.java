package heigit.ors.partitioning;

public class FlowNodeData {
    public int visited = 0;

    public FlowNodeData(int visited) {
        this.visited = visited;
    }

    public void reset() {
        this.visited = 0;
    }

}
