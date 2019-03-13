package heigit.ors.api.requests.routing;

import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.exceptions.ParameterValueException;
import org.junit.Assert;
import org.junit.Test;

public class APIEnumsTest {
    @Test(expected = ParameterValueException.class)
    public void testBordersEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIEnums.AvoidBorders.CONTROLLED, APIEnums.AvoidBorders.forValue("controlled"));
        Assert.assertEquals(APIEnums.AvoidBorders.ALL, APIEnums.AvoidBorders.forValue("all"));
        Assert.assertEquals(APIEnums.AvoidBorders.NONE, APIEnums.AvoidBorders.forValue("none"));

        APIEnums.AvoidBorders.forValue("invalid");

    }

    @Test
    public void testBordersEnumValue() {
        Assert.assertEquals("controlled", APIEnums.AvoidBorders.CONTROLLED.toString());
        Assert.assertEquals("all", APIEnums.AvoidBorders.ALL.toString());
        Assert.assertEquals("none", APIEnums.AvoidBorders.NONE.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testExtraInfoEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIEnums.ExtraInfo.STEEPNESS, APIEnums.ExtraInfo.forValue("steepness"));
        Assert.assertEquals(APIEnums.ExtraInfo.SUITABILITY, APIEnums.ExtraInfo.forValue("suitability"));
        Assert.assertEquals(APIEnums.ExtraInfo.SURFACE, APIEnums.ExtraInfo.forValue("surface"));
        Assert.assertEquals(APIEnums.ExtraInfo.WAY_CATEGORY, APIEnums.ExtraInfo.forValue("waycategory"));
        Assert.assertEquals(APIEnums.ExtraInfo.WAY_TYPE, APIEnums.ExtraInfo.forValue("waytype"));
        Assert.assertEquals(APIEnums.ExtraInfo.TOLLWAYS, APIEnums.ExtraInfo.forValue("tollways"));
        Assert.assertEquals(APIEnums.ExtraInfo.TRAIL_DIFFICULTY, APIEnums.ExtraInfo.forValue("traildifficulty"));
        Assert.assertEquals(APIEnums.ExtraInfo.OSM_ID, APIEnums.ExtraInfo.forValue("osmid"));

        APIEnums.ExtraInfo.forValue("invalid");
    }

    @Test
    public void testExtraInfoEnumValue() {
        Assert.assertEquals("steepness", APIEnums.ExtraInfo.STEEPNESS.toString());
        Assert.assertEquals("suitability", APIEnums.ExtraInfo.SUITABILITY.toString());
        Assert.assertEquals("surface", APIEnums.ExtraInfo.SURFACE.toString());
        Assert.assertEquals("waycategory", APIEnums.ExtraInfo.WAY_CATEGORY.toString());
        Assert.assertEquals("waytype", APIEnums.ExtraInfo.WAY_TYPE.toString());
        Assert.assertEquals("tollways", APIEnums.ExtraInfo.TOLLWAYS.toString());
        Assert.assertEquals("traildifficulty", APIEnums.ExtraInfo.TRAIL_DIFFICULTY.toString());
        Assert.assertEquals("osmid", APIEnums.ExtraInfo.OSM_ID.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testRouteResponseTypeEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIEnums.RouteResponseType.JSON, APIEnums.RouteResponseType.forValue("json"));
        Assert.assertEquals(APIEnums.RouteResponseType.GEOJSON, APIEnums.RouteResponseType.forValue("geojson"));
        Assert.assertEquals(APIEnums.RouteResponseType.GPX, APIEnums.RouteResponseType.forValue("gpx"));

        APIEnums.RouteResponseType.forValue("invalid");
    }

    @Test(expected = ParameterValueException.class)
    public void testMatrixResponseTypeEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIEnums.MatrixResponseType.JSON, APIEnums.MatrixResponseType.forValue("json"));

        APIEnums.MatrixResponseType.forValue("invalid");
    }


    @Test
    public void testResponseTypeEnumValue() {
        Assert.assertEquals("geojson", APIEnums.RouteResponseType.GEOJSON.toString());
        Assert.assertEquals("gpx", APIEnums.RouteResponseType.GPX.toString());
        Assert.assertEquals("json", APIEnums.RouteResponseType.JSON.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testVehicleTypeEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIEnums.VehicleType.AGRICULTURAL, APIEnums.VehicleType.forValue("agricultural"));
        Assert.assertEquals(APIEnums.VehicleType.BUS, APIEnums.VehicleType.forValue("bus"));
        Assert.assertEquals(APIEnums.VehicleType.DELIVERY, APIEnums.VehicleType.forValue("delivery"));
        Assert.assertEquals(APIEnums.VehicleType.FORESTRY, APIEnums.VehicleType.forValue("forestry"));
        Assert.assertEquals(APIEnums.VehicleType.GOODS, APIEnums.VehicleType.forValue("goods"));
        Assert.assertEquals(APIEnums.VehicleType.HGV, APIEnums.VehicleType.forValue("hgv"));
        Assert.assertEquals(APIEnums.VehicleType.UNKNOWN, APIEnums.VehicleType.forValue("unkown"));

        APIEnums.VehicleType.forValue("invalid");
    }

    @Test
    public void testVehicleTypeEnumValue() {
        Assert.assertEquals("agricultural", APIEnums.VehicleType.AGRICULTURAL.toString());
        Assert.assertEquals("bus", APIEnums.VehicleType.BUS.toString());
        Assert.assertEquals("delivery", APIEnums.VehicleType.DELIVERY.toString());
        Assert.assertEquals("forestry", APIEnums.VehicleType.FORESTRY.toString());
        Assert.assertEquals("goods", APIEnums.VehicleType.GOODS.toString());
        Assert.assertEquals("hgv", APIEnums.VehicleType.HGV.toString());
        Assert.assertEquals("unknown", APIEnums.VehicleType.UNKNOWN.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testAvoidFeaturesEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIEnums.AvoidFeatures.FORDS, APIEnums.AvoidFeatures.forValue("fords"));
        Assert.assertEquals(APIEnums.AvoidFeatures.FERRIES, APIEnums.AvoidFeatures.forValue("ferries"));
        Assert.assertEquals(APIEnums.AvoidFeatures.HIGHWAYS, APIEnums.AvoidFeatures.forValue("highways"));
        Assert.assertEquals(APIEnums.AvoidFeatures.STEPS, APIEnums.AvoidFeatures.forValue("steps"));
        Assert.assertEquals(APIEnums.AvoidFeatures.TOLLWAYS, APIEnums.AvoidFeatures.forValue("tollways"));

        APIEnums.AvoidFeatures.forValue("invalid");
    }

    @Test
    public void testAvoidFeaturesEnumValue() {
        Assert.assertEquals("fords", APIEnums.AvoidFeatures.FORDS.toString());
        Assert.assertEquals("ferries", APIEnums.AvoidFeatures.FERRIES.toString());
        Assert.assertEquals("highways", APIEnums.AvoidFeatures.HIGHWAYS.toString());
        Assert.assertEquals("steps", APIEnums.AvoidFeatures.STEPS.toString());
        Assert.assertEquals("tollways", APIEnums.AvoidFeatures.TOLLWAYS.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testPreferenceEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIEnums.RoutePreference.FASTEST, APIEnums.RoutePreference.forValue("fastest"));
        Assert.assertEquals(APIEnums.RoutePreference.SHORTEST, APIEnums.RoutePreference.forValue("shortest"));
        Assert.assertEquals(APIEnums.RoutePreference.RECOMMENDED, APIEnums.RoutePreference.forValue("recommended"));

        APIEnums.RoutePreference.forValue("invalid");
    }

    @Test
    public void testPreferenceEnumValue() {
        Assert.assertEquals("fastest", APIEnums.RoutePreference.FASTEST.toString());
        Assert.assertEquals("shortest", APIEnums.RoutePreference.SHORTEST.toString());
        Assert.assertEquals("recommended", APIEnums.RoutePreference.RECOMMENDED.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testProfileEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIEnums.Profile.DRIVING_CAR, APIEnums.Profile.forValue("driving-car"));
        Assert.assertEquals(APIEnums.Profile.DRIVING_HGV, APIEnums.Profile.forValue("driving-hgv"));
        Assert.assertEquals(APIEnums.Profile.CYCLING_REGULAR, APIEnums.Profile.forValue("cycling-regular"));
        Assert.assertEquals(APIEnums.Profile.CYCLING_ROAD, APIEnums.Profile.forValue("cycling-road"));
        Assert.assertEquals(APIEnums.Profile.CYCLING_MOUNTAIN, APIEnums.Profile.forValue("cycling-mountain"));
        Assert.assertEquals(APIEnums.Profile.CYCLING_ELECTRIC, APIEnums.Profile.forValue("cycling-electric"));
        Assert.assertEquals(APIEnums.Profile.FOOT_WALKING, APIEnums.Profile.forValue("foot-walking"));
        Assert.assertEquals(APIEnums.Profile.FOOT_HIKING, APIEnums.Profile.forValue("foot-hiking"));
        Assert.assertEquals(APIEnums.Profile.WHEELCHAIR, APIEnums.Profile.forValue("wheelchair"));

        APIEnums.Profile.forValue("invalid");
    }

    @Test
    public void testProfileEnumValue() {
        Assert.assertEquals("driving-car", APIEnums.Profile.DRIVING_CAR.toString());
        Assert.assertEquals("driving-hgv", APIEnums.Profile.DRIVING_HGV.toString());
        Assert.assertEquals("cycling-regular", APIEnums.Profile.CYCLING_REGULAR.toString());
        Assert.assertEquals("cycling-road", APIEnums.Profile.CYCLING_ROAD.toString());
        Assert.assertEquals("cycling-mountain", APIEnums.Profile.CYCLING_MOUNTAIN.toString());
        Assert.assertEquals("cycling-electric", APIEnums.Profile.CYCLING_ELECTRIC.toString());
        Assert.assertEquals("foot-walking", APIEnums.Profile.FOOT_WALKING.toString());
        Assert.assertEquals("foot-hiking", APIEnums.Profile.FOOT_HIKING.toString());
        Assert.assertEquals("wheelchair", APIEnums.Profile.WHEELCHAIR.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testUnitsEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIEnums.Units.METRES, APIEnums.Units.forValue("m"));
        Assert.assertEquals(APIEnums.Units.KILOMETRES, APIEnums.Units.forValue("km"));
        Assert.assertEquals(APIEnums.Units.MILES, APIEnums.Units.forValue("mi"));

        APIEnums.Units.forValue("invalid");
    }

    @Test
    public void testUnitsEnumValue() {
        Assert.assertEquals("m", APIEnums.Units.METRES.toString());
        Assert.assertEquals("km", APIEnums.Units.KILOMETRES.toString());
        Assert.assertEquals("mi", APIEnums.Units.MILES.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testLanguagesEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIEnums.Languages.EN, APIEnums.Languages.forValue("en"));
        Assert.assertEquals(APIEnums.Languages.ZH_CN, APIEnums.Languages.forValue("zh-cn"));
        Assert.assertEquals(APIEnums.Languages.DE, APIEnums.Languages.forValue("de"));
        Assert.assertEquals(APIEnums.Languages.ES, APIEnums.Languages.forValue("es"));
        Assert.assertEquals(APIEnums.Languages.RU, APIEnums.Languages.forValue("re"));
        Assert.assertEquals(APIEnums.Languages.FR, APIEnums.Languages.forValue("fr"));
        Assert.assertEquals(APIEnums.Languages.IT, APIEnums.Languages.forValue("it"));
        Assert.assertEquals(APIEnums.Languages.NL, APIEnums.Languages.forValue("nl"));
        Assert.assertEquals(APIEnums.Languages.GR, APIEnums.Languages.forValue("gr"));

        APIEnums.Languages.forValue("invalid");
    }

    @Test
    public void testLanguagesEnumValue() {
        Assert.assertEquals("en", APIEnums.Languages.EN.toString());
        Assert.assertEquals("zh-cn", APIEnums.Languages.ZH_CN.toString());
        Assert.assertEquals("de", APIEnums.Languages.DE.toString());
        Assert.assertEquals("es", APIEnums.Languages.ES.toString());
        Assert.assertEquals("ru", APIEnums.Languages.RU.toString());
        Assert.assertEquals("fr", APIEnums.Languages.FR.toString());
        Assert.assertEquals("it", APIEnums.Languages.IT.toString());
        Assert.assertEquals("nl", APIEnums.Languages.NL.toString());
        Assert.assertEquals("gr", APIEnums.Languages.GR.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testInstructionSFormatEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIEnums.InstructionsFormat.HTML, APIEnums.InstructionsFormat.forValue("html"));
        Assert.assertEquals(APIEnums.InstructionsFormat.TEXT, APIEnums.InstructionsFormat.forValue("text"));

        APIEnums.InstructionsFormat.forValue("invalid");
    }

    @Test
    public void testInstructionSFormatEnumvALUE() {
        Assert.assertEquals("html", APIEnums.InstructionsFormat.HTML.toString());
        Assert.assertEquals("text", APIEnums.InstructionsFormat.TEXT.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testAttributesEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIEnums.Attributes.AVERAGE_SPEED, APIEnums.Attributes.forValue("avgspeed"));
        Assert.assertEquals(APIEnums.Attributes.DETOUR_FACTOR, APIEnums.Attributes.forValue("detourfactor"));
        Assert.assertEquals(APIEnums.Attributes.ROUTE_PERCENTAGE, APIEnums.Attributes.forValue("percentage"));

        APIEnums.Attributes.forValue("invalid");
    }

    @Test
    public void testAttributesEnumValue() {
        Assert.assertEquals("avgspeed", APIEnums.Attributes.AVERAGE_SPEED.toString());
        Assert.assertEquals("detourfactor", APIEnums.Attributes.DETOUR_FACTOR.toString());
        Assert.assertEquals("percentage", APIEnums.Attributes.ROUTE_PERCENTAGE.toString());
    }
}
