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
package heigit.ors.routing.graphhopper.extensions.corelm;

//import com.graphhopper.routing.AbstractRoutingAlgorithmTester;
import com.graphhopper.routing.lm.LandmarkStorage;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.util.spatialrules.DefaultSpatialRule;
import com.graphhopper.routing.util.spatialrules.SpatialRule;
import com.graphhopper.routing.util.spatialrules.SpatialRuleLookup;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import heigit.ors.common.Pair;
import heigit.ors.routing.graphhopper.extensions.core.CoreLMAlgoFactoryDecorator;
import heigit.ors.routing.graphhopper.extensions.core.CoreLandmarkStorage;
import heigit.ors.routing.graphhopper.extensions.core.CoreTestEdgeFilter;
import heigit.ors.routing.graphhopper.extensions.core.PrepareCore;
import heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;
import heigit.ors.util.DebugUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * @author Peter Karich
 */
public class CoreLandmarkStorageTest {
    private GraphHopperStorage ghStorage;
    private FlagEncoder encoder;
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = new EncodingManager(carEncoder);
    private final Weighting weighting = new ShortestWeighting(carEncoder);
    private final TraversalMode tMode = TraversalMode.NODE_BASED;
    private Directory dir;

    @Before
    public void setUp() {
        encoder = new CarFlagEncoder();
        ghStorage = new GraphHopperStorage(new RAMDirectory(),
                new EncodingManager(encoder), false, new GraphExtension.NoOpExtension());
        ghStorage.create(1000);
        dir = new GHDirectory("", DAType.RAM_INT);
    }

    @After
    public void tearDown() {
        if (ghStorage != null)
            ghStorage.close();
    }

    GraphHopperStorage createGHStorage() {
        return new GraphBuilder(encodingManager).setCoreGraph(weighting).create();
    }


    public GraphHopperStorage createMediumGraph() {
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
        g.edge(3, 4, 2, true); // restricted in #4
        g.edge(4, 5, 1, true);
        g.edge(4, 6, 1, true);
        g.edge(5, 7, 1, true);
        g.edge(6, 7, 2, true);
        g.edge(7, 8, 3, true); // restricted in #3 and #4
        return g;
    }

    public HashMap<Integer, Integer> createCoreNodeIdMap(CHGraph core, Weighting weighting) {
//        CHGraphImpl core = graph.getCoreGraph(weighting);
        HashMap<Integer, Integer> coreNodeIdMap = new HashMap<>();
        int maxNode = core.getNodes();
        int coreNodeLevel = maxNode + 1;
        int index = 0;
        for (int i = 0; i < maxNode; i++){
            if (core.getLevel(i) < coreNodeLevel)
                continue;
            coreNodeIdMap.put(i, index);
            index++;
        }
        return coreNodeIdMap;
    }

    //    @Test
//    public void testInfinitWeight() {
//        Directory dir = new RAMDirectory();
//        EdgeIteratorState edge = ghStorage.edge(0, 1);
//        int res = new CoreLandmarkStorage(dir, ghStorage, new FastestWeighting(encoder) {
//            @Override
//            public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
//                return Integer.MAX_VALUE * 2L;
//            }
//        }, 8).setMaximumWeight(CoreLandmarkStorage.PRECISION).calcWeight(edge, false);
//        assertEquals(Integer.MAX_VALUE, res);
//
//        dir = new RAMDirectory();
//        res = new CoreLandmarkStorage(ghStorage, dir, new FastestWeighting(encoder) {
//            @Override
//            public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
//                return Double.POSITIVE_INFINITY;
//            }
//        }, 8).setMaximumWeight(CoreLandmarkStorage.PRECISION).calcWeight(edge, false);
//        assertEquals(Integer.MAX_VALUE, res);
//    }
//
//    @Test
//    public void testSetGetWeight() {
//        ghStorage.edge(0, 1, 40, true);
//        Directory dir = new RAMDirectory();
//        DataAccess da = dir.find("landmarks_fastest_car");
//        da.create(2000);
//
//        CoreLandmarkStorage lms = new CoreLandmarkStorage(ghStorage, dir, new FastestWeighting(encoder), 4).
//                setMaximumWeight(CoreLandmarkStorage.PRECISION);
//        // 2^16=65536, use -1 for infinity and -2 for maximum
//        lms.setWeight(0, 65536);
//        // reached maximum value but do not reset to 0 instead use 2^16-2
//        assertEquals(65536 - 2, lms.getFromWeight(0, 0));
//        lms.setWeight(0, 65535);
//        assertEquals(65534, lms.getFromWeight(0, 0));
//        lms.setWeight(0, 79999);
//        assertEquals(65534, lms.getFromWeight(0, 0));
//
//        da.setInt(0, Integer.MAX_VALUE);
//        assertTrue(lms.isInfinity(0));
//        // for infinity return much bigger value
//        // assertEquals(Integer.MAX_VALUE, lms.getFromWeight(0, 0));
//
//        lms.setWeight(0, 79999);
//        assertFalse(lms.isInfinity(0));
//    }
    public CHGraph contractGraph(GraphHopperStorage g, CoreTestEdgeFilter restrictedEdges) {
        CHGraph lg = g.getGraph(CHGraph.class);
        PrepareCore prepare = new PrepareCore(dir, g, lg, weighting, tMode, restrictedEdges);

        // set contraction parameters to prevent test results from changing when algorithm parameters are tweaked
        prepare.setPeriodicUpdates(20);
        prepare.setLazyUpdates(10);
        prepare.setNeighborUpdates(20);
        prepare.setContractedNodes(100);

        prepare.doWork();

        if (DebugUtility.isDebug()) {
            for (int i = 0; i < lg.getNodes(); i++)
                System.out.println("nodeId " + i + " level: " + lg.getLevel(i));
            AllCHEdgesIterator iter = lg.getAllEdges();
            while (iter.next()) {
                System.out.print(iter.getBaseNode() + " -> " + iter.getAdjNode() + " via edge " + iter.getEdge());
                if (iter.isShortcut())
                    System.out.print(" (shortcut)");
                System.out.println(" [weight: " + iter.getDistance()+ "]");
            }
        }

        return lg;
    }
    @Test
    public void testOneSubnetwork() {
        // All edges in medium graph are part of core. Test if landmarks are built
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
        ghStorage = createMediumGraph();
        CHGraph g = contractGraph(ghStorage, restrictedEdges);
        HashMap<Integer, Integer> coreNodeIdMap = createCoreNodeIdMap(g, weighting);




//        ghStorage.edge(0, 1, 10, true);
//        ghStorage.edge(1, 2, 10, true);
//
//        ghStorage.edge(2, 4).setFlags(encoder.setAccess(0, false, false));
//        ghStorage.edge(4, 5, 10, true);
//        ghStorage.edge(5, 6, 10, false);
        CoreLandmarkStorage storage = new CoreLandmarkStorage(dir, ghStorage, coreNodeIdMap, weighting,new LMEdgeFilterSequence(), 2 );
//        CoreLandmarkStorage storage = new CoreLandmarkStorage(ghStorage, new RAMDirectory(), new FastestWeighting(encoder), 2);
        storage.setMinimumNodes(2);
        storage.createLandmarks();
        assertEquals(2, storage.getSubnetworksWithLandmarks());
        assertEquals("[6, 2]", Arrays.toString(storage.getLandmarks(1)));
    }

    @Test
    public void testTwoSubnetworks() {
        // All edges in medium graph are part of core. Test if landmarks are built
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        CoreTestEdgeFilter passableEdges = new CoreTestEdgeFilter();
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

        passableEdges.add(7);
        passableEdges.add(12);
//        passableEdges.add(2);
//        passableEdges.add(3);
//        passableEdges.add(4);
//        passableEdges.add(5);
//        passableEdges.add(6);
//        passableEdges.add(8);
//        passableEdges.add(9);
//        passableEdges.add(10);
//        passableEdges.add(11);

        ghStorage = createMediumGraph();
        CHGraph g = contractGraph(ghStorage, restrictedEdges);
        HashMap<Integer, Integer> coreNodeIdMap = createCoreNodeIdMap(g, weighting);




//        ghStorage.edge(0, 1, 10, true);
//        ghStorage.edge(1, 2, 10, true);
//
//        ghStorage.edge(2, 4).setFlags(encoder.setAccess(0, false, false));
//        ghStorage.edge(4, 5, 10, true);
//        ghStorage.edge(5, 6, 10, false);
        LMEdgeFilterSequence lmEdgeFilterSequence = new LMEdgeFilterSequence();
        lmEdgeFilterSequence.add(passableEdges);
        CoreLandmarkStorage storage = new CoreLandmarkStorage(dir, ghStorage, coreNodeIdMap, weighting, lmEdgeFilterSequence, 2 );
//        CoreLandmarkStorage storage = new CoreLandmarkStorage(ghStorage, new RAMDirectory(), new FastestWeighting(encoder), 2);
        storage.setMinimumNodes(2);
        storage.createLandmarks();
        assertEquals(3, storage.getSubnetworksWithLandmarks());
        assertEquals("[3, 8]", Arrays.toString(storage.getLandmarks(1)));
        assertEquals("[7, 4]", Arrays.toString(storage.getLandmarks(2)));
    }

//    @Test
//    public void testWithSubnetworks2() {
//        // should not happen with subnetwork preparation
//        // 0 - 1 - 2 = 3 - 4
//        ghStorage.edge(0, 1, 10, true);
//        ghStorage.edge(1, 2, 10, true);
//        ghStorage.edge(2, 3, 10, false);
//        ghStorage.edge(3, 2, 10, false);
//        ghStorage.edge(3, 4, 10, true);
//
//        CoreLandmarkStorage storage = new CoreLandmarkStorage(ghStorage, new RAMDirectory(), new FastestWeighting(encoder), 2);
//        storage.setMinimumNodes(3);
//        storage.createLandmarks();
//        assertEquals(2, storage.getSubnetworksWithLandmarks());
//        assertEquals("[4, 0]", Arrays.toString(storage.getLandmarks(1)));
//    }
//
//    @Test
//    public void testWithOnewaySubnetworks() {
//        // should not happen with subnetwork preparation
//        // create an indifferent problem: node 2 and 3 are part of two 'disconnected' subnetworks
//        ghStorage.edge(0, 1, 10, true);
//        ghStorage.edge(1, 2, 10, false);
//        ghStorage.edge(2, 3, 10, false);
//
//        ghStorage.edge(4, 5, 10, true);
//        ghStorage.edge(5, 2, 10, false);
//
//        CoreLandmarkStorage storage = new CoreLandmarkStorage(ghStorage, new RAMDirectory(), new FastestWeighting(encoder), 2);
//        storage.setMinimumNodes(2);
//        storage.createLandmarks();
//
//        assertEquals(2, storage.getSubnetworksWithLandmarks());
//        assertEquals("[4, 0]", Arrays.toString(storage.getLandmarks(1)));
//    }
//
//    @Test
//    public void testWeightingConsistence() {
//        // create an indifferent problem: shortest weighting can pass the speed==0 edge but fastest cannot (?)
//        ghStorage.edge(0, 1, 10, true);
//        ghStorage.edge(1, 2).setDistance(10).setFlags(encoder.setProperties(0.9, true, true));
//        ghStorage.edge(2, 3, 10, true);
//
//        CoreLandmarkStorage storage = new CoreLandmarkStorage(ghStorage, new RAMDirectory(), new FastestWeighting(encoder), 2);
//        storage.setMinimumNodes(2);
//        storage.createLandmarks();
//
//        assertEquals(2, storage.getSubnetworksWithLandmarks());
//        assertEquals("[1, 0]", Arrays.toString(storage.getLandmarks(1)));
//    }
//
//    @Test
//    public void testWithBorderBlocking() {
//        AbstractRoutingAlgorithmTester.initBiGraph(ghStorage);
//
//        CoreLandmarkStorage storage = new CoreLandmarkStorage(ghStorage, new RAMDirectory(), new FastestWeighting(encoder), 2);
//        final SpatialRule ruleRight = new DefaultSpatialRule() {
//            @Override
//            public String getId() {
//                return "right";
//            }
//        };
//        final SpatialRule ruleLeft = new DefaultSpatialRule() {
//            @Override
//            public String getId() {
//                return "left";
//            }
//        };
//        final SpatialRuleLookup lookup = new SpatialRuleLookup() {
//
//            @Override
//            public SpatialRule lookupRule(double lat, double lon) {
//                if (lon > 0.00105)
//                    return ruleRight;
//
//                return ruleLeft;
//            }
//
//            @Override
//            public SpatialRule lookupRule(GHPoint point) {
//                return lookupRule(point.lat, point.lon);
//            }
//
//            @Override
//            public int getSpatialId(SpatialRule rule) {
//                throw new IllegalStateException();
//            }
//
//            @Override
//            public int size() {
//                return 2;
//            }
//
//            @Override
//            public BBox getBounds() {
//                return new BBox(-180, 180, -90, 90);
//            }
//        };
//
//        storage.setSpatialRuleLookup(lookup);
//        storage.setMinimumNodes(2);
//        storage.createLandmarks();
//        assertEquals(3, storage.getSubnetworksWithLandmarks());
//    }
}
