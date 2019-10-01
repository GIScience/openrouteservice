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
package heigit.ors.routing.graphhopper.extensions.core;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.AbstractRoutingAlgorithm;
import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.Parameters;

import java.util.PriorityQueue;

/**
 * Finds proxy nodes in the graph.
 * A proxy node of a given node is the closest node to it in the graph given a weighting.
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner
 */
public class ProxyNodeDijkstra extends AbstractRoutingAlgorithm {
    protected IntObjectMap<SPTEntry> fromMap;
    protected PriorityQueue<SPTEntry> fromHeap;
    protected SPTEntry currEdge;
    private int visitedNodes;
    private int maxVisitedNodes = Integer.MAX_VALUE;
    private int coreNodeLevel = -1;
    private CHGraph chGraph;
    EdgeExplorer explorer;

    // Modification by Maxim Rylov: Added a new class variable used for computing isochrones.
    protected Boolean reverseDirection = false;

    public ProxyNodeDijkstra(GraphHopperStorage graph, Weighting weighting, TraversalMode tMode) {
        super(graph, weighting, tMode);
        int size = Math.min(Math.max(200, graph.getNodes() / 10), 2000);
        chGraph  = graph.getCoreGraph(weighting);
        coreNodeLevel = chGraph.getNodes() + 1;
        initCollections(size);
    }

    public ProxyNodeDijkstra(GraphHopperStorage graph, Weighting weighting, TraversalMode tMode, int maxVisitedNodes) {
        this(graph,weighting,tMode);
        this.maxVisitedNodes = maxVisitedNodes;
    }


    protected void initCollections(int size) {
        fromHeap = new PriorityQueue<SPTEntry>(size);
        fromMap = new GHIntObjectHashMap<SPTEntry>(size);
    }

    @Override
    public Path calcPath(int from, int to) {
        throw new IllegalStateException("Cannot calc a path with this algorithm");

    }

    /**
     * Get a proxy node for a given node
     * @param from the node for which to calc a proxy
     * @param bwd use backwards weights
     * @return SPTEntry of the proxy node
     */
    public SPTEntry getProxyNode(int from, boolean bwd){
        checkAlreadyRun();
        currEdge = createSPTEntry(from, 0);
        if (!traversalMode.isEdgeBased()) {
            fromMap.put(from, currEdge);
        }
        explorer = bwd? inEdgeExplorer : outEdgeExplorer;
        reverseDirection = bwd;
        runAlgo();

        if (finished())
            return currEdge;
        else
            return null;
    }

    /**
     * Run a Dijkstra on the base graph to find the closest node that is in the core
     */
    protected void runAlgo() {
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
                // Modification by Maxim Rylov: use originalEdge as the previousEdgeId
                double tmpWeight = weighting.calcWeight(iter, reverseDirection, currEdge.originalEdge) + currEdge.weight;
                if (Double.isInfinite(tmpWeight))
                    continue;

                SPTEntry nEdge = fromMap.get(traversalId);
                if (nEdge == null) {
                    nEdge = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
                    nEdge.parent = currEdge;
                    // Modification by Maxim Rylov: Assign the original edge id.
                    nEdge.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
                    fromMap.put(traversalId, nEdge);
                    fromHeap.add(nEdge);
                } else if (nEdge.weight > tmpWeight) {
                    fromHeap.remove(nEdge);
                    nEdge.edge = iter.getEdge();
                    nEdge.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
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

    @Override
    protected boolean finished() {
        return chGraph.getLevel(currEdge.adjNode) == coreNodeLevel;
    }

    @Override
    protected Path extractPath() {
        if (currEdge == null || !finished())
            return createEmptyPath();

        return new Path(graph, weighting).setWeight(currEdge.weight).setSPTEntry(currEdge).extract();
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
