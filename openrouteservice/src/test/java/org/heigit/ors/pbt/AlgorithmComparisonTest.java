package org.heigit.ors.pbt;

import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.ch.CHRoutingAlgorithmFactory;
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.PMap;
import net.jqwik.api.domains.Domain;
import net.jqwik.api.lifecycle.AfterProperty;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.matrix.MatrixLocations;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.matrix.MultiTreeMetricsExtractor;
import org.heigit.ors.routing.algorithms.RPHASTAlgorithm;
import org.heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntry;

import java.util.HashMap;
import java.util.Map;

import static org.heigit.ors.pbt.GraphHopperDomain.carEncoder;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Domain(GraphHopperDomain.class)
class AlgorithmComparisonTest {
    private static Directory dir;
    private  Weighting weighting = new ShortestWeighting(carEncoder);
    private  CHConfig chConfig = new CHConfig("c", weighting, false, CHConfig.TYPE_CORE);
    private RoutingCHGraph routingCHGraph;

    @BeforeProperty
    public void setUp() {
        // This should be done globally only once
        dir = new GHDirectory("", DAType.RAM_INT);
    }

    private PrepareContractionHierarchies createPrepareContractionHierarchies(GraphHopperStorage g) {
        return createPrepareContractionHierarchies(g, chConfig);
    }

    private PrepareContractionHierarchies createPrepareContractionHierarchies(GraphHopperStorage g, CHConfig p) {
        g.freeze();
        return PrepareContractionHierarchies.fromGraphHopperStorage(g, p);
    }


    @AfterProperty
    public void cleanUp() {
        dir.clear();
    }

// TODO Future improvement : Uncomment this and resolve differences to enable this test
    
//    @Property(tries = 2000)// , seed="-2270368960184993644") // reproduces a failure
//        // @Report(Reporting.GENERATED)
//    void compare_distance_computation_between_CoreMatrix_and_CoreALT(
//            @ForAll @MaxNodes(2000) Tuple3<GraphHopperStorage, MatrixLocations, MatrixLocations> matrixScenario
//    ) throws Exception {
//
//        GraphHopperStorage sampleGraph = matrixScenario.get1();
//        FlagEncoder encoder = sampleGraph.getEncodingManager().getEncoder("car");
//        weighting = new ShortestWeighting(encoder);
//        chConfig = sampleGraph.getCHConfig();
//        PrepareContractionHierarchies prepare = createPrepareContractionHierarchies(sampleGraph);
//        prepare.doWork();
//        routingCHGraph = sampleGraph.getRoutingCHGraph("c");
//
//        MatrixLocations sources = matrixScenario.get2();
//        MatrixLocations destinations = matrixScenario.get3();
//        try {
//            float[] matrixDistances = computeDistancesFromRPHAST(sampleGraph, sources, destinations);
//            float[] coreDistances = computeDistancesFromCH(sources, destinations);
//
////             System.out.println(Arrays.toString(matrixDistances));
////             System.out.println(Arrays.toString(coreDistances));
//
//            assertDistancesAreEqual(matrixDistances, coreDistances, sources, destinations);
//        } finally {
//            sampleGraph.close();
//        }
//    }

    private void assertDistancesAreEqual(
            float[] matrixDistances,
            float[] coreDistances,
            MatrixLocations sources,
            MatrixLocations destinations
    ) {
        Map<Integer, String> edgesByIndex = buildEdgesIndex(sources, destinations);
        assertEquals(coreDistances.length, matrixDistances.length, "number of distances");
        for (int i = 0; i < coreDistances.length; i++) {
            String edge = edgesByIndex.get(i);
            String errorMessage = String.format("Length mismatch for edge %s: ", edge);
            assertEquals(coreDistances[i], matrixDistances[i], 0.1, errorMessage);
        }
    }

    private Map<Integer, String> buildEdgesIndex(MatrixLocations sources, MatrixLocations destinations) {
        Map<Integer, String> edgesByIndex = new HashMap<>();
        int index = 0;
        for (int sourceId : sources.getNodeIds()) {
            for (int destinationId : destinations.getNodeIds()) {
                edgesByIndex.put(index, String.format("%s->%s", sourceId, destinationId));
                index += 1;
            }
        }
        return edgesByIndex;
    }

    private float[] computeDistancesFromCH(MatrixLocations sources, MatrixLocations destinations) {
        float[] coreDistances = new float[sources.size() * destinations.size()];
        int index = 0;
        for (int sourceId : sources.getNodeIds()) {
            for (int destinationId : destinations.getNodeIds()) {
                RoutingAlgorithm algo = new CHRoutingAlgorithmFactory(routingCHGraph).createAlgo(new PMap());
                Path path = algo.calcPath(sourceId, destinationId);
                coreDistances[index] = (float) path.getWeight();
                // Matrix algorithm returns -1.0 instead of Infinity
                if (Float.isInfinite(coreDistances[index])) {
                    coreDistances[index] = -1.0f;
                }
                index += 1;
            }
        }
        return coreDistances;
    }

    private float[] computeDistancesFromRPHAST(GraphHopperStorage sampleGraph, MatrixLocations sources, MatrixLocations destinations) throws Exception {
        RPHASTAlgorithm matrixAlgorithm = createAndPrepareRPHAST(sampleGraph.getRoutingCHGraph());
        matrixAlgorithm.prepare(sources.getNodeIds(), destinations.getNodeIds());
        MultiTreeSPEntry[] destTrees = matrixAlgorithm.calcPaths(sources.getNodeIds(), destinations.getNodeIds());
        return extractValues(sampleGraph, sources, destinations, destTrees);
    }

    private float[]  extractValues(GraphHopperStorage sampleGraph, MatrixLocations sources, MatrixLocations destinations, MultiTreeSPEntry[] destTrees) throws Exception {
        MultiTreeMetricsExtractor pathMetricsExtractor = new MultiTreeMetricsExtractor(MatrixMetricsType.DISTANCE, sampleGraph.getRoutingCHGraph(), carEncoder, weighting, DistanceUnit.METERS);
        int tableSize = sources.size() * destinations.size();

        float[] distances = new float[tableSize];
        float[] times = new float[tableSize];
        float[] weights = new float[tableSize];
        MultiTreeSPEntry[] originalDestTrees = new MultiTreeSPEntry[destinations.size()];

        int j = 0;
        for (int i = 0; i < destinations.size(); i++) {
            if (destinations.getNodeIds()[i] != -1) {
                originalDestTrees[i] = destTrees[j];
                ++j;
            } else {
                originalDestTrees[i] = null;
            }
        }

        pathMetricsExtractor.calcValues(originalDestTrees, sources, destinations, times, distances, weights);
        return distances;

    }

    private RPHASTAlgorithm createAndPrepareRPHAST(RoutingCHGraph routingCHGraph) {
        return new RPHASTAlgorithm(routingCHGraph, weighting, TraversalMode.NODE_BASED);
    }
}