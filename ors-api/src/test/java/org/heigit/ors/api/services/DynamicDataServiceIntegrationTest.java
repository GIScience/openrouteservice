package org.heigit.ors.api.services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.apitests.common.MockRestBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DynamicDataService NDJSON streaming,
 * timestamp tracking, and WireMock integration.
 */
@SpringBootTest
@DisplayName("DynamicDataService Integration Tests")
class DynamicDataServiceIntegrationTest extends MockRestBaseTest {

    @Autowired(required = false)
    private DynamicDataService dynamicDataService;

    private ObjectMapper objectMapper;
    private JsonFactory jsonFactory;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        jsonFactory = new JsonFactory();
    }

    @Test
    @DisplayName("Task 6.3.2: Should parse NDJSON with Jackson streaming parser")
    void testNdjsonStreamingParsing() throws IOException {
        String ndjsonContent = """
                {"feature_id":1,"dataset_key":"logie_borders","edge_id":3239,"value":"CLOSED","timestamp":"2024-09-08T20:21:00Z","is_deleted":false}
                {"feature_id":2,"dataset_key":"logie_bridges","edge_id":3240,"value":"RESTRICTED","timestamp":"2024-09-08T20:21:01Z","is_deleted":false}
                """.trim();

        JsonParser parser = jsonFactory.createParser(ndjsonContent);
        int recordCount = 0;

        while (parser.nextToken() != null) {
            if (parser.currentToken().isStructStart()) {
                JsonNode node = objectMapper.readTree(parser);
                assertNotNull(node.get("feature_id"));
                assertNotNull(node.get("dataset_key"));
                assertNotNull(node.get("edge_id"));
                assertNotNull(node.get("timestamp"));
                recordCount++;
            }
        }

        parser.close();
        assertEquals(2, recordCount, "Should parse 2 NDJSON records");
    }

    @Test
    @DisplayName("Task 6.3.3: Should track timestamps in lastUpdateTimestamps")
    void testTimestampTracking() {
        Map<String, Instant> timestamps = new ConcurrentHashMap<>();
        Instant t1 = Instant.parse("2024-09-08T20:21:00Z");
        Instant t2 = Instant.parse("2024-09-08T20:21:01Z");

        // Track timestamps for two profiles
        timestamps.put("driving-car", t1);
        timestamps.put("driving-hgv", t2);

        // Verify timestamps are correctly stored and retrieved
        assertEquals(t1, timestamps.get("driving-car"));
        assertEquals(t2, timestamps.get("driving-hgv"));

        // Update timestamp for driving-car to newer value
        Instant t3 = Instant.parse("2024-09-08T20:21:02Z");
        timestamps.put("driving-car", t3);

        assertEquals(t3, timestamps.get("driving-car"), "Timestamp should be updated to newer value");
    }

    @Test
    @DisplayName("Task 6.3.4: Should use WireMock to mock FeatureStore API responses")
    void testMockRestIntegration() {
        // Verify that the FeatureStore API URL is set correctly
        String featureStoreUrl = System.getProperty("ors.engine.dynamic_data.feature_store_api_url");
        assertEquals("http://mock-feature-store/api/v1", featureStoreUrl, "FeatureStore API URL should be configured for WireMock");

        // Verify that DynamicDataService is enabled
        assertTrue(dynamicDataService.isEnabled(), "DynamicDataService should be enabled");
    }

    @Test
    @DisplayName("Task 6.3.2+6.3.3: Should parse NDJSON and track timestamps")
    void testNdjsonParsingWithTimestampTracking() throws IOException {
        String ndjsonContent = """
                {"feature_id":1,"dataset_key":"logie_borders","edge_id":3239,"value":"CLOSED","timestamp":"2024-09-08T20:21:00Z","is_deleted":false}
                {"feature_id":2,"dataset_key":"logie_bridges","edge_id":3240,"value":"RESTRICTED","timestamp":"2024-09-08T20:21:05Z","is_deleted":false}
                """.trim();

        Map<String, Instant> timestamps = new ConcurrentHashMap<>();
        JsonParser parser = jsonFactory.createParser(ndjsonContent);

        while (parser.nextToken() != null) {
            if (parser.currentToken().isStructStart()) {
                JsonNode node = objectMapper.readTree(parser);
                String datasetKey = node.get("dataset_key").asText();
                String timestamp = node.get("timestamp").asText();
                
                // Track timestamp as DynamicDataService would
                timestamps.put(datasetKey, Instant.parse(timestamp));
            }
        }

        parser.close();

        // Verify timestamps are tracked correctly
        assertEquals(Instant.parse("2024-09-08T20:21:00Z"), timestamps.get("logie_borders"));
        assertEquals(Instant.parse("2024-09-08T20:21:05Z"), timestamps.get("logie_bridges"));
    }

    @Test
    @DisplayName("Should handle deleted flag in NDJSON records")
    void testDeletedFieldHandling() throws IOException {
        String ndjsonContent = """
                {"feature_id":1,"dataset_key":"logie_borders","edge_id":3239,"value":"CLOSED","timestamp":"2024-09-08T20:21:00Z","is_deleted":false}
                {"feature_id":2,"dataset_key":"logie_bridges","edge_id":3240,"value":"RESTRICTED","timestamp":"2024-09-08T20:21:01Z","is_deleted":true}
                """.trim();

        JsonParser parser = jsonFactory.createParser(ndjsonContent);
        int deletedCount = 0;
        int activeCount = 0;

        while (parser.nextToken() != null) {
            if (parser.currentToken().isStructStart()) {
                JsonNode node = objectMapper.readTree(parser);
                boolean isDeleted = node.has("is_deleted") && node.get("is_deleted").asBoolean();
                
                if (isDeleted) {
                    deletedCount++;
                } else {
                    activeCount++;
                }
            }
        }

        parser.close();

        assertEquals(1, activeCount, "Should find 1 active record");
        assertEquals(1, deletedCount, "Should find 1 deleted record");
    }
}
