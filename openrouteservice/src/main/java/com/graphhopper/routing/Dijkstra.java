/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.routing;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.Parameters;

import java.util.PriorityQueue;

/**
 * Implements a single source shortest path algorithm
 * http://en.wikipedia.org/wiki/Dijkstra's_algorithm
 * <p>
 *
 * @author Peter Karich
 */
public class Dijkstra extends AbstractRoutingAlgorithm {
    protected IntObjectMap<SPTEntry> fromMap;
    protected PriorityQueue<SPTEntry> fromHeap;
    protected SPTEntry currEdge;
    private int visitedNodes;
    private int to = -1;

    // MARQ24 Modification by Maxim Rylov: Added a new class variable used for computing isochrones.
    protected Boolean reverseDirection = false;

    // MARQ24 MOD START
    public Dijkstra(Graph graph, Weighting weighting, TraversalMode tMode) {
        this(graph, weighting, tMode, -1);
    }

    public Dijkstra(Graph graph, Weighting weighting, TraversalMode tMode, double maxSpeed) {
        super(graph, weighting, tMode, maxSpeed);
        int size = Math.min(Math.max(200, graph.getNodes() / 10), 2000);
        initCollections(size);
    }
    // MARQ24 MOD END

    protected void initCollections(int size) {
        fromHeap = new PriorityQueue<SPTEntry>(size);
        fromMap = new GHIntObjectHashMap<SPTEntry>(size);
    }

    // MARQ24 MOD START Modification by Maxim Rylov: Added a new method.
    public void setReverseDirection(Boolean reverse) {
        reverseDirection = reverse;
    }
    // MARQ24 MOD END

    @Override
    public Path calcPath(int from, int to) {
        checkAlreadyRun();
        this.to = to;
        currEdge = createSPTEntry(from, 0);
        if (!traversalMode.isEdgeBased()) {
            fromMap.put(from, currEdge);
        }
        runAlgo();
        return extractPath();
    }

    protected void runAlgo() {
        EdgeExplorer explorer = outEdgeExplorer;
        while (true) {
            visitedNodes++;
            if (isMaxVisitedNodesExceeded() || finished())
                break;

            int startNode = currEdge.adjNode;
            EdgeIterator iter = explorer.setBaseNode(startNode);
            while (iter.next()) {
                if (!accept(iter, currEdge.edge))
                    continue;

                // MARQ24 MOD START
                // ORG CODE START
                //int traversalId = traversalMode.createTraversalId(iter, false);
                //double tmpWeight = weighting.calcWeight(iter, false, currEdge.edge) + currEdge.weight;
                // ORIGINAL END
                // MARQ24 WHY the heck the 'reverseDirection' is not used also for the traversal ID ???
                int traversalId = traversalMode.createTraversalId(iter, false);
                // Modification by Maxim Rylov: use originalEdge as the previousEdgeId
                double tmpWeight = weighting.calcWeight(iter, reverseDirection, currEdge.originalEdge) + currEdge.weight;
                // MARQ24 MOD END
                if (Double.isInfinite(tmpWeight))
                    continue;

                SPTEntry nEdge = fromMap.get(traversalId);
                if (nEdge == null) {
                    nEdge = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
                    nEdge.parent = currEdge;
                    // MARQ24 MOD START
                    // Modification by Maxim Rylov: Assign the original edge id.
                    nEdge.originalEdge = iter.getOriginalEdge();
                    // MARQ24 MOD END
                    fromMap.put(traversalId, nEdge);
                    fromHeap.add(nEdge);
                } else if (nEdge.weight > tmpWeight) {
                    fromHeap.remove(nEdge);
                    nEdge.edge = iter.getEdge();
                    // MARQ24 MOD START
                    nEdge.originalEdge = iter.getOriginalEdge();
                    // MARQ24 MOD END
                    nEdge.weight = tmpWeight;
                    nEdge.parent = currEdge;
                    fromHeap.add(nEdge);
                } else
                    continue;

                updateBestPath(iter, nEdge, traversalId);
            }

            if (fromHeap.isEmpty())
                break;

            currEdge = fromHeap.poll();
            if (currEdge == null)
                throw new AssertionError("Empty edge cannot happen");
        }
    }

    @Override
    protected boolean finished() {
        return currEdge.adjNode == to;
    }

    @Override
    protected Path extractPath() {
        if (currEdge == null || !finished())
            return createEmptyPath();

        return new Path(graph, weighting).
                setWeight(currEdge.weight).setSPTEntry(currEdge).extract();
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
