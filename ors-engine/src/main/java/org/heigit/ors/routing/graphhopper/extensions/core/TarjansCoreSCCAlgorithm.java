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
package org.heigit.ors.routing.graphhopper.extensions.core;

import com.carrotsearch.hppc.IntArrayDeque;
import com.carrotsearch.hppc.IntArrayList;
import com.graphhopper.coll.GHBitSet;
import com.graphhopper.coll.GHBitSetImpl;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.*;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperStorage;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreLandmarkStorage.CoreEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.util.GraphUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Implementation of Tarjan's algorithm using an explicit stack. The traditional recursive approach
 * runs into stack overflow pretty quickly. The algorithm is used within GraphHopper to find
 * strongly connected components to detect dead-ends leading to routes not found.
 * <p>
 * See http://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm. See
 * http://www.timl.id.au/?p=327 and http://homepages.ecs.vuw.ac.nz/~djp/files/P05.pdf
 *
 *
 * This has been adapted for use in the core.
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner
 */
public class TarjansCoreSCCAlgorithm {
    private final ArrayList<IntArrayList> components = new ArrayList<>();
    // TODO Refactoring : check if this comment might have been taken from GH code, probably irrelevant : "use just the Graph interface here"
    private final IntArrayDeque nodeStack;
    private final GHBitSet onStack;
    private final GHBitSet ignoreSet;
    private final int[] nodeIndex;
    private final int[] nodeLowLink;
    private final CHEdgeFilter edgeFilter;
    private int index = 1;
    private final RoutingCHGraph core;
    private final int coreNodeLevel;

    public TarjansCoreSCCAlgorithm(ORSGraphHopperStorage ghStorage, RoutingCHGraph core, final EdgeFilter edgeFilter, boolean ignoreSingleEntries) {
        this.core = core;
        this.nodeStack = new IntArrayDeque();
        this.onStack = new GHBitSetImpl(ghStorage.getNodes());
        this.nodeIndex = new int[ghStorage.getNodes()];
        this.nodeLowLink = new int[ghStorage.getNodes()];
        CoreEdgeFilter coreEdgeFilter = new CoreEdgeFilter(core, edgeFilter);
        this.edgeFilter = edge -> coreEdgeFilter.accept(edge) && Double.isFinite(edge.getWeight(false));
        this.coreNodeLevel = GraphUtils.getBaseGraph(core).getNodes();


        if (ignoreSingleEntries) {
            // Very important case to boost performance - see #520. Exclude single entry components as we don't need them! 
            // But they'll be created a lot for multiple vehicles because many nodes e.g. for foot are not accessible at all for car.
            // We can ignore these single entry components as they are already set 'not accessible'
            RoutingCHEdgeExplorer explorer = core.createOutEdgeExplorer();
            int nodes = ghStorage.getNodes();
            ignoreSet = new GHBitSetImpl(ghStorage.getCoreNodes());
            for (int start = 0; start < nodes; start++) {
                    CoreEdgeIterator iter = new CoreEdgeIterator(explorer.setBaseNode(start), this.edgeFilter);
                    if (!iter.next())
                        ignoreSet.add(start);
            }
        } else {
            ignoreSet = new GHBitSetImpl();
        }
    }

    public GHBitSet getIgnoreSet() {
        return ignoreSet;
    }

    /**
     * Find and return list of all strongly connected components in g.
     */
    public List<IntArrayList> findComponents() {
        int nodes = core.getNodes();
        for (int start = 0; start < nodes; start++) {
            if(core.getLevel(start) < coreNodeLevel)
                continue;
            if (nodeIndex[start] == 0 && !ignoreSet.contains(start))
                strongConnect(start);
        }

        return components;
    }

    /**
     * Find all components reachable from firstNode, add them to 'components'
     * <p>
     *
     * @param firstNode start search of SCC at this node
     */
    private void strongConnect(int firstNode) {
        final Deque <TarjanState> stateStack = new ArrayDeque<>();
        stateStack.push(TarjanState.startState(firstNode));

        // nextState label is equivalent to the function entry point in the recursive Tarjan's algorithm.
        nextState:

        while (!stateStack.isEmpty()) {
            TarjanState state = stateStack.pop();
            final int start = state.start;
            final RoutingCHEdgeIterator iter;

            if (state.isStart()) {
                // We're traversing a new node 'start'.  Set the depth index for this node to the smallest unused index.
                nodeIndex[start] = index;
                nodeLowLink[start] = index;
                index++;
                nodeStack.addLast(start);
                onStack.add(start);

                iter = new CoreEdgeIterator(core.createOutEdgeExplorer().setBaseNode(start), edgeFilter);

            } else {
                // We're resuming iteration over the next child of 'start', set lowLink as appropriate.
                iter = state.iter;

                int prevConnectedId = iter.getAdjNode();
                nodeLowLink[start] = Math.min(nodeLowLink[start], nodeLowLink[prevConnectedId]);
            }

            // Each element (excluding the first) in the current component should be able to find
            // a successor with a lower nodeLowLink.
            while (iter.next()) {
                int connectedId = iter.getAdjNode();
                if (ignoreSet.contains(start) || core.getLevel(start) < coreNodeLevel)
                    continue;

                if (nodeIndex[connectedId] == 0) {
                    // Push resume and start states onto state stack to continue our DFS through the graph after the jump.
                    // Ideally we'd just call strongConnectIterative(connectedId);
                    stateStack.push(TarjanState.resumeState(start, iter));
                    stateStack.push(TarjanState.startState(connectedId));
                    continue nextState;
                } else if (onStack.contains(connectedId)) {
                    nodeLowLink[start] = Math.min(nodeLowLink[start], nodeIndex[connectedId]);
                }
            }

            // If nodeLowLink == nodeIndex, then we are the first element in a component.
            // Add all nodes higher up on nodeStack to this component.
            if (nodeIndex[start] == nodeLowLink[start]) {
                IntArrayList component = new IntArrayList();
                int node;
                while ((node = nodeStack.removeLast()) != start) {
                    component.add(node);
                    onStack.remove(node);
                }
                component.add(start);
                component.trimToSize();
                onStack.remove(start);
                components.add(component);
            }
        }
    }

    /**
     * Internal stack state of algorithm, used to avoid recursive function calls and hitting stack
     * overflow exceptions. State is either 'start' for new nodes or 'resume' for partially
     * traversed nodes.
     */
    private static class TarjanState {
        final int start;
        final RoutingCHEdgeIterator iter;

        private TarjanState(final int start, final RoutingCHEdgeIterator iter) {
            this.start = start;
            this.iter = iter;
        }

        public static TarjanState startState(int start) {
            return new TarjanState(start, null);
        }

        public static TarjanState resumeState(int start, RoutingCHEdgeIterator iter) {
            return new TarjanState(start, iter);
        }

        // Iterator only present in 'resume' state.
        boolean isStart() {
            return iter == null;
        }
    }

    private static class CoreEdgeIterator implements RoutingCHEdgeIterator {
        private RoutingCHEdgeIterator chIterator;
        private CHEdgeFilter coreFilter;

        public CoreEdgeIterator(RoutingCHEdgeIterator chIterator, CHEdgeFilter coreFilter) {
            this.chIterator = chIterator;
            this.coreFilter = coreFilter;
        }

        @Override
        public boolean next() {
            while(chIterator.next()) {
                if (coreFilter.accept(chIterator))
                    return true;
            }
            return false;
        }

        @Override
        public int getEdge() {
            return chIterator.getEdge();
        }

        @Override
        public int getOrigEdge() {
            return chIterator.getOrigEdge();
        }

        @Override
        public int getOrigEdgeFirst() {
            return chIterator.getOrigEdgeFirst();
        }

        @Override
        public int getOrigEdgeLast() {
            return chIterator.getOrigEdgeLast();
        }

        @Override
        public int getBaseNode() {
            return chIterator.getBaseNode();
        }

        @Override
        public int getAdjNode() {
            return chIterator.getAdjNode();
        }

        @Override
        public boolean isShortcut() {
            return chIterator.isShortcut();
        }

        @Override
        public int getSkippedEdge1() {
            return chIterator.getSkippedEdge1();
        }

        @Override
        public int getSkippedEdge2() {
            return chIterator.getSkippedEdge2();
        }

        @Override
        public double getWeight(boolean reverse) {
            return chIterator.getWeight(reverse);
        }

        @Override
        public int getTime(boolean reverse) {
            return chIterator.getTime(reverse);
        }
    }
}
