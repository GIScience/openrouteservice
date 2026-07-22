package org.heigit.ors.routing.algorithms;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
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
import java.util.List;
import java.util.PriorityQueue;

import static java.lang.Math.max;

public class MultiLabelDijkstraAlgorithm extends AbstractRoutingAlgorithm {
    public static final int INITIAL_CAPACITY = 2000;
    private static final Logger LOGGER = Logger.getLogger(MultiLabelDijkstraAlgorithm.class);
    protected IntObjectMap<Label> fromMap;
    protected PriorityQueue<Label> queue;
    protected HashMap<Integer, List<Label>> nodeToLabelMap = new HashMap<>();
    protected Label currentLabel;
    protected int visitedNodes;
    protected int to = -1;
    protected double penalty;
    protected double restThreshold;


    public MultiLabelDijkstraAlgorithm(Graph graph, Weighting weighting, TraversalMode tMode, double restThreshold, double penalty) {
        super(graph, weighting, tMode);
        queue = new PriorityQueue<>(INITIAL_CAPACITY);
        fromMap = new GHIntObjectHashMap<>(INITIAL_CAPACITY);
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
        if (!traversalMode.isEdgeBased()) {
            fromMap.put(from, currentLabel);
        }
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

                double tmpWeight = GHUtility.calcWeightWithTurnWeightWithAccess(weighting, iter, false, currentLabel.edgeId) + currentLabel.weight;
                if (Double.isInfinite(tmpWeight)) {
                    continue;
                }

                // TODO ORS (minor): MARQ24 WHY the heck the 'reverseDirection' is not used also for the traversal ID ???
                int traversalId = traversalMode.createTraversalId(iter, false);

                Label nextLabel = fromMap.get(traversalId);
                double sinceRest = calculateNewSinceRest(currentLabel, iter);
                double adjustedWeight = adjustWeightWithSinceRest(tmpWeight, iter, currentLabel.sinceRest);

                if (nextLabel == null) {
                    nextLabel = new Label(iter.getEdge(), iter.getAdjNode(), adjustedWeight, sinceRest);
                    nextLabel.parent = currentLabel;
                    fromMap.put(traversalId, nextLabel);
                    if (checkAndPrune(nextLabel)) {
                        queue.add(nextLabel);
                        nodeToLabelMap.computeIfAbsent(nextLabel.nodeId, k -> new java.util.ArrayList<>()).add(nextLabel);
                    }
                } else if (nextLabel.weight > adjustedWeight) {
                    queue.remove(nextLabel);
                    nextLabel.edgeId = iter.getEdge();
                    nextLabel.weight = adjustedWeight;
                    nextLabel.sinceRest = sinceRest;
                    nextLabel.parent = currentLabel;
                    if (checkAndPrune(nextLabel)) {
                        queue.add(nextLabel);
                        nodeToLabelMap.computeIfAbsent(nextLabel.nodeId, k -> new java.util.ArrayList<>()).add(nextLabel);
                    }
                }
            }
            if (queue.isEmpty())
                break;

            currentLabel = queue.poll();
            while (currentLabel != null && !currentLabel.isActive()) { // get next Label that is not set to active=false
                currentLabel = queue.poll();
            }
            if (currentLabel == null)
                throw new AssertionError("Empty edge cannot happen");
        }
    }

    private boolean checkAndPrune(Label nextLabel) {
        List<Label> labelsAtNode = nodeToLabelMap.get(nextLabel.nodeId);
        if (labelsAtNode == null) {
            return true;
        }
        labelsAtNode.forEach(existingLabel -> {
            if (existingLabel.weight >= nextLabel.weight && existingLabel.sinceRest >= nextLabel.sinceRest) {
                existingLabel.setActive(false);
            }
        });
        return !labelsAtNode.stream().anyMatch(existingLabel -> existingLabel.weight <= nextLabel.weight && existingLabel.sinceRest <= nextLabel.sinceRest);
    }

    private double adjustWeightWithSinceRest(double tmpWeight, EdgeIterator iter, double sinceRest) {
        if (edgeHasRestPoint(iter)) {
            double distanceToRest = iter.getDistance() / 2;
            return max(0, sinceRest + distanceToRest - restThreshold) * penalty + max(0, distanceToRest - restThreshold) * penalty + tmpWeight;
        }
        return max(0, sinceRest + iter.getDistance() - restThreshold) * penalty + tmpWeight;
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
