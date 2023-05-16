/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.routing.ch.NodeOrderingProvider;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.GHUtility;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperStorage;
import org.heigit.ors.util.DebugUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Hendrik Leuschner, Andrzej Oles, Djime Gueye
 */
public class PrepareCoreTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder(5, 5, 3);
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private final Weighting weighting = new ShortestWeighting(carEncoder);
    private final CHConfig chConfig = new CHConfig("c", weighting, false, CHConfig.TYPE_CORE);
    private ORSGraphHopperStorage g;
    private RoutingCHGraph routingCHGraph;

    @BeforeEach
    void setUp() {
        g = new ORSGraphHopperStorage(new RAMDirectory(), encodingManager, false, false, -1);
        g.addCoreGraph(chConfig);
        g.create(1000);
        routingCHGraph = g.getCoreGraph(chConfig.getName());
    }

    private void createSimpleGraph() {
        // 5--1---2
        //     \ /|
        //      0 |
        //     /  |
        //    4---3
        addEdge(0, 1, 1);
        addEdge(0, 2, 1);
        addEdge(0, 4, 3); // restricted in #1 and #4
        addEdge(1, 2, 2);
        addEdge(2, 3, 1); // restricted in #2
        addEdge(4, 3, 2); // restricted in #3, #4 and #5
        addEdge(5, 1, 2); // restricted in #5
    }

    private void addEdge(int a, int b, double distance) {
        GHUtility.setSpeed(60, true, true, carEncoder, g.edge(a, b).setDistance(distance));
    }

    public void createMediumGraph() {
        //    3---4--5
        //   /\   |  |
        //  2--0  6--7
        //  | / \   /
        //  |/   \ /
        //  1-----8
        addEdge(0, 1, 1); // restricted in #1 and #2
        addEdge(0, 2, 1);
        addEdge(0, 3, 5);
        addEdge(0, 8, 1);
        addEdge(1, 2, 1);
        addEdge(1, 8, 2);
        addEdge(2, 3, 2); // restricted in #2 and #3
        addEdge(3, 4, 2); // restricted in #4
        addEdge(4, 5, 1);
        addEdge(4, 6, 1);
        addEdge(5, 7, 1);
        addEdge(6, 7, 2);
        addEdge(7, 8, 3); // restricted in #3 and #4
    }

    private void createComplexGraph() {
        // prepare-routing.svg
        addEdge(0, 1, 1);
        addEdge(0, 2, 1);
        addEdge(1, 2, 1);
        addEdge(2, 3, 1.5);
        addEdge(1, 4, 1);
        addEdge(2, 9, 1);
        addEdge(9, 3, 1);
        addEdge(10, 3, 1);
        addEdge(4, 5, 1);
        addEdge(5, 6, 1);
        addEdge(6, 7, 1); //make this restricted; edge 10
        addEdge(7, 8, 1);
        addEdge(8, 9, 1);
        addEdge(4, 11, 1);
        addEdge(9, 14, 1);
        addEdge(10, 14, 1);
        addEdge(11, 12, 1);
        addEdge(12, 15, 1); //make this restricted; edge 17
        addEdge(12, 13, 1);
        addEdge(13, 16, 1);
        addEdge(15, 16, 2);
        addEdge(14, 16, 1);
    }

    private RoutingCHGraph contractGraph(EdgeFilter restrictedEdges) {
        return contractGraph(restrictedEdges, null);
    }

    private RoutingCHGraph contractGraph(EdgeFilter restrictedEdges, int[] nodeOrdering) {
        return contractGraph(g, chConfig, restrictedEdges, nodeOrdering);
    }

    public static RoutingCHGraph contractGraph(ORSGraphHopperStorage g, CHConfig chConfig, EdgeFilter restrictedEdges) {
        return contractGraph(g, chConfig, restrictedEdges, null);
    }

    public static RoutingCHGraph contractGraph(ORSGraphHopperStorage g, CHConfig chConfig, EdgeFilter restrictedEdges, int[] nodeOrdering) {
        RoutingCHGraph routingCHGraph = g.getCoreGraph(chConfig.getName());
        g.freeze();

        PrepareCore prepare = new PrepareCore(g, chConfig, restrictedEdges);

        if (nodeOrdering != null)
            prepare.useFixedNodeOrdering(NodeOrderingProvider.fromArray(nodeOrdering));

        // set contraction parameters to prevent test results from changing when algorithm parameters are tweaked
        //prepare.setParams(new PMap(CONTRACTED_NODES+"=100"));

        prepare.doWork();

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

        return routingCHGraph;
    }

    @Test
    void testSimpleUnrestrictedFixedContractionOrder() {
        createSimpleGraph();
        contractGraph(new CoreTestEdgeFilter(), new int[]{5, 3, 4, 0, 1, 2});

        HashSet<Shortcut> shortcuts = new HashSet<>();
        shortcuts.add(new Shortcut(4, 2, 3));
        assertShortcuts(shortcuts);

        assertCore(new HashSet<>());
    }

    // Original GH contraction heuristic does not produce any shortcuts
    @Test
    void testSimpleUnrestricted() {
        createSimpleGraph();
        contractGraph(new CoreTestEdgeFilter());

        assertShortcuts(new HashSet<>());
        assertCore(new HashSet<>());
    }

    // Original shortcut + one new
    @Test
    void testSimpleRestricted1() {
        createSimpleGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(2);

        contractGraph(restrictedEdges);

        HashSet<Shortcut> shortcuts = new HashSet<>();
        shortcuts.add(new Shortcut(0, 4, 4));
        shortcuts.add(new Shortcut(2, 4, 3));
        shortcuts.add(new Shortcut(4, 0, 4));
        assertShortcuts(shortcuts);

        Integer[] core = {0, 4};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Restricting different edge introduces different shortcuts
    @Test
    void testSimpleRestricted2() {
        createSimpleGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(4);
        contractGraph(restrictedEdges);

        HashSet<Shortcut> shortcuts = new HashSet<>();
        shortcuts.add(new Shortcut(0, 3, 5));
        shortcuts.add(new Shortcut(1, 3, 6));
        shortcuts.add(new Shortcut(2, 3, 6));
        shortcuts.add(new Shortcut(3, 2, 6));
        assertShortcuts(shortcuts);

        Integer[] core = {2, 3};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Now 2 shortcuts
    @Test
    void testSimpleRestricted3() {
        createSimpleGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(5);
        contractGraph(restrictedEdges);

        HashSet<Shortcut> shortcuts = new HashSet<>();
        shortcuts.add(new Shortcut(0, 3, 2));
        shortcuts.add(new Shortcut(3, 4, 5));
        shortcuts.add(new Shortcut(4, 3, 5));
        assertShortcuts(shortcuts);

        Integer[] core = {3, 4};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Core consisting of 3 nodes
    @Test
    void testSimpleRestricted4() {
        createSimpleGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(2);
        restrictedEdges.add(5);
        contractGraph(restrictedEdges);

        HashSet<Shortcut> shortcuts = new HashSet<>();
        shortcuts.add(new Shortcut(0, 3, 2));
        shortcuts.add(new Shortcut(3, 0, 2));
        assertShortcuts(shortcuts);

        Integer[] core = {0, 3, 4};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Core consisting of 4 nodes connected by 2 shortcuts
    @Test
    void testSimpleRestricted5() {
        createSimpleGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(5);
        restrictedEdges.add(6);
        contractGraph(restrictedEdges);

        HashSet<Shortcut> shortcuts = new HashSet<>();
        shortcuts.add(new Shortcut(1, 3, 3));
        shortcuts.add(new Shortcut(1, 4, 4));
        shortcuts.add(new Shortcut(2, 4, 4));
        shortcuts.add(new Shortcut(3, 1, 3));
        shortcuts.add(new Shortcut(3, 4, 5));
        shortcuts.add(new Shortcut(4, 1, 4));
        shortcuts.add(new Shortcut(4, 3, 5));
        assertShortcuts(shortcuts);

        Integer[] core = {1, 3, 4, 5};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    @Test
    void testMediumUnrestricted() {
        createMediumGraph();
        contractGraph(new CoreTestEdgeFilter());

        HashSet<Shortcut> shortcuts = new HashSet<>();
        shortcuts.add(new Shortcut(2, 4, 4));
        shortcuts.add(new Shortcut(4, 0, 5));
        shortcuts.add(new Shortcut(4, 7, 2));
        assertShortcuts(shortcuts);

        assertCore(new HashSet<>());
    }

    // With a single restriction on 0-1
    @Test
    void testMediumRestricted1() {
        createMediumGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        contractGraph(restrictedEdges);

        HashSet<Shortcut> shortcuts = new HashSet<>();
        shortcuts.add(new Shortcut(0, 1, 2));
        shortcuts.add(new Shortcut(1, 0, 2));
        shortcuts.add(new Shortcut(3, 0, 3));
        shortcuts.add(new Shortcut(3, 1, 3));
        shortcuts.add(new Shortcut(4, 0, 5));
        shortcuts.add(new Shortcut(4, 1, 5));
        shortcuts.add(new Shortcut(4, 8, 5));
        shortcuts.add(new Shortcut(7, 4, 2));
        assertShortcuts(shortcuts);

        Integer[] core = {0, 1};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Restrictions on edges: 0-1, 2-3
    @Test
    void testMediumRestricted2() {
        createMediumGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        restrictedEdges.add(6);

        contractGraph(restrictedEdges);

        HashSet<Shortcut> shortcuts = new HashSet<>();
        shortcuts.add(new Shortcut(4, 0, 6));
        shortcuts.add(new Shortcut(4, 1, 7));
        shortcuts.add(new Shortcut(7, 4, 2));
        shortcuts.add(new Shortcut(8, 4, 5));

        assertShortcuts(shortcuts);

        Integer[] core = {0, 1, 2, 3};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Restrictions on edges: 2-3, 7-8
    @Test
    void testMediumRestricted3() {
        createMediumGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(12);
        restrictedEdges.add(6);

        contractGraph(restrictedEdges);

        HashSet<Shortcut> shortcuts = new HashSet<>();
        shortcuts.add(new Shortcut(2, 3, 6));
        shortcuts.add(new Shortcut(2, 8, 2));
        shortcuts.add(new Shortcut(3, 2, 6));
        shortcuts.add(new Shortcut(3, 7, 4));
        shortcuts.add(new Shortcut(3, 8, 6));
        shortcuts.add(new Shortcut(4, 7, 2));
        shortcuts.add(new Shortcut(7, 3, 4));
        shortcuts.add(new Shortcut(8, 3, 6));
        shortcuts.add(new Shortcut(8, 2, 2));
        assertShortcuts(shortcuts);

        Integer[] core = {2, 3, 7, 8};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Restrictions on edges: 3-4, 7-8 -> Separated graph
    @Test
    void testMediumRestricted4() {
        createMediumGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(12);
        restrictedEdges.add(7);

        contractGraph(restrictedEdges);

        HashSet<Shortcut> shortcuts = new HashSet<>();
        shortcuts.add(new Shortcut(0, 3, 3));
        shortcuts.add(new Shortcut(3, 8, 4));
        shortcuts.add(new Shortcut(4, 7, 2));
        shortcuts.add(new Shortcut(7, 4, 2));
        shortcuts.add(new Shortcut(8, 3, 4));
        assertShortcuts(shortcuts);

        Integer[] core = {3, 4, 7, 8};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    @Test
    void testComplexUnrestricted() {
        createComplexGraph();
        contractGraph(new CoreTestEdgeFilter());

        HashSet<Shortcut> shortcuts = new HashSet<>();
        shortcuts.add(new Shortcut(8, 6, 2));
        shortcuts.add(new Shortcut(6, 4, 2));
        shortcuts.add(new Shortcut(6, 9, 3));
        shortcuts.add(new Shortcut(16, 9, 2));
        shortcuts.add(new Shortcut(16, 12, 2));
        shortcuts.add(new Shortcut(12, 9, 4));
        shortcuts.add(new Shortcut(12, 4, 2));
        shortcuts.add(new Shortcut(4, 2, 2));
        assertShortcuts(shortcuts);

        assertCore(new HashSet<>());
    }

    @Test
    void testComplexRestricted() {
        createComplexGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(10);
        restrictedEdges.add(17);

        contractGraph(restrictedEdges);

        HashSet<Shortcut> shortcuts = new HashSet<>();
        shortcuts.add(new Shortcut(2, 6, 4));
        shortcuts.add(new Shortcut(2, 12, 4));
        shortcuts.add(new Shortcut(4, 2, 2));
        shortcuts.add(new Shortcut(4, 6, 2));
        shortcuts.add(new Shortcut(4, 12, 2));
        shortcuts.add(new Shortcut(6, 7, 7));
        shortcuts.add(new Shortcut(6, 12, 4));
        shortcuts.add(new Shortcut(7, 6, 7));
        shortcuts.add(new Shortcut(7, 12, 6));
        shortcuts.add(new Shortcut(7, 15, 6));
        shortcuts.add(new Shortcut(9, 6, 5));
        shortcuts.add(new Shortcut(9, 7, 2));
        shortcuts.add(new Shortcut(9, 12, 4));
        shortcuts.add(new Shortcut(9, 15, 4));
        shortcuts.add(new Shortcut(12, 6, 4));
        shortcuts.add(new Shortcut(12, 7, 6));
        shortcuts.add(new Shortcut(12, 15, 4));
        shortcuts.add(new Shortcut(14, 12, 3));
        shortcuts.add(new Shortcut(14, 15, 3));
        shortcuts.add(new Shortcut(15, 12, 4));
        shortcuts.add(new Shortcut(15, 7, 6));
        shortcuts.add(new Shortcut(16, 12, 2));
        assertShortcuts(shortcuts);

        Integer[] core = {6, 7, 12, 15};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Test directed restriction
    @Test
    void testSimpleRestrictedReverse() {
        createSimpleGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(2, true);

        contractGraph(restrictedEdges);

        HashSet<Shortcut> shortcuts = new HashSet<>();
        shortcuts.add(new Shortcut(2, 4, 3));
        shortcuts.add(new Shortcut(4, 0, 4));
        assertShortcuts(shortcuts);

        Integer[] core = {0, 4};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Test whole graph is core
    @Test
    void testSimpleAllCore() {
        createSimpleGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        for (int i = 0; i < g.getEdges(); i++)
            restrictedEdges.add(i);
        contractGraph(restrictedEdges);

        assertShortcuts(new HashSet<>());

        Integer[] core = {0, 1, 2, 3, 4, 5};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    /**
     * Test whether only the core nodes have maximum level
     *
     * @param coreNodes
     */
    private void assertCore(Set<Integer> coreNodes) {
        int nodes = routingCHGraph.getNodes();
        int maxLevel = nodes;
        for (int node = 0; node < nodes; node++) {
            int level = routingCHGraph.getLevel(node);
            if (coreNodes.contains(node)) {
                assertEquals(maxLevel, level);
            } else {
                assertTrue(level < maxLevel);
            }
        }
    }

    /**
     * Test whether all the expected shortcuts are built and they are no additional shortcuts
     *
     * @param shortcutsExpected map with edge ids as key and as a value a pair of the nodes of the corresponding edge
     */
    private void assertShortcuts(Set<Shortcut> shortcutsExpected) {
        RoutingCHEdgeExplorer explorer = routingCHGraph.createOutEdgeExplorer();
        Set<Shortcut> shortcutsFound = new HashSet<>();

        for (int i = 0; i < routingCHGraph.getNodes(); i++) {
            RoutingCHEdgeIterator iter = explorer.setBaseNode(i);
            while (iter.next()) {
                if (iter.isShortcut()) {
                    Shortcut shortcut = new Shortcut(iter.getBaseNode(), iter.getAdjNode(), iter.getWeight(false));
                    shortcutsFound.add(shortcut);
                }
            }
        }

        assertEquals(shortcutsExpected.size(), shortcutsFound.size());
        assertTrue(shortcutsExpected.containsAll(shortcutsFound));
    }

    @Test
    void testHelperShortcut() {
        // node order does matter
        assertNotEquals(new Shortcut(1, 2, 3), new Shortcut(2, 1, 3));
        // shortcuts must have equal weight
        assertNotEquals(new Shortcut(1, 2, 3.0), new Shortcut(1, 2, 3.5));
    }

    private class Shortcut {
        int first;
        int second;
        double weight;

        Shortcut(int a, int b, double weight) {
            first = a;
            second = b;
            this.weight = weight;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Shortcut) {
                Shortcut s = (Shortcut) o;
                return this.first == s.first && this.second == s.second && this.weight == s.weight;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 61 * hash + first;
            hash = 61 * hash + second;
            hash = 61 * hash + (int) weight;
            return hash;
        }
    }
}
