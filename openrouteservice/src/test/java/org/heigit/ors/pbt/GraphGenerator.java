package org.heigit.ors.pbt;

import java.util.*;

import com.graphhopper.storage.*;
import net.jqwik.api.*;
import net.jqwik.api.Tuple.*;

import static java.lang.Math.*;
import static org.heigit.ors.pbt.GraphHopperDomain.*;

/**
 * Simple graph generator for up to maxSize nodes and up to (nodes * (nodes-1))/2 edges
 *
 * <ul>
 * <li>The number of nodes is between 2 and maxNodes</li>
 * <li>The average number of edges per node is <= AVERAGE_EDGES_PER_NODE</li>
 * <li>All edges are bidirectional</li>
 * <li>Distances are between 0 and MAX_DISTANCE</li>
 * </ul>
 */
class GraphGenerator implements RandomGenerator<GraphHopperStorage> {
	private final static int MAX_DISTANCE = 10;
	private final static int AVERAGE_EDGES_PER_NODE = 4;

	private final int maxNodes;

	static GraphHopperStorage createGHStorage() {
		return new GraphBuilder(encodingManager)
			.setCHProfiles(new ArrayList<>())
			.setCoreGraph(SHORTEST_WEIGHTING_FOR_CARS)
			.withTurnCosts(true)
			.create();
	}

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

	public GraphGenerator(int maxNodes) {
		this.maxNodes = maxNodes;
	}

	@Override
	public Shrinkable<GraphHopperStorage> next(Random random) {
		long randomSeed = random.nextLong();
		// Regenerating a graph on each request is necessary because the underlying
		// graph storage will be closed after each try.
		// TODO: this code uses an internal jqwik API Shrinkable.supplyUnshrinkable
		// This will be unnecessary if graph generation is done using arbitrary combination
		return Shrinkable.supplyUnshrinkable(() -> {
			GraphHopperStorage sampleGraph = create(randomSeed);
			rememberRandom(sampleGraph, randomSeed);
			return sampleGraph;
		});
	}

	// TODO: Make sure graph is fully connected
	public GraphHopperStorage create(long randomSeed) {
		GraphHopperStorage storage = createGHStorage();
		Random random = new Random(randomSeed);

		int nodes = random.nextInt(maxNodes - 1) + 2;

		Set<Tuple2<Integer, Integer>> setOfEdges = new HashSet<>();

		for (int from = 0; from < nodes; from++) {
			int maxDistance = 2;
			Set<Integer> neighbours = findNeighbours(nodes, from, maxDistance);
			double probability = AVERAGE_EDGES_PER_NODE / Math.max(1.0, neighbours.size());
			for (int to : neighbours) {
				if (random.nextDouble() <= probability) {
					if (!setOfEdges.contains(Tuple.of(to, from))) {
						setOfEdges.add(Tuple.of(from, to));
					}
				}
			}
		}

		for (Tuple2<Integer, Integer> edge : setOfEdges) {
			double distance = random.nextInt(MAX_DISTANCE + 1);
			storage.edge(edge.get1(), edge.get2(), distance, true);
		}
		storage.freeze();

		return storage;
	}

	private Tuple2<Integer, Integer> rasterCoordinates(int rasterWidth, int node) {
		int x = node % rasterWidth;
		int y = node / rasterWidth;
		Tuple2<Integer, Integer> coordinates = Tuple.of(x, y);
		return coordinates;
	}

	/**
	 * Find neighbours in an approximated square raster
	 */
	private Set<Integer> findNeighbours(
		int numberOfNodes,
		int node,
		double maxDistance
	) {
		Set<Integer> neighbours = new HashSet<>();
		int rasterWidth = (int) Math.sqrt(numberOfNodes);

		Tuple2<Integer, Integer> nodeLoc = rasterCoordinates(rasterWidth, node);
		for (int candidate = 0; candidate < numberOfNodes; candidate++) {
			if (candidate == node) {
				continue;
			}
			Tuple2<Integer, Integer> candidateLoc = rasterCoordinates(rasterWidth, candidate);
			int xDiff = abs(candidateLoc.get1() - nodeLoc.get1());
			int yDiff = abs(candidateLoc.get2() - nodeLoc.get2());
			double distance = sqrt(xDiff * xDiff + yDiff * yDiff);
			if (distance <= maxDistance) {
				neighbours.add(candidate);
			}
		}

		return neighbours;
	}

}
