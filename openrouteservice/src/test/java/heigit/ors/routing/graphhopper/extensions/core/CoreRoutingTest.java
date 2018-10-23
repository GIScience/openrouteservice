///*
// *  Licensed to GraphHopper GmbH under one or more contributor
// *  license agreements. See the NOTICE file distributed with this work for
// *  additional information regarding copyright ownership.
// *
// *  GraphHopper GmbH licenses this file to you under the Apache License,
// *  Version 2.0 (the "License"); you may not use this file except in
// *  compliance with the License. You may obtain a copy of the License at
// *
// *       http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//package heigit.ors.routing.graphhopper.extensions.core;
//
//import com.graphhopper.routing.util.AllCHEdgesIterator;
//import com.graphhopper.routing.util.CarFlagEncoder;
//import com.graphhopper.routing.util.EncodingManager;
//import com.graphhopper.routing.util.TraversalMode;
//import com.graphhopper.routing.weighting.ShortestWeighting;
//import com.graphhopper.routing.weighting.Weighting;
//import com.graphhopper.storage.*;
//import heigit.ors.common.Pair;
//import heigit.ors.util.DebugUtility;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.*;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
///**
// * @author Hendrik Leuschner, Andrzej Oles, Djime Gueye
// */
//public class CoreRoutingTest {
//    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
//    private final EncodingManager encodingManager = new EncodingManager(carEncoder);
//    private final Weighting weighting = new ShortestWeighting(carEncoder);
//    private final TraversalMode tMode = TraversalMode.NODE_BASED;
//    private Directory dir;
//
//    @Before
//    public void setUp() {
//        dir = new GHDirectory("", DAType.RAM_INT);
//    }
//
//    GraphHopperStorage createGHStorage() {
//        return new GraphBuilder(encodingManager).setCoreGraph(weighting).create();
//    }
//
//    private GraphHopperStorage createSimpleGraph() {
//        // 5--1---2
//        //     \ /|
//        //      0 |
//        //     /  |
//        //    4---3
//        GraphHopperStorage g = createGHStorage();
//        g.edge(0, 1, 1, true);
//        g.edge(0, 2, 1, true);
//        g.edge(0, 4, 3, true); // restricted in #1 and #4
//        g.edge(1, 2, 2, true);
//        g.edge(2, 3, 1, true); // restricted in #2
//        g.edge(4, 3, 2, true); // restricted in #3, #4 and #5
//        g.edge(5, 1, 2, true); // restricted in #5
//        return g;
//    }
//
//    private GraphHopperStorage createMediumGraph() {
//        //    3---4--5
//        //   /\   |  |
//        //  2--0  6--7
//        //  | / \   /
//        //  |/   \ /
//        //  1-----8
//        GraphHopperStorage g = createGHStorage();
//        g.edge(0, 1, 1, true); // restricted in #1 and #2
//        g.edge(0, 2, 1, true);
//        g.edge(0, 3, 5, true);
//        g.edge(0, 8, 1, true);
//        g.edge(1, 2, 1, true);
//        g.edge(1, 8, 2, true);
//        g.edge(2, 3, 2, true); // restricted in #2 and #3
//        g.edge(3, 4, 2, true); // restricted in #4
//        g.edge(4, 5, 1, true);
//        g.edge(4, 6, 1, true);
//        g.edge(5, 7, 1, true);
//        g.edge(6, 7, 2, true);
//        g.edge(7, 8, 3, true); // restricted in #3 and #4
//        return g;
//    }
//
//    private GraphHopperStorage createComplexGraph() {
//        // prepare-routing.svg
//        GraphHopperStorage g = createGHStorage();
//        g.edge(0, 1, 1, true);
//        g.edge(0, 2, 1, true);
//        g.edge(1, 2, 1, true);
//        g.edge(2, 3, 1.5, true);
//        g.edge(1, 4, 1, true);
//        g.edge(2, 9, 1, true);
//        g.edge(9, 3, 1, true);
//        g.edge(10, 3, 1, true);
//        g.edge(4, 5, 1, true);
//        g.edge(5, 6, 1, true);
//        g.edge(6, 7, 1, true); //make this restricted; edge 10
//        g.edge(7, 8, 1, true);
//        g.edge(8, 9, 1, true);
//        g.edge(4, 11, 1, true);
//        g.edge(9, 14, 1, true);
//        g.edge(10, 14, 1, true);
//        g.edge(11, 12, 1, true);
//        g.edge(12, 15, 1, true); //make this restricted; edge 17
//        g.edge(12, 13, 1, true);
//        g.edge(13, 16, 1, true);
//        g.edge(15, 16, 2, true);
//        g.edge(14, 16, 1, true);
//        return g;
//    }
//
//    private CHGraph contractGraph(GraphHopperStorage g, CoreTestEdgeFilter restrictedEdges) {
//        CHGraph lg = g.getGraph(CHGraph.class);
//        PrepareCore prepare = new PrepareCore(dir, g, lg, weighting, tMode, restrictedEdges);
//        prepare.doWork();
//        int lm = 5, activeLM = 2;
//
////        CoreLandmarkStorage store = new CoreLandmarkStorage(g, dir, weighting, lm);
////        store.createCoreNodeIdMap();
////        store.setMinimumNodes(2);
////        store.createLandmarks();
//
//        if (DebugUtility.isDebug()) {
//            for (int i = 0; i < lg.getNodes(); i++)
//                System.out.println("nodeId " + i + " level: " + lg.getLevel(i));
//            AllCHEdgesIterator iter = lg.getAllEdges();
//            while (iter.next()) {
//                System.out.print(iter.getBaseNode() + " -> " + iter.getAdjNode() + " via edge " + iter.getEdge());
//                if (iter.isShortcut()) System.out.println(" (shortcut)");
//                else System.out.println(" ");
//            }
//        }
//
//        return lg;
//    }
//
//    @Test
//    public void testSimpleUnrestricted() {
//        CHGraph g = contractGraph(createSimpleGraph(), new CoreTestEdgeFilter());
//
//        HashMap<Integer, Pair> shortcuts = new HashMap<>();
//        shortcuts.put(7, new Pair<>(2, 4));
//        assertShortcuts(g, shortcuts);
//    }
//
//    // Original shortcut + one new
//    @Test
////    public void testSimpleRestricted1() {
////        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
////        restrictedEdges.add(2);
////        GraphHopperStorage g = createMediumGraph();
////        CHGraph lg = g.getGraph(CHGraph.class);
////        PrepareCore prepare = new PrepareCore(dir, g, lg, weighting, tMode, restrictedEdges);
////        prepare.doWork();
////        int lm = 2, activeLM = 2;
////
//////        CoreLandmarkStorage store = new CoreLandmarkStorage(g, dir, weighting, lm);
//////        store.createCoreNodeIdMap();
//////        store.setMinimumNodes(2);
//////        store.createLandmarks();
////
//////        coreALT calt = prepare.createAlgo()
//////
////        HashMap<Integer, Pair> shortcuts = new HashMap<>();
////        shortcuts.put(7, new Pair<>(2, 4)); // original shortcut
////        shortcuts.put(8, new Pair<>(0, 4)); // the new one replacing the restricted edge
////        assertShortcuts(lg, shortcuts);
////
////        Integer core[] = {0, 4};
////    }
//
//
//    private void assertShortcuts(CHGraph g, HashMap<Integer, Pair> shortcuts) {
//        AllCHEdgesIterator iter = g.getAllEdges();
//        HashSet<Integer> shortcutsFound = new HashSet<>();
//        while (iter.next()) {
//            if (iter.isShortcut()) {
//                int edge = iter.getEdge();
//                assertTrue(shortcuts.containsKey(edge));
//                assertEquals(shortcuts.get(edge).first, iter.getBaseNode());
//                assertEquals(shortcuts.get(edge).second, iter.getAdjNode());
//                shortcutsFound.add(edge);
//            }
//        }
//        // Verify that all the expected shortcuts were found
//        Iterator<Integer> shortcutIds = shortcuts.keySet().iterator();
//        while (shortcutIds.hasNext()) {
//            assertTrue(shortcutsFound.contains(shortcutIds.next()));
//        }
//    }
//
//}
