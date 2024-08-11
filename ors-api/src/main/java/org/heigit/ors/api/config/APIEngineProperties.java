package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import org.heigit.ors.api.config.util.EnvironmentVariableParser;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.utils.PropertyUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class APIEngineProperties extends EngineProperties {

    @JsonIgnore
    public void loadFromEnvironmentVariables(List<Map.Entry<String, String>> rawEnvVariables) {
        Boolean isInitialized = this.isInitialized();
        APIEngineProperties defaultEngineProperties = new APIEngineProperties();
        defaultEngineProperties.initialize();

        String enginePropertiesToJson = convertToJson(defaultEngineProperties);

        Properties properties = EnvironmentVariableParser.validateEnvironmentVariablesAgainstClassJson(
                enginePropertiesToJson,
                List.of("ORS_ENGINE_", "ors.engine."),
                rawEnvVariables
        ).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, Properties::new));

        APIEngineProperties engineProperties = readProperties(properties);

        PropertyUtils.deepCopyObjectsProperties(this, engineProperties, false);
        PropertyUtils.deepCopyObjectsProperties(engineProperties, this, true);

        // Reinitialize the object if it was initialized before
        if (isInitialized) {
            this.initialize();
        }
    }

    @JsonIgnore
    String convertToJson(APIEngineProperties properties) {
        try {
            return new ObjectMapper().writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonIgnore
    APIEngineProperties readProperties(Properties properties) {
        try {
            return new JavaPropsMapper().readPropertiesAs(properties, APIEngineProperties.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}