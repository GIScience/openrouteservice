package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.utils.PropertyUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class APIEnginePropertiesTest {
    @Test
    void testLoadFromEnvironmentVariables() {
        // Set the environment variables
        List<Map.Entry<String, String>> envVars = new ArrayList<>();
        envVars.add(Map.entry("ors.engine.source_file", "/path/to/file.pbf"));
        envVars.add(Map.entry("ors.engine.profile_default.preparation.methods.lm.enabled", "false"));
        envVars.add(Map.entry("ors.engine.profile_default.preparation.methods.lm.weightings", "shortest"));
        envVars.add(Map.entry("ors.engine.profiles.hgv.enabled", "false"));

        APIEngineProperties engineProperties = new APIEngineProperties();
        assertNull(engineProperties.getSourceFile());
        assertNull(engineProperties.getProfileDefault());
        assertNull(engineProperties.getProfiles());
        engineProperties.loadFromEnvironmentVariables(envVars);

        assertEquals("/path/to/file.pbf", engineProperties.getSourceFile().toString());
        assertFalse(engineProperties.getProfileDefault().getPreparation().getMethods().getLm().isEnabled());
        assertEquals("shortest", engineProperties.getProfileDefault().getPreparation().getMethods().getLm().getWeightings());
        assertFalse(engineProperties.getProfiles().get("hgv").getEnabled());
    }

    @Test
    void testLoadFromEnvironmentVariablesInitializedEngineProperties() {
        // Set the environment variables
        List<Map.Entry<String, String>> envVars = new ArrayList<>();
        envVars.add(Map.entry("ors.engine.source_file", "/foo/bar/baz.pbf"));
        envVars.add(Map.entry("ors.engine.profile_default.preparation.methods.lm.enabled", "false"));
        envVars.add(Map.entry("ors.engine.profile_default.preparation.methods.lm.weightings", "shortest"));
        envVars.add(Map.entry("ors.engine.profiles.hgv.traffic", "true"));

        APIEngineProperties engineProperties = new APIEngineProperties();
        APIEngineProperties enginePropertiesBackup = new APIEngineProperties();
        engineProperties.initialize();
        enginePropertiesBackup.initialize();

        engineProperties.loadFromEnvironmentVariables(envVars);

        assertEquals("/foo/bar/baz.pbf", engineProperties.getSourceFile().toString());
        assertFalse(engineProperties.getProfileDefault().getPreparation().getMethods().getLm().isEnabled());
        assertEquals("shortest", engineProperties.getProfileDefault().getPreparation().getMethods().getLm().getWeightings());
        assertFalse(engineProperties.getProfiles().get("hgv").getEnabled());

        // Assert deep nested objects without changes are still the same
        Set<String> excludeFields = new HashSet<>();
        excludeFields.add("source_file");
        excludeFields.add("profile_default.preparation.methods.lm.enabled");
        excludeFields.add("profile_default.preparation.methods.lm.weightings");
        excludeFields.add("profiles.hgv.traffic");
        assertFalse(PropertyUtils.deepEqualityCheckIsUnequal(engineProperties, enginePropertiesBackup, excludeFields), "Besides the exclusions, the objects should be equal.");
    }

    @Test
    void testLoadFromEnvironmentVariablesCapitalOverwrite() {
        // Set the environment variables
        List<Map.Entry<String, String>> envVars = new ArrayList<>(List.of(
                Map.entry("ORS_ENGINE_SOURCE_FILE", "/PATH/TO/FILE.PBF"),
                Map.entry("ors.engine.source_file", "/path/to/file.pbf"),
                Map.entry("ORS_ENGINE_PROFILE_DEFAULT_PREPARATION_METHODS_LM_ENABLED", "false"),
                Map.entry("ors.engine.profile_default.preparation.methods.lm.enabled", "true"),
                Map.entry("ORS_ENGINE_PROFILE_DEFAULT_PREPARATION_METHODS_LM_WEIGHTINGS", "shortest"),
                Map.entry("ors.engine.profile_default.preparation.methods.lm.weightings", "fastest"),
                Map.entry("ors.engine.profiles.hgv.enabled", "true"),
                Map.entry("profiles.hgv.enabled", "false")
        ));

        APIEngineProperties engineProperties = new APIEngineProperties();
        assertNull(engineProperties.getSourceFile());
        assertNull(engineProperties.getProfileDefault());
        assertNull(engineProperties.getProfiles());
        engineProperties.loadFromEnvironmentVariables(envVars);

        assertEquals("/PATH/TO/FILE.PBF", engineProperties.getSourceFile().toString());
        assertFalse(engineProperties.getProfileDefault().getPreparation().getMethods().getLm().isEnabled());
        assertEquals("shortest", engineProperties.getProfileDefault().getPreparation().getMethods().getLm().getWeightings());
        assertTrue(engineProperties.getProfiles().get("hgv").getEnabled());
    }

    @Test
    void convertToJson_withValidProperties_shouldReturnJsonString() {
        Properties properties = new Properties();
        properties.setProperty("source_file", "/path/to/file.pbf");

        ObjectMapper objectMapper = new ObjectMapper();
        APIEngineProperties engineProperties = objectMapper.convertValue(properties, APIEngineProperties.class);
        String json = engineProperties.convertToJson(engineProperties);

        assertNotNull(json);
        assertTrue(json.contains("\"source_file\":\"/path/to/file.pbf\""));
    }

    @Test
    void convertToJson_withInvalidProperties_shouldThrowRuntimeException() {
        APIEngineProperties properties = new APIEngineProperties() {
            @Override
            public Path getSourceFile() {
                throw new RuntimeException("Test exception");
            }
        };

        assertThrows(RuntimeException.class, () -> properties.convertToJson(properties));
    }

    @Test
    void readProperties_withValidProperties_shouldReturnAPIEngineProperties() {
        Properties properties = new Properties();
        properties.setProperty("source_file", "/path/to/file.pbf");

        APIEngineProperties engineProperties = new APIEngineProperties();
        assertNull(engineProperties.getSourceFile());

        APIEngineProperties result = engineProperties.readProperties(properties);
        assertNotNull(result);
        assertEquals("/path/to/file.pbf", result.getSourceFile().toString());
    }

    @Test
    void readProperties_withInvalidProperties_shouldThrowRuntimeException() {
        Properties properties = new Properties();
        properties.setProperty("invalid.property", "value");

        APIEngineProperties engineProperties = new APIEngineProperties();

        assertThrows(RuntimeException.class, () -> engineProperties.readProperties(properties));
    }
}