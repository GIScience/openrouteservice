package org.heigit.ors.fastisochrones.partitioning;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.concurrent.ExecutorService;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.*;

/**
 * Prepares the partition of the graph.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class PreparePartition implements RoutingAlgorithmFactory {

    private GraphHopperStorage ghStorage;
    private EdgeFilterSequence edgeFilters;
    private IsochroneNodeStorage isochroneNodeStorage;
    private CellStorage cellStorage;

    private int nodes;

    public PreparePartition(GraphHopperStorage ghStorage, EdgeFilterSequence edgeFilters) {
        this.ghStorage = ghStorage;
        this.edgeFilters = edgeFilters;
        this.isochroneNodeStorage = new IsochroneNodeStorage(ghStorage, ghStorage.getDirectory());
        this.cellStorage = new CellStorage(ghStorage, ghStorage.getDirectory(), isochroneNodeStorage);
        this.nodes = ghStorage.getBaseGraph().getNodes();
    }

    public PreparePartition prepare() {
        int[] nodeCellId = runInertialFlow();
        boolean[] nodeBorderness = calcBorderNodes(nodeCellId);

        //Create and calculate isochrone info that is ordered by node
        if (!isochroneNodeStorage.loadExisting()) {
            isochroneNodeStorage.setCellIds(nodeCellId);
            isochroneNodeStorage.setBorderness(nodeBorderness);
            isochroneNodeStorage.flush();
        }

        //Info that is ordered by cell
        if (!cellStorage.loadExisting()) {
            cellStorage.init();
            cellStorage.calcCellNodesMap();
            cellStorage.flush();
        }
        return this;
    }

    private int[] runInertialFlow() {
        int[] nodeToCellArray = new int[ghStorage.getNodes()];
        ExecutorService threadPool = java.util.concurrent.Executors.newFixedThreadPool(Math.min(getMaxThreadCount(), Runtime.getRuntime().availableProcessors()));
        InverseSemaphore inverseSemaphore = new InverseSemaphore();
        inverseSemaphore.beforeSubmit();
        if (isLogEnabled()) System.out.println("Submitting task for cell 1");
        threadPool.execute(new InertialFlow(nodeToCellArray, ghStorage, edgeFilters, threadPool, inverseSemaphore));
        try {
            inverseSemaphore.awaitCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadPool.shutdown();
        return nodeToCellArray;
    }

    /**
     * S-E-T
     **/
    public PreparePartition setGhStorage(GraphHopperStorage _ghStorage) {
        this.ghStorage = _ghStorage;
        return this;
    }

    private boolean[] calcBorderNodes(int[] nodeCellId) {
        boolean[] nodeBorderness = new boolean[this.nodes];
        boolean borderness = false;
        int adjNode;
        EdgeExplorer ghEdgeExpl = ghStorage.getBaseGraph().createEdgeExplorer();
        EdgeIterator edgeIter;
        for (int baseNode = 0; baseNode < nodes; baseNode++) {
            edgeIter = ghEdgeExpl.setBaseNode(baseNode);
            while (edgeIter.next()) {
                adjNode = edgeIter.getAdjNode();
                if (nodeCellId[baseNode] != nodeCellId[adjNode])
                    borderness = true;
            }
            nodeBorderness[baseNode] = borderness;
            borderness = false;
        }
        return nodeBorderness;
    }

    public int getNodes() {
        return nodes;
    }

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
}
