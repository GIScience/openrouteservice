package org.heigit.ors.routing.algorithms;

import com.graphhopper.routing.AbstractRoutingAlgorithm;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.ev.Rest;
import com.graphhopper.routing.querygraph.QueryGraphHelper;
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
import static org.heigit.ors.routing.graphhopper.extensions.flagencoders.FootFlagEncoder.REST_ENCODER_OFFSET;

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
        while (true) {
            visitedNodes++;
            if (isMaxVisitedNodesExceeded() || finished())
                break;

            int currNode = currentLabel.nodeId;
            EdgeIterator iter = edgeExplorer.setBaseNode(currNode);
            while (iter.next()) {
                if (!accept(iter, currentLabel.edgeId))
                    continue;

                double nextEdgeWeight = GHUtility.calcWeightWithTurnWeightWithAccess(weighting, iter, false, currentLabel.edgeId);
                double tmpWeight = nextEdgeWeight + currentLabel.weight;
                if (Double.isInfinite(tmpWeight)) {
                    continue;
                }

                int traversalId = traversalMode.createTraversalId(iter, false);

                double edgeRestValue = getEdgeRestValue(iter);
                double sinceRest = calculateNewSinceRest(currentLabel, iter, edgeRestValue);
                double adjustedWeight = adjustWeightWithSinceRest(tmpWeight, nextEdgeWeight, iter, currentLabel.sinceRest, edgeRestValue);

                Label nextLabel = new Label(iter.getEdge(), iter.getAdjNode(), adjustedWeight, sinceRest);
                nextLabel.parent = currentLabel;
                checkAndPrune(nextLabel, traversalId);
            }
            if (queue.isEmpty())
                break;

            currentLabel = queue.poll();
            while (currentLabel != null && !currentLabel.isActive()) { // get next Label that is not set to active=false
                currentLabel = queue.poll();
            }
            if (currentLabel == null)
                break;
        }
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

    private double adjustWeightWithSinceRest(double tmpWeight, double edgeWeight, EdgeIterator iter, double sinceRest, double edgeRestValue) {
        double edgeDistance = iter.getDistance();
        if (edgeHasRestPoint(edgeRestValue)) {
            double toRestFactor = edgeToRestFactor(edgeRestValue);
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
            return iter.getDistance() * (1 - edgeToRestFactor(edgeRestValue));
        } else {
            return currentLabel.sinceRest + iter.getDistance();
        }
    }

    private double getEdgeRestValue(EdgeIterator iter) {
        EdgeIteratorState originalEdge = QueryGraphHelper.getOriginalEdgeFromVirtualEdge(iter, graph);
        if (originalEdge != null) {
            return handleVirtualEdges(iter, originalEdge);
        } else {
            return offsetRelativeToBaseNode(iter);
        }
    }

    private double handleVirtualEdges(EdgeIterator iter, EdgeIteratorState originalEdge) {
        double originalEdgeDistance = originalEdge.getDistance();
        double originalEdgeValue = offsetRelativeToBaseNode(iter);
        double restPointDistance = edgeToRestFactor(originalEdgeValue) * originalEdgeDistance;
        double virtualEdgeDistance = iter.getDistance();
        if (edgeHasRestPoint(originalEdgeValue)) {
            if (iter.getBaseNode() == originalEdge.getBaseNode()) {
                // Before virtual node
                if (virtualEdgeDistance > restPointDistance) {
                    return restPointDistance / virtualEdgeDistance + REST_ENCODER_OFFSET;
                }
            } else {
                // After virtual node
                double otherVirtualEdgeDistance = originalEdgeDistance - virtualEdgeDistance;
                if (otherVirtualEdgeDistance < restPointDistance) {
                    return (restPointDistance - otherVirtualEdgeDistance) / virtualEdgeDistance + REST_ENCODER_OFFSET;
                }
            }
        }
        return 0;
    }

    private double offsetRelativeToBaseNode(EdgeIterator iter) {
        FootFlagEncoder encoder = (FootFlagEncoder) weighting.getFlagEncoder();
        double edgeRestValue = iter.get(encoder.getDecimalEncodedValue(encoder + "$" + Rest.KEY));
        if (edgeRestValue > 0 && GraphHelper.isReversed(iter)) {
            edgeRestValue = 1 + 2*REST_ENCODER_OFFSET - edgeRestValue;
        }
        return edgeRestValue;
    }

    private static boolean edgeHasRestPoint(double edgeRestValue) {
        return edgeRestValue > 0;
    }

    private static double edgeToRestFactor(double edgeRestValue) {
        return edgeRestValue - REST_ENCODER_OFFSET;
    }

    @Override
    protected boolean finished() {
        return currentLabel.nodeId == to;
    }

    @Override
    protected Path extractPath() {
        if (currentLabel == null || !finished())
            return createEmptyPath();
        return MultiLabelPathExtractor.extract(graph, weighting, currentLabel);
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
