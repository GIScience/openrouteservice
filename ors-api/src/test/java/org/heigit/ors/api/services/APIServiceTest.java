package org.heigit.ors.api.services;

import org.heigit.ors.api.requests.routing.RequestProfileParams;
import org.heigit.ors.api.requests.routing.RequestProfileParamsRestrictions;
import org.heigit.ors.api.requests.routing.RouteRequestOptions;
import org.heigit.ors.exceptions.IncompatibleParameterException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.exceptions.UnknownParameterValueException;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.routing.parameters.ProfileParameters;
import org.heigit.ors.routing.parameters.VehicleParameters;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Polygon;

import static org.junit.jupiter.api.Assertions.*;

class APIServiceTest {

    ApiService apiService;

    @BeforeEach
    void setUp() throws Exception {
        apiService = new ApiService();
    }

    @Test
    void convertAPIEnumListToStrings() {
        String[] strVals = ApiService.convertAPIEnumListToStrings(new APIEnums.ExtraInfo[]{APIEnums.ExtraInfo.STEEPNESS, APIEnums.ExtraInfo.SURFACE});
        assertEquals(2, strVals.length);
        assertEquals("steepness", strVals[0]);
        assertEquals("surface", strVals[1]);
    }

    @Test
    void convertAPIEnum() {
        String strVal = ApiService.convertAPIEnum(APIEnums.AvoidBorders.CONTROLLED);
        assertEquals("controlled", strVal);
    }

    @Test
    void convertVehicleType() throws IncompatibleParameterException {
        int type = ApiService.convertVehicleType(APIEnums.VehicleType.HGV, 2);
        assertEquals(2, type);
    }

    @Test
    void convertVehicleTypeError() throws IncompatibleParameterException {
        assertThrows(IncompatibleParameterException.class, () -> {
            ApiService.convertVehicleType(APIEnums.VehicleType.HGV, 1);
        });
    }

    @Test
    void convertAvoidBorders() {
        BordersExtractor.Avoid avoid = ApiService.convertAvoidBorders(APIEnums.AvoidBorders.CONTROLLED);
        assertEquals(BordersExtractor.Avoid.CONTROLLED, avoid);
        avoid = ApiService.convertAvoidBorders(APIEnums.AvoidBorders.ALL);
        assertEquals(BordersExtractor.Avoid.ALL, avoid);
        avoid = ApiService.convertAvoidBorders(APIEnums.AvoidBorders.NONE);
        assertEquals(BordersExtractor.Avoid.NONE, avoid);
    }

    @Test
    void convertRouteProfileType() {
        int type = ApiService.convertRouteProfileType(APIEnums.Profile.DRIVING_CAR);
        assertEquals(1, type);
        type = ApiService.convertRouteProfileType(APIEnums.Profile.FOOT_WALKING);
        assertEquals(20, type);
    }

    @Test
    void convertAvoidAreas() throws StatusCodeException {
        JSONObject geomJSON = new JSONObject();
        geomJSON.put("type", "Polygon");

        JSONArray poly = generateGeoJSONPolyCoords();

        JSONArray coords = new JSONArray();
        coords.add(0, poly);
        geomJSON.put("coordinates", coords);

        Polygon[] avoidAreas = apiService.convertAvoidAreas(geomJSON);
        assertEquals(1, avoidAreas.length);
        assertEquals(4, avoidAreas[0].getCoordinates().length);
        assertEquals(1, avoidAreas[0].getCoordinates()[0].x, 0.0);

        JSONObject geomJSONMulti = new JSONObject();
        geomJSONMulti.put("type", "MultiPolygon");

        JSONArray polys1 = new JSONArray();
        polys1.add(0, poly);
        JSONArray polys2 = new JSONArray();
        polys2.add(0, poly);

        coords = new JSONArray();
        coords.add(0, polys1);
        coords.add(0, polys2);

        geomJSONMulti.put("coordinates", coords);

        avoidAreas = apiService.convertAvoidAreas(geomJSONMulti);

        assertEquals(2, avoidAreas.length);
    }

    @Test
    void convertAvoidAreasInvalidType() throws StatusCodeException {
        assertThrows(ParameterValueException.class, () -> {
            JSONObject geomJSON = new JSONObject();
            geomJSON.put("type", "LineString");

            JSONArray poly = generateGeoJSONPolyCoords();

            geomJSON.put("coordinates", poly);
            apiService.convertAvoidAreas(geomJSON);
        });
    }

    @Test
    void convertAvoidAreasInvalidFeature() throws StatusCodeException {
        assertThrows(ParameterValueException.class, () -> {
            JSONObject geomJSON = new JSONObject();
            geomJSON.put("type", "Polygon");

            JSONArray poly = generateGeoJSONPolyCoords();

            geomJSON.put("coooooooooooordinates", poly);
            apiService.convertAvoidAreas(geomJSON);
        });
    }

    private JSONArray generateGeoJSONPolyCoords() {
        JSONArray coords = new JSONArray();
        JSONArray coord0 = new JSONArray();
        coord0.add(0, 1.0);
        coord0.add(1, 1.0);
        coords.add(0, coord0);
        JSONArray coord1 = new JSONArray();
        coord1.add(0, 1.0);
        coord1.add(1, 2.0);
        coords.add(0, coord1);
        JSONArray coord2 = new JSONArray();
        coord2.add(0, 2.0);
        coord2.add(1, 1.0);
        coords.add(0, coord2);
        JSONArray coord3 = new JSONArray();
        coord3.add(0, 1.0);
        coord3.add(1, 1.0);
        coords.add(0, coord3);

        JSONArray poly = new JSONArray();
        poly.add(0, coords);

        return coords;
    }

    @Test
    void convertFeatureTypes() throws UnknownParameterValueException, IncompatibleParameterException {
        APIEnums.AvoidFeatures[] avoids = new APIEnums.AvoidFeatures[]{APIEnums.AvoidFeatures.FERRIES, APIEnums.AvoidFeatures.FORDS};
        int converted = ApiService.convertFeatureTypes(avoids, 1);
        assertEquals(24, converted);
    }

    @Test
    void convertFeatureTypesIncompatible() throws UnknownParameterValueException, IncompatibleParameterException {
        assertThrows(IncompatibleParameterException.class, () -> {
            APIEnums.AvoidFeatures[] avoids = new APIEnums.AvoidFeatures[]{APIEnums.AvoidFeatures.STEPS};
            ApiService.convertFeatureTypes(avoids, 1);
        });
    }

    @Test
    void convertParameters() throws StatusCodeException {
        RouteRequestOptions opts = new RouteRequestOptions();
        RequestProfileParams params = new RequestProfileParams();
        RequestProfileParamsRestrictions restrictions = new RequestProfileParamsRestrictions();
        restrictions.setHeight(10.0f);
        params.setRestrictions(restrictions);
        opts.setVehicleType(APIEnums.VehicleType.HGV);
        opts.setProfileParams(params);

        ProfileParameters generatedParams = apiService.convertParameters(opts, 2);

        assertEquals(10.0f, ((VehicleParameters) generatedParams).getHeight(), 0.0);
    }

    @Test
    void convertSpecificProfileParameters() {
        RequestProfileParamsRestrictions restrictions = new RequestProfileParamsRestrictions();
        restrictions.setHeight(10.0f);
        ProfileParameters params = apiService.convertSpecificProfileParameters(2, restrictions, APIEnums.VehicleType.HGV);
        assertTrue(params instanceof VehicleParameters);
        assertEquals(10.0f, ((VehicleParameters) params).getHeight(), 0.0);
    }
}
