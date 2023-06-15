package org.heigit.ors.pbt;

import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import net.jqwik.api.*;
import net.jqwik.api.Tuple.Tuple2;
import net.jqwik.api.Tuple.Tuple3;
import net.jqwik.api.domains.DomainContextBase;
import net.jqwik.api.providers.TypeUsage;
import org.heigit.ors.matrix.MatrixLocations;

import java.lang.annotation.*;
import java.util.*;

public class GraphHopperDomain extends DomainContextBase {

    final static CarFlagEncoder carEncoder = new CarFlagEncoder(5, 5.0D, 1);
    final static EncodingManager encodingManager = EncodingManager.create(carEncoder);
    final static Weighting SHORTEST_WEIGHTING_FOR_CARS = new ShortestWeighting(carEncoder);

    @Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface MaxNodes {
        int value();
    }

    public static final int DEFAULT_MAX_NODES = 500;

    @Provide
    Arbitrary<Tuple3<GraphHopperStorage, MatrixLocations, MatrixLocations>> matrixScenarios(TypeUsage typeUsage) {
        Arbitrary<GraphHopperStorage> graphs = graphs(typeUsage);
        return graphs.flatMap(graph -> {
            Set<Integer> nodes = getAllNodes(graph);
            Arbitrary<MatrixLocations> sources = Arbitraries.of(nodes).set().ofMinSize(1).map(this::locations);
            Arbitrary<MatrixLocations> destinations = Arbitraries.of(nodes).set().ofMinSize(1).map(this::locations);
            return Combinators.combine(sources, destinations).as((s, d) -> Tuple.of(graph, s, d));
        });
    }

    @Provide
    Arbitrary<Tuple2<GraphHopperStorage, Tuple2<Integer, Integer>>> routingScenarios(TypeUsage typeUsage) {
        Arbitrary<GraphHopperStorage> graphs = graphs(typeUsage);
        return graphs.flatMap(graph -> {
            Set<Integer> nodes = getAllNodes(graph);
            Arbitrary<Tuple2<Integer, Integer>> pairsOfNodes = Arbitraries.of(nodes).tuple2().filter(t -> !t.get1().equals(t.get2()));
            return pairsOfNodes.map(pair -> Tuple.of(graph, pair));
        });
    }


    @Provide
    Arbitrary<GraphHopperStorage> graphs(TypeUsage typeUsage) {
        Optional<MaxNodes> annotation = typeUsage.findAnnotation(MaxNodes.class);
        int maxNodes = annotation.map(MaxNodes::value).orElse(DEFAULT_MAX_NODES);
        return connectedBidirectionalGraph(maxNodes);
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
            return ((MatrixLocations) o).getNodeIds();
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
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("seed", GraphGenerator.getSeed(graph));
            attributes.put("nodes", graph.getNodes());
            int edgesCount = graph.getEdges();
            attributes.put("edges count", edgesCount);
            if (edgesCount < 20) {
                Map<Integer, String> edges = new HashMap<>();
                AllEdgesIterator edgesIterator = graph.getAllEdges();
                while (edgesIterator.next()) {
                    String edgeString = String.format(
                            "%s->%s: %s",
                            edgesIterator.getBaseNode(),
                            edgesIterator.getAdjNode(),
                            edgesIterator.getDistance()
                    );
                    edges.put(edgesIterator.getEdge(), edgeString);
                }
                attributes.put("edges", edges);
            }
            return attributes;
        }
    }
}
