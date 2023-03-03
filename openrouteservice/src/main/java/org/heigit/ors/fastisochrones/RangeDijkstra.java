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

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

import java.util.HashSet;
import java.util.Set;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.RANGEDIJKSTRA;

/**
 * calculates maximum range (eccentricity) within a cell.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class RangeDijkstra extends AbstractIsochroneDijkstra {
    private static final boolean USERELEVANTONLY = true;
    private double maximumWeight = 0;
    private IntHashSet cellNodes;
    private final Set<Integer> visitedIds = new HashSet<>();
    private IntHashSet relevantNodes = new IntHashSet();

    public RangeDijkstra(Graph graph, Weighting weighting) {
        super(graph, weighting);
    }

    public double calcMaxWeight(int from, IntHashSet relevantNodes) {
        checkAlreadyRun();
        currEdge = new SPTEntry(EdgeIterator.NO_EDGE, from, 0);
        this.relevantNodes = relevantNodes;
        if (!traversalMode.isEdgeBased()) {
            fromMap.put(from, currEdge);
        }
        if (cellNodes.contains(from))
            visitedIds.add(from);
        else
            throw new IllegalArgumentException("Start node does not belong to cell?");
        runAlgo();
        getMaxWeight();
        return maximumWeight;
    }

    private void getMaxWeight() {
        for (IntObjectCursor<SPTEntry> entry : fromMap) {
            if (USERELEVANTONLY && !relevantNodes.contains(entry.key))
                continue;

            if (maximumWeight < entry.value.weight)
                maximumWeight = entry.value.weight;
        }
    }

    protected void runAlgo() {
        EdgeExplorer explorer = graph.createEdgeExplorer();
        while (true) {
            visitedNodes++;
            if (isMaxVisitedNodesExceeded() || finished())
                break;

            int startNode = currEdge.adjNode;
            EdgeIterator iter = explorer.setBaseNode(startNode);
            while (iter.next()) {
                if (!accept(iter, currEdge.edge))
                    continue;
                int traversalId = traversalMode.createTraversalId(iter, false);

                if (cellNodes.contains(traversalId))
                    visitedIds.add(traversalId);

                double tmpWeight = weighting.calcEdgeWeight(iter, reverseDirection, currEdge.originalEdge) + currEdge.weight;
                if (Double.isInfinite(tmpWeight))
                    continue;

                SPTEntry nEdge = fromMap.get(traversalId);
                if (nEdge == null) {
                    createEntry(iter, traversalId, tmpWeight);
                } else if (nEdge.weight > tmpWeight) {
                    updateEntry(nEdge, iter, tmpWeight);
                }
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
        return false;
    }

    public void setCellNodes(IntHashSet cellNodes) {
        this.cellNodes = cellNodes;
    }

    public int getFoundCellNodeSize() {
        return visitedIds.size();
    }

    @Override
    public String getName() {
        return RANGEDIJKSTRA;
    }
}
