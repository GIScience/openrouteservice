package org.heigit.ors.benchmark;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
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
        when(mockConfig.getRange()).thenReturn("300,600,900");
        when(mockConfig.getRanges()).thenReturn(Arrays.asList(300, 600, 900));
    }

    @Test
    void createRequestBody_ShouldCreateValidJson() {
        // given
        TestConfig config = new TestConfig();

        // when
        String result = IsochronesLoadTest.createRequestBody(mockSession, 1, config, RangeType.TIME);

        // then
        assertThat(result)
                .contains("\"locations\":[[8.681495,49.41461]]")
                .contains("\"range\":[300]")
                .contains("\"range_type\":\"time\"");
    }

    @Test
    void createRequestBody_ShouldCreateValidJsonForDistance() {
        // given
        TestConfig config = new TestConfig();

        // when
        String result = IsochronesLoadTest.createRequestBody(mockSession, 1, config, RangeType.DISTANCE);

        // then
        assertThat(result)
                .contains("\"locations\":[[8.681495,49.41461]]")
                .contains("\"range\":[300]")
                .contains("\"range_type\":\"distance\"");
    }

    @Test
    void createRequestBody_ShouldIncludeMultipleLocations() {
        // given
        TestConfig config = new TestConfig();

        // when
        String result = IsochronesLoadTest.createRequestBody(mockSession, 2, config, RangeType.TIME);

        // then
        assertThat(result).contains("\"locations\":[[8.681495,49.41461],[8.681495,49.41461]]");
    }

    @Test
    void createRequestBody_ShouldThrowExceptionOnInvalidJson() {
        // given
        Session invalidSession = mock(Session.class);
        when(invalidSession.getDouble(anyString())).thenThrow(new RuntimeException("Invalid session"));
        TestConfig config = new TestConfig();

        // when / then
        Throwable thrown = assertThrows(RuntimeException.class,
                () -> IsochronesLoadTest.createRequestBody(invalidSession, 1, config, RangeType.TIME));
        assertEquals(RuntimeException.class, thrown.getClass());
    }

    @Test
    void testCreateRequestBodySingleLocation() throws JsonProcessingException {
        String requestBody = IsochronesLoadTest.createRequestBody(mockSession, 1, mockConfig, RangeType.TIME);
        JsonNode json = objectMapper.readTree(requestBody);

        assertEquals(1, json.get("locations").size());
        assertEquals(8.681495, json.get("locations").get(0).get(0).asDouble());
        assertEquals(49.41461, json.get("locations").get(0).get(1).asDouble());
        assertEquals(300, json.get("range").get(0).asInt());
    }

    @Test
    void testCreateRequestBodyMultipleLocations() throws JsonProcessingException {
        when(mockConfig.getRange()).thenReturn("500");
        String requestBody = IsochronesLoadTest.createRequestBody(mockSession, 3, mockConfig, RangeType.TIME);
        JsonNode json = objectMapper.readTree(requestBody);

        assertEquals(3, json.get("locations").size());
        for (int i = 0; i < 3; i++) {
            assertEquals(8.681495, json.get("locations").get(i).get(0).asDouble());
            assertEquals(49.41461, json.get("locations").get(i).get(1).asDouble());
        }
        assertEquals(300, json.get("range").get(0).asInt());
    }

    @Test
    void testCreateRequestBodyWithInvalidSession() {
        Session invalidSession = mock(Session.class);
        when(invalidSession.getDouble("longitude")).thenThrow(new RuntimeException("Session error"));

        Throwable thrown = assertThrows(RuntimeException.class,
                () -> IsochronesLoadTest.createRequestBody(invalidSession, 1, mockConfig, RangeType.TIME));
        assertEquals(RuntimeException.class, thrown.getClass());
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

    @Test
    void createRequestBody_ShouldCreateValidJsonWithMultipleRanges() throws JsonProcessingException {
        // given
        when(mockConfig.getRanges()).thenReturn(Arrays.asList(300, 600, 900));

        // when
        String result = IsochronesLoadTest.createRequestBody(mockSession, 1, mockConfig, RangeType.TIME);
        JsonNode json = objectMapper.readTree(result);

        // then
        assertThat(json.get("range").size()).isEqualTo(3);
    }
}