package org.heigit.ors.partitioning;

import com.carrotsearch.hppc.IntHashSet;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;


public abstract class AbstractMaxFlowMinCutAlgorithm extends MaxFlowMinCut {

    private IntHashSet srcPartition, snkPartition;


    public AbstractMaxFlowMinCutAlgorithm(GraphHopperStorage ghStorage, PartitioningData pData,  EdgeFilter edgeFilter, boolean init) {
        super(ghStorage, pData, edgeFilter, init);
    }

    public AbstractMaxFlowMinCutAlgorithm() {}

    public void setVisited(int node) {
        pData.setVisited(node, visitedToken);
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
        for (int nodeId : nodeOrder.keys) {
            if (isVisited(pData.getVisited(nodeId)))
                this.srcPartition.add(nodeId);
            else
                this.snkPartition.add(nodeId);
        }
    }

    public IntHashSet getSrcPartition() {
        calcNodePartition();
        return srcPartition;
    }

    public IntHashSet getSnkPartition() {
//        calcNodePartition();
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
