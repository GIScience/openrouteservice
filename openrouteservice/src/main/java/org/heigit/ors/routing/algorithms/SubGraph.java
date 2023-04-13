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

    class EdgeIteratorLink {
        private RoutingCHEdgeIteratorState state;
        private EdgeIteratorLink next;

        public EdgeIteratorLink(RoutingCHEdgeIteratorState iterState) {
            state = iterState;
        }

        public RoutingCHEdgeIteratorState getState() {
            return state;
        }

        public void setState(RoutingCHEdgeIteratorState state) {
            this.state = state;
        }

        public EdgeIteratorLink getNext() {
            return next;
        }

        public void setNext(EdgeIteratorLink next) {
            this.next = next;
        }
    }

    class SubGraphEdgeExplorer implements RoutingCHEdgeExplorer {
        private final SubGraph graph;

        public SubGraphEdgeExplorer(SubGraph graph) {
            this.graph = graph;
        }

        @Override
        public RoutingCHEdgeIterator setBaseNode(int baseNode) {
            return graph.setBaseNode(baseNode);
        }
    }

    public class EdgeIteratorLinkIterator implements RoutingCHEdgeIterator, RoutingCHEdgeIteratorState {
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

//        @Override
//        public int getEdgeKey() {
//            return 0;
//        }

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

//        @Override
//        public PointList fetchWayGeometry(FetchMode mode) {
//            return null;
//        }
//
//        @Override
//        public EdgeIteratorState setWayGeometry(PointList list) {
//            return null;
//        }
//
//        @Override
//        public double getDistance() {
//            return currState.getDistance();
//        }
//
//        @Override
//        public EdgeIteratorState setDistance(double dist) {
//            return null;
//        }
//
//        @Override
//        public IntsRef getFlags() {
//            return currState.getFlags();
//        }
//
//        @Override
//        public EdgeIteratorState setFlags(IntsRef edgeFlags) {
//            return currState.setFlags(edgeFlags);
//        }
//
//        @Override
//        public boolean get(BooleanEncodedValue property) {
//            return currState.get(property);
//        }
//
//        @Override
//        public EdgeIteratorState set(BooleanEncodedValue property, boolean value) {
//            return currState.set(property, value);
//        }
//
//        @Override
//        public boolean getReverse(BooleanEncodedValue property) {
//            return currState.getReverse(property);
//        }
//
//        @Override
//        public EdgeIteratorState setReverse(BooleanEncodedValue property, boolean value) {
//            return currState.setReverse(property, value);
//        }
//
//        @Override
//        public EdgeIteratorState set(BooleanEncodedValue booleanEncodedValue, boolean b, boolean b1) {
//            return null;
//        }
//
//        @Override
//        public int get(IntEncodedValue property) {
//            return currState.get(property);
//        }
//
//        @Override
//        public EdgeIteratorState set(IntEncodedValue property, int value) {
//            return currState.set(property, value);
//        }
//
//        @Override
//        public int getReverse(IntEncodedValue property) {
//            return currState.getReverse(property);
//        }
//
//        @Override
//        public EdgeIteratorState setReverse(IntEncodedValue property, int value) {
//            return currState.setReverse(property, value);
//        }
//
//        @Override
//        public EdgeIteratorState set(IntEncodedValue intEncodedValue, int i, int i1) {
//            return null;
//        }
//
//        @Override
//        public double get(DecimalEncodedValue property) {
//            return currState.get(property);
//        }
//
//        @Override
//        public EdgeIteratorState set(DecimalEncodedValue property, double value) {
//            return currState.set(property, value);
//        }
//
//        @Override
//        public double getReverse(DecimalEncodedValue property) {
//            return currState.getReverse(property);
//        }
//
//        @Override
//        public EdgeIteratorState setReverse(DecimalEncodedValue property, double value) {
//            return currState.setReverse(property, value);
//        }
//
//        @Override
//        public EdgeIteratorState set(DecimalEncodedValue decimalEncodedValue, double v, double v1) {
//            return null;
//        }
//
//        @Override
//        public <T extends Enum<?>> T get(EnumEncodedValue<T> property) {
//            return currState.get(property);
//        }
//
//        @Override
//        public <T extends Enum<?>> EdgeIteratorState set(EnumEncodedValue<T> property, T value) {
//            return currState.set(property, value);
//        }
//
//        @Override
//        public <T extends Enum<?>> T getReverse(EnumEncodedValue<T> property) {
//            return currState.getReverse(property);
//        }
//
//        @Override
//        public <T extends Enum<?>> EdgeIteratorState setReverse(EnumEncodedValue<T> property, T value) {
//            return currState.setReverse(property, value);
//        }
//
//        @Override
//        public <T extends Enum<?>> EdgeIteratorState set(EnumEncodedValue<T> enumEncodedValue, T t, T t1) {
//            return null;
//        }
//
//        @Override
//        public String get(StringEncodedValue stringEncodedValue) {
//            return null;
//        }
//
//        @Override
//        public EdgeIteratorState set(StringEncodedValue stringEncodedValue, String s) {
//            return null;
//        }
//
//        @Override
//        public String getReverse(StringEncodedValue stringEncodedValue) {
//            return null;
//        }
//
//        @Override
//        public EdgeIteratorState setReverse(StringEncodedValue stringEncodedValue, String s) {
//            return null;
//        }
//
//        @Override
//        public EdgeIteratorState set(StringEncodedValue stringEncodedValue, String s, String s1) {
//            return null;
//        }
//
//        @Override
//        public String getName() {
//            return currState.getName();
//        }
//
//        @Override
//        public EdgeIteratorState setName(String name) {
//            return null;
//        }
//
//        @Override
//        public EdgeIteratorState detach(boolean reverse) {
//            return currState.detach(reverse);
//        }
//
//        @Override
//        public EdgeIteratorState copyPropertiesFrom(EdgeIteratorState e) {
//            return null;
//        }


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

//        @Override
//        public CHEdgeIteratorState setSkippedEdges(int edge1, int edge2) {
//            return this;
//        }

        @Override
        public boolean isShortcut() {
            if (currState instanceof CHEdgeIteratorState)
                return (((CHEdgeIteratorState) currState).isShortcut());
            else
                return false;
        }

//        @Override
//        public boolean getFwdAccess() {
//            return false;
//        }
//
//        @Override
//        public boolean getBwdAccess() {
//            return false;
//        }
//
//        @Override
//        public double getWeight() {
//            return (((CHEdgeIteratorState) currState).getWeight());
//        }
//
//        @Override
//        public CHEdgeIteratorState setWeight(double weight) {
//            return null;
//        }
//
//        @Override
//        public void setFlagsAndWeight(int flags, double weight) {
//            // do nothing
//        }
//
//        @Override
//        public CHEdgeIteratorState setTime(long time) {
//            return null;
//        }
//
//        @Override
//        public long getTime() {
//            return 0;
//        }
    }
}
