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

import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.*;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.routing.util.FlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;

import java.util.HashSet;

/**
 * This class includes in the core all edges with turn restrictions.
 *
 * @author Athanasios Kogios
 */

public class TurnRestrictionsCoreEdgeFilter implements EdgeFilter {
    private TurnCostExtension turnCostExtension;
    public final FlagEncoder flagEncoder;
    private final  EdgeExplorer allEdgesExplorer;
    private Graph graph;
    public static HashSet<Integer> acceptedEdges = new HashSet<>();

    public TurnRestrictionsCoreEdgeFilter(FlagEncoder encoder, GraphHopperStorage graphHopperStorage) {
        this.flagEncoder = encoder;

        this.graph = graphHopperStorage.getBaseGraph();

        if (!flagEncoder.isRegistered())
            throw new IllegalStateException("Make sure you add the FlagEncoder " + flagEncoder + " to an EncodingManager before using it elsewhere");
        turnCostExtension = GraphStorageUtils.getGraphExtension(graphHopperStorage, TurnCostExtension.class);
        allEdgesExplorer = graph.createEdgeExplorer(DefaultEdgeFilter.allEdges(flagEncoder));
    }

    boolean hasTurnRestrictions(EdgeIteratorState edge) {
        EdgeIterator iterationTo = allEdgesExplorer.setBaseNode(edge.getAdjNode());

        while ( iterationTo.next()) {
            long turnFlags = turnCostExtension.getTurnCostFlags( edge.getEdge() , edge.getAdjNode(), iterationTo.getEdge());
            if (flagEncoder.isTurnRestricted(turnFlags))
                return true;
            turnFlags = turnCostExtension.getTurnCostFlags( iterationTo.getEdge() , edge.getAdjNode(), edge.getEdge());
            if (flagEncoder.isTurnRestricted(turnFlags))
                return true;
        }

        return false;
    }


    @Override
    public boolean accept(EdgeIteratorState edge) {

        if ( hasTurnRestrictions(edge) ) {
            acceptedEdges.add(edge.getEdge());
            return false;
        } else{
            return true;
        }
    }
}


