package org.heigit.ors.routing;

import com.vividsolutions.jts.geom.Polygon;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import org.heigit.ors.routing.parameters.VehicleParameters;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RouteSearchParametersTest {
    @Before
    public void setUp() {
        System.setProperty("ors_app_config", "target/test-classes/app.config.test");
    }

    @Test(expected = ParameterValueException.class)
    public void expectFailingProfileParamsWithVehicleProfile() throws Exception {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setProfileType(1);
        routeSearchParameters.setOptions("{\"profile_params\":{\"weightings\":{\"green\":{\"factor\":0.8}}}}");
    }

    @Test
    public void getProfileType() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertEquals(0, routeSearchParameters.getProfileType());
    }

    @Test
    public void setProfileType() throws Exception {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setProfileType(2);
        Assert.assertEquals(2, routeSearchParameters.getProfileType());
    }

    @Test
    public void getWeightingMethod() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertEquals(WeightingMethod.FASTEST, routeSearchParameters.getWeightingMethod(), 0.0);
    }

    @Test
    public void setWeightingMethod() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setWeightingMethod(WeightingMethod.RECOMMENDED);
        Assert.assertEquals(WeightingMethod.RECOMMENDED, routeSearchParameters.getWeightingMethod(), 0.0);
    }

    @Test
    public void getAvoidAreas() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertArrayEquals(null, routeSearchParameters.getAvoidAreas());
    }

    @Test
    public void setAvoidAreas() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidAreas(new Polygon[0]);
        Assert.assertArrayEquals(new Polygon[0], routeSearchParameters.getAvoidAreas());
    }

    @Test
    public void hasAvoidAreas() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertFalse(routeSearchParameters.hasAvoidAreas());
        routeSearchParameters.setAvoidAreas(new Polygon[1]);
        Assert.assertTrue(routeSearchParameters.hasAvoidAreas());
    }

    @Test
    public void getAvoidFeatureTypes() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertEquals(0, routeSearchParameters.getAvoidFeatureTypes());

    }

    @Test
    public void setAvoidFeatureTypes() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidFeatureTypes(1);
        Assert.assertEquals(1, routeSearchParameters.getAvoidFeatureTypes());
    }

    @Test
    public void hasAvoidFeatures() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertFalse(routeSearchParameters.hasAvoidFeatures());
        routeSearchParameters.setAvoidFeatureTypes(1);
        Assert.assertTrue(routeSearchParameters.hasAvoidFeatures());
    }

    @Test
    public void getAvoidCountries() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertNull(routeSearchParameters.getAvoidCountries());
    }

    @Test
    public void setAvoidCountries() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidCountries(new int[1]);
        Assert.assertArrayEquals(new int[1], routeSearchParameters.getAvoidCountries());
    }

    @Test
    public void hasAvoidCountries() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertFalse(routeSearchParameters.hasAvoidCountries());
        routeSearchParameters.setAvoidCountries(new int[1]);
        Assert.assertTrue(routeSearchParameters.hasAvoidCountries());
    }

    @Test
    public void hasAvoidBorders() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertFalse(routeSearchParameters.hasAvoidBorders());
        routeSearchParameters.setAvoidBorders(BordersExtractor.Avoid.CONTROLLED);
        Assert.assertTrue(routeSearchParameters.hasAvoidBorders());
    }

    @Test
    public void setAvoidBorders() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidBorders(BordersExtractor.Avoid.CONTROLLED);
        Assert.assertEquals(BordersExtractor.Avoid.CONTROLLED, routeSearchParameters.getAvoidBorders());
    }

    @Test
    public void getAvoidBorders() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertEquals(BordersExtractor.Avoid.NONE, routeSearchParameters.getAvoidBorders());
    }

    @Test
    public void getConsiderTurnRestrictions() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertFalse(routeSearchParameters.getConsiderTurnRestrictions());
    }

    @Test
    public void setConsiderTurnRestrictions() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setConsiderTurnRestrictions(true);
        Assert.assertTrue(routeSearchParameters.getConsiderTurnRestrictions());
    }

    @Test
    public void getVehicleType() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertEquals(HeavyVehicleAttributes.UNKNOWN, routeSearchParameters.getVehicleType());
    }

    @Test
    public void setVehicleType() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setVehicleType(HeavyVehicleAttributes.AGRICULTURE);
        Assert.assertEquals(HeavyVehicleAttributes.AGRICULTURE, routeSearchParameters.getVehicleType());

    }

    @Test
    public void getOptions() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertNull(routeSearchParameters.getOptions());
    }

    @Test
    public void hasParameters() throws Exception {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertFalse(routeSearchParameters.hasParameters(routeSearchParameters.getClass()));
        routeSearchParameters.setProfileType(2);
        routeSearchParameters.setOptions("{\"profile_params\":{\"weightings\":{\"green\":{\"factor\":0.8}}}}");
        Assert.assertTrue(routeSearchParameters.hasParameters(VehicleParameters.class));
    }

    @Test
    public void getProfileParameters() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertNull(routeSearchParameters.getProfileParameters());
    }

    @Test
    public void getFlexibleMode() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertFalse(routeSearchParameters.getFlexibleMode());
    }

    @Test
    public void setFlexibleMode() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setFlexibleMode(true);
        Assert.assertTrue(routeSearchParameters.getFlexibleMode());
    }

    @Test
    public void getMaximumRadiuses() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertNull(routeSearchParameters.getMaximumRadiuses());
    }

    @Test
    public void setMaximumRadiuses() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setMaximumRadiuses(new double[0]);
        Assert.assertNotNull(routeSearchParameters.getMaximumRadiuses());
        Assert.assertSame(routeSearchParameters.getMaximumRadiuses().getClass(), double[].class);
        Assert.assertEquals(0, routeSearchParameters.getMaximumRadiuses().length);
    }

    @Test
    public void getBearings() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertNull(routeSearchParameters.getBearings());
    }

    @Test
    public void setBearings() {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setBearings(new WayPointBearing[]{});
        Assert.assertArrayEquals(new WayPointBearing[]{}, routeSearchParameters.getBearings());
    }

    @Test
    public void requiresDynamicPreprocessedWeights() throws Exception {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        Assert.assertFalse(routeSearchParameters.requiresDynamicPreprocessedWeights());

        routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidAreas(new Polygon[1]);
        Assert.assertTrue("avoid areas", routeSearchParameters.requiresDynamicPreprocessedWeights());

        routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidFeatureTypes(1);
        Assert.assertTrue("avoid features", routeSearchParameters.requiresDynamicPreprocessedWeights());

        routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidBorders(BordersExtractor.Avoid.CONTROLLED);
        Assert.assertTrue("avoid borders", routeSearchParameters.requiresDynamicPreprocessedWeights());

        routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setAvoidCountries(new int[1]);
        Assert.assertTrue("avoid countries", routeSearchParameters.requiresDynamicPreprocessedWeights());

        routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setConsiderTurnRestrictions(true);
        Assert.assertTrue("turn restrictions", routeSearchParameters.requiresDynamicPreprocessedWeights());

        routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setProfileType(RoutingProfileType.DRIVING_HGV);
        routeSearchParameters.setVehicleType(HeavyVehicleAttributes.HGV);
        Assert.assertTrue("heavy vehicle", routeSearchParameters.requiresDynamicPreprocessedWeights());

        routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setProfileType(RoutingProfileType.DRIVING_HGV);
        routeSearchParameters.setOptions("{\"profile_params\":{\"weightings\":{\"green\":{\"factor\":0.8}}}}");
        Assert.assertTrue("profile param", routeSearchParameters.requiresDynamicPreprocessedWeights());
    }

    @Test
    public void alternativeRoutesParams() throws Exception {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        routeSearchParameters.setOptions("{\"alternative_routes_count\": 2, \"alternative_routes_weight_factor\": 3.3, \"alternative_routes_share_factor\": 4.4}}");
        Assert.assertEquals(2, routeSearchParameters.getAlternativeRoutesCount());
        Assert.assertEquals(3.3, routeSearchParameters.getAlternativeRoutesWeightFactor(), 0.0);
        Assert.assertEquals(4.4, routeSearchParameters.getAlternativeRoutesShareFactor(), 0.0);
    }
}
