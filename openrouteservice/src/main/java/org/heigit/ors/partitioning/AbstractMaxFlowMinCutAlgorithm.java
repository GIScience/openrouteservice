package org.heigit.ors.partitioning;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;

import java.util.HashSet;
import java.util.Set;


public abstract class AbstractMaxFlowMinCutAlgorithm extends MaxFlowMinCut {

    private IntHashSet srcPartition, snkPartition;


    public AbstractMaxFlowMinCutAlgorithm(GraphHopperStorage ghStorage, EdgeFilter edgeFilter, boolean init) {
        super(ghStorage, edgeFilter, init);
    }

    public AbstractMaxFlowMinCutAlgorithm() {}

    public void setVisited(int node) {
        FlowNodeData flowNodeData = pData.getFlowNodeData(node);
        flowNodeData.visited = visitedToken;
    }

    public boolean isVisited(int visited) {
        return (visited == visitedToken);
    }

    public void setUnvisitedAll() {
        ++this.visitedToken;
    }

    public int getMaxFlow() {
        execute();
        return maxFlow;
    }

    private void calcNodePartition() {
        srcPartition = new IntHashSet();
        snkPartition = new IntHashSet();

        execute();
        for (IntCursor nodeId : nodeIdSet) {
            if (isVisited(pData.getFlowNodeData(nodeId.value).visited))
                this.srcPartition.add(nodeId.value);
            else
                this.snkPartition.add(nodeId.value);
        }
    }

    public IntHashSet getSrcPartition() {
        calcNodePartition();
        return srcPartition;
    }

    public IntHashSet getSnkPartition() {
        calcNodePartition();
        return snkPartition;
    }

    private void execute() {
        if (flooded)
            return;

        this.flooded = true;
        flood();
    }

    public abstract void flood();
}
