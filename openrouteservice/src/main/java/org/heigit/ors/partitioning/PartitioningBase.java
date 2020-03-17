package org.heigit.ors.partitioning;

import com.carrotsearch.hppc.IntHashSet;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIterator;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.concurrent.ExecutorService;
/**
 *
 * <p>
 *
 * @author Hendrik Leuschner
 */
public abstract class PartitioningBase implements Runnable{
    int cellId;
    Graph ghGraph;
    IntHashSet nodeIdSet;
    AbstractMaxFlowMinCutAlgorithm mincutAlgo;
    EdgeFilter edgeFilter;
    PartitioningData pData;

    int[] nodeToCellArr;
    GraphHopperStorage ghStorage;
    ExecutorService executorService;

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
        this.nodeIdSet = new IntHashSet();
        this.ghGraph = ghStorage.getBaseGraph();
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

    public int[] getNodeToCellArr() {
        return nodeToCellArr;
    }
}
