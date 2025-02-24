package org.heigit.ors.benchmark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestConfigTest {
    private TestConfig config;

    @BeforeEach
    void setUp() {
        config = new TestConfig();
    }

    @Test
    void testDefaultValues() {
        assertEquals("http://localhost:8082/ors", config.getBaseUrl());
        assertEquals("API KEY", config.getApiKey());
        assertEquals("driving-car", config.getTargetProfile());
        assertEquals("300", config.getRange());
        assertEquals("longitude", config.getFieldLon());
        assertEquals("latitude", config.getFieldLat());
        assertEquals(100, config.getNumConcurrentUsers());
        assertEquals(List.of(1), config.getQuerySizes());
        assertEquals(1, config.getRunTime());
        assertEquals("search.csv", config.getSourceFile());
    }

    @Test
    void testCustomValues() {
        System.setProperty("base_url", "http://test.com");
        System.setProperty("api_key", "test-key");
        System.setProperty("calls", "200");
        
        TestConfig customConfig = new TestConfig();
        assertEquals("http://test.com", customConfig.getBaseUrl());
        assertEquals("test-key", customConfig.getApiKey());
        assertEquals(200, customConfig.getNumConcurrentUsers());

        // Clean up system properties
        System.clearProperty("base_url");
        System.clearProperty("api_key");
        System.clearProperty("calls");
    }
}
