package org.heigit.ors.api.services;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.api.config.ApiEngineProperties;
import org.heigit.ors.api.config.EndpointsProperties;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.requests.routing.RouteRequestRoundTripOptions;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.localization.LocalizationManager;
import org.heigit.ors.routing.*;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;


@Service
public class RoutingService extends ApiService {

    @Autowired
    public RoutingService(EndpointsProperties endpointsProperties, ApiEngineProperties apiEngineProperties) {
        this.endpointsProperties = endpointsProperties;
        this.apiEngineProperties = apiEngineProperties;
    }

    public static void validateRouteProfileForRequest(RoutingRequest req) throws InternalServerException, ServerLimitExceededException, ParameterValueException {
        boolean oneToMany = false;
        RouteSearchParameters searchParams = req.getSearchParameters();
        String profileName = searchParams.getProfileName();

        boolean preprocessedWeights = searchParams.requiresPreprocessedWeights();
        boolean dynamicWeights = searchParams.requiresDynamicWeights();
        boolean useAlternativeRoutes = searchParams.getAlternativeRoutesCount() > 1;

        RoutingProfile rp = req.profile();

        if (rp == null)
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Unable to get an appropriate routing profile for the name " + profileName + ".");

        ProfileProperties profileProperties = rp.getProfileConfiguration();

        if (profileProperties.getService().getMaximumDistance() != null
                || dynamicWeights && profileProperties.getService().getMaximumDistanceDynamicWeights() != null
                || profileProperties.getService().getMaximumWayPoints() != null
                || searchParams.hasAvoidAreas() && profileProperties.getService().getMaximumDistanceAvoidAreas() != null
        ) {
            Coordinate[] coords = req.getCoordinates();
            int nCoords = coords.length;
            if (profileProperties.getService().getMaximumWayPoints() > 0 && !oneToMany && nCoords > profileProperties.getService().getMaximumWayPoints()) {
                throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "The specified number of waypoints must not be greater than " + profileProperties.getService().getMaximumWayPoints() + ".");
            }

            if (profileProperties.getService().getMaximumDistance() != null
                    || dynamicWeights && profileProperties.getService().getMaximumDistanceDynamicWeights() != null
                    || searchParams.hasAvoidAreas() && profileProperties.getService().getMaximumDistanceAvoidAreas() != null
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
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "With dynamic weighting, the approximated distance of a route segment must not be greater than %s meters.".formatted(profileProperties.getService().getMaximumDistanceDynamicWeights()));
                if (searchParams.hasAvoidAreas() && profileProperties.getService().getMaximumDistanceAvoidAreas() != null && totalDist > profileProperties.getService().getMaximumDistanceAvoidAreas())
                    throw new ServerLimitExceededException(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT, "With avoid areas, the approximated route distance must not be greater than %s meters.".formatted(profileProperties.getService().getMaximumDistanceAvoidAreas()));
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
    }

    @Override
    double getMaximumAvoidPolygonArea() {
        return this.endpointsProperties.getRouting().getMaximumAvoidPolygonArea();
    }

    @Override
    double getMaximumAvoidPolygonExtent() {
        return this.endpointsProperties.getRouting().getMaximumAvoidPolygonExtent();
    }

    public RouteResult[] generateRouteFromRequest(RouteRequest routeApiRequest) throws StatusCodeException {
        try {
            RoutingRequest routingRequest = convertRouteRequest(routeApiRequest);
            RoutingProfile profile = parseRoutingProfile(routeApiRequest.getProfileName());
            routingRequest.setRoutingProfile(profile);
            validateRouteProfileForRequest(routingRequest);
            if (routeApiRequest.hasCustomModel()) {
                if (Boolean.FALSE == profile.getProfileProperties().getBuild().getEncoderOptions().getEnableCustomModels()) {
                    throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, RoutingErrorCodes.UNSUPPORTED_REQUEST_OPTION,
                            "Custom model not available for profile '" + profile.name() + "'.");
                }
                if (Boolean.FALSE == profile.getProfileProperties().getService().getAllowCustomModels()) {
                    throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, RoutingErrorCodes.UNSUPPORTED_REQUEST_OPTION,
                            "Custom model disabled for profile '" + profile.name() + "'.");
                }
            }

            return routingRequest.computeRoute();
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, RoutingErrorCodes.UNKNOWN);
        }
    }

    public RoutingRequest convertRouteRequest(RouteRequest routeApiRequest) throws StatusCodeException {
        RoutingRequest routingRequest = new RoutingRequest();
        boolean isRoundTrip = routeApiRequest.hasRouteOptions() && routeApiRequest.getRouteOptions().hasRoundTripOptions();
        routingRequest.setCoordinates(convertCoordinates(routeApiRequest.getCoordinates(), isRoundTrip));
        routingRequest.setGeometryFormat(convertGeometryFormat(routeApiRequest.getResponseType()));

        if (routeApiRequest.hasUseElevation())
            routingRequest.setIncludeElevation(routeApiRequest.getUseElevation());

        if (routeApiRequest.hasContinueStraightAtWaypoints())
            routingRequest.setContinueStraight(routeApiRequest.getContinueStraightAtWaypoints());

        if (routeApiRequest.hasIncludeGeometry())
            routingRequest.setIncludeGeometry(convertIncludeGeometry(routeApiRequest));

        if (routeApiRequest.hasIncludeManeuvers())
            routingRequest.setIncludeManeuvers(routeApiRequest.getIncludeManeuvers());

        if (routeApiRequest.hasIncludeInstructions())
            routingRequest.setIncludeInstructions(routeApiRequest.getIncludeInstructionsInResponse());

        if (routeApiRequest.hasIncludeRoundaboutExitInfo())
            routingRequest.setIncludeRoundaboutExits(routeApiRequest.getIncludeRoundaboutExitInfo());

        if (routeApiRequest.hasAttributes())
            routingRequest.setAttributes(convertAttributes(routeApiRequest));

        if (routeApiRequest.hasExtraInfo()) {
            routingRequest.setExtraInfo(convertExtraInfo(routeApiRequest));
            for (APIEnums.ExtraInfo extra : routeApiRequest.getExtraInfo()) {
                if (extra.compareTo(APIEnums.ExtraInfo.COUNTRY_INFO) == 0) {
                    routingRequest.setIncludeCountryInfo(true);
                }
            }
        }
        if (routeApiRequest.hasLanguage())
            routingRequest.setLanguage(convertLanguage(routeApiRequest.getLanguage()));

        if (routeApiRequest.hasInstructionsFormat())
            routingRequest.setInstructionsFormat(convertInstructionsFormat(routeApiRequest.getInstructionsFormat()));

        if (routeApiRequest.hasUnits())
            routingRequest.setUnits(convertUnits(routeApiRequest.getUnits()));

        if (routeApiRequest.hasSimplifyGeometry()) {
            routingRequest.setGeometrySimplify(routeApiRequest.getSimplifyGeometry());
            if (routeApiRequest.hasExtraInfo() && routeApiRequest.getSimplifyGeometry()) {
                throw new IncompatibleParameterException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, RouteRequest.PARAM_SIMPLIFY_GEOMETRY, "true", RouteRequest.PARAM_EXTRA_INFO, "*");
            }
        }

        if (routeApiRequest.hasSkipSegments()) {
            routingRequest.setSkipSegments(processSkipSegments(routeApiRequest));
        }

        if (routeApiRequest.hasId())
            routingRequest.setId(routeApiRequest.getId());

        if (routeApiRequest.hasMaximumSpeed()) {
            routingRequest.setMaximumSpeed(routeApiRequest.getMaximumSpeed());
        }

        int profileType = -1;

        int coordinatesLength = routeApiRequest.getCoordinates().size();

        RouteSearchParameters params = new RouteSearchParameters();

        params.setProfileName(routeApiRequest.getProfileName());

        if (routeApiRequest.hasExtraInfo()) {
            routingRequest.setExtraInfo(convertExtraInfo(routeApiRequest));//todo remove duplicate?
            params.setExtraInfo(convertExtraInfo(routeApiRequest));
        }

        if (routeApiRequest.hasSuppressWarnings())
            params.setSuppressWarnings(routeApiRequest.getSuppressWarnings());

        try {
            profileType = convertRouteProfileType(routeApiRequest.getProfile());
            params.setProfileType(profileType);
        } catch (Exception e) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_PROFILE);
        }

        APIEnums.RoutePreference preference = routeApiRequest.hasRoutePreference() ? routeApiRequest.getRoutePreference() : APIEnums.RoutePreference.RECOMMENDED;
        params.setWeightingMethod(convertWeightingMethod(routeApiRequest, preference));

        if (routeApiRequest.hasBearings())
            params.setBearings(convertBearings(routeApiRequest.getBearings(), coordinatesLength));

        if (routeApiRequest.hasContinueStraightAtWaypoints())
            params.setContinueStraight(routeApiRequest.getContinueStraightAtWaypoints());

        if (routeApiRequest.hasMaximumSearchRadii())
            params.setMaximumRadiuses(convertMaxRadii(routeApiRequest.getMaximumSearchRadii(), coordinatesLength, profileType));

        if (routeApiRequest.hasUseContractionHierarchies()) {
            params.setFlexibleMode(convertSetFlexibleMode(routeApiRequest.getUseContractionHierarchies()));
            params.setOptimized(routeApiRequest.getUseContractionHierarchies());
        }

        if (routeApiRequest.hasRouteOptions()) {
            params = processRouteRequestOptions(routeApiRequest, params);
        }

        if (routeApiRequest.hasAlternativeRoutes()) {
            if (routeApiRequest.getCoordinates().size() > 2) {
                throw new IncompatibleParameterException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, RouteRequest.PARAM_ALTERNATIVE_ROUTES, "(number of waypoints > 2)");
            }
            if (routeApiRequest.getAlternativeRoutes().hasTargetCount()) {
                params.setAlternativeRoutesCount(routeApiRequest.getAlternativeRoutes().getTargetCount());
                int countLimit = endpointsProperties.getRouting().getMaximumAlternativeRoutes();
                if (countLimit > 0 && routeApiRequest.getAlternativeRoutes().getTargetCount() > countLimit) {
                    throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_ALTERNATIVE_ROUTES, Integer.toString(routeApiRequest.getAlternativeRoutes().getTargetCount()), "The target alternative routes count has to be equal to or less than " + countLimit);
                }
            }
            if (routeApiRequest.getAlternativeRoutes().hasWeightFactor())
                params.setAlternativeRoutesWeightFactor(routeApiRequest.getAlternativeRoutes().getWeightFactor());
            if (routeApiRequest.getAlternativeRoutes().hasShareFactor())
                params.setAlternativeRoutesShareFactor(routeApiRequest.getAlternativeRoutes().getShareFactor());
        }

        if (routeApiRequest.hasDeparture() && routeApiRequest.hasArrival())
            throw new IncompatibleParameterException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, RouteRequest.PARAM_DEPARTURE, RouteRequest.PARAM_ARRIVAL);
        else if (routeApiRequest.hasDeparture())
            params.setDeparture(routeApiRequest.getDeparture());
        else if (routeApiRequest.hasArrival())
            params.setArrival(routeApiRequest.getArrival());

        if (routeApiRequest.hasMaximumSpeed()) {
            params.setMaximumSpeed(routeApiRequest.getMaximumSpeed());
        }

        // propagate GTFS-parameters to params to convert to ptRequest in RoutingProfile.computeRoute
        if (routeApiRequest.hasSchedule()) {
            params.setSchedule(routeApiRequest.getSchedule());
        }

        if (routeApiRequest.hasWalkingTime()) {
            params.setWalkingTime(routeApiRequest.getWalkingTime());
        }

        if (routeApiRequest.hasScheduleRows()) {
            params.setScheduleRows(routeApiRequest.getScheduleRows());
        }

        if (routeApiRequest.hasIgnoreTransfers()) {
            params.setIgnoreTransfers(routeApiRequest.isIgnoreTransfers());
        }

        if (routeApiRequest.hasScheduleDuration()) {
            params.setScheduleDuaration(routeApiRequest.getScheduleDuration());
        }

        if (routeApiRequest.hasCustomModel()) {
            params.setCustomModel(routeApiRequest.getCustomModel().toGHCustomModel());
        }

        params.setConsiderTurnRestrictions(false);

        routingRequest.setSearchParameters(params);

        return routingRequest;
    }

    private static RoutingProfile parseRoutingProfile(String profileName) throws InternalServerException {
        RoutingProfile rp = RoutingProfileManager.getInstance().getRoutingProfile(profileName);
        if (rp == null)
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Unable to find routing profile named '" + profileName + "'.");
        return rp;
    }

    private Coordinate[] convertCoordinates(List<List<Double>> coordinates, boolean allowSingleCoordinate) throws ParameterValueException {
        if (!allowSingleCoordinate && coordinates.size() < 2)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_COORDINATES);

        if (allowSingleCoordinate && coordinates.size() > 1)
            throw new ParameterValueException(
                    RoutingErrorCodes.INVALID_PARAMETER_VALUE,
                    RouteRequest.PARAM_COORDINATES,
                    "Length = " + coordinates.size(),
                    "Only one coordinate pair is allowed");

        ArrayList<Coordinate> coords = new ArrayList<>();

        for (List<Double> coord : coordinates) {
            coords.add(convertSingleCoordinate(coord));
        }

        return coords.toArray(new Coordinate[coords.size()]);
    }

    private Coordinate convertSingleCoordinate(List<Double> coordinate) throws ParameterValueException {
        if (coordinate.size() != 2)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_COORDINATES);

        return new Coordinate(coordinate.get(0), coordinate.get(1));
    }

    private String convertGeometryFormat(APIEnums.RouteResponseType responseType) throws ParameterValueException {
        switch (responseType) {
            case GEOJSON:
                return "geojson";
            case JSON:
                return "encodedpolyline";
            case GPX:
                return "gpx";
            default:
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_FORMAT);
        }
    }

    private boolean convertIncludeGeometry(RouteRequest request) throws IncompatibleParameterException {
        if (!request.getIncludeGeometry() && request.getResponseType() != APIEnums.RouteResponseType.JSON) {
            throw new IncompatibleParameterException(RoutingErrorCodes.INVALID_PARAMETER_VALUE,
                    RouteRequest.PARAM_GEOMETRY, "false",
                    RouteRequest.PARAM_FORMAT, APIEnums.RouteResponseType.GEOJSON + "/" + APIEnums.RouteResponseType.GPX);
        }
        return request.getIncludeGeometry();
    }

    //TODO method needed, or directly call delegate method?
    private static String[] convertAttributes(RouteRequest request) {
        return convertAPIEnumListToStrings(request.getAttributes());
    }

    private static int convertExtraInfo(RouteRequest request) {
        String[] extraInfosStrings = convertAPIEnumListToStrings(request.getExtraInfo());

        String extraInfoPiped = String.join("|", extraInfosStrings);

        return RouteExtraInfoFlag.getFromString(extraInfoPiped);
    }

    private String convertLanguage(APIEnums.Languages languageIn) throws StatusCodeException {
        boolean isLanguageSupported;
        String languageString = languageIn.toString();

        try {
            isLanguageSupported = LocalizationManager.getInstance().isLanguageSupported(languageString);
        } catch (Exception e) {
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Could not access Localization Manager");
        }

        if (!isLanguageSupported)
            throw new StatusCodeException(StatusCode.BAD_REQUEST, RoutingErrorCodes.INVALID_PARAMETER_VALUE, "Specified language '" + languageIn + "' is not supported.");

        return languageString;
    }

    private RouteInstructionsFormat convertInstructionsFormat(APIEnums.InstructionsFormat formatIn) throws UnknownParameterValueException {
        RouteInstructionsFormat instrFormat = RouteInstructionsFormat.fromString(formatIn.toString());
        if (instrFormat == RouteInstructionsFormat.UNKNOWN)
            throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_INSTRUCTIONS_FORMAT, formatIn.toString());

        return instrFormat;
    }

    private List<Integer> processSkipSegments(RouteRequest request) throws ParameterOutOfRangeException, ParameterValueException, EmptyElementException {
        for (Integer skipSegment : request.getSkipSegments()) {
            if (skipSegment >= request.getCoordinates().size()) {
                throw new ParameterOutOfRangeException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_SKIP_SEGMENTS, skipSegment.toString(), String.valueOf(request.getCoordinates().size() - 1));
            }
            if (skipSegment <= 0) {
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_SKIP_SEGMENTS, request.getSkipSegments().toString(), "The individual skip_segments values have to be greater than 0.");
            }

        }
        if (request.getSkipSegments().size() > request.getCoordinates().size() - 1) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_SKIP_SEGMENTS, request.getSkipSegments().toString(), "The amount of segments to skip shouldn't be more than segments in the coordinates.");
        }
        if (request.getSkipSegments().isEmpty()) {
            throw new EmptyElementException(RoutingErrorCodes.EMPTY_ELEMENT, RouteRequest.PARAM_SKIP_SEGMENTS);
        }
        return request.getSkipSegments();
    }

    private int convertWeightingMethod(RouteRequest request, APIEnums.RoutePreference preferenceIn) throws UnknownParameterValueException {
        if (request.getProfile().equals(APIEnums.Profile.DRIVING_CAR) && preferenceIn.equals(APIEnums.RoutePreference.RECOMMENDED)) {
            if (request.getCustomModel() != null)
                return WeightingMethod.CUSTOM;
            return WeightingMethod.FASTEST;
        }
        int weightingMethod = WeightingMethod.getFromString(preferenceIn.toString());
        if (weightingMethod == WeightingMethod.UNKNOWN)
            throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_PREFERENCE, preferenceIn.toString());

        return weightingMethod;
    }

    private WayPointBearing[] convertBearings(Double[][] bearingsIn, int coordinatesLength) throws ParameterValueException {
        if (bearingsIn == null || bearingsIn.length == 0)
            return new WayPointBearing[0];

        if (bearingsIn.length != coordinatesLength && bearingsIn.length != coordinatesLength - 1)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_BEARINGS, Arrays.toString(bearingsIn), "The number of bearings must be equal to the number of waypoints on the route.");

        WayPointBearing[] bearingsList = new WayPointBearing[coordinatesLength];
        for (int i = 0; i < bearingsIn.length; i++) {
            Double[] singleBearingIn = bearingsIn[i];

            if (singleBearingIn.length == 0) {
                bearingsList[i] = new WayPointBearing(Double.NaN);
            } else if (singleBearingIn.length == 1) {
                bearingsList[i] = new WayPointBearing(singleBearingIn[0]);
            } else {
                bearingsList[i] = new WayPointBearing(singleBearingIn[0], singleBearingIn[1]);
            }
        }

        return bearingsList;
    }

    private double[] convertMaxRadii(Double[] radiiIn, int coordinatesLength, int profileType) throws ParameterValueException {
        if (radiiIn != null) {
            if (radiiIn.length == 1) {
                double[] maxRadii = new double[coordinatesLength];
                Arrays.fill(maxRadii, radiiIn[0]);
                return maxRadii;
            }
            if (radiiIn.length != coordinatesLength)
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_RADII, Arrays.toString(radiiIn), "The number of specified radiuses must be one or equal to the number of specified waypoints.");
            return Stream.of(radiiIn).mapToDouble(Double::doubleValue).toArray();
        } else if (profileType == RoutingProfileType.WHEELCHAIR) {
            // As there are generally less ways that can be used as pedestrian ways, we need to restrict search
            // radii else we end up with starting and ending ways really far from the actual points. This is
            // especially a problem for wheelchair users as the restrictions are stricter
            double[] maxRadii = new double[coordinatesLength];
            Arrays.fill(maxRadii, 50);
            return maxRadii;
        } else {
            return new double[0];
        }
    }

    private boolean convertSetFlexibleMode(boolean useContractionHierarchies) throws ParameterValueException {
        if (useContractionHierarchies)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, RouteRequest.PARAM_OPTIMIZED);

        return true;
    }

    private RouteSearchParameters processRouteRequestOptions(RouteRequest request, RouteSearchParameters params) throws StatusCodeException {
        params = processRequestOptions(request.getRouteOptions(), params);
        if (request.getRouteOptions().hasProfileParams())
            params.setProfileParams(convertParameters(request.getRouteOptions(), params.getProfileType()));

        if (request.getRouteOptions().hasVehicleType())
            params.setVehicleType(convertVehicleType(request.getRouteOptions().getVehicleType(), params.getProfileType()));

        if (request.getRouteOptions().hasRoundTripOptions()) {
            RouteRequestRoundTripOptions roundTripOptions = request.getRouteOptions().getRoundTripOptions();
            if (roundTripOptions.hasLength()) {
                params.setRoundTripLength(roundTripOptions.getLength());
            } else {
                throw new MissingParameterException(RoutingErrorCodes.MISSING_PARAMETER, RouteRequestRoundTripOptions.PARAM_LENGTH);
            }
            if (roundTripOptions.hasPoints()) {
                params.setRoundTripPoints(roundTripOptions.getPoints());
            }
            if (roundTripOptions.hasSeed()) {
                params.setRoundTripSeed(roundTripOptions.getSeed());
            }
        }
        return params;
    }

}
