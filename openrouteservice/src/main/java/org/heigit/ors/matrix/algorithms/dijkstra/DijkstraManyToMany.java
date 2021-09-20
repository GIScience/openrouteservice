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
package org.heigit.ors.matrix.algorithms.dijkstra;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Parameters;
import org.heigit.ors.routing.algorithms.AbstractManyToManyRoutingAlgorithm;
import org.heigit.ors.routing.algorithms.SubGraph;
import org.heigit.ors.routing.graphhopper.extensions.storages.AveragedMultiTreeSPEntry;
import org.heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntryItem;
import org.heigit.ors.routing.graphhopper.extensions.util.MultiSourceStoppingCriterion;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;

public class DijkstraManyToMany extends AbstractManyToManyRoutingAlgorithm {
    protected IntObjectMap<AveragedMultiTreeSPEntry> bestWeightMap;
    IntObjectMap<List<AveragedMultiTreeSPEntry>> bestWeightMapCore;
    IntObjectMap<AveragedMultiTreeSPEntry> targetMap;
    IntHashSet targetSet;
    protected PriorityQueue<AveragedMultiTreeSPEntry> prioQueue;
    private CHGraph chGraph;
    private IntHashSet coreExitPoints;
    protected AveragedMultiTreeSPEntry currEdge;
    private EdgeExplorer targetGraphExplorer;
    private MultiSourceStoppingCriterion stoppingCriterion;
    //TODO visited nodes
    private int visitedNodes;
    private int treeEntrySize;

    private boolean hasTurnWeighting = false;
    private int coreNodeLevel;
    private int turnRestrictedNodeLevel;
    protected boolean approximate = false;
    private TurnWeighting turnWeighting = null;
    private boolean swap = false;

    public DijkstraManyToMany(Graph graph, CHGraph chGraph, Weighting weighting, TraversalMode tMode) {
        super(graph, weighting, tMode);
        this.chGraph = chGraph;
        this.coreNodeLevel = chGraph.getNodes() + 1;
        this.turnRestrictedNodeLevel = this.coreNodeLevel + 1;
        int size = Math.min(Math.max(200, graph.getNodes() / 10), 2000);
        initCollections(size);
    }

    public DijkstraManyToMany(Graph graph, CHGraph chGraph, IntObjectMap<AveragedMultiTreeSPEntry> existingWeightMap, IntObjectMap<List<AveragedMultiTreeSPEntry>> existingCoreWeightMap, Weighting weighting, TraversalMode tMode) {
        this(graph, chGraph, weighting, tMode);
        bestWeightMap = existingWeightMap;
        bestWeightMapCore = existingCoreWeightMap;
    }

    protected void initCollections(int size) {
        prioQueue = new PriorityQueue<>(size);
        bestWeightMap = new GHIntObjectHashMap<>(size);
    }

    public void reset() {
        prioQueue.clear();
        bestWeightMap.clear();
    }

    public void prepare(int[] from, int[] to) {
        int targetsCount = to.length;
        this.coreExitPoints = new IntHashSet(targetsCount);

        for (int i = 0; i < to.length; ++i)
        {
            int nodeId = to[i];
            if (nodeId >= 0) {
                this.coreExitPoints.add(nodeId);
            }
        }
    }

    public AveragedMultiTreeSPEntry[] calcPaths(int[] from, int[] to) {
        if(from == null || to == null)
            throw new IllegalArgumentException("Input points are null");

        prepare(from, to);
        addEntriesFromMapToQueue();

        outEdgeExplorer = swap ? graph.createEdgeExplorer(DefaultEdgeFilter.inEdges(flagEncoder)) : graph.createEdgeExplorer(DefaultEdgeFilter.outEdges(flagEncoder));
        this.stoppingCriterion = new MultiSourceStoppingCriterion(targetSet, targetMap,treeEntrySize);

        runAlgo();
        return new AveragedMultiTreeSPEntry[0];
    }

    private void addEntriesFromMapToQueue(){
        for (IntObjectCursor<AveragedMultiTreeSPEntry> reachedNode : bestWeightMap)
            prioQueue.add(reachedNode.value);
    }

    protected void runAlgo() {
        EdgeExplorer explorer = swap? chGraph.createEdgeExplorer(DefaultEdgeFilter.inEdges(flagEncoder)) : chGraph.createEdgeExplorer(DefaultEdgeFilter.outEdges(flagEncoder));
        currEdge = prioQueue.poll();
        if(currEdge == null)
            return;

        while (!(isMaxVisitedNodesExceeded())){
            int currNode = currEdge.getAdjNode();
            boolean isCoreNode = isCoreNode(currNode);
            if(isCoreNode) {
                EdgeIterator iter = explorer.setBaseNode(currNode);
                exploreEntry(iter);
            }
            // If we find a core exit node or a node in the subgraph, explore it
            if (coreExitPoints.contains(currNode) || !isCoreNode) {
                EdgeIterator iter = targetGraphExplorer.setBaseNode(currNode);
                exploreEntryDownwards(iter);
            }
            updateTarget(currEdge);
            if (finishedDownwards() || prioQueue.isEmpty())
                break;
            currEdge = prioQueue.poll();
            if (currEdge == null)
                throw new AssertionError("Empty edge cannot happen");
        }
    }

    /**
     * Explore an entry, either for the turn restricted or not turn restricted case
     * @param iter
     */
    private void exploreEntry(EdgeIterator iter) {
        while (iter.next()) {
            if (considerTurnRestrictions(iter.getAdjNode())) {
                handleMultiEdgeCase(iter);
            }
            else {
                handleSingleEdgeCase(iter);
            }
        }
    }

    /**
     * Update the entry for the target in the targetMap. This is where the final results will be drawn from.
     * Also update the combinedUnsettled target if it exists.
     * @param update the entry to update a target from
     */
    private void updateTarget(AveragedMultiTreeSPEntry update) {
        int nodeId = update.getAdjNode();
        if(targetSet.contains(nodeId)) {
            if (!targetMap.containsKey(nodeId)) {
                AveragedMultiTreeSPEntry newTarget = new AveragedMultiTreeSPEntry(nodeId, EdgeIterator.NO_EDGE, Double.POSITIVE_INFINITY, false, null, update.getSize());
                newTarget.setOriginalEdge(EdgeIterator.NO_EDGE);
                newTarget.setSubItemOriginalEdgeIds(EdgeIterator.NO_EDGE);
                targetMap.put(nodeId, newTarget);
            }
            AveragedMultiTreeSPEntry target = targetMap.get(nodeId);
            boolean updated = false;
            for (int i = 0; i < treeEntrySize; ++i) {
                MultiTreeSPEntryItem targetItem = target.getItem(i);
                double targetWeight = targetItem.getWeight();

                MultiTreeSPEntryItem msptSubItem = update.getItem(i);
                double updateWeight = msptSubItem.getWeight();

                if (targetWeight > updateWeight) {
                    targetItem.setWeight(updateWeight);
                    targetItem.setEdge(msptSubItem.getEdge());
                    targetItem.setOriginalEdge(msptSubItem.getOriginalEdge());
                    targetItem.setParent(msptSubItem.getParent());
                    targetItem.setUpdate(true);
                    updated = true;
                }
            }
            if(updated)
                stoppingCriterion.updateCombinedUnsettled();
        }
    }

    /**
     * Search without turn restrictions
     * @param iter
     */
    private void handleSingleEdgeCase(EdgeIterator iter) {
        AveragedMultiTreeSPEntry entry = bestWeightMap.get(iter.getAdjNode());
        if (entry == null) {
            entry = createEmptyEntry(iter);
            boolean addToQueue = iterateMultiTree(iter, entry);
            if (addToQueue) {
                updateEntryInQueue(entry, true);
                bestWeightMap.put(iter.getAdjNode(), entry);
            }
        } else {
            boolean addToQueue = iterateMultiTree(iter, entry);
            if (addToQueue) {
                updateEntryInQueue(entry, false);
            }
        }
    }

    /**
     * Search with turn restrictions
     * @param iter
     */
    private void handleMultiEdgeCase(EdgeIterator iter) {
        AveragedMultiTreeSPEntry entry = null;
        List<AveragedMultiTreeSPEntry> entries = bestWeightMapCore.get(iter.getAdjNode());

        //Select or generate edge based entry list and entry
        if (entries == null)
            entries = createEntriesList(iter);
        else
            entry = getEdgeEntry(iter, entries);
        //Handle entry
        if (entry == null) {
            entry = createEmptyEntry(iter);
            boolean addToQueue = iterateMultiTree(iter, entry);
            if (addToQueue) {
                entries.add(entry);
                updateEntryInQueue(entry, true);
            }

        } else {
            boolean addToQueue = iterateMultiTree(iter, entry);
            if (addToQueue) {
                updateEntryInQueue(entry, false);
            }
        }
    }

    private boolean iterateMultiTree(EdgeIterator iter, AveragedMultiTreeSPEntry entry) {
        boolean addToQueue = false;
        visitedNodes++;

        for (int source = 0; source < treeEntrySize; ++source) {
            MultiTreeSPEntryItem currEdgeItem = this.currEdge.getItem(source);
            double entryWeight = currEdgeItem.getWeight();

            if (entryWeight == Double.POSITIVE_INFINITY || !currEdgeItem.isUpdate())
                continue;

            if (stoppingCriterion.isEntryLargerThanAllTargets(source, entryWeight))
                continue;

            MultiTreeSPEntryItem msptSubItem = entry.getItem(source);
            if (!accept(iter, currEdgeItem.getEdge()))
                continue;

            configureTurnWeighting(iter, currEdgeItem);
            double edgeWeight = weighting.calcWeight(iter, swap, currEdgeItem.getOriginalEdge());
            resetTurnWeighting();
            if (edgeWeight == Double.POSITIVE_INFINITY)
                continue;

            double tmpWeight = edgeWeight + entryWeight;
            if (stoppingCriterion.isEntryLargerThanAllTargets(source, tmpWeight))
                continue;

            if (msptSubItem.getWeight() > tmpWeight) {
                msptSubItem.setWeight(tmpWeight);
                msptSubItem.setEdge(iter.getEdge());
                msptSubItem.setOriginalEdge(EdgeIteratorStateHelper.getOriginalEdge(iter));
                msptSubItem.setParent(this.currEdge);
                msptSubItem.setUpdate(true);
                addToQueue = true;
            }
        }
        return addToQueue;
    }

    private void exploreEntryDownwards(EdgeIterator iter) {
        currEdge.resetUpdate(true);
        currEdge.setVisited(true);
        if (iter == null)
            return;

        while (iter.next()) {
            AveragedMultiTreeSPEntry entry = bestWeightMap.get(iter.getAdjNode());

            if (entry == null) {
                entry = createEmptyEntry(iter);
                boolean addToQueue = iterateMultiTreeDownwards(currEdge, iter, entry, false);
                if(addToQueue) {
                    bestWeightMap.put(iter.getAdjNode(), entry);
                    updateEntryInQueue(entry, true);
                }
            } else {
                boolean addToQueue = iterateMultiTreeDownwards(currEdge, iter, entry, false);
                if (!entry.isVisited() || addToQueue) {
                    // This is the case if the node has been assigned a weight in
                    // the upwards pass (fillEdges). We need to use it in the
                    // downwards pass to access lower level nodes, though
                    // the weight does not have to be reset necessarily
                    updateEntryInQueue(entry, false);
                }
            }
            if(hasTurnWeighting)
                turnWeighting.setInORS(true);
        }
    }

    private boolean iterateMultiTreeDownwards(AveragedMultiTreeSPEntry currEdge, EdgeIterator iter, AveragedMultiTreeSPEntry adjEntry, boolean checkUpdate) {
        boolean addToQueue = false;
        visitedNodes++;

        for (int source = 0; source < treeEntrySize; ++source) {
            MultiTreeSPEntryItem currEdgeItem = currEdge.getItem(source);
            double entryWeight = currEdgeItem.getWeight();

            if (entryWeight == Double.POSITIVE_INFINITY || (checkUpdate && !currEdgeItem.isUpdate()))
                continue;
            if (stoppingCriterion.isEntryLargerThanAllTargets(source, entryWeight))
                continue;

            double edgeWeight;

            if(hasTurnWeighting && !isInORS(((SubGraph.EdgeIteratorLinkIterator) iter).getCurrState(), currEdgeItem))
                turnWeighting.setInORS(false);
            edgeWeight = weighting.calcWeight(((SubGraph.EdgeIteratorLinkIterator) iter).getCurrState(), swap, currEdgeItem.getOriginalEdge());
            if(Double.isInfinite(edgeWeight))
                continue;
            double tmpWeight = edgeWeight + entryWeight;

            if (stoppingCriterion.isEntryLargerThanAllTargets(source, tmpWeight))
                continue;

            MultiTreeSPEntryItem eeItem = adjEntry.getItem(source);

            if (eeItem.getWeight() > tmpWeight) {
                eeItem.setWeight(tmpWeight);
                eeItem.setEdge(iter.getEdge());
                eeItem.setOriginalEdge(EdgeIteratorStateHelper.getOriginalEdge(iter));
                eeItem.setParent(currEdge);
                eeItem.setUpdate(true);
                addToQueue = true;
            }
            if(hasTurnWeighting)
                turnWeighting.setInORS(true);
        }
        return addToQueue;
    }

    private AveragedMultiTreeSPEntry createEmptyEntry(EdgeIterator iter) {
        return new AveragedMultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), Double.POSITIVE_INFINITY, false, null, currEdge.getSize());
    }

    /**
     * Update an existing entry in the priority queue
     * @param entry entry to update
     */
    private void updateEntryInQueue(AveragedMultiTreeSPEntry entry, boolean isNewEntry) {
        if(!isNewEntry)
            prioQueue.remove(entry);
        entry.updateWeights();
        prioQueue.add(entry);
    }

    /**
     * Select the entry from the entries list that corresponds to the current edge. This is based on adj node and edge id.
     * @param iter the entry to select
     * @param entries the list to select from
     * @return the entry in the list or null if does not exist
     */
    private AveragedMultiTreeSPEntry getEdgeEntry(EdgeIterator iter, List<AveragedMultiTreeSPEntry> entries) {
        AveragedMultiTreeSPEntry entry = null;
        ListIterator<AveragedMultiTreeSPEntry> it = entries.listIterator();
        while (it.hasNext()) {
            AveragedMultiTreeSPEntry listEntry = it.next();
            if (listEntry.getEdge() == iter.getEdge()) {
                entry = listEntry;
                break;
            }
        }
        return entry;
    }

    /**
     * Generate the list of entries for a given node. Initialize the target node in the normal weight map if none exists
     * @param iter Iterator with adj node to initialize
     * @return list of entries
     */
    private List<AveragedMultiTreeSPEntry> createEntriesList(EdgeIterator iter) {
        List<AveragedMultiTreeSPEntry> entries;
        entries = initBestWeightMapEntryList(bestWeightMapCore, iter.getAdjNode());
        //Initialize target entry in normal weight map
        if(coreExitPoints.contains(iter.getAdjNode())){
            AveragedMultiTreeSPEntry target = bestWeightMap.get(iter.getAdjNode());
            if (target == null) {
                target = createEmptyEntry(iter);
                bestWeightMap.put(iter.getAdjNode(), target);
            }
        }
        return entries;
    }

    List<AveragedMultiTreeSPEntry> initBestWeightMapEntryList(IntObjectMap<List<AveragedMultiTreeSPEntry>> map, int traversalId) {
        if (map.get(traversalId) != null)
            throw new IllegalStateException("Core entry point already exists in best weight map.");

        List<AveragedMultiTreeSPEntry> entryList = new ArrayList<>(5);
        map.put(traversalId, entryList);

        return entryList;
    }

    /**
     *
     * @return whether all goal nodes have been found
     */
    private boolean finishedDownwards() {
        //First check whether all targets found for all sources
        return stoppingCriterion.isFinished(currEdge, prioQueue);

    }

    private void configureTurnWeighting(EdgeIterator iter, MultiTreeSPEntryItem currEdgeItem) {
        if(hasTurnWeighting && !isInORS(iter, currEdgeItem))
            turnWeighting.setInORS(false);
    }

    private void resetTurnWeighting() {
        if(hasTurnWeighting)
            turnWeighting.setInORS(true);
    }

    /**
     * Check whether the turnWeighting should be in the inORS mode. If one of the edges is a virtual one, we need the original edge to get the turn restriction.
     * If the two edges are actually virtual edges on the same original edge, we want to disable inORS mode so that they are not regarded as u turn,
     * because the same edge id left and right of a virtual node results in a u turn
     * @param iter
     * @param currEdgeItem
     * @return
     */
    private boolean isInORS(EdgeIteratorState iter, MultiTreeSPEntryItem currEdgeItem) {
        return currEdgeItem.getEdge() == iter.getEdge()
                || currEdgeItem.getOriginalEdge() != EdgeIteratorStateHelper.getOriginalEdge(iter);
    }

    public void setTurnWeighting(TurnWeighting turnWeighting) {
        this.turnWeighting = turnWeighting;
    }

    public void setTargetGraphExplorer(EdgeExplorer targetGraphExplorer) {
        this.targetGraphExplorer = targetGraphExplorer;
    }

    public void setTargetMap(IntObjectMap<AveragedMultiTreeSPEntry> targetMap) {
        this.targetMap = targetMap;
    }

    public void setTargetSet(IntHashSet targetSet) {
        this.targetSet = targetSet;
    }

    boolean considerTurnRestrictions(int node) {
        if (!hasTurnWeighting)
            return false;
        if (approximate)
            return isTurnRestrictedNode(node);
        return true;
    }

    boolean isCoreNode(int node) {
        return chGraph.getLevel(node) >= coreNodeLevel;
    }

    boolean isTurnRestrictedNode(int node) {
        return chGraph.getLevel(node) == turnRestrictedNodeLevel;
    }

    public void setHasTurnWeighting(boolean hasTurnWeighting) {
        this.hasTurnWeighting = hasTurnWeighting;
    }

    public void setTreeEntrySize(int entrySize){
        this.treeEntrySize = entrySize;
    }

    public void setSwap(boolean swap) {
        this.swap = swap;
    }


    @Override
    public int getVisitedNodes() {
        return visitedNodes;
    }

    @Override
    public void setMaxVisitedNodes(int numberOfNodes) {
        this.maxVisitedNodes = numberOfNodes;
    }

    public void setVisitedNodes(int numberOfNodes) {
        this.visitedNodes = numberOfNodes;
    }

    @Override
    protected boolean isMaxVisitedNodesExceeded() {
        return this.maxVisitedNodes < this.visitedNodes;
    }

    @Override
    public String getName() {
        return Parameters.Algorithms.DIJKSTRA;
    }
}
