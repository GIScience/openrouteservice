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
import org.heigit.ors.fastisochrones.partitioning.storage.*;
import org.heigit.ors.routing.algorithms.DijkstraOneToManyAlgorithm;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.getMaxCellNodesNumber;
import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.getMaxThreadCount;

/**
 * Eccentricity implementation. Calculates the maximum value of all shortest paths within a cell given a starting bordernode.
 * Further calculates all distance pairs of bordernodes.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class Eccentricity extends AbstractEccentricity {
    //This value determines how many nodes of a cell need to be reached in order for the cell to count as fully reachable.
    //Some nodes might be part of a cell but unreachable (disconnected, behind infinite weight, ...)
    double acceptedFullyReachablePercentage = 0.995;
    int eccentricityDijkstraLimitFactor = 10;
    LocationIndex locationIndex;

    public Eccentricity(GraphHopperStorage graphHopperStorage, LocationIndex locationIndex, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage) {
        super(graphHopperStorage);
        this.locationIndex = locationIndex;
        this.isochroneNodeStorage = isochroneNodeStorage;
        this.cellStorage = cellStorage;
    }

    public void calcEccentricities(Graph graph, Weighting weighting, FlagEncoder flagEncoder, TraversalMode traversalMode, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage) {
        if (eccentricityStorages == null) {
            eccentricityStorages = new ArrayList<>();
        }
        EccentricityStorage eccentricityStorage = getEccentricityStorage(weighting);
        if (!eccentricityStorage.loadExisting())
            eccentricityStorage.init();
        ExecutorService threadPool = java.util.concurrent.Executors.newFixedThreadPool(Math.min(getMaxThreadCount(), Runtime.getRuntime().availableProcessors()));

        ExecutorCompletionService<String> completionService = new ExecutorCompletionService<>(threadPool);

        EdgeFilter defaultEdgeFilter = DefaultEdgeFilter.outEdges(flagEncoder);

        IntObjectHashMap<IntHashSet> relevantNodesSets = new IntObjectHashMap<>(isochroneNodeStorage.getCellIds().size());
        for (IntCursor cellId : isochroneNodeStorage.getCellIds()) {
            relevantNodesSets.put(cellId.value, getRelevantContourNodes(cellId.value, cellStorage, isochroneNodeStorage));
        }

        //Calculate the eccentricity without fixed cell edge filter for now
        int borderNodeCount = 0;
        for (int borderNode = 0; borderNode < graph.getNodes(); borderNode++) {
            if (!isochroneNodeStorage.getBorderness(borderNode))
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
                rangeDijkstra.setMaxVisitedNodes(getMaxCellNodesNumber() * eccentricityDijkstraLimitFactor);
                rangeDijkstra.setAcceptedFullyReachablePercentage(1.0);
                rangeDijkstra.setEdgeFilter(edgeFilterSequence);
                rangeDijkstra.setCellNodes(cellStorage.getNodesOfCell(isochroneNodeStorage.getCellId(node)));
                double eccentricity = rangeDijkstra.calcMaxWeight(node, relevantNodesSets.get(isochroneNodeStorage.getCellId(node)));
                int cellNodeCount = cellStorage.getNodesOfCell(isochroneNodeStorage.getCellId(node)).size();
                //Rerun outside of cell if not enough nodes were found in first run
                if (((double) rangeDijkstra.getFoundCellNodeSize()) / cellNodeCount < acceptedFullyReachablePercentage) {
                    rangeDijkstra.setAcceptedFullyReachablePercentage(acceptedFullyReachablePercentage);
                    edgeFilterSequence = new EdgeFilterSequence();
                    edgeFilterSequence.add(defaultEdgeFilter);
                    rangeDijkstra.setEdgeFilter(edgeFilterSequence);
                    eccentricity = rangeDijkstra.calcMaxWeight(node, relevantNodesSets.get(isochroneNodeStorage.getCellId(node)));
                }

                //TODO Maybe implement a smarter logic than having some high percentage for acceptedFullyReachable
                boolean isFullyReachable = ((double) rangeDijkstra.getFoundCellNodeSize()) / cellNodeCount >= acceptedFullyReachablePercentage;
                eccentricityStorage.setFullyReachable(node, isFullyReachable);

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
    }

    @Override
    public void calcEccentricities() {
        calcEccentricities(this.baseGraph, this.weighting, this.encoder, this.traversalMode, this.isochroneNodeStorage, this.cellStorage);
    }

    public void calcCoreGraphDistances(Graph graph, Weighting weighting, FlagEncoder flagEncoder, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage) {
        if (borderNodeDistanceStorages == null) {
            borderNodeDistanceStorages = new ArrayList<>();
        }
        BorderNodeDistanceStorage borderNodeDistanceStorage = getBorderNodeDistanceStorage(weighting);
        if (!borderNodeDistanceStorage.loadExisting())
            borderNodeDistanceStorage.init();

        ExecutorService threadPool = java.util.concurrent.Executors.newFixedThreadPool(Math.min(getMaxThreadCount(), Runtime.getRuntime().availableProcessors()));
        ExecutorCompletionService<String> completionService = new ExecutorCompletionService<>(threadPool);

        int cellCount = 0;
        for (IntCursor cellId : isochroneNodeStorage.getCellIds()) {
            final int currentCellId = cellId.value;
            cellCount++;
            completionService.submit(() -> calculateBorderNodeDistances(borderNodeDistanceStorage, currentCellId, graph, weighting, flagEncoder, isochroneNodeStorage, cellStorage), String.valueOf(currentCellId));
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

        for (int borderNode : cellBorderNodes) {
            DijkstraOneToManyAlgorithm algorithm = new DijkstraOneToManyAlgorithm(graph, weighting, TraversalMode.NODE_BASED);
            algorithm.setEdgeFilter(defaultEdgeFilter);
            algorithm.prepare(new int[]{borderNode}, cellBorderNodes);
            algorithm.setMaxVisitedNodes(getMaxCellNodesNumber() * 20);
            SPTEntry[] targets = algorithm.calcPaths(borderNode, cellBorderNodes);
            int[] ids = new int[targets.length];
            double[] distances = new double[targets.length];
            for (int i = 0; i < targets.length; i++) {
                ids[i] = cellBorderNodes[i];
                if (targets[i] == null) {
                    distances[i] = Double.POSITIVE_INFINITY;
                } else if (targets[i].adjNode == borderNode) {
                    distances[i] = 0;
                } else
                    distances[i] = targets[i].weight;
            }
            borderNodeDistanceStorage.storeBorderNodeDistanceSet(borderNode, new BorderNodeDistanceSet(ids, distances));
        }
    }

    private IntHashSet getBorderNodesOfCell(int cellId, CellStorage cellStorage, IsochroneNodeStorage isochroneNodeStorage) {
        IntHashSet borderNodes = new IntHashSet();
        for (IntCursor node : cellStorage.getNodesOfCell(cellId)) {
            if (isochroneNodeStorage.getBorderness(node.value))
                borderNodes.add(node.value);
        }
        return borderNodes;
    }

    private IntHashSet getRelevantContourNodes(int cellId, CellStorage cellStorage, IsochroneNodeStorage isochroneNodeStorage) {
        List<Double> contourCoordinates = cellStorage.getCellContourOrder(cellId);
        FixedCellEdgeFilter fixedCellEdgeFilter = new FixedCellEdgeFilter(isochroneNodeStorage, cellId, Integer.MAX_VALUE);
        int j = 0;
        IntHashSet contourNodes = new IntHashSet();
        while (j < contourCoordinates.size()) {
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
