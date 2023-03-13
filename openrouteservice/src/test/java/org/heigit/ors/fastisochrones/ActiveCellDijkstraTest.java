package org.heigit.ors.fastisochrones;

import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActiveCellDijkstraTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private IsochroneNodeStorage ins;

    private void createMockStorages(GraphHopperStorage ghStorage) {
        IsochroneNodeStorage isochroneNodeStorage = new IsochroneNodeStorage(9, ghStorage.getDirectory());
        int[] cellIds = new int[]{2, 2, 2, 2, 3, 3, 3, 3, 2};
        boolean[] borderNess = new boolean[]{false, false, false, true, true, false, false, true, true};
        isochroneNodeStorage.setCellIds(cellIds);
        isochroneNodeStorage.setBorderness(borderNess);
        this.ins = isochroneNodeStorage;
    }

    @Test
    void testAddInitialBorderNode() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(encodingManager);
        createMockStorages(graphHopperStorage);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);
        ActiveCellDijkstra activeCellDijkstra = new ActiveCellDijkstra(graphHopperStorage.getBaseGraph(), shortestWeighting, ins, 2);
        activeCellDijkstra.setIsochroneLimit(5000);

        //Add all the start points with their respective already visited weight
        for (int nodeId : new int[]{3, 8}) {
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
    void testRun() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(encodingManager);
        createMockStorages(graphHopperStorage);
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);
        ActiveCellDijkstra activeCellDijkstra = new ActiveCellDijkstra(graphHopperStorage.getBaseGraph(), shortestWeighting, ins, 2);
        activeCellDijkstra.setIsochroneLimit(5000);

        for (int nodeId : new int[]{3, 8}) {
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
        // Two bordernodes of adjacent cell are added to generate overlapping cell polygons
        expectedNodeIds.add(4);
        expectedNodeIds.add(7);
        expectedNodeIds.add(8);
        for (IntObjectCursor<SPTEntry> entry : activeCellDijkstra.getFromMap()) {
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