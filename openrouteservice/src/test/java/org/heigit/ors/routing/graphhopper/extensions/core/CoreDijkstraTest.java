/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.heigit.ors.routing.graphhopper.extensions.core;

import com.carrotsearch.hppc.IntArrayList;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.TurnCost;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.DefaultTurnCostProvider;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.ShortestWeighting;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;

import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.TurnRestrictionsCoreEdgeFilter;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.graphhopper.util.GHUtility.updateDistancesFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test routing with {@link CoreDijkstra}
 *
 * @author Andrzej Oles
 */
public class CoreDijkstraTest {

    private final EncodingManager encodingManager;
    private final FlagEncoder carEncoder;
    private final Weighting weighting;
    private final CHConfig chConfig;
    GraphHopperStorage ghStorage;

    public CoreDijkstraTest() {
        encodingManager = EncodingManager.create("car");
        carEncoder = encodingManager.getEncoder("car");
        weighting = new ShortestWeighting(carEncoder);
        chConfig = new CHConfig(weighting.getName(), weighting, false, CHConfig.TYPE_CORE);
    }

    // 0-1-2-3
    // |/|/ /|
    // 4-5-- |
    // |/ \--7
    // 6----/
    static void initDirectedAndDiffSpeed(Graph graph, FlagEncoder enc) {
        GHUtility.setSpeed(10, true, false, enc, graph.edge(0, 1));
        GHUtility.setSpeed(100, true, false, enc, graph.edge(0, 4));

        GHUtility.setSpeed(10, true, true, enc, graph.edge(1, 4));
        GHUtility.setSpeed(10, true, true, enc, graph.edge(1, 5));
        EdgeIteratorState edge12 = GHUtility.setSpeed(10, true, true, enc, graph.edge(1, 2));

        GHUtility.setSpeed(10, true, false, enc, graph.edge(5, 2));
        GHUtility.setSpeed(10, true, false, enc, graph.edge(2, 3));

        EdgeIteratorState edge53 = GHUtility.setSpeed(20, true, false, enc, graph.edge(5, 3));
        GHUtility.setSpeed(10, true, false, enc, graph.edge(3, 7));

        GHUtility.setSpeed(100, true, false, enc, graph.edge(4, 6));
        GHUtility.setSpeed(10, true, false, enc, graph.edge(5, 4));

        GHUtility.setSpeed(10, true, false, enc, graph.edge(5, 6));
        GHUtility.setSpeed(100, true, false, enc, graph.edge(7, 5));

        GHUtility.setSpeed(100, true, true, enc, graph.edge(6, 7));

        updateDistancesFor(graph, 0, 0.002, 0);
        updateDistancesFor(graph, 1, 0.002, 0.001);
        updateDistancesFor(graph, 2, 0.002, 0.002);
        updateDistancesFor(graph, 3, 0.002, 0.003);
        updateDistancesFor(graph, 4, 0.0015, 0);
        updateDistancesFor(graph, 5, 0.0015, 0.001);
        updateDistancesFor(graph, 6, 0, 0);
        updateDistancesFor(graph, 7, 0.001, 0.003);

        edge12.setDistance(edge12.getDistance() * 2);
        edge53.setDistance(edge53.getDistance() * 2);
    }

    private GraphHopperStorage createGHStorage(Weighting weighting) {
        return new GraphBuilder(encodingManager).setCHConfigs(chConfig).create();
    }

    private void prepareCore(GraphHopperStorage graphHopperStorage, CHConfig chConfig, EdgeFilter restrictedEdges) {
        graphHopperStorage.freeze();
        PrepareCore prepare = new PrepareCore(graphHopperStorage, chConfig, restrictedEdges);
        prepare.doWork();
    }

    @Test
    public void testCHGraph() {
        // No core at all
        GraphHopperStorage ghStorage = createGHStorage(weighting);
        initDirectedAndDiffSpeed(ghStorage, carEncoder);

        prepareCore(ghStorage, chConfig, new CoreTestEdgeFilter());

        RoutingCHGraph chGraph = ghStorage.getRoutingCHGraph();

        CoreDijkstraFilter coreFilter = new CoreDijkstraFilter(chGraph);
        RoutingAlgorithm algo = new CoreRoutingAlgorithmFactory(chGraph).createAlgo(weighting, new AlgorithmOptions()).setEdgeFilter(coreFilter);
        Path p1 = algo.calcPath(0, 3);

        assertEquals(IntArrayList.from(0, 1, 5, 2, 3), p1.calcNodes());
        assertEquals(p1.toString(), 402.30, p1.getDistance(), 1e-2);
        assertEquals(p1.toString(), 144829, p1.getTime());
    }

    @Test
    public void testCoreGraph() {
        // All edges are part of core
        GraphHopperStorage ghStorage = createGHStorage(weighting);
        initDirectedAndDiffSpeed(ghStorage, carEncoder);

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        for (int edge = 0; edge < ghStorage.getEdges(); edge++)
            restrictedEdges.add(edge);

        prepareCore(ghStorage, chConfig, restrictedEdges);

        RoutingCHGraph chGraph = ghStorage.getRoutingCHGraph();

        CoreDijkstraFilter coreFilter = new CoreDijkstraFilter(chGraph);
        RoutingAlgorithm algo = new CoreRoutingAlgorithmFactory(chGraph).createAlgo(weighting, new AlgorithmOptions()).setEdgeFilter(coreFilter);
        Path p1 = algo.calcPath(0, 3);

        assertEquals(IntArrayList.from(0, 1, 5, 2, 3), p1.calcNodes());
        assertEquals(p1.toString(), 402.30, p1.getDistance(), 1e-2);
        assertEquals(p1.toString(), 144829, p1.getTime());
    }

    @Test
    public void testMixedGraph() {
        // Core consisting of a single edge 1-2
        ghStorage = createGHStorage(weighting);
        initDirectedAndDiffSpeed(ghStorage, carEncoder);

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(4);

        prepareCore(ghStorage, chConfig, restrictedEdges);

        RoutingCHGraph chGraph = ghStorage.getRoutingCHGraph();

        CoreDijkstraFilter coreFilter = new CoreDijkstraFilter(chGraph);
        RoutingAlgorithm algo = new CoreRoutingAlgorithmFactory(chGraph).createAlgo(weighting, new AlgorithmOptions()).setEdgeFilter(coreFilter);
        Path p1 = algo.calcPath(0, 3);

        Integer[] core = {1, 2};
        assertCore(ghStorage, new HashSet<>(Arrays.asList(core)));
        assertEquals(IntArrayList.from(0, 1, 5, 2, 3), p1.calcNodes());
        assertEquals(p1.toString(), 402.30, p1.getDistance(), 1e-2);
        assertEquals(p1.toString(), 144829, p1.getTime());
    }

    @Test
    public void testMixedGraph2() {
        // Core consisting of edges 1-5 and 5-2
        ghStorage = createGHStorage(weighting);
        initDirectedAndDiffSpeed(ghStorage, carEncoder);

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(3);
        restrictedEdges.add(5);

        prepareCore(ghStorage, chConfig, restrictedEdges);

        RoutingCHGraph chGraph = ghStorage.getRoutingCHGraph();

        CoreDijkstraFilter coreFilter = new CoreDijkstraFilter(chGraph);
        RoutingAlgorithm algo = new CoreRoutingAlgorithmFactory(chGraph).createAlgo(weighting, new AlgorithmOptions()).setEdgeFilter(coreFilter);
        Path p1 = algo.calcPath(0, 3);

        Integer[] core = {1, 2, 5};
        assertCore(ghStorage, new HashSet<>(Arrays.asList(core)));
        assertEquals(IntArrayList.from(0, 1, 5, 2, 3), p1.calcNodes());
        assertEquals(p1.toString(), 402.30, p1.getDistance(), 1e-2);
        assertEquals(p1.toString(), 144829, p1.getTime());
    }

    @Test
    public void testCoreRestriction() {
        // Core consisting of edges 1-5 and 5-2
        ghStorage = createGHStorage(weighting);
        initDirectedAndDiffSpeed(ghStorage, carEncoder);

        CoreTestEdgeFilter coreEdges = new CoreTestEdgeFilter();
        coreEdges.add(3);
        coreEdges.add(5);

        prepareCore(ghStorage, chConfig, coreEdges);

        Integer[] core = {1, 2, 5};
        assertCore(ghStorage, new HashSet<>(Arrays.asList(core)));

        RoutingCHGraph chGraph = ghStorage.getRoutingCHGraph();
        CoreDijkstraFilter coreFilter = new CoreDijkstraFilter(chGraph);
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        coreFilter.addRestrictionFilter(restrictedEdges);

        RoutingAlgorithm algo = new CoreRoutingAlgorithmFactory(chGraph).createAlgo(weighting, new AlgorithmOptions()).setEdgeFilter(coreFilter);
        restrictedEdges.add(5);
        Path p1 = algo.calcPath(0, 3);
        assertEquals(IntArrayList.from(0, 1, 2, 3), p1.calcNodes());

        algo = new CoreRoutingAlgorithmFactory(chGraph).createAlgo(weighting, new AlgorithmOptions()).setEdgeFilter(coreFilter);
        restrictedEdges.add(4);
        Path p2 = algo.calcPath(0, 3);
        assertEquals(IntArrayList.from(0, 1, 5, 3), p2.calcNodes());
    }

    /**
     * Test whether only the core nodes have maximum level
     *
     * @param coreNodes
     */
    private void assertCore(GraphHopperStorage ghStorage, Set<Integer> coreNodes) {
        int nodes = ghStorage.getRoutingCHGraph().getNodes();
        int maxLevel = nodes;
        for (int node = 0; node < nodes; node++) {
            int level = ghStorage.getRoutingCHGraph().getLevel(node);
            if (coreNodes.contains(node)) {
                assertEquals(maxLevel, level);
            } else {
                assertTrue(level < maxLevel);
            }
        }
    }

    @Test
    public void testTwoProfiles() {
        EncodingManager em = EncodingManager.create("foot,car");
        FlagEncoder footEncoder = em.getEncoder("foot");
        FlagEncoder carEncoder = em.getEncoder("car");
        FastestWeighting footWeighting = new FastestWeighting(footEncoder);
        FastestWeighting carWeighting = new FastestWeighting(carEncoder);

        CHConfig footConfig = new CHConfig("p_foot", footWeighting, false, CHConfig.TYPE_CORE);
        CHConfig carConfig = new CHConfig("p_car", carWeighting, false, CHConfig.TYPE_CORE);
        GraphHopperStorage g = new GraphBuilder(em).setCHConfigs(footConfig, carConfig).create();
        initFootVsCar(carEncoder, footEncoder, g);

        //car
        prepareCore(g, carConfig, new CoreTestEdgeFilter());
        RoutingCHGraph chGraph = g.getRoutingCHGraph(carConfig.getName());
        CoreDijkstraFilter coreFilter = new CoreDijkstraFilter(chGraph);
        RoutingAlgorithm algo = new CoreRoutingAlgorithmFactory(chGraph).createAlgo(weighting, new AlgorithmOptions()).setEdgeFilter(coreFilter);
        Path p1 = algo.calcPath(0, 7);

        assertEquals(IntArrayList.from(0, 4, 6, 7), p1.calcNodes());
        assertEquals(p1.toString(), 15000, p1.getDistance(), 1e-6);
        assertEquals(p1.toString(), 2700 * 1000, p1.getTime());

        //foot
        prepareCore(g, footConfig, new CoreTestEdgeFilter());
        chGraph = g.getRoutingCHGraph(footConfig.getName());
        coreFilter = new CoreDijkstraFilter(chGraph);
        algo = new CoreRoutingAlgorithmFactory(chGraph).createAlgo(weighting, new AlgorithmOptions()).setEdgeFilter(coreFilter);

        Path p2 = algo.calcPath(0, 7);
        assertEquals(p2.toString(), 17000, p2.getDistance(), 1e-6);
        assertEquals(p2.toString(), 12240 * 1000, p2.getTime());
        assertEquals(IntArrayList.from(0, 4, 5, 7), p2.calcNodes());
    }

    static void initFootVsCar(FlagEncoder carEncoder, FlagEncoder footEncoder, Graph graph) {
        EdgeIteratorState edge = graph.edge(0, 1).setDistance(7000);
        GHUtility.setSpeed(5, true, true, footEncoder, edge);
        GHUtility.setSpeed(10, true, false, carEncoder, edge);
        edge = graph.edge(0, 4).setDistance(5000);
        GHUtility.setSpeed(5, true, true, footEncoder, edge);
        GHUtility.setSpeed(20, true, false, carEncoder, edge);

        GHUtility.setSpeed(10, true, true, carEncoder, graph.edge(1, 4).setDistance(7000));
        GHUtility.setSpeed(10, true, true, carEncoder, graph.edge(1, 5).setDistance(7000));
        edge = graph.edge(1, 2).setDistance(20000);
        GHUtility.setSpeed(5, true, true, footEncoder, edge);
        GHUtility.setSpeed(10, true, true, carEncoder, edge);

        GHUtility.setSpeed(10, true, false, carEncoder, graph.edge(5, 2).setDistance(5000));
        edge = graph.edge(2, 3).setDistance(5000);
        GHUtility.setSpeed(5, true, true, footEncoder, edge);
        GHUtility.setSpeed(10, true, false, carEncoder, edge);

        GHUtility.setSpeed(20, true, false, carEncoder, graph.edge(5, 3).setDistance(11000));
        edge = graph.edge(3, 7).setDistance(7000);
        GHUtility.setSpeed(5, true, true, footEncoder, edge);
        GHUtility.setSpeed(10, true, false, carEncoder, edge);

        GHUtility.setSpeed(20, true, false, carEncoder, graph.edge(4, 6).setDistance(5000));
        edge = graph.edge(5, 4).setDistance(7000);
        GHUtility.setSpeed(5, true, true, footEncoder, edge);
        GHUtility.setSpeed(10, true, false, carEncoder, edge);

        GHUtility.setSpeed(10, true, false, carEncoder, graph.edge(5, 6).setDistance(7000));
        edge = graph.edge(7, 5).setDistance(5000);
        GHUtility.setSpeed(5, true, true, footEncoder, edge);
        GHUtility.setSpeed(20, true, false, carEncoder, edge);

        GHUtility.setSpeed(20, true, true, carEncoder, graph.edge(6, 7).setDistance(5000));
    }

    @Test
    public void testOneToOneTurnRestrictions() {
        CarFlagEncoder carEncoder = new CarFlagEncoder(5, 5, 3);
        EncodingManager encodingManager = EncodingManager.create(carEncoder);
        GraphHopperStorage ghStorage = new GraphBuilder(encodingManager).build();
        Weighting weighting = new ShortestWeighting(carEncoder, new DefaultTurnCostProvider(carEncoder, ghStorage.getTurnCostStorage()));
        CHConfig chConfig = new CHConfig("c", weighting, true, CHConfig.TYPE_CORE);
        ghStorage.addCHGraph(chConfig).create(1000);

        ToyGraphCreationUtil.createMediumGraph(ghStorage, encodingManager);
        setTurnCost(ghStorage, Double.POSITIVE_INFINITY, 1, 2, 6);

        EdgeFilterSequence coreEdgeFilter = new EdgeFilterSequence();
        coreEdgeFilter.add(new TurnRestrictionsCoreEdgeFilter(carEncoder, ghStorage));
        prepareCore(ghStorage, chConfig, coreEdgeFilter);

        Integer[] core = {0, 2, 3};
        assertCore(ghStorage, new HashSet<>(Arrays.asList(core)));

        RoutingCHGraph chGraph = ghStorage.getRoutingCHGraph();
        CoreDijkstraFilter coreFilter = new CoreDijkstraFilter(chGraph);
        RoutingAlgorithm algo = new CoreRoutingAlgorithmFactory(chGraph).createAlgo(weighting, new AlgorithmOptions()).setEdgeFilter(coreFilter);

        Path p = algo.calcPath(0, 3);
        assertEquals(p.toString(), 4, p.getDistance(), 1e-6);
    }

    private void setTurnCost(GraphHopperStorage g, double cost, int from, int via, int to) {
        g.getTurnCostStorage().set(
                ((EncodedValueLookup) g.getEncodingManager()).getDecimalEncodedValue(TurnCost.key(carEncoder.toString())),
                from,
                via,
                to,
                cost);
    }

    @Ignore
    @Test
    public void testUTurn() {
        CarFlagEncoder carEncoder = new CarFlagEncoder(5, 5, 3);
        EncodingManager encodingManager = EncodingManager.create(carEncoder);
        GraphHopperStorage graph = new GraphBuilder(encodingManager).build();
        Weighting weighting = new ShortestWeighting(carEncoder, new DefaultTurnCostProvider(carEncoder, graph.getTurnCostStorage()));
        CHConfig chConfig = new CHConfig("c", weighting, true, CHConfig.TYPE_CORE);
        graph.addCHGraph(chConfig).create(1000);
        //       0
        //       |
        //       1
        //       |
        // 7-6-5-2-3-4
        // | |
        // 8-9
        GHUtility.setSpeed(60, true, false, carEncoder, graph.edge(0, 1).setDistance(1));// 0
        GHUtility.setSpeed(60, true, false, carEncoder, graph.edge(1, 2).setDistance(1));// 1
        GHUtility.setSpeed(60, true, false, carEncoder, graph.edge(2, 3).setDistance(1));// 2
        GHUtility.setSpeed(60, true, false, carEncoder, graph.edge(3, 4).setDistance(1));// 3
        GHUtility.setSpeed(60, true, true, carEncoder, graph.edge(2, 5).setDistance(1));//  4
        GHUtility.setSpeed(60, true, true, carEncoder, graph.edge(5, 6).setDistance(1));//  5
        GHUtility.setSpeed(60, true, false, carEncoder, graph.edge(6, 7).setDistance(1));// 6
        GHUtility.setSpeed(60, true, false, carEncoder, graph.edge(7, 8).setDistance(1));// 7
        GHUtility.setSpeed(60, true, false, carEncoder, graph.edge(8, 9).setDistance(1));// 8
        GHUtility.setSpeed(60, true, false, carEncoder, graph.edge(9, 6).setDistance(1));// 9

        setTurnCost(graph, Double.POSITIVE_INFINITY, 1, 2, 2);
        setTurnCost(graph, Double.POSITIVE_INFINITY, 9, 6, 6);

        EdgeFilterSequence coreEdgeFilter = new EdgeFilterSequence();
        coreEdgeFilter.add(new TurnRestrictionsCoreEdgeFilter(carEncoder, graph));
        prepareCore(graph, chConfig, coreEdgeFilter);

        Integer[] core = {1, 2, 3, 9, 6, 7};
        assertCore(graph, new HashSet<>(Arrays.asList(core)));

        RoutingCHGraph chGraph = graph.getRoutingCHGraph();
        RoutingAlgorithm algo = new CoreRoutingAlgorithmFactory(chGraph).createAlgo(weighting, new AlgorithmOptions());

        Path p = algo.calcPath(0, 4);
        assertEquals(p.toString(), 12, p.getDistance(), 1e-6);
    }
}
