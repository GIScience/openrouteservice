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

import com.graphhopper.routing.ch.NodeOrderingProvider;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.GHUtility;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperStorage;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreLMConfig;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreLandmarkStorage;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreTestEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.core.PrepareCore;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;
import org.heigit.ors.util.DebugUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.heigit.ors.routing.graphhopper.extensions.core.CoreLMPreparationHandler.createCoreNodeIdMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Andrzej Oles, Hendrik Leuschner
 */
class CoreLandmarkStorageTest {
    private ORSGraphHopperStorage graph;
    private FlagEncoder encoder;
    private EncodingManager encodingManager;
    private BooleanEncodedValue subnetworkEnc;

    private Weighting weighting;
    private Directory dir = new GHDirectory("", DAType.RAM_INT);
    private RoutingCHGraph routingCHGraph;
    private CHConfig chConfig;

    @BeforeEach
    void setUp() {
        encoder = new CarFlagEncoder();
        subnetworkEnc = Subnetwork.create(encoder.toString());
        encodingManager = new EncodingManager.Builder().add(encoder).add(subnetworkEnc).build();

        weighting = new ShortestWeighting(encoder);
        chConfig = new CHConfig(encoder.toString(), weighting, false, CHConfig.TYPE_CORE);

        graph =  new ORSGraphHopperStorage(new RAMDirectory(), encodingManager, false, false, -1);
        graph.addCoreGraph(chConfig);
        graph.create(1000);
        routingCHGraph = graph.getCoreGraph(chConfig.getName());
    }

    @AfterEach
    void tearDown() {
        if (graph != null)
            graph.close();
    }

    private void addEdge(int a, int b, double distance) {
        GHUtility.setSpeed(60, true, true, encoder, graph.edge(a, b).setDistance(distance));
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

    private void contractGraph(CoreTestEdgeFilter restrictedEdges) {
        contractGraph(restrictedEdges, null);
    }

    private void contractGraph(CoreTestEdgeFilter restrictedEdges, int[] nodeOrdering) {
        graph.freeze();

        PrepareCore prepare = new PrepareCore(graph, chConfig, restrictedEdges);

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

    private CoreLandmarkStorage createLandmarks(LMEdgeFilterSequence lmEdgeFilter) {
        HashMap<Integer, Integer> coreNodeIdMap = createCoreNodeIdMap(routingCHGraph);
        CoreLMConfig coreLMConfig = new CoreLMConfig(encoder.toString(), weighting).setEdgeFilter(lmEdgeFilter);
        CoreLandmarkStorage storage = new CoreLandmarkStorage(dir, graph, routingCHGraph, coreLMConfig, 2);
        storage.setCoreNodeIdMap(coreNodeIdMap);
        storage.setMinimumNodes(2);
        storage.createLandmarks();
        return storage;
    }

    @Test
    void testOneSubnetwork() {
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

        createMediumGraph();
        contractGraph(restrictedEdges);

        CoreLandmarkStorage storage = createLandmarks(new LMEdgeFilterSequence());

        assertEquals(2, storage.getSubnetworksWithLandmarks());
        assertEquals("[6, 2]", Arrays.toString(storage.getLandmarks(1)));
    }

    @Test
    void testTwoSubnetworks() {
        // All edges in medium graph are part of core. Test if landmarks are built
        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        restrictedEdges.add(1);
        restrictedEdges.add(2);
        restrictedEdges.add(3);
        restrictedEdges.add(4);
        restrictedEdges.add(5);
        restrictedEdges.add(6);
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(10);
        restrictedEdges.add(11);

        CoreTestEdgeFilter passableEdges = new CoreTestEdgeFilter();
        passableEdges.add(7);
        passableEdges.add(12);

        createMediumGraph();
        contractGraph(restrictedEdges);

        LMEdgeFilterSequence lmEdgeFilter = new LMEdgeFilterSequence();
        lmEdgeFilter.add(passableEdges);
        CoreLandmarkStorage storage = createLandmarks(lmEdgeFilter);

        assertEquals(3, storage.getSubnetworksWithLandmarks());
        assertEquals("[3, 8]", Arrays.toString(storage.getLandmarks(1)));
        assertEquals("[7, 4]", Arrays.toString(storage.getLandmarks(2)));
    }
}
