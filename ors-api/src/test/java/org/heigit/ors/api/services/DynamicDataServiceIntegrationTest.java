package org.heigit.ors.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.heigit.ors.apitests.common.MockRestBaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DynamicDataService with MockRestServiceServer mocking.
 * Tests the real DynamicDataService behavior with mocked FeatureStore API responses.
 */
@SpringBootTest
@DisplayName("DynamicDataService Integration Tests")
class DynamicDataServiceIntegrationTest extends MockRestBaseTest {

    @Autowired(required = false)
    private DynamicDataService dynamicDataService;

    @Test
    @DisplayName("Should autowire DynamicDataService bean successfully")
    void testServiceIsAutowired() {
        assertNotNull(dynamicDataService, "DynamicDataService should be autowired from Spring context");
    }

    @Test
    @DisplayName("Should indicate service is enabled in integration test context")
    void testServiceIsEnabledWhenConfigured() {
        assertNotNull(dynamicDataService, "DynamicDataService should be autowired");
        assertTrue(dynamicDataService.isEnabled(), "DynamicDataService should be enabled when mock URL is configured");
    }

    @Test
    @DisplayName("Should fetch stats and return JsonNode array")
    void testFetchStatsReturnsJsonArray() {
        assertNotNull(dynamicDataService, "DynamicDataService should be autowired");

        JsonNode stats = dynamicDataService.getFeatureStoreStats("driving-car");

        assertNotNull(stats, "getFeatureStoreStats should never return null");
        assertTrue(stats.isArray(), "Result should be a JSON array from the call");
        // An empty array is also "valid" per the above assertions but is what getFeatureStoreStats
        // silently falls back to on any HTTP error (wrong path, 404, etc.) - assert on the actual
        // mocked content (see MockRestBaseTest) so a broken request path fails this test instead
        // of slipping through as a "successful" empty response.
        assertFalse(stats.isEmpty(), "Result should contain the datasets stubbed in MockRestBaseTest, not be empty");
        assertEquals(3, stats.size(), "Should contain all three stubbed datasets");
        java.util.Set<String> datasetIds = new java.util.HashSet<>();
        stats.forEach(node -> datasetIds.add(node.get("datasetId").asText()));
        assertEquals(java.util.Set.of("logie_borders", "logie_bridges", "logie_roads"), datasetIds);
    }

    @Test
    @DisplayName("Should handle multiple calls to getFeatureStoreStats gracefully")
    void testMultipleCallsToStatsEndpoint() {
        assertNotNull(dynamicDataService, "DynamicDataService should be autowired");

        JsonNode stats1 = dynamicDataService.getFeatureStoreStats("driving-car");
        JsonNode stats2 = dynamicDataService.getFeatureStoreStats("cycling-regular");
        JsonNode stats3 = dynamicDataService.getFeatureStoreStats("foot-walking");

        // All should return JsonNode arrays (may be empty if mock server isn't properly intercepting)
        assertNotNull(stats1, "First call should return non-null");
        assertNotNull(stats2, "Second call should return non-null");
        assertNotNull(stats3, "Third call should return non-null");
        
        assertTrue(stats1.isArray(), "Result should be arrays");
        assertTrue(stats2.isArray(), "Result should be arrays");
        assertTrue(stats3.isArray(), "Result should be arrays");
    }

    @Test
    @DisplayName("Should handle service without null pointer exceptions")
    void testServiceMethodsDoNotThrowNullPointer() {
        // Ensure calling service methods doesn't result in NPE
        try {
            JsonNode stats = dynamicDataService.getFeatureStoreStats("driving-car");
            assertNotNull(stats, "Should not throw NPE and should return JsonNode");
        } catch (NullPointerException e) {
            fail("getFeatureStoreStats should not throw NullPointerException: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should verify mockServer is available from MockRestBaseTest")
    void testMockServerIsConfigured() {
        assertNotNull(mockServer, "MockServer should be configured in base class");
    }
}
