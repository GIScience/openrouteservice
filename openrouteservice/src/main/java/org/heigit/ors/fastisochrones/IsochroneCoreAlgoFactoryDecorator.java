package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.RoutingAlgorithmFactoryDecorator;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.Helper;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters.IsoCore;
import org.heigit.ors.services.isochrones.IsochronesServiceSettings;
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
 * @author Peter Karich
 * @author Hendrik Leuschner
 */
public class IsochroneCoreAlgoFactoryDecorator implements RoutingAlgorithmFactoryDecorator {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final List<PrepareIsochroneCore> preparations = new ArrayList<>();
    // we need to decouple weighting objects from the weighting list of strings
    // as we need the strings to create the GraphHopperStorage and the GraphHopperStorage to create the preparations from the Weighting objects currently requiring the encoders
//    private final List<Weighting> weightings = new ArrayList<>();
//    private final Set<String> weightingsAsStrings = new LinkedHashSet<>();
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


    public IsochroneCoreAlgoFactoryDecorator() {
        setPreparationThreads(1);
        setCHProfilesAsStrings(Arrays.asList(getDefaultProfile()));
    }

    @Override
    public void init(CmdArgs args) {
        // throw explicit error for deprecated configs
        if (!args.get("prepare.threads", "").isEmpty())
            throw new IllegalStateException("Use " + IsoCore.PREPARE + "threads instead of prepare.threads");
        if (!args.get("prepare.chWeighting", "").isEmpty() || !args.get("prepare.chWeightings", "").isEmpty())
            throw new IllegalStateException("Use " + IsoCore.PREPARE + "weightings and a comma separated list instead of prepare.chWeighting or prepare.chWeightings");

        setPreparationThreads(args.getInt(IsoCore.PREPARE + "threads", getPreparationThreads()));

        // default is enabled & fastest
        String isoCoreWeightingStr = args.get(IsoCore.PREPARE + "weightings", "");

        if (isoCoreWeightingStr.equals("no")) {
            // default is fastest and we need to clear this explicitely
            chProfileStrings.clear();
        } else if (!isoCoreWeightingStr.isEmpty()) {
            setCHProfilesAsStrings(Arrays.asList(isoCoreWeightingStr.split(",")));

//            List<String> tmpCHWeightingList = Arrays.asList(isoCoreWeightingStr.split(","));
//            setWeightingsAsStrings(tmpCHWeightingList);
        }

        boolean enableThis = !chProfileStrings.isEmpty();
        setEnabled(enableThis);
        if (enableThis)
            setDisablingAllowed(args.getBool(IsoCore.INIT_DISABLING_ALLOWED, isDisablingAllowed()));

        setPreparationPeriodicUpdates(args.getInt(IsoCore.PREPARE + "updates.periodic", getPreparationPeriodicUpdates()));
        setPreparationLazyUpdates(args.getInt(IsoCore.PREPARE + "updates.lazy", getPreparationLazyUpdates()));
        setPreparationNeighborUpdates(args.getInt(IsoCore.PREPARE + "updates.neighbor", getPreparationNeighborUpdates()));
        setPreparationContractedNodes(args.getInt(IsoCore.PREPARE + "contracted_nodes", getPreparationContractedNodes()));
        setPreparationLogMessages(args.getDouble(IsoCore.PREPARE + "log_messages", getPreparationLogMessages()));
    }

    private int getPreparationPeriodicUpdates() {
        return preparationPeriodicUpdates;
    }

    private IsochroneCoreAlgoFactoryDecorator setPreparationPeriodicUpdates(int preparePeriodicUpdates) {
        this.preparationPeriodicUpdates = preparePeriodicUpdates;
        return this;
    }

    private int getPreparationContractedNodes() {
        return preparationContractedNodes;
    }

    private IsochroneCoreAlgoFactoryDecorator setPreparationContractedNodes(int prepareContractedNodes) {
        this.preparationContractedNodes = prepareContractedNodes;
        return this;
    }

    private int getPreparationLazyUpdates() {
        return preparationLazyUpdates;
    }

    private IsochroneCoreAlgoFactoryDecorator setPreparationLazyUpdates(int prepareLazyUpdates) {
        this.preparationLazyUpdates = prepareLazyUpdates;
        return this;
    }

    private double getPreparationLogMessages() {
        return preparationLogMessages;
    }

    private IsochroneCoreAlgoFactoryDecorator setPreparationLogMessages(double prepareLogMessages) {
        this.preparationLogMessages = prepareLogMessages;
        return this;
    }

    private int getPreparationNeighborUpdates() {
        return preparationNeighborUpdates;
    }

    private IsochroneCoreAlgoFactoryDecorator setPreparationNeighborUpdates(int prepareNeighborUpdates) {
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
    public final IsochroneCoreAlgoFactoryDecorator setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    private boolean isDisablingAllowed() {
        return disablingAllowed || !isEnabled();
    }

    /**
     * This method specifies if it is allowed to disable Core routing at runtime via routing hints.
     */
    private IsochroneCoreAlgoFactoryDecorator setDisablingAllowed(boolean disablingAllowed) {
        this.disablingAllowed = disablingAllowed;
        return this;
    }
    public IsochroneCoreAlgoFactoryDecorator addPreparation(PrepareIsochroneCore pc) {
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
//    private IsochroneCoreAlgoFactoryDecorator addPreparation(PrepareIsochroneCore pc) {
//        preparations.add(pc);
//        int lastIndex = preparations.size() - 1;
//
//        if (lastIndex >= weightings.size())
//            throw new IllegalStateException("Cannot access weighting for PrepareIsoCore with " + pc.getWeighting() + ". Call add(Weighting) before");
//        if (preparations.get(lastIndex).getWeighting() != weightings.get(lastIndex))
//            throw new IllegalArgumentException("Weighting of PrepareIsoCore " + preparations.get(lastIndex).getWeighting() + " needs to be identical to previously added " + weightings.get(lastIndex));
//
//        return this;
//    }


    /**
     * Decouple CH profiles from PrepareContractionHierarchies as we need CH profiles for the
     * graphstorage and the graphstorage for the preparation.
     */
    public IsochroneCoreAlgoFactoryDecorator addCHProfile(CHProfile chProfile) {
        chProfiles.add(chProfile);
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

    public IsochroneCoreAlgoFactoryDecorator setCHProfileStrings(String... profileStrings) {
        return setCHProfilesAsStrings(Arrays.asList(profileStrings));
    }

    /**
     * @param profileStrings A list of multiple CH profile strings
     * @see #addCHProfileAsString(String)
     */
    public IsochroneCoreAlgoFactoryDecorator setCHProfilesAsStrings(List<String> profileStrings) {
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
    public IsochroneCoreAlgoFactoryDecorator addCHProfileAsString(String profileString) {
        chProfileStrings.add(profileString);
        return this;
    }

    private String getDefaultProfile() {
        return chProfileStrings.isEmpty() ? "fastest" : chProfileStrings.iterator().next();
    }


    public List<PrepareIsochroneCore> getPreparations() {
        return preparations;
    }

    @Override
    public RoutingAlgorithmFactory getDecoratedAlgorithmFactory(RoutingAlgorithmFactory defaultAlgoFactory, HintsMap map) {
        boolean disableIsoCore = map.getBool(IsoCore.DISABLE, false);
        if (!isEnabled() || disablingAllowed && disableIsoCore)
            throw new IllegalStateException("Isochrone core preparation was called but is disabled");

        if (preparations.isEmpty())
            throw new IllegalStateException("No preparations added to this decorator");

        if (map.getWeighting().isEmpty())
            map.setWeighting(getDefaultProfile());

        String entriesStr = "";
        for (PrepareIsochroneCore p : preparations) {
            if (p.getWeighting().matches(map))
                return p;

            entriesStr += p.getWeighting() + ", ";
        }

        throw new IllegalArgumentException("Cannot find IsoCore RoutingAlgorithmFactory for weighting map " + map + " in entries " + entriesStr);
    }

    private int getPreparationThreads() {
        return preparationThreads;
    }

    /**
     * This method changes the number of threads used for preparation on import. Default is 1. Make
     * sure that you have enough memory when increasing this number!
     */
    private void setPreparationThreads(int preparationThreads) {
        this.preparationThreads = preparationThreads;
        this.threadPool = Executors.newFixedThreadPool(preparationThreads);
    }

    public void prepare(final StorableProperties properties) {
        ExecutorCompletionService completionService = new ExecutorCompletionService<>(threadPool);
        int counter = 0;
        for (final PrepareIsochroneCore prepare : getPreparations()) {
            LOGGER.info((++counter) + "/" + getPreparations().size() + " calling IsoCore prepare.doWork for " + prepare.getWeighting() + " ... (" + Helper.getMemInfo() + ")");
            final String name = "IsochroneCore  " + AbstractWeighting.weightingToFileName(prepare.getWeighting());
            completionService.submit(new Runnable() {
                @Override
                public void run() {
                    // toString is not taken into account so we need to cheat, see http://stackoverflow.com/q/6113746/194609 for other options
                    Thread.currentThread().setName(name);
                    // System.out.println(name);
                    prepare.doWork();
//                    nodeLevelMap = prepare.getNodeLevelMap();
                    properties.put(IsoCore.PREPARE + "date." + name, Helper.createFormatter().format(new Date()));
                }
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
    private PrepareIsochroneCore createCHPreparation(GraphHopperStorage ghStorage, CHProfile chProfile, EdgeFilter restrictionFilter) {
        PrepareIsochroneCore tmpPrepareCore = new PrepareIsochroneCore(
                new GHDirectory("", DAType.RAM_INT), ghStorage, ghStorage.getCHGraph(chProfile), restrictionFilter);
        tmpPrepareCore.setPeriodicUpdates(preparationPeriodicUpdates).
                setLazyUpdates(preparationLazyUpdates).
                setNeighborUpdates(preparationNeighborUpdates).
                setLogMessages(preparationLogMessages);
        return tmpPrepareCore;
    }

//    public void createPreparations(GraphHopperStorage ghStorage, EdgeFilter restrictionFilter) {
//        if (!isEnabled() || !preparations.isEmpty())
//            return;
//        if (weightings.isEmpty())
//            throw new IllegalStateException("No IsochroneCore weightings found");
//
//        TraversalMode traversalMode = getNodeBase();
//
//        for (Weighting weighting : getWeightings()) {
//            PrepareIsochroneCore tmpPrepareIsochroneCore = new PrepareIsochroneCore(
//                    new GHDirectory("", DAType.RAM_INT), ghStorage, ghStorage.getGraph(CHGraph.class, weighting), weighting, traversalMode, restrictionFilter);
//            tmpPrepareIsochroneCore.setPeriodicUpdates(preparationPeriodicUpdates).
//                    setLazyUpdates(preparationLazyUpdates).
//                    setNeighborUpdates(preparationNeighborUpdates).
////                    setDAO(dao).
//                    setLogMessages(preparationLogMessages);
//
//            addPreparation(tmpPrepareIsochroneCore);
//        }
//    }


    /**
     * For now only nodeId based will work, later on we can easily find usage of this method to remove it.
     */
    private TraversalMode getNodeBase() {
        return TraversalMode.NODE_BASED;
    }
}
