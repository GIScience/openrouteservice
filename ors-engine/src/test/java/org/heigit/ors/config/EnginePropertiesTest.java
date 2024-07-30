package org.heigit.ors.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ExecutionProperties;
import org.heigit.ors.config.profile.PreparationProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.config.profile.defaults.DefaultProfileProperties;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.heigit.ors.config.profile.storages.ExtendedStorageRoadAccessRestrictions;
import org.heigit.ors.config.profile.storages.ExtendedStorageWayCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnginePropertiesTest {

    private EngineProperties engineProperties;

    @BeforeEach
    void setUp() {
        engineProperties = new EngineProperties();
    }

    @Test
    void sourceFile_DefaultValue() {
        assertEquals(Paths.get(""), engineProperties.getSourceFile());
    }

    @Test
    void initThreads_DefaultValue() {
        assertEquals(1, engineProperties.getInitThreads());
    }

    @Test
    void preparationMode_DefaultValue() {
        assertFalse(engineProperties.getPreparationMode());
    }

    @Test
    void configOutputMode_DefaultValue() {
        assertFalse(engineProperties.getConfigOutputMode());
    }

    @Test
    void graphsRootPath_DefaultValue() {
        assertEquals(Paths.get("./graphs"), engineProperties.getGraphsRootPath());
    }

    @Test
    void graphsDataAccess_DefaultValue() {
        assertEquals(DataAccessEnum.RAM_STORE, engineProperties.getGraphsDataAccess());
    }

    @Test
    void elevation_DefaultValue() {
        assertNotNull(engineProperties.getElevation());
    }

    @Test
    void profileDefault_DefaultValue() {
        assertNotNull(engineProperties.getProfileDefault());
    }

    @Test
    void profiles_DefaultValue() {
        Map<String, ProfileProperties> profiles = engineProperties.getProfiles();
        assertNotNull(profiles);
        assertEquals(10, profiles.size());
        assertTrue(profiles.containsKey("car"));
        assertTrue(profiles.containsKey("hgv"));
        assertTrue(profiles.containsKey("bike-regular"));
        assertTrue(profiles.containsKey("bike-electric"));
        assertTrue(profiles.containsKey("bike-mountain"));
        assertTrue(profiles.containsKey("bike-road"));
        assertTrue(profiles.containsKey("walking"));
        assertTrue(profiles.containsKey("hiking"));
        assertTrue(profiles.containsKey("wheelchair"));
        assertTrue(profiles.containsKey("public-transport"));
    }

    @Test
    void testSerializeDefaultEngineProperties() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(engineProperties);
        assertNotNull(json);
        String expectedJson = "{\"source_file\":\"\",\"init_threads\":1,\"preparation_mode\":false,\"config_output_mode\":false,\"graphs_root_path\":\"./graphs\",\"graphs_data_access\":\"RAM_STORE\",\"elevation\":{\"preprocessed\":false,\"data_access\":\"MMAP\",\"cache_clear\":false,\"provider\":\"multi\",\"cache_path\":\"./elevation_cache\"},\"profile_default\":{\"enabled\":false,\"encoder_name\":\"unknown\",\"elevation\":true,\"elevation_smoothing\":true,\"encoder_flags_size\":8,\"instructions\":true,\"optimize\":false,\"traffic\":false,\"interpolate_bridges_and_tunnels\":true,\"force_turn_costs\":false,\"location_index_resolution\":500,\"location_index_search_iterations\":4,\"maximum_distance\":100000.0,\"maximum_distance_dynamic_weights\":100000.0,\"maximum_distance_avoid_areas\":100000.0,\"maximum_distance_alternative_routes\":100000.0,\"maximum_distance_round_trip_routes\":100000.0,\"maximum_speed_lower_bound\":80.0,\"maximum_way_points\":50,\"maximum_snapping_radius\":400,\"maximum_visited_nodes\":1000000,\"ext_storages\":{}},\"profiles\":{\"car\":{\"enabled\":false,\"encoder_name\":\"driving-car\",\"elevation\":true,\"elevation_smoothing\":true,\"encoder_flags_size\":8,\"instructions\":true,\"optimize\":false,\"traffic\":false,\"interpolate_bridges_and_tunnels\":true,\"force_turn_costs\":false,\"location_index_resolution\":500,\"location_index_search_iterations\":4,\"maximum_distance\":100000.0,\"maximum_distance_dynamic_weights\":100000.0,\"maximum_distance_avoid_areas\":100000.0,\"maximum_distance_alternative_routes\":100000.0,\"maximum_distance_round_trip_routes\":100000.0,\"maximum_speed_lower_bound\":80.0,\"maximum_way_points\":50,\"maximum_snapping_radius\":400,\"maximum_visited_nodes\":1000000,\"ext_storages\":{}},\"hgv\":{\"enabled\":false,\"encoder_name\":\"driving-hgv\",\"elevation\":true,\"elevation_smoothing\":true,\"encoder_flags_size\":8,\"instructions\":true,\"optimize\":false,\"traffic\":false,\"interpolate_bridges_and_tunnels\":true,\"force_turn_costs\":false,\"location_index_resolution\":500,\"location_index_search_iterations\":4,\"maximum_distance\":100000.0,\"maximum_distance_dynamic_weights\":100000.0,\"maximum_distance_avoid_areas\":100000.0,\"maximum_distance_alternative_routes\":100000.0,\"maximum_distance_round_trip_routes\":100000.0,\"maximum_speed_lower_bound\":80.0,\"maximum_way_points\":50,\"maximum_snapping_radius\":400,\"maximum_visited_nodes\":1000000,\"ext_storages\":{}},\"bike-regular\":{\"enabled\":false,\"encoder_name\":\"cycling-regular\",\"elevation\":true,\"elevation_smoothing\":true,\"encoder_flags_size\":8,\"instructions\":true,\"optimize\":false,\"traffic\":false,\"interpolate_bridges_and_tunnels\":true,\"force_turn_costs\":false,\"location_index_resolution\":500,\"location_index_search_iterations\":4,\"maximum_distance\":100000.0,\"maximum_distance_dynamic_weights\":100000.0,\"maximum_distance_avoid_areas\":100000.0,\"maximum_distance_alternative_routes\":100000.0,\"maximum_distance_round_trip_routes\":100000.0,\"maximum_speed_lower_bound\":80.0,\"maximum_way_points\":50,\"maximum_snapping_radius\":400,\"maximum_visited_nodes\":1000000,\"ext_storages\":{}},\"bike-electric\":{\"enabled\":false,\"encoder_name\":\"cycling-electric\",\"elevation\":true,\"elevation_smoothing\":true,\"encoder_flags_size\":8,\"instructions\":true,\"optimize\":false,\"traffic\":false,\"interpolate_bridges_and_tunnels\":true,\"force_turn_costs\":false,\"location_index_resolution\":500,\"location_index_search_iterations\":4,\"maximum_distance\":100000.0,\"maximum_distance_dynamic_weights\":100000.0,\"maximum_distance_avoid_areas\":100000.0,\"maximum_distance_alternative_routes\":100000.0,\"maximum_distance_round_trip_routes\":100000.0,\"maximum_speed_lower_bound\":80.0,\"maximum_way_points\":50,\"maximum_snapping_radius\":400,\"maximum_visited_nodes\":1000000,\"ext_storages\":{}},\"bike-mountain\":{\"enabled\":false,\"encoder_name\":\"cycling-mountain\",\"elevation\":true,\"elevation_smoothing\":true,\"encoder_flags_size\":8,\"instructions\":true,\"optimize\":false,\"traffic\":false,\"interpolate_bridges_and_tunnels\":true,\"force_turn_costs\":false,\"location_index_resolution\":500,\"location_index_search_iterations\":4,\"maximum_distance\":100000.0,\"maximum_distance_dynamic_weights\":100000.0,\"maximum_distance_avoid_areas\":100000.0,\"maximum_distance_alternative_routes\":100000.0,\"maximum_distance_round_trip_routes\":100000.0,\"maximum_speed_lower_bound\":80.0,\"maximum_way_points\":50,\"maximum_snapping_radius\":400,\"maximum_visited_nodes\":1000000,\"ext_storages\":{}},\"bike-road\":{\"enabled\":false,\"encoder_name\":\"cycling-road\",\"elevation\":true,\"elevation_smoothing\":true,\"encoder_flags_size\":8,\"instructions\":true,\"optimize\":false,\"traffic\":false,\"interpolate_bridges_and_tunnels\":true,\"force_turn_costs\":false,\"location_index_resolution\":500,\"location_index_search_iterations\":4,\"maximum_distance\":100000.0,\"maximum_distance_dynamic_weights\":100000.0,\"maximum_distance_avoid_areas\":100000.0,\"maximum_distance_alternative_routes\":100000.0,\"maximum_distance_round_trip_routes\":100000.0,\"maximum_speed_lower_bound\":80.0,\"maximum_way_points\":50,\"maximum_snapping_radius\":400,\"maximum_visited_nodes\":1000000,\"ext_storages\":{}},\"walking\":{\"enabled\":false,\"encoder_name\":\"foot-walking\",\"elevation\":true,\"elevation_smoothing\":true,\"encoder_flags_size\":8,\"instructions\":true,\"optimize\":false,\"traffic\":false,\"interpolate_bridges_and_tunnels\":true,\"force_turn_costs\":false,\"location_index_resolution\":500,\"location_index_search_iterations\":4,\"maximum_distance\":100000.0,\"maximum_distance_dynamic_weights\":100000.0,\"maximum_distance_avoid_areas\":100000.0,\"maximum_distance_alternative_routes\":100000.0,\"maximum_distance_round_trip_routes\":100000.0,\"maximum_speed_lower_bound\":80.0,\"maximum_way_points\":50,\"maximum_snapping_radius\":400,\"maximum_visited_nodes\":1000000,\"ext_storages\":{}},\"hiking\":{\"enabled\":false,\"encoder_name\":\"foot-hiking\",\"elevation\":true,\"elevation_smoothing\":true,\"encoder_flags_size\":8,\"instructions\":true,\"optimize\":false,\"traffic\":false,\"interpolate_bridges_and_tunnels\":true,\"force_turn_costs\":false,\"location_index_resolution\":500,\"location_index_search_iterations\":4,\"maximum_distance\":100000.0,\"maximum_distance_dynamic_weights\":100000.0,\"maximum_distance_avoid_areas\":100000.0,\"maximum_distance_alternative_routes\":100000.0,\"maximum_distance_round_trip_routes\":100000.0,\"maximum_speed_lower_bound\":80.0,\"maximum_way_points\":50,\"maximum_snapping_radius\":400,\"maximum_visited_nodes\":1000000,\"ext_storages\":{}},\"wheelchair\":{\"enabled\":false,\"encoder_name\":\"wheelchair\",\"elevation\":true,\"elevation_smoothing\":true,\"encoder_flags_size\":8,\"instructions\":true,\"optimize\":false,\"traffic\":false,\"interpolate_bridges_and_tunnels\":true,\"force_turn_costs\":false,\"location_index_resolution\":500,\"location_index_search_iterations\":4,\"maximum_distance\":100000.0,\"maximum_distance_dynamic_weights\":100000.0,\"maximum_distance_avoid_areas\":100000.0,\"maximum_distance_alternative_routes\":100000.0,\"maximum_distance_round_trip_routes\":100000.0,\"maximum_speed_lower_bound\":80.0,\"maximum_way_points\":50,\"maximum_snapping_radius\":50,\"maximum_visited_nodes\":1000000,\"ext_storages\":{}},\"public-transport\":{\"enabled\":false,\"encoder_name\":\"public-transport\",\"elevation\":true,\"elevation_smoothing\":true,\"encoder_flags_size\":8,\"instructions\":true,\"optimize\":false,\"traffic\":false,\"interpolate_bridges_and_tunnels\":true,\"force_turn_costs\":false,\"location_index_resolution\":500,\"location_index_search_iterations\":4,\"maximum_distance\":100000.0,\"maximum_distance_dynamic_weights\":100000.0,\"maximum_distance_avoid_areas\":100000.0,\"maximum_distance_alternative_routes\":100000.0,\"maximum_distance_round_trip_routes\":100000.0,\"maximum_speed_lower_bound\":80.0,\"maximum_way_points\":50,\"maximum_snapping_radius\":400,\"maximum_visited_nodes\":1000000}}}";
        assertEquals(expectedJson, json);
    }

    @Test
    void testDeserialize() throws JsonProcessingException {
        String json = "{\"source_file\":\"/absolute/path/osm.pbf\",\"init_threads\":1,\"preparation_mode\":true,\"config_output_mode\":true,\"graphs_root_path\":\"./graphs\",\"graphs_data_access\":\"RAM_STORE\",\"elevation\":{\"preprocessed\":true,\"data_access\":\"MMAP\",\"cache_clear\":true,\"provider\":\"multi\",\"cache_path\":\"./elevation_cache\"},\"profile_default\":{\"enabled\":true,\"encoder_name\":\"unknown\",\"elevation\":true,\"elevation_smoothing\":true,\"encoder_flags_size\":8,\"instructions\":true,\"optimize\":true,\"traffic\":true,\"interpolate_bridges_and_tunnels\":true,\"force_turn_costs\":true,\"location_index_resolution\":400,\"location_index_search_iterations\":2,\"maximum_distance\":200000.0,\"maximum_distance_dynamic_weights\":200000.0,\"maximum_distance_avoid_areas\":200000.0,\"maximum_distance_alternative_routes\":200000.0,\"maximum_distance_round_trip_routes\":200000.0,\"maximum_speed_lower_bound\":10.0,\"maximum_way_points\":20,\"maximum_snapping_radius\":100,\"maximum_visited_nodes\":5000000,\"ext_storages\":{}},\"profiles\":{\"car\":{\"enabled\":true,\"encoder_name\":\"driving-car\",\"elevation\":true,\"elevation_smoothing\":true,\"encoder_flags_size\":1,\"instructions\":true,\"optimize\":true,\"traffic\":true,\"interpolate_bridges_and_tunnels\":true,\"force_turn_costs\":true,\"location_index_resolution\":700,\"location_index_search_iterations\":1,\"maximum_distance\":400000.0,\"maximum_distance_dynamic_weights\":200000.0,\"maximum_distance_avoid_areas\":300000.0,\"maximum_distance_alternative_routes\":600000.0,\"maximum_distance_round_trip_routes\":200000.0,\"maximum_speed_lower_bound\":20.0,\"maximum_way_points\":30,\"maximum_snapping_radius\":300,\"maximum_visited_nodes\":2000000,\"ext_storages\":{}},\"hgv\":{\"enabled\":true,\"encoder_name\":\"driving-hgv\",\"elevation\":true,\"elevation_smoothing\":true,\"encoder_flags_size\":8,\"instructions\":true,\"optimize\":true,\"traffic\":true,\"interpolate_bridges_and_tunnels\":true,\"force_turn_costs\":true,\"location_index_resolution\":500,\"location_index_search_iterations\":4,\"maximum_distance\":100000.0,\"maximum_distance_dynamic_weights\":100000.0,\"maximum_distance_avoid_areas\":100000.0,\"maximum_distance_alternative_routes\":100000.0,\"maximum_distance_round_trip_routes\":100000.0,\"maximum_speed_lower_bound\":80.0,\"maximum_way_points\":50,\"maximum_snapping_radius\":400,\"maximum_visited_nodes\":1000000,\"ext_storages\":{}}}}";
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
    void deserializeOneCompleteProfile() throws JsonProcessingException {
        DefaultProfileProperties defaultProfileProperties = new DefaultProfileProperties();
        // Cast to (ExtendedStorageWayCategoryTest)
        String json = "{\n" +
                "    \"source_file\": \"ors-api/src/test/files/heidelberg.osm.gz\",\n" +
                "    \"config_output_mode\": true,\n" +
                "    \"graphs_root_path\": \"./graphs\",\n" +
                "    \"graphs_data_access\": \"RAM_STORE\",\n" +
                "    \"elevation\": {\n" +
                "      \"cache_path\": \"./elevation_cache\"\n" +
                "    },\n" +
                "    \"profile_default\": {\n" +
                "      \"enabled\": true,\n" +
                "      \"preparation\": {\n" +
                "        \"min_network_size\": 300,\n" +
                "        \"methods\": {\n" +
                "          \"lm\": {\n" +
                "            \"enabled\": true,\n" +
                "            \"threads\": 4,\n" +
                "            \"weightings\": \"shortest\",\n" +
                "            \"landmarks\": 2\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"execution\": {\n" +
                "        \"methods\": {\n" +
                "          \"lm\": {\n" +
                "            \"active_landmarks\": 8\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"profiles\": {\n" +
                "      \"car\": {\n" +
                "        \"enabled\": true,\n" +
                "        \"encoder_name\": \"driving-car\",\n" +
                "        \"encoder_options\": {\n" +
                "          \"turn_costs\": true\n" +
                "        },\n" +
                "        \"preparation\": {\n" +
                "          \"min_network_size\": 900,\n" +
                "          \"methods\": {\n" +
                "            \"ch\": {\n" +
                "              \"enabled\": true,\n" +
                "              \"threads\": 1,\n" +
                "              \"weightings\": \"fastest\"\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"execution\": {\n" +
                "          \"methods\": {\n" +
                "            \"lm\": {\n" +
                "              \"active_landmarks\": 2\n" +
                "            },\n" +
                "            \"core\": {\n" +
                "              \"active_landmarks\": 6\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"ext_storages\": {\n" +
                "          \"WayCategory\": {},\n" +
                "          \"RoadAccessRestrictions\": {\n" +
                "            \"use_for_warnings\": true\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "}";
        ObjectMapper objectMapper = new ObjectMapper();
        EngineProperties deserializedEngineProperties = objectMapper.readValue(json, EngineProperties.class);
        assertNotNull(deserializedEngineProperties);
        assertEquals(Paths.get("ors-api/src/test/files/heidelberg.osm.gz").toAbsolutePath(), deserializedEngineProperties.getSourceFile());
        assertFalse(deserializedEngineProperties.getPreparationMode());
        assertTrue(deserializedEngineProperties.getConfigOutputMode());
        assertEquals(Paths.get("./graphs").toAbsolutePath(), deserializedEngineProperties.getGraphsRootPath());
        assertEquals(DataAccessEnum.RAM_STORE, deserializedEngineProperties.getGraphsDataAccess());
        assertNotNull(deserializedEngineProperties.getElevation());
        assertNotNull(deserializedEngineProperties.getProfileDefault());

        ProfileProperties deserializedDefaultProfile = deserializedEngineProperties.getProfileDefault();
        // Check default overrides
        assertTrue(deserializedDefaultProfile.getEnabled());
        assertEquals(300, deserializedDefaultProfile.getPreparation().getMinNetworkSize());
        assertNull(deserializedDefaultProfile.getPreparation().getMethods().getCh());
        assertNull(deserializedDefaultProfile.getPreparation().getMethods().getCore());
        assertNull(deserializedDefaultProfile.getPreparation().getMethods().getFastisochrones());

        // Check Defaults
        assertEquals(defaultProfileProperties.getElevation(), deserializedDefaultProfile.getElevation());
        assertEquals(defaultProfileProperties.getElevationSmoothing(), deserializedDefaultProfile.getElevationSmoothing());
        assertEquals(defaultProfileProperties.getEncoderFlagsSize(), deserializedDefaultProfile.getEncoderFlagsSize());
        assertEquals(defaultProfileProperties.getInstructions(), deserializedDefaultProfile.getInstructions());
        assertEquals(defaultProfileProperties.getOptimize(), deserializedDefaultProfile.getOptimize());
        assertEquals(defaultProfileProperties.getTraffic(), deserializedDefaultProfile.getTraffic());
        assertEquals(defaultProfileProperties.getInterpolateBridgesAndTunnels(), deserializedDefaultProfile.getInterpolateBridgesAndTunnels());
        assertEquals(defaultProfileProperties.getForceTurnCosts(), deserializedDefaultProfile.getForceTurnCosts());
        assertEquals(defaultProfileProperties.getLocationIndexResolution(), deserializedDefaultProfile.getLocationIndexResolution());
        assertEquals(defaultProfileProperties.getLocationIndexSearchIterations(), deserializedDefaultProfile.getLocationIndexSearchIterations());
        assertEquals(defaultProfileProperties.getMaximumDistance(), deserializedDefaultProfile.getMaximumDistance());
        assertEquals(defaultProfileProperties.getMaximumDistanceDynamicWeights(), deserializedDefaultProfile.getMaximumDistanceDynamicWeights());
        assertEquals(defaultProfileProperties.getMaximumDistanceAvoidAreas(), deserializedDefaultProfile.getMaximumDistanceAvoidAreas());
        assertEquals(defaultProfileProperties.getMaximumDistanceAlternativeRoutes(), deserializedDefaultProfile.getMaximumDistanceAlternativeRoutes());
        assertEquals(defaultProfileProperties.getMaximumDistanceRoundTripRoutes(), deserializedDefaultProfile.getMaximumDistanceRoundTripRoutes());
        assertEquals(defaultProfileProperties.getMaximumSpeedLowerBound(), deserializedDefaultProfile.getMaximumSpeedLowerBound());
        assertEquals(defaultProfileProperties.getMaximumWayPoints(), deserializedDefaultProfile.getMaximumWayPoints());
        assertEquals(defaultProfileProperties.getMaximumSnappingRadius(), deserializedDefaultProfile.getMaximumSnappingRadius());
        assertEquals(defaultProfileProperties.getMaximumVisitedNodes(), deserializedDefaultProfile.getMaximumVisitedNodes());
        assertEquals(0, deserializedDefaultProfile.getExtStorages().size());


        PreparationProperties.MethodsProperties lmMethods = deserializedDefaultProfile.getPreparation().getMethods();
        assertNotNull(lmMethods);
        assertTrue(lmMethods.getLm().isEnabled());
        assertEquals(4, lmMethods.getLm().getThreads());
        assertEquals("shortest", lmMethods.getLm().getWeightings());
        assertEquals(2, lmMethods.getLm().getLandmarks());

        assertNull(deserializedDefaultProfile.getExecution().getMethods().getAstar());
        assertNull(deserializedDefaultProfile.getExecution().getMethods().getCore());

        ExecutionProperties.MethodsProperties lmExecutionMethods = deserializedDefaultProfile.getExecution().getMethods();
        assertNotNull(lmExecutionMethods);
        assertEquals(8, lmExecutionMethods.getLm().getActiveLandmarks());

        Map<String, ProfileProperties> profiles = deserializedEngineProperties.getProfiles();
        assertNotNull(profiles);
        assertEquals(1, profiles.size());
        assertTrue(profiles.containsKey("car"));

        ProfileProperties deserializedCarProfile = profiles.get("car");
        assertNotNull(deserializedCarProfile);
        assertTrue(deserializedCarProfile.getEnabled());
        assertEquals(EncoderNameEnum.DRIVING_CAR, deserializedCarProfile.getEncoderName());
        assertNotNull(deserializedCarProfile.getEncoderOptions());
        assertTrue(deserializedCarProfile.getEncoderOptions().getTurnCosts());
        assertEquals(900, deserializedCarProfile.getPreparation().getMinNetworkSize());
        assertNull(deserializedCarProfile.getPreparation().getMethods().getLm());
        assertNull(deserializedCarProfile.getPreparation().getMethods().getFastisochrones());
        assertNotNull(deserializedCarProfile.getPreparation().getMethods().getCh());

        PreparationProperties.MethodsProperties.CHProperties ch = deserializedCarProfile.getPreparation().getMethods().getCh();
        assertNotNull(ch);
        assertTrue(ch.isEnabled());
        assertEquals(1, ch.getThreads());
        assertEquals("fastest", ch.getWeightings());

        ExecutionProperties.MethodsProperties executionMethods = deserializedCarProfile.getExecution().getMethods();
        assertNotNull(executionMethods);
        assertNull(executionMethods.getAstar());
        assertNotNull(executionMethods.getLm());
        assertEquals(2, executionMethods.getLm().getActiveLandmarks());
        assertNotNull(executionMethods.getCore());
        assertEquals(6, executionMethods.getCore().getActiveLandmarks());


        Map<String, ExtendedStorage> extStorages = deserializedCarProfile.getExtStorages();
        assertNotNull(extStorages);
        assertEquals(2, extStorages.size());
        assertTrue(extStorages.containsKey("WayCategory"));
        assertTrue(extStorages.containsKey("RoadAccessRestrictions"));

        ExtendedStorageWayCategory wayCategory = (ExtendedStorageWayCategory) extStorages.get("WayCategory");
        assertNotNull(wayCategory);
        assertTrue(wayCategory.getEnabled());

        ExtendedStorageRoadAccessRestrictions roadAccessRestrictions = (ExtendedStorageRoadAccessRestrictions) extStorages.get("RoadAccessRestrictions");
        assertNotNull(roadAccessRestrictions);
        assertTrue(roadAccessRestrictions.getEnabled());
        assertTrue(roadAccessRestrictions.getUseForWarnings());

    }

}