package org.heigit.ors.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.EncoderOptionsProperties;
import org.heigit.ors.config.profile.ExecutionProperties;
import org.heigit.ors.config.profile.PreparationProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.config.profile.defaults.DefaultElevationProperties;
import org.heigit.ors.config.profile.defaults.DefaultProfileProperties;
import org.heigit.ors.config.profile.defaults.DefaultProfiles;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.heigit.ors.config.profile.storages.ExtendedStorageGreenIndex;
import org.heigit.ors.config.profile.storages.ExtendedStorageHeavyVehicle;
import org.heigit.ors.config.profile.storages.ExtendedStorageWayCategory;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.heigit.ors.config.utils.PropertyUtils.assertAllNull;
import static org.junit.jupiter.api.Assertions.*;

class EnginePropertiesTest {

    ;
    //language=JSON
    private final String testJson = """
                  {
                    "graphs_data_access": "MMAP",
                    "elevation": {},
                    "profile_default": {
                        "preparation": {
                        "min_network_size": 300,
                        "methods": {
                          "lm": {
                            "enabled": false,
                              "threads": 4,
                              "weightings": "shortest",
                              "landmarks": 2
                          }
                        }
                      },
                        "execution": {
                          "methods": {
                            "lm": {
                              "active_landmarks": 8
                            }
                          }
                        }
                    },
                    "profiles": {
                      "car": {
                        "encoder_name": "driving-car",
                        "enabled": true,
                        "encoder_options": {},
                        "preparation": {\
                          "methods": {
                            "lm": {
                              "enabled": true,
                              "threads": 1
                            }
                          }
                         },
                        "execution": {
                           "methods": {\
                            "lm": {\
                              "active_landmarks": 2\
                            }
                          }
                        },
                        "ext_storages": {}
                      },
                      "hgv": {
                        "encoder_name": "driving-hgv",
                        "preparation": {
                          "min_network_size": 900,
                          "methods": {
                            "lm": {
                              "enabled": true
                            }
                          }
                        },
                        "ext_storages": {
                          "WayCategory": {},
                          "HeavyVehicle": {
                            "restrictions": true
                          },
                          "GreenIndex": {
                            "filepath": "/path/to/file.csv"
                          }
            }
                      }
                    }
                  }""";

    @Test
    void testSerializeEmptyDefaultEngineProperties() throws JsonProcessingException {
        EngineProperties engineProperties = new EngineProperties();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(engineProperties);
        assertNotNull(json);
        //language=JSON
        String expectedJson = """
                       {
                "source_file": null,
                "init_threads": null,
                "preparation_mode": null,
                "config_output_mode": null,
                "graphs_root_path": null,
                "graphs_data_access": null,
                "elevation": {
                    "preprocessed": null,
                    "data_access": null,
                    "cache_clear": null,
                    "provider": null,
                    "cache_path": null
                },
                "profile_default": {
                    "encoder_options": {},
                    "preparation": {
                        "methods": {}
                    },
                    "execution": {
                        "methods": {}
                    },
                    "ext_storages": {}
                },
                "profiles": {}
                            }""";
        // compare the two json strings as actual json objects
        assertEquals(objectMapper.readTree(expectedJson), objectMapper.readTree(json));
    }

    @Test
    void testDeserialize() throws JsonProcessingException {
        //language=JSON
        String json = """
                {
                    "source_file": "/absolute/path/osm.pbf",
                    "init_threads": 1,
                    "preparation_mode": true,
                    "config_output_mode": true,
                    "graphs_root_path": "./graphs",
                    "graphs_data_access": "RAM_STORE",
                    "elevation": {
                        "preprocessed": true,
                        "data_access": "MMAP",
                        "cache_clear": true,
                        "provider": "multi",
                        "cache_path": "./elevation_cache"
                    },
                    "profile_default": {
                        "enabled": true,
                        "encoder_name": "unknown",
                        "elevation": true,
                        "elevation_smoothing": true,
                        "encoder_flags_size": 8,
                        "instructions": true,
                        "optimize": true,
                        "traffic": true,
                        "interpolate_bridges_and_tunnels": true,
                        "force_turn_costs": true,
                        "location_index_resolution": 400,
                        "location_index_search_iterations": 2,
                        "maximum_distance": 200000,
                        "maximum_distance_dynamic_weights": 200000,
                        "maximum_distance_avoid_areas": 200000,
                        "maximum_distance_alternative_routes": 200000,
                        "maximum_distance_round_trip_routes": 200000,
                        "maximum_speed_lower_bound": 10,
                        "maximum_way_points": 20,
                        "maximum_snapping_radius": 100,
                        "maximum_visited_nodes": 5000000,
                        "ext_storages": {}
                    },
                    "profiles": {
                        "car": {
                            "enabled": true,
                            "encoder_name": "driving-car",
                            "elevation": true,
                            "elevation_smoothing": true,
                            "encoder_flags_size": 1,
                            "instructions": true,
                            "optimize": true,
                            "traffic": true,
                            "interpolate_bridges_and_tunnels": true,
                            "force_turn_costs": true,
                            "location_index_resolution": 700,
                            "location_index_search_iterations": 1,
                            "maximum_distance": 400000,
                            "maximum_distance_dynamic_weights": 200000,
                            "maximum_distance_avoid_areas": 300000,
                            "maximum_distance_alternative_routes": 600000,
                            "maximum_distance_round_trip_routes": 200000,
                            "maximum_speed_lower_bound": 20,
                            "maximum_way_points": 30,
                            "maximum_snapping_radius": 300,
                            "maximum_visited_nodes": 2000000,
                            "ext_storages": {}
                        },
                        "hgv": {
                            "enabled": true,
                            "encoder_name": "driving-hgv",
                            "elevation": true,
                            "elevation_smoothing": true,
                            "encoder_flags_size": 8,
                            "instructions": true,
                            "optimize": true,
                            "traffic": true,
                            "interpolate_bridges_and_tunnels": true,
                            "force_turn_costs": true,
                            "location_index_resolution": 500,
                            "location_index_search_iterations": 4,
                            "maximum_distance": 100000,
                            "maximum_distance_dynamic_weights": 100000,
                            "maximum_distance_avoid_areas": 100000,
                            "maximum_distance_alternative_routes": 100000,
                            "maximum_distance_round_trip_routes": 100000,
                            "maximum_speed_lower_bound": 80,
                            "maximum_way_points": 50,
                            "maximum_snapping_radius": 400,
                            "maximum_visited_nodes": 1000000,
                            "ext_storages": {}
                        }
                    }
                }
                """;
        ObjectMapper objectMapper = new ObjectMapper();
        EngineProperties deserializedEngineProperties = objectMapper.readValue(json, EngineProperties.class);
        assertNotNull(deserializedEngineProperties);
        assertEquals("/absolute/path/osm.pbf", deserializedEngineProperties.getSourceFile().toString());
        assertEquals(1, deserializedEngineProperties.getInitThreads());
        assertTrue(deserializedEngineProperties.getPreparationMode());
        assertTrue(deserializedEngineProperties.getConfigOutputMode());
        assertEquals(Paths.get("./graphs").toAbsolutePath(), deserializedEngineProperties.getGraphsRootPath());
        assertEquals(DataAccessEnum.RAM_STORE, deserializedEngineProperties.getGraphsDataAccess());
        assertNotNull(deserializedEngineProperties.getElevation());
        assertNotNull(deserializedEngineProperties.getProfileDefault());
        Map<String, ProfileProperties> profiles = deserializedEngineProperties.getProfiles();
        assertNotNull(profiles);
        assertEquals(2, profiles.size());
        assertTrue(profiles.containsKey("car"));
        assertTrue(profiles.containsKey("hgv"));

        ProfileProperties carProfile = profiles.get("car");
        assertNotNull(carProfile);
        assertTrue(carProfile.getEnabled());
        assertEquals(EncoderNameEnum.DRIVING_CAR, carProfile.getEncoderName());
        assertTrue(carProfile.getElevation());
        assertTrue(carProfile.getElevationSmoothing());
        assertEquals(1, carProfile.getEncoderFlagsSize());
        assertTrue(carProfile.getInstructions());
        assertTrue(carProfile.getOptimize());
        assertTrue(carProfile.getTraffic());
        assertTrue(carProfile.getInterpolateBridgesAndTunnels());
        assertTrue(carProfile.getForceTurnCosts());
        assertEquals(700, carProfile.getLocationIndexResolution());
        assertEquals(1, carProfile.getLocationIndexSearchIterations());
        assertEquals(400000.0, carProfile.getMaximumDistance());
        assertEquals(200000.0, carProfile.getMaximumDistanceDynamicWeights());
        assertEquals(300000.0, carProfile.getMaximumDistanceAvoidAreas());
        assertEquals(600000.0, carProfile.getMaximumDistanceAlternativeRoutes());
        assertEquals(200000.0, carProfile.getMaximumDistanceRoundTripRoutes());
        assertEquals(20.0, carProfile.getMaximumSpeedLowerBound());
        assertEquals(30, carProfile.getMaximumWayPoints());
        assertEquals(300, carProfile.getMaximumSnappingRadius());
        assertEquals(2000000, carProfile.getMaximumVisitedNodes());
        assertNotNull(carProfile.getExtStorages());
        assertTrue(carProfile.getExtStorages().isEmpty());

        ProfileProperties hgvProfile = profiles.get("hgv");
        assertNotNull(hgvProfile);
        assertTrue(hgvProfile.getEnabled());
        assertEquals(EncoderNameEnum.DRIVING_HGV, hgvProfile.getEncoderName());
        assertTrue(hgvProfile.getElevation());
        assertTrue(hgvProfile.getElevationSmoothing());
        assertEquals(8, hgvProfile.getEncoderFlagsSize());
        assertTrue(hgvProfile.getInstructions());
        assertTrue(hgvProfile.getOptimize());
        assertTrue(hgvProfile.getTraffic());
        assertTrue(hgvProfile.getInterpolateBridgesAndTunnels());
        assertTrue(hgvProfile.getForceTurnCosts());
        assertEquals(500, hgvProfile.getLocationIndexResolution());
        assertEquals(4, hgvProfile.getLocationIndexSearchIterations());
        assertEquals(100000.0, hgvProfile.getMaximumDistance());
        assertEquals(100000.0, hgvProfile.getMaximumDistanceDynamicWeights());
        assertEquals(100000.0, hgvProfile.getMaximumDistanceAvoidAreas());
        assertEquals(100000.0, hgvProfile.getMaximumDistanceAlternativeRoutes());
        assertEquals(100000.0, hgvProfile.getMaximumDistanceRoundTripRoutes());
        assertEquals(80.0, hgvProfile.getMaximumSpeedLowerBound());
        assertEquals(50, hgvProfile.getMaximumWayPoints());
        assertEquals(400, hgvProfile.getMaximumSnappingRadius());
        assertEquals(1000000, hgvProfile.getMaximumVisitedNodes());
        assertNotNull(hgvProfile.getExtStorages());
        assertTrue(hgvProfile.getExtStorages().isEmpty());
    }

    @Test
    void testRawSettingEverythingElseNullDefaultProfiles() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);

        Map<String, ProfileProperties> defaultProfiles = foo.getDefault_profiles();
        assertEquals(0, defaultProfiles.size());
    }

    @Test
    void testRawSettingEverythingElseNullGraphsDataAccess() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);
        assertEquals(DataAccessEnum.MMAP, foo.getGraphsDataAccess());
    }

    @Test
    void testRawSettingEverythingElseNullElevationProperties() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);

        ElevationProperties elevationProperties = foo.getElevation();
        assertTrue(assertAllNull(elevationProperties, new ArrayList<>()));
    }

    @Test
    void testRawSettingEverythingElseNullProfileDefaultProperties() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);

        ProfileProperties profileProperties = foo.getProfileDefault();

        assertTrue(assertAllNull(profileProperties, new ArrayList<>()));

        EncoderOptionsProperties encoderOptions = profileProperties.getEncoderOptions();
        assertTrue(assertAllNull(encoderOptions, new ArrayList<>()));

        PreparationProperties preparationProperties = profileProperties.getPreparation();
        assertEquals(300, preparationProperties.getMinNetworkSize());
        assertNull(preparationProperties.getMinOneWayNetworkSize());

        PreparationProperties.MethodsProperties methodsProperties = preparationProperties.getMethods();
        assertTrue(assertAllNull(methodsProperties, new ArrayList<>(List.of("lm"))));

        PreparationProperties.MethodsProperties.LMProperties lmProperties = methodsProperties.getLm();
        assertFalse(lmProperties.isEnabled());
        assertEquals(4, lmProperties.getThreads());
        assertEquals("shortest", lmProperties.getWeightings());
        assertEquals(2, lmProperties.getLandmarks());

        ExecutionProperties executionProperties = profileProperties.getExecution();
        assertTrue(assertAllNull(executionProperties.getMethods(), new ArrayList<>(List.of("lm"))));

        ExecutionProperties.MethodsProperties.LMProperties lmExecutionProperties = executionProperties.getMethods().getLm();
        assertEquals(8, lmExecutionProperties.getActiveLandmarks());
        assertEquals(0, profileProperties.getExtStorages().size());
    }

    @Test
    void testRawSettingEverythingElseNullCarProfileProperties() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);

        Map<String, ProfileProperties> profiles = foo.getProfiles();
        assertEquals(2, profiles.size());
        assertTrue(profiles.containsKey("car"));

        ProfileProperties carProfile = profiles.get("car");
        assertTrue(carProfile.getEnabled());
        assertEquals(EncoderNameEnum.DRIVING_CAR, carProfile.getEncoderName());

        assertTrue(assertAllNull(carProfile, new ArrayList<>(List.of("encoderName", "enabled", "encoderOptions", "preparation", "execution", "extStorages"))));

        EncoderOptionsProperties carEncoderOptions = carProfile.getEncoderOptions();
        assertTrue(assertAllNull(carEncoderOptions, new ArrayList<>()));

        PreparationProperties carPreparation = carProfile.getPreparation();
        assertTrue(assertAllNull(carPreparation, new ArrayList<>(List.of("methods"))));

        PreparationProperties.MethodsProperties carMethods = carPreparation.getMethods();
        assertTrue(assertAllNull(carMethods, new ArrayList<>(List.of("lm"))));

        PreparationProperties.MethodsProperties.LMProperties carLm = carMethods.getLm();
        assertTrue(assertAllNull(carLm, new ArrayList<>(List.of("enabled", "threads"))));
        assertTrue(carLm.isEnabled());
        assertEquals(1, carLm.getThreads());

        ExecutionProperties carExecution = carProfile.getExecution();
        assertTrue(assertAllNull(carExecution.getMethods(), new ArrayList<>(List.of("lm"))));

        ExecutionProperties.MethodsProperties.LMProperties carLmExecution = carExecution.getMethods().getLm();
        assertEquals(2, carLmExecution.getActiveLandmarks());
        assertEquals(0, carProfile.getExtStorages().size());
    }

    @Test
    void testRawSettingEverythingElseNullHgvProfileProperties() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);

        assertTrue(assertAllNull(foo,
                new ArrayList<>(List.of("default_profiles", "graphsDataAccess", "profiles", "minNetworkSize", "lm")),
                true));
        assertEquals(DataAccessEnum.MMAP, foo.getGraphsDataAccess());


        Map<String, ProfileProperties> profiles = foo.getProfiles();
        assertEquals(2, profiles.size());
        assertTrue(profiles.containsKey("hgv"));

        ProfileProperties hgvProfile = profiles.get("hgv");
        assertNull(hgvProfile.getEnabled());
        assertEquals(EncoderNameEnum.DRIVING_HGV, hgvProfile.getEncoderName());

        assertTrue(assertAllNull(hgvProfile, new ArrayList<>(List.of("encoderName", "enabled", "encoderOptions", "preparation", "execution", "extStorages"))));

        EncoderOptionsProperties hgvEncoderOptions = hgvProfile.getEncoderOptions();
        assertTrue(assertAllNull(hgvEncoderOptions, new ArrayList<>()));

        PreparationProperties hgvPreparation = hgvProfile.getPreparation();
        assertEquals(900, hgvPreparation.getMinNetworkSize());
        assertNull(hgvPreparation.getMinOneWayNetworkSize());
        PreparationProperties.MethodsProperties hgvMethods = hgvPreparation.getMethods();
        assertTrue(assertAllNull(hgvMethods, new ArrayList<>(List.of("lm"))));
        assertTrue(assertAllNull(hgvMethods.getLm(), new ArrayList<>(List.of("enabled"))));

        PreparationProperties.MethodsProperties hgvMethodsProperties = hgvPreparation.getMethods();
        assertTrue(assertAllNull(hgvMethodsProperties, new ArrayList<>(List.of("lm"))));

        PreparationProperties.MethodsProperties.LMProperties hgvLm = hgvMethods.getLm();
        assertTrue(assertAllNull(hgvLm, new ArrayList<>(List.of("enabled"))));
        assertTrue(hgvLm.isEnabled());

        ExecutionProperties hgvExecution = hgvProfile.getExecution();
        assertTrue(assertAllNull(hgvExecution.getMethods(), new ArrayList<>(List.of("lm"))));

        Map<String, ExtendedStorage> hgvExtStorages = hgvProfile.getExtStorages();
        assertEquals(3, hgvExtStorages.size());
        assertTrue(hgvExtStorages.containsKey("WayCategory"));
        assertTrue(hgvExtStorages.containsKey("HeavyVehicle"));
        assertTrue(hgvExtStorages.containsKey("GreenIndex"));

        ExtendedStorageWayCategory wayCategory = (ExtendedStorageWayCategory) hgvExtStorages.get("WayCategory");
        assertInstanceOf(ExtendedStorageWayCategory.class, wayCategory);
        assertTrue(wayCategory.getEnabled());

        ExtendedStorageHeavyVehicle heavyVehicle = (ExtendedStorageHeavyVehicle) hgvExtStorages.get("HeavyVehicle");
        assertInstanceOf(ExtendedStorageHeavyVehicle.class, heavyVehicle);
        assertTrue(heavyVehicle.getEnabled());
        assertTrue(heavyVehicle.getRestrictions());

        ExtendedStorageGreenIndex greenIndex = (ExtendedStorageGreenIndex) hgvExtStorages.get("GreenIndex");
        assertInstanceOf(ExtendedStorageGreenIndex.class, greenIndex);
        assertTrue(greenIndex.getEnabled());
        assertEquals(Paths.get("/path/to/file.csv"), greenIndex.getFilepath());
    }

    @Test
    void testDefaultEngineProperties() {
        // Defaults to check against
        DefaultProfiles defaultProfiles = new DefaultProfiles(true);
        DefaultElevationProperties defaultElevationProperties = new DefaultElevationProperties(true);
        DefaultProfileProperties defaultProfileProperties = new DefaultProfileProperties(true);

        // Initialize the whole engine properties default chain
        EngineProperties defaultEngineProperties = new EngineProperties(true);
        // source file, init threads, preparation mode, config output mode, graphs root path, graphs data access
        assertEquals(0, defaultEngineProperties.getProfiles().size());
        assertEquals(Path.of(""), defaultEngineProperties.getSourceFile());
        assertEquals(1, defaultEngineProperties.getInitThreads());
        assertFalse(defaultEngineProperties.getPreparationMode());
        assertFalse(defaultEngineProperties.getConfigOutputMode());
        assertEquals(Paths.get("./graphs"), defaultEngineProperties.getGraphsRootPath());
        assertEquals(DataAccessEnum.RAM_STORE, defaultEngineProperties.getGraphsDataAccess());

        // Check equality for elevation
        assertEquals(defaultElevationProperties, defaultEngineProperties.getElevation());

        // Check profileDefaults
        assertEquals(defaultProfileProperties, defaultEngineProperties.getProfileDefault());

        // check default_profiles
        Map<String, ProfileProperties> expectedDefaultProfiles = defaultProfiles.getProfiles();
        Map<String, ProfileProperties> actualDefaultProfiles = defaultEngineProperties.getDefault_profiles();
        assertEquals(expectedDefaultProfiles.size(), actualDefaultProfiles.size());

        // Get hiking
        for (String key : expectedDefaultProfiles.keySet()) {
            assertEquals(expectedDefaultProfiles.get(key), actualDefaultProfiles.get(key));
        }
    }
}