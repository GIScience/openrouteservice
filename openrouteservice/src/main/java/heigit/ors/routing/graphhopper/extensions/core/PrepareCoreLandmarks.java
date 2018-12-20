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
package heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.routing.*;
import com.graphhopper.routing.lm.LMApproximator;
import com.graphhopper.routing.lm.LandmarkStorage;
import com.graphhopper.routing.lm.LandmarkSuggestion;
import com.graphhopper.routing.util.AbstractAlgoPreparation;
import com.graphhopper.routing.util.spatialrules.SpatialRuleLookup;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraphImpl;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.Parameters.Landmark;
import com.graphhopper.util.StopWatch;
import heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;
import heigit.ors.routing.graphhopper.extensions.util.ORSParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class does the preprocessing for the ALT algorithm (A* , landmark, triangle inequality) in the core.
 * <p>
 * http://www.siam.org/meetings/alenex05/papers/03agoldberg.pdf
 *
 * @author Peter Karich
 * @author Hendrik Leuschner
 */
public class PrepareCoreLandmarks extends AbstractAlgoPreparation {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrepareCoreLandmarks.class);
    private final Graph graph;
    private final CoreLandmarkStorage lms;
    private final Weighting weighting;
    private int defaultActiveLandmarks;
    private LMEdgeFilterSequence landmarksFilter;

    public PrepareCoreLandmarks(Directory dir, GraphHopperStorage graph, Weighting weighting, LMEdgeFilterSequence landmarksFilter, int landmarks,
                                int activeLandmarks) {
        if (activeLandmarks > landmarks)
            throw new IllegalArgumentException("Default value for active landmarks " + activeLandmarks
                    + " should be less or equal to landmark count of " + landmarks);
        this.graph = graph;
        this.defaultActiveLandmarks = activeLandmarks;
        this.weighting = weighting;
        this.landmarksFilter = landmarksFilter;
        lms = new CoreLandmarkStorage(graph, dir, weighting, landmarksFilter, landmarks);
    }

    /**
     * @see LandmarkStorage#setLandmarkSuggestions(List)
     */
    public PrepareCoreLandmarks setLandmarkSuggestions(List<LandmarkSuggestion> landmarkSuggestions) {
        lms.setLandmarkSuggestions(landmarkSuggestions);
        return this;
    }

    /**
     * @see LandmarkStorage#setSpatialRuleLookup(SpatialRuleLookup)
     */
    public PrepareCoreLandmarks setSpatialRuleLookup(SpatialRuleLookup ruleLookup) {
        lms.setSpatialRuleLookup(ruleLookup);
        return this;
    }

    /**
     * @see LandmarkStorage#setMaximumWeight(double)
     */
    public PrepareCoreLandmarks setMaximumWeight(double maximumWeight) {
        lms.setMaximumWeight(maximumWeight);
        return this;
    }

    /**
     * @see LandmarkStorage#setLMSelectionWeighting(Weighting)
     */
    public void setLMSelectionWeighting(Weighting w) {
        lms.setLMSelectionWeighting(w);
    }

    /**
     * @see LandmarkStorage#setMinimumNodes(int)
     */
    public void setMinimumNodes(int nodes) {
        if (nodes < 2)
            throw new IllegalArgumentException("minimum node count must be at least 2");

        lms.setMinimumNodes(nodes);
    }

    public PrepareCoreLandmarks setLogDetails(boolean logDetails) {
        lms.setLogDetails(logDetails);
        return this;
    }

    public CoreLandmarkStorage getLandmarkStorage() {
        return lms;
    }

    public int getSubnetworksWithLandmarks() {
        return lms.getSubnetworksWithLandmarks();
    }

    public Weighting getWeighting() {
        return weighting;
    }

    public boolean loadExisting() {
        return lms.loadExisting();
    }

    @Override
    public void doWork() {
        super.doWork();

        LOGGER.info("Start calculating " + lms.getLandmarkCount() + " landmarks, default active lms:"
                + defaultActiveLandmarks + ", weighting:" + lms.getLmSelectionWeighting() + ", " + Helper.getMemInfo());
        lms.createCoreNodeIdMap();
        lms.createLandmarks();
        lms.flush();
    }

    public RoutingAlgorithm getDecoratedAlgorithm(Graph qGraph, RoutingAlgorithm algo, AlgorithmOptions opts) {
        int activeLM = Math.max(1, opts.getHints().getInt(ORSParameters.CoreLandmark.ACTIVE_COUNT, defaultActiveLandmarks));

        if (algo instanceof CoreALT) {
            if (!lms.isInitialized())
                throw new IllegalStateException("Initalize landmark storage before creating algorithms");

            double epsilon = opts.getHints().getDouble(Parameters.Algorithms.ASTAR_BI + ".epsilon", 1);
            CoreALT coreALT = (CoreALT) algo;

            coreALT.setApproximation(
                    new CoreLMApproximator(qGraph, this.graph.getNodes(), lms, activeLM, lms.getFactor(), false)
                            .setEpsilon(epsilon));
            return algo;
        }
        if (algo instanceof AStar) {
            if (!lms.isInitialized())
                throw new IllegalStateException("Initalize landmark storage before creating algorithms");

            double epsilon = opts.getHints().getDouble(Parameters.Algorithms.ASTAR + ".epsilon", 1);
            AStar astar = (AStar) algo;

            astar.setApproximation(
                    new CoreLMApproximator(qGraph, this.graph.getNodes(), lms, activeLM, lms.getFactor(), false)
                            .setEpsilon(epsilon));
            return algo;
        } else if (algo instanceof AStarBidirection) {
            if (!lms.isInitialized())
                throw new IllegalStateException("Initalize landmark storage before creating algorithms");

            double epsilon = opts.getHints().getDouble(Parameters.Algorithms.ASTAR_BI + ".epsilon", 1);
            AStarBidirection astarbi = (AStarBidirection) algo;

            astarbi.setApproximation(
                    new CoreLMApproximator(qGraph, this.graph.getNodes(), lms, activeLM, lms.getFactor(), false)
                            .setEpsilon(epsilon));
            return algo;
        } else if (algo instanceof AlternativeRoute) {
            if (!lms.isInitialized())
                throw new IllegalStateException("Initalize landmark storage before creating algorithms");

            double epsilon = opts.getHints().getDouble(Parameters.Algorithms.ASTAR_BI + ".epsilon", 1);
            AlternativeRoute altRoute = (AlternativeRoute) algo;
            //TODO  //TODO Should work with standard LMApproximator

            altRoute.setApproximation(
                    new CoreLMApproximator(qGraph, this.graph.getNodes(), lms, activeLM, lms.getFactor(), false)
                            .setEpsilon(epsilon));
            // landmark algorithm follows good compromise between fast response and exploring 'interesting' paths so we
            // can decrease this exploration factor further (1->dijkstra, 0.8->bidir. A*)
            altRoute.setMaxExplorationFactor(0.6);
        }

        return algo;
    }

    public boolean matchesFilter(PMap pmap){
        //Returns true if the landmarkset is for the avoidables.
        //Also returns true if the query has no avoidables and the set has no avoidables
            if(landmarksFilter.isFilter(pmap))
                return true;
            return false;
    }
}
