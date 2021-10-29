package org.heigit.ors.matrix;

import com.carrotsearch.hppc.IntHashSet;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import org.heigit.ors.routing.algorithms.SubGraph;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.ExclusiveDownwardSearchEdgeFilter;

import java.util.PriorityQueue;

import static org.heigit.ors.matrix.util.GraphUtils.isCoreNode;

public class TargetGraphBuilder {
    private int coreNodeLevel;
    RoutingCHGraph chGraph;
    private int nodeCount;
    /**
     * Phase I: build shortest path tree from all target nodes to the core, only upwards in level.
     * The EdgeFilter in use is a downward search edge filter with reverse access acceptance so that in the last phase of the algorithm, the targetGraph can be explored downwards
     *
     * @param targets the targets that form the seed for target graph building
     */
    public TargetGraphResults prepareTargetGraph(int[] targets, RoutingCHGraph chGraph, Graph graph, FlagEncoder encoder, boolean swap, int coreNodeLevel) {
        PriorityQueue<Integer> localPrioQueue = new PriorityQueue<>(100);
        ExclusiveDownwardSearchEdgeFilter downwardEdgeFilter = new ExclusiveDownwardSearchEdgeFilter(chGraph, encoder, swap);
        EdgeExplorer edgeExplorer = swap ? graph.createEdgeExplorer(AccessFilter.outEdges(encoder.getAccessEnc()))
                : graph.createEdgeExplorer(AccessFilter.inEdges(encoder.getAccessEnc()));
        SubGraph targetGraph = new SubGraph(graph);
        IntHashSet coreExitPoints = new IntHashSet();

        this.coreNodeLevel = coreNodeLevel;
        this.chGraph = chGraph;
        this.nodeCount = chGraph.getNodes();

        addNodes(targetGraph, localPrioQueue, targets, coreExitPoints);

        while (!localPrioQueue.isEmpty()) {
            int node = localPrioQueue.poll();
            EdgeIterator iter = edgeExplorer.setBaseNode(node);
            downwardEdgeFilter.setBaseNode(node);
            exploreEntry(targetGraph, localPrioQueue, downwardEdgeFilter, node, iter, coreExitPoints);
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
     * @param baseNode
     * @param iter
     */
    private void exploreEntry(SubGraph targetGraph, PriorityQueue<Integer> localPrioQueue, ExclusiveDownwardSearchEdgeFilter downwardEdgeFilter, int baseNode, EdgeIterator iter, IntHashSet coreExitPoints) {
        while (iter.next()) {
            if (!downwardEdgeFilter.accept(iter))
                continue;
            boolean isNewNode = targetGraph.addEdge(baseNode, iter, true);
            int adjNode = iter.getAdjNode();
            if (isCoreNode(chGraph, adjNode, nodeCount, coreNodeLevel))
                coreExitPoints.add(adjNode);
            else if(isNewNode)
                localPrioQueue.add(adjNode);
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
                if (isCoreNode(chGraph, nodeId, nodeCount, coreNodeLevel))
                    coreExitPoints.add(nodeId);
                else
                    prioQueue.add(nodeId);
            }
        }
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
