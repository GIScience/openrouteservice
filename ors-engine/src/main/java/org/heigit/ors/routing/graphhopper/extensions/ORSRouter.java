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

import com.graphhopper.GHRequest;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.*;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.lm.LandmarkStorage;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.PMap;
import com.graphhopper.util.TranslationMap;
import com.graphhopper.util.details.PathDetailsBuilderFactory;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreRoutingAlgorithmFactory;
import org.heigit.ors.routing.graphhopper.extensions.core.PrepareCoreLandmarks;

import java.util.Map;

public class ORSRouter extends Router {
    private final GraphHopperStorage ghStorage;
    private final EncodingManager encodingManager;
    private final Map<String, Profile> profilesByName;
    private final RouterConfig routerConfig;
    private final WeightingFactory weightingFactory;
    private Map<String, RoutingCHGraph> coreGraphs;
    private Map<String, PrepareCoreLandmarks> coreLandmarks;

    public ORSRouter(GraphHopperStorage ghStorage, LocationIndex locationIndex, Map<String, Profile> profilesByName, PathDetailsBuilderFactory pathDetailsBuilderFactory, TranslationMap translationMap, RouterConfig routerConfig, WeightingFactory weightingFactory, Map<String, RoutingCHGraph> chGraphs, Map<String, LandmarkStorage> landmarks) {
        super(ghStorage, locationIndex, profilesByName, pathDetailsBuilderFactory, translationMap, routerConfig, weightingFactory, chGraphs, landmarks);
        this.ghStorage = ghStorage;
        this.encodingManager = ghStorage.getEncodingManager();
        this.profilesByName = profilesByName;
        this.routerConfig = routerConfig;
        this.weightingFactory = weightingFactory;
    }

    public void setCoreGraphs(Map<String, RoutingCHGraph> coreGraphs) {
        this.coreGraphs = coreGraphs;
    }

    public void setCoreLandmarks(Map<String, PrepareCoreLandmarks> coreLandmarks) {
        this.coreLandmarks = coreLandmarks;
    }

    private static boolean getDisableCore(PMap hints) {
        return hints.getBool("core.disable", true);
    }

    @Override
    protected Router.Solver createSolver(GHRequest request, EdgeFilterFactory edgeFilterFactory) {
        boolean disableCore = getDisableCore(request.getHints());
        if (!disableCore) {
            return new ORSRouter.CoreSolver(request, this.profilesByName, this.routerConfig, this.encodingManager, this.weightingFactory, this.ghStorage, this.coreGraphs, this.coreLandmarks).setEdgeFilterFactory(edgeFilterFactory);
        } else {
            return super.createSolver(request, edgeFilterFactory);
        }
    }

    private static class CoreSolver extends Router.Solver {
        private final Map<String, RoutingCHGraph> chGraphs;
        private final GraphHopperStorage ghStorage;
        private final WeightingFactory weightingFactory;
        private final Map<String, PrepareCoreLandmarks> landmarks;

        CoreSolver(GHRequest request, Map<String, Profile> profilesByName, RouterConfig routerConfig, EncodedValueLookup lookup, WeightingFactory weightingFactory, GraphHopperStorage ghStorage, Map<String, RoutingCHGraph> chGraphs, Map<String, PrepareCoreLandmarks> landmarks) {
            super(request, profilesByName, routerConfig, lookup);
            this.weightingFactory = weightingFactory;
            this.ghStorage = ghStorage;
            this.chGraphs = chGraphs;
            this.landmarks = landmarks;
        }

        @Override
        protected void checkRequest() {
            super.checkRequest();
            // TODO Refactoring: check request params compatibility with core algo
        }

        protected Weighting createWeighting() {
            return weightingFactory.createWeighting(profile, request.getHints(), false);
        }

        protected PathCalculator createPathCalculator(QueryGraph queryGraph) {
            RoutingCHGraph chGraph = getRoutingCHGraph(this.profile.getName());
            RoutingAlgorithmFactory algorithmFactory = getRoutingAlgorithmFactory(chGraph, queryGraph);
            return new CorePathCalculator(queryGraph, algorithmFactory, weighting, getAlgoOpts());
        }

        AlgorithmOptions getAlgoOpts() {
            AlgorithmOptions algoOpts = new AlgorithmOptions().
                    setAlgorithm(request.getAlgorithm()).
                    setTraversalMode(profile.isTurnCosts() ? TraversalMode.EDGE_BASED : TraversalMode.NODE_BASED).
                    setMaxVisitedNodes(getMaxVisitedNodes(request.getHints())).
                    setHints(request.getHints());

            if (edgeFilterFactory != null)
                algoOpts.setEdgeFilter(edgeFilterFactory.createEdgeFilter(request.getAdditionalHints(), weighting.getFlagEncoder(), ghStorage));

            return algoOpts;
        }

        @Override
        protected EdgeFilter getSnapFilter() {
            EdgeFilter defaultSnapFilter = new DefaultSnapFilter(weighting, lookup.getBooleanEncodedValue(Subnetwork.key(profile.getName())));
            if (edgeFilterFactory != null)
                return edgeFilterFactory.createEdgeFilter(request.getAdditionalHints(), weighting.getFlagEncoder(), ghStorage, defaultSnapFilter);
            return defaultSnapFilter;
        }

        private RoutingCHGraph getRoutingCHGraph(String profileName) {
            RoutingCHGraph chGraph = this.chGraphs.get(profileName);
            if (chGraph == null) {
                throw new IllegalArgumentException("Cannot find core preparation for the requested profile: '" + profileName + "'\nYou can try disabling core using " + "core.disable" + "=true\navailable core profiles: " + this.chGraphs.keySet());
            } else {
                return chGraph;
            }
        }

        private RoutingAlgorithmFactory getRoutingAlgorithmFactory(RoutingCHGraph chGraph, QueryGraph queryGraph) {
            PMap map = request.getHints();
            LandmarkStorage lms = null;
            for (PrepareCoreLandmarks p : landmarks.values()) {
                if (p.getLMConfig().getWeighting().getName().equals(map.getString("weighting_method", "")))
                    if (p.matchesFilter(map)) {
                        lms = p.getLandmarkStorage();
                        break;
                    }
            }

            if (lms==null) {
                return new CoreRoutingAlgorithmFactory(chGraph, queryGraph);
            } else {
                return new CoreRoutingAlgorithmFactory(chGraph, queryGraph, lms);
            }
        }
    }
}
