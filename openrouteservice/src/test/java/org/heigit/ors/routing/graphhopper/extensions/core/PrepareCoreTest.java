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
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.GHUtility;
import org.heigit.ors.common.Pair;
import org.heigit.ors.util.DebugUtility;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Hendrik Leuschner, Andrzej Oles, Djime Gueye
 */
public class PrepareCoreTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder(5, 5, 3);
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private final Weighting weighting = new ShortestWeighting(carEncoder);
    private final CHConfig chConfig = new CHConfig("c", weighting, false, CHConfig.TYPE_CORE);
    private GraphHopperStorage g;
    private RoutingCHGraph routingCHGraph;

    @Before
    public void setUp() {
        g = new GraphBuilder(encodingManager).setCHConfigs(chConfig).create();
        routingCHGraph = g.getRoutingCHGraph();
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

    private void contractGraph(CoreTestEdgeFilter restrictedEdges) {
        contractGraph(restrictedEdges, null);
    }

    private void contractGraph(CoreTestEdgeFilter restrictedEdges, int[] nodeOrdering) {
        g.freeze();

        PrepareCore prepare = new PrepareCore(g, chConfig, restrictedEdges);

        if (nodeOrdering!=null)
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
    }

    @Test
    public void testSimpleUnrestrictedFixedContractionOrder() {
        createSimpleGraph();
        contractGraph(new CoreTestEdgeFilter(), new int[]{5, 3, 4, 0, 1, 2});

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(7, new Pair<>(4, 2));
        assertShortcuts(shortcuts);

        assertCore(new HashSet<>());
    }

    // Original GH contraction heuristic does not produce any shortcuts
    @Test
    public void testSimpleUnrestricted() {
        createSimpleGraph();
        contractGraph(new CoreTestEdgeFilter());

        assertShortcuts(new HashMap<>());
        assertCore(new HashSet<>());
    }

    // Original shortcut + one new
    @Test
    public void testSimpleRestricted1() {
        createSimpleGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(2);

        contractGraph(restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(7, new Pair<>(2, 4));
        shortcuts.put(8, new Pair<>(0, 4));

        assertShortcuts(shortcuts);

        Integer[] core = {0, 4};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Restricting different edge introduces different shortcuts
    @Test
    public void testSimpleRestricted2() {
        createSimpleGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(4);
        contractGraph(restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(7, new Pair<>(0, 3));
        shortcuts.put(8, new Pair<>(2, 3));
        assertShortcuts(shortcuts);

        Integer[] core = {2, 3};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Now 2 shortcuts
    @Test
    public void testSimpleRestricted3() {
        createSimpleGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(5);
        contractGraph(restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(7, new Pair<>(1, 3));
        shortcuts.put(8, new Pair<>(0, 3));
        shortcuts.put(9, new Pair<>(3, 4));
        assertShortcuts(shortcuts);

        Integer[] core = {3, 4};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Core consisting of 3 nodes
    @Test
    public void testSimpleRestricted4() {
        createSimpleGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(2);
        restrictedEdges.add(5);
        contractGraph(restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(7, new Pair<>(1, 3));
        shortcuts.put(8, new Pair<>(0, 3));
        assertShortcuts(shortcuts);

        Integer[] core = {0, 3, 4};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Core consisting of 4 nodes connected by 2 shortcuts
    @Test
    public void testSimpleRestricted5() {
        createSimpleGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(5);
        restrictedEdges.add(6);
        contractGraph(restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(7, new Pair<>(2, 4));
        shortcuts.put(8, new Pair<>(3, 4));
        shortcuts.put(9, new Pair<>(1, 3));
        shortcuts.put(10, new Pair<>(1, 4));
        assertShortcuts(shortcuts);

        Integer[] core = {1, 3, 4, 5};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    @Test
    public void testMediumUnrestricted(){
        createMediumGraph();
        contractGraph(new CoreTestEdgeFilter());

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(13, new Pair<>(2,4));
        shortcuts.put(14, new Pair<>(4,0));
        shortcuts.put(15, new Pair<>(4,7));
        assertShortcuts(shortcuts);

        assertCore(new HashSet<>());
    }

    // With a single restriction on 0-1
    @Test
    public void testMediumRestricted1(){
        createMediumGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        contractGraph(restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(13, new Pair<>(2,4));
        shortcuts.put(14, new Pair<>(7,4));
        shortcuts.put(15, new Pair<>(4,8));
        shortcuts.put(16, new Pair<>(4,0));
        shortcuts.put(17, new Pair<>(4,1));
        shortcuts.put(18, new Pair<>(0,1));
        assertShortcuts(shortcuts);

        Integer[] core = {0, 1};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Restrictions on edges: 0-1, 2-3
    @Test
    public void testMediumRestricted2() {
        createMediumGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        restrictedEdges.add(6);

        contractGraph(restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(13, new Pair<>(7,4));
        shortcuts.put(14, new Pair<>(8,4));
        shortcuts.put(15, new Pair<>(4,0));
        shortcuts.put(16, new Pair<>(4,1));
        assertShortcuts(shortcuts);

        Integer[] core = {0, 1, 2, 3};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Restrictions on edges: 2-3, 7-8
    @Test
    public void testMediumRestricted3() {
        createMediumGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(12);
        restrictedEdges.add(6);

        contractGraph(restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(13, new Pair<>(4,7));
        shortcuts.put(14, new Pair<>(3,8));
        shortcuts.put(15, new Pair<>(3,7));
        shortcuts.put(16, new Pair<>(2,3));
        shortcuts.put(17, new Pair<>(2,8));
        assertShortcuts(shortcuts);

        Integer[] core = {2, 3, 7, 8};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    // Restrictions on edges: 3-4, 7-8 -> Separated graph
    @Test
    public void testMediumRestricted4() {
        createMediumGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(12);
        restrictedEdges.add(7);

        contractGraph(restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(13, new Pair<>(1,3));
        shortcuts.put(14, new Pair<>(0,3));
        shortcuts.put(15, new Pair<>(4,7));
        shortcuts.put(16, new Pair<>(3,8));
        assertShortcuts(shortcuts);

        Integer[] core = {3, 4, 7, 8};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    @Test
    public void testComplexUnrestricted() {
        createComplexGraph();
        contractGraph(new CoreTestEdgeFilter());

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(22, new Pair<>(6, 4));
        shortcuts.put(23, new Pair<>(4, 7));
        shortcuts.put(24, new Pair<>(12, 4));
        shortcuts.put(25, new Pair<>(16, 12));
        shortcuts.put(26, new Pair<>(4, 2));
        shortcuts.put(27, new Pair<>(14, 2));
        shortcuts.put(28, new Pair<>(2, 16));
        assertShortcuts(shortcuts);

        assertCore(new HashSet<>());
    }

    @Test
    public void testComplexRestricted() {
        createComplexGraph();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(10);
        restrictedEdges.add(17);

        contractGraph(restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(22, new Pair<>(6, 4));
        shortcuts.put(23, new Pair<>(9, 7));
        shortcuts.put(24, new Pair<>(12, 4));
        shortcuts.put(25, new Pair<>(16, 12));
        shortcuts.put(26, new Pair<>(4, 2));
        shortcuts.put(27, new Pair<>(12, 6));
        shortcuts.put(28, new Pair<>(2, 12));
        shortcuts.put(29, new Pair<>(2, 6));
        shortcuts.put(30, new Pair<>(16, 9));
        shortcuts.put(31, new Pair<>(6, 9));
        shortcuts.put(32, new Pair<>(16, 7));
        shortcuts.put(33, new Pair<>(6, 7));
        shortcuts.put(34, new Pair<>(7, 12));
        shortcuts.put(35, new Pair<>(12, 15));
        shortcuts.put(36, new Pair<>(7, 15));
        assertShortcuts(shortcuts);

        Integer[] core = {6, 7, 12, 15};
        assertCore(new HashSet<>(Arrays.asList(core)));
    }

    /**
     * Test whether only the core nodes have maximum level
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
     * @param shortcuts map with edge ids as key and as a value a pair of the nodes of the corresponding edge
     */
    private void assertShortcuts(HashMap<Integer, Pair> shortcuts) {
        RoutingCHEdgeExplorer explorer = routingCHGraph.createOutEdgeExplorer();
        HashSet<Integer> shortcutsFound = new HashSet<>();

        for (int i = 0; i < routingCHGraph.getNodes(); i++) {
            RoutingCHEdgeIterator iter = explorer.setBaseNode(i);
            while (iter.next()) {
                if (iter.isShortcut()) {
                    int edge = iter.getEdge();
                    assertTrue(shortcuts.containsKey(edge));
                    assertEquals(shortcuts.get(edge).second, iter.getAdjNode());
                    assertEquals(shortcuts.get(edge).first, iter.getBaseNode());
                    shortcutsFound.add(edge);
                }
            }
        }
        // Verify that all the expected shortcuts were found
        Iterator<Integer> shortcutIds = shortcuts.keySet().iterator();
        while (shortcutIds.hasNext()) {
            assertTrue(shortcutsFound.contains(shortcutIds.next()));
        }
    }

}
