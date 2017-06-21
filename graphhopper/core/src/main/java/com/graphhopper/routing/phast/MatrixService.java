package com.graphhopper.routing.phast;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.ch.PrepareContractionHierarchies.DijkstraBidirectionCHRPHAST;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.SPTEntry;

public class MatrixService {
	private PrepareContractionHierarchies pch;
	private CHGraph graph;
	private FlagEncoder encoder;
	private int metricsFlag;

	public final static int DURATION = 1;
	public final static int DISTANCE = 2;
	public final static int WEIGHT = 4;

	public MatrixService(PrepareContractionHierarchies pch, CHGraph graph, FlagEncoder encoder) {
		this.pch = pch;
		this.graph = graph;
		this.encoder = encoder;
		this.metricsFlag = 0;
	}

	public MatrixResponse calcMatrix(int[] sources, int[] destinations, int metricsFlag) {
		this.metricsFlag = metricsFlag;
		if (metricsFlag == DURATION) {
			float[] times = new float[sources.length * destinations.length];
			DijkstraBidirectionCHRPHAST algorithm = pch.createRPHAST(graph, encoder);
			// Compute target tree only once as it is the same for every source
			IntObjectMap<SPTEntry> tree = algorithm.createTargetTree(destinations);
			IntObjectMap<SPTEntry> destinationTree = algorithm.calcMatrix(sources[0], destinations, tree, times, 0);

			for (int source = 1; source < sources.length; source++) {
				algorithm = pch.createRPHAST(graph, encoder);
				destinationTree = algorithm.calcMatrix(sources[source], destinations, tree, times,
						source * destinations.length);

			}
			return new MatrixResponse(times);
		}
		if (metricsFlag == DISTANCE) {
			float[] times = new float[sources.length * destinations.length];
			float[] distances = new float[sources.length * destinations.length];
			PropertyExtractorRPHAST extractor = new PropertyExtractorRPHAST(graph, DISTANCE);
			DijkstraBidirectionCHRPHAST algorithm = pch.createRPHAST(graph, encoder);
			// Compute target tree only once as it is the same for every source
			IntObjectMap<SPTEntry> tree = algorithm.createTargetTree(destinations);

			IntObjectMap<SPTEntry> destinationTree = algorithm.calcMatrix(sources[0], destinations, tree, times, 0);
			extractor.setMatrix(destinationTree, destinations);
			extractor.extractProperty(distances, 0);

			for (int source = 1; source < sources.length; source++) {
				algorithm = pch.createRPHAST(graph, encoder);
				destinationTree = algorithm.calcMatrix(sources[source], destinations, tree, times,
						source * destinations.length);
				// destinationTree = algorithm.calcMatrix(sources[source],
				// destinations, times, source * destinations.length);
				extractor.setMatrix(destinationTree, destinations);
				extractor.extractProperty(distances, source * destinations.length);

			}
			return new MatrixResponse(distances);
		}
		if (metricsFlag == (DURATION | DISTANCE)) {
			float[] times = new float[sources.length * destinations.length];
			float[] distances = new float[sources.length * destinations.length];
			PropertyExtractorRPHAST extractor = new PropertyExtractorRPHAST(graph, DISTANCE);
			DijkstraBidirectionCHRPHAST algorithm = pch.createRPHAST(graph, encoder);
			// Compute target tree only once as it is the same for every source
			IntObjectMap<SPTEntry> tree = algorithm.createTargetTree(destinations);

			IntObjectMap<SPTEntry> destinationTree = algorithm.calcMatrix(sources[0], destinations, tree, times, 0);
			extractor.setMatrix(destinationTree, destinations);
			extractor.extractProperty(distances, 0);

			for (int source = 1; source < sources.length; source++) {
				algorithm = pch.createRPHAST(graph, encoder);
				destinationTree = algorithm.calcMatrix(sources[source], destinations, tree, times,
						source * destinations.length);
				extractor.setMatrix(destinationTree, destinations);
				extractor.extractProperty(distances, source * destinations.length);

			}
			return new MatrixResponse(times, distances);
		}

		if (metricsFlag == (DURATION | WEIGHT)) {
			float[] times = new float[sources.length * destinations.length];
			float[] distances = new float[sources.length * destinations.length];
			PropertyExtractorRPHAST extractor = new PropertyExtractorRPHAST(graph, WEIGHT);
			DijkstraBidirectionCHRPHAST algorithm = pch.createRPHAST(graph, encoder);
			// Compute target tree only once as it is the same for every source
			IntObjectMap<SPTEntry> tree = algorithm.createTargetTree(destinations);

			IntObjectMap<SPTEntry> destinationTree = algorithm.calcMatrix(sources[0], destinations, tree, times, 0);
			extractor.setMatrix(destinationTree, destinations);
			extractor.extractProperty(distances, 0);

			for (int source = 1; source < sources.length; source++) {
				algorithm = pch.createRPHAST(graph, encoder);
				destinationTree = algorithm.calcMatrix(sources[source], destinations, tree, times,
						source * destinations.length);
				extractor.setMatrix(destinationTree, destinations);
				extractor.extractProperty(distances, source * destinations.length);

			}
			return new MatrixResponse(times, distances);
		} else
			throw new IllegalArgumentException("Unsupported flag: " + metricsFlag);

	}
}
