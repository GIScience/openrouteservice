package org.heigit.ors.config;

import org.heigit.ors.common.DataAccessEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ElevationPropertiesTest {
    private ElevationProperties elevationProperties;

    public static Stream<Arguments> providePreparationProperties() {
        return Stream.of(
                Arguments.of(DataAccessEnum.MMAP),
                Arguments.of(DataAccessEnum.RAM_STORE),
                Arguments.of(DataAccessEnum.MMAP_RO)
        );
    }

    @BeforeEach
    void setUp() {
        elevationProperties = new ElevationProperties();
    }

    @Test
    void isPreprocessedReturnsCorrectValue() {
        elevationProperties.setPreprocessed(true);
        assertTrue(elevationProperties.isPreprocessed());

        elevationProperties.setPreprocessed(false);
        assertFalse(elevationProperties.isPreprocessed());
    }

    @ParameterizedTest
    @MethodSource("providePreparationProperties")
    void isEmptyReturnsCorrectValue(DataAccessEnum dataAccessEnum) {
        elevationProperties.setDataAccess(dataAccessEnum);
        assertEquals(dataAccessEnum, elevationProperties.getDataAccess());
    }

    @Test
    void dataAccessReturnsCorrectValue() {
        elevationProperties.setDataAccess(DataAccessEnum.MMAP);
        assertEquals(DataAccessEnum.MMAP, elevationProperties.getDataAccess());

        elevationProperties.setDataAccess(DataAccessEnum.RAM_STORE);
        assertEquals(DataAccessEnum.RAM_STORE, elevationProperties.getDataAccess());
    }

    @Test
    void cacheClearReturnsCorrectValue() {
        elevationProperties.setCacheClear(true);
        assertTrue(elevationProperties.getCacheClear());

        elevationProperties.setCacheClear(false);
        assertFalse(elevationProperties.getCacheClear());
    }

    @Test
    void providerReturnsCorrectValue() {
        elevationProperties.setProvider("provider1");
        assertEquals("provider1", elevationProperties.getProvider());

        elevationProperties.setProvider("provider2");
        assertEquals("provider2", elevationProperties.getProvider());
    }

    @Test
    void cachePathReturnsCorrectValue() {
        Path path = Paths.get("/some/path");
        elevationProperties.setCachePath(path);
        assertEquals(path, elevationProperties.getCachePath());
    }

    @Test
    void cachePathHandlesNull() {
        elevationProperties.setCachePath(null);
        assertNull(elevationProperties.getCachePath());
    }

    @Test
    void copyPropertiesToEmptyTarget() {
        ElevationProperties source = new ElevationProperties();
        ElevationProperties target = new ElevationProperties();

        source.setPreprocessed(true);
        source.setDataAccess(DataAccessEnum.MMAP);
        source.setCacheClear(true);
        source.setProvider("provider1");
        source.setCachePath(Paths.get("/some/path"));

        // Prepare and check target
        target.setPreprocessed(false);
        assertNull(target.getDataAccess());
        assertNull(target.getCacheClear());
        assertNull(target.getProvider());
        assertNull(target.getCachePath());

        // Copy properties
        target.copyProperties(source, false);

        // Check target properties
        assertNotEquals(source, target);
        assertEquals(false, target.isPreprocessed());

        // Set null again to allow for a copy
        target.setPreprocessed(null);

        // Copy properties again
        target.copyProperties(source, false);

        assertEquals(source.getPreprocessed(), target.getPreprocessed());
        assertEquals(source.getDataAccess(), target.getDataAccess());
        assertEquals(source.getCacheClear(), target.getCacheClear());
        assertEquals(source.getProvider(), target.getProvider());
        assertEquals(source.getCachePath(), target.getCachePath());

        // Copy properties again with overwrite
        target.copyProperties(source, true);

        // Check source and target are equal
        assertEquals(source, target);
    }

    @Test
    void copyWithEmptySource() {
        ElevationProperties source = new ElevationProperties();
        ElevationProperties target = new ElevationProperties();

        target.setPreprocessed(true);
        target.setDataAccess(DataAccessEnum.MMAP);
        target.setCacheClear(true);
        target.setProvider("provider1");
        target.setCachePath(Paths.get("/some/path"));

        // Prepare and check source
        assertNull(source.isPreprocessed());
        assertNull(source.getDataAccess());
        assertNull(source.getCacheClear());
        assertNull(source.getProvider());
        assertNull(source.getCachePath());

        // Copy properties
        target.copyProperties(source, false);

        // Check target properties
        assertNotEquals(source, target);
        assertNotNull(target.isPreprocessed());
        assertNotNull(target.getDataAccess());
        assertNotNull(target.getCacheClear());
        assertNotNull(target.getProvider());
        assertNotNull(target.getCachePath());

        // Copy properties again with overwrite
        target.copyProperties(source, true);

        // Check source and target are still not equal, since null is never copied
        assertNotEquals(source, target);
    }

    @Test
    void copyPropertiesWithNullSource() {
        ElevationProperties target = new ElevationProperties();

        target.setPreprocessed(true);

        target.copyProperties(null, false);
        assertTrue(target.isPreprocessed());
    }
}