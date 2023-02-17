package org.heigit.ors.api.requests.routing;

import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.exceptions.ParameterValueException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class APIEnumsTest {
    @Test
    public void testBordersEnumCreation() {
        assertThrows(ParameterValueException.class, () -> {
            assertEquals(APIEnums.AvoidBorders.CONTROLLED, APIEnums.AvoidBorders.forValue("controlled"));
            assertEquals(APIEnums.AvoidBorders.ALL, APIEnums.AvoidBorders.forValue("all"));
            assertEquals(APIEnums.AvoidBorders.NONE, APIEnums.AvoidBorders.forValue("none"));

            APIEnums.AvoidBorders.forValue("invalid");

        });

    }

    @Test
    void testBordersEnumValue() {
        assertEquals("controlled", APIEnums.AvoidBorders.CONTROLLED.toString());
        assertEquals("all", APIEnums.AvoidBorders.ALL.toString());
        assertEquals("none", APIEnums.AvoidBorders.NONE.toString());
    }

    @Test
    void testExtraInfoEnumCreation() {
        assertThrows(ParameterValueException.class, () -> {
            assertEquals(APIEnums.ExtraInfo.STEEPNESS, APIEnums.ExtraInfo.forValue("steepness"));
            assertEquals(APIEnums.ExtraInfo.SUITABILITY, APIEnums.ExtraInfo.forValue("suitability"));
            assertEquals(APIEnums.ExtraInfo.SURFACE, APIEnums.ExtraInfo.forValue("surface"));
            assertEquals(APIEnums.ExtraInfo.WAY_CATEGORY, APIEnums.ExtraInfo.forValue("waycategory"));
            assertEquals(APIEnums.ExtraInfo.WAY_TYPE, APIEnums.ExtraInfo.forValue("waytype"));
            assertEquals(APIEnums.ExtraInfo.TOLLWAYS, APIEnums.ExtraInfo.forValue("tollways"));
            assertEquals(APIEnums.ExtraInfo.TRAIL_DIFFICULTY, APIEnums.ExtraInfo.forValue("traildifficulty"));
            assertEquals(APIEnums.ExtraInfo.OSM_ID, APIEnums.ExtraInfo.forValue("osmid"));
            assertEquals(APIEnums.ExtraInfo.COUNTRY_INFO, APIEnums.ExtraInfo.forValue("countryinfo"));

            APIEnums.ExtraInfo.forValue("invalid");
        });
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
    void testRouteResponseTypeEnumCreation() {
        assertThrows(ParameterValueException.class, () -> {
            assertEquals(APIEnums.RouteResponseType.JSON, APIEnums.RouteResponseType.forValue("json"));
            assertEquals(APIEnums.RouteResponseType.GEOJSON, APIEnums.RouteResponseType.forValue("geojson"));
            assertEquals(APIEnums.RouteResponseType.GPX, APIEnums.RouteResponseType.forValue("gpx"));

            APIEnums.RouteResponseType.forValue("invalid");
        });
    }

    @Test
    void testMatrixResponseTypeEnumCreation() {
        assertThrows(ParameterValueException.class, () -> {
            assertEquals(APIEnums.MatrixResponseType.JSON, APIEnums.MatrixResponseType.forValue("json"));

            APIEnums.MatrixResponseType.forValue("invalid");
        });
    }


    @Test
    void testResponseTypeEnumValue() {
        assertEquals("geojson", APIEnums.RouteResponseType.GEOJSON.toString());
        assertEquals("gpx", APIEnums.RouteResponseType.GPX.toString());
        assertEquals("json", APIEnums.RouteResponseType.JSON.toString());
    }

    @Test
    void testVehicleTypeEnumCreation() {
        assertThrows(ParameterValueException.class, () -> {
            assertEquals(APIEnums.VehicleType.AGRICULTURAL, APIEnums.VehicleType.forValue("agricultural"));
            assertEquals(APIEnums.VehicleType.BUS, APIEnums.VehicleType.forValue("bus"));
            assertEquals(APIEnums.VehicleType.DELIVERY, APIEnums.VehicleType.forValue("delivery"));
            assertEquals(APIEnums.VehicleType.FORESTRY, APIEnums.VehicleType.forValue("forestry"));
            assertEquals(APIEnums.VehicleType.GOODS, APIEnums.VehicleType.forValue("goods"));
            assertEquals(APIEnums.VehicleType.HGV, APIEnums.VehicleType.forValue("hgv"));
            assertEquals(APIEnums.VehicleType.UNKNOWN, APIEnums.VehicleType.forValue("unkown"));

            APIEnums.VehicleType.forValue("invalid");
        });
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
    void testAvoidFeaturesEnumCreation() {
        assertThrows(ParameterValueException.class, () -> {
            assertEquals(APIEnums.AvoidFeatures.FORDS, APIEnums.AvoidFeatures.forValue("fords"));
            assertEquals(APIEnums.AvoidFeatures.FERRIES, APIEnums.AvoidFeatures.forValue("ferries"));
            assertEquals(APIEnums.AvoidFeatures.HIGHWAYS, APIEnums.AvoidFeatures.forValue("highways"));
            assertEquals(APIEnums.AvoidFeatures.STEPS, APIEnums.AvoidFeatures.forValue("steps"));
            assertEquals(APIEnums.AvoidFeatures.TOLLWAYS, APIEnums.AvoidFeatures.forValue("tollways"));

            APIEnums.AvoidFeatures.forValue("invalid");
        });
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
    void testPreferenceEnumCreation() {
        assertThrows(ParameterValueException.class, () -> {
            assertEquals(APIEnums.RoutePreference.FASTEST, APIEnums.RoutePreference.forValue("fastest"));
            assertEquals(APIEnums.RoutePreference.SHORTEST, APIEnums.RoutePreference.forValue("shortest"));
            assertEquals(APIEnums.RoutePreference.RECOMMENDED, APIEnums.RoutePreference.forValue("recommended"));

            APIEnums.RoutePreference.forValue("invalid");
        });
    }

    @Test
    void testPreferenceEnumValue() {
        assertEquals("fastest", APIEnums.RoutePreference.FASTEST.toString());
        assertEquals("shortest", APIEnums.RoutePreference.SHORTEST.toString());
        assertEquals("recommended", APIEnums.RoutePreference.RECOMMENDED.toString());
    }

    @Test
    void testProfileEnumCreation() {
        assertThrows(ParameterValueException.class, () -> {
            assertEquals(APIEnums.Profile.DRIVING_CAR, APIEnums.Profile.forValue("driving-car"));
            assertEquals(APIEnums.Profile.DRIVING_HGV, APIEnums.Profile.forValue("driving-hgv"));
            assertEquals(APIEnums.Profile.CYCLING_REGULAR, APIEnums.Profile.forValue("cycling-regular"));
            assertEquals(APIEnums.Profile.CYCLING_ROAD, APIEnums.Profile.forValue("cycling-road"));
            assertEquals(APIEnums.Profile.CYCLING_MOUNTAIN, APIEnums.Profile.forValue("cycling-mountain"));
            assertEquals(APIEnums.Profile.CYCLING_ELECTRIC, APIEnums.Profile.forValue("cycling-electric"));
            assertEquals(APIEnums.Profile.FOOT_WALKING, APIEnums.Profile.forValue("foot-walking"));
            assertEquals(APIEnums.Profile.FOOT_HIKING, APIEnums.Profile.forValue("foot-hiking"));
            assertEquals(APIEnums.Profile.WHEELCHAIR, APIEnums.Profile.forValue("wheelchair"));

            APIEnums.Profile.forValue("invalid");
        });
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
    void testUnitsEnumCreation() {
        assertThrows(ParameterValueException.class, () -> {
            assertEquals(APIEnums.Units.METRES, APIEnums.Units.forValue("m"));
            assertEquals(APIEnums.Units.KILOMETRES, APIEnums.Units.forValue("km"));
            assertEquals(APIEnums.Units.MILES, APIEnums.Units.forValue("mi"));

            APIEnums.Units.forValue("invalid");
        });
    }

    @Test
    void testUnitsEnumValue() {
        assertEquals("m", APIEnums.Units.METRES.toString());
        assertEquals("km", APIEnums.Units.KILOMETRES.toString());
        assertEquals("mi", APIEnums.Units.MILES.toString());
    }

    @Test
    void testLanguagesEnumCreation() {
        assertThrows(ParameterValueException.class, () -> {
            assertEquals(APIEnums.Languages.DE, APIEnums.Languages.forValue("de"));
            assertEquals(APIEnums.Languages.DE_DE, APIEnums.Languages.forValue("de-de"));
            assertEquals(APIEnums.Languages.EN, APIEnums.Languages.forValue("en"));
            assertEquals(APIEnums.Languages.EN_US, APIEnums.Languages.forValue("en-us"));
            assertEquals(APIEnums.Languages.ES, APIEnums.Languages.forValue("es"));
            assertEquals(APIEnums.Languages.ES_ES, APIEnums.Languages.forValue("es-es"));
            assertEquals(APIEnums.Languages.FR, APIEnums.Languages.forValue("fr"));
            assertEquals(APIEnums.Languages.FR_FR, APIEnums.Languages.forValue("fr-fr"));
            assertEquals(APIEnums.Languages.GR, APIEnums.Languages.forValue("gr"));
            assertEquals(APIEnums.Languages.GR_GR, APIEnums.Languages.forValue("gr-gr"));
            assertEquals(APIEnums.Languages.HE, APIEnums.Languages.forValue("he"));
            assertEquals(APIEnums.Languages.HE_IL, APIEnums.Languages.forValue("he-il"));
            assertEquals(APIEnums.Languages.HU, APIEnums.Languages.forValue("hu"));
            assertEquals(APIEnums.Languages.HU_HU, APIEnums.Languages.forValue("hu-hu"));
            assertEquals(APIEnums.Languages.ID, APIEnums.Languages.forValue("id"));
            assertEquals(APIEnums.Languages.ID_ID, APIEnums.Languages.forValue("id-id"));
            assertEquals(APIEnums.Languages.IT, APIEnums.Languages.forValue("it"));
            assertEquals(APIEnums.Languages.IT_IT, APIEnums.Languages.forValue("it-it"));
            assertEquals(APIEnums.Languages.NE, APIEnums.Languages.forValue("ne"));
            assertEquals(APIEnums.Languages.NE_NP, APIEnums.Languages.forValue("ne-np"));
            assertEquals(APIEnums.Languages.NL, APIEnums.Languages.forValue("nl"));
            assertEquals(APIEnums.Languages.NL_NL, APIEnums.Languages.forValue("nl-nl"));
            assertEquals(APIEnums.Languages.PL, APIEnums.Languages.forValue("pl"));
            assertEquals(APIEnums.Languages.PL_PL, APIEnums.Languages.forValue("pl-pl"));
            assertEquals(APIEnums.Languages.PT, APIEnums.Languages.forValue("pt"));
            assertEquals(APIEnums.Languages.PT_PT, APIEnums.Languages.forValue("pt-pt"));
            assertEquals(APIEnums.Languages.RU, APIEnums.Languages.forValue("ru"));
            assertEquals(APIEnums.Languages.RU_RU, APIEnums.Languages.forValue("ru-ru"));
            assertEquals(APIEnums.Languages.ZH, APIEnums.Languages.forValue("zh"));
            assertEquals(APIEnums.Languages.ZH_CN, APIEnums.Languages.forValue("zh-cn"));

            APIEnums.Languages.forValue("invalid");
        });
    }

    @Test
    void testLanguagesEnumValue() {
        assertEquals("de", APIEnums.Languages.DE.toString());
        assertEquals("de-de", APIEnums.Languages.DE_DE.toString());
        assertEquals("en", APIEnums.Languages.EN.toString());
        assertEquals("en-us", APIEnums.Languages.EN_US.toString());
        assertEquals("es", APIEnums.Languages.ES.toString());
        assertEquals("es-es", APIEnums.Languages.ES_ES.toString());
        assertEquals("fr", APIEnums.Languages.FR.toString());
        assertEquals("fr-fr", APIEnums.Languages.FR_FR.toString());
        assertEquals("gr", APIEnums.Languages.GR.toString());
        assertEquals("gr-gr", APIEnums.Languages.GR_GR.toString());
        assertEquals("he", APIEnums.Languages.HE.toString());
        assertEquals("he-il", APIEnums.Languages.HE_IL.toString());
        assertEquals("hu", APIEnums.Languages.HU.toString());
        assertEquals("hu-hu", APIEnums.Languages.HU_HU.toString());
        assertEquals("id", APIEnums.Languages.ID.toString());
        assertEquals("id-id", APIEnums.Languages.ID_ID.toString());
        assertEquals("it", APIEnums.Languages.IT.toString());
        assertEquals("it-it", APIEnums.Languages.IT_IT.toString());
        assertEquals("ne", APIEnums.Languages.NE.toString());
        assertEquals("ne-np", APIEnums.Languages.NE_NP.toString());
        assertEquals("nl", APIEnums.Languages.NL.toString());
        assertEquals("nl-nl", APIEnums.Languages.NL_NL.toString());
        assertEquals("pl", APIEnums.Languages.PL.toString());
        assertEquals("pl-pl", APIEnums.Languages.PL_PL.toString());
        assertEquals("pt", APIEnums.Languages.PT.toString());
        assertEquals("pt-pt", APIEnums.Languages.PT_PT.toString());
        assertEquals("ru", APIEnums.Languages.RU.toString());
        assertEquals("ru-ru", APIEnums.Languages.RU_RU.toString());
        assertEquals("zh", APIEnums.Languages.ZH.toString());
        assertEquals("zh-cn", APIEnums.Languages.ZH_CN.toString());
    }

    @Test
    void testInstructionSFormatEnumCreation() {
        assertThrows(ParameterValueException.class, () -> {
            assertEquals(APIEnums.InstructionsFormat.HTML, APIEnums.InstructionsFormat.forValue("html"));
            assertEquals(APIEnums.InstructionsFormat.TEXT, APIEnums.InstructionsFormat.forValue("text"));

            APIEnums.InstructionsFormat.forValue("invalid");
        });
    }

    @Test
    void testInstructionSFormatEnumvALUE() {
        assertEquals("html", APIEnums.InstructionsFormat.HTML.toString());
        assertEquals("text", APIEnums.InstructionsFormat.TEXT.toString());
    }

    @Test
    void testAttributesEnumCreation() {
        assertThrows(ParameterValueException.class, () -> {
            assertEquals(APIEnums.Attributes.AVERAGE_SPEED, APIEnums.Attributes.forValue("avgspeed"));
            assertEquals(APIEnums.Attributes.DETOUR_FACTOR, APIEnums.Attributes.forValue("detourfactor"));
            assertEquals(APIEnums.Attributes.ROUTE_PERCENTAGE, APIEnums.Attributes.forValue("percentage"));

            APIEnums.Attributes.forValue("invalid");
        });
    }

    @Test
    void testAttributesEnumValue() {
        assertEquals("avgspeed", APIEnums.Attributes.AVERAGE_SPEED.toString());
        assertEquals("detourfactor", APIEnums.Attributes.DETOUR_FACTOR.toString());
        assertEquals("percentage", APIEnums.Attributes.ROUTE_PERCENTAGE.toString());
    }
}
