package org.heigit.ors;

import java.util.*;

import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.*;
import com.graphhopper.storage.*;
import net.jqwik.api.*;
import net.jqwik.api.domains.*;
import org.heigit.ors.matrix.*;
import org.heigit.ors.util.*;

import static net.jqwik.api.Arbitraries.*;

class GHAlgorithmDomain extends DomainContextBase {

	final static CarFlagEncoder carEncoder = new CarFlagEncoder(5, 5.0D, 1);
	final static EncodingManager encodingManager = EncodingManager.create(carEncoder);
	final static Weighting weighting = new ShortestWeighting(carEncoder);

	@Provide
	Arbitrary<Tuple.Tuple3<GraphHopperStorage, MatrixLocations, MatrixLocations>> matrixScenarios() {
		Arbitrary<GraphHopperStorage> graphs = just(createSampleGraph());
		return graphs.flatMap(graph -> {
			Set<Integer> nodes = getAllNodes(graph);
			Arbitrary<MatrixLocations> sources = Arbitraries.of(nodes).set().ofMinSize(1).map(this::locations);
			Arbitrary<MatrixLocations> destinations = Arbitraries.of(nodes).set().ofMinSize(1).map(this::locations);
			return Combinators.combine(sources, destinations).as((s, d) -> Tuple.of(graph, s, d));
		});
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

	private GraphHopperStorage createSampleGraph() {
		return ToyGraphCreationUtil.createMediumGraph(createGHStorage());
	}

	private GraphHopperStorage createGHStorage() {
		return new GraphBuilder(encodingManager).setCHProfiles(new ArrayList<>()).setCoreGraph(weighting).withTurnCosts(true).create();
	}

	private MatrixLocations locations(Collection<Integer> nodeIds) {
		List<Integer> nodes = new ArrayList<>(nodeIds);
		MatrixLocations locations = new MatrixLocations(nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			locations.setData(i, nodes.get(i), null);
		}
		return locations;
	}

	class MatrixLocationsFormat implements SampleReportingFormat {

		@Override
		public boolean appliesTo(Object o) {
			return o instanceof MatrixLocations;
		}

		@Override
		public Object report(Object o) {
			MatrixLocations matrixLocations = (MatrixLocations) o;
			return ((MatrixLocations) o).getLocations();
		}
	}
}
