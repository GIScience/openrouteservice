package org.heigit.ors.config.profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class EncoderOptionsPropertiesTest {

    private EncoderOptionsProperties source;
    private EncoderOptionsProperties target;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        source = new EncoderOptionsProperties();
        source.setBlockFords(true);
        source.setConsiderElevation(true);
        source.setTurnCosts(true);
        source.setUseAcceleration(true);
        source.setMaximumGradeLevel(10);
        source.setPreferredSpeedFactor(1.5);
        source.setProblematicSpeedFactor(2.5);
        source.setConditionalAccess(true);
        source.setConditionalSpeed(true);

        target = new EncoderOptionsProperties();
        target.setBlockFords(false);
        target.setConsiderElevation(false);
        target.setTurnCosts(false);
        target.setUseAcceleration(false);
        target.setMaximumGradeLevel(20);
        target.setPreferredSpeedFactor(2.5);
        target.setProblematicSpeedFactor(3.5);
        target.setConditionalAccess(false);
        target.setConditionalSpeed(false);
    }

    @Test
    void testConstructor() throws IllegalAccessException {
        EncoderOptionsProperties properties = new EncoderOptionsProperties();
        // They must be null to be able to distinguish between set and not set
        Field[] declaredFields = properties.getClass().getDeclaredFields();
        assertEquals(9, declaredFields.length);
        for (Field field : declaredFields) {
            field.setAccessible(true);
            assertNull(field.get(properties));
        }
        assertTrue(properties.isEmpty());
    }


    @Test
    void testCopyPropertiesWithoutOverwrite() {
        target.copyProperties(source, false);

        assertNotEquals(source, target);
        assertFalse(target.getBlockFords());
        assertFalse(target.getConsiderElevation());
        assertFalse(target.getTurnCosts());
        assertFalse(target.getUseAcceleration());
        assertEquals(20, target.getMaximumGradeLevel());
        assertEquals(2.5, target.getPreferredSpeedFactor());
        assertEquals(3.5, target.getProblematicSpeedFactor());
        assertFalse(target.getConditionalAccess());
        assertFalse(target.getConditionalSpeed());
    }

    @Test
    void testCopyPropertiesWithOverwrite() {
        assertNotEquals(source, target);
        target.copyProperties(source, true);

        assertEquals(source, target);
    }

    @Test
    void testCopyPropertiesWithNullSource() {
        target.copyProperties(null, true);

        assertFalse(target.getBlockFords());
        assertFalse(target.getConsiderElevation());
        assertFalse(target.getTurnCosts());
        assertFalse(target.getUseAcceleration());
        assertEquals(20, target.getMaximumGradeLevel());
        assertEquals(2.5, target.getPreferredSpeedFactor());
        assertEquals(3.5, target.getProblematicSpeedFactor());
        assertFalse(target.getConditionalAccess());
        assertFalse(target.getConditionalSpeed());
    }

    @Test
    void testCopyPropertiesWithEmptySource() {
        EncoderOptionsProperties source = new EncoderOptionsProperties();
        assertNotEquals(source, target);
        target.copyProperties(source, true);

        assertNotEquals(source, target);
    }

    @Test
    void testCopyPropertiesWithEmptyTarget() {
        EncoderOptionsProperties target = new EncoderOptionsProperties();
        assertNotEquals(source, target);
        target.copyProperties(source, false);

        assertEquals(source, target);
    }
}