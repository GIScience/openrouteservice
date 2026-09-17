package org.heigit.ors.routing.algorithms;

import com.graphhopper.routing.AbstractRoutingAlgorithm;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.PathExtractor;
import com.graphhopper.routing.ev.Rest;
import com.graphhopper.routing.querygraph.QueryGraphHelper;
import com.graphhopper.routing.querygraph.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHelper;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import org.apache.log4j.Logger;

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


    public MultiLabelDijkstraAlgorithm(Graph graph, Weighting weighting, TraversalMode tMode, double restThreshold, double penalty) {
        super(graph, weighting, tMode);
        queue = new PriorityQueue<>(INITIAL_CAPACITY);
        this.restThreshold = restThreshold;
        this.penalty = penalty;
        LOGGER.debug("MultiLabelDijkstraAlgorithm initialized with graph size: " + graph.getNodes() + ", weighting: " + weighting.getName() + ", traversal mode: " + tMode + ", rest threshold: " + restThreshold + ", penalty: " + penalty);
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

    private double handleVirtualEdges(VirtualEdgeIteratorState virtualEdge) {
        EdgeIteratorState originalEdge = QueryGraphHelper.getOriginalEdgeFromVirtualEdge(virtualEdge, graph);
        double edgeRestValue = offsetRelativeToBaseNode(originalEdge);
        if (edgeHasRestPoint(edgeRestValue)) {
            double originalEdgeDistance = originalEdge.getDistance();
            double virtualEdgeDistance = virtualEdge.getDistance();
            double restPointDistance = edgeRestValue * originalEdgeDistance;
            if (virtualEdge.getBaseNode() == originalEdge.getBaseNode()) {
                // Before virtual node
                if (restPointDistance < virtualEdgeDistance) {
                    return restPointDistance / virtualEdgeDistance;
                }
            } else {
                // After virtual node
                restPointDistance -= originalEdgeDistance - virtualEdgeDistance;
                if (restPointDistance > 0) {
                    return restPointDistance / virtualEdgeDistance;
                }
            }
        }
        return -1;
    }

    private double offsetRelativeToBaseNode(EdgeIteratorState iter) {
        FootFlagEncoder encoder = (FootFlagEncoder) weighting.getFlagEncoder();
        double edgeRestValue = iter.get(encoder.getDecimalEncodedValue(encoder + "$" + Rest.KEY));
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
