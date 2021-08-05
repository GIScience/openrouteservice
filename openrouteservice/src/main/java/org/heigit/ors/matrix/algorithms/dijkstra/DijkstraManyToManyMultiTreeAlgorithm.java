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

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntCursor;
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
import org.heigit.ors.routing.graphhopper.extensions.storages.MinimumWeightMultiTreeSPEntry;
import org.heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntry;
import org.heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;

public class DijkstraManyToManyMultiTreeAlgorithm extends AbstractManyToManyRoutingAlgorithm {
    protected IntObjectMap<MinimumWeightMultiTreeSPEntry> bestWeightMap;
    IntObjectMap<List<MinimumWeightMultiTreeSPEntry>> bestWeightMapCore;
    IntObjectMap<MinimumWeightMultiTreeSPEntry> targetMap;
    IntHashSet targetSet;
    protected PriorityQueue<MinimumWeightMultiTreeSPEntry> prioQueue;
    private CHGraph chGraph;
    private IntHashSet coreExitPoints;
    private IntObjectMap<Boolean> foundCoreExitPoints;
    private IntObjectMap<Boolean> foundTargets;
    protected MinimumWeightMultiTreeSPEntry currEdge;
    private EdgeExplorer targetGraphExplorer;
    //TODO visited nodes
    private int maxVisitedNodes = Integer.MAX_VALUE;
    private int visitedNodes;
    private int treeEntrySize;

    private boolean hasTurnWeighting = false;
    private int coreNodeLevel;
    private int turnRestrictedNodeLevel;
    protected boolean approximate = false;
    private TurnWeighting turnWeighting = null;

    public DijkstraManyToManyMultiTreeAlgorithm(Graph graph, CHGraph chGraph, Weighting weighting, TraversalMode tMode) {
        super(graph, weighting, tMode);
        this.chGraph = chGraph;
        this.coreNodeLevel = chGraph.getNodes() + 1;
        this.turnRestrictedNodeLevel = this.coreNodeLevel + 1;
        int size = Math.min(Math.max(200, graph.getNodes() / 10), 2000);
        initCollections(size);
    }

    public DijkstraManyToManyMultiTreeAlgorithm(Graph graph, CHGraph chGraph, IntObjectMap<MinimumWeightMultiTreeSPEntry> existingWeightMap, IntObjectMap<List<MinimumWeightMultiTreeSPEntry>> existingCoreWeightMap, Weighting weighting, TraversalMode tMode) {
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
        this.foundCoreExitPoints = new IntObjectHashMap<>(targetsCount);
        this.foundTargets = new IntObjectHashMap<>(targetSet.size());

        for (int i = 0; i < to.length; ++i)
        {
            int nodeId = to[i];
            if (nodeId >= 0) {
                this.coreExitPoints.add(nodeId);
                this.foundCoreExitPoints.put(nodeId, false);
            }
        }
        for (IntCursor entry : targetSet)
        {
            int nodeId = entry.value;
            if (nodeId >= 0) {
                this.foundTargets.put(nodeId, false);
            }
        }
    }

    public void setHasTurnWeighting(boolean hasTurnWeighting) {
        this.hasTurnWeighting = hasTurnWeighting;
    }

    public void setTreeEntrySize(int entrySize){
        this.treeEntrySize = entrySize;
    }

    private void addEntriesFromMapToQueue(int[] from){
//        for (int j : from) {
//            if ((j != -1)) {
                //If two queried points are on the same node, this case can occur
        for (IntObjectCursor<MinimumWeightMultiTreeSPEntry> reachedNode : bestWeightMap) {
            prioQueue.add(reachedNode.value);
        }
//                if (existing == null) {
//                    throw new IllegalStateException("Node " + j + " was not found in existing weight map");
//                }
//                prioQueue.add(existing);
//            }
//        }
    }

    public MinimumWeightMultiTreeSPEntry[] calcPaths(int[] from, int[] to) {
        if(from == null || to == null)
            throw new IllegalArgumentException("Input points are null");

        if(from.length == 0 || to.length == 0)
            return new MinimumWeightMultiTreeSPEntry[]{};

        prepare(from, to);
        addEntriesFromMapToQueue(from);

        outEdgeExplorer = graph.createEdgeExplorer(DefaultEdgeFilter.outEdges(flagEncoder));

        runAlgo();
        List<MinimumWeightMultiTreeSPEntry> targetEntries = new ArrayList<>(from.length + to.length);
        for (int i = 0; i < from.length; ++i) {
            if(!hasTurnWeighting) {
                targetEntries.add(bestWeightMap.get(from[i]));
            }
            else {
                List<MinimumWeightMultiTreeSPEntry> singleEntry = bestWeightMapCore.get(from[i]);
                if (singleEntry != null)
                    targetEntries.addAll(singleEntry);
            }
        }
        for (int i = 0; i < to.length; ++i) {
            if(!hasTurnWeighting) {
                targetEntries.add(bestWeightMap.get(to[i]));
            }
            else {
                List<MinimumWeightMultiTreeSPEntry> singleEntry = bestWeightMapCore.get(to[i]);
                if (singleEntry != null)
                    targetEntries.addAll(singleEntry);
            }
        }
        MinimumWeightMultiTreeSPEntry[] extractedTargets = new MinimumWeightMultiTreeSPEntry[targetEntries.size()];
//        int testNodeId = 8270;
//        List<MinimumWeightMultiTreeSPEntry> entries = bestWeightMapCore.get(testNodeId);

        return targetEntries.toArray(extractedTargets);
    }

    protected void runAlgo() {
        EdgeExplorer explorer = chGraph.createEdgeExplorer(DefaultEdgeFilter.outEdges(flagEncoder));
        currEdge = prioQueue.poll();
        if(currEdge == null)
            return;

        while (!(isMaxVisitedNodesExceeded())){// || finishedCore())) {
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

            if (finishedDownwards() || prioQueue.isEmpty()) {
                break;
            }

            currEdge = prioQueue.poll();
            if (currEdge == null)
                throw new AssertionError("Empty edge cannot happen");
            visitedNodes++;
        }
        System.out.println("visited nodes: " + visitedNodes);
    }

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

    private void exploreEntryDownwards(EdgeIterator iter) {
        currEdge.resetUpdate(true);
        currEdge.setVisited(true);
        if (iter == null)
            return;

        while (iter.next()) {
            MinimumWeightMultiTreeSPEntry entry = bestWeightMap.get(iter.getAdjNode());

            if (entry == null) {
                entry = new MinimumWeightMultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), Double.POSITIVE_INFINITY, true, null, currEdge.getSize());
//                entry.setVisited(true);
                boolean addToQueue = iterateMultiTreeDownwards(currEdge, iter, entry, false);
                entry.updateWeights();
                if(addToQueue) {
                    bestWeightMap.put(iter.getAdjNode(), entry);
                    prioQueue.add(entry);
                    updateTarget(entry);
                }
            } else {
                boolean addToQueue = iterateMultiTreeDownwards(currEdge, iter, entry, false);
                if (!entry.isVisited()) {
                    // This is the case if the node has been assigned a weight in
                    // the upwards pass (fillEdges). We need to use it in the
                    // downwards pass to access lower level nodes, though
                    // the weight does not have to be reset necessarily
//                    entry.setVisited(true);
                    prioQueue.remove(entry);
                    entry.updateWeights();
                    prioQueue.add(entry);
                    updateTarget(entry);
                } else
                if (addToQueue) {
//                    entry.setVisited(true);
                    prioQueue.remove(entry);
                    entry.updateWeights();
                    prioQueue.add(entry);
                    updateTarget(entry);
                }
            }
            if(hasTurnWeighting)
                turnWeighting.setInORS(true);
        }
    }

    private boolean iterateMultiTreeDownwards(MinimumWeightMultiTreeSPEntry currEdge, EdgeIterator iter, MinimumWeightMultiTreeSPEntry adjEntry, boolean checkUpdate) {
        boolean addToQueue = false;
        for (int i = 0; i < treeEntrySize; ++i) {
            MultiTreeSPEntryItem currEdgeItem = currEdge.getItem(i);
            double entryWeight = currEdgeItem.getWeight();

            if (entryWeight == Double.POSITIVE_INFINITY || (checkUpdate && !currEdgeItem.isUpdate()))
                continue;
            double edgeWeight;

            if(hasTurnWeighting && !isInORS(((SubGraph.EdgeIteratorLinkIterator) iter).getCurrState(), currEdgeItem))
                turnWeighting.setInORS(false);
            edgeWeight = weighting.calcWeight(((SubGraph.EdgeIteratorLinkIterator) iter).getCurrState(), false, currEdgeItem.getOriginalEdge());
            if(Double.isInfinite(edgeWeight))
                continue;
            double tmpWeight = edgeWeight + entryWeight;

            MultiTreeSPEntryItem eeItem = adjEntry.getItem(i);
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

    private void updateTarget(MinimumWeightMultiTreeSPEntry update) {
        int nodeId = update.getAdjNode();
        if(targetSet.contains(nodeId)) {
            if (!targetMap.containsKey(nodeId)) {
                MinimumWeightMultiTreeSPEntry newTarget = new MinimumWeightMultiTreeSPEntry(nodeId, EdgeIterator.NO_EDGE, Double.POSITIVE_INFINITY, true, null, update.getSize());
                newTarget.setOriginalEdge(EdgeIterator.NO_EDGE);
                newTarget.setSubItemOriginalEdgeIds(EdgeIterator.NO_EDGE);
                targetMap.put(nodeId, newTarget);
            }
            MinimumWeightMultiTreeSPEntry target = targetMap.get(nodeId);
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
                }
            }
        }
    }

    private void handleSingleEdgeCase(EdgeIterator iter) {
        MinimumWeightMultiTreeSPEntry entry = bestWeightMap.get(iter.getAdjNode());
        if (entry == null) {
            entry = createEmptyEntry(iter);
            boolean addToQueue = iterateMultiTree(iter, entry);
            if (addToQueue) {
                updateEntryInQueue(entry);
                bestWeightMap.put(iter.getAdjNode(), entry);
            }
        } else {
            boolean addToQueue = iterateMultiTree(iter, entry);
            if (addToQueue) {
                updateEntryInQueue(entry);
            }
        }
    }

    private void handleMultiEdgeCase(EdgeIterator iter) {
        MinimumWeightMultiTreeSPEntry entry = null;
        List<MinimumWeightMultiTreeSPEntry> entries = bestWeightMapCore.get(iter.getAdjNode());

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
                updateEntryInQueue(entry);
            }

        } else {
            boolean addToQueue = iterateMultiTree(iter, entry);
            if (addToQueue) {
                updateEntryInQueue(entry);
            }
        }
    }

    private MinimumWeightMultiTreeSPEntry createEmptyEntry(EdgeIterator iter) {
        return new MinimumWeightMultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), Double.POSITIVE_INFINITY, true, null, currEdge.getSize());
    }

    /**
     * Update an existing entry in the priority queue
     * @param entry entry to update
     */
    private void updateEntryInQueue(MinimumWeightMultiTreeSPEntry entry) {
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
    private MinimumWeightMultiTreeSPEntry getEdgeEntry(EdgeIterator iter, List<MinimumWeightMultiTreeSPEntry> entries) {
        MinimumWeightMultiTreeSPEntry entry = null;
        ListIterator<MinimumWeightMultiTreeSPEntry> it = entries.listIterator();
        while (it.hasNext()) {
            MinimumWeightMultiTreeSPEntry listEntry = it.next();
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
    private List<MinimumWeightMultiTreeSPEntry> createEntriesList(EdgeIterator iter) {
        List<MinimumWeightMultiTreeSPEntry> entries;
        entries = initBestWeightMapEntryList(bestWeightMapCore, iter.getAdjNode());
        //Initialize target entry in normal weight map
        if(coreExitPoints.contains(iter.getAdjNode())){
            MinimumWeightMultiTreeSPEntry target = bestWeightMap.get(iter.getAdjNode());
            if (target == null) {
                target = createEmptyEntry(iter);
                bestWeightMap.put(iter.getAdjNode(), target);
            }
        }
        return entries;
    }

    List<MinimumWeightMultiTreeSPEntry> initBestWeightMapEntryList(IntObjectMap<List<MinimumWeightMultiTreeSPEntry>> map, int traversalId) {
        if (map.get(traversalId) != null)
            throw new IllegalStateException("Core entry point already exists in best weight map.");

        List<MinimumWeightMultiTreeSPEntry> entryList = new ArrayList<>(5);// TODO: Proper assessment of the optimal size
        map.put(traversalId, entryList);

        return entryList;
    }

    private boolean iterateMultiTree(EdgeIterator iter, MinimumWeightMultiTreeSPEntry entry) {
        boolean addToQueue = false;

        for (int i = 0; i < treeEntrySize; ++i) {
            MultiTreeSPEntryItem currEdgeItem = this.currEdge.getItem(i);
            double entryWeight = currEdgeItem.getWeight();

            if (entryWeight == Double.POSITIVE_INFINITY || !currEdgeItem.isUpdate())
                continue;

            MultiTreeSPEntryItem msptSubItem = entry.getItem(i);
            if (!accept(iter, currEdgeItem.getEdge()))
                continue;

            configureTurnWeighting(iter, currEdgeItem);
            double edgeWeight = weighting.calcWeight(iter, false, currEdgeItem.getOriginalEdge());
            resetTurnWeighting();
            if (edgeWeight == Double.POSITIVE_INFINITY)
                continue;
            double tmpWeight = edgeWeight + entryWeight;

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

    /**
     *
     * @return whether all coreExitPoints have been settled
     */
    private boolean finishedCore() {
        if (!coreExitPoints.contains(currEdge.getAdjNode()))
            return false;
        // Check whether all paths found
        for (int i = 0; i < treeEntrySize; ++i) {
            MultiTreeSPEntryItem msptItem = currEdge.getItem(i);
            double entryWeight = msptItem.getWeight();

            if (entryWeight == Double.POSITIVE_INFINITY)
                return false;
        }
        // Check whether a shorter path to one entry can be found
        if(couldExistShorterPath())
            return false;
        updateCoreExitFound(currEdge.getAdjNode());
        return allExitsFound();
    }

    /**
     *
     * @return whether all goal nodes have been found
     */
    private boolean finishedDownwards() {
        if (!targetSet.contains(currEdge.getAdjNode()))
            return false;
        // Check whether all paths found
        for (int i = 0; i < treeEntrySize; ++i) {
            MultiTreeSPEntryItem msptItem = currEdge.getItem(i);
            double entryWeight = msptItem.getWeight();

            if (entryWeight == Double.POSITIVE_INFINITY)
                return false;
        }
        // Check whether a shorter path to one entry can be found
        if(couldExistShorterPath())
            return false;
        updateTargetFound(currEdge.getAdjNode());
        return allTargetsFound();
    }

    private void updateCoreExitFound(int node){
        this.foundCoreExitPoints.put(node, true);
    }

    private void updateTargetFound(int node){
        this.foundTargets.put(node, true);
    }

    private boolean allExitsFound(){
        for (IntObjectCursor<Boolean> entry : this.foundCoreExitPoints) {
            if(entry.value == false)
                return false;
        }
        System.out.println("+++++++++++++++++++++++all EXITS found");
        return true;
    }

    private boolean allTargetsFound(){
        for (IntObjectCursor<Boolean> entry : this.foundTargets) {
            if(entry.value == false)
                return false;
        }
        System.out.println("all TARGETS found");
        return true;
    }



    /**
     * Check whether the priorityqueue has an entry that could possibly lead to a shorter path for any of the subItems
     * @return
     */
    private boolean couldExistShorterPath() {
        for (MultiTreeSPEntry entry : prioQueue) {
            for (int i = 0; i < treeEntrySize; ++i) {
                if(entry.getItem(i).getWeight() < currEdge.getItem(i).getWeight())
                    return true;
            }
        }
        return false;
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
        if(currEdgeItem.getEdge() != iter.getEdge() && currEdgeItem.getOriginalEdge() == EdgeIteratorStateHelper.getOriginalEdge(iter))
            return false;
        return true;
    }

    public void setTurnWeighting(TurnWeighting turnWeighting) {
        this.turnWeighting = turnWeighting;
    }

    public void setTargetGraphExplorer(EdgeExplorer targetGraphExplorer) {
        this.targetGraphExplorer = targetGraphExplorer;
    }

    public void setTargetMap(IntObjectMap<MinimumWeightMultiTreeSPEntry> targetMap) {
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
