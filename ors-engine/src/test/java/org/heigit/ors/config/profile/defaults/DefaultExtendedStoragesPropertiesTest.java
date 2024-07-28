package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.storages.ExtendedStorageWayCategory;
import org.heigit.ors.config.profile.storages.ExtendedStorageWaySurfaceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

class DefaultExtendedStoragesPropertiesTest {

    // Define the correct extended storages for the given encoder name
    static Object[] correctExtendedStoragesPerProfile() {
        return new Object[]{
                new Object[]{"DRIVING_CAR", new String[]{"WayCategory", "WaySurfaceType", "HeavyVehicle", "RoadAccessRestrictions"}},
                new Object[]{"DRIVING_HGV", new String[]{"WayCategory", "WaySurfaceType", "HeavyVehicle"}},
                new Object[]{"CYCLING_REGULAR", new String[]{"WayCategory", "WaySurfaceType", "HillIndex", "TrailDifficulty"}},
                new Object[]{"CYCLING_MOUNTAIN", new String[]{"WayCategory", "WaySurfaceType", "HillIndex", "TrailDifficulty"}},
                new Object[]{"CYCLING_ROAD", new String[]{"WayCategory", "WaySurfaceType", "HillIndex", "TrailDifficulty"}},
                new Object[]{"CYCLING_ELECTRIC", new String[]{"WayCategory", "WaySurfaceType", "HillIndex", "TrailDifficulty"}},
                new Object[]{"FOOT_WALKING", new String[]{"WayCategory", "WaySurfaceType", "HillIndex", "TrailDifficulty"}},
                new Object[]{"FOOT_HIKING", new String[]{"WayCategory", "WaySurfaceType", "HillIndex", "TrailDifficulty"}},
                new Object[]{"WHEELCHAIR", new String[]{"WayCategory", "WaySurfaceType", "Wheelchair", "OsmId"}}
        };
    }

    @Test
    void testDefaultExtendedStorages() {
        DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties();
        assertEquals(2, defaultExtendedStoragesProperties.getExtStorages().size());

        // Assert WayCategory and WaySurfaceType are present
        assertTrue(defaultExtendedStoragesProperties.getExtStorages().containsKey("WayCategory"));
        assertTrue(defaultExtendedStoragesProperties.getExtStorages().containsKey("WaySurfaceType"));
        // And both are of the correct type
        assertInstanceOf(ExtendedStorageWayCategory.class, defaultExtendedStoragesProperties.getExtStorages().get("WayCategory"));
        assertInstanceOf(ExtendedStorageWaySurfaceType.class, defaultExtendedStoragesProperties.getExtStorages().get("WaySurfaceType"));

    }

    @Test
    void testDefaultExtendedStoragesWithNullEncoderName() {
        DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties(null);
        assertEquals(2, defaultExtendedStoragesProperties.getExtStorages().size());

        // Assert WayCategory and WaySurfaceType are present
        assertTrue(defaultExtendedStoragesProperties.getExtStorages().containsKey("WayCategory"));
        assertTrue(defaultExtendedStoragesProperties.getExtStorages().containsKey("WaySurfaceType"));
        // And both are of the correct type
        assertInstanceOf(ExtendedStorageWayCategory.class, defaultExtendedStoragesProperties.getExtStorages().get("WayCategory"));
        assertInstanceOf(ExtendedStorageWaySurfaceType.class, defaultExtendedStoragesProperties.getExtStorages().get("WaySurfaceType"));
    }

    @ParameterizedTest
    @CsvSource({
            "DRIVING_CAR,4",
            "DRIVING_HGV,3",
            "CYCLING_REGULAR,4",
            "CYCLING_MOUNTAIN,4",
            "CYCLING_ROAD,4",
            "CYCLING_ELECTRIC,4",
            "FOOT_WALKING,4",
            "FOOT_HIKING,4",
            "WHEELCHAIR,4"
    })
    void testDefaultExtendedStoragesSizeWithEncoderName(String encoderName, Integer expectedSize) {
        DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties(EncoderNameEnum.valueOf(encoderName));
        assertEquals(expectedSize, defaultExtendedStoragesProperties.getExtStorages().size());
    }

    // Test the default extended storages for the given encoder name
    @ParameterizedTest
    @MethodSource("correctExtendedStoragesPerProfile")
    void testDefaultExtendedStoragesWithEncoderName(String encoderName, String[] expectedKeys) {
        DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties(EncoderNameEnum.valueOf(encoderName));
        assertEquals(expectedKeys.length, defaultExtendedStoragesProperties.getExtStorages().size());
        for (String key : expectedKeys) {
            assertTrue(defaultExtendedStoragesProperties.getExtStorages().containsKey(key));
        }
    }


}