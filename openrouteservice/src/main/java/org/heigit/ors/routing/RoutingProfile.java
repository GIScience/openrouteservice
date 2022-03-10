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

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import com.typesafe.config.Config;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.centrality.CentralityRequest;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.centrality.CentralityWarning;
import org.heigit.ors.centrality.algorithms.CentralityAlgorithm;
import org.heigit.ors.centrality.algorithms.brandes.BrandesCentralityAlgorithm;
import org.heigit.ors.common.Pair;
import org.heigit.ors.config.IsochronesServiceSettings;
import org.heigit.ors.config.MatrixServiceSettings;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.isochrones.*;
import org.heigit.ors.isochrones.statistics.StatisticsProvider;
import org.heigit.ors.isochrones.statistics.StatisticsProviderConfiguration;
import org.heigit.ors.isochrones.statistics.StatisticsProviderFactory;
import org.heigit.ors.mapmatching.MapMatcher;
import org.heigit.ors.mapmatching.RouteSegmentInfo;
import org.heigit.ors.mapmatching.hmm.HiddenMarkovMapMatcher;
import org.heigit.ors.matrix.*;
import org.heigit.ors.matrix.algorithms.dijkstra.DijkstraMatrixAlgorithm;
import org.heigit.ors.matrix.algorithms.rphast.RPHASTMatrixAlgorithm;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.heigit.ors.routing.graphhopper.extensions.*;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.BordersGraphStorageBuilder;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters;
import org.heigit.ors.routing.parameters.ProfileParameters;
import org.heigit.ors.routing.pathprocessors.ORSPathProcessorFactory;
import org.heigit.ors.util.DebugUtility;
import org.heigit.ors.util.RuntimeUtility;
import org.heigit.ors.util.StringUtility;
import org.heigit.ors.util.TimeUtility;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
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
    private static final String KEY_CUSTOM_WEIGHTINGS = "custom_weightings";
    private static final String VAL_SHORTEST = "shortest";
    private static final String VAL_FASTEST = "fastest";
    private static final String VAL_RECOMMENDED = "recommended";
    private static final String KEY_WEIGHTING = "weighting";
    private static final String KEY_WEIGHTING_METHOD = "weighting_method";
    private static final String KEY_CH_DISABLE = "ch.disable";
    private static final String KEY_LM_DISABLE = "lm.disable";
    private static final String KEY_CORE_DISABLE = "core.disable";
    private static final String KEY_PREPARE_CH_WEIGHTINGS = "prepare.ch.weightings";
    private static final String KEY_PREPARE_LM_WEIGHTINGS = "prepare.lm.weightings";
    private static final String KEY_PREPARE_CORE_WEIGHTINGS = "prepare.core.weightings";
    private static final String KEY_PREPARE_FASTISOCHRONE_WEIGHTINGS = "prepare.fastisochrone.weightings";
    private static final String KEY_METHODS_CH = "methods.ch";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_THREADS = "threads";
    private static final String KEY_WEIGHTINGS = "weightings";
    private static final String KEY_LMSETS = "lmsets";
    private static final String KEY_MAXCELLNODES = "maxcellnodes";
    private static final String KEY_METHODS_LM = "methods.lm";
    private static final String KEY_LANDMARKS = "landmarks";
    private static final String KEY_METHODS_CORE = "methods.core";
    private static final String KEY_DISABLING_ALLOWED = "disabling_allowed";
    private static final String KEY_ACTIVE_LANDMARKS = "active_landmarks";
    private static final String KEY_TOTAL_POP = "total_pop";
    private static final String KEY_TOTAL_AREA_KM = "total_area_km";
    private static final int KEY_FLEX_STATIC = 0;
    private static final int KEY_FLEX_PREPROCESSED = 1;
    private static final int KEY_FLEX_FULLY = 2;
    private static final Object lockObj = new Object();
    private static int profileIdentifier = 0;
    private final Integer[] mRoutePrefs;
    private final RouteProfileConfiguration config;
    private ORSGraphHopper mGraphHopper;
    private Integer mUseCounter;
    private boolean mUpdateRun;
    private MapMatcher mMapMatcher;
    private String astarApproximation;
    private Double astarEpsilon;

    public RoutingProfile(String osmFile, RouteProfileConfiguration rpc, RoutingProfileLoadContext loadCntx) throws Exception {
        mRoutePrefs = rpc.getProfilesTypes();
        mUseCounter = 0;

        mGraphHopper = initGraphHopper(osmFile, rpc, loadCntx);

        config = rpc;

        Config optsExecute = config.getExecutionOpts();
        if (optsExecute != null) {
            if (optsExecute.hasPath("methods.astar.approximation"))
                astarApproximation = optsExecute.getString("methods.astar.approximation");
            if (optsExecute.hasPath("methods.astar.epsilon"))
                astarEpsilon = Double.parseDouble(optsExecute.getString("methods.astar.epsilon"));
        }
    }

    public static ORSGraphHopper initGraphHopper(String osmFile, RouteProfileConfiguration config, RoutingProfileLoadContext loadCntx) throws Exception {
        GraphHopperConfig args = createGHSettings(osmFile, config);

        int profileId;
        synchronized (lockObj) {
            profileIdentifier++;
            profileId = profileIdentifier;
        }

        long startTime = System.currentTimeMillis();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format("[%d] Profiles: '%s', location: '%s'.", profileId, config.getProfiles(), config.getGraphPath()));
        }

        GraphProcessContext gpc = new GraphProcessContext(config);

        ORSGraphHopper gh = new ORSGraphHopper(gpc);

        ORSDefaultFlagEncoderFactory flagEncoderFactory = new ORSDefaultFlagEncoderFactory();
        gh.setFlagEncoderFactory(flagEncoderFactory);

        ORSEdgeFilterFactory edgeFilterFactory = new ORSEdgeFilterFactory();
        // TODO: gh.setEdgeFilterFactory(edgeFilterFactory);

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
//        gh.setWeightingFactory(new ORSWeightingFactory());

        gh.importOrLoad();

        // store CountryBordersReader for later use
        for (GraphStorageBuilder builder : gpc.getStorageBuilders()) {
            if (builder.getName().equals(BordersGraphStorageBuilder.BUILDER_NAME)) {
                pathProcessorFactory.setCountryBordersReader(((BordersGraphStorageBuilder) builder).getCbReader());
            }
        }

        if (LOGGER.isInfoEnabled()) {
            GraphHopperStorage ghStorage = gh.getGraphHopperStorage();
            LOGGER.info(String.format("[%d] Edges: %s - Nodes: %s.", profileId, ghStorage.getEdges(), ghStorage.getNodes()));
            LOGGER.info(String.format("[%d] Total time: %s.", profileId, TimeUtility.getElapsedTime(startTime, true)));
            LOGGER.info(String.format("[%d] Finished at: %s.", profileId, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
        }

        // Make a stamp which help tracking any changes in the size of OSM file.
        File file = new File(osmFile);
        Path pathTimestamp = Paths.get(config.getGraphPath(), "stamp.txt");
        File file2 = pathTimestamp.toFile();
        if (!file2.exists())
            Files.write(pathTimestamp, Long.toString(file.length()).getBytes());

        return gh;
    }

    private static GraphHopperConfig createGHSettings(String sourceFile, RouteProfileConfiguration config) {
        GraphHopperConfig ghConfig = new GraphHopperConfig();
        ghConfig.putObject("graph.dataaccess", "RAM_STORE");
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

        ghConfig.putObject(KEY_PREPARE_CORE_WEIGHTINGS, "no");

        if (config.getIsochronePreparationOpts() != null) {
            Config fastisochroneOpts = config.getIsochronePreparationOpts();
            prepareFI = true;
            if (fastisochroneOpts.hasPath(KEY_ENABLED) || fastisochroneOpts.getBoolean(KEY_ENABLED)) {
                prepareFI = fastisochroneOpts.getBoolean(KEY_ENABLED);
                if (!prepareFI)
                    ghConfig.putObject(KEY_PREPARE_FASTISOCHRONE_WEIGHTINGS, "no");
                else
                    ghConfig.putObject(ORSParameters.FastIsochrone.PROFILE, config.getProfiles());
            }

            if (prepareFI) {
                if (fastisochroneOpts.hasPath(KEY_THREADS))
                    ghConfig.putObject("prepare.fastisochrone.threads", fastisochroneOpts.getInt(KEY_THREADS));
                if (fastisochroneOpts.hasPath(KEY_WEIGHTINGS))
                    ghConfig.putObject(KEY_PREPARE_FASTISOCHRONE_WEIGHTINGS, StringUtility.trimQuotes(fastisochroneOpts.getString(KEY_WEIGHTINGS)));
                if (fastisochroneOpts.hasPath(KEY_MAXCELLNODES))
                    ghConfig.putObject("prepare.fastisochrone.maxcellnodes", StringUtility.trimQuotes(fastisochroneOpts.getString(KEY_MAXCELLNODES)));
            }
        }

        if (config.getPreparationOpts() != null) {
            Config opts = config.getPreparationOpts();
            if (opts.hasPath("min_network_size"))
                ghConfig.putObject("prepare.min_network_size", opts.getInt("min_network_size"));
            if (opts.hasPath("min_one_way_network_size"))
                ghConfig.putObject("prepare.min_one_way_network_size", opts.getInt("min_one_way_network_size"));

            if (opts.hasPath("methods")) {
                if (opts.hasPath(KEY_METHODS_CH)) {
                    prepareCH = true;
                    Config chOpts = opts.getConfig(KEY_METHODS_CH);

                    if (chOpts.hasPath(KEY_ENABLED) || chOpts.getBoolean(KEY_ENABLED)) {
                        prepareCH = chOpts.getBoolean(KEY_ENABLED);
                    }

                    if (prepareCH) {
//                        if (chOpts.hasPath(KEY_THREADS))
//                            ghConfig.putObject("prepare.ch.threads", chOpts.getInt(KEY_THREADS));
//                        if (chOpts.hasPath(KEY_WEIGHTINGS))
//                            ghConfig.putObject(KEY_PREPARE_CH_WEIGHTINGS, StringUtility.trimQuotes(chOpts.getString(KEY_WEIGHTINGS)));
                    }
                }

                if (opts.hasPath(KEY_METHODS_LM)) {
                    prepareLM = true;
                    Config lmOpts = opts.getConfig(KEY_METHODS_LM);

                    if (lmOpts.hasPath(KEY_ENABLED) || lmOpts.getBoolean(KEY_ENABLED)) {
                        prepareLM = lmOpts.getBoolean(KEY_ENABLED);
                    }

                    if (prepareLM) {
//                        if (lmOpts.hasPath(KEY_THREADS))
//                            ghConfig.putObject("prepare.lm.threads", lmOpts.getInt(KEY_THREADS));
//                        if (lmOpts.hasPath(KEY_WEIGHTINGS))
//                            ghConfig.putObject(KEY_PREPARE_LM_WEIGHTINGS, StringUtility.trimQuotes(lmOpts.getString(KEY_WEIGHTINGS)));
//                        if (lmOpts.hasPath(KEY_LANDMARKS))
//                            ghConfig.putObject("prepare.lm.landmarks", lmOpts.getInt(KEY_LANDMARKS));
                    }
                }

                if (opts.hasPath(KEY_METHODS_CORE)) {
                    prepareCore = true;
                    Config coreOpts = opts.getConfig(KEY_METHODS_CORE);

                    if (coreOpts.hasPath(KEY_ENABLED) || coreOpts.getBoolean(KEY_ENABLED)) {
                        prepareCore = coreOpts.getBoolean(KEY_ENABLED);
                        if (!prepareCore)
                            ghConfig.putObject(KEY_PREPARE_CORE_WEIGHTINGS, "no");
                    }

                    if (prepareCore) {
                        if (coreOpts.hasPath(KEY_THREADS))
                            ghConfig.putObject("prepare.core.threads", coreOpts.getInt(KEY_THREADS));
                        if (coreOpts.hasPath(KEY_WEIGHTINGS))
                            ghConfig.putObject(KEY_PREPARE_CORE_WEIGHTINGS, StringUtility.trimQuotes(coreOpts.getString(KEY_WEIGHTINGS)));
                        if (coreOpts.hasPath(KEY_LMSETS))
                            ghConfig.putObject("prepare.corelm.lmsets", StringUtility.trimQuotes(coreOpts.getString(KEY_LMSETS)));
                        if (coreOpts.hasPath(KEY_LANDMARKS))
                            ghConfig.putObject("prepare.corelm.landmarks", coreOpts.getInt(KEY_LANDMARKS));
                    }
                }
            }
        }

        if (config.getExecutionOpts() != null) {
            Config opts = config.getExecutionOpts();
            if (opts.hasPath(KEY_METHODS_CORE)) {
                Config coreOpts = opts.getConfig(KEY_METHODS_CORE);
                if (coreOpts.hasPath(KEY_DISABLING_ALLOWED))
                    ghConfig.putObject("routing.core.disabling_allowed", coreOpts.getBoolean(KEY_DISABLING_ALLOWED));

                if (coreOpts.hasPath(KEY_ACTIVE_LANDMARKS))
                    ghConfig.putObject("routing.corelm.active_landmarks", coreOpts.getInt(KEY_ACTIVE_LANDMARKS));
            }
            if (opts.hasPath(KEY_METHODS_LM)) {
                Config lmOpts = opts.getConfig(KEY_METHODS_LM);
                if (lmOpts.hasPath(KEY_ACTIVE_LANDMARKS))
                    ghConfig.putObject("routing.lm.active_landmarks", lmOpts.getInt(KEY_ACTIVE_LANDMARKS));
            }
        }

        if (config.getOptimize() && !prepareCH)
            ghConfig.putObject("graph.do_sort", true);

        StringBuilder flagEncoders = new StringBuilder();
        String[] encoderOpts = !Helper.isEmpty(config.getEncoderOptions()) ? config.getEncoderOptions().split(",") : null;
        Integer[] profilesTypes = config.getProfilesTypes();
        List<Profile> profiles = new ArrayList(profilesTypes.length);
        List<CHProfile> chProfiles = new ArrayList<>();
        List<LMProfile> lmProfiles = new ArrayList<>();

        // TODO: Multiple profiles were used to share the graph  for several
        //       bike profiles. We don't use this feature now but it might be
        //       desireable in the future. However, this behavior is standard
        //       in original GH through an already existing mechanism.
        for (int i = 0; i < profilesTypes.length; i++) {
            String vehicle = RoutingProfileType.getEncoderName(profilesTypes[i]);
            if (encoderOpts == null)
                flagEncoders.append(vehicle);
            else
                flagEncoders.append(vehicle + "|" + encoderOpts[i]);
            if (i < profilesTypes.length - 1)
                flagEncoders.append(",");

            // TODO: make this list of weightings configurable for each vehicle as in GH
            String[] weightings = {VAL_FASTEST, VAL_SHORTEST, VAL_RECOMMENDED};
            for (String weighting : weightings) {
                String profileName = makeProfileName(vehicle, weighting);

                profiles.add(new Profile(profileName).setVehicle(vehicle).setWeighting(weighting));
                if (prepareCH) {
                    chProfiles.add(new CHProfile(profileName));
                }
                if (prepareLM) {
                    lmProfiles.add(new LMProfile(profileName));
                }
            }
        }

        ghConfig.putObject("graph.flag_encoders", flagEncoders.toString().toLowerCase());
        ghConfig.putObject("index.high_resolution", config.getLocationIndexResolution());
        ghConfig.putObject("index.max_region_search", config.getLocationIndexSearchIterations());
        ghConfig.setProfiles(profiles);
        ghConfig.setCHProfiles(chProfiles);
        ghConfig.setLMProfiles(lmProfiles);

        return ghConfig;
    }

    private static String makeProfileName(String vehicleName, String weightingName) {
        return vehicleName + "_" + weightingName;
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

    public long getCapacity() {
        GraphHopperStorage graph = mGraphHopper.getGraphHopperStorage();
        return graph.getCapacity(); // TODO: how to deal with + graph.getExtension().getCapacity();
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

    public void updateGH(GraphHopper gh) throws Exception {
        if (gh == null)
            throw new Exception("GraphHopper instance is null.");

        try {
            mUpdateRun = true;
            while (true) {
                if (!isGHUsed()) {
                    GraphHopper ghOld = mGraphHopper;

                    ghOld.close();
                    ghOld.clean();

                    gh.close();
                    // gh.clean(); // do not remove on-disk files, we need to
                    // copy them as follows

                    RuntimeUtility.clearMemory(LOGGER);

                    // Change the content of the graph folder
                    String oldLocation = ghOld.getGraphHopperLocation();
                    File dstDir = new File(oldLocation);
                    File srcDir = new File(gh.getGraphHopperLocation());
                    FileUtils.copyDirectory(srcDir, dstDir, true);
                    FileUtils.deleteDirectory(srcDir);

                    RoutingProfileLoadContext loadCntx = new RoutingProfileLoadContext();

                    mGraphHopper = initGraphHopper(ghOld.getOSMFile(), config, loadCntx);

                    loadCntx.releaseElevationProviderCacheAfterAllVehicleProfilesHaveBeenProcessed();

                    break;
                }

                Thread.sleep(2000);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }

        mUpdateRun = false;
    }

    private void waitForUpdateCompletion() throws Exception {
        if (mUpdateRun) {
            long startTime = System.currentTimeMillis();

            while (mUpdateRun) {
                long curTime = System.currentTimeMillis();
                if (curTime - startTime > 600000) {
                    throw new Exception("The route profile is currently being updated.");
                }

                Thread.sleep(1000);
            }
        }
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
        if (Arrays.toString(attributes).contains(KEY_TOTAL_POP.toLowerCase()) && !(Arrays.toString(attributes).contains(KEY_TOTAL_AREA_KM.toLowerCase()))) {
            tempAttributes = new String[attributes.length + 1];
            int i = 0;
            while (i < attributes.length) {
                String attribute = attributes[i];
                tempAttributes[i] = attribute;
                i++;
            }
            tempAttributes[i] = KEY_TOTAL_AREA_KM;
        } else if ((Arrays.toString(attributes).contains(KEY_TOTAL_AREA_KM.toLowerCase())) && (!Arrays.toString(attributes).contains(KEY_TOTAL_POP.toLowerCase()))) {
            tempAttributes = new String[attributes.length + 1];
            int i = 0;
            while (i < attributes.length) {
                String attribute = attributes[i];
                tempAttributes[i] = attribute;
                i++;
            }
            tempAttributes[i] = KEY_TOTAL_POP;
        } else {
            tempAttributes = attributes;
        }


        IsochroneMap result;
        waitForUpdateCompletion();

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
                    StatisticsProviderConfiguration provConfig = IsochronesServiceSettings.getStatsProviders().get(attr);

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
        setWeightingMethod(hintsMap, weightingMethod, req.getProfileType(), false);
        setWeighting(hintsMap, weightingMethod, req.getProfileType(), false);
        String profileName = makeProfileName(encoderName, hintsMap.getString("weighting", ""));

        //TODO probably remove MatrixAlgorithmFactory alltogether as the checks for algorithm choice have to be performed here again. Or combine in a single check nicely
        try {
            // RPHAST
            if (!req.getFlexibleMode() && gh.getCHPreparationHandler().isEnabled() && hasCHProfile(profileName)) {
                return computeRPHASTMatrix(req, gh, flagEncoder, profileName);
            }
            // Core
            //TODO check whether hasCoreProfile is equivalent to isCoreAvailable
            else if (req.getSearchParameters().getDynamicSpeeds() && hasCoreProfile(profileName)) { //&& ((ORSGraphHopper) (gh)).isCoreAvailable(weightingName)) {
                return computeCoreMatrix();
            }
            // Dijkstra
            else {
                return computeDijkstraMatrix(req, gh, flagEncoder, hintsMap, profileName);
            }
        } catch (Exception ex) {
            throw new InternalServerException(MatrixErrorCodes.UNKNOWN, "Unable to compute a distance/duration matrix.");
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
        MatrixSearchContextBuilder builder = new MatrixSearchContextBuilder(gh.getLocationIndex(), AccessFilter.allEdges(flagEncoder.getAccessEnc()), req.getResolveLocations());
        MatrixSearchContext mtxSearchCntx = builder.create(routingCHGraph.getBaseGraph(), routingCHGraph, req.getSources(), req.getDestinations(), MatrixServiceSettings.getMaximumSearchRadius());

        RPHASTMatrixAlgorithm algorithm = new RPHASTMatrixAlgorithm();
        algorithm.init(req, gh, mtxSearchCntx.getRoutingCHGraph(), flagEncoder, routingCHGraph.getWeighting());
        MatrixResult mtxResult = algorithm.compute(mtxSearchCntx.getSources(), mtxSearchCntx.getDestinations(), req.getMetrics());
        return mtxResult;
    }

    /**
     * Compute a matrix based on a core contracted graph, which is slower than RPHAST, but offers all the flexibility of the core
     *
     * @return
     */
    private MatrixResult computeCoreMatrix() {
//        Weighting weighting = new ORSWeightingFactory().createWeighting(hintsMap, flagEncoder, gh.getGraphHopperStorage());
//        RoutingCHGraph graph = this.mGraphHopper.getGraphHopperStorage().getCoreGraph(weighting);
//        RouteSearchContext searchCntx = createSearchContext(req.getSearchParameters());
//        PMap additionalHints = (PMap) searchCntx.getProperties();
//        EdgeFilter edgeFilter = this.mGraphHopper.getEdgeFilterFactory().createEdgeFilter(additionalHints, flagEncoder, this.mGraphHopper.getGraphHopperStorage());
//
//        MatrixSearchContextBuilder builder = new MatrixSearchContextBuilder(gh.getLocationIndex(), edgeFilter, req.getResolveLocations());
//        MatrixSearchContext mtxSearchCntx = builder.create(graph, req.getSources(), req.getDestinations(), MatrixServiceSettings.getMaximumSearchRadius());
//
//        weighting = createTurnWeighting(graph, weighting, TraversalMode.EDGE_BASED, MatrixServiceSettings.getUTurnCost());
//        if (weighting instanceof TurnWeighting)
//            ((TurnWeighting) weighting).setInORS(true);
//        ((CoreMatrixAlgorithm) alg).init(req, gh, mtxSearchCntx.getGraph(), flagEncoder, weighting, edgeFilter);
//        mtxResult = alg.compute(mtxSearchCntx.getSources(), mtxSearchCntx.getDestinations(), req.getMetrics());
//        return mtxResult;
        return null;
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
        Weighting weighting = new OrsWeightingFactoryGh4(gh.getGraphHopperStorage(), gh.getEncodingManager()).createWeighting(gh.getProfile(profileName), hintsMap, false);
        MatrixSearchContextBuilder builder = new MatrixSearchContextBuilder(gh.getLocationIndex(), AccessFilter.allEdges(flagEncoder.getAccessEnc()), req.getResolveLocations());
        MatrixSearchContext mtxSearchCntx = builder.create(graph, null, req.getSources(), req.getDestinations(), MatrixServiceSettings.getMaximumSearchRadius());

        DijkstraMatrixAlgorithm algorithm = new DijkstraMatrixAlgorithm();
        algorithm.init(req, gh, mtxSearchCntx.getGraph(), flagEncoder, weighting);
        MatrixResult mtxResult = algorithm.compute(mtxSearchCntx.getSources(), mtxSearchCntx.getDestinations(), req.getMetrics());
        return mtxResult;
    }

    public CentralityResult computeCentrality(CentralityRequest req) throws Exception {
        CentralityResult res = new CentralityResult();

        GraphHopper gh = getGraphhopper();
        String encoderName = RoutingProfileType.getEncoderName(req.getProfileType());
        FlagEncoder flagEncoder = gh.getEncodingManager().getEncoder(encoderName);
        Graph graph = gh.getGraphHopperStorage().getBaseGraph();

        PMap hintsMap = new PMap();
        int weightingMethod = WeightingMethod.FASTEST;
        setWeightingMethod(hintsMap, weightingMethod, req.getProfileType(), false);
        Weighting weighting = new ORSWeightingFactory(gh.getGraphHopperStorage(), flagEncoder).createWeighting(hintsMap, false);
        EdgeExplorer explorer = graph.createEdgeExplorer(AccessFilter.outEdges(flagEncoder.getAccessEnc()));

        // filter graph for nodes in Bounding Box
        LocationIndex index = gh.getLocationIndex();
        NodeAccess nodeAccess = graph.getNodeAccess();
        BBox bbox = req.getBoundingBox();
        List<Integer> excludeNodes = req.getExcludeNodes();

        ArrayList<Integer> nodesInBBox = new ArrayList<>();
        index.query(bbox, edgeId -> {
            // According to GHUtility.getEdgeFromEdgeKey, edgeIds are calculated as edgeKey/2.
            EdgeIteratorState edge = graph.getEdgeIteratorStateForKey(edgeId * 2);
            int baseNode = edge.getBaseNode();
            int adjNode = edge.getAdjNode();

            //we only add nodes once, if they are not excluded and in our bbox.
            if (!nodesInBBox.contains(baseNode) && !excludeNodes.contains(baseNode) && bbox.contains(nodeAccess.getLat(baseNode), nodeAccess.getLon(baseNode))) {
                nodesInBBox.add(baseNode);
            }
            if (!nodesInBBox.contains(adjNode) && !excludeNodes.contains(adjNode) && bbox.contains(nodeAccess.getLat(adjNode), nodeAccess.getLon(adjNode))) {
                nodesInBBox.add(adjNode);
            }

        });

        if (nodesInBBox.isEmpty()) {
            // without nodes, no centrality can be calculated
            res.setWarning(new CentralityWarning(CentralityWarning.EMPTY_BBOX));
            return res;
        }

        CentralityAlgorithm alg = new BrandesCentralityAlgorithm();
        alg.init(graph, weighting, explorer);

        // transform node ids to coordinates,
        for (int v : nodesInBBox) {
            Coordinate coord = new Coordinate(nodeAccess.getLon(v), nodeAccess.getLat(v));
            res.addLocation(v, coord);
        }

        if (req.getMode().equals("nodes")) {
            Map<Integer, Double> nodeBetweenness = alg.computeNodeCentrality(nodesInBBox);
            res.setNodeCentralityScores(nodeBetweenness);
        } else {
            Map<Pair<Integer, Integer>, Double> edgeBetweenness = alg.computeEdgeCentrality(nodesInBBox);
            res.setEdgeCentralityScores(edgeBetweenness);
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

        // PARAMETERS FOR EdgeFilterFactory

        /* Avoid areas */
        if (searchParams.hasAvoidAreas()) {
            props.putObject("avoid_areas", true);
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
            props.putObject("avoid_features", searchParams.getAvoidFeatureTypes());
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
            props.putObject(KEY_CUSTOM_WEIGHTINGS, true);
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

        String profileName = encoderName + "_" + WeightingMethod.getName(searchParams.getWeightingMethod());
        RouteSearchContext searchCntx = new RouteSearchContext(mGraphHopper, flagEncoder, profileName);
        searchCntx.setProperties(props);

        return searchCntx;
    }

    public RouteSegmentInfo[] getMatchedSegments(Coordinate[] locations, double searchRadius, boolean bothDirections)
            throws Exception {
        RouteSegmentInfo[] rsi;

        waitForUpdateCompletion();

        beginUseGH();

        try {
            rsi = getMatchedSegmentsInternal(locations, searchRadius, null, bothDirections);

            endUseGH();
        } catch (Exception ex) {
            endUseGH();

            throw ex;
        }

        return rsi;
    }

    private RouteSegmentInfo[] getMatchedSegmentsInternal(Coordinate[] locations, double searchRadius, EdgeFilter edgeFilter, boolean bothDirections) {
        if (mMapMatcher == null) {
            mMapMatcher = new HiddenMarkovMapMatcher();
            mMapMatcher.setGraphHopper(mGraphHopper);
        }

        mMapMatcher.setSearchRadius(searchRadius);
        mMapMatcher.setEdgeFilter(edgeFilter);

        return mMapMatcher.match(locations, bothDirections);
    }

    public GHResponse computeRoundTripRoute(double lat0, double lon0, WayPointBearing
            bearing, RouteSearchParameters searchParams, Boolean geometrySimplify) throws Exception {
        GHResponse resp;

        waitForUpdateCompletion();

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
                setWeightingMethod(req.getHints(), weightingMethod, profileType, false);
            else
                throw new IllegalArgumentException("Unsupported weighting " + weightingMethod + " for profile + " + profileType);

            //Roundtrip not possible with preprocessed edges.
            setSpeedups(req, false, false, true);

            if (astarEpsilon != null)
                req.getHints().putObject("astarbi.epsilon", astarEpsilon);
            if (astarApproximation != null)
                req.getHints().putObject("astarbi.approximation", astarApproximation);
            //Overwrite algorithm selected in setSpeedups
            req.setAlgorithm(Parameters.Algorithms.ROUND_TRIP);

            mGraphHopper.setSimplifyResponse(geometrySimplify);
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

        waitForUpdateCompletion();

        beginUseGH();

        try {
            int profileType = searchParams.getProfileType();
            int weightingMethod = searchParams.getWeightingMethod();
            RouteSearchContext searchCntx = createSearchContext(searchParams);

            int flexibleMode = searchParams.getFlexibleMode() ? KEY_FLEX_PREPROCESSED : KEY_FLEX_STATIC;
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
                setWeightingMethod(req.getHints(), weightingMethod, profileType, hasTimeDependentSpeed(searchParams, searchCntx));
                if (requiresTimeDependentWeighting(searchParams, searchCntx))
                    flexibleMode = KEY_FLEX_PREPROCESSED;
                flexibleMode = getFlexibilityMode(flexibleMode, searchParams, profileType);
            } else
                throw new IllegalArgumentException("Unsupported weighting " + weightingMethod + " for profile + " + profileType);

            if (flexibleMode == KEY_FLEX_STATIC)
                //Speedup order: useCH, useCore, useALT
                setSpeedups(req, true, true, true);

            if (flexibleMode == KEY_FLEX_PREPROCESSED) {
                setSpeedups(req, false, optimized, true);
            }

            //cannot use CH or CoreALT with requests where the weighting of non-predefined edges might change
            if (flexibleMode == KEY_FLEX_FULLY)
                setSpeedups(req, false, false, true);

            if (searchParams.isTimeDependent()) {
                if (searchParams.hasDeparture())
                    req.getHints().putObject(RouteRequest.PARAM_DEPARTURE, searchParams.getDeparture());
                else if (searchParams.hasArrival())
                    req.getHints().putObject(RouteRequest.PARAM_ARRIVAL, searchParams.getArrival());
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
                mGraphHopper.setSimplifyResponse(geometrySimplify);
                resp = mGraphHopper.route(req);
            }
            if (DebugUtility.isDebug() && !directedSegment) {
                LOGGER.info("visited_nodes.average - " + resp.getHints().getString("visited_nodes.average", ""));
            }
            if (DebugUtility.isDebug() && directedSegment) {
                LOGGER.info("skipped segment - " + resp.getHints().getString("skipped_segment", ""));
            }
            endUseGH();
        } catch (Exception ex) {
            endUseGH();

            LOGGER.error(ex);

            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Unable to compute a route");
        }

        return resp;
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
            flexibleMode = KEY_FLEX_PREPROCESSED;

        if (searchParams.requiresFullyDynamicWeights())
            flexibleMode = KEY_FLEX_FULLY;
        //If we have special weightings, we have to fall back to ALT with Beeline
        ProfileParameters profileParams = searchParams.getProfileParameters();
        if (profileParams != null && profileParams.hasWeightings())
            flexibleMode = KEY_FLEX_FULLY;

        return flexibleMode;
    }

    /**
     * Set the weightingMethod for the request based on input weighting.
     *
     * @param map              Hints map for setting up the request
     * @param requestWeighting Originally requested weighting
     * @param profileType      Necessary for HGV
     */
    private void setWeightingMethod(PMap map, int requestWeighting, int profileType, boolean hasTimeDependentSpeed) {
        //Defaults
        String weightingMethod = VAL_RECOMMENDED;

        if (requestWeighting == WeightingMethod.SHORTEST)
            weightingMethod = VAL_SHORTEST;

        //For a requested recommended weighting, use recommended for bike, walking and hgv. Use fastest for car.
        if (requestWeighting == WeightingMethod.RECOMMENDED || requestWeighting == WeightingMethod.FASTEST) {
            if (profileType == RoutingProfileType.DRIVING_CAR) {
                weightingMethod = VAL_FASTEST;
            }
            if (RoutingProfileType.isHeavyVehicle(profileType) || RoutingProfileType.isCycling(profileType) || RoutingProfileType.isWalking(profileType)) {
                weightingMethod = VAL_RECOMMENDED;
            }
        }

        map.putObject(KEY_WEIGHTING_METHOD, weightingMethod);

        if (hasTimeDependentSpeed)
            map.putObject(ORSParameters.Weighting.TIME_DEPENDENT_SPEED, true);
    }

    /**
     * Set the weighting for the request based on input weighting.
     *
     * @param map              Hints map for setting up the request
     * @param requestWeighting Originally requested weighting
     * @param profileType      Necessary for HGV
     */
    private void setWeighting(PMap map, int requestWeighting, int profileType, boolean hasTimeDependentSpeed) {
        //Defaults
        String weighting = VAL_RECOMMENDED;

        if (requestWeighting == WeightingMethod.SHORTEST)
            weighting = VAL_SHORTEST;

        //For a requested recommended weighting, use recommended for bike, walking and hgv. Use fastest for car.
        if (requestWeighting == WeightingMethod.RECOMMENDED || requestWeighting == WeightingMethod.FASTEST) {
            if (profileType == RoutingProfileType.DRIVING_CAR) {
                weighting = VAL_FASTEST;
            }
            if (RoutingProfileType.isHeavyVehicle(profileType) || RoutingProfileType.isCycling(profileType) || RoutingProfileType.isWalking(profileType)) {
                weighting = VAL_RECOMMENDED;
            }
        }

        map.putObject(KEY_WEIGHTING, weighting);

        if (hasTimeDependentSpeed)
            map.putObject(ORSParameters.Weighting.TIME_DEPENDENT_SPEED, true);
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
    private void setSpeedups(GHRequest req, boolean useCH, boolean useCore, boolean useALT) {
        String weighting = mGraphHopper.getProfile(req.getProfile()).getWeighting();

        //Priority: CH->Core->ALT
        useCH = useCH && mGraphHopper.isCHAvailable(weighting);
        useCore = useCore && !useCH && mGraphHopper.isCoreAvailable(weighting);
        useALT = useALT && !useCH && !useCore && mGraphHopper.isLMAvailable(weighting);

        req.getHints().putObject(KEY_CH_DISABLE, !useCH);
        req.getHints().putObject(KEY_CORE_DISABLE, !useCore);
        req.getHints().putObject(KEY_LM_DISABLE, !useALT);

        if (useCH)
            req.setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
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
                || flagEncoder.hasEncodedValue(EncodingManager.getKey(flagEncoder, ConditionalEdges.SPEED));
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
        waitForUpdateCompletion();
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
            if (parameters.hasAttribute(KEY_TOTAL_POP)) {
                try {
                    Map<StatisticsProviderConfiguration, List<String>> mapProviderToAttrs = new HashMap<>();
                    StatisticsProviderConfiguration provConfig = IsochronesServiceSettings.getStatsProviders().get(KEY_TOTAL_POP);
                    if (provConfig != null) {
                        List<String> attrList = new ArrayList<>();
                        attrList.add(KEY_TOTAL_POP);
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

    public Weighting createTurnWeighting(Graph graph, Weighting weighting, TraversalMode tMode, double uTurnCosts) {
        // TODO: clarify whether this is still needed, as the weightings know their turn costs now
//        if (!(weighting instanceof TurnWeighting)) {
//            FlagEncoder encoder = weighting.getFlagEncoder();
//            if (encoder.supports(TurnWeighting.class) && tMode.isEdgeBased()) {
//                return new TurnWeighting(weighting, HelperORS.getTurnCostExtensions(graph.getExtension()), uTurnCosts);
//            }
//        }

        return weighting;
    }

    public boolean equals(Object o) {
        return o != null && o.getClass().equals(RoutingProfile.class) && this.hashCode() == o.hashCode();
    }

    public int hashCode() {
        return mGraphHopper.getGraphHopperStorage().getDirectory().getLocation().hashCode();
    }
}
