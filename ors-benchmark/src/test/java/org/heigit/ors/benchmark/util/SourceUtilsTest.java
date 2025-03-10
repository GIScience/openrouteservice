package org.heigit.ors.benchmark.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SourceUtilsTest {
    
    private List<Map<String, Object>> multipleProfiles;
    
    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        multipleProfiles = new ArrayList<>();
        multipleProfiles.add(Map.of("profile", "driving-car", "lon", "8.68", "lat", "49.41"));
        multipleProfiles.add(Map.of("profile", "cycling-regular", "lon", "8.69", "lat", "49.42"));
        multipleProfiles.add(Map.of("profile", "foot-walking", "lon", "8.71", "lat", "49.43"));
    }
    
    @Test
    void testGetRecordsByProfileWithMatchingProfile() throws IllegalStateException {
        List<Map<String, Object>> records = SourceUtils.getRecordsByProfile(multipleProfiles, "driving-car");
        assertNotNull(records);
        assertFalse(records.isEmpty());
        assertEquals(1, records.size());

        // Get the first record and verify its contents
        Map<String, Object> sampleRecord = records.get(0);
        assertEquals("driving-car", sampleRecord.get("profile"));
        assertEquals("8.68", sampleRecord.get("lon"));
        assertEquals("49.41", sampleRecord.get("lat"));
    }


    @Test
    void testGetRecordsByProfileWithoutMatchingProfile() {
        IllegalStateException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            SourceUtils.getRecordsByProfile(multipleProfiles, "foo-bar");
        });
        assertTrue(exception.getMessage().contains("No records found for profile 'foo-bar'"));
    }


    

}
