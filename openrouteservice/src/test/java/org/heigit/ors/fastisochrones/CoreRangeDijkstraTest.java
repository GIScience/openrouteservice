package org.heigit.ors.fastisochrones;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.SPTEntry;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoreRangeDijkstraTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private IsochroneNodeStorage ins;
    private CellStorage cs;

    GraphHopperStorage createGHStorage() {
        return new GraphBuilder(encodingManager).create();
    }

    private GraphHopperStorage createSimpleGraph() {
        // 5--1---2
        //     \ /|
        //      0 |
        //     /  |
        //    4---3
        GraphHopperStorage g = createGHStorage();
        g.edge(0, 1, 1, true);
        g.edge(0, 2, 1, true);
        g.edge(0, 4, 3, true);
        g.edge(1, 2, 2, true);
        g.edge(2, 3, 1, true);
        g.edge(4, 3, 2, true);
        g.edge(5, 1, 2, true);

        g.getBaseGraph().getNodeAccess().setNode(0, 2, 2);
        g.getBaseGraph().getNodeAccess().setNode(1, 3, 2);
        g.getBaseGraph().getNodeAccess().setNode(2, 3, 3);
        g.getBaseGraph().getNodeAccess().setNode(3, 1, 3);
        g.getBaseGraph().getNodeAccess().setNode(4, 1, 2);
        g.getBaseGraph().getNodeAccess().setNode(5, 3, 1);
        return g;
    }

    private GraphHopperStorage createSimpleGraph2() {
        // 5--1---2
        //     \ /
        //      0
        //     /
        //    4--6--3
        GraphHopperStorage g = createGHStorage();
        g.edge(0, 1, 1, true);
        g.edge(0, 2, 1, true);
        g.edge(0, 4, 3, true);
        g.edge(1, 2, 2, true);
        g.edge(4, 6, 2, true);
        g.edge(6, 3, 2, true);
        g.edge(5, 1, 2, true);

        g.getBaseGraph().getNodeAccess().setNode(0, 2, 2);
        g.getBaseGraph().getNodeAccess().setNode(1, 3, 2);
        g.getBaseGraph().getNodeAccess().setNode(2, 3, 3);
        g.getBaseGraph().getNodeAccess().setNode(3, 1, 4);
        g.getBaseGraph().getNodeAccess().setNode(4, 1, 2);
        g.getBaseGraph().getNodeAccess().setNode(5, 3, 1);
        g.getBaseGraph().getNodeAccess().setNode(6, 3, 3);
        return g;
    }

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
    public void testUnreachedBorder() {
        GraphHopperStorage graphHopperStorage = createSimpleGraph();
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);

        ecc.loadExisting(shortestWeighting);
        ecc.calcBorderNodeDistances(shortestWeighting, carEncoder);

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
        assertNull(bestWeightMap.get(0));
        assertNotNull(bestWeightMap.get(1));
        assertNull(bestWeightMap.get(2));
        assertNotNull(bestWeightMap.get(5));
        assertNull(bestWeightMap.get(3));
        assertNull(bestWeightMap.get(4));

        assertEquals(2.0, bestWeightMap.get(1).weight, 1e-10);
        assertEquals(0.0, bestWeightMap.get(5).weight, 1e-10);
    }

    @Test
    public void testNextBorderNodes() {
        GraphHopperStorage graphHopperStorage = createSimpleGraph();
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);

        ecc.loadExisting(shortestWeighting);
        ecc.calcBorderNodeDistances(shortestWeighting, carEncoder);

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
    public void testHandleAdjBorderNodes() {
        GraphHopperStorage graphHopperStorage = createSimpleGraph2();
        createMockStorages2(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);

        ecc.loadExisting(shortestWeighting);
        ecc.calcBorderNodeDistances(shortestWeighting, carEncoder);

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