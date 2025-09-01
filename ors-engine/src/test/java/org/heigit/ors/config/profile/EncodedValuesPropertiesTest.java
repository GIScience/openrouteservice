package org.heigit.ors.config.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncodedValuesPropertiesTest {
    private EncodedValuesProperties encodedValuesProperties;

    @BeforeEach
    void setUp() {
        encodedValuesProperties = new EncodedValuesProperties();
    }

    @Test
    void isWaySurfaceReturnsCorrectValue() {
        assertNull(encodedValuesProperties.getWaySurface());

        encodedValuesProperties.setWaySurface(true);
        assertTrue(encodedValuesProperties.getWaySurface());

        encodedValuesProperties.setWaySurface(false);
        assertFalse(encodedValuesProperties.getWaySurface());
    }

    @Test
    void isWayTypeReturnsCorrectValue() {
        assertNull(encodedValuesProperties.getWayType());

        encodedValuesProperties.setWayType(true);
        assertTrue(encodedValuesProperties.getWayType());

        encodedValuesProperties.setWayType(false);
        assertFalse(encodedValuesProperties.getWayType());
    }
}