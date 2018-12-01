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

package heigit.ors.api.requests.routing;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.common.GenericHandler;
import heigit.ors.common.DistanceUnit;
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.*;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.localization.LocalizationManager;
import heigit.ors.routing.*;
import heigit.ors.routing.pathprocessors.BordersExtractor;
import heigit.ors.util.DistanceUnitUtil;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RouteRequestHandler extends GenericHandler {
    public RouteRequestHandler() {
        super();
        this.errorCodes.put("UNKNOWN_PARAMETER", RoutingErrorCodes.UNKNOWN_PARAMETER);
        this.errorCodes.put("INVALID_JSON_FORMAT", RoutingErrorCodes.INVALID_JSON_FORMAT);
        this.errorCodes.put("INVALID_PARAMETER_VALUE", RoutingErrorCodes.INVALID_PARAMETER_VALUE);

    }

    public  RouteResult generateRouteFromRequest(RouteRequest request) throws StatusCodeException{
        RoutingRequest routingRequest = convertRouteRequest(request);

        try {
            RouteResult result = RoutingProfileManager.getInstance().computeRoute(routingRequest);
            return result;
        } catch (Exception e) {
            if(e instanceof StatusCodeException)
                throw (StatusCodeException)e;

            throw new StatusCodeException(RoutingErrorCodes.UNKNOWN);
        }
    }

    public  RoutingRequest convertRouteRequest(RouteRequest request) throws StatusCodeException {
        RoutingRequest routingRequest = new RoutingRequest();
        routingRequest.setCoordinates(convertCoordinates(request.getCoordinates()));

        if(request.hasReturnElevationForPoints())
            routingRequest.setIncludeElevation(request.getUseElevation());

        routingRequest.setContinueStraight(request.getContinueStraightAtWaypoints());

        routingRequest.setIncludeGeometry(convertIncludeGeometry(request));

        routingRequest.setIncludeManeuvers(request.getIncĺudeManeuvers());

        routingRequest.setIncludeInstructions(request.getIncludeInstructionsInResponse());

        if(request.hasIncludeRoundaboutExitInfo())
            routingRequest.setIncludeRoundaboutExits(request.getIncludeRoundaboutExitInfo());

        if(request.hasAttributes())
            routingRequest.setAttributes(convertAttributes(request.getAttributes()));

        if(request.isHasExtraInfo())
            routingRequest.setExtraInfo(convertExtraInfo(request.getExtraInfo()));

        routingRequest.setLanguage(convertLanguage(request.getLanguage()));

        routingRequest.setGeometryFormat(convertGeometryFormat(request.getResponseType()));

        routingRequest.setInstructionsFormat(convertInstructionsFormat(request.getInstructionsFormat()));

        routingRequest.setUnits(convertUnits(request.getUnits()));

        if(request.hasId())
            routingRequest.setId(request.getId());

        int profileType = -1;

        int coordinatesLength = request.getCoordinates().size();

        RouteSearchParameters params = new RouteSearchParameters();

        try {
            profileType = convertRouteProfileType(request.getProfile());
            params.setProfileType(profileType);
        } catch (Exception e) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "profile");
        }

        params.setWeightingMethod(convertWeightingMethod(request.getRoutePreference()));

        if(request.hasBearings())
            params.setBearings(convertBearings(request.getBearings(), coordinatesLength));

        if(request.hasMaximumSearchRadii())
            params.setMaximumRadiuses(convertMaxRadii(request.getMaximumSearchRadii(), coordinatesLength, profileType));

        if(request.hasUseContractionHierarchies())
            params.setFlexibleMode(convertSetFlexibleMode(request.getUseContractionHierarchies()));

        if(request.hasRouteOptions()) {
            RouteRequestOptions options = request.getRouteOptions();
            if (options.hasAvoidBorders())
                params.setAvoidBorders(convertAvoidBorders(options.getAvoidBorders()));

            if (options.hasAvoidPolygonFeatures())
                params.setAvoidAreas(convertAvoidAreas(options.getAvoidPolygonFeatures()));

            if (options.hasAvoidCountries())
                params.setAvoidCountries(options.getAvoidCountries());

            if (options.hasAvoidFeatures())
                params.setAvoidFeatureTypes(convertFeatureTypes(options.getAvoidFeatures(), profileType));

            if (options.hasMaximumSpeed())
                params.setMaximumSpeed(options.getMaximumSpeed());

            if (options.hasVehicleType())
                params.setVehicleType(convertVehicleType(options.getVehicleType(), profileType));

            if(options.hasProfileParams())
                params.setProfileParams(convertParameters(request, profileType));
        }

        params.setConsiderTraffic(false);

        params.setConsiderTurnRestrictions(false);

        routingRequest.setSearchParameters(params);

        return routingRequest;
    }

    private  boolean convertIncludeGeometry(RouteRequest request) throws IncompatableParameterException {
        boolean includeGeometry = request.getIncludeGeometry();
        if(!includeGeometry) {
            if(request.getResponseType() == APIEnums.RouteResponseType.GEOJSON)
                throw new IncompatableParameterException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "geometry", "false", "response type", "geojson");
        }
        return includeGeometry;
    }

    private  String convertGeometryFormat(APIEnums.RouteResponseType responseType) throws ParameterValueException {
        switch(responseType) {
            case GEOJSON:
                return "geojson";
            case JSON:
                return "encodedpolyline";
            case GPX:
                return "gpx";
                default:
                    throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "format");
        }
    }

    private  Coordinate[] convertCoordinates(List<List<Double>> coordinates) throws ParameterValueException {
        if(coordinates.size() < 2)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "coordinates");

        ArrayList<Coordinate> coords = new ArrayList<>();

        for(List<Double> coord : coordinates) {
            coords.add(convertSingleCoordinate(coord));
        }

        return coords.toArray(new Coordinate[coords.size()]);
    }

    private  Coordinate convertSingleCoordinate(List<Double> coordinate) throws ParameterValueException {
        if(coordinate.size() != 2)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "coordinates");

        return new Coordinate(coordinate.get(0), coordinate.get(1));
    }

    private  int convertFeatureTypes(APIEnums.AvoidFeatures[] avoidFeatures, int profileType) throws UnknownParameterValueException, IncompatableParameterException {
        int flags = 0;
        for(APIEnums.AvoidFeatures avoid : avoidFeatures) {
            String avoidFeatureName = avoid.toString();
            int flag = AvoidFeatureFlags.getFromString(avoidFeatureName);
            if(flag == 0)
                throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "avoid_features", avoidFeatureName);

            if (!AvoidFeatureFlags.isValid(profileType, flag, avoidFeatureName))
                throw new IncompatableParameterException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "avoid_features", avoidFeatureName, "profile", RoutingProfileType.getName(profileType));

            flags |= flag;
        }

        return flags;
    }

    private  int convertRouteProfileType(APIEnums.Profile profile) {
        return RoutingProfileType.getFromString(profile.toString());
    }

    private  BordersExtractor.Avoid convertAvoidBorders(APIEnums.AvoidBorders avoidBorders) {
        if(avoidBorders != null) {
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

    private  Polygon[] convertAvoidAreas(JSONObject geoJson) throws ParameterValueException {
        // It seems that arrays in json.simple cannot be converted to strings simply
        org.json.JSONObject complexJson = new org.json.JSONObject();
        complexJson.put("type", geoJson.get("type"));
        List<List<Double[]>> coordinates = (List<List<Double[]>>) geoJson.get("coordinates");
        complexJson.put("coordinates", coordinates);

        Geometry convertedGeom;
        try {
            convertedGeom = GeometryJSON.parse(complexJson);
        } catch (Exception e) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_JSON_FORMAT, "avoid_polygons");
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
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "avoid_polygons");
        }

        return avoidAreas;
    }

    private  WayPointBearing[] convertBearings(Double[][] bearingsIn, int coordinatesLength) throws ParameterValueException {
        if(bearingsIn == null || bearingsIn.length == 0)
            return null;

        if(bearingsIn.length != coordinatesLength && bearingsIn.length != coordinatesLength-1)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "bearings", bearingsIn.toString(), "The number of bearings must be equal to the number of waypoints on the route.");

        WayPointBearing[] bearings = new WayPointBearing[coordinatesLength];
        for(int i=0; i<bearingsIn.length; i++) {
            Double[] singleBearingIn = bearingsIn[i];

            if(singleBearingIn.length == 0) {
                bearings[i] = new WayPointBearing(Double.NaN, Double.NaN);
            } else if(singleBearingIn.length == 1) {
                bearings[i] = new WayPointBearing(singleBearingIn[0], Double.NaN);
            } else {
                bearings[i] = new WayPointBearing(singleBearingIn[0], singleBearingIn[1]);
            }
        }

        return bearings;
    }

    private  double[] convertMaxRadii(Double[] radiiIn, int coordinatesLength, int profileType) throws ParameterValueException {
        double[] maxRadii = new double[coordinatesLength];
        if(radiiIn != null) {
            if(radiiIn.length != coordinatesLength)
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "radiuses", radiiIn.toString(), "The number of radius pairs must be equal to the number of waypoints on the route.");
            for(int i=0; i<coordinatesLength; i++) {
                maxRadii[i] = radiiIn[i];
            }
        } else if(profileType == RoutingProfileType.WHEELCHAIR) {
            // As there are generally less ways that can be used as pedestrian ways, we need to restrict search
            // radii else we end up with starting and ending ways really far from the actual points. This is
            // especially a problem for wheelchair users as the restrictions are stricter

            for(int i=0; i<coordinatesLength; i++) {
                maxRadii[i] = 50;
            }
        } else {
            return null;
        }

        return maxRadii;
    }

    private  String[] convertAttributes(APIEnums.Attributes[] attributes) {
        return convertAPIEnumListToStrings(attributes);
    }

    private  int convertExtraInfo(APIEnums.ExtraInfo[] extraInfos) {
        String[] extraInfosStrings = convertAPIEnumListToStrings(extraInfos);

        String extraInfoPiped = String.join("|", extraInfosStrings);

        return RouteExtraInfoFlag.getFromString(extraInfoPiped);
    }

    private  String convertLanguage(APIEnums.Languages languageIn) throws StatusCodeException {
        boolean isLanguageSupported;
        String languageString = languageIn.toString();

        try {
            isLanguageSupported = LocalizationManager.getInstance().isLanguageSupported(languageString);
        } catch (Exception e) {
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Could not access Localization Manager");
        }

        if(!isLanguageSupported)
            throw new StatusCodeException(StatusCode.BAD_REQUEST, RoutingErrorCodes.INVALID_PARAMETER_VALUE, "Specified language '" +  languageIn + "' is not supported.");

        return languageString;
    }

    private  RouteInstructionsFormat convertInstructionsFormat(APIEnums.InstructionsFormat formatIn) throws UnknownParameterValueException {
        RouteInstructionsFormat instrFormat = RouteInstructionsFormat.fromString(formatIn.toString());
        if (instrFormat == RouteInstructionsFormat.UNKNOWN)
            throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "instructions_format", formatIn.toString());

        return instrFormat;
    }

    private  DistanceUnit convertUnits(APIEnums.Units unitsIn) throws ParameterValueException {
        DistanceUnit units = DistanceUnitUtil.getFromString(unitsIn.toString(), DistanceUnit.Unknown);

        if (units == DistanceUnit.Unknown)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "units", unitsIn.toString());

        return units;
    }

    private  int convertWeightingMethod(APIEnums.RoutePreference preferenceIn) throws UnknownParameterValueException {
        int weightingMethod = WeightingMethod.getFromString(preferenceIn.toString());
        if (weightingMethod == WeightingMethod.UNKNOWN)
            throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "preference", preferenceIn.toString());

        return weightingMethod;
    }

    private  boolean convertSetFlexibleMode(boolean useContractionHierarchies) throws ParameterValueException {
        if(useContractionHierarchies)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, "optimized");

        return(!useContractionHierarchies);
    }
}
