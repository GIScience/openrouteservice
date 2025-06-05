package org.heigit.ors.benchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.Session;
import org.heigit.ors.benchmark.BenchmarkEnums.MatrixModes;
import org.heigit.ors.benchmark.exceptions.RequestBodyCreationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MatrixAlgorithmLoadTestTest {
    private ObjectMapper objectMapper;
    private Session mockSession;
    private MatrixModes mockMode;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockSession = mock(Session.class);
        mockMode = mock(MatrixModes.class);

        // Mock CSV data as it would appear in the session after CSV feeder
        // Each CSV column becomes a List<String> in the session
        when(mockSession.get("coordinates"))
                .thenReturn(Arrays.asList("[[8.695556, 49.392701], [8.684623, 49.398284], [8.705916, 49.406309]]"));
        when(mockSession.get("sources")).thenReturn(Arrays.asList("[0, 1]"));
        when(mockSession.get("destinations")).thenReturn(Arrays.asList("[2]"));
        when(mockMode.getRequestParams()).thenReturn(Map.of("preference", "recommended"));
    }

    @Test
    void createRequestBody_ShouldCreateValidJson() throws JsonProcessingException {
        // when
        String result = MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode);

        // then
        JsonNode json = objectMapper.readTree(result);
        assertThat(json.get("locations")).isNotNull();
        assertThat(json.get("sources")).isNotNull();
        assertThat(json.get("destinations")).isNotNull();
        assertThat(json.get("preference").asText()).isEqualTo("recommended");
    }

    @Test
    void createRequestBody_ShouldIncludeCorrectLocations() throws JsonProcessingException {
        // when
        String result = MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode);
        JsonNode json = objectMapper.readTree(result);

        // then
        JsonNode locations = json.get("locations");
        assertEquals(3, locations.size());
        assertEquals(8.695556, locations.get(0).get(0).asDouble(), 0.000001);
        assertEquals(49.392701, locations.get(0).get(1).asDouble(), 0.000001);
        assertEquals(8.684623, locations.get(1).get(0).asDouble(), 0.000001);
        assertEquals(49.398284, locations.get(1).get(1).asDouble(), 0.000001);
        assertEquals(8.705916, locations.get(2).get(0).asDouble(), 0.000001);
        assertEquals(49.406309, locations.get(2).get(1).asDouble(), 0.000001);
    }

    @Test
    void createRequestBody_ShouldIncludeCorrectSourcesAndDestinations() throws JsonProcessingException {
        // when
        String result = MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode);
        JsonNode json = objectMapper.readTree(result);

        // then
        JsonNode sources = json.get("sources");
        JsonNode destinations = json.get("destinations");

        assertEquals(2, sources.size());
        assertEquals(0, sources.get(0).asInt());
        assertEquals(1, sources.get(1).asInt());

        assertEquals(1, destinations.size());
        assertEquals(2, destinations.get(0).asInt());
    }

    @Test
    void createRequestBody_ShouldThrowExceptionForMissingCoordinates() {
        // given
        when(mockSession.get("coordinates")).thenReturn(null);

        // when & then
        assertThrows(RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode));
    }

    @Test
    void createRequestBody_ShouldThrowExceptionForEmptyCoordinatesList() {
        // given
        when(mockSession.get("coordinates")).thenReturn(Arrays.asList());

        // when & then
        assertThrows(RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode));
    }

    @Test
    void createRequestBody_ShouldThrowExceptionForMissingSources() {
        // given
        when(mockSession.get("sources")).thenReturn(null);

        // when & then
        assertThrows(RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode));
    }

    @Test
    void createRequestBody_ShouldThrowExceptionForEmptySourcesList() {
        // given
        when(mockSession.get("sources")).thenReturn(Arrays.asList());

        // when & then
        assertThrows(RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode));
    }

    @Test
    void createRequestBody_ShouldThrowExceptionForMissingDestinations() {
        // given
        when(mockSession.get("destinations")).thenReturn(null);

        // when & then
        assertThrows(RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode));
    }

    @Test
    void createRequestBody_ShouldThrowExceptionForEmptyDestinationsList() {
        // given
        when(mockSession.get("destinations")).thenReturn(Arrays.asList());

        // when & then
        assertThrows(RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode));
    }

    @Test
    void createRequestBody_ShouldThrowExceptionForInvalidCoordinatesJson() {
        // given
        when(mockSession.get("coordinates")).thenReturn(Arrays.asList("invalid json"));

        // when & then
        assertThrows(RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode));
    }

    @Test
    void createRequestBody_ShouldThrowExceptionForInvalidSourcesJson() {
        // given
        when(mockSession.get("sources")).thenReturn(Arrays.asList("invalid json"));

        // when & then
        assertThrows(RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode));
    }

    @Test
    void createRequestBody_ShouldThrowExceptionForInvalidDestinationsJson() {
        // given
        when(mockSession.get("destinations")).thenReturn(Arrays.asList("invalid json"));

        // when & then
        assertThrows(RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode));
    }

    @Test
    void createRequestBody_WithDifferentMatrixMode() throws JsonProcessingException {
        // given
        when(mockMode.getRequestParams()).thenReturn(Map.of(
                "preference", "fastest",
                "options", Map.of("avoid_features", Arrays.asList("highways"))));

        // when
        String result = MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode);
        JsonNode json = objectMapper.readTree(result);

        // then
        assertEquals("fastest", json.get("preference").asText());
        assertThat(json.get("options")).isNotNull();
    }

    @Test
    void createRequestBody_ShouldHandleComplexSourcesAndDestinations() throws JsonProcessingException {
        // given
        when(mockSession.get("sources")).thenReturn(Arrays.asList("[0, 1, 2, 3]"));
        when(mockSession.get("destinations")).thenReturn(Arrays.asList("[4, 5, 6, 7, 8]"));

        // when
        String result = MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode);
        JsonNode json = objectMapper.readTree(result);

        // then
        JsonNode sources = json.get("sources");
        JsonNode destinations = json.get("destinations");

        assertEquals(4, sources.size());
        assertEquals(5, destinations.size());
        assertEquals(0, sources.get(0).asInt());
        assertEquals(3, sources.get(3).asInt());
        assertEquals(4, destinations.get(0).asInt());
        assertEquals(8, destinations.get(4).asInt());
    }

    @Test
    void createRequestBody_ShouldHandleSingleCoordinate() throws JsonProcessingException {
        // given
        when(mockSession.get("coordinates")).thenReturn(Arrays.asList("[[8.695556, 49.392701]]"));
        when(mockSession.get("sources")).thenReturn(Arrays.asList("[0]"));
        when(mockSession.get("destinations")).thenReturn(Arrays.asList("[0]"));

        // when
        String result = MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode);
        JsonNode json = objectMapper.readTree(result);

        // then
        JsonNode locations = json.get("locations");
        assertEquals(1, locations.size());
        assertEquals(8.695556, locations.get(0).get(0).asDouble(), 0.000001);
        assertEquals(49.392701, locations.get(0).get(1).asDouble(), 0.000001);
    }

    @Test
    void createRequestBody_ShouldHandleLargeCoordinateArray() throws JsonProcessingException {
        // given
        when(mockSession.get("coordinates")).thenReturn(Arrays.asList(
                "[[8.695556, 49.392701], [8.684623, 49.398284], [8.705916, 49.406309], [8.689981, 49.394522], [8.681502, 49.394791]]"));
        when(mockSession.get("sources")).thenReturn(Arrays.asList("[0, 1, 2]"));
        when(mockSession.get("destinations")).thenReturn(Arrays.asList("[3, 4]"));

        // when
        String result = MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode);
        JsonNode json = objectMapper.readTree(result);

        // then
        JsonNode locations = json.get("locations");
        assertEquals(5, locations.size());
        assertEquals(8.695556, locations.get(0).get(0).asDouble(), 0.000001);
        assertEquals(49.392701, locations.get(0).get(1).asDouble(), 0.000001);
        assertEquals(8.681502, locations.get(4).get(0).asDouble(), 0.000001);
        assertEquals(49.394791, locations.get(4).get(1).asDouble(), 0.000001);
    }

    @Test
    void createRequestBody_ShouldHandleEmptyArrays() throws JsonProcessingException {
        // given
        when(mockSession.get("coordinates")).thenReturn(Arrays.asList("[]"));
        when(mockSession.get("sources")).thenReturn(Arrays.asList("[]"));
        when(mockSession.get("destinations")).thenReturn(Arrays.asList("[]"));

        // when
        String result = MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode);
        JsonNode json = objectMapper.readTree(result);

        // then
        JsonNode locations = json.get("locations");
        JsonNode sources = json.get("sources");
        JsonNode destinations = json.get("destinations");

        assertEquals(0, locations.size());
        assertEquals(0, sources.size());
        assertEquals(0, destinations.size());
    }

    @Test
    void createRequestBody_ShouldMergeMatrixModeParameters() throws JsonProcessingException {
        // given
        when(mockMode.getRequestParams()).thenReturn(Map.of(
                "preference", "recommended",
                "units", "m",
                "metrics", Arrays.asList("distance", "duration")));

        // when
        String result = MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode);
        JsonNode json = objectMapper.readTree(result);

        // then
        assertEquals("recommended", json.get("preference").asText());
        assertEquals("m", json.get("units").asText());
        assertThat(json.get("metrics")).isNotNull();
        assertEquals(2, json.get("metrics").size());
        assertEquals("distance", json.get("metrics").get(0).asText());
        assertEquals("duration", json.get("metrics").get(1).asText());
    }

    @Test
    void createRequestBody_ShouldHandleNestedCoordinates() throws JsonProcessingException {
        // given - coordinates with high precision
        when(mockSession.get("coordinates")).thenReturn(Arrays.asList(
                "[[8.695556789, 49.392701123], [8.684623456, 49.398284789]]"));

        // when
        String result = MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode);
        JsonNode json = objectMapper.readTree(result);

        // then
        JsonNode locations = json.get("locations");
        assertEquals(2, locations.size());
        assertEquals(8.695556789, locations.get(0).get(0).asDouble(), 0.000000001);
        assertEquals(49.392701123, locations.get(0).get(1).asDouble(), 0.000000001);
        assertEquals(8.684623456, locations.get(1).get(0).asDouble(), 0.000000001);
        assertEquals(49.398284789, locations.get(1).get(1).asDouble(), 0.000000001);
    }
}