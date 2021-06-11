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

import java.util.Iterator;
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

    private void createQueueAndMapFromIds(int[] from){
        for (int i = 0; i < from.length; i++) {
            if (from[i] == -1)
                continue;

            //If two queried points are on the same node, this case can occur
            MinimumWeightMultiTreeSPEntry existing = bestWeightMapFrom.get(from[i]);
            if (existing != null) {
                existing.getItem(i).setWeight(0.0);
                continue;
            }

            MinimumWeightMultiTreeSPEntry newFrom = new MinimumWeightMultiTreeSPEntry(from[i], EdgeIterator.NO_EDGE, 0.0, true, null, from.length);
            newFrom.getItem(i).setWeight(0.0);
            newFrom.updateWeights();
            newFrom.setVisited(true);
            prioQueue.add(newFrom);

            if (!traversalMode.isEdgeBased())
                bestWeightMapFrom.put(from[i], newFrom);
            else
                throw new IllegalStateException("Edge-based behavior not supported");
        }
    }

    private void createQueueAndMapFromQueue(PriorityQueue<MinimumWeightMultiTreeSPEntry> queue) {
        prioQueue = queue;
        Iterator<MinimumWeightMultiTreeSPEntry> iterator = queue.iterator();
        while(iterator.hasNext()){
            MinimumWeightMultiTreeSPEntry item = iterator.next();
            bestWeightMapFrom.put(item.getAdjNode(), item);
        }
    }

    @Override
    public MinimumWeightMultiTreeSPEntry[] calcPaths(int[] from, int[] to) {
        return this.calcPaths(from, to, null);
    }

    public MinimumWeightMultiTreeSPEntry[] calcPaths(int[] from, int[] to, PriorityQueue<MinimumWeightMultiTreeSPEntry> fromQueue) {
        prepare(from, to);
        if(from == null || fromQueue.isEmpty())
            createQueueAndMapFromIds(from);
        else {
            createQueueAndMapFromQueue(fromQueue);
        }

        outEdgeExplorer = graph.createEdgeExplorer();

        runAlgo();

        MinimumWeightMultiTreeSPEntry[] targets = new MinimumWeightMultiTreeSPEntry[to.length];

        for (int i = 0; i < to.length; ++i)
            targets[i] = bestWeightMapFrom.get(to[i]);

        return targets;
    }

    protected void runAlgo() {
        EdgeExplorer explorer = graph.createEdgeExplorer(DefaultEdgeFilter.outEdges(flagEncoder));
        currEdge = prioQueue.poll();

        while (true) {
            EdgeIterator iter = explorer.setBaseNode(currEdge.getAdjNode());
//            System.out.println("Based node " + currEdge.getAdjNode());
            if (iter == null) // we reach one of the target nodes
                return;
            if (isMaxVisitedNodesExceeded() || finished())
                break;

            while (iter.next()) {
                if (!accept(iter, -1))
                    continue;
//                System.out.println("Checking edge " + iter.getEdge() + " to " + iter.getAdjNode());

                double edgeWeight = weighting.calcWeight(iter, false, 0);

                if (!Double.isInfinite(edgeWeight)) {
                    MinimumWeightMultiTreeSPEntry ee = bestWeightMapFrom.get(iter.getAdjNode());

                    if (ee == null) {
                        ee = new MinimumWeightMultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), edgeWeight, true, currEdge, currEdge.getSize());

                        bestWeightMapFrom.put(iter.getAdjNode(), ee);
                        prioQueue.add(ee);
                    } else {
                        boolean addToQueue = false;

                        for (int i = 0; i < treeEntrySize; ++i) {
                            MultiTreeSPEntryItem msptItem = currEdge.getItem(i);
                            double entryWeight = msptItem.getWeight();

                            if (entryWeight == Double.POSITIVE_INFINITY || !msptItem.isUpdate())
                                continue;

                            MultiTreeSPEntryItem msptSubItem = ee.getItem(i);

                            double tmpWeight = edgeWeight + entryWeight;

                            if (msptSubItem.getWeight() > tmpWeight) {
                                msptSubItem.setWeight(tmpWeight);
                                msptSubItem.setEdge(iter.getEdge());
                                msptSubItem.setParent(currEdge);
                                msptSubItem.setUpdate(true);
                                addToQueue = true;
                            }
                        }

                        if (addToQueue) {
                            ee.updateWeights();
                            prioQueue.remove(ee);
                            prioQueue.add(ee);
                        }
                    }
                }
            }
            if (prioQueue.isEmpty())
                break;

            currEdge = prioQueue.poll();
            if (currEdge == null)
                throw new AssertionError("Empty edge cannot happen");
        }
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
//        System.out.println("Found target " + currEdge.getAdjNode());
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
