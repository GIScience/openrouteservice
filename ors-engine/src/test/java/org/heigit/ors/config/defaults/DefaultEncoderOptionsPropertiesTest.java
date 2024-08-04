package org.heigit.ors.config.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultEncoderOptionsPropertiesTest {

    @Test
    void defaultConstructorSetsCorrectDefaults() {
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties(true, EncoderNameEnum.UNKNOWN);
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
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties(true, EncoderNameEnum.DRIVING_CAR);
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
    void constructorWithEncoderNameSetsCorrectDefaultsForDrivingHGVWithGlobalDefaults() {
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties(true, EncoderNameEnum.DRIVING_HGV);
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
    void constructorWithEncoderNameSetsCorrectDefaultsForDrivingHGVWithoutGlobalDefaults() {
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties(EncoderNameEnum.DRIVING_HGV);
        assertTrue(properties.getTurnCosts());
        assertFalse(properties.getBlockFords());
        assertTrue(properties.getUseAcceleration());

        // Defaults
        assertNull(properties.getConsiderElevation());
        assertNull(properties.getConditionalAccess());
        assertNull(properties.getMaximumGradeLevel());
        assertNull(properties.getPreferredSpeedFactor());
        assertNull(properties.getProblematicSpeedFactor());
        assertNull(properties.getConditionalSpeed());
    }


    @Test
    void constructorWithEncoderNameSetsCorrectDefaultsForCyclingRegularWithGlobalDefaults() {
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties(true, EncoderNameEnum.CYCLING_REGULAR);
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
    void constructorWithEncoderNameSetsCorrectDefaultsForCyclingRegularWithoutGlobalDefaults() {
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties(EncoderNameEnum.CYCLING_REGULAR);
        assertTrue(properties.getConsiderElevation());
        assertTrue(properties.getTurnCosts());
        assertFalse(properties.getBlockFords());

        // Defaults
        assertNull(properties.getUseAcceleration());
        assertNull(properties.getConditionalAccess());
        assertNull(properties.getMaximumGradeLevel());
        assertNull(properties.getPreferredSpeedFactor());
        assertNull(properties.getProblematicSpeedFactor());
        assertNull(properties.getConditionalSpeed());
    }


    @Test
    void constructorWithEncoderNameSetsCorrectDefaultsForFootWalkingWithGlobalDefaults() {
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties(true, EncoderNameEnum.FOOT_WALKING);
        assertFalse(properties.getBlockFords());

        // Defaults
        assertFalse(properties.getTurnCosts());
        assertFalse(properties.getConsiderElevation());
        assertFalse(properties.getUseAcceleration());
        assertFalse(properties.getConditionalAccess());
        assertNull(properties.getMaximumGradeLevel());
        assertNull(properties.getPreferredSpeedFactor());
        assertNull(properties.getProblematicSpeedFactor());
        assertFalse(properties.getConditionalSpeed());
    }

    @Test
    void constructorWithEncoderNameSetsCorrectDefaultsForFootWalkingWithoutGlobalDefaults() {
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties(EncoderNameEnum.FOOT_WALKING);
        assertFalse(properties.getBlockFords());

        // Defaults
        assertFalse(properties.getTurnCosts());
        assertNull(properties.getConsiderElevation());
        assertNull(properties.getUseAcceleration());
        assertNull(properties.getConditionalAccess());
        assertNull(properties.getMaximumGradeLevel());
        assertNull(properties.getPreferredSpeedFactor());
        assertNull(properties.getProblematicSpeedFactor());
        assertNull(properties.getConditionalSpeed());
    }

    @Test
    void constructorWithNullEncoderNameSetsUnknownDefaults() {
        DefaultEncoderOptionsProperties properties = new DefaultEncoderOptionsProperties(true);
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