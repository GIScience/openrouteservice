package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPreparationPropertiesTest {

    @Test
    void defaultConstructorSetsDefaultValues() {
        DefaultPreparationProperties properties = new DefaultPreparationProperties();
        assertEquals(200, properties.getMinNetworkSize());
        assertEquals(200, properties.getMinOneWayNetworkSize());
        assertFalse(properties.getMethods().getCh().isEnabled());
        assertEquals("fastest", properties.getMethods().getCh().getWeightings());
        assertEquals(2, properties.getMethods().getCh().getThreads());
        assertEquals(2, properties.getMethods().getCh().getThreadsSave());
        assertTrue(properties.getMethods().getLm().isEnabled());
        assertEquals("recommended,shortest", properties.getMethods().getLm().getWeightings());
        assertEquals(2, properties.getMethods().getLm().getThreads());
        assertEquals(2, properties.getMethods().getLm().getThreadsSave());
        assertEquals(16, properties.getMethods().getLm().getLandmarks());
        assertFalse(properties.getMethods().getCore().isEnabled());
        assertEquals(2, properties.getMethods().getCore().getThreads());
        assertEquals(2, properties.getMethods().getCore().getThreadsSave());
        assertEquals("fastest,shortest", properties.getMethods().getCore().getWeightings());
        assertEquals(64, properties.getMethods().getCore().getLandmarks());
        assertEquals("highways;allow_all", properties.getMethods().getCore().getLmsets());
        assertFalse(properties.getMethods().getFastisochrones().isEnabled());
        assertEquals(2, properties.getMethods().getFastisochrones().getThreads());
        assertEquals(2, properties.getMethods().getFastisochrones().getThreadsSave());
        assertEquals("recommended,shortest", properties.getMethods().getFastisochrones().getWeightings());
    }

    @Test
    void constructorWithEncoderNameSetsValuesForDrivingCar() {
        DefaultPreparationProperties properties = new DefaultPreparationProperties(EncoderNameEnum.DRIVING_CAR);
        assertTrue(properties.getMethods().getCh().isEnabled());
        assertEquals("fastest", properties.getMethods().getCh().getWeightings());
        assertFalse(properties.getMethods().getLm().isEnabled());
        assertEquals("fastest,shortest", properties.getMethods().getLm().getWeightings());
        assertTrue(properties.getMethods().getCore().isEnabled());

        // Defaults
        assertEquals(200, properties.getMinNetworkSize());
        assertEquals(200, properties.getMinOneWayNetworkSize());
        assertEquals(2, properties.getMethods().getCh().getThreadsSave());
        assertEquals(2, properties.getMethods().getLm().getThreadsSave());
        assertEquals(16, properties.getMethods().getLm().getLandmarks());
        assertEquals(2, properties.getMethods().getCore().getThreadsSave());
        assertEquals("fastest,shortest", properties.getMethods().getCore().getWeightings());
        assertEquals(64, properties.getMethods().getCore().getLandmarks());
        assertEquals("highways;allow_all", properties.getMethods().getCore().getLmsets());
        assertFalse(properties.getMethods().getFastisochrones().isEnabled());
        assertEquals(2, properties.getMethods().getFastisochrones().getThreadsSave());
        assertEquals("recommended,shortest", properties.getMethods().getFastisochrones().getWeightings());
    }

    @Test
    void constructorWithEncoderNameSetsValuesForDrivingHGV() {
        DefaultPreparationProperties properties = new DefaultPreparationProperties(EncoderNameEnum.DRIVING_HGV);
        assertTrue(properties.getMethods().getCh().isEnabled());
        assertEquals("recommended", properties.getMethods().getCh().getWeightings());
        assertTrue(properties.getMethods().getCore().isEnabled());
        assertEquals("recommended,shortest", properties.getMethods().getCore().getWeightings());

        // Defaults
        assertEquals(200, properties.getMinNetworkSize());
        assertEquals(200, properties.getMinOneWayNetworkSize());
        assertEquals(2, properties.getMethods().getCh().getThreadsSave());
        assertTrue(properties.getMethods().getLm().isEnabled());
        assertEquals("recommended,shortest", properties.getMethods().getLm().getWeightings());
        assertEquals(2, properties.getMethods().getLm().getThreadsSave());
        assertEquals(16, properties.getMethods().getLm().getLandmarks());
        assertEquals(2, properties.getMethods().getCore().getThreadsSave());
        assertEquals(64, properties.getMethods().getCore().getLandmarks());
        assertEquals("highways;allow_all", properties.getMethods().getCore().getLmsets());
        assertFalse(properties.getMethods().getFastisochrones().isEnabled());
        assertEquals(2, properties.getMethods().getFastisochrones().getThreadsSave());
        assertEquals("recommended,shortest", properties.getMethods().getFastisochrones().getWeightings());

    }

    @Test
    void constructorWithNullEncoderNameSetsDefaultValues() {
        DefaultPreparationProperties properties = new DefaultPreparationProperties(null);
        assertEquals(200, properties.getMinNetworkSize());
        assertEquals(200, properties.getMinOneWayNetworkSize());
        assertFalse(properties.getMethods().getCh().isEnabled());
        assertEquals("fastest", properties.getMethods().getCh().getWeightings());
        assertEquals(2, properties.getMethods().getCh().getThreadsSave());
        assertTrue(properties.getMethods().getLm().isEnabled());
        assertEquals("recommended,shortest", properties.getMethods().getLm().getWeightings());
        assertEquals(2, properties.getMethods().getLm().getThreadsSave());
        assertEquals(16, properties.getMethods().getLm().getLandmarks());
        assertFalse(properties.getMethods().getCore().isEnabled());
        assertEquals(2, properties.getMethods().getCore().getThreadsSave());
        assertEquals("fastest,shortest", properties.getMethods().getCore().getWeightings());
        assertEquals(64, properties.getMethods().getCore().getLandmarks());
        assertEquals("highways;allow_all", properties.getMethods().getCore().getLmsets());
        assertFalse(properties.getMethods().getFastisochrones().isEnabled());
        assertEquals(2, properties.getMethods().getFastisochrones().getThreadsSave());
        assertEquals("recommended,shortest", properties.getMethods().getFastisochrones().getWeightings());
    }

    @Test
    void constructorWithUnknownEncoderNameSetsDefaultValues() {
        DefaultPreparationProperties properties = new DefaultPreparationProperties(EncoderNameEnum.UNKNOWN);
        assertEquals(200, properties.getMinNetworkSize());
        assertEquals(200, properties.getMinOneWayNetworkSize());
        assertFalse(properties.getMethods().getCh().isEnabled());
        assertEquals("fastest", properties.getMethods().getCh().getWeightings());
        assertEquals(2, properties.getMethods().getCh().getThreadsSave());
        assertTrue(properties.getMethods().getLm().isEnabled());
        assertEquals("recommended,shortest", properties.getMethods().getLm().getWeightings());
        assertEquals(2, properties.getMethods().getLm().getThreadsSave());
        assertEquals(16, properties.getMethods().getLm().getLandmarks());
        assertFalse(properties.getMethods().getCore().isEnabled());
        assertEquals(2, properties.getMethods().getCore().getThreadsSave());
        assertEquals("fastest,shortest", properties.getMethods().getCore().getWeightings());
        assertEquals(64, properties.getMethods().getCore().getLandmarks());
        assertEquals("highways;allow_all", properties.getMethods().getCore().getLmsets());
        assertFalse(properties.getMethods().getFastisochrones().isEnabled());
        assertEquals(2, properties.getMethods().getFastisochrones().getThreadsSave());
        assertEquals("recommended,shortest", properties.getMethods().getFastisochrones().getWeightings());
    }
}