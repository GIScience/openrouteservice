package org.heigit.ors;

import java.io.*;

import com.graphhopper.routing.util.*;
import com.graphhopper.storage.*;

public class GraphPrinter {

	public static String toDotFile(GraphHopperStorage graph, int... nodesToHighlight) {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("graph g {%n"));
		AllEdgesIterator edgesIterator = graph.getAllEdges();
		for (int node : nodesToHighlight) {
			builder.append(String.format("    %s [color = red];%n", node));
		}
		while (edgesIterator.next()) {
			String edgeString =
				String.format(
					"    %s -- %s [label = %s];%n",
					edgesIterator.getBaseNode(),
					edgesIterator.getAdjNode(),
					edgesIterator.getDistance()
				);
			builder.append(edgeString);
		}
		builder.append(String.format("}%n"));
		return builder.toString();
	}

	// Convert dot file to graph with graphviz, e.g.:
	// `dot -Tgif graph.dot > graph.gif`

	public static void main(String[] args) throws IOException {
		// This example produces a comparison mismatch
		GraphGenerator graphGenerator = new GraphGenerator(2000);
		GraphHopperStorage graph = graphGenerator.create(612342264752893324L);
		String dotFile = toDotFile(graph, 512, 1286);

		// Random random = new Random();
		// GraphGenerator graphGenerator = new GraphGenerator(50);
		// GraphHopperStorage graph = graphGenerator.create(random.nextLong());
		// String dotFile = toDotFile(graph);

		System.out.printf("Generated graph: [nodes: %s, edges: %s]%n", graph.getNodes(), graph.getEdges());
		FileWriter fileWriter = new FileWriter("graph.dot");
		PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.print(dotFile);
		printWriter.close();
	}
}
