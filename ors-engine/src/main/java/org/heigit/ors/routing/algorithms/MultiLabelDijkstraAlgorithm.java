package org.heigit.ors.routing.algorithms;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.AbstractRoutingAlgorithm;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.GHUtility;
import org.apache.log4j.Logger;

import java.util.PriorityQueue;

public class MultiLabelDijkstraAlgorithm extends AbstractRoutingAlgorithm {
    public static final int INITIAL_CAPACITY = 2000;
    private static final Logger LOGGER = Logger.getLogger(MultiLabelDijkstraAlgorithm.class);
    protected IntObjectMap<Label> fromMap;
    protected PriorityQueue<Label> queue;
    protected Label currentLabel;
    protected int visitedNodes;
    protected int to = -1;


    public MultiLabelDijkstraAlgorithm(Graph graph, Weighting weighting, TraversalMode tMode) {
        super(graph, weighting, tMode);
        queue = new PriorityQueue<>(INITIAL_CAPACITY);
        fromMap = new GHIntObjectHashMap<>(INITIAL_CAPACITY);
        LOGGER.debug("MultiLabelDijkstraAlgorithm initialized with graph size: " + graph.getNodes() + ", weighting: " + weighting.getName() + ", traversal mode: " + tMode);
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
                double adjustedWeight = adjustWeightWithSinceRest(tmpWeight, iter);

                if (nextLabel == null) {
                    nextLabel = new Label(iter.getEdge(), iter.getAdjNode(), adjustedWeight, sinceRest);
                    nextLabel.parent = currentLabel;
                    fromMap.put(traversalId, nextLabel);
                    queue.add(nextLabel);
                } else if (nextLabel.weight > adjustedWeight) {
                    queue.remove(nextLabel);
                    nextLabel.edgeId = iter.getEdge();
                    nextLabel.weight = adjustedWeight;
                    nextLabel.sinceRest = sinceRest;
                    nextLabel.parent = currentLabel;
                    queue.add(nextLabel);
                }
                checkAndPrune(nextLabel);
            }
            if (queue.isEmpty())
                break;

            currentLabel = queue.poll();
            if (currentLabel == null)
                throw new AssertionError("Empty edge cannot happen");
        }
    }

    private void checkAndPrune(Label nextLabel) {
        // TODO: decide if pruning is worth it
        // what we would do: find all labels with same node id, remove dominated
    }

    private double adjustWeightWithSinceRest(double tmpWeight, EdgeIterator iter) {
        return tmpWeight;
    }

    private double calculateNewSinceRest(Label currentLabel, EdgeIterator iter) {
        return 0;
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
