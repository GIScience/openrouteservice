package org.heigit.ors.matrix;

import com.carrotsearch.hppc.IntHashSet;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import org.heigit.ors.routing.algorithms.SubGraph;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.ch.DownwardSearchEdgeFilter;

import java.util.PriorityQueue;

public class TargetGraphBuilder {
    private int coreNodeLevel;
    CHGraph chGraph;
    /**
     * Phase I: build shortest path tree from all target nodes to the core, only upwards in level.
     * The EdgeFilter in use is a downward search edge filter with reverse access acceptance so that in the last phase of the algorithm, the targetGraph can be explored downwards
     *
     * @param targets the targets that form the seed for target graph building
     */
    public TargetGraphResults prepareTargetGraph(int[] targets, CHGraph chGraph, Graph graph, FlagEncoder encoder, boolean hasTurnWeighting, boolean swap, int coreNodeLevel) {
        PriorityQueue<Integer> localPrioQueue = new PriorityQueue<>(100);
        DownwardSearchEdgeFilter downwardEdgeFilter = new DownwardSearchEdgeFilter(chGraph, encoder, true, hasTurnWeighting, swap);
        EdgeExplorer edgeExplorer = swap ? graph.createEdgeExplorer(DefaultEdgeFilter.outEdges(encoder)) : graph.createEdgeExplorer(DefaultEdgeFilter.inEdges(encoder));
        SubGraph targetGraph = new SubGraph(graph);
        IntHashSet coreExitPoints = new IntHashSet();

        this.coreNodeLevel = coreNodeLevel;
        this.chGraph = chGraph;
        addNodes(targetGraph, localPrioQueue, targets, coreExitPoints);

        while (!localPrioQueue.isEmpty()) {
            int adjNode = localPrioQueue.poll();
            EdgeIterator iter = edgeExplorer.setBaseNode(adjNode);
            downwardEdgeFilter.setBaseNode(adjNode);
            exploreEntry(targetGraph, localPrioQueue, downwardEdgeFilter, adjNode, iter, coreExitPoints);
        }
        TargetGraphResults targetGraphResults = new TargetGraphResults();
        targetGraphResults.setTargetGraph(targetGraph);
        targetGraphResults.setCoreExitPoints(coreExitPoints);
        return targetGraphResults;
    }

    /**
     * Explore the target graph and build coreExitPoints
     * @param localPrioQueue
     * @param downwardEdgeFilter
     * @param adjNode
     * @param iter
     */
    private void exploreEntry(SubGraph targetGraph, PriorityQueue<Integer> localPrioQueue, DownwardSearchEdgeFilter downwardEdgeFilter, int adjNode, EdgeIterator iter, IntHashSet coreExitPoints) {
        while (iter.next()) {
            if (!downwardEdgeFilter.accept(iter))
                continue;
            boolean isNewNode = targetGraph.addEdge(adjNode, iter, true);
            if (isCoreNode(iter.getAdjNode()) && !isCoreNode(iter.getBaseNode())) {
                coreExitPoints.add(iter.getAdjNode());
            } else if(isNewNode) {
                localPrioQueue.add(iter.getAdjNode());
            }
        }
    }

    /**
     * Add nodes to target graph and prioQueue for target graph
     * @param graph
     * @param prioQueue
     * @param nodes
     */
    private void addNodes(SubGraph graph, PriorityQueue<Integer> prioQueue, int[] nodes, IntHashSet coreExitPoints) {
        for (int i = 0; i < nodes.length; i++) {
            int nodeId = nodes[i];
            if (nodeId >= 0) {
                if (graph != null)
                    graph.addEdge(nodeId, null, true);
                prioQueue.add(nodeId);
                if (isCoreNode(nodeId)) {
                    coreExitPoints.add(nodeId);
                }
            }
        }
    }

    boolean isCoreNode(int node) {
        return chGraph.getLevel(node) >= coreNodeLevel;
    }

    public class TargetGraphResults {
        SubGraph targetGraph;
        IntHashSet coreExitPoints;

        public SubGraph getTargetGraph() {
            return targetGraph;
        }

        public void setTargetGraph(SubGraph targetGraph) {
            this.targetGraph = targetGraph;
        }

        public IntHashSet getCoreExitPoints() {
            return coreExitPoints;
        }

        public void setCoreExitPoints(IntHashSet coreExitPoints) {
            this.coreExitPoints = coreExitPoints;
        }
    }
}
