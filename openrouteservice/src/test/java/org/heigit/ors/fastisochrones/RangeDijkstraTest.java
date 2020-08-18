package org.heigit.ors.fastisochrones;

import com.carrotsearch.hppc.IntHashSet;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.junit.Test;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.getMaxCellNodesNumber;
import static org.junit.Assert.assertEquals;

public class RangeDijkstraTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);

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


    @Test
    public void testGetMaxWeight() {
        GraphHopperStorage graphHopperStorage = createSimpleGraph();
        RangeDijkstra rangeDijkstra = new RangeDijkstra(graphHopperStorage.getBaseGraph(), new ShortestWeighting(carEncoder));
        rangeDijkstra.setMaxVisitedNodes(getMaxCellNodesNumber() * 10);
        IntHashSet cellNodes = new IntHashSet();
        IntHashSet relevantNodes = new IntHashSet();
        cellNodes.addAll(0, 1, 2, 5);
        relevantNodes.addAll(0, 1, 2);
        rangeDijkstra.setCellNodes(cellNodes);
        //Check eccentricity when all nodes are relevant
        assertEquals(3.0, rangeDijkstra.calcMaxWeight(0, cellNodes), 1e-10);

        //Check eccentricity when all nodes but node 5 are relevant
        rangeDijkstra = new RangeDijkstra(graphHopperStorage.getBaseGraph(), new ShortestWeighting(carEncoder));
        rangeDijkstra.setMaxVisitedNodes(getMaxCellNodesNumber() * 10);
        rangeDijkstra.setCellNodes(cellNodes);
        assertEquals(1.0, rangeDijkstra.calcMaxWeight(0, relevantNodes), 1e-10);
    }
}