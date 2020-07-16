package org.heigit.ors.fastisochrones;

import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.SPTEntry;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ActiveCellDijkstraTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private IsochroneNodeStorage ins;
    private CellStorage cs;

    GraphHopperStorage createGHStorage() {
        return new GraphBuilder(encodingManager).create();
    }

    public GraphHopperStorage createMediumGraph() {
        //    3---4--5
        //   /\   |  |
        //  2--0  6--7
        //  | / \   /
        //  |/   \ /
        //  1-----8
        GraphHopperStorage g = createGHStorage();
        g.edge(0, 1, 1, true);
        g.edge(0, 2, 1, true);
        g.edge(0, 3, 5, true);
        g.edge(0, 8, 1, true);
        g.edge(1, 2, 1, true);
        g.edge(1, 8, 2, true);
        g.edge(2, 3, 2, true);
        g.edge(3, 4, 2, true);
        g.edge(4, 5, 1, true);
        g.edge(4, 6, 1, true);
        g.edge(5, 7, 1, true);
        g.edge(6, 7, 2, true);
        g.edge(7, 8, 3, true);
        //Set test lat lon
        g.getBaseGraph().getNodeAccess().setNode(0, 3, 3);
        g.getBaseGraph().getNodeAccess().setNode(1, 1, 1);
        g.getBaseGraph().getNodeAccess().setNode(2, 3, 1);
        g.getBaseGraph().getNodeAccess().setNode(3, 4, 2);
        g.getBaseGraph().getNodeAccess().setNode(4, 4, 4);
        g.getBaseGraph().getNodeAccess().setNode(5, 4, 5);
        g.getBaseGraph().getNodeAccess().setNode(6, 3, 4);
        g.getBaseGraph().getNodeAccess().setNode(7, 3, 5);
        g.getBaseGraph().getNodeAccess().setNode(8, 1, 4);
        return g;
    }

    private void createMockStorages(GraphHopperStorage ghStorage) {
        IsochroneNodeStorage isochroneNodeStorage = new IsochroneNodeStorage(9, ghStorage.getDirectory());
        int[] cellIds = new int[]{2, 2, 2, 2, 3, 3, 3, 3, 2};
        boolean[] borderNess = new boolean[]{false, false, false, true, true, false, false, true, true};
        isochroneNodeStorage.setCellIds(cellIds);
        isochroneNodeStorage.setBorderness(borderNess);

        CellStorage cellStorage = new CellStorage(9, ghStorage.getDirectory(), isochroneNodeStorage);
        cellStorage.init();
        cellStorage.calcCellNodesMap();
        this.ins = isochroneNodeStorage;
        this.cs = cellStorage;
    }

    @Test
    public void testAddInitialBorderNode() {
        GraphHopperStorage graphHopperStorage = createMediumGraph();
        createMockStorages(graphHopperStorage);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);
        ActiveCellDijkstra activeCellDijkstra = new ActiveCellDijkstra(graphHopperStorage.getBaseGraph(), shortestWeighting, ins, 2);
        activeCellDijkstra.setIsochroneLimit(5000);

        //Add all the start points with their respective already visited weight
        for (int nodeId : new int[]{3,8}) {
            activeCellDijkstra.addInitialBordernode(nodeId, 0);
        }
        SPTEntry entry = activeCellDijkstra.fromHeap.poll();
        assertEquals(3, entry.adjNode);
        assertEquals(0.0, entry.getWeightOfVisitedPath(), 1e-10);
        entry = activeCellDijkstra.fromHeap.poll();
        assertEquals(8, entry.adjNode);
        assertEquals(0.0, entry.getWeightOfVisitedPath(), 1e-10);

        assertEquals(2, activeCellDijkstra.getFromMap().size());

    }

    @Test
    public void testRun() {
        GraphHopperStorage graphHopperStorage = createMediumGraph();
        createMockStorages(graphHopperStorage);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);
        ActiveCellDijkstra activeCellDijkstra = new ActiveCellDijkstra(graphHopperStorage.getBaseGraph(), shortestWeighting, ins, 2);
        activeCellDijkstra.setIsochroneLimit(5000);

        for (int nodeId : new int[]{3,8}) {
            activeCellDijkstra.addInitialBordernode(nodeId, 0);
        }
        activeCellDijkstra.init();
        activeCellDijkstra.runAlgo();
        Set<Integer> nodeIds = new HashSet<>();
        Set<Integer> expectedNodeIds = new HashSet<>();
        expectedNodeIds.add(0);
        expectedNodeIds.add(1);
        expectedNodeIds.add(2);
        expectedNodeIds.add(3);
        expectedNodeIds.add(8);
        for (IntObjectCursor<SPTEntry> entry : activeCellDijkstra.getFromMap()){
            nodeIds.add(entry.value.adjNode);
        }
        assertEquals(expectedNodeIds, nodeIds);
        assertEquals(1.0, activeCellDijkstra.getFromMap().get(0).weight, 1e-10);
        assertEquals(2.0, activeCellDijkstra.getFromMap().get(1).weight, 1e-10);
        assertEquals(2.0, activeCellDijkstra.getFromMap().get(2).weight, 1e-10);
        assertEquals(0.0, activeCellDijkstra.getFromMap().get(3).weight, 1e-10);
        assertEquals(0.0, activeCellDijkstra.getFromMap().get(8).weight, 1e-10);

    }
}