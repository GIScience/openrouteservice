package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.heigit.ors.fastisochrones.partitioning.Projector.Projection;

public abstract class PartitioningBase implements Runnable {
    protected Map<Projection, IntArrayList> projections;
    int cellId;
    Graph ghGraph;
    EdgeFilter edgeFilter;
    PartitioningData pData;
    int[] nodeToCellArr;
    GraphHopperStorage ghStorage;
    ExecutorService executorService;

    PartitioningBase() {
    }

    PartitioningBase(int[] nodeToCellArray, GraphHopperStorage _ghStorage, PartitioningData pData, EdgeFilterSequence edgeFilters, ExecutorService executorService) {
        ghStorage = _ghStorage;
        this.pData = pData;
        this.nodeToCellArr = nodeToCellArray;
        this.edgeFilter = edgeFilters;
        setExecutorService(executorService);
        this.ghGraph = ghStorage.getBaseGraph();
    }



    public MaxFlowMinCut initAlgo() {
        return new EdmondsKarpAStar(ghStorage, pData, this.edgeFilter, true);
    }

    public MaxFlowMinCut setAlgo() {
        return new EdmondsKarpAStar(ghStorage, pData, this.edgeFilter, false);
    }

    void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }



}
