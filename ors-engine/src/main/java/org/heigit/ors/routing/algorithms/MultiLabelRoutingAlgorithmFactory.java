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

package org.heigit.ors.routing.algorithms;

import com.graphhopper.routing.AbstractRoutingAlgorithm;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.lm.LMApproximator;
import com.graphhopper.routing.lm.LandmarkStorage;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.querygraph.QueryRoutingCoreGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.RoutingCHGraph;
import org.heigit.ors.routing.RouteRequestParameterNames;
import org.heigit.ors.routing.graphhopper.extensions.core.*;
import org.heigit.ors.routing.graphhopper.extensions.util.GraphUtils;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters;

import static com.graphhopper.util.Parameters.Algorithms.*;

public class MultiLabelRoutingAlgorithmFactory implements RoutingAlgorithmFactory {

    @Override
    public RoutingAlgorithm createAlgo(Graph graph, Weighting weighting, AlgorithmOptions opts) {
        AbstractRoutingAlgorithm algo = new MultiLabelDijkstraAlgorithm(graph, weighting, opts.getTraversalMode());
        algo.setMaxVisitedNodes(opts.getMaxVisitedNodes());
        return algo;
    }
}
