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
	private final static int DEFAULT_MAX_NODES = 500;
	private final int maxNodes;

	private static Map<GraphHopperStorage, Long> randomSeeds = new HashMap<>();

	private static void rememberRandom(GraphHopperStorage storage, long randomSeed) {
		randomSeeds.put(storage, randomSeed);
	}

	static long getSeed(GraphHopperStorage storage) {
		return randomSeeds.get(storage);
	}

	static void clearRandomSeeds() {
		randomSeeds.clear();
	}

	GraphGenerator() {
		this(DEFAULT_MAX_NODES);
	}

	public GraphGenerator(int maxNodes) {
		this.maxNodes = maxNodes;
	}

	@Override
	public Shrinkable<GraphHopperStorage> next(Random random) {
		long randomSeed = random.nextLong();
		GraphHopperStorage sampleGraph = create(randomSeed);
		rememberRandom(sampleGraph, randomSeed);
		return Shrinkable.unshrinkable(sampleGraph);
	}

	public GraphHopperStorage create(long randomSeed) {
		GraphHopperStorage storage = createGHStorage();
		Random random = new Random(randomSeed);

		int nodes = random.nextInt(maxNodes - 1) + 2;
		int bound = computeMaxEdges(nodes);
		int edges = random.nextInt(bound) + 1;

		Set<Tuple2<Integer, Integer>> setOfEdges = new HashSet<>();

		for (int i = 0; i < edges; i++) {
			int from = random.nextInt(nodes);
			int to = random.nextInt(nodes);

			// Exclude edges to itself
			if (from == to) {
				continue;
			}

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
		return new GraphBuilder(encodingManager)
			.setCHProfiles(new ArrayList<>())
			.setCoreGraph(weighting)
			.withTurnCosts(true)
			.create();
	}

}
