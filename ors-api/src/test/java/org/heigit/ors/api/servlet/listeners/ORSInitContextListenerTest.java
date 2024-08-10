package org.heigit.ors.api.servlet.listeners;

import org.heigit.ors.api.config.*;
import org.heigit.ors.api.services.GraphService;
import org.heigit.ors.config.EngineProperties;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.heigit.ors.api.ORSEnvironmentPostProcessor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ORSInitContextListenerTest {
    @Test
    void testConfigurationOutputTarget() {
        ORSInitContextListener orsInitContextListener = new ORSInitContextListener(new EndpointsProperties(), new CorsProperties(), new SystemMessageProperties(), new LoggingProperties(), new ServerProperties(), new GraphService());
        EngineProperties engineProperties = new EngineProperties();
        Map<String, String> envMap = new HashMap<>();

        assertNull(orsInitContextListener.configurationOutputTarget(engineProperties, envMap), "default should return null");

        engineProperties.setConfigOutput("test");
        assertEquals("test.yml", orsInitContextListener.configurationOutputTarget(engineProperties, envMap), "Properties class var config_output should be returned with file extension");

        engineProperties.setConfigOutput("test.yml");
        assertEquals("test.yml", orsInitContextListener.configurationOutputTarget(engineProperties, envMap), "Variable with yml file extension should be returned as is");

        engineProperties.setConfigOutput("test.yaml");
        assertEquals("test.yaml", orsInitContextListener.configurationOutputTarget(engineProperties, envMap), "Variable with yaml file extension should be returned as is");

        envMap.put(ORS_CONFIG_OUTPUT_ENV, "test1");
        assertEquals("test1.yml", orsInitContextListener.configurationOutputTarget(engineProperties, envMap), "Env var ORS_CONFIG_OUTPUT should override properties class variable");

        System.setProperty(ORS_CONFIG_OUTPUT_PROPERTY, "test2");
        assertEquals("test2.yml", orsInitContextListener.configurationOutputTarget(engineProperties, envMap), "System property ors.config.output should override all previous");

        envMap.put(ORS_CONFIG_DEFAULT_OUTPUT_ENV, "test3");
        assertEquals("test3.yml", orsInitContextListener.configurationOutputTarget(engineProperties, envMap), "Env var ORS_CONFIG_DEFAULT_OUTPUT should override all previous");

        System.setProperty(ORS_CONFIG_DEFAULT_OUTPUT_PROPERTY, "test4");
        assertEquals("test4.yml", orsInitContextListener.configurationOutputTarget(engineProperties, envMap), "System property ors.config.default_output should override all previous");
    }
}