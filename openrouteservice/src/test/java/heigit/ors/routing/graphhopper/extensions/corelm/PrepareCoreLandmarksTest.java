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
package heigit.ors.routing.graphhopper.extensions.corelm;

import com.graphhopper.routing.*;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.Helper;
import heigit.ors.routing.graphhopper.extensions.core.CoreLandmarkStorage;
import heigit.ors.routing.graphhopper.extensions.core.CoreTestEdgeFilter;
import heigit.ors.routing.graphhopper.extensions.core.PrepareCore;
import heigit.ors.routing.graphhopper.extensions.core.PrepareCoreLandmarks;
import heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;
import heigit.ors.util.DebugUtility;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner
 */

public class PrepareCoreLandmarksTest
/* extends AbstractRoutingAlgorithmTester */ {
    private GraphHopperStorage graph;
    private FlagEncoder encoder;
    private TraversalMode tm;
    private EncodingManager encodingManager;
    private Weighting weighting;
    private static DistanceCalc distCalc;
    Directory dir = new RAMDirectory();


    @Before
    public void setUp() {
        encoder = new CarFlagEncoder();
        encodingManager = new EncodingManager(encoder);
        weighting = new FastestWeighting(encoder);
        tm = TraversalMode.NODE_BASED;
        distCalc = new DistanceCalcEarth();
        GraphHopperStorage tmp = new GraphBuilder(encodingManager).setCoreGraph(weighting).create();
        graph = tmp;
    }

    public HashMap<Integer, Integer> createCoreNodeIdMap(CHGraph core) {
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

    public CHGraph contractGraph(GraphHopperStorage g, CoreTestEdgeFilter restrictedEdges) {
        CHGraph lg = g.getGraph(CHGraph.class);
        PrepareCore prepare = new PrepareCore(dir, g, lg, weighting, tm, restrictedEdges);

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
    public void testLandmarkStorageAndRouting() {
        // create graph with lat,lon
        // 0  1  2  ...
        // 15 16 17 ...
        Random rand = new Random(0);
        int width = 15, height = 15;
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        for (int hIndex = 0; hIndex < height; hIndex++) {
            for (int wIndex = 0; wIndex < width; wIndex++) {
                int node = wIndex + hIndex * width;

                long flags = encoder.setProperties(20 + rand.nextDouble() * 30, true, true);
                // do not connect first with last column!
                if (wIndex + 1 < width)
                    graph.edge(node, node + 1).setFlags(flags);

                // avoid dead ends
                if (hIndex + 1 < height)
                    graph.edge(node, node + width).setFlags(flags);

                NodeAccess na = graph.getNodeAccess();
                na.setNode(node, -hIndex / 50.0, wIndex / 50.0);
                EdgeIterator iter = graph.createEdgeExplorer().setBaseNode(node);
                while (iter.next()) {
                    iter.setDistance(iter.fetchWayGeometry(3).calcDistance(distCalc));
                    restrictedEdges.add(iter.getEdge());
                }
            }
        }
        CHGraph g = contractGraph(graph, restrictedEdges);
        HashMap<Integer, Integer> coreNodeIdMap = createCoreNodeIdMap(g);
        LocationIndex index = new LocationIndexTree(graph, dir);
        index.prepareIndex();

        int lm = 5, activeLM = 2;
        CoreLandmarkStorage store = new CoreLandmarkStorage(dir, graph, coreNodeIdMap, weighting,new LMEdgeFilterSequence(), lm );
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
        assertEquals(3640, Math.round(store.getFromWeight(0, 52) * factor));

        long weight1_224 = store.getFromWeight(1, 224);
        assertEquals(5525, Math.round(weight1_224 * factor));
        long weight1_47 = store.getFromWeight(1, 47);
        assertEquals(921, Math.round(weight1_47 * factor));

        // grid is symmetric
        assertEquals(weight1_224, store.getToWeight(1, 224));
        assertEquals(weight1_47, store.getToWeight(1, 47));

        // prefer the landmarks before and behind the goal
        int activeLandmarkIndices[] = new int[activeLM];
        int activeFroms[] = new int[activeLM];
        int activeTos[] = new int[activeLM];
        Arrays.fill(activeLandmarkIndices, -1);
        store.initActiveLandmarks(27, 47, activeLandmarkIndices, activeFroms, activeTos, false);
        List<Integer> list = new ArrayList<>();
        for (int idx : activeLandmarkIndices) {
            list.add(store.getLandmarks(1)[idx]);
        }
        // TODO should better select 0 and 224?
        assertEquals(Arrays.asList(224, 70), list);

        AlgorithmOptions opts = AlgorithmOptions.start().weighting(weighting).traversalMode(tm).
                build();

        PrepareCoreLandmarks prepare = new PrepareCoreLandmarks(new RAMDirectory(), graph, coreNodeIdMap, weighting, new LMEdgeFilterSequence(), 4, 2);
        prepare.setMinimumNodes(2);
        prepare.doWork();

        AStar expectedAlgo = new AStar(graph, weighting, tm);
        Path expectedPath = expectedAlgo.calcPath(41, 183);

        // landmarks with A*
        RoutingAlgorithm oneDirAlgoWithLandmarks = prepare.getDecoratedAlgorithm(graph, new AStar(graph, weighting, tm), opts);
        Path path = oneDirAlgoWithLandmarks.calcPath(41, 183);

        assertEquals(expectedPath.getWeight(), path.getWeight(), .1);
        assertEquals(expectedPath.calcNodes(), path.calcNodes());
        assertEquals(expectedAlgo.getVisitedNodes(), oneDirAlgoWithLandmarks.getVisitedNodes() + 142);

        // landmarks with bidir A*
        opts.getHints().put("lm.recalc_count", 50);
        RoutingAlgorithm biDirAlgoWithLandmarks = prepare.getDecoratedAlgorithm(graph,
                new AStarBidirection(graph, weighting, tm), opts);
        path = biDirAlgoWithLandmarks.calcPath(41, 183);
        assertEquals(expectedPath.getWeight(), path.getWeight(), .1);
        assertEquals(expectedPath.calcNodes(), path.calcNodes());
        assertEquals(expectedAlgo.getVisitedNodes(), biDirAlgoWithLandmarks.getVisitedNodes() + 66);

        // landmarks with A* and a QueryGraph. We expect slightly less optimal as two more cycles needs to be traversed
        // due to the two more virtual nodes but this should not harm in practise
        QueryGraph qGraph = new QueryGraph(graph);
        QueryResult fromQR = index.findClosest(-0.0401, 0.2201, EdgeFilter.ALL_EDGES);
        QueryResult toQR = index.findClosest(-0.2401, 0.0601, EdgeFilter.ALL_EDGES);
        qGraph.lookup(fromQR, toQR);
        RoutingAlgorithm qGraphOneDirAlgo = prepare.getDecoratedAlgorithm(qGraph,
                new AStar(qGraph, weighting, tm), opts);
        path = qGraphOneDirAlgo.calcPath(fromQR.getClosestNode(), toQR.getClosestNode());

        expectedAlgo = new AStar(qGraph, weighting, tm);
        expectedPath = expectedAlgo.calcPath(fromQR.getClosestNode(), toQR.getClosestNode());
        assertEquals(expectedPath.getWeight(), path.getWeight(), .1);
        assertEquals(expectedPath.calcNodes(), path.calcNodes());
        assertEquals(expectedAlgo.getVisitedNodes(), qGraphOneDirAlgo.getVisitedNodes() + 133);
    }

    @Test
    public void testStoreAndLoad() {
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        graph.edge(0, 1, 80_000, true);
        graph.edge(1, 2, 80_000, true);
        restrictedEdges.add(0);
        restrictedEdges.add(1);
        CHGraph g = contractGraph(graph, restrictedEdges);
        HashMap<Integer, Integer> coreNodeIdMap = createCoreNodeIdMap(g);
        String fileStr = "./target/tmp-lm";
        Helper.removeDir(new File(fileStr));

        Directory dir = new RAMDirectory(fileStr, true).create();
        Weighting weighting = new FastestWeighting(encoder);
        PrepareCoreLandmarks plm = new PrepareCoreLandmarks(dir, graph, coreNodeIdMap, weighting, new LMEdgeFilterSequence(), 2, 2);
        plm.setMinimumNodes(2);
        plm.doWork();

        double expectedFactor = plm.getLandmarkStorage().getFactor();
        assertTrue(plm.getLandmarkStorage().isInitialized());
        assertEquals(Arrays.toString(new int[]{
                2, 0
        }), Arrays.toString(plm.getLandmarkStorage().getLandmarks(1)));
        assertEquals(4791, Math.round(plm.getLandmarkStorage().getFromWeight(0, 1) * expectedFactor));

        dir = new RAMDirectory(fileStr, true);
        plm = new PrepareCoreLandmarks(dir, graph, coreNodeIdMap, weighting, new LMEdgeFilterSequence(), 2, 2);
        assertTrue(plm.loadExisting());
        assertEquals(expectedFactor, plm.getLandmarkStorage().getFactor(), 1e-6);
        assertEquals(Arrays.toString(new int[]{
                2, 0
        }), Arrays.toString(plm.getLandmarkStorage().getLandmarks(1)));
        assertEquals(4791, Math.round(plm.getLandmarkStorage().getFromWeight(0, 1) * expectedFactor));

        Helper.removeDir(new File(fileStr));
    }
}
