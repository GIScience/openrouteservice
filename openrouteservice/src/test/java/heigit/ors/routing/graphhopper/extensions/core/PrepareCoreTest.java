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
package heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.routing.util.AllCHEdgesIterator;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import heigit.ors.common.Pair;
import heigit.ors.util.DebugUtility;
import java.util.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Hendrik Leuschner, Andrzej Oles, Djime Gueye
 */
public class PrepareCoreTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = new EncodingManager(carEncoder);
    private final Weighting weighting = new ShortestWeighting(carEncoder);
    private final TraversalMode tMode = TraversalMode.NODE_BASED;
    private Directory dir;

    @Before
    public void setUp() {
        dir = new GHDirectory("", DAType.RAM_INT);
    }

    GraphHopperStorage createGHStorage() {
        return new GraphBuilder(encodingManager).setCHGraph(weighting).create();
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
        g.edge(0, 4, 3, true); // restricted in #1 and #4
        g.edge(1, 2, 2, true);
        g.edge(2, 3, 1, true); // restricted in #2
        g.edge(4, 3, 2, true); // restricted in #3, #4 and #5
        g.edge(5, 1, 2, true); // restricted in #5
        return g;
    }

    private GraphHopperStorage createMediumGraph() {
        //    3---4--5
        //   /\   |  |
        //  2--0  6--7
        //  | / \   /
        //  |/   \ /
        //  1-----8
        GraphHopperStorage g = createGHStorage();
        g.edge(0, 1, 1, true); // restricted in #1 and #2
        g.edge(0, 2, 1, true);
        g.edge(0, 3, 5, true);
        g.edge(0, 8, 1, true);
        g.edge(1, 2, 1, true);
        g.edge(1, 8, 2, true);
        g.edge(2, 3, 2, true); // restricted in #2 and #3
        g.edge(3, 4, 2, true);
        g.edge(4, 5, 1, true);
        g.edge(4, 6, 1, true);
        g.edge(5, 7, 1, true);
        g.edge(6, 7, 2, true);
        g.edge(7, 8, 3, true); // restricted in #3
        return g;
    }

    private GraphHopperStorage createComplexGraph() {
        // prepare-routing.svg
        GraphHopperStorage g = createGHStorage();
        g.edge(0, 1, 1, true);
        g.edge(0, 2, 1, true);
        g.edge(1, 2, 1, true);
        g.edge(2, 3, 1.5, true);
        g.edge(1, 4, 1, true);
        g.edge(2, 9, 1, true);
        g.edge(9, 3, 1, true);
        g.edge(10, 3, 1, true);
        g.edge(4, 5, 1, true);
        g.edge(5, 6, 1, true);
        g.edge(6, 7, 1, true); //make this restricted; edge 10
        g.edge(7, 8, 1, true);
        g.edge(8, 9, 1, true);
        g.edge(4, 11, 1, true);
        g.edge(9, 14, 1, true);
        g.edge(10, 14, 1, true);
        g.edge(11, 12, 1, true);
        g.edge(12, 15, 1, true); //make this restricted; edge 17
        g.edge(12, 13, 1, true);
        g.edge(13, 16, 1, true);
        g.edge(15, 16, 2, true);
        g.edge(14, 16, 1, true);
        return g;
    }

    private CHGraph contractGraph(GraphHopperStorage g, CoreTestEdgeFilter restrictedEdges) {
        CHGraph lg = g.getGraph(CHGraph.class);
        PrepareCore prepare = new PrepareCore(dir, g, lg, weighting, tMode, restrictedEdges);
        prepare.doWork();

        if (DebugUtility.isDebug()) {
            for (int i = 0; i < lg.getNodes(); i++)
                System.out.println("nodeId " + i + " level: " + lg.getLevel(i));
            AllCHEdgesIterator iter = lg.getAllEdges();
            while (iter.next()) {
                System.out.print(iter.getBaseNode() + " -> " + iter.getAdjNode() + " via edge " + iter.getEdge());
                if (iter.isShortcut()) System.out.println(" (shortcut)");
                else System.out.println(" ");
            }
        }

        return lg;
    }

    @Test
    public void testSimpleUnrestricted() {
        CHGraph g = contractGraph(createSimpleGraph(), new CoreTestEdgeFilter());

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(7, new Pair<>(2, 4));
        assertShortcuts(g, shortcuts);

        assertCore(g, new HashSet<>());
    }

    // Original shortcut unaltered
    @Test
    public void testSimpleRestricted1() {
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(2);
        CHGraph g = contractGraph(createSimpleGraph(), restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(7, new Pair<>(2, 4));
        assertShortcuts(g, shortcuts);

        Integer core[] = {0, 4};
        assertCore(g, new HashSet<>(Arrays.asList(core)));
    }

    // No shortcuts at all
    @Test
    public void testSimpleRestricted2() {
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(4);
        CHGraph g = contractGraph(createSimpleGraph(), restrictedEdges);

        assertShortcuts(g, new HashMap<>());

        Integer core[] = {2, 3};
        assertCore(g, new HashSet<>(Arrays.asList(core)));
    }

    // One shortcut different from the unrestricted case
    @Test
    public void testSimpleRestricted3() {
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(5);
        CHGraph g = contractGraph(createSimpleGraph(), restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(7, new Pair<>(1, 4));
        assertShortcuts(g, shortcuts);

        Integer core[] = {3, 4};
        assertCore(g, new HashSet<>(Arrays.asList(core)));
    }

    // Core consisting of 3 nodes
    @Test
    public void testSimpleRestricted4() {
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(2);
        restrictedEdges.add(5);
        CHGraph g = contractGraph(createSimpleGraph(), restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(7, new Pair<>(0, 3));
        assertShortcuts(g, shortcuts);

        Integer core[] = {0, 3, 4};
        assertCore(g, new HashSet<>(Arrays.asList(core)));
    }

    // Core consisting of 4 nodes connected by 2 shortcuts
    @Test
    public void testSimpleRestricted5() {
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(5);
        restrictedEdges.add(6);
        CHGraph g = contractGraph(createSimpleGraph(), restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(7, new Pair<>(1, 4));
        shortcuts.put(8, new Pair<>(1, 3));
        assertShortcuts(g, shortcuts);

        Integer core[] = {1, 3, 4, 5};
        assertCore(g, new HashSet<>(Arrays.asList(core)));
    }

    @Test
    public void testMediumUnrestricted(){
        CHGraph g = contractGraph(createMediumGraph(), new CoreTestEdgeFilter());

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(13, new Pair<>(4,7));
        shortcuts.put(14, new Pair<>(0,3));
        shortcuts.put(15, new Pair<>(0,4));
        assertShortcuts(g, shortcuts);

        assertCore(g, new HashSet<>());
    }

    // With a single restriction on 0-1
    @Test
    public void testMediumRestricted1(){
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        CHGraph g = contractGraph(createMediumGraph(), restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(13, new Pair<>(4,7));
        shortcuts.put(14, new Pair<>(1,3));
        shortcuts.put(15, new Pair<>(0,3));
        shortcuts.put(16, new Pair<>(1,4));
        shortcuts.put(17, new Pair<>(0,4));
        assertShortcuts(g, shortcuts);

        Integer core[] = {0, 1};
        assertCore(g, new HashSet<>(Arrays.asList(core)));
    }

    // Restrictions on edges: 0-1, 2-3
    @Test
    public void testMediumRestricted2() {
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        restrictedEdges.add(6);

        CHGraph g = contractGraph(createMediumGraph(), restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(13, new Pair<>(4,7));
        shortcuts.put(14, new Pair<>(3,7));
        assertShortcuts(g, shortcuts);

        Integer core[] = {0, 1, 2, 3};
        assertCore(g, new HashSet<>(Arrays.asList(core)));
    }

    // Restrictions on edges: 2-3, 7-8
    @Test
    public void testMediumRestricted3() {
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(12);
        restrictedEdges.add(6);

        CHGraph g = contractGraph(createMediumGraph(), restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(13, new Pair<>(4,7));
        shortcuts.put(14, new Pair<>(3,7));
        shortcuts.put(15, new Pair<>(3,8));
        shortcuts.put(16, new Pair<>(2,8));
        assertShortcuts(g, shortcuts);

        Integer core[] = {2, 3, 7, 8};
        assertCore(g, new HashSet<>(Arrays.asList(core)));
    }

    @Test
    public void testComplexUnrestricted() {
        CHGraph g = contractGraph(createComplexGraph(), new CoreTestEdgeFilter());

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(22, new Pair<>(4, 6));
        shortcuts.put(23, new Pair<>(4, 7));
        shortcuts.put(24, new Pair<>(4, 12));
        shortcuts.put(25, new Pair<>(12, 16));
        shortcuts.put(26, new Pair<>(2, 4));
        shortcuts.put(27, new Pair<>(2, 14));
        shortcuts.put(28, new Pair<>(2, 16));
        assertShortcuts(g, shortcuts);

        assertCore(g, new HashSet<>());
    }

    @Test
    public void testComplexRestricted() {
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(10);
        restrictedEdges.add(17);

        CHGraph g = contractGraph(createComplexGraph(), restrictedEdges);

        HashMap<Integer, Pair> shortcuts = new HashMap<>();
        shortcuts.put(22, new Pair<>(4, 6));
        shortcuts.put(23, new Pair<>(7, 9));
        shortcuts.put(24, new Pair<>(4, 12));
        shortcuts.put(25, new Pair<>(12, 16));
        shortcuts.put(26, new Pair<>(2, 4));
        shortcuts.put(27, new Pair<>(6, 12));
        shortcuts.put(28, new Pair<>(2, 12));
        shortcuts.put(29, new Pair<>(9, 16));
        shortcuts.put(30, new Pair<>(9, 12));
        shortcuts.put(31, new Pair<>(9, 15));
        assertShortcuts(g, shortcuts);

        Integer core[] = {6, 7, 12, 15};
        assertCore(g, new HashSet<>(Arrays.asList(core)));
    }

    /**
     * Test whether only the core nodes have maximum level
     * @param g the contraction hierarchy Graph
     * @param coreNodes
     */
    private void assertCore(CHGraph g, Set<Integer> coreNodes) {
        int nodes = g.getNodes();
        int maxLevel = nodes + 1;
        for (int node = 0; node < nodes; node++) {
            int level = g.getLevel(node);
            if (coreNodes.contains(node)) {
                assertEquals(maxLevel, level);
            } else {
                assertTrue(level < maxLevel);
            }
        }
    }



    /**
     * Test whether all the expected shortcuts are built and they are no addtional shortcuts
     * @param g contraction hierarchy Graph
     * @param shortcuts map with edge ids as key and as a value a pair of the nodes of the corresponding edge
     */
    private void assertShortcuts(CHGraph g, HashMap<Integer, Pair> shortcuts) {
        AllCHEdgesIterator iter = g.getAllEdges();
        HashSet<Integer> shortcutsFound = new HashSet<>();
        while (iter.next()) {
            if (iter.isShortcut()) {
                int edge = iter.getEdge();
                assertTrue(shortcuts.containsKey(edge));
                assertEquals(iter.getBaseNode(), shortcuts.get(edge).first);
                assertEquals(iter.getAdjNode(), shortcuts.get(edge).second);
                shortcutsFound.add(edge);
            }
        }
        // Verify that all the expected shortcuts were found
        Iterator<Integer> shortcutIds = shortcuts.keySet().iterator();
        while (shortcutIds.hasNext()) {
            assertTrue(shortcutsFound.contains(shortcutIds.next()));
        }
    }

}
