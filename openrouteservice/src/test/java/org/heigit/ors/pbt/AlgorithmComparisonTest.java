package org.heigit.ors.pbt;

import java.util.*;

import com.graphhopper.routing.*;
import com.graphhopper.routing.ch.*;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.*;
import com.graphhopper.storage.*;
import net.jqwik.api.*;
import net.jqwik.api.Tuple.*;
import net.jqwik.api.domains.*;
import net.jqwik.api.lifecycle.*;
import org.heigit.ors.matrix.*;
import org.heigit.ors.matrix.algorithms.core.*;
import org.heigit.ors.routing.graphhopper.extensions.core.*;
import org.heigit.ors.util.*;

import static org.heigit.ors.pbt.GraphHopperDomain.*;
import static org.junit.Assert.*;

@Domain(GraphHopperDomain.class)
class AlgorithmComparisonTest {
	private final TraversalMode tMode = TraversalMode.NODE_BASED;
	private static Directory dir;

	@BeforeProperty
	public void setUp() {
		// This should be done globally only once
		System.setProperty("ors_config", "target/test-classes/ors-config-test.json");
		dir = new GHDirectory("", DAType.RAM_INT);
	}

	@AfterProperty
	public void cleanUp() {
		dir.clear();
	}

	@Property(tries = 100) //, seed="7382662923941489490") // reproduces a failure
	// @Report(Reporting.GENERATED)
	void compare_distance_computation_between_CoreMatrix_and_CoreALT(
			@ForAll @MaxNodes(2000) Tuple3<GraphHopperStorage, MatrixLocations, MatrixLocations> matrixScenario
	) throws Exception {

		GraphHopperStorage sampleGraph = matrixScenario.get1();
		MatrixLocations sources = matrixScenario.get2();
		MatrixLocations destinations = matrixScenario.get3();

		try {
			float[] matrixDistances = computeDistancesFromCoreMatrixAlgorithm(sampleGraph, sources, destinations);
			float[] coreDistances = computeDistancesFromCoreALTAlgorithm(sampleGraph, sources, destinations);

			// System.out.println(Arrays.toString(matrixDistances));
			// System.out.println(Arrays.toString(coreDistances));

			assertDistancesAreEqual(matrixDistances, coreDistances, sources, destinations);
		} finally {
			sampleGraph.close();
		}
	}

	private void assertDistancesAreEqual(
		float[] matrixDistances,
		float[] coreDistances,
		MatrixLocations sources,
		MatrixLocations destinations
	) {
		Map<Integer, String> edgesByIndex = buildEdgesIndex(sources, destinations);
		assertEquals("number of distances", coreDistances.length, matrixDistances.length);
		for (int i = 0; i < coreDistances.length; i++) {
			String edge = edgesByIndex.get(i);
			String errorMessage = String.format("Length mismatch for edge %s: ", edge);
			assertEquals(errorMessage, coreDistances[i], matrixDistances[i], 0);
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

	private float[] computeDistancesFromCoreALTAlgorithm(
			GraphHopperStorage sampleGraph,
			MatrixLocations sources,
			MatrixLocations destinations
	) {
		float[] coreDistances = new float[sources.size() * destinations.size()];
		int index = 0;
		for (int sourceId : sources.getNodeIds()) {
			for (int destinationId : destinations.getNodeIds()) {
				CoreALT coreAlgorithm = createCoreAlgorithm(sampleGraph);
				Path path = coreAlgorithm.calcPath(sourceId, destinationId);
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

	private CoreALT createCoreAlgorithm(GraphHopperStorage sampleGraph) {
		return Algorithms.coreALT(sampleGraph, SHORTEST_WEIGHTING_FOR_CARS);
	}

	private float[] computeDistancesFromCoreMatrixAlgorithm(
			GraphHopperStorage sampleGraph,
			MatrixLocations sources,
			MatrixLocations destinations
	) throws Exception {
		CoreMatrixAlgorithm matrixAlgorithm = createAndPrepareMatrixAlgorithm(sampleGraph);
		MatrixResult result = matrixAlgorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
		return result.getTable(MatrixMetricsType.DISTANCE);
	}

	private CoreMatrixAlgorithm createAndPrepareMatrixAlgorithm(GraphHopperStorage sampleGraph) {
		CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
		AllEdgesIterator allEdges = sampleGraph.getAllEdges();
		while (allEdges.next()) {
			restrictedEdges.add(allEdges.getEdge());
		}
		CHGraph contractedGraph = contractGraph(sampleGraph, restrictedEdges);

		CoreMatrixAlgorithm matrixAlgorithm = new CoreMatrixAlgorithm();

		MatrixRequest matrixRequest = new MatrixRequest();
		matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);

		matrixAlgorithm.init(matrixRequest, contractedGraph, carEncoder, SHORTEST_WEIGHTING_FOR_CARS, new CoreTestEdgeFilter());
		return matrixAlgorithm;
	}

	private CHGraph contractGraph(GraphHopperStorage g, EdgeFilter restrictedEdges) {
		CHGraph lg = g.getCHGraph(new CHProfile(SHORTEST_WEIGHTING_FOR_CARS, tMode, TurnWeighting.INFINITE_U_TURN_COSTS, "core"));
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
				System.out.println(" [weight: " + (new PreparationWeighting(SHORTEST_WEIGHTING_FOR_CARS)).calcWeight(iter, false, -1) + "]");
			}
		}

		return lg;
	}

}
