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

import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreLandmarkStorage;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreTestEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.core.PrepareCore;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;
import org.heigit.ors.util.DebugUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Andrzej Oles, Hendrik Leuschner
 */
public class CoreLandmarkStorageTest {
    private GraphHopperStorage ghStorage;
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private final Weighting weighting = new ShortestWeighting(carEncoder);
    private final TraversalMode tMode = TraversalMode.NODE_BASED;
    private Directory dir;

    @BeforeEach
    void setUp() {
        FlagEncoder encoder = new CarFlagEncoder();
        ghStorage = new GraphHopperStorage(new RAMDirectory(),
                EncodingManager.create(encoder), false, new GraphExtension.NoOpExtension());
        ghStorage.create(1000);
        dir = new GHDirectory("", DAType.RAM_INT);
    }

    @AfterEach
    void tearDown() {
        if (ghStorage != null)
            ghStorage.close();
    }

    private GraphHopperStorage createGHStorage() {
        return new GraphBuilder(encodingManager).setCHProfiles(new ArrayList<>()).setCoreGraph(weighting).create();
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
        g.edge(3, 4, 2, true); // restricted in #4
        g.edge(4, 5, 1, true);
        g.edge(4, 6, 1, true);
        g.edge(5, 7, 1, true);
        g.edge(6, 7, 2, true);
        g.edge(7, 8, 3, true); // restricted in #3 and #4
        return g;
    }

    private HashMap<Integer, Integer> createCoreNodeIdMap(CHGraph core) {
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

    private CHGraph contractGraph(GraphHopperStorage g, CoreTestEdgeFilter restrictedEdges) {
        CHGraph lg = g.getCHGraph(new CHProfile(weighting, tMode, TurnWeighting.INFINITE_U_TURN_COSTS, "core"));
        PrepareCore prepare = new PrepareCore(dir, g, lg, restrictedEdges);

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
        ghStorage = createMediumGraph();
        CHGraph g = contractGraph(ghStorage, restrictedEdges);
        HashMap<Integer, Integer> coreNodeIdMap = createCoreNodeIdMap(g);

        CoreLandmarkStorage storage = new CoreLandmarkStorage(dir, ghStorage, coreNodeIdMap, weighting, new LMEdgeFilterSequence(), 2 );
        storage.setMinimumNodes(2);
        storage.createLandmarks();
        assertEquals(2, storage.getSubnetworksWithLandmarks());
        assertEquals("[6, 2]", Arrays.toString(storage.getLandmarks(1)));
    }

    @Test
    void testTwoSubnetworks() {
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

        ghStorage = createMediumGraph();
        CHGraph g = contractGraph(ghStorage, restrictedEdges);
        HashMap<Integer, Integer> coreNodeIdMap = createCoreNodeIdMap(g);


        LMEdgeFilterSequence lmEdgeFilterSequence = new LMEdgeFilterSequence();
        lmEdgeFilterSequence.add(passableEdges);
        CoreLandmarkStorage storage = new CoreLandmarkStorage(dir, ghStorage, coreNodeIdMap, weighting, lmEdgeFilterSequence, 2 );
        storage.setMinimumNodes(2);
        storage.createLandmarks();
        assertEquals(3, storage.getSubnetworksWithLandmarks());
        assertEquals("[3, 8]", Arrays.toString(storage.getLandmarks(1)));
        assertEquals("[7, 4]", Arrays.toString(storage.getLandmarks(2)));
    }

}
