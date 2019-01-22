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

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.RoutingAlgorithmFactoryDecorator;
import com.graphhopper.routing.lm.LandmarkSuggestion;
import com.graphhopper.routing.lm.PrepareLandmarks;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.StorableProperties;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.Parameters.Landmark;
import heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;
import heigit.ors.routing.graphhopper.extensions.util.ORSParameters.CoreLandmark;
import heigit.ors.routing.graphhopper.extensions.util.ORSParameters.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class implements the A*, landmark and triangulation (ALT) decorator for Core.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner
 */
public class CoreLMAlgoFactoryDecorator implements RoutingAlgorithmFactoryDecorator {
    private Logger LOGGER = LoggerFactory.getLogger(CoreLMAlgoFactoryDecorator.class);
    private int landmarkCount = 16;
    private int activeLandmarkCount = 4;

    private final List<PrepareCoreLandmarks> preparations = new ArrayList<>();
    // input weighting list from configuration file
    // one such entry can result into multiple Weighting objects e.g. fastest & car,foot => fastest|car and fastest|foot
    private final List<String> weightingsAsStrings = new ArrayList<>();
    private final List<Weighting> weightings = new ArrayList<>();
    private final Map<String, Double> maximumWeights = new HashMap<>();
    private boolean enabled = true;
    private int minNodes = -1;
    private boolean disablingAllowed = true;
    private final List<String> lmSuggestionsLocations = new ArrayList<>(5);
    private int preparationThreads;
    private ExecutorService threadPool;
    private boolean logDetails = false;
    private CoreLMOptions coreLMOptions;
    private GraphHopperStorage ghStorage;

    public CoreLMAlgoFactoryDecorator() {
        setPreparationThreads(1);
    }
    public CoreLMAlgoFactoryDecorator(GraphHopperStorage graphHopperStorage) {
        setPreparationThreads(1);
        this.ghStorage = graphHopperStorage;
    }

    @Override
    public void init(CmdArgs args) {
        setPreparationThreads(args.getInt(CoreLandmark.PREPARE + "threads", getPreparationThreads()));

        landmarkCount = args.getInt(CoreLandmark.COUNT, landmarkCount);
        activeLandmarkCount = args.getInt(CoreLandmark.ACTIVE_COUNT, Math.min(4, landmarkCount));
        logDetails = args.getBool(CoreLandmark.PREPARE + "log_details", false);
        minNodes = args.getInt(CoreLandmark.PREPARE + "min_network_size", -1);

        for (String loc : args.get(CoreLandmark.PREPARE + "suggestions_location", "").split(",")) {
            if (!loc.trim().isEmpty())
                lmSuggestionsLocations.add(loc.trim());
        }
        String lmWeightingsStr = args.get(Core.PREPARE + "weightings", "");
        if (!lmWeightingsStr.isEmpty() && !lmWeightingsStr.equalsIgnoreCase("no")) {
            List<String> tmpLMWeightingList = Arrays.asList(lmWeightingsStr.split(","));
            setWeightingsAsStrings(tmpLMWeightingList);
        }

        boolean enableThis = !weightingsAsStrings.isEmpty();
        setEnabled(enableThis);
        if (enableThis)
            setDisablingAllowed(args.getBool(CoreLandmark.INIT_DISABLING_ALLOWED, isDisablingAllowed()));

        //Get the landmark sets that should be calculated
        this.coreLMOptions = new CoreLMOptions(ghStorage);
        String coreLMSets = args.get(CoreLandmark.LMSETS, "allow_all");
        if (!coreLMSets.isEmpty() && !coreLMSets.equalsIgnoreCase("no")) {
            List<String> tmpCoreLMSets = Arrays.asList(coreLMSets.split(";"));
            coreLMOptions.setRestrictionFilters(tmpCoreLMSets);
        }

    }

    public int getLandmarks() {
        return landmarkCount;
    }

    public CoreLMAlgoFactoryDecorator setDisablingAllowed(boolean disablingAllowed) {
        this.disablingAllowed = disablingAllowed;
        return this;
    }

    public final boolean isDisablingAllowed() {
        return disablingAllowed || !isEnabled();
    }

    /**
     * Enables or disables this decorator. This speed-up mode is disabled by default.
     */
    public final CoreLMAlgoFactoryDecorator setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    public int getPreparationThreads() {
        return preparationThreads;
    }

    /**
     * This method changes the number of threads used for preparation on import. Default is 1. Make
     * sure that you have enough memory when increasing this number!
     */
    public void setPreparationThreads(int preparationThreads) {
        this.preparationThreads = preparationThreads;
        this.threadPool = java.util.concurrent.Executors.newFixedThreadPool(preparationThreads);
    }

    /**
     *
     *
     * @param weightingList A list containing multiple weightings like: "fastest", "shortest" or
     *                      your own weight-calculation type.
     */
    public CoreLMAlgoFactoryDecorator setWeightingsAsStrings(List<String> weightingList) {
        if (weightingList.isEmpty())
            throw new IllegalArgumentException("It is not allowed to pass an emtpy weightingList");

        weightingsAsStrings.clear();
        for (String strWeighting : weightingList) {
            strWeighting = strWeighting.toLowerCase();
            strWeighting = strWeighting.trim();
            addWeighting(strWeighting);
        }
        return this;
    }

    public List<String> getWeightingsAsStrings() {
        if (this.weightingsAsStrings.isEmpty())
            throw new IllegalStateException("Potential bug: weightingsAsStrings is empty");

        return this.weightingsAsStrings;
    }

    public CoreLMAlgoFactoryDecorator addWeighting(String weighting) {
        String str[] = weighting.split("\\|");
        double value = -1;
        if (str.length > 1) {
            PMap map = new PMap(weighting);
            value = map.getDouble("maximum", -1);
        }

        weightingsAsStrings.add(str[0]);
        maximumWeights.put(str[0], value);
        return this;
    }

    /**
     * Decouple weightings from PrepareCoreLandmarks as we need weightings for the graphstorage and the
     * graphstorage for the preparation.
     */
    public CoreLMAlgoFactoryDecorator addWeighting(Weighting weighting) {
        weightings.add(weighting);
        return this;
    }

    public CoreLMAlgoFactoryDecorator addPreparation(PrepareCoreLandmarks pch) {
        preparations.add(pch);
        int lastIndex = preparations.size() - 1;
        if (lastIndex >= weightings.size() * coreLMOptions.getFilters().size())
            throw new IllegalStateException("Cannot access weighting for PrepareCoreLandmarks with " + pch.getWeighting()
                    + ". Call add(Weighting) before");

        if (preparations.get(lastIndex).getWeighting() != weightings.get(lastIndex / coreLMOptions.getFilters().size()))
            throw new IllegalArgumentException(
                    "Weighting of PrepareCoreLandmarks " + preparations.get(lastIndex).getWeighting()
                            + " needs to be identical to previously added " + weightings.get(lastIndex));
        return this;
    }

    public boolean hasWeightings() {
        return !weightings.isEmpty();
    }

    public boolean hasPreparations() {
        return !preparations.isEmpty();
    }

    public int size() {
        return preparations.size();
    }

    public List<Weighting> getWeightings() {
        return weightings;
    }

    public List<PrepareCoreLandmarks> getPreparations() {
        return preparations;
    }

    @Override
    public RoutingAlgorithmFactory getDecoratedAlgorithmFactory(RoutingAlgorithmFactory defaultAlgoFactory,
            HintsMap map) {
        // for now do not allow mixing CH&LM #1082
        boolean disableCH = map.getBool(Parameters.CH.DISABLE, false);
        boolean disableLM = map.getBool(Core.DISABLE, false);
        if (!isEnabled() || disablingAllowed && disableLM || !disableCH)
            return defaultAlgoFactory;

        if (preparations.isEmpty())
            throw new IllegalStateException("No preparations added to this decorator");

        //First try to find a preparation with a landmarkset that fits the query
        for (final PrepareCoreLandmarks p : preparations) {
            if (p.getWeighting().matches(map) && p.matchesFilter(map))
                return new CoreLMRAFactory(p, defaultAlgoFactory);
        }
        //If none matches, we return the original one and will be using slow beeline approx
        return defaultAlgoFactory;
    }

    /**
     * TODO needs to be public to pick defaultAlgoFactory.weighting if the defaultAlgoFactory is a CH one.
     *
     * @see com.graphhopper.GraphHopper#calcPaths(GHRequest, GHResponse)
     */
    public static class CoreLMRAFactory implements RoutingAlgorithmFactory {
        private RoutingAlgorithmFactory defaultAlgoFactory;
        private PrepareCoreLandmarks p;

        public CoreLMRAFactory(PrepareCoreLandmarks p, RoutingAlgorithmFactory defaultAlgoFactory) {
            this.defaultAlgoFactory = defaultAlgoFactory;
            this.p = p;
        }

        public RoutingAlgorithmFactory getDefaultAlgoFactory() {
            return defaultAlgoFactory;
        }

        @Override
        public RoutingAlgorithm createAlgo(Graph g, AlgorithmOptions opts) {
            RoutingAlgorithm algo = defaultAlgoFactory.createAlgo(g, opts);
            return p.getDecoratedAlgorithm(g, algo, opts);
        }
    }

    /**
     * This method calculates the landmark data for all weightings (optionally in parallel) or if already existent loads it.
     *
     * @return true if the preparation data for at least one weighting was calculated.
     * @see com.graphhopper.routing.ch.CHAlgoFactoryDecorator#prepare(StorableProperties) for a very similar method
     */
    public boolean loadOrDoWork(final StorableProperties properties) {
        ExecutorCompletionService completionService = new ExecutorCompletionService<>(threadPool);
        int counter = 0;
        final AtomicBoolean prepared = new AtomicBoolean(false);
        for (final PrepareCoreLandmarks plm : preparations) {
            counter++;
            final int tmpCounter = counter;
            final String name = AbstractWeighting.weightingToFileName(plm.getWeighting());
            completionService.submit(new Runnable() {
                @Override
                public void run() {
                    if (plm.loadExisting())
                        return;

                    LOGGER.info(tmpCounter + "/" + getPreparations().size() + " calling CoreLM prepare.doWork for "
                            + plm.getWeighting() + " ... (" + Helper.getMemInfo() + ")");
                    prepared.set(true);
                    Thread.currentThread().setName(name);
                    plm.doWork();
                    properties.put(CoreLandmark.PREPARE + "date." + name, Helper.createFormatter().format(new Date()));
                }
            }, name);
        }

        threadPool.shutdown();

        try {
            for (int i = 0; i < preparations.size(); i++) {
                completionService.take().get();
            }
        } catch (Exception e) {
            threadPool.shutdownNow();
            throw new RuntimeException(e);
        }
        return prepared.get();
    }

    /**
     * This method creates the landmark storages ready for landmark creation.
     */
    public void createPreparations(GraphHopperStorage ghStorage, LocationIndex locationIndex) {
        if (!isEnabled() || !preparations.isEmpty())
            return;
        if (weightings.isEmpty())
            throw new IllegalStateException("No landmark weightings found");

        List<LandmarkSuggestion> lmSuggestions = new ArrayList<>(lmSuggestionsLocations.size());
        if (!lmSuggestionsLocations.isEmpty()) {
            try {
                for (String loc : lmSuggestionsLocations) {
                    lmSuggestions.add(LandmarkSuggestion.readLandmarks(loc, locationIndex));
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        coreLMOptions.setGhStorage(ghStorage);
        coreLMOptions.createRestrictionFilters();

        for (Weighting weighting : getWeightings()) {
            for (LMEdgeFilterSequence edgeFilterSequence : coreLMOptions.getFilters()) {
                Double maximumWeight = maximumWeights.get(weighting.getName());
                if (maximumWeight == null)
                    throw new IllegalStateException("maximumWeight cannot be null. Default should be just negative. "
                            + "Couldn't find " + weighting.getName() + " in " + maximumWeights);

                PrepareCoreLandmarks tmpPrepareLM = new PrepareCoreLandmarks(ghStorage.getDirectory(), ghStorage, weighting, edgeFilterSequence,
                        landmarkCount, activeLandmarkCount).setLandmarkSuggestions(lmSuggestions)
                        .setMaximumWeight(maximumWeight).setLogDetails(logDetails);
                if (minNodes > 1)
                    tmpPrepareLM.setMinimumNodes(minNodes);
                addPreparation(tmpPrepareLM);
            }
        }
    }
}
