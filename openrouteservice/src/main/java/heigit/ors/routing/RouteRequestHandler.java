package heigit.ors.routing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import heigit.ors.api.requests.routing.*;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;
import heigit.ors.routing.graphhopper.extensions.WheelchairTypesEncoder;
import heigit.ors.routing.parameters.*;
import heigit.ors.routing.pathprocessors.BordersExtractor;
import org.json.simple.JSONObject;

public class RouteRequestHandler {
    public static RouteResult generateRouteFromRequest(RouteRequest request) throws ParameterValueException, Exception  {
        RoutingRequest routingRequest = new RoutingRequest();

        routingRequest.setCoordinates(request.getCoordinates());

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

        return RoutingProfileManager.getInstance().computeRoute(routingRequest);
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
        org.json.JSONObject jsonComplex = new org.json.JSONObject(geoJson.toJSONString());
        Geometry convertedGeom;
        try {
            convertedGeom = GeometryJSON.parse(jsonComplex);
        } catch (Exception e) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "avoid_polygons");
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

    private static WayPointBearing[] convertBearings(Double[][] bearingsIn, int coordinatesLength) throws ParameterValueException {
        if(bearingsIn == null || bearingsIn.length == 0)
            return null;

        if(bearingsIn.length != coordinatesLength)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "bearings", bearingsIn.toString());

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
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "radiuses", radiiIn.toString());
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
            params.setSmoothnessType(WheelchairTypesEncoder.getTrackType(restrictions.getSmoothnessType()));
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
