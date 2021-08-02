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
    protected PriorityQueue<MinimumWeightMultiTreeSPEntry> prioQueue;
    private CHGraph chGraph;
    private IntHashSet targets;
    private IntObjectMap<Boolean> foundTargets;
    protected MinimumWeightMultiTreeSPEntry currEdge;
    //TODO visited nodes
    private int maxVisitedNodes = Integer.MAX_VALUE;
    private int visitedNodes;
    private int treeEntrySize;

    private boolean hasTurnWeighting = false;
    private int turnRestrictedNodeLevel;
    protected boolean approximate = false;
    private int targetsCount = 0;
    private TurnWeighting turnWeighting = null;

    public DijkstraManyToManyMultiTreeAlgorithm(Graph graph, CHGraph chGraph, Weighting weighting, TraversalMode tMode) {
        super(graph, weighting, tMode);
        this.chGraph = chGraph;
        this.turnRestrictedNodeLevel = chGraph.getNodes() + 2;
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
        targetsCount = to.length;
        this.targets = new IntHashSet(targetsCount);
        this.foundTargets = new IntObjectHashMap<>(targetsCount);

        for (int i = 0; i < to.length; ++i)
        {
            int nodeId = to[i];
            if (nodeId >= 0) {
                this.targets.add(nodeId);
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
        for (int j : from) {
            if ((j != -1)) {
                //If two queried points are on the same node, this case can occur
                MinimumWeightMultiTreeSPEntry existing = bestWeightMap.get(j);
                if (existing == null) {
                    throw new IllegalStateException("Node " + j + " was not found in existing weight map");
                }
                prioQueue.add(existing);
            }
        }
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
        int testNodeId = 8270;
        List<MinimumWeightMultiTreeSPEntry> entries = bestWeightMapCore.get(testNodeId);

        return targetEntries.toArray(extractedTargets);
    }

    protected void runAlgo() {
        EdgeExplorer explorer = graph.createEdgeExplorer(DefaultEdgeFilter.outEdges(flagEncoder));
        currEdge = prioQueue.poll();
        if(currEdge == null)
            return;

        while (!(isMaxVisitedNodesExceeded() || finished())) {
            EdgeIterator iter = explorer.setBaseNode(currEdge.getAdjNode());
            exploreEntry(iter);
            if (prioQueue.isEmpty())
                break;

            currEdge = prioQueue.poll();
            if (currEdge == null)
                throw new AssertionError("Empty edge cannot happen");
            visitedNodes++;
        }
    }

    private void exploreEntry(EdgeIterator iter) {
        while (iter.next()) {
            if (accept(iter, currEdge.getEdge())) {

                if(hasTurnWeighting && !isInORS(iter, currEdge))
                    turnWeighting.setInORS(false);

                double edgeWeight = weighting.calcWeight(iter, false, currEdge.getOriginalEdge());

                if (Double.isInfinite(edgeWeight))
                    continue;

                if (considerTurnRestrictions(iter.getAdjNode())) {
                    handleMultiEdgeCase(iter, edgeWeight);
                }
                else {
                    handleSingleEdgeCase(iter, edgeWeight);
                }
            }
            if(hasTurnWeighting)
                turnWeighting.setInORS(true);
        }
    }

    private void handleSingleEdgeCase(EdgeIterator iter, double edgeWeight) {
        MinimumWeightMultiTreeSPEntry entry = bestWeightMap.get(iter.getAdjNode());
        if (entry == null) {
            entry = new MinimumWeightMultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), edgeWeight, true, currEdge, currEdge.getSize());
            entry.setOriginalEdge(EdgeIteratorStateHelper.getOriginalEdge(iter));
            entry.setSubItemOriginalEdgeIds(EdgeIteratorStateHelper.getOriginalEdge(iter));
            bestWeightMap.put(iter.getAdjNode(), entry);
            prioQueue.add(entry);
        } else {
            boolean addToQueue = iterateMultiTree(iter, entry);

            if (addToQueue) {
                prioQueue.remove(entry);
                entry.updateWeights();
                prioQueue.add(entry);
            }
        }
    }

    private void handleMultiEdgeCase(EdgeIterator iter, double edgeWeight) {
        List<MinimumWeightMultiTreeSPEntry> entries = bestWeightMapCore.get(iter.getAdjNode());
        MinimumWeightMultiTreeSPEntry entry = null;

        if (entries == null) {
            entries = initBestWeightMapEntryList(bestWeightMapCore, iter.getAdjNode());
            //Initialize target entry in normal weight map
            if(targets.contains(iter.getAdjNode())){
                MinimumWeightMultiTreeSPEntry target = bestWeightMap.get(iter.getAdjNode());
                if (target == null) {
                    target = new MinimumWeightMultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), Double.POSITIVE_INFINITY, true, currEdge, currEdge.getSize());
                    target.setOriginalEdge(EdgeIteratorStateHelper.getOriginalEdge(iter));
                    target.setSubItemOriginalEdgeIds(EdgeIteratorStateHelper.getOriginalEdge(iter));
                    bestWeightMap.put(iter.getAdjNode(), target);
                }
            }
        } else {
            ListIterator<MinimumWeightMultiTreeSPEntry> it = entries.listIterator();
            while (it.hasNext()) {
                MinimumWeightMultiTreeSPEntry listEntry = it.next();
                if (listEntry.getEdge() == iter.getEdge()) {
                    entry = listEntry;
                    break;
                }
            }
        }

        if (entry == null) {
            entry = new MinimumWeightMultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), Double.POSITIVE_INFINITY, true, currEdge, currEdge.getSize());
            // Modification by Maxim Rylov: Assign the original edge id.
            // TODO original edge
            entry.setOriginalEdge(EdgeIteratorStateHelper.getOriginalEdge(iter));
            if(entry.getAdjNode() == 8270) {
                int x = 0;
            }
            entry.setSubItemOriginalEdgeIds(EdgeIteratorStateHelper.getOriginalEdge(iter));
            iterateMultiTree(iter, entry);
//            if(targets.contains(iter.getAdjNode())) {
//                updateTargetEntry(entry, bestWeightMap.get(iter.getAdjNode()));
//            }
            entries.add(entry);
            prioQueue.add(entry);

        } else {
            if(entry.getAdjNode() == 8270) {
                int x = 0;
            }
            boolean addToQueue = iterateMultiTree(iter, entry);
            if (addToQueue) {
                prioQueue.remove(entry);
                entry.updateWeights();
                prioQueue.add(entry);
//                if(targets.contains(iter.getAdjNode())) {
//                    updateTargetEntry(entry, bestWeightMap.get(iter.getAdjNode()));
//                }
            }
        }
    }

    private void updateTargetEntry(MinimumWeightMultiTreeSPEntry updateEntry, MinimumWeightMultiTreeSPEntry target) {
//        target.setOriginalEdge(updateEntry.getOriginalEdge());

        for (int i = 0; i < treeEntrySize; ++i) {
            MultiTreeSPEntryItem targetItem = target.getItem(i);
            double targetWeight = targetItem.getWeight();

            MultiTreeSPEntryItem msptSubItem = updateEntry.getItem(i);
            double updateWeight = msptSubItem.getWeight();

            if (targetWeight > updateWeight) {
                targetItem.setWeight(updateWeight);
                targetItem.setEdge(msptSubItem.getEdge());
                targetItem.setOriginalEdge(msptSubItem.getOriginalEdge());
                targetItem.setParent(currEdge);
                targetItem.setUpdate(true);
            }
        }
        target.updateWeights();
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
            MultiTreeSPEntryItem msptItem = currEdge.getItem(i);
            double entryWeight = msptItem.getWeight();

            if (entryWeight == Double.POSITIVE_INFINITY)// || !msptItem.isUpdate())
                continue;

            MultiTreeSPEntryItem msptSubItem = entry.getItem(i);
            double edgeWeight = weighting.calcWeight(iter, false, msptItem.getOriginalEdge());

            double tmpWeight = edgeWeight + entryWeight;

            if (msptSubItem.getWeight() > tmpWeight) {
                msptSubItem.setWeight(tmpWeight);
                //TODO check whether these are the correct edges. Think they are
                msptSubItem.setEdge(iter.getEdge());
                msptSubItem.setOriginalEdge(EdgeIteratorStateHelper.getOriginalEdge(iter));
                msptSubItem.setParent(currEdge);
                msptSubItem.setUpdate(true);
                addToQueue = true;
//                if(targets.contains(entry.getAdjNode())) {
//                    MinimumWeightMultiTreeSPEntry targetEntry = bestWeightMap.get(entry.getAdjNode());
//                    MultiTreeSPEntryItem targetItem = targetEntry.getItem(i);
//                    if (targetItem.getWeight() > tmpWeight) {
//                        targetItem.setWeight(tmpWeight);
//                        //TODO check whether these are the correct edges. Think they are
//                        targetItem.setEdge(iter.getEdge());
//                        targetItem.setOriginalEdge(EdgeIteratorStateHelper.getOriginalEdge(iter));
//                        targetItem.setParent(currEdge);
//                        targetItem.setUpdate(true);
//                    }
//                }
            }
        }
        return addToQueue;
    }

    private boolean finished() {
        if (!targets.contains(currEdge.getAdjNode()))
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

    private void updateTargetFound(int node){
        this.foundTargets.put(node, true);
    }

    private boolean allTargetsFound(){
        for (IntObjectCursor<Boolean> entry : this.foundTargets) {
            if(entry.value == false)
                return false;
        }
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

    private boolean isInORS(EdgeIteratorState iter, MinimumWeightMultiTreeSPEntry currEdge) {
        if(currEdge.getEdge() != iter.getEdge() && currEdge.getOriginalEdge() == EdgeIteratorStateHelper.getOriginalEdge(iter))
            return false;
        return true;
    }

    public void setTurnWeighting(TurnWeighting turnWeighting) {
        this.turnWeighting = turnWeighting;
    }

    boolean considerTurnRestrictions(int node) {
        if (!hasTurnWeighting)
            return false;
        if (approximate)
            return isTurnRestrictedNode(node);
        return true;
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
