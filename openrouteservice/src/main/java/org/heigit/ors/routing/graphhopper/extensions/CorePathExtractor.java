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
package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.routing.DefaultBidirPathExtractor;
import com.graphhopper.routing.ch.ShortcutUnpacker;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.RoutingCHGraph;

public class CorePathExtractor extends DefaultBidirPathExtractor {
    private final ShortcutUnpacker shortcutUnpacker;
    private final RoutingCHGraph routingGraph;

    public CorePathExtractor(RoutingCHGraph routingGraph, Weighting weighting) {
        super(routingGraph.getBaseGraph(), weighting);
        this.routingGraph = routingGraph;
        this.shortcutUnpacker = this.createShortcutUnpacker(weighting);
    }

    public void onEdge(int edge, int adjNode, boolean reverse, int prevOrNextEdge) {
        if (reverse) {
            this.shortcutUnpacker.visitOriginalEdgesBwd(edge, adjNode, true, prevOrNextEdge);
        } else {
            this.shortcutUnpacker.visitOriginalEdgesFwd(edge, adjNode, true, prevOrNextEdge);
        }

    }

    private ShortcutUnpacker createShortcutUnpacker(Weighting weighting) {
        return new ShortcutUnpacker(this.routingGraph, (edge, reverse, prevOrNextEdgeId) -> {
            this.path.addDistance(edge.getDistance());
            this.path.addTime(weighting.calcEdgeMillis(edge, reverse));
            this.path.addEdge(edge.getEdge());
        }, false);
    }
}
