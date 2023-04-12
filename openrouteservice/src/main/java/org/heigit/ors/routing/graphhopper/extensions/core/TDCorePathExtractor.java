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

import com.graphhopper.routing.Path;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.RoutingCHEdgeIteratorState;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.util.*;

public class TDCorePathExtractor extends CorePathExtractor {

    public static Path extractPath(RoutingCHGraph graph, Weighting weighting, SPTEntry fwdEntry, SPTEntry bwdEntry, double weight) {
        return (new TDCorePathExtractor(graph, weighting)).extract(fwdEntry, bwdEntry, weight);
    }

    protected TDCorePathExtractor(RoutingCHGraph routingGraph, Weighting weighting) {
        super(routingGraph, weighting);
    }

    @Override
    protected void onMeetingPoint(int inEdge, int viaNode, int outEdge) {
        // no need to process any turns at meeting point
    }

    @Override
    protected SPTEntry followParentsUntilRoot(SPTEntry sptEntry, boolean reverse) {
        SPTEntry currEntry = sptEntry;
        SPTEntry parentEntry = currEntry.parent;
        while (EdgeIterator.Edge.isValid(currEntry.edge)) {
            onTdEdge(currEntry, reverse); // Here, TD differs from DefaultBidirPathExtractor
            currEntry = parentEntry;
            parentEntry = currEntry.parent;
        }
        return currEntry;
    }

    private void onTdEdge(SPTEntry currEdge, boolean bwd) {
        int edgeId = currEdge.edge;
        int adjNode = currEdge.adjNode;
        RoutingCHEdgeIteratorState edgeState = getRoutingGraph().getEdgeIteratorState(edgeId, adjNode);

        // Shortcuts do only contain valid weight, so first expand before adding
        // to distance and time
        if (edgeState.isShortcut()) {
            int edge = currEdge.parent.edge;
            onEdge(edgeId, adjNode, bwd, edge);
        } else {
            EdgeIteratorState edge = getRoutingGraph().getBaseGraph().getEdgeIteratorState(edgeState.getOrigEdge(), edgeState.getAdjNode());
            path.addDistance(edge.getDistance());
            path.addTime((bwd ? -1 : 1) * (currEdge.time - currEdge.parent.time));
            path.addEdge(edge.getEdge());
        }
    }

}
