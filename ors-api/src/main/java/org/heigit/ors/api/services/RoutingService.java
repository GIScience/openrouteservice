package org.heigit.ors.api.services;

import org.heigit.ors.api.APIEnums;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.requests.routing.RouteRequestRoundTripOptions;
import org.heigit.ors.common.StatusCode;
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
    public RoutingService(EndpointsProperties endpointsProperties) {
        this.endpointsProperties = endpointsProperties;
    }

    @Override
    double getMaximumAvoidPolygonArea() {
        return this.endpointsProperties.getRouting().getMaximumAvoidPolygonArea();
    }

    @Override
    double getMaximumAvoidPolygonExtent() {
        return this.endpointsProperties.getRouting().getMaximumAvoidPolygonExtent();
    }

    public RouteResult[] generateRouteFromRequest(RouteRequest request) throws StatusCodeException {
        RoutingRequest routingRequest = this.convertRouteRequest(request);

        try {
            return RoutingProfileManager.getInstance().computeRoute(routingRequest);
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, RoutingErrorCodes.UNKNOWN);
        }
    }

    public RoutingRequest convertRouteRequest(RouteRequest request) throws StatusCodeException {
        RoutingRequest routingRequest = new RoutingRequest();
        boolean isRoundTrip = request.hasRouteOptions() && request.getRouteOptions().hasRoundTripOptions();
        routingRequest.setCoordinates(convertCoordinates(request.getCoordinates(), isRoundTrip));
        routingRequest.setGeometryFormat(convertGeometryFormat(request.getResponseType()));

        if (request.hasUseElevation())
            routingRequest.setIncludeElevation(request.getUseElevation());

        if (request.hasContinueStraightAtWaypoints())
            routingRequest.setContinueStraight(request.getContinueStraightAtWaypoints());

        if (request.hasIncludeGeometry())
            routingRequest.setIncludeGeometry(convertIncludeGeometry(request));

        if (request.hasIncludeManeuvers())
            routingRequest.setIncludeManeuvers(request.getIncludeManeuvers());

        if (request.hasIncludeInstructions())
            routingRequest.setIncludeInstructions(request.getIncludeInstructionsInResponse());

        if (request.hasIncludeRoundaboutExitInfo())
            routingRequest.setIncludeRoundaboutExits(request.getIncludeRoundaboutExitInfo());

        if (request.hasAttributes())
            routingRequest.setAttributes(convertAttributes(request));

        if (request.hasExtraInfo()) {
            routingRequest.setExtraInfo(convertExtraInfo(request));
            for (APIEnums.ExtraInfo extra : request.getExtraInfo()) {
                if (extra.compareTo(APIEnums.ExtraInfo.COUNTRY_INFO) == 0) {
                    routingRequest.setIncludeCountryInfo(true);
                }
            }
        }
        if (request.hasLanguage())
            routingRequest.setLanguage(convertLanguage(request.getLanguage()));

        if (request.hasInstructionsFormat())
            routingRequest.setInstructionsFormat(convertInstructionsFormat(request.getInstructionsFormat()));

        if (request.hasUnits())
            routingRequest.setUnits(convertUnits(request.getUnits()));

        if (request.hasSimplifyGeometry()) {
            routingRequest.setGeometrySimplify(request.getSimplifyGeometry());
            if (request.hasExtraInfo() && request.getSimplifyGeometry()) {
                throw new IncompatibleParameterException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, RouteRequest.PARAM_SIMPLIFY_GEOMETRY, "true", RouteRequest.PARAM_EXTRA_INFO, "*");
            }
        }

        if (request.hasSkipSegments()) {
            routingRequest.setSkipSegments(processSkipSegments(request));
        }

        if (request.hasId())
            routingRequest.setId(request.getId());

        if (request.hasMaximumSpeed()) {
            routingRequest.setMaximumSpeed(request.getMaximumSpeed());
        }

        int profileType = -1;

        int coordinatesLength = request.getCoordinates().size();

        RouteSearchParameters params = new RouteSearchParameters();

        if (request.hasExtraInfo()) {
            routingRequest.setExtraInfo(convertExtraInfo(request));//todo remove duplicate?
            params.setExtraInfo(convertExtraInfo(request));
        }

        if (request.hasSuppressWarnings())
            params.setSuppressWarnings(request.getSuppressWarnings());

        try {
            profileType = convertRouteProfileType(request.getProfile());
            params.setProfileType(profileType);
        } catch (Exception e) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_PROFILE);
        }

        APIEnums.RoutePreference preference = request.hasRoutePreference() ? request.getRoutePreference() : APIEnums.RoutePreference.RECOMMENDED;
        params.setWeightingMethod(convertWeightingMethod(request, preference));

        if (request.hasBearings())
            params.setBearings(convertBearings(request.getBearings(), coordinatesLength));

        if (request.hasContinueStraightAtWaypoints())
            params.setContinueStraight(request.getContinueStraightAtWaypoints());

        if (request.hasMaximumSearchRadii())
            params.setMaximumRadiuses(convertMaxRadii(request.getMaximumSearchRadii(), coordinatesLength, profileType));

        if (request.hasUseContractionHierarchies()) {
            params.setFlexibleMode(convertSetFlexibleMode(request.getUseContractionHierarchies()));
            params.setOptimized(request.getUseContractionHierarchies());
        }

        if (request.hasRouteOptions()) {
            params = processRouteRequestOptions(request, params);
        }

        if (request.hasAlternativeRoutes()) {
            if (request.getCoordinates().size() > 2) {
                throw new IncompatibleParameterException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, RouteRequest.PARAM_ALTERNATIVE_ROUTES, "(number of waypoints > 2)");
            }
            if (request.getAlternativeRoutes().hasTargetCount()) {
                params.setAlternativeRoutesCount(request.getAlternativeRoutes().getTargetCount());
                int countLimit = endpointsProperties.getRouting().getMaximumAlternativeRoutes();
                if (countLimit > 0 && request.getAlternativeRoutes().getTargetCount() > countLimit) {
                    throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_ALTERNATIVE_ROUTES, Integer.toString(request.getAlternativeRoutes().getTargetCount()), "The target alternative routes count has to be equal to or less than " + countLimit);
                }
            }
            if (request.getAlternativeRoutes().hasWeightFactor())
                params.setAlternativeRoutesWeightFactor(request.getAlternativeRoutes().getWeightFactor());
            if (request.getAlternativeRoutes().hasShareFactor())
                params.setAlternativeRoutesShareFactor(request.getAlternativeRoutes().getShareFactor());
        }

        if (request.hasDeparture() && request.hasArrival())
            throw new IncompatibleParameterException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, RouteRequest.PARAM_DEPARTURE, RouteRequest.PARAM_ARRIVAL);
        else if (request.hasDeparture())
            params.setDeparture(request.getDeparture());
        else if (request.hasArrival())
            params.setArrival(request.getArrival());

        if (request.hasMaximumSpeed()) {
            params.setMaximumSpeed(request.getMaximumSpeed());
        }

        // propagate GTFS-parameters to params to convert to ptRequest in RoutingProfile.computeRoute
        if (request.hasSchedule()) {
            params.setSchedule(request.getSchedule());
        }

        if (request.hasWalkingTime()) {
            params.setWalkingTime(request.getWalkingTime());
        }

        if (request.hasScheduleRows()) {
            params.setScheduleRows(request.getScheduleRows());
        }

        if (request.hasIgnoreTransfers()) {
            params.setIgnoreTransfers(request.isIgnoreTransfers());
        }

        if (request.hasScheduleDuration()) {
            params.setScheduleDuaration(request.getScheduleDuration());
        }

        params.setConsiderTurnRestrictions(false);

        routingRequest.setSearchParameters(params);

        return routingRequest;
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
        if (request.getProfile().equals(APIEnums.Profile.DRIVING_CAR) && preferenceIn.equals(APIEnums.RoutePreference.RECOMMENDED))
            return WeightingMethod.FASTEST;
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
