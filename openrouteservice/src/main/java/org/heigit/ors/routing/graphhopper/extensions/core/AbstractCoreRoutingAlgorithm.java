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

import com.graphhopper.routing.*;
import com.graphhopper.routing.ch.CHEntry;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.util.GraphUtils;

/**
 * Calculates best path using core routing algorithm.
 * A core algorithm is separated into phase 1, which is run outside of the core
 * and phase 2, which is run inside the core
 *
 * @author Andrzej Oles
 */

public abstract class AbstractCoreRoutingAlgorithm extends AbstractRoutingAlgorithm {
    protected boolean finishedFrom;
    protected boolean finishedTo;
    int visitedCountFrom1;
    int visitedCountTo1;
    int visitedCountFrom2;
    int visitedCountTo2;

    private CoreDijkstraFilter additionalCoreEdgeFilter;
    protected RoutingCHEdgeExplorer inEdgeExplorer;
    protected RoutingCHEdgeExplorer outEdgeExplorer;

    boolean inCore;

    @Deprecated  protected Weighting turnWeighting;
    protected boolean hasTurnWeighting;
    protected boolean approximate = false;

    protected AbstractCoreRoutingAlgorithm(RoutingCHGraph graph, Weighting weighting) {
        super(graph.getBaseGraph(), weighting, weighting.hasTurnCosts() ? TraversalMode.EDGE_BASED : TraversalMode.NODE_BASED);

        chGraph = graph;

        inEdgeExplorer =  chGraph.createInEdgeExplorer();
        outEdgeExplorer = chGraph.createOutEdgeExplorer();

        // TODO Refactoring: remove this unnecessary duplication
        if (weighting.hasTurnCosts()) {
            hasTurnWeighting = true;
        }

        int size = Math.min(2000, Math.max(200, graph.getNodes() / 10));
        initCollections(size);

        coreNodeLevel = GraphUtils.getBaseGraph(chGraph).getNodes();
        turnRestrictedNodeLevel = coreNodeLevel + 1;
    }

    protected abstract void initCollections(int size);
    protected SPTEntry bestFwdEntry;
    protected SPTEntry bestBwdEntry;
    protected double bestWeight = Double.MAX_VALUE;

    RoutingCHGraph chGraph;
    protected final int coreNodeLevel;
    protected final int turnRestrictedNodeLevel;

    public abstract void initFrom(int from, double weight, long time);

    public abstract void initTo(int to, double weight, long time);

    public abstract boolean fillEdgesFrom();

    public abstract boolean fillEdgesTo();

    /**
     * Stopping criterion for phase outside core
     * @return should stop
     */
    public abstract boolean finishedPhase1();

    /**
     * Begin running of the algorithm phase inside the core
     */
    abstract void runPhase2();

    /**
     * Stopping criterion for phase inside core
     * @return should stop
     */
    public abstract boolean finishedPhase2();

    /**
     * Begin the phase that runs outside of the core
     */
    void runPhase1() {
        while (!finishedPhase1() && !isMaxVisitedNodesExceeded()) {
            if (!finishedFrom)
                finishedFrom = !fillEdgesFrom();

            if (!finishedTo)
                finishedTo = !fillEdgesTo();
        }
    }

    @Override
    protected boolean finished() {
        return finishedPhase2();
    }

    @Override
    protected Path extractPath() {
        if (finished())
            return CorePathExtractor.extractPath(chGraph, weighting, bestFwdEntry, bestBwdEntry, bestWeight);

        return createEmptyPath();
    }

    protected Path createEmptyPath() {
        return new Path(graph.getBaseGraph());
    }


    protected void runAlgo() {
        // PHASE 1: run modified CH outside of core to find entry points
        inCore = false;
        additionalCoreEdgeFilter.setInCore(false);
        runPhase1();

        // PHASE 2 Perform routing in core with the restrictions filter
        initPhase2();
        additionalCoreEdgeFilter.setInCore(true);
        inCore = true;
        runPhase2();
    }

    protected void initPhase2() {}

    @Override
    public Path calcPath(int from, int to, long at) {
        checkAlreadyRun();
        initFrom(from, 0, at);
        initTo(to, 0, at);
        runAlgo();
        return extractPath();
    }

    @Override
    public Path calcPath(int from, int to) {
        return calcPath(from, to, 0);
    }

    @Override
    public int getVisitedNodes() {
        return getVisitedNodesPhase1() + getVisitedNodesPhase2();
    }

    public int getVisitedNodesPhase1() {
        return visitedCountFrom1 + visitedCountTo1;
    }

    public int getVisitedNodesPhase2() {
        return visitedCountFrom2 + visitedCountTo2;
    }

    public RoutingAlgorithm setEdgeFilter(CoreDijkstraFilter additionalEdgeFilter) {
        this.additionalCoreEdgeFilter = additionalEdgeFilter;
        return this;
    }

    //TODO: refactor CoreEdgeFilter to plain EdgeFilter to avoid overriding this method
    protected boolean accept(RoutingCHEdgeIteratorState iter, CHEntry prevOrNextEdgeId, boolean reverse) {
        if (iter.getEdge() == prevOrNextEdgeId.edge)
            return false;
        if (iter.isShortcut())
            return getIncEdge(iter, !reverse) != prevOrNextEdgeId.incEdge;

        return additionalCoreEdgeFilter == null || additionalCoreEdgeFilter.accept(iter);
    }

    int getIncEdge(RoutingCHEdgeIteratorState iter, boolean reverse) {
        if (iter.isShortcut()) {
            return reverse ? iter.getSkippedEdge1() : iter.getSkippedEdge2();
        }
        else {
            return iter.getOrigEdge();
        }
    }

    protected CHEntry createCHEntry(int node, double weight, long time) {
        CHEntry entry = new CHEntry(EdgeIterator.NO_EDGE, EdgeIterator.NO_EDGE, node, weight);
        entry.time = time;
        return entry;
    }

    void updateBestPath(SPTEntry entryCurrent, SPTEntry entryOther, double newWeight, boolean reverse) {
        bestFwdEntry = reverse ? entryOther : entryCurrent;
        bestBwdEntry = reverse ? entryCurrent : entryOther;
        bestWeight = newWeight;
    }

    boolean isCoreNode(int node) {
        return !isVirtualNode(node) && chGraph.getLevel(node) >= coreNodeLevel;
    }

    boolean isVirtualNode(int node) {
        return node >= graph.getBaseGraph().getNodes();//QueryGraph->BaseGraph
    }

    boolean isTurnRestrictedNode(int node) {
        return chGraph.getLevel(node) == turnRestrictedNodeLevel;
    }

    boolean considerTurnRestrictions(int node) {
        if (!hasTurnWeighting)
            return false;
        if (approximate)
            return isTurnRestrictedNode(node);
        return true;
    }

    double calcEdgeWeight(RoutingCHEdgeIteratorState iter, SPTEntry currEdge, boolean reverse) {
        return calcWeight(iter, reverse, currEdge.originalEdge, currEdge.time) + currEdge.getWeightOfVisitedPath();
    }

    double calcWeight(RoutingCHEdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId, long time) {
        double edgeWeight = (edgeState.isShortcut() || !inCore) ?
                edgeState.getWeight(reverse) :
                weighting.calcEdgeWeight(getEdgeIteratorState(edgeState), reverse, time);
        double turnCost = getTurnWeight(prevOrNextEdgeId, edgeState.getBaseNode(), edgeState.getOrigEdge(), reverse);
        return edgeWeight + turnCost;
    }

    double getTurnWeight(int edgeA, int viaNode, int edgeB, boolean reverse) {
        return reverse
                ? chGraph.getTurnWeight(edgeB, viaNode, edgeA)
                : chGraph.getTurnWeight(edgeA, viaNode, edgeB);
    }

    EdgeIteratorState getEdgeIteratorState(RoutingCHEdgeIteratorState edgeState) {
        return graph.getBaseGraph().getEdgeIteratorState(edgeState.getEdge(), edgeState.getAdjNode());
    }

    long calcEdgeTime(RoutingCHEdgeIteratorState iter, SPTEntry currEdge, boolean reverse) {
        return 0;
    }

    long calcTime(RoutingCHEdgeIteratorState edgeState, boolean reverse, long time) {
        return (edgeState.isShortcut() || !inCore) ?
                edgeState.getTime(reverse) :
                weighting.calcEdgeMillis(getEdgeIteratorState(edgeState), reverse, time);
    }
}
