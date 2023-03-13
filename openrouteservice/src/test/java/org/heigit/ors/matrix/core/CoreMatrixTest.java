package org.heigit.ors.matrix.core;

import com.graphhopper.routing.ch.NodeOrderingProvider;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.TurnCost;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.DefaultTurnCostProvider;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import org.heigit.ors.matrix.MatrixLocations;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.matrix.MatrixRequest;
import org.heigit.ors.matrix.MatrixResult;
import org.heigit.ors.matrix.algorithms.core.CoreMatrixAlgorithm;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreTestEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.core.PrepareCore;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.TurnRestrictionsCoreEdgeFilter;
import org.heigit.ors.util.DebugUtility;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CoreMatrixTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder(5, 5, 3);
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private Weighting weighting = new ShortestWeighting(carEncoder);
    private final CHConfig chConfig = new CHConfig("c", weighting, true, CHConfig.TYPE_CORE);
    private GraphHopperStorage g;
    private RoutingCHGraph routingCHGraph;

    private void addRestrictedTurn(GraphHopperStorage g, int from, int via, int to) {
        setTurnCost(g, Double.POSITIVE_INFINITY, from, via, to);
    }

    private void setTurnCost(GraphHopperStorage g, double cost, int from, int via, int to) {
        g.getTurnCostStorage().set(
                ((EncodedValueLookup) g.getEncodingManager()).getDecimalEncodedValue(TurnCost.key(carEncoder.toString())),
                from,
                via,
                to,
                cost);
    }

    @BeforeEach
    void setUp() {
        g = new GraphBuilder(encodingManager).setCHConfigs(chConfig).withTurnCosts(true).create();
        routingCHGraph = g.getRoutingCHGraph();
    }

    /**
     * Run before any turn restricted tests to set up the storage and weighting.
     */
    public void setUpTurnRestrictions() {
        g = new GraphBuilder(encodingManager).withTurnCosts(true).build();
        Weighting TRWeighting = new ShortestWeighting(carEncoder, new DefaultTurnCostProvider(carEncoder, g.getTurnCostStorage()));
        CHConfig TRChConfig = new CHConfig("c", TRWeighting, true, CHConfig.TYPE_CORE);
        g.addCHGraph(TRChConfig).create(1000);
        routingCHGraph = g.getRoutingCHGraph();
    }

    private void contractGraph(EdgeFilter restrictedEdges) {
        contractGraph(restrictedEdges, null);
    }

    private void contractGraph(EdgeFilter restrictedEdges, int[] nodeOrdering) {
        g.freeze();

        PrepareCore prepare = new PrepareCore(g, chConfig, restrictedEdges);

        if (nodeOrdering != null)
            prepare.useFixedNodeOrdering(NodeOrderingProvider.fromArray(nodeOrdering));

        prepare.doWork();

        if (DebugUtility.isDebug()) {
            for (int i = 0; i < routingCHGraph.getNodes(); i++)
                System.out.println("nodeId " + i + " level: " + routingCHGraph.getLevel(i));
            for (int i = 0; i < routingCHGraph.getNodes(); i++) {
                RoutingCHEdgeIterator iter = routingCHGraph.createOutEdgeExplorer().setBaseNode(i);
                while (iter.next()) {
                    System.out.print(iter.getBaseNode() + " -> " + iter.getAdjNode() + " via edge " + iter.getEdge());
                    if (iter.isShortcut())
                        System.out.print(" shortcut (" + iter.getSkippedEdge1() + ", " + iter.getSkippedEdge2() + ")");
                    System.out.println(" [weight: " + iter.getWeight(false) + "]");
                }
            }
        }
    }


    @Test
    void testOneToManyAllEdgesInCore() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        restrictedEdges.add(1);
        restrictedEdges.add(2);
        restrictedEdges.add(3);
        restrictedEdges.add(4);
        restrictedEdges.add(5);
        restrictedEdges.add(6);
        restrictedEdges.add(7);
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(10);
        restrictedEdges.add(11);
        restrictedEdges.add(12);
        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(1);
        sources.setData(0, 1, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 4, null);
        destinations.setData(1, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);

        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }

    @Test
    void testManyToManyAllEdgesInCore() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        restrictedEdges.add(1);
        restrictedEdges.add(2);
        restrictedEdges.add(3);
        restrictedEdges.add(4);
        restrictedEdges.add(5);
        restrictedEdges.add(6);
        restrictedEdges.add(7);
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(10);
        restrictedEdges.add(11);
        restrictedEdges.add(12);
        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 1, null);
        sources.setData(1, 0, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 4, null);
        destinations.setData(1, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    /**
     * All start and goal nodes are in core
     */
    @Test
    void testOneToManySomeEdgesInCore() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(5);
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(11);
        restrictedEdges.add(12);

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(1);
        sources.setData(0, 1, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 4, null);
        destinations.setData(1, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }

    /**
     * All start and goal nodes are in core
     */
    @Test
    void testManyToManySomeEdgesInCore() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(1);
        restrictedEdges.add(5);
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(11);
        restrictedEdges.add(12);
        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 1, null);
        sources.setData(1, 0, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 4, null);
        destinations.setData(1, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    /**
     * Not all start and goal nodes are in core
     */
    @Test
    void testOneToManySomeNodesInCore() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(5);
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(11);
        restrictedEdges.add(12);

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(1);
        sources.setData(0, 0, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 4, null);
        destinations.setData(1, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }

    /**
     * Not all start and goal nodes are in core
     */
    @Test
    void testManyToManySomeNodesInCore() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(11);
        restrictedEdges.add(12);

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 0, null);
        sources.setData(1, 1, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 4, null);
        destinations.setData(1, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    /**
     * Not all start and goal nodes are in core
     */
    @Test
    void testOneToManyNoNodesInCore() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(11);
        restrictedEdges.add(12);

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 0, null);
        sources.setData(1, 1, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 3, null);
        destinations.setData(1, 2, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(3.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(3.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    /**
     * There is no core
     */
    @Test
    void testOneToManyNoCore() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 0, null);
        sources.setData(1, 1, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 3, null);
        destinations.setData(1, 2, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(3.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(3.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    /**
     * The connection is found already outside the core
     */
    @Test
    void testConnectionOutsideCoreFromHighestNode() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(1);
        restrictedEdges.add(8);
        sources.setData(0, 0, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 3, null);
        destinations.setData(1, 2, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(3.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }

    /**
     * The connection is found already outside the core
     */
    @Test
    void testConnectionOutsideCoreFromLowestNode() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(1);
        restrictedEdges.add(11);
        sources.setData(0, 5, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 3, null);
        destinations.setData(1, 2, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(3.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }

    /**
     * The connection is found already outside the core
     */
    @Test
    void testConnectionOutsideCoreManyToMany() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        restrictedEdges.add(9);
        sources.setData(0, 5, null);
        sources.setData(1, 8, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 3, null);
        destinations.setData(1, 0, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(3.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(4.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    /**
     * There is no core
     */
    @Test
    void testAllToAll() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(11);
        restrictedEdges.add(12);

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(9);
        sources.setData(0, 0, null);
        sources.setData(1, 1, null);
        sources.setData(2, 2, null);
        sources.setData(3, 3, null);
        sources.setData(4, 4, null);
        sources.setData(5, 5, null);
        sources.setData(6, 6, null);
        sources.setData(7, 7, null);
        sources.setData(8, 8, null);
        MatrixLocations destinations = new MatrixLocations(9);
        destinations.setData(0, 0, null);
        destinations.setData(1, 1, null);
        destinations.setData(2, 2, null);
        destinations.setData(3, 3, null);
        destinations.setData(4, 4, null);
        destinations.setData(5, 5, null);
        destinations.setData(6, 6, null);
        destinations.setData(7, 7, null);
        destinations.setData(8, 8, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        assertArrayEquals(expected, result.getTable(MatrixMetricsType.DISTANCE), 0);
    }

    /**
     * The connection is found already outside the core
     */
    @Test
    void testStartAndTargetSameNodeOutsideCore() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(1);
        restrictedEdges.add(9);
        sources.setData(0, 1, null);
        MatrixLocations destinations = new MatrixLocations(1);
        destinations.setData(0, 1, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
    }

    /**
     * The connection is found already outside the core
     */
    @Test
    void testStartAndTargetManyToManySameNodeOutsideCore() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        restrictedEdges.add(9);
        sources.setData(0, 1, null);
        sources.setData(0, 2, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 1, null);
        destinations.setData(0, 2, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    /**
     * The connection is found already outside the core
     */
    @Test
    void testStartAndTargetSameNodeInsideCore() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(1);
        restrictedEdges.add(3);
        sources.setData(0, 1, null);
        MatrixLocations destinations = new MatrixLocations(1);
        destinations.setData(0, 1, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
    }

    @Test
    void testStartAndTargetManyToManySameNodeInsideCore() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        restrictedEdges.add(3);
        sources.setData(0, 1, null);
        sources.setData(0, 2, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 1, null);
        destinations.setData(0, 2, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    @Test
    void testStartAndTargetManyToManySameNodeAllNodesInsideCore() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        restrictedEdges.add(1);
        sources.setData(0, 1, null);
        sources.setData(0, 2, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 1, null);
        destinations.setData(0, 2, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    /**
     * All start and goal nodes are in core. Tests a special case in a diamond shaped graph where only the correct stopping criterion will find all shortest paths
     */
    @Test
    void testStoppingCriterion() {
        ToyGraphCreationUtil.createDiamondGraph(g, encodingManager);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        restrictedEdges.add(1);
        restrictedEdges.add(2);
        restrictedEdges.add(3);
        restrictedEdges.add(4);
        restrictedEdges.add(5);

        contractGraph(restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 0, null);
        sources.setData(1, 1, null);
        MatrixLocations destinations = new MatrixLocations(1);
        destinations.setData(0, 4, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(2.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(4.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }

    @Test
    void testOneToOneTurnRestrictions() {
        setUpTurnRestrictions();

        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        addRestrictedTurn(g, 1, 2, 6);

        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        edgeFilterSequence.add(new TurnRestrictionsCoreEdgeFilter(carEncoder, g));

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(11);
        edgeFilterSequence.add(restrictedEdges);
        contractGraph(edgeFilterSequence);
        MatrixLocations sources = new MatrixLocations(1);
        sources.setData(0, 0, null);
        MatrixLocations destinations = new MatrixLocations(1);
        destinations.setData(0, 3, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);

        weighting = new ShortestWeighting(carEncoder, new DefaultTurnCostProvider(carEncoder, g.getTurnCostStorage()));
        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(4.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
    }

    @Test
    void testManyToOneTurnRestrictions() {
        setUpTurnRestrictions();
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        addRestrictedTurn(g, 1, 2, 6);

        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        edgeFilterSequence.add(new TurnRestrictionsCoreEdgeFilter(carEncoder, g));

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(11);
        edgeFilterSequence.add(restrictedEdges);
        contractGraph(edgeFilterSequence);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 0, null);
        sources.setData(1, 8, null);
        MatrixLocations destinations = new MatrixLocations(1);
        destinations.setData(0, 3, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);

        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(4.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }

    @Test
    void testManyToManyTurnRestrictions() {
        setUpTurnRestrictions();
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        addRestrictedTurn(g, 1, 2, 6);

        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        edgeFilterSequence.add(new TurnRestrictionsCoreEdgeFilter(carEncoder, g));

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(11);
        edgeFilterSequence.add(restrictedEdges);
        contractGraph(edgeFilterSequence);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 0, null);
        sources.setData(1, 8, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 3, null);
        destinations.setData(1, 4, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);

        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(4.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    @Test
    void testManyToManyMultipleTurnRestrictions() {
        setUpTurnRestrictions();
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);
        addRestrictedTurn(g, 1, 2, 6);
        addRestrictedTurn(g, 4, 2, 6);
        addRestrictedTurn(g, 12, 7, 10);

        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        edgeFilterSequence.add(new TurnRestrictionsCoreEdgeFilter(carEncoder, g));

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(11);
        edgeFilterSequence.add(restrictedEdges);
        contractGraph(edgeFilterSequence);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 0, null);
        sources.setData(1, 8, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 3, null);
        destinations.setData(1, 4, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);

        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(7.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    @Test
    void testOneToManyRestrictedEdges() {
        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);

        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(6);
        restrictedEdges.add(10);
        restrictedEdges.add(11);
        edgeFilterSequence.add(restrictedEdges);
        contractGraph(edgeFilterSequence);
        MatrixLocations sources = new MatrixLocations(1);
        sources.setData(0, 8, null);
        MatrixLocations destinations = new MatrixLocations(3);
        destinations.setData(0, 3, null);
        destinations.setData(1, 4, null);
        destinations.setData(2, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);

        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, restrictedEdges);
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(8.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(9.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
    }

    @Test
    void testManyToManyRestrictedEdges() {
        setUpTurnRestrictions();

        ToyGraphCreationUtil.createMediumGraph(g, encodingManager);

        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(6);
        restrictedEdges.add(10);
        restrictedEdges.add(11);
        edgeFilterSequence.add(restrictedEdges);
        contractGraph(edgeFilterSequence);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 8, null);
        sources.setData(1, 7, null);
        MatrixLocations destinations = new MatrixLocations(3);
        destinations.setData(0, 3, null);
        destinations.setData(1, 4, null);
        destinations.setData(2, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);

        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, restrictedEdges);
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(8.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(9.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(9.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
        assertEquals(11.0, result.getTable(MatrixMetricsType.DISTANCE)[4], 0);
        assertEquals(12.0, result.getTable(MatrixMetricsType.DISTANCE)[5], 0);
    }

    @Test
    void testOneToOneLevelProblemCase() {
        ToyGraphCreationUtil.createUpDownGraph(g, encodingManager);

        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(9);
        edgeFilterSequence.add(restrictedEdges);
        contractGraph(edgeFilterSequence);

        MatrixLocations sources = new MatrixLocations(1);
        sources.setData(0, 0, null);
        MatrixLocations destinations = new MatrixLocations(1);
        destinations.setData(0, 7, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);

        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);

    }

    @Test
    void testDownwardPassFasterUTurn() {
        ToyGraphCreationUtil.createTwoWayGraph(g, encodingManager);
        addRestrictedTurn(g, 2, 3, 3);

        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        edgeFilterSequence.add(new TurnRestrictionsCoreEdgeFilter(carEncoder, g));
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(10);
        restrictedEdges.add(11);
        edgeFilterSequence.add(restrictedEdges);
        contractGraph(edgeFilterSequence);

        MatrixLocations sources = new MatrixLocations(1);
        sources.setData(0, 7, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 5, null);
        destinations.setData(1, 7, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);

        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // TODO Future improvement of algo: this would be the correct result, but the algorithm cannot provide this currently.
//        assertEquals(13.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
//        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);

    }

    @Test
    void testUpdateWeight() {
        ToyGraphCreationUtil.createUpdatedGraph(g, encodingManager);

        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        contractGraph(edgeFilterSequence);

        MatrixLocations sources = new MatrixLocations(1);
        sources.setData(0, 0, null);
        MatrixLocations destinations = new MatrixLocations(1);
        destinations.setData(0, 1, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);

        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(2.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
    }

    @Test
    void testSwapStartsGoalsNoCore() {
        ToyGraphCreationUtil.createDirectedGraph(g, encodingManager);

        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        contractGraph(edgeFilterSequence);

        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 0, null);
        sources.setData(1, 2, null);
        MatrixLocations destinations = new MatrixLocations(1);
        destinations.setData(0, 1, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);

        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(2.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }

    @Test
    void testSwapStartsGoalsCore() {
        ToyGraphCreationUtil.createDirectedGraph(g, encodingManager);

        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        restrictedEdges.add(1);
        restrictedEdges.add(2);
        restrictedEdges.add(3);

        contractGraph(restrictedEdges);

        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 0, null);
        sources.setData(1, 2, null);
        MatrixLocations destinations = new MatrixLocations(1);
        destinations.setData(0, 1, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);

        algorithm.init(matrixRequest, g.getRoutingCHGraph(), carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try {
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(2.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }
}
