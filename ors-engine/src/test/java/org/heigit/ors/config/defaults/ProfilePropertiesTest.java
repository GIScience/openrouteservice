package org.heigit.ors.config.defaults;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.config.utils.PropertyUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ProfilePropertiesTest {

    public static Stream<Arguments> encoderAndExpectedKeysWithDefaults() {
        return Stream.of(
                Arguments.of(new DefaultProfilePropertiesBikeRegular(true), EncoderNameEnum.CYCLING_REGULAR),
                Arguments.of(new DefaultProfilePropertiesBikeElectric(true), EncoderNameEnum.CYCLING_ELECTRIC),
                Arguments.of(new DefaultProfilePropertiesBikeMountain(true), EncoderNameEnum.CYCLING_MOUNTAIN),
                Arguments.of(new DefaultProfilePropertiesBikeRoad(true), EncoderNameEnum.CYCLING_ROAD),
                Arguments.of(new DefaultProfilePropertiesCar(true), EncoderNameEnum.DRIVING_CAR),
                Arguments.of(new DefaultProfilePropertiesWalking(true), EncoderNameEnum.FOOT_WALKING),
                Arguments.of(new DefaultProfilePropertiesHiking(true), EncoderNameEnum.FOOT_HIKING),
                Arguments.of(new DefaultProfilePropertiesHgv(true), EncoderNameEnum.DRIVING_HGV),
                Arguments.of(new DefaultProfilePropertiesWheelchair(true), EncoderNameEnum.WHEELCHAIR),
                Arguments.of(new DefaultProfilePropertiesPublicTransport(true), EncoderNameEnum.PUBLIC_TRANSPORT)
        );
    }

    public static Stream<Arguments> propertyJsonAndExpectedKeys() {
        return Stream.of(
                Arguments.of("{\"enabled\": true}", EncoderNameEnum.UNKNOWN, new DefaultProfileProperties()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.UNKNOWN.getName() + "\", \"enabled\": true}", EncoderNameEnum.UNKNOWN, new DefaultProfileProperties()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.CYCLING_REGULAR.getName() + "\", \"enabled\": true}", EncoderNameEnum.CYCLING_REGULAR, new DefaultProfilePropertiesBikeRegular()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.CYCLING_ELECTRIC.getName() + "\", \"enabled\": true}", EncoderNameEnum.CYCLING_ELECTRIC, new DefaultProfilePropertiesBikeElectric()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.CYCLING_MOUNTAIN.getName() + "\", \"enabled\": true}", EncoderNameEnum.CYCLING_MOUNTAIN, new DefaultProfilePropertiesBikeMountain()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.CYCLING_ROAD.getName() + "\", \"enabled\": true}", EncoderNameEnum.CYCLING_ROAD, new DefaultProfilePropertiesBikeRoad()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.DRIVING_CAR.getName() + "\", \"enabled\": true}", EncoderNameEnum.DRIVING_CAR, new DefaultProfilePropertiesCar()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.FOOT_WALKING.getName() + "\", \"enabled\": true}", EncoderNameEnum.FOOT_WALKING, new DefaultProfilePropertiesWalking()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.FOOT_HIKING.getName() + "\", \"enabled\": true}", EncoderNameEnum.FOOT_HIKING, new DefaultProfilePropertiesHiking()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.DRIVING_HGV.getName() + "\", \"enabled\": true}", EncoderNameEnum.DRIVING_HGV, new DefaultProfilePropertiesHgv()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.WHEELCHAIR.getName() + "\", \"enabled\": true}", EncoderNameEnum.WHEELCHAIR, new DefaultProfilePropertiesWheelchair()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.PUBLIC_TRANSPORT.getName() + "\", \"enabled\": true}", EncoderNameEnum.PUBLIC_TRANSPORT, new DefaultProfilePropertiesPublicTransport())

        );
    }

    @Test
    void testDefaultConstructor() {
        ProfileProperties profileProperties = new HelperClass();
        assertNull(profileProperties.getEnabled());
        assertNull(profileProperties.getEncoderName());
        assertNull(profileProperties.getElevation());
        assertNull(profileProperties.getElevationSmoothing());
        assertNull(profileProperties.getEncoderFlagsSize());
        assertNull(profileProperties.getInstructions());
        assertNull(profileProperties.getOptimize());
        assertNull(profileProperties.getTraffic());
        assertNull(profileProperties.getInterpolateBridgesAndTunnels());
        assertNull(profileProperties.getForceTurnCosts());
        assertNull(profileProperties.getGraphPath());
        assertNull(profileProperties.getLocationIndexResolution());
        assertNull(profileProperties.getLocationIndexSearchIterations());
        assertNull(profileProperties.getGtfsFile());
        assertNull(profileProperties.getMaximumDistance());
        assertNull(profileProperties.getMaximumDistanceDynamicWeights());
        assertNull(profileProperties.getMaximumDistanceAvoidAreas());
        assertNull(profileProperties.getMaximumDistanceAlternativeRoutes());
        assertNull(profileProperties.getMaximumDistanceRoundTripRoutes());
        assertNull(profileProperties.getMaximumSpeedLowerBound());
        assertNull(profileProperties.getMaximumWayPoints());
        assertNull(profileProperties.getMaximumSnappingRadius());
        assertNull(profileProperties.getMaximumVisitedNodes());
    }

    @ParameterizedTest
    @MethodSource("encoderAndExpectedKeysWithDefaults")
    void getProfilesTypesWithVariousEncoderNames(ProfileProperties properties, EncoderNameEnum expectedEncoderName) {
        assertArrayEquals(new Integer[]{expectedEncoderName.getValue()}, properties.getProfilesTypes());
    }

    @Test
    void defaultPropertiesWithGlobalDefaults() {
        DefaultProfileProperties defaultProfileProperties = new DefaultProfileProperties(true);
        assertNull(defaultProfileProperties.getEncoderName());
        assertFalse(defaultProfileProperties.getEnabled());
        assertTrue(defaultProfileProperties.getElevation());
        assertTrue(defaultProfileProperties.getElevationSmoothing());
        assertEquals(8, defaultProfileProperties.getEncoderFlagsSize());
        assertTrue(defaultProfileProperties.getInstructions());
        assertFalse(defaultProfileProperties.getOptimize());
        assertFalse(defaultProfileProperties.getTraffic());
        assertTrue(defaultProfileProperties.getInterpolateBridgesAndTunnels());
        assertFalse(defaultProfileProperties.getForceTurnCosts());
        assertEquals(500, defaultProfileProperties.getLocationIndexResolution());
        assertEquals(4, defaultProfileProperties.getLocationIndexSearchIterations());
        assertEquals(100000d, defaultProfileProperties.getMaximumDistance());
        assertEquals(100000d, defaultProfileProperties.getMaximumDistanceDynamicWeights());
        assertEquals(100000d, defaultProfileProperties.getMaximumDistanceAvoidAreas());
        assertEquals(100000d, defaultProfileProperties.getMaximumDistanceAlternativeRoutes());
        assertEquals(100000d, defaultProfileProperties.getMaximumDistanceRoundTripRoutes());
        assertEquals(80d, defaultProfileProperties.getMaximumSpeedLowerBound());
        assertEquals(50, defaultProfileProperties.getMaximumWayPoints());
        assertEquals(400, defaultProfileProperties.getMaximumSnappingRadius());
        assertEquals(1000000, defaultProfileProperties.getMaximumVisitedNodes());
    }

    @ParameterizedTest
    @MethodSource("encoderAndExpectedKeysWithDefaults")
    void defaultPropertiesWithDefaultsAreSetCorrectly(ProfileProperties properties, EncoderNameEnum expectedEncoderName) {
        assertEquals(expectedEncoderName, properties.getEncoderName());
        assertNull(properties.getElevationSmoothing());
        assertNull(properties.getEncoderFlagsSize());
        assertNull(properties.getInstructions());
        assertNull(properties.getOptimize());
        assertNull(properties.getTraffic());
        assertNull(properties.getInterpolateBridgesAndTunnels());
        assertNull(properties.getForceTurnCosts());
        assertNull(properties.getLocationIndexResolution());
        assertNull(properties.getLocationIndexSearchIterations());
        assertNull(properties.getMaximumDistance());
        assertNull(properties.getMaximumDistanceDynamicWeights());
        assertNull(properties.getMaximumDistanceAvoidAreas());
        assertNull(properties.getMaximumDistanceAlternativeRoutes());
        assertNull(properties.getMaximumDistanceRoundTripRoutes());
        assertNull(properties.getMaximumSpeedLowerBound());
        assertNull(properties.getMaximumWayPoints());
        if (expectedEncoderName == EncoderNameEnum.WHEELCHAIR) {
            assertNull(properties.getEnabled());
            assertEquals(50, properties.getMaximumSnappingRadius());
            assertNull(properties.getElevation());
            assertNull(properties.getMaximumVisitedNodes());
        } else if (expectedEncoderName == EncoderNameEnum.PUBLIC_TRANSPORT) {
            assertFalse(properties.getEnabled());
            assertTrue(properties.getElevation());
            assertEquals(1000000, properties.getMaximumVisitedNodes());
            assertEquals(Path.of(""), properties.getGtfsFile());
            assertNull(properties.getMaximumSnappingRadius());
        } else {
            assertNull(properties.getEnabled());
            assertNull(properties.getElevation());
            assertNull(properties.getMaximumSnappingRadius());
            assertNull(properties.getMaximumVisitedNodes());
        }

    }

    @ParameterizedTest
    @MethodSource("encoderAndExpectedKeysWithDefaults")
    void defaultConstructorDefaultPropertiesBike(ProfileProperties properties, EncoderNameEnum expectedEncoderName) {
        assertEquals(expectedEncoderName, properties.getEncoderName());
        assertInstanceOf(ProfileProperties.class, properties);
        if (expectedEncoderName == EncoderNameEnum.PUBLIC_TRANSPORT)
            assertFalse(properties.getEnabled());
        else
            assertNull(properties.getEnabled());
    }

    @ParameterizedTest
    @MethodSource("propertyJsonAndExpectedKeys")
    void deSerializeProfileProperties(String profileJson, EncoderNameEnum expectedEncoderName, ProfileProperties profilePropertiesClass) throws JsonProcessingException, IllegalAccessException {
        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        // Step 3: Deserialize the JSON to an object
        ProfileProperties profileProperties = mapper.readValue(profileJson, ProfileProperties.class);
        assertTrue(profileProperties.getEnabled());
        assertInstanceOf(ProfileProperties.class, profileProperties);
        if (expectedEncoderName == EncoderNameEnum.UNKNOWN) {
            assertInstanceOf(DefaultProfileProperties.class, profileProperties);
            assertNull(profileProperties.getEncoderName());
            PropertyUtils.assertAllNull(profileProperties, new HashSet<>());
        } else {
            assertEquals(expectedEncoderName, profileProperties.getEncoderName());

        }

    }

    @ParameterizedTest
    @MethodSource("encoderAndExpectedKeysWithDefaults")
    void serializeProfileProperties(ProfileProperties profileProperties, EncoderNameEnum expectedEncoderName) throws JsonProcessingException {
        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String serializedJson = mapper.writeValueAsString(profileProperties);
        // Step 3: Deserialize the JSON to an object
        ProfileProperties deserializedProfileProperties = mapper.readValue(serializedJson, ProfileProperties.class);

        assertEquals(expectedEncoderName, deserializedProfileProperties.getEncoderName());
        assertInstanceOf(ProfileProperties.class, deserializedProfileProperties);
    }

    static class HelperClass extends ProfileProperties {
        public HelperClass() {
            super();
        }
    }

    static class HelperPropertiesClass extends ProfileProperties {
        public HelperPropertiesClass(EncoderNameEnum encoderName) {
            super(false, encoderName);
        }
    }

}