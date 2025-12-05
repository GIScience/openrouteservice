package org.heigit.ors.api.servlet.listeners;

import org.heigit.ors.api.services.EngineService;
import org.heigit.ors.config.EngineProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EngineServiceTest {
    @Test
    void testConfigurationOutputTarget() {
        EngineProperties engineProperties = new EngineProperties();

        assertNull(EngineService.configurationOutputTarget(engineProperties), "default should return null");

        engineProperties.setConfigOutput("test");
        assertEquals("test.yml", EngineService.configurationOutputTarget(engineProperties), "Properties class var config_output should be returned with file extension");

        engineProperties.setConfigOutput("test.yml");
        assertEquals("test.yml", EngineService.configurationOutputTarget(engineProperties), "Variable with yml file extension should be returned as is");

        engineProperties.setConfigOutput("test.yaml");
        assertEquals("test.yaml", EngineService.configurationOutputTarget(engineProperties), "Variable with yaml file extension should be returned as is");
    }

    @Test
    void testCopyDefaultConfigurationToFile(@TempDir Path tempDir) throws Exception {
        Path out = tempDir.resolve("application-output.yml");

        EngineService.copyDefaultConfigurationToFile(out.toString());

        // verify file was created and has content
        assertTrue(Files.exists(out), "Output file should exist");
        long size = Files.size(out);
        assertTrue(size > 0, "Output file should not be empty");

        String content = Files.readString(out, StandardCharsets.UTF_8);
        assertFalse(content.isEmpty(), "Configuration content should be present");
    }
}