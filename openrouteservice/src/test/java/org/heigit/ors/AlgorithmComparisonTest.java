package org.heigit.ors;

import java.util.*;

import com.graphhopper.routing.*;
import com.graphhopper.routing.ch.*;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.*;
import com.graphhopper.storage.*;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import org.heigit.ors.matrix.*;
import org.heigit.ors.matrix.algorithms.core.*;
import org.heigit.ors.routing.graphhopper.extensions.core.*;
import org.heigit.ors.util.*;

import static org.junit.Assert.*;

class AlgorithmComparisonTest {
	private final CarFlagEncoder carEncoder = new CarFlagEncoder(5, 5.0D, 1);
	private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
	private final Weighting weighting = new ShortestWeighting(carEncoder);
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

	@Example
	void compareManyToManyAllEdges_CoreMatrix_CoreALT() throws Exception {
		GraphHopperStorage sampleGraph = createSampleGraph();

//        MatrixLocations sources = locations(1, 0);
		MatrixLocations sources = locations(2, 3);
		MatrixLocations destinations = locations(4, 5, 6);

		float[] matrixDistances = computeDistancesFromMatrixAlgorithm(sampleGraph, sources, destinations);
		float[] coreDistances = computeDistancesFromCoreAlgorithm(sampleGraph, sources, destinations);

		assertEquals("number of distances", coreDistances.length, matrixDistances.length);
		for (int i = 0; i < coreDistances.length; i++) {
			assertEquals(coreDistances[i], matrixDistances[i], 0);
		}
	}

	private GraphHopperStorage createSampleGraph() {
		return ToyGraphCreationUtil.createMediumGraph(createGHStorage());
	}

	private GraphHopperStorage createGHStorage() {
		return new GraphBuilder(encodingManager).setCHProfiles(new ArrayList<>()).setCoreGraph(weighting).withTurnCosts(true).create();
	}

	private float[] computeDistancesFromCoreAlgorithm(
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
				index += 1;
			}
		}
		return coreDistances;
	}

	private CoreALT createCoreAlgorithm(GraphHopperStorage sampleGraph) {
		QueryGraph queryGraph = new QueryGraph(sampleGraph.getCHGraph());
		queryGraph.lookup(Collections.emptyList());
		CoreALT coreAlgorithm = new CoreALT(queryGraph, weighting);
		CoreDijkstraFilter levelFilter = new CoreDijkstraFilter(sampleGraph.getCHGraph());
		coreAlgorithm.setEdgeFilter(levelFilter);
		return coreAlgorithm;
	}

	private float[] computeDistancesFromMatrixAlgorithm(
			GraphHopperStorage sampleGraph,
			MatrixLocations sources,
			MatrixLocations destinations
	) throws Exception {
		CoreMatrixAlgorithm matrixAlgorithm = createAndPrepareMatrixAlgorithm(sampleGraph);
		MatrixResult result = matrixAlgorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
		return result.getTable(MatrixMetricsType.DISTANCE);
	}

	private MatrixLocations locations(int... nodeIds) {
		MatrixLocations sources = new MatrixLocations(nodeIds.length);
		for (int i = 0; i < nodeIds.length; i++) {
			sources.setData(i, nodeIds[i], null);
		}
		return sources;
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

		matrixAlgorithm.init(matrixRequest, contractedGraph, carEncoder, weighting, new CoreTestEdgeFilter());
		return matrixAlgorithm;
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

}
