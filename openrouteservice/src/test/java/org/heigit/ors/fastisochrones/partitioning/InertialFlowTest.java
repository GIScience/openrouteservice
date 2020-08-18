package org.heigit.ors.fastisochrones.partitioning;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.ToyGraphCreationUtil;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;

public class InertialFlowTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);

    @Test
    public void testInertialFlowSimpleGraph() {
        GraphHopperStorage ghStorage = ToyGraphCreationUtil.createSimpleGraph(encodingManager);
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
        GraphHopperStorage ghStorage = ToyGraphCreationUtil.createMediumGraph(encodingManager);
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
        GraphHopperStorage ghStorage = ToyGraphCreationUtil.createSingleEdgeGraph(encodingManager);
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
        GraphHopperStorage ghStorage = ToyGraphCreationUtil.createDisconnectedGraph(encodingManager);
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