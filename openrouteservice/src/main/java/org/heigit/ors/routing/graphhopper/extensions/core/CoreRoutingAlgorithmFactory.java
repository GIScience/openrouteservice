/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.routing.*;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.querygraph.QueryRoutingCHGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.RoutingCHGraph;
import org.heigit.ors.api.requests.routing.RouteRequest;

import static com.graphhopper.util.Parameters.Algorithms.*;

/**
 * Given a {@link RoutingCHGraph} and possibly a {@link QueryGraph} this class sets up and creates routing
 * algorithm instances used for CH.
 */
public class CoreRoutingAlgorithmFactory {
    private final RoutingCHGraph routingCHGraph;

    public CoreRoutingAlgorithmFactory(RoutingCHGraph routingCHGraph, QueryGraph queryGraph) {
        this(new QueryRoutingCHGraph(routingCHGraph, queryGraph));
    }

    public CoreRoutingAlgorithmFactory(RoutingCHGraph routingCHGraph) {
        this.routingCHGraph = routingCHGraph;
    }

    public AbstractCoreRoutingAlgorithm createAlgo(Weighting weighting, AlgorithmOptions opts) {
        AbstractCoreRoutingAlgorithm algo;
        String algoStr = DIJKSTRA_BI;//FIXME: opts.getAlgorithm();

        if (ASTAR_BI.equals(algoStr)) {
            CoreALT tmpAlgo = new CoreALT(routingCHGraph, weighting);
            //FIXME tmpAlgo.setApproximation(RoutingAlgorithmFactorySimple.getApproximation(ASTAR_BI, opts, graph.getNodeAccess()));
            algo = tmpAlgo;
        } else if (DIJKSTRA_BI.equals(algoStr)) {
            algo = new CoreDijkstra(routingCHGraph, weighting);
        } else if (TD_DIJKSTRA.equals(algoStr)) {
            algo = new TDCoreDijkstra(routingCHGraph, weighting, opts.getHints().has(RouteRequest.PARAM_ARRIVAL));
        } else if (TD_ASTAR.equals(algoStr)) {
            CoreALT tmpAlgo = new TDCoreALT(routingCHGraph, weighting, opts.getHints().has(RouteRequest.PARAM_ARRIVAL));
            //FIXME tmpAlgo.setApproximation(RoutingAlgorithmFactorySimple.getApproximation(ASTAR_BI, opts, graph.getNodeAccess()));
            algo = tmpAlgo;
        } else {
            throw new IllegalArgumentException("Algorithm " + opts.getAlgorithm()
                    + " not supported for Contraction Hierarchies. Try with ch.disable=true");
        }

        algo.setMaxVisitedNodes(opts.getMaxVisitedNodes());

        // append any restriction filters after node level filter
        CoreDijkstraFilter levelFilter = new CoreDijkstraFilter(routingCHGraph);
        EdgeFilter ef = opts.getEdgeFilter();
        if (ef != null)
            levelFilter.addRestrictionFilter(ef);

        algo.setEdgeFilter(levelFilter);

        return algo;
    }
}
