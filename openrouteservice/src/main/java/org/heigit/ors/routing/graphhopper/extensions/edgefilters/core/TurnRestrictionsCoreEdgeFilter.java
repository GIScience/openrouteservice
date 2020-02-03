package org.heigit.ors.routing.graphhopper.extensions.edgefilters.core;

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

import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.TurnCostEncoder;
import com.graphhopper.storage.*;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.routing.util.FlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;

/**
 * This class includes in the core all edges with turn restrictions.
 *
 * @author Athanasios Kogios
 */

public class TurnRestrictionsCoreEdgeFilter implements EdgeFilter {
    private TurnCostExtension turnCostExtension;
    public final FlagEncoder flagEncoder;
    private final EdgeExplorer innerInExplorer;
    private final EdgeExplorer innerOutExplorer;
    private Graph graph;

    public TurnRestrictionsCoreEdgeFilter(FlagEncoder encoder, GraphStorage graphStorage, Graph graph) {
        this.flagEncoder = encoder;

        if (!flagEncoder.isRegistered())
            throw new IllegalStateException("Make sure you add the FlagEncoder " + flagEncoder + " to an EncodingManager before using it elsewhere");
        turnCostExtension = GraphStorageUtils.getGraphExtension(graphStorage, TurnCostExtension.class);
        innerInExplorer = graph.createEdgeExplorer(DefaultEdgeFilter.inEdges(flagEncoder));
        innerOutExplorer = graph.createEdgeExplorer(DefaultEdgeFilter.outEdges(flagEncoder));

        this.graph = graph;
    }

    protected int getOrigEdgeId(EdgeIteratorState edge, boolean reverse) {
        return reverse ? edge.getOrigEdgeFirst() : edge.getOrigEdgeLast();
    }

    boolean hasTurnRestrictions(EdgeIteratorState edge, boolean reverse) {
        EdgeIterator iter = reverse ? innerInExplorer.setBaseNode(edge.getAdjNode()) : innerOutExplorer.setBaseNode(edge.getAdjNode());
        boolean hasTurnRestrictions = false;

        while (iter.next()) {
            final int edgeId = getOrigEdgeId(iter, !reverse);
            final int prevOrNextOrigEdgeId = getOrigEdgeId(edge, reverse);
            if (edgeId == prevOrNextOrigEdgeId) {
                continue;
            }

            long turnFlags = reverse ? turnCostExtension.getTurnCostFlags(iter.getOrigEdgeLast() , iter.getAdjNode(), prevOrNextOrigEdgeId) : turnCostExtension.getTurnCostFlags(prevOrNextOrigEdgeId, iter.getAdjNode(), iter.getOrigEdgeFirst());
            boolean test = flagEncoder.isTurnRestricted(turnFlags);
            if (flagEncoder.isTurnRestricted(turnFlags)) { //There is a turn restriction
                hasTurnRestrictions = true;
                break;
            }
        }

        return hasTurnRestrictions;
    }


    //TODO Solve problems in the code looking at the AbstractBidirectionEdgeCHNoSOD.java
    @Override
    public boolean accept(EdgeIteratorState edge) {
        EdgeIteratorState edgeTest = edge;
        boolean reverse = edge.get(EdgeIteratorState.REVERSE_STATE);


        if (hasTurnRestrictions(edge, reverse)) {
            return false;
        } else if (hasTurnRestrictions(edge, !reverse)) {
            return false;
        } else {
            return true;
        }
    }
}


