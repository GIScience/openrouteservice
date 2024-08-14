package org.heigit.ors.config.profile.storages;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExtendedStorageNameTest {

    @Test
    void testGetName() {
        assertEquals("Borders", ExtendedStorageName.BORDERS.getName());
        assertEquals("csv", ExtendedStorageName.CSV.getName());
        assertEquals("GreenIndex", ExtendedStorageName.GREEN_INDEX.getName());
        assertEquals("HeavyVehicle", ExtendedStorageName.HEAVY_VEHICLE.getName());
        assertEquals("HereTraffic", ExtendedStorageName.HERE_TRAFFIC.getName());
        assertEquals("HillIndex", ExtendedStorageName.HILL_INDEX.getName());
        assertEquals("NoiseIndex", ExtendedStorageName.NOISE_INDEX.getName());
        assertEquals("OsmId", ExtendedStorageName.OSM_ID.getName());
        assertEquals("RoadAccessRestrictions", ExtendedStorageName.ROAD_ACCESS_RESTRICTIONS.getName());
        assertEquals("ShadowIndex", ExtendedStorageName.SHADOW_INDEX.getName());
        assertEquals("TollWays", ExtendedStorageName.TOLLWAYS.getName());
        assertEquals("TrailDifficulty", ExtendedStorageName.TRAIL_DIFFICULTY.getName());
        assertEquals("WayCategory", ExtendedStorageName.WAY_CATEGORY.getName());
        assertEquals("WaySurfaceType", ExtendedStorageName.WAY_SURFACE_TYPE.getName());
        assertEquals("Wheelchair", ExtendedStorageName.WHEELCHAIR.getName());

    }

    @Test
    void getValue() {
        assertEquals("Borders", ExtendedStorageName.BORDERS.getValue());
        assertEquals("csv", ExtendedStorageName.CSV.getValue());
        assertEquals("GreenIndex", ExtendedStorageName.GREEN_INDEX.getValue());
        assertEquals("HeavyVehicle", ExtendedStorageName.HEAVY_VEHICLE.getValue());
        assertEquals("HereTraffic", ExtendedStorageName.HERE_TRAFFIC.getValue());
        assertEquals("HillIndex", ExtendedStorageName.HILL_INDEX.getValue());
        assertEquals("NoiseIndex", ExtendedStorageName.NOISE_INDEX.getValue());
        assertEquals("OsmId", ExtendedStorageName.OSM_ID.getValue());
        assertEquals("RoadAccessRestrictions", ExtendedStorageName.ROAD_ACCESS_RESTRICTIONS.getValue());
        assertEquals("ShadowIndex", ExtendedStorageName.SHADOW_INDEX.getValue());
        assertEquals("TollWays", ExtendedStorageName.TOLLWAYS.getValue());
        assertEquals("TrailDifficulty", ExtendedStorageName.TRAIL_DIFFICULTY.getValue());
        assertEquals("WayCategory", ExtendedStorageName.WAY_CATEGORY.getValue());
        assertEquals("WaySurfaceType", ExtendedStorageName.WAY_SURFACE_TYPE.getValue());
        assertEquals("Wheelchair", ExtendedStorageName.WHEELCHAIR.getValue());

    }

    @Test
    void testToString() {
        assertEquals("Borders", ExtendedStorageName.BORDERS.toString());
        assertEquals("csv", ExtendedStorageName.CSV.toString());
        assertEquals("GreenIndex", ExtendedStorageName.GREEN_INDEX.toString());
        assertEquals("HeavyVehicle", ExtendedStorageName.HEAVY_VEHICLE.toString());
        assertEquals("HereTraffic", ExtendedStorageName.HERE_TRAFFIC.toString());
        assertEquals("HillIndex", ExtendedStorageName.HILL_INDEX.toString());
        assertEquals("NoiseIndex", ExtendedStorageName.NOISE_INDEX.toString());
        assertEquals("OsmId", ExtendedStorageName.OSM_ID.toString());
        assertEquals("RoadAccessRestrictions", ExtendedStorageName.ROAD_ACCESS_RESTRICTIONS.toString());
        assertEquals("ShadowIndex", ExtendedStorageName.SHADOW_INDEX.toString());
        assertEquals("TollWays", ExtendedStorageName.TOLLWAYS.toString());
        assertEquals("TrailDifficulty", ExtendedStorageName.TRAIL_DIFFICULTY.toString());
        assertEquals("WayCategory", ExtendedStorageName.WAY_CATEGORY.toString());
        assertEquals("WaySurfaceType", ExtendedStorageName.WAY_SURFACE_TYPE.toString());
        assertEquals("Wheelchair", ExtendedStorageName.WHEELCHAIR.toString());
    }

    @Test
    void getEnum() {
        assertEquals(ExtendedStorageName.BORDERS, ExtendedStorageName.getEnum("Borders"));
        assertEquals(ExtendedStorageName.CSV, ExtendedStorageName.getEnum("csv"));
        assertEquals(ExtendedStorageName.GREEN_INDEX, ExtendedStorageName.getEnum("GreenIndex"));
        assertEquals(ExtendedStorageName.HEAVY_VEHICLE, ExtendedStorageName.getEnum("HeavyVehicle"));
        assertEquals(ExtendedStorageName.HERE_TRAFFIC, ExtendedStorageName.getEnum("HereTraffic"));
        assertEquals(ExtendedStorageName.HILL_INDEX, ExtendedStorageName.getEnum("HillIndex"));
        assertEquals(ExtendedStorageName.NOISE_INDEX, ExtendedStorageName.getEnum("NoiseIndex"));
        assertEquals(ExtendedStorageName.OSM_ID, ExtendedStorageName.getEnum("OsmId"));
        assertEquals(ExtendedStorageName.ROAD_ACCESS_RESTRICTIONS, ExtendedStorageName.getEnum("RoadAccessRestrictions"));
        assertEquals(ExtendedStorageName.SHADOW_INDEX, ExtendedStorageName.getEnum("ShadowIndex"));
        assertEquals(ExtendedStorageName.TOLLWAYS, ExtendedStorageName.getEnum("TollWays"));
        assertEquals(ExtendedStorageName.TRAIL_DIFFICULTY, ExtendedStorageName.getEnum("TrailDifficulty"));
        assertEquals(ExtendedStorageName.WAY_CATEGORY, ExtendedStorageName.getEnum("WayCategory"));
        assertEquals(ExtendedStorageName.WAY_SURFACE_TYPE, ExtendedStorageName.getEnum("WaySurfaceType"));
        assertEquals(ExtendedStorageName.WHEELCHAIR, ExtendedStorageName.getEnum("Wheelchair"));

        // fail with illegal argument exception
        assertThrows(IllegalArgumentException.class, () -> ExtendedStorageName.getEnum("Unknown"));
    }
}