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
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.isochrones.IsochroneMap;
import org.heigit.ors.isochrones.IsochroneSearchParameters;
import org.heigit.ors.mapmatching.MapMatchingRequest;
import org.heigit.ors.matrix.MatrixErrorCodes;
import org.heigit.ors.matrix.MatrixRequest;
import org.heigit.ors.matrix.MatrixResult;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.heigit.ors.routing.configuration.RoutingManagerConfiguration;
import org.heigit.ors.routing.pathprocessors.ExtraInfoProcessor;
import org.heigit.ors.services.routing.RoutingServiceSettings;
import org.heigit.ors.util.FormatUtility;
import org.heigit.ors.util.RuntimeUtility;
import org.heigit.ors.util.StringUtility;
import org.heigit.ors.util.TimeUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class RoutingProfileManager {
    private static final Logger LOGGER = Logger.getLogger(RoutingProfileManager.class.getName());

    private RoutingProfilesCollection routeProfiles;
    private RoutingProfilesUpdater profileUpdater;
    private static RoutingProfileManager mInstance;

    public static synchronized RoutingProfileManager getInstance() throws IOException {
        if (mInstance == null) {
            mInstance = new RoutingProfileManager();
            mInstance.initialize(null);
        }
        return mInstance;
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

            LOGGER.info("               ");

            int nCompletedTasks = 0;
            while (nCompletedTasks < nTotalTasks) {
                Future<RoutingProfile> future = compService.take();
                try {
                    RoutingProfile rp = future.get();
                    nCompletedTasks++;
                    rp.close();
                    LOGGER.info("Graph preparation done.");
                } catch (InterruptedException|ExecutionException e) {
                    LOGGER.error(e);
                    throw e;
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

                    LOGGER.info("               ");

                    int nCompletedTasks = 0;
                    while (nCompletedTasks < nTotalTasks) {
                        Future<RoutingProfile> future = compService.take();

                        try {
                            RoutingProfile rp = future.get();
                            nCompletedTasks++;
                            if (!routeProfiles.add(rp))
                                LOGGER.warn("Routing profile has already been added.");
                        } catch (ExecutionException|InterruptedException e) {
                            LOGGER.error(e);
                            throw e;
                        }
                    }

                    executor.shutdown();
                    loadCntx.releaseElevationProviderCacheAfterAllVehicleProfilesHaveBeenProcessed();

                    LOGGER.info("Total time: " + TimeUtility.getElapsedTime(startTime, true) + ".");
                    LOGGER.info("========================================================================");

                    if (rmc.getUpdateConfig().getEnabled()) {
                        profileUpdater = new RoutingProfilesUpdater(rmc.getUpdateConfig(), routeProfiles);
                        profileUpdater.start();
                    }
                }

                RoutingProfileManagerStatus.setReady(true);
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to initialize RoutingProfileManager instance.", ex);
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
        throw new NotImplementedException();
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
                        extraInfoProcessor = (ExtraInfoProcessor)obj;
                        if (!StringUtility.isNullOrEmpty(((ExtraInfoProcessor)obj).getSkippedExtraInfo())) {
                            gr.getHints().put("skipped_extra_info", ((ExtraInfoProcessor)obj).getSkippedExtraInfo());
                        }
                    } else {
                        extraInfoProcessor.appendData((ExtraInfoProcessor)obj);
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
                    bearings[0] = new WayPointBearing(getHeadingDirection(prevResp), Double.NaN);
                }

                if (searchParams.getBearings() != null) {
                    bearings[0] = searchParams.getBearings()[i - 1];
                    bearings[1] = (i == nSegments && searchParams.getBearings().length != nSegments + 1) ? new WayPointBearing(Double.NaN, Double.NaN) : searchParams.getBearings()[i];
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
                        throw new RouteNotFoundException(
                                RoutingErrorCodes.ROUTE_NOT_FOUND,
                                String.format("Unable to find a route between points %d (%s) and %d (%s).",
                                        i,
                                        FormatUtility.formatCoordinate(c0),
                                        i + 1,
                                        FormatUtility.formatCoordinate(c1))
                        );
                    } else if(gr.getErrors().get(0) instanceof com.graphhopper.util.exceptions.PointNotFoundException) {
                        StringBuilder message = new StringBuilder();
                        for(Throwable error: gr.getErrors()) {
                            if(message.length() > 0)
                                message.append("; ");
                            if (error instanceof com.graphhopper.util.exceptions.PointNotFoundException) {
                                com.graphhopper.util.exceptions.PointNotFoundException pointNotFoundException = (com.graphhopper.util.exceptions.PointNotFoundException) error;
                                int pointReference = (i-1) + pointNotFoundException.getPointIndex();

                                Coordinate pointCoordinate = (pointNotFoundException.getPointIndex() == 0) ? c0 : c1;
                                double pointRadius = radiuses[pointNotFoundException.getPointIndex()];

                                message.append(String.format("Could not find point %d: %s within a radius of %.1f meters.",
                                        pointReference,
                                        FormatUtility.formatCoordinate(pointCoordinate),
                                        pointRadius));

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
                        extraInfoProcessors[extraInfoProcessorIndex] = (ExtraInfoProcessor)o;
                        extraInfoProcessorIndex++;
                        if (!StringUtility.isNullOrEmpty(((ExtraInfoProcessor)o).getSkippedExtraInfo())) {
                            gr.getHints().put("skipped_extra_info", ((ExtraInfoProcessor)o).getSkippedExtraInfo());
                        }
                    }
                }
            } else {
                for (Object o : gr.getReturnObjects()) {
                    if (o instanceof ExtraInfoProcessor) {
                        if (extraInfoProcessors[0] == null) {
                            extraInfoProcessors[0] = (ExtraInfoProcessor)o;
                            if (!StringUtility.isNullOrEmpty(((ExtraInfoProcessor)o).getSkippedExtraInfo())) {
                                gr.getHints().put("skipped_extra_info", ((ExtraInfoProcessor)o).getSkippedExtraInfo());
                            }
                        } else {
                            extraInfoProcessors[0].appendData((ExtraInfoProcessor)o);
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
            return Helper.ANGLE_CALC.calcAzimuth(lat1, lon1, lat2, lon2);
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
                throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "The specified number of waypoints must not be greater than " + Integer.toString(config.getMaximumWayPoints()) + ".");
            }

            if (config.getMaximumDistance() > 0
                    || (dynamicWeights && config.getMaximumDistanceDynamicWeights() > 0)
                    || (fallbackAlgorithm && config.getMaximumDistanceAvoidAreas() > 0)) {
                DistanceCalc distCalc = Helper.DIST_EARTH;

                Coordinate c0 = coords[0];
                Coordinate c1;
                double totalDist = 0.0;

                if (oneToMany) {
                    for (int i = 1; i < nCoords; i++) {
                        c1 = coords[i];
                        totalDist = distCalc.calcDist(c0.y, c0.x, c1.y, c1.x);
                    }
                } else {
                    if (nCoords == 2) {
                        c1 = coords[1];
                        totalDist = distCalc.calcDist(c0.y, c0.x, c1.y, c1.x);
                    } else {
                        double dist = 0;
                        for (int i = 1; i < nCoords; i++) {
                            c1 = coords[i];
                            dist = distCalc.calcDist(c0.y, c0.x, c1.y, c1.x);
                            totalDist += dist;
                            c0 = c1;
                        }
                    }
                }

                if (config.getMaximumDistance() > 0 && totalDist > config.getMaximumDistance())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, String.format("The approximated route distance must not be greater than %s meters.", config.getMaximumDistance()));
                if (dynamicWeights && config.getMaximumDistanceDynamicWeights() > 0 && totalDist > config.getMaximumDistanceDynamicWeights())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, String.format("By dynamic weighting, the approximated distance of a route segment must not be greater than %s meters.", config.getMaximumDistanceDynamicWeights()));
                if (fallbackAlgorithm && config.getMaximumDistanceAvoidAreas() > 0 && totalDist > config.getMaximumDistanceAvoidAreas())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, String.format("With these options, the approximated route distance must not be greater than %s meters.", config.getMaximumDistanceAvoidAreas()));
                if (useAlternativeRoutes && config.getMaximumDistanceAlternativeRoutes() > 0 && totalDist > config.getMaximumDistanceAlternativeRoutes())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, String.format("The approximated route distance must not be greater than %s meters for use with the alternative Routes algotirhm.", config.getMaximumDistanceAlternativeRoutes()));
            }
        }

        if(searchParams.hasMaximumSpeed()){
            if(searchParams.getMaximumSpeed() < config.getMaximumSpeedLowerBound()) {
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_MAXIMUM_SPEED, String.valueOf(searchParams.getMaximumSpeed()), "The maximum speed must not be lower than " + config.getMaximumSpeedLowerBound() + " km/h.");
            }
            if(RoutingProfileCategory.getFromEncoder(rp.getGraphhopper().getEncodingManager()) != RoutingProfileCategory.DRIVING){
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

}
