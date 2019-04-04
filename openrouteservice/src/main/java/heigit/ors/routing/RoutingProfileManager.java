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

import com.graphhopper.GHResponse;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.exceptions.PointNotFoundException;
import heigit.ors.exceptions.RouteNotFoundException;
import heigit.ors.exceptions.ServerLimitExceededException;
import heigit.ors.isochrones.IsochroneMap;
import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.mapmatching.MapMatchingRequest;
import heigit.ors.matrix.MatrixErrorCodes;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.optimization.OptimizationErrorCodes;
import heigit.ors.optimization.RouteOptimizationRequest;
import heigit.ors.optimization.RouteOptimizationResult;
import heigit.ors.routing.configuration.RouteProfileConfiguration;
import heigit.ors.routing.configuration.RoutingManagerConfiguration;
import heigit.ors.routing.pathprocessors.ElevationSmoothPathProcessor;
import heigit.ors.routing.pathprocessors.ExtraInfoProcessor;
import heigit.ors.routing.traffic.RealTrafficDataProvider;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.util.FormatUtility;
import heigit.ors.util.RuntimeUtility;
import heigit.ors.util.StringUtility;
import heigit.ors.util.TimeUtility;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class RoutingProfileManager {
    private static final Logger LOGGER = Logger.getLogger(RoutingProfileManager.class.getName());

    private RoutingProfilesCollection _routeProfiles;
    private RoutingProfilesUpdater _profileUpdater;
    private static RoutingProfileManager mInstance;

    public static synchronized RoutingProfileManager getInstance() throws IOException {
        if (mInstance == null) {
            mInstance = new RoutingProfileManager();
            mInstance.initialize(null);
        }

        return mInstance;
    }

    public RoutingProfileManager() {
    }

    public void prepareGraphs(String graphProps) {
        long startTime = System.currentTimeMillis();

        try {
			/* 
			RoutingManagerConfiguration rmc = RoutingManagerConfiguration.loadFromFile(graphProps);
			RoutingProfilesCollection coll = new RoutingProfilesCollection();
			RoutingProfileLoadContext loadCntx = new RoutingProfileLoadContext();
			int nRouteInstances = rmc.Profiles.length;

			for (int i = 0; i < nRouteInstances; i++) {
				RouteProfileConfiguration rpc = rmc.Profiles[i];
				if (!rpc.getEnabled())
					continue;

				LOGGER.info("Preparing route profile in "  + rpc.getGraphPath() + " ...");

				RoutingProfile rp = new RoutingProfile(RoutingServiceSettings.getSourceFile(), rpc, coll, loadCntx);

				rp.close();

				LOGGER.info("Done.");
			}

			loadCntx.release();

			*/

            RoutingManagerConfiguration rmc = RoutingManagerConfiguration.loadFromFile(graphProps);

            _routeProfiles = new RoutingProfilesCollection();
            int nRouteInstances = rmc.Profiles.length;

            RoutingProfileLoadContext loadCntx = new RoutingProfileLoadContext(RoutingServiceSettings.getInitializationThreads());
            ExecutorService executor = Executors.newFixedThreadPool(RoutingServiceSettings.getInitializationThreads());
            ExecutorCompletionService<RoutingProfile> compService = new ExecutorCompletionService<RoutingProfile>(executor);

            int nTotalTasks = 0;

            for (int i = 0; i < nRouteInstances; i++) {
                RouteProfileConfiguration rpc = rmc.Profiles[i];
                if (!rpc.getEnabled())
                    continue;

                Integer[] routeProfiles = rpc.getProfilesTypes();

                if (routeProfiles != null) {
                    Callable<RoutingProfile> task = new RoutingProfileLoader(RoutingServiceSettings.getSourceFile(), rpc,
                            _routeProfiles, loadCntx);
                    compService.submit(task);
                    nTotalTasks++;
                }
            }

            LOGGER.info("               ");

            int nCompletedTasks = 0;
            while (nCompletedTasks < nTotalTasks) {
                Future<RoutingProfile> future = compService.take();

                try {
                    RoutingProfile rp = future.get();
                    nCompletedTasks++;
                    rp.close();
                    LOGGER.info("Graph preparation done.");
                } catch (InterruptedException e) {
                    LOGGER.error(e);
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    LOGGER.error(e);
                    e.printStackTrace();
                }
            }

            executor.shutdown();
            loadCntx.releaseElevationProviderCacheAfterAllVehicleProfilesHaveBeenProcessed();


            LOGGER.info("Graphs were prepaired in " + TimeUtility.getElapsedTime(startTime, true) + ".");
        } catch (Exception ex) {
            LOGGER.error("Failed to prepare graphs.", ex);
        }

        RuntimeUtility.clearMemory(LOGGER);
    }

    public void initialize(String graphProps) {
        RuntimeUtility.printRAMInfo("", LOGGER);

        LOGGER.info("      ");

        long startTime = System.currentTimeMillis();

        try {
            if (RoutingServiceSettings.getEnabled()) {
                RoutingManagerConfiguration rmc = RoutingManagerConfiguration.loadFromFile(graphProps);

                LOGGER.info(String.format("====> Initializing profiles from '%s' (%d threads) ...", RoutingServiceSettings.getSourceFile(), RoutingServiceSettings.getInitializationThreads()));
                LOGGER.info("                              ");

                // MARQ24 MOD START
                // RAMDataAccess.LZ4_COMPRESSION_ENABLED = "LZ4".equalsIgnoreCase(RoutingServiceSettings.getStorageFormat());
                // the ExGHOverwrite-FlagEncoder package contains the previously overwritten flagencoders of graphhopper
                // in the ors fork...
                // MARQ24 removed 'ExGhORSBikeCommonFlagEncoder.SKIP_WAY_TYPE_INFO' because we will use the NextGen
                // BikeFlagEncoders from now on...
                // ExGhORSBikeCommonFlagEncoder.SKIP_WAY_TYPE_INFO = true;
                // MARQ24 MOD END

                if ("preparation".equalsIgnoreCase(RoutingServiceSettings.getWorkingMode())) {
                    prepareGraphs(graphProps);
                } else {
                    _routeProfiles = new RoutingProfilesCollection();
                    int nRouteInstances = rmc.Profiles.length;

                    RoutingProfileLoadContext loadCntx = new RoutingProfileLoadContext(RoutingServiceSettings.getInitializationThreads());
                    ExecutorService executor = Executors.newFixedThreadPool(RoutingServiceSettings.getInitializationThreads());
                    ExecutorCompletionService<RoutingProfile> compService = new ExecutorCompletionService<RoutingProfile>(executor);

                    int nTotalTasks = 0;

                    for (int i = 0; i < nRouteInstances; i++) {
                        RouteProfileConfiguration rpc = rmc.Profiles[i];
                        if (!rpc.getEnabled())
                            continue;

                        Integer[] routeProfiles = rpc.getProfilesTypes();

                        if (routeProfiles != null) {
                            Callable<RoutingProfile> task = new RoutingProfileLoader(RoutingServiceSettings.getSourceFile(), rpc,
                                    _routeProfiles, loadCntx);
                            compService.submit(task);
                            nTotalTasks++;
                        }
                    }

                    LOGGER.info("               ");

                    int nCompletedTasks = 0;
                    while (nCompletedTasks < nTotalTasks) {
                        Future<RoutingProfile> future = compService.take();

                        try {
                            RoutingProfile rp = future.get();
                            nCompletedTasks++;
                            if (!_routeProfiles.add(rp))
                                LOGGER.warn("Routing profile has already been added.");
                        } catch (InterruptedException e) {
                            LOGGER.error(e);
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            LOGGER.error(e);
                            e.printStackTrace();
                        }
                    }

                    executor.shutdown();
                    loadCntx.releaseElevationProviderCacheAfterAllVehicleProfilesHaveBeenProcessed();

                    LOGGER.info("Total time: " + TimeUtility.getElapsedTime(startTime, true) + ".");
                    LOGGER.info("========================================================================");

                    if (rmc.TrafficInfoConfig != null && rmc.TrafficInfoConfig.Enabled) {
                        RealTrafficDataProvider.getInstance().initialize(rmc, _routeProfiles);
                    }

                    if (rmc.UpdateConfig != null && rmc.UpdateConfig.Enabled) {
                        _profileUpdater = new RoutingProfilesUpdater(rmc.UpdateConfig, _routeProfiles);
                        _profileUpdater.start();
                    }
                }

                RoutingProfileManagerStatus.setReady(true);
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to initialize RoutingProfileManager instance.", ex);
        }

        RuntimeUtility.clearMemory(LOGGER);

        if (LOGGER.isInfoEnabled())
            _routeProfiles.printStatistics(LOGGER);
    }

    public void destroy() {
        if (_profileUpdater != null)
            _profileUpdater.destroy();

        if (RealTrafficDataProvider.getInstance().isInitialized())
            RealTrafficDataProvider.getInstance().destroy();

        _routeProfiles.destroy();
    }

    public RoutingProfilesCollection getProfiles() {
        return _routeProfiles;
    }

    public boolean updateEnabled() {
        return _profileUpdater != null;
    }

    public Date getNextUpdateTime() {
        return _profileUpdater == null ? new Date() : _profileUpdater.getNextUpdate();
    }

    public String getUpdatedStatus() {
        return _profileUpdater == null ? null : _profileUpdater.getStatus();
    }

    public List<RouteResult> computeRoutes(RoutingRequest req, boolean invertFlow, boolean oneToMany) throws Exception {
        if (req.getCoordinates().length <= 1)
            throw new Exception("Number of coordinates must be greater than 1.");

        List<RouteResult> routes = new ArrayList<RouteResult>(req.getCoordinates().length - 1);

        RoutingProfile rp = getRouteProfile(req, true);
        RouteSearchParameters searchParams = req.getSearchParameters();
        PathProcessor pathProcessor = null;

        if (req.getExtraInfo() > 0) {
            pathProcessor = new ExtraInfoProcessor(rp.getGraphhopper(), req);
        } else {
            if (req.getIncludeElevation())
                pathProcessor = new ElevationSmoothPathProcessor();
        }

        Coordinate[] coords = req.getCoordinates();
        Coordinate c0 = coords[0];
        int nSegments = coords.length - 1;
        RouteProcessContext routeProcCntx = new RouteProcessContext(pathProcessor);
        EdgeFilter customEdgeFilter = rp.createAccessRestrictionFilter(coords);
        List<GHResponse> resp = new ArrayList<GHResponse>();

        for (int i = 1; i <= nSegments; ++i) {
            if (pathProcessor != null)
                pathProcessor.setSegmentIndex(i - 1, nSegments);

            Coordinate c1 = coords[i];
            GHResponse gr = null;
            if (invertFlow)
                gr = rp.computeRoute(c0.y, c0.x, c1.y, c1.x, null, null, false, searchParams, customEdgeFilter, routeProcCntx, req.getGeometrySimplify());
            else
                gr = rp.computeRoute(c1.y, c1.x, c0.y, c0.x, null, null, false, searchParams, customEdgeFilter, routeProcCntx, req.getGeometrySimplify());

            //if (gr.hasErrors())
            //	throw new InternalServerException(RoutingErrorCodes.UNKNOWN, String.format("Unable to find a route between points %d (%s) and %d (%s)", i, FormatUtility.formatCoordinate(c0), i + 1, FormatUtility.formatCoordinate(c1)));

            if (!gr.hasErrors()) {
                resp.clear();
                resp.add(gr);
                RouteResult route = new RouteResultBuilder().createMergedRouteResultFromBestPaths(resp, req, (pathProcessor != null && (pathProcessor instanceof ExtraInfoProcessor)) ? ((ExtraInfoProcessor) pathProcessor).getExtras() : null);
                routes.add(route);
            } else
                routes.add(null);
        }

        return routes;
    }

    public RouteResult matchTrack(MapMatchingRequest req) throws Exception {
        //RoutingProfile rp = getRouteProfile(req, false);

        return null;
    }

    public RouteResult computeRoute(RoutingRequest req) throws Exception {
        List<Integer> skipSegments = req.getSkipSegments();
        List<GHResponse> routes = new ArrayList<GHResponse>();

        RoutingProfile rp = getRouteProfile(req, false);
        RouteSearchParameters searchParams = req.getSearchParameters();
        PathProcessor pathProcessor = null;

        pathProcessor = new ExtraInfoProcessor(rp.getGraphhopper(), req);

        Coordinate[] coords = req.getCoordinates();
        Coordinate c0 = coords[0];
        Coordinate c1;
        int nSegments = coords.length - 1;
        RouteProcessContext routeProcCntx = new RouteProcessContext(pathProcessor);
        EdgeFilter customEdgeFilter = rp.createAccessRestrictionFilter(coords);
        GHResponse prevResp = null;
        WayPointBearing[] bearings = (req.getContinueStraight() || searchParams.getBearings() != null) ? new WayPointBearing[2] : null;
        int profileType = req.getSearchParameters().getProfileType();
        double[] radiuses;
        if (searchParams.getMaximumRadiuses() != null) {
            radiuses = new double[2];
        } else if (_routeProfiles.getRouteProfile(profileType).getConfiguration().hasMaximumSnappingRadius()) {
            radiuses = new double[2];
        } else {
            radiuses = null;
        }

        for (int i = 1; i <= nSegments; ++i) {
            c1 = coords[i];

            if (pathProcessor != null)
                pathProcessor.setSegmentIndex(i - 1, nSegments);

            if (bearings != null) {
                bearings[0] = null;
                if (i > 1 && req.getContinueStraight()) {
                    bearings[0] = new WayPointBearing(getHeadingDirection(prevResp), Double.NaN);
                }

                if (searchParams.getBearings() != null) {
                    bearings[0] = searchParams.getBearings()[i - 1];
                    bearings[1] = (i == nSegments && searchParams.getBearings().length != nSegments + 1) ? new WayPointBearing(Double.NaN, Double.NaN) : searchParams.getBearings()[i];
                }
            }

            if (searchParams.getMaximumRadiuses() != null) {
                radiuses[0] = searchParams.getMaximumRadiuses()[i - 1];
                radiuses[1] = searchParams.getMaximumRadiuses()[i];
            }else {
                try {
                    int maximumSnappingRadius = _routeProfiles.getRouteProfile(profileType).getConfiguration().getMaximumSnappingRadius();
                    radiuses[0] = maximumSnappingRadius;
                    radiuses[1] = maximumSnappingRadius;
                 } catch (Exception ex) {
                }

            }

            GHResponse gr;
            if ((skipSegments.contains(i))) {
                gr = rp.computeRoute(c0.y, c0.x, c1.y, c1.x, bearings, radiuses, true, searchParams, customEdgeFilter, routeProcCntx, req.getGeometrySimplify());
            } else {
                gr = rp.computeRoute(c0.y, c0.x, c1.y, c1.x, bearings, radiuses, false, searchParams, customEdgeFilter, routeProcCntx, req.getGeometrySimplify());
            }

            if (gr.hasErrors()) {
                if (gr.getErrors().size() > 0) {
                    if (gr.getErrors().get(0) instanceof com.graphhopper.util.exceptions.ConnectionNotFoundException) {
                        throw new RouteNotFoundException(
                                RoutingErrorCodes.ROUTE_NOT_FOUND,
                                String.format("Unable to find a route between points %d (%s) and %d (%s).",
                                        i,
                                        FormatUtility.formatCoordinate(c0),
                                        i + 1,
                                        FormatUtility.formatCoordinate(c1))
                        );
                    } else if(gr.getErrors().get(0) instanceof com.graphhopper.util.exceptions.PointNotFoundException) {
                        String message = "";
                        for(Throwable error: gr.getErrors()) {
                            if(!StringUtility.isEmpty(message))
                                message = message + "; ";
                            message = message + error.getMessage();
                        }
                        throw new PointNotFoundException(message);
                    } else {
                        throw new InternalServerException(RoutingErrorCodes.UNKNOWN, gr.getErrors().get(0).getMessage());
                    }
                } else {
                    // If there are no errors stored but there is indication that there are errors, something strange
                    // has happened, so return that a route could not be found
                    throw new RouteNotFoundException(
                            RoutingErrorCodes.ROUTE_NOT_FOUND,
                            String.format("Unable to find a route between points %d (%s) and %d (%s).",
                                    i,
                                    FormatUtility.formatCoordinate(c0),
                                    i + 1,
                                    FormatUtility.formatCoordinate(c1))
                    );
                }
            }

            prevResp = gr;
            routes.add(gr);
            c0 = c1;
        }
        routes = enrichDirectRoutesTime(routes);
        return new RouteResultBuilder().createMergedRouteResultFromBestPaths(routes, req, (pathProcessor != null && (pathProcessor instanceof ExtraInfoProcessor)) ? ((ExtraInfoProcessor) pathProcessor).getExtras() : null);
    }

    /**
     * This will enrich all direct routes with an approximated travel time that is being calculated from the real graphhopper
     * results. The routes object should contain all routes, so the function can maintain and return the proper order!
     *
     * @param routes Should hold all the routes that have been calculated, not only the direct routes.
     * @return will return routes object with enriched direct routes if any we're found in the same order as the input object.
     */
    private List<GHResponse> enrichDirectRoutesTime(List<GHResponse> routes) {
        List<GHResponse> graphhopperRoutes = new ArrayList<>();
        List<GHResponse> directRoutes = new ArrayList<>();
        long graphHopperTravelTime = 0;
        double graphHopperTravelDistance = 0;
        double averageTravelTimePerMeter;

        for (GHResponse ghResponse : routes) {
            if (!ghResponse.getHints().has("skipped_segment")) {
                graphHopperTravelDistance += ghResponse.getBest().getDistance();
                graphHopperTravelTime += ghResponse.getBest().getTime();
                graphhopperRoutes.add(ghResponse);
            } else {
                directRoutes.add(ghResponse);
            }
        }

        if (graphhopperRoutes.isEmpty() || directRoutes.isEmpty()) {
            return routes;
        }

        if (graphHopperTravelDistance == 0) {
            return routes;
        }

        averageTravelTimePerMeter = graphHopperTravelTime / graphHopperTravelDistance;
        for (GHResponse ghResponse : routes) {
            if (ghResponse.getHints().has("skipped_segment")) {
                double directRouteDistance = ghResponse.getBest().getDistance();
                ghResponse.getBest().setTime(Math.round(directRouteDistance * averageTravelTimePerMeter));
                double directRouteInstructionDistance = ghResponse.getBest().getInstructions().get(0).getDistance();
                ghResponse.getBest().getInstructions().get(0).setTime(Math.round(directRouteInstructionDistance * averageTravelTimePerMeter));
            }
        }

        return routes;
    }

    private double getHeadingDirection(GHResponse resp) {
        PointList points = resp.getBest().getPoints();
        int nPoints = points.size();
        if (nPoints > 1) {
            double lon1 = points.getLon(nPoints - 2);
            double lat1 = points.getLat(nPoints - 2);
            double lon2 = points.getLon(nPoints - 1);
            double lat2 = points.getLat(nPoints - 1);
            // For some reason, GH may return a response where the last two points are identical
            if (lon1 == lon2 && lat1 == lat2 && nPoints > 2) {
                lon1 = points.getLon(nPoints - 3);
                lat1 = points.getLat(nPoints - 3);
            }
            return Helper.ANGLE_CALC.calcAzimuth(lat1, lon1, lat2, lon2);
        } else
            return 0;
    }

    public RoutingProfile getRouteProfile(RoutingRequest req, boolean oneToMany) throws Exception {
        RouteSearchParameters searchParams = req.getSearchParameters();
        int profileType = searchParams.getProfileType();

        boolean hasAvoidAreas = searchParams.hasAvoidAreas();
        boolean dynamicWeights = searchParams.requiresDynamicWeights();

        RoutingProfile rp = _routeProfiles.getRouteProfile(profileType, !dynamicWeights);

        if (rp == null && dynamicWeights == false)
            rp = _routeProfiles.getRouteProfile(profileType, false);

        if (rp == null)
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Unable to get an appropriate route profile for RoutePreference = " + RoutingProfileType.getName(req.getSearchParameters().getProfileType()));

        RouteProfileConfiguration config = rp.getConfiguration();

        if (config.getMaximumDistance() > 0
                || (dynamicWeights && config.getMaximumDistanceDynamicWeights() > 0)
                || config.getMaximumWayPoints() > 0
                || (hasAvoidAreas && config.getMaximumDistanceAvoidAreas() > 0)) {
            Coordinate[] coords = req.getCoordinates();
            int nCoords = coords.length;
            if (config.getMaximumWayPoints() > 0) {
                if (!oneToMany && nCoords > config.getMaximumWayPoints())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "The specified number of waypoints must not be greater than " + Integer.toString(config.getMaximumWayPoints()) + ".");
            }

            if (config.getMaximumDistance() > 0
                    || (dynamicWeights && config.getMaximumDistanceDynamicWeights() > 0)
                    || (hasAvoidAreas && config.getMaximumDistanceAvoidAreas() > 0)) {
                double longestSegmentDist = 0.0;
                DistanceCalc distCalc = Helper.DIST_EARTH;

                Coordinate c0 = coords[0], c1 = null;
                double totalDist = 0.0;

                if (oneToMany) {
                    for (int i = 1; i < nCoords; i++) {
                        c1 = coords[i];
                        totalDist = distCalc.calcDist(c0.y, c0.x, c1.y, c1.x);
                        if (totalDist > longestSegmentDist)
                            longestSegmentDist = totalDist;
                    }
                } else {
                    if (nCoords == 2) {
                        c1 = coords[1];
                        totalDist = distCalc.calcDist(c0.y, c0.x, c1.y, c1.x);
                        longestSegmentDist = totalDist;
                    } else {
                        double dist = 0;
                        for (int i = 1; i < nCoords; i++) {
                            c1 = coords[i];
                            dist = distCalc.calcDist(c0.y, c0.x, c1.y, c1.x);
                            totalDist += dist;
                            if (dist > longestSegmentDist)
                                longestSegmentDist = dist;

                            c0 = c1;
                        }
                    }
                }

                if (config.getMaximumDistance() > 0 && totalDist > config.getMaximumDistance())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "The approximated route distance must not be greater than " + Double.toString(config.getMaximumDistance()) + " meters.");

                if (dynamicWeights && config.getMaximumDistanceDynamicWeights() > 0 && totalDist > config.getMaximumDistanceDynamicWeights())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "By dynamic weighting, the approximated distance of a route segment must not be greater than " + Double.toString(config.getMaximumDistanceDynamicWeights()) + " meters.");
                if (hasAvoidAreas && config.getMaximumDistanceAvoidAreas() > 0 && totalDist > config.getMaximumDistanceAvoidAreas())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "With areas to avoid, the approximated route distance must not be greater than " + Double.toString(config.getMaximumDistanceAvoidAreas()) + " meters.");
            }
        }

        return rp;
    }

    /**
     * This function sends the {@link IsochroneSearchParameters} together with the Attributes to the {@link RoutingProfile}.
     *
     * @param parameters The input is a {@link IsochroneSearchParameters}
     * @return Return is a {@link IsochroneMap} holding the calculated data plus statistical data if the attributes where set.
     * @throws Exception
     */
    public IsochroneMap buildIsochrone(IsochroneSearchParameters parameters) throws Exception {

        int profileType = parameters.getRouteParameters().getProfileType();
        RoutingProfile rp = _routeProfiles.getRouteProfile(profileType, false);

        return rp.buildIsochrone(parameters);
    }

    public MatrixResult computeMatrix(MatrixRequest req) throws Exception {
        RoutingProfile rp = _routeProfiles.getRouteProfile(req.getProfileType(), !req.getFlexibleMode());

        if (rp == null)
            throw new InternalServerException(MatrixErrorCodes.UNKNOWN, "Unable to find an appropriate routing profile.");

        return rp.computeMatrix(req);
    }

    public RouteOptimizationResult computeOptimizedRoutes(RouteOptimizationRequest req) throws Exception {
        RoutingProfile rp = _routeProfiles.getRouteProfile(req.getProfileType(), true);

        if (rp == null)
            throw new InternalServerException(OptimizationErrorCodes.UNKNOWN, "Unable to find an appropriate routing profile.");

        return rp.computeOptimizedRoutes(req);
    }
}
