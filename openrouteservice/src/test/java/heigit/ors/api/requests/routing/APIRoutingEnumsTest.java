package heigit.ors.api.requests.routing;

import heigit.ors.exceptions.ParameterValueException;
import org.junit.Assert;
import org.junit.Test;

public class APIRoutingEnumsTest {
    @Test(expected = ParameterValueException.class)
    public void testBordersEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIRoutingEnums.AvoidBorders.CONTROLLED, APIRoutingEnums.AvoidBorders.forValue("controlled"));
        Assert.assertEquals(APIRoutingEnums.AvoidBorders.ALL, APIRoutingEnums.AvoidBorders.forValue("all"));
        Assert.assertEquals(APIRoutingEnums.AvoidBorders.NONE, APIRoutingEnums.AvoidBorders.forValue("none"));

        APIRoutingEnums.AvoidBorders.forValue("invalid");

    }

    @Test
    public void testBordersEnumValue() {
        Assert.assertEquals("controlled", APIRoutingEnums.AvoidBorders.CONTROLLED.toString());
        Assert.assertEquals("all", APIRoutingEnums.AvoidBorders.ALL.toString());
        Assert.assertEquals("none", APIRoutingEnums.AvoidBorders.NONE.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testExtraInfoEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIRoutingEnums.ExtraInfo.STEEPNESS, APIRoutingEnums.ExtraInfo.forValue("steepness"));
        Assert.assertEquals(APIRoutingEnums.ExtraInfo.SUITABILITY, APIRoutingEnums.ExtraInfo.forValue("suitability"));
        Assert.assertEquals(APIRoutingEnums.ExtraInfo.SURFACE, APIRoutingEnums.ExtraInfo.forValue("surface"));
        Assert.assertEquals(APIRoutingEnums.ExtraInfo.WAY_CATEGORY, APIRoutingEnums.ExtraInfo.forValue("waycategory"));
        Assert.assertEquals(APIRoutingEnums.ExtraInfo.WAY_TYPE, APIRoutingEnums.ExtraInfo.forValue("waytype"));
        Assert.assertEquals(APIRoutingEnums.ExtraInfo.TOLLWAYS, APIRoutingEnums.ExtraInfo.forValue("tollways"));
        Assert.assertEquals(APIRoutingEnums.ExtraInfo.TRAIL_DIFFICULTY, APIRoutingEnums.ExtraInfo.forValue("traildifficulty"));
        Assert.assertEquals(APIRoutingEnums.ExtraInfo.OSM_ID, APIRoutingEnums.ExtraInfo.forValue("osmid"));

        APIRoutingEnums.ExtraInfo.forValue("invalid");
    }

    @Test
    public void testExtraInfoEnumValue() {
        Assert.assertEquals("steepness", APIRoutingEnums.ExtraInfo.STEEPNESS.toString());
        Assert.assertEquals("suitability", APIRoutingEnums.ExtraInfo.SUITABILITY.toString());
        Assert.assertEquals("surface", APIRoutingEnums.ExtraInfo.SURFACE.toString());
        Assert.assertEquals("waycategory", APIRoutingEnums.ExtraInfo.WAY_CATEGORY.toString());
        Assert.assertEquals("waytype", APIRoutingEnums.ExtraInfo.WAY_TYPE.toString());
        Assert.assertEquals("tollways", APIRoutingEnums.ExtraInfo.TOLLWAYS.toString());
        Assert.assertEquals("traildifficulty", APIRoutingEnums.ExtraInfo.TRAIL_DIFFICULTY.toString());
        Assert.assertEquals("osmid", APIRoutingEnums.ExtraInfo.OSM_ID.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testGeomTypeEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIRoutingEnums.RouteResponseGeometryType.ENCODED_POLYLINE, APIRoutingEnums.RouteResponseGeometryType.forValue("encodedpolyline"));
        Assert.assertEquals(APIRoutingEnums.RouteResponseGeometryType.GEOJSON, APIRoutingEnums.RouteResponseGeometryType.forValue("geojson"));
        Assert.assertEquals(APIRoutingEnums.RouteResponseGeometryType.GPX, APIRoutingEnums.RouteResponseGeometryType.forValue("gpx"));

        APIRoutingEnums.RouteResponseGeometryType.forValue("invalid");
    }

    @Test
    public void testGeomTypeEnumValue() {
        Assert.assertEquals("geojson", APIRoutingEnums.RouteResponseGeometryType.GEOJSON.toString());
        Assert.assertEquals("gpx", APIRoutingEnums.RouteResponseGeometryType.GPX.toString());
        Assert.assertEquals("encodedpolyline", APIRoutingEnums.RouteResponseGeometryType.ENCODED_POLYLINE.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testResponseTypeEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIRoutingEnums.RouteResponseType.JSON, APIRoutingEnums.RouteResponseType.forValue("json"));
        Assert.assertEquals(APIRoutingEnums.RouteResponseType.GEOJSON, APIRoutingEnums.RouteResponseType.forValue("geojson"));
        Assert.assertEquals(APIRoutingEnums.RouteResponseType.GPX, APIRoutingEnums.RouteResponseType.forValue("gpx"));

        APIRoutingEnums.RouteResponseType.forValue("invalid");
    }

    @Test
    public void testResponseTypeEnumValue() {
        Assert.assertEquals("geojson", APIRoutingEnums.RouteResponseType.GEOJSON.toString());
        Assert.assertEquals("gpx", APIRoutingEnums.RouteResponseType.GPX.toString());
        Assert.assertEquals("json", APIRoutingEnums.RouteResponseType.JSON.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testVehicleTypeEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIRoutingEnums.VehicleType.AGRICULTURAL, APIRoutingEnums.VehicleType.forValue("agricultural"));
        Assert.assertEquals(APIRoutingEnums.VehicleType.BUS, APIRoutingEnums.VehicleType.forValue("bus"));
        Assert.assertEquals(APIRoutingEnums.VehicleType.DELIVERY, APIRoutingEnums.VehicleType.forValue("delivery"));
        Assert.assertEquals(APIRoutingEnums.VehicleType.FORESTRY, APIRoutingEnums.VehicleType.forValue("forestry"));
        Assert.assertEquals(APIRoutingEnums.VehicleType.GOODS, APIRoutingEnums.VehicleType.forValue("goods"));
        Assert.assertEquals(APIRoutingEnums.VehicleType.HGV, APIRoutingEnums.VehicleType.forValue("hgv"));
        Assert.assertEquals(APIRoutingEnums.VehicleType.UNKNOWN, APIRoutingEnums.VehicleType.forValue("unkown"));

        APIRoutingEnums.VehicleType.forValue("invalid");
    }

    @Test
    public void testVehicleTypeEnumValue() {
        Assert.assertEquals("agricultural", APIRoutingEnums.VehicleType.AGRICULTURAL.toString());
        Assert.assertEquals("bus", APIRoutingEnums.VehicleType.BUS.toString());
        Assert.assertEquals("delivery", APIRoutingEnums.VehicleType.DELIVERY.toString());
        Assert.assertEquals("forestry", APIRoutingEnums.VehicleType.FORESTRY.toString());
        Assert.assertEquals("goods", APIRoutingEnums.VehicleType.GOODS.toString());
        Assert.assertEquals("hgv", APIRoutingEnums.VehicleType.HGV.toString());
        Assert.assertEquals("unknown", APIRoutingEnums.VehicleType.UNKNOWN.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testAvoidFeaturesEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIRoutingEnums.AvoidFeatures.FORDS, APIRoutingEnums.AvoidFeatures.forValue("fords"));
        Assert.assertEquals(APIRoutingEnums.AvoidFeatures.FERRIES, APIRoutingEnums.AvoidFeatures.forValue("ferries"));
        Assert.assertEquals(APIRoutingEnums.AvoidFeatures.HIGHWAYS, APIRoutingEnums.AvoidFeatures.forValue("highways"));
        Assert.assertEquals(APIRoutingEnums.AvoidFeatures.HILLS, APIRoutingEnums.AvoidFeatures.forValue("hills"));
        Assert.assertEquals(APIRoutingEnums.AvoidFeatures.PAVED_ROADS, APIRoutingEnums.AvoidFeatures.forValue("pavedroads"));
        Assert.assertEquals(APIRoutingEnums.AvoidFeatures.STEPS, APIRoutingEnums.AvoidFeatures.forValue("steps"));
        Assert.assertEquals(APIRoutingEnums.AvoidFeatures.TOLLWAYS, APIRoutingEnums.AvoidFeatures.forValue("tollways"));
        Assert.assertEquals(APIRoutingEnums.AvoidFeatures.TRACKS, APIRoutingEnums.AvoidFeatures.forValue("tracks"));
        Assert.assertEquals(APIRoutingEnums.AvoidFeatures.TUNNELS, APIRoutingEnums.AvoidFeatures.forValue("tunnels"));
        Assert.assertEquals(APIRoutingEnums.AvoidFeatures.UNPAVED_ROADS, APIRoutingEnums.AvoidFeatures.forValue("unpavedroads"));

        APIRoutingEnums.AvoidFeatures.forValue("invalid");
    }

    @Test
    public void testAvoidFeaturesEnumValue() {
        Assert.assertEquals("fords", APIRoutingEnums.AvoidFeatures.FORDS.toString());
        Assert.assertEquals("ferries", APIRoutingEnums.AvoidFeatures.FERRIES.toString());
        Assert.assertEquals("highways", APIRoutingEnums.AvoidFeatures.HIGHWAYS.toString());
        Assert.assertEquals("hills", APIRoutingEnums.AvoidFeatures.HILLS.toString());
        Assert.assertEquals("pavedroads", APIRoutingEnums.AvoidFeatures.PAVED_ROADS.toString());
        Assert.assertEquals("steps", APIRoutingEnums.AvoidFeatures.STEPS.toString());
        Assert.assertEquals("tollways", APIRoutingEnums.AvoidFeatures.TOLLWAYS.toString());
        Assert.assertEquals("tracks", APIRoutingEnums.AvoidFeatures.TRACKS.toString());
        Assert.assertEquals("tunnels", APIRoutingEnums.AvoidFeatures.TUNNELS.toString());
        Assert.assertEquals("unpavedroads", APIRoutingEnums.AvoidFeatures.UNPAVED_ROADS.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testPreferenceEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIRoutingEnums.RoutePreference.FASTEST, APIRoutingEnums.RoutePreference.forValue("fastest"));
        Assert.assertEquals(APIRoutingEnums.RoutePreference.SHORTEST, APIRoutingEnums.RoutePreference.forValue("shortest"));
        Assert.assertEquals(APIRoutingEnums.RoutePreference.RECOMMENDED, APIRoutingEnums.RoutePreference.forValue("recommended"));

        APIRoutingEnums.RoutePreference.forValue("invalid");
    }

    @Test
    public void testPreferenceEnumValue() {
        Assert.assertEquals("fastest", APIRoutingEnums.RoutePreference.FASTEST.toString());
        Assert.assertEquals("shortest", APIRoutingEnums.RoutePreference.SHORTEST.toString());
        Assert.assertEquals("recommended", APIRoutingEnums.RoutePreference.RECOMMENDED.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testProfileEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIRoutingEnums.RoutingProfile.DRIVING_CAR, APIRoutingEnums.RoutingProfile.forValue("driving-car"));
        Assert.assertEquals(APIRoutingEnums.RoutingProfile.DRIVING_HGV, APIRoutingEnums.RoutingProfile.forValue("driving-hgv"));
        Assert.assertEquals(APIRoutingEnums.RoutingProfile.CYCLING_REGULAR, APIRoutingEnums.RoutingProfile.forValue("cycling-regular"));
        Assert.assertEquals(APIRoutingEnums.RoutingProfile.CYCLING_ROAD, APIRoutingEnums.RoutingProfile.forValue("cycling-road"));
        Assert.assertEquals(APIRoutingEnums.RoutingProfile.CYCLING_SAFE, APIRoutingEnums.RoutingProfile.forValue("cycling-safe"));
        Assert.assertEquals(APIRoutingEnums.RoutingProfile.CYCLING_MOUNTAIN, APIRoutingEnums.RoutingProfile.forValue("cycling-mountain"));
        Assert.assertEquals(APIRoutingEnums.RoutingProfile.CYCLING_TOUR, APIRoutingEnums.RoutingProfile.forValue("cycling-tour"));
        Assert.assertEquals(APIRoutingEnums.RoutingProfile.CYCLING_ELECTRIC, APIRoutingEnums.RoutingProfile.forValue("cycling-electric"));
        Assert.assertEquals(APIRoutingEnums.RoutingProfile.FOOT_WALKING, APIRoutingEnums.RoutingProfile.forValue("foot-walking"));
        Assert.assertEquals(APIRoutingEnums.RoutingProfile.FOOT_HIKING, APIRoutingEnums.RoutingProfile.forValue("foot-hiking"));
        Assert.assertEquals(APIRoutingEnums.RoutingProfile.WHEELCHAIR, APIRoutingEnums.RoutingProfile.forValue("wheelchair"));

        APIRoutingEnums.RoutingProfile.forValue("invalid");
    }

    @Test
    public void testProfileEnumValue() {
        Assert.assertEquals("driving-car", APIRoutingEnums.RoutingProfile.DRIVING_CAR.toString());
        Assert.assertEquals("driving-hgv", APIRoutingEnums.RoutingProfile.DRIVING_HGV.toString());
        Assert.assertEquals("cycling-regular", APIRoutingEnums.RoutingProfile.CYCLING_REGULAR.toString());
        Assert.assertEquals("cycling-road", APIRoutingEnums.RoutingProfile.CYCLING_ROAD.toString());
        Assert.assertEquals("cycling-safe", APIRoutingEnums.RoutingProfile.CYCLING_SAFE.toString());
        Assert.assertEquals("cycling-mountain", APIRoutingEnums.RoutingProfile.CYCLING_MOUNTAIN.toString());
        Assert.assertEquals("cycling-tour", APIRoutingEnums.RoutingProfile.CYCLING_TOUR.toString());
        Assert.assertEquals("cycling-electric", APIRoutingEnums.RoutingProfile.CYCLING_ELECTRIC.toString());
        Assert.assertEquals("foot-walking", APIRoutingEnums.RoutingProfile.FOOT_WALKING.toString());
        Assert.assertEquals("foot-hiking", APIRoutingEnums.RoutingProfile.FOOT_HIKING.toString());
        Assert.assertEquals("wheelchair", APIRoutingEnums.RoutingProfile.WHEELCHAIR.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testUnitsEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIRoutingEnums.Units.METRES, APIRoutingEnums.Units.forValue("m"));
        Assert.assertEquals(APIRoutingEnums.Units.KILOMETRES, APIRoutingEnums.Units.forValue("km"));
        Assert.assertEquals(APIRoutingEnums.Units.MILES, APIRoutingEnums.Units.forValue("mi"));

        APIRoutingEnums.Units.forValue("invalid");
    }

    @Test
    public void testUnitsEnumValue() {
        Assert.assertEquals("m", APIRoutingEnums.Units.METRES.toString());
        Assert.assertEquals("km", APIRoutingEnums.Units.KILOMETRES.toString());
        Assert.assertEquals("mi", APIRoutingEnums.Units.MILES.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testLanguagesEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIRoutingEnums.Languages.EN, APIRoutingEnums.Languages.forValue("en"));
        Assert.assertEquals(APIRoutingEnums.Languages.CN, APIRoutingEnums.Languages.forValue("cn"));
        Assert.assertEquals(APIRoutingEnums.Languages.DE, APIRoutingEnums.Languages.forValue("de"));
        Assert.assertEquals(APIRoutingEnums.Languages.ES, APIRoutingEnums.Languages.forValue("es"));
        Assert.assertEquals(APIRoutingEnums.Languages.RU, APIRoutingEnums.Languages.forValue("re"));
        Assert.assertEquals(APIRoutingEnums.Languages.DK, APIRoutingEnums.Languages.forValue("dk"));
        Assert.assertEquals(APIRoutingEnums.Languages.FR, APIRoutingEnums.Languages.forValue("fr"));
        Assert.assertEquals(APIRoutingEnums.Languages.IT, APIRoutingEnums.Languages.forValue("it"));
        Assert.assertEquals(APIRoutingEnums.Languages.NL, APIRoutingEnums.Languages.forValue("nl"));
        Assert.assertEquals(APIRoutingEnums.Languages.BR, APIRoutingEnums.Languages.forValue("br"));
        Assert.assertEquals(APIRoutingEnums.Languages.SE, APIRoutingEnums.Languages.forValue("se"));
        Assert.assertEquals(APIRoutingEnums.Languages.TR, APIRoutingEnums.Languages.forValue("tr"));
        Assert.assertEquals(APIRoutingEnums.Languages.GR, APIRoutingEnums.Languages.forValue("gr"));

        APIRoutingEnums.Languages.forValue("invalid");
    }

    @Test
    public void testLanguagesEnumValue() {
        Assert.assertEquals("en", APIRoutingEnums.Languages.EN.toString());
        Assert.assertEquals("cn", APIRoutingEnums.Languages.CN.toString());
        Assert.assertEquals("de", APIRoutingEnums.Languages.DE.toString());
        Assert.assertEquals("es", APIRoutingEnums.Languages.ES.toString());
        Assert.assertEquals("ru", APIRoutingEnums.Languages.RU.toString());
        Assert.assertEquals("dk", APIRoutingEnums.Languages.DK.toString());
        Assert.assertEquals("fr", APIRoutingEnums.Languages.FR.toString());
        Assert.assertEquals("it", APIRoutingEnums.Languages.IT.toString());
        Assert.assertEquals("nl", APIRoutingEnums.Languages.NL.toString());
        Assert.assertEquals("br", APIRoutingEnums.Languages.BR.toString());
        Assert.assertEquals("se", APIRoutingEnums.Languages.SE.toString());
        Assert.assertEquals("tr", APIRoutingEnums.Languages.TR.toString());
        Assert.assertEquals("gr", APIRoutingEnums.Languages.GR.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testInstructionSFormatEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIRoutingEnums.InstructionsFormat.HTML, APIRoutingEnums.InstructionsFormat.forValue("html"));
        Assert.assertEquals(APIRoutingEnums.InstructionsFormat.TEXT, APIRoutingEnums.InstructionsFormat.forValue("text"));

        APIRoutingEnums.InstructionsFormat.forValue("invalid");
    }

    @Test
    public void testInstructionSFormatEnumvALUE() {
        Assert.assertEquals("html", APIRoutingEnums.InstructionsFormat.HTML.toString());
        Assert.assertEquals("text", APIRoutingEnums.InstructionsFormat.TEXT.toString());
    }

    @Test(expected = ParameterValueException.class)
    public void testAttributesEnumCreation() throws ParameterValueException {
        Assert.assertEquals(APIRoutingEnums.Attributes.AVERAGE_SPEED, APIRoutingEnums.Attributes.forValue("avgspeed"));
        Assert.assertEquals(APIRoutingEnums.Attributes.DETOUR_FACTOR, APIRoutingEnums.Attributes.forValue("detourfactor"));
        Assert.assertEquals(APIRoutingEnums.Attributes.ROUTE_PERCENTAGE, APIRoutingEnums.Attributes.forValue("percentage"));

        APIRoutingEnums.Attributes.forValue("invalid");
    }

    @Test
    public void testAttributesEnumValue() {
        Assert.assertEquals("avgspeed", APIRoutingEnums.Attributes.AVERAGE_SPEED.toString());
        Assert.assertEquals("detourfactor", APIRoutingEnums.Attributes.DETOUR_FACTOR.toString());
        Assert.assertEquals("percentage", APIRoutingEnums.Attributes.ROUTE_PERCENTAGE.toString());
    }
}
