package org.heigit.ors.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.*;
import org.heigit.ors.config.utils.PropertyUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.heigit.ors.config.utils.PropertyUtils.assertAllNull;
import static org.junit.jupiter.api.Assertions.*;

class EnginePropertiesTest {

    //language=JSON
    private final String testJson = """
            {
              "graphs_data_access": "MMAP_RO",
              "elevation": {
                "data_access": "RAM_STORE",
                "cache_clear": true
              },
              "graph_management": {
                "graph_extent": null,
                "repository_uri": null,
                "repository_name": null,
                "repository_profile_group": null,
                "download_schedule": "0 0 0 31 2 *",
                "activation_schedule": "0 0 0 31 2 *",
                "max_backups": 0
              },           
              "profile_default": {
                "enabled": true,
                "preparation": {
                  "min_network_size": 300,
                  "methods": {
                    "lm": {
                      "enabled": false,
                      "weightings": "shortest",
                      "landmarks": 2
                    }
                  }
                },
                "execution": {
                  "methods": {
                    "lm": {
                      "active_landmarks": 2
                    }
                  }
                },
                "ext_storages": {
                  "WayCategory": {
                    "enabled": true
                  },
                  "GreenIndex": {
                    "enabled": true,
                    "filepath": "/path/to/file.csv"
                  }
                }
              },
              "profiles": {
                "car": {
                  "encoder_name": "driving-car",
                  "enabled": true,
                  "encoder_options": {},
                  "preparation": {
                    "methods": {
                      "lm": {
                        "enabled": true,
                        "threads": 5
                      }
                    }
                  },
                  "execution": {
                    "methods": {
                      "lm": {
                        "active_landmarks": 2
                      }
                    }
                  },
                  "ext_storages": {}
                },
                "hgv": {
                  "enabled": false,
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
                    "HeavyVehicle": {
                      "restrictions": true
                    }
                  }
                },
                "car-custom": {
                  "enabled": true,
                  "encoder_name": "driving-car",
                  "preparation": {
                    "min_network_size": 900
                  }
                },
                "car-custom2": {
                  "enabled": false,
                  "encoder_name": "driving-car"
                }
              }
            }""";
    EngineProperties enginePropertiesTest;
    EngineProperties defaultEngineProperties;
    HashSet<String> defaultProfilePropertiesIgnoreList = new HashSet<>(List.of("initialized", "graphsDataAccess", "elevation.dataAccess", "elevation.cacheClear", "graphManagement", "profileDefault.enabled", "profileDefault.extStorages", "profileDefault.preparation.minNetworkSize", "profileDefault.preparation.methods.lm.enabled", "profileDefault.preparation.methods.lm.weightings", "profileDefault.preparation.methods.lm.landmarks", "profileDefault.execution.methods.lm.activeLandmarks", "profiles"));

    @BeforeEach
    void setUp() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        enginePropertiesTest = mapper.readValue(testJson, EngineProperties.class);
        // Defaults to check against
        defaultEngineProperties = new EngineProperties();
        enginePropertiesTest.initProfilesMap();
    }

    @Test
    void getActiveProfilesReturnsEmptyMapWhenProfileDefaultIsNotEnabled() {
        EngineProperties engineProperties = new EngineProperties();
        engineProperties.getProfileDefault().setEnabled(false);
        Map<String, ProfileProperties> activeProfiles = engineProperties.getActiveProfiles();
        assertNotNull(activeProfiles);
        assertTrue(activeProfiles.isEmpty());
    }

    @Test
    void getActiveProfilesReturnsNonEmptyMapWhenProfileDefaultIsEnabled() {
        EngineProperties engineProperties = new EngineProperties();
        engineProperties.getProfileDefault().setEnabled(true);
        Map<String, ProfileProperties> activeProfiles = engineProperties.getActiveProfiles();
        assertNotNull(activeProfiles);
        assertFalse(activeProfiles.isEmpty());
    }

    @Test
    void getActiveProfilesReturnsCorrectProfiles() {
        EngineProperties engineProperties = new EngineProperties();
        engineProperties.getProfileDefault().setEnabled(true);
        Map<String, ProfileProperties> activeProfiles = engineProperties.getActiveProfiles();
        assertTrue(activeProfiles.containsKey("driving-car"));
        assertTrue(activeProfiles.containsKey("driving-hgv"));
        assertTrue(activeProfiles.containsKey("wheelchair"));
        assertTrue(activeProfiles.containsKey("cycling-mountain"));
        assertTrue(activeProfiles.containsKey("cycling-road"));
        assertTrue(activeProfiles.containsKey("cycling-electric"));
        assertTrue(activeProfiles.containsKey("cycling-regular"));
        assertTrue(activeProfiles.containsKey("public-transport"));
        assertTrue(activeProfiles.containsKey("foot-hiking"));
        assertTrue(activeProfiles.containsKey("foot-walking"));
    }

    @Test
    void testDeserialize() throws JsonProcessingException {
        //language=JSON
        String json = """
                {
                    "init_threads": 1,
                    "preparation_mode": true,
                    "config_output": "output_file",
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
                        "source_file": "/absolute/path/osm.pbf",
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
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        EngineProperties deserializedEngineProperties = objectMapper.readValue(json, EngineProperties.class);
        assertNotNull(deserializedEngineProperties);
        assertEquals(1, deserializedEngineProperties.getInitThreads());
        assertTrue(deserializedEngineProperties.getPreparationMode());
        assertEquals("output_file", deserializedEngineProperties.getConfigOutput());
        assertEquals(Paths.get("./graphs").toAbsolutePath(), deserializedEngineProperties.getGraphsRootPath().toAbsolutePath());
        assertEquals(DataAccessEnum.RAM_STORE, deserializedEngineProperties.getGraphsDataAccess());
        assertNotNull(deserializedEngineProperties.getElevation());
        assertNotNull(deserializedEngineProperties.getProfileDefault());
        Map<String, ProfileProperties> profiles = deserializedEngineProperties.getProfiles();
        assertNotNull(profiles);
        assertEquals(12, profiles.size());
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
        assertEquals(4, carProfile.getExtStorages().size());

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
        assertEquals(4, hgvProfile.getExtStorages().size());
    }

    @Test
    void testRawSettingEverythingElseNullGraphsDataAccess() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);
        assertEquals(DataAccessEnum.MMAP_RO, foo.getGraphsDataAccess());
    }

    @Test
    void testRawSettingEverythingElseNullProfileDefaultProperties() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);

        ProfileProperties profileProperties = foo.getProfileDefault();

//        assertTrue(assertAllNull(profileProperties, new HashSet<>()));

        EncoderOptionsProperties encoderOptions = profileProperties.getEncoderOptions();
        assertTrue(assertAllNull(encoderOptions, new HashSet<>()));

        PreparationProperties preparationProperties = profileProperties.getPreparation();
        assertEquals(300, preparationProperties.getMinNetworkSize());
        assertNull(preparationProperties.getMinOneWayNetworkSize());

        PreparationProperties.MethodsProperties methodsProperties = preparationProperties.getMethods();
        assertTrue(assertAllNull(methodsProperties, new HashSet<>(List.of("lm"))));

        PreparationProperties.MethodsProperties.LMProperties lmProperties = methodsProperties.getLm();
        assertFalse(lmProperties.isEnabled());
        assertEquals(1, lmProperties.getThreadsSave());
        assertEquals("shortest", lmProperties.getWeightings());
        assertEquals(2, lmProperties.getLandmarks());

        ExecutionProperties executionProperties = profileProperties.getExecution();
        assertTrue(assertAllNull(executionProperties.getMethods(), new HashSet<>(List.of("lm"))));

        ExecutionProperties.MethodsProperties.LMProperties lmExecutionProperties = executionProperties.getMethods().getLm();
        assertEquals(2, lmExecutionProperties.getActiveLandmarks());
        assertEquals(2, profileProperties.getExtStorages().size());
    }

    @Test
    void testRawSettingEverythingElseNullCarProfileProperties() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);

        Map<String, ProfileProperties> profiles = foo.getProfiles();
        assertEquals(14, profiles.size());
        assertTrue(profiles.containsKey("car"));

        ProfileProperties carProfile = profiles.get("car");
        assertTrue(carProfile.getEnabled());
        assertEquals(EncoderNameEnum.DRIVING_CAR, carProfile.getEncoderName());

        assertTrue(assertAllNull(carProfile, new HashSet<>(List.of("encoderName", "enabled", "encoderOptions", "preparation", "execution", "extStorages"))));

        PreparationProperties.MethodsProperties.LMProperties carLm = carProfile.getPreparation().getMethods().getLm();
        assertTrue(carLm.isEnabled());
        assertEquals(5, carLm.getThreads());

        ExecutionProperties.MethodsProperties.LMProperties carLmExecution = carProfile.getExecution().getMethods().getLm();
        assertEquals(2, carLmExecution.getActiveLandmarks());
        assertEquals(5, carProfile.getExtStorages().size());
    }

    @Test
    void testRawSettingOverwriteDefaultHgvProfileProperties() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);

        assertEquals(DataAccessEnum.MMAP_RO, foo.getGraphsDataAccess());


        Map<String, ProfileProperties> activeProfiles = foo.getActiveProfiles();
        assertEquals(12, activeProfiles.size());
        assertFalse(activeProfiles.containsKey("hgv"));

        Map<String, ProfileProperties> profiles = foo.getProfiles();
        assertEquals(14, profiles.size());
        assertTrue(profiles.containsKey("hgv"));

        ProfileProperties hgvProfile = profiles.get("hgv");
        assertFalse(hgvProfile.getEnabled());
        assertEquals(EncoderNameEnum.DRIVING_HGV, hgvProfile.getEncoderName());

        assertTrue(assertAllNull(hgvProfile, new HashSet<>(List.of("encoderName", "enabled", "encoderOptions", "preparation", "execution", "extStorages"))));

        PreparationProperties hgvPreparation = hgvProfile.getPreparation();
        assertEquals(900, hgvPreparation.getMinNetworkSize());
        assertNull(hgvPreparation.getMinOneWayNetworkSize());
        PreparationProperties.MethodsProperties hgvMethods = hgvPreparation.getMethods();

        PreparationProperties.MethodsProperties.LMProperties hgvLm = hgvMethods.getLm();
        assertTrue(hgvLm.isEnabled());
        assertEquals(1, hgvLm.getThreadsSave());

        Map<String, ExtendedStorage> hgvExtStorages = hgvProfile.getExtStorages();
        assertEquals(5, hgvExtStorages.size());
        assertTrue(hgvExtStorages.containsKey("HeavyVehicle"));

        ExtendedStorage heavyVehicle = hgvExtStorages.get("HeavyVehicle");
        assertInstanceOf(ExtendedStorage.class, heavyVehicle);
        assertTrue(heavyVehicle.getEnabled());
        assertTrue(heavyVehicle.getRestrictions());
    }

    @Test
    void testRawSettingEverythingElseNullCarCustomProfileProperties() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);

        Map<String, ProfileProperties> profiles = foo.getProfiles();
        assertEquals(14, profiles.size());
        assertTrue(profiles.containsKey("car-custom"));

        ProfileProperties carCustomProfile2 = profiles.get("car-custom2");
        assertFalse(carCustomProfile2.getEnabled());

        ProfileProperties carCustomProfile = profiles.get("car-custom");
        assertTrue(carCustomProfile.getEnabled());
        assertEquals(EncoderNameEnum.DRIVING_CAR, carCustomProfile.getEncoderName());

        assertTrue(assertAllNull(carCustomProfile, new HashSet<>(List.of("encoderName", "enabled", "encoderOptions", "preparation", "execution", "extStorages"))));

        PreparationProperties carCustomPreparation = carCustomProfile.getPreparation();
        assertEquals(900, carCustomPreparation.getMinNetworkSize());
        assertNull(carCustomPreparation.getMinOneWayNetworkSize());

        Map<String, ExtendedStorage> carCustomExtStorages = carCustomProfile.getExtStorages();
        assertEquals(5, carCustomExtStorages.size());
    }

    @Test
    void testMergeRawSettingsWithDefaultValuesCheckEngineDefaults() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException, CloneNotSupportedException {
        // Default fallback values
        boolean equal = PropertyUtils.deepEqualityCheckIsUnequal(defaultEngineProperties, enginePropertiesTest, defaultProfilePropertiesIgnoreList);
//        assertTrue(equal, "The engine properties are not equal to the default engine properties");
        // Test the raw top level settings
        assertThat(defaultEngineProperties).usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .ignoringFields(defaultProfilePropertiesIgnoreList.toArray(new String[0]))
                .isEqualTo(enginePropertiesTest);
        assertEquals(enginePropertiesTest.getGraphsDataAccess(), DataAccessEnum.MMAP_RO);
        assertEquals(enginePropertiesTest.getElevation().getDataAccess(), DataAccessEnum.RAM_STORE);
        assertEquals(enginePropertiesTest.getElevation().getCacheClear(), true);
        assertEquals(enginePropertiesTest.getProfileDefault().getEnabled(), true);
        assertEquals(enginePropertiesTest.getProfileDefault().getExtStorages().size(), 2);
        // Check that GreenIndex and WayCategory are set correctly
        assertTrue(enginePropertiesTest.getProfileDefault().getExtStorages().get("GreenIndex").getEnabled());
        assertEquals(Path.of("/path/to/file.csv"), enginePropertiesTest.getProfileDefault().getExtStorages().get("GreenIndex").getFilepath());
        assertTrue(enginePropertiesTest.getProfileDefault().getExtStorages().get("WayCategory").getEnabled());
        // Check Preparation properties
        assertEquals(300, enginePropertiesTest.getProfileDefault().getPreparation().getMinNetworkSize());
        assertFalse(enginePropertiesTest.getProfileDefault().getPreparation().getMethods().getLm().isEnabled());
        assertEquals("shortest", enginePropertiesTest.getProfileDefault().getPreparation().getMethods().getLm().getWeightings());
        assertEquals(2, enginePropertiesTest.getProfileDefault().getPreparation().getMethods().getLm().getLandmarks());
        // Check Execution properties
        assertEquals(2, enginePropertiesTest.getProfileDefault().getExecution().getMethods().getLm().getActiveLandmarks());

    }

    @Test
    void testMergeRawSettingsWithDefaultValuesCheckProfiles() {
        // Check the profiles
        Map<String, ProfileProperties> actualProfiles = enginePropertiesTest.getActiveProfiles();
        assertEquals(12, actualProfiles.size());

        // Check the defaults
        for (Map.Entry<String, ProfileProperties> profile : actualProfiles.entrySet()) {
            String profileMapKey = profile.getKey();

            ProfileProperties actualProfileProperties = profile.getValue();
            assertTrue(actualProfileProperties.getEnabled());
            if (profileMapKey.equals("car")) {
                assertEquals(EncoderNameEnum.DRIVING_CAR, actualProfileProperties.getEncoderName());
                assertTrue(actualProfileProperties.getPreparation().getMethods().getLm().isEnabled());
                assertEquals(5, actualProfileProperties.getPreparation().getMethods().getLm().getThreads());
                assertEquals(2, actualProfileProperties.getExecution().getMethods().getLm().getActiveLandmarks());
                assertEquals(5, actualProfileProperties.getExtStorages().size());
            } else if (profileMapKey.equals("car-custom")) {
                assertEquals(EncoderNameEnum.DRIVING_CAR, actualProfileProperties.getEncoderName());
                assertEquals(900, actualProfileProperties.getPreparation().getMinNetworkSize());
                assertEquals(5, actualProfileProperties.getExtStorages().size());
            } else if (profileMapKey.equals("hgv")) {
                assertEquals(EncoderNameEnum.DRIVING_HGV, actualProfileProperties.getEncoderName());
                assertEquals(900, actualProfileProperties.getPreparation().getMinNetworkSize());
                assertTrue(actualProfileProperties.getPreparation().getMethods().getLm().isEnabled());
                assertEquals(3, actualProfileProperties.getExtStorages().size());
                assertTrue(actualProfileProperties.getExtStorages().containsKey("HeavyVehicle"));
                assertTrue(actualProfileProperties.getExtStorages().get("HeavyVehicle").getRestrictions());
            } else if (profileMapKey.equals(EncoderNameEnum.PUBLIC_TRANSPORT.getName())) {
                assertTrue(actualProfileProperties.getElevation());
                assertEquals(1000000, actualProfileProperties.getMaximumVisitedNodes());
                assertEquals(Path.of(""), actualProfileProperties.getGtfsFile());
                // The profileDefault also sets it for public-transport
                assertEquals(2, actualProfileProperties.getExtStorages().size());
            } else {
                assertEquals(300, actualProfileProperties.getPreparation().getMinNetworkSize());
                assertFalse(actualProfileProperties.getPreparation().getMethods().getLm().isEnabled());
                assertEquals("shortest", actualProfileProperties.getPreparation().getMethods().getLm().getWeightings());
                assertEquals(2, actualProfileProperties.getPreparation().getMethods().getLm().getLandmarks());
                assertEquals(2, actualProfileProperties.getExecution().getMethods().getLm().getActiveLandmarks());
            }
            if (profileMapKey.equals(EncoderNameEnum.WHEELCHAIR.getName())) {
                assertEquals(50, actualProfileProperties.getMaximumSnappingRadius());
            }
            assertTrue(actualProfileProperties.getExtStorages().containsKey("WayCategory"));
            assertTrue(actualProfileProperties.getExtStorages().containsKey("GreenIndex"));
            assertTrue(actualProfileProperties.getExtStorages().get("WayCategory").getEnabled());
            assertTrue(actualProfileProperties.getExtStorages().get("GreenIndex").getEnabled());
            assertEquals(Path.of("/path/to/file.csv"), actualProfileProperties.getExtStorages().get("GreenIndex").getFilepath());


        }
    }
}