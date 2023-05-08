package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntHashMap;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaxFlowMinCutTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);

    @Test
    void testNodes() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(encodingManager);
        Graph graph = graphHopperStorage.getBaseGraph();
        //Create mock projection
        IntArrayList projection_m00 = new IntArrayList();
        projection_m00.add(1, 2, 3, 0, 4, 6, 8, 5, 7);

        MaxFlowMinCut maxFlowMinCut = new EdmondsKarpAStar(graph, null, null);
        maxFlowMinCut.setOrderedNodes(projection_m00);
        maxFlowMinCut.setNodeOrder();
        IntIntHashMap nodeOrder = maxFlowMinCut.nodeOrder;
        assertEquals(3, nodeOrder.get(0));
        assertEquals(0, nodeOrder.get(1));
        assertEquals(1, nodeOrder.get(2));
        assertEquals(2, nodeOrder.get(3));
        assertEquals(4, nodeOrder.get(4));
        assertEquals(7, nodeOrder.get(5));
        assertEquals(5, nodeOrder.get(6));
        assertEquals(8, nodeOrder.get(7));
        assertEquals(6, nodeOrder.get(8));
    }

    @Test
    void testReset() {
        //Create test graph
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(encodingManager);
        Graph graph = graphHopperStorage.getBaseGraph();
        //Create data for test graph
        //FlowEdgeBaseNode is an array representing base and adj node for each edgeId, ordered by edgeId, i.e. 2 entries/edge
        int[] flowEdgeBaseNode = new int[]{
                0, 1, 0, 2, 0, 3, 0, 8, 1, 2, 1, 8, 2, 3, 3, 4, 4, 5, 4, 6, 5, 7, 6, 7, 7, 8, -1, -1
        };
        //mock flow after run. Should be reset to false entirely
        boolean[] flow = new boolean[]{
                false, true, false, true, false, true, false,
                false, true, false, true, false, true, false,
                false, true, false, true, false, true, false,
                false, true, false, true, false, false, false
        };
        //mock visited after run. Should be reset to 0 entirely
        int[] visited = new int[]{
                0, 1, 0, 1, 0, 1, 0, 1, 0, 0
        };
        PartitioningData pData = new PartitioningData(flowEdgeBaseNode, flow, visited);
        //Create mock projection
        IntArrayList projection_m00 = new IntArrayList();
        projection_m00.add(1, 2, 3, 0, 4, 6, 8, 5, 7);

        MaxFlowMinCut maxFlowMinCut = new EdmondsKarpAStar(graph, pData, null);
        maxFlowMinCut.setOrderedNodes(projection_m00);
        maxFlowMinCut.setNodeOrder();
        maxFlowMinCut.reset();
        for (boolean f : pData.flow)
            assertEquals(false, f);

        for (int v : pData.visited)
            assertEquals(0, v);
    }

    @Test
    void testGetMaxFlowGoodProjection() {
        //Create test graph
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(encodingManager);
        Graph graph = graphHopperStorage.getBaseGraph();
        //Create data for test graph
        //FlowEdgeBaseNode is an array representing base and adj node for each edgeId, ordered by edgeId, i.e. 2 entries/edge
        int[] flowEdgeBaseNode = new int[]{
                0, 1, 0, 2, 0, 3, 0, 8, 1, 2, 1, 8, 2, 3, 3, 4, 4, 5, 4, 6, 5, 7, 6, 7, 7, 8, -1, -1
        };
        //mock flow
        boolean[] flow = new boolean[28];
        //mock visited
        int[] visited = new int[10];
        PartitioningData pData = new PartitioningData(flowEdgeBaseNode, flow, visited);
        //Create mock projection
        IntArrayList projection_p675 = new IntArrayList();
        projection_p675.add(1, 2, 3, 0, 8, 6, 4, 7, 5);

        MaxFlowMinCut maxFlowMinCut = new EdmondsKarpAStar(graph, pData, null);
        maxFlowMinCut.setOrderedNodes(projection_p675);
        maxFlowMinCut.setNodeOrder();
        maxFlowMinCut.reset();
        int maxFlow = maxFlowMinCut.getMaxFlow();
        assertEquals(2, maxFlow);
    }

    @Test
    void testGetMaxFlowBadProjection() {
        //Create test graph
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(encodingManager);
        Graph graph = graphHopperStorage.getBaseGraph();
        //Create data for test graph
        //FlowEdgeBaseNode is an array representing base and adj node for each edgeId, ordered by edgeId, i.e. 2 entries/edge
        int[] flowEdgeBaseNode = new int[]{
                0, 1, 0, 2, 0, 3, 0, 8, 1, 2, 1, 8, 2, 3, 3, 4, 4, 5, 4, 6, 5, 7, 6, 7, 7, 8, -1, -1
        };
        //mock flow
        boolean[] flow = new boolean[28];
        //mock visited
        int[] visited = new int[10];
        PartitioningData pData = new PartitioningData(flowEdgeBaseNode, flow, visited);
        //Create mock projection
        IntArrayList projection_m45 = new IntArrayList();
        projection_m45.add(8, 7, 5, 6, 0, 1, 4, 2, 3);

        MaxFlowMinCut maxFlowMinCut = new EdmondsKarpAStar(graph, pData, null);
        maxFlowMinCut.setOrderedNodes(projection_m45);
        maxFlowMinCut.setNodeOrder();
        maxFlowMinCut.reset();
        int maxFlow = maxFlowMinCut.getMaxFlow();
        assertEquals(4, maxFlow);
    }

    @Test
    void testSingleEdgeGraph() {
        //Create test graph
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createSingleEdgeGraph(encodingManager);
        Graph graph = graphHopperStorage.getBaseGraph();
        //Create data for test graph
        //FlowEdgeBaseNode is an array representing base and adj node for each edgeId, ordered by edgeId, i.e. 2 entries/edge
        int[] flowEdgeBaseNode = new int[]{
                0, 1
        };
        //mock flow
        boolean[] flow = new boolean[4];
        //mock visited
        int[] visited = new int[3];
        PartitioningData pData = new PartitioningData(flowEdgeBaseNode, flow, visited);
        //Create mock projection
        IntArrayList projection_m45 = new IntArrayList();
        projection_m45.add(0, 1);

        MaxFlowMinCut maxFlowMinCut = new EdmondsKarpAStar(graph, pData, null);
        maxFlowMinCut.setOrderedNodes(projection_m45);
        maxFlowMinCut.setNodeOrder();
        maxFlowMinCut.reset();
        int maxFlow = maxFlowMinCut.getMaxFlow();
        assertEquals(1, maxFlow);
    }
}