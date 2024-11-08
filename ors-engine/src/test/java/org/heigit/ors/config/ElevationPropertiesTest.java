package org.heigit.ors.config;

import org.heigit.ors.common.DataAccessEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
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
        assertTrue(elevationProperties.getPreprocessed());

        elevationProperties.setPreprocessed(false);
        assertFalse(elevationProperties.getPreprocessed());
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
    void cachePathReturnsNull() {
        elevationProperties.setCachePath(null);
        assertNull(elevationProperties.getCachePath());
    }

    @Test
    void cachePathReturnsAbsolutePath() {
        Path expectedCachePath = Path.of("cache");
        elevationProperties.setCachePath(expectedCachePath);
        assertEquals(expectedCachePath.toAbsolutePath(), elevationProperties.getCachePath());
    }
}