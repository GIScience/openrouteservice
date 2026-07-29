package org.heigit.ors.routing.algorithms;

import com.carrotsearch.hppc.IntArrayList;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.Rest;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.PedestrianFlagEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.heigit.ors.routing.graphhopper.extensions.flagencoders.FootFlagEncoder.REST_ENCODER_OFFSET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultiLabelDijkstraAlgorithmTest {
    private static final double EPSILON = 1e-6;

    private EncodingManager encodingManager;
    private PedestrianFlagEncoder encoder;
    private Weighting weighting;
    private DecimalEncodedValue restEncodedValue;

    @BeforeEach
    void setUp() {
        encodingManager = EncodingManager.create(
                new ORSDefaultFlagEncoderFactory(),
                FlagEncoderNames.PEDESTRIAN_ORS);
        encoder = (PedestrianFlagEncoder) encodingManager.getEncoder(
                FlagEncoderNames.PEDESTRIAN_ORS);
        weighting = new ShortestWeighting(encoder);
        restEncodedValue =
                encoder.getDecimalEncodedValue(encoder + "$" + Rest.KEY);
    }

    @Test
    @Order(1)
    @DisplayName("1. A bench stored as zero at an edge base node resets fatigue")
    void baseNodeRestStoredAsZeroMustResetFatigue() {
        GraphHopperStorage graph = createGraph();
        addEdge(graph, 0, 1, 100);
        EdgeIteratorState secondEdge = addEdge(graph, 1, 2, 100);

        // This reproduces the current ORSGraphHopper import value for a bench
        // snapped exactly to the base node of an edge.
        markRestPoint(secondEdge, 0);

        Path path = createAlgorithm(graph, 100, 2).calcPath(0, 2);

        assertTrue(path.isFound());
        assertEquals(200, path.getDistance(), EPSILON);
        assertEquals(200, path.getWeight(), EPSILON);
    }

    @Test
    @Order(2)
    @DisplayName("2. Penalty zero reproduces ordinary shortest routing")
    void penaltyZeroUsesOrdinaryShortestRoute() {
        Path path = createAlgorithm(
                createChoiceGraph(RestPattern.LONG_ROUTE), 100, 0)
                .calcPath(0, 4);

        assertTrue(path.isFound());
        assertEquals(IntArrayList.from(0, 1, 4), path.calcNodes());
        assertEquals(160, path.getDistance(), EPSILON);
        assertEquals(160, path.getWeight(), EPSILON);
    }

    @Test
    @Order(3)
    @DisplayName("3. A low penalty keeps the shorter route")
    void lowPenaltyKeepsShorterRoute() {
        Path path = createAlgorithm(
                createChoiceGraph(RestPattern.LONG_ROUTE), 100, 1)
                .calcPath(0, 4);

        assertEquals(IntArrayList.from(0, 1, 4), path.calcNodes());
        assertEquals(160, path.getDistance(), EPSILON);
        assertEquals(220, path.getWeight(), EPSILON);
    }

    @Test
    @Order(4)
    @DisplayName("4. A high penalty selects the longer rested route")
    void highPenaltyChoosesLongerRouteWithRestPoint() {
        Path path = createAlgorithm(
                createChoiceGraph(RestPattern.LONG_ROUTE), 100, 10)
                .calcPath(0, 4);

        assertEquals(IntArrayList.from(0, 2, 4), path.calcNodes());
        assertEquals(200, path.getDistance(), EPSILON);
        assertEquals(700, path.getWeight(), EPSILON);
    }

    @Test
    @Order(5)
    @DisplayName("5. Without rest points, the shortest route remains optimal")
    void noRestPointsKeepsShortestRouteEvenWithHighPenalty() {
        Path path = createAlgorithm(
                createChoiceGraph(RestPattern.NONE), 100, 10)
                .calcPath(0, 4);

        assertEquals(IntArrayList.from(0, 1, 4), path.calcNodes());
        assertEquals(160, path.getDistance(), EPSILON);
        assertEquals(760, path.getWeight(), EPSILON);
    }

    @Test
    @Order(6)
    @DisplayName("6. Rest points on every edge reduce routing to shortest distance")
    void restPointOnEveryEdgeReducesChoiceToShortestDistance() {
        Path path = createAlgorithm(
                createChoiceGraph(RestPattern.EVERY_EDGE), 100, 10)
                .calcPath(0, 4);

        assertEquals(IntArrayList.from(0, 1, 4), path.calcNodes());
        assertEquals(160, path.getDistance(), EPSILON);
        assertEquals(160, path.getWeight(), EPSILON);
    }

    @Test
    @Order(7)
    @DisplayName("7. Excess distance is charged incrementally")
    void chargesOnlyNewlyAccumulatedExcessOnEachEdge() {
        GraphHopperStorage graph = createGraph();
        addEdge(graph, 0, 1, 150);
        addEdge(graph, 1, 2, 50);

        Path path = createAlgorithm(graph, 100, 2).calcPath(0, 2);

        assertEquals(200, path.getDistance(), EPSILON);
        assertEquals(400, path.getWeight(), EPSILON);
    }

    @Test
    @Order(8)
    @DisplayName("8. The stored quarter-edge position controls fatigue and weight")
    void storedQuarterPositionControlsWeightAndSinceRest() {
        GraphHopperStorage graph = createGraph();
        markRestPoint(addEdge(graph, 0, 1, 200), 0.25);
        InspectableAlgorithm algorithm = createAlgorithm(graph, 100, 2);

        Path path = algorithm.calcPath(0, 1);

        assertEquals(200, path.getDistance(), EPSILON);
        assertEquals(300, path.getWeight(), EPSILON);
        Label targetLabel = algorithm.getActiveLabels(1).get(0);
        assertEquals(150, targetLabel.sinceRest, EPSILON);
    }

    @Test
    @Order(9)
    @DisplayName("9. Incomparable labels survive until the better continuation is known")
    void keepsIncomparableLabelsAndChoosesBetterContinuation() {
        GraphHopperStorage graph = createGraph();
        addEdge(graph, 0, 1, 60);
        addEdge(graph, 1, 3, 40);
        markRestPoint(addEdge(graph, 0, 2, 60), 0.5);
        addEdge(graph, 2, 3, 50);
        addEdge(graph, 3, 4, 50);

        InspectableAlgorithm algorithm = createAlgorithm(graph, 100, 2);
        Path path = algorithm.calcPath(0, 4);

        assertEquals(IntArrayList.from(0, 2, 3, 4), path.calcNodes());
        assertEquals(160, path.getDistance(), EPSILON);
        assertEquals(220, path.getWeight(), EPSILON);

        List<Label> labels = algorithm.getActiveLabels(3);
        assertEquals(2, labels.size());
        assertTrue(labels.stream().anyMatch(
                label -> closeTo(label.weight, 100) && closeTo(label.sinceRest, 100)));
        assertTrue(labels.stream().anyMatch(
                label -> closeTo(label.weight, 110) && closeTo(label.sinceRest, 80)));
    }

    @Test
    @Order(10)
    @DisplayName("10. An equal candidate does not remove the surviving label")
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
    @Order(11)
    @DisplayName("11. Inactive queue labels are skipped")
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
    @Order(12)
    @DisplayName("12. An unreachable destination returns an empty path")
    void unreachableDestinationReturnsEmptyPath() {
        GraphHopperStorage graph = createGraph();
        graph.getBaseGraph().getNodeAccess().ensureNode(1);

        Path path = createAlgorithm(graph, 100, 2).calcPath(0, 1);

        assertFalse(path.isFound());
    }

    @Test
    @Order(13)
    @DisplayName("13. Reverse routing mirrors the stored bench position")
    void reverseRoutingMirrorsStoredBenchPosition() {
        GraphHopperStorage graph = createGraph();
        markRestPoint(addEdge(graph, 0, 1, 200), 0.25);

        Path path = createAlgorithm(graph, 100, 2).calcPath(1, 0);

        assertTrue(path.isFound());
        assertEquals(IntArrayList.from(1, 0), path.calcNodes());
        assertEquals(200, path.getDistance(), EPSILON);
        assertEquals(300, path.getWeight(), EPSILON);
    }

    @Test
    @Order(14)
    @DisplayName("14. Routing to an interior point preserves a bench on the first segment")
    void routeToInteriorPointUsesBenchOnFirstVirtualSegment() {
        GraphHopperStorage graph = createGraph();
        graph.getNodeAccess().setNode(0, 0, 0);
        graph.getNodeAccess().setNode(1, 0, 0.0018);
        markRestPoint(addEdge(graph, 0, 1, 200), 0.25);
        LocationIndexTree index =
                new LocationIndexTree(graph, new RAMDirectory());
        index.prepareIndex();
        Snap snap = index.findClosest(
                0, 0.00072, EdgeFilter.ALL_EDGES);
        QueryGraph queryGraph = QueryGraph.create(graph, snap);

        Path path = createAlgorithm(queryGraph, 40, 2)
                .calcPath(0, snap.getClosestNode());

        assertTrue(path.isFound());
        assertEquals(80, path.getDistance(), 0.2);
        assertEquals(100, path.getWeight(), 0.2);
    }

    private GraphHopperStorage createChoiceGraph(RestPattern restPattern) {
        GraphHopperStorage graph = createGraph();
        EdgeIteratorState shortFirst = addEdge(graph, 0, 1, 80);
        EdgeIteratorState shortSecond = addEdge(graph, 1, 4, 80);
        EdgeIteratorState longFirst = addEdge(graph, 0, 2, 100);
        EdgeIteratorState longSecond = addEdge(graph, 2, 4, 100);

        if (restPattern == RestPattern.LONG_ROUTE) {
            markRestPoint(longFirst, 0.5);
        } else if (restPattern == RestPattern.EVERY_EDGE) {
            markRestPoint(shortFirst, 0.5);
            markRestPoint(shortSecond, 0.5);
            markRestPoint(longFirst, 0.5);
            markRestPoint(longSecond, 0.5);
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

    private void markRestPoint(EdgeIteratorState edge, double position) {
        setStoredRestValue(edge, position + REST_ENCODER_OFFSET);
    }

    private void setStoredRestValue(
            EdgeIteratorState edge, double storedValue) {
        IntsRef flags = edge.getFlags();
        restEncodedValue.setDecimal(false, flags, storedValue);
        edge.setFlags(flags);
    }

    private InspectableAlgorithm createAlgorithm(
            Graph graph, double threshold, double penalty) {
        return new InspectableAlgorithm(graph, weighting, threshold, penalty);
    }

    private static boolean closeTo(double actual, double expected) {
        return Math.abs(actual - expected) < EPSILON;
    }

    private enum RestPattern {
        NONE,
        LONG_ROUTE,
        EVERY_EDGE
    }

    private static class InspectableAlgorithm
            extends MultiLabelDijkstraAlgorithm {

        private InspectableAlgorithm(
                Graph graph,
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
