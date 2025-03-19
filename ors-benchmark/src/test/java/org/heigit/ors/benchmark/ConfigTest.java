package org.heigit.ors.benchmark;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigTest {
    private Config config;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        config = new Config();
    }

    @AfterEach
    @SuppressWarnings("unused")
    void tearDown() {
        System.clearProperty("base_url");
        System.clearProperty("api_key");
        System.clearProperty("concurrent_users");
        System.clearProperty("query_sizes");
    }

    @Test
    void testDefaultValues() {
        assertEquals("http://localhost:8082/ors", config.getBaseUrl());
        assertEquals("API KEY", config.getApiKey());
        assertEquals("driving-car", config.getTargetProfile());
        assertEquals("300", config.getRange());
        assertEquals("longitude", config.getFieldLon());
        assertEquals("latitude", config.getFieldLat());
        assertEquals(1, config.getNumConcurrentUsers());
        assertEquals(List.of(1), config.getQuerySizes());
        assertEquals(Collections.emptyList(), config.getSourceFiles());
        assertEquals(false, config.isParallelExecution());
    }

    @Test
    void testQuerySizesParsing() {
        System.setProperty("query_sizes", "1,3,2,5,4");
        Config customConfig = new Config();
        assertEquals(List.of(1, 2, 3, 4, 5), customConfig.getQuerySizes());
    }

    @Test
    void testCustomValues() {
        System.setProperty("base_url", "http://test.com");
        System.setProperty("api_key", "test-key");
        System.setProperty("concurrent_users", "5");
        System.setProperty("query_sizes", "2,4");
        System.setProperty("parallel_execution", "false");

        Config customConfig = new Config();
        assertEquals("http://test.com", customConfig.getBaseUrl());
        assertEquals("test-key", customConfig.getApiKey());
        assertEquals(5, customConfig.getNumConcurrentUsers());
        assertEquals(List.of(2, 4), customConfig.getQuerySizes());
        assertEquals(false, customConfig.isParallelExecution());
    }
}
