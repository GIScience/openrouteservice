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
package org.heigit.ors.routing.graphhopper.extensions.corelm;

import com.graphhopper.routing.AStar;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.lm.PrepareLandmarks;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHConfig;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.*;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperStorage;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreLMConfig;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreLandmarkStorage;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreTestEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.core.PrepareCoreLandmarks;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;

import static com.graphhopper.util.GHUtility.updateDistancesFor;
import static com.graphhopper.util.Parameters.Algorithms.ASTAR;
import static com.graphhopper.util.Parameters.Algorithms.ASTAR_BI;
import static org.heigit.ors.routing.graphhopper.extensions.core.CoreLMPreparationHandler.createCoreNodeIdMap;
import static org.heigit.ors.routing.graphhopper.extensions.core.PrepareCoreTest.contractGraph;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner
 */

class PrepareCoreLandmarksTest
/* extends AbstractRoutingAlgorithmTester */ {
    private ORSGraphHopperStorage graph;
    private FlagEncoder encoder;
    private TraversalMode tm = TraversalMode.NODE_BASED;
    private EncodingManager encodingManager;
    private Weighting weighting;
    private CHConfig chConfig;

    @BeforeEach
    void setUp() {
        encoder = new CarFlagEncoder(5, 5, 3);
        encodingManager = new EncodingManager.Builder().add(encoder).add(Subnetwork.create("car")).build();
        weighting = new FastestWeighting(encoder);
        chConfig = new CHConfig("car", weighting, false, CHConfig.TYPE_CORE);
        graph = new ORSGraphHopperStorage(new RAMDirectory(), encodingManager, false, false, -1);
        graph.addCoreGraph(chConfig);
        graph.create(1000);
    }

    @Test
    void testLandmarkStorageAndRouting() {
        // create graph with lat,lon
        // 0  1  2  ...
        // 15 16 17 ...
        Random rand = new Random(0);
        int width = 15, height = 15;

        DecimalEncodedValue avSpeedEnc = encoder.getAverageSpeedEnc();
        BooleanEncodedValue accessEnc = encoder.getAccessEnc();
        for (int hIndex = 0; hIndex < height; hIndex++) {
            for (int wIndex = 0; wIndex < width; wIndex++) {
                int node = wIndex + hIndex * width;

                // do not connect first with last column!
                double speed = 20 + rand.nextDouble() * 30;
                if (wIndex + 1 < width)
                    graph.edge(node, node + 1).set(accessEnc, true, true).set(avSpeedEnc, speed);

                // avoid dead ends
                if (hIndex + 1 < height)
                    graph.edge(node, node + width).set(accessEnc, true, true).set(avSpeedEnc, speed);

                updateDistancesFor(graph, node, -hIndex / 50.0, wIndex / 50.0);
            }
        }

        RoutingCHGraph core = contractGraph(graph, chConfig, new AllCoreEdgeFilter());
        HashMap<Integer, Integer> coreNodeIdMap = createCoreNodeIdMap(core);
        Directory dir = new RAMDirectory();
        LocationIndexTree index = new LocationIndexTree(graph, dir);
        index.prepareIndex();

        int lm = 5, activeLM = 2;
        Weighting weighting = new FastestWeighting(encoder);
        CoreLMConfig coreLMConfig = new CoreLMConfig("car", weighting).setEdgeFilter(new LMEdgeFilterSequence());
        CoreLandmarkStorage store = new CoreLandmarkStorage(dir, graph, core, coreLMConfig, lm);
        store.setCoreNodeIdMap(coreNodeIdMap);
        store.setMinimumNodes(2);
        store.createLandmarks();

        // landmarks should be the 4 corners of the grid:
        int[] intList = store.getLandmarks(1);
        Arrays.sort(intList);
        assertEquals("[0, 14, 70, 182, 224]", Arrays.toString(intList));
        // two landmarks: one for subnetwork 0 (all empty) and one for subnetwork 1
        assertEquals(2, store.getSubnetworksWithLandmarks());

        assertEquals(0, store.getFromWeight(0, 224));
        double factor = store.getFactor();
        assertEquals(4671, Math.round(store.getFromWeight(0, 47) * factor));
        System.out.println(factor + " " + store.getFromWeight(0, 52));
        assertEquals(3639, Math.round(store.getFromWeight(0, 52) * factor));// The difference to a corresponding GH test is due to a different node order in the output of Tarjan which is used to compute the storage factor based on estimated maxWeight

        long weight1_224 = store.getFromWeight(1, 224);
        assertEquals(5525, Math.round(weight1_224 * factor));
        long weight1_47 = store.getFromWeight(1, 47);
        assertEquals(921, Math.round(weight1_47 * factor));

        // grid is symmetric
        assertEquals(weight1_224, store.getToWeight(1, 224));
        assertEquals(weight1_47, store.getToWeight(1, 47));

        // prefer the landmarks before and behind the goal
        int[] activeLandmarkIndices = new int[activeLM];
        Arrays.fill(activeLandmarkIndices, -1);
        store.chooseActiveLandmarks(27, 47, activeLandmarkIndices, false);
        List<Integer> list = new ArrayList<>();
        for (int idx : activeLandmarkIndices) {
            list.add(store.getLandmarks(1)[idx]);
        }
        // TODO should better select 0 and 224?
        assertEquals(Arrays.asList(224, 70), list);

        PrepareLandmarks prepare = new PrepareCoreLandmarks(new RAMDirectory(), graph, coreLMConfig, 4, coreNodeIdMap);
        prepare.setMinimumNodes(2);
        prepare.doWork();

        AStar expectedAlgo = new AStar(graph, weighting, tm);
        Path expectedPath = expectedAlgo.calcPath(41, 183);

        PMap hints = new PMap().putObject(Parameters.Landmark.ACTIVE_COUNT, 2);

        // landmarks with A*
        RoutingAlgorithm oneDirAlgoWithLandmarks = prepare.getRoutingAlgorithmFactory().createAlgo(graph, weighting,
                new AlgorithmOptions().setAlgorithm(ASTAR).setTraversalMode(tm).setHints(hints));

        Path path = oneDirAlgoWithLandmarks.calcPath(41, 183);

        assertEquals(expectedPath.getWeight(), path.getWeight(), .1);
        assertEquals(expectedPath.calcNodes(), path.calcNodes());
        assertEquals(expectedAlgo.getVisitedNodes(), oneDirAlgoWithLandmarks.getVisitedNodes() + 133);

        // landmarks with bidir A*
        RoutingAlgorithm biDirAlgoWithLandmarks = prepare.getRoutingAlgorithmFactory().createAlgo(graph, weighting,
                new AlgorithmOptions().setAlgorithm(ASTAR_BI).setTraversalMode(tm).setHints(hints));
        path = biDirAlgoWithLandmarks.calcPath(41, 183);
        assertEquals(expectedPath.getWeight(), path.getWeight(), .1);
        assertEquals(expectedPath.calcNodes(), path.calcNodes());
        assertEquals(expectedAlgo.getVisitedNodes(), biDirAlgoWithLandmarks.getVisitedNodes() + 162);

        // landmarks with A* and a QueryGraph. We expect slightly less optimal as two more cycles needs to be traversed
        // due to the two more virtual nodes but this should not harm in practise
        Snap fromSnap = index.findClosest(-0.0401, 0.2201, EdgeFilter.ALL_EDGES);
        Snap toSnap = index.findClosest(-0.2401, 0.0601, EdgeFilter.ALL_EDGES);
        QueryGraph qGraph = QueryGraph.create(graph, fromSnap, toSnap);
        RoutingAlgorithm qGraphOneDirAlgo = prepare.getRoutingAlgorithmFactory().createAlgo(qGraph, weighting,
                new AlgorithmOptions().setAlgorithm(ASTAR).setTraversalMode(tm).setHints(hints));
        path = qGraphOneDirAlgo.calcPath(fromSnap.getClosestNode(), toSnap.getClosestNode());

        expectedAlgo = new AStar(qGraph, weighting, tm);
        expectedPath = expectedAlgo.calcPath(fromSnap.getClosestNode(), toSnap.getClosestNode());
        assertEquals(expectedPath.getWeight(), path.getWeight(), .1);
        assertEquals(expectedPath.calcNodes(), path.calcNodes());
        assertEquals(expectedAlgo.getVisitedNodes(), qGraphOneDirAlgo.getVisitedNodes() + 133);
    }

    @Test
    void testStoreAndLoad() {
        GHUtility.setSpeed(60, true, true, encoder, graph.edge(0, 1).setDistance(80_000));
        GHUtility.setSpeed(60, true, true, encoder, graph.edge(1, 2).setDistance(80_000));
        String fileStr = "./target/tmp-lm";
        Helper.removeDir(new File(fileStr));

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        restrictedEdges.add(1);
        RoutingCHGraph core = contractGraph(graph, chConfig, restrictedEdges);
        Map<Integer, Integer> coreNodeIdMap = createCoreNodeIdMap(core);

        Directory dir = new RAMDirectory(fileStr, true).create();
        CoreLMConfig coreLMConfig = new CoreLMConfig("car", weighting).setEdgeFilter(new LMEdgeFilterSequence());
        PrepareCoreLandmarks plm = new PrepareCoreLandmarks(dir, graph, coreLMConfig, 2, coreNodeIdMap);
        plm.setMinimumNodes(2);
        plm.doWork();

        double expectedFactor = plm.getLandmarkStorage().getFactor();
        assertTrue(plm.getLandmarkStorage().isInitialized());
        assertEquals(Arrays.toString(new int[]{
                2, 0
        }), Arrays.toString(plm.getLandmarkStorage().getLandmarks(1)));
        assertEquals(4800, Math.round(plm.getLandmarkStorage().getFromWeight(0, 1) * expectedFactor));

        dir = new RAMDirectory(fileStr, true);
        plm = new PrepareCoreLandmarks(dir, graph, coreLMConfig, 2, coreNodeIdMap);
        assertTrue(plm.loadExisting());
        assertEquals(expectedFactor, plm.getLandmarkStorage().getFactor(), 1e-6);
        assertEquals(Arrays.toString(new int[]{
                2, 0
        }), Arrays.toString(plm.getLandmarkStorage().getLandmarks(1)));
        assertEquals(4800, Math.round(plm.getLandmarkStorage().getFromWeight(0, 1) * expectedFactor));

        Helper.removeDir(new File(fileStr));
    }

    private class AllCoreEdgeFilter implements EdgeFilter {

        @Override
        public final boolean accept(EdgeIteratorState iter) {
            return false;
        }
    }
}
