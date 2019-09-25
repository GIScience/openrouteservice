package org.heigit.ors.partitioning;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIterator;

import java.util.HashSet;
import java.util.Set;

public abstract class PartitioningBase {

    public enum PartitionAlgo {FordFulkerson, FordFulkerson2, EdmondsKarp, Dinic}

    int cellId;
    Graph ghGraph;
    String[] partAlgoAll;
    Set<Integer> nodeIdSet;
    AbstractMaxFlowMinCutAlgorithm mincutAlgo;

    static int[] nodeToCellArr;
    static GraphHopperStorage ghStorage;


    PartitioningBase() {
    }

    PartitioningBase(GraphHopperStorage _ghStorage) {
        ghStorage = _ghStorage;
        nodeToCellArr = new int[ghStorage.getNodes()];

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
        mincutAlgo = new EdmondsKarp(ghStorage, true);
    }

    void setAlgo() {
        mincutAlgo = new EdmondsKarp(ghStorage, false);
    }

    AbstractMaxFlowMinCutAlgorithm getAlgo() {
        return new EdmondsKarp();

    }

    public abstract void run();

    public static int[] getNodeToCellArr() {
        return nodeToCellArr;
    }
}
