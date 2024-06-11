package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContourTest {
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
    void testCalculateContour() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createSimpleGraph(encodingManager);
        createMockStorages(graphHopperStorage);
        Contour contour = new Contour(graphHopperStorage, graphHopperStorage.getBaseGraph().getNodeAccess(), ins, cs);
        contour.calculateContour();
        List<Double> coordinatesCell2 = cs.getCellContourOrder(2);
        assertEquals(9618, coordinatesCell2.size());
        assertEquals(3.0, coordinatesCell2.get(0), 1e-3);
        assertEquals(1.0, coordinatesCell2.get(1), 1e-3);
        assertEquals(3.0, coordinatesCell2.get(2), 1e-3);
        assertEquals(1.0002998858757293, coordinatesCell2.get(3), 1e-3);
    }

    @Test
    void testDistance() {
        double distance = Contour.distance(1, 1, 1, 2);
        assertEquals(111177.99068882648, distance, 1e-10);
        double distance2 = Contour.distance(1, 1, 0.5, -0.5);
        assertEquals(111177.99068882648, distance2, 1e-10);
    }
}