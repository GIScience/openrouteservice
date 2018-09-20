/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper GmbH licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions.core;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.*;
import com.graphhopper.routing.ch.Path4CH;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.*;

import java.util.PriorityQueue;

/**
 * Calculates best path using core routing algorithm.
 *
 * @author Andrzej Oles
 */

public class CoreRouting extends AbstractRoutingAlgorithm {
    protected boolean finishedFrom;
    protected boolean finishedTo;
    int visitedCountFrom;
    int visitedCountTo;

    protected IntObjectMap<SPTEntry> bestWeightMapFrom;
    protected IntObjectMap<SPTEntry> bestWeightMapTo;
    protected IntObjectMap<SPTEntry> bestWeightMapOther;
    protected SPTEntry currFrom;
    protected SPTEntry currTo;
    protected PathBidirRef bestPath;
    private PriorityQueue<SPTEntry> pqCHFrom;
    private PriorityQueue<SPTEntry> pqCHTo;
    private boolean updateBestPath = true;

    CHGraph chGraph;
    int coreNodeLevel;

    private CoreDijkstraFilter additionalEdgeFilter;

    private PriorityQueue<SPTEntry> pqCoreFrom;
    private PriorityQueue<SPTEntry> pqCoreTo;

    boolean inCore;

    public CoreRouting(Graph graph, Weighting weighting, TraversalMode tMode, double maxSpeed) {
        super(graph, weighting, tMode, maxSpeed);

        int size = Math.min(2000, Math.max(200, graph.getNodes() / 10));
        initCollections(size);

        chGraph = (CHGraph) ((QueryGraph) graph).getMainGraph();
        coreNodeLevel = chGraph.getNodes() + 1;
    }

    protected void initCollections(int size) {
        pqCHFrom = new PriorityQueue<SPTEntry>(size);
        bestWeightMapFrom = new GHIntObjectHashMap<SPTEntry>(size);

        pqCHTo = new PriorityQueue<SPTEntry>(size);
        bestWeightMapTo = new GHIntObjectHashMap<SPTEntry>(size);

        pqCoreFrom = new PriorityQueue<>(size);
        pqCoreTo = new PriorityQueue<>(size);
    }

    public void initFrom(int from, double weight) {
        currFrom = createSPTEntry(from, weight);
        pqCHFrom.add(currFrom);
        if (!traversalMode.isEdgeBased()) {
            bestWeightMapFrom.put(from, currFrom);
            if (currTo != null) {
                bestWeightMapOther = bestWeightMapTo;
                updateBestPath(GHUtility.getEdge(graph, from, currTo.adjNode), currTo, from);
            }
        } else if (currTo != null && currTo.adjNode == from) {
            // special case of identical start and end
            bestPath.setSPTEntry(currFrom);
            bestPath.setSPTEntryTo(currTo);
            finishedFrom = true;
            finishedTo = true;
        }
    }

    public void initTo(int to, double weight) {
        currTo = createSPTEntry(to, weight);
        pqCHTo.add(currTo);
        if (!traversalMode.isEdgeBased()) {
            bestWeightMapTo.put(to, currTo);
            if (currFrom != null) {
                bestWeightMapOther = bestWeightMapFrom;
                updateBestPath(GHUtility.getEdge(graph, currFrom.adjNode, to), currFrom, to);
            }
        } else if (currFrom != null && currFrom.adjNode == to) {
            // special case of identical start and end
            bestPath.setSPTEntry(currFrom);
            bestPath.setSPTEntryTo(currTo);
            finishedFrom = true;
            finishedTo = true;
        }
    }

    protected Path createAndInitPath() {
        bestPath = new Path4CH(graph, graph.getBaseGraph(), weighting, maxSpeed);
        return bestPath;
    }


    @Override
    protected Path extractPath() {
        if (finished())
            return bestPath.extract();

        return bestPath;
    }

    protected double getCurrentFromWeight() {
        return currFrom.weight;
    }

    protected double getCurrentToWeight() {
        return currTo.weight;
    }

    public boolean fillEdgesFrom() {
        if (pqCHFrom.isEmpty())
            return false;

        currFrom = pqCHFrom.poll();

        if (!inCore && chGraph.getLevel(currFrom.adjNode) == coreNodeLevel) {
            // core entry point, do not relax its edges
            pqCoreFrom.add(currFrom);
        }
        else {
            bestWeightMapOther = bestWeightMapTo;
            fillEdges(currFrom, pqCHFrom, bestWeightMapFrom, outEdgeExplorer, false);
            visitedCountFrom++;
        }

        return true;
    }

    public boolean fillEdgesTo() {
        if (pqCHTo.isEmpty())
            return false;

        currTo = pqCHTo.poll();

        if (!inCore && chGraph.getLevel(currTo.adjNode) == coreNodeLevel) {
            // core entry point, do not relax its edges
            pqCoreTo.add(currTo);
        }
        else {
            bestWeightMapOther = bestWeightMapFrom;
            fillEdges(currTo, pqCHTo, bestWeightMapTo, inEdgeExplorer, true);
            visitedCountTo++;
        }

        return true;
    }

    public boolean finishedPhase1() {
        // we need to finish BOTH searches for CH!
        if (finishedFrom && finishedTo)
            return true;

        double fromWeight = currFrom.weight;
        double toWeight = currTo.weight;

        // changed also the final finish condition for CH
        if (!pqCoreFrom.isEmpty())
            fromWeight = Math.min(pqCoreFrom.peek().weight, fromWeight);
        if (!pqCoreTo.isEmpty())
                toWeight = Math.min(pqCoreTo.peek().weight, toWeight);


        return fromWeight >= bestPath.getWeight() && toWeight >= bestPath.getWeight();
    }

    void runPhase1() {
        while (!finishedPhase1() && !isMaxVisitedNodesExceeded()) {
            if (!finishedFrom)
                finishedFrom = !fillEdgesFrom();

            if (!finishedTo)
                finishedTo = !fillEdgesTo();
        }
    }

    void runPhase2() {
        // re-init queues
        pqCHFrom = pqCoreFrom;
        pqCHTo = pqCoreTo;

        finishedFrom = pqCHFrom.isEmpty();
        finishedTo = pqCHFrom.isEmpty();

        if (!finishedFrom)
            currFrom = pqCHFrom.peek();

        if (!finishedTo)
            currTo = pqCHTo.peek();

        while (!finishedPhase2() && !isMaxVisitedNodesExceeded()) {
            if (!finishedFrom)
                finishedFrom = !fillEdgesFrom();

            if (!finishedTo)
                finishedTo = !fillEdgesTo();
        }
    }

    public boolean finishedPhase2() {
        if (finishedFrom || finishedTo)
            return true;

        return currFrom.weight + currTo.weight >= bestPath.getWeight();
    }

    @Override
    protected boolean finished() {
        return finishedPhase2();
    }


    protected void runAlgo() {

        // PHASE 1: run modified CH outside of core to find entry points
        inCore = false;
        additionalEdgeFilter.setInCore(false);
        runPhase1();

        // PHASE 2 Perform routing in core with the restrictions filter
        additionalEdgeFilter.setInCore(true);
        inCore = true;
        runPhase2();
    }

    void fillEdges(SPTEntry currEdge, PriorityQueue<SPTEntry> prioQueue, IntObjectMap<SPTEntry> bestWeightMap,
                   EdgeExplorer explorer, boolean reverse) {
        EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);
        while (iter.next()) {
            if (!accept(iter, currEdge.edge))
                continue;

            int traversalId = traversalMode.createTraversalId(iter, reverse);
            // Modification by Maxim Rylov: use originalEdge as the previousEdgeId
            double tmpWeight = weighting.calcWeight(iter, reverse, currEdge.originalEdge) + currEdge.weight;
            if (Double.isInfinite(tmpWeight))
                continue;
            SPTEntry ee = bestWeightMap.get(traversalId);
            if (ee == null) {
                ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
                // Modification by Maxim Rylov: Assign the original edge id.
                ee.originalEdge = iter.getOriginalEdge();
                ee.parent = currEdge;
                bestWeightMap.put(traversalId, ee);
                prioQueue.add(ee);
            } else if (ee.weight > tmpWeight) {
                prioQueue.remove(ee);
                ee.edge = iter.getEdge();
                ee.weight = tmpWeight;
                ee.parent = currEdge;
                prioQueue.add(ee);
            } else
                continue;

            if (updateBestPath)
                updateBestPath(iter, ee, traversalId);
        }
    }

    @Override
    protected void updateBestPath(EdgeIteratorState edgeState, SPTEntry entryCurrent, int traversalId) {
        SPTEntry entryOther = bestWeightMapOther.get(traversalId);
        if (entryOther == null)
            return;

        boolean reverse = bestWeightMapFrom == bestWeightMapOther;

        // update μ
        double newWeight = entryCurrent.weight + entryOther.weight;
        if (traversalMode.isEdgeBased()) {
            if (entryOther.edge != entryCurrent.edge)
                throw new IllegalStateException("cannot happen for edge based execution of " + getName());

            if (entryOther.adjNode != entryCurrent.adjNode) {
                // prevents the path to contain the edge at the meeting point twice and subtract the weight (excluding turn weight => no previous edge)
                entryCurrent = entryCurrent.parent;
                newWeight -= weighting.calcWeight(edgeState, reverse, EdgeIterator.NO_EDGE);
            } else if (!traversalMode.hasUTurnSupport())
                // we detected a u-turn at meeting point, skip if not supported
                return;
        }

        if (newWeight < bestPath.getWeight()) {
            bestPath.setSwitchToFrom(reverse);
            bestPath.setSPTEntry(entryCurrent);
            bestPath.setWeight(newWeight);
            bestPath.setSPTEntryTo(entryOther);
        }
    }

    protected void setUpdateBestPath(boolean b) {
        updateBestPath = b;
    }

    @Override
    public String getName() {
        return Parameters.Algorithms.DIJKSTRA_BI;
    }

    @Override
    public Path calcPath(int from, int to) {
        checkAlreadyRun();
        createAndInitPath();
        initFrom(from, 0);
        initTo(to, 0);
        runAlgo();
        return extractPath();
    }

    @Override
    public int getVisitedNodes() {
        return visitedCountFrom + visitedCountTo;
    }

    public RoutingAlgorithm setEdgeFilter(CoreDijkstraFilter additionalEdgeFilter) {
        this.additionalEdgeFilter = additionalEdgeFilter;
        return this;
    }

    protected boolean accept(EdgeIterator iter, int prevOrNextEdgeId) {
        if (!traversalMode.hasUTurnSupport() && iter.getEdge() == prevOrNextEdgeId)
            return false;

        return additionalEdgeFilter == null || additionalEdgeFilter.accept(iter);
    }
}
