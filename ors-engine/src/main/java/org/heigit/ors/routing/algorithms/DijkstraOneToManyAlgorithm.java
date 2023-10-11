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

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.Parameters;
import org.heigit.ors.exceptions.MaxVisitedNodesExceededException;

import java.util.PriorityQueue;

public class DijkstraOneToManyAlgorithm extends AbstractOneToManyRoutingAlgorithm {
    protected IntObjectMap<SPTEntry> fromMap;
    protected PriorityQueue<SPTEntry> fromHeap;
    protected SPTEntry currEdge;
    private int visitedNodes;

    private int targetsFound = 0;
    private IntObjectMap<SPTEntry> targets;
    private int targetsCount = 0;

    private boolean failOnMaxVisitedNodesExceeded = false;

    public DijkstraOneToManyAlgorithm(Graph graph, Weighting weighting, TraversalMode tMode) {
        this(graph, weighting, tMode, false);
    }

    public DijkstraOneToManyAlgorithm(Graph graph, Weighting weighting, TraversalMode tMode, boolean failOnMaxVisitedNodesExceeded) {
        super(graph, weighting, tMode);
        int size = Math.min(Math.max(200, graph.getNodes() / 10), 2000);
        initCollections(size);
        this.failOnMaxVisitedNodesExceeded = failOnMaxVisitedNodesExceeded;
    }

    protected void initCollections(int size) {
        fromHeap = new PriorityQueue<>(size);
        fromMap = new GHIntObjectHashMap<>(size);
        targets = new GHIntObjectHashMap<>();
    }

    public void reset() {
        fromHeap.clear();
        fromMap.clear();
        targetsFound = 0;
    }

    public int getFoundTargets() {
        return targetsFound;
    }

    public int getTargetsCount() {
        return targetsCount;
    }

    public void prepare(int[] from, int[] to) {
        this.targets.clear();

        for (int i = 0; i < to.length; ++i) {
            int nodeId = to[i];
            if (nodeId >= 0)
                this.targets.put(nodeId, new SPTEntry(EdgeIterator.NO_EDGE, nodeId, 1));
        }
    }

    @Override
    public SPTEntry[] calcPaths(int from, int[] to) {
        targetsCount = targets.containsKey(from) ? targets.size() - 1 : targets.size();

        if (targetsCount > 0) {
            currEdge = createSPTEntry(from, 0);
            if (!traversalMode.isEdgeBased()) {
                fromMap.put(from, currEdge);
            }

            runAlgo();
        }

        SPTEntry[] res = new SPTEntry[to.length];

        for (int i = 0; i < to.length; i++) {
            int nodeId = to[i];
            if (nodeId >= 0)
                res[i] = fromMap.get(to[i]);
        }

        return res;
    }

    protected void runAlgo() {
        EdgeExplorer explorer = outEdgeExplorer;
        while (true) {
            visitedNodes++;
            if (this.failOnMaxVisitedNodesExceeded && isMaxVisitedNodesExceeded())
                // Fail only if necessary for the matrix api endpoint
                throw new MaxVisitedNodesExceededException();
            else if (isMaxVisitedNodesExceeded() || finished())
                // Do not fail but quit the search if the max visited nodes are exceeded
                // Important for the fast-isochrones cell nodes calculation
                break;

            int startNode = currEdge.adjNode;
            EdgeIterator iter = explorer.setBaseNode(startNode);
            while (iter.next()) {
                if (!accept(iter, currEdge.edge))
                    continue;

                int traversalId = traversalMode.createTraversalId(iter, false);
                double tmpWeight = weighting.calcEdgeWeight(iter, false, currEdge.edge) + currEdge.weight;
                if (Double.isInfinite(tmpWeight))
                    continue;

                SPTEntry nEdge = fromMap.get(traversalId);
                if (nEdge == null) {
                    nEdge = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
                    nEdge.parent = currEdge;
                    fromMap.put(traversalId, nEdge);
                    fromHeap.add(nEdge);
                } else if (nEdge.weight > tmpWeight) {
                    fromHeap.remove(nEdge);
                    nEdge.edge = iter.getEdge();
                    nEdge.weight = tmpWeight;
                    nEdge.parent = currEdge;
                    fromHeap.add(nEdge);
                }
            }

            if (fromHeap.isEmpty())
                break;

            currEdge = fromHeap.poll();
            if (currEdge == null)
                throw new AssertionError("Empty edge cannot happen");
        }
    }

    private boolean finished() {
        if (currEdge.edge != -1) {
            SPTEntry entry = targets.get(currEdge.adjNode);
            if (entry != null) {
                entry.adjNode = currEdge.adjNode;
                entry.weight = currEdge.weight;
                entry.edge = currEdge.edge;
                entry.parent = currEdge.parent;

                entry.originalEdge = currEdge.originalEdge;

                targetsFound++;
            }
        }

        return targetsFound == targetsCount;
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
