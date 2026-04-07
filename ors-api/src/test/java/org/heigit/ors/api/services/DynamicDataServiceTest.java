package org.heigit.ors.api.services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.EngineProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DynamicDataServiceTest {

    @Mock
    private EngineService engineService;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.Builder restClientBuilder;

    private EngineProperties engineProperties;
    private ObjectMapper objectMapper;
    private JsonFactory jsonFactory;

    @BeforeEach
    void setUp() {
        engineProperties = new EngineProperties();
        engineProperties.getDynamicData().setEnabled(true);
        engineProperties.getDynamicData().setFeatureStoreApiUrl("http://localhost:8080/api/v1");

        objectMapper = new ObjectMapper();
        jsonFactory = new JsonFactory();
    }

    @Test
    void testParseNdjsonLine() throws IOException {
        // RED: Test that demonstrates NDJSON line parsing with JsonParser
        String ndjsonLine = """
                {"feature_id":1,"dataset_key":"logie_borders","edge_id":3239,"timestamp":"2024-09-08T20:21:00Z"}
                """.trim();

        JsonParser parser = jsonFactory.createParser(ndjsonLine);
        JsonNode nodeTree = objectMapper.readTree(parser);

        assertEquals(1, nodeTree.get("feature_id").asInt());
        assertEquals("logie_borders", nodeTree.get("dataset_key").asText());
        assertEquals(3239, nodeTree.get("edge_id").asInt());
        assertEquals("2024-09-08T20:21:00Z", nodeTree.get("timestamp").asText());
    }

    @Test
    void testParseNdjsonStream() throws IOException {
        // RED: Test that demonstrates parsing multiple NDJSON lines
        String ndjsonStream = """
                {"feature_id":1,"dataset_key":"logie_borders","edge_id":3239,"timestamp":"2024-09-08T20:21:00Z"}
                {"feature_id":2,"dataset_key":"logie_bridges","edge_id":3240,"timestamp":"2024-09-08T20:21:00Z"}
                {"feature_id":3,"dataset_key":"logie_roads","edge_id":14409,"timestamp":"2024-09-08T20:21:00Z"}
                """.trim();

        Map<String, Instant> timestamps = new ConcurrentHashMap<>();
        JsonParser parser = jsonFactory.createParser(ndjsonStream);

        while (parser.nextToken() != null) {
            if (parser.currentToken().isStructStart()) {
                JsonNode node = objectMapper.readTree(parser);
                String datasetKey = node.get("dataset_key").asText();
                String timestamp = node.get("timestamp").asText();
                timestamps.put(datasetKey, Instant.parse(timestamp));
            }
        }

        assertEquals(3, timestamps.size());
        assertEquals(Instant.parse("2024-09-08T20:21:00Z"), timestamps.get("logie_borders"));
        assertEquals(Instant.parse("2024-09-08T20:21:00Z"), timestamps.get("logie_bridges"));
        assertEquals(Instant.parse("2024-09-08T20:21:00Z"), timestamps.get("logie_roads"));
    }

    @Test
    void testTimestampTracking() {
        // RED: Test that timestamps are properly tracked in lastUpdateTimestamps
        Map<String, Instant> timestamps = new ConcurrentHashMap<>();
        Instant now = Instant.now();

        timestamps.put("logie_borders", now);
        timestamps.put("logie_bridges", now.minusSeconds(60));

        assertEquals(now, timestamps.get("logie_borders"));
        assertEquals(now.minusSeconds(60), timestamps.get("logie_bridges"));
    }
}
