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
import com.graphhopper.routing.PathTDCore;
import com.graphhopper.routing.util.ConditionalAccessEdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Parameters;

public class TDCoreDijkstra extends CoreDijkstra {
    private boolean reverse;

    public TDCoreDijkstra(Graph graph, Weighting weighting, boolean reverse) {
        super(graph, weighting);
        this.reverse = reverse;
    }

    @Override
    protected Path createAndInitPath() {
        bestPath = new PathTDCore(graph, graph.getBaseGraph(), weighting);
        return bestPath;
    }

    @Override
    protected void initPhase2() {
        if (flagEncoder.hasEncodedValue(flagEncoder.toString()+"-conditional_access")) {
            inEdgeExplorer = graph.createEdgeExplorer(ConditionalAccessEdgeFilter.inEdges(flagEncoder));
            outEdgeExplorer = graph.createEdgeExplorer(ConditionalAccessEdgeFilter.outEdges(flagEncoder));
        }
    }

    @Override
    public boolean fillEdgesFromCore() {
        if (reverse)
            return true;

        return super.fillEdgesFromCore();
    }

    @Override
    public boolean fillEdgesToCore() {
        if (!reverse)
            return true;

        return super.fillEdgesToCore();
    }

    @Override
    double calcWeight(EdgeIterator iter, SPTEntry currEdge, boolean reverse) {
        return weighting.calcEdgeWeight(iter, reverse, currEdge.originalEdge/* TODO: , currEdge.time*/);
    }

    @Override
    long calcTime(EdgeIteratorState iter, SPTEntry currEdge, boolean reverse) {
        return currEdge.time + (reverse ? -1 : 1) * weighting.calcEdgeMillis(iter, reverse, currEdge.edge/* TODO: , currEdge.time*/);
    }

    @Override
    public String getName() {
        return Parameters.Algorithms.TD_DIJKSTRA;
    }
}
