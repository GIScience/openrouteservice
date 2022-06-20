package org.heigit.ors.fastisochrones.partitioning;

import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.getMaxThreadCount;

/**
 * Prepares the partition of the graph.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class PreparePartition {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreparePartition.class);
    private final GraphHopperStorage ghStorage;
    private final EdgeFilterSequence edgeFilters;
    private final IsochroneNodeStorage isochroneNodeStorage;
    private final CellStorage cellStorage;
    private final int nodes;

    public PreparePartition(GraphHopperStorage ghStorage, EdgeFilterSequence edgeFilters) {
        this.ghStorage = ghStorage;
        this.edgeFilters = edgeFilters;
        this.nodes = ghStorage.getBaseGraph().getNodes();
        this.isochroneNodeStorage = new IsochroneNodeStorage(this.nodes, ghStorage.getDirectory());
        this.cellStorage = new CellStorage(this.nodes, ghStorage.getDirectory(), isochroneNodeStorage);
    }

    public PreparePartition prepare() {
        //Use Inertialflow to calculate node id to cell
        int[] nodeCellId = runInertialFlow();
        //Identify border nodes
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
        LOGGER.debug("Submitting task for cell 1");
        threadPool.execute(new InertialFlow(nodeToCellArray, ghStorage, edgeFilters, threadPool, inverseSemaphore));
        try {
            inverseSemaphore.awaitCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error(e.getLocalizedMessage());
        }

        threadPool.shutdown();
        return nodeToCellArray;
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
                if (nodeCellId[baseNode] != nodeCellId[adjNode]) {
                    borderness = true;
                    break;
                }
            }
            nodeBorderness[baseNode] = borderness;
            borderness = false;
        }
        return nodeBorderness;
    }

    public IsochroneNodeStorage getIsochroneNodeStorage() {
        return isochroneNodeStorage;
    }

    public CellStorage getCellStorage() {
        return cellStorage;
    }
}
