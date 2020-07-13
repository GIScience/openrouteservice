package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.BorderNodeDistanceSet;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.junit.Test;

import static org.junit.Assert.*;

public class CellPropertiesTest {
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

    @Test
    public void testLoadExisting() {
        GraphHopperStorage graphHopperStorage = createSimpleGraph();
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);
        Weighting fastestWeighting = new FastestWeighting(carEncoder);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);

        ecc.loadExisting(fastestWeighting);
        assertNotNull(ecc.getBorderNodeDistanceStorage(fastestWeighting));
        assertNotNull(ecc.getEccentricityStorage(fastestWeighting));
        assertNull(ecc.getEccentricityStorage(shortestWeighting));
        assertNull(ecc.getBorderNodeDistanceStorage(shortestWeighting));
    }

    @Test
    public void testCalcEccentricities() {
        GraphHopperStorage graphHopperStorage = createSimpleGraph();
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);

        ecc.loadExisting(shortestWeighting);
        ecc.calcEccentricities(shortestWeighting, carEncoder);
        assertEquals(3, ecc.getEccentricityStorage(shortestWeighting).getEccentricity(0));
        assertEquals(4, ecc.getEccentricityStorage(shortestWeighting).getEccentricity(2));
        assertEquals(2, ecc.getEccentricityStorage(shortestWeighting).getEccentricity(3));
        assertEquals(2, ecc.getEccentricityStorage(shortestWeighting).getEccentricity(4));
    }

    @Test
    public void testCalcBorderNodeDistances() {
        GraphHopperStorage graphHopperStorage = createSimpleGraph();
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);

        ecc.loadExisting(shortestWeighting);
        ecc.calcBorderNodeDistances(shortestWeighting, carEncoder);
        BorderNodeDistanceSet borderNodeDistanceSet = ecc.getBorderNodeDistanceStorage(shortestWeighting).getBorderNodeDistanceSet(0);
        assertEquals(1, borderNodeDistanceSet.getAdjBorderNodeIds().length);
        assertEquals(2, borderNodeDistanceSet.getAdjBorderNodeIds()[0]);
        assertEquals(1.0, borderNodeDistanceSet.getAdjBorderNodeDistances()[0], 1e-10);

        borderNodeDistanceSet = ecc.getBorderNodeDistanceStorage(shortestWeighting).getBorderNodeDistanceSet(2);
        assertEquals(1, borderNodeDistanceSet.getAdjBorderNodeIds().length);
        assertEquals(0, borderNodeDistanceSet.getAdjBorderNodeIds()[0]);
        assertEquals(1.0, borderNodeDistanceSet.getAdjBorderNodeDistances()[0], 1e-10);

        borderNodeDistanceSet = ecc.getBorderNodeDistanceStorage(shortestWeighting).getBorderNodeDistanceSet(3);
        assertEquals(1, borderNodeDistanceSet.getAdjBorderNodeIds().length);
        assertEquals(4, borderNodeDistanceSet.getAdjBorderNodeIds()[0]);
        assertEquals(2.0, borderNodeDistanceSet.getAdjBorderNodeDistances()[0], 1e-10);

        borderNodeDistanceSet = ecc.getBorderNodeDistanceStorage(shortestWeighting).getBorderNodeDistanceSet(4);
        assertEquals(1, borderNodeDistanceSet.getAdjBorderNodeIds().length);
        assertEquals(3, borderNodeDistanceSet.getAdjBorderNodeIds()[0]);
        assertEquals(2.0, borderNodeDistanceSet.getAdjBorderNodeDistances()[0], 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEccentricityOfNonBorderNode() {
        GraphHopperStorage graphHopperStorage = createSimpleGraph();
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);

        ecc.loadExisting(shortestWeighting);
        ecc.calcEccentricities(shortestWeighting, carEncoder);
        ecc.getEccentricityStorage(shortestWeighting).getEccentricity(5);
    }

    @Test
    public void testDistance() {
        double distance = Contour.distance(1, 1, 1, 2);
        assertEquals(111177.99068882648, distance, 1e-10);
        double distance2 = Contour.distance(1, 1, 0.5, -0.5);
        assertEquals(111177.99068882648, distance2, 1e-10);
    }
}