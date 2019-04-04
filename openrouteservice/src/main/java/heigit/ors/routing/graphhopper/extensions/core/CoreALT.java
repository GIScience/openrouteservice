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

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.BeelineWeightApproximator;
import com.graphhopper.routing.weighting.ConsistentWeightApproximator;
import com.graphhopper.routing.weighting.WeightApproximator;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.*;

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
    protected AStarEntry currFrom;
    protected AStarEntry currTo;
    protected IntObjectMap<AStarEntry> bestWeightMapFrom;
    protected IntObjectMap<AStarEntry> bestWeightMapTo;
    private IntObjectMap<AStarEntry> bestWeightMapOther;
    private ConsistentWeightApproximator weightApprox;
    private IntHashSet ignoreExplorationFrom = new IntHashSet();
    private IntHashSet ignoreExplorationTo = new IntHashSet();

    private PriorityQueue<AStarEntry> fromPriorityQueueCH;
    private PriorityQueue<AStarEntry> toPriorityQueueCH;
    private PriorityQueue<AStarEntry> fromPriorityQueueCore;
    private PriorityQueue<AStarEntry> toPriorityQueueCore;

    int from, to, fromProxy, toProxy;

    double approximatorOffset;


    public CoreALT(Graph graph, Weighting weighting, TraversalMode tMode) {
        super(graph, weighting, tMode);
        BeelineWeightApproximator defaultApprox = new BeelineWeightApproximator(nodeAccess, weighting);
        defaultApprox.setDistanceCalc(Helper.DIST_PLANE);
        setApproximation(defaultApprox);
        visitedEdgesALTCount = 0;
    }

    @Override
    protected void initCollections(int size) {
        fromPriorityQueueCH = new PriorityQueue<AStarEntry>(size);
        bestWeightMapFrom = new GHIntObjectHashMap<AStarEntry>(size);

        toPriorityQueueCH = new PriorityQueue<AStarEntry>(size);
        bestWeightMapTo = new GHIntObjectHashMap<AStarEntry>(size);

        fromPriorityQueueCore = new PriorityQueue<>(size);
        toPriorityQueueCore = new PriorityQueue<>(size);
    }

    /**
     * @param approx if true it enables approximate distance calculation from lat,lon values
     */
    public CoreALT setApproximation(WeightApproximator approx) {
        weightApprox = new ConsistentWeightApproximator(approx);
        return this;
    }

    @Override
    protected SPTEntry createSPTEntry(int node, double weight) {
        throw new IllegalStateException("use AStarEdge constructor directly");
    }

    @Override
    public void initFrom(int from, double weight) {
        this.from = from;
        currFrom = new AStarEntry(EdgeIterator.NO_EDGE, from, weight, weight);
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
        this.to = to;
        currTo = new AStarEntry(EdgeIterator.NO_EDGE, to, weight, weight);
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

    /**
     * Run the ALT algo in the core
     */
    @Override
    void runPhase2() {
        // re-init queues
        finishedFrom = fromPriorityQueueCore.isEmpty();
        finishedTo = toPriorityQueueCore.isEmpty();

        if (!finishedFrom && !finishedTo) {
            toProxy = toPriorityQueueCore.peek().adjNode;
            fromProxy = fromPriorityQueueCore.peek().adjNode;

            initApproximator();

            recalculateWeights(fromPriorityQueueCore, false);
            recalculateWeights(toPriorityQueueCore, true);

            currTo = toPriorityQueueCore.peek();
            currFrom = fromPriorityQueueCore.peek();
        }

        while (!finishedPhase2() && !isMaxVisitedNodesExceeded()) {
            if (!finishedFrom)
                finishedFrom = !fillEdgesFromALT();

            if (!finishedTo)
                finishedTo = !fillEdgesToALT();
        }

    }

    private void initApproximator() {
        if (weightApprox.getApproximation() instanceof CoreLMApproximator && weightApprox.getReverseApproximation() instanceof CoreLMApproximator) {
            CoreLMApproximator forwardApproximator = (CoreLMApproximator) weightApprox.getApproximation();
            forwardApproximator.setTo(toProxy);
            // AO: when ConsistentWeight Approximator is used it is not necessary to account for proxy weights as any constant terms cancel out

            boolean activeLandmarksSet = forwardApproximator.initActiveLandmarks(fromProxy);

            CoreLMApproximator reverseApproximator = (CoreLMApproximator) weightApprox.getReverseApproximation();
            reverseApproximator.setTo(fromProxy);

            // AO: typically the optimal landmarks set for the forward approximator is the same as for the reverse one so there is no need to recompute them
            if (activeLandmarksSet)
                reverseApproximator.setActiveLandmarks(forwardApproximator.getActiveLandmarks());
            else
                reverseApproximator.initActiveLandmarks(toProxy);

            approximatorOffset = 2.0D * forwardApproximator.getfFactor();
        }
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
        return currFrom.weight + currTo.weight >= bestPath.getWeight() + approximatorOffset;
    }

    void fillEdges(AStarEntry currEdge, PriorityQueue<AStarEntry> prioQueue, IntObjectMap<AStarEntry> bestWeightMap,
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
            AStarEntry aStarEntry = bestWeightMap.get(traversalId);
            if (aStarEntry == null) {
                aStarEntry = new AStarEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight, tmpWeight);
                // Modification by Maxim Rylov: Assign the original edge id.
                aStarEntry.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
                aStarEntry.parent = currEdge;
                bestWeightMap.put(traversalId, aStarEntry);
                prioQueue.add(aStarEntry);
            } else if (aStarEntry.weight > tmpWeight) {
                prioQueue.remove(aStarEntry);
                aStarEntry.edge = iter.getEdge();
                aStarEntry.weight = tmpWeight;
                aStarEntry.weightOfVisitedPath = tmpWeight;
                aStarEntry.parent = currEdge;
                prioQueue.add(aStarEntry);
            } else
                continue;

            if (updateBestPath)
                updateBestPath(iter, aStarEntry, traversalId);
        }
    }

    boolean fillEdgesFromALT() {
        if (fromPriorityQueueCore.isEmpty())
            return false;

        currFrom = fromPriorityQueueCore.poll();
        bestWeightMapOther = bestWeightMapTo;
        fillEdgesALT(currFrom, fromPriorityQueueCore, bestWeightMapFrom, ignoreExplorationFrom, outEdgeExplorer, false);
        visitedCountFrom2++;
        return true;
    }

    boolean fillEdgesToALT() {
        if (toPriorityQueueCore.isEmpty())
            return false;

        currTo = toPriorityQueueCore.poll();
        bestWeightMapOther = bestWeightMapFrom;
        fillEdgesALT(currTo, toPriorityQueueCore, bestWeightMapTo, ignoreExplorationTo, inEdgeExplorer, true);
        visitedCountTo2++;
        return true;
    }

    private void fillEdgesALT(AStarEntry currEdge, PriorityQueue<AStarEntry> prioQueueOpenSet,
                              IntObjectMap<AStarEntry> bestWeightMap, IntHashSet ignoreExploration, EdgeExplorer explorer,
                              boolean reverse) {

        int currNode = currEdge.adjNode;
        EdgeIterator iter = explorer.setBaseNode(currNode);
        while (iter.next()) {
            if (!accept(iter, currEdge.edge))
                continue;
            visitedEdgesALTCount++;


            int neighborNode = iter.getAdjNode();
            int traversalId = traversalMode.createTraversalId(iter, reverse);
            if (ignoreExploration.contains(traversalId))
                continue;

            // TODO performance: check if the node is already existent in the opposite direction
            // then we could avoid the approximation as we already know the exact complete path!
            // Modification by Maxim Rylov: use originalEdge as the previousEdgeId
            double alreadyVisitedWeight = weighting.calcWeight(iter, reverse, currEdge.originalEdge)
                    + currEdge.getWeightOfVisitedPath();
            if (Double.isInfinite(alreadyVisitedWeight))
                continue;

            AStarEntry aStarEntry = bestWeightMap.get(traversalId);
            if (aStarEntry == null || aStarEntry.getWeightOfVisitedPath() > alreadyVisitedWeight) {
                double currWeightToGoal = weightApprox.approximate(neighborNode, reverse);
                double estimationFullWeight = alreadyVisitedWeight + currWeightToGoal;
                if (aStarEntry == null) {
                    aStarEntry = new AStarEntry(iter.getEdge(), neighborNode, estimationFullWeight, alreadyVisitedWeight);
                    // Modification by Maxim Rylov: assign originalEdge
                    aStarEntry.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
                    bestWeightMap.put(traversalId, aStarEntry);
                } else {
                    prioQueueOpenSet.remove(aStarEntry);
                    aStarEntry.edge = iter.getEdge();
                    aStarEntry.weight = estimationFullWeight;
                    aStarEntry.weightOfVisitedPath = alreadyVisitedWeight;
                }

                aStarEntry.parent = currEdge;
                prioQueueOpenSet.add(aStarEntry);

                if (updateBestPath)
                    updateBestPath(iter, aStarEntry, traversalId);
            }
        }
    }

    protected void updateBestPath(EdgeIteratorState edgeState, AStarEntry entryCurrent, int traversalId) {
        AStarEntry entryOther = bestWeightMapOther.get(traversalId);
        if (entryOther == null)
            return;

        boolean reverse = bestWeightMapFrom == bestWeightMapOther;

        // update μ
        double newWeight = entryCurrent.weightOfVisitedPath + entryOther.weightOfVisitedPath;
        if (traversalMode.isEdgeBased()) {
            if (entryOther.edge != entryCurrent.edge)
                throw new IllegalStateException("cannot happen for edge based execution of " + getName());

            if (entryOther.adjNode != entryCurrent.adjNode) {
                // prevents the path to contain the edge at the meeting point twice and subtract the weight (excluding turn weight => no previous edge)
                entryCurrent = (AStarEntry) entryCurrent.parent;
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

    public static class AStarEntry extends SPTEntry {
        double weightOfVisitedPath;

        public AStarEntry(int edgeId, int adjNode, double weightForHeap, double weightOfVisitedPath) {
            super(edgeId, adjNode, weightForHeap);
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
