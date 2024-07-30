package org.heigit.ors.config.profile.defaults;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ProfilePropertiesTest {

    public static Stream<Arguments> encoderAndExpectedKeys() {
        return Stream.of(
                Arguments.of(new DefaultProfileProperties(), EncoderNameEnum.UNKNOWN),
                Arguments.of(new DefaultProfilePropertiesBikeRegular(), EncoderNameEnum.CYCLING_REGULAR),
                Arguments.of(new DefaultProfilePropertiesBikeElectric(), EncoderNameEnum.CYCLING_ELECTRIC),
                Arguments.of(new DefaultProfilePropertiesBikeMountain(), EncoderNameEnum.CYCLING_MOUNTAIN),
                Arguments.of(new DefaultProfilePropertiesBikeRoad(), EncoderNameEnum.CYCLING_ROAD),
                Arguments.of(new DefaultProfilePropertiesCar(), EncoderNameEnum.DRIVING_CAR),
                Arguments.of(new DefaultProfilePropertiesWalking(), EncoderNameEnum.FOOT_WALKING),
                Arguments.of(new DefaultProfilePropertiesHiking(), EncoderNameEnum.FOOT_HIKING),
                Arguments.of(new DefaultProfilePropertiesHgv(), EncoderNameEnum.DRIVING_HGV),
                Arguments.of(new DefaultProfilePropertiesWheelchair(), EncoderNameEnum.WHEELCHAIR),
                Arguments.of(new DefaultProfilePropertiesPublicTransport(), EncoderNameEnum.PUBLIC_TRANSPORT)

        );
    }

    public static Stream<Arguments> propertyJsonAndExpectedKeys() {
        return Stream.of(
                Arguments.of("{}", EncoderNameEnum.UNKNOWN, new DefaultProfileProperties()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.UNKNOWN.getName() + "\"}", EncoderNameEnum.UNKNOWN, new DefaultProfileProperties()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.CYCLING_REGULAR.getName() + "\"}", EncoderNameEnum.CYCLING_REGULAR, new DefaultProfilePropertiesBikeRegular()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.CYCLING_ELECTRIC.getName() + "\"}", EncoderNameEnum.CYCLING_ELECTRIC, new DefaultProfilePropertiesBikeElectric()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.CYCLING_MOUNTAIN.getName() + "\"}", EncoderNameEnum.CYCLING_MOUNTAIN, new DefaultProfilePropertiesBikeMountain()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.CYCLING_ROAD.getName() + "\"}", EncoderNameEnum.CYCLING_ROAD, new DefaultProfilePropertiesBikeRoad()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.DRIVING_CAR.getName() + "\"}", EncoderNameEnum.DRIVING_CAR, new DefaultProfilePropertiesCar()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.FOOT_WALKING.getName() + "\"}", EncoderNameEnum.FOOT_WALKING, new DefaultProfilePropertiesWalking()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.FOOT_HIKING.getName() + "\"}", EncoderNameEnum.FOOT_HIKING, new DefaultProfilePropertiesHiking()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.DRIVING_HGV.getName() + "\"}", EncoderNameEnum.DRIVING_HGV, new DefaultProfilePropertiesHgv()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.WHEELCHAIR.getName() + "\"}", EncoderNameEnum.WHEELCHAIR, new DefaultProfilePropertiesWheelchair()),
                Arguments.of("{\"encoder_name\":\"" + EncoderNameEnum.PUBLIC_TRANSPORT.getName() + "\"}", EncoderNameEnum.PUBLIC_TRANSPORT, new DefaultProfilePropertiesPublicTransport())

        );
    }

    @ParameterizedTest
    @MethodSource("encoderAndExpectedKeys")
    void defaultPropertiesAreSetCorrectly(ProfileProperties properties, EncoderNameEnum expectedEncoderName) {
        assertEquals(expectedEncoderName, properties.getEncoderName());
        assertFalse(properties.getEnabled());
        assertTrue(properties.getElevation());
        assertTrue(properties.getElevationSmoothing());
        assertEquals(8, properties.getEncoderFlagsSize());
        assertTrue(properties.getInstructions());
        assertFalse(properties.getOptimize());
        assertFalse(properties.getTraffic());
        assertTrue(properties.getInterpolateBridgesAndTunnels());
        assertFalse(properties.getForceTurnCosts());
        assertEquals(500, properties.getLocationIndexResolution());
        assertEquals(4, properties.getLocationIndexSearchIterations());
        assertEquals(100000d, properties.getMaximumDistance());
        assertEquals(100000d, properties.getMaximumDistanceDynamicWeights());
        assertEquals(100000d, properties.getMaximumDistanceAvoidAreas());
        assertEquals(100000d, properties.getMaximumDistanceAlternativeRoutes());
        assertEquals(100000d, properties.getMaximumDistanceRoundTripRoutes());
        assertEquals(80d, properties.getMaximumSpeedLowerBound());
        assertEquals(50, properties.getMaximumWayPoints());

        if (expectedEncoderName == EncoderNameEnum.WHEELCHAIR)
            assertEquals(50, properties.getMaximumSnappingRadius());
        else
            assertEquals(400, properties.getMaximumSnappingRadius());

        assertEquals(1000000, properties.getMaximumVisitedNodes());
    }

    @ParameterizedTest
    @MethodSource("encoderAndExpectedKeys")
    void defaultConstructorDefaultPropertiesBike(ProfileProperties properties, EncoderNameEnum expectedEncoderName) {
        assertEquals(expectedEncoderName, properties.getEncoderName());
        assertInstanceOf(ProfileProperties.class, properties);
        assertInstanceOf(DefaultProfileProperties.class, properties);
        assertFalse(properties.getEnabled());
    }

    @ParameterizedTest
    @MethodSource("propertyJsonAndExpectedKeys")
    void deSerializeProfileProperties(String profileJson, EncoderNameEnum expectedEncoderName, ProfileProperties profilePropertiesClass) throws JsonProcessingException {
        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        // Step 3: Deserialize the JSON to an object
        ProfileProperties profileProperties = mapper.readValue(profileJson, ProfileProperties.class);
        assertEquals(expectedEncoderName, profileProperties.getEncoderName());
        assertFalse(profileProperties.getEnabled());
        assertInstanceOf(ProfileProperties.class, profileProperties);
        assertInstanceOf(DefaultProfileProperties.class, profileProperties);
    }

    @ParameterizedTest
    @MethodSource("encoderAndExpectedKeys")
    void serializeProfileProperties(ProfileProperties profileProperties, EncoderNameEnum expectedEncoderName) throws JsonProcessingException {
        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String serializedJson = mapper.writeValueAsString(profileProperties);
        // Step 3: Deserialize the JSON to an object
        ProfileProperties deserializedProfileProperties = mapper.readValue(serializedJson, ProfileProperties.class);

        assertEquals(expectedEncoderName, deserializedProfileProperties.getEncoderName());
        assertInstanceOf(ProfileProperties.class, deserializedProfileProperties);
    }

}