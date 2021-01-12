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
import com.graphhopper.routing.ch.PreparationWeighting;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.TurnWeighting;
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
    protected IntObjectMap<SPTEntry> bestWeightMapFrom;
    protected IntObjectMap<SPTEntry> bestWeightMapTo;
    protected IntObjectMap<SPTEntry> bestWeightMapOther;
    protected SPTEntry currFrom;
    protected SPTEntry currTo;

    private PriorityQueue<SPTEntry> fromPriorityQueueCH;
    private PriorityQueue<SPTEntry> toPriorityQueueCH;
    private PriorityQueue<SPTEntry> fromPriorityQueueCore;
    private PriorityQueue<SPTEntry> toPriorityQueueCore;

    protected IntObjectMap<List<SPTEntry>> bestWeightMapFromCore;
    protected IntObjectMap<List<SPTEntry>> bestWeightMapToCore;
    protected IntObjectMap<List<SPTEntry>> bestWeightMapOtherCore;

    TurnWeighting turnWeighting;

    public CoreDijkstra(Graph graph, Weighting weighting, TraversalMode tMode) {
        super(graph, new PreparationWeighting(weighting), tMode);
        this.turnWeighting = (TurnWeighting) weighting;
    }

    @Override
    protected void initCollections(int size) {
        fromPriorityQueueCH = new PriorityQueue<>(size);
        bestWeightMapFrom = new GHIntObjectHashMap<>(size);

        toPriorityQueueCH = new PriorityQueue<>(size);
        bestWeightMapTo = new GHIntObjectHashMap<>(size);

        fromPriorityQueueCore = new PriorityQueue<>(size);
        toPriorityQueueCore = new PriorityQueue<>(size);

        bestWeightMapFromCore = new GHIntObjectHashMap<>(size);
        bestWeightMapToCore = new GHIntObjectHashMap<>(size);
    }

    @Override
    public void initFrom(int from, double weight) {
        currFrom = createSPTEntry(from, weight);
        fromPriorityQueueCH.add(currFrom);
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

    @Override
    public void initTo(int to, double weight) {
        currTo = createSPTEntry(to, weight);
        toPriorityQueueCH.add(currTo);
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

    @Override
    public boolean fillEdgesFrom() {
        if (fromPriorityQueueCH.isEmpty())
            return false;

        currFrom = fromPriorityQueueCH.poll();

        if (!inCore && chGraph.getLevel(currFrom.adjNode) == coreNodeLevel) {
            // core entry point, do not relax its edges
            fromPriorityQueueCore.add(currFrom);
            List<SPTEntry> entryList = new ArrayList<>();
            entryList.add(currFrom);
            bestWeightMapFromCore.put(currFrom.adjNode, entryList);
        }
        else {
            bestWeightMapOther = bestWeightMapTo;
            fillEdges(currFrom, fromPriorityQueueCH, bestWeightMapFrom, outEdgeExplorer, false);
            if (inCore)
                visitedCountFrom2++;
            else
                visitedCountFrom1++;
        }

        return true;
    }

    public boolean fillEdgesFromCore() {
        if (fromPriorityQueueCH.isEmpty())
            return false;

        currFrom = fromPriorityQueueCH.poll();

        if (!inCore && chGraph.getLevel(currFrom.adjNode) == coreNodeLevel) {
            // core entry point, do not relax its edges
            fromPriorityQueueCore.add(currFrom);
            List<SPTEntry> entryList = new ArrayList<>();
            entryList.add(currFrom);
            bestWeightMapFromCore.put(currFrom.adjNode, entryList);
        }
        else {
            bestWeightMapOtherCore = bestWeightMapToCore;
            fillEdgesCore(currFrom, fromPriorityQueueCH, bestWeightMapFromCore, outEdgeExplorer, false);
            if (inCore)
                visitedCountFrom2++;
            else
                visitedCountFrom1++;
        }

        return true;
    }

    @Override
    public boolean fillEdgesTo() {
        if (toPriorityQueueCH.isEmpty())
            return false;

        currTo = toPriorityQueueCH.poll();

        if (!inCore && chGraph.getLevel(currTo.adjNode) == coreNodeLevel) {
            // core entry point, do not relax its edges
            toPriorityQueueCore.add(currTo);
            List<SPTEntry> entryList = new ArrayList<>();
            entryList.add(currTo);
            bestWeightMapToCore.put(currTo.adjNode, entryList);
        }
        else {
            bestWeightMapOther = bestWeightMapFrom;
            fillEdges(currTo, toPriorityQueueCH, bestWeightMapTo, inEdgeExplorer, true);
            if (inCore)
                visitedCountTo2++;
            else
                visitedCountTo1++;
        }

        return true;
    }

    public boolean fillEdgesToCore() {
        if (toPriorityQueueCH.isEmpty())
            return false;

        currTo = toPriorityQueueCH.poll();

        if (!inCore && chGraph.getLevel(currTo.adjNode) == coreNodeLevel) {
            // core entry point, do not relax its edges
            toPriorityQueueCore.add(currTo);
            List<SPTEntry> entryList = new ArrayList<>();
            entryList.add(currTo);
            bestWeightMapToCore.put(currTo.adjNode, entryList);
        }
        else {
            bestWeightMapOtherCore = bestWeightMapFromCore;
            fillEdgesCore(currTo, toPriorityQueueCH, bestWeightMapToCore, inEdgeExplorer, true);
            if (inCore)
                visitedCountTo2++;
            else
                visitedCountTo1++;
        }

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
        // re-init queues
        fromPriorityQueueCH = fromPriorityQueueCore;
        toPriorityQueueCH = toPriorityQueueCore;

        finishedFrom = fromPriorityQueueCH.isEmpty();
        finishedTo = toPriorityQueueCH.isEmpty();

        if (!finishedFrom && !finishedTo) {
            currTo = toPriorityQueueCH.peek();
            currFrom = fromPriorityQueueCH.peek();
        }

        while (!finishedPhase2() && !isMaxVisitedNodesExceeded()) {
            if (!finishedFrom)
                finishedFrom = !fillEdgesFromCore();

            if (!finishedTo)
                finishedTo = !fillEdgesToCore();
        }
    }

    @Override
    public boolean finishedPhase2() {
        if (finishedFrom || finishedTo)
            return true;

        return currFrom.weight + currTo.weight >= bestPath.getWeight();
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
                ee.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
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

            if (doUpdateBestPath)
                updateBestPath(iter, ee, traversalId);
        }
    }

    void fillEdgesCore(SPTEntry currEdge, PriorityQueue<SPTEntry> prioQueue, IntObjectMap<List<SPTEntry>> bestWeightMap,
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
            List<SPTEntry> entries = bestWeightMap.get(traversalId);
            SPTEntry ee = null;
            doUpdateBestPath = true;
            boolean found = false;
            if (entries == null) {
                entries = new ArrayList<>();
                bestWeightMap.put(traversalId, entries);
            }
            else {
                ListIterator<SPTEntry> it = entries.listIterator();
                while (it.hasNext()) {
                    ee = it.next();
                    if (ee.edge == iter.getEdge()) {
                        found = true;
                        if (ee.weight > tmpWeight) {
                            prioQueue.remove(ee);
                            ee.edge = iter.getEdge();
                            ee.weight = tmpWeight;
                            ee.parent = currEdge;
                            prioQueue.add(ee);
                        } else {
                            doUpdateBestPath = false;
                        }
                        break;
                    }
                }
            }
            if (!found) {
                ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
                // Modification by Maxim Rylov: Assign the original edge id.
                ee.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
                ee.parent = currEdge;
                entries.add(ee);
                prioQueue.add(ee);
            }

            if (ee==null)
                throw new IllegalStateException("Cannot happen");

            if (doUpdateBestPath)
                updateBestPathCore(iter, ee, traversalId);
        }
    }

    protected void updateBestPath(EdgeIteratorState edgeState, SPTEntry entryCurrent, int traversalId) {
        SPTEntry entryOther = bestWeightMapOther.get(traversalId);
        if (entryOther == null)
            return;

        boolean reverse = bestWeightMapFrom == bestWeightMapOther;

        // update μ
        double newWeight = entryCurrent.weight + entryOther.weight;

        if (newWeight < bestPath.getWeight()) {
            bestPath.setSwitchToFrom(reverse);
            bestPath.setSPTEntry(entryCurrent);
            bestPath.setWeight(newWeight);
            bestPath.setSPTEntryTo(entryOther);
        }
    }

    protected void updateBestPathCore(EdgeIteratorState iter, SPTEntry entryCurrent, int traversalId) {
        List<SPTEntry> entries = bestWeightMapOtherCore.get(traversalId);
        if (entries == null)
            return;

        boolean reverse = bestWeightMapFromCore == bestWeightMapOtherCore;

        ListIterator<SPTEntry> it = entries.listIterator();
        while (it.hasNext()) {
            SPTEntry entryOther = it.next();

            double newWeight = entryCurrent.weight + entryOther.weight;

            if (newWeight < bestPath.getWeight()) {
                double tmpWeight = reverse ?
                        turnWeighting.calcTurnWeight(entryOther.originalEdge, entryCurrent.adjNode, entryCurrent.originalEdge):
                        turnWeighting.calcTurnWeight(entryCurrent.originalEdge, entryCurrent.adjNode, entryOther.originalEdge);
                if (Double.isInfinite(tmpWeight))
                    continue;
                bestPath.setSwitchToFrom(reverse);
                bestPath.setSPTEntry(entryCurrent);
                bestPath.setWeight(newWeight);
                bestPath.setSPTEntryTo(entryOther);
            }
        }
    }



    @Override
    public String getName() {
        return Parameters.Algorithms.DIJKSTRA_BI;
    }
}
