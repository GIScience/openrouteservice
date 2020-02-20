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
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.AbstractRoutingAlgorithm;
import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.Parameters;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import static org.heigit.ors.partitioning.FastIsochroneParameters.ECC__USERELEVANTONLY;

/**
 * Implements a single source shortest path algorithm
 * http://en.wikipedia.org/wiki/Dijkstra's_algorithm
 * <p>
 *
 * @author Peter Karich
 */
public class RangeDijkstra extends AbstractRoutingAlgorithm {
    protected IntObjectMap<SPTEntry> fromMap;
    protected PriorityQueue<SPTEntry> fromHeap;
    protected SPTEntry currEdge;
    public int visitedNodes;
    private double maximumWeight = 0;
    private IntHashSet cellNodes;
    private Set<Integer> visitedIds = new HashSet<>();
    private IntHashSet relevantNodes = new IntHashSet();
    public int calcs = 0;



    private double acceptedFullyReachablePercentage = 1.0;

    // ORS-GH MOD START Modification by Maxim Rylov: Added a new class variable used for computing isochrones.
    protected Boolean reverseDirection = false;
    // ORS-GH MOD END

    public RangeDijkstra(Graph graph, Weighting weighting, TraversalMode tMode) {
        super(graph, weighting, tMode);
        int size = Math.min(Math.max(200, graph.getNodes() / 10), 5000);
        initCollections(size);
    }

    protected void initCollections(int size) {
        fromHeap = new PriorityQueue<>(size);
        fromMap = new GHIntObjectHashMap<>(size);
    }

    // ORS-GH MOD START Modification by Maxim Rylov: Added a new method.
    public void setReverseDirection(Boolean reverse) {
        reverseDirection = reverse;
    }
    // ORS-GH MOD END

    public double calcMaxWeight(int from, IntHashSet relevantNodes) {
//        checkAlreadyRun();
        currEdge = new SPTEntry(EdgeIterator.NO_EDGE, from, 0);
        this.relevantNodes = relevantNodes;
        if (!traversalMode.isEdgeBased()) {
            fromMap.put(from, currEdge);
        }
        runAlgo();
        getMaxWeight();
        return maximumWeight;
    }

    private void getMaxWeight() {
        for (IntObjectCursor<SPTEntry> entry : fromMap) {
            if(ECC__USERELEVANTONLY && !relevantNodes.contains(entry.key))
                continue;

            if (maximumWeight < entry.value.weight)
                maximumWeight = entry.value.weight;
        }
    }

    protected void runAlgo() {
//        EdgeExplorer explorer = graph.createEdgeExplorer(new DefaultEdgeFilter(flagEncoder, true, true));
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

                // ORS-GH MOD START
                // ORG CODE START
                //int traversalId = traversalMode.createTraversalId(iter, false);
                //double tmpWeight = weighting.calcWeight(iter, false, currEdge.edge) + currEdge.weight;
                // ORIGINAL END
                // TODO: MARQ24 WHY the heck the 'reverseDirection' is not used also for the traversal ID ???
                int traversalId = traversalMode.createTraversalId(iter, false);

                if(cellNodes.contains(traversalId))
                    visitedIds.add(traversalId);

                // Modification by Maxim Rylov: use originalEdge as the previousEdgeId
                double tmpWeight = weighting.calcWeight(iter, reverseDirection, currEdge.originalEdge) + currEdge.weight;
                calcs++;
                // ORS-GH MOD END
                if (Double.isInfinite(tmpWeight))
                    continue;

                SPTEntry nEdge = fromMap.get(traversalId);
                if (nEdge == null) {
                    nEdge = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
                    nEdge.parent = currEdge;
                    // ORS-GH MOD START
                    // Modification by Maxim Rylov: Assign the original edge id.
                    nEdge.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
                    // ORS-GH MOD END
                    fromMap.put(traversalId, nEdge);
                    fromHeap.add(nEdge);
//                    if(tmpWeight > maximumWeight)
//                        maximumWeight = tmpWeight;
                } else if (nEdge.weight > tmpWeight) {
                    fromHeap.remove(nEdge);
                    nEdge.edge = iter.getEdge();
                    // ORS-GH MOD START
                    nEdge.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
                    // ORS-GH MOD END
                    nEdge.weight = tmpWeight;
                    nEdge.parent = currEdge;
                    fromHeap.add(nEdge);
//                    if(tmpWeight > maximumWeight)
//                        maximumWeight = tmpWeight;
                } else
                    continue;
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
        return(((double)visitedIds.size()) / cellNodes.size() >= acceptedFullyReachablePercentage);
    }

    @Override
    protected Path extractPath() {
        throw new IllegalStateException("Cannot calc a path with this algorithm");
    }

    @Override
    public int getVisitedNodes() {
        return visitedNodes;
    }

    public void setCellNodes(IntHashSet cellNodes) {
        this.cellNodes = cellNodes;
    }

    public int getFoundCellNodeSize(){
        return visitedIds.size();
    }

    public void setAcceptedFullyReachablePercentage(double acceptedFullyReachablePercentage) {
        this.acceptedFullyReachablePercentage = acceptedFullyReachablePercentage;
    }


    @Override
    public Path calcPath(int from, int to) {
        throw new IllegalStateException("Cannot calc a path with this algorithm");
    }

    @Override
    public String getName() {
        return Parameters.Algorithms.DIJKSTRA;
    }
}
