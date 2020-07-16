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

import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.RoutingAlgorithmFactoryDecorator;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.Helper;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.graphhopper.util.Helper.toLowerCase;

/**
 * This class implements the Core Algo decorator and provides several helper methods related to core
 * preparation and its vehicle profiles.
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner
 */
public class CoreAlgoFactoryDecorator implements RoutingAlgorithmFactoryDecorator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreAlgoFactoryDecorator.class);
    private final List<PrepareCore> preparations = new ArrayList<>();
    // we need to decouple weighting objects from the weighting list of strings
    // as we need the strings to create the GraphHopperStorage and the GraphHopperStorage to create the preparations from the Weighting objects currently requiring the encoders
    private final List<CHProfile> chProfiles = new ArrayList<>();
    private final Set<String> chProfileStrings = new LinkedHashSet<>();
    private boolean disablingAllowed = true;
    // for backward compatibility enable CH by default.
    private boolean enabled = true;
    private int preparationThreads;
    private ExecutorService threadPool;
    private int preparationPeriodicUpdates = -1;
    private int preparationLazyUpdates = -1;
    private int preparationNeighborUpdates = -1;
    private int preparationContractedNodes = -1;
    private double preparationLogMessages = -1;

    public CoreAlgoFactoryDecorator() {
        setPreparationThreads(1);
        setCHProfilesAsStrings(Arrays.asList(getDefaultProfile()));
    }

    @Override
    public void init(CmdArgs args) {
        // throw explicit error for deprecated configs
        //TODO need to make the core parameters
        if (!args.get("prepare.threads", "").isEmpty())
            throw new IllegalStateException("Use " + Core.PREPARE + "threads instead of prepare.threads");
        if (!args.get("prepare.chWeighting", "").isEmpty() || !args.get("prepare.chWeightings", "").isEmpty())
            throw new IllegalStateException("Use " + Core.PREPARE + "weightings and a comma separated list instead of prepare.chWeighting or prepare.chWeightings");

        setPreparationThreads(args.getInt(Core.PREPARE + "threads", getPreparationThreads()));

        // default is enabled & recommended
        String coreWeightingsStr = args.get(Core.PREPARE + "weightings", "");

        if ("no".equals(coreWeightingsStr)) {
            // default is recommended and we need to clear this explicitely
            chProfileStrings.clear();
        } else if (!coreWeightingsStr.isEmpty()) {
            setCHProfilesAsStrings(Arrays.asList(coreWeightingsStr.split(",")));
        }

        boolean enableThis = !chProfileStrings.isEmpty();
        setEnabled(enableThis);
        if (enableThis)
            setDisablingAllowed(args.getBool(Core.INIT_DISABLING_ALLOWED, isDisablingAllowed()));

        setPreparationPeriodicUpdates(args.getInt(Core.PREPARE + "updates.periodic", getPreparationPeriodicUpdates()));
        setPreparationLazyUpdates(args.getInt(Core.PREPARE + "updates.lazy", getPreparationLazyUpdates()));
        setPreparationNeighborUpdates(args.getInt(Core.PREPARE + "updates.neighbor", getPreparationNeighborUpdates()));
        setPreparationContractedNodes(args.getInt(Core.PREPARE + "contracted_nodes", getPreparationContractedNodes()));
        setPreparationLogMessages(args.getDouble(Core.PREPARE + "log_messages", getPreparationLogMessages()));
    }

    public int getPreparationPeriodicUpdates() {
        return preparationPeriodicUpdates;
    }

    public CoreAlgoFactoryDecorator setPreparationPeriodicUpdates(int preparePeriodicUpdates) {
        this.preparationPeriodicUpdates = preparePeriodicUpdates;
        return this;
    }

    public int getPreparationContractedNodes() {
        return preparationContractedNodes;
    }

    public CoreAlgoFactoryDecorator setPreparationContractedNodes(int prepareContractedNodes) {
        this.preparationContractedNodes = prepareContractedNodes;
        return this;
    }

    public int getPreparationLazyUpdates() {
        return preparationLazyUpdates;
    }

    public CoreAlgoFactoryDecorator setPreparationLazyUpdates(int prepareLazyUpdates) {
        this.preparationLazyUpdates = prepareLazyUpdates;
        return this;
    }

    public double getPreparationLogMessages() {
        return preparationLogMessages;
    }

    public CoreAlgoFactoryDecorator setPreparationLogMessages(double prepareLogMessages) {
        this.preparationLogMessages = prepareLogMessages;
        return this;
    }

    public int getPreparationNeighborUpdates() {
        return preparationNeighborUpdates;
    }

    public CoreAlgoFactoryDecorator setPreparationNeighborUpdates(int prepareNeighborUpdates) {
        this.preparationNeighborUpdates = prepareNeighborUpdates;
        return this;
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables core calculation..
     */
    public final CoreAlgoFactoryDecorator setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public final boolean isDisablingAllowed() {
        return disablingAllowed || !isEnabled();
    }

    /**
     * This method specifies if it is allowed to disable Core routing at runtime via routing hints.
     */
    public final CoreAlgoFactoryDecorator setDisablingAllowed(boolean disablingAllowed) {
        this.disablingAllowed = disablingAllowed;
        return this;
    }

    /**
     * Decouple CH profiles from PrepareContractionHierarchies as we need CH profiles for the
     * graphstorage and the graphstorage for the preparation.
     */
    public CoreAlgoFactoryDecorator addCHProfile(CHProfile chProfile) {
        chProfiles.add(chProfile);
        return this;
    }

    public CoreAlgoFactoryDecorator addPreparation(PrepareCore pc) {
        if (preparations.size() >= chProfiles.size()) {
            throw new IllegalStateException("You need to add the corresponding CH profiles before adding preparations.");
        }
        CHProfile expectedProfile = chProfiles.get(preparations.size());
        if (!pc.getCHProfile().equals(expectedProfile)) {
            throw new IllegalArgumentException("CH profile of preparation: " + pc + " needs to be identical to previously added CH profile: " + expectedProfile);
        }
        preparations.add(pc);
        return this;
    }

    public final boolean hasCHProfiles() {
        return !chProfiles.isEmpty();
    }

    public List<CHProfile> getCHProfiles() {
        return chProfiles;
    }

    public List<String> getCHProfileStrings() {
        if (chProfileStrings.isEmpty())
            throw new IllegalStateException("Potential bug: chProfileStrings is empty");

        return new ArrayList<>(chProfileStrings);
    }

    public CoreAlgoFactoryDecorator setCHProfileStrings(String... profileStrings) {
        return setCHProfilesAsStrings(Arrays.asList(profileStrings));
    }

    /**
     * @param profileStrings A list of multiple CH profile strings
     * @see #addCHProfileAsString(String)
     */
    public CoreAlgoFactoryDecorator setCHProfilesAsStrings(List<String> profileStrings) {
        if (profileStrings.isEmpty())
            throw new IllegalArgumentException("It is not allowed to pass an empty list of CH profile strings");

        chProfileStrings.clear();
        for (String profileString : profileStrings) {
            profileString = toLowerCase(profileString);
            profileString = profileString.trim();
            addCHProfileAsString(profileString);
        }
        return this;
    }

    /**
     * Enables the use of contraction hierarchies to reduce query times. Enabled by default.
     *
     * @param profileString String representation of a CH profile like: "fastest", "shortest|edge_based=true",
     *                      "fastest|u_turn_costs=30 or your own weight-calculation type.
     */
    public CoreAlgoFactoryDecorator addCHProfileAsString(String profileString) {
        chProfileStrings.add(profileString);
        return this;
    }

    private String getDefaultProfile() {
        return chProfileStrings.isEmpty() ? "recommended" : chProfileStrings.iterator().next();
    }

    public List<PrepareCore> getPreparations() {
        return preparations;
    }

    @Override
    public RoutingAlgorithmFactory getDecoratedAlgorithmFactory(RoutingAlgorithmFactory defaultAlgoFactory, HintsMap map) {
        boolean disableCore = map.getBool(Core.DISABLE, false);
        if (!isEnabled() || disablingAllowed && disableCore)
            return defaultAlgoFactory;

        if (preparations.isEmpty())
            throw new IllegalStateException("No preparations added to this decorator");

        if (map.getWeighting().isEmpty())
            map.setWeighting(getDefaultProfile());

        StringBuilder entriesStr = new StringBuilder();
        for (PrepareCore p : preparations) {
            if (p.getWeighting().matches(map))
                return p;
            if (entriesStr.length() > 0)
                entriesStr.append(", ");
            entriesStr.append(p.getWeighting());
        }
        throw new IllegalArgumentException("Cannot find Core RoutingAlgorithmFactory for weighting map " + map + " in entries " + entriesStr);
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
        this.threadPool = Executors.newFixedThreadPool(preparationThreads);
    }

    public void prepare(final StorableProperties properties) {
        ExecutorCompletionService completionService = new ExecutorCompletionService<>(threadPool);
        int counter = 0;
        for (final PrepareCore prepare : getPreparations()) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info(String.format("%d/%d calling Core prepare.doWork for %s ... (%s)", ++counter, getPreparations().size(), prepare.getWeighting(), Helper.getMemInfo()));
            final String name = AbstractWeighting.weightingToFileName(prepare.getWeighting());
            completionService.submit(() -> {
                // toString is not taken into account so we need to cheat, see http://stackoverflow.com/q/6113746/194609 for other options
                Thread.currentThread().setName(name);
                prepare.doWork();
                properties.put(Core.PREPARE + "date." + name, Helper.createFormatter().format(new Date()));
            }, name);

        }

        threadPool.shutdown();

        try {
            for (int i = 0; i < getPreparations().size(); i++) {
                completionService.take().get();
            }
        } catch (Exception e) {
            threadPool.shutdownNow();
            throw new RuntimeException(e);
        }
    }

    public void createPreparations(GraphHopperStorage ghStorage, EdgeFilter restrictionFilter) {
        if (!isEnabled() || !preparations.isEmpty())
            return;
        if (!hasCHProfiles())
            throw new IllegalStateException("No profiles found");

        for (CHProfile chProfile : chProfiles) {
            addPreparation(createCHPreparation(ghStorage, chProfile, restrictionFilter));
        }
    }
    private PrepareCore createCHPreparation(GraphHopperStorage ghStorage, CHProfile chProfile, EdgeFilter restrictionFilter) {
        PrepareCore tmpPrepareCore = new PrepareCore(
                new GHDirectory("", DAType.RAM_INT), ghStorage, ghStorage.getCHGraph(chProfile), restrictionFilter);
        tmpPrepareCore.setPeriodicUpdates(preparationPeriodicUpdates).
                setLazyUpdates(preparationLazyUpdates).
                setNeighborUpdates(preparationNeighborUpdates).
                setLogMessages(preparationLogMessages);
        return tmpPrepareCore;
    }
}
