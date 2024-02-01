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
package org.heigit.ors.routing;

import com.google.common.base.Strings;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.gtfs.*;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import com.typesafe.config.Config;
import org.apache.log4j.Logger;
import org.heigit.ors.common.Pair;
import org.heigit.ors.config.EngineConfig;
import org.heigit.ors.exceptions.IncompatibleParameterException;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.exceptions.MaxVisitedNodesExceededException;
import org.heigit.ors.exceptions.PointNotFoundException;
import org.heigit.ors.export.ExportRequest;
import org.heigit.ors.export.ExportResult;
import org.heigit.ors.export.ExportWarning;
import org.heigit.ors.isochrones.*;
import org.heigit.ors.isochrones.statistics.StatisticsProvider;
import org.heigit.ors.isochrones.statistics.StatisticsProviderConfiguration;
import org.heigit.ors.isochrones.statistics.StatisticsProviderFactory;
import org.heigit.ors.matrix.*;
import org.heigit.ors.matrix.algorithms.core.CoreMatrixAlgorithm;
import org.heigit.ors.matrix.algorithms.dijkstra.DijkstraMatrixAlgorithm;
import org.heigit.ors.matrix.algorithms.rphast.RPHASTMatrixAlgorithm;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.heigit.ors.routing.graphhopper.extensions.*;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.OsmIdGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.BordersGraphStorageBuilder;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters;
import org.heigit.ors.routing.parameters.ProfileParameters;
import org.heigit.ors.routing.pathprocessors.ORSPathProcessorFactory;
import org.heigit.ors.util.DebugUtility;
import org.heigit.ors.util.ProfileTools;
import org.heigit.ors.util.StringUtility;
import org.heigit.ors.util.TimeUtility;
import org.locationtech.jts.geom.Coordinate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

/**
 * This class generates {@link RoutingProfile} classes and is used by mostly all service classes e.g.
 * <p>
 * {@link RoutingProfileManager} etc.
 *
 * @author Openrouteserviceteam
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class RoutingProfile {
    private static final Logger LOGGER = Logger.getLogger(RoutingProfile.class);
    private static final Object lockObj = new Object();
    private static int profileIdentifier = 0;
    private final Integer[] mRoutePrefs;
    private final RouteProfileConfiguration config;
    private final ORSGraphHopper mGraphHopper;
    private Integer mUseCounter;
    private String astarApproximation;
    private Double astarEpsilon;

    public RoutingProfile(EngineConfig engineConfig, RouteProfileConfiguration rpc, RoutingProfileLoadContext loadCntx) throws Exception {
        mRoutePrefs = rpc.getProfilesTypes();
        mUseCounter = 0;

        mGraphHopper = initGraphHopper(engineConfig, rpc, loadCntx);

        config = rpc;

        Config optsExecute = config.getExecutionOpts();
        if (optsExecute != null) {
            if (optsExecute.hasPath("methods.astar.approximation"))
                astarApproximation = optsExecute.getString("methods.astar.approximation");
            if (optsExecute.hasPath("methods.astar.epsilon"))
                astarEpsilon = Double.parseDouble(optsExecute.getString("methods.astar.epsilon"));
        }
    }

    public static ORSGraphHopper initGraphHopper(EngineConfig engineConfig, RouteProfileConfiguration config, RoutingProfileLoadContext loadCntx) throws Exception {
        String osmFile = engineConfig.getSourceFile();
        String dataAccessType = Strings.isNullOrEmpty(config.getGraphDataAccess()) ? engineConfig.getGraphsDataAccess() : config.getGraphDataAccess();
        ORSGraphHopperConfig args = createGHSettings(osmFile, dataAccessType, config);

        int profileId;
        synchronized (lockObj) {
            profileIdentifier++;
            profileId = profileIdentifier;
        }

        long startTime = System.currentTimeMillis();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("[%d] Profiles: '%s', location: '%s'.".formatted(profileId, config.getProfiles(), config.getGraphPath()));
        }

        GraphProcessContext gpc = new GraphProcessContext(config);
        gpc.setGetElevationFromPreprocessedData(engineConfig.isElevationPreprocessed());

        ORSGraphHopper gh = new ORSGraphHopper(gpc);

        ORSDefaultFlagEncoderFactory flagEncoderFactory = new ORSDefaultFlagEncoderFactory();
        gh.setFlagEncoderFactory(flagEncoderFactory);

        ORSPathProcessorFactory pathProcessorFactory = new ORSPathProcessorFactory();
        gh.setPathProcessorFactory(pathProcessorFactory);

        gh.init(args);

        // MARQ24: make sure that we only use ONE instance of the ElevationProvider across the multiple vehicle profiles
        // so the caching for elevation data will/can be reused across different vehicles. [the loadCntx is a single
        // Object that will shared across the (potential) multiple running instances]
        if (loadCntx.getElevationProvider() != null) {
            if (args.has("graph.elevation.provider")) {
                gh.setElevationProvider(loadCntx.getElevationProvider());
            }
        } else {
            loadCntx.setElevationProvider(gh.getElevationProvider());
        }
        gh.setGraphStorageFactory(new ORSGraphStorageFactory(gpc.getStorageBuilders()));

        gh.importOrLoad();
        // store CountryBordersReader for later use
        for (GraphStorageBuilder builder : gpc.getStorageBuilders()) {
            if (builder.getName().equals(BordersGraphStorageBuilder.BUILDER_NAME)) {
                pathProcessorFactory.setCountryBordersReader(((BordersGraphStorageBuilder) builder).getCbReader());
            }
        }

        if (LOGGER.isInfoEnabled()) {
            GraphHopperStorage ghStorage = gh.getGraphHopperStorage();
            LOGGER.info("[%d] Edges: %s - Nodes: %s.".formatted(profileId, ghStorage.getEdges(), ghStorage.getNodes()));
            LOGGER.info("[%d] Total time: %s.".formatted(profileId, TimeUtility.getElapsedTime(startTime, true)));
            LOGGER.info("[%d] Finished at: %s.".formatted(profileId, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
        }

        // Make a stamp which help tracking any changes in the size of OSM file.
        File file = new File(osmFile);
        Path pathTimestamp = Paths.get(config.getGraphPath(), "stamp.txt");
        File file2 = pathTimestamp.toFile();
        if (!file2.exists())
            Files.write(pathTimestamp, Long.toString(file.length()).getBytes());

        return gh;
    }

    private static ORSGraphHopperConfig createGHSettings(String sourceFile, String dataAccessType, RouteProfileConfiguration config) {
        ORSGraphHopperConfig ghConfig = new ORSGraphHopperConfig();
        ghConfig.putObject("graph.dataaccess", dataAccessType);
        ghConfig.putObject("datareader.file", sourceFile);
        ghConfig.putObject("graph.location", config.getGraphPath());
        ghConfig.putObject("graph.bytes_for_flags", config.getEncoderFlagsSize());

        if (!config.getInstructions())
            ghConfig.putObject("instructions", false);
        if (config.getElevationProvider() != null && config.getElevationCachePath() != null) {
            ghConfig.putObject("graph.elevation.provider", StringUtility.trimQuotes(config.getElevationProvider()));
            ghConfig.putObject("graph.elevation.cache_dir", StringUtility.trimQuotes(config.getElevationCachePath()));
            ghConfig.putObject("graph.elevation.dataaccess", StringUtility.trimQuotes(config.getElevationDataAccess()));
            ghConfig.putObject("graph.elevation.clear", config.getElevationCacheClear());
            if (config.getInterpolateBridgesAndTunnels())
                ghConfig.putObject("graph.encoded_values", "road_environment");
            if (config.getElevationSmoothing())
                ghConfig.putObject("graph.elevation.smoothing", true);
        }

        boolean prepareCH = false;
        boolean prepareLM = false;
        boolean prepareCore = false;
        boolean prepareFI = false;

        Integer[] profilesTypes = config.getProfilesTypes();
        Map<String, Profile> profiles = new LinkedHashMap<>();

        // TODO Future improvement : Multiple profiles were used to share the graph  for several
        //       bike profiles. We don't use this feature now but it might be
        //       desireable in the future. However, this behavior is standard
        //       in original GH through an already existing mechanism.
        if (profilesTypes.length != 1)
            throw new IllegalStateException("Expected single profile in config");

        String vehicle = RoutingProfileType.getEncoderName(profilesTypes[0]);

        boolean hasTurnCosts = config.isTurnCostEnabled();

        // TODO Future improvement : make this list of weightings configurable for each vehicle as in GH
        String[] weightings = {ProfileTools.VAL_FASTEST, ProfileTools.VAL_SHORTEST, ProfileTools.VAL_RECOMMENDED};
        for (String weighting : weightings) {
            if (hasTurnCosts) {
                String profileName = ProfileTools.makeProfileName(vehicle, weighting, true);
                profiles.put(profileName, new Profile(profileName).setVehicle(vehicle).setWeighting(weighting).setTurnCosts(true));
            }
            String profileName = ProfileTools.makeProfileName(vehicle, weighting, false);
            profiles.put(profileName, new Profile(profileName).setVehicle(vehicle).setWeighting(weighting).setTurnCosts(false));
        }

        ghConfig.putObject(ProfileTools.KEY_PREPARE_CORE_WEIGHTINGS, "no");

        if (config.getIsochronePreparationOpts() != null) {
            Config fastisochroneOpts = config.getIsochronePreparationOpts();
            prepareFI = true;
            if (fastisochroneOpts.hasPath(ProfileTools.KEY_ENABLED) || fastisochroneOpts.getBoolean(ProfileTools.KEY_ENABLED)) {
                prepareFI = fastisochroneOpts.getBoolean(ProfileTools.KEY_ENABLED);
                if (!prepareFI)
                    ghConfig.putObject(ProfileTools.KEY_PREPARE_FASTISOCHRONE_WEIGHTINGS, "no");
                else
                    ghConfig.putObject(ORSParameters.FastIsochrone.PROFILE, config.getProfiles());
            }

            if (prepareFI) {
                //Copied from core
                if (fastisochroneOpts.hasPath(ProfileTools.KEY_THREADS))
                    ghConfig.putObject("prepare.fastisochrone.threads", fastisochroneOpts.getInt(ProfileTools.KEY_THREADS));
                if (fastisochroneOpts.hasPath(ProfileTools.KEY_MAXCELLNODES))
                    ghConfig.putObject("prepare.fastisochrone.maxcellnodes", StringUtility.trimQuotes(fastisochroneOpts.getString(ProfileTools.KEY_MAXCELLNODES)));
                if (fastisochroneOpts.hasPath(ProfileTools.KEY_WEIGHTINGS)) {
                    List<Profile> fastisochronesProfiles = new ArrayList<>();
                    String fastisochronesWeightingsString = StringUtility.trimQuotes(fastisochroneOpts.getString(ProfileTools.KEY_WEIGHTINGS));
                    for (String weighting : fastisochronesWeightingsString.split(",")) {
                        String configStr = "";
                        weighting = weighting.trim();
                        if (weighting.contains("|")) {
                            configStr = weighting;
                            weighting = weighting.split("\\|")[0];
                        }
                        PMap configMap = new PMap(configStr);
                        boolean considerTurnRestrictions = configMap.getBool("edge_based", hasTurnCosts);

                        String profileName = ProfileTools.makeProfileName(vehicle, weighting, considerTurnRestrictions);
                        Profile profile = new Profile(profileName).setVehicle(vehicle).setWeighting(weighting).setTurnCosts(considerTurnRestrictions);
                        profiles.put(profileName, profile);
                        fastisochronesProfiles.add(profile);
                    }
                    ghConfig.setFastisochroneProfiles(fastisochronesProfiles);
                }
            }
        }

        if (config.getPreparationOpts() != null) {
            Config opts = config.getPreparationOpts();
            if (opts.hasPath("min_network_size"))
                ghConfig.putObject("prepare.min_network_size", opts.getInt("min_network_size"));

            if (opts.hasPath("methods")) {
                if (opts.hasPath(ProfileTools.KEY_METHODS_CH)) {
                    prepareCH = true;
                    Config chOpts = opts.getConfig(ProfileTools.KEY_METHODS_CH);

                    if (chOpts.hasPath(ProfileTools.KEY_ENABLED) || chOpts.getBoolean(ProfileTools.KEY_ENABLED)) {
                        prepareCH = chOpts.getBoolean(ProfileTools.KEY_ENABLED);
                    }

                    if (prepareCH) {
                        if (chOpts.hasPath(ProfileTools.KEY_THREADS))
                            ghConfig.putObject("prepare.ch.threads", chOpts.getInt(ProfileTools.KEY_THREADS));
                        if (chOpts.hasPath(ProfileTools.KEY_WEIGHTINGS)) {
                            List<CHProfile> chProfiles = new ArrayList<>();
                            String chWeightingsString = StringUtility.trimQuotes(chOpts.getString(ProfileTools.KEY_WEIGHTINGS));
                            for (String weighting : chWeightingsString.split(","))
                                chProfiles.add(new CHProfile(ProfileTools.makeProfileName(vehicle, weighting, false)));
                            ghConfig.setCHProfiles(chProfiles);
                        }
                    }
                }

                if (opts.hasPath(ProfileTools.KEY_METHODS_LM)) {
                    prepareLM = true;
                    Config lmOpts = opts.getConfig(ProfileTools.KEY_METHODS_LM);

                    if (lmOpts.hasPath(ProfileTools.KEY_ENABLED) || lmOpts.getBoolean(ProfileTools.KEY_ENABLED)) {
                        prepareLM = lmOpts.getBoolean(ProfileTools.KEY_ENABLED);
                    }

                    if (prepareLM) {
                        if (lmOpts.hasPath(ProfileTools.KEY_THREADS))
                            ghConfig.putObject("prepare.lm.threads", lmOpts.getInt(ProfileTools.KEY_THREADS));
                        if (lmOpts.hasPath(ProfileTools.KEY_WEIGHTINGS)) {
                            List<LMProfile> lmProfiles = new ArrayList<>();
                            String lmWeightingsString = StringUtility.trimQuotes(lmOpts.getString(ProfileTools.KEY_WEIGHTINGS));
                            for (String weighting : lmWeightingsString.split(","))
                                lmProfiles.add(new LMProfile(ProfileTools.makeProfileName(vehicle, weighting, hasTurnCosts)));
                            ghConfig.setLMProfiles(lmProfiles);
                        }
                        if (lmOpts.hasPath(ProfileTools.KEY_LANDMARKS))
                            ghConfig.putObject("prepare.lm.landmarks", lmOpts.getInt(ProfileTools.KEY_LANDMARKS));
                    }
                }

                if (opts.hasPath(ProfileTools.KEY_METHODS_CORE)) {
                    prepareCore = true;
                    Config coreOpts = opts.getConfig(ProfileTools.KEY_METHODS_CORE);

                    if (coreOpts.hasPath(ProfileTools.KEY_ENABLED) || coreOpts.getBoolean(ProfileTools.KEY_ENABLED)) {
                        prepareCore = coreOpts.getBoolean(ProfileTools.KEY_ENABLED);
                        if (!prepareCore)
                            ghConfig.putObject(ProfileTools.KEY_PREPARE_CORE_WEIGHTINGS, "no");
                    }

                    if (prepareCore) {
                        if (coreOpts.hasPath(ProfileTools.KEY_THREADS)) {
                            String[] threads = coreOpts.getString(ProfileTools.KEY_THREADS).split(",");
                            int threadsCH = Integer.parseInt(threads[0]);
                            int threadsLM = threads.length > 1 ? Integer.parseInt(threads[1]) : threadsCH;
                            ghConfig.putObject("prepare.core.threads", threadsCH);
                            ghConfig.putObject("prepare.corelm.threads", threadsLM);
                        }
                        if (coreOpts.hasPath(ProfileTools.KEY_WEIGHTINGS)) {
                            List<CHProfile> coreProfiles = new ArrayList<>();
                            List<LMProfile> coreLMProfiles = new ArrayList<>();
                            String coreWeightingsString = StringUtility.trimQuotes(coreOpts.getString(ProfileTools.KEY_WEIGHTINGS));
                            for (String weighting : coreWeightingsString.split(",")) {
                                String configStr = "";
                                if (weighting.contains("|")) {
                                    configStr = weighting;
                                    weighting = weighting.split("\\|")[0];
                                }
                                PMap configMap = new PMap(configStr);
                                boolean considerTurnRestrictions = configMap.getBool("edge_based", hasTurnCosts);

                                String profileName = ProfileTools.makeProfileName(vehicle, weighting, considerTurnRestrictions);
                                profiles.put(profileName, new Profile(profileName).setVehicle(vehicle).setWeighting(weighting).setTurnCosts(considerTurnRestrictions));
                                coreProfiles.add(new CHProfile(profileName));
                                coreLMProfiles.add(new LMProfile(profileName));
                            }
                            ghConfig.setCoreProfiles(coreProfiles);
                            ghConfig.setCoreLMProfiles(coreLMProfiles);
                        }
                        if (coreOpts.hasPath(ProfileTools.KEY_LMSETS))
                            ghConfig.putObject("prepare.corelm.lmsets", StringUtility.trimQuotes(coreOpts.getString(ProfileTools.KEY_LMSETS)));
                        if (coreOpts.hasPath(ProfileTools.KEY_LANDMARKS))
                            ghConfig.putObject("prepare.corelm.landmarks", coreOpts.getInt(ProfileTools.KEY_LANDMARKS));
                    }
                }
            }
        }

        if (config.getExecutionOpts() != null) {
            Config opts = config.getExecutionOpts();
            if (opts.hasPath(ProfileTools.KEY_METHODS_CORE)) {
                Config coreOpts = opts.getConfig(ProfileTools.KEY_METHODS_CORE);
                if (coreOpts.hasPath(ProfileTools.KEY_ACTIVE_LANDMARKS))
                    ghConfig.putObject("routing.corelm.active_landmarks", coreOpts.getInt(ProfileTools.KEY_ACTIVE_LANDMARKS));
            }
            if (opts.hasPath(ProfileTools.KEY_METHODS_LM)) {
                Config lmOpts = opts.getConfig(ProfileTools.KEY_METHODS_LM);
                if (lmOpts.hasPath(ProfileTools.KEY_ACTIVE_LANDMARKS))
                    ghConfig.putObject("routing.lm.active_landmarks", lmOpts.getInt(ProfileTools.KEY_ACTIVE_LANDMARKS));
            }
        }

        if (config.getOptimize() && !prepareCH)
            ghConfig.putObject("graph.do_sort", true);

        if (!config.getGtfsFile().isEmpty())
            ghConfig.putObject("gtfs.file", config.getGtfsFile());

        String flagEncoder = vehicle;
        if (!Helper.isEmpty(config.getEncoderOptions()))
            flagEncoder += "|" + config.getEncoderOptions();

        ghConfig.putObject("graph.flag_encoders", flagEncoder.toLowerCase());
        ghConfig.putObject("index.high_resolution", config.getLocationIndexResolution());
        ghConfig.putObject("index.max_region_search", config.getLocationIndexSearchIterations());
        ghConfig.setProfiles(new ArrayList<>(profiles.values()));

        return ghConfig;
    }

    private static boolean supportWeightingMethod(int profileType) {
        return RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType) || RoutingProfileType.isPedestrian(profileType);
    }

    private boolean hasCHProfile(String profileName) {
        boolean hasCHProfile = false;
        for (CHProfile chProfile : getGraphhopper().getCHPreparationHandler().getCHProfiles()) {
            if (profileName.equals(chProfile.getProfile()))
                hasCHProfile = true;
        }
        return hasCHProfile;
    }

    private boolean hasCoreProfile(String profileName) {
        boolean hasCoreProfile = false;
        for (CHProfile chProfile : getGraphhopper().getCorePreparationHandler().getCHProfiles()) {
            if (profileName.equals(chProfile.getProfile()))
                hasCoreProfile = true;
        }
        return hasCoreProfile;
    }

    public long getMemoryUsage() {
        return mGraphHopper.getMemoryUsage();
    }

    public ORSGraphHopper getGraphhopper() {
        return mGraphHopper;
    }

    public BBox getBounds() {
        return mGraphHopper.getGraphHopperStorage().getBounds();
    }

    public StorableProperties getGraphProperties() {
        return mGraphHopper.getGraphHopperStorage().getProperties();
    }

    public RouteProfileConfiguration getConfiguration() {
        return config;
    }

    public Integer[] getPreferences() {
        return mRoutePrefs;
    }

    public boolean hasCarPreferences() {
        for (Integer mRoutePref : mRoutePrefs) {
            if (RoutingProfileType.isDriving(mRoutePref))
                return true;
        }
        return false;
    }

    public boolean isCHEnabled() {
        return mGraphHopper != null && mGraphHopper.getCHPreparationHandler().isEnabled();
    }

    public void close() {
        mGraphHopper.close();
    }

    private synchronized boolean isGHUsed() {
        return mUseCounter > 0;
    }

    private synchronized void beginUseGH() {
        mUseCounter++;
    }

    private synchronized void endUseGH() {
        mUseCounter--;
    }

    /**
     * This function creates the actual {@link IsochroneMap}.
     * It is important, that whenever attributes contains pop_total it must also contain pop_area. If not the data won't be complete.
     * So the first step in the function is a checkup on that.
     *
     * @param parameters The input are {@link IsochroneSearchParameters}
     * @param attributes The input are a {@link String}[] holding the attributes if set
     * @return The return will be an {@link IsochroneMap}
     * @throws Exception
     */
    public IsochroneMap buildIsochrone(IsochroneSearchParameters parameters, String[] attributes) throws Exception {
        // Checkup for pop_total. If the value is set, pop_area must always be set here, if not already done so by the user.
        String[] tempAttributes;
        if (Arrays.toString(attributes).contains(ProfileTools.KEY_TOTAL_POP.toLowerCase()) && !(Arrays.toString(attributes).contains(ProfileTools.KEY_TOTAL_AREA_KM.toLowerCase()))) {
            tempAttributes = new String[attributes.length + 1];
            int i = 0;
            while (i < attributes.length) {
                String attribute = attributes[i];
                tempAttributes[i] = attribute;
                i++;
            }
            tempAttributes[i] = ProfileTools.KEY_TOTAL_AREA_KM;
        } else if ((Arrays.toString(attributes).contains(ProfileTools.KEY_TOTAL_AREA_KM.toLowerCase())) && (!Arrays.toString(attributes).contains(ProfileTools.KEY_TOTAL_POP.toLowerCase()))) {
            tempAttributes = new String[attributes.length + 1];
            int i = 0;
            while (i < attributes.length) {
                String attribute = attributes[i];
                tempAttributes[i] = attribute;
                i++;
            }
            tempAttributes[i] = ProfileTools.KEY_TOTAL_POP;
        } else {
            tempAttributes = attributes;
        }


        IsochroneMap result;

        beginUseGH();

        try {
            RouteSearchContext searchCntx = createSearchContext(parameters.getRouteParameters());

            IsochroneMapBuilderFactory isochroneMapBuilderFactory = new IsochroneMapBuilderFactory(searchCntx);
            result = isochroneMapBuilderFactory.buildMap(parameters);

            endUseGH();
        } catch (Exception ex) {
            endUseGH();
            if (DebugUtility.isDebug()) {
                LOGGER.error(ex);
            }
            throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "Unable to build an isochrone map.");
        }

        if (tempAttributes != null && result.getIsochronesCount() > 0) {
            try {
                Map<StatisticsProviderConfiguration, List<String>> mapProviderToAttrs = new HashMap<>();
                for (String attr : tempAttributes) {
                    StatisticsProviderConfiguration provConfig = parameters.getStatsProviders().get(attr);

                    if (provConfig != null) {
                        if (mapProviderToAttrs.containsKey(provConfig)) {
                            List<String> attrList = mapProviderToAttrs.get(provConfig);
                            attrList.add(attr);
                        } else {
                            List<String> attrList = new ArrayList<>();
                            attrList.add(attr);
                            mapProviderToAttrs.put(provConfig, attrList);
                        }
                    }
                }

                for (Map.Entry<StatisticsProviderConfiguration, List<String>> entry : mapProviderToAttrs.entrySet()) {
                    StatisticsProviderConfiguration provConfig = entry.getKey();
                    StatisticsProvider provider = StatisticsProviderFactory.getProvider(provConfig.getName(), provConfig.getParameters());
                    String[] provAttrs = provConfig.getMappedProperties(entry.getValue());

                    for (Isochrone isochrone : result.getIsochrones()) {
                        double[] attrValues = provider.getStatistics(isochrone, provAttrs);
                        isochrone.setAttributes(entry.getValue(), attrValues, provConfig.getAttribution());
                    }
                }

            } catch (Exception ex) {
                if (DebugUtility.isDebug()) {
                    LOGGER.error(ex);
                }
                throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "Unable to compute isochrone attributes.");
            }
        }

        return result;
    }

    /**
     * Compute a NxM matrix from a request using any of the three available approaches.
     * For performance reasons, RPHAST is preferred over CoreMatrix, which is preferred over DijkstraMatrix, depending on request conditions.
     *
     * @param req The MatrixRequest object containing details which define which approach should be used.
     * @return A MatrixResult object, possibly with both time and distance values for all combinations of N and M input locations
     * @throws Exception
     */
    public MatrixResult computeMatrix(MatrixRequest req) throws Exception {
        GraphHopper gh = getGraphhopper();
        String encoderName = RoutingProfileType.getEncoderName(req.getProfileType());
        FlagEncoder flagEncoder = gh.getEncodingManager().getEncoder(encoderName);
        PMap hintsMap = new PMap();
        int weightingMethod = req.getWeightingMethod() == WeightingMethod.UNKNOWN ? WeightingMethod.RECOMMENDED : req.getWeightingMethod();
        ProfileTools.setWeightingMethod(hintsMap, weightingMethod, req.getProfileType(), false);
        ProfileTools.setWeighting(hintsMap, weightingMethod, req.getProfileType(), false);
        String CHProfileName = ProfileTools.makeProfileName(encoderName, hintsMap.getString("weighting", ""), false);
        String CoreProfileName = ProfileTools.makeProfileName(encoderName, hintsMap.getString("weighting", ""), true);

        //TODO Refactoring : probably remove MatrixAlgorithmFactory alltogether as the checks for algorithm choice have to be performed here again. Or combine in a single check nicely
        try {
            // RPHAST
            if (!req.getFlexibleMode() && gh.getCHPreparationHandler().isEnabled() && hasCHProfile(CHProfileName)) {
                return computeRPHASTMatrix(req, gh, flagEncoder, CHProfileName);
            }
            // Core
            else if (req.getSearchParameters().getDynamicSpeeds() && mGraphHopper.isCoreAvailable(CoreProfileName)) {
                return computeCoreMatrix(req, gh, flagEncoder, hintsMap, CoreProfileName);
            }
            // Dijkstra
            else {
                // use CHProfileName (w/o turn costs) since Dijkstra is node-based so turn restrictions are not used.
                return computeDijkstraMatrix(req, gh, flagEncoder, hintsMap, CHProfileName);
            }
        } catch (PointNotFoundException e) {
            throw e;
        } catch (MaxVisitedNodesExceededException e) {
            throw new InternalServerException(MatrixErrorCodes.MAX_VISITED_NODES_EXCEEDED, "Unable to compute a distance/duration matrix: " + e.getMessage());
        } catch (Exception ex) {
            throw new InternalServerException(MatrixErrorCodes.UNKNOWN, "Unable to compute a distance/duration matrix: " + ex.getMessage());
        }
    }

    /**
     * Compute a matrix based on a contraction hierarchies graph using the RPHAST algorithm. This is fast, but inflexible.
     *
     * @param req
     * @param gh
     * @param flagEncoder
     * @param profileName
     * @return
     * @throws Exception
     */
    private MatrixResult computeRPHASTMatrix(MatrixRequest req, GraphHopper gh, FlagEncoder flagEncoder, String profileName) throws Exception {
        RoutingCHGraph routingCHGraph = gh.getGraphHopperStorage().getRoutingCHGraph(profileName);
        MatrixSearchContextBuilder builder = new MatrixSearchContextBuilder(gh.getGraphHopperStorage(), gh.getLocationIndex(), AccessFilter.allEdges(flagEncoder.getAccessEnc()), req.getResolveLocations());
        MatrixSearchContext mtxSearchCntx = builder.create(routingCHGraph.getBaseGraph(), routingCHGraph, routingCHGraph.getWeighting(), profileName, req.getSources(), req.getDestinations(), req.getMaximumSearchRadius());

        RPHASTMatrixAlgorithm algorithm = new RPHASTMatrixAlgorithm();
        algorithm.init(req, gh, mtxSearchCntx.getRoutingCHGraph(), flagEncoder, routingCHGraph.getWeighting());
        return algorithm.compute(mtxSearchCntx.getSources(), mtxSearchCntx.getDestinations(), req.getMetrics());
    }

    /**
     * Compute a matrix based on a core contracted graph, which is slower than RPHAST, but offers all the flexibility of the core
     *
     * @return
     */
    private MatrixResult computeCoreMatrix(MatrixRequest req, GraphHopper gh, FlagEncoder flagEncoder, PMap hintsMap, String profileName) throws Exception {
        Weighting weighting = new ORSWeightingFactory(gh.getGraphHopperStorage(), gh.getEncodingManager()).createWeighting(gh.getProfile(profileName), hintsMap, false);
        RoutingCHGraph graph = ((ORSGraphHopperStorage) gh.getGraphHopperStorage()).getCoreGraph(profileName);
        RouteSearchContext searchCntx = createSearchContext(req.getSearchParameters());
        PMap additionalHints = searchCntx.getProperties();
        EdgeFilter edgeFilter = new ORSEdgeFilterFactory().createEdgeFilter(additionalHints, flagEncoder, gh.getGraphHopperStorage());

        MatrixSearchContextBuilder builder = new MatrixSearchContextBuilder(gh.getGraphHopperStorage(), gh.getLocationIndex(), edgeFilter, req.getResolveLocations());
        MatrixSearchContext mtxSearchCntx = builder.create(graph.getBaseGraph(), graph, weighting, profileName, req.getSources(), req.getDestinations(), req.getMaximumSearchRadius());

        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();
        algorithm.init(req, gh, mtxSearchCntx.getRoutingCHGraph(), flagEncoder, weighting, edgeFilter);
        return algorithm.compute(mtxSearchCntx.getSources(), mtxSearchCntx.getDestinations(), req.getMetrics());
    }

    /**
     * Compute a matrix based on the normal graph. Slow, but highly flexible in terms of request parameters.
     *
     * @param req
     * @param gh
     * @param flagEncoder
     * @param hintsMap
     * @param profileName
     * @return
     * @throws Exception
     */
    private MatrixResult computeDijkstraMatrix(MatrixRequest req, GraphHopper gh, FlagEncoder flagEncoder, PMap hintsMap, String profileName) throws Exception {
        Graph graph = gh.getGraphHopperStorage().getBaseGraph();
        Weighting weighting = new ORSWeightingFactory(gh.getGraphHopperStorage(), gh.getEncodingManager()).createWeighting(gh.getProfile(profileName), hintsMap, false);
        MatrixSearchContextBuilder builder = new MatrixSearchContextBuilder(gh.getGraphHopperStorage(), gh.getLocationIndex(), AccessFilter.allEdges(flagEncoder.getAccessEnc()), req.getResolveLocations());
        MatrixSearchContext mtxSearchCntx = builder.create(graph, null, weighting, profileName, req.getSources(), req.getDestinations(), req.getMaximumSearchRadius());

        DijkstraMatrixAlgorithm algorithm = new DijkstraMatrixAlgorithm();
        algorithm.init(req, gh, mtxSearchCntx.getGraph(), flagEncoder, weighting);
        return algorithm.compute(mtxSearchCntx.getSources(), mtxSearchCntx.getDestinations(), req.getMetrics());
    }

    public ExportResult computeExport(ExportRequest req) throws Exception {
        ExportResult res = new ExportResult();

        GraphHopper gh = getGraphhopper();
        String encoderName = RoutingProfileType.getEncoderName(req.getProfileType());
        Graph graph = gh.getGraphHopperStorage().getBaseGraph();

        PMap hintsMap = new PMap();
        int weightingMethod = WeightingMethod.FASTEST;
        ProfileTools.setWeightingMethod(hintsMap, weightingMethod, req.getProfileType(), false);
        String profileName = ProfileTools.makeProfileName(encoderName, hintsMap.getString("weighting_method", ""), false);
        Weighting weighting = gh.createWeighting(gh.getProfile(profileName), hintsMap);

        FlagEncoder flagEncoder = gh.getEncodingManager().getEncoder(encoderName);
        EdgeExplorer explorer = graph.createEdgeExplorer(AccessFilter.outEdges(flagEncoder.getAccessEnc()));


        // filter graph for nodes in Bounding Box
        LocationIndex index = gh.getLocationIndex();
        NodeAccess nodeAccess = graph.getNodeAccess();
        BBox bbox = req.getBoundingBox();

        ArrayList<Integer> nodesInBBox = new ArrayList<>();
        index.query(bbox, edgeId -> {
            // According to GHUtility.getEdgeFromEdgeKey, edgeIds are calculated as edgeKey/2.
            EdgeIteratorState edge = graph.getEdgeIteratorStateForKey(edgeId * 2);
            int baseNode = edge.getBaseNode();
            int adjNode = edge.getAdjNode();

            if (bbox.contains(nodeAccess.getLat(baseNode), nodeAccess.getLon(baseNode))) {
                nodesInBBox.add(baseNode);
            }
            if (bbox.contains(nodeAccess.getLat(adjNode), nodeAccess.getLon(adjNode))) {
                nodesInBBox.add(adjNode);
            }
        });

        LOGGER.debug("Found %d nodes in bbox.".formatted(nodesInBBox.size()));

        if (nodesInBBox.isEmpty()) {
            // without nodes, no export can be calculated
            res.setWarning(new ExportWarning(ExportWarning.EMPTY_BBOX));
            return res;
        }

        // calculate node coordinates
        for (int from : nodesInBBox) {
            Coordinate coord = new Coordinate(nodeAccess.getLon(from), nodeAccess.getLat(from));
            res.addLocation(from, coord);

            EdgeIterator iter = explorer.setBaseNode(from);
            while (iter.next()) {
                int to = iter.getAdjNode();
                if (nodesInBBox.contains(to)) {
                    double weight = weighting.calcEdgeWeight(iter, false, EdgeIterator.NO_EDGE);
                    Pair<Integer, Integer> p = new Pair<>(from, to);
                    res.addEdge(p, weight);

                    if (req.debug()) {
                        Map<String, Object> extra = new HashMap<>();
                        extra.put("edge_id", iter.getEdge());
                        WheelchairAttributesGraphStorage storage = GraphStorageUtils.getGraphExtension(gh.getGraphHopperStorage(), WheelchairAttributesGraphStorage.class);
                        if (storage != null) {
                            WheelchairAttributes attributes = new WheelchairAttributes();
                            byte[] buffer = new byte[WheelchairAttributesGraphStorage.BYTE_COUNT];
                            storage.getEdgeValues(iter.getEdge(), attributes, buffer);
                            if (attributes.hasValues()) {
                                extra.put("incline", attributes.getIncline());
                                extra.put("surface_quality_known", attributes.isSurfaceQualityKnown());
                                extra.put("suitable", attributes.isSuitable());
                            }
                        }
                        OsmIdGraphStorage storage2 = GraphStorageUtils.getGraphExtension(gh.getGraphHopperStorage(), OsmIdGraphStorage.class);
                        if (storage2 != null) {
                            extra.put("osm_id", storage2.getEdgeValue(iter.getEdge()));
                        }
                        res.addEdgeExtra(p, extra);
                    }
                }
            }
        }

        return res;
    }

    private RouteSearchContext createSearchContext(RouteSearchParameters searchParams) throws Exception {
        PMap props = new PMap();

        int profileType = searchParams.getProfileType();
        String encoderName = RoutingProfileType.getEncoderName(profileType);

        if (FlagEncoderNames.UNKNOWN.equals(encoderName))
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "unknown vehicle profile.");

        if (!mGraphHopper.getEncodingManager().hasEncoder(encoderName)) {
            throw new IllegalArgumentException("Vehicle " + encoderName + " unsupported. " + "Supported are: "
                    + mGraphHopper.getEncodingManager());
        }

        FlagEncoder flagEncoder = mGraphHopper.getEncodingManager().getEncoder(encoderName);
        ProfileParameters profileParams = searchParams.getProfileParameters();

        // PARAMETERS FOR PathProcessorFactory

        props.putObject("routing_extra_info", searchParams.getExtraInfo());
        props.putObject("routing_suppress_warnings", searchParams.getSuppressWarnings());

        props.putObject("routing_profile_type", profileType);
        props.putObject("routing_profile_params", profileParams);

        /*
         * PARAMETERS FOR EdgeFilterFactory
         * ======================================================================================================
         */

        /* Avoid areas */
        if (searchParams.hasAvoidAreas()) {
            props.putObject("avoid_areas", searchParams.getAvoidAreas());
        }

        /* Heavy vehicle filter */
        if (profileType == RoutingProfileType.DRIVING_HGV) {
            props.putObject("edgefilter_hgv", searchParams.getVehicleType());
        }

        /* Wheelchair filter */
        else if (profileType == RoutingProfileType.WHEELCHAIR) {
            props.putObject("edgefilter_wheelchair", "true");
        }

        /* Avoid features */
        if (searchParams.hasAvoidFeatures()) {
            props.putObject("avoid_features", searchParams);
        }

        /* Avoid borders of some form */
        if ((searchParams.hasAvoidBorders() || searchParams.hasAvoidCountries())
                && (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType))) {
            props.putObject("avoid_borders", searchParams);
            if (searchParams.hasAvoidCountries())
                props.putObject("avoid_countries", Arrays.toString(searchParams.getAvoidCountries()));
        }

        if (profileParams != null && profileParams.hasWeightings()) {
            props.putObject(ProfileTools.KEY_CUSTOM_WEIGHTINGS, true);
            Iterator<ProfileWeighting> iterator = profileParams.getWeightings().getIterator();
            while (iterator.hasNext()) {
                ProfileWeighting weighting = iterator.next();
                if (!weighting.getParameters().isEmpty()) {
                    String name = ProfileWeighting.encodeName(weighting.getName());
                    for (Map.Entry<String, Object> kv : weighting.getParameters().toMap().entrySet())
                        props.putObject(name + kv.getKey(), kv.getValue());
                }
            }
        }

        boolean useTurnCostProfile = config.isTurnCostEnabled();
        String profileName = ProfileTools.makeProfileName(encoderName, WeightingMethod.getName(searchParams.getWeightingMethod()), useTurnCostProfile);
        String profileNameCH = ProfileTools.makeProfileName(encoderName, WeightingMethod.getName(searchParams.getWeightingMethod()), false);
        RouteSearchContext searchCntx = new RouteSearchContext(mGraphHopper, flagEncoder, profileName, profileNameCH);
        searchCntx.setProperties(props);

        return searchCntx;
    }

    public GHResponse computeRoundTripRoute(double lat0, double lon0, WayPointBearing
            bearing, RouteSearchParameters searchParams, Boolean geometrySimplify) throws Exception {
        GHResponse resp;

        beginUseGH();

        try {
            int profileType = searchParams.getProfileType();
            int weightingMethod = searchParams.getWeightingMethod();
            RouteSearchContext searchCntx = createSearchContext(searchParams);

            List<GHPoint> points = new ArrayList<>();
            points.add(new GHPoint(lat0, lon0));
            List<Double> bearings = new ArrayList<>();
            GHRequest req;

            if (bearing != null) {
                bearings.add(bearing.getValue());
                req = new GHRequest(points, bearings);
            } else {
                req = new GHRequest(points);
            }

            req.setProfile(searchCntx.profileName());
            req.getHints().putObject(Parameters.Algorithms.RoundTrip.DISTANCE, searchParams.getRoundTripLength());
            req.getHints().putObject(Parameters.Algorithms.RoundTrip.POINTS, searchParams.getRoundTripPoints());

            if (searchParams.getRoundTripSeed() > -1) {
                req.getHints().putObject(Parameters.Algorithms.RoundTrip.SEED, searchParams.getRoundTripSeed());
            }

            PMap props = searchCntx.getProperties();
            req.setAdditionalHints(props);

            if (props != null && !props.isEmpty())
                req.getHints().putAll(props);

            if (supportWeightingMethod(profileType))
                ProfileTools.setWeightingMethod(req.getHints(), weightingMethod, profileType, false);
            else
                throw new IllegalArgumentException("Unsupported weighting " + weightingMethod + " for profile + " + profileType);

            //Roundtrip not possible with preprocessed edges.
            setSpeedups(req, false, false, true, searchCntx.profileNameCH());

            if (astarEpsilon != null)
                req.getHints().putObject("astarbi.epsilon", astarEpsilon);
            if (astarApproximation != null)
                req.getHints().putObject("astarbi.approximation", astarApproximation);
            //Overwrite algorithm selected in setSpeedups
            req.setAlgorithm(Parameters.Algorithms.ROUND_TRIP);

            mGraphHopper.getRouterConfig().setSimplifyResponse(geometrySimplify);
            resp = mGraphHopper.route(req);

            endUseGH();

        } catch (Exception ex) {
            endUseGH();

            LOGGER.error(ex);

            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Unable to compute a route");
        }

        return resp;
    }

    public GHResponse computeRoute(double lat0, double lon0, double lat1, double lon1, WayPointBearing[] bearings,
                                   double[] radiuses, boolean directedSegment, RouteSearchParameters searchParams, Boolean geometrySimplify)
            throws Exception {

        GHResponse resp;

        beginUseGH();

        try {
            int profileType = searchParams.getProfileType();
            if (profileType == RoutingProfileType.PUBLIC_TRANSPORT) {
                StopWatch stopWatch = (new StopWatch()).start();
                PtRouter ptRouter = new PtRouterImpl.Factory(mGraphHopper.getConfig(), new TranslationMap().doImport(), mGraphHopper.getGraphHopperStorage(), mGraphHopper.getLocationIndex(), mGraphHopper.getGtfsStorage())
                        .createWithoutRealtimeFeed();
                Request ptRequest = createPTRequest(lat0, lon0, lat1, lon1, searchParams);
                GHResponse res = ptRouter.route(ptRequest);
                res.addDebugInfo("Request total:" + stopWatch.stop().getSeconds() + "s");
                return res;
            }
            int weightingMethod = searchParams.getWeightingMethod();
            RouteSearchContext searchCntx = createSearchContext(searchParams);

            int flexibleMode = searchParams.hasFlexibleMode() || config.isEnforceTurnCosts() ? ProfileTools.KEY_FLEX_PREPROCESSED : ProfileTools.KEY_FLEX_STATIC;
            boolean optimized = searchParams.getOptimized();

            GHRequest req;
            if (bearings == null || bearings[0] == null)
                req = new GHRequest(new GHPoint(lat0, lon0), new GHPoint(lat1, lon1));
            else if (bearings[1] == null)
                req = new GHRequest(new GHPoint(lat0, lon0), new GHPoint(lat1, lon1), bearings[0].getValue(), Double.NaN);
            else
                req = new GHRequest(new GHPoint(lat0, lon0), new GHPoint(lat1, lon1), bearings[0].getValue(), bearings[1].getValue());

            req.setEncoderName(searchCntx.getEncoder().toString());
            req.setProfile(searchCntx.profileName());
            req.setAlgorithm(Parameters.Algorithms.ASTAR_BI);

            if (radiuses != null)
                req.setMaxSearchDistance(radiuses);

            PMap props = searchCntx.getProperties();

            req.setAdditionalHints(props);

            if (props != null && !props.isEmpty())
                req.getHints().putAll(props);

            if (supportWeightingMethod(profileType)) {
                ProfileTools.setWeightingMethod(req.getHints(), weightingMethod, profileType, hasTimeDependentSpeed(searchParams, searchCntx));
                if (requiresTimeDependentWeighting(searchParams, searchCntx))
                    flexibleMode = ProfileTools.KEY_FLEX_PREPROCESSED;
                flexibleMode = getFlexibilityMode(flexibleMode, searchParams, profileType);
            } else
                throw new IllegalArgumentException("Unsupported weighting " + weightingMethod + " for profile + " + profileType);

            if (flexibleMode == ProfileTools.KEY_FLEX_STATIC)
                //Speedup order: useCH, useCore, useALT
                // TODO Future improvement: profileNameCH is an ugly hack and is required because of the hard-coded turnCost=false for CH
                setSpeedups(req, true, true, true, searchCntx.profileNameCH());

            if (flexibleMode == ProfileTools.KEY_FLEX_PREPROCESSED) {
                setSpeedups(req, false, optimized, true, searchCntx.profileNameCH());
            }

            //cannot use CH or CoreALT with requests where the weighting of non-predefined edges might change
            if (flexibleMode == ProfileTools.KEY_FLEX_FULLY)
                setSpeedups(req, false, false, true, searchCntx.profileNameCH());

            if (searchParams.isTimeDependent()) {
                req.setAlgorithm(Parameters.Algorithms.TD_ASTAR);

                String key;
                LocalDateTime time;
                if (searchParams.hasDeparture()) {
                    key = RouteRequestParameterNames.PARAM_DEPARTURE;
                    time = searchParams.getDeparture();
                } else {
                    key = RouteRequestParameterNames.PARAM_ARRIVAL;
                    time = searchParams.getArrival();
                }

                req.getHints().putObject(key, time.atZone(ZoneId.of("Europe/Berlin")).toInstant());
            }

            if (astarEpsilon != null)
                req.getHints().putObject("astarbi.epsilon", astarEpsilon);
            if (astarApproximation != null)
                req.getHints().putObject("astarbi.approximation", astarApproximation);

            if (searchParams.getAlternativeRoutesCount() > 0) {
                req.setAlgorithm("alternative_route");
                req.getHints().putObject("alternative_route.max_paths", searchParams.getAlternativeRoutesCount());
                req.getHints().putObject("alternative_route.max_weight_factor", searchParams.getAlternativeRoutesWeightFactor());
                req.getHints().putObject("alternative_route.max_share_factor", searchParams.getAlternativeRoutesShareFactor());
            }

            if (searchParams.hasMaximumSpeed()) {
                req.getHints().putObject("maximum_speed", searchParams.getMaximumSpeed());
                req.getHints().putObject("maximum_speed_lower_bound", config.getMaximumSpeedLowerBound());
            }

            if (directedSegment) {
                resp = mGraphHopper.constructFreeHandRoute(req);
            } else {
                mGraphHopper.getRouterConfig().setSimplifyResponse(geometrySimplify);
                resp = mGraphHopper.route(req);
            }
            if (DebugUtility.isDebug() && !directedSegment) {
                LOGGER.info("visited nodes: " + resp.getHints().getObject("visited_nodes.sum", null));
            }
            if (DebugUtility.isDebug() && directedSegment) {
                LOGGER.info("skipped segment: " + resp.getHints().getString("skipped_segment", null));
            }
            endUseGH();
        } catch (Exception ex) {
            endUseGH();

            LOGGER.error(ex);

            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Unable to compute a route");
        }

        return resp;
    }

    private Request createPTRequest(double lat0, double lon0, double lat1, double lon1, RouteSearchParameters params) throws IncompatibleParameterException {
        List<GHLocation> points = Arrays.asList(new GHPointLocation(new GHPoint(lat0, lon0)), new GHPointLocation(new GHPoint(lat1, lon1)));

        // GH uses pt.earliest_departure_time for both departure and arrival.
        // We need to check which is used here (and issue an exception if it's both) and consequently parse it and set arrive_by.
        Instant departureTime = null;
        boolean arrive_by = false;
        if (params.hasDeparture() && params.hasArrival()) {
            throw new IncompatibleParameterException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, RouteRequestParameterNames.PARAM_DEPARTURE, RouteRequestParameterNames.PARAM_ARRIVAL);
        } else if (params.hasArrival()) {
            departureTime = params.getArrival().toInstant(ZoneOffset.UTC);
            arrive_by = true;
        } else if (params.hasDeparture()) {
            departureTime = params.getDeparture().toInstant(ZoneOffset.UTC);
        } else {
            // pt.earliest_departure_time is @NotNull, we need to emulate that here.
            departureTime = Instant.now();
        }

        Request ptRequest = new Request(points, departureTime);
        ptRequest.setArriveBy(arrive_by);

        // schedule is called profile in GraphHopper
        if (params.hasSchedule()) {
            ptRequest.setProfileQuery(params.getSchedule());
        } else {
            ptRequest.setProfileQuery(false);
        }

        // scheduleDuration is called profileDuration accordingly
        if (params.hasScheduleDuration()) {
            ptRequest.setMaxProfileDuration(params.getScheduleDuaration());
        }

        // this will default to false
        ptRequest.setIgnoreTransfers(params.getIgnoreTransfers());

        // TODO: check whether language can be parsed in RouteResultBuilder
        // language is called locale in GraphHopper
        // if (params.hasLanguage()) {
        //    ptRequest.setLocale(Helper.getLocale(params.getLanguage().toString()));
        // }

        // scheduleRows is called limitSolutions in GraphHopper
        if (params.hasScheduleRows()) {
            ptRequest.setLimitSolutions(params.getScheduleRows());
        }

        // setLimitTripTime missing from documentation
        // according to GraphHopper

        // walkingTime is called limit_street_time in GraphHopper
        if (params.hasWalkingTime()) {
            ptRequest.setLimitStreetTime(params.getWalkingTime());
        } else {
            ptRequest.setLimitStreetTime(Duration.ofMinutes(15));
        }

        // default to foot access and egress
        ptRequest.setAccessProfile("foot_fastest");
        ptRequest.setEgressProfile("foot_fastest");

        ptRequest.setMaxVisitedNodes(config.getMaximumVisitedNodesPT());

        return ptRequest;
    }

    /**
     * Get the flexibility mode necessary for the searchParams.
     * Reults in usage of CH, Core or ALT/AStar
     *
     * @param flexibleMode initial flexibleMode
     * @param searchParams RouteSearchParameters
     * @param profileType  Necessary for HGV
     * @return flexibility as int
     */
    private int getFlexibilityMode(int flexibleMode, RouteSearchParameters searchParams, int profileType) {
        if (searchParams.requiresDynamicPreprocessedWeights() || profileType == RoutingProfileType.WHEELCHAIR)
            flexibleMode = ProfileTools.KEY_FLEX_PREPROCESSED;

        if (searchParams.requiresFullyDynamicWeights())
            flexibleMode = ProfileTools.KEY_FLEX_FULLY;
        //If we have special weightings, we have to fall back to ALT with Beeline
        ProfileParameters profileParams = searchParams.getProfileParameters();
        if (profileParams != null && profileParams.hasWeightings())
            flexibleMode = ProfileTools.KEY_FLEX_FULLY;

        return flexibleMode;
    }

    /**
     * Set the speedup techniques used for calculating the route.
     * Reults in usage of CH, Core or ALT/AStar, if they are enabled.
     *
     * @param req     Request whose hints will be set
     * @param useCH   Should CH be enabled
     * @param useCore Should Core be enabled
     * @param useALT  Should ALT be enabled
     */
    private void setSpeedups(GHRequest req, boolean useCH, boolean useCore, boolean useALT, String profileNameCH) {
        String profileName = req.getProfile();

        //Priority: CH->Core->ALT
        String profileNameNoTC = profileName.replace("_with_turn_costs", "");

        useCH = useCH && mGraphHopper.isCHAvailable(profileNameCH);
        useCore = useCore && !useCH && (mGraphHopper.isCoreAvailable(profileName) || mGraphHopper.isCoreAvailable(profileNameNoTC));
        useALT = useALT && !useCH && !useCore && mGraphHopper.isLMAvailable(profileName);

        req.getHints().putObject(ProfileTools.KEY_CH_DISABLE, !useCH);
        req.getHints().putObject(ProfileTools.KEY_CORE_DISABLE, !useCore);
        req.getHints().putObject(ProfileTools.KEY_LM_DISABLE, !useALT);

        if (useCH) {
            req.setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
            req.setProfile(profileNameCH);
        }
        if (useCore) {
            // fallback to a core profile without turn costs if one is available
            if (!mGraphHopper.isCoreAvailable(profileName) && mGraphHopper.isCoreAvailable(profileNameNoTC))
                req.setProfile(profileNameNoTC);
        }
    }

    boolean hasTimeDependentSpeed(RouteSearchParameters searchParams, RouteSearchContext searchCntx) {
        FlagEncoder flagEncoder = searchCntx.getEncoder();
        String key = EncodingManager.getKey(flagEncoder, ConditionalEdges.SPEED);
        return searchParams.isTimeDependent() && flagEncoder.hasEncodedValue(key);
    }

    boolean requiresTimeDependentWeighting(RouteSearchParameters searchParams, RouteSearchContext searchCntx) {
        if (!searchParams.isTimeDependent())
            return false;

        FlagEncoder flagEncoder = searchCntx.getEncoder();

        return flagEncoder.hasEncodedValue(EncodingManager.getKey(flagEncoder, ConditionalEdges.ACCESS))
                || flagEncoder.hasEncodedValue(EncodingManager.getKey(flagEncoder, ConditionalEdges.SPEED))
                || mGraphHopper.isTrafficEnabled();
    }

    /**
     * This function creates the actual {@link IsochroneMap}.
     * So the first step in the function is a checkup on that.
     *
     * @param parameters The input are {@link IsochroneSearchParameters}
     * @return The return will be an {@link IsochroneMap}
     * @throws Exception
     */
    public IsochroneMap buildIsochrone(IsochroneSearchParameters parameters) throws Exception {
        IsochroneMap result;

        beginUseGH();
        try {
            RouteSearchContext searchCntx = createSearchContext(parameters.getRouteParameters());
            IsochroneMapBuilderFactory isochroneMapBuilderFactory = new IsochroneMapBuilderFactory(searchCntx);
            result = isochroneMapBuilderFactory.buildMap(parameters);
            endUseGH();
        } catch (Exception ex) {
            endUseGH();
            if (DebugUtility.isDebug()) {
                LOGGER.error(ex);
            }
            throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "Unable to build an isochrone map.");
        }

        if (result.getIsochronesCount() > 0) {
            if (parameters.hasAttribute(ProfileTools.KEY_TOTAL_POP)) {
                try {
                    Map<StatisticsProviderConfiguration, List<String>> mapProviderToAttrs = new HashMap<>();
                    StatisticsProviderConfiguration provConfig = parameters.getStatsProviders().get(ProfileTools.KEY_TOTAL_POP);
                    if (provConfig != null) {
                        List<String> attrList = new ArrayList<>();
                        attrList.add(ProfileTools.KEY_TOTAL_POP);
                        mapProviderToAttrs.put(provConfig, attrList);
                    }
                    for (Map.Entry<StatisticsProviderConfiguration, List<String>> entry : mapProviderToAttrs.entrySet()) {
                        provConfig = entry.getKey();
                        StatisticsProvider provider = StatisticsProviderFactory.getProvider(provConfig.getName(), provConfig.getParameters());
                        String[] provAttrs = provConfig.getMappedProperties(entry.getValue());

                        for (Isochrone isochrone : result.getIsochrones()) {

                            double[] attrValues = provider.getStatistics(isochrone, provAttrs);
                            isochrone.setAttributes(entry.getValue(), attrValues, provConfig.getAttribution());

                        }
                    }
                } catch (Exception ex) {
                    LOGGER.error(ex);

                    throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "Unable to compute isochrone total_pop attribute.");
                }
            }
            if (parameters.hasAttribute("reachfactor") || parameters.hasAttribute("area")) {
                for (Isochrone isochrone : result.getIsochrones()) {
                    String units = parameters.getUnits();
                    String areaUnits = parameters.getAreaUnits();
                    if (areaUnits != null) units = areaUnits;
                    double area = isochrone.calcArea(units);
                    if (parameters.hasAttribute("area")) {
                        isochrone.setArea(area);
                    }
                    if (parameters.hasAttribute("reachfactor")) {
                        double reachfactor = isochrone.calcReachfactor(units);
                        // reach factor could be > 1, which would confuse people
                        reachfactor = (reachfactor > 1) ? 1 : reachfactor;
                        isochrone.setReachfactor(reachfactor);
                    }
                }
            }
        }
        return result;
    }

    public boolean equals(Object o) {
        return o != null && o.getClass().equals(RoutingProfile.class) && this.hashCode() == o.hashCode();
    }

    public int hashCode() {
        return mGraphHopper.getGraphHopperStorage().getDirectory().getLocation().hashCode();
    }
}
