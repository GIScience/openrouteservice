package org.heigit.ors.routing;

import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import org.heigit.ors.routing.parameters.VehicleParameters;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Polygon;

import static org.junit.jupiter.api.Assertions.*;

class RouteSearchParametersTest {

    @Test
    void expectFailingProfileParamsWithVehicleProfile() {
        assertThrows(ParameterValueException.class, () -> {
            RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
            routeSearchParameters.setProfileType(1);
            routeSearchParameters.setOptions("{\"profile_params\":{\"weightings\":{\"green\":{\"factor\":0.8}}}}");
        });
    }

    @Test
    void getProfileType() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertEquals(0, routeSearchParameters.getProfileType());
    }

    @Test
    void setProfileType() throws Exception {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setProfileType(2);
        assertEquals(2, routeSearchParameters.getProfileType());
    }

    @Test
    void getWeightingMethod() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertEquals(WeightingMethod.RECOMMENDED, routeSearchParameters.getWeightingMethod(), 0.0);
    }

    @Test
    void setWeightingMethod() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setWeightingMethod(WeightingMethod.FASTEST);
        assertEquals(WeightingMethod.FASTEST, routeSearchParameters.getWeightingMethod(), 0.0);
    }

    @Test
    void getAvoidAreas() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertArrayEquals(null, routeSearchParameters.getAvoidAreas());
    }

    @Test
    void setAvoidAreas() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidAreas(new Polygon[0]);
        assertArrayEquals(new Polygon[0], routeSearchParameters.getAvoidAreas());
    }

    @Test
    void hasAvoidAreas() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertFalse(routeSearchParameters.hasAvoidAreas());
        routeSearchParameters.setAvoidAreas(new Polygon[1]);
        assertTrue(routeSearchParameters.hasAvoidAreas());
    }

    @Test
    void getAvoidFeatureTypes() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertEquals(0, routeSearchParameters.getAvoidFeatureTypes());

    }

    @Test
    void setAvoidFeatureTypes() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidFeatureTypes(1);
        assertEquals(1, routeSearchParameters.getAvoidFeatureTypes());
    }

    @Test
    void hasAvoidFeatures() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertFalse(routeSearchParameters.hasAvoidFeatures());
        routeSearchParameters.setAvoidFeatureTypes(1);
        assertTrue(routeSearchParameters.hasAvoidFeatures());
    }

    @Test
    void getAvoidCountries() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertNull(routeSearchParameters.getAvoidCountries());
    }

    @Test
    void setAvoidCountries() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidCountries(new int[1]);
        assertArrayEquals(new int[1], routeSearchParameters.getAvoidCountries());
    }

    @Test
    void hasAvoidCountries() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertFalse(routeSearchParameters.hasAvoidCountries());
        routeSearchParameters.setAvoidCountries(new int[1]);
        assertTrue(routeSearchParameters.hasAvoidCountries());
    }

    @Test
    void hasAvoidBorders() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertFalse(routeSearchParameters.hasAvoidBorders());
        routeSearchParameters.setAvoidBorders(BordersExtractor.Avoid.CONTROLLED);
        assertTrue(routeSearchParameters.hasAvoidBorders());
    }

    @Test
    void setAvoidBorders() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidBorders(BordersExtractor.Avoid.CONTROLLED);
        assertEquals(BordersExtractor.Avoid.CONTROLLED, routeSearchParameters.getAvoidBorders());
    }

    @Test
    void getAvoidBorders() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertEquals(BordersExtractor.Avoid.NONE, routeSearchParameters.getAvoidBorders());
    }

    @Test
    void getConsiderTurnRestrictions() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertFalse(routeSearchParameters.getConsiderTurnRestrictions());
    }

    @Test
    void setConsiderTurnRestrictions() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setConsiderTurnRestrictions(true);
        assertTrue(routeSearchParameters.getConsiderTurnRestrictions());
    }

    @Test
    void getVehicleType() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertEquals(HeavyVehicleAttributes.UNKNOWN, routeSearchParameters.getVehicleType());
    }

    @Test
    void setVehicleType() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setVehicleType(HeavyVehicleAttributes.AGRICULTURE);
        assertEquals(HeavyVehicleAttributes.AGRICULTURE, routeSearchParameters.getVehicleType());

    }

    @Test
    void getOptions() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertNull(routeSearchParameters.getOptions());
    }

    @Test
    void hasParameters() throws Exception {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertFalse(routeSearchParameters.hasParameters(routeSearchParameters.getClass()));
        routeSearchParameters.setProfileType(2);
        routeSearchParameters.setOptions("{\"profile_params\":{\"weightings\":{\"green\":{\"factor\":0.8}}}}");
        assertTrue(routeSearchParameters.hasParameters(VehicleParameters.class));
    }

    @Test
    void getProfileParameters() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertNull(routeSearchParameters.getProfileParameters());
    }

    @Test
    void getFlexibleMode() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertFalse(routeSearchParameters.hasFlexibleMode());
    }

    @Test
    void setFlexibleMode() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setFlexibleMode(true);
        assertTrue(routeSearchParameters.hasFlexibleMode());
    }

    @Test
    void getMaximumRadiuses() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertNull(routeSearchParameters.getMaximumRadiuses());
    }

    @Test
    void setMaximumRadiuses() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setMaximumRadiuses(new double[0]);
        assertNotNull(routeSearchParameters.getMaximumRadiuses());
        assertSame(double[].class, routeSearchParameters.getMaximumRadiuses().getClass());
        assertEquals(0, routeSearchParameters.getMaximumRadiuses().length);
    }

    @Test
    void getBearings() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertNull(routeSearchParameters.getBearings());
    }

    @Test
    void setBearings() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setBearings(new WayPointBearing[]{});
        assertArrayEquals(new WayPointBearing[]{}, routeSearchParameters.getBearings());
    }

    @Test
    void requiresDynamicPreprocessedWeights() throws Exception {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        assertFalse(routeSearchParameters.requiresDynamicPreprocessedWeights());

        routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidAreas(new Polygon[1]);
        assertTrue(routeSearchParameters.requiresDynamicPreprocessedWeights(), "avoid areas");

        routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidFeatureTypes(1);
        assertTrue(routeSearchParameters.requiresDynamicPreprocessedWeights(), "avoid features");

        routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidBorders(BordersExtractor.Avoid.CONTROLLED);
        assertTrue(routeSearchParameters.requiresDynamicPreprocessedWeights(), "avoid borders");

        routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidCountries(new int[1]);
        assertTrue(routeSearchParameters.requiresDynamicPreprocessedWeights(), "avoid countries");

        routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setConsiderTurnRestrictions(true);
        assertTrue(routeSearchParameters.requiresDynamicPreprocessedWeights(), "turn restrictions");

        routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setProfileType(RoutingProfileType.DRIVING_HGV);
        assertFalse(routeSearchParameters.requiresDynamicPreprocessedWeights(), "default vehicle type");
        routeSearchParameters.setVehicleType(HeavyVehicleAttributes.BUS);
        assertTrue(routeSearchParameters.requiresDynamicPreprocessedWeights(), "non-default vehicle type");

        routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setProfileType(RoutingProfileType.DRIVING_HGV);
        routeSearchParameters.setOptions("{\"profile_params\":{\"weightings\":{\"green\":{\"factor\":0.8}}}}");
        assertTrue(routeSearchParameters.requiresDynamicPreprocessedWeights(), "profile param");
    }

    @Test
    void alternativeRoutesParams() throws Exception {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setOptions("{\"alternative_routes_count\": 2, \"alternative_routes_weight_factor\": 3.3, \"alternative_routes_share_factor\": 4.4}}");
        assertEquals(2, routeSearchParameters.getAlternativeRoutesCount());
        assertEquals(3.3, routeSearchParameters.getAlternativeRoutesWeightFactor(), 0.0);
        assertEquals(4.4, routeSearchParameters.getAlternativeRoutesShareFactor(), 0.0);
    }
}
