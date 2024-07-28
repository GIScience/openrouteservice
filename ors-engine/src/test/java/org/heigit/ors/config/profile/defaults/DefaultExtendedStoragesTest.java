package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.storages.ExtendedStorageWayCategory;
import org.heigit.ors.config.profile.storages.ExtendedStorageWaySurfaceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

class DefaultExtendedStoragesTest {

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
        DefaultExtendedStorages defaultExtendedStorages = new DefaultExtendedStorages();
        assertEquals(2, defaultExtendedStorages.getExtStorages().size());

        // Assert WayCategory and WaySurfaceType are present
        assertTrue(defaultExtendedStorages.getExtStorages().containsKey("WayCategory"));
        assertTrue(defaultExtendedStorages.getExtStorages().containsKey("WaySurfaceType"));
        // And both are of the correct type
        assertInstanceOf(ExtendedStorageWayCategory.class, defaultExtendedStorages.getExtStorages().get("WayCategory"));
        assertInstanceOf(ExtendedStorageWaySurfaceType.class, defaultExtendedStorages.getExtStorages().get("WaySurfaceType"));

    }

    @Test
    void testDefaultExtendedStoragesWithNullEncoderName() {
        DefaultExtendedStorages defaultExtendedStorages = new DefaultExtendedStorages(null);
        assertEquals(2, defaultExtendedStorages.getExtStorages().size());

        // Assert WayCategory and WaySurfaceType are present
        assertTrue(defaultExtendedStorages.getExtStorages().containsKey("WayCategory"));
        assertTrue(defaultExtendedStorages.getExtStorages().containsKey("WaySurfaceType"));
        // And both are of the correct type
        assertInstanceOf(ExtendedStorageWayCategory.class, defaultExtendedStorages.getExtStorages().get("WayCategory"));
        assertInstanceOf(ExtendedStorageWaySurfaceType.class, defaultExtendedStorages.getExtStorages().get("WaySurfaceType"));
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
        DefaultExtendedStorages defaultExtendedStorages = new DefaultExtendedStorages(EncoderNameEnum.valueOf(encoderName));
        assertEquals(expectedSize, defaultExtendedStorages.getExtStorages().size());
    }

    // Test the default extended storages for the given encoder name
    @ParameterizedTest
    @MethodSource("correctExtendedStoragesPerProfile")
    void testDefaultExtendedStoragesWithEncoderName(String encoderName, String[] expectedKeys) {
        DefaultExtendedStorages defaultExtendedStorages = new DefaultExtendedStorages(EncoderNameEnum.valueOf(encoderName));
        assertEquals(expectedKeys.length, defaultExtendedStorages.getExtStorages().size());
        for (String key : expectedKeys) {
            assertTrue(defaultExtendedStorages.getExtStorages().containsKey(key));
        }
    }


}