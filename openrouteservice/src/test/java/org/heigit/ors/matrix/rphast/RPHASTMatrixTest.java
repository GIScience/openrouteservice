package org.heigit.ors.matrix.rphast;

import com.graphhopper.routing.ch.NodeOrderingProvider;
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import org.heigit.ors.routing.algorithms.RPHASTAlgorithm;
import org.heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntry;
import org.heigit.ors.util.DebugUtility;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RPHASTMatrixTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder().setSpeedTwoDirections(true);
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private final Weighting weighting = new ShortestWeighting(carEncoder);
    private final CHConfig chConfig = CHConfig.nodeBased("c", weighting);
    private GraphHopperStorage g;
    private RoutingCHGraph routingCHGraph;

    @BeforeEach
    void setUp() {
        g = createGHStorage();
        routingCHGraph = g.getRoutingCHGraph();
    }

    private GraphHopperStorage createGHStorage() {
        return createGHStorage(chConfig);
    }

    private GraphHopperStorage createGHStorage(CHConfig c) {
        return new GraphBuilder(encodingManager).setCHConfigs(c).create();
    }

    private void printGraph() {
        if (DebugUtility.isDebug()) {
            for (int i = 0; i < routingCHGraph.getNodes(); i++)
                System.out.println("nodeId " + i + " level: " + routingCHGraph.getLevel(i));
            for (int i = 0; i < routingCHGraph.getNodes(); i++) {
                RoutingCHEdgeIterator iter = routingCHGraph.createOutEdgeExplorer().setBaseNode(i);
                while (iter.next()) {
                    System.out.print(iter.getBaseNode() + " -> " + iter.getAdjNode() + " via edge " + iter.getEdge());
                    if (iter.isShortcut())
                        System.out.print(" (shortcut)");
                    System.out.println(" [weight: " + iter.getWeight(false) + "]");
                }
            }
        }
    }

    @Test
    void testAddShortcuts() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        PrepareContractionHierarchies prepare = createPrepareContractionHierarchies(g);
        prepare.doWork();
        printGraph();
        assertEquals(16, routingCHGraph.getEdges());
    }

    @Test
    void testOneToOne() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        PrepareContractionHierarchies prepare = createPrepareContractionHierarchies(g);
        prepare.doWork();
        RPHASTAlgorithm algorithm = new RPHASTAlgorithm(routingCHGraph, weighting,
                TraversalMode.NODE_BASED);
        int[] srcIds = new int[]{1};
        int[] dstIds = new int[]{5};
        algorithm.prepare(srcIds, dstIds);
        MultiTreeSPEntry[] destTrees = algorithm.calcPaths(srcIds, dstIds);
        assertEquals(6.0, destTrees[0].getItem(0).getWeight(), 1e-6);
    }

    @Test
    void testOneToMany() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        PrepareContractionHierarchies prepare = createPrepareContractionHierarchies(g);
        prepare.doWork();
        RPHASTAlgorithm algorithm = new RPHASTAlgorithm(routingCHGraph, weighting,
                TraversalMode.NODE_BASED);
        int[] srcIds = new int[]{1};
        int[] dstIds = new int[]{4, 5, 6, 7};
        algorithm.prepare(srcIds, dstIds);
        MultiTreeSPEntry[] destTrees = algorithm.calcPaths(srcIds, dstIds);
        assertEquals(5.0, destTrees[0].getItem(0).getWeight(), 1e-6);
        assertEquals(6.0, destTrees[1].getItem(0).getWeight(), 1e-6);
        assertEquals(6.0, destTrees[2].getItem(0).getWeight(), 1e-6);
        assertEquals(5.0, destTrees[3].getItem(0).getWeight(), 1e-6);
    }

    @Test
    void testManyToOne() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        PrepareContractionHierarchies prepare = createPrepareContractionHierarchies(g);
        prepare.doWork();
        RPHASTAlgorithm algorithm = new RPHASTAlgorithm(routingCHGraph, weighting,
                TraversalMode.NODE_BASED);
        int[] srcIds = new int[]{4, 5, 6, 7};
        int[] dstIds = new int[]{1};
        algorithm.prepare(srcIds, dstIds);
        MultiTreeSPEntry[] destTrees = algorithm.calcPaths(srcIds, dstIds);
        assertEquals(5.0, destTrees[0].getItem(0).getWeight(), 1e-6);
        assertEquals(6.0, destTrees[0].getItem(1).getWeight(), 1e-6);
        assertEquals(6.0, destTrees[0].getItem(2).getWeight(), 1e-6);
        assertEquals(5.0, destTrees[0].getItem(3).getWeight(), 1e-6);
    }

    @Test
    void testManyToMany() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        PrepareContractionHierarchies prepare = createPrepareContractionHierarchies(g);
        prepare.doWork();
        RPHASTAlgorithm algorithm = new RPHASTAlgorithm(routingCHGraph, weighting,
                TraversalMode.NODE_BASED);
        int[] srcIds = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
        int[] dstIds = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
        algorithm.prepare(srcIds, dstIds);
        MultiTreeSPEntry[] destTrees = algorithm.calcPaths(srcIds, dstIds);
        float[] expected = new float[]{
                0.0f, 1.0f, 1.0f, 3.0f, 5.0f, 5.0f, 6.0f, 4.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 3.0f, 5.0f, 6.0f, 6.0f, 5.0f, 2.0f,
                1.0f, 1.0f, 0.0f, 2.0f, 4.0f, 5.0f, 5.0f, 5.0f, 2.0f,
                3.0f, 3.0f, 2.0f, 0.0f, 2.0f, 3.0f, 3.0f, 4.0f, 4.0f,
                5.0f, 5.0f, 4.0f, 2.0f, 0.0f, 1.0f, 1.0f, 2.0f, 5.0f,
                5.0f, 6.0f, 5.0f, 3.0f, 1.0f, 0.0f, 2.0f, 1.0f, 4.0f,
                6.0f, 6.0f, 5.0f, 3.0f, 1.0f, 2.0f, 0.0f, 2.0f, 5.0f,
                4.0f, 5.0f, 5.0f, 4.0f, 2.0f, 1.0f, 2.0f, 0.0f, 3.0f,
                1.0f, 2.0f, 2.0f, 4.0f, 5.0f, 4.0f, 5.0f, 3.0f, 0.0f
        };
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                assertEquals(expected[i * 9 + j], destTrees[j].getItem(i).getWeight(), 1e-6);
            }
        }
    }


    private PrepareContractionHierarchies createPrepareContractionHierarchies(GraphHopperStorage g) {
        return createPrepareContractionHierarchies(g, chConfig);
    }

    private PrepareContractionHierarchies createPrepareContractionHierarchies(GraphHopperStorage g, CHConfig p) {
        g.freeze();
        return PrepareContractionHierarchies.fromGraphHopperStorage(g, p);
    }

    private void useNodeOrdering(PrepareContractionHierarchies prepare, int[] nodeOrdering) {
        prepare.useFixedNodeOrdering(NodeOrderingProvider.fromArray(nodeOrdering));
    }
}
