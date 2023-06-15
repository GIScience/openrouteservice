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

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.ch.CHEntry;
import com.graphhopper.routing.weighting.BeelineWeightApproximator;
import com.graphhopper.routing.weighting.BalancedWeightApproximator;
import com.graphhopper.routing.weighting.WeightApproximator;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.RoutingCHEdgeExplorer;
import com.graphhopper.storage.RoutingCHEdgeIterator;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;

/**
 * Calculates best path using CH routing outside core and ALT inside core.
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author jansoe
 * @author Andrzej Oles
 */

public class CoreALT extends AbstractCoreRoutingAlgorithm {
    PriorityQueue<AStarEntry> fromPriorityQueueCH;
    PriorityQueue<AStarEntry> toPriorityQueueCH;
    PriorityQueue<AStarEntry> fromPriorityQueueCore;
    PriorityQueue<AStarEntry> toPriorityQueueCore;

    IntObjectMap<AStarEntry> bestWeightMapFromCH;
    IntObjectMap<AStarEntry> bestWeightMapToCH;
    IntObjectMap<AStarEntry> bestWeightMapOtherCH;

    IntObjectMap<List<AStarEntry>> bestWeightMapFromCore;
    IntObjectMap<List<AStarEntry>> bestWeightMapToCore;
    IntObjectMap<List<AStarEntry>> bestWeightMapOtherCore;

    protected AStarEntry currFrom;
    protected AStarEntry currTo;

    private BalancedWeightApproximator weightApprox;


    int fromProxy;
    int toProxy;

    double approximatorOffset;


    public CoreALT(RoutingCHGraph graph, Weighting weighting) {
        super(graph, weighting);
        BeelineWeightApproximator defaultApprox = new BeelineWeightApproximator(nodeAccess, weighting);
        defaultApprox.setDistanceCalc(DistancePlaneProjection.DIST_PLANE);
        setApproximation(defaultApprox);
    }

    @Override
    protected void initCollections(int size) {
        fromPriorityQueueCH = new PriorityQueue<>(size);
        toPriorityQueueCH = new PriorityQueue<>(size);
        fromPriorityQueueCore = new PriorityQueue<>(size);
        toPriorityQueueCore = new PriorityQueue<>(size);

        bestWeightMapFromCH = new GHIntObjectHashMap<>(size);
        bestWeightMapToCH = new GHIntObjectHashMap<>(size);
        bestWeightMapFromCore = new GHIntObjectHashMap<>(size);
        bestWeightMapToCore = new GHIntObjectHashMap<>(size);
    }

    /**
     * @param approx if true it enables approximate distance calculation from lat,lon values
     */
    public CoreALT setApproximation(WeightApproximator approx) {
        weightApprox = new BalancedWeightApproximator(approx);
        return this;
    }

    @Override
    protected CHEntry createCHEntry(int node, double weight, long time) {
        throw new IllegalStateException("use AStarEdge constructor directly");
    }

    @Override
    public void initFrom(int from, double weight, long at) {
        currFrom = new AStarEntry(EdgeIterator.NO_EDGE, EdgeIterator.NO_EDGE, from, weight, weight);
        currFrom.time = at;
        fromPriorityQueueCH.add(currFrom);
        bestWeightMapFromCH.put(from, currFrom);
        if (currTo != null) {
            bestWeightMapOtherCH = bestWeightMapToCH;
            updateBestPathCH(currTo, from, false);
        }
    }

    @Override
    public void initTo(int to, double weight, long at) {
        currTo = new AStarEntry(EdgeIterator.NO_EDGE, EdgeIterator.NO_EDGE, to, weight, weight);
        currTo.time = at;
        toPriorityQueueCH.add(currTo);
        bestWeightMapToCH.put(to, currTo);
        if (currFrom != null) {
            bestWeightMapOtherCH = bestWeightMapFromCH;
            updateBestPathCH(currFrom, to, true);
        }
    }

    @Override
    public boolean fillEdgesFrom() {
        if (fromPriorityQueueCH.isEmpty())
            return false;

        currFrom = fromPriorityQueueCH.poll();

        if (isCoreNode(currFrom.adjNode)) {
            // core entry point, do not relax its edges
            fromPriorityQueueCore.add(currFrom);
            // for regular CH Dijkstra we don't expect an entry to exist because the picked node is supposed to be already settled
            if (considerTurnRestrictions(currFrom.adjNode))
                initBestWeightMapEntryList(bestWeightMapFromCore, currFrom.adjNode).add(currFrom);
        }
        else {
            bestWeightMapOtherCH = bestWeightMapToCH;
            fillEdgesCH(currFrom, fromPriorityQueueCH, bestWeightMapFromCH, outEdgeExplorer, false);
            visitedCountFrom1++;
        }

        return true;
    }

    @Override
    public boolean fillEdgesTo() {
        if (toPriorityQueueCH.isEmpty())
            return false;

        currTo = toPriorityQueueCH.poll();

        if (isCoreNode(currTo.adjNode)) {
            // core entry point, do not relax its edges
            toPriorityQueueCore.add(currTo);
            // for regular CH Dijkstra we don't expect an entry to exist because the picked node is supposed to be already settled
            if (considerTurnRestrictions(currTo.adjNode))
                initBestWeightMapEntryList(bestWeightMapToCore, currTo.adjNode).add(currTo);
        }
        else {
            bestWeightMapOtherCH = bestWeightMapFromCH;
            fillEdgesCH(currTo, toPriorityQueueCH, bestWeightMapToCH, inEdgeExplorer, true);
            visitedCountTo1++;
        }

        return true;
    }

    List<AStarEntry> initBestWeightMapEntryList(IntObjectMap<List<AStarEntry>> bestWeightMap, int traversalId) {
        if (bestWeightMap.get(traversalId) != null)
            throw new IllegalStateException("Core entry point already exists in best weight map.");

        List<AStarEntry> entryList = new ArrayList<>(5);// TODO: Proper assessment of the optimal size
        bestWeightMap.put(traversalId, entryList);

        return entryList;
    }

    @Override
    public boolean finishedPhase1() {
        // we need to finish BOTH searches for CH!
        if (finishedFrom && finishedTo)
            return true;

        double fromWeight = currFrom.weight;
        double toWeight = currTo.weight;

        // changed also the final finish condition for CH
        if (!fromPriorityQueueCore.isEmpty())
            fromWeight = Math.min(fromPriorityQueueCore.peek().weight, fromWeight);
        if (!toPriorityQueueCore.isEmpty())
            toWeight = Math.min(toPriorityQueueCore.peek().weight, toWeight);

        return fromWeight >= bestWeight && toWeight >= bestWeight;
    }

    /**
     * Run the ALT algo in the core
     */
    @Override
    void runPhase2() {
        // re-init queues
        finishedFrom = fromPriorityQueueCore.isEmpty();
        finishedTo = toPriorityQueueCore.isEmpty();

        if (!finishedFrom && !finishedTo) {
            fromProxy = fromPriorityQueueCore.peek().adjNode;
            toProxy = toPriorityQueueCore.peek().adjNode;

            initApproximator();

            recalculateWeights(fromPriorityQueueCore, false);
            recalculateWeights(toPriorityQueueCore, true);

            currFrom = fromPriorityQueueCore.peek();
            currTo = toPriorityQueueCore.peek();
        }

        while (!finishedPhase2() && !isMaxVisitedNodesExceeded()) {
            finishedFrom = !fillEdgesFromCore();
            finishedTo = !fillEdgesToCore();
        }

    }

    private void initApproximator() {
        weightApprox.setFromTo(fromProxy, toProxy);
        approximatorOffset = weightApprox.approximate(toProxy, true) + weightApprox.getSlack();
    }

    private void recalculateWeights(PriorityQueue<AStarEntry> queue, boolean reverse) {
        AStarEntry[] entries = queue.toArray(new AStarEntry[queue.size()]);

        queue.clear();
        for (AStarEntry value : entries) {
            value.weight = value.weightOfVisitedPath + weightApprox.approximate(value.adjNode, reverse);
            queue.add(value);
        }
    }

    @Override
    public boolean finishedPhase2() {
        if (finishedFrom || finishedTo)
            return true;
            // AO: in order to guarantee that the shortest path is found it is neccesary to account for possible precision loss in LM distance approximation by introducing the additional offset
        return currFrom.weight + currTo.weight >= bestWeight + approximatorOffset;
    }

    void fillEdgesCH(AStarEntry currEdge, PriorityQueue<AStarEntry> prioQueue, IntObjectMap<AStarEntry> bestWeightMap,
                     RoutingCHEdgeExplorer explorer, boolean reverse) {
        RoutingCHEdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);
        while (iter.next()) {
            if (!accept(iter, currEdge, reverse))
                continue;

            int traversalId = iter.getAdjNode();
            double tmpWeight = calcEdgeWeight(iter, currEdge, reverse);
            if (Double.isInfinite(tmpWeight))
                continue;

            AStarEntry aStarEntry = bestWeightMap.get(traversalId);
            if (aStarEntry == null) {
                aStarEntry = new AStarEntry(iter.getEdge(), getIncEdge(iter, reverse), iter.getAdjNode(), tmpWeight, tmpWeight);
                aStarEntry.originalEdge = iter.getOrigEdge();
                bestWeightMap.put(traversalId, aStarEntry);
            } else if (aStarEntry.weight > tmpWeight) {
                prioQueue.remove(aStarEntry);
                aStarEntry.edge = iter.getEdge();
                aStarEntry.originalEdge = iter.getOrigEdge();
                aStarEntry.incEdge = getIncEdge(iter, reverse);
                aStarEntry.weight = tmpWeight;
                aStarEntry.weightOfVisitedPath = tmpWeight;
            } else
                continue;

            aStarEntry.parent = currEdge;
            aStarEntry.time = calcEdgeTime(iter, currEdge, reverse);
            prioQueue.add(aStarEntry);

            updateBestPathCH(aStarEntry, traversalId, reverse);
        }
    }

    public boolean fillEdgesFromCore() {
        if (fromPriorityQueueCore.isEmpty())
            return false;

        currFrom = fromPriorityQueueCore.poll();

        bestWeightMapOtherCH = bestWeightMapToCH;
        bestWeightMapOtherCore = bestWeightMapToCore;
        fillEdgesCore(currFrom, fromPriorityQueueCore, bestWeightMapFromCH, bestWeightMapFromCore, outEdgeExplorer, false);
        visitedCountFrom2++;

        return true;
    }

    public boolean fillEdgesToCore() {
        if (toPriorityQueueCore.isEmpty())
            return false;

        currTo = toPriorityQueueCore.poll();

        bestWeightMapOtherCH = bestWeightMapFromCH;
        bestWeightMapOtherCore = bestWeightMapFromCore;
        fillEdgesCore(currTo, toPriorityQueueCore, bestWeightMapToCH, bestWeightMapToCore, inEdgeExplorer, true);
        visitedCountTo2++;

        return true;
    }

    private void fillEdgesCore(AStarEntry currEdge, PriorityQueue<AStarEntry> prioQueue, IntObjectMap<AStarEntry> bestWeightMap, IntObjectMap<List<AStarEntry>> bestWeightMapCore, RoutingCHEdgeExplorer explorer, boolean reverse) {
        RoutingCHEdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);
        while (iter.next()) {
            if (!accept(iter, currEdge, reverse))
                continue;

            int traversalId = iter.getAdjNode();

            // TODO performance: check if the node is already existent in the opposite direction
            // then we could avoid the approximation as we already know the exact complete path!
            // Modification by Maxim Rylov: use originalEdge as the previousEdgeId
            double alreadyVisitedWeight = calcEdgeWeight(iter, currEdge, reverse);
            if (Double.isInfinite(alreadyVisitedWeight))
                continue;

            if (inCore && considerTurnRestrictions(iter.getAdjNode())) {
                List<AStarEntry> entries = bestWeightMapCore.get(traversalId);
                AStarEntry aStarEntry = null;

                if (entries == null) {
                    entries = initBestWeightMapEntryList(bestWeightMapCore, traversalId);
                } else {
                    ListIterator<AStarEntry> it = entries.listIterator();
                    while (it.hasNext()) {
                        AStarEntry entry = it.next();
                        if (entry.edge == iter.getEdge()) {
                            aStarEntry = entry;
                            break;
                        }
                    }
                }

                if (aStarEntry == null || aStarEntry.getWeightOfVisitedPath() > alreadyVisitedWeight) {
                    double currWeightToGoal = weightApprox.approximate(iter.getAdjNode(), reverse);
                    double estimationFullWeight = alreadyVisitedWeight + currWeightToGoal;
                    if (aStarEntry == null) {
                        aStarEntry = new AStarEntry(iter.getEdge(), getIncEdge(iter, reverse), iter.getAdjNode(), estimationFullWeight, alreadyVisitedWeight);
                        aStarEntry.originalEdge = iter.getOrigEdge();
                        entries.add(aStarEntry);
                    } else {
                        prioQueue.remove(aStarEntry);
                        aStarEntry.edge = iter.getEdge();
                        aStarEntry.originalEdge = iter.getOrigEdge();
                        aStarEntry.incEdge = getIncEdge(iter, reverse);
                        aStarEntry.weight = estimationFullWeight;
                        aStarEntry.weightOfVisitedPath = alreadyVisitedWeight;
                    }

                    aStarEntry.parent = currEdge;
                    aStarEntry.time = calcEdgeTime(iter, currEdge, reverse);
                    prioQueue.add(aStarEntry);

                    updateBestPathCore(aStarEntry, traversalId, reverse);
                }
            }
            else {
                AStarEntry aStarEntry = bestWeightMap.get(traversalId);
                if (aStarEntry == null || aStarEntry.getWeightOfVisitedPath() > alreadyVisitedWeight) {
                    double currWeightToGoal = weightApprox.approximate(iter.getAdjNode(), reverse);
                    double estimationFullWeight = alreadyVisitedWeight + currWeightToGoal;
                    if (aStarEntry == null) {
                        aStarEntry = new AStarEntry(iter.getEdge(), getIncEdge(iter, reverse), iter.getAdjNode(), estimationFullWeight, alreadyVisitedWeight);
                        aStarEntry.originalEdge = iter.getOrigEdge();
                        bestWeightMap.put(traversalId, aStarEntry);
                    } else {
                        prioQueue.remove(aStarEntry);
                        aStarEntry.edge = iter.getEdge();
                        aStarEntry.originalEdge = iter.getOrigEdge();
                        aStarEntry.incEdge = getIncEdge(iter, reverse);
                        aStarEntry.weight = estimationFullWeight;
                        aStarEntry.weightOfVisitedPath = alreadyVisitedWeight;
                    }

                    aStarEntry.parent = currEdge;
                    aStarEntry.time = calcEdgeTime(iter, currEdge, reverse);
                    prioQueue.add(aStarEntry);

                    updateBestPathCH(aStarEntry, traversalId, reverse);
                }
            }
        }
    }

    protected void updateBestPathCH(AStarEntry entryCurrent, int traversalId, boolean reverse) {
        AStarEntry entryOther = bestWeightMapOtherCH.get(traversalId);
        if (entryOther == null)
            return;

        double newWeight = entryCurrent.weightOfVisitedPath + entryOther.weightOfVisitedPath;

        if (newWeight < bestWeight)
            updateBestPath(entryCurrent, entryOther, newWeight, reverse);
    }

    protected void updateBestPathCore(AStarEntry entryCurrent, int traversalId, boolean reverse) {
        List<AStarEntry> entries = bestWeightMapOtherCore.get(traversalId);
        if (entries == null)
            return;

        ListIterator<AStarEntry> it = entries.listIterator();
        while (it.hasNext()) {
            AStarEntry entryOther = it.next();
            // u-turn check neccessary because getTurnWeight discards them based on originalEdge which is -1 for shortcuts
            if (entryCurrent.edge == entryOther.edge)
                continue;

            double newWeight = entryCurrent.weightOfVisitedPath + entryOther.weightOfVisitedPath;
            if (newWeight < bestWeight) {
                double turnWeight = getTurnWeight(entryCurrent.originalEdge, entryCurrent.adjNode, entryOther.originalEdge, reverse);
                // currently only infinite (u-)turn costs are supported
                if (Double.isInfinite(turnWeight))
                    continue;

                updateBestPath(entryCurrent, entryOther, newWeight, reverse);
            }
        }
    }

    public static class AStarEntry extends CHEntry {
        double weightOfVisitedPath;

        public AStarEntry(int edgeId, int incEdge, int adjNode, double weightForHeap, double weightOfVisitedPath) {
            super(edgeId, incEdge, adjNode, weightForHeap);
            this.weightOfVisitedPath = weightOfVisitedPath;
        }

        @Override
        public final double getWeightOfVisitedPath() {
            return weightOfVisitedPath;
        }
    }

    @Override
    public String getName() {
        return Parameters.Algorithms.ASTAR_BI + "|" + weightApprox;
    }

}
