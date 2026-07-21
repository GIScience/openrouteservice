package org.heigit.ors.routing.algorithms;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.AbstractRoutingAlgorithm;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.PathExtractor;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.querygraph.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.Parameters;
import org.apache.log4j.Logger;

import java.util.PriorityQueue;

public class MultiLabelDijkstraAlgorithm extends AbstractRoutingAlgorithm {
    public static final int INITIAL_CAPACITY = 2000;
    private static final Logger LOGGER = Logger.getLogger(MultiLabelDijkstraAlgorithm.class);
    protected IntObjectMap<MultiLabelSPTEntry> fromMap;
    protected PriorityQueue<MultiLabelSPTEntry> fromHeap;
    protected MultiLabelSPTEntry currEdge;
    protected int visitedNodes;
    protected int to = -1;


    public MultiLabelDijkstraAlgorithm(Graph graph, Weighting weighting, TraversalMode tMode) {
        super(graph, weighting, tMode);
        fromHeap = new PriorityQueue<>(INITIAL_CAPACITY);
        fromMap = new GHIntObjectHashMap<>(INITIAL_CAPACITY);
        LOGGER.debug("MultiLabelDijkstraAlgorithm initialized with graph size: " + graph.getNodes() + ", weighting: " + weighting.getName() + ", traversal mode: " + tMode);
    }

    @Override
    public Path calcPath(int from, int to) {
        LOGGER.debug("Calculating path from " + from + " to " + to);
        checkAlreadyRun();
//        this.to = to;
//        currEdge = new SPTEntry(from, 0);
//        if (!traversalMode.isEdgeBased()) {
//            fromMap.put(from, currEdge);
//        }
        runAlgo();
        return extractPath();
    }

    protected void runAlgo() {
        LOGGER.debug("Running MultiLabelDijkstraAlgorithm...");
        while (true) {
            visitedNodes++;
            if (isMaxVisitedNodesExceeded() || finished())
                break;
            break;
//            int currNode = currEdge.adjNode;
//            EdgeIterator iter = edgeExplorer.setBaseNode(currNode);
//            while (iter.next()) {
//                if (!accept(iter, currEdge.edge))
//                    continue;
//
//                // ORS-GH MOD END - use reverseDirection for matrix
//                //double tmpWeight = GHUtility.calcWeightWithTurnWeightWithAccess(weighting, iter, false, currEdge.edge) + currEdge.weight;
//                double tmpWeight = GHUtility.calcWeightWithTurnWeightWithAccess(weighting, iter, reverseDirection, currEdge.edge) + currEdge.weight;
//                // ORS-GH MOD END
//                if (Double.isInfinite(tmpWeight)) {
//                    continue;
//                }
//                // TODO ORS (minor): MARQ24 WHY the heck the 'reverseDirection' is not used also for the traversal ID ???
//                int traversalId = traversalMode.createTraversalId(iter, false);
//
//                SPTEntry nEdge = fromMap.get(traversalId);
//                if (nEdge == null) {
//                    nEdge = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
//                    nEdge.parent = currEdge;
//                    // ORS-GH MOD START
//                    // Modification by Maxim Rylov: Assign the original edge id.
//                    nEdge.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
//                    // ORS-GH MOD END
//                    fromMap.put(traversalId, nEdge);
//                    fromHeap.add(nEdge);
//                } else if (nEdge.weight > tmpWeight) {
//                    fromHeap.remove(nEdge);
//                    nEdge.edge = iter.getEdge();
//                    // ORS-GH MOD START
//                    nEdge.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
//                    // ORS-GH MOD END
//                    nEdge.weight = tmpWeight;
//                    nEdge.parent = currEdge;
//                    fromHeap.add(nEdge);
//                } else
//                    continue;
//
//                updateBestPath(iter, nEdge, traversalId);
//            }
//
//            if (fromHeap.isEmpty())
//                break;
//
//            currEdge = fromHeap.poll();
//            if (currEdge == null)
//                throw new AssertionError("Empty edge cannot happen");
        }
    }

    @Override
    protected boolean finished() {
        return false;
    }

    @Override
    protected Path extractPath() {
//        if (currEdge == null || !finished())
            return createEmptyPath();

//        return MultiLabelPathExtractor.extractPath(graph, weighting, currEdge);
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
