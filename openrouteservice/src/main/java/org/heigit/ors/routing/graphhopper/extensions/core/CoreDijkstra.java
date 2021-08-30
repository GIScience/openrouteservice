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
import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;

/**
 * Calculates best path using core routing algorithm.
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Andrzej Oles
 */

public class CoreDijkstra extends AbstractCoreRoutingAlgorithm {
    PriorityQueue<SPTEntry> fromPriorityQueueCH;
    PriorityQueue<SPTEntry> toPriorityQueueCH;
    PriorityQueue<SPTEntry> fromPriorityQueueCore;
    PriorityQueue<SPTEntry> toPriorityQueueCore;

    IntObjectMap<SPTEntry> bestWeightMapFromCH;
    IntObjectMap<SPTEntry> bestWeightMapToCH;
    IntObjectMap<SPTEntry> bestWeightMapOtherCH;

    IntObjectMap<List<SPTEntry>> bestWeightMapFromCore;
    IntObjectMap<List<SPTEntry>> bestWeightMapToCore;
    IntObjectMap<List<SPTEntry>> bestWeightMapOtherCore;

    SPTEntry currFrom;
    SPTEntry currTo;

    public CoreDijkstra(Graph graph, Weighting weighting) {
        super(graph, weighting);
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

    @Override
    public void initFrom(int from, double weight, long time) {
        currFrom = createSPTEntry(from, weight, time);
        fromPriorityQueueCH.add(currFrom);
        bestWeightMapFromCH.put(from, currFrom);
        if (currTo != null) {
            bestWeightMapOtherCH = bestWeightMapToCH;
            updateBestPathCH(currTo, from, false);
        }
    }

    @Override
    public void initTo(int to, double weight, long time) {
        currTo = createSPTEntry(to, weight, time);
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
            fillEdges(currFrom, fromPriorityQueueCH, bestWeightMapFromCH, null, outEdgeExplorer, false);
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
            fillEdges(currTo, toPriorityQueueCH, bestWeightMapToCH, null, inEdgeExplorer, true);
            visitedCountTo1++;
        }

        return true;
    }

    List<SPTEntry> initBestWeightMapEntryList(IntObjectMap<List<SPTEntry>> bestWeightMap, int traversalId) {
        if (bestWeightMap.get(traversalId) != null)
            throw new IllegalStateException("Core entry point already exists in best weight map.");

        List<SPTEntry> entryList = new ArrayList<>(5);// TODO: Proper assessment of the optimal size
        bestWeightMap.put(traversalId, entryList);

        return entryList;
    }

    public boolean fillEdgesFromCore() {
        if (fromPriorityQueueCore.isEmpty())
            return false;

        currFrom = fromPriorityQueueCore.poll();

        bestWeightMapOtherCH = bestWeightMapToCH;
        bestWeightMapOtherCore = bestWeightMapToCore;
        fillEdges(currFrom, fromPriorityQueueCore, bestWeightMapFromCH, bestWeightMapFromCore, outEdgeExplorer, false);
        visitedCountFrom2++;

        return true;
    }

    public boolean fillEdgesToCore() {
        if (toPriorityQueueCore.isEmpty())
            return false;

        currTo = toPriorityQueueCore.poll();

        bestWeightMapOtherCH = bestWeightMapFromCH;
        bestWeightMapOtherCore = bestWeightMapFromCore;
        fillEdges(currTo, toPriorityQueueCore, bestWeightMapToCH, bestWeightMapToCore, inEdgeExplorer, true);
        visitedCountTo2++;

        return true;
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

        return fromWeight >= bestPath.getWeight() && toWeight >= bestPath.getWeight();
    }

    @Override
    void runPhase2() {
        finishedFrom = fromPriorityQueueCore.isEmpty();
        if (!finishedFrom)
            currFrom = fromPriorityQueueCore.peek();

        finishedTo = toPriorityQueueCore.isEmpty();
        if (!finishedTo)
            currTo = toPriorityQueueCore.peek();

        while (!finishedPhase2() && !isMaxVisitedNodesExceeded()) {
            finishedFrom = !fillEdgesFromCore();
            finishedTo = !fillEdgesToCore();
        }
    }

    @Override
    public boolean finishedPhase2() {
        if (finishedFrom || finishedTo)
            return true;

        return currFrom.weight + currTo.weight >= bestPath.getWeight();
    }

    void fillEdges(SPTEntry currEdge, PriorityQueue<SPTEntry> prioQueue, IntObjectMap<SPTEntry> bestWeightMap, IntObjectMap<List<SPTEntry>> bestWeightMapCore, EdgeExplorer explorer, boolean reverse) {
        EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);
        while (iter.next()) {
            if (!accept(iter, currEdge.edge))
                continue;

            int traversalId = iter.getAdjNode();
            // Modification by Maxim Rylov: use originalEdge as the previousEdgeId
            double tmpWeight = calcWeight(iter, currEdge, reverse) + currEdge.weight;
            if (Double.isInfinite(tmpWeight))
                continue;

            if (inCore && considerTurnRestrictions(iter.getAdjNode())) {
                List<SPTEntry> entries = bestWeightMapCore.get(traversalId);
                SPTEntry ee = null;

                if (entries == null) {
                    entries = initBestWeightMapEntryList(bestWeightMapCore, traversalId);
                } else {
                    ListIterator<SPTEntry> it = entries.listIterator();
                    while (it.hasNext()) {
                        SPTEntry entry = it.next();
                        if (entry.edge == iter.getEdge()) {
                            ee = entry;
                            break;
                        }
                    }
                }

                if (ee == null) {
                    ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
                    // Modification by Maxim Rylov: Assign the original edge id.
                    ee.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
                    entries.add(ee);
                } else if (ee.weight > tmpWeight) {
                    prioQueue.remove(ee);
                    ee.edge = iter.getEdge();
                    ee.weight = tmpWeight;
                } else
                    continue;

                ee.parent = currEdge;
                ee.time = calcTime(iter, currEdge, reverse);
                prioQueue.add(ee);

                updateBestPathCore(ee, traversalId, reverse);
            }
            else {
                SPTEntry ee = bestWeightMap.get(traversalId);
                if (ee == null) {
                    ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
                    // Modification by Maxim Rylov: Assign the original edge id.
                    ee.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
                    bestWeightMap.put(traversalId, ee);
                } else if (ee.weight > tmpWeight) {
                    prioQueue.remove(ee);
                    ee.edge = iter.getEdge();
                    ee.weight = tmpWeight;
                } else
                    continue;

                ee.parent = currEdge;
                ee.time = calcTime(iter, currEdge, reverse);
                prioQueue.add(ee);

                updateBestPathCH(ee, traversalId, reverse);
            }
        }
    }

    protected void updateBestPathCH(SPTEntry entryCurrent, int traversalId, boolean reverse) {
        SPTEntry entryOther = bestWeightMapOtherCH.get(traversalId);
        if (entryOther == null)
            return;

        double newWeight = entryCurrent.weight + entryOther.weight;

        if (newWeight < bestPath.getWeight())
            updateBestPath(entryCurrent, entryOther, newWeight, reverse);
    }

    protected void updateBestPathCore(SPTEntry entryCurrent, int traversalId, boolean reverse) {
        List<SPTEntry> entries = bestWeightMapOtherCore.get(traversalId);
        if (entries == null)
            return;

        ListIterator<SPTEntry> it = entries.listIterator();
        while (it.hasNext()) {
            SPTEntry entryOther = it.next();

            double newWeight = entryCurrent.weight + entryOther.weight;

            if (newWeight < bestPath.getWeight()) {
                double turnWeight = reverse ?
                        turnWeighting.calcTurnWeight(entryOther.originalEdge, entryCurrent.adjNode, entryCurrent.originalEdge):
                        turnWeighting.calcTurnWeight(entryCurrent.originalEdge, entryCurrent.adjNode, entryOther.originalEdge);
                if (Double.isInfinite(turnWeight))
                    continue;

                updateBestPath(entryCurrent, entryOther, newWeight, reverse);
            }
        }
    }

    double calcWeight(EdgeIterator iter, SPTEntry currEdge, boolean reverse) {
        return weighting.calcWeight(iter, reverse, currEdge.originalEdge);
    }

    long calcTime(EdgeIteratorState iter, SPTEntry currEdge, boolean reverse) {
        return 0;
    }

    @Override
    public String getName() {
        return Parameters.Algorithms.DIJKSTRA_BI;
    }
}
