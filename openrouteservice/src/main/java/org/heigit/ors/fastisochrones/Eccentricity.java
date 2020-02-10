package org.heigit.ors.fastisochrones;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
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
import java.util.List;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;


import static org.heigit.ors.partitioning.FastIsochroneParameters.FASTISO_MAXTHREADCOUNT;
import static org.heigit.ors.partitioning.FastIsochroneParameters.PART__MAX_CELL_NODES_NUMBER;


public class Eccentricity extends AbstractEccentricity {

    double acceptedFullyReachablePercentage = 0.995;
    int eccentricityDijkstraLimitFactor = 10;
    LocationIndex locationIndex;

    public Eccentricity(GraphHopperStorage graphHopperStorage, LocationIndex locationIndex){
        super(graphHopperStorage);
        this.locationIndex = locationIndex;
    }

        public void calcEccentricities(GraphHopperStorage ghStorage, Graph graph, Weighting weighting, FlagEncoder flagEncoder, TraversalMode traversalMode, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage) {
            if(eccentricityStorages == null) {
                eccentricityStorages = new ArrayList<>();
            }
            EccentricityStorage eccentricityStorage = new EccentricityStorage(ghStorage, ghStorage.getDirectory(), weighting);
            if(!eccentricityStorage.loadExisting())
                eccentricityStorage.init();
            ExecutorService threadPool = java.util.concurrent.Executors.newFixedThreadPool(Math.min(FASTISO_MAXTHREADCOUNT, Runtime.getRuntime().availableProcessors()));

            ExecutorCompletionService completionService = new ExecutorCompletionService<>(threadPool);

            EdgeFilter defaultEdgeFilter = DefaultEdgeFilter.outEdges(flagEncoder);

            IntObjectHashMap relevantNodesSets = new IntObjectHashMap(isochroneNodeStorage.getCellIds().size());
            for(int cellId : isochroneNodeStorage.getCellIds()){
                relevantNodesSets.put(cellId, getRelevantContourNodes(cellId, cellStorage, isochroneNodeStorage));
            }

            //Calculate the eccentricity without fixed cell edge filter for now
    //        edgeFilterSequence.add(fixedCellEdgeFilter);
            int borderNodeCount = 0;
            for (int borderNode = 0; borderNode < graph.getNodes(); borderNode++){
                if(!isochroneNodeStorage.getBorderness(borderNode))
                    continue;
                final int node = borderNode;
                borderNodeCount++;
                completionService.submit(() -> {
                    //First run dijkstra in cell
                    EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
                    FixedCellEdgeFilter fixedCellEdgeFilter = new FixedCellEdgeFilter(isochroneNodeStorage, isochroneNodeStorage.getCellId(node), graph.getNodes());
                    edgeFilterSequence.add(defaultEdgeFilter);
                    edgeFilterSequence.add(fixedCellEdgeFilter);
                    RangeDijkstra rangeDijkstra = new RangeDijkstra(graph, weighting, traversalMode);
                    rangeDijkstra.setMaxVisitedNodes(PART__MAX_CELL_NODES_NUMBER * eccentricityDijkstraLimitFactor);
                    rangeDijkstra.setAcceptedFullyReachablePercentage(1.0);
                    rangeDijkstra.setEdgeFilter(edgeFilterSequence);
                    rangeDijkstra.setCellNodes(cellStorage.getNodesOfCell(isochroneNodeStorage.getCellId(node)));
                    double eccentricity = rangeDijkstra.calcMaxWeight(node, (IntHashSet)relevantNodesSets.get(isochroneNodeStorage.getCellId(node)));
                    int cellNodeCount = cellStorage.getNodesOfCell(isochroneNodeStorage.getCellId(node)).size();
//                    System.out.println("node: " + node + " took visitedNodes: " + rangeDijkstra.visitedNodes + " took calcs: " + rangeDijkstra.calcs + " for  ecc: " + eccentricity + " with foundCellNode %: " + ((double) rangeDijkstra.getFoundCellNodeSize()) / cellNodeCount);
                    //Rerun outside of cell if not enough nodes were found in first run
                    if (((double) rangeDijkstra.getFoundCellNodeSize()) / cellNodeCount < acceptedFullyReachablePercentage) {
                        rangeDijkstra.setAcceptedFullyReachablePercentage(acceptedFullyReachablePercentage);
                        edgeFilterSequence = new EdgeFilterSequence();
                        edgeFilterSequence.add(defaultEdgeFilter);
                        rangeDijkstra.setEdgeFilter(edgeFilterSequence);
                        eccentricity = rangeDijkstra.calcMaxWeight(node, (IntHashSet)relevantNodesSets.get(isochroneNodeStorage.getCellId(node)));
//                        System.out.println("node: " + node + " took visitedNodes: " + rangeDijkstra.visitedNodes + " took calcs: " + rangeDijkstra.calcs + " for  ecc: " + eccentricity + " with foundCellNode %: " + ((double) rangeDijkstra.getFoundCellNodeSize()) / cellNodeCount);
                    }

                    //TODO This is really just a cheap workaround that should be something smart instead
                    //If set to 1, it is okay though
                    if (((double) rangeDijkstra.getFoundCellNodeSize()) / cellNodeCount >= acceptedFullyReachablePercentage) {
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

    private IntHashSet getRelevantContourNodes(int cellId, CellStorage cellStorage, IsochroneNodeStorage isochroneNodeStorage){
        List<Double> contourCoordinates = cellStorage.getCellContourOrder(cellId);
        FixedCellEdgeFilter fixedCellEdgeFilter = new FixedCellEdgeFilter(isochroneNodeStorage, cellId, Integer.MAX_VALUE);
        int j = 0;
        IntHashSet contourNodes = new IntHashSet();
        while (j < contourCoordinates.size()){
            double latitude = contourCoordinates.get(j);
            j++;
            double longitude = contourCoordinates.get(j);
            j++;
            int nodeId = locationIndex.findClosest(latitude, longitude, fixedCellEdgeFilter).getClosestNode();
            contourNodes.add(nodeId);
        }
        return contourNodes;
    }
}
