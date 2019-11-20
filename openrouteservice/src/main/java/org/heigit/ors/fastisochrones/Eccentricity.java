package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;

import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.partitioning.CellStorage;
import org.heigit.ors.partitioning.IsochroneNodeStorage;
import org.heigit.ors.partitioning.EccentricityStorage;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.ArrayList;
import java.util.concurrent.ExecutorCompletionService;

import static org.heigit.ors.partitioning.FastIsochroneParameters.PART__MAX_CELL_NODES_NUMBER;


public class Eccentricity extends AbstractEccentricity {

    double acceptedFullyReachablePercentage = 1.0;
    int eccentricityDijkstraLimitFactor = 10;

    public Eccentricity(GraphHopperStorage graphHopperStorage){
        super(graphHopperStorage);
    }

        public void calcEccentricities(GraphHopperStorage ghStorage, Graph graph, Weighting weighting, FlagEncoder flagEncoder, TraversalMode traversalMode, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage) {
            if(eccentricityStorages == null) {
                eccentricityStorages = new ArrayList<>();
            }
            EccentricityStorage eccentricityStorage = new EccentricityStorage(ghStorage, ghStorage.getDirectory(), weighting);
            if(!eccentricityStorage.loadExisting())
                eccentricityStorage.init();

            ExecutorCompletionService completionService = new ExecutorCompletionService<>(threadPool);

    //        FixedCellEdgeFilter fixedCellEdgeFilter = new FixedCellEdgeFilter(isochroneNodeStorage, 0, graph.getNodes());
            EdgeFilter defaultEdgeFilter = new DefaultEdgeFilter(flagEncoder, false, true);

            //Calculate the eccentricity without fixed cell edge filter for now
    //        edgeFilterSequence.add(fixedCellEdgeFilter);
            int borderNodeCount = 0;
            for (int borderNode = 0; borderNode < graph.getNodes(); borderNode++){
                if(!isochroneNodeStorage.getBorderness(borderNode))
                    continue;
                final int node = borderNode;
                borderNodeCount++;
    //            fixedCellEdgeFilter.setCellId(isochroneNodeStorage.getCellId(borderNode));
                completionService.submit(() -> {
                    EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
                    edgeFilterSequence.add(defaultEdgeFilter);
                    RangeDijkstra rangeDijkstra = new RangeDijkstra(graph, weighting, traversalMode);
                    rangeDijkstra.setMaxVisitedNodes(PART__MAX_CELL_NODES_NUMBER * eccentricityDijkstraLimitFactor);
                    rangeDijkstra.setEdgeFilter(edgeFilterSequence);
                    rangeDijkstra.setCellNodes(cellStorage.getNodesOfCell(isochroneNodeStorage.getCellId(node)));
                    double eccentricity = rangeDijkstra.calcMaxWeight(node);
                    int visitedNodesInCell = rangeDijkstra.fromMap.size();
                    int cellNodeCount = cellStorage.getNodesOfCell(isochroneNodeStorage.getCellId(node)).size();

                    //TODO This is really just a cheap workaround that should really really be something smart instead
                    //If set to 1, it is okay though
                    if (((double) visitedNodesInCell) / cellNodeCount >= acceptedFullyReachablePercentage) {
                        eccentricityStorage.setFullyReachable(node, true);
                    }
                    else {
                        eccentricityStorage.setFullyReachable(node, false);
                    }

                    eccentricityStorage.setEccentricity(node, eccentricity);
                }, String.valueOf(node));
            }

        threadPool.shutdown();

        try {
            for (int i = 0; i < borderNodeCount; i++) {
                completionService.take().get();
            }
        } catch (Exception e) {
            threadPool.shutdownNow();
            throw new RuntimeException(e);
        }

        eccentricityStorage.flush();
        eccentricityStorages.add(eccentricityStorage);
    }

    @Override
    public void calcEccentricities() {
        calcEccentricities(this.ghStorage, this.baseGraph, this.weighting, this.encoder, this.traversalMode, this.isochroneNodeStorage, this.cellStorage);
    }
}
