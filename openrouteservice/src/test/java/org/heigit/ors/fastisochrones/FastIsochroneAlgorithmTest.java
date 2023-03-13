package org.heigit.ors.fastisochrones;

import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FastIsochroneAlgorithmTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private IsochroneNodeStorage ins;
    private CellStorage cs;

    private void createMockStorages(GraphHopperStorage ghStorage) {
        IsochroneNodeStorage isochroneNodeStorage = new IsochroneNodeStorage(10, ghStorage.getDirectory());
        int[] cellIds = new int[]{2, 2, 2, 2, 3, 3, 3, 3, 2, 3};
        boolean[] borderNess = new boolean[]{false, false, false, true, true, false, false, true, true, false};
        isochroneNodeStorage.setCellIds(cellIds);
        isochroneNodeStorage.setBorderness(borderNess);

        CellStorage cellStorage = new CellStorage(10, ghStorage.getDirectory(), isochroneNodeStorage);
        cellStorage.init();
        cellStorage.calcCellNodesMap();
        this.ins = isochroneNodeStorage;
        this.cs = cellStorage;
    }

    @Test
    void testExactWeightActiveCell() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraphWithAdditionalEdge(encodingManager);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);

        ecc.loadExisting(shortestWeighting);
        ecc.calcEccentricities(shortestWeighting, new EdgeFilterSequence(), carEncoder);
        ecc.calcBorderNodeDistances(shortestWeighting, new EdgeFilterSequence(), carEncoder);
        FastIsochroneAlgorithm fastIsochroneAlgorithm = new FastIsochroneAlgorithm(
                graphHopperStorage.getBaseGraph(),
                shortestWeighting,
                TraversalMode.NODE_BASED,
                cs,
                ins,
                ecc.getEccentricityStorage(shortestWeighting),
                ecc.getBorderNodeDistanceStorage(shortestWeighting),
                null);

        fastIsochroneAlgorithm.calcIsochroneNodes(1, 5.0);
        Set<Integer> nodeIds = new HashSet<>();
        Set<Integer> expectedNodeIds = new HashSet<>();
        expectedNodeIds.add(4);
        expectedNodeIds.add(7);

        for (IntObjectCursor<SPTEntry> entry : fastIsochroneAlgorithm.getActiveCellMaps().get(3)) {
            nodeIds.add(entry.value.adjNode);
        }
        assertEquals(expectedNodeIds, nodeIds);
        assertEquals(5.0, fastIsochroneAlgorithm.getActiveCellMaps().get(3).get(4).weight, 1e-10);
        assertEquals(5.0, fastIsochroneAlgorithm.getActiveCellMaps().get(3).get(7).weight, 1e-10);
    }

    @Test
    void testLimitInBetweenNodesActiveCell() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraphWithAdditionalEdge(encodingManager);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);

        ecc.loadExisting(shortestWeighting);
        ecc.calcEccentricities(shortestWeighting, new EdgeFilterSequence(), carEncoder);
        ecc.calcBorderNodeDistances(shortestWeighting, new EdgeFilterSequence(), carEncoder);
        FastIsochroneAlgorithm fastIsochroneAlgorithm = new FastIsochroneAlgorithm(
                graphHopperStorage.getBaseGraph(),
                shortestWeighting,
                TraversalMode.NODE_BASED,
                cs,
                ins,
                ecc.getEccentricityStorage(shortestWeighting),
                ecc.getBorderNodeDistanceStorage(shortestWeighting),
                null);

        fastIsochroneAlgorithm.calcIsochroneNodes(1, 5.5);
        Set<Integer> nodeIds = new HashSet<>();
        Set<Integer> expectedNodeIds = new HashSet<>();
        expectedNodeIds.add(3);
        expectedNodeIds.add(4);
        expectedNodeIds.add(5);
        expectedNodeIds.add(6);
        expectedNodeIds.add(7);
        expectedNodeIds.add(8);

        for (IntObjectCursor<SPTEntry> entry : fastIsochroneAlgorithm.getActiveCellMaps().get(3)) {
            nodeIds.add(entry.value.adjNode);
        }
        assertEquals(expectedNodeIds, nodeIds);
        assertEquals(5.0, fastIsochroneAlgorithm.getActiveCellMaps().get(3).get(4).weight, 1e-10);
        assertEquals(6.0, fastIsochroneAlgorithm.getActiveCellMaps().get(3).get(5).weight, 1e-10);
        assertEquals(6.0, fastIsochroneAlgorithm.getActiveCellMaps().get(3).get(6).weight, 1e-10);
        assertEquals(5.0, fastIsochroneAlgorithm.getActiveCellMaps().get(3).get(7).weight, 1e-10);
    }

    @Test
    void testStartCell() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraphWithAdditionalEdge(encodingManager);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);

        ecc.loadExisting(shortestWeighting);
        ecc.calcEccentricities(shortestWeighting, new EdgeFilterSequence(), carEncoder);
        ecc.calcBorderNodeDistances(shortestWeighting, new EdgeFilterSequence(), carEncoder);
        FastIsochroneAlgorithm fastIsochroneAlgorithm = new FastIsochroneAlgorithm(
                graphHopperStorage.getBaseGraph(),
                shortestWeighting,
                TraversalMode.NODE_BASED,
                cs,
                ins,
                ecc.getEccentricityStorage(shortestWeighting),
                ecc.getBorderNodeDistanceStorage(shortestWeighting),
                null);

        fastIsochroneAlgorithm.calcIsochroneNodes(1, 5.5);
        Set<Integer> nodeIds = new HashSet<>();
        Set<Integer> expectedNodeIds = new HashSet<>();
        expectedNodeIds.add(0);
        expectedNodeIds.add(1);
        expectedNodeIds.add(2);
        expectedNodeIds.add(3);
        expectedNodeIds.add(8);

        for (IntObjectCursor<SPTEntry> entry : fastIsochroneAlgorithm.getStartCellMap()) {
            nodeIds.add(entry.value.adjNode);
        }
        assertEquals(expectedNodeIds, nodeIds);
        assertEquals(1.0, fastIsochroneAlgorithm.getStartCellMap().get(0).weight, 1e-10);
        assertEquals(0.0, fastIsochroneAlgorithm.getStartCellMap().get(1).weight, 1e-10);
        assertEquals(1.0, fastIsochroneAlgorithm.getStartCellMap().get(2).weight, 1e-10);
        assertEquals(3.0, fastIsochroneAlgorithm.getStartCellMap().get(3).weight, 1e-10);
        assertEquals(2.0, fastIsochroneAlgorithm.getStartCellMap().get(8).weight, 1e-10);
    }

    @Test
    void testFullyReachableCells() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraphWithAdditionalEdge(encodingManager);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);

        ecc.loadExisting(shortestWeighting);
        ecc.calcEccentricities(shortestWeighting, new EdgeFilterSequence(), carEncoder);
        ecc.calcBorderNodeDistances(shortestWeighting, new EdgeFilterSequence(), carEncoder);
        FastIsochroneAlgorithm fastIsochroneAlgorithm = new FastIsochroneAlgorithm(
                graphHopperStorage.getBaseGraph(),
                shortestWeighting,
                TraversalMode.NODE_BASED,
                cs,
                ins,
                ecc.getEccentricityStorage(shortestWeighting),
                ecc.getBorderNodeDistanceStorage(shortestWeighting),
                null);

        fastIsochroneAlgorithm.calcIsochroneNodes(1, 5.5);

        Set<Integer> cellIds = fastIsochroneAlgorithm.getFullyReachableCells();
        Set<Integer> expectedCellIds = new HashSet<>();

        assertEquals(expectedCellIds, cellIds);

        fastIsochroneAlgorithm = new FastIsochroneAlgorithm(
                graphHopperStorage.getBaseGraph(),
                shortestWeighting,
                TraversalMode.NODE_BASED,
                cs,
                ins,
                ecc.getEccentricityStorage(shortestWeighting),
                ecc.getBorderNodeDistanceStorage(shortestWeighting),
                null);

        fastIsochroneAlgorithm.calcIsochroneNodes(1, 6);

        cellIds = fastIsochroneAlgorithm.getFullyReachableCells();
        expectedCellIds = new HashSet<>();
        expectedCellIds.add(2);
        assertEquals(expectedCellIds, cellIds);

        fastIsochroneAlgorithm = new FastIsochroneAlgorithm(
                graphHopperStorage.getBaseGraph(),
                shortestWeighting,
                TraversalMode.NODE_BASED,
                cs,
                ins,
                ecc.getEccentricityStorage(shortestWeighting),
                ecc.getBorderNodeDistanceStorage(shortestWeighting),
                null);

        fastIsochroneAlgorithm.calcIsochroneNodes(8, 6);

        cellIds = fastIsochroneAlgorithm.getFullyReachableCells();
        expectedCellIds = new HashSet<>();
        expectedCellIds.add(2);
        expectedCellIds.add(3);
        assertEquals(expectedCellIds, cellIds);
    }
}