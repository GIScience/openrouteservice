package org.heigit.ors.config.profile;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class EncoderOptionsPropertiesTest {

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
}