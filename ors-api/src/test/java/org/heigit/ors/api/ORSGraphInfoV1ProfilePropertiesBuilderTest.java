package org.heigit.ors.api;

import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphInfoV1ProfileProperties;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ORSGraphInfoV1ProfilePropertiesBuilderTest {

    static final String INITIAL_STRING = "initialValue";
    static final String OVERRIDE_STRING = "overrideValue";
    static final Double INITIAL_DOUBLE = 123d;
    static final Double OVERRIDE_DOUBLE = 321d;
    static final Integer INITIAL_INTEGER = 123;
    static final Integer OVERRIDE_INTEGER = 321;

    @NotNull
    private static ORSGraphInfoV1ProfileProperties buildDefaultWithOverride(EngineProperties.ProfileProperties overrideProperties) {
        EngineProperties.ProfileProperties initialProperties = getInitialProfileProperties();
        ORSGraphInfoV1ProfileProperties opp = ORSGraphInfoV1ProfilePropertiesBuilder
                .from(initialProperties)
                .overrideWith(overrideProperties)
                .build();
        assertNotNull(opp);
        return opp;
    }

    @NotNull
    private static EngineProperties.ProfileProperties getInitialProfileProperties() {
        EngineProperties.ProfileProperties profileProperties = new EngineProperties.ProfileProperties();
        profileProperties.setProfile(INITIAL_STRING);
        profileProperties.setMaximumDistance(INITIAL_DOUBLE);
        profileProperties.setMaximumWayPoints(INITIAL_INTEGER);
        profileProperties.setEncoderOptions(Map.of(INITIAL_STRING, INITIAL_STRING));
        profileProperties.setEnabled(true);
        profileProperties.setElevation(false);
        return profileProperties;
    }

    private static void assertInitialProperties(ORSGraphInfoV1ProfileProperties opp) {
        assertNotNull(opp);
        assertEquals(getInitialProfileProperties().asORSGraphInfoV1ProfileProperties(), opp);

        //Initially set and not set String properties
        assertEquals(INITIAL_STRING, opp.profile());
        assertNull(opp.gtfsFile());

        //Initially set and not set Double properties
        assertEquals(INITIAL_DOUBLE, opp.maximumDistance());
        assertNull(opp.maximumDistanceDynamicWeights());

        //Initially set and not set Integer properties
        assertEquals(INITIAL_INTEGER, opp.maximumWayPoints());
        assertNull(opp.maximumSnappingRadius());

        //Initially set and not set Map properties
        assertEquals(INITIAL_STRING, opp.encoderOptions().get(INITIAL_STRING));
        assertNull(opp.preparation());

        //Initially set and not set Boolean properties
        assertEquals(true, opp.enabled());
        assertEquals(false, opp.elevation());
        assertNull(opp.traffic());
        assertNull(opp.instructions());
        assertNull(opp.optimize());
    }

    @Test
    public void from_null() {
        assertThrows(NullPointerException.class, () -> ORSGraphInfoV1ProfilePropertiesBuilder.from(null).build());
    }

    @Test
    public void from() {
        EngineProperties.ProfileProperties profileProperties = getInitialProfileProperties();
        ORSGraphInfoV1ProfileProperties opp = ORSGraphInfoV1ProfilePropertiesBuilder.from(profileProperties).build();
        assertInitialProperties(opp);
    }

    @Test
    public void overrideWith_emptyProperties() {
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(new EngineProperties.ProfileProperties());
        assertInitialProperties(opp);
    }

    @Test
    public void overrideStringWithUnsetString() {
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(new EngineProperties.ProfileProperties());
        assertEquals(INITIAL_STRING, opp.profile());
    }

    @Test
    public void overrideStringWithString() {
        EngineProperties.ProfileProperties overrideProperties = new EngineProperties.ProfileProperties();
        overrideProperties.setProfile(OVERRIDE_STRING);
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(overrideProperties);
        assertEquals(OVERRIDE_STRING, opp.profile());
    }

    @Test
    public void overrideUnsetStringWithString() {
        EngineProperties.ProfileProperties overrideProperties = new EngineProperties.ProfileProperties();
        overrideProperties.setGtfsFile(OVERRIDE_STRING);
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(overrideProperties);
        assertEquals(OVERRIDE_STRING, opp.gtfsFile());
    }

    @Test
    public void overrideDoubleWithUnsetDouble() {
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(new EngineProperties.ProfileProperties());
        assertEquals(INITIAL_DOUBLE, opp.maximumDistance());
    }

    @Test
    public void overrideDoubleWithDouble() {
        EngineProperties.ProfileProperties overrideProperties = new EngineProperties.ProfileProperties();
        overrideProperties.setMaximumDistance(OVERRIDE_DOUBLE);
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(overrideProperties);
        assertEquals(OVERRIDE_DOUBLE, opp.maximumDistance());
    }

    @Test
    public void overrideUnsetDoubleWithDouble() {
        EngineProperties.ProfileProperties overrideProperties = new EngineProperties.ProfileProperties();
        overrideProperties.setMaximumDistanceDynamicWeights(OVERRIDE_DOUBLE);
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(overrideProperties);
        assertEquals(OVERRIDE_DOUBLE, opp.maximumDistanceDynamicWeights());
    }

    @Test
    public void overrideIntegerWithUnsetInteger() {
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(new EngineProperties.ProfileProperties());
        assertEquals(INITIAL_INTEGER, opp.maximumWayPoints());
    }

    @Test
    public void overrideIntegerWithInteger() {
        EngineProperties.ProfileProperties overrideProperties = new EngineProperties.ProfileProperties();
        overrideProperties.setMaximumWayPoints(OVERRIDE_INTEGER);
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(overrideProperties);
        assertEquals(OVERRIDE_INTEGER, opp.maximumWayPoints());
    }

    @Test
    public void overrideUnsetIntegerWithInteger() {
        EngineProperties.ProfileProperties overrideProperties = new EngineProperties.ProfileProperties();
        overrideProperties.setMaximumSnappingRadius(OVERRIDE_INTEGER);
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(overrideProperties);
        assertEquals(OVERRIDE_INTEGER, opp.maximumSnappingRadius());
    }

    @Test
    public void overrideMapWithUnsetMap() {
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(new EngineProperties.ProfileProperties());
        assertEquals(INITIAL_STRING, opp.encoderOptions().get(INITIAL_STRING));
    }

    @Test
    public void overrideMapWithMap() {
        EngineProperties.ProfileProperties overrideProperties = new EngineProperties.ProfileProperties();
        overrideProperties.setEncoderOptions(Map.of(INITIAL_STRING, OVERRIDE_STRING));
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(overrideProperties);
        assertEquals(OVERRIDE_STRING, opp.encoderOptions().get(INITIAL_STRING));
    }

    @Test
    public void overrideUnsetMapWithMap() {
        EngineProperties.ProfileProperties overrideProperties = new EngineProperties.ProfileProperties();
        overrideProperties.setPreparation(Map.of(INITIAL_STRING, OVERRIDE_STRING));
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(overrideProperties);
        assertEquals(OVERRIDE_STRING, opp.preparation().get(INITIAL_STRING));
    }

    @Test
    public void overrideBooleanWithUnsetBoolean() {
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(new EngineProperties.ProfileProperties());
        assertTrue(opp.enabled());
        assertFalse(opp.elevation());
    }

    @Test
    public void overrideBooleanWithBoolean() {
        EngineProperties.ProfileProperties overrideProperties = new EngineProperties.ProfileProperties();
        overrideProperties.setEnabled(false);
        overrideProperties.setElevation(true);
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(overrideProperties);
        assertFalse(opp.enabled());
        assertTrue(opp.elevation());
    }

    @Test
    public void overrideUnsetBooleanWithBoolean() {
        EngineProperties.ProfileProperties overrideProperties = new EngineProperties.ProfileProperties();
        overrideProperties.setInstructions(true);
        overrideProperties.setOptimize(false);
        ORSGraphInfoV1ProfileProperties opp = buildDefaultWithOverride(overrideProperties);
        assertTrue(opp.instructions());
        assertFalse(opp.optimize());
    }
}