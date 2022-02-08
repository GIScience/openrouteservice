package org.heigit.ors;

import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.ch.PreparationWeighting;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import org.heigit.ors.matrix.MatrixLocations;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.matrix.MatrixRequest;
import org.heigit.ors.matrix.MatrixResult;
import org.heigit.ors.matrix.algorithms.core.CoreMatrixAlgorithm;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreALT;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreDijkstraFilter;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreTestEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.core.PrepareCore;
import org.heigit.ors.util.DebugUtility;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class AlgorithmComparisonTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder(5, 5.0D, 1);
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private final Weighting weighting = new ShortestWeighting(carEncoder);
    private final TraversalMode tMode = TraversalMode.NODE_BASED;
    private Directory dir;

    GraphHopperStorage createGHStorage() {
        return new GraphBuilder(encodingManager).setCHProfiles(new ArrayList<>()).setCoreGraph(weighting).withTurnCosts(true).create();
    }

    @BeforeClass
    public static void setupConfiguration() {
        // This should be done globally
        System.setProperty("ors_config", "target/test-classes/ors-config-test.json");
    }

    @Before
    public void setUp() {
        dir = new GHDirectory("", DAType.RAM_INT);
    }

    private CHGraph contractGraph(GraphHopperStorage g, EdgeFilter restrictedEdges) {
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
                System.out.println(" [weight: " + (new PreparationWeighting(weighting)).calcWeight(iter, false, -1) + "]");
            }
        }

        return lg;
    }

    @Test
    public void compareManyToManyAllEdges_CoreMatrix_CoreALT() throws Exception {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());

        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        QueryGraph queryGraph = new QueryGraph(graphHopperStorage.getCHGraph());
        queryGraph.lookup(Collections.emptyList());

        CoreALT coreALT = new CoreALT(queryGraph, weighting);

        // append any restriction filters after node level filter
        CoreDijkstraFilter levelFilter = new CoreDijkstraFilter(graphHopperStorage.getCHGraph());
        coreALT.setEdgeFilter(levelFilter);

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
        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 1, null);
        sources.setData(1, 0, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 4, null);
        destinations.setData(1, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);

        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);

        //CoreALT
        Path path = coreALT.calcPath(1, 4);
        assertEquals(path.getWeight(), result.getTable(MatrixMetricsType.DISTANCE)[0], 0);

    }


}
