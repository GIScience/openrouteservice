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
import com.graphhopper.routing.lm.LMApproximator;
import com.graphhopper.routing.lm.LandmarkStorage;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.querygraph.QueryRoutingCoreGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.RoutingCHGraph;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.routing.graphhopper.extensions.util.GraphUtils;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters;

import static com.graphhopper.util.Parameters.Algorithms.*;

/**
 * Given a {@link RoutingCHGraph} and possibly a {@link QueryGraph} this class sets up and creates routing
 * algorithm instances used for CH.
 */
public class CoreRoutingAlgorithmFactory implements RoutingAlgorithmFactory {
    private final RoutingCHGraph routingCHGraph;
    private LandmarkStorage lms;
    private int defaultActiveLandmarks;

    public CoreRoutingAlgorithmFactory(RoutingCHGraph routingCHGraph, QueryGraph queryGraph, LandmarkStorage lms) {
        this(routingCHGraph, queryGraph);
        this.lms = lms;
        this.defaultActiveLandmarks = Math.max(1, Math.min(lms.getLandmarkCount() / 2, 12));
    }

    public CoreRoutingAlgorithmFactory(RoutingCHGraph routingCHGraph, QueryGraph queryGraph) {
        this(new QueryRoutingCoreGraph(routingCHGraph, queryGraph));
    }

    public CoreRoutingAlgorithmFactory(RoutingCHGraph routingCHGraph) {
        this.routingCHGraph = routingCHGraph;
    }

    @Override
    public RoutingAlgorithm createAlgo(Graph graph, Weighting weighting, AlgorithmOptions opts) {
        AbstractCoreRoutingAlgorithm algo;
        String algoStr = opts.getAlgorithm();

        if (ASTAR_BI.equals(algoStr)) {
            CoreALT tmpAlgo = new CoreALT(routingCHGraph, weighting);
            if (lms != null) {
                int activeLM = Math.max(1, opts.getHints().getInt(ORSParameters.CoreLandmark.ACTIVE_COUNT, defaultActiveLandmarks));
                LMApproximator lmApproximator = new LMApproximator(graph, lms.getWeighting(), GraphUtils.getBaseGraph(graph).getNodes(), lms, activeLM, lms.getFactor(), false);
                tmpAlgo.setApproximation(lmApproximator);
            }
            algo = tmpAlgo;
        } else if (DIJKSTRA_BI.equals(algoStr)) {
            algo = new CoreDijkstra(routingCHGraph, weighting);
        } else if (TD_DIJKSTRA.equals(algoStr)) {
            algo = new TDCoreDijkstra(routingCHGraph, weighting, opts.getHints().has(RouteRequest.PARAM_ARRIVAL));
        } else if (TD_ASTAR.equals(algoStr)) {
            CoreALT tmpAlgo = new TDCoreALT(routingCHGraph, weighting, opts.getHints().has(RouteRequest.PARAM_ARRIVAL));
            if (lms != null) {
                int activeLM = Math.max(1, opts.getHints().getInt(ORSParameters.CoreLandmark.ACTIVE_COUNT, defaultActiveLandmarks));
                LMApproximator lmApproximator = new LMApproximator(graph, lms.getWeighting(), GraphUtils.getBaseGraph(graph).getNodes(), lms, activeLM, lms.getFactor(), false);
                tmpAlgo.setApproximation(lmApproximator);
            }
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
