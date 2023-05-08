/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.graphhopper.extensions.core;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.predicates.IntPredicate;
import com.graphhopper.coll.MinHeapWithUpdate;
import com.graphhopper.routing.ch.*;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.AbstractAdjustedWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.*;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperStorage;

import static com.graphhopper.routing.ch.CHParameters.*;
import static com.graphhopper.util.Helper.getMemInfo;

/**
 * Prepare the core graph. The core graph is a contraction hierarchies graph in which specified parts are not contracted
 * but remain on the highest level. E.g. used to build the core from restrictions.
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner, Andrzej Oles
 */
public class PrepareCore extends PrepareContractionHierarchies {
    private final EdgeFilter restrictionFilter;
    private boolean [] restrictedNodes;
    private int restrictedNodesCount = 0;

    private static int nodesContractedPercentage = 99;

    IntPredicate isCoreNode = new IntPredicate() {
        public boolean apply(int node) {
            return restrictedNodes[node];
        }
    };

    public PrepareCore(GraphHopperStorage ghStorage, CHConfig chConfig, EdgeFilter restrictionFilter) {
        super(ghStorage, chConfig);
        PMap pMap = new PMap(CONTRACTED_NODES+"="+nodesContractedPercentage);
        setParams(pMap);
        this.restrictionFilter = restrictionFilter;
    }

    @Override
    public CHStorage getCHStore (CHConfig chConfig) {
        if (CHConfig.TYPE_CORE.equals(chConfig.getType()) && graph instanceof ORSGraphHopperStorage) {
            ORSGraphHopperStorage ghStorage = (ORSGraphHopperStorage) graph;
            CHStorage chStore = ghStorage.getCoreStore(chConfig.getName());
            if (chStore == null)
                throw new IllegalArgumentException("There is no Core graph '" + chConfig.getName() + "', existing: " + ghStorage.getCoreGraphNames());
            return chStore;
        }
        return super.getCHStore(chConfig);
    }

    @Override
    public void initFromGraph() {
        // todo: this whole chain of initFromGraph() methods is just needed because PrepareContractionHierarchies does
        // not simply prepare contraction hierarchies, but instead it also serves as some kind of 'container' to give
        // access to the preparations in the GraphHopper class. If this was not so we could make this a lot cleaner here,
        // declare variables final and would not need all these close() methods...
        CorePreparationGraph prepareGraph;
        if (chConfig.getTraversalMode().isEdgeBased()) {
            TurnCostStorage turnCostStorage = graph.getTurnCostStorage();
            if (turnCostStorage == null) {
                throw new IllegalArgumentException("For edge-based CH you need a turn cost storage");
            }
        }
        logger.info("Creating Core graph, {}", getMemInfo());
        prepareGraph = CorePreparationGraph.nodeBased(graph.getNodes(), graph.getEdges());
        nodeContractor = new CoreNodeContractor(prepareGraph, chBuilder, pMap);
        maxLevel = nodes;
        // we need a memory-efficient priority queue with an efficient update method
        // TreeMap is not memory-efficient and PriorityQueue does not support an efficient update method
        // (and is not memory efficient either)
        sortedNodes = new MinHeapWithUpdate(prepareGraph.getNodes());
        logger.info("Building Core graph, {}", getMemInfo());
        StopWatch sw = new StopWatch().start();
        Weighting weighting = new RestrictedEdgesWeighting(chConfig.getWeighting(), restrictionFilter);
        buildFromGraph(prepareGraph, graph, weighting);
        logger.info("Finished building Core graph, took: {}s, {}", sw.stop().getSeconds(), getMemInfo());
        nodeContractor.initFromGraph();
        postInit(prepareGraph);
    }

    public void postInit(CHPreparationGraph prepareGraph) {
        restrictedNodes = new boolean[nodes];
        EdgeExplorer restrictionExplorer;
        restrictionExplorer = graph.createEdgeExplorer(EdgeFilter.ALL_EDGES);//FIXME: each edge is probably unnecessarily visited twice

        for (int node = 0; node < nodes; node++) {
            EdgeIterator edgeIterator = restrictionExplorer.setBaseNode(node);
            while (edgeIterator.next())
                if (!restrictionFilter.accept(edgeIterator))
                    restrictedNodes[node] = restrictedNodes[edgeIterator.getAdjNode()] = true;
        }

        for (int node = 0; node < nodes; node++)
            if (restrictedNodes[node])
                restrictedNodesCount++;
    }

    private class RestrictedEdgesWeighting extends AbstractAdjustedWeighting {
        private final EdgeFilter restrictionFilter;

        RestrictedEdgesWeighting(Weighting weighting, EdgeFilter restrictionFilter) {
            super(weighting);
            this.restrictionFilter = restrictionFilter;
        }

        public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
            if (restrictionFilter.accept(edgeState))
                return superWeighting.calcEdgeWeight(edgeState, reverse);
            else
                return Double.POSITIVE_INFINITY;
        }

        public String getName() {
            return this.superWeighting.getName();
        }
    }

    public static void buildFromGraph(CorePreparationGraph prepareGraph, Graph graph, Weighting weighting) {
        if (graph.getNodes() != prepareGraph.getNodes())
            throw new IllegalArgumentException("Cannot initialize from given graph. The number of nodes does not match: " +
                    graph.getNodes() + " vs. " + prepareGraph.getNodes());
        if (graph.getEdges() != prepareGraph.getOriginalEdges())
            throw new IllegalArgumentException("Cannot initialize from given graph. The number of edges does not match: " +
                    graph.getEdges() + " vs. " + prepareGraph.getOriginalEdges());
        AllEdgesIterator iter = graph.getAllEdges();
        while (iter.next()) {
            double weightFwd = weighting.calcEdgeWeightWithAccess(iter, false);
            // use reverse iterator because restrictionFilter.accept in RestrictedEdgesWeighting cannot be queried in reverse direction
            EdgeIteratorState iterReverse = graph.getEdgeIteratorStateForKey(GHUtility.reverseEdgeKey(iter.getEdgeKey()));
            double weightBwd = weighting.calcEdgeWeightWithAccess(iterReverse, false);
            int timeFwd = Double.isFinite(weightFwd) ? (int) weighting.calcEdgeMillis(iter, false) : Integer.MAX_VALUE;
            int timeBwd = Double.isFinite(weightBwd) ? (int) weighting.calcEdgeMillis(iter, true) : Integer.MAX_VALUE;
            prepareGraph.addEdge(iter.getBaseNode(), iter.getAdjNode(), iter.getEdge(), weightFwd, weightBwd, timeFwd, timeBwd);
        }
        prepareGraph.prepareForContraction();
    }

    @Override
    protected boolean doNotContract(int node) {
        return super.doNotContract(node) || restrictedNodes[node];
    }

    protected IntContainer contractNode(int node, int level) {
        IntContainer neighbors = super.contractNode(node, level);

        if (neighbors instanceof IntArrayList)
            ((IntArrayList) neighbors).removeAll(isCoreNode);
        else
            throw(new IllegalStateException("Not an isntance of IntArrayList"));

        return neighbors;
    }

    @Override
    public void finishContractionHook() {
        chStore.setCoreNodes(sortedNodes.size() + restrictedNodesCount);

        // insert shortcuts connected to core nodes
        CoreNodeContractor coreNodeContractor = (CoreNodeContractor) nodeContractor;
        while (!sortedNodes.isEmpty())
            coreNodeContractor.insertShortcuts(sortedNodes.poll(), false);
        for (int node = 0; node < nodes; node++)
            if (restrictedNodes[node])
                coreNodeContractor.insertShortcuts(node, false);
    }
}
