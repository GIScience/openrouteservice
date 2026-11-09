package org.heigit.ors.benchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.Session;
import org.heigit.ors.benchmark.BenchmarkEnums.MatrixModes;
import org.heigit.ors.benchmark.exceptions.RequestBodyCreationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    private static final List<String> VALID_COORDINATES =
            Arrays.asList(
                    "[[8.695556, 49.392701], [8.684623, 49.398284], [8.705916, 49.406309]]"
            );
    private static final List<String> VALID_SOURCES =
            Arrays.asList("[0, 1]");
    private static final List<String> VALID_DESTINATIONS =
            Arrays.asList("[2]");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockSession = mock(Session.class);
        mockMode = mock(MatrixModes.class);

        // Mock CSV data as it would appear in the session after CSV feeder
        // Each CSV column becomes a List<String> in the session
        when(mockSession.get("coordinates"))
                .thenReturn(VALID_COORDINATES);
        when(mockSession.get("sources")).thenReturn(VALID_SOURCES);
        when(mockSession.get("destinations")).thenReturn(VALID_DESTINATIONS);
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

    @ParameterizedTest(name = "createRequestBody throws if '{0}' list is empty")
    @ValueSource(strings = { "coordinates", "sources", "destinations" })
    void createRequestBody_ShouldThrowExceptionForEmptyList(String emptyKey) {
        // only override the one under test to be empty
        when(mockSession.get(emptyKey)).thenReturn(List.of());

        // verify that empty‐list triggers the exception
        assertThrows(
                RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode)
        );
    }

    @ParameterizedTest(name = "createRequestBody should throw when '{0}' is missing")
    @ValueSource(strings = { "coordinates", "sources", "destinations" })
    void createRequestBody_ShouldThrowExceptionForMissingRequiredSessionAttributes(String missingAttribute) {
        when(mockSession.get(missingAttribute)).thenReturn(null);
        assertThrows(RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode));
    }

    @ParameterizedTest(name = "createRequestBody throws when '{0}' contains invalid JSON")
    @ValueSource(strings = { "coordinates", "sources", "destinations" })
    void createRequestBody_ShouldThrowExceptionForInvalidJson(String invalidKey) {
        // override the one under test to invalid JSON
        when(mockSession.get(invalidKey)).thenReturn(List.of("invalid json"));

        assertThrows(
                RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.createRequestBody(mockSession, mockMode)
        );
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