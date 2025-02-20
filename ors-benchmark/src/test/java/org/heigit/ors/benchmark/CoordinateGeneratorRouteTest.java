package org.heigit.ors.benchmark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CoordinateGeneratorRouteTest {
    private double[] extent;
    private TestCoordinateGeneratorRoute testGenerator;

    @Mock
    CloseableHttpClient closeableHttpClient;

    @Captor
    private ArgumentCaptor<HttpClientResponseHandler<String>> handlerCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        extent = new double[] { 7.6286, 50.3590, 7.7957, 50.4715 };
        testGenerator = new TestCoordinateGeneratorRoute(
                2, extent, "driving-car", null);
    }

    @Test
    void testGenerateRoutesSuccessful() throws Exception {
        String mockJsonResponse = """
                {
                    "durations": [[0,100.2],[100.3,0]],
                    "destinations": [
                        {"location": [8.666862, 49.413181], "snapped_distance": 200.94},
                        {"location": [8.676105, 49.41853], "snapped_distance": 19.11}
                    ],
                    "sources": [
                        {"location": [8.666862, 49.413181], "snapped_distance": 200.94},
                        {"location": [8.676105, 49.41853], "snapped_distance": 19.11}
                    ]
                }
                """;

        when(closeableHttpClient.execute(any(HttpPost.class), handlerCaptor.capture())).thenReturn(mockJsonResponse);

        testGenerator.setHttpClient(closeableHttpClient);
        testGenerator.generateRoutes();

        List<CoordinateGeneratorRoute.Route> result = testGenerator.getResult();
        assertEquals(2, result.size());
        verify(closeableHttpClient, atLeast(1)).execute(any(), handlerCaptor.capture());
    }

    @Test
    void testWriteCSVToFile(@TempDir Path tempDir) throws Exception {
        String mockJsonResponse = """
                {
                    "durations": [[0,100.3],[100.1,0]],
                    "destinations": [
                        {"location": [8.666862, 49.413181], "snapped_distance": 200.94},
                        {"location": [8.676105, 49.41853], "snapped_distance": 19.11}
                    ],
                    "sources": [
                        {"location": [8.666862, 49.413181], "snapped_distance": 200.94},
                        {"location": [8.676105, 49.41853], "snapped_distance": 19.11}
                    ]
                }
                """;

        when(closeableHttpClient.execute(any(HttpPost.class), handlerCaptor.capture())).thenReturn(mockJsonResponse);

        testGenerator.setHttpClient(closeableHttpClient);
        testGenerator.generateRoutes();

        String filename = "test_routes.csv";
        Path filePath = tempDir.resolve(filename);
        testGenerator.writeToCSV(filePath.toString());

        assertTrue(Files.exists(filePath));
        List<String> lines = Files.readAllLines(filePath);
        assertEquals("start_longitude,start_latitude,end_longitude,end_latitude,duration", lines.get(0));
        assertEquals(3, lines.size()); // Header + 2 routes
    }

    @Test
    void testProcessResponseSuccess() throws IOException {
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        HttpEntity entity = new StringEntity("test content");
        when(response.getCode()).thenReturn(HttpStatus.SC_OK);
        when(response.getEntity()).thenReturn(entity);

        String result = testGenerator.processResponse(response);
        assertEquals("test content", result);
    }

    @Test
    void testProcessResponseNonOkStatus() {
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        when(response.getCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);

        assertThrows(IOException.class, () -> testGenerator.processResponse(response));
    }

    private static Stream<Arguments> invalidConstructorParameters() {
        double[] validExtent = new double[] { 7.6286, 50.3590, 7.7957, 50.4715 };
        return Stream.of(
                Arguments.of(0, validExtent, "driving-car", "Number of routes must be positive"),
                Arguments.of(1, null, "driving-car", "Extent must contain 4 coordinates"),
                Arguments.of(1, new double[3], "driving-car", "Extent must contain 4 coordinates"),
                Arguments.of(1, validExtent, "", "Profile must not be empty"),
                Arguments.of(1, validExtent, null, "Profile must not be empty")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidConstructorParameters")
    void testInvalidConstructorParameters(int numRoutes, double[] extent, String profile, String expectedMessage) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TestCoordinateGeneratorRoute(numRoutes, extent, profile, null)
        );
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testUniquePairGeneration() throws Exception {
        String mockJsonResponse = """
                {
                    "durations": [[0,100.2,200.3],[100.0,0,300.6],[200.5,300.3,0]],
                    "destinations": [
                        {"location": [8.666862, 49.413181]},
                        {"location": [8.676105, 49.418530]},
                        {"location": [8.686105, 49.428530]}
                    ],
                    "sources": [
                        {"location": [8.666862, 49.413181]},
                        {"location": [8.676105, 49.418530]},
                        {"location": [8.686105, 49.428530]}
                    ]
                }
                """;

        when(closeableHttpClient.execute(any(HttpPost.class), handlerCaptor.capture())).thenReturn(mockJsonResponse);

        TestCoordinateGeneratorRoute generator = new TestCoordinateGeneratorRoute(
                6, extent, "driving-car", null);
        generator.setHttpClient(closeableHttpClient);
        generator.generateRoutes();

        List<CoordinateGeneratorRoute.Route> result = generator.getResult();
        assertEquals(6, result.size());

        // Verify all pairs are unique
        for (int i = 0; i < result.size(); i++) {
            for (int j = i + 1; j < result.size(); j++) {
                assertFalse(
                    areRoutePairsEqual(result.get(i), result.get(j)),
                    "Found duplicate route pair at indices " + i + " and " + j
                );
            }
        }
    }

    private boolean areRoutePairsEqual(CoordinateGeneratorRoute.Route r1, CoordinateGeneratorRoute.Route r2) {
        return coordinatesEqual(r1.start, r2.start) && coordinatesEqual(r1.end, r2.end);
    }

    private boolean coordinatesEqual(double[] coord1, double[] coord2) {
        return Math.abs(coord1[0] - coord2[0]) < 1e-6 && Math.abs(coord1[1] - coord2[1]) < 1e-6;
    }

    private class TestCoordinateGeneratorRoute extends CoordinateGeneratorRoute {
        private CloseableHttpClient testClient;

        public TestCoordinateGeneratorRoute(int numRoutes, double[] extent, String profile, String baseUrl) {
            super(numRoutes, extent, profile, baseUrl);
        }

        void setHttpClient(CloseableHttpClient client) {
            this.testClient = client;
        }

        @Override
        protected CloseableHttpClient createHttpClient() {
            return testClient;
        }
    }
}
