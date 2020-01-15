package org.heigit.ors.partitioning;

import com.carrotsearch.hppc.IntHashSet;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIterator;
import heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.WayCategoryGraphStorage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RecursiveAction;

public abstract class PartitioningBase implements Runnable{

    public enum PartitionAlgo {FordFulkerson, FordFulkerson2, EdmondsKarp, Dinic}

    int cellId;
    Graph ghGraph;
    String[] partAlgoAll;
    IntHashSet nodeIdSet;
    AbstractMaxFlowMinCutAlgorithm mincutAlgo;
    EdgeFilter edgeFilter;
    PartitioningData pData;

    static int[] nodeToCellArr;
    static GraphHopperStorage ghStorage;
    ExecutorService executorService;
//    static WayCategoryGraphStorage storage;

    PartitioningBase() {
    }

    PartitioningBase(GraphHopperStorage _ghStorage, PartitioningData pData, EdgeFilterSequence edgeFilters, ExecutorService executorService) {
        ghStorage = _ghStorage;
        this.pData = pData;
        nodeToCellArr = new int[ghStorage.getNodes()];
        this.edgeFilter = edgeFilters;
        setExecutorService(executorService);

        init();
    }


    private void init() {
        this.partAlgoAll = new String[PartitionAlgo.values().length];
        this.nodeIdSet = new IntHashSet();
        this.ghGraph = ghStorage.getBaseGraph();
//        storage = GraphStorageUtils.getGraphExtension(ghStorage, WayCategoryGraphStorage.class);


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
        mincutAlgo = new EdmondsKarpAStar(ghStorage, pData, this.edgeFilter, true);
//        mincutAlgo.setAdditionalEdgeFilter(this.edgeFilter);
    }

    void setAlgo() {
        mincutAlgo = new EdmondsKarpAStar(ghStorage, pData, this.edgeFilter, false);
    }

    AbstractMaxFlowMinCutAlgorithm getAlgo() {
        return new EdmondsKarpAStar();

    }

    void setExecutorService(ExecutorService executorService){
        this.executorService = executorService;
    }

//    public abstract void run();

    public static int[] getNodeToCellArr() {
        return nodeToCellArr;
    }
}
