package org.heigit.ors.partitioning;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIterator;
import heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RecursiveAction;

public abstract class PartitioningBase extends RecursiveAction{

    public enum PartitionAlgo {FordFulkerson, FordFulkerson2, EdmondsKarp, Dinic}

    int cellId;
    Graph ghGraph;
    String[] partAlgoAll;
    Set<Integer> nodeIdSet;
    AbstractMaxFlowMinCutAlgorithm mincutAlgo;
    EdgeFilter edgeFilter;

    static int[] nodeToCellArr;
    static GraphHopperStorage ghStorage;

    ExecutorService threadPool;



    PartitioningBase() {
    }

    PartitioningBase(GraphHopperStorage _ghStorage, EdgeFilterSequence edgeFilters) {
        ghStorage = _ghStorage;
        nodeToCellArr = new int[ghStorage.getNodes()];
        this.edgeFilter = edgeFilters;

        init();
    }


    private void init() {
        this.partAlgoAll = new String[PartitionAlgo.values().length];
        this.nodeIdSet = new HashSet<>();
        this.ghGraph = ghStorage.getBaseGraph();

        for (PartitionAlgo algo : PartitionAlgo.values())
            partAlgoAll[algo.ordinal()] = algo.name();
    }

    void initNodes() {
        EdgeIterator edgeIter = ghGraph.getAllEdges();
        while (edgeIter.next()) {
            nodeIdSet.add(edgeIter.getBaseNode());
            nodeIdSet.add(edgeIter.getAdjNode());
        }
    }

    void initAlgo() {
        mincutAlgo = new EdmondsKarp(ghStorage, this.edgeFilter, true);
//        mincutAlgo.setAdditionalEdgeFilter(this.edgeFilter);
    }

    void setAlgo() {
        mincutAlgo = new EdmondsKarp(ghStorage, this.edgeFilter, false);
    }

    AbstractMaxFlowMinCutAlgorithm getAlgo() {
        return new EdmondsKarp();

    }


//    public abstract void run();

    public static int[] getNodeToCellArr() {
        return nodeToCellArr;
    }
}
