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

package org.heigit.ors.routing.graphhopper.extensions.edgefilters.core;

import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.DefaultTurnCostProvider;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;


/**
 * This class includes in the core all edges with turn restrictions.
 *
 * @author Athanasios Kogios
 * @author Andrzej Oles
 */

public class TurnRestrictionsCoreEdgeFilter implements EdgeFilter {
    private final TurnCostProvider turnCostProvider;
    private final FlagEncoder flagEncoder;
    private final EdgeExplorer inEdgeExplorer;
    private final EdgeExplorer outEdgeExplorer;
    private final Graph graph;

    public TurnRestrictionsCoreEdgeFilter(FlagEncoder encoder, GraphHopperStorage graphHopperStorage) {
        if (!encoder.isRegistered())
            throw new IllegalStateException("Make sure you add the FlagEncoder " + encoder + " to an EncodingManager before using it elsewhere");

        this.flagEncoder = encoder;
        this.graph = graphHopperStorage.getBaseGraph();
        turnCostProvider = new DefaultTurnCostProvider(flagEncoder, graphHopperStorage.getTurnCostStorage());
        inEdgeExplorer = graph.createEdgeExplorer(AccessFilter.inEdges(flagEncoder.getAccessEnc()));
        outEdgeExplorer = graph.createEdgeExplorer(AccessFilter.outEdges(flagEncoder.getAccessEnc()));
    }

    boolean hasTurnRestrictions(EdgeIteratorState edge) {
        return (isInvolvedInTurnRelation(edge, inEdgeExplorer) || isInvolvedInTurnRelation(edge, outEdgeExplorer));
    }

    boolean isInvolvedInTurnRelation(EdgeIteratorState edge, EdgeExplorer edgeExplorer) {
        int queriedEdge = edge.getEdge();
        int viaNode = (edgeExplorer == inEdgeExplorer) ? edge.getBaseNode() : edge.getAdjNode();
        EdgeIterator edgeIterator = edgeExplorer.setBaseNode(viaNode);

        while (edgeIterator.next()) {
            int otherEdge = edgeIterator.getEdge();
            //Do not add edges to the core because of u turn restrictions
            if (queriedEdge == otherEdge)
                continue;
            Double turnCost = (edgeExplorer == inEdgeExplorer) ?
                    turnCostProvider.calcTurnWeight(otherEdge, viaNode, queriedEdge) :
                    turnCostProvider.calcTurnWeight(queriedEdge, viaNode, otherEdge);
            if (turnCost.equals(Double.POSITIVE_INFINITY))
                return true;
        }

        return false;
    }

    @Override
    public boolean accept(EdgeIteratorState edge) {
        return !hasTurnRestrictions(edge);
    }
}


