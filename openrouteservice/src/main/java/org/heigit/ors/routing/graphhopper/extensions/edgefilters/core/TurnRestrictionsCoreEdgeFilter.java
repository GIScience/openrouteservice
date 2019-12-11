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

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.storage.TurnCostExtension;
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
    private TurnCostExtension storage;
    public final FlagEncoder flagEncoder;
    private EdgeExplorer edgeExplorer;

    public TurnRestrictionsCoreEdgeFilter(FlagEncoder encoder, GraphStorage graphStorage) {
        this.flagEncoder = encoder;

        if (!flagEncoder.isRegistered())
            throw new IllegalStateException("Make sure you add the FlagEncoder " + flagEncoder + " to an EncodingManager before using it elsewhere");
        storage = GraphStorageUtils.getGraphExtension(graphStorage, TurnCostExtension.class);
    }


    @Override
    public boolean accept(EdgeIteratorState edge) {
        EdgeIteratorState edgeTest = edge;
        IntsRef edgeFlags = edge.getFlags();
        EdgeIterator iter = edgeExplorer.setBaseNode(edge.getAdjNode());
        boolean hasTurnRestriction = false;
        while(iter.next()){

        }
        long test = storage.getTurnCostFlags(edge.getEdge(), edge.getAdjNode(), edge.getOrigEdgeLast());
        if ( test == Double.POSITIVE_INFINITY ) { //If the max speed of the road is greater than that of the limit include it in the core.
            return false;
        } else {
            return true;
        }
    }
}

