package org.heigit.ors;

import java.util.*;

import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.*;
import com.graphhopper.storage.*;
import net.jqwik.api.*;
import net.jqwik.api.Tuple.*;
import net.jqwik.api.domains.*;
import org.heigit.ors.matrix.*;

public class GHAlgorithmDomain extends DomainContextBase {

	final static CarFlagEncoder carEncoder = new CarFlagEncoder(5, 5.0D, 1);
	final static EncodingManager encodingManager = EncodingManager.create(carEncoder);
	final static Weighting weighting = new ShortestWeighting(carEncoder);

	@Provide
	Arbitrary<Tuple3<GraphHopperStorage, MatrixLocations, MatrixLocations>> matrixScenarios() {
		Arbitrary<GraphHopperStorage> graphs = connectedBidirectionalGraph();
		return graphs.flatMap(graph -> {
			Set<Integer> nodes = getAllNodes(graph);
			Arbitrary<MatrixLocations> sources = Arbitraries.of(nodes).set().ofMinSize(1).map(this::locations);
			Arbitrary<MatrixLocations> destinations = Arbitraries.of(nodes).set().ofMinSize(1).map(this::locations);
			return Combinators.combine(sources, destinations).as((s, d) -> Tuple.of(graph, s, d));
		});
	}

	private Arbitrary<GraphHopperStorage> connectedBidirectionalGraph() {
		return Arbitraries.fromGenerator(new GraphGenerator(100));
	}

	private Set<Integer> getAllNodes(GraphHopperStorage graph) {
		Set<Integer> nodes = new HashSet<>();
		AllEdgesIterator allEdges = graph.getAllEdges();
		while (allEdges.next()) {
			nodes.add(allEdges.getBaseNode());
			nodes.add(allEdges.getAdjNode());
		}
		return nodes;
	}

	private MatrixLocations locations(Collection<Integer> nodeIds) {
		List<Integer> nodes = new ArrayList<>(nodeIds);
		MatrixLocations locations = new MatrixLocations(nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			locations.setData(i, nodes.get(i), null);
		}
		return locations;
	}

	// TODO: With jqwik 1.6.4 entry in META-INF/services/net.jqwik.api.SampleReportingFormat can be removed
	public static class MatrixLocationsFormat implements SampleReportingFormat {

		@Override
		public boolean appliesTo(Object o) {
			return o instanceof MatrixLocations;
		}

		@Override
		public Object report(Object o) {
			MatrixLocations matrixLocations = (MatrixLocations) o;
			return matrixLocations.getNodeIds();
		}
	}

	// TODO: With jqwik 1.6.4 entry in META-INF/services/net.jqwik.api.SampleReportingFormat can be removed
	public static class GraphFormat implements SampleReportingFormat {

		@Override
		public boolean appliesTo(Object o) {
			return o instanceof GraphHopperStorage;
		}

		@Override
		public Object report(Object o) {
			GraphHopperStorage graph = (GraphHopperStorage) o;
			return String.format("Graph[nodes=%s, edges=%s]", graph.getNodes(), graph.getEdges());
		}
	}
}
