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
package org.heigit.ors.fastisochrones;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.AbstractRoutingAlgorithm;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.querygraph.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;

import java.util.PriorityQueue;

/**
 * calculates maximum range (eccentricity) within a cell.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public abstract class AbstractIsochroneDijkstra extends AbstractRoutingAlgorithm {
    protected IntObjectMap<SPTEntry> fromMap;
    protected PriorityQueue<SPTEntry> fromHeap;
    protected SPTEntry currEdge;
    protected int visitedNodes;
    protected boolean reverseDirection = false;

    protected AbstractIsochroneDijkstra(Graph graph, Weighting weighting) {
        super(graph, weighting, weighting.hasTurnCosts() ? TraversalMode.EDGE_BASED : TraversalMode.NODE_BASED);
        int size = Math.min(Math.max(200, graph.getNodes() / 10), 2000);
        initCollections(size);
    }

    protected void initCollections(int size) {
        fromHeap = new PriorityQueue<>(size);
        fromMap = new GHIntObjectHashMap<>(size);
    }

    protected abstract void runAlgo();

    protected void createEntry(EdgeIterator iter, int traversalId, double tmpWeight) {
        SPTEntry nEdge = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
        nEdge.parent = currEdge;
        nEdge.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
        fromMap.put(traversalId, nEdge);
        fromHeap.add(nEdge);
    }

    protected void updateEntry(SPTEntry nEdge, EdgeIterator iter, double tmpWeight) {
        fromHeap.remove(nEdge);
        nEdge.edge = iter.getEdge();
        nEdge.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
        nEdge.weight = tmpWeight;
        nEdge.parent = currEdge;
        fromHeap.add(nEdge);
    }

    public IntObjectMap<SPTEntry> getFromMap() {
        return fromMap;
    }

    @Override
    protected Path extractPath() {
        throw new IllegalStateException("Cannot calculate a path with this algorithm");
    }

    @Override
    public int getVisitedNodes() {
        return visitedNodes;
    }

    @Override
    public Path calcPath(int from, int to) {
        throw new IllegalStateException("Cannot calculate a path with this algorithm");
    }
}
