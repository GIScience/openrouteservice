package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.DataAccessEnum;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class DefaultElevationPropertiesTest {
    @Test
    void defaultConstructorSetsCorrectDefaults() {
        DefaultElevationProperties propertiesEmptyConstructor = new DefaultElevationProperties();
        assertNull(propertiesEmptyConstructor.getPreprocessed());
        assertNull(propertiesEmptyConstructor.getDataAccess());
        assertNull(propertiesEmptyConstructor.getCacheClear());
        assertNull(propertiesEmptyConstructor.getProvider());
        assertNull(propertiesEmptyConstructor.getCachePath());
        propertiesEmptyConstructor = new DefaultElevationProperties(false);
        assertNull(propertiesEmptyConstructor.getPreprocessed());
        assertNull(propertiesEmptyConstructor.getDataAccess());
        assertNull(propertiesEmptyConstructor.getCacheClear());
        assertNull(propertiesEmptyConstructor.getProvider());
        assertNull(propertiesEmptyConstructor.getCachePath());
    }

    @Test
    void constructorWithTrueSetsCorrectDefaults() {
        DefaultElevationProperties properties = new DefaultElevationProperties(true);
        assertFalse(properties.getPreprocessed());
        assertEquals(DataAccessEnum.MMAP, properties.getDataAccess());
        assertFalse(properties.getCacheClear());
        assertEquals("multi", properties.getProvider());
        assertEquals(Paths.get("./elevation_cache"), properties.getCachePath());
    }

}