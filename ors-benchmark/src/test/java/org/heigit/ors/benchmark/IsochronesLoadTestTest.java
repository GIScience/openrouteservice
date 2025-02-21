package org.heigit.ors.benchmark;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.gatling.javaapi.core.Session;

class IsochronesLoadTestTest {
    private ObjectMapper objectMapper;
    private Session mockSession;
    private TestConfig mockConfig;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        objectMapper = new ObjectMapper();
        mockSession = mock(Session.class);
        mockConfig = mock(TestConfig.class);

        when(mockSession.getDouble("longitude")).thenReturn(8.681495);
        when(mockSession.getDouble("latitude")).thenReturn(49.41461);
        when(mockConfig.getFieldLon()).thenReturn("longitude");
        when(mockConfig.getFieldLat()).thenReturn("latitude");
        when(mockConfig.getRange()).thenReturn("300");
    }

    @Test
    void testCreateRequestBodySingleLocation() throws Exception {
        String requestBody = IsochronesLoadTest.createRequestBody(mockSession, 1, mockConfig);
        JsonNode json = objectMapper.readTree(requestBody);
        
        assertEquals(1, json.get("locations").size());
        assertEquals(8.681495, json.get("locations").get(0).get(0).asDouble());
        assertEquals(49.41461, json.get("locations").get(0).get(1).asDouble());
        assertEquals(300, json.get("range").get(0).asInt());
    }

    @Test
    void testCreateRequestBodyMultipleLocations() throws Exception {
        when(mockConfig.getRange()).thenReturn("500");
        String requestBody = IsochronesLoadTest.createRequestBody(mockSession, 3, mockConfig);
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
        
        Throwable thrown = assertThrows(RuntimeException.class,
                () -> IsochronesLoadTest.createRequestBody(invalidSession, 1, mockConfig));
        assertEquals(RuntimeException.class, thrown.getClass());
    }

    @Test
    void testCreateRequestBodyWithInvalidRange() {
        when(mockConfig.getRange()).thenReturn("invalid");

        Throwable thrown = assertThrows(NumberFormatException.class,
                () -> IsochronesLoadTest.createRequestBody(mockSession, 1, mockConfig));
        assertEquals(NumberFormatException.class, thrown.getClass());
    }

    @Test
    void testCreateLocationsList() {
        List<List<Double>> locations = IsochronesLoadTest.createLocationsList(mockSession, 2, mockConfig);

        assertEquals(2, locations.size());
        locations.forEach(coord -> {
            assertEquals(8.681495, coord.get(0));
            assertEquals(49.41461, coord.get(1));
        });
    }
}
