package org.heigit.ors.fastisochrones;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.storage.index.LocationIndex;
import org.heigit.ors.partitioning.*;
import org.heigit.ors.routing.algorithms.DijkstraOneToManyAlgorithm;
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

    public Eccentricity(GraphHopperStorage graphHopperStorage, LocationIndex locationIndex, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage){
        super(graphHopperStorage);
        this.locationIndex = locationIndex;
        this.isochroneNodeStorage = isochroneNodeStorage;
        this.cellStorage = cellStorage;
    }

    public void calcEccentricities(GraphHopperStorage ghStorage, Graph graph, Weighting weighting, FlagEncoder flagEncoder, TraversalMode traversalMode, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage) {
        if(eccentricityStorages == null) {
            eccentricityStorages = new ArrayList<>();
        }
        EccentricityStorage eccentricityStorage = getEccentricityStorage(weighting);
        if(!eccentricityStorage.loadExisting())
            eccentricityStorage.init();
        ExecutorService threadPool = java.util.concurrent.Executors.newFixedThreadPool(Math.min(FASTISO_MAXTHREADCOUNT, Runtime.getRuntime().availableProcessors()));

        ExecutorCompletionService completionService = new ExecutorCompletionService<>(threadPool);

        EdgeFilter defaultEdgeFilter = DefaultEdgeFilter.outEdges(flagEncoder);

        IntObjectHashMap relevantNodesSets = new IntObjectHashMap(isochroneNodeStorage.getCellIds().size());
        for(IntCursor cellId : isochroneNodeStorage.getCellIds()){
            relevantNodesSets.put(cellId.value, getRelevantContourNodes(cellId.value, cellStorage, isochroneNodeStorage));
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

        eccentricityStorage.storeBorderNodeToPointerMap();
        eccentricityStorage.flush();
//        eccentricityStorages.add(eccentricityStorage);
    }

    @Override
    public void calcEccentricities() {
        calcEccentricities(this.ghStorage, this.baseGraph, this.weighting, this.encoder, this.traversalMode, this.isochroneNodeStorage, this.cellStorage);
    }

    public void calcCoreGraphDistances(Graph graph, Weighting weighting, FlagEncoder flagEncoder, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage){
        if(borderNodeDistanceStorages == null) {
            borderNodeDistanceStorages = new ArrayList<>();
        }
        BorderNodeDistanceStorage borderNodeDistanceStorage = getBorderNodeDistanceStorage(weighting);
        if(!borderNodeDistanceStorage.loadExisting())
            borderNodeDistanceStorage.init();

        ExecutorService threadPool = java.util.concurrent.Executors.newFixedThreadPool(Math.min(FASTISO_MAXTHREADCOUNT, Runtime.getRuntime().availableProcessors()));
        ExecutorCompletionService completionService = new ExecutorCompletionService<>(threadPool);

        int cellCount = 0;
        for (IntCursor cellId : isochroneNodeStorage.getCellIds()){
            final int currentCellId = cellId.value;
            cellCount++;
            completionService.submit(() -> {
                calculateBorderNodeDistances(borderNodeDistanceStorage, currentCellId, graph, weighting, flagEncoder, isochroneNodeStorage, cellStorage);

            }, String.valueOf(currentCellId));
        }


        threadPool.shutdown();
//
        try {
            for (int i = 0; i < cellCount; i++) {
                completionService.take().get();
            }
        } catch (Exception e) {
            threadPool.shutdownNow();
            throw new RuntimeException(e);
        }
        borderNodeDistanceStorage.storeBorderNodeToPointerMap();
        borderNodeDistanceStorage.flush();
    }

    public void calculateBorderNodeDistances(BorderNodeDistanceStorage borderNodeDistanceStorage, int cellId, Graph graph, Weighting weighting, FlagEncoder flagEncoder, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage) {
        int[] cellBorderNodes = getBorderNodesOfCell(cellId, cellStorage, isochroneNodeStorage).toArray();
        EdgeFilter defaultEdgeFilter = DefaultEdgeFilter.outEdges(flagEncoder);

        for(int borderNode : cellBorderNodes) {
            DijkstraOneToManyAlgorithm algorithm = new DijkstraOneToManyAlgorithm(graph, weighting, TraversalMode.NODE_BASED);
            algorithm.setEdgeFilter(defaultEdgeFilter);
            algorithm.prepare(new int[]{borderNode}, cellBorderNodes);
            algorithm.setMaxVisitedNodes(PART__MAX_CELL_NODES_NUMBER * 20);
            SPTEntry[] targets = algorithm.calcPaths(borderNode, cellBorderNodes);
            int[] ids = new int[targets.length];
            double[] distances = new double[targets.length];
            for(int i = 0; i < targets.length; i++){
                ids[i] = cellBorderNodes[i];
                if(targets[i] == null){
                    distances[i] = Double.POSITIVE_INFINITY;
                    continue;
                }
                if(targets[i].adjNode == borderNode){
                    distances[i] = 0;
                    continue;
                }
                distances[i] = targets[i].weight;
            }
            borderNodeDistanceStorage.storeBorderNodeDistanceSet(borderNode, new BorderNodeDistanceSet(ids, distances));
        }
    }

    private IntHashSet getBorderNodesOfCell(int cellId, CellStorage cellStorage, IsochroneNodeStorage isochroneNodeStorage){
        IntHashSet borderNodes =  new IntHashSet();
        for(IntCursor node : cellStorage.getNodesOfCell(cellId)){
            if(isochroneNodeStorage.getBorderness(node.value))
                borderNodes.add(node.value);
        }
        return borderNodes;
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
