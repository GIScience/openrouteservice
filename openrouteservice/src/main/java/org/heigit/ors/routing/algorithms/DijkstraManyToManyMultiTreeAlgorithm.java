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
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.Parameters;
import org.heigit.ors.routing.graphhopper.extensions.storages.MinimumWeightMultiTreeSPEntry;
import org.heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntry;
import org.heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntryItem;

import java.util.PriorityQueue;

public class DijkstraManyToManyMultiTreeAlgorithm extends AbstractManyToManyRoutingAlgorithm {
    protected IntObjectMap<MinimumWeightMultiTreeSPEntry> bestWeightMapFrom;
    protected PriorityQueue<MinimumWeightMultiTreeSPEntry> prioQueue;
    private IntHashSet targets;
    protected MinimumWeightMultiTreeSPEntry currEdge;
    private int visitedNodes;
    private int treeEntrySize;


    private int targetsFound = 0;
    private int targetsCount = 0;

    public DijkstraManyToManyMultiTreeAlgorithm(Graph graph, Weighting weighting, TraversalMode tMode) {
        super(graph, weighting, tMode);
        int size = Math.min(Math.max(200, graph.getNodes() / 10), 2000);
        initCollections(size);
    }

    public DijkstraManyToManyMultiTreeAlgorithm(Graph graph, IntObjectMap<MinimumWeightMultiTreeSPEntry> existingWeightMap, Weighting weighting, TraversalMode tMode) {
        super(graph, weighting, tMode);
        int size = Math.min(Math.max(200, graph.getNodes() / 10), 2000);
        initCollections(size);
        bestWeightMapFrom = existingWeightMap;
    }

    protected void initCollections(int size) {
        prioQueue = new PriorityQueue<>(size);
        bestWeightMapFrom = new GHIntObjectHashMap<>(size);
    }

    public void reset() {
        prioQueue.clear();
        bestWeightMapFrom.clear();
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

    public void setTreeEntrySize(int entrySize){
        this.treeEntrySize = entrySize;
    }

    private void addEntriesFromMapToQueue(int[] from){
        for (int i = 0; i < from.length; i++) {
            if ((from[i] != -1)) {
                //If two queried points are on the same node, this case can occur
                MinimumWeightMultiTreeSPEntry existing = bestWeightMapFrom.get(from[i]);
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
            extractedTargets[i] = bestWeightMapFrom.get(to[i]);

        return extractedTargets;
    }

    protected void runAlgo() {
        EdgeExplorer explorer = graph.createEdgeExplorer(DefaultEdgeFilter.outEdges(flagEncoder));
        currEdge = prioQueue.poll();
        if(currEdge == null)
            return;
        while (true) {
            EdgeIterator iter = explorer.setBaseNode(currEdge.getAdjNode());
            if (!(isMaxVisitedNodesExceeded() || finished())){
                exploreEntry(iter);

                if (prioQueue.isEmpty())
                    break;

                currEdge = prioQueue.poll();
                if (currEdge == null)
                    throw new AssertionError("Empty edge cannot happen");
            }
        }
    }

    private void exploreEntry(EdgeIterator iter) {
        while (iter.next()) {
            if (accept(iter, -1)) {

                double edgeWeight = weighting.calcWeight(iter, false, 0);
                if (Double.isInfinite(edgeWeight))
                    continue;

                MinimumWeightMultiTreeSPEntry entry = bestWeightMapFrom.get(iter.getAdjNode());

                if (entry == null) {
                    entry = new MinimumWeightMultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), edgeWeight, true, currEdge, currEdge.getSize());

                    bestWeightMapFrom.put(iter.getAdjNode(), entry);
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
                msptSubItem.setWeight(tmpWeight);
                msptSubItem.setEdge(iter.getEdge());
                msptSubItem.setParent(currEdge);
                msptSubItem.setUpdate(true);
                addToQueue = true;
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
     * Check whether the priorityqueue has an entry that could possibly lead to a shorter path
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


    @Override
    public int getVisitedNodes() {
        return visitedNodes;
    }

    @Override
    public String getName() {
        return Parameters.Algorithms.DIJKSTRA;
    }
}
