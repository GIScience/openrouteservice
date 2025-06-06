package org.heigit.ors.api.services;

import org.heigit.ors.api.APIEnums;
import org.heigit.ors.api.config.ApiEngineProperties;
import org.heigit.ors.api.config.EndpointsProperties;
import org.heigit.ors.api.requests.common.APIRequest;
import org.heigit.ors.api.requests.common.RequestOptions;
import org.heigit.ors.api.requests.routing.RequestProfileParamsRestrictions;
import org.heigit.ors.api.requests.routing.RequestProfileParamsWeightings;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.geojson.GeometryJSON;
import org.heigit.ors.routing.*;
import org.heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import org.heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairTypesEncoder;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import org.heigit.ors.routing.parameters.ProfileParameters;
import org.heigit.ors.routing.parameters.VehicleParameters;
import org.heigit.ors.routing.parameters.WheelchairParameters;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.heigit.ors.util.DistanceUnitUtil;
import org.heigit.ors.util.GeomUtility;
import org.json.simple.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Optional.ofNullable;

public class ApiService {

    protected EndpointsProperties endpointsProperties;
    protected ApiEngineProperties apiEngineProperties;

    double getMaximumAvoidPolygonArea() {
        return 0d;
    }

    double getMaximumAvoidPolygonExtent() {
        return 0d;
    }

    public static String[] convertAPIEnumListToStrings(Enum[] valuesIn) {
        String[] attributes = new String[valuesIn.length];

        for (int i = 0; i < valuesIn.length; i++) {
            attributes[i] = convertAPIEnum(valuesIn[i]);
        }

        return attributes;
    }

    protected static String convertAPIEnum(Enum valuesIn) {
        return valuesIn.toString();
    }

    public static int convertVehicleType(APIEnums.VehicleType vehicleTypeIn, int profileType) throws IncompatibleParameterException {
        if (!RoutingProfileType.isHeavyVehicle(profileType)) {
            throw new IncompatibleParameterException(GenericErrorCodes.INVALID_PARAMETER_VALUE,
                    "vehicle_type", vehicleTypeIn.toString(),
                    APIRequest.PARAM_PROFILE, RoutingProfileType.getName(profileType));
        }

        if (vehicleTypeIn == null) {
            return HeavyVehicleAttributes.UNKNOWN;
        }

        return HeavyVehicleAttributes.getFromString(vehicleTypeIn.toString());
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

    public static int convertRouteProfileType(APIEnums.Profile profile) {
        return RoutingProfileType.getFromString(profile.toString());
    }

    protected Polygon[] convertAndValidateAvoidAreas(JSONObject geoJson, int profileType) throws StatusCodeException {
        Polygon[] avoidAreas = convertAvoidAreas(geoJson);
        validateAreaLimits(avoidAreas, profileType);
        return avoidAreas;
    }

    protected Polygon[] convertAvoidAreas(JSONObject geoJson) throws StatusCodeException {
        // It seems that arrays in json.simple cannot be converted to strings simply
        org.json.JSONObject complexJson = new org.json.JSONObject();
        complexJson.put("type", geoJson.get("type"));
        List<List<Double[]>> coordinates = (List<List<Double[]>>) geoJson.get("coordinates");
        complexJson.put("coordinates", coordinates);

        Geometry convertedGeom;
        try {
            convertedGeom = GeometryJSON.parse(complexJson);
        } catch (Exception e) {
            throw new ParameterValueException(GenericErrorCodes.INVALID_JSON_FORMAT, RequestOptions.PARAM_AVOID_POLYGONS);
        }

        Polygon[] avoidAreas;

        if (convertedGeom instanceof Polygon) {
            avoidAreas = new Polygon[]{(Polygon) convertedGeom};
        } else if (convertedGeom instanceof MultiPolygon multiPoly) {
            avoidAreas = new Polygon[multiPoly.getNumGeometries()];
            for (int i = 0; i < multiPoly.getNumGeometries(); i++)
                avoidAreas[i] = (Polygon) multiPoly.getGeometryN(i);
        } else {
            throw new ParameterValueException(GenericErrorCodes.INVALID_PARAMETER_VALUE, RequestOptions.PARAM_AVOID_POLYGONS);
        }

        return avoidAreas;
    }

    protected void validateAreaLimits(Polygon[] avoidAreas, int profileType) throws StatusCodeException {
        double areaLimit = getMaximumAvoidPolygonArea();
        double extentLimit = getMaximumAvoidPolygonExtent();
        for (Polygon avoidArea : avoidAreas) {
            try {
                if (areaLimit > 0) {
                    long area = Math.round(GeomUtility.getArea(avoidArea, true));
                    if (area > areaLimit) {
                        throw new StatusCodeException(StatusCode.BAD_REQUEST, GenericErrorCodes.INVALID_PARAMETER_VALUE, String.format("The area of a polygon to avoid must not exceed %s square meters.", areaLimit));
                    }
                }
                if (extentLimit > 0) {
                    long extent = Math.round(GeomUtility.calculateMaxExtent(avoidArea));
                    if (extent > extentLimit) {
                        throw new StatusCodeException(StatusCode.BAD_REQUEST, GenericErrorCodes.INVALID_PARAMETER_VALUE, String.format("The extent of a polygon to avoid must not exceed %s meters.", extentLimit));
                    }
                }
            } catch (InternalServerException e) {
                throw new ParameterValueException(GenericErrorCodes.INVALID_PARAMETER_VALUE, RequestOptions.PARAM_AVOID_POLYGONS);
            }
        }
    }

    protected static int[] convertAvoidCountries(String[] avoidCountries) throws ParameterValueException {
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
                        throw new ParameterValueException(GenericErrorCodes.INVALID_PARAMETER_VALUE, RequestOptions.PARAM_AVOID_COUNTRIES, avoidCountries[i]);
                    }
                }
            }
        }

        return avoidCountryIds;
    }

    public static DistanceUnit convertUnits(APIEnums.Units unitsIn) throws ParameterValueException {
        DistanceUnit units = DistanceUnitUtil.getFromString(unitsIn.toString(), DistanceUnit.UNKNOWN);

        if (units == DistanceUnit.UNKNOWN)
            throw new ParameterValueException(GenericErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_UNITS, unitsIn.toString());

        return units;
    }

    protected static int convertFeatureTypes(APIEnums.AvoidFeatures[] avoidFeatures, int profileType) throws UnknownParameterValueException, IncompatibleParameterException {
        int flags = 0;
        for (APIEnums.AvoidFeatures avoid : avoidFeatures) {
            String avoidFeatureName = avoid.toString();
            int flag = AvoidFeatureFlags.getFromString(avoidFeatureName);
            if (flag == 0)
                throw new UnknownParameterValueException(GenericErrorCodes.INVALID_PARAMETER_VALUE, RequestOptions.PARAM_AVOID_FEATURES, avoidFeatureName);

            if (!AvoidFeatureFlags.isValid(profileType, flag))
                throw new IncompatibleParameterException(GenericErrorCodes.INVALID_PARAMETER_VALUE, RequestOptions.PARAM_AVOID_FEATURES, avoidFeatureName, APIRequest.PARAM_PROFILE, RoutingProfileType.getName(profileType));

            flags |= flag;
        }

        return flags;
    }

    public RouteSearchParameters processRequestOptions(RequestOptions options, RouteSearchParameters params) throws StatusCodeException {
        if (options.hasAvoidBorders())
            params.setAvoidBorders(convertAvoidBorders(options.getAvoidBorders()));

        if (options.hasAvoidPolygonFeatures())
            params.setAvoidAreas(convertAndValidateAvoidAreas(options.getAvoidPolygonFeatures(), params.getProfileType()));

        if (options.hasAvoidCountries())
            params.setAvoidCountries(convertAvoidCountries(options.getAvoidCountries()));

        if (options.hasAvoidFeatures())
            params.setAvoidFeatureTypes(convertFeatureTypes(options.getAvoidFeatures(), params.getProfileType()));

        return params;
    }

    protected ProfileParameters convertParameters(RequestOptions options, int profileType) throws StatusCodeException {
        ProfileParameters params = new ProfileParameters();
        if (options.getProfileParams().hasSurfaceQualityKnown() || options.getProfileParams().hasAllowUnsuitable()) {
            params = new WheelchairParameters();
        }

        if (options.getProfileParams().hasRestrictions()) {
            RequestProfileParamsRestrictions restrictions = options.getProfileParams().getRestrictions();
            validateRestrictionsForProfile(restrictions, profileType);
            params = convertSpecificProfileParameters(profileType, restrictions);
        }

        if (options.getProfileParams().hasWeightings()) {
            RequestProfileParamsWeightings weightings = options.getProfileParams().getWeightings();
            applyWeightings(weightings, params);
        }

        if (params instanceof WheelchairParameters) {
            if (options.getProfileParams().hasSurfaceQualityKnown()) {
                ((WheelchairParameters) params).setSurfaceQualityKnown(options.getProfileParams().getSurfaceQualityKnown());
            }
            if (options.getProfileParams().hasAllowUnsuitable()) {
                ((WheelchairParameters) params).setAllowUnsuitable(options.getProfileParams().getAllowUnsuitable());
            }
        }
        return params;
    }

    protected ProfileParameters convertSpecificProfileParameters(int profileType, RequestProfileParamsRestrictions restrictions) {
        ProfileParameters params = new ProfileParameters();
        if (RoutingProfileType.isHeavyVehicle(profileType))
            params = convertHeavyVehicleParameters(restrictions);
        if (RoutingProfileType.isWheelchair(profileType))
            params = convertWheelchairParamRestrictions(restrictions);
        return params;
    }

    private VehicleParameters convertHeavyVehicleParameters(RequestProfileParamsRestrictions restrictions) {

        VehicleParameters params = new VehicleParameters();

        setLengthParam(restrictions, params);
        setWidthParam(restrictions, params);
        setHeightParam(restrictions, params);
        setWeightParam(restrictions, params);
        setAxleLoadParam(restrictions, params);

        setLoadCharacteristicsParam(restrictions, params);

        return params;
    }

    private VehicleParameters setLengthParam(RequestProfileParamsRestrictions restrictions, VehicleParameters params) {
        if (params != null && restrictions != null && restrictions.hasLength()) {
            params.setLength(restrictions.getLength());
        }

        return params;
    }

    private VehicleParameters setWidthParam(RequestProfileParamsRestrictions restrictions, VehicleParameters params) {
        if (params != null && restrictions != null && restrictions.hasWidth()) {
            params.setWidth(restrictions.getWidth());
        }

        return params;
    }

    private VehicleParameters setHeightParam(RequestProfileParamsRestrictions restrictions, VehicleParameters params) {
        if (params != null && restrictions != null && restrictions.hasHeight()) {
            params.setHeight(restrictions.getHeight());
        }

        return params;
    }

    private VehicleParameters setWeightParam(RequestProfileParamsRestrictions restrictions, VehicleParameters params) {
        if (params != null && restrictions != null && restrictions.hasWeight()) {
            params.setWeight(restrictions.getWeight());
        }

        return params;
    }

    private VehicleParameters setAxleLoadParam(RequestProfileParamsRestrictions restrictions, VehicleParameters params) {
        if (params != null && restrictions != null && restrictions.hasAxleLoad()) {
            params.setAxleload(restrictions.getAxleLoad());
        }

        return params;
    }

    private VehicleParameters setLoadCharacteristicsParam(RequestProfileParamsRestrictions restrictions, VehicleParameters params) {
        if (params != null && restrictions != null) {
            int loadCharacteristics = 0;
            if (restrictions.hasHazardousMaterial() && restrictions.getHazardousMaterial())
                loadCharacteristics |= VehicleLoadCharacteristicsFlags.HAZMAT;

            if (loadCharacteristics != 0)
                params.setLoadCharacteristics(loadCharacteristics);
        }
        return params;
    }

    private WheelchairParameters convertWheelchairParamRestrictions(RequestProfileParamsRestrictions restrictions) {
        WheelchairParameters params = new WheelchairParameters();

        if (restrictions.hasSurfaceType())
            params.setSurfaceType(WheelchairTypesEncoder.getSurfaceType(restrictions.getSurfaceType()));
        if (restrictions.hasTrackType())
            params.setTrackType(WheelchairTypesEncoder.getTrackType(restrictions.getTrackType()));
        if (restrictions.hasSmoothnessType())
            params.setSmoothnessType(WheelchairTypesEncoder.getSmoothnessType(restrictions.getSmoothnessType().toString()));
        if (restrictions.hasMaxSlopedKerb())
            params.setMaximumSlopedKerb(restrictions.getMaxSlopedKerb());
        if (restrictions.hasMaxIncline())
            params.setMaximumIncline(restrictions.getMaxIncline());
        if (restrictions.hasMinWidth())
            params.setMinimumWidth(restrictions.getMinWidth());

        return params;
    }

    private void validateRestrictionsForProfile(RequestProfileParamsRestrictions restrictions, int profile) throws IncompatibleParameterException {
        // Check that we do not have some parameters that should not be there
        List<String> setRestrictions = restrictions.getRestrictionsThatAreSet();
        ProfileParameters params = new ProfileParameters();
        if (RoutingProfileType.isWheelchair(profile)) {
            params = new WheelchairParameters();
        }
        if (RoutingProfileType.isHeavyVehicle(profile)) {
            params = new VehicleParameters();
        }

        List<String> invalidParams = new ArrayList<>();

        for (String setRestriction : setRestrictions) {
            boolean valid = false;
            for (String validRestriction : params.getValidRestrictions()) {
                if (validRestriction.equals(setRestriction)) {
                    valid = true;
                    break;
                }
            }

            if (!valid) {
                invalidParams.add(setRestriction);
            }
        }

        if (!invalidParams.isEmpty()) {
            // There are some parameters present that shouldn't be there
            String invalidParamsString = String.join(", ", invalidParams);
            throw new IncompatibleParameterException(GenericErrorCodes.UNKNOWN_PARAMETER, "restrictions", invalidParamsString, APIRequest.PARAM_PROFILE, RoutingProfileType.getName(profile));
        }
    }

    private ProfileParameters applyWeightings(RequestProfileParamsWeightings weightings, ProfileParameters params) throws ParameterOutOfRangeException, ParameterValueException {
        String factorKey = "factor";
        try {
            if (weightings.hasGreenIndex()) {
                ProfileWeighting pw = new ProfileWeighting("green");
                Float greenFactor = weightings.getGreenIndex();
                if (greenFactor > 1)
                    throw new ParameterOutOfRangeException(GenericErrorCodes.INVALID_PARAMETER_VALUE, String.format(Locale.UK, "%.2f", greenFactor), "green factor", "1.0");
                pw.addParameter(factorKey, greenFactor);
                params.add(pw);
            }

            if (weightings.hasQuietIndex()) {
                ProfileWeighting pw = new ProfileWeighting("quiet");
                Float quietFactor = weightings.getQuietIndex();
                if (quietFactor > 1)
                    throw new ParameterOutOfRangeException(GenericErrorCodes.INVALID_PARAMETER_VALUE, String.format(Locale.UK, "%.2f", quietFactor), "quiet factor", "1.0");
                pw.addParameter(factorKey, quietFactor);
                params.add(pw);
            }

            if (weightings.hasShadowIndex()) {
                ProfileWeighting pw = new ProfileWeighting("shadow");
                Float shadowFactor = weightings.getShadowIndex();
                if (shadowFactor > 1)
                    throw new ParameterOutOfRangeException(GenericErrorCodes.INVALID_PARAMETER_VALUE, String.format(Locale.UK, "%.2f", shadowFactor), "shadow factor", "1.0");
                pw.addParameter(factorKey, shadowFactor);
                params.add(pw);
            }

            if (weightings.hasSteepnessDifficulty()) {
                ProfileWeighting pw = new ProfileWeighting("steepness_difficulty");
                pw.addParameter("level", weightings.getSteepnessDifficulty());
                params.add(pw);
            }
            if (weightings.hasCsv()) {
                ProfileWeighting pw = new ProfileWeighting("csv");
                pw.addParameter("column", weightings.getCsvColumn());
                pw.addParameter(factorKey, weightings.getCsvFactor());
                params.add(pw);
            }
        } catch (InternalServerException e) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "weightings");

        }

        return params;
    }

    public EncoderNameEnum getEncoderForProfile(String profile) {
        //TODO Change: use RoutingProfileManager
        return ofNullable(RoutingProfileManager.getInstance().getRoutingProfile(profile))
                .map(RoutingProfile::getProfileProperties)
                .map(ProfileProperties::getEncoderName)
                .orElse(EncoderNameEnum.UNKNOWN);
    }
}
