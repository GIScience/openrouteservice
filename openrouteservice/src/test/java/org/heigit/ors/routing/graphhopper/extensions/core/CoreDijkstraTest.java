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
import com.graphhopper.routing.Path;
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.ShortestWeighting;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;

import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
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

    private void prepareCH(GraphHopperStorage graphHopperStorage, CHConfig chConfig) {
        graphHopperStorage.freeze();
        PrepareContractionHierarchies pch = PrepareContractionHierarchies.fromGraphHopperStorage(graphHopperStorage, chConfig);
        pch.doWork();
    }

    private void prepareCore(GraphHopperStorage graphHopperStorage, CHConfig chConfig, CoreTestEdgeFilter restrictedEdges) {
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

        CoreDijkstraFilter coreFilter = new CoreDijkstraFilter(ghStorage.getRoutingCHGraph());
        Path p1 = new CoreDijkstra(ghStorage, weighting).setEdgeFilter(coreFilter).calcPath(0, 3);

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
        for (int edge = 0; edge < ghStorage.getEdges(); edge ++)
            restrictedEdges.add(edge);

        prepareCore(ghStorage, chConfig, restrictedEdges);

        CoreDijkstraFilter coreFilter = new CoreDijkstraFilter(ghStorage.getRoutingCHGraph());
        Path p1 = new CoreDijkstra(ghStorage, weighting).setEdgeFilter(coreFilter).calcPath(0, 3);

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

        CoreDijkstraFilter coreFilter = new CoreDijkstraFilter(ghStorage.getRoutingCHGraph());
        Path p1 = new CoreDijkstra(ghStorage, weighting).setEdgeFilter(coreFilter).calcPath(0, 3);

        Integer[] core = {1, 2};
        assertCore(new HashSet<>(Arrays.asList(core)));
        assertEquals(IntArrayList.from(0, 1, 5, 2, 3), p1.calcNodes());
        assertEquals(p1.toString(), 402.30, p1.getDistance(), 1e-2);
        assertEquals(p1.toString(), 144829, p1.getTime());
    }

    @Test
    public void testMixedGraph2() {
        // Core consisting of a single edges 1-5 and 5-2
        ghStorage = createGHStorage(weighting);
        initDirectedAndDiffSpeed(ghStorage, carEncoder);

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(3);
        restrictedEdges.add(5);

        prepareCore(ghStorage, chConfig, restrictedEdges);

        CoreDijkstraFilter coreFilter = new CoreDijkstraFilter(ghStorage.getRoutingCHGraph());
        Path p1 = new CoreDijkstra(ghStorage, weighting).setEdgeFilter(coreFilter).calcPath(0, 3);

        Integer[] core = {1, 2, 5};
        assertCore(new HashSet<>(Arrays.asList(core)));
        assertEquals(IntArrayList.from(0, 1, 5, 2, 3), p1.calcNodes());
        assertEquals(p1.toString(), 402.30, p1.getDistance(), 1e-2);
        assertEquals(p1.toString(), 144829, p1.getTime());
    }

    /**
     * Test whether only the core nodes have maximum level
     *
     * @param coreNodes
     */
    private void assertCore(Set<Integer> coreNodes) {
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
/*
    @Test
    public void testBaseGraphMultipleVehicles() {
        EncodingManager em = EncodingManager.create("foot,car");
        FlagEncoder footEncoder = em.getEncoder("foot");
        FlagEncoder carEncoder = em.getEncoder("car");
        FastestWeighting footWeighting = new FastestWeighting(footEncoder);
        FastestWeighting carWeighting = new FastestWeighting(carEncoder);

        CHConfig footConfig = CHConfig.nodeBased("p_foot", footWeighting);
        CHConfig carConfig = CHConfig.nodeBased("p_car", carWeighting);
        GraphHopperStorage g = new GraphBuilder(em).setCHConfigs(footConfig, carConfig).create();
        RoutingAlgorithmTest.initFootVsCar(carEncoder, footEncoder, g);

        // do CH preparation for car
        prepareCH(g, carConfig);

        // use contracted graph for car
        RoutingCHGraph chGraph = g.getRoutingCHGraph(carConfig.getName());
        Path p1 = createCHAlgo(chGraph, true).calcPath(0, 7);
        assertEquals(IntArrayList.from(0, 4, 6, 7), p1.calcNodes());
        assertEquals(15000, p1.getDistance(), 1e-6, p1.toString());

        // use base graph for solving normal Dijkstra via car
        Path p2 = new RoutingAlgorithmFactorySimple().createAlgo(g, carWeighting, new AlgorithmOptions()).calcPath(0, 7);
        assertEquals(IntArrayList.from(0, 4, 6, 7), p2.calcNodes());
        assertEquals(15000, p2.getDistance(), 1e-6, p2.toString());
        assertEquals(2700 * 1000, p2.getTime(), p2.toString());

        // use base graph for solving normal Dijkstra via foot
        Path p4 = new RoutingAlgorithmFactorySimple().createAlgo(g, footWeighting, new AlgorithmOptions()).calcPath(0, 7);
        assertEquals(17000, p4.getDistance(), 1e-6, p4.toString());
        assertEquals(12240 * 1000, p4.getTime(), p4.toString());
        assertEquals(IntArrayList.from(0, 4, 5, 7), p4.calcNodes());
    }
*/
}
