package org.heigit.ors.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.defaults.DefaultEngineProperties;
import org.heigit.ors.config.defaults.DefaultProfiles;
import org.heigit.ors.config.profile.EncoderOptionsProperties;
import org.heigit.ors.config.profile.ExecutionProperties;
import org.heigit.ors.config.profile.PreparationProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.heigit.ors.config.utils.PropertyUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.heigit.ors.config.utils.PropertyUtils.assertAllNull;
import static org.junit.jupiter.api.Assertions.*;

class EnginePropertiesTest {

    ;
    //language=JSON
    private final String testJson = """
            {
              "graphs_data_access": "MMAP_RO",
              "elevation": {
                "data_access": "RAM_STORE",
                "cache_clear": true
              },
              "graph_management": null,
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
                  "enabled": false,
                  "encoder_name": "driving-car",
                  "preparation": {
                    "min_network_size": 900
                  }
                }
              }
            }""";
    EngineProperties enginePropertiesTest;
    EngineProperties defaultEngineProperties;
    HashSet<String> defaultProfilePropertiesIgnoreList = new HashSet<>(List.of("initialized", "graphsDataAccess", "elevation.dataAccess", "elevation.cacheClear", "graphManagement", "profileDefault.enabled", "profileDefault.extStorages", "profileDefault.preparation.minNetworkSize", "profileDefault.preparation.methods.lm.enabled", "profileDefault.preparation.methods.lm.weightings", "profileDefault.preparation.methods.lm.landmarks", "profileDefault.execution.methods.lm.activeLandmarks", "profiles"));

    @BeforeEach
    void setUp() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        enginePropertiesTest = mapper.readValue(testJson, EngineProperties.class);
        // Defaults to check against
        defaultEngineProperties = new DefaultEngineProperties(true);
        enginePropertiesTest.initialize();
    }

    @Test
    void testEmptyConstructor() {
        EngineProperties engineProperties = new EngineProperties();
        assertNotNull(engineProperties);
        assertNull(engineProperties.getSourceFile());
        assertNull(engineProperties.getInitThreads());
        assertNull(engineProperties.getPreparationMode());
        assertNull(engineProperties.getConfigOutput());
        assertNull(engineProperties.getGraphsRootPath());
        assertNull(engineProperties.getGraphsDataAccess());
        assertNull(engineProperties.getElevation());
        assertNull(engineProperties.getProfileDefault());
        assertNull(engineProperties.getProfiles());
    }

    @Test
    void getActiveProfilesReturnsNonEmptyMapWhenInitialized() {
        EngineProperties engineProperties = new EngineProperties();
        engineProperties.initialize();
        Map<String, ProfileProperties> activeProfiles = engineProperties.getActiveProfiles();
        assertNotNull(activeProfiles);
        assertFalse(activeProfiles.isEmpty());
    }

    @Test
    void getActiveProfilesReturnsNonEmptyMapWhenNotInitialized() {
        EngineProperties engineProperties = new EngineProperties();
        assertNull(engineProperties.getProfiles());
        Map<String, ProfileProperties> activeProfiles = engineProperties.getActiveProfiles();
        assertNotNull(activeProfiles);
        assertFalse(activeProfiles.isEmpty());
    }

    @Test
    void getActiveProfilesReturnsCorrectProfiles() {
        EngineProperties engineProperties = new EngineProperties();
        Map<String, ProfileProperties> activeProfiles = engineProperties.getActiveProfiles();
        assertTrue(activeProfiles.containsKey("car"));
        assertTrue(activeProfiles.containsKey("hgv"));
        assertTrue(activeProfiles.containsKey("wheelchair"));
        assertTrue(activeProfiles.containsKey("bike-mountain"));
        assertTrue(activeProfiles.containsKey("bike-road"));
        assertTrue(activeProfiles.containsKey("bike-electric"));
        assertTrue(activeProfiles.containsKey("bike-regular"));
        assertTrue(activeProfiles.containsKey("public-transport"));
        assertTrue(activeProfiles.containsKey("hiking"));
        assertTrue(activeProfiles.containsKey("walking"));
    }

    @Test
    void getActiveProfilesDoesNotReinitializeIfAlreadyInitialized() {
        enginePropertiesTest.initialize();
        Map<String, ProfileProperties> firstCall = enginePropertiesTest.getActiveProfiles();
        Map<String, ProfileProperties> secondCall = enginePropertiesTest.getActiveProfiles();
        assertSame(firstCall, secondCall);
    }

    @Test
    void testSerializeEmptyEngineProperties() throws JsonProcessingException {
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
                    "config_output": null,
                    "graphs_root_path": null,
                    "graphs_data_access": null,
                    "elevation": null,
                    "graph_management": null,
                    "profile_default": null,
                    "profiles": null
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
        assertEquals("output_file", deserializedEngineProperties.getConfigOutput());
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
    void testRawSettingEverythingElseNullGraphsDataAccess() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);
        assertEquals(DataAccessEnum.MMAP_RO, foo.getGraphsDataAccess());
    }

    @Test
    void testRawSettingEverythingElseNullElevationProperties() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);

        ElevationProperties elevationProperties = foo.getElevation();
        assertTrue(assertAllNull(elevationProperties, Set.of("dataAccess", "cacheClear")));
    }

    @Test
    void testRawSettingEverythingElseNullProfileDefaultProperties() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);

        ProfileProperties profileProperties = foo.getProfileDefault();

        assertTrue(assertAllNull(profileProperties, new HashSet<>()));

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
        assertEquals(3, profiles.size());
        assertTrue(profiles.containsKey("car"));

        ProfileProperties carProfile = profiles.get("car");
        assertTrue(carProfile.getEnabled());
        assertEquals(EncoderNameEnum.DRIVING_CAR, carProfile.getEncoderName());

        assertTrue(assertAllNull(carProfile, new HashSet<>(List.of("encoderName", "enabled", "encoderOptions", "preparation", "execution", "extStorages"))));

        EncoderOptionsProperties carEncoderOptions = carProfile.getEncoderOptions();
        assertTrue(assertAllNull(carEncoderOptions, new HashSet<>()));

        PreparationProperties carPreparation = carProfile.getPreparation();
        assertTrue(assertAllNull(carPreparation, new HashSet<>(List.of("methods"))));

        PreparationProperties.MethodsProperties carMethods = carPreparation.getMethods();
        assertTrue(assertAllNull(carMethods, new HashSet<>(List.of("lm"))));

        PreparationProperties.MethodsProperties.LMProperties carLm = carMethods.getLm();
        assertTrue(assertAllNull(carLm, new HashSet<>(List.of("enabled", "threads"))));
        assertTrue(carLm.isEnabled());
        assertEquals(5, carLm.getThreads());

        ExecutionProperties carExecution = carProfile.getExecution();
        assertTrue(assertAllNull(carExecution.getMethods(), new HashSet<>(List.of("lm"))));

        ExecutionProperties.MethodsProperties.LMProperties carLmExecution = carExecution.getMethods().getLm();
        assertEquals(2, carLmExecution.getActiveLandmarks());
        assertEquals(0, carProfile.getExtStorages().size());
    }

    @Test
    void testRawSettingEverythingElseNullHgvProfileProperties() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException {
        ObjectMapper mapper = new ObjectMapper();
        EngineProperties foo = mapper.readValue(testJson, EngineProperties.class);

        assertTrue(assertAllNull(foo, defaultProfilePropertiesIgnoreList));
        assertEquals(DataAccessEnum.MMAP_RO, foo.getGraphsDataAccess());


        Map<String, ProfileProperties> profiles = foo.getProfiles();
        assertEquals(3, profiles.size());
        assertTrue(profiles.containsKey("hgv"));

        ProfileProperties hgvProfile = profiles.get("hgv");
        assertFalse(hgvProfile.getEnabled());
        assertEquals(EncoderNameEnum.DRIVING_HGV, hgvProfile.getEncoderName());

        assertTrue(assertAllNull(hgvProfile, new HashSet<>(List.of("encoderName", "enabled", "encoderOptions", "preparation", "execution", "extStorages"))));

        EncoderOptionsProperties hgvEncoderOptions = hgvProfile.getEncoderOptions();
        assertTrue(assertAllNull(hgvEncoderOptions, new HashSet<>()));

        PreparationProperties hgvPreparation = hgvProfile.getPreparation();
        assertEquals(900, hgvPreparation.getMinNetworkSize());
        assertNull(hgvPreparation.getMinOneWayNetworkSize());
        PreparationProperties.MethodsProperties hgvMethods = hgvPreparation.getMethods();
        assertTrue(assertAllNull(hgvMethods, new HashSet<>(List.of("lm"))));
        assertTrue(assertAllNull(hgvMethods.getLm(), new HashSet<>(List.of("enabled"))));

        PreparationProperties.MethodsProperties hgvMethodsProperties = hgvPreparation.getMethods();
        assertTrue(assertAllNull(hgvMethodsProperties, new HashSet<>(List.of("lm"))));

        PreparationProperties.MethodsProperties.LMProperties hgvLm = hgvMethods.getLm();
        assertTrue(assertAllNull(hgvLm, new HashSet<>(List.of("enabled"))));
        assertTrue(hgvLm.isEnabled());
        assertEquals(1, hgvLm.getThreadsSave());

        ExecutionProperties hgvExecution = hgvProfile.getExecution();
        assertTrue(assertAllNull(hgvExecution.getMethods(), new HashSet<>(List.of("lm"))));

        Map<String, ExtendedStorage> hgvExtStorages = hgvProfile.getExtStorages();
        assertEquals(1, hgvExtStorages.size());
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
        assertEquals(3, profiles.size());
        assertTrue(profiles.containsKey("car-custom"));

        ProfileProperties carCustomProfile = profiles.get("car-custom");
        assertFalse(carCustomProfile.getEnabled());
        assertEquals(EncoderNameEnum.DRIVING_CAR, carCustomProfile.getEncoderName());

        assertTrue(assertAllNull(carCustomProfile, new HashSet<>(List.of("encoderName", "enabled", "encoderOptions", "preparation", "execution", "extStorages"))));

        EncoderOptionsProperties carCustomEncoderOptions = carCustomProfile.getEncoderOptions();
        assertTrue(assertAllNull(carCustomEncoderOptions, new HashSet<>()));

        PreparationProperties carCustomPreparation = carCustomProfile.getPreparation();
        assertEquals(900, carCustomPreparation.getMinNetworkSize());
        assertNull(carCustomPreparation.getMinOneWayNetworkSize());
        assertTrue(assertAllNull(carCustomPreparation.getMethods(), new HashSet<>()));

        ExecutionProperties carCustomExecution = carCustomProfile.getExecution();
        assertTrue(assertAllNull(carCustomExecution.getMethods(), new HashSet<>(List.of("lm"))));

        Map<String, ExtendedStorage> carCustomExtStorages = carCustomProfile.getExtStorages();
        assertNull(carCustomExtStorages);
    }

    @Test
    void testMergeRawSettingsWithDefaultValuesCheckEngineDefaults() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException, CloneNotSupportedException {
        // Default fallback values
        boolean equal = PropertyUtils.deepEqualityCheckIsUnequal(defaultEngineProperties, enginePropertiesTest, defaultProfilePropertiesIgnoreList);
        // Test the raw top level settings
        assertTrue(equal, "The engine properties are not equal to the default engine properties");
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
    void testMergeRawSettingsWithDefaultValuesCheckProfiles() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException, CloneNotSupportedException {
        // Check the profiles
        Map<String, ProfileProperties> defaultProfiles = new DefaultProfiles(true).getProfiles();
        Map<String, ProfileProperties> actualProfiles = enginePropertiesTest.getProfiles();
        assertEquals(defaultProfiles.size() + 1, actualProfiles.size());


        // Check the defaults
        for (Map.Entry<String, ProfileProperties> profile : actualProfiles.entrySet()) {
            String profileMapKey = profile.getKey();

            ProfileProperties actualProfileProperties = profile.getValue();
            assertEquals("shortest", actualProfileProperties.getPreparation().getMethods().getLm().getWeightings());
            assertEquals(2, actualProfileProperties.getPreparation().getMethods().getLm().getLandmarks());
            assertEquals(2, actualProfileProperties.getExecution().getMethods().getLm().getActiveLandmarks());
            if (profileMapKey.equals("car")) {
                assertTrue(actualProfileProperties.getPreparation().getMethods().getLm().isEnabled());
                assertEquals(0, actualProfileProperties.getExtStorages().size());
            } else if (profileMapKey.equals("car-custom")) {
                assertFalse(actualProfileProperties.getEnabled());
                assertEquals(EncoderNameEnum.DRIVING_CAR, actualProfileProperties.getEncoderName());
                assertEquals(900, actualProfileProperties.getPreparation().getMinNetworkSize());
                assertEquals(2, actualProfileProperties.getExtStorages().size());
                assertTrue(actualProfileProperties.getExtStorages().containsKey("WayCategory"));
                assertTrue(actualProfileProperties.getExtStorages().containsKey("GreenIndex"));
                assertTrue(actualProfileProperties.getExtStorages().get("WayCategory").getEnabled());
                assertTrue(actualProfileProperties.getExtStorages().get("GreenIndex").getEnabled());
                assertEquals(Path.of("/path/to/file.csv"), actualProfileProperties.getExtStorages().get("GreenIndex").getFilepath());
            } else if (profileMapKey.equals("hgv")) {
                assertEquals(1, actualProfileProperties.getExtStorages().size());
                assertEquals(900, actualProfileProperties.getPreparation().getMinNetworkSize());
                assertTrue(actualProfileProperties.getExtStorages().containsKey("HeavyVehicle"));
                assertTrue(actualProfileProperties.getExtStorages().get("HeavyVehicle").getRestrictions());
            } else if (profileMapKey.equals(EncoderNameEnum.PUBLIC_TRANSPORT.getName())) {
                assertTrue(actualProfileProperties.getElevation());
                assertEquals(1000000, actualProfileProperties.getMaximumVisitedNodes());
                assertEquals(Path.of(""), actualProfileProperties.getGtfsFile());
                // The profileDefault also sets it for public-transport
                assertEquals(2, actualProfileProperties.getExtStorages().size());
            } else {
                assertTrue(actualProfileProperties.getEnabled());
                assertEquals(300, actualProfileProperties.getPreparation().getMinNetworkSize());
                assertFalse(actualProfileProperties.getPreparation().getMethods().getLm().isEnabled());
                assertEquals(2, actualProfileProperties.getExtStorages().size());
                assertTrue(actualProfileProperties.getExtStorages().containsKey("WayCategory"));
                assertTrue(actualProfileProperties.getExtStorages().containsKey("GreenIndex"));
                assertTrue(actualProfileProperties.getExtStorages().get("WayCategory").getEnabled());
                assertTrue(actualProfileProperties.getExtStorages().get("GreenIndex").getEnabled());
                assertEquals(Path.of("/path/to/file.csv"), actualProfileProperties.getExtStorages().get("GreenIndex").getFilepath());
            }

            if (profileMapKey.equals(EncoderNameEnum.WHEELCHAIR.getName())) {
                assertEquals(50, actualProfileProperties.getMaximumSnappingRadius());
            }
        }
    }
}