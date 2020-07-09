package org.heigit.ors.fastisochrones;

import com.carrotsearch.hppc.IntHashSet;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import org.heigit.ors.fastisochrones.Contour;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ContourTest {
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
    public void testCalculateContour() {
        GraphHopperStorage graphHopperStorage = createSimpleGraph();
        createMockStorages(graphHopperStorage);
        Contour contour = new Contour(graphHopperStorage, graphHopperStorage.getBaseGraph().getNodeAccess(), ins, cs);
        contour.calculateContour();
        List<Double> coordinatesCell2 = cs.getCellContourOrder(2);
        assertEquals(2686, coordinatesCell2.size());
        assertEquals(3.0, coordinatesCell2.get(0), 1e-10);
        assertEquals(1.0, coordinatesCell2.get(1), 1e-10);
        assertEquals(3.0, coordinatesCell2.get(2), 1e-10);
        assertEquals(1.003596954128078, coordinatesCell2.get(3), 1e-10);
        assertEquals(3.0, coordinatesCell2.get(2684), 1e-10);
        assertEquals(1.0, coordinatesCell2.get(2685), 1e-10);
    }

    @Test
    public void testDistance() {
        double distance = Contour.distance(1,1,1,2);
        assertEquals(111177.99068882648, distance, 1e-10);
        double distance2 = Contour.distance(1,1,0.5,-0.5);
        assertEquals(111177.99068882648, distance2, 1e-10);
    }

    private String printCell(List<Double> coordinates, int cellId) {
        if (coordinates.size() < 3)
            return "";
        StringBuilder statement = new StringBuilder();
        statement.append("{\"type\": \"Feature\",\"properties\": {\"name\": \"" + cellId + "\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[");
        int i;
        for (i = coordinates.size() - 2; i > 0; i -= 2) {
            statement.append("[" + String.valueOf(coordinates.get(i + 1)).substring(0, Math.min(8, String.valueOf(coordinates.get(i + 1)).length())) + "," + String.valueOf(coordinates.get(i)).substring(0, Math.min(8, String.valueOf(coordinates.get(i)).length())) + "],");
        }
        statement.append("[" + String.valueOf(coordinates.get(coordinates.size() - 1)).substring(0, Math.min(8, String.valueOf(coordinates.get(coordinates.size() - 1)).length())) + "," + String.valueOf(coordinates.get(coordinates.size() - 2)).substring(0, Math.min(8, String.valueOf(coordinates.get(coordinates.size() - 2)).length())) + "]");

        statement.append("]]}},");
        statement.append(System.lineSeparator());
        return statement.toString();
    }
}