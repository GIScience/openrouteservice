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
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint;
import org.apache.log4j.Logger;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.common.ServiceRequest;
import org.heigit.ors.exceptions.IncompatibleParameterException;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.util.DebugUtility;
import org.heigit.ors.util.ProfileTools;
import org.heigit.ors.util.TemporaryUtilShelter;
import org.locationtech.jts.geom.Coordinate;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        ptRequest.setMaxVisitedNodes(routingProfile.getConfiguration().getMaximumVisitedNodesPT());

        return ptRequest;
    }

    public GHResponse computeRoute(double lat0, double lon0, double lat1, double lon1, WayPointBearing[] bearings,
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
            RouteSearchContext searchCntx = routingProfile.createSearchContext(searchParams);

            int flexibleMode = searchParams.hasFlexibleMode() || routingProfile.getConfiguration().isEnforceTurnCosts() ? ProfileTools.KEY_FLEX_PREPROCESSED : ProfileTools.KEY_FLEX_STATIC;
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

            if (TemporaryUtilShelter.supportWeightingMethod(profileType)) {
                ProfileTools.setWeightingMethod(req.getHints(), weightingMethod, profileType, TemporaryUtilShelter.hasTimeDependentSpeed(searchParams, searchCntx));
                if (routingProfile.requiresTimeDependentWeighting(searchParams, searchCntx))
                    flexibleMode = ProfileTools.KEY_FLEX_PREPROCESSED;
                flexibleMode = TemporaryUtilShelter.getFlexibilityMode(flexibleMode, searchParams, profileType);
            } else
                throw new IllegalArgumentException("Unsupported weighting " + weightingMethod + " for profile + " + profileType);

            if (flexibleMode == ProfileTools.KEY_FLEX_STATIC)
                //Speedup order: useCH, useCore, useALT
                // TODO Future improvement: profileNameCH is an ugly hack and is required because of the hard-coded turnCost=false for CH
                routingProfile.setSpeedups(req, true, true, true, searchCntx.profileNameCH());

            if (flexibleMode == ProfileTools.KEY_FLEX_PREPROCESSED) {
                routingProfile.setSpeedups(req, false, optimized, true, searchCntx.profileNameCH());
            }

            //cannot use CH or CoreALT with requests where the weighting of non-predefined edges might change
            if (flexibleMode == ProfileTools.KEY_FLEX_FULLY)
                routingProfile.setSpeedups(req, false, false, true, searchCntx.profileNameCH());

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
                req.getHints().putObject("maximum_speed_lower_bound", routingProfile.getConfiguration().getMaximumSpeedLowerBound());
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

    public GHResponse computeRoundTripRoute(double lat0, double lon0, WayPointBearing
            bearing, RouteSearchParameters searchParams, Boolean geometrySimplify, RoutingProfile routingProfile) throws Exception {
        GHResponse resp;

        try {
            int profileType = searchParams.getProfileType();
            int weightingMethod = searchParams.getWeightingMethod();
            RouteSearchContext searchCntx = routingProfile.createSearchContext(searchParams);

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

            if (TemporaryUtilShelter.supportWeightingMethod(profileType))
                ProfileTools.setWeightingMethod(req.getHints(), weightingMethod, profileType, false);
            else
                throw new IllegalArgumentException("Unsupported weighting " + weightingMethod + " for profile + " + profileType);

            //Roundtrip not possible with preprocessed edges.
            routingProfile.setSpeedups(req, false, false, true, searchCntx.profileNameCH());

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
}
