package org.heigit.ors.partitioning;

import com.graphhopper.storage.GraphHopperStorage;

import java.util.HashSet;
import java.util.Set;


public abstract class AbstractMaxFlowMinCutAlgorithm extends MaxFlowMinCut {

    private Set<Integer> srcPartition, snkPartition;


    public AbstractMaxFlowMinCutAlgorithm(GraphHopperStorage ghStorage) {
        super(ghStorage);
    }

    public AbstractMaxFlowMinCutAlgorithm() {}

    public void setVisited(FlowNode node) {
        node.visited = visitedToken;
    }

    public boolean isVisited(FlowNode node) {
        return (node.visited == visitedToken);
    }

    public void setUnvisitedAll() {
        ++this.visitedToken;
    }

    public int getMaxFlow() {
        execute();
        return maxFlow;
    }

    private void calcNodePartition() {
        srcPartition = new HashSet<>();
        snkPartition = new HashSet<>();

        execute();
        for (int nodeId : nodeIdSet) {
            if (isVisited(_nodeMap.get(nodeId)))
                this.srcPartition.add(nodeId);
            else
                this.snkPartition.add(nodeId);
        }
    }

    public Set<Integer> getSrcPartition() {
        calcNodePartition();
        return srcPartition;
    }

    public Set<Integer> getSnkPartition() {
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
