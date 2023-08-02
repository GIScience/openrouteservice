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

import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.storage.RoutingCHEdgeExplorer;
import com.graphhopper.storage.RoutingCHEdgeIterator;
import com.graphhopper.storage.RoutingCHEdgeIteratorState;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.util.CHEdgeIteratorState;
import org.apache.log4j.Logger;

public class SubGraph {
    private final Logger logger = Logger.getLogger(getClass());

    private final GHIntObjectHashMap<EdgeIteratorLink> node2EdgesMap;
    private final RoutingCHGraph baseGraph;

    public SubGraph(RoutingCHGraph graph) {
        baseGraph = graph;
        node2EdgesMap = new GHIntObjectHashMap<>(Math.min(Math.max(200, graph.getNodes() / 10), 2000));
    }

    /**
     * Returns true/false depending on whether node is already in the graph or not.
     */
    public boolean addEdge(int adjNode, RoutingCHEdgeIteratorState iter, boolean reverse) {
        if (iter == null) {
            node2EdgesMap.put(adjNode, null);
            return true;
        }

        RoutingCHEdgeIteratorState iterState;
        if (reverse) {
            iterState = baseGraph.getEdgeIteratorState(iter.getEdge(), adjNode);
            adjNode = iter.getAdjNode();
        } else {
            iterState = baseGraph.getEdgeIteratorState(iter.getEdge(), iter.getAdjNode());
            adjNode = iter.getBaseNode();
        }

        EdgeIteratorLink link = node2EdgesMap.get(adjNode);
        if (link == null) {
            link = new EdgeIteratorLink(iterState);
            node2EdgesMap.put(adjNode, link);
            return true;
        } else {
            while (link.next != null)
                link = link.next;
            link.next = new EdgeIteratorLink(iterState);
            return false;
        }
    }

    public boolean containsNode(int adjNode) {
        return node2EdgesMap.containsKey(adjNode);
    }

    public RoutingCHEdgeIterator setBaseNode(int baseNode) {
        EdgeIteratorLink link = node2EdgesMap.get(baseNode);
        return link == null ? null : new EdgeIteratorLinkIterator(link);
    }

    public RoutingCHEdgeExplorer createExplorer() {
        return new SubGraphEdgeExplorer(this);
    }

    public void print() {
        int edgesCount = 0;

        RoutingCHEdgeExplorer explorer = createExplorer();

        for (IntObjectCursor<?> node : node2EdgesMap) {
            RoutingCHEdgeIterator iter = explorer.setBaseNode(node.key);

            if (iter != null) {
                while (iter.next()) {
                    edgesCount++;
                }
            }
        }

        logger.info("SubGraph: nodes - " + node2EdgesMap.size() + "; edges - " + edgesCount);
    }

    private static class EdgeIteratorLink {
        private final RoutingCHEdgeIteratorState state;
        private EdgeIteratorLink next;

        public EdgeIteratorLink(RoutingCHEdgeIteratorState iterState) {
            state = iterState;
        }

        public EdgeIteratorLink getNext() {
            return next;
        }

        public void setNext(EdgeIteratorLink next) {
            this.next = next;
        }
    }

    static class SubGraphEdgeExplorer implements RoutingCHEdgeExplorer {
        private final SubGraph graph;

        public SubGraphEdgeExplorer(SubGraph graph) {
            this.graph = graph;
        }

        @Override
        public RoutingCHEdgeIterator setBaseNode(int baseNode) {
            return graph.setBaseNode(baseNode);
        }
    }

    public static class EdgeIteratorLinkIterator implements RoutingCHEdgeIterator, RoutingCHEdgeIteratorState {
        private RoutingCHEdgeIteratorState currState;
        private EdgeIteratorLink link;
        private boolean firstRun = true;

        public EdgeIteratorLinkIterator(EdgeIteratorLink link) {
            this.link = link;
            currState = link.state;
        }

        public RoutingCHEdgeIteratorState getCurrState() {
            return currState;
        }

        @Override
        public int getEdge() {
            return currState.getEdge();
        }

        @Override
        public int getOrigEdge() {
            return currState.getOrigEdge();
        }

        @Override
        public int getOrigEdgeFirst() {
            return currState.getOrigEdgeFirst();
        }

        @Override
        public int getOrigEdgeLast() {
            return currState.getOrigEdgeLast();
        }

        @Override
        public int getBaseNode() {
            return currState.getBaseNode();
        }

        @Override
        public int getAdjNode() {
            return currState.getAdjNode();
        }

        @Override
        public boolean next() {
            if (firstRun) {
                firstRun = false;
                return true;
            }

            link = link.next;

            if (link == null) {
                currState = null;

                return false;
            }

            currState = link.state;

            return true;
        }

        @Override
        public int getSkippedEdge1() {
            return 0;
        }

        @Override
        public int getSkippedEdge2() {
            return 0;
        }

        @Override
        public double getWeight(boolean b) {
            return currState.getWeight(b);
        }

        @Override
        public int getTime(boolean b) {
            return currState.getTime(b);
        }

        @Override
        public boolean isShortcut() {
            if (currState instanceof CHEdgeIteratorState state)
                return (state.isShortcut());
            else
                return false;
        }
    }
}
