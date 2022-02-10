package org.heigit.ors;

import java.util.*;

import com.graphhopper.storage.*;
import net.jqwik.api.*;
import net.jqwik.api.Tuple.*;

import static org.heigit.ors.GHAlgorithmDomain.*;

/**
 * Simple graph generator for up to maxSize nodes and up to (nodes * (nodes-1))/2 edges
 *
 * <ul>
 * <li>The number of nodes is between 2 and MAX_NODES</li>
 * <li>All edges are bidirectional</li>
 * <li>Distances are between 0 and MAX_DISTANCE</li>
 * </ul>
 */
class GraphGenerator implements RandomGenerator<GraphHopperStorage> {
	private final static int MAX_DISTANCE = 10;
	private final static int MAX_NODES = 100;

	@Override
	public Shrinkable<GraphHopperStorage> next(Random random) {
		return Shrinkable.unshrinkable(createSampleGraph(random));
	}

	private GraphHopperStorage createSampleGraph(Random random) {
		GraphHopperStorage storage = createGHStorage();

		int nodes = random.nextInt(MAX_NODES - 1) + 2;
		int bound = computeMaxEdges(nodes);
		int edges = random.nextInt(bound) + 1;

		Set<Tuple2<Integer, Integer>> setOfEdges = new HashSet<>();

		for (int i = 0; i < edges; i++) {
			int from = random.nextInt(nodes);
			int to = random.nextInt(nodes);

			// Exclude reverse direction
			if (!setOfEdges.contains(Tuple.of(to, from))) {
				setOfEdges.add(Tuple.of(from, to));
			}
		}

		for (Tuple2<Integer, Integer> edge : setOfEdges) {
			double distance = random.nextInt(MAX_DISTANCE + 1);
			storage.edge(edge.get1(), edge.get2(), distance, true);
		}

		return storage;
	}

	int computeMaxEdges(int numberOfNodes) {
		// As it is bidirectional graph
		// there can be at-most v*(v-1)/2 number of edges
		return (numberOfNodes * (numberOfNodes - 1)) / 2;
	}

	private GraphHopperStorage createGHStorage() {
		return new GraphBuilder(encodingManager).setCHProfiles(new ArrayList<>()).setCoreGraph(weighting).withTurnCosts(true).create();
	}

}
