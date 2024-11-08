package org.heigit.ors.api.servlet.listeners;

import org.heigit.ors.api.services.GraphService;
import org.heigit.ors.config.EngineProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ORSInitContextListenerTest {
    @Test
    void testConfigurationOutputTarget() {
        ORSInitContextListener orsInitContextListener = new ORSInitContextListener(new EngineProperties(), new GraphService());
        EngineProperties engineProperties = new EngineProperties();

        assertNull(orsInitContextListener.configurationOutputTarget(engineProperties), "default should return null");

        engineProperties.setConfigOutput("test");
        assertEquals("test.yml", orsInitContextListener.configurationOutputTarget(engineProperties), "Properties class var config_output should be returned with file extension");

        engineProperties.setConfigOutput("test.yml");
        assertEquals("test.yml", orsInitContextListener.configurationOutputTarget(engineProperties), "Variable with yml file extension should be returned as is");

        engineProperties.setConfigOutput("test.yaml");
        assertEquals("test.yaml", orsInitContextListener.configurationOutputTarget(engineProperties), "Variable with yaml file extension should be returned as is");
    }
}