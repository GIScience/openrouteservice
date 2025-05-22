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
import com.graphhopper.gtfs.*;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.ConditionalEdges;
import com.graphhopper.util.*;
import com.graphhopper.util.exceptions.MaximumNodesExceededException;
import com.graphhopper.util.shapes.GHPoint;
import org.apache.log4j.Logger;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.common.ServiceRequest;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.pathprocessors.ExtraInfoProcessor;
import org.heigit.ors.util.*;
import org.locationtech.jts.geom.Coordinate;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoutingRequest extends ServiceRequest {
    private static final Logger LOGGER = Logger.getLogger(RoutingRequest.class);
    public static final String ATTR_DETOURFACTOR = "detourfactor";

    private Coordinate[] coordinates;
    private RouteSearchParameters searchParameters;
    private DistanceUnit units = DistanceUnit.METERS;
    private String language = "en";
    private String geometryFormat = "encodedpolyline";
    private boolean geometrySimplify = false;
    private RouteInstructionsFormat instructionsFormat = RouteInstructionsFormat.TEXT;
    private boolean includeInstructions = true;
    private boolean includeElevation = false;
    private boolean includeGeometry = true;
    private boolean includeManeuvers = false;
    private boolean includeRoundaboutExits = false;
    private String[] attributes = null;
    private int extraInfo;
    private int locationIndex = -1;
    private boolean continueStraight = false;
    private List<Integer> skipSegments = new ArrayList<>();
    private boolean includeCountryInfo = false;
    private double maximumSpeed;
    private RoutingProfile routingProfile;

    private String responseFormat = "json";
    // Fields specific to GraphHopper GTFS
    private boolean schedule;
    private Duration walkingTime;
    private int scheduleRows;
    private boolean ignoreTransfers;
    private Duration scheduleDuration;

    public RoutingRequest() {
        searchParameters = new RouteSearchParameters();
    }

    public static double getHeadingDirection(GHResponse resp) {
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

    /**
     * This will enrich all direct routes with an approximated travel time that is being calculated from the real graphhopper
     * results. The routes object should contain all routes, so the function can maintain and return the proper order!
     *
     * @param routes Should hold all the routes that have been calculated, not only the direct routes.
     * @return will return routes object with enriched direct routes if any we're found in the same order as the input object.
     */
    public static List<GHResponse> enrichDirectRoutesTime(List<GHResponse> routes) {
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

    public RoutingProfile profile() {
        return routingProfile;
    }

    public void setRoutingProfile(RoutingProfile profile) {
        this.routingProfile = profile;
    }

    public Coordinate[] getCoordinates() {
        return coordinates;
    }

    public Coordinate getDestination() {
        return coordinates[coordinates.length - 1];
    }

    public void setCoordinates(Coordinate[] coordinates) {
        this.coordinates = coordinates;
    }

    public RouteSearchParameters getSearchParameters() {
        return searchParameters;
    }

    public void setSearchParameters(RouteSearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    public boolean getIncludeInstructions() {
        return includeInstructions;
    }

    public void setIncludeInstructions(boolean includeInstructions) {
        this.includeInstructions = includeInstructions;
    }

    public DistanceUnit getUnits() {
        return units;
    }

    public void setUnits(DistanceUnit units) {
        this.units = units;
    }

    public String getGeometryFormat() {
        return geometryFormat;
    }

    public void setGeometryFormat(String geometryFormat) {
        this.geometryFormat = geometryFormat;
    }

    public boolean getGeometrySimplify() {
        return geometrySimplify;
    }

    public void setGeometrySimplify(boolean geometrySimplify) {
        this.geometrySimplify = geometrySimplify;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public RouteInstructionsFormat getInstructionsFormat() {
        return instructionsFormat;
    }

    public void setInstructionsFormat(RouteInstructionsFormat format) {
        instructionsFormat = format;
    }

    public int getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(int extraInfo) {
        this.extraInfo = extraInfo;
    }

    public boolean getIncludeElevation() {
        return includeElevation;
    }

    public void setIncludeElevation(boolean includeElevation) {
        this.includeElevation = includeElevation;
    }

    public boolean getIncludeGeometry() {
        return includeGeometry;
    }

    public void setIncludeGeometry(boolean includeGeometry) {
        this.includeGeometry = includeGeometry;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
    }

    public boolean hasAttribute(String attr) {
        if (attributes == null || attr == null)
            return false;

        for (String attribute : attributes)
            if (attr.equalsIgnoreCase(attribute))
                return true;

        return false;
    }

    public int getLocationIndex() {
        return locationIndex;
    }

    public void setLocationIndex(int locationIndex) {
        this.locationIndex = locationIndex;
    }

    public boolean getIncludeManeuvers() {
        return includeManeuvers;
    }

    public void setIncludeManeuvers(boolean includeManeuvers) {
        this.includeManeuvers = includeManeuvers;
    }

    public boolean getContinueStraight() {
        return continueStraight;
    }

    public void setContinueStraight(boolean continueStraight) {
        this.continueStraight = continueStraight;
    }

    public boolean getIncludeRoundaboutExits() {
        return includeRoundaboutExits;
    }

    public void setIncludeRoundaboutExits(boolean includeRoundaboutExits) {
        this.includeRoundaboutExits = includeRoundaboutExits;
    }

    public boolean isValid() {
        return coordinates != null;
    }

    public List<Integer> getSkipSegments() {
        return skipSegments;
    }

    public void setSkipSegments(List<Integer> skipSegments) {
        this.skipSegments = skipSegments;
    }

    public boolean getIncludeCountryInfo() {
        return includeCountryInfo;
    }

    public void setIncludeCountryInfo(boolean includeCountryInfo) {
        this.includeCountryInfo = includeCountryInfo;
    }

    public void setMaximumSpeed(double maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
    }

    public double getMaximumSpeed() {
        return maximumSpeed;
    }

    public void setResponseFormat(String responseFormat) {
        if (!Helper.isEmpty(responseFormat)) {
            this.responseFormat = responseFormat;
        }
    }

    public String getResponseFormat() {
        return this.responseFormat;
    }

    public boolean isRoundTripRequest() {
        return this.coordinates.length == 1 && this.searchParameters.getRoundTripLength() > 0;
    }

    public void setSchedule(boolean schedule) {
        this.schedule = schedule;
    }

    public void setWalkingTime(Duration walkingTime) {
        this.walkingTime = walkingTime;
    }

    public void setScheduleRows(int scheduleRows) {
        this.scheduleRows = scheduleRows;
    }

    public void setIgnoreTransfers(boolean ignoreTransfers) {
        this.ignoreTransfers = ignoreTransfers;
    }

    public void setScheduleDuaration(Duration scheduleDuration) {
        this.scheduleDuration = scheduleDuration;
    }

    public Request createPTRequest(double lat0, double lon0, double lat1, double lon1, RouteSearchParameters params, RoutingProfile routingProfile) throws IncompatibleParameterException {
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

        ptRequest.setMaxVisitedNodes(routingProfile.getProfileConfiguration().getService().getMaximumVisitedNodes());

        return ptRequest;
    }

    private GHResponse computeRoute(double lat0, double lon0, double lat1, double lon1, WayPointBearing[] bearings,
                                   double[] radiuses, boolean directedSegment, RouteSearchParameters searchParams, Boolean geometrySimplify, RoutingProfile routingProfile)
            throws Exception {

        GHResponse resp;

        try {
            int profileType = searchParams.getProfileType();
            ORSGraphHopper gh = routingProfile.getGraphhopper();
            if (profileType == RoutingProfileType.PUBLIC_TRANSPORT) {
                StopWatch stopWatch = (new StopWatch()).start();
                PtRouter ptRouter = new PtRouterImpl.Factory(gh.getConfig(), new TranslationMap().doImport(), gh.getGraphHopperStorage(), gh.getLocationIndex(), gh.getGtfsStorage())
                        .createWithoutRealtimeFeed();
                Request ptRequest = createPTRequest(lat0, lon0, lat1, lon1, searchParams, routingProfile);
                GHResponse res = ptRouter.route(ptRequest);
                res.addDebugInfo("Request total:" + stopWatch.stop().getSeconds() + "s");
                return res;
            }
            int weightingMethod = searchParams.getWeightingMethod();
            RouteSearchContext searchCntx = TemporaryUtilShelter.createSearchContext(searchParams, routingProfile);

            int flexibleMode = searchParams.hasFlexibleMode() || Boolean.TRUE.equals(routingProfile.getProfileConfiguration().getService().getForceTurnCosts()) ? ProfileTools.KEY_FLEX_PREPROCESSED : ProfileTools.KEY_FLEX_STATIC;
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

            if (searchParams.getCustomModel() != null) {
                req.setCustomModel(searchParams.getCustomModel());
            }

            if (TemporaryUtilShelter.supportWeightingMethod(profileType)) {
                ProfileTools.setWeightingMethod(req.getHints(), weightingMethod, profileType, TemporaryUtilShelter.hasTimeDependentSpeed(searchParams, searchCntx));
                if (requiresTimeDependentAlgorithm(searchCntx))
                    flexibleMode = ProfileTools.KEY_FLEX_PREPROCESSED;
                flexibleMode = TemporaryUtilShelter.getFlexibilityMode(flexibleMode, searchParams, profileType);
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
                String key;
                LocalDateTime dateTime;
                if (searchParams.hasDeparture()) {
                    key = RouteRequestParameterNames.PARAM_DEPARTURE;
                    dateTime = searchParams.getDeparture();
                } else {
                    key = RouteRequestParameterNames.PARAM_ARRIVAL;
                    dateTime = searchParams.getArrival();
                }

                Instant time = dateTime.atZone(ZoneId.of("Europe/Berlin")).toInstant();
                req.getHints().putObject(key, time);

                if (requiresTimeDependentAlgorithm(searchCntx)) {
                    req.getHints().putObject("time", time.toEpochMilli());
                    req.setAlgorithm(Parameters.Algorithms.TD_ASTAR);
                }
            }

            if (routingProfile.getAstarEpsilon() != null)
                req.getHints().putObject("astarbi.epsilon", routingProfile.getAstarEpsilon());
            if (routingProfile.getAstarApproximation() != null)
                req.getHints().putObject("astarbi.approximation", routingProfile.getAstarApproximation());

            if (searchParams.getAlternativeRoutesCount() > 0) {
                req.setAlgorithm("alternative_route");
                req.getHints().putObject("alternative_route.max_paths", searchParams.getAlternativeRoutesCount());
                req.getHints().putObject("alternative_route.max_weight_factor", searchParams.getAlternativeRoutesWeightFactor());
                req.getHints().putObject("alternative_route.max_share_factor", searchParams.getAlternativeRoutesShareFactor());
            }

            if (searchParams.hasMaximumSpeed()) {
                req.getHints().putObject("maximum_speed", searchParams.getMaximumSpeed());
                req.getHints().putObject("maximum_speed_lower_bound", routingProfile.getProfileConfiguration().getBuild().getMaximumSpeedLowerBound());
            }

            if (directedSegment) {
                resp = gh.constructFreeHandRoute(req);
            } else {
                gh.getRouterConfig().setSimplifyResponse(geometrySimplify);
                resp = gh.route(req);
            }
            if (DebugUtility.isDebug() && !directedSegment) {
                LOGGER.info("visited nodes: " + resp.getHints().getObject("visited_nodes.sum", null));
            }
            if (DebugUtility.isDebug() && directedSegment) {
                LOGGER.info("skipped segment: " + resp.getHints().getString("skipped_segment", null));
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Unable to compute a route");
        }

        return resp;
    }

    private GHResponse computeRoundTripRoute(double lat0, double lon0, WayPointBearing
            bearing, RouteSearchParameters searchParams, Boolean geometrySimplify, RoutingProfile routingProfile) throws Exception {
        GHResponse resp;

        try {
            int profileType = searchParams.getProfileType();
            int weightingMethod = searchParams.getWeightingMethod();
            RouteSearchContext searchCntx = TemporaryUtilShelter.createSearchContext(searchParams, routingProfile);

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
            req.setEncoderName(searchCntx.getEncoder().toString());
            req.getHints().putObject(Parameters.Algorithms.RoundTrip.DISTANCE, searchParams.getRoundTripLength());
            req.getHints().putObject(Parameters.Algorithms.RoundTrip.POINTS, searchParams.getRoundTripPoints());

            if (searchParams.getRoundTripSeed() > -1) {
                req.getHints().putObject(Parameters.Algorithms.RoundTrip.SEED, searchParams.getRoundTripSeed());
            }

            PMap props = searchCntx.getProperties();
            req.setAdditionalHints(props);

            if (props != null && !props.isEmpty())
                req.getHints().putAll(props);

            if (TemporaryUtilShelter.supportWeightingMethod(profileType))
                ProfileTools.setWeightingMethod(req.getHints(), weightingMethod, profileType, false);
            else
                throw new IllegalArgumentException("Unsupported weighting " + weightingMethod + " for profile + " + profileType);

            //Roundtrip not possible with preprocessed edges.
            setSpeedups(req, false, false, true, searchCntx.profileNameCH());

            if (routingProfile.getAstarEpsilon() != null)
                req.getHints().putObject("astarbi.epsilon", routingProfile.getAstarEpsilon());
            if (routingProfile.getAstarApproximation() != null)
                req.getHints().putObject("astarbi.approximation", routingProfile.getAstarApproximation());
            //Overwrite algorithm selected in setSpeedups
            req.setAlgorithm(Parameters.Algorithms.ROUND_TRIP);

            routingProfile.getGraphhopper().getRouterConfig().setSimplifyResponse(geometrySimplify);
            resp = routingProfile.getGraphhopper().route(req);

        } catch (Exception ex) {
            LOGGER.error(ex);
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Unable to compute a route");
        }

        return resp;
    }

    private RouteResult[] computeRoundTripRoute() throws Exception {
        List<GHResponse> routes = new ArrayList<>();

        RoutingProfile rp = profile();
        RouteSearchParameters searchParams = getSearchParameters();
        ProfileProperties profileProperties = rp.getProfileConfiguration();

        if (profileProperties.getService().getMaximumDistanceRoundTripRoutes() != null && profileProperties.getService().getMaximumDistanceRoundTripRoutes() < searchParams.getRoundTripLength()) {
            throw new ServerLimitExceededException(
                    RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT,
                    "The requested route length must not be greater than %s meters.".formatted(profileProperties.getService().getMaximumDistanceRoundTripRoutes())
            );
        }

        Coordinate[] coords = getCoordinates();
        Coordinate c0 = coords[0];

        ExtraInfoProcessor extraInfoProcessor = null;

        WayPointBearing bearing = null;
        if (searchParams.getBearings() != null) {
            bearing = searchParams.getBearings()[0];
        }

        GHResponse gr = computeRoundTripRoute(c0.y, c0.x, bearing, searchParams, getGeometrySimplify(), rp);

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
                            gr.getHints().putObject(RoutingProfileManager.KEY_SKIPPED_EXTRA_INFO, processor.getSkippedExtraInfo());
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
        return new RouteResultBuilder().createRouteResults(routes, this, new List[]{extraInfos});
    }

    public RouteResult[] computeRoute() throws Exception {
        if (getSearchParameters().getRoundTripLength() > 0) {
            return computeRoundTripRoute();
        } else {
            return computeLinearRoute();
        }
    }

    private RouteResult[] computeLinearRoute() throws Exception {
        List<Integer> skipSegments = getSkipSegments();
        List<GHResponse> routes = new ArrayList<>();

        RoutingProfile rp = profile();
        RouteSearchParameters searchParams = getSearchParameters();

        Coordinate[] coords = getCoordinates();
        Coordinate c0 = coords[0];
        Coordinate c1;
        int nSegments = coords.length - 1;
        GHResponse prevResp = null;
        WayPointBearing[] bearings = (getContinueStraight() || searchParams.getBearings() != null) ? new WayPointBearing[2] : null;
        String profileName = getSearchParameters().getProfileName();
        double[] radiuses = null;

        if (getSearchParameters().getAlternativeRoutesCount() > 1 && coords.length > 2) {
            throw new InternalServerException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "Alternative routes algorithm does not support more than two way points.");
        }

        int numberOfExpectedExtraInfoProcessors = getSearchParameters().getAlternativeRoutesCount() < 0 ? 1 : getSearchParameters().getAlternativeRoutesCount();
        ExtraInfoProcessor[] extraInfoProcessors = new ExtraInfoProcessor[numberOfExpectedExtraInfoProcessors];

        for (int i = 1; i <= nSegments; ++i) {
            c1 = coords[i];

            if (bearings != null) {
                bearings[0] = null;
                if (prevResp != null && getContinueStraight()) {
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
                    int maximumSnappingRadius = profile().getProfileConfiguration().getService().getMaximumSnappingRadius();
                    radiuses = new double[2];
                    radiuses[0] = maximumSnappingRadius;
                    radiuses[1] = maximumSnappingRadius;
                } catch (Exception ex) {
                    // do nothing
                }
            }

            GHResponse gr = computeRoute(c0.y, c0.x, c1.y, c1.x, bearings, radiuses, skipSegments.contains(i), searchParams, getGeometrySimplify(), rp);

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
                                    pointRadius = profile().getProfileConfiguration().getService().getMaximumSnappingRadius();
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
                    } else if (gr.getErrors().get(0) instanceof IllegalArgumentException) {
                        throw new InternalServerException(RoutingErrorCodes.UNSUPPORTED_REQUEST_OPTION, gr.getErrors().get(0).getMessage());
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
                            gr.getHints().putObject(RoutingProfileManager.KEY_SKIPPED_EXTRA_INFO, processor.getSkippedExtraInfo());
                        }
                    }
                }
            } else {
                for (Object o : gr.getReturnObjects()) {
                    if (o instanceof ExtraInfoProcessor processor) {
                        if (extraInfoProcessors[0] == null) {
                            extraInfoProcessors[0] = processor;
                            if (!StringUtility.isNullOrEmpty(processor.getSkippedExtraInfo())) {
                                gr.getHints().putObject(RoutingProfileManager.KEY_SKIPPED_EXTRA_INFO, processor.getSkippedExtraInfo());
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
        return new RouteResultBuilder().createRouteResults(routes, this, extraInfos);
    }

    boolean requiresTimeDependentAlgorithm(RouteSearchContext searchCntx) {
        RouteSearchParameters searchParams = getSearchParameters();

        if (!searchParams.isTimeDependent())
            return false;

        FlagEncoder flagEncoder = searchCntx.getEncoder();

        if (flagEncoder.hasEncodedValue(EncodingManager.getKey(flagEncoder, ConditionalEdges.ACCESS)))
            return true;

        if (WeightingMethod.SHORTEST == searchParams.getWeightingMethod())
            return false;

        return flagEncoder.hasEncodedValue(EncodingManager.getKey(flagEncoder, ConditionalEdges.SPEED))
                || profile().getGraphhopper().isTrafficEnabled();
    }

    /**
     * Set the speedup techniques used for calculating the route.
     * Reults in usage of CH, Core or ALT/AStar, if they are enabled.
     *
     * @param req           Request whose hints will be set
     * @param useCH         Should CH be enabled
     * @param useCore       Should Core be enabled
     * @param useALT        Should ALT be enabled
     * @param profileNameCH
     */
    public void setSpeedups(GHRequest req, boolean useCH, boolean useCore, boolean useALT, String profileNameCH) {
        String requestProfileName = req.getProfile();

        //Priority: CH->Core->ALT
        String profileNameNoTC = requestProfileName.replace("_with_turn_costs", "");

        ORSGraphHopper gh = profile().getGraphhopper();

        useCH = useCH && gh.isCHAvailable(requestProfileName);
        useCore = useCore && !useCH && (gh.isCoreAvailable(requestProfileName) || gh.isCoreAvailable(profileNameNoTC));
        useALT = useALT && !useCH && !useCore && gh.isLMAvailable(requestProfileName);

        req.getHints().putObject(ProfileTools.KEY_CH_DISABLE, !useCH);
        req.getHints().putObject(ProfileTools.KEY_CORE_DISABLE, !useCore);
        req.getHints().putObject(ProfileTools.KEY_LM_DISABLE, !useALT);

        if (useCH) {
            // either Dijkstra or AStar is selected downstream depending on whether node- or egde-based CH is being used
            req.setAlgorithm("");
        }
        if (useCore && !gh.isCoreAvailable(requestProfileName) && gh.isCoreAvailable(profileNameNoTC))
            // fallback to a core profile without turn costs if one is available
            req.setProfile(profileNameNoTC);

    }
}
