package org.heigit.ors.fastisochrones;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoreRangeDijkstraTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private IsochroneNodeStorage ins;
    private CellStorage cs;

    private void createMockStorages(GraphHopperStorage ghStorage) {
        IsochroneNodeStorage isochroneNodeStorage = new IsochroneNodeStorage(6, ghStorage.getDirectory());
        int[] cellIds = new int[]{2, 2, 2, 3, 3, 2};
        boolean[] borderNess = new boolean[]{true, false, true, true, true, false};
        isochroneNodeStorage.setCellIds(cellIds);
        isochroneNodeStorage.setBorderness(borderNess);

        CellStorage cellStorage = new CellStorage(6, ghStorage.getDirectory(), isochroneNodeStorage);
        cellStorage.init();
        cellStorage.calcCellNodesMap();
        this.ins = isochroneNodeStorage;
        this.cs = cellStorage;
    }

    private void createMockStorages2(GraphHopperStorage ghStorage) {
        IsochroneNodeStorage isochroneNodeStorage = new IsochroneNodeStorage(7, ghStorage.getDirectory());
        int[] cellIds = new int[]{2, 2, 2, 3, 3, 2, 3};
        boolean[] borderNess = new boolean[]{true, false, true, true, true, false, false};
        isochroneNodeStorage.setCellIds(cellIds);
        isochroneNodeStorage.setBorderness(borderNess);

        CellStorage cellStorage = new CellStorage(6, ghStorage.getDirectory(), isochroneNodeStorage);
        cellStorage.init();
        cellStorage.calcCellNodesMap();
        this.ins = isochroneNodeStorage;
        this.cs = cellStorage;
    }

    @Test
    void testUnreachedBorder() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createSimpleGraph(encodingManager);
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);

        ecc.loadExisting(shortestWeighting);
        ecc.calcBorderNodeDistances(shortestWeighting, new EdgeFilterSequence(), carEncoder);

        int startCell = ins.getCellId(5);
        CoreRangeDijkstra rangeSweepToAndInCore = new CoreRangeDijkstra(graphHopperStorage.getBaseGraph(), shortestWeighting, ins, ecc.getBorderNodeDistanceStorage(shortestWeighting));
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        edgeFilterSequence.add(
                new CellAndBorderNodeFilter(this.ins,
                        startCell,
                        graphHopperStorage.getNodes())
        );
        rangeSweepToAndInCore.setEdgeFilter(edgeFilterSequence);
        rangeSweepToAndInCore.setIsochroneLimit(2);
        rangeSweepToAndInCore.initFrom(5);
        rangeSweepToAndInCore.runAlgo();
        IntObjectMap<SPTEntry> bestWeightMap = rangeSweepToAndInCore.getFromMap();
        assertNotNull(bestWeightMap.get(0));
        assertNotNull(bestWeightMap.get(1));
        assertNotNull(bestWeightMap.get(2));
        assertNotNull(bestWeightMap.get(5));
        assertNull(bestWeightMap.get(3));
        assertNull(bestWeightMap.get(4));

        assertEquals(3.0, bestWeightMap.get(0).weight, 1e-10);
        assertEquals(2.0, bestWeightMap.get(1).weight, 1e-10);
        assertEquals(4.0, bestWeightMap.get(2).weight, 1e-10);
        assertEquals(0.0, bestWeightMap.get(5).weight, 1e-10);
    }

    @Test
    void testNextBorderNodes() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createSimpleGraph(encodingManager);
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);

        ecc.loadExisting(shortestWeighting);
        ecc.calcBorderNodeDistances(shortestWeighting, new EdgeFilterSequence(), carEncoder);

        int startCell = ins.getCellId(5);
        CoreRangeDijkstra rangeSweepToAndInCore = new CoreRangeDijkstra(graphHopperStorage.getBaseGraph(), shortestWeighting, ins, ecc.getBorderNodeDistanceStorage(shortestWeighting));
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        edgeFilterSequence.add(
                new CellAndBorderNodeFilter(this.ins,
                        startCell,
                        graphHopperStorage.getNodes())
        );
        rangeSweepToAndInCore.setEdgeFilter(edgeFilterSequence);
        rangeSweepToAndInCore.setIsochroneLimit(10);
        rangeSweepToAndInCore.initFrom(5);
        rangeSweepToAndInCore.runAlgo();
        IntObjectMap<SPTEntry> bestWeightMap = rangeSweepToAndInCore.getFromMap();
        assertNotNull(bestWeightMap.get(0));
        assertNotNull(bestWeightMap.get(1));
        assertNotNull(bestWeightMap.get(2));
        assertNotNull(bestWeightMap.get(5));
        assertNotNull(bestWeightMap.get(3));
        assertNotNull(bestWeightMap.get(4));

        assertEquals(3.0, bestWeightMap.get(0).weight, 1e-10);
        assertEquals(2.0, bestWeightMap.get(1).weight, 1e-10);
        assertEquals(4.0, bestWeightMap.get(2).weight, 1e-10);
        assertEquals(0.0, bestWeightMap.get(5).weight, 1e-10);
        assertEquals(6.0, bestWeightMap.get(4).weight, 1e-10);
        assertEquals(5.0, bestWeightMap.get(3).weight, 1e-10);
    }

    @Test
    void testHandleAdjBorderNodes() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createSimpleGraph2(encodingManager);
        createMockStorages2(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);

        ecc.loadExisting(shortestWeighting);
        ecc.calcBorderNodeDistances(shortestWeighting, new EdgeFilterSequence(), carEncoder);

        int startCell = ins.getCellId(5);
        CoreRangeDijkstra rangeSweepToAndInCore = new CoreRangeDijkstra(graphHopperStorage.getBaseGraph(), shortestWeighting, ins, ecc.getBorderNodeDistanceStorage(shortestWeighting));
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        edgeFilterSequence.add(
                new CellAndBorderNodeFilter(this.ins,
                        startCell,
                        graphHopperStorage.getNodes())
        );
        rangeSweepToAndInCore.setEdgeFilter(edgeFilterSequence);
        rangeSweepToAndInCore.setIsochroneLimit(10);
        rangeSweepToAndInCore.initFrom(5);
        rangeSweepToAndInCore.runAlgo();
        IntObjectMap<SPTEntry> bestWeightMap = rangeSweepToAndInCore.getFromMap();
        assertEquals(10.0, bestWeightMap.get(3).weight, 1e-10);
    }
}