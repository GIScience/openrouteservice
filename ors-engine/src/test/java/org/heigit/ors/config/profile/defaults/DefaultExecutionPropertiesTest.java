package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultExecutionPropertiesTest {

    @Test
    void testDefaultExecutionProperties() {
        DefaultExecutionProperties defaultExecutionProperties = new DefaultExecutionProperties();
        assertEquals(8, defaultExecutionProperties.getMethods().getLm().getActiveLandmarks());
        assertEquals(6, defaultExecutionProperties.getMethods().getCore().getActiveLandmarks());
    }

    @Test
    void testDefaultExecutionPropertiesWithDrivingCar() {
        DefaultExecutionProperties defaultExecutionProperties = new DefaultExecutionProperties(EncoderNameEnum.DRIVING_CAR);
        assertEquals(6, defaultExecutionProperties.getMethods().getLm().getActiveLandmarks());
        assertEquals(6, defaultExecutionProperties.getMethods().getCore().getActiveLandmarks());
    }

    @Test
    void testDefaultExecutionPropertiesWithDrivingHgv() {
        DefaultExecutionProperties defaultExecutionProperties = new DefaultExecutionProperties(EncoderNameEnum.DRIVING_HGV);
        assertEquals(8, defaultExecutionProperties.getMethods().getLm().getActiveLandmarks());
        assertEquals(6, defaultExecutionProperties.getMethods().getCore().getActiveLandmarks());
    }

    @Test
    void testDefaultExecutionPropertiesWithUnkown() {
        DefaultExecutionProperties defaultExecutionProperties = new DefaultExecutionProperties(EncoderNameEnum.UNKNOWN);
        assertEquals(8, defaultExecutionProperties.getMethods().getLm().getActiveLandmarks());
        assertEquals(6, defaultExecutionProperties.getMethods().getCore().getActiveLandmarks());
    }

    @Test
    void testDefaultExecutionPropertiesWithNull() {
        DefaultExecutionProperties defaultExecutionProperties = new DefaultExecutionProperties(null);
        assertEquals(8, defaultExecutionProperties.getMethods().getLm().getActiveLandmarks());
        assertEquals(6, defaultExecutionProperties.getMethods().getCore().getActiveLandmarks());
    }

    @Test
    void testDefaultExecutionPropertiesWithFootHiking() {
        DefaultExecutionProperties defaultExecutionProperties = new DefaultExecutionProperties(EncoderNameEnum.FOOT_HIKING);
        assertEquals(8, defaultExecutionProperties.getMethods().getLm().getActiveLandmarks());
        assertEquals(6, defaultExecutionProperties.getMethods().getCore().getActiveLandmarks());
    }
}