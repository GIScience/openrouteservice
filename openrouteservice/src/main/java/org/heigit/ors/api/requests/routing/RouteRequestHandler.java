/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.api.requests.routing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.common.GenericHandler;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.config.AppConfig;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.geojson.GeometryJSON;
import org.heigit.ors.localization.LocalizationManager;
import org.heigit.ors.routing.*;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.heigit.ors.util.DistanceUnitUtil;
import org.heigit.ors.util.GeomUtility;
import org.heigit.ors.util.StringUtility;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class RouteRequestHandler extends GenericHandler {
    public RouteRequestHandler() {
        super();
        this.errorCodes.put("UNKNOWN_PARAMETER", RoutingErrorCodes.UNKNOWN_PARAMETER);
    }

    public RouteResult[] generateRouteFromRequest(RouteRequest request) throws StatusCodeException {
        RoutingRequest routingRequest = convertRouteRequest(request);

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
            routingRequest.setAttributes(convertAttributes(request.getAttributes()));

        if (request.hasExtraInfo()) {
            routingRequest.setExtraInfo(convertExtraInfo(request.getExtraInfo()));
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
            routingRequest.setExtraInfo(convertExtraInfo(request.getExtraInfo()));
            params.setExtraInfo(convertExtraInfo(request.getExtraInfo()));
        }

        if (request.hasSuppressWarnings())
            params.setSuppressWarnings(request.getSuppressWarnings());

        try {
            profileType = convertRouteProfileType(request.getProfile());
            params.setProfileType(profileType);
        } catch (Exception e) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_PROFILE);
        }

        if (request.hasRoutePreference())
            params.setWeightingMethod(convertWeightingMethod(request.getRoutePreference()));

        if (request.hasBearings())
            params.setBearings(convertBearings(request.getBearings(), coordinatesLength));

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
            RouteRequestAlternativeRoutes alternativeRoutes = request.getAlternativeRoutes();
            if (alternativeRoutes.hasTargetCount()) {
                params.setAlternativeRoutesCount(alternativeRoutes.getTargetCount());
                String paramMaxAlternativeRoutesCount = AppConfig.getGlobal().getRoutingProfileParameter(request.getProfile().toString(), "maximum_alternative_routes");
                int countLimit = StringUtility.isNullOrEmpty(paramMaxAlternativeRoutesCount) ? 0 : Integer.parseInt(paramMaxAlternativeRoutesCount);
                if (countLimit > 0 && alternativeRoutes.getTargetCount() > countLimit) {
                    throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_ALTERNATIVE_ROUTES, Integer.toString(alternativeRoutes.getTargetCount()), "The target alternative routes count has to be equal to or less than " + paramMaxAlternativeRoutesCount);
                }
            }
            if (alternativeRoutes.hasWeightFactor())
                params.setAlternativeRoutesWeightFactor(alternativeRoutes.getWeightFactor());
            if (alternativeRoutes.hasShareFactor())
                params.setAlternativeRoutesShareFactor(alternativeRoutes.getShareFactor());
        }

        if (request.hasMaximumSpeed()) {
            params.setMaximumSpeed(request.getMaximumSpeed());
        }

        params.setConsiderTurnRestrictions(false);

        routingRequest.setSearchParameters(params);

        return routingRequest;
    }

    private List<Integer> processSkipSegments(RouteRequest request) throws ParameterOutOfRangeException, ParameterValueException, EmptyElementException {
        List<Integer> skipSegments = request.getSkipSegments();
        for (Integer skipSegment : skipSegments) {
            if (skipSegment >= request.getCoordinates().size()) {
                throw new ParameterOutOfRangeException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_SKIP_SEGMENTS, skipSegment.toString(), String.valueOf(request.getCoordinates().size() - 1));
            }
            if (skipSegment <= 0) {
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_SKIP_SEGMENTS, skipSegments.toString(), "The individual skip_segments values have to be greater than 0.");
            }

        }
        if (skipSegments.size() > request.getCoordinates().size() - 1) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_SKIP_SEGMENTS, skipSegments.toString(), "The amount of segments to skip shouldn't be more than segments in the coordinates.");
        }
        if (skipSegments.isEmpty()) {
            throw new EmptyElementException(RoutingErrorCodes.EMPTY_ELEMENT, RouteRequest.PARAM_SKIP_SEGMENTS);
        }
        return skipSegments;
    }

    private RouteSearchParameters processRouteRequestOptions(RouteRequest request, RouteSearchParameters params) throws StatusCodeException {
        RouteRequestOptions routeOptions = request.getRouteOptions();
        params = processRequestOptions(routeOptions, params);
        if (routeOptions.hasProfileParams())
            params.setProfileParams(convertParameters(routeOptions, params.getProfileType()));
        return params;
    }

    public RouteSearchParameters processRequestOptions(RouteRequestOptions options, RouteSearchParameters params) throws StatusCodeException {
        if (options.hasAvoidBorders())
            params.setAvoidBorders(convertAvoidBorders(options.getAvoidBorders()));

        if (options.hasAvoidPolygonFeatures())
            params.setAvoidAreas(convertAvoidAreas(options.getAvoidPolygonFeatures(), params.getProfileType()));

        if (options.hasAvoidCountries())
            params.setAvoidCountries(convertAvoidCountries(options.getAvoidCountries()));

        if (options.hasAvoidFeatures())
            params.setAvoidFeatureTypes(convertFeatureTypes(options.getAvoidFeatures(), params.getProfileType()));

        if (options.hasVehicleType())
            params.setVehicleType(convertVehicleType(options.getVehicleType(), params.getProfileType()));

        if (options.hasRoundTripOptions()) {
            RouteRequestRoundTripOptions roundTripOptions = options.getRoundTripOptions();
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

    private boolean convertIncludeGeometry(RouteRequest request) throws IncompatibleParameterException {
        boolean includeGeometry = request.getIncludeGeometry();
        if (!includeGeometry && request.getResponseType() != APIEnums.RouteResponseType.JSON) {
            throw new IncompatibleParameterException(RoutingErrorCodes.INVALID_PARAMETER_VALUE,
                    RouteRequest.PARAM_GEOMETRY, "false",
                    RouteRequest.PARAM_FORMAT, APIEnums.RouteResponseType.GEOJSON + "/" + APIEnums.RouteResponseType.GPX);
        }
        return includeGeometry;
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

    @Override
    protected int convertFeatureTypes(APIEnums.AvoidFeatures[] avoidFeatures, int profileType) throws UnknownParameterValueException, IncompatibleParameterException {
        int flags = 0;
        for (APIEnums.AvoidFeatures avoid : avoidFeatures) {
            String avoidFeatureName = avoid.toString();
            int flag = AvoidFeatureFlags.getFromString(avoidFeatureName);
            if (flag == 0)
                throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequestOptions.PARAM_AVOID_FEATURES, avoidFeatureName);

            if (!AvoidFeatureFlags.isValid(profileType, flag))
                throw new IncompatibleParameterException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequestOptions.PARAM_AVOID_FEATURES, avoidFeatureName, RouteRequest.PARAM_PROFILE, RoutingProfileType.getName(profileType));

            flags |= flag;
        }

        return flags;
    }

    @Override
    public int convertRouteProfileType(APIEnums.Profile profile) {
        return RoutingProfileType.getFromString(profile.toString());
    }

    @Override
    protected BordersExtractor.Avoid convertAvoidBorders(APIEnums.AvoidBorders avoidBorders) {
        if (avoidBorders != null) {
            switch (avoidBorders) {
                case ALL:
                    return BordersExtractor.Avoid.ALL;
                case CONTROLLED:
                    return BordersExtractor.Avoid.CONTROLLED;
                default:
                    return BordersExtractor.Avoid.NONE;
            }
        }
        return null;
    }

    @Override
    protected Polygon[] convertAvoidAreas(JSONObject geoJson, int profileType) throws StatusCodeException {
        // It seems that arrays in json.simple cannot be converted to strings simply
        org.json.JSONObject complexJson = new org.json.JSONObject();
        complexJson.put("type", geoJson.get("type"));
        List<List<Double[]>> coordinates = (List<List<Double[]>>) geoJson.get("coordinates");
        complexJson.put("coordinates", coordinates);

        Geometry convertedGeom;
        try {
            convertedGeom = GeometryJSON.parse(complexJson);
        } catch (Exception e) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_JSON_FORMAT, RouteRequestOptions.PARAM_AVOID_POLYGONS);
        }

        Polygon[] avoidAreas;

        if (convertedGeom instanceof Polygon) {
            avoidAreas = new Polygon[]{(Polygon) convertedGeom};
        } else if (convertedGeom instanceof MultiPolygon) {
            MultiPolygon multiPoly = (MultiPolygon) convertedGeom;
            avoidAreas = new Polygon[multiPoly.getNumGeometries()];
            for (int i = 0; i < multiPoly.getNumGeometries(); i++)
                avoidAreas[i] = (Polygon) multiPoly.getGeometryN(i);
        } else {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequestOptions.PARAM_AVOID_POLYGONS);
        }

        String paramMaxAvoidPolygonArea = AppConfig.getGlobal().getRoutingProfileParameter(RoutingProfileType.getName(profileType), "maximum_avoid_polygon_area");
        String paramMaxAvoidPolygonExtent = AppConfig.getGlobal().getRoutingProfileParameter(RoutingProfileType.getName(profileType), "maximum_avoid_polygon_extent");
        double areaLimit = StringUtility.isNullOrEmpty(paramMaxAvoidPolygonArea) ? 0 : Double.parseDouble(paramMaxAvoidPolygonArea);
        double extentLimit = StringUtility.isNullOrEmpty(paramMaxAvoidPolygonExtent) ? 0 : Double.parseDouble(paramMaxAvoidPolygonExtent);
        for (Polygon avoidArea : avoidAreas) {
            try {
                if (areaLimit > 0) {
                    long area = Math.round(GeomUtility.getArea(avoidArea, true));
                    if (area > areaLimit) {
                        throw new StatusCodeException(StatusCode.BAD_REQUEST, RoutingErrorCodes.INVALID_PARAMETER_VALUE, String.format("The area of a polygon to avoid must not exceed %s square meters.", areaLimit));
                    }
                }
                if (extentLimit > 0) {
                    long extent = Math.round(GeomUtility.calculateMaxExtent(avoidArea));
                    if (extent > extentLimit) {
                        throw new StatusCodeException(StatusCode.BAD_REQUEST, RoutingErrorCodes.INVALID_PARAMETER_VALUE, String.format("The extent of a polygon to avoid must not exceed %s meters.", extentLimit));
                    }
                }
            } catch (InternalServerException e) {
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequestOptions.PARAM_AVOID_POLYGONS);
            }
        }

        return avoidAreas;
    }

    private WayPointBearing[] convertBearings(Double[][] bearingsIn, int coordinatesLength) throws ParameterValueException {
        if (bearingsIn == null || bearingsIn.length == 0)
            return new WayPointBearing[0];

        if (bearingsIn.length != coordinatesLength && bearingsIn.length != coordinatesLength - 1)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_BEARINGS, Arrays.toString(bearingsIn), "The number of bearings must be equal to the number of waypoints on the route.");

        WayPointBearing[] bearings = new WayPointBearing[coordinatesLength];
        for (int i = 0; i < bearingsIn.length; i++) {
            Double[] singleBearingIn = bearingsIn[i];

            if (singleBearingIn.length == 0) {
                bearings[i] = new WayPointBearing(Double.NaN, Double.NaN);
            } else if (singleBearingIn.length == 1) {
                bearings[i] = new WayPointBearing(singleBearingIn[0], Double.NaN);
            } else {
                bearings[i] = new WayPointBearing(singleBearingIn[0], singleBearingIn[1]);
            }
        }

        return bearings;
    }

    private double[] convertMaxRadii(Double[] radiiIn, int coordinatesLength, int profileType) throws ParameterValueException {
        if (radiiIn != null) {
            if (radiiIn.length != coordinatesLength)
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_RADII, Arrays.toString(radiiIn), "The number of radius pairs must be equal to the number of waypoints on the route.");
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

    private String[] convertAttributes(APIEnums.Attributes[] attributes) {
        return convertAPIEnumListToStrings(attributes);
    }

    private int convertExtraInfo(APIEnums.ExtraInfo[] extraInfos) {
        String[] extraInfosStrings = convertAPIEnumListToStrings(extraInfos);

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

    private DistanceUnit convertUnits(APIEnums.Units unitsIn) throws ParameterValueException {
        DistanceUnit units = DistanceUnitUtil.getFromString(unitsIn.toString(), DistanceUnit.UNKNOWN);

        if (units == DistanceUnit.UNKNOWN)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_UNITS, unitsIn.toString());

        return units;
    }

    private int convertWeightingMethod(APIEnums.RoutePreference preferenceIn) throws UnknownParameterValueException {
        int weightingMethod = WeightingMethod.getFromString(preferenceIn.toString());
        if (weightingMethod == WeightingMethod.UNKNOWN)
            throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_PREFERENCE, preferenceIn.toString());

        return weightingMethod;
    }

    private boolean convertSetFlexibleMode(boolean useContractionHierarchies) throws ParameterValueException {
        if (useContractionHierarchies)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, RouteRequest.PARAM_OPTIMIZED);

        return true;
    }

    private int[] convertAvoidCountries(String[] avoidCountries) throws ParameterValueException {
        int[] avoidCountryIds = new int[avoidCountries.length];
        if (avoidCountries.length > 0) {
            for (int i = 0; i < avoidCountries.length; i++) {
                try {
                    avoidCountryIds[i] = Integer.parseInt(avoidCountries[i]);
                } catch (NumberFormatException nfe) {
                    // Check if ISO-3166-1 Alpha-2 / Alpha-3 code
                    int countryId = CountryBordersReader.getCountryIdByISOCode(avoidCountries[i]);
                    if (countryId > 0) {
                        avoidCountryIds[i] = countryId;
                    } else {
                        throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequestOptions.PARAM_AVOID_COUNTRIES, avoidCountries[i]);
                    }
                }
            }
        }

        return avoidCountryIds;
    }
}