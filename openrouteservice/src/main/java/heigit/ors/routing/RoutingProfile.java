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
package heigit.ors.routing;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
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
import heigit.ors.common.TravelRangeType;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.exceptions.StatusCodeException;
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
        mUseTrafficInfo = hasCarPreferences() ? rpc.getUseTrafficInformation() : false;

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

        ORSGraphHopper gh = new ORSGraphHopper(gpc, config.getUseTrafficInformation(), refProfile);

        ORSDefaultFlagEncoderFactory flagEncoderFactory = new ORSDefaultFlagEncoderFactory();
        gh.setFlagEncoderFactory(flagEncoderFactory);

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
        gh.setWeightingFactory(new ORSWeightingFactory(RealTrafficDataProvider.getInstance()));

        gh.importOrLoad();

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

        if (config.getInstructions() == false)
            args.put("instructions", false);
        if (config.getElevationProvider() != null && config.getElevationCachePath() != null) {
            args.put("graph.elevation.provider", StringUtility.trimQuotes(config.getElevationProvider()));
            args.put("graph.elevation.cache_dir", StringUtility.trimQuotes(config.getElevationCachePath()));
            args.put("graph.elevation.dataaccess", StringUtility.trimQuotes(config.getElevationDataAccess()));
            args.put("graph.elevation.clear", config.getElevationCacheClear());
        }

        boolean prepareCH = false;
        boolean prepareLM = false;
        boolean prepareCore = false;
        boolean prepareCoreLM = false;

        args.put("prepare.ch.weightings", "no");
        args.put("prepare.lm.weightings", "no");
        args.put("prepare.core.weightings", "no");

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

                if (opts.hasPath("methods.core")) {
                    prepareCore = true;
                    Config coreOpts = opts.getConfig("methods.core");

                    if (coreOpts.hasPath("enabled") || coreOpts.getBoolean("enabled")) {
                        prepareCore = coreOpts.getBoolean("enabled");
                        if (prepareCore == false)
                            args.put("prepare.ch.weightings", "no");
                    }


                    if (prepareCore) {
                        if (coreOpts.hasPath("threads"))
                            args.put("prepare.core.threads", coreOpts.getInt("threads"));
                        if (coreOpts.hasPath("weightings"))
                            args.put("prepare.core.weightings", StringUtility.trimQuotes(coreOpts.getString("weightings")));
                        if (coreOpts.hasPath("lmsets"))
                            args.put("prepare.corelm.lmsets", StringUtility.trimQuotes(coreOpts.getString("lmsets")));
                        if (coreOpts.hasPath("landmarks"))
                            args.put("prepare.corelm.landmarks", coreOpts.getInt("landmarks"));
                    }
                }
            }
        }

        if (config.getExecutionOpts() != null) {
            Config opts = config.getExecutionOpts();
            if (opts.hasPath("methods.ch")) {
                Config coreOpts = opts.getConfig("methods.ch");
                if (coreOpts.hasPath("disabling_allowed"))
                    args.put("routing.ch.disabling_allowed", coreOpts.getBoolean("disabling_allowed"));
            }
            if (opts.hasPath("methods.core")) {
                Config chOpts = opts.getConfig("methods.core");
                if (chOpts.hasPath("disabling_allowed"))
                    args.put("routing.core.disabling_allowed", chOpts.getBoolean("disabling_allowed"));
            }
            if (opts.hasPath("methods.lm")) {
                Config lmOpts = opts.getConfig("methods.lm");
                if (lmOpts.hasPath("disabling_allowed"))
                    args.put("routing.lm.disabling_allowed", lmOpts.getBoolean("disabling_allowed"));

                if (lmOpts.hasPath("active_landmarks"))
                    args.put("routing.lm.active_landmarks", lmOpts.getInt("active_landmarks"));
            }
            if (opts.hasPath("methods.corelm")) {
                Config lmOpts = opts.getConfig("methods.corelm");
                if (lmOpts.hasPath("disabling_allowed"))
                    args.put("routing.lm.disabling_allowed", lmOpts.getBoolean("disabling_allowed"));

                if (lmOpts.hasPath("active_landmarks"))
                    args.put("routing.corelm.active_landmarks", lmOpts.getInt("active_landmarks"));
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
        return RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType) || RoutingProfileType.isWalking(profileType) || profileType == RoutingProfileType.WHEELCHAIR;
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

            MatrixSearchContextBuilder builder = new MatrixSearchContextBuilder(gh.getLocationIndex(), new DefaultEdgeFilter(flagEncoder), req.getResolveLocations());
            MatrixSearchContext mtxSearchCntx = builder.create(graph, req.getSources(), req.getDestinations(), MatrixServiceSettings.getMaximumSearchRadius());

            HintsMap hintsMap = new HintsMap();
            hintsMap.setWeighting(weightingStr);
            Weighting weighting = new ORSWeightingFactory(RealTrafficDataProvider.getInstance()).createWeighting(hintsMap, gh.getTraversalMode(), flagEncoder, graph, null, gh.getGraphHopperStorage());

            alg.init(req, gh, mtxSearchCntx.getGraph(), flagEncoder, weighting);

            mtxResult = alg.compute(mtxSearchCntx.getSources(), mtxSearchCntx.getDestinations(), req.getMetrics());
        } catch (Exception ex) {
            LOGGER.error(ex);
            if (ex instanceof StatusCodeException)
                throw ex;
            throw new InternalServerException(MatrixErrorCodes.UNKNOWN, "Unable to compute a distance/duration matrix.");
        }

        return mtxResult;
    }

    public RouteOptimizationResult computeOptimizedRoutes(RouteOptimizationRequest req) throws Exception {
        RouteOptimizationResult optResult = null;

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

        optResult = new RouteOptimizationResult();

        return optResult;
    }

    private RouteSearchContext createSearchContext(RouteSearchParameters searchParams, RouteSearchMode mode, EdgeFilter customEdgeFilter) throws Exception {
        PMap props = new PMap();

        int profileType = searchParams.getProfileType();
        String encoderName = RoutingProfileType.getEncoderName(profileType);

        if ("UNKNOWN".equals(encoderName))
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "unknown vehicle profile.");

        if (!mGraphHopper.getEncodingManager().supports(encoderName)) {
            throw new IllegalArgumentException("Vehicle " + encoderName + " unsupported. " + "Supported are: "
                    + mGraphHopper.getEncodingManager());
        }

        FlagEncoder flagEncoder = mGraphHopper.getEncodingManager().getEncoder(encoderName);
        GraphStorage gs = mGraphHopper.getGraphHopperStorage();
        ProfileParameters profileParams = searchParams.getProfileParameters();

        /* Initialize empty edge filter sequence */

        EdgeFilterSequence edgeFilters = new EdgeFilterSequence();

        /* Default edge filter which accepts both directions of the specified vehicle */

        edgeFilters.add(new DefaultEdgeFilter(flagEncoder));

        /* Avoid areas */

        if (searchParams.hasAvoidAreas()) {
            props.put("avoid_areas", true);
            edgeFilters.add(new AvoidAreasEdgeFilter(searchParams.getAvoidAreas()));
        }

        /* Heavy vehicle filter */

        if (RoutingProfileType.isDriving(profileType)) {
            if (RoutingProfileType.isHeavyVehicle(profileType) && searchParams.hasParameters(VehicleParameters.class)) {
                VehicleParameters vehicleParams = (VehicleParameters) profileParams;

                if (vehicleParams.hasAttributes()) {

                    if (profileType == RoutingProfileType.DRIVING_HGV)
                        edgeFilters.add(new HeavyVehicleEdgeFilter(flagEncoder, searchParams.getVehicleType(), vehicleParams, gs));
                    else if (profileType == RoutingProfileType.DRIVING_EMERGENCY)
                        edgeFilters.add(new EmergencyVehicleEdgeFilter(vehicleParams, gs));
                }
            }
        }

        /* Wheelchair filter */

        else if (profileType == RoutingProfileType.WHEELCHAIR && searchParams.hasParameters(WheelchairParameters.class)) {
            edgeFilters.add(new WheelchairEdgeFilter((WheelchairParameters) profileParams, gs));
        }

        /* Avoid features */

        if (searchParams.hasAvoidFeatures()) {
            props.put("avoid_features", searchParams.getAvoidFeatureTypes());
            edgeFilters.add(new AvoidFeaturesEdgeFilter(profileType, searchParams, gs));
        }

        /* Avoid borders of some form */

        if (searchParams.hasAvoidBorders() || searchParams.hasAvoidCountries()) {
            if (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType)) {
                edgeFilters.add(new AvoidBordersEdgeFilter(searchParams, gs));
                if(searchParams.hasAvoidCountries())
                    props.put("avoid_countries", Arrays.toString(searchParams.getAvoidCountries()));
            }
        }


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

        /* Live traffic filter - currently disabled */

        if (searchParams.getConsiderTraffic()) {
            RealTrafficDataProvider trafficData = RealTrafficDataProvider.getInstance();
            if (RoutingProfileType.isDriving(profileType) && searchParams.getWeightingMethod() != WeightingMethod.SHORTEST && trafficData.isInitialized()) {
                props.put("weighting_traffic_block", true);
                edgeFilters.add(new BlockedEdgesEdgeFilter(flagEncoder, trafficData.getBlockedEdges(gs), trafficData.getHeavyVehicleBlockedEdges(gs)));
            }
        }

        RouteSearchContext searchCntx = new RouteSearchContext(mGraphHopper, edgeFilters, flagEncoder);
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

    public GHResponse computeRoute(double lat0, double lon0, double lat1, double lon1, WayPointBearing[] bearings, double[] radiuses, boolean directedSegment, RouteSearchParameters searchParams, EdgeFilter customEdgeFilter, RouteProcessContext routeProcCntx, Boolean geometrySimplify)
            throws Exception {

        GHResponse resp = null;

        waitForUpdateCompletion();

        beginUseGH();

        try {
            int profileType = searchParams.getProfileType();
            int weightingMethod = searchParams.getWeightingMethod();
            RouteSearchContext searchCntx = createSearchContext(searchParams, RouteSearchMode.Routing, customEdgeFilter);

            boolean flexibleMode = searchParams.getFlexibleMode();
            boolean optimized = searchParams.getOptimized();
            GHRequest req = null;
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

            // MARQ24 for what ever reason after the 'weighting_method' hint have been set (based
            // on the given searchParameter Max have decided that's necessary 'patch' the hint
            // for certain profiles...
            // ...and BTW if the flexibleMode set to true, CH will be disabled!
            if (weightingMethod == WeightingMethod.RECOMMENDED){
                if(profileType == RoutingProfileType.DRIVING_HGV && HeavyVehicleAttributes.HGV == searchParams.getVehicleType()){
                    req.setWeighting("fastest");
                    req.getHints().put("weighting_method", "recommended_pref");
                    flexibleMode = true;
                }
            }

            if(profileType == RoutingProfileType.WHEELCHAIR) {
                flexibleMode = true;
            }

            if (RoutingProfileType.isDriving(profileType) && RealTrafficDataProvider.getInstance().isInitialized())
                req.setEdgeAnnotator(new TrafficEdgeAnnotator(mGraphHopper.getGraphHopperStorage()));

            req.setEdgeFilter(searchCntx.getEdgeFilter());
            req.setPathProcessor(routeProcCntx.getPathProcessor());

            if (searchParams.requiresDynamicWeights() || flexibleMode) {
                if (mGraphHopper.isCHEnabled())
                    req.getHints().put("ch.disable", true);
                if (mGraphHopper.getLMFactoryDecorator().isEnabled()) {
                    req.setAlgorithm("astarbi");
                    req.getHints().put("lm.disable", false);
                    req.getHints().put("core.disable", true);
                    req.getHints().put("ch.disable", true);
                }
                if (mGraphHopper.isCoreEnabled() && optimized) {
                    req.getHints().put("core.disable", false);
                    req.getHints().put("lm.disable", true);
                    req.getHints().put("ch.disable", true);
                    req.setAlgorithm("astarbi");
                }
            } else {
                if (mGraphHopper.isCHEnabled()) {
                    req.getHints().put("lm.disable", true);
                    req.getHints().put("core.disable", true);
                }
                else {
                    if (mGraphHopper.isCoreEnabled() && optimized) {
                        req.getHints().put("core.disable", false);
                        req.getHints().put("lm.disable", true);
                        req.getHints().put("ch.disable", true);
                        req.setAlgorithm("astarbi");
                    }
                    else {
                        req.getHints().put("ch.disable", true);
                        req.getHints().put("core.disable", true);
                    }
                }
            }
            //cannot use CH or CoreALT with avoid areas. Need to fallback to ALT with beeline approximator or Dijkstra
            if(props.getBool("avoid_areas", false)){
                req.setAlgorithm("astarbi");
                req.getHints().put("lm.disable", false);
                req.getHints().put("core.disable", true);
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
            RouteSearchContext searchCntx = createSearchContext(parameters.getRouteParameters(), RouteSearchMode.Isochrones, null);

            IsochroneMapBuilderFactory isochroneMapBuilderFactory = new IsochroneMapBuilderFactory(searchCntx);
            result = isochroneMapBuilderFactory.buildMap(parameters);

            endUseGH();
        } catch (Exception ex) {
            endUseGH();

            LOGGER.error(ex);

            throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "Unable to build an isochrone map.");
        }

        if (result.getIsochronesCount() > 0) {

            if (parameters.hasAttribute("total_pop")) {

                try {

                    Map<StatisticsProviderConfiguration, List<String>> mapProviderToAttrs = new HashMap<StatisticsProviderConfiguration, List<String>>();

                    StatisticsProviderConfiguration provConfig = IsochronesServiceSettings.getStatsProviders().get("total_pop");

                    if (provConfig != null) {
                        if (mapProviderToAttrs.containsKey(provConfig)) {
                            List<String> attrList = mapProviderToAttrs.get(provConfig);
                            attrList.add("total_pop");
                        } else {
                            List<String> attrList = new ArrayList<String>();
                            attrList.add("total_pop");
                            mapProviderToAttrs.put(provConfig, attrList);
                        }
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
                    String area_units = parameters.getAreaUnits();

                    if (area_units != null) units = area_units;

                    double area = isochrone.calcArea(units);

                    if (parameters.hasAttribute("area")) {

                        isochrone.setArea(area);

                    }

                    if (parameters.hasAttribute("reachfactor") && parameters.getRangeType() == TravelRangeType.Time) {

                        double reachfactor = isochrone.calcReachfactor(units);
                        isochrone.setReachfactor(reachfactor);

                    }

                }

            }

        }

        return result;
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
        return null;
    }

    public int hashCode() {
        return mGraphHopper.getGraphHopperStorage().getDirectory().getLocation().hashCode();
    }
}
