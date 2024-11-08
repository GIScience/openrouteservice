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

import com.graphhopper.GHResponse;
import com.graphhopper.util.AngleCalc;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.PointList;
import com.graphhopper.util.exceptions.MaximumNodesExceededException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.isochrones.IsochroneMap;
import org.heigit.ors.isochrones.IsochroneSearchParameters;
import org.heigit.ors.routing.pathprocessors.ExtraInfoProcessor;
import org.heigit.ors.util.FormatUtility;
import org.heigit.ors.util.RuntimeUtility;
import org.heigit.ors.util.StringUtility;
import org.heigit.ors.util.TimeUtility;
import org.locationtech.jts.geom.Coordinate;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RoutingProfileManager {
    private static final Logger LOGGER = Logger.getLogger(RoutingProfileManager.class.getName());
    private static final String SOURCE_PROPERTY_NAME = "source_file";
    public static final String KEY_SKIPPED_EXTRA_INFO = "skipped_extra_info";
    private LinkedHashMap<String, RoutingProfile> routingProfiles = new LinkedHashMap<>();
    private static RoutingProfileManager instance;

    public RoutingProfileManager(EngineProperties config, String graphVersion) {
        if (instance == null) {
            instance = this;
            initialize(config, graphVersion);
        }
    }

    public static synchronized RoutingProfileManager getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("RoutingProfileManager has not been initialized!");
        }
        return instance;
    }

    public void initialize(EngineProperties config, String graphVersion) {
        RuntimeUtility.printRAMInfo("", LOGGER);
        long startTime = System.currentTimeMillis();
        routingProfiles = new LinkedHashMap<>();
        try {
            Map<String, ProfileProperties> profiles = config.getInitializedActiveProfiles();
            if (profiles.isEmpty()) {
                fail("No profiles configured. Exiting.");
                return;
            }
            int initializationThreads = config.getInitThreads();
            LOGGER.info("====> Initializing %d profiles (%d threads) ...".formatted(profiles.size(), initializationThreads));
            RoutingProfileLoadContext loadContext = new RoutingProfileLoadContext();
            ExecutorService executor = Executors.newFixedThreadPool(initializationThreads);
            ExecutorCompletionService<RoutingProfile> compService = new ExecutorCompletionService<>(executor);
            int nTotalTasks = 0;
            for (Map.Entry<String, ProfileProperties> profile : profiles.entrySet()) {
                if (profile.getValue().getProfilesTypes() != null && profile.getValue().getEnabled()) {
                    Callable<RoutingProfile> task = new RoutingProfileLoader(profile.getKey(), profile.getValue(), config, graphVersion, loadContext);
                    compService.submit(task);
                    nTotalTasks++;
                }
            }
            LOGGER.info("%d profile configurations submitted as tasks.".formatted(nTotalTasks));

            int nCompletedTasks = 0;
            while (nCompletedTasks < nTotalTasks) {
                Future<RoutingProfile> future = compService.take();

                try {
                    RoutingProfile rp = future.get();
                    nCompletedTasks++;
                    routingProfiles.put(rp.getProfileName(), rp);
                } catch (ExecutionException e) {
                    LOGGER.debug(e);
                    if (ExceptionUtils.indexOfThrowable(e, FileNotFoundException.class) != -1) {
                        throw new IllegalStateException("Output files can not be written. Make sure ors.engine.graphs_data_access is set to a writable type! ");
                    }
                    throw e;
                } catch (InterruptedException e) {
                    LOGGER.debug(e);
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
            executor.shutdown();
            loadContext.releaseElevationProviderCacheAfterAllVehicleProfilesHaveBeenProcessed();

            LOGGER.info("Total time: " + TimeUtility.getElapsedTime(startTime, true) + ".");
            LOGGER.info("========================================================================");
            RoutingProfileManagerStatus.setReady(true);
        } catch (ExecutionException ex) {
            fail("ExecutionException while initializing RoutingProfileManager: " + ex.getMessage());
            Thread.currentThread().interrupt();
            return;
        } catch (Exception ex) {
            fail("Unhandled exception at RoutingProfileManager initialization: " + ex.getMessage());
            Thread.currentThread().interrupt();
            System.exit(1);
        }
        RuntimeUtility.clearMemory(LOGGER);
        if (LOGGER.isInfoEnabled())
            printStatistics();
    }

    private void printStatistics() {
        LOGGER.info("====> Memory usage by profiles:");
        long totalUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long totalProfilesMemory = 0;

        int i = 0;
        for (RoutingProfile profile : getUniqueProfiles()) {
            i++;
            long profileMemory = profile.getMemoryUsage();
            totalProfilesMemory += profileMemory;
            LOGGER.info("[%d] %s (%.1f%%)".formatted(i, RuntimeUtility.getMemorySize(profileMemory), ((double) profileMemory / totalUsedMemory) * 100));
        }
        LOGGER.info("Total: %s (%.1f%%)".formatted(RuntimeUtility.getMemorySize(totalProfilesMemory), ((double) totalProfilesMemory / totalUsedMemory) * 100));
        LOGGER.info("========================================================================");
    }

    public RoutingProfile getRoutingProfile(String profileName) {
        if (!routingProfiles.containsKey(profileName))
            return null;
        return routingProfiles.get(profileName);
    }

    public List<RoutingProfile> getUniqueProfiles() {
        return new ArrayList<>(routingProfiles.values());
    }

    public void destroy() {
        for (RoutingProfile rp : routingProfiles.values()) {
            rp.close();
        }
        instance = null;
    }

    private void fail(String message) {
        LOGGER.error("");
        LOGGER.error(message);
        LOGGER.error("");
        RoutingProfileManagerStatus.setFailed(true);
        RoutingProfileManagerStatus.setShutdown(true);
    }

    public RouteResult[] computeRoundTripRoute(RoutingRequest req) throws Exception {
        List<GHResponse> routes = new ArrayList<>();

        RoutingProfile rp = getRouteProfileForRequest(req, false);
        RouteSearchParameters searchParams = req.getSearchParameters();
        ProfileProperties profileProperties = rp.getProfileConfiguration();

        if (profileProperties.getService().getMaximumDistanceRoundTripRoutes() != null && profileProperties.getService().getMaximumDistanceRoundTripRoutes() < searchParams.getRoundTripLength()) {
            throw new ServerLimitExceededException(
                    RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT,
                    "The requested route length must not be greater than %s meters.".formatted(profileProperties.getService().getMaximumDistanceRoundTripRoutes())
            );
        }

        Coordinate[] coords = req.getCoordinates();
        Coordinate c0 = coords[0];

        ExtraInfoProcessor extraInfoProcessor = null;

        WayPointBearing bearing = null;
        if (searchParams.getBearings() != null) {
            bearing = searchParams.getBearings()[0];
        }

        GHResponse gr = req.computeRoundTripRoute(c0.y, c0.x, bearing, searchParams, req.getGeometrySimplify(), rp);

        if (gr.hasErrors()) {
            if (!gr.getErrors().isEmpty()) {
                if (gr.getErrors().get(0) instanceof com.graphhopper.util.exceptions.ConnectionNotFoundException) {
                    throw new RouteNotFoundException(
                            RoutingErrorCodes.ROUTE_NOT_FOUND,
                            "Unable to find a route for point (%s).".formatted(FormatUtility.formatCoordinate(c0))
                    );
                } else if (gr.getErrors().get(0) instanceof com.graphhopper.util.exceptions.PointNotFoundException) {
                    StringBuilder message = new StringBuilder();
                    for (Throwable error : gr.getErrors()) {
                        if (!message.isEmpty())
                            message.append("; ");
                        message.append(error.getMessage());
                    }
                    throw new PointNotFoundException(message.toString());
                } else {
                    throw new InternalServerException(RoutingErrorCodes.UNKNOWN, gr.getErrors().get(0).getMessage());
                }
            } else {
                // If there are no errors stored but there is indication that there are errors, something strange
                // has happened, so return that a route could not be found
                throw new RouteNotFoundException(
                        RoutingErrorCodes.ROUTE_NOT_FOUND,
                        "Unable to find a route for point (%s).".formatted(
                                FormatUtility.formatCoordinate(c0)
                        ));
            }
        }

        try {
            for (Object obj : gr.getReturnObjects()) {
                if (obj instanceof ExtraInfoProcessor processor) {
                    if (extraInfoProcessor == null) {
                        extraInfoProcessor = processor;
                        if (!StringUtility.isNullOrEmpty(processor.getSkippedExtraInfo())) {
                            gr.getHints().putObject(KEY_SKIPPED_EXTRA_INFO, processor.getSkippedExtraInfo());
                        }
                    } else {
                        extraInfoProcessor.appendData(processor);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }

        routes.add(gr);

        List<RouteExtraInfo> extraInfos = extraInfoProcessor != null ? extraInfoProcessor.getExtras() : null;
        return new RouteResultBuilder().createRouteResults(routes, req, new List[]{extraInfos});
    }

    public RouteResult[] computeRoute(RoutingRequest req) throws Exception {
        if (req.getSearchParameters().getRoundTripLength() > 0) {
            return computeRoundTripRoute(req);
        } else {
            return computeLinearRoute(req);
        }
    }

    public RouteResult[] computeLinearRoute(RoutingRequest req) throws Exception {
        List<Integer> skipSegments = req.getSkipSegments();
        List<GHResponse> routes = new ArrayList<>();

        RoutingProfile rp = getRouteProfileForRequest(req, false);
        RouteSearchParameters searchParams = req.getSearchParameters();

        Coordinate[] coords = req.getCoordinates();
        Coordinate c0 = coords[0];
        Coordinate c1;
        int nSegments = coords.length - 1;
        GHResponse prevResp = null;
        WayPointBearing[] bearings = (req.getContinueStraight() || searchParams.getBearings() != null) ? new WayPointBearing[2] : null;
        String profileName = req.getSearchParameters().getProfileName();
        double[] radiuses = null;

        if (req.getSearchParameters().getAlternativeRoutesCount() > 1 && coords.length > 2) {
            throw new InternalServerException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "Alternative routes algorithm does not support more than two way points.");
        }

        int numberOfExpectedExtraInfoProcessors = req.getSearchParameters().getAlternativeRoutesCount() < 0 ? 1 : req.getSearchParameters().getAlternativeRoutesCount();
        ExtraInfoProcessor[] extraInfoProcessors = new ExtraInfoProcessor[numberOfExpectedExtraInfoProcessors];

        for (int i = 1; i <= nSegments; ++i) {
            c1 = coords[i];

            if (bearings != null) {
                bearings[0] = null;
                if (prevResp != null && req.getContinueStraight()) {
                    bearings[0] = new WayPointBearing(getHeadingDirection(prevResp));
                }

                if (searchParams.getBearings() != null) {
                    bearings[0] = searchParams.getBearings()[i - 1];
                    bearings[1] = (i == nSegments && searchParams.getBearings().length != nSegments + 1) ? new WayPointBearing(Double.NaN) : searchParams.getBearings()[i];
                }
            }

            if (searchParams.getMaximumRadiuses() != null) {
                radiuses = new double[2];
                radiuses[0] = searchParams.getMaximumRadiuses()[i - 1];
                radiuses[1] = searchParams.getMaximumRadiuses()[i];
            } else {
                try {
                    int maximumSnappingRadius = getRoutingProfile(profileName).getProfileConfiguration().getService().getMaximumSnappingRadius();
                    radiuses = new double[2];
                    radiuses[0] = maximumSnappingRadius;
                    radiuses[1] = maximumSnappingRadius;
                } catch (Exception ex) {
                    // do nothing
                }
            }

            GHResponse gr = req.computeRoute(c0.y, c0.x, c1.y, c1.x, bearings, radiuses, skipSegments.contains(i), searchParams, req.getGeometrySimplify(), rp);

            if (gr.hasErrors()) {
                if (!gr.getErrors().isEmpty()) {
                    if (gr.getErrors().get(0) instanceof com.graphhopper.util.exceptions.ConnectionNotFoundException ex) {
                        Map<String, Object> details = ex.getDetails();
                        if (!details.isEmpty()) {
                            int code = RoutingErrorCodes.ROUTE_NOT_FOUND;
                            if (details.containsKey("entry_not_reached") && details.containsKey("exit_not_reached")) {
                                code = RoutingErrorCodes.PT_NOT_REACHED;
                            } else if (details.containsKey("entry_not_reached")) {
                                code = RoutingErrorCodes.PT_ENTRY_NOT_REACHED;
                            } else if (details.containsKey("exit_not_reached")) {
                                code = RoutingErrorCodes.PT_EXIT_NOT_REACHED;
                            } else if (details.containsKey("combined_not_reached")) {
                                code = RoutingErrorCodes.PT_ROUTE_NOT_FOUND;
                            }
                            throw new RouteNotFoundException(
                                    code,
                                    "Unable to find a route between points %d (%s) and %d (%s). %s".formatted(
                                            i,
                                            FormatUtility.formatCoordinate(c0),
                                            i + 1,
                                            FormatUtility.formatCoordinate(c1),
                                            details.values().stream().map(Object::toString).collect(Collectors.joining(" "))
                                    )
                            );
                        }
                        throw new RouteNotFoundException(
                                RoutingErrorCodes.ROUTE_NOT_FOUND,
                                "Unable to find a route between points %d (%s) and %d (%s).".formatted(
                                        i,
                                        FormatUtility.formatCoordinate(c0),
                                        i + 1,
                                        FormatUtility.formatCoordinate(c1)
                                )
                        );
                    } else if (gr.getErrors().get(0) instanceof com.graphhopper.util.exceptions.MaximumNodesExceededException ex) {
                        Map<String, Object> details = ex.getDetails();
                        throw new RouteNotFoundException(
                                RoutingErrorCodes.PT_MAX_VISITED_NODES_EXCEEDED,
                                "Unable to find a route between points %d (%s) and %d (%s). Maximum number of nodes exceeded: %s".formatted(
                                        i,
                                        FormatUtility.formatCoordinate(c0),
                                        i + 1,
                                        FormatUtility.formatCoordinate(c1),
                                        details.get(MaximumNodesExceededException.NODES_KEY).toString()
                                )
                        );
                    } else if (gr.getErrors().get(0) instanceof com.graphhopper.util.exceptions.PointNotFoundException) {
                        StringBuilder message = new StringBuilder();
                        for (Throwable error : gr.getErrors()) {
                            if (!message.isEmpty())
                                message.append("; ");
                            if (error instanceof com.graphhopper.util.exceptions.PointNotFoundException pointNotFoundException) {
                                int pointReference = (i - 1) + pointNotFoundException.getPointIndex();

                                Coordinate pointCoordinate = (pointNotFoundException.getPointIndex() == 0) ? c0 : c1;
                                assert radiuses != null;
                                double pointRadius = radiuses[pointNotFoundException.getPointIndex()];

                                // -1 is used to indicate the use of internal limits instead of specifying it in the request.
                                // we should therefore let them know that they are already using the limit.
                                if (pointRadius == -1) {
                                    pointRadius = getRoutingProfile(profileName).getProfileConfiguration().getService().getMaximumSnappingRadius();
                                    message.append("Could not find routable point within the maximum possible radius of %.1f meters of specified coordinate %d: %s.".formatted(
                                            pointRadius,
                                            pointReference,
                                            FormatUtility.formatCoordinate(pointCoordinate)));
                                } else {
                                    message.append("Could not find routable point within a radius of %.1f meters of specified coordinate %d: %s.".formatted(
                                            pointRadius,
                                            pointReference,
                                            FormatUtility.formatCoordinate(pointCoordinate)));
                                }

                            } else {
                                message.append(error.getMessage());
                            }
                        }
                        throw new PointNotFoundException(message.toString());
                    } else {
                        throw new InternalServerException(RoutingErrorCodes.UNKNOWN, gr.getErrors().get(0).getMessage());
                    }
                } else {
                    // If there are no errors stored but there is indication that there are errors, something strange
                    // has happened, so return that a route could not be found
                    throw new RouteNotFoundException(
                            RoutingErrorCodes.ROUTE_NOT_FOUND,
                            "Unable to find a route between points %d (%s) and %d (%s).".formatted(
                                    i,
                                    FormatUtility.formatCoordinate(c0),
                                    i + 1,
                                    FormatUtility.formatCoordinate(c1))
                    );
                }
            }

            if (numberOfExpectedExtraInfoProcessors > 1) {
                int extraInfoProcessorIndex = 0;
                for (Object o : gr.getReturnObjects()) {
                    if (o instanceof ExtraInfoProcessor processor) {
                        extraInfoProcessors[extraInfoProcessorIndex] = processor;
                        extraInfoProcessorIndex++;
                        if (!StringUtility.isNullOrEmpty(processor.getSkippedExtraInfo())) {
                            gr.getHints().putObject(KEY_SKIPPED_EXTRA_INFO, processor.getSkippedExtraInfo());
                        }
                    }
                }
            } else {
                for (Object o : gr.getReturnObjects()) {
                    if (o instanceof ExtraInfoProcessor processor) {
                        if (extraInfoProcessors[0] == null) {
                            extraInfoProcessors[0] = processor;
                            if (!StringUtility.isNullOrEmpty(processor.getSkippedExtraInfo())) {
                                gr.getHints().putObject(KEY_SKIPPED_EXTRA_INFO, processor.getSkippedExtraInfo());
                            }
                        } else {
                            extraInfoProcessors[0].appendData(processor);
                        }
                    }
                }
            }

            prevResp = gr;
            routes.add(gr);
            c0 = c1;
        }
        routes = enrichDirectRoutesTime(routes);

        List<RouteExtraInfo>[] extraInfos = new List[numberOfExpectedExtraInfoProcessors];
        int i = 0;
        for (ExtraInfoProcessor e : extraInfoProcessors) {
            extraInfos[i] = e != null ? e.getExtras() : null;
            i++;
        }
        return new RouteResultBuilder().createRouteResults(routes, req, extraInfos);
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
            return AngleCalc.ANGLE_CALC.calcAzimuth(lat1, lon1, lat2, lon2);
        } else
            return 0;
    }

    public RoutingProfile getRouteProfileForRequest(RoutingRequest req, boolean oneToMany) throws Exception {
        RouteSearchParameters searchParams = req.getSearchParameters();
        String profileName = searchParams.getProfileName();

        boolean fallbackAlgorithm = searchParams.requiresFullyDynamicWeights();
        boolean dynamicWeights = searchParams.requiresDynamicPreprocessedWeights();
        boolean useAlternativeRoutes = searchParams.getAlternativeRoutesCount() > 1;

        RoutingProfile rp = getRoutingProfile(profileName);

        if (rp == null)
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Unable to get an appropriate routing profile for the name " + profileName + ".");

        ProfileProperties profileProperties = rp.getProfileConfiguration();

        if (profileProperties.getService().getMaximumDistance() != null
                || dynamicWeights && profileProperties.getService().getMaximumDistanceDynamicWeights() != null
                || profileProperties.getService().getMaximumWayPoints() != null
                || fallbackAlgorithm && profileProperties.getService().getMaximumDistanceAvoidAreas() != null
        ) {
            Coordinate[] coords = req.getCoordinates();
            int nCoords = coords.length;
            if (profileProperties.getService().getMaximumWayPoints() > 0 && !oneToMany && nCoords > profileProperties.getService().getMaximumWayPoints()) {
                throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "The specified number of waypoints must not be greater than " + profileProperties.getService().getMaximumWayPoints() + ".");
            }

            if (profileProperties.getService().getMaximumDistance() != null
                    || dynamicWeights && profileProperties.getService().getMaximumDistanceDynamicWeights() != null
                    || fallbackAlgorithm && profileProperties.getService().getMaximumDistanceAvoidAreas() != null
            ) {
                DistanceCalc distCalc = DistanceCalcEarth.DIST_EARTH;

                List<Integer> skipSegments = req.getSkipSegments();
                Coordinate c0 = coords[0];
                Coordinate c1;
                double totalDist = 0.0;

                if (oneToMany) {
                    for (int i = 1; i < nCoords; i++) {
                        c1 = coords[i];
                        totalDist = distCalc.calcDist(c0.y, c0.x, c1.y, c1.x);
                    }
                } else {
                    for (int i = 1; i < nCoords; i++) {
                        c1 = coords[i];
                        if (!skipSegments.contains(i)) { // ignore skipped segments
                            totalDist += distCalc.calcDist(c0.y, c0.x, c1.y, c1.x);
                        }
                        c0 = c1;
                    }
                }

                if (profileProperties.getService().getMaximumDistance() != null && totalDist > profileProperties.getService().getMaximumDistance())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "The approximated route distance must not be greater than %s meters.".formatted(profileProperties.getService().getMaximumDistance()));
                if (dynamicWeights && profileProperties.getService().getMaximumDistanceDynamicWeights() != null && totalDist > profileProperties.getService().getMaximumDistanceDynamicWeights())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "By dynamic weighting, the approximated distance of a route segment must not be greater than %s meters.".formatted(profileProperties.getService().getMaximumDistanceDynamicWeights()));
                if (fallbackAlgorithm && profileProperties.getService().getMaximumDistanceAvoidAreas() != null && totalDist > profileProperties.getService().getMaximumDistanceAvoidAreas())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "With these options, the approximated route distance must not be greater than %s meters.".formatted(profileProperties.getService().getMaximumDistanceAvoidAreas()));
                if (useAlternativeRoutes && profileProperties.getService().getMaximumDistanceAlternativeRoutes() != null && totalDist > profileProperties.getService().getMaximumDistanceAlternativeRoutes())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "The approximated route distance must not be greater than %s meters for use with the alternative Routes algorithm.".formatted(profileProperties.getService().getMaximumDistanceAlternativeRoutes()));
            }
        }

        if (searchParams.hasMaximumSpeed() && profileProperties.getBuild().getMaximumSpeedLowerBound() != null) {
            if (searchParams.getMaximumSpeed() < profileProperties.getBuild().getMaximumSpeedLowerBound()) {
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequestParameterNames.PARAM_MAXIMUM_SPEED, String.valueOf(searchParams.getMaximumSpeed()), "The maximum speed must not be lower than " + profileProperties.getBuild().getMaximumSpeedLowerBound() + " km/h.");
            }
            if (RoutingProfileCategory.getFromEncoder(rp.getGraphhopper().getEncodingManager()) != RoutingProfileCategory.DRIVING) {
                throw new ParameterValueException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, "The maximum speed feature can only be used with cars and heavy vehicles.");
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
        RoutingProfile rp = getRoutingProfile(parameters.getRouteParameters().getProfileName());
        return rp.buildIsochrone(parameters);
    }

}
