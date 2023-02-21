package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.fastisochrones.storage.BorderNodeDistanceSet;
import org.heigit.ors.fastisochrones.storage.EccentricityStorage;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellPropertiesTest {
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

    @Test
    void testLoadExisting() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createSimpleGraph(encodingManager);
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
    void testCalcEccentricities() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createSimpleGraph(encodingManager);
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);

        ecc.loadExisting(shortestWeighting);
        ecc.calcEccentricities(shortestWeighting, new EdgeFilterSequence(), carEncoder);
        assertEquals(3, ecc.getEccentricityStorage(shortestWeighting).getEccentricity(0));
        assertEquals(4, ecc.getEccentricityStorage(shortestWeighting).getEccentricity(2));
        assertEquals(2, ecc.getEccentricityStorage(shortestWeighting).getEccentricity(3));
        assertEquals(2, ecc.getEccentricityStorage(shortestWeighting).getEccentricity(4));
    }

    @Test
    void testCalcBorderNodeDistances() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createSimpleGraph(encodingManager);
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);

        ecc.loadExisting(shortestWeighting);
        ecc.calcBorderNodeDistances(shortestWeighting, new EdgeFilterSequence(), carEncoder);
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

    @Test
    void testGetEccentricityOfNonBorderNode() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createSimpleGraph(encodingManager);
        createMockStorages(graphHopperStorage);
        Eccentricity ecc = new Eccentricity(graphHopperStorage, null, ins, cs);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);
        ecc.loadExisting(shortestWeighting);
        ecc.calcEccentricities(shortestWeighting, new EdgeFilterSequence(), carEncoder);
        EccentricityStorage eccentricityStorage = ecc.getEccentricityStorage(shortestWeighting);
        assertThrows(IllegalArgumentException.class, () -> eccentricityStorage.getEccentricity(5));
    }

    @Test
    void testDistance() {
        double distance = Contour.distance(1, 1, 1, 2);
        assertEquals(111177.99068882648, distance, 1e-10);
        double distance2 = Contour.distance(1, 1, 0.5, -0.5);
        assertEquals(111177.99068882648, distance2, 1e-10);
    }
}