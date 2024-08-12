package org.heigit.ors.config.defaults;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultEnginePropertiesTest {
    EngineProperties defaultEngineProperties;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        // Defaults to check against
        defaultEngineProperties = new DefaultEngineProperties(true);
    }

    @Test
    void testDefaultEngineConstructor() {
        // Check if the default constructor sets the correct defaults
        EngineProperties propertiesEmptyConstructor = new DefaultEngineProperties();
        assertNull(propertiesEmptyConstructor.getSourceFile());
        assertNull(propertiesEmptyConstructor.getInitThreads());
        assertNull(propertiesEmptyConstructor.getPreparationMode());
        assertNull(propertiesEmptyConstructor.getConfigOutput());
        assertNull(propertiesEmptyConstructor.getGraphsRootPath());
        assertNull(propertiesEmptyConstructor.getGraphsDataAccess());
        assertNull(propertiesEmptyConstructor.getElevation());
        assertNull(propertiesEmptyConstructor.getProfileDefault());
        assertNull(propertiesEmptyConstructor.getProfiles());
    }

    @Test
    void getActiveProfilesReturnsEmptyMapWhenNoProfilesSet() {
        Map<String, ProfileProperties> profiles = defaultEngineProperties.getProfiles();
        assertNotNull(profiles);
        assertTrue(profiles.isEmpty());
    }

    @Test
    void getActiveProfilesReturnsDefaultProfilesWhenNotSet() {
        defaultEngineProperties.initialize();
        Map<String, ProfileProperties> activeProfiles = defaultEngineProperties.getActiveProfiles();
        assertNotNull(activeProfiles);
        assertFalse(activeProfiles.isEmpty());
        assertTrue(activeProfiles.containsKey("car"));
        assertTrue(activeProfiles.containsKey("hgv"));
    }

    @Test
    void testSerializeEmptyDefaultEngineProperties() throws JsonProcessingException {
        EngineProperties engineProperties = new DefaultEngineProperties(true);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(engineProperties);
        assertNotNull(json);
        String customGraphsPath = Path.of("graphs").toAbsolutePath().toString();
        String customElevationCachePath = Path.of("elevation_cache").toAbsolutePath().toString();
        //language=JSON
        String expectedJson = String.format("""
                {
                     "source_file": "",
                     "init_threads": 2,
                     "preparation_mode": false,
                     "config_output": null,
                     "graphs_root_path": "%s",
                     "graphs_data_access": "RAM_STORE",
                     "elevation": {
                         "preprocessed": false,
                         "data_access": "MMAP",
                         "cache_clear": false,
                         "provider": "multi",
                         "cache_path": "%s"
                     },
                     "profile_default": {
                         "enabled": false,
                         "elevation": true,
                         "elevation_smoothing": true,
                         "encoder_flags_size": 8,
                         "instructions": true,
                         "optimize": false,
                         "traffic": false,
                         "interpolate_bridges_and_tunnels": true,
                         "force_turn_costs": false,
                         "location_index_resolution": 500,
                         "location_index_search_iterations": 4,
                         "maximum_distance": 100000.0,
                         "maximum_distance_dynamic_weights": 100000.0,
                         "maximum_distance_avoid_areas": 100000.0,
                         "maximum_distance_alternative_routes": 100000.0,
                         "maximum_distance_round_trip_routes": 100000.0,
                         "maximum_speed_lower_bound": 80.0,
                         "maximum_way_points": 50,
                         "maximum_snapping_radius": 400,
                         "maximum_visited_nodes": 1000000,
                         "encoder_options": {},
                         "preparation": {
                             "min_network_size": 200,
                             "min_one_way_network_size": 200,
                             "methods": {
                                 "ch": {
                                     "threads": 2,
                                     "weightings": "fastest",
                                     "enabled": false
                                 },
                                 "lm": {
                                     "threads": 2,
                                     "weightings": "recommended,shortest",
                                     "landmarks": 16,
                                     "enabled": true
                                 },
                                 "core": {
                                     "threads": 2,
                                     "weightings": "fastest,shortest",
                                     "landmarks": 64,
                                     "lmsets": "highways;allow_all",
                                     "enabled": false
                                 },
                                 "fastisochrones": {
                                     "threads": 2,
                                     "weightings": "recommended,shortest",
                                     "enabled": false
                                 }
                             }
                         },
                         "execution": {
                             "methods": {
                                 "lm": {
                                     "active_landmarks": 8
                                 },
                                 "core": {
                                     "active_landmarks": 6
                                 }
                             }
                         }
                     },
                     "profiles": {}
                 }""", customGraphsPath, customElevationCachePath);
        // compare the two json strings as actual json objects
        assertEquals(objectMapper.readTree(expectedJson), objectMapper.readTree(json));
    }

    @Test
    void testDefaultEngineProperties() {
        // Defaults to check against
        DefaultElevationProperties defaultElevationProperties = new DefaultElevationProperties(true);
        DefaultProfileProperties defaultProfileProperties = new DefaultProfileProperties(true);

        // Initialize the whole engine properties default chain
        // source file, init threads, preparation mode, config output mode, graphs root path, graphs data access
        assertEquals(0, defaultEngineProperties.getProfiles().size());
        assertEquals(Path.of(""), defaultEngineProperties.getSourceFile());
        assertEquals(2, defaultEngineProperties.getInitThreads());
        assertFalse(defaultEngineProperties.getPreparationMode());
        assertNull(defaultEngineProperties.getConfigOutput());
        assertEquals(Paths.get("graphs").toAbsolutePath(), defaultEngineProperties.getGraphsRootPath());
        assertEquals(DataAccessEnum.RAM_STORE, defaultEngineProperties.getGraphsDataAccess());

        // Check equality for elevation
        assertEquals(defaultElevationProperties, defaultEngineProperties.getElevation());

        // Check profileDefaults
        assertEquals(defaultProfileProperties, defaultEngineProperties.getProfileDefault());
    }

}
