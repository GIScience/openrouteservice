package org.heigit.ors.routing.algorithms;

import com.carrotsearch.hppc.IntArrayList;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.Rest;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.PedestrianFlagEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiLabelDijkstraAlgorithmTest {
    private EncodingManager encodingManager;
    private PedestrianFlagEncoder encoder;
    private Weighting weighting;
    private BooleanEncodedValue restEncodedValue;

    @BeforeEach
    void setUp() {
        encodingManager = EncodingManager.create(
                new ORSDefaultFlagEncoderFactory(),
                FlagEncoderNames.PEDESTRIAN_ORS);
        encoder = (PedestrianFlagEncoder) encodingManager.getEncoder(
                FlagEncoderNames.PEDESTRIAN_ORS);
        weighting = new ShortestWeighting(encoder);
        restEncodedValue =
                encoder.getBooleanEncodedValue(encoder + "$" + Rest.KEY);
    }


    @Test
    void penaltyZeroUsesOrdinaryShortestRoute() {
        Path path = createAlgorithm(
                createChoiceGraph(RestPattern.LONG_ROUTE), 100, 0)
                .calcPath(0, 4);

        assertTrue(path.isFound());
        assertEquals(IntArrayList.from(0, 1, 4), path.calcNodes());
        assertEquals(160, path.getDistance(), 1e-6);
        assertEquals(160, path.getWeight(), 1e-6);
    }

    @Test
    void lowPenaltyKeepsShorterRoute() {
        Path path = createAlgorithm(
                createChoiceGraph(RestPattern.LONG_ROUTE), 100, 1)
                .calcPath(0, 4);

        assertEquals(IntArrayList.from(0, 1, 4), path.calcNodes());
        assertEquals(160, path.getDistance(), 1e-6);
        assertEquals(220, path.getWeight(), 1e-6);
    }

    @Test
    void highPenaltyChoosesLongerRouteWithRestPoint() {
        Path path = createAlgorithm(
                createChoiceGraph(RestPattern.LONG_ROUTE), 100, 10)
                .calcPath(0, 4);

        assertEquals(IntArrayList.from(0, 2, 4), path.calcNodes());
        assertEquals(200, path.getDistance(), 1e-6);
        assertEquals(700, path.getWeight(), 1e-6);
    }

    @Test
    void noRestPointsKeepsShortestRouteEvenWithHighPenalty() {
        Path path = createAlgorithm(
                createChoiceGraph(RestPattern.NONE), 100, 10)
                .calcPath(0, 4);

        assertEquals(IntArrayList.from(0, 1, 4), path.calcNodes());
        assertEquals(160, path.getDistance(), 1e-6);
        assertEquals(760, path.getWeight(), 1e-6);
    }

    @Test
    void restPointOnEveryEdgeReducesChoiceToShortestDistance() {
        Path path = createAlgorithm(
                createChoiceGraph(RestPattern.EVERY_EDGE), 100, 10)
                .calcPath(0, 4);

        assertEquals(IntArrayList.from(0, 1, 4), path.calcNodes());
        assertEquals(160, path.getDistance(), 1e-6);
        assertEquals(160, path.getWeight(), 1e-6);
    }

    @Test
    void chargesOnlyNewlyAccumulatedExcessOnEachEdge() {
        GraphHopperStorage graph = createGraph();
        addEdge(graph, 0, 1, 150);
        addEdge(graph, 1, 2, 50);

        Path path = createAlgorithm(graph, 100, 2).calcPath(0, 2);

        assertEquals(200, path.getDistance(), 1e-6);
        assertEquals(400, path.getWeight(), 1e-6);
    }

    @Test
    void restEdgeUsesFullEdgeDistanceForWeightRate() {
        GraphHopperStorage graph = createGraph();
        markRestPoint(addEdge(graph, 0, 1, 200));

        Path path = createAlgorithm(graph, 50, 2).calcPath(0, 1);

        assertEquals(200, path.getDistance(), 1e-6);
        assertEquals(400, path.getWeight(), 1e-6);
    }

    @Test
    void keepsIncomparableLabelsAndChoosesBetterContinuation() {
        GraphHopperStorage graph = createGraph();
        addEdge(graph, 0, 1, 60);
        addEdge(graph, 1, 3, 40);
        markRestPoint(addEdge(graph, 0, 2, 60));
        addEdge(graph, 2, 3, 50);
        addEdge(graph, 3, 4, 50);

        InspectableAlgorithm algorithm = createAlgorithm(graph, 100, 2);
        Path path = algorithm.calcPath(0, 4);

        assertEquals(IntArrayList.from(0, 2, 3, 4), path.calcNodes());
        assertEquals(160, path.getDistance(), 1e-6);
        assertEquals(220, path.getWeight(), 1e-6);

        List<Label> labels = algorithm.getActiveLabels(3);
        assertEquals(2, labels.size());
        assertTrue(labels.stream().anyMatch(
                l -> l.weight == 100 && l.sinceRest == 100));
        assertTrue(labels.stream().anyMatch(
                l -> l.weight == 110 && l.sinceRest == 80));
    }

    @Test
    void equalCandidateDoesNotRemoveExistingLabel() {
        GraphHopperStorage graph = createGraph();
        addEdge(graph, 0, 1, 10);
        addEdge(graph, 0, 1, 10);

        InspectableAlgorithm algorithm = createAlgorithm(graph, 100, 2);
        Path path = algorithm.calcPath(0, 1);

        assertTrue(path.isFound());
        assertEquals(1, algorithm.getActiveLabels(1).size());
        assertTrue(algorithm.getActiveLabels(1).get(0).isActive());
        assertEquals(0, algorithm.getQueueSize());
    }


    @Test
    void skipsInactiveLabelsWhenTheyAreOnlyQueueEntries() {
        GraphHopperStorage graph = createGraph();
        graph.getBaseGraph().getNodeAccess().ensureNode(0);
        InspectableAlgorithm algorithm = createAlgorithm(graph, 100, 2);
        algorithm.setCurrentLabelForTest(Label.createStartLabel(0));
        algorithm.setTargetForTest(99);

        Label inactive = new Label(0, 1, 10, 10);
        inactive.setActive(false);
        algorithm.addToQueue(inactive);
        algorithm.runQueue();

        assertNull(algorithm.getCurrentLabel());
        assertEquals(1, algorithm.getVisitedNodes());
    }

    @Test
    void unreachableDestinationReturnsEmptyPath() {
        GraphHopperStorage graph = createGraph();
        graph.getBaseGraph().getNodeAccess().ensureNode(1);

        Path path = createAlgorithm(graph, 100, 2).calcPath(0, 1);

        assertFalse(path.isFound());
    }


    private GraphHopperStorage createChoiceGraph(RestPattern restPattern) {
        GraphHopperStorage graph = createGraph();
        EdgeIteratorState shortFirst = addEdge(graph, 0, 1, 80);
        EdgeIteratorState shortSecond = addEdge(graph, 1, 4, 80);
        EdgeIteratorState longFirst = addEdge(graph, 0, 2, 100);
        EdgeIteratorState longSecond = addEdge(graph, 2, 4, 100);

        if (restPattern == RestPattern.LONG_ROUTE) {
            markRestPoint(longFirst);
        } else if (restPattern == RestPattern.EVERY_EDGE) {
            markRestPoint(shortFirst);
            markRestPoint(shortSecond);
            markRestPoint(longFirst);
            markRestPoint(longSecond);
        }
        return graph;
    }

    private GraphHopperStorage createGraph() {
        return new GraphBuilder(encodingManager).create();
    }

    private EdgeIteratorState addEdge(
            GraphHopperStorage graph, int from, int to, double distance) {
        EdgeIteratorState edge = graph.edge(from, to).setDistance(distance);
        return GHUtility.setSpeed(5, true, true, encoder, edge);
    }

    private void markRestPoint(EdgeIteratorState edge) {
        IntsRef flags = edge.getFlags();
        restEncodedValue.setBool(false, flags, true);
        edge.setFlags(flags);
    }

    private InspectableAlgorithm createAlgorithm(
            GraphHopperStorage graph, double threshold, double penalty) {
        return new InspectableAlgorithm(graph, weighting, threshold, penalty);
    }

    private enum RestPattern {
        NONE,
        LONG_ROUTE,
        EVERY_EDGE
    }
    private static class InspectableAlgorithm
            extends MultiLabelDijkstraAlgorithm {

        private InspectableAlgorithm(
                GraphHopperStorage graph,
                Weighting weighting,
                double threshold,
                double penalty) {
            super(graph, weighting, TraversalMode.NODE_BASED,
                    threshold, penalty);
        }

        private List<Label> getActiveLabels(int traversalId) {
            return traversalIdToLabelMap.get(traversalId);
        }

        private int getQueueSize() {
            return queue.size();
        }

        private void addToQueue(Label label) {
            queue.add(label);
        }

        private void setCurrentLabelForTest(Label label) {
            currentLabel = label;
        }

        private void setTargetForTest(int target) {
            to = target;
        }

        private void runQueue() {
            runAlgo();
        }

        private Label getCurrentLabel() {
            return currentLabel;
        }
    }

}

