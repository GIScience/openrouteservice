package org.heigit.ors.routing.algorithms;

import com.graphhopper.routing.AbstractRoutingAlgorithm;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.ev.Rest;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;
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

                // TODO ORS (minor): MARQ24 WHY the heck the 'reverseDirection' is not used also for the traversal ID ???
                int traversalId = traversalMode.createTraversalId(iter, false);

                double sinceRest = calculateNewSinceRest(currentLabel, iter);
                double adjustedWeight = adjustWeightWithSinceRest(tmpWeight, nextEdgeWeight, iter, currentLabel.sinceRest);

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

    private double adjustWeightWithSinceRest(double tmpWeight, double edgeWeight, EdgeIterator iter, double sinceRest) {
        double edgeDistance = iter.getDistance();
        if (edgeHasRestPoint(iter)) {
            double distanceToRest = edgeDistance / 2; // TODO: precise point along edge instead of assuming in the middle
            double distanceFromRest = edgeDistance / 2;
            double edgeWeightToRest = edgeWeight / 2;
            double edgeWeightFromRest = edgeWeight / 2;
            return penaltyWeight(sinceRest, distanceToRest, edgeWeightToRest) + penaltyWeight(0, distanceFromRest, edgeWeightFromRest) + tmpWeight;
        }
        return penaltyWeight(sinceRest, edgeDistance, edgeWeight) + tmpWeight;
    }

    private double penaltyWeight(double sinceRest, double edgeDistance, double edgeWeight) {
        double beforeExcess = max(0.0, sinceRest - restThreshold);
        double afterExcess = max(0.0, sinceRest + edgeDistance - restThreshold);
        double newExcess = max(0.0, afterExcess - beforeExcess);
        double weightPerMeter = edgeDistance == 0 ? 0 : edgeWeight / edgeDistance;
        return newExcess * weightPerMeter * penalty;
    }

    private double calculateNewSinceRest(Label currentLabel, EdgeIterator iter) {
        if (edgeHasRestPoint(iter)) {
            return iter.getDistance() / 2;
        } else {
            return currentLabel.sinceRest + iter.getDistance();
        }
    }

    private boolean edgeHasRestPoint(EdgeIterator iter) {
        FootFlagEncoder encoder = (FootFlagEncoder) weighting.getFlagEncoder();
        return iter.get(encoder.getBooleanEncodedValue(encoder + "$" + Rest.KEY));
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
