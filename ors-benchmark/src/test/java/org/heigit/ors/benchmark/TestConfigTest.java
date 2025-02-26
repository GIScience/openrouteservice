package org.heigit.ors.benchmark;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestConfigTest {
    private TestConfig config;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        config = new TestConfig();
    }

    @AfterEach
    @SuppressWarnings("unused")
    void tearDown() {
        System.clearProperty("base_url");
        System.clearProperty("api_key");
        System.clearProperty("concurrent_users");
        System.clearProperty("query_sizes");
        System.clearProperty("run_time");
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
        assertEquals(Collections.singletonList("search.csv"), config.getSourceFiles());
        assertEquals(true, config.isParallelExecution());
    }

    @Test
    void testQuerySizesParsing() {
        System.setProperty("query_sizes", "1,3,2,5,4");
        TestConfig customConfig = new TestConfig();
        assertEquals(List.of(1, 2, 3, 4, 5), customConfig.getQuerySizes());
    }

    @Test
    void testCustomValues() {
        System.setProperty("base_url", "http://test.com");
        System.setProperty("api_key", "test-key");
        System.setProperty("concurrent_users", "5");
        System.setProperty("query_sizes", "2,4");
        System.setProperty("run_time", "120");
        System.setProperty("parallel_execution", "false");

        TestConfig customConfig = new TestConfig();
        assertEquals("http://test.com", customConfig.getBaseUrl());
        assertEquals("test-key", customConfig.getApiKey());
        assertEquals(5, customConfig.getNumConcurrentUsers());
        assertEquals(List.of(2, 4), customConfig.getQuerySizes());
        assertEquals(false, customConfig.isParallelExecution());
    }
}
