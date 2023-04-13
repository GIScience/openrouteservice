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

import com.graphhopper.routing.DefaultBidirPathExtractor;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.ch.ShortcutUnpacker;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.RoutingCHGraph;

public class CorePathExtractor extends DefaultBidirPathExtractor {
    private final RoutingCHGraph routingGraph;
    private final ShortcutUnpacker shortcutUnpacker;
    private final Weighting weighting;

    public static Path extractPath(RoutingCHGraph graph, Weighting weighting, SPTEntry fwdEntry, SPTEntry bwdEntry, double weight) {
        return (new CorePathExtractor(graph, weighting)).extract(fwdEntry, bwdEntry, weight);
    }

    public CorePathExtractor(RoutingCHGraph routingGraph, Weighting weighting) {
        super(routingGraph.getBaseGraph(), weighting);
        this.routingGraph = routingGraph;
        this.shortcutUnpacker = this.createShortcutUnpacker();
        this.weighting = weighting;
    }

    public void onEdge(int edge, int adjNode, boolean reverse, int prevOrNextEdge) {
        if (reverse) {
            this.shortcutUnpacker.visitOriginalEdgesBwd(edge, adjNode, true, prevOrNextEdge);
        } else {
            this.shortcutUnpacker.visitOriginalEdgesFwd(edge, adjNode, true, prevOrNextEdge);
        }
    }

    private ShortcutUnpacker createShortcutUnpacker() {
        return new ShortcutUnpacker(this.routingGraph, (edge, reverse, prevOrNextEdgeId) -> {
            this.path.addDistance(edge.getDistance());
            this.path.addTime(weighting.calcEdgeMillis(edge, reverse));
            this.path.addEdge(edge.getEdge());
        }, false);// Turn restrictions are handled in the core, hence, shortcuts have no turn restrictions and unpacking can be node based.
    }

    public RoutingCHGraph getRoutingGraph() {
        return routingGraph;
    }
}
