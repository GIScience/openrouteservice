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
package org.heigit.ors.routing.algorithms;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectMap;
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
import com.graphhopper.util.Parameters;
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
    protected MinimumWeightMultiTreeSPEntry currEdge;
    //TODO visited nodes
    private int visitedNodes;
    private int treeEntrySize;

    private boolean hasTurnWeighting = false;
    private int turnRestrictedNodeLevel;
    protected boolean approximate = true;

    private int targetsFound = 0;
    private int targetsCount = 0;

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
        targetsFound = 0;
    }

    public int getFoundTargets() {
        return targetsFound;
    }

    public int getTargetsCount() {
        return targetsCount;
    }

    public void prepare(int[] from, int[] to) {
        targetsCount = to.length;
        this.targets = new IntHashSet(targetsCount);
        for (int i = 0; i < to.length; ++i)
        {
            int nodeId = to[i];
            if (nodeId >= 0)
                this.targets.add(nodeId);
        }
    }

    public void setHasTurnWeighting(boolean hasTurnWeighting) {
        this.hasTurnWeighting = hasTurnWeighting;
    }

    public void setTreeEntrySize(int entrySize){
        this.treeEntrySize = entrySize;
    }

    private void addEntriesFromMapToQueue(int[] from){
        for (int i = 0; i < from.length; i++) {
            if ((from[i] != -1)) {
                //If two queried points are on the same node, this case can occur
                MinimumWeightMultiTreeSPEntry existing = bestWeightMap.get(from[i]);
                if (existing == null) {
                    throw new IllegalStateException("Node " + from[i] + " was not found in existing weight map");
                }
                prioQueue.add(existing);
            }
        }
    }

    public MinimumWeightMultiTreeSPEntry[] calcPaths(int[] from, int[] to) {
        if(from.length == 0 || to.length == 0)
            return new MinimumWeightMultiTreeSPEntry[]{};
        if(from == null || to == null)
            throw new IllegalArgumentException("Input points are null");
        prepare(from, to);
        addEntriesFromMapToQueue(from);

        outEdgeExplorer = graph.createEdgeExplorer();

        runAlgo();

        MinimumWeightMultiTreeSPEntry[] extractedTargets = new MinimumWeightMultiTreeSPEntry[to.length];

        for (int i = 0; i < to.length; ++i)
            extractedTargets[i] = bestWeightMap.get(to[i]);

        return extractedTargets;
    }

    protected void runAlgo() {
        EdgeExplorer explorer = graph.createEdgeExplorer(DefaultEdgeFilter.outEdges(flagEncoder));
        currEdge = prioQueue.poll();
        System.out.println();
        System.out.println("Polled queue entry for edge " + currEdge.getEdge() + " to node " + currEdge.getAdjNode() + " with weight " + currEdge.toString());
        if(currEdge == null)
            return;
        while (true) {
            EdgeIterator iter = explorer.setBaseNode(currEdge.getAdjNode());
            if ((isMaxVisitedNodesExceeded() || finished()))
                break;
            exploreEntry(iter);

            if (prioQueue.isEmpty())
                break;

            currEdge = prioQueue.poll();
            System.out.println();
            System.out.println("Polled queue entry for edge " + currEdge.getEdge() + " to node " + currEdge.getAdjNode() + " with weight " + currEdge.toString());
            if (currEdge == null)
                throw new AssertionError("Empty edge cannot happen");

        }
    }

    private void exploreEntry(EdgeIterator iter) {
        while (iter.next()) {
            if (accept(iter, -1)) {

                double edgeWeight = weighting.calcWeight(iter, false, currEdge.getEdge());
                System.out.println("Fill Core from currEdge " + currEdge.getEdge() + " via basenode " + currEdge.getAdjNode() + " to edge " + iter.getEdge() + " with  adjNode " + iter.getAdjNode() + " and weight " + edgeWeight);

                if (Double.isInfinite(edgeWeight))
                    continue;

                if (considerTurnRestrictions(iter.getAdjNode())) {
                    List<MinimumWeightMultiTreeSPEntry> entries = bestWeightMapCore.get(iter.getAdjNode());
                    MinimumWeightMultiTreeSPEntry entry = null;

                    if (entries == null) {
                        entries = initBestWeightMapEntryList(bestWeightMapCore, iter.getAdjNode());
                        //Initialize target entry in normal weight map
                        if(targets.contains(iter.getAdjNode())){
                            MinimumWeightMultiTreeSPEntry target = bestWeightMap.get(iter.getAdjNode());
                            if (target == null) {
                                target = new MinimumWeightMultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), Double.POSITIVE_INFINITY, true, currEdge, currEdge.getSize());

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
                        entry = new MinimumWeightMultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), edgeWeight, true, currEdge, currEdge.getSize());
                        // Modification by Maxim Rylov: Assign the original edge id.
                        // TODO original edge
//                        ee.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
                        if(targets.contains(iter.getAdjNode())) {
                            updateTargetEntry(entry, bestWeightMap.get(iter.getAdjNode()));
                        }
                        entries.add(entry);
                        prioQueue.add(entry);

                    } else {
                        boolean addToQueue = iterateMultiTree(iter, edgeWeight, entry);
                        if(targets.contains(iter.getAdjNode())) {
                            updateTargetEntry(entry, bestWeightMap.get(iter.getAdjNode()));
                        }
                        if (addToQueue) {
                            entry.updateWeights();
                            prioQueue.remove(entry);
                            prioQueue.add(entry);
                        }
                    }
                    //TODO time
//                    ee.time = calcTime(iter, currEdge, reverse);
                }
                else {
                    MinimumWeightMultiTreeSPEntry entry = bestWeightMap.get(iter.getAdjNode());
                    if (entry == null) {
                        entry = new MinimumWeightMultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), edgeWeight, true, currEdge, currEdge.getSize());

                        bestWeightMap.put(iter.getAdjNode(), entry);
                        prioQueue.add(entry);
                    } else {
                        boolean addToQueue = iterateMultiTree(iter, edgeWeight, entry);

                        if (addToQueue) {
                            entry.updateWeights();
                            prioQueue.remove(entry);
                            prioQueue.add(entry);
                        }
                    }
                }
            }
        }
    }

    private boolean updateTargetEntry(MinimumWeightMultiTreeSPEntry updateEntry, MinimumWeightMultiTreeSPEntry target) {
        boolean addToQueue = false;

        for (int i = 0; i < treeEntrySize; ++i) {
            MultiTreeSPEntryItem targetItem = target.getItem(i);
            double targetWeight = targetItem.getWeight();

            MultiTreeSPEntryItem msptSubItem = updateEntry.getItem(i);
            double updateWeight = msptSubItem.getWeight();

            if (targetWeight > updateWeight) {
//                System.out.println("For node " + entry.getAdjNode() + " with weight " + msptSubItem.getWeight() + " setting weight " + msptSubItem.getWeight());
                targetItem.setWeight(updateWeight);
                targetItem.setEdge(updateEntry.getEdge());
                //TODO
//                msptSubItem.setOriginalEdge(EdgeIteratorStateHelper.getOriginalEdge(iter));
                targetItem.setParent(currEdge);
                targetItem.setUpdate(true);
                addToQueue = true;
            }
        }
        return addToQueue;
    }

    List<MinimumWeightMultiTreeSPEntry> initBestWeightMapEntryList(IntObjectMap<List<MinimumWeightMultiTreeSPEntry>> bestWeightMap, int traversalId) {
        if (bestWeightMap.get(traversalId) != null)
            throw new IllegalStateException("Core entry point already exists in best weight map.");

        List<MinimumWeightMultiTreeSPEntry> entryList = new ArrayList<>(5);// TODO: Proper assessment of the optimal size
        bestWeightMap.put(traversalId, entryList);

        return entryList;
    }

    private boolean iterateMultiTree(EdgeIterator iter, double edgeWeight, MinimumWeightMultiTreeSPEntry entry) {
        boolean addToQueue = false;

        for (int i = 0; i < treeEntrySize; ++i) {
            MultiTreeSPEntryItem msptItem = currEdge.getItem(i);
            double entryWeight = msptItem.getWeight();

            if (entryWeight == Double.POSITIVE_INFINITY || !msptItem.isUpdate())
                continue;

            MultiTreeSPEntryItem msptSubItem = entry.getItem(i);

            double tmpWeight = edgeWeight + entryWeight;

            if (msptSubItem.getWeight() > tmpWeight) {
                System.out.print("For node " + entry.getAdjNode() + " with weight " + msptSubItem.getWeight() + " setting old weight " + entry.toString());
                msptSubItem.setWeight(tmpWeight);
                //TODO check whether these are the correct edges. Think they are
                msptSubItem.setEdge(iter.getEdge());
                msptSubItem.setOriginalEdge(EdgeIteratorStateHelper.getOriginalEdge(iter));
                msptSubItem.setParent(currEdge);
                msptSubItem.setUpdate(true);
                addToQueue = true;
                System.out.println(" to new weight " + entry.toString());
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
        if(treeEntrySize > 1 && existsShorterPath())
            return false;
        targetsFound++;

        return targetsFound == targetsCount;
    }

    /**
     * Check whether the priorityqueue has an entry that could possibly lead to a shorter path for any of the subItems
     * @return
     */
    private boolean existsShorterPath() {
        for (MultiTreeSPEntry entry : prioQueue) {
            for (int i = 0; i < treeEntrySize; ++i) {
                if(entry.getItem(i).getWeight() < currEdge.getItem(i).getWeight())
                    return true;
            }
        }
        return false;
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
    public String getName() {
        return Parameters.Algorithms.DIJKSTRA;
    }
}
