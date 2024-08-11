package org.heigit.ors.api.config.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.EngineProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvironmentVariableParserTest {

    ObjectMapper mapper = new ObjectMapper();
    List<String> relevantPrefixes = List.of("ors.engine.", "ORS_ENGINE_");

    @Test
    void testEmptyEnvVarsReturnsEmptyList() {
        assertEquals(new ArrayList<>(), EnvironmentVariableParser.validateEnvironmentVariablesAgainstClassJson("{}", null, null));
        assertEquals(new ArrayList<>(), EnvironmentVariableParser.validateEnvironmentVariablesAgainstClassJson("{}", null, new ArrayList<>()));
        assertEquals(new ArrayList<>(), EnvironmentVariableParser.validateEnvironmentVariablesAgainstClassJson("{}", new ArrayList<>(), null));
        assertEquals(new ArrayList<>(), EnvironmentVariableParser.validateEnvironmentVariablesAgainstClassJson("{}", new ArrayList<>(), new ArrayList<>()));
        assertEquals(new ArrayList<>(), EnvironmentVariableParser.validateEnvironmentVariablesAgainstClassJson("{}", null, List.of(Map.entry("ors.engine.source_file", "/some/path/to/file.pbf"))));
        assertEquals(new ArrayList<>(), EnvironmentVariableParser.validateEnvironmentVariablesAgainstClassJson("{}", new ArrayList<>(), List.of(Map.entry("ors.engine.source_file", "/some/path/to/file.pbf"))));

    }

    @Test
    void testCapitalVariableOverwritesProperty() throws JsonProcessingException {
        List<Map.Entry<String, String>> envVars = List.of(
                Map.entry("ORS_ENGINE_SOURCE_FILE", "/SOME/CAPITAL/PATH/TO/FILE.pbf"),
                Map.entry("ors.engine.source_file", "/some/path/to/file.pbf"),
                Map.entry("ors.engine.profiles.bike-regular.enabled", "true")
        );

        EngineProperties engineProperties = new EngineProperties();
        engineProperties.initialize();
        String enginePropertiesAsJson = mapper.writeValueAsString(engineProperties);
        List<Map.Entry<String, String>> result = EnvironmentVariableParser.validateEnvironmentVariablesAgainstClassJson(enginePropertiesAsJson, relevantPrefixes, envVars);
        assertEquals(2, result.size());
        for (Map.Entry<String, String> entry : result) {
            if (entry.getKey().equals("source_file")) {
                assertEquals("/SOME/CAPITAL/PATH/TO/FILE.pbf", entry.getValue());
            } else if (entry.getKey().equals("profiles.bike-regular.enabled")) {
                assertEquals("true", entry.getValue());
            } else {
                throw new AssertionError("Unexpected key: " + entry.getKey());
            }
        }
    }

    @Test
    void testIgnoreInvalidEnvironmentVariables() throws JsonProcessingException {
        List<Map.Entry<String, String>> envVars = List.of(
                Map.entry("ors.engine.elevation.preprocessed", "true"),
                Map.entry("ors.engine.foo", "bar")
        );

        EngineProperties engineProperties = new EngineProperties();
        engineProperties.initialize();
        String enginePropertiesAsJson = mapper.writeValueAsString(engineProperties);
        List<Map.Entry<String, String>> result = EnvironmentVariableParser.validateEnvironmentVariablesAgainstClassJson(enginePropertiesAsJson, relevantPrefixes, envVars);
        assertEquals(1, result.size());
        assertEquals("elevation.preprocessed", result.get(0).getKey());
        assertEquals("true", result.get(0).getValue());
    }

    // write a parametrized test with a stream function to check different key value paris with null etc
    @ParameterizedTest
    @CsvSource({
            "ORS_ENGINE_SOURCE_FILE, /some/path/to/file.pbf, source_file, /some/path/to/file.pbf",
            "ors.engine.source_file, /some/path/to/file.pbf, source_file, /some/path/to/file.pbf",
            "ORS_ENGINE_PROFILES_BIKE_REGULAR_ENABLED, true, profiles.bike-regular.enabled",
            "ors.engine.profiles.bike-regular.enabled, true, profiles.bike-regular.enabled",
            // test with white spaces
            "'   ors.engine.profiles.bike-regular.enabled   ', true, profiles.bike-regular.enabled",
            "ors.engine.profiles.bike-regular.enabled, '', profiles.bike-regular.enabled",
    })
    void testDifferentKeyValuesSuccess(String key, String value, String expectedKey) throws JsonProcessingException {
        EngineProperties engineProperties = new EngineProperties();
        engineProperties.initialize();
        String enginePropertiesAsJson = mapper.writeValueAsString(engineProperties);
        List<Map.Entry<String, String>> envVars = List.of(Map.entry(key, value));
        List<Map.Entry<String, String>> result = EnvironmentVariableParser.validateEnvironmentVariablesAgainstClassJson(enginePropertiesAsJson, relevantPrefixes, envVars);
        assertEquals(1, result.size());
        assertEquals(expectedKey, result.get(0).getKey());
        assertEquals(value, result.get(0).getValue());
    }

    // write a parametrized test with a stream function to check different key value paris with null etc
    @ParameterizedTest
    @CsvSource({
            "ors.engine.foo.bar, true, ''",
            "foo.bar, true, ''",
            "ORS_ENGINE_FOO_BAR, true, ''",
            "'' , true, ''",
            // Should not appear in the result due to the missing key in the engine properties
            "profiles.driving-car.enabled, false",
            "PROFILES_DRIVING_CAR_ENABLED, false",
            "ors.engine.foo, bar"
    })
    void testDifferentKeyValuesFailure(String key, String value) throws JsonProcessingException {
        EngineProperties engineProperties = new EngineProperties();
        engineProperties.initialize();
        String enginePropertiesAsJson = mapper.writeValueAsString(engineProperties);
        List<Map.Entry<String, String>> envVars = List.of(Map.entry(key, value));
        List<Map.Entry<String, String>> result = EnvironmentVariableParser.validateEnvironmentVariablesAgainstClassJson(enginePropertiesAsJson, relevantPrefixes, envVars);
        assertEquals(0, result.size());
    }

    @Test
    void testWithInvalidJson() {
        List<Map.Entry<String, String>> envVars = List.of(
                Map.entry("ors.engine.profiles.bike-regular.enabled", "true")
        );
        List<Map.Entry<String, String>> result = EnvironmentVariableParser.validateEnvironmentVariablesAgainstClassJson("invalid json", relevantPrefixes, envVars);
        assertEquals(0, result.size());
    }
}