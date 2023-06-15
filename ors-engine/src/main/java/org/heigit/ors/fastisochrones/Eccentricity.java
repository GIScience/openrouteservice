package org.heigit.ors.fastisochrones;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.storage.index.LocationIndex;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.fastisochrones.storage.BorderNodeDistanceSet;
import org.heigit.ors.fastisochrones.storage.BorderNodeDistanceStorage;
import org.heigit.ors.fastisochrones.storage.EccentricityStorage;
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
    private static final double ACCEPTED_FULLY_REACHABLE_PERCENTAGE = 0.995;
    //This factor determines how far the Dijkstra should search for a path to find the eccentricity if it cannot be found inside the cell.
    //A factor of 10 means that the Dijkstra will search an area of 10 * maxCellNodesNumber.
    //This is needed to get a better estimate on the eccentricity, but not run a Dijkstra on the whole graph to find it.
    private static final int ECCENTRICITY_DIJKSTRA_LIMIT_FACTOR = 10;
    private final LocationIndex locationIndex;

    public Eccentricity(GraphHopperStorage graphHopperStorage, LocationIndex locationIndex, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage) {
        super(graphHopperStorage);
        this.locationIndex = locationIndex;
        this.isochroneNodeStorage = isochroneNodeStorage;
        this.cellStorage = cellStorage;
    }

    public void calcEccentricities(Weighting weighting, EdgeFilter additionalEdgeFilter, FlagEncoder flagEncoder) {
        if (eccentricityStorages == null) {
            eccentricityStorages = new ArrayList<>();
        }
        EccentricityStorage eccentricityStorage = getEccentricityStorage(weighting);
        Graph graph = ghStorage.getBaseGraph();
        if (!eccentricityStorage.loadExisting())
            eccentricityStorage.init();
        ExecutorService threadPool = java.util.concurrent.Executors.newFixedThreadPool(Math.min(getMaxThreadCount(), Runtime.getRuntime().availableProcessors()));

        ExecutorCompletionService<String> completionService = new ExecutorCompletionService<>(threadPool);

        EdgeFilter defaultEdgeFilter = AccessFilter.outEdges(flagEncoder.getAccessEnc());

        IntObjectHashMap<IntHashSet> relevantNodesSets = new IntObjectHashMap<>(isochroneNodeStorage.getCellIds().size());
        for (IntCursor cellId : isochroneNodeStorage.getCellIds()) {
            relevantNodesSets.put(cellId.value, getRelevantContourNodes(cellId.value, cellStorage, isochroneNodeStorage));
        }

        //Calculate the eccentricity via RangeDijkstra
        int borderNodeCount = 0;
        for (int borderNode = 0; borderNode < graph.getNodes(); borderNode++) {
            if (!isochroneNodeStorage.getBorderness(borderNode))
                continue;
            final int node = borderNode;
            borderNodeCount++;
            completionService.submit(() -> {
                //First run dijkstra only in cell and try to find _all_ nodes in the cell
                EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
                FixedCellEdgeFilter fixedCellEdgeFilter = new FixedCellEdgeFilter(isochroneNodeStorage, isochroneNodeStorage.getCellId(node), graph.getNodes());
                edgeFilterSequence.add(defaultEdgeFilter);
                edgeFilterSequence.add(fixedCellEdgeFilter);
                edgeFilterSequence.add(additionalEdgeFilter);
                RangeDijkstra rangeDijkstra = new RangeDijkstra(graph, weighting);
                rangeDijkstra.setMaxVisitedNodes(getMaxCellNodesNumber() * ECCENTRICITY_DIJKSTRA_LIMIT_FACTOR);
                rangeDijkstra.setEdgeFilter(edgeFilterSequence);
                rangeDijkstra.setCellNodes(cellStorage.getNodesOfCell(isochroneNodeStorage.getCellId(node)));
                double eccentricity = rangeDijkstra.calcMaxWeight(node, relevantNodesSets.get(isochroneNodeStorage.getCellId(node)));
                int cellNodeCount = cellStorage.getNodesOfCell(isochroneNodeStorage.getCellId(node)).size();
                //Rerun outside of cell if not enough nodes were found in first run, but try to find almost all
                //Sometimes nodes in a cell cannot be found, but we do not want to search the entire graph each time, so we limit the Dijkstra
                if (((double) rangeDijkstra.getFoundCellNodeSize()) / cellNodeCount < ACCEPTED_FULLY_REACHABLE_PERCENTAGE) {
                    rangeDijkstra = new RangeDijkstra(graph, weighting);
                    rangeDijkstra.setMaxVisitedNodes(getMaxCellNodesNumber() * ECCENTRICITY_DIJKSTRA_LIMIT_FACTOR);
                    rangeDijkstra.setEdgeFilter(edgeFilterSequence);
                    rangeDijkstra.setCellNodes(cellStorage.getNodesOfCell(isochroneNodeStorage.getCellId(node)));
                    edgeFilterSequence = new EdgeFilterSequence();
                    edgeFilterSequence.add(defaultEdgeFilter);
                    rangeDijkstra.setEdgeFilter(edgeFilterSequence);
                    eccentricity = rangeDijkstra.calcMaxWeight(node, relevantNodesSets.get(isochroneNodeStorage.getCellId(node)));
                }

                //TODO Maybe implement a logic smarter than having some high percentage for acceptedFullyReachable
                boolean isFullyReachable = ((double) rangeDijkstra.getFoundCellNodeSize()) / cellNodeCount >= ACCEPTED_FULLY_REACHABLE_PERCENTAGE;
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

    public void calcBorderNodeDistances(Weighting weighting, EdgeFilter additionalEdgeFilter, FlagEncoder flagEncoder) {
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
            completionService.submit(() -> calculateBorderNodeDistances(borderNodeDistanceStorage, additionalEdgeFilter, currentCellId, weighting, flagEncoder), String.valueOf(currentCellId));
        }

        threadPool.shutdown();

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

    private void calculateBorderNodeDistances(BorderNodeDistanceStorage borderNodeDistanceStorage, EdgeFilter additionalEdgeFilter, int cellId, Weighting weighting, FlagEncoder flagEncoder) {
        int[] cellBorderNodes = getBorderNodesOfCell(cellId, cellStorage, isochroneNodeStorage).toArray();
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        EdgeFilter defaultEdgeFilter = AccessFilter.outEdges(flagEncoder.getAccessEnc());
        edgeFilterSequence.add(defaultEdgeFilter);
        edgeFilterSequence.add(additionalEdgeFilter);
        Graph graph = ghStorage.getBaseGraph();

        for (int borderNode : cellBorderNodes) {
            DijkstraOneToManyAlgorithm algorithm = new DijkstraOneToManyAlgorithm(graph, weighting, TraversalMode.NODE_BASED);
            algorithm.setEdgeFilter(edgeFilterSequence);
            algorithm.prepare(new int[]{borderNode}, cellBorderNodes);
            algorithm.setMaxVisitedNodes(getMaxCellNodesNumber() * 20);
            SPTEntry[] targets = algorithm.calcPaths(borderNode, cellBorderNodes);
            int[] ids = new int[targets.length - 1];
            double[] distances = new double[targets.length - 1];
            int index = 0;
            for (int i = 0; i < targets.length; i++) {
                if (cellBorderNodes[i] == borderNode)
                    continue;
                ids[index] = cellBorderNodes[i];
                if (targets[i] == null) {
                    distances[index] = Double.POSITIVE_INFINITY;
                } else if (targets[i].adjNode == borderNode) {
                    distances[index] = 0;
                } else
                    distances[index] = targets[i].weight;
                index++;
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
        if (this.locationIndex == null)
            return cellStorage.getNodesOfCell(cellId);
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
