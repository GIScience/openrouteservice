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

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.RoutingCHEdgeIteratorState;
import com.graphhopper.storage.RoutingCHGraph;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.ch.DownwardSearchEdgeFilter;

public class ExclusiveDownwardSearchEdgeFilter extends DownwardSearchEdgeFilter {
    private boolean swap = false;

    public ExclusiveDownwardSearchEdgeFilter(RoutingCHGraph g, FlagEncoder encoder) {
        super(g, encoder);
    }

    public ExclusiveDownwardSearchEdgeFilter(RoutingCHGraph g, FlagEncoder encoder, boolean swap) {
        this(g, encoder);
        this.swap = swap;
    }

    //    @Override
    public boolean accept(RoutingCHEdgeIteratorState edgeIterState) {
        int adj = edgeIterState.getAdjNode();
        if (baseNode >= maxNodes || adj >= maxNodes || baseNodeLevel < graph.getLevel(adj))
            return swap ? isAccessible(edgeIterState, true) : isAccessible(edgeIterState, false);
        else
            return false;
    }
}
