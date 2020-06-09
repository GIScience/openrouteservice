package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.getMaxThreadCount;
import static org.junit.Assert.*;

public class InertialFlowTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);

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

    private GraphHopperStorage createSingleEdgeGraph() {
        GraphHopperStorage g = createGHStorage();
        g.edge(0, 1, 1, true);

        g.getBaseGraph().getNodeAccess().setNode(0, 0, 0);
        g.getBaseGraph().getNodeAccess().setNode(1, 1, 1);

        return g;
    }

    private GraphHopperStorage createDisconnectedGraph() {
        //   5--1---2
        //       \ /
        //        0
        //       /
        //      /
        //     / 6  9
        //    /  |  |
        //   /   7--8
        //  4---3
        //  |   |
        //  11  10
        GraphHopperStorage g = createGHStorage();
        g.edge(0, 1, 1, true);
        g.edge(0, 2, 1, true);
        g.edge(0, 4, 3, true);
        g.edge(1, 2, 2, true);
        g.edge(4, 3, 2, true);
        g.edge(5, 1, 2, true);
        g.edge(6, 7, 1, true);
        g.edge(7, 8, 1, true);
        g.edge(8, 9, 1, true);
        g.edge(3, 10, 1, true);
        g.edge(4, 11, 1, true);

        g.getBaseGraph().getNodeAccess().setNode(0, 2, 2);
        g.getBaseGraph().getNodeAccess().setNode(1, 3, 2);
        g.getBaseGraph().getNodeAccess().setNode(2, 3, 3);
        g.getBaseGraph().getNodeAccess().setNode(3, 1, 3);
        g.getBaseGraph().getNodeAccess().setNode(4, 1, 2);
        g.getBaseGraph().getNodeAccess().setNode(5, 3, 1);
        g.getBaseGraph().getNodeAccess().setNode(6, 1.2, 3);
        g.getBaseGraph().getNodeAccess().setNode(7, 1.1, 3);
        g.getBaseGraph().getNodeAccess().setNode(8, 1.1, 2);
        g.getBaseGraph().getNodeAccess().setNode(9, 1.2, 2);
        g.getBaseGraph().getNodeAccess().setNode(10, 0.8, 2.2);
        g.getBaseGraph().getNodeAccess().setNode(11, 0.8, 2);

        return g;
    }

    @Test
    public void testInertialFlowSimpleGraph() {
        GraphHopperStorage ghStorage = createSimpleGraph();
        int[] nodeToCell = new int[ghStorage.getNodes()];
        ExecutorService threadPool = java.util.concurrent.Executors.newFixedThreadPool(1);
        InverseSemaphore inverseSemaphore = new InverseSemaphore();
        inverseSemaphore.beforeSubmit();
        InertialFlow inertialFlow = new InertialFlow(nodeToCell, ghStorage, null, threadPool, inverseSemaphore);
        threadPool.execute(inertialFlow);
        try {
            inverseSemaphore.awaitCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadPool.shutdown();
        //Check for partitioning. Cell numbers are not too relevant.
        int cellId0 = nodeToCell[0];
        int cellId1 = nodeToCell[4];
        assertFalse(cellId0 == cellId1);
        assertArrayEquals(new int[]{cellId0, cellId0, cellId0, cellId1, cellId1, cellId0}, nodeToCell);
    }

    @Test
    public void testInertialFlowMediumGraph() {
        GraphHopperStorage ghStorage = createMediumGraph();
        int[] nodeToCell = new int[ghStorage.getNodes()];
        ExecutorService threadPool = java.util.concurrent.Executors.newFixedThreadPool(1);
        InverseSemaphore inverseSemaphore = new InverseSemaphore();
        inverseSemaphore.beforeSubmit();
        InertialFlow inertialFlow = new InertialFlow(nodeToCell, ghStorage, null, threadPool, inverseSemaphore);
        threadPool.execute(inertialFlow);
        try {
            inverseSemaphore.awaitCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadPool.shutdown();
        //Check for partitioning. Cell numbers are not too relevant.
        int cellId0 = nodeToCell[0];
        int cellId1 = nodeToCell[4];
        assertFalse(cellId0 == cellId1);
        assertArrayEquals(new int[]{cellId0, cellId0, cellId0, cellId0, cellId1, cellId1, cellId1, cellId1, cellId0}, nodeToCell);
    }

    @Test
    public void testSingleEdgeGraph() {
        GraphHopperStorage ghStorage = createSingleEdgeGraph();
        int[] nodeToCell = new int[ghStorage.getNodes()];
        ExecutorService threadPool = java.util.concurrent.Executors.newFixedThreadPool(1);
        InverseSemaphore inverseSemaphore = new InverseSemaphore();
        inverseSemaphore.beforeSubmit();
        InertialFlow inertialFlow = new InertialFlow(nodeToCell, ghStorage, null, threadPool, inverseSemaphore);
        threadPool.execute(inertialFlow);
        try {
            inverseSemaphore.awaitCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadPool.shutdown();
        //Check for partitioning. Cell numbers are not too relevant.
        assertFalse(nodeToCell[0] == nodeToCell[1]);
    }

    @Test
    public void testDisconnect() {
        //This graph would be split into two cells by pure InertialFlow
        //Additional separation based on connection between nodes is performed.
        //This will split off the part of the graph consisting of nodes 6-7-8-9 from the part that is 3-4-10-11
        //This will not work if SEPARATEDISCONNECTED flag is set to false in InertialFlow
        GraphHopperStorage ghStorage = createDisconnectedGraph();
        int[] nodeToCell = new int[ghStorage.getNodes()];
        ExecutorService threadPool = java.util.concurrent.Executors.newFixedThreadPool(1);
        InverseSemaphore inverseSemaphore = new InverseSemaphore();
        inverseSemaphore.beforeSubmit();
        InertialFlow inertialFlow = new InertialFlow(nodeToCell, ghStorage, null, threadPool, inverseSemaphore);
        threadPool.execute(inertialFlow);
        try {
            inverseSemaphore.awaitCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadPool.shutdown();
        //Check for partitioning. Cell numbers are not too relevant.
        int cellId0 = nodeToCell[0];
        int cellId1 = nodeToCell[3];
        int cellId2 = nodeToCell[6];
        assertArrayEquals(new int[]{cellId0, cellId0, cellId0, cellId1, cellId1, cellId0, cellId2, cellId2, cellId2, cellId2, cellId1, cellId1}, nodeToCell);
        assertFalse(cellId0 == cellId1);
        assertFalse(cellId1 == cellId2);
        assertFalse(cellId2 == cellId0);
    }
}