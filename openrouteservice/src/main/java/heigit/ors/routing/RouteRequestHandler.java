package heigit.ors.routing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import heigit.ors.api.requests.routing.*;
import heigit.ors.common.DistanceUnit;
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.localization.LocalizationManager;
import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;
import heigit.ors.routing.graphhopper.extensions.WheelchairTypesEncoder;
import heigit.ors.routing.parameters.*;
import heigit.ors.routing.pathprocessors.BordersExtractor;
import heigit.ors.util.DistanceUnitUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RouteRequestHandler {
    public static RouteResult generateRouteFromRequest(RouteRequest request) throws StatusCodeException, Exception  {
        RoutingRequest routingRequest = convertRouteRequest(request);

        return RoutingProfileManager.getInstance().computeRoute(routingRequest);
    }

    public static RoutingRequest convertRouteRequest(RouteRequest request) throws StatusCodeException {
        RoutingRequest routingRequest = new RoutingRequest();

        routingRequest.setCoordinates(request.getCoordinates());

        if(request.hasReturnElevationForPoints())
            routingRequest.setIncludeElevation(request.getReturnElevationForPoints());

        routingRequest.setContinueStraight(request.getContinueStraightAtWaypoints());

        routingRequest.setIncludeGeometry(request.getIncludeGeometry());

        routingRequest.setIncludeManeuvers(request.getIncĺudeManeuvers());

        routingRequest.setIncludeInstructions(request.getIncludeInstructionsInResponse());

        if(request.hasIncludeRoundaboutExitInfo())
            routingRequest.setIncludeRoundaboutExits(request.getIncludeRoundaboutExitInfo());

        if(request.hasAttributes())
            routingRequest.setAttributes(convertAttributes(request.getAttributes()));

        if(request.isHasExtraInfo())
            routingRequest.setExtraInfo(convertExtraInfo(request.getExtraInfo()));

        routingRequest.setLanguage(convertLanguage(request.getLanguage()));

        routingRequest.setGeometryFormat(convertAPIEnum(request.getGeometryType()));

        routingRequest.setInstructionsFormat(convertInstructionsFormat(request.getInstructionsFormat()));

        if(request.hasSimplifyGeography())
            routingRequest.setSimplifyGeometry(request.getSimplifyGeometry());

        routingRequest.setUnits(convertUnits(request.getUnits()));

        if(request.hasId())
            routingRequest.setId(request.getId());

        int profileType = -1;

        int coordinatesLength = request.getCoordinates().length;

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
                params.setVehicleType(convertVehicleType(options.getVehicleType()));

            params.setProfileParams(convertParameters(request, profileType));
        }

        params.setConsiderTraffic(false);

        params.setConsiderTurnRestrictions(false);

        routingRequest.setSearchParameters(params);

        return routingRequest;
    }

    private static int convertFeatureTypes(APIRoutingEnums.AvoidFeatures[] avoidFeatures, int profileType) throws UnknownParameterValueException, ParameterValueException {
        int flags = 0;
        for(APIRoutingEnums.AvoidFeatures avoid : avoidFeatures) {
            String avoidFeatureName = avoid.toString();
            int flag = AvoidFeatureFlags.getFromString(avoidFeatureName);
            if(flag == 0)
                throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "avoid_features", avoidFeatureName);

            if (!AvoidFeatureFlags.isValid(profileType, flag, avoidFeatureName))
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "avoid_features", avoidFeatureName);

            flags |= flag;
        }

        return flags;
    }

    private static int convertRouteProfileType(APIRoutingEnums.RoutingProfile profile) {
        return RoutingProfileType.getFromString(profile.toString());
    }

    private static BordersExtractor.Avoid convertAvoidBorders(APIRoutingEnums.AvoidBorders avoidBorders) {
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

    private static Polygon[] convertAvoidAreas(JSONObject geoJson) throws ParameterValueException {
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

    private double[][][] geoJsonPolygonCoordinates(JSONArray coordinatesIn) {
        List<List<Double[]>> group = new ArrayList();
        for(int i=0; i< coordinatesIn.size(); i++) {
            List<Double[]> polygonIn = (List<Double[]>) coordinatesIn.get(i);
            for(Double[] coords : polygonIn) {

            }
        }

        return null;
    }

    private static WayPointBearing[] convertBearings(Double[][] bearingsIn, int coordinatesLength) throws ParameterValueException {
        if(bearingsIn == null || bearingsIn.length == 0)
            return null;

        if(bearingsIn.length != coordinatesLength)
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

    private static double[] convertMaxRadii(Double[] radiiIn, int coordinatesLength, int profileType) throws ParameterValueException {
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
            // especially a problem for wheechair users as the restrictions are stricter

            for(int i=0; i<coordinatesLength; i++) {
                maxRadii[i] = 50;
            }
        } else {
            return null;
        }

        return maxRadii;
    }

    private static String[] convertAPIEnumListToStrings(Enum[] valuesIn) {
        String[] attributes = new String[valuesIn.length];
        for(int i=0; i<valuesIn.length; i++) {
            attributes[i] = convertAPIEnum(valuesIn[i]);
        }

        return attributes;
    }

    private static String convertAPIEnum(Enum valuesIn) {
        return valuesIn.toString();
    }

    private static String[] convertAttributes(APIRoutingEnums.Attributes[] attributes) {
        return convertAPIEnumListToStrings(attributes);
    }

    private static int convertExtraInfo(APIRoutingEnums.ExtraInfo[] extraInfos) {
        String[] extraInfosStrings = convertAPIEnumListToStrings(extraInfos);

        String extraInfoPiped = String.join("|", extraInfosStrings);

        return RouteExtraInfoFlag.getFromString(extraInfoPiped);
    }

    private static String convertLanguage(APIRoutingEnums.Languages languageIn) throws StatusCodeException {
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

    private static RouteInstructionsFormat convertInstructionsFormat(APIRoutingEnums.InstructionsFormat formatIn) throws UnknownParameterValueException {
        RouteInstructionsFormat instrFormat = RouteInstructionsFormat.fromString(formatIn.toString());
        if (instrFormat == RouteInstructionsFormat.UNKNOWN)
            throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "instructions_format", formatIn.toString());

        return instrFormat;
    }

    private static DistanceUnit convertUnits(APIRoutingEnums.Units unitsIn) throws ParameterValueException {
        DistanceUnit units = DistanceUnitUtil.getFromString(unitsIn.toString(), DistanceUnit.Unknown);

        if (units == DistanceUnit.Unknown)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "units", unitsIn.toString());

        return units;
    }

    private static int convertVehicleType(APIRoutingEnums.VehicleType vehicleTypeIn) throws ParameterValueException {
        if(vehicleTypeIn == null) {
            return HeavyVehicleAttributes.UNKNOWN;
        }

        return HeavyVehicleAttributes.getFromString(vehicleTypeIn.toString());
    }

    private static int convertWeightingMethod(APIRoutingEnums.RoutePreference preferenceIn) throws UnknownParameterValueException {
        int weightingMethod = WeightingMethod.getFromString(preferenceIn.toString());
        if (weightingMethod == WeightingMethod.UNKNOWN)
            throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "preference", preferenceIn.toString());

        return weightingMethod;
    }

    private static boolean convertSetFlexibleMode(boolean useContractionHierarchies) throws ParameterValueException {
        if(useContractionHierarchies)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, "optimized");

        return(!useContractionHierarchies);
    }

    private static ProfileParameters convertParameters(RouteRequest request, int profileType) {
        ProfileParameters params = new ProfileParameters();

        if(request.getRouteOptions().getProfileParams().hasRestrictions()) {

            RequestProfileParamsRestrictions restrictions = request.getRouteOptions().getProfileParams().getRestrictions();
            APIRoutingEnums.VehicleType vehicleType = request.getRouteOptions().getVehicleType();

            if (RoutingProfileType.isCycling(profileType))
                params = convertCyclingParameters(restrictions);
            if (RoutingProfileType.isHeavyVehicle(profileType))
                params = convertHeavyVehicleParameters(restrictions, vehicleType);
            if (RoutingProfileType.isWalking(profileType))
                params = convertWalkingParameters(restrictions);
            if (RoutingProfileType.isWheelchair(profileType))
                params = convertWheelchairParameters(restrictions);
        }

        if(request.getRouteOptions().getProfileParams().hasWeightings()) {
            RequestProfileParamsWeightings weightings = request.getRouteOptions().getProfileParams().getWeightings();
            applyWeightings(weightings, params);
        }

        return params;
    }

    private static CyclingParameters convertCyclingParameters(RequestProfileParamsRestrictions restrictions) {
        CyclingParameters params = new CyclingParameters();
        if(restrictions.hasGradient())
            params.setMaximumGradient(restrictions.getGradient());
        if(restrictions.hasTrailDifficulty())
            params.setMaximumTrailDifficulty(restrictions.getTrailDifficulty());

        return params;
    }

    private static WalkingParameters convertWalkingParameters(RequestProfileParamsRestrictions restrictions) {
        WalkingParameters params = new WalkingParameters();
        if(restrictions.hasGradient())
            params.setMaximumGradient(restrictions.getGradient());
        if(restrictions.hasTrailDifficulty())
            params.setMaximumTrailDifficulty(restrictions.getTrailDifficulty());

        return params;
    }

    private static VehicleParameters convertHeavyVehicleParameters(RequestProfileParamsRestrictions restrictions, APIRoutingEnums.VehicleType vehicleType) {
        VehicleParameters params = new VehicleParameters();
        if(vehicleType != null && vehicleType != APIRoutingEnums.VehicleType.UNKNOWN) {
            if(restrictions.hasLength())
                params.setLength(restrictions.getLength());
            if(restrictions.hasWidth())
                params.setWidth(restrictions.getWidth());
            if(restrictions.hasHeight())
                params.setHeight(restrictions.getHeight());
            if(restrictions.hasWeight())
                params.setWeight(restrictions.getWeight());
            if(restrictions.hasAxleLoad())
                params.setAxleload(restrictions.getAxleLoad());

            int loadCharacteristics = 0;
            if(restrictions.hasHazardousMaterial() && restrictions.getHazardousMaterial() == true)
                loadCharacteristics |= VehicleLoadCharacteristicsFlags.HAZMAT;

            if(loadCharacteristics != 0)
                params.setLoadCharacteristics(loadCharacteristics);
        }

        return params;
    }

    private static WheelchairParameters convertWheelchairParameters(RequestProfileParamsRestrictions restrictions) {
        WheelchairParameters params = new WheelchairParameters();

        if(restrictions.hasSurfaceType())
            params.setSurfaceType(WheelchairTypesEncoder.getSurfaceType(restrictions.getSurfaceType()));
        if(restrictions.hasTrackType())
            params.setTrackType(WheelchairTypesEncoder.getTrackType(restrictions.getTrackType()));
        if(restrictions.hasSmoothnessType())
            params.setSmoothnessType(WheelchairTypesEncoder.getSmoothnessType(restrictions.getSmoothnessType()));
        if(restrictions.hasMaxSlopedKerb())
            params.setMaximumSlopedKerb(restrictions.getMaxSlopedKerb());
        if(restrictions.hasMaxIncline())
            params.setMaximumIncline(restrictions.getMaxIncline());
        if(restrictions.hasMinWidth())
            params.setMinimumWidth(restrictions.getMinWidth());

        return params;
    }

    private static ProfileParameters applyWeightings(RequestProfileParamsWeightings weightings, ProfileParameters params) {
        try {
            if (weightings.hasGreenIndex()) {
                ProfileWeighting pw = new ProfileWeighting("green");
                pw.addParameter("factor", String.format("%.2f", weightings.getGreenIndex()));
                params.add(pw);
            }
            if(weightings.hasQuietIndex()) {
                ProfileWeighting pw = new ProfileWeighting("quiet");
                pw.addParameter("factor", String.format("%.2f", weightings.getQuietIndex()));
                params.add(pw);
            }
            if(weightings.hasSteepnessDifficulty()) {
                ProfileWeighting pw = new ProfileWeighting("steepness_difficulty");
                pw.addParameter("level", String.format("%d", weightings.getSteepnessDifficulty()));
                params.add(pw);
            }
        } catch (Exception e) {

        }

        return params;
    }
}
