package org.heigit.ors.routing.algorithms;

import com.graphhopper.routing.AbstractRoutingAlgorithm;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.PathExtractor;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraphHelper;
import com.graphhopper.routing.querygraph.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHelper;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.parameters.RestParameters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import static java.lang.Math.max;

public class MultiLabelDijkstraAlgorithm extends AbstractRoutingAlgorithm {
    public static final int INITIAL_CAPACITY = 2000;
    private static final Logger LOGGER = Logger.getLogger(MultiLabelDijkstraAlgorithm.class);
    protected PriorityQueue<Label> queue;
    protected HashMap<Integer, List<Label>> traversalIdToLabelMap = new HashMap<>();
    protected Label currentLabel;
    protected int visitedNodes;
    protected int to = -1;
    protected double penalty;
    protected double restThreshold;
    private final DecimalEncodedValue restEnc;
    private final EdgeExplorer virtualPieceExplorer;

    public MultiLabelDijkstraAlgorithm(Graph graph, Weighting weighting, DecimalEncodedValue restEnc, TraversalMode tMode, double restThreshold, double penalty) {
        checkRestArguments(restEnc, restThreshold, penalty);
        super(graph, weighting, tMode);
        this.restEnc = restEnc;
        virtualPieceExplorer = graph.createEdgeExplorer();
        queue = new PriorityQueue<>(INITIAL_CAPACITY);
        this.restThreshold = restThreshold;
        this.penalty = penalty;
        LOGGER.debug("MultiLabelDijkstraAlgorithm initialized with graph size: " + graph.getNodes() + ", weighting: " + weighting.getName() + ", traversal mode: " + tMode + ", rest threshold: " + restThreshold + ", penalty: " + penalty);
    }

    private static void checkRestArguments(DecimalEncodedValue restEnc, double restThreshold, double penalty) {
        if (restEnc == null)
            throw new IllegalArgumentException("Rest point routing needs the rest encoded value");
        if (restThreshold <= 0)
            throw new IllegalArgumentException("Rest threshold must be positive: " + restThreshold);
        if (!RestParameters.isValidPenalty(penalty))
            throw new IllegalArgumentException("Rest penalty must be a finite number of zero or more: " + penalty);
    }

    @Override
    public Path calcPath(int from, int to) {
        LOGGER.debug("Calculating path from " + from + " to " + to);
        checkAlreadyRun();
        this.to = to;
        currentLabel = Label.createStartLabel(from);
        runAlgo();
        return extractPath();
    }

    protected void runAlgo() {
        LOGGER.debug("Running MultiLabelDijkstraAlgorithm...");
        while (!finished() && !isMaxVisitedNodesExceeded()) {
            visitedNodes++;

            EdgeIterator iter = edgeExplorer.setBaseNode(currentLabel.adjNode);
            while (iter.next()) {
                if (accept(iter, currentLabel.edge))
                    processEdge(iter);
            }
            do {
                currentLabel = queue.poll();
            }
            while (currentLabel != null && !currentLabel.isActive());// get next Label that is not set to active=false
            if (currentLabel == null)
                break;
        }
    }

    private void processEdge(EdgeIterator iter) {
        double nextEdgeWeight = GHUtility.calcWeightWithTurnWeightWithAccess(weighting, iter, false, currentLabel.edge);
        double tmpWeight = nextEdgeWeight + currentLabel.weight;
        if (Double.isInfinite(tmpWeight)) {
            return;
        }

        int traversalId = traversalMode.createTraversalId(iter, false);

        double edgeRestValue = getEdgeRestValue(iter);
        double sinceRest = calculateNewSinceRest(currentLabel, iter, edgeRestValue);
        double adjustedWeight = adjustWeightWithSinceRest(tmpWeight, nextEdgeWeight, iter, currentLabel.sinceRest, edgeRestValue);

        Label nextLabel = new Label(iter.getEdge(), iter.getAdjNode(), adjustedWeight, sinceRest);
        nextLabel.parent = currentLabel;
        checkAndPrune(nextLabel, traversalId);
    }

    private void checkAndPrune(Label nextLabel, int traversalId) {
        List<Label> labelsAtNode = traversalIdToLabelMap.computeIfAbsent(traversalId, k -> new java.util.ArrayList<>());
        for (Label existingLabel : labelsAtNode) {
            if (dominates(existingLabel, nextLabel)) {
                nextLabel.setActive(false);
                return;
            }
        }
        Iterator<Label> i = labelsAtNode.iterator();
        while (i.hasNext()) {
            Label existingLabel = i.next();
            if (dominates(nextLabel, existingLabel)) {
                existingLabel.setActive(false);
                i.remove();
            }
        }
        labelsAtNode.add(nextLabel);
        queue.add(nextLabel);
    }

    private boolean dominates(Label aLabel, Label bLabel) {
        return aLabel.weight <= bLabel.weight && aLabel.sinceRest <= bLabel.sinceRest;
    }

    private double adjustWeightWithSinceRest(double tmpWeight, double edgeWeight, EdgeIterator iter, double sinceRest, double toRestFactor) {
        double edgeDistance = iter.getDistance();
        if (edgeHasRestPoint(toRestFactor)) {
            double fromRestFactor = 1 - toRestFactor;
            double distanceToRest = edgeDistance * toRestFactor;
            double distanceFromRest = edgeDistance * fromRestFactor;
            double edgeWeightToRest = edgeWeight * toRestFactor;
            double edgeWeightFromRest = edgeWeight * fromRestFactor;
            return penaltyWeight(sinceRest, distanceToRest, edgeWeightToRest) + penaltyWeight(0, distanceFromRest, edgeWeightFromRest) + tmpWeight;
        }
        return penaltyWeight(sinceRest, edgeDistance, edgeWeight) + tmpWeight;
    }

    private double penaltyWeight(double sinceRest, double edgeDistance, double edgeWeight) {
        double excessBeforeEdge = max(0.0, sinceRest - restThreshold);
        double excessAfterEdge = max(0.0, sinceRest + edgeDistance - restThreshold);
        double additionalExcess = max(0.0, excessAfterEdge - excessBeforeEdge);
        double weightPerMeter = edgeDistance == 0 ? 0 : edgeWeight / edgeDistance;
        return additionalExcess * weightPerMeter * penalty;
    }

    private static double calculateNewSinceRest(Label currentLabel, EdgeIterator iter, double edgeRestValue) {
        if (edgeHasRestPoint(edgeRestValue)) {
            return iter.getDistance() * (1 - edgeRestValue);
        } else {
            return currentLabel.sinceRest + iter.getDistance();
        }
    }

    private double getEdgeRestValue(EdgeIterator iter) {
        EdgeIteratorState edgeIteratorState = iter.detach(false);
        if (edgeIteratorState instanceof VirtualEdgeIteratorState virtualEdgeIteratorState) {
            return handleVirtualEdges(virtualEdgeIteratorState);
        } else {
            return offsetRelativeToBaseNode(edgeIteratorState);
        }
    }

    /**
     * Maps the rest point of the original edge onto one virtual piece of it. Snapped points split the original edge
     * into a chain of virtual pieces; the piece may lie anywhere in that chain, including between two snapped points.
     *
     * @return the rest point's position on the piece as a fraction from its base node, or -1 if it lies outside
     */
    private double handleVirtualEdges(VirtualEdgeIteratorState virtualEdge) {
        EdgeIteratorState originalEdge = QueryGraphHelper.getOriginalEdgeFromVirtualEdge(virtualEdge, graph);
        double edgeRestValue = offsetRelativeToBaseNode(originalEdge);
        if (!edgeHasRestPoint(edgeRestValue))
            return -1;
        double pieceDistance = virtualEdge.getDistance();
        double distanceBefore = distanceToOriginalNode(virtualEdge.getBaseNode(), virtualEdge.getEdge());
        double distanceAfter = distanceToOriginalNode(virtualEdge.getAdjNode(), virtualEdge.getEdge());
        double restPointDistance = edgeRestValue * (distanceBefore + pieceDistance + distanceAfter) - distanceBefore;
        if (restPointDistance < 0 || restPointDistance > pieceDistance)
            return -1;
        return restPointDistance / pieceDistance;
    }

    /**
     * Sums the virtual pieces from a node to the end of the original edge, walking away from the given piece.
     */
    private double distanceToOriginalNode(int node, int pieceEdge) {
        int firstVirtualNode = graph.getBaseGraph().getNodes();
        int currentNode = node;
        int previousEdge = pieceEdge;
        double distance = 0;
        while (currentNode >= firstVirtualNode) {
            EdgeIterator nextPiece = nextVirtualPiece(currentNode, previousEdge);
            distance += nextPiece.getDistance();
            previousEdge = nextPiece.getEdge();
            currentNode = nextPiece.getAdjNode();
        }
        return distance;
    }

    /**
     * A virtual node has exactly two edges, one towards each end of the original edge; returns the one not yet walked.
     */
    private EdgeIterator nextVirtualPiece(int virtualNode, int previousEdge) {
        EdgeIterator iter = virtualPieceExplorer.setBaseNode(virtualNode);
        iter.next();
        if (iter.getEdge() == previousEdge)
            iter.next();
        return iter;
    }

    private double offsetRelativeToBaseNode(EdgeIteratorState iter) {
        double edgeRestValue = iter.get(restEnc);
        if (edgeHasRestPoint(edgeRestValue) && GraphHelper.isReversed(iter)) {
            edgeRestValue = 1 - edgeRestValue;
        }
        return edgeRestValue;
    }

    private static boolean edgeHasRestPoint(double edgeRestValue) {
        return edgeRestValue != -1;
    }

    @Override
    protected boolean finished() {
        return currentLabel.adjNode == to;
    }

    @Override
    protected Path extractPath() {
        if (currentLabel == null || !finished())
            return createEmptyPath();
        return PathExtractor.extractPath(graph, weighting, currentLabel);
    }

    @Override
    public int getVisitedNodes() {
        return visitedNodes;
    }

    @Override
    public String getName() {
        return "multi_label_dijkstra";
    }
}
