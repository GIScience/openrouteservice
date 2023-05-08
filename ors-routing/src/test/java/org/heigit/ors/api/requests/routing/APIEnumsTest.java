package org.heigit.ors.api.requests.routing;

import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.exceptions.ParameterValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class APIEnumsTest {
    @Test
    void testBordersEnumCreation() throws ParameterValueException {
        assertEquals(APIEnums.AvoidBorders.CONTROLLED, APIEnums.AvoidBorders.forValue("controlled"));
        assertEquals(APIEnums.AvoidBorders.ALL, APIEnums.AvoidBorders.forValue("all"));
        assertEquals(APIEnums.AvoidBorders.NONE, APIEnums.AvoidBorders.forValue("none"));
        assertThrows(ParameterValueException.class, () -> APIEnums.AvoidBorders.forValue("invalid"));
    }

    @Test
    void testBordersEnumValue() {
        assertEquals("controlled", APIEnums.AvoidBorders.CONTROLLED.toString());
        assertEquals("all", APIEnums.AvoidBorders.ALL.toString());
        assertEquals("none", APIEnums.AvoidBorders.NONE.toString());
    }

    @Test
    void testExtraInfoEnumCreation() throws ParameterValueException {
        assertEquals(APIEnums.ExtraInfo.STEEPNESS, APIEnums.ExtraInfo.forValue("steepness"));
        assertEquals(APIEnums.ExtraInfo.SUITABILITY, APIEnums.ExtraInfo.forValue("suitability"));
        assertEquals(APIEnums.ExtraInfo.SURFACE, APIEnums.ExtraInfo.forValue("surface"));
        assertEquals(APIEnums.ExtraInfo.WAY_CATEGORY, APIEnums.ExtraInfo.forValue("waycategory"));
        assertEquals(APIEnums.ExtraInfo.WAY_TYPE, APIEnums.ExtraInfo.forValue("waytype"));
        assertEquals(APIEnums.ExtraInfo.TOLLWAYS, APIEnums.ExtraInfo.forValue("tollways"));
        assertEquals(APIEnums.ExtraInfo.TRAIL_DIFFICULTY, APIEnums.ExtraInfo.forValue("traildifficulty"));
        assertEquals(APIEnums.ExtraInfo.OSM_ID, APIEnums.ExtraInfo.forValue("osmid"));
        assertEquals(APIEnums.ExtraInfo.COUNTRY_INFO, APIEnums.ExtraInfo.forValue("countryinfo"));
        assertThrows(ParameterValueException.class, () -> APIEnums.ExtraInfo.forValue("invalid"));
    }

    @Test
    void testExtraInfoEnumValue() {
        assertEquals("steepness", APIEnums.ExtraInfo.STEEPNESS.toString());
        assertEquals("suitability", APIEnums.ExtraInfo.SUITABILITY.toString());
        assertEquals("surface", APIEnums.ExtraInfo.SURFACE.toString());
        assertEquals("waycategory", APIEnums.ExtraInfo.WAY_CATEGORY.toString());
        assertEquals("waytype", APIEnums.ExtraInfo.WAY_TYPE.toString());
        assertEquals("tollways", APIEnums.ExtraInfo.TOLLWAYS.toString());
        assertEquals("traildifficulty", APIEnums.ExtraInfo.TRAIL_DIFFICULTY.toString());
        assertEquals("osmid", APIEnums.ExtraInfo.OSM_ID.toString());
        assertEquals("countryinfo", APIEnums.ExtraInfo.COUNTRY_INFO.toString());
    }

    @Test
    void testRouteResponseTypeEnumCreation() throws ParameterValueException {
        assertEquals(APIEnums.RouteResponseType.JSON, APIEnums.RouteResponseType.forValue("json"));
        assertEquals(APIEnums.RouteResponseType.GEOJSON, APIEnums.RouteResponseType.forValue("geojson"));
        assertEquals(APIEnums.RouteResponseType.GPX, APIEnums.RouteResponseType.forValue("gpx"));
        assertThrows(ParameterValueException.class, () -> APIEnums.RouteResponseType.forValue("invalid"));
    }

    @Test
    void testMatrixResponseTypeEnumCreation() throws ParameterValueException {
        assertEquals(APIEnums.MatrixResponseType.JSON, APIEnums.MatrixResponseType.forValue("json"));
        assertThrows(ParameterValueException.class, () -> APIEnums.MatrixResponseType.forValue("invalid"));
    }


    @Test
    void testResponseTypeEnumValue() {
        assertEquals("geojson", APIEnums.RouteResponseType.GEOJSON.toString());
        assertEquals("gpx", APIEnums.RouteResponseType.GPX.toString());
        assertEquals("json", APIEnums.RouteResponseType.JSON.toString());
    }

    @Test
    void testVehicleTypeEnumCreation() throws ParameterValueException {
        assertEquals(APIEnums.VehicleType.AGRICULTURAL, APIEnums.VehicleType.forValue("agricultural"));
        assertEquals(APIEnums.VehicleType.BUS, APIEnums.VehicleType.forValue("bus"));
        assertEquals(APIEnums.VehicleType.DELIVERY, APIEnums.VehicleType.forValue("delivery"));
        assertEquals(APIEnums.VehicleType.FORESTRY, APIEnums.VehicleType.forValue("forestry"));
        assertEquals(APIEnums.VehicleType.GOODS, APIEnums.VehicleType.forValue("goods"));
        assertEquals(APIEnums.VehicleType.HGV, APIEnums.VehicleType.forValue("hgv"));
        assertEquals(APIEnums.VehicleType.UNKNOWN, APIEnums.VehicleType.forValue("unknown"));
        assertThrows(ParameterValueException.class, () -> APIEnums.VehicleType.forValue("invalid"));
    }

    @Test
    void testVehicleTypeEnumValue() {
        assertEquals("agricultural", APIEnums.VehicleType.AGRICULTURAL.toString());
        assertEquals("bus", APIEnums.VehicleType.BUS.toString());
        assertEquals("delivery", APIEnums.VehicleType.DELIVERY.toString());
        assertEquals("forestry", APIEnums.VehicleType.FORESTRY.toString());
        assertEquals("goods", APIEnums.VehicleType.GOODS.toString());
        assertEquals("hgv", APIEnums.VehicleType.HGV.toString());
        assertEquals("unknown", APIEnums.VehicleType.UNKNOWN.toString());
    }

    @Test
    void testAvoidFeaturesEnumCreation() throws ParameterValueException {
        assertEquals(APIEnums.AvoidFeatures.FORDS, APIEnums.AvoidFeatures.forValue("fords"));
        assertEquals(APIEnums.AvoidFeatures.FERRIES, APIEnums.AvoidFeatures.forValue("ferries"));
        assertEquals(APIEnums.AvoidFeatures.HIGHWAYS, APIEnums.AvoidFeatures.forValue("highways"));
        assertEquals(APIEnums.AvoidFeatures.STEPS, APIEnums.AvoidFeatures.forValue("steps"));
        assertEquals(APIEnums.AvoidFeatures.TOLLWAYS, APIEnums.AvoidFeatures.forValue("tollways"));
        assertThrows(ParameterValueException.class, () -> APIEnums.AvoidFeatures.forValue("invalid"));
    }

    @Test
    void testAvoidFeaturesEnumValue() {
        assertEquals("fords", APIEnums.AvoidFeatures.FORDS.toString());
        assertEquals("ferries", APIEnums.AvoidFeatures.FERRIES.toString());
        assertEquals("highways", APIEnums.AvoidFeatures.HIGHWAYS.toString());
        assertEquals("steps", APIEnums.AvoidFeatures.STEPS.toString());
        assertEquals("tollways", APIEnums.AvoidFeatures.TOLLWAYS.toString());
    }

    @Test
    void testPreferenceEnumCreation() throws ParameterValueException {
        assertEquals(APIEnums.RoutePreference.FASTEST, APIEnums.RoutePreference.forValue("fastest"));
        assertEquals(APIEnums.RoutePreference.SHORTEST, APIEnums.RoutePreference.forValue("shortest"));
        assertEquals(APIEnums.RoutePreference.RECOMMENDED, APIEnums.RoutePreference.forValue("recommended"));
        assertThrows(ParameterValueException.class, () -> APIEnums.RoutePreference.forValue("invalid"));
    }

    @Test
    void testPreferenceEnumValue() {
        assertEquals("fastest", APIEnums.RoutePreference.FASTEST.toString());
        assertEquals("shortest", APIEnums.RoutePreference.SHORTEST.toString());
        assertEquals("recommended", APIEnums.RoutePreference.RECOMMENDED.toString());
    }

    @Test
    void testProfileEnumCreation() throws ParameterValueException {
        assertEquals(APIEnums.Profile.DRIVING_CAR, APIEnums.Profile.forValue("driving-car"));
        assertEquals(APIEnums.Profile.DRIVING_HGV, APIEnums.Profile.forValue("driving-hgv"));
        assertEquals(APIEnums.Profile.CYCLING_REGULAR, APIEnums.Profile.forValue("cycling-regular"));
        assertEquals(APIEnums.Profile.CYCLING_ROAD, APIEnums.Profile.forValue("cycling-road"));
        assertEquals(APIEnums.Profile.CYCLING_MOUNTAIN, APIEnums.Profile.forValue("cycling-mountain"));
        assertEquals(APIEnums.Profile.CYCLING_ELECTRIC, APIEnums.Profile.forValue("cycling-electric"));
        assertEquals(APIEnums.Profile.FOOT_WALKING, APIEnums.Profile.forValue("foot-walking"));
        assertEquals(APIEnums.Profile.FOOT_HIKING, APIEnums.Profile.forValue("foot-hiking"));
        assertEquals(APIEnums.Profile.WHEELCHAIR, APIEnums.Profile.forValue("wheelchair"));
        assertThrows(ParameterValueException.class, () -> APIEnums.Profile.forValue("invalid"));
    }

    @Test
    void testProfileEnumValue() {
        assertEquals("driving-car", APIEnums.Profile.DRIVING_CAR.toString());
        assertEquals("driving-hgv", APIEnums.Profile.DRIVING_HGV.toString());
        assertEquals("cycling-regular", APIEnums.Profile.CYCLING_REGULAR.toString());
        assertEquals("cycling-road", APIEnums.Profile.CYCLING_ROAD.toString());
        assertEquals("cycling-mountain", APIEnums.Profile.CYCLING_MOUNTAIN.toString());
        assertEquals("cycling-electric", APIEnums.Profile.CYCLING_ELECTRIC.toString());
        assertEquals("foot-walking", APIEnums.Profile.FOOT_WALKING.toString());
        assertEquals("foot-hiking", APIEnums.Profile.FOOT_HIKING.toString());
        assertEquals("wheelchair", APIEnums.Profile.WHEELCHAIR.toString());
    }

    @Test
    void testUnitsEnumCreation() throws ParameterValueException {
        assertEquals(APIEnums.Units.METRES, APIEnums.Units.forValue("m"));
        assertEquals(APIEnums.Units.KILOMETRES, APIEnums.Units.forValue("km"));
        assertEquals(APIEnums.Units.MILES, APIEnums.Units.forValue("mi"));
        assertThrows(ParameterValueException.class, () -> APIEnums.Units.forValue("invalid"));
    }

    @Test
    void testUnitsEnumValue() {
        assertEquals("m", APIEnums.Units.METRES.toString());
        assertEquals("km", APIEnums.Units.KILOMETRES.toString());
        assertEquals("mi", APIEnums.Units.MILES.toString());
    }

    @ParameterizedTest
    @MethodSource("org.heigit.ors.util.TestProvider#languagesEnumTestProvider")
    void testLanguagesEnumCreation(String languageEnum, APIEnums.Languages expectedLanguageEnum) throws ParameterValueException {
        assertEquals(expectedLanguageEnum, APIEnums.Languages.forValue(languageEnum));
    }

    @Test
    void testLanguagesEnumCreationMustFail() {
        assertThrows(ParameterValueException.class, () -> APIEnums.Languages.forValue("invalid"));
    }

    @ParameterizedTest
    @MethodSource("org.heigit.ors.util.TestProvider#languagesEnumTestProvider")
    void testLanguagesEnumValue(String expectedLanguageEnumString, APIEnums.Languages languageEnum) {
        assertEquals(expectedLanguageEnumString, languageEnum.toString());
    }

    @Test
    void testInstructionSFormatEnumCreation() throws ParameterValueException {
        assertEquals(APIEnums.InstructionsFormat.HTML, APIEnums.InstructionsFormat.forValue("html"));
        assertEquals(APIEnums.InstructionsFormat.TEXT, APIEnums.InstructionsFormat.forValue("text"));
        assertThrows(ParameterValueException.class, () -> APIEnums.InstructionsFormat.forValue("invalid"));
    }

    @Test
    void testInstructionSFormatEnumvALUE() {
        assertEquals("html", APIEnums.InstructionsFormat.HTML.toString());
        assertEquals("text", APIEnums.InstructionsFormat.TEXT.toString());
    }

    @Test
    void testAttributesEnumCreation() throws ParameterValueException {
        assertEquals(APIEnums.Attributes.AVERAGE_SPEED, APIEnums.Attributes.forValue("avgspeed"));
        assertEquals(APIEnums.Attributes.DETOUR_FACTOR, APIEnums.Attributes.forValue("detourfactor"));
        assertEquals(APIEnums.Attributes.ROUTE_PERCENTAGE, APIEnums.Attributes.forValue("percentage"));
        assertThrows(ParameterValueException.class, () -> APIEnums.Attributes.forValue("invalid"));
    }

    @Test
    void testAttributesEnumValue() {
        assertEquals("avgspeed", APIEnums.Attributes.AVERAGE_SPEED.toString());
        assertEquals("detourfactor", APIEnums.Attributes.DETOUR_FACTOR.toString());
        assertEquals("percentage", APIEnums.Attributes.ROUTE_PERCENTAGE.toString());
    }
}
