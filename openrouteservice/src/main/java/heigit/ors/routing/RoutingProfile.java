/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. The GIScience licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import com.typesafe.config.Config;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.isochrones.*;
import heigit.ors.isochrones.statistics.StatisticsProvider;
import heigit.ors.isochrones.statistics.StatisticsProviderConfiguration;
import heigit.ors.isochrones.statistics.StatisticsProviderFactory;
import heigit.ors.mapmatching.MapMatcher;
import heigit.ors.mapmatching.RouteSegmentInfo;
import heigit.ors.mapmatching.hmm.HiddenMarkovMapMatcher;
import heigit.ors.matrix.*;
import heigit.ors.matrix.algorithms.MatrixAlgorithm;
import heigit.ors.matrix.algorithms.MatrixAlgorithmFactory;
import heigit.ors.optimization.OptimizationErrorCodes;
import heigit.ors.optimization.RouteOptimizationRequest;
import heigit.ors.optimization.RouteOptimizationResult;
import heigit.ors.optimization.solvers.OptimizationProblemSolver;
import heigit.ors.optimization.solvers.OptimizationProblemSolverFactory;
import heigit.ors.optimization.solvers.OptimizationSolution;
import heigit.ors.routing.configuration.RouteProfileConfiguration;
import heigit.ors.routing.graphhopper.extensions.*;
import heigit.ors.routing.graphhopper.extensions.edgefilters.*;
import heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.parameters.*;
import heigit.ors.routing.traffic.RealTrafficDataProvider;
import heigit.ors.routing.traffic.TrafficEdgeAnnotator;
import heigit.ors.services.isochrones.IsochronesServiceSettings;
import heigit.ors.services.matrix.MatrixServiceSettings;
import heigit.ors.services.optimization.OptimizationServiceSettings;
import heigit.ors.util.DebugUtility;
import heigit.ors.util.RuntimeUtility;
import heigit.ors.util.StringUtility;
import heigit.ors.util.TimeUtility;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class generates {@link RoutingProfile} classes and is used by mostly all service classes e.g.
 * <p>
 * {@link heigit.ors.services.isochrones.requestprocessors.json.JsonIsochronesRequestProcessor}
 * <p>
 * {@link RoutingProfileManager} etc.
 *
 * @author Openrouteserviceteam
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class RoutingProfile {
    private static final Logger LOGGER = Logger.getLogger(RoutingProfileManager.class.getName());
    private static int profileIdentifier = 0;
    private static final Object lockObj = new Object();

    private ORSGraphHopper mGraphHopper;
    private boolean mUseTrafficInfo;
    private Integer[] mRoutePrefs;
    private Integer mUseCounter;
    private boolean mUpdateRun;
    private MapMatcher mMapMatcher;

    private RouteProfileConfiguration _config;
    private String _astarApproximation;
    private Double _astarEpsilon;

    public RoutingProfile(String osmFile, RouteProfileConfiguration rpc, RoutingProfilesCollection profiles, RoutingProfileLoadContext loadCntx) throws Exception {
        mRoutePrefs = rpc.getProfilesTypes();
        mUseCounter = 0;
        mUseTrafficInfo = /*mHasDynamicWeights &&*/ hasCarPreferences() ? rpc.getUseTrafficInformation() : false;

        mGraphHopper = initGraphHopper(osmFile, rpc, profiles, loadCntx);

        _config = rpc;

        Config optsExecute = _config.getExecutionOpts();
        if (optsExecute != null) {
            if (optsExecute.hasPath("methods.astar.approximation"))
                _astarApproximation = optsExecute.getString("methods.astar.approximation");
            if (optsExecute.hasPath("methods.astar.epsilon"))
                _astarEpsilon = Double.parseDouble(optsExecute.getString("methods.astar.epsilon"));
        }
    }

    public static ORSGraphHopper initGraphHopper(String osmFile, RouteProfileConfiguration config, RoutingProfilesCollection profiles, RoutingProfileLoadContext loadCntx) throws Exception {
        CmdArgs args = createGHSettings(osmFile, config);

        RoutingProfile refProfile = null;

        try {
            refProfile = profiles.getRouteProfile(RoutingProfileType.DRIVING_CAR);
        } catch (Exception ex) {
        }

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

        ORSGraphHopper gh = (ORSGraphHopper) new ORSGraphHopper(gpc, config.getUseTrafficInformation(), refProfile);

        ORSDefaultFlagEncoderFactory flagEncoderFactory = new ORSDefaultFlagEncoderFactory();
        gh.setFlagEncoderFactory(flagEncoderFactory);

        gh.init(args);

        gh.setGraphStorageFactory(new ORSGraphStorageFactory(gpc.getStorageBuilders()));
        gh.setWeightingFactory(new ORSWeightingFactory(RealTrafficDataProvider.getInstance()));

        if (!Helper.isEmpty(config.getElevationProvider()) && !Helper.isEmpty(config.getElevationCachePath())) {
            ElevationProvider elevProvider = loadCntx.getElevationProvider(config.getElevationProvider(), config.getElevationCachePath(), config.getElevationDataAccess(), config.getElevationCacheClear());
            gh.setElevationProvider(elevProvider);
        }

        gh.importOrLoad();

        if (LOGGER.isInfoEnabled()) {
            EncodingManager encodingMgr = gh.getEncodingManager();
            GraphHopperStorage ghStorage = gh.getGraphHopperStorage();
            LOGGER.info(String.format("[%d] FlagEncoders: %s, bits used %d/%d.", profileId, encodingMgr.fetchEdgeEncoders().size(), encodingMgr.getUsedBitsForFlags(), encodingMgr.getBytesForFlags() * 8));
            LOGGER.info(String.format("[%d] Capacity:  %s. (edges - %s, nodes - %s)", profileId, RuntimeUtility.getMemorySize(gh.getCapacity()), ghStorage.getEdges(), ghStorage.getNodes()));
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

        if (config.getInstructions() == false)
            args.put("instructions", false);
        if (config.getElevationProvider() != null && config.getElevationCachePath() != null) {
            args.put("graph.elevation.provider", StringUtility.trimQuotes(config.getElevationProvider()));
            args.put("graph.elevation.cache_dir", StringUtility.trimQuotes(config.getElevationCachePath()));
            args.put("graph.elevation.dataaccess", StringUtility.trimQuotes(config.getElevationDataAccess()));
        }

        boolean prepareCH = false;
        boolean prepareLM = false;

        args.put("prepare.ch.weightings", "no");
        args.put("prepare.lm.weightings", "no");

        if (config.getPreparationOpts() != null) {
            Config opts = config.getPreparationOpts();
            if (opts.hasPath("min_network_size"))
                args.put("prepare.min_network_size", opts.getInt("min_network_size"));
            if (opts.hasPath("min_one_way_network_size"))
                args.put("prepare.min_one_way_network_size", opts.getInt("min_one_way_network_size"));

            if (opts.hasPath("methods")) {
                if (opts.hasPath("methods.ch")) {
                    prepareCH = true;
                    Config chOpts = opts.getConfig("methods.ch");

                    if (chOpts.hasPath("enabled") || chOpts.getBoolean("enabled")) {
                        prepareCH = chOpts.getBoolean("enabled");
                        if (prepareCH == false)
                            args.put("prepare.ch.weightings", "no");
                    }


                    if (prepareCH) {
                        if (chOpts.hasPath("threads"))
                            args.put("prepare.ch.threads", chOpts.getInt("threads"));
                        if (chOpts.hasPath("weightings"))
                            args.put("prepare.ch.weightings", StringUtility.trimQuotes(chOpts.getString("weightings")));
                    }
                }

                if (opts.hasPath("methods.lm")) {
                    prepareLM = true;
                    Config lmOpts = opts.getConfig("methods.lm");

                    if (lmOpts.hasPath("enabled") || lmOpts.getBoolean("enabled")) {
                        prepareLM = lmOpts.getBoolean("enabled");
                        if (prepareLM == false)
                            args.put("prepare.lm.weightings", "no");
                    }

                    if (prepareLM) {
                        if (lmOpts.hasPath("threads"))
                            args.put("prepare.lm.threads", lmOpts.getInt("threads"));
                        if (lmOpts.hasPath("weightings"))
                            args.put("prepare.lm.weightings", StringUtility.trimQuotes(lmOpts.getString("weightings")));
                        if (lmOpts.hasPath("landmarks"))
                            args.put("prepare.lm.landmarks", lmOpts.getInt("landmarks"));
                    }
                }
            }
        }

        if (config.getExecutionOpts() != null) {
            Config opts = config.getExecutionOpts();
            if (opts.hasPath("methods.ch")) {
                Config chOpts = opts.getConfig("methods.ch");
                if (chOpts.hasPath("disabling_allowed"))
                    args.put("routing.ch.disabling_allowed", chOpts.getBoolean("disabling_allowed"));
            }
            if (opts.hasPath("methods.lm")) {
                Config lmOpts = opts.getConfig("methods.lm");
                if (lmOpts.hasPath("disabling_allowed"))
                    args.put("routing.lm.disabling_allowed", lmOpts.getBoolean("disabling_allowed"));

                if (lmOpts.hasPath("active_landmarks"))
                    args.put("routing.lm.active_landmarks", lmOpts.getInt("active_landmarks"));
            }
        }


        if (config.getOptimize() && !prepareCH)
            args.put("graph.do_sort", true);

        String flagEncoders = "";
        String[] encoderOpts = !Helper.isEmpty(config.getEncoderOptions()) ? config.getEncoderOptions().split(",") : null;
        Integer[] profiles = config.getProfilesTypes();

        for (int i = 0; i < profiles.length; i++) {
            if (encoderOpts == null)
                flagEncoders += RoutingProfileType.getEncoderName(profiles[i]);
            else
                flagEncoders += RoutingProfileType.getEncoderName(profiles[i]) + "|" + encoderOpts[i];
            if (i < profiles.length - 1)
                flagEncoders += ",";
        }

        args.put("graph.flag_encoders", flagEncoders.toLowerCase());

        //args.put("osmreader.wayPointMaxDistance",1);
        args.put("index.high_resolution", 500);

        return args;
    }

    public HashMap<Integer, Long> getTmcEdges() {
        return mGraphHopper.getTmcGraphEdges();
    }

    public HashMap<Long, ArrayList<Integer>> getOsmId2edgeIds() {
        return mGraphHopper.getOsmId2EdgeIds();
    }

    public ORSGraphHopper getGraphhopper() {
        return mGraphHopper;
    }

    public BBox getBounds() {
        return mGraphHopper.getGraphHopperStorage().getBounds();
    }

    public StorableProperties getGraphProperties() {
        StorableProperties props = mGraphHopper.getGraphHopperStorage().getProperties();
        return props;
    }

    public String getGraphLocation() {
        return mGraphHopper == null ? null : mGraphHopper.getGraphHopperStorage().getDirectory().toString();
    }

    public RouteProfileConfiguration getConfiguration() {
        return _config;
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

    public boolean useTrafficInformation() {
        return mUseTrafficInfo;
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

                    mGraphHopper = initGraphHopper(ghOld.getDataReaderFile(), _config, RoutingProfileManager.getInstance().getProfiles(), loadCntx);

                    loadCntx.release();

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
        if (Arrays.toString(attributes).contains("total_pop".toLowerCase()) && !(Arrays.toString(attributes).contains("total_area_km".toLowerCase()))) {
            tempAttributes = new String[attributes.length + 1];
            int i = 0;
            while (i < attributes.length) {
                String attribute = attributes[i];
                tempAttributes[i] = attribute;
                i++;
            }
            tempAttributes[i] = "total_area_km";
        } else if ((Arrays.toString(attributes).contains("total_area_km".toLowerCase())) && (!Arrays.toString(attributes).contains("total_pop".toLowerCase()))) {
            tempAttributes = new String[attributes.length + 1];
            int i = 0;
            while (i < attributes.length) {
                String attribute = attributes[i];
                tempAttributes[i] = attribute;
                i++;
            }
            tempAttributes[i] = "total_pop";
        } else {
            tempAttributes = attributes;
        }


        IsochroneMap result = null;
        waitForUpdateCompletion();

        beginUseGH();

        try {
            RouteSearchContext searchCntx = createSearchContext(parameters.getRouteParameters(), RouteSearchMode.Isochrones, null);

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
                Map<StatisticsProviderConfiguration, List<String>> mapProviderToAttrs = new HashMap<StatisticsProviderConfiguration, List<String>>();
                for (String attr : tempAttributes) {
                    StatisticsProviderConfiguration provConfig = IsochronesServiceSettings.getStatsProviders().get(attr);

                    if (provConfig != null) {
                        if (mapProviderToAttrs.containsKey(provConfig)) {
                            List<String> attrList = mapProviderToAttrs.get(provConfig);
                            attrList.add(attr);
                        } else {
                            List<String> attrList = new ArrayList<String>();
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

        MatrixAlgorithm alg = MatrixAlgorithmFactory.createAlgorithm(req, gh, flagEncoder);

        if (alg == null)
            throw new Exception("Unable to create an algorithm to for computing distance/duration matrix.");

        try {
            String weightingStr = Helper.isEmpty(req.getWeightingMethod()) ? "fastest" : req.getWeightingMethod();
            Graph graph = null;
            if (!req.getFlexibleMode() && gh.getCHFactoryDecorator().isEnabled() && gh.getCHFactoryDecorator().getWeightingsAsStrings().contains(weightingStr))
                graph = gh.getGraphHopperStorage().getGraph(CHGraph.class);
            else
                graph = gh.getGraphHopperStorage().getBaseGraph();

            MatrixSearchContextBuilder builder = new MatrixSearchContextBuilder(gh.getLocationIndex(), new DefaultEdgeFilter(flagEncoder), new ByteArrayBuffer(), req.getResolveLocations());
            MatrixSearchContext mtxSearchCntx = builder.create(graph, req.getSources(), req.getDestinations(), MatrixServiceSettings.getMaximumSearchRadius());

            HintsMap hintsMap = new HintsMap();
            hintsMap.setWeighting(weightingStr);
            Weighting weighting = new ORSWeightingFactory(RealTrafficDataProvider.getInstance()).createWeighting(hintsMap, gh.getTraversalMode(), flagEncoder, graph, null, gh.getGraphHopperStorage());

            alg.init(req, gh, mtxSearchCntx.getGraph(), flagEncoder, weighting);

            mtxResult = alg.compute(mtxSearchCntx.getSources(), mtxSearchCntx.getDestinations(), req.getMetrics());
        } catch (Exception ex) {
            LOGGER.error(ex);
            throw new InternalServerException(MatrixErrorCodes.UNKNOWN, "Unable to compute a distance/duration matrix.");
        }

        return mtxResult;
    }

    public RouteOptimizationResult computeOptimizedRoutes(RouteOptimizationRequest req) throws Exception {
        RouteOptimizationResult optResult = null;

        //RouteProcessContext routeProcCntx = new RouteProcessContext(null);

        MatrixResult mtxResult = null;

        try {
            MatrixRequest mtxReq = req.createMatrixRequest();
            mtxResult = computeMatrix(mtxReq);
        } catch (Exception ex) {
            LOGGER.error(ex);
            throw new InternalServerException(OptimizationErrorCodes.UNKNOWN, "Unable to compute an optimized route.");
        }

        OptimizationProblemSolver solver = OptimizationProblemSolverFactory.createSolver(OptimizationServiceSettings.getSolverName(), OptimizationServiceSettings.getSolverOptions());

        if (solver == null)
            throw new Exception("Unable to create an algorithm to distance/duration matrix.");

        OptimizationSolution solution = null;

        try {
            float[] costs = mtxResult.getTable(req.getMetric());
            costs[0] = 0; // TODO

            solution = solver.solve();
        } catch (Exception ex) {
            LOGGER.error(ex);

            throw new InternalServerException(OptimizationErrorCodes.UNKNOWN, "Optimization problem solver threw an exception.");
        }

        if (!solution.isValid())
            throw new InternalServerException(OptimizationErrorCodes.UNKNOWN, "Optimization problem solver was unable to find an appropriate solution.");

        //RouteSearchParameters searchParams = new RouteSearchParameters();

        optResult = new RouteOptimizationResult();


        //getRoute(lat0, lon0, lat1, lon1, false, searchParams, req.getSimplifyGeometry(), routeProcCntx);
        // compute final route
        //optResult.setRouteResult(routeResult);


        return optResult;
    }

    private RouteSearchContext createSearchContext(RouteSearchParameters searchParams, RouteSearchMode mode, EdgeFilter customEdgeFilter) throws Exception {
        int profileType = searchParams.getProfileType();
        int weightingMethod = searchParams.getWeightingMethod();
        String encoderName = RoutingProfileType.getEncoderName(profileType);
        EdgeFilter edgeFilter = null;
        FlagEncoder flagEncoder = mGraphHopper.getEncodingManager().getEncoder(encoderName);
        //String algorithm = null;
        PMap props = new PMap();

        if (searchParams.hasAvoidAreas()) {
            if (encoderName.isEmpty())
                throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "vehicle parameter is empty.");

            if (!mGraphHopper.getEncodingManager().supports(encoderName)) {
                throw new IllegalArgumentException("Vehicle " + encoderName + " unsupported. " + "Supported are: "
                        + mGraphHopper.getEncodingManager());
            }

            edgeFilter = new AvoidAreasEdgeFilter(flagEncoder, searchParams.getAvoidAreas());
        }

        if (RoutingProfileType.isDriving(profileType)) {
            if (RoutingProfileType.isHeavyVehicle(profileType)) {
                edgeFilter = createHeavyVehicleEdgeFilter(searchParams, flagEncoder, edgeFilter);
            } else if (searchParams.hasParameters(VehicleParameters.class)) {
                //edgeFilter = createWayRestrictionsEdgeFilter(searchParams, flagEncoder, edgeFilter);
            }
        } else if (profileType == RoutingProfileType.WHEELCHAIR) {
            if (searchParams.hasParameters(WheelchairParameters.class)) {
                edgeFilter = createWheelchairRestrictionsEdgeFilter(searchParams, flagEncoder,
                        edgeFilter);
            }
        }

        if (searchParams.hasAvoidFeatures()) {
            if (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType)
                    || profileType == RoutingProfileType.FOOT_WALKING || profileType == RoutingProfileType.FOOT_HIKING
                    || profileType == RoutingProfileType.WHEELCHAIR) {

                if (searchParams.getAvoidFeatureTypes() != AvoidFeatureFlags.Hills) {
                    EdgeFilter ef = new AvoidFeaturesEdgeFilter(flagEncoder, searchParams,
                            mGraphHopper.getGraphHopperStorage());
                    edgeFilter = createEdgeFilter(ef, edgeFilter);
                }

                if (mode == RouteSearchMode.Routing) {
                    if ((searchParams.getAvoidFeatureTypes() & AvoidFeatureFlags.Hills) == AvoidFeatureFlags.Hills) {
                        props.put("custom_weightings", true);
                        props.put(ProfileWeighting.encodeName("avoid_hills"), true);

                        if (searchParams.hasParameters(CyclingParameters.class)) // FIXME: have no idea what this line was meant for.
                        {
                            CyclingParameters cyclingParams = (CyclingParameters) searchParams.getProfileParameters();
                            props.put("steepness_maximum", cyclingParams.getMaximumGradient());
                        }
                    }
                }
            }
        }

        if (searchParams.hasAvoidBorders() || searchParams.hasAvoidCountries()) {
            // We want to avoid borders of some form
            if (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType)) {
                EdgeFilter ef = new AvoidBordersEdgeFilter(flagEncoder, searchParams, mGraphHopper.getGraphHopperStorage());
                edgeFilter = createEdgeFilter(ef, edgeFilter);
            }
        }

        if (searchParams.hasParameters(CyclingParameters.class)) {
            CyclingParameters cyclingParams = (CyclingParameters) searchParams.getProfileParameters();

            if (cyclingParams.getMaximumGradient() > 0) {
                EdgeFilter ef = new AvoidSteepnessEdgeFilter(flagEncoder, mGraphHopper.getGraphHopperStorage(), cyclingParams.getMaximumGradient());
                edgeFilter = createEdgeFilter(ef, edgeFilter);
            }

            if (cyclingParams.getMaximumTrailDifficulty() > 0) {
                EdgeFilter ef = new TrailDifficultyEdgeFilter(flagEncoder, mGraphHopper.getGraphHopperStorage(), cyclingParams.getMaximumTrailDifficulty());
                edgeFilter = createEdgeFilter(ef, edgeFilter);
            }
        } else if (searchParams.hasParameters(WalkingParameters.class)) {
            WalkingParameters walkingParams = (WalkingParameters) searchParams.getProfileParameters();

            if (walkingParams.getMaximumGradient() > 0) {
                EdgeFilter ef = new AvoidSteepnessEdgeFilter(flagEncoder, mGraphHopper.getGraphHopperStorage(), walkingParams.getMaximumGradient());
                edgeFilter = createEdgeFilter(ef, edgeFilter);
            }

            if (walkingParams.getMaximumTrailDifficulty() > 0) {
                EdgeFilter ef = new TrailDifficultyEdgeFilter(flagEncoder, mGraphHopper.getGraphHopperStorage(), walkingParams.getMaximumTrailDifficulty());
                edgeFilter = createEdgeFilter(ef, edgeFilter);
            }
        }

        ProfileParameters profileParams = searchParams.getProfileParameters();
        if (profileParams != null && profileParams.hasWeightings()) {
            props.put("custom_weightings", true);
            Iterator<ProfileWeighting> iterator = profileParams.getWeightings().getIterator();
            while (iterator.hasNext()) {
                ProfileWeighting weighting = iterator.next();
                if (!weighting.getParameters().isEmpty()) {
                    String name = ProfileWeighting.encodeName(weighting.getName());
                    for (Map.Entry<String, String> kv : weighting.getParameters().getMap().entrySet())
                        props.put(name + kv.getKey(), kv.getValue());
                }
            }
        }

        if (searchParams.getConsiderTraffic()/* && mHasDynamicWeights */) {
            if (RoutingProfileType.isDriving(profileType) && weightingMethod != WeightingMethod.SHORTEST
                    && RealTrafficDataProvider.getInstance().isInitialized()) {
                props.put("weighting_traffic_block", true);

                EdgeFilter ef = new BlockedEdgesEdgeFilter(flagEncoder, RealTrafficDataProvider.getInstance()
                        .getBlockedEdges(mGraphHopper.getGraphHopperStorage()), RealTrafficDataProvider.getInstance()
                        .getHeavyVehicleBlockedEdges(mGraphHopper.getGraphHopperStorage()));

                edgeFilter = createEdgeFilter(ef, edgeFilter);
            }
        }

        if (edgeFilter == null)
            edgeFilter = new DefaultEdgeFilter(flagEncoder);

        RouteSearchContext searchCntx = new RouteSearchContext(mGraphHopper, edgeFilter, flagEncoder);
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

    public boolean canProcessRequest(double totalDistance, double longestSegmentDistance, int wayPoints) {
        double maxDistance = (_config.getMaximumDistance() > 0) ? _config.getMaximumDistance() : Double.MAX_VALUE;
        int maxWayPoints = (_config.getMaximumWayPoints() > 0) ? _config.getMaximumWayPoints() : Integer.MAX_VALUE;

        return totalDistance <= maxDistance && wayPoints <= maxWayPoints;
    }

    public GHResponse computeRoute(double lat0, double lon0, double lat1, double lon1, WayPointBearing[] bearings, double[] radiuses, boolean directedSegment, RouteSearchParameters searchParams, EdgeFilter customEdgeFilter, boolean simplifyGeometry, RouteProcessContext routeProcCntx)
            throws Exception {

        GHResponse resp = null;

        waitForUpdateCompletion();

        beginUseGH();

        try {
            int profileType = searchParams.getProfileType();
            int weightingMethod = searchParams.getWeightingMethod();
            RouteSearchContext searchCntx = createSearchContext(searchParams, RouteSearchMode.Routing, customEdgeFilter);

            boolean flexibleMode = searchParams.getFlexibleMode();
            GHRequest req = null;
            if (bearings == null || bearings[0] == null)
                req = new GHRequest(new GHPoint(lat0, lon0), new GHPoint(lat1, lon1));
            else if (bearings[1] == null)
                req = new GHRequest(new GHPoint(lat0, lon0), new GHPoint(lat1, lon1), bearings[0].getValue(), bearings[0].getDeviation(), Double.NaN, Double.NaN);
            else
                req = new GHRequest(new GHPoint(lat0, lon0), new GHPoint(lat1, lon1), bearings[0].getValue(), bearings[0].getDeviation(), bearings[1].getValue(), bearings[1].getDeviation());

            req.setVehicle(searchCntx.getEncoder().toString());
            req.setMaxSpeed(searchParams.getMaximumSpeed());
            req.setSimplifyGeometry(simplifyGeometry);
            req.setAlgorithm("dijkstrabi");

            if (radiuses != null)
                req.setMaxSearchDistance(radiuses);

            PMap props = searchCntx.getProperties();
            if (props != null && props.size() > 0)
                req.getHints().merge(props);

            if (supportWeightingMethod(profileType)) {
                if (weightingMethod == WeightingMethod.FASTEST) {
                    req.setWeighting("fastest");
                    req.getHints().put("weighting_method", "fastest");
                } else if (weightingMethod == WeightingMethod.SHORTEST) {
                    req.setWeighting("shortest");
                    req.getHints().put("weighting_method", "shortest");
                    flexibleMode = true;
                } else if (weightingMethod == WeightingMethod.RECOMMENDED) {
                    req.setWeighting("fastest");
                    req.getHints().put("weighting_method", "recommended");
                    flexibleMode = true;
                }
            }

            if ((profileType == RoutingProfileType.CYCLING_TOUR || profileType == RoutingProfileType.CYCLING_MOUNTAIN)
                    && weightingMethod == WeightingMethod.FASTEST) {
                req.setWeighting("fastest");
                req.getHints().put("weighting_method", "recommended");
                flexibleMode = true;
            }

            if ((profileType == RoutingProfileType.CYCLING_TOUR /*RoutingProfileType.isCycling(profileType) || RoutingProfileType.isWalking(profileType)*/ || (profileType == RoutingProfileType.DRIVING_HGV && HeavyVehicleAttributes.HGV == searchParams
                    .getVehicleType())) && weightingMethod == WeightingMethod.RECOMMENDED) {
                req.setWeighting("fastest");
                req.getHints().put("weighting_method", "recommended_pref");

                flexibleMode = true;
            }

            if (RoutingProfileType.isDriving(profileType) && RealTrafficDataProvider.getInstance().isInitialized())
                req.setEdgeAnnotator(new TrafficEdgeAnnotator(mGraphHopper.getGraphHopperStorage()));

            req.setEdgeFilter(searchCntx.getEdgeFilter());
            req.setPathProcessor(routeProcCntx.getPathProcessor());

            if (useDynamicWeights(searchParams) || flexibleMode) {
                if (mGraphHopper.isCHEnabled())
                    req.getHints().put("ch.disable", true);
                if (mGraphHopper.getLMFactoryDecorator().isEnabled())
                    req.setAlgorithm("astarbi");
                req.getHints().put("lm.disable", false);
            } else {
                if (mGraphHopper.isCHEnabled())
                    req.getHints().put("lm.disable", true);
                else
                    req.getHints().put("ch.disable", true);
            }

            if (profileType == RoutingProfileType.DRIVING_EMERGENCY) {
                req.getHints().put("custom_weightings", true);
                req.getHints().put("weighting_#acceleration#", true);
                req.getHints().put("lm.disable", true); // REMOVE
            }

            if (_astarEpsilon != null)
                req.getHints().put("astarbi.epsilon", _astarEpsilon);
            if (_astarApproximation != null)
                req.getHints().put("astarbi.approximation", _astarApproximation);

			/*if (directedSegment)
				resp = mGraphHopper.directRoute(req); NOTE IMPLEMENTED!!!
			else */
            resp = mGraphHopper.route(req, routeProcCntx.getArrayBuffer());

            if (DebugUtility.isDebug()) {
                System.out.println("visited_nodes.average - " + resp.getHints().get("visited_nodes.average", ""));
            }

            endUseGH();
        } catch (Exception ex) {
            endUseGH();

            LOGGER.error(ex);

            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Unable to compute a route");
        }

        return resp;
    }

    private boolean useDynamicWeights(RouteSearchParameters searchParams) {
        boolean dynamicWeights = (searchParams.hasAvoidAreas() || searchParams.hasAvoidFeatures() || searchParams.hasAvoidCountries() || searchParams.hasAvoidBorders() || searchParams.getMaximumSpeed() > 0 || (RoutingProfileType.isDriving(searchParams.getProfileType()) && (searchParams.hasParameters(VehicleParameters.class) || searchParams.getConsiderTraffic())) || (searchParams.getWeightingMethod() == WeightingMethod.SHORTEST || searchParams.getWeightingMethod() == WeightingMethod.RECOMMENDED) || searchParams.getConsiderTurnRestrictions() /*|| RouteExtraInformationFlag.isSet(extraInfo, value) searchParams.getIncludeWaySurfaceInfo()*/);

        return dynamicWeights;
    }

    private EdgeFilter createEdgeFilter(EdgeFilter edgeFilter, EdgeFilter seq) {
        if (seq != null && seq instanceof EdgeFilterSequence) {
            EdgeFilterSequence seqFilter = (EdgeFilterSequence) seq;
            seqFilter.addFilter(edgeFilter);
            return seqFilter;
        } else {
            ArrayList<EdgeFilter> edgeFilters = new ArrayList<EdgeFilter>();
            edgeFilters.add(edgeFilter);
            if (seq != null)
                edgeFilters.add(seq);
            else
                return edgeFilter;
            EdgeFilterSequence seqFilter = new EdgeFilterSequence(edgeFilters);
            return seqFilter;
        }
    }

    private EdgeFilter createWheelchairRestrictionsEdgeFilter(RouteSearchParameters searchParams,
                                                              FlagEncoder flagEncoder, EdgeFilter edgeFilter) throws Exception {
        if (searchParams.hasParameters(WheelchairParameters.class)) {
            EdgeFilter ef = null;
            GraphStorage gs = mGraphHopper.getGraphHopperStorage();
            ef = new WheelchairEdgeFilter((WheelchairParameters) searchParams.getProfileParameters(), (WheelchairFlagEncoder) flagEncoder, gs);
            edgeFilter = createEdgeFilter(ef, edgeFilter);
        }
        return edgeFilter;
    }

    private EdgeFilter createHeavyVehicleEdgeFilter(RouteSearchParameters searchParams, FlagEncoder flagEncoder,
                                                    EdgeFilter edgeFilter) {
        if (searchParams.hasParameters(VehicleParameters.class)) {
            GraphStorage gs = mGraphHopper.getGraphHopperStorage();

            int vehicleType = searchParams.getVehicleType();
            VehicleParameters vehicleParams = (VehicleParameters) searchParams.getProfileParameters();

            if (vehicleParams.hasAttributes()) {
                EdgeFilter ef = null;
                if (searchParams.getProfileType() == RoutingProfileType.DRIVING_HGV)
                    ef = new HeavyVehicleEdgeFilter(flagEncoder, vehicleType, vehicleParams, gs);
                else if (searchParams.getProfileType() == RoutingProfileType.DRIVING_EMERGENCY)
                    ef = new EmergencyVehicleEdgeFilter(flagEncoder, vehicleParams, gs);

                if (ef != null)
                    edgeFilter = createEdgeFilter(ef, edgeFilter);
            }
        }

        return edgeFilter;
    }

    private static boolean supportWeightingMethod(int profileType) {
        if (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType) || RoutingProfileType.isWalking(profileType) || profileType == RoutingProfileType.WHEELCHAIR)
            return true;
        else
            return false;
    }

    public Geometry getEdgeGeometry(int edgeId) {
        return getEdgeGeometry(edgeId, 3, Integer.MIN_VALUE);
    }

    public Geometry getEdgeGeometry(int edgeId, int mode, int adjnodeid) {
        EdgeIteratorState iter = mGraphHopper.getGraphHopperStorage().getEdgeIteratorState(edgeId, adjnodeid);
        PointList points = iter.fetchWayGeometry(mode);
        if (points.size() > 1) {
            Coordinate[] coords = new Coordinate[points.size()];
            for (int i = 0; i < points.size(); i++) {
                double x = points.getLon(i);
                double y = points.getLat(i);
                coords[i] = new Coordinate(x, y);
            }
            return new GeometryFactory().createLineString(coords);
        }
        return null;
    }

    public EdgeFilter createAccessRestrictionFilter(Coordinate[] wayPoints) {
        //rp.getGraphhopper()
        return null;
    }

    public int hashCode() {
        return mGraphHopper.getGraphHopperStorage().getDirectory().getLocation().hashCode();
    }
}
