package heigit.ors.api.requests.common;

import com.vividsolutions.jts.geom.Polygon;
import heigit.ors.api.requests.routing.RequestProfileParams;
import heigit.ors.api.requests.routing.RequestProfileParamsRestrictions;
import heigit.ors.api.requests.routing.RouteRequestOptions;
import heigit.ors.exceptions.IncompatibleParameterException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.routing.parameters.ProfileParameters;
import heigit.ors.routing.parameters.VehicleParameters;
import heigit.ors.routing.pathprocessors.BordersExtractor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GenericHandlerTest {
    GenericHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = new GenericHandler();
    }

    @Test
    public void convertAPIEnumListToStrings() {
        String[] strVals = handler.convertAPIEnumListToStrings(new APIEnums.ExtraInfo[] {APIEnums.ExtraInfo.STEEPNESS, APIEnums.ExtraInfo.SURFACE});
        Assert.assertEquals(2, strVals.length);
        Assert.assertEquals("steepness", strVals[0]);
        Assert.assertEquals("surface", strVals[1]);
    }

    @Test
    public void convertAPIEnum() {
        String strVal = handler.convertAPIEnum(APIEnums.AvoidBorders.CONTROLLED);
        Assert.assertEquals("controlled", strVal);
    }

    @Test
    public void convertVehicleType() throws IncompatibleParameterException {
        int type = handler.convertVehicleType(APIEnums.VehicleType.HGV, 2);
        Assert.assertEquals(2, type);
    }

    @Test(expected = IncompatibleParameterException.class)
    public void convertVehicleTypeError() throws IncompatibleParameterException {
        handler.convertVehicleType(APIEnums.VehicleType.HGV, 1);
    }

    @Test
    public void convertAvoidBorders() {
        BordersExtractor.Avoid avoid = handler.convertAvoidBorders(APIEnums.AvoidBorders.CONTROLLED);
        Assert.assertEquals(BordersExtractor.Avoid.CONTROLLED, avoid);
        avoid = handler.convertAvoidBorders(APIEnums.AvoidBorders.ALL);
        Assert.assertEquals(BordersExtractor.Avoid.ALL, avoid);
        avoid = handler.convertAvoidBorders(APIEnums.AvoidBorders.NONE);
        Assert.assertEquals(BordersExtractor.Avoid.NONE, avoid);
    }

    @Test
    public void convertRouteProfileType() {
        int type = handler.convertRouteProfileType(APIEnums.Profile.DRIVING_CAR);
        Assert.assertEquals(1, type);
        type = handler.convertRouteProfileType(APIEnums.Profile.FOOT_WALKING);
        Assert.assertEquals(20, type);
    }

    @Test
    public void convertAvoidAreas() throws ParameterValueException {
        JSONObject geomJSON = new JSONObject();
        geomJSON.put("type", "Polygon");

        JSONArray poly = generateGeoJSONPolyCoords();

        JSONArray coords = new JSONArray();
        coords.add(0, poly);
        geomJSON.put("coordinates", coords);

        Polygon[] avoidAreas = handler.convertAvoidAreas(geomJSON);
        Assert.assertEquals(1, avoidAreas.length);
        Assert.assertEquals(4, avoidAreas[0].getCoordinates().length);
        Assert.assertEquals(1, avoidAreas[0].getCoordinates()[0].x, 0.0);

        JSONObject geomJSONMulti = new JSONObject();
        geomJSONMulti.put("type", "MultiPolygon");

        JSONArray polys1 = new JSONArray();
        polys1.add(0, poly);
        JSONArray polys2 = new JSONArray();
        polys2.add(0, poly);

        coords = new JSONArray();
        coords.add(0,polys1);
        coords.add(0,polys2);

        geomJSONMulti.put("coordinates", coords);

        avoidAreas = handler.convertAvoidAreas(geomJSONMulti);

        Assert.assertEquals(2, avoidAreas.length);
    }

    @Test(expected = ParameterValueException.class)
    public void convertAvoidAreasInvalidType() throws ParameterValueException {
        JSONObject geomJSON = new JSONObject();
        geomJSON.put("type", "LineString");

        JSONArray poly = generateGeoJSONPolyCoords();

        geomJSON.put("coordinates", poly);
        handler.convertAvoidAreas(geomJSON);
    }

    @Test(expected = ParameterValueException.class)
    public void convertAvoidAreasInvalidFeature() throws ParameterValueException {
        JSONObject geomJSON = new JSONObject();
        geomJSON.put("type", "Polygon");

        JSONArray poly = generateGeoJSONPolyCoords();

        geomJSON.put("coooooooooooordinates", poly);
        handler.convertAvoidAreas(geomJSON);
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
    public void convertFeatureTypes() throws UnknownParameterValueException, IncompatibleParameterException {
        APIEnums.AvoidFeatures[] avoids = new APIEnums.AvoidFeatures[] { APIEnums.AvoidFeatures.FERRIES, APIEnums.AvoidFeatures.FORDS };
        int converted = handler.convertFeatureTypes(avoids, 1);
        Assert.assertEquals(24, converted);
    }

    @Test(expected = IncompatibleParameterException.class)
    public void convertFeatureTypesIncompatible() throws UnknownParameterValueException, IncompatibleParameterException {
        APIEnums.AvoidFeatures[] avoids = new APIEnums.AvoidFeatures[] { APIEnums.AvoidFeatures.STEPS};
        handler.convertFeatureTypes(avoids, 1);
    }

    @Test
    public void convertParameters() throws StatusCodeException {
        RouteRequestOptions opts = new RouteRequestOptions();
        RequestProfileParams params = new RequestProfileParams();
        RequestProfileParamsRestrictions restrictions = new RequestProfileParamsRestrictions();
        restrictions.setHeight(10.0f);
        params.setRestrictions(restrictions);
        opts.setVehicleType(APIEnums.VehicleType.HGV);
        opts.setProfileParams(params);

        ProfileParameters generatedParams = handler.convertParameters(opts, 2);

        Assert.assertEquals(10.0f, ((VehicleParameters)generatedParams).getHeight(), 0.0);
    }

    @Test
    public void convertSpecificProfileParameters() {
        RequestProfileParamsRestrictions restrictions = new RequestProfileParamsRestrictions();
        restrictions.setHeight(10.0f);
        ProfileParameters params = handler.convertSpecificProfileParameters(2, restrictions, APIEnums.VehicleType.HGV);
        Assert.assertTrue(params instanceof VehicleParameters);
        Assert.assertEquals(10.0f, ((VehicleParameters)params).getHeight(), 0.0);
    }
}