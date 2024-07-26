package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultEncoderOptionsPropertiesTest {

    @Test
    void defaultConstructorSetsCorrectDefaults() {
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties();
        assertFalse(properties.getBlockFords());
        assertTrue(properties.getTurnCosts());
        assertFalse(properties.getConsiderElevation());
        assertFalse(properties.getUseAcceleration());
        assertFalse(properties.getConditionalAccess());
        assertNull(properties.getMaximumGradeLevel());
        assertNull(properties.getPreferredSpeedFactor());
        assertNull(properties.getProblematicSpeedFactor());
        assertFalse(properties.getConditionalSpeed());
    }

    @Test
    void constructorWithEncoderNameSetsCorrectDefaultsForDrivingCar() {
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties(EncoderNameEnum.DRIVING_CAR);
        assertTrue(properties.getTurnCosts());
        assertFalse(properties.getBlockFords());
        assertTrue(properties.getUseAcceleration());
        assertFalse(properties.getConsiderElevation());

        // Defaults
        assertFalse(properties.getConditionalAccess());
        assertNull(properties.getMaximumGradeLevel());
        assertNull(properties.getPreferredSpeedFactor());
        assertNull(properties.getProblematicSpeedFactor());
        assertFalse(properties.getConditionalSpeed());
    }

    @Test
    void constructorWithEncoderNameSetsCorrectDefaultsForDrivingHGV() {
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties(EncoderNameEnum.DRIVING_HGV);
        assertTrue(properties.getTurnCosts());
        assertFalse(properties.getBlockFords());
        assertTrue(properties.getUseAcceleration());

        // Defaults
        assertFalse(properties.getConsiderElevation());
        assertFalse(properties.getConditionalAccess());
        assertNull(properties.getMaximumGradeLevel());
        assertNull(properties.getPreferredSpeedFactor());
        assertNull(properties.getProblematicSpeedFactor());
        assertFalse(properties.getConditionalSpeed());
    }

    @Test
    void constructorWithEncoderNameSetsCorrectDefaultsForCyclingRegular() {
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties(EncoderNameEnum.CYCLING_REGULAR);
        assertTrue(properties.getConsiderElevation());
        assertTrue(properties.getTurnCosts());
        assertFalse(properties.getBlockFords());

        // Defaults
        assertFalse(properties.getUseAcceleration());
        assertFalse(properties.getConditionalAccess());
        assertNull(properties.getMaximumGradeLevel());
        assertNull(properties.getPreferredSpeedFactor());
        assertNull(properties.getProblematicSpeedFactor());
        assertFalse(properties.getConditionalSpeed());
    }

    @Test
    void constructorWithEncoderNameSetsCorrectDefaultsForFootWalking() {
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties(EncoderNameEnum.FOOT_WALKING);
        assertFalse(properties.getBlockFords());

        // Defaults
        assertTrue(properties.getTurnCosts());
        assertFalse(properties.getConsiderElevation());
        assertFalse(properties.getUseAcceleration());
        assertFalse(properties.getConditionalAccess());
        assertNull(properties.getMaximumGradeLevel());
        assertNull(properties.getPreferredSpeedFactor());
        assertNull(properties.getProblematicSpeedFactor());
        assertFalse(properties.getConditionalSpeed());
    }

    @Test
    void constructorWithNullEncoderNameSetsUnknownDefaults() {
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties(null);
        assertFalse(properties.getBlockFords());
        assertTrue(properties.getTurnCosts());
        assertFalse(properties.getConsiderElevation());
        assertFalse(properties.getUseAcceleration());
        assertFalse(properties.getConditionalAccess());
        assertNull(properties.getMaximumGradeLevel());
        assertNull(properties.getPreferredSpeedFactor());
        assertNull(properties.getProblematicSpeedFactor());
        assertFalse(properties.getConditionalSpeed());
    }
}