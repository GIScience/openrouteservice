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

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import org.heigit.ors.config.MatrixServiceSettings;

public abstract class AbstractManyToManyRoutingAlgorithm implements ManyToManyRoutingAlgorithm {
    protected final RoutingCHGraph graph;
    protected final Weighting weighting;
    protected final FlagEncoder flagEncoder;
    protected final TraversalMode traversalMode;
    protected NodeAccess nodeAccess;
    protected RoutingCHEdgeExplorer inEdgeExplorer;
    protected RoutingCHEdgeExplorer outEdgeExplorer;
    protected int maxVisitedNodes = Integer.MAX_VALUE;
    private CHEdgeFilter additionalEdgeFilter;

    /**
     * @param graph         specifies the graph where this algorithm will run on
     * @param weighting     set the used weight calculation (e.g. fastest, shortest).
     * @param traversalMode how the graph is traversed e.g. if via nodes or edges.
     */
    protected AbstractManyToManyRoutingAlgorithm(RoutingCHGraph graph, Weighting weighting, TraversalMode traversalMode) {
        this.weighting = weighting;
        flagEncoder = weighting.getFlagEncoder();
        this.traversalMode = traversalMode;
        this.graph = graph;
        nodeAccess = graph.getBaseGraph().getNodeAccess();
        outEdgeExplorer = graph.createOutEdgeExplorer();//graph.createEdgeExplorer(AccessFilter.outEdges(flagEncoder.getAccessEnc()));
        inEdgeExplorer = graph.createInEdgeExplorer();//graph.createEdgeExplorer(AccessFilter.inEdges(flagEncoder.getAccessEnc()));
    }

    @Override
    public void setMaxVisitedNodes(int numberOfNodes) {
        maxVisitedNodes = numberOfNodes;
    }

    public AbstractManyToManyRoutingAlgorithm setEdgeFilter(CHEdgeFilter additionalEdgeFilter) {
        this.additionalEdgeFilter = additionalEdgeFilter;
        return this;
    }
    
    protected boolean accept(RoutingCHEdgeIterator iter, int prevOrNextEdgeId, boolean reverse) {
        if (MatrixServiceSettings.getUTurnCost() == Weighting.INFINITE_U_TURN_COSTS) {
            if (iter.getEdge() == prevOrNextEdgeId)
                return false;
            if (iter.isShortcut())
                return getIncEdge(iter, !reverse) != prevOrNextEdgeId;
        }
        return additionalEdgeFilter == null || additionalEdgeFilter.accept(iter);
    }

    /**
     * Get the incoming edge for iter. This is the last edge of the iter coming into the next edge.
     * This algorithm only uses forwards searches, therefore its always last edge, never first edge.
     * @param iter The iterator whose edge is incoming
     * @return the incoming edge
     */
    protected int getIncEdge(RoutingCHEdgeIteratorState iter, boolean reverse) {
        if (iter.isShortcut()) {
            return reverse ? iter.getSkippedEdge1() : iter.getSkippedEdge2();
        }
        else {
            return iter.getOrigEdge();
        }
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getName() + "|" + weighting;
    }

    protected boolean isMaxVisitedNodesExceeded() {
        return maxVisitedNodes < getVisitedNodes();
    }
}
