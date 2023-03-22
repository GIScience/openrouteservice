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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.GHResponse;
import com.graphhopper.util.*;
import com.graphhopper.util.exceptions.ConnectionNotFoundException;
import com.graphhopper.util.exceptions.MaximumNodesExceededException;
import org.locationtech.jts.geom.Coordinate;
import org.apache.log4j.Logger;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.centrality.CentralityErrorCodes;
import org.heigit.ors.centrality.CentralityRequest;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.config.RoutingServiceSettings;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.export.ExportRequest;
import org.heigit.ors.export.ExportResult;
import org.heigit.ors.isochrones.IsochroneMap;
import org.heigit.ors.isochrones.IsochroneSearchParameters;
import org.heigit.ors.kafka.ORSKafkaConsumerMessageSpeedUpdate;
import org.heigit.ors.mapmatching.MapMatchingRequest;
import org.heigit.ors.matrix.MatrixErrorCodes;
import org.heigit.ors.matrix.MatrixRequest;
import org.heigit.ors.matrix.MatrixResult;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.heigit.ors.routing.configuration.RoutingManagerConfiguration;
import org.heigit.ors.routing.graphhopper.extensions.storages.ExpiringSpeedStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.pathprocessors.ExtraInfoProcessor;
import org.heigit.ors.util.FormatUtility;
import org.heigit.ors.util.RuntimeUtility;
import org.heigit.ors.util.StringUtility;
import org.heigit.ors.util.TimeUtility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RoutingProfileManager {
    private static final Logger LOGGER = Logger.getLogger(RoutingProfileManager.class.getName());
    public static final String KEY_SKIPPED_EXTRA_INFO = "skipped_extra_info";

    private RoutingProfilesCollection routeProfiles;
    private RoutingProfilesUpdater profileUpdater;
    private static RoutingProfileManager instance;
    private boolean initComplete = false;
    private final ObjectMapper mapper = new ObjectMapper();
    private long kafkaMessagesProcessed = 0;
    private long kafkaMessagesFailed = 0;
    public static final boolean KAFKA_DEBUG = false;

    public static synchronized RoutingProfileManager getInstance() {
        if (instance == null) {
            instance = new RoutingProfileManager();
            instance.initialize(null);
        }
        return instance;
    }

    public void prepareGraphs(String graphProps) {
        long startTime = System.currentTimeMillis();

        try {
            RoutingManagerConfiguration rmc = RoutingManagerConfiguration.loadFromFile(graphProps);

            routeProfiles = new RoutingProfilesCollection();
            int nRouteInstances = rmc.getProfiles().length;

            RoutingProfileLoadContext loadCntx = new RoutingProfileLoadContext();
            ExecutorService executor = Executors.newFixedThreadPool(RoutingServiceSettings.getInitializationThreads());
            ExecutorCompletionService<RoutingProfile> compService = new ExecutorCompletionService<>(executor);

            int nTotalTasks = 0;

            for (int i = 0; i < nRouteInstances; i++) {
                RouteProfileConfiguration rpc = rmc.getProfiles()[i];
                if (!rpc.getEnabled())
                    continue;

                Integer[] profilesTypes = rpc.getProfilesTypes();

                if (profilesTypes != null) {
                    Callable<RoutingProfile> task = new RoutingProfileLoader(RoutingServiceSettings.getSourceFile(), rpc, loadCntx);
                    compService.submit(task);
                    nTotalTasks++;
                }
            }

            LOGGER.info(String.format("%d tasks submitted.", nTotalTasks));

            int nCompletedTasks = 0;
            while (nCompletedTasks < nTotalTasks) {
                Future<RoutingProfile> future = compService.take();
                try {
                    RoutingProfile rp = future.get();
                    nCompletedTasks++;
                    rp.close();
                    LOGGER.info("Graph preparation done.");
                } catch (ExecutionException e) {
                    LOGGER.error(e);
                    throw e;
                } catch (InterruptedException e) {
                    LOGGER.error(e);
                    Thread.currentThread().interrupt();
                }
            }
            executor.shutdown();
            loadCntx.releaseElevationProviderCacheAfterAllVehicleProfilesHaveBeenProcessed();

            LOGGER.info("Graphs were prepared in " + TimeUtility.getElapsedTime(startTime, true) + ".");
        } catch (Exception ex) {
            LOGGER.error("Failed to prepare graphs.", ex);
            Thread.currentThread().interrupt();
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

                LOGGER.info(String.format("====> Initializing profiles from '%s' (%d threads) ...",
                        RoutingServiceSettings.getSourceFile(), RoutingServiceSettings.getInitializationThreads()));

                if ("preparation".equalsIgnoreCase(RoutingServiceSettings.getWorkingMode())) {
                    prepareGraphs(graphProps);
                } else {
                    routeProfiles = new RoutingProfilesCollection();
                    int nRouteInstances = rmc.getProfiles().length;

                    RoutingProfileLoadContext loadCntx = new RoutingProfileLoadContext();
                    ExecutorService executor = Executors.newFixedThreadPool(RoutingServiceSettings.getInitializationThreads());
                    ExecutorCompletionService<RoutingProfile> compService = new ExecutorCompletionService<>(executor);

                    int nTotalTasks = 0;

                    for (int i = 0; i < nRouteInstances; i++) {
                        RouteProfileConfiguration rpc = rmc.getProfiles()[i];
                        if (!rpc.getEnabled())
                            continue;

                        if (rpc.getProfilesTypes() != null) {
                            Callable<RoutingProfile> task = new RoutingProfileLoader(RoutingServiceSettings.getSourceFile(), rpc, loadCntx);
                            compService.submit(task);
                            nTotalTasks++;
                        }
                    }

                    LOGGER.info(String.format("%d tasks submitted.", nTotalTasks));

                    int nCompletedTasks = 0;
                    while (nCompletedTasks < nTotalTasks) {
                        Future<RoutingProfile> future = compService.take();

                        try {
                            RoutingProfile rp = future.get();
                            nCompletedTasks++;
                            if (!routeProfiles.add(rp))
                                LOGGER.warn("Routing profile has already been added.");
                        } catch (ExecutionException e) {
                            LOGGER.error(e);
                            throw e;
                        } catch (InterruptedException e) {
                            LOGGER.error(e);
                            Thread.currentThread().interrupt();
                        }
                    }

                    executor.shutdown();
                    loadCntx.releaseElevationProviderCacheAfterAllVehicleProfilesHaveBeenProcessed();

                    LOGGER.info("Total time: " + TimeUtility.getElapsedTime(startTime, true) + ".");
                    LOGGER.info("========================================================================");
                    initCompleted();

                    if (rmc.getUpdateConfig().getEnabled()) {
                        profileUpdater = new RoutingProfilesUpdater(rmc.getUpdateConfig(), routeProfiles);
                        profileUpdater.start();
                    }
                }

                RoutingProfileManagerStatus.setReady(true);
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to initialize RoutingProfileManager instance.", ex);
            Thread.currentThread().interrupt();
        }

        RuntimeUtility.clearMemory(LOGGER);

        if (LOGGER.isInfoEnabled())
            routeProfiles.printStatistics(LOGGER);
    }

    public void destroy() {
        if (profileUpdater != null)
            profileUpdater.destroy();

        routeProfiles.destroy();
    }

    public RoutingProfilesCollection getProfiles() {
        return routeProfiles;
    }

    public boolean updateEnabled() {
        return profileUpdater != null;
    }

    public Date getNextUpdateTime() {
        return profileUpdater == null ? new Date() : profileUpdater.getNextUpdate();
    }

    public String getUpdatedStatus() {
        return profileUpdater == null ? null : profileUpdater.getStatus();
    }

    public RouteResult matchTrack(MapMatchingRequest req) throws Exception {
        LOGGER.error("mapmatching not implemented. " + req);
        throw new UnsupportedOperationException("mapmatching not implemented. " + req);
    }

    public RouteResult[] computeRoundTripRoute(RoutingRequest req) throws Exception {
        List<GHResponse> routes = new ArrayList<>();

        RoutingProfile rp = getRouteProfile(req, false);
        RouteSearchParameters searchParams = req.getSearchParameters();
        RouteProfileConfiguration config = rp.getConfiguration();

        if (config.getMaximumDistanceRoundTripRoutes() != 0 && config.getMaximumDistanceRoundTripRoutes() < searchParams.getRoundTripLength()) {
            throw new ServerLimitExceededException(
                    RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT,
                    String.format("The requested route length must not be greater than %s meters.", config.getMaximumDistanceRoundTripRoutes())
            );
        }

        Coordinate[] coords = req.getCoordinates();
        Coordinate c0 = coords[0];

        ExtraInfoProcessor extraInfoProcessor = null;

        WayPointBearing bearing = null;
        if (searchParams.getBearings() != null) {
            bearing = searchParams.getBearings()[0];
        }

        GHResponse gr = rp.computeRoundTripRoute(c0.y, c0.x, bearing, searchParams, req.getGeometrySimplify());

        if (gr.hasErrors()) {
            if (!gr.getErrors().isEmpty()) {
                if (gr.getErrors().get(0) instanceof com.graphhopper.util.exceptions.ConnectionNotFoundException) {
                    throw new RouteNotFoundException(
                            RoutingErrorCodes.ROUTE_NOT_FOUND,
                            String.format("Unable to find a route for point (%s).",
                                    FormatUtility.formatCoordinate(c0))
                    );
                } else if (gr.getErrors().get(0) instanceof com.graphhopper.util.exceptions.PointNotFoundException) {
                    StringBuilder message = new StringBuilder();
                    for (Throwable error : gr.getErrors()) {
                        if (message.length() > 0)
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
                        String.format("Unable to find a route for point (%s).",
                                FormatUtility.formatCoordinate(c0)
                        ));
            }
        }

        try {
            for (Object obj : gr.getReturnObjects()) {
                if (obj instanceof ExtraInfoProcessor) {
                    if (extraInfoProcessor == null) {
                        extraInfoProcessor = (ExtraInfoProcessor) obj;
                        if (!StringUtility.isNullOrEmpty(((ExtraInfoProcessor) obj).getSkippedExtraInfo())) {
                            gr.getHints().putObject(KEY_SKIPPED_EXTRA_INFO, ((ExtraInfoProcessor) obj).getSkippedExtraInfo());
                        }
                    } else {
                        extraInfoProcessor.appendData((ExtraInfoProcessor) obj);
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

        RoutingProfile rp = getRouteProfile(req, false);
        RouteSearchParameters searchParams = req.getSearchParameters();

        Coordinate[] coords = req.getCoordinates();
        Coordinate c0 = coords[0];
        Coordinate c1;
        int nSegments = coords.length - 1;
        GHResponse prevResp = null;
        WayPointBearing[] bearings = (req.getContinueStraight() || searchParams.getBearings() != null) ? new WayPointBearing[2] : null;
        int profileType = req.getSearchParameters().getProfileType();
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
                    int maximumSnappingRadius = routeProfiles.getRouteProfile(profileType).getConfiguration().getMaximumSnappingRadius();
                    radiuses = new double[2];
                    radiuses[0] = maximumSnappingRadius;
                    radiuses[1] = maximumSnappingRadius;
                } catch (Exception ex) {
                    // do nothing
                }
            }

            GHResponse gr = rp.computeRoute(c0.y, c0.x, c1.y, c1.x, bearings, radiuses, skipSegments.contains(i), searchParams, req.getGeometrySimplify());

            if (gr.hasErrors()) {
                if (!gr.getErrors().isEmpty()) {
                    if (gr.getErrors().get(0) instanceof com.graphhopper.util.exceptions.ConnectionNotFoundException) {
                        Map<String, Object> details = ((ConnectionNotFoundException) gr.getErrors().get(0)).getDetails();
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
                                String.format("Unable to find a route between points %d (%s) and %d (%s). %s",
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
                            String.format("Unable to find a route between points %d (%s) and %d (%s).",
                                i,
                                FormatUtility.formatCoordinate(c0),
                                i + 1,
                                FormatUtility.formatCoordinate(c1)
                            )
                        );
                    } else if (gr.getErrors().get(0) instanceof com.graphhopper.util.exceptions.MaximumNodesExceededException) {
                        Map<String, Object> details = ((MaximumNodesExceededException) gr.getErrors().get(0)).getDetails();
                        throw new RouteNotFoundException(
                            RoutingErrorCodes.PT_MAX_VISITED_NODES_EXCEEDED,
                            String.format("Unable to find a route between points %d (%s) and %d (%s). Maximum number of nodes exceeded: %s",
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
                            if (message.length() > 0)
                                message.append("; ");
                            if (error instanceof com.graphhopper.util.exceptions.PointNotFoundException) {
                                com.graphhopper.util.exceptions.PointNotFoundException pointNotFoundException = (com.graphhopper.util.exceptions.PointNotFoundException) error;
                                int pointReference = (i - 1) + pointNotFoundException.getPointIndex();

                                Coordinate pointCoordinate = (pointNotFoundException.getPointIndex() == 0) ? c0 : c1;
                                double pointRadius = radiuses[pointNotFoundException.getPointIndex()];

                                // -1 is used to indicate the use of internal limits instead of specifying it in the request.
                                // we should therefore let them know that they are already using the limit.
                                if (pointRadius == -1) {
                                    pointRadius = routeProfiles.getRouteProfile(profileType).getConfiguration().getMaximumSnappingRadius();
                                    message.append(String.format("Could not find routable point within the maximum possible radius of %.1f meters of specified coordinate %d: %s.",
                                            pointRadius,
                                            pointReference,
                                            FormatUtility.formatCoordinate(pointCoordinate)));
                                } else {
                                    message.append(String.format("Could not find routable point within a radius of %.1f meters of specified coordinate %d: %s.",
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
                            String.format("Unable to find a route between points %d (%s) and %d (%s).",
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
                    if (o instanceof ExtraInfoProcessor) {
                        extraInfoProcessors[extraInfoProcessorIndex] = (ExtraInfoProcessor) o;
                        extraInfoProcessorIndex++;
                        if (!StringUtility.isNullOrEmpty(((ExtraInfoProcessor) o).getSkippedExtraInfo())) {
                            gr.getHints().putObject(KEY_SKIPPED_EXTRA_INFO, ((ExtraInfoProcessor) o).getSkippedExtraInfo());
                        }
                    }
                }
            } else {
                for (Object o : gr.getReturnObjects()) {
                    if (o instanceof ExtraInfoProcessor) {
                        if (extraInfoProcessors[0] == null) {
                            extraInfoProcessors[0] = (ExtraInfoProcessor) o;
                            if (!StringUtility.isNullOrEmpty(((ExtraInfoProcessor) o).getSkippedExtraInfo())) {
                                gr.getHints().putObject(KEY_SKIPPED_EXTRA_INFO, ((ExtraInfoProcessor) o).getSkippedExtraInfo());
                            }
                        } else {
                            extraInfoProcessors[0].appendData((ExtraInfoProcessor) o);
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

    public RoutingProfile getRouteProfile(RoutingRequest req, boolean oneToMany) throws Exception {
        RouteSearchParameters searchParams = req.getSearchParameters();
        int profileType = searchParams.getProfileType();

        boolean fallbackAlgorithm = searchParams.requiresFullyDynamicWeights();
        boolean dynamicWeights = searchParams.requiresDynamicPreprocessedWeights();
        boolean useAlternativeRoutes = searchParams.getAlternativeRoutesCount() > 1;

        RoutingProfile rp = routeProfiles.getRouteProfile(profileType, !dynamicWeights);

        if (rp == null && !dynamicWeights)
            rp = routeProfiles.getRouteProfile(profileType, false);

        if (rp == null)
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Unable to get an appropriate route profile for RoutePreference = " + RoutingProfileType.getName(req.getSearchParameters().getProfileType()));

        RouteProfileConfiguration config = rp.getConfiguration();

        if (config.getMaximumDistance() > 0
                || (dynamicWeights && config.getMaximumDistanceDynamicWeights() > 0)
                || config.getMaximumWayPoints() > 0
                || (fallbackAlgorithm && config.getMaximumDistanceAvoidAreas() > 0)) {
            Coordinate[] coords = req.getCoordinates();
            int nCoords = coords.length;
            if (config.getMaximumWayPoints() > 0 && !oneToMany && nCoords > config.getMaximumWayPoints()) {
                throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "The specified number of waypoints must not be greater than " + config.getMaximumWayPoints() + ".");
            }

            if (config.getMaximumDistance() > 0
                    || (dynamicWeights && config.getMaximumDistanceDynamicWeights() > 0)
                    || (fallbackAlgorithm && config.getMaximumDistanceAvoidAreas() > 0)) {
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

                if (config.getMaximumDistance() > 0 && totalDist > config.getMaximumDistance())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, String.format("The approximated route distance must not be greater than %s meters.", config.getMaximumDistance()));
                if (dynamicWeights && config.getMaximumDistanceDynamicWeights() > 0 && totalDist > config.getMaximumDistanceDynamicWeights())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, String.format("By dynamic weighting, the approximated distance of a route segment must not be greater than %s meters.", config.getMaximumDistanceDynamicWeights()));
                if (fallbackAlgorithm && config.getMaximumDistanceAvoidAreas() > 0 && totalDist > config.getMaximumDistanceAvoidAreas())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, String.format("With these options, the approximated route distance must not be greater than %s meters.", config.getMaximumDistanceAvoidAreas()));
                if (useAlternativeRoutes && config.getMaximumDistanceAlternativeRoutes() > 0 && totalDist > config.getMaximumDistanceAlternativeRoutes())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, String.format("The approximated route distance must not be greater than %s meters for use with the alternative Routes algorithm.", config.getMaximumDistanceAlternativeRoutes()));
            }
        }

        if (searchParams.hasMaximumSpeed()) {
            if (searchParams.getMaximumSpeed() < config.getMaximumSpeedLowerBound()) {
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_MAXIMUM_SPEED, String.valueOf(searchParams.getMaximumSpeed()), "The maximum speed must not be lower than " + config.getMaximumSpeedLowerBound() + " km/h.");
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

        int profileType = parameters.getRouteParameters().getProfileType();
        RoutingProfile rp = routeProfiles.getRouteProfile(profileType, false);

        return rp.buildIsochrone(parameters);
    }

    public MatrixResult computeMatrix(MatrixRequest req) throws Exception {
        RoutingProfile rp = routeProfiles.getRouteProfile(req.getProfileType(), !req.getFlexibleMode());

        if (rp == null)
            throw new InternalServerException(MatrixErrorCodes.UNKNOWN, "Unable to find an appropriate routing profile.");

        return rp.computeMatrix(req);
    }

    public CentralityResult computeCentrality(CentralityRequest req) throws Exception {
        RoutingProfile rp = routeProfiles.getRouteProfile((req.getProfileType()));

        if (rp == null)
            throw new InternalServerException(CentralityErrorCodes.UNKNOWN, "Unable to find an appropriate routing profile.");
        return rp.computeCentrality(req);
    }

    public ExportResult computeExport(ExportRequest req) throws Exception {
        RoutingProfile rp = routeProfiles.getRouteProfile((req.getProfileType()));

        if (rp == null)
            throw new InternalServerException(CentralityErrorCodes.UNKNOWN, "Unable to find an appropriate routing profile.");
        return rp.computeExport(req);
    }

    public void initCompleted() {
        initComplete = true;
    }

    public static boolean isInitComplete() {
        return RoutingProfileManager.getInstance().initComplete;
    }

    public long getKafkaMessagesProcessed() {
        return this.kafkaMessagesProcessed;
    }

    public long getKafkaMessagesFailed() {
        return this.kafkaMessagesFailed;
    }

    /**
     * Process message received via ORSKafkaConsumer.
     *
     * @param profile target profile according to configuration
     * @param value   message value passed from KafkaConsumer
     */
    public void updateProfile(String profile, String value) {
        switch (profile) {
            // profile specific processing
            case "driving-car":
            case "driving-hgv":
                try {
                    ORSKafkaConsumerMessageSpeedUpdate msg = mapper.readValue(value, ORSKafkaConsumerMessageSpeedUpdate.class);
                    RoutingProfile rp = null;
                    int profileType = RoutingProfileType.getFromString(profile);
                    rp = getRoutingProfileFromType(rp, profileType);
                    if(rp == null)
                        return;
                    if (!msg.hasDurationMin())
                        msg.setDurationMin(rp.getConfiguration().getTrafficExpirationMin());
                    ExpiringSpeedStorage storage = GraphStorageUtils.getGraphExtension(rp.getGraphhopper().getGraphHopperStorage(), ExpiringSpeedStorage.class);
                    if(storage == null)
                        throw new IllegalStateException("Unable to find ExpiringSpeedStorage to process speed update");
                    processMessage(msg, storage);
                    LOGGER.debug(String.format("kafka message for speed update received: %s (%s) => %s, duration: %s", msg.getEdgeId(), msg.isReverse(), msg.getSpeed(), msg.getDurationMin()));
                    this.kafkaMessagesProcessed++;
                } catch (JsonProcessingException e) {
                    LOGGER.error(e);
                    this.kafkaMessagesFailed++;
                }
                break;
            case "test":
                try {
                    ORSKafkaConsumerMessageSpeedUpdate msg = mapper.readValue(value, ORSKafkaConsumerMessageSpeedUpdate.class);
                    if (KAFKA_DEBUG)
                        LOGGER.debug(String.format("kafka message for speed update received: %s (%s) => %s, duration: %s", msg.getEdgeId(), msg.isReverse(), msg.getSpeed(), msg.getDurationMin()));
                    this.kafkaMessagesProcessed++;
                } catch (JsonProcessingException e) {
                    LOGGER.error(e);
                    this.kafkaMessagesFailed++;
                }
                break;
            default:
                LOGGER.error(String.format("kafka message received for unknown profile %s", profile));
                this.kafkaMessagesFailed++;
                break;
        }
    }

    private void processMessage(ORSKafkaConsumerMessageSpeedUpdate msg, ExpiringSpeedStorage storage) {
        try{
            storage.process(msg);
        }
        catch (Exception e) {
            LOGGER.error(e);
        }
    }

    private RoutingProfile getRoutingProfileFromType(RoutingProfile rp, int profileType) {
        try {
            rp = routeProfiles.getRouteProfile(profileType);
        }
        catch (Exception e) {
            LOGGER.error(e);
        }
        return rp;
    }
}
