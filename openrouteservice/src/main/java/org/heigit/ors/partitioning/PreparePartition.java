package org.heigit.ors.partitioning;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.Timer;
import java.util.concurrent.*;

import static heigit.ors.partitioning.FastIsochroneParameters.FASTISO_MAXTHREADCOUNT;

public class PreparePartition implements RoutingAlgorithmFactory {

    private Graph ghGraph;
    private EdgeIterator edgeIter;
    private EdgeExplorer ghEdgeExpl;
    private GraphHopperStorage ghStorage;
    private EdgeFilterSequence edgeFilters;
    private IsochroneNodeStorage isochroneNodeStorage;
    private CellStorage cellStorage;

    private int nodes;
    protected Timer timer;
    private int[] nodeCellId;
    private boolean[] nodeBorderness;
    private PartitioningBase partitioningAlgo;


    public PreparePartition(GraphHopperStorage ghStorage, EdgeFilterSequence edgeFilters) {
        this.ghStorage = ghStorage;
        this.ghGraph = ghStorage.getBaseGraph();
        this.edgeFilters = edgeFilters;
        this.isochroneNodeStorage = new IsochroneNodeStorage(ghStorage, ghStorage.getDirectory());
        this.cellStorage = new CellStorage(ghStorage, ghStorage.getDirectory(), isochroneNodeStorage);
        this.ghEdgeExpl = ghGraph.createEdgeExplorer();

        this.nodes = ghGraph.getNodes();
        this.nodeBorderness = new boolean[nodes];
    }

    public PreparePartition prepare() {
//        ForkJoinPool forkJoinPool = new ForkJoinPool(Math.min(FASTISO_MAXTHREADCOUNT, Runtime.getRuntime().availableProcessors()));
//        forkJoinPool.invoke(new InertialFlow(ghStorage, edgeFilters));
//        forkJoinPool.shutdown();
        ExecutorService threadPool = java.util.concurrent.Executors.newFixedThreadPool(Math.min(FASTISO_MAXTHREADCOUNT, Runtime.getRuntime().availableProcessors()));
        InverseSemaphore inverseSemaphore =  new InverseSemaphore();
        inverseSemaphore.beforeSubmit();
        System.out.println("Submitting task for cell 1");
        threadPool.execute(new InertialFlow(ghStorage, edgeFilters, threadPool, inverseSemaphore));
        try {
            inverseSemaphore.awaitCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        prepareData();

        //Create and calculate isochrone info that is ordered by node
        if (!isochroneNodeStorage.loadExisting()) {
            isochroneNodeStorage.setCellId(this.nodeCellId);
            isochroneNodeStorage.setBorderness(this.nodeBorderness);
            isochroneNodeStorage.flush();
        }

        //Info that is ordered by cell
        if (!cellStorage.loadExisting()) {
            cellStorage.init();
            cellStorage.calcCellNodesMap();
            cellStorage.flush();
        }

        freeMemory();
        return this;
    }


    private void prepareData() {
        this.nodeCellId = PartitioningBase.getNodeToCellArr();
        calcBorderNodes();
    }

    private void freeMemory() {
        partitioningAlgo = null;
    }

    /**
     * S-E-T
     **/
    public PreparePartition setGhStorage(GraphHopperStorage _ghStorage) {
        this.ghStorage = _ghStorage;
        return this;
    }


    private void calcBorderNodes() {
        boolean borderness = false;
        int adjNode;
        for (int baseNode = 0; baseNode < nodes; baseNode++) {
            edgeIter = ghEdgeExpl.setBaseNode(baseNode);
            while (edgeIter.next()) {
                adjNode = edgeIter.getAdjNode();
                if (nodeCellId[baseNode] != nodeCellId[adjNode])
                    borderness = true;
            }
            this.nodeBorderness[baseNode] = borderness;
            borderness = false;
        }
    }


    /**
     * G-E-T
     **/
    public int getNodes() {
        return nodes;
    }

//    public Weighting getWeighting() {
//        return weighting;
//    }

    @Override
    public RoutingAlgorithm createAlgo(Graph g, AlgorithmOptions opts) {
        return null;
    }

    public IsochroneNodeStorage getIsochroneNodeStorage() {
        return isochroneNodeStorage;
    }


    public CellStorage getCellStorage() {
        return cellStorage;
    }

    public class InverseSemaphore {
        private int value = 0;
        private Object lock = new Object();

        public void beforeSubmit() {
            synchronized(lock) {
                value++;
            }
        }

        public void taskCompleted() {
            synchronized(lock) {
                value--;
                if (value == 0) lock.notifyAll();
            }
        }

        public void awaitCompletion() throws InterruptedException {
            synchronized(lock) {
                while (value > 0) lock.wait();
            }
        }
    }
}
