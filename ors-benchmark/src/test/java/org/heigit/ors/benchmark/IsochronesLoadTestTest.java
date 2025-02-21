package org.heigit.ors.benchmark;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IsochronesLoadTestTest {
    private ObjectMapper objectMapper;
    private Session mockSession;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockSession = mock(Session.class);
        when(mockSession.getDouble("longitude")).thenReturn(8.681495);
        when(mockSession.getDouble("latitude")).thenReturn(49.41461);
    }

    @Test
    void testCreateRequestBodySingleLocation() throws Exception {
        String requestBody = IsochronesLoadTest.createRequestBody(
            mockSession, 1, "longitude", "latitude", "300"
        );
        
        JsonNode json = objectMapper.readTree(requestBody);
        
        assertEquals(1, json.get("locations").size());
        assertEquals(8.681495, json.get("locations").get(0).get(0).asDouble());
        assertEquals(49.41461, json.get("locations").get(0).get(1).asDouble());
        assertEquals(300, json.get("range").get(0).asInt());
    }

    @Test
    void testCreateRequestBodyMultipleLocations() throws Exception {
        String requestBody = IsochronesLoadTest.createRequestBody(
            mockSession, 3, "longitude", "latitude", "500"
        );
        
        JsonNode json = objectMapper.readTree(requestBody);
        
        assertEquals(3, json.get("locations").size());
        for (int i = 0; i < 3; i++) {
            assertEquals(8.681495, json.get("locations").get(i).get(0).asDouble());
            assertEquals(49.41461, json.get("locations").get(i).get(1).asDouble());
        }
        assertEquals(500, json.get("range").get(0).asInt());
    }

    @Test
    void testCreateRequestBodyWithInvalidSession() {
        Session invalidSession = mock(Session.class);
        when(invalidSession.getDouble("longitude")).thenThrow(new RuntimeException("Session error"));
        
        assertThrows(RuntimeException.class, () -> 
            IsochronesLoadTest.createRequestBody(
                invalidSession, 1, "longitude", "latitude", "300"
            )
        );
    }

    @Test
    void testCreateRequestBodyWithInvalidRange() {
        assertThrows(NumberFormatException.class, () -> 
            IsochronesLoadTest.createRequestBody(
                mockSession, 1, "longitude", "latitude", "invalid"
            )
        );
    }

    @Test
    void testCreateScenario() {
        TestConfig config = new TestConfig();
        var scenario = IsochronesLoadTest.createScenario("Test", 2, config);
        
        assertNotNull(scenario);
    }
}
