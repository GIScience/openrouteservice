package org.heigit.ors.pbt;

import java.util.*;

import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.*;
import com.graphhopper.storage.*;
import net.jqwik.api.*;
import net.jqwik.api.Tuple.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.domains.*;
import net.jqwik.api.providers.*;

import org.heigit.ors.matrix.*;

public class GraphHopperDomain extends DomainContextBase {

	final static CarFlagEncoder carEncoder = new CarFlagEncoder(5, 5.0D, 1);
	final static EncodingManager encodingManager = EncodingManager.create(carEncoder);
	final static Weighting weighting = new ShortestWeighting(carEncoder);

	@Provide
	Arbitrary<Tuple3<GraphHopperStorage, MatrixLocations, MatrixLocations>> matrixScenarios(TypeUsage typeUsage) {
		Optional<Size> annotation = typeUsage.findAnnotation(Size.class);
		int maxNodes = annotation.map(size -> effectiveMaxSize(size)).orElse(500);
		Arbitrary<GraphHopperStorage> graphs = connectedBidirectionalGraph(maxNodes);
		return graphs.flatMap(graph -> {
			Set<Integer> nodes = getAllNodes(graph);
			Arbitrary<MatrixLocations> sources = Arbitraries.of(nodes).set().ofMinSize(1).map(this::locations);
			Arbitrary<MatrixLocations> destinations = Arbitraries.of(nodes).set().ofMinSize(1).map(this::locations);
			return Combinators.combine(sources, destinations).as((s, d) -> Tuple.of(graph, s, d));
		});
	}

	private int effectiveMaxSize(Size size) {
		if (size.max() != 0) {
			return Math.max(size.max(), size.min());
		}
		return size.value();
	}

	private Arbitrary<GraphHopperStorage> connectedBidirectionalGraph(int maxNodes) {
		return Arbitraries.fromGenerator(new GraphGenerator(maxNodes));
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

	static class MatrixLocationsFormat implements SampleReportingFormat {

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

	static class GraphFormat implements SampleReportingFormat {

		@Override
		public boolean appliesTo(Object o) {
			return o instanceof GraphHopperStorage;
		}

		@Override
		public Optional<String> label(Object value) {
			return Optional.of("Graph");
		}

		@Override
		public Object report(Object o) {
			GraphHopperStorage graph = (GraphHopperStorage) o;
			SortedMap<String, Object> attributes = new TreeMap<>();
			attributes.put("seed", GraphGenerator.getSeed(graph));
			attributes.put("nodes", graph.getNodes());
			attributes.put("edges", graph.getEdges());
			return attributes;
			// return String.format("Graph[nodes=%s, edges=%s]", graph.getNodes(), graph.getEdges());
		}
	}
}
