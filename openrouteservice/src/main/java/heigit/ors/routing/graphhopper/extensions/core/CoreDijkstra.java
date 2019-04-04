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
package heigit.ors.routing.graphhopper.extensions.core;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.*;

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

    public CoreDijkstra(Graph graph, Weighting weighting, TraversalMode tMode) {
        super(graph, weighting, tMode);
    }

    @Override
    protected void initCollections(int size) {
        fromPriorityQueueCH = new PriorityQueue<SPTEntry>(size);
        bestWeightMapFrom = new GHIntObjectHashMap<SPTEntry>(size);

        toPriorityQueueCH = new PriorityQueue<SPTEntry>(size);
        bestWeightMapTo = new GHIntObjectHashMap<SPTEntry>(size);

        fromPriorityQueueCore = new PriorityQueue<>(size);
        toPriorityQueueCore = new PriorityQueue<>(size);
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

    @Override
    public boolean fillEdgesTo() {
        if (toPriorityQueueCH.isEmpty())
            return false;

        currTo = toPriorityQueueCH.poll();

        if (!inCore && chGraph.getLevel(currTo.adjNode) == coreNodeLevel) {
            // core entry point, do not relax its edges
            toPriorityQueueCore.add(currTo);
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
        finishedTo = fromPriorityQueueCH.isEmpty();

        if (!finishedFrom)
            currFrom = fromPriorityQueueCH.peek();

        if (!finishedTo)
            currTo = toPriorityQueueCH.peek();

        while (!finishedPhase2() && !isMaxVisitedNodesExceeded()) {
            if (!finishedFrom)
                finishedFrom = !fillEdgesFrom();

            if (!finishedTo)
                finishedTo = !fillEdgesTo();
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


    @Override
    public String getName() {
        return Parameters.Algorithms.DIJKSTRA_BI;
    }
}
