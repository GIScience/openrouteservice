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

package org.heigit.ors.api.requests.matrix;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.requests.routing.RouteRequestOptions;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.config.AppConfig;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.geojson.GeometryJSON;
import org.heigit.ors.matrix.MatrixErrorCodes;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.matrix.MatrixResult;
import org.heigit.ors.matrix.MatrixSearchParameters;
import org.heigit.ors.routing.*;
import org.heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.heigit.ors.services.matrix.MatrixServiceSettings;
import org.heigit.ors.util.DistanceUnitUtil;
import org.heigit.ors.util.GeomUtility;
import org.heigit.ors.util.StringUtility;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MatrixRequestHandler {
    private MatrixRequestHandler() throws InternalServerException {
        throw new InternalServerException("MatrixRequestHandler should not be instantiated empty");
    }

    public static MatrixResult generateMatrixFromRequest(MatrixRequest request) throws StatusCodeException {
        org.heigit.ors.matrix.MatrixRequest coreRequest = convertMatrixRequest(request);

        try {
            return RoutingProfileManager.getInstance().computeMatrix(coreRequest);
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(MatrixErrorCodes.UNKNOWN);
        }
    }

    public static org.heigit.ors.matrix.MatrixRequest convertMatrixRequest(MatrixRequest request) throws StatusCodeException {
        org.heigit.ors.matrix.MatrixRequest coreRequest = new org.heigit.ors.matrix.MatrixRequest();

        int sources = request.getSources() == null ? request.getLocations().size() : request.getSources().length;
        int destinations = request.getDestinations() == null ? request.getLocations().size() : request.getDestinations().length;
        Coordinate[] locations = convertLocations(request.getLocations(), sources * destinations);

        coreRequest.setProfileType(convertToMatrixProfileType(request.getProfile()));

        if (request.hasMetrics())
            coreRequest.setMetrics(convertMetrics(request.getMetrics()));

        if (request.hasDestinations())
            coreRequest.setDestinations(convertDestinations(request.getDestinations(), locations));
        else {
            coreRequest.setDestinations(convertDestinations(new String[]{"all"}, locations));
        }
        if (request.hasSources())
            coreRequest.setSources(convertSources(request.getSources(), locations));
        else {
            coreRequest.setSources(convertSources(new String[]{"all"}, locations));
        }
        if (request.hasId())
            coreRequest.setId(request.getId());
        if (request.hasOptimized())
            coreRequest.setFlexibleMode(!request.getOptimized());
        if (request.hasResolveLocations())
            coreRequest.setResolveLocations(request.getResolveLocations());
        if (request.hasUnits())
            coreRequest.setUnits(convertUnits(request.getUnits()));

        MatrixSearchParameters params = new MatrixSearchParameters();
        if(request.hasMatrixOptions())
            coreRequest.setFlexibleMode(processMatrixRequestOptions(request, params));
        coreRequest.setSearchParameters(params);
        return coreRequest;
    }

    private static boolean processMatrixRequestOptions(MatrixRequest request, MatrixSearchParameters params) throws StatusCodeException {
        MatrixRequestOptions routeOptions = request.getMatrixOptions();
        try {
            int profileType = convertRouteProfileType(request.getProfile());
            params.setProfileType(profileType);
        } catch (Exception e) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_PROFILE);
        }
        boolean flexibleMode = processRequestOptions(routeOptions, params);
        return flexibleMode;
    }

    public static int convertRouteProfileType(APIEnums.Profile profile) {
        return RoutingProfileType.getFromString(profile.toString());
    }

    public static boolean processRequestOptions(MatrixRequestOptions options, MatrixSearchParameters params) throws StatusCodeException {
        boolean flexibleMode = false;
        if (options.hasAvoidBorders()) {
            params.setAvoidBorders(convertAvoidBorders(options.getAvoidBorders()));
            flexibleMode = true;
        }

        if (options.hasAvoidPolygonFeatures()) {
            params.setAvoidAreas(convertAvoidAreas(options.getAvoidPolygonFeatures(), params.getProfileType()));
            flexibleMode = true;
        }

        if (options.hasAvoidCountries()) {
            params.setAvoidCountries(convertAvoidCountries(options.getAvoidCountries()));
            flexibleMode = true;
        }

        if (options.hasAvoidFeatures()) {
            params.setAvoidFeatureTypes(convertFeatureTypes(options.getAvoidFeatures(), params.getProfileType()));
            flexibleMode = true;
        }

        if (options.hasDynamicSpeeds()) {
            params.setDynamicSpeeds(options.getDynamicSpeeds());
            flexibleMode = true;
        }

        return flexibleMode;
    }

    public static int convertMetrics(MatrixRequestEnums.Metrics[] metrics) throws ParameterValueException {
        List<String> metricsAsStrings = new ArrayList<>();
        for (int i=0; i<metrics.length; i++) {
            metricsAsStrings.add(metrics[i].toString());
        }

        String concatMetrics = String.join("|", metricsAsStrings);

        int combined = MatrixMetricsType.getFromString(concatMetrics);

        if (combined == MatrixMetricsType.UNKNOWN)
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_METRICS);

        return combined;
    }

    protected static Coordinate[] convertLocations(List<List<Double>> locations, int numberOfRoutes) throws ParameterValueException, ServerLimitExceededException {
        if (locations == null || locations.size() < 2)
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_LOCATIONS);
        int maximumNumberOfRoutes = MatrixServiceSettings.getMaximumRoutes(false);
        if (numberOfRoutes > maximumNumberOfRoutes)
            throw new ServerLimitExceededException(MatrixErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "Only a total of " + maximumNumberOfRoutes + " routes are allowed.");
        ArrayList<Coordinate> locationCoordinates = new ArrayList<>();

        for (List<Double> coordinate : locations) {
            locationCoordinates.add(convertSingleLocationCoordinate(coordinate));
        }
        try {
            return locationCoordinates.toArray(new Coordinate[locations.size()]);
        } catch (NumberFormatException | ArrayStoreException | NullPointerException ex) {
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_LOCATIONS);
        }
    }

    protected static Coordinate convertSingleLocationCoordinate(List<Double> coordinate) throws ParameterValueException {
        if (coordinate.size() != 2)
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_LOCATIONS);
        return new Coordinate(coordinate.get(0), coordinate.get(1));
    }

    protected static Coordinate[] convertSources(String[] sourcesIndex, Coordinate[] locations) throws ParameterValueException {
        int length = sourcesIndex.length;
        if (length == 0) return locations;
        if (length == 1 && "all".equalsIgnoreCase(sourcesIndex[0])) return locations;
        try {
            ArrayList<Coordinate> indexCoordinateArray = convertIndexToLocations(sourcesIndex, locations);
            return indexCoordinateArray.toArray(new Coordinate[0]);
        } catch (Exception ex) {
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_SOURCES);
        }
    }

    protected static Coordinate[] convertDestinations(String[] destinationsIndex, Coordinate[] locations) throws ParameterValueException {
        int length = destinationsIndex.length;
        if (length == 0) return locations;
        if (length == 1 && "all".equalsIgnoreCase(destinationsIndex[0])) return locations;
        try {
            ArrayList<Coordinate> indexCoordinateArray = convertIndexToLocations(destinationsIndex, locations);
            return indexCoordinateArray.toArray(new Coordinate[0]);
        } catch (Exception ex) {
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_DESTINATIONS);
        }
    }

    protected static ArrayList<Coordinate> convertIndexToLocations(String[] index, Coordinate[] locations) {
        ArrayList<Coordinate> indexCoordinates = new ArrayList<>();
        for (String indexString : index) {
            int indexInteger = Integer.parseInt(indexString);
            indexCoordinates.add(locations[indexInteger]);
        }
        return indexCoordinates;
    }

    protected static DistanceUnit convertUnits(APIEnums.Units unitsIn) throws ParameterValueException {
        DistanceUnit units = DistanceUnitUtil.getFromString(unitsIn.toString(), DistanceUnit.UNKNOWN);
        if (units == DistanceUnit.UNKNOWN)
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_UNITS, unitsIn.toString());
        return units;
    }

    protected static int convertToMatrixProfileType(APIEnums.Profile profile) throws ParameterValueException {
        try {
            int profileFromString = RoutingProfileType.getFromString(profile.toString());
            if (profileFromString == 0) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_PROFILE);
            }
            return profileFromString;
        } catch (Exception e) {
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_PROFILE);
        }
    }

    protected static BordersExtractor.Avoid convertAvoidBorders(APIEnums.AvoidBorders avoidBorders) {
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

    protected static Polygon[] convertAvoidAreas(JSONObject geoJson, int profileType) throws StatusCodeException {
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

    private static int[] convertAvoidCountries(String[] avoidCountries) throws ParameterValueException {
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

    protected static int convertFeatureTypes(APIEnums.AvoidFeatures[] avoidFeatures, int profileType) throws UnknownParameterValueException, IncompatibleParameterException {
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
}
