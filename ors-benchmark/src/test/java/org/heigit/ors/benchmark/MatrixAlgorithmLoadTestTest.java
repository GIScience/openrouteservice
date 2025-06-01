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
    private Config mockConfig;
    private MatrixModes mockMode;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockSession = mock(Session.class);
        mockConfig = mock(Config.class);
        mockMode = mock(MatrixModes.class);

        // Mock CSV data as it would appear in the session
        when(mockSession.get("coordinates"))
                .thenReturn("[[8.695556, 49.392701], [8.684623, 49.398284], [8.705916, 49.406309]]");
        when(mockSession.get("sources")).thenReturn("[0, 1]");
        when(mockSession.get("destinations")).thenReturn("[2]");
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
    void parseCoordinatesFromString_ShouldParseValidCoordinates() {
        // given
        String coordinatesStr = "[[8.695556, 49.392701], [8.684623, 49.398284]]";

        // when
        List<List<Double>> result = MatrixAlgorithmLoadTest.parseCoordinatesFromString(coordinatesStr);

        // then
        assertEquals(2, result.size());
        assertEquals(8.695556, result.get(0).get(0), 0.000001);
        assertEquals(49.392701, result.get(0).get(1), 0.000001);
        assertEquals(8.684623, result.get(1).get(0), 0.000001);
        assertEquals(49.398284, result.get(1).get(1), 0.000001);
    }

    @Test
    void parseCoordinatesFromString_ShouldHandleQuotedStrings() {
        // given
        String coordinatesStr = "\"[[8.695556, 49.392701], [8.684623, 49.398284]]\"";

        // when
        List<List<Double>> result = MatrixAlgorithmLoadTest.parseCoordinatesFromString(coordinatesStr);

        // then
        assertEquals(2, result.size());
        assertEquals(8.695556, result.get(0).get(0), 0.000001);
        assertEquals(49.392701, result.get(0).get(1), 0.000001);
    }

    @Test
    void parseCoordinatesFromString_ShouldThrowExceptionForNullInput() {
        // when & then
        assertThrows(RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.parseCoordinatesFromString(null));
    }

    @Test
    void parseCoordinatesFromString_ShouldThrowExceptionForEmptyInput() {
        // when & then
        assertThrows(RequestBodyCreationException.class, () -> MatrixAlgorithmLoadTest.parseCoordinatesFromString(""));
    }

    @Test
    void parseCoordinatesFromString_ShouldThrowExceptionForInvalidCoordinatePair() {
        // given
        String invalidCoordinates = "[[8.695556], [8.684623, 49.398284]]";

        // when & then
        assertThrows(RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.parseCoordinatesFromString(invalidCoordinates));
    }

    @Test
    void parseIntegerArrayFromString_ShouldParseValidArray() {
        // given
        String arrayStr = "[0, 1, 2]";

        // when
        List<Integer> result = MatrixAlgorithmLoadTest.parseIntegerArrayFromString(arrayStr);

        // then
        assertEquals(3, result.size());
        assertEquals(0, result.get(0));
        assertEquals(1, result.get(1));
        assertEquals(2, result.get(2));
    }

    @Test
    void parseIntegerArrayFromString_ShouldHandleQuotedStrings() {
        // given
        String arrayStr = "\"[0, 1, 2]\"";

        // when
        List<Integer> result = MatrixAlgorithmLoadTest.parseIntegerArrayFromString(arrayStr);

        // then
        assertEquals(3, result.size());
        assertEquals(0, result.get(0));
        assertEquals(1, result.get(1));
        assertEquals(2, result.get(2));
    }

    @Test
    void parseIntegerArrayFromString_ShouldReturnEmptyListForEmptyArray() {
        // given
        String arrayStr = "[]";

        // when
        List<Integer> result = MatrixAlgorithmLoadTest.parseIntegerArrayFromString(arrayStr);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void parseIntegerArrayFromString_ShouldThrowExceptionForNullInput() {
        // when & then
        assertThrows(RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.parseIntegerArrayFromString(null));
    }

    @Test
    void parseIntegerArrayFromString_ShouldThrowExceptionForEmptyInput() {
        // when & then
        assertThrows(RequestBodyCreationException.class, () -> MatrixAlgorithmLoadTest.parseIntegerArrayFromString(""));
    }

    @Test
    void parseIntegerArrayFromString_ShouldThrowExceptionForInvalidInteger() {
        // given
        String invalidArray = "[0, invalid, 2]";

        // when & then
        assertThrows(RequestBodyCreationException.class,
                () -> MatrixAlgorithmLoadTest.parseIntegerArrayFromString(invalidArray));
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
        when(mockSession.get("sources")).thenReturn("[0, 1, 2, 3]");
        when(mockSession.get("destinations")).thenReturn("[4, 5, 6, 7, 8]");

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
        when(mockSession.get("coordinates")).thenReturn("[[8.695556, 49.392701]]");
        when(mockSession.get("sources")).thenReturn("[0]");
        when(mockSession.get("destinations")).thenReturn("[0]");

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
    void parseCoordinatesFromString_ShouldHandleLargeCoordinateArray() {
        // given
        String coordinatesStr = "[[8.695556, 49.392701], [8.684623, 49.398284], [8.705916, 49.406309], [8.689981, 49.394522], [8.681502, 49.394791]]";

        // when
        List<List<Double>> result = MatrixAlgorithmLoadTest.parseCoordinatesFromString(coordinatesStr);

        // then
        assertEquals(5, result.size());
        assertEquals(8.695556, result.get(0).get(0), 0.000001);
        assertEquals(49.392701, result.get(0).get(1), 0.000001);
        assertEquals(8.681502, result.get(4).get(0), 0.000001);
        assertEquals(49.394791, result.get(4).get(1), 0.000001);
    }
}