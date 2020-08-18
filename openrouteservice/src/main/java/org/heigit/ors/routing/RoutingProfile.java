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
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.StorableProperties;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import com.typesafe.config.Config;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.isochrones.*;
import org.heigit.ors.isochrones.statistics.StatisticsProvider;
import org.heigit.ors.isochrones.statistics.StatisticsProviderConfiguration;
import org.heigit.ors.isochrones.statistics.StatisticsProviderFactory;
import org.heigit.ors.mapmatching.MapMatcher;
import org.heigit.ors.mapmatching.RouteSegmentInfo;
import org.heigit.ors.mapmatching.hmm.HiddenMarkovMapMatcher;
import org.heigit.ors.matrix.*;
import org.heigit.ors.matrix.algorithms.MatrixAlgorithm;
import org.heigit.ors.matrix.algorithms.MatrixAlgorithmFactory;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.heigit.ors.routing.graphhopper.extensions.*;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.BordersGraphStorageBuilder;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSPMap;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters;
import org.heigit.ors.routing.parameters.ProfileParameters;
import org.heigit.ors.routing.parameters.VehicleParameters;
import org.heigit.ors.routing.parameters.WheelchairParameters;
import org.heigit.ors.routing.pathprocessors.ORSPathProcessorFactory;
import org.heigit.ors.services.isochrones.IsochronesServiceSettings;
import org.heigit.ors.services.matrix.MatrixServiceSettings;
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
 * {@link org.heigit.ors.services.isochrones.requestprocessors.json.JsonIsochronesRequestProcessor}
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
    private static final String VAL_RECOMMENDED_PREF = "recommended_pref";
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
    private static final String VAL_ENABLED = "enabled";
    private static final String KEY_THREADS = "threads";
    private static final String KEY_WEIGHTINGS = "weightings";
    private static final String KEY_MAXCELLNODES = "maxcellnodes";
    private static final String KEY_METHODS_LM = "methods.lm";
    private static final String KEY_LANDMARKS = "landmarks";
    private static final String KEY_METHODS_CORE = "methods.core";
    private static final String KEY_METHODS_FASTISOCHRONE = "methods.fastisochrone";
    private static final String KEY_DISABLING_ALLOWED = "disabling_allowed";
    private static final String KEY_ACTIVE_LANDMARKS = "active_landmarks";
    private static final String KEY_TOTAL_POP = "total_pop";
    private static final String KEY_TOTAL_AREA_KM = "total_area_km";
    private static final String KEY_ASTARBI = "astarbi";
    private static final int KEY_FLEX_STATIC = 0;
    private static final int KEY_FLEX_PREPROCESSED = 1;
    private static final int KEY_FLEX_FULLY = 2;
    private static int profileIdentifier = 0;
    private static final Object lockObj = new Object();

    private ORSGraphHopper mGraphHopper;
    private Integer[] mRoutePrefs;
    private Integer mUseCounter;
    private boolean mUpdateRun;
    private MapMatcher mMapMatcher;

    private RouteProfileConfiguration config;
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
        CmdArgs args = createGHSettings(osmFile, config);

        int profileId = 0;
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
        gh.setEdgeFilterFactory(edgeFilterFactory);

        ORSPathProcessorFactory pathProcessorFactory = new ORSPathProcessorFactory();
        gh.setPathProcessorFactory(pathProcessorFactory);

        gh.init(args);

        // MARQ24: make sure that we only use ONE instance of the ElevationProvider across the multiple vehicle profiles
        // so the caching for elevation data will/can be reused across different vehicles. [the loadCntx is a single
        // Object that will shared across the (potential) multiple running instances]
        if(loadCntx.getElevationProvider() != null) {
            gh.setElevationProvider(loadCntx.getElevationProvider());
        }else {
            loadCntx.setElevationProvider(gh.getElevationProvider());
        }
        gh.setGraphStorageFactory(new ORSGraphStorageFactory(gpc.getStorageBuilders()));
        gh.setWeightingFactory(new ORSWeightingFactory());

        gh.importOrLoad();

        // store CountryBordersReader for later use
        for (GraphStorageBuilder builder : gpc.getStorageBuilders()) {
            if (builder.getName().equals(BordersGraphStorageBuilder.BUILDER_NAME)) {
                pathProcessorFactory.setCountryBordersReader(((BordersGraphStorageBuilder) builder).getCbReader());
            }
        }

        if (LOGGER.isInfoEnabled()) {
            EncodingManager encodingMgr = gh.getEncodingManager();
            GraphHopperStorage ghStorage = gh.getGraphHopperStorage();
            // MARQ24 MOD START
            // Same here as for the 'gh.getCapacity()' below - the 'encodingMgr.getUsedBitsForFlags()' method requires
            // the EncodingManager to be patched - and this is ONLY required for this logging line... which is IMHO
            // not worth it (and since we are not sharing FlagEncoders for mutiple vehicles this info is anyhow
            // obsolete
            LOGGER.info(String.format("[%d] FlagEncoders: %s, bits used [UNKNOWN]/%d.", profileId, encodingMgr.fetchEdgeEncoders().size(), encodingMgr.getBytesForFlags() * 8));
            // the 'getCapacity()' impl is the root cause of having a copy of the gh 'com.graphhopper.routing.lm.PrepareLandmarks'
            // class (to make the store) accessible (getLandmarkStorage()) - IMHO this is not worth it!
            // so gh.getCapacity() will be removed!
            LOGGER.info(String.format("[%d] Capacity: [UNKNOWN]. (edges - %s, nodes - %s)", profileId, ghStorage.getEdges(), ghStorage.getNodes()));
            // MARQ24 MOD END
            LOGGER.info(String.format("[%d] Total time: %s.", profileId, TimeUtility.getElapsedTime(startTime, true)));
            LOGGER.info(String.format("[%d] Finished at: %s.", profileId, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
            LOGGER.info("                              ");
        }

        // Make a stamp which help tracking any changes in the size of OSM file.
        File file = new File(osmFile);
        Path pathTimestamp = Paths.get(config.getGraphPath(), "stamp.txt");
        File file2 = pathTimestamp.toFile();
        if (!file2.exists())
            Files.write(pathTimestamp, Long.toString(file.length()).getBytes());

        return gh;
    }

    public long getCapacity() {
        GraphHopperStorage graph = mGraphHopper.getGraphHopperStorage();
        return graph.getCapacity() + GraphStorageUtils.getCapacity(graph.getExtension());
    }

    private static CmdArgs createGHSettings(String sourceFile, RouteProfileConfiguration config) {
        CmdArgs args = new CmdArgs();
        args.put("graph.dataaccess", "RAM_STORE");
        args.put("datareader.file", sourceFile);
        args.put("graph.location", config.getGraphPath());
        args.put("graph.bytes_for_flags", config.getEncoderFlagsSize());

        if (!config.getInstructions())
            args.put("instructions", false);
        if (config.getElevationProvider() != null && config.getElevationCachePath() != null) {
            args.put("graph.elevation.provider", StringUtility.trimQuotes(config.getElevationProvider()));
            args.put("graph.elevation.cache_dir", StringUtility.trimQuotes(config.getElevationCachePath()));
            args.put("graph.elevation.dataaccess", StringUtility.trimQuotes(config.getElevationDataAccess()));
            args.put("graph.elevation.clear", config.getElevationCacheClear());
            if (config.getInterpolateBridgesAndTunnels())
                args.put("graph.encoded_values", "road_environment");
            if (config.getElevationSmoothing())
                args.put("graph.elevation.smoothing", true);
        }

        boolean prepareCH = false;
        boolean prepareLM = false;
        boolean prepareCore = false;
        boolean prepareFI= false;

        args.put(KEY_PREPARE_CH_WEIGHTINGS, "no");
        args.put(KEY_PREPARE_LM_WEIGHTINGS, "no");
        args.put(KEY_PREPARE_CORE_WEIGHTINGS, "no");

        if (config.getIsochronePreparationOpts() != null) {
            Config fastisochroneOpts = config.getIsochronePreparationOpts();
            prepareFI = true;
            if (fastisochroneOpts.hasPath(VAL_ENABLED) || fastisochroneOpts.getBoolean(VAL_ENABLED)) {
                prepareFI = fastisochroneOpts.getBoolean(VAL_ENABLED);
                if (!prepareFI)
                    args.put(KEY_PREPARE_FASTISOCHRONE_WEIGHTINGS, "no");
                else
                    args.put(ORSParameters.FastIsochrone.PROFILE, config.getProfiles());
            }

            if (prepareFI) {
                if (fastisochroneOpts.hasPath(KEY_THREADS))
                    args.put("prepare.fastisochrone.threads", fastisochroneOpts.getInt(KEY_THREADS));
                if (fastisochroneOpts.hasPath(KEY_WEIGHTINGS))
                    args.put(KEY_PREPARE_FASTISOCHRONE_WEIGHTINGS, StringUtility.trimQuotes(fastisochroneOpts.getString(KEY_WEIGHTINGS)));
                if (fastisochroneOpts.hasPath(KEY_MAXCELLNODES))
                    args.put("prepare.fastisochrone.maxcellnodes", StringUtility.trimQuotes(fastisochroneOpts.getString(KEY_MAXCELLNODES)));
            }
        }

        if (config.getPreparationOpts() != null) {
            Config opts = config.getPreparationOpts();
            if (opts.hasPath("min_network_size"))
                args.put("prepare.min_network_size", opts.getInt("min_network_size"));
            if (opts.hasPath("min_one_way_network_size"))
                args.put("prepare.min_one_way_network_size", opts.getInt("min_one_way_network_size"));

            if (opts.hasPath("methods")) {
                if (opts.hasPath(KEY_METHODS_CH)) {
                    prepareCH = true;
                    Config chOpts = opts.getConfig(KEY_METHODS_CH);

                    if (chOpts.hasPath(VAL_ENABLED) || chOpts.getBoolean(VAL_ENABLED)) {
                        prepareCH = chOpts.getBoolean(VAL_ENABLED);
                        if (!prepareCH)
                            args.put(KEY_PREPARE_CH_WEIGHTINGS, "no");
                    }

                    if (prepareCH) {
                        if (chOpts.hasPath(KEY_THREADS))
                            args.put("prepare.ch.threads", chOpts.getInt(KEY_THREADS));
                        if (chOpts.hasPath(KEY_WEIGHTINGS))
                            args.put(KEY_PREPARE_CH_WEIGHTINGS, StringUtility.trimQuotes(chOpts.getString(KEY_WEIGHTINGS)));
                    }
                }

                if (opts.hasPath(KEY_METHODS_LM)) {
                    prepareLM = true;
                    Config lmOpts = opts.getConfig(KEY_METHODS_LM);

                    if (lmOpts.hasPath(VAL_ENABLED) || lmOpts.getBoolean(VAL_ENABLED)) {
                        prepareLM = lmOpts.getBoolean(VAL_ENABLED);
                        if (!prepareLM)
                            args.put(KEY_PREPARE_LM_WEIGHTINGS, "no");
                    }

                    if (prepareLM) {
                        if (lmOpts.hasPath(KEY_THREADS))
                            args.put("prepare.lm.threads", lmOpts.getInt(KEY_THREADS));
                        if (lmOpts.hasPath(KEY_WEIGHTINGS))
                            args.put(KEY_PREPARE_LM_WEIGHTINGS, StringUtility.trimQuotes(lmOpts.getString(KEY_WEIGHTINGS)));
                        if (lmOpts.hasPath(KEY_LANDMARKS))
                            args.put("prepare.lm.landmarks", lmOpts.getInt(KEY_LANDMARKS));
                    }
                }

                if (opts.hasPath(KEY_METHODS_CORE)) {
                    prepareCore = true;
                    Config coreOpts = opts.getConfig(KEY_METHODS_CORE);

                    if (coreOpts.hasPath(VAL_ENABLED) || coreOpts.getBoolean(VAL_ENABLED)) {
                        prepareCore = coreOpts.getBoolean(VAL_ENABLED);
                        if (!prepareCore)
                            args.put(KEY_PREPARE_CORE_WEIGHTINGS, "no");
                    }

                    if (prepareCore) {
                        if (coreOpts.hasPath(KEY_THREADS))
                            args.put("prepare.core.threads", coreOpts.getInt(KEY_THREADS));
                        if (coreOpts.hasPath(KEY_WEIGHTINGS))
                            args.put(KEY_PREPARE_CORE_WEIGHTINGS, StringUtility.trimQuotes(coreOpts.getString(KEY_WEIGHTINGS)));
                        if (coreOpts.hasPath("lmsets"))
                            args.put("prepare.corelm.lmsets", StringUtility.trimQuotes(coreOpts.getString("lmsets")));
                        if (coreOpts.hasPath(KEY_LANDMARKS))
                            args.put("prepare.corelm.landmarks", coreOpts.getInt(KEY_LANDMARKS));
                    }
                }
            }
        }

        if (config.getExecutionOpts() != null) {
            Config opts = config.getExecutionOpts();
            if (opts.hasPath(KEY_METHODS_CH)) {
                Config coreOpts = opts.getConfig(KEY_METHODS_CH);
                if (coreOpts.hasPath(KEY_DISABLING_ALLOWED))
                    args.put("routing.ch.disabling_allowed", coreOpts.getBoolean(KEY_DISABLING_ALLOWED));
            }
            if (opts.hasPath(KEY_METHODS_CORE)) {
                Config chOpts = opts.getConfig(KEY_METHODS_CORE);
                if (chOpts.hasPath(KEY_DISABLING_ALLOWED))
                    args.put("routing.core.disabling_allowed", chOpts.getBoolean(KEY_DISABLING_ALLOWED));
            }
            if (opts.hasPath(KEY_METHODS_LM)) {
                Config lmOpts = opts.getConfig(KEY_METHODS_LM);
                if (lmOpts.hasPath(KEY_DISABLING_ALLOWED))
                    args.put("routing.lm.disabling_allowed", lmOpts.getBoolean(KEY_DISABLING_ALLOWED));

                if (lmOpts.hasPath(KEY_ACTIVE_LANDMARKS))
                    args.put("routing.lm.active_landmarks", lmOpts.getInt(KEY_ACTIVE_LANDMARKS));
            }
            if (opts.hasPath("methods.corelm")) {
                Config lmOpts = opts.getConfig("methods.corelm");
                if (lmOpts.hasPath(KEY_DISABLING_ALLOWED))
                    args.put("routing.lm.disabling_allowed", lmOpts.getBoolean(KEY_DISABLING_ALLOWED));

                if (lmOpts.hasPath(KEY_ACTIVE_LANDMARKS))
                    args.put("routing.corelm.active_landmarks", lmOpts.getInt(KEY_ACTIVE_LANDMARKS));
            }
        }

        if (config.getOptimize() && !prepareCH)
            args.put("graph.do_sort", true);

        StringBuilder flagEncoders = new StringBuilder();
        String[] encoderOpts = !Helper.isEmpty(config.getEncoderOptions()) ? config.getEncoderOptions().split(",") : null;
        Integer[] profiles = config.getProfilesTypes();

        for (int i = 0; i < profiles.length; i++) {
            if (encoderOpts == null)
                flagEncoders.append(RoutingProfileType.getEncoderName(profiles[i]));
            else
                flagEncoders.append(RoutingProfileType.getEncoderName(profiles[i]) + "|" + encoderOpts[i]);
            if (i < profiles.length - 1)
                flagEncoders.append(",");
        }

        args.put("graph.flag_encoders", flagEncoders.toString().toLowerCase());

        args.put("index.high_resolution", config.getLocationIndexResolution());
        args.put("index.max_region_search", config.getLocationIndexSearchIterations());

        return args;
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

    public String getGraphLocation() {
        return mGraphHopper == null ? null : mGraphHopper.getGraphHopperStorage().getDirectory().toString();
    }

    public RouteProfileConfiguration getConfiguration() {
        return config;
    }

    public Integer[] getPreferences() {
        return mRoutePrefs;
    }

    public boolean hasCarPreferences() {
        for (int i = 0; i < mRoutePrefs.length; i++) {
            if (RoutingProfileType.isDriving(mRoutePrefs[i]))
                return true;
        }

        return false;
    }


    public boolean isCHEnabled() {
        return mGraphHopper != null && mGraphHopper.isCHEnabled();
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

                    mGraphHopper = initGraphHopper(ghOld.getDataReaderFile(), config, loadCntx);

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

    private static boolean supportWeightingMethod(int profileType) {
        return RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType) || RoutingProfileType.isPedestrian(profileType);
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


        IsochroneMap result = null;
        waitForUpdateCompletion();

        beginUseGH();

        try {
            RouteSearchContext searchCntx = createSearchContext(parameters.getRouteParameters());

            IsochroneMapBuilderFactory isochroneMapBuilderFactory = new IsochroneMapBuilderFactory(searchCntx);
            result = isochroneMapBuilderFactory.buildMap(parameters);

            endUseGH();
        } catch (Exception ex) {
            endUseGH();

            LOGGER.error(ex);

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
                LOGGER.error(ex);

                throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "Unable to compute isochrone attributes.");
            }
        }

        return result;
    }

    public MatrixResult computeMatrix(MatrixRequest req) throws Exception {
        MatrixResult mtxResult = null;

        GraphHopper gh = getGraphhopper();
        String encoderName = RoutingProfileType.getEncoderName(req.getProfileType());
        FlagEncoder flagEncoder = gh.getEncodingManager().getEncoder(encoderName);

        MatrixAlgorithm alg = MatrixAlgorithmFactory.createAlgorithm(req, gh);

        if (alg == null)
            throw new Exception("Unable to create an algorithm to for computing distance/duration matrix.");

        try {
            HintsMap hintsMap = new HintsMap();
            int weightingMethod = req.getWeightingMethod() == WeightingMethod.UNKNOWN ? WeightingMethod.RECOMMENDED : req.getWeightingMethod();
            setWeighting(hintsMap, weightingMethod, req.getProfileType());
            Graph graph = null;
            if (!req.getFlexibleMode() && gh.getCHFactoryDecorator().isEnabled() && gh.getCHFactoryDecorator().getCHProfileStrings().contains(hintsMap.getWeighting())) {
                hintsMap.setVehicle(encoderName);
                graph = gh.getGraphHopperStorage().getCHGraph(((PrepareContractionHierarchies) gh.getAlgorithmFactory(hintsMap)).getCHProfile());
            }
            else
                graph = gh.getGraphHopperStorage().getBaseGraph();

            MatrixSearchContextBuilder builder = new MatrixSearchContextBuilder(gh.getLocationIndex(), DefaultEdgeFilter.allEdges(flagEncoder), req.getResolveLocations());
            MatrixSearchContext mtxSearchCntx = builder.create(graph, req.getSources(), req.getDestinations(), MatrixServiceSettings.getMaximumSearchRadius());

            Weighting weighting = new ORSWeightingFactory().createWeighting(hintsMap, flagEncoder, gh.getGraphHopperStorage());

            alg.init(req, gh, mtxSearchCntx.getGraph(), flagEncoder, weighting);

            mtxResult = alg.compute(mtxSearchCntx.getSources(), mtxSearchCntx.getDestinations(), req.getMetrics());
        } catch (StatusCodeException ex) {
            LOGGER.error(ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error(ex);
            throw new InternalServerException(MatrixErrorCodes.UNKNOWN, "Unable to compute a distance/duration matrix.");
        }

        return mtxResult;
    }

    private RouteSearchContext createSearchContext(RouteSearchParameters searchParams) throws Exception {
        ORSPMap props = new ORSPMap();

        int profileType = searchParams.getProfileType();
        String encoderName = RoutingProfileType.getEncoderName(profileType);

        if ("UNKNOWN".equals(encoderName))
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "unknown vehicle profile.");

        if (!mGraphHopper.getEncodingManager().hasEncoder(encoderName)) {
            throw new IllegalArgumentException("Vehicle " + encoderName + " unsupported. " + "Supported are: "
                    + mGraphHopper.getEncodingManager());
        }

        FlagEncoder flagEncoder = mGraphHopper.getEncodingManager().getEncoder(encoderName);
        ProfileParameters profileParams = searchParams.getProfileParameters();

        /*
         * PARAMETERS FOR PathProcessorFactory
         * ======================================================================================================
         */

        props.put("routing_extra_info", searchParams.getExtraInfo());
        props.put("routing_suppress_warnings", searchParams.getSuppressWarnings());

        props.put("routing_profile_type", profileType);
        props.putObj("routing_profile_params", profileParams);

        /*
        * PARAMETERS FOR EdgeFilterFactory
        * ======================================================================================================
        */

        /* Avoid areas */
        if (searchParams.hasAvoidAreas()) {
            props.put("avoid_areas", true);
            props.putObj("avoid_areas", searchParams.getAvoidAreas());
        }

        /* Heavy vehicle filter */
        if (profileType == RoutingProfileType.DRIVING_HGV
            && searchParams.hasParameters(VehicleParameters.class)
            && ((VehicleParameters)profileParams).hasAttributes()
        ) {
            props.put("edgefilter_hgv", searchParams.getVehicleType());
        }

        /* Wheelchair filter */
        else if (profileType == RoutingProfileType.WHEELCHAIR
            && searchParams.hasParameters(WheelchairParameters.class)) {
            props.put("edgefilter_wheelchair", "true");
        }

        /* Avoid features */
        if (searchParams.hasAvoidFeatures()) {
            props.put("avoid_features", searchParams.getAvoidFeatureTypes());
            props.putObj("avoid_features", searchParams);
        }

        /* Avoid borders of some form */
        if ((searchParams.hasAvoidBorders() || searchParams.hasAvoidCountries())
            && (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType))) {
            props.putObj("avoid_borders", searchParams);
            if(searchParams.hasAvoidCountries())
                props.put("avoid_countries", Arrays.toString(searchParams.getAvoidCountries()));
        }

        if (profileParams != null && profileParams.hasWeightings()) {
            props.put(KEY_CUSTOM_WEIGHTINGS, true);
            Iterator<ProfileWeighting> iterator = profileParams.getWeightings().getIterator();
            while (iterator.hasNext()) {
                ProfileWeighting weighting = iterator.next();
                if (!weighting.getParameters().isEmpty()) {
                    String name = ProfileWeighting.encodeName(weighting.getName());
                    for (Map.Entry<String, String> kv : weighting.getParameters().toMap().entrySet())
                        props.put(name + kv.getKey(), kv.getValue());
                }
            }
        }

        RouteSearchContext searchCntx = new RouteSearchContext(mGraphHopper, flagEncoder);
        searchCntx.setProperties(props);

        return searchCntx;
    }

    public RouteSegmentInfo[] getMatchedSegments(Coordinate[] locations, double searchRadius, boolean bothDirections)
            throws Exception {
        RouteSegmentInfo[] rsi = null;

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

    private RouteSegmentInfo[] getMatchedSegmentsInternal(Coordinate[] locations,
                                                          double searchRadius, EdgeFilter edgeFilter, boolean bothDirections) {
        if (mMapMatcher == null) {
            mMapMatcher = new HiddenMarkovMapMatcher();
            mMapMatcher.setGraphHopper(mGraphHopper);
        }

        mMapMatcher.setSearchRadius(searchRadius);
        mMapMatcher.setEdgeFilter(edgeFilter);

        return mMapMatcher.match(locations, bothDirections);
    }

    public GHResponse computeRoundTripRoute(double lat0, double lon0, WayPointBearing bearing, RouteSearchParameters searchParams, Boolean geometrySimplify) throws Exception {
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

            req.setVehicle(searchCntx.getEncoder().toString());
            req.getHints().put(Parameters.Algorithms.RoundTrip.DISTANCE, searchParams.getRoundTripLength());
            req.getHints().put(Parameters.Algorithms.RoundTrip.POINTS, searchParams.getRoundTripPoints());

            if (searchParams.getRoundTripSeed() > -1) {
               req.getHints().put(Parameters.Algorithms.RoundTrip.SEED, searchParams.getRoundTripSeed());
            }

            PMap props = searchCntx.getProperties();
            req.setAdditionalHints(props);

            if (props != null && !props.isEmpty())
                req.getHints().merge(props);

            if (supportWeightingMethod(profileType))
                setWeighting(req.getHints(), weightingMethod, profileType);
            else
                throw new IllegalArgumentException("Unsupported weighting " + weightingMethod + " for profile + " + profileType);

            //Roundtrip not possible with preprocessed edges.
            setSpeedups(req, false, false, true);

            if (astarEpsilon != null)
                req.getHints().put("astarbi.epsilon", astarEpsilon);
            if (astarApproximation != null)
                req.getHints().put("astarbi.approximation", astarApproximation);
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

    public GHResponse computeRoute(double lat0, double lon0, double lat1, double lon1, WayPointBearing[] bearings, double[] radiuses, boolean directedSegment, RouteSearchParameters searchParams, Boolean geometrySimplify)
            throws Exception {

        GHResponse resp = null;

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

            req.setVehicle(searchCntx.getEncoder().toString());
            req.setAlgorithm("dijkstrabi");

            if (radiuses != null)
                req.setMaxSearchDistance(radiuses);

            PMap props = searchCntx.getProperties();
            req.setAdditionalHints(props);

            if (props != null && !props.isEmpty())
                req.getHints().merge(props);

            if (supportWeightingMethod(profileType)) {
                setWeighting(req.getHints(), weightingMethod, profileType);
                flexibleMode = getFlexibilityMode(flexibleMode, searchParams, profileType);
            }
            else
                throw new IllegalArgumentException("Unsupported weighting " + weightingMethod + " for profile + " + profileType);

            if(flexibleMode == KEY_FLEX_STATIC)
                //Speedup order: useCH, useCore, useALT
                setSpeedups(req, true, true, true);

            if (flexibleMode == KEY_FLEX_PREPROCESSED) {
                if(optimized)
                    setSpeedups(req, false, true, true);
                else
                    setSpeedups(req, false, false, true);
            }

            //cannot use CH or CoreALT with requests where the weighting of non-predefined edges might change
            if(flexibleMode == KEY_FLEX_FULLY)
                setSpeedups(req, false, false, true);

            if (astarEpsilon != null)
                req.getHints().put("astarbi.epsilon", astarEpsilon);
            if (astarApproximation != null)
                req.getHints().put("astarbi.approximation", astarApproximation);

            if (searchParams.getAlternativeRoutesCount() > 0) {
                //TAKB: CH and CORE have to be disabled for alternative routes
                setSpeedups(req, false, false, true);
                req.setAlgorithm("alternative_route");
                req.getHints().put("alternative_route.max_paths", searchParams.getAlternativeRoutesCount());
                req.getHints().put("alternative_route.max_weight_factor", searchParams.getAlternativeRoutesWeightFactor());
                req.getHints().put("alternative_route.max_share_factor", searchParams.getAlternativeRoutesShareFactor());
            }

            if(searchParams.hasMaximumSpeed()){
                req.getHints().put("maximum_speed", searchParams.getMaximumSpeed());
            }

            if (directedSegment) {
                resp = mGraphHopper.constructFreeHandRoute(req);
            } else {
                mGraphHopper.setSimplifyResponse(geometrySimplify);
                resp = mGraphHopper.route(req);
            }
            if (DebugUtility.isDebug() && !directedSegment) {
                LOGGER.info("visited_nodes.average - " + resp.getHints().get("visited_nodes.average", ""));
            }
            if (DebugUtility.isDebug() && directedSegment) {
                LOGGER.info("skipped segment - " + resp.getHints().get("skipped_segment", ""));
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
     * @param profileType Necessary for HGV
     * @return flexibility as int
     */
    private int getFlexibilityMode(int flexibleMode, RouteSearchParameters searchParams, int profileType){
        if(searchParams.requiresDynamicPreprocessedWeights())
            flexibleMode = KEY_FLEX_PREPROCESSED;
        if(profileType == RoutingProfileType.WHEELCHAIR)
            flexibleMode = KEY_FLEX_PREPROCESSED;

        if(searchParams.requiresFullyDynamicWeights())
            flexibleMode = KEY_FLEX_FULLY;
        //If we have special weightings, we have to fall back to ALT with Beeline
        ProfileParameters profileParams = searchParams.getProfileParameters();
        if (profileParams != null && profileParams.hasWeightings())
            flexibleMode = KEY_FLEX_FULLY;

        return flexibleMode;
    }

    /**
     * Set the weighting for the request based on input weighting.
     * Also set the weighting_method.
     *
     * @param map Hints map for setting up the request
     * @param requestWeighting Originally requested weighting
     * @param profileType Necessary for HGV
     * @return Weighting as int
     */
    private void setWeighting(HintsMap map, int requestWeighting, int profileType){
        //Defaults
        String weighting = VAL_RECOMMENDED;
        String weightingMethod = VAL_RECOMMENDED;

        if(requestWeighting == WeightingMethod.SHORTEST)
            weighting = weightingMethod = VAL_SHORTEST;

        //For a requested recommended weighting, use recommended for bike, walking and hgv. Use fastest for car.
        if (requestWeighting == WeightingMethod.RECOMMENDED || requestWeighting == WeightingMethod.FASTEST) {
            if (profileType == RoutingProfileType.DRIVING_CAR) {
                weighting = weightingMethod = VAL_FASTEST;
            }
            else if (RoutingProfileType.isHeavyVehicle(profileType)) {
                weighting = VAL_RECOMMENDED;
                weightingMethod = VAL_RECOMMENDED_PREF;
            }
            if (RoutingProfileType.isCycling(profileType) || RoutingProfileType.isWalking(profileType)){
                weighting = VAL_RECOMMENDED;
                weightingMethod = VAL_RECOMMENDED;
            }

        }

        map.put(KEY_WEIGHTING, weighting);
        map.put(KEY_WEIGHTING_METHOD, weightingMethod);
    }
    /**
     * Set the speedup techniques used for calculating the route.
     * Reults in usage of CH, Core or ALT/AStar, if they are enabled.
     *
     * @param req Request whose hints will be set
     * @param useCH Should CH be enabled
     * @param useCore Should Core be enabled
     * @param useALT Should ALT be enabled
     */
    private void setSpeedups(GHRequest req, boolean useCH, boolean useCore, boolean useALT){
        String weighting = req.getWeighting();

        //Priority: CH->Core->ALT
        useCH = useCH && mGraphHopper.isCHAvailable(weighting);
        useCore = useCore && !useCH && mGraphHopper.isCoreAvailable(weighting);
        useALT = useALT && !useCH && !useCore && mGraphHopper.isLMAvailable(weighting);

        req.getHints().put(KEY_CH_DISABLE, !useCH);
        req.getHints().put(KEY_CORE_DISABLE, !useCore);
        req.getHints().put(KEY_LM_DISABLE, !useALT);

        if(useCore || useALT)
            req.setAlgorithm(KEY_ASTARBI);
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
        IsochroneMap result = null;
        waitForUpdateCompletion();
        beginUseGH();
        try {
            RouteSearchContext searchCntx = createSearchContext(parameters.getRouteParameters());
            IsochroneMapBuilderFactory isochroneMapBuilderFactory = new IsochroneMapBuilderFactory(searchCntx);
            result = isochroneMapBuilderFactory.buildMap(parameters);
            endUseGH();
        } catch (Exception ex) {
            endUseGH();
            LOGGER.error(ex);
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

    public boolean equals(Object o) {
        return o != null && o.getClass().equals(RoutingProfile.class) && this.hashCode() == o.hashCode();
    }

    public int hashCode() {
        return mGraphHopper.getGraphHopperStorage().getDirectory().getLocation().hashCode();
    }
}
