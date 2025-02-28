package org.heigit.ors.generators;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoordinateGeneratorRouteTest extends AbstractCoordinateGeneratorTest {
    private TestCoordinateGeneratorRoute testGenerator;

    @BeforeEach
    @Override
    protected void setUpBase() {
        super.setUpBase();
        testGenerator = new TestCoordinateGeneratorRoute(2, extent, "driving-car", null, 0);
    }

    @Override
    protected AbstractCoordinateGenerator createTestGenerator() {
        return new TestCoordinateGeneratorRoute(2, extent, "driving-car", null, 0);
    }

    @Test
    void testGenerateRoutesSuccessful() throws Exception {
        String mockJsonResponse = """
                {
                    "distances": [[0,1500.2],[1500.3,0]],
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

        ClassicHttpResponse mockResponse = mock(ClassicHttpResponse.class);
        when(mockResponse.getCode()).thenReturn(HttpStatus.SC_OK);
        when(mockResponse.getEntity()).thenReturn(new StringEntity(mockJsonResponse, ContentType.APPLICATION_JSON));

        when(closeableHttpClient.execute(any(HttpPost.class), handlerCaptor.capture())).thenAnswer(invocation -> {
            HttpClientResponseHandler<String> handler = invocation.getArgument(1);
            return handler.handleResponse(mockResponse);
        });

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
                    "distances": [[0,1500.3],[1500.1,0]],
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
        assertEquals("start_longitude,start_latitude,end_longitude,end_latitude,distance", lines.get(0)); // Updated
                                                                                                          // header
        assertEquals(3, lines.size()); // Header + 2 routes
    }

    @Test
    void testMinimumDistanceFiltering() throws Exception {
        String mockJsonResponse = """
                {
                    "distances": [
                        [0, 500.2, 1500.3],
                        [500.0, 0, 2000.6],
                        [1500.5, 2000.3, 0]
                    ],
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

        // Create generator with minimum distance of 1000 meters
        TestCoordinateGeneratorRoute generator = new TestCoordinateGeneratorRoute(
                6, extent, "driving-car", null, 1000.0);
        generator.setHttpClient(closeableHttpClient);
        generator.generateRoutes();

        List<CoordinateGeneratorRoute.Route> result = generator.getResult();

        // Should only include routes with distance > 1000m
        for (CoordinateGeneratorRoute.Route route : result) {
            assertTrue(route.distance > 1000.0,
                    "Route distance " + route.distance + " should be greater than minimum 1000.0");
        }
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> invalidConstructorParameters() {
        double[] validExtent = new double[] { 7.6286, 50.3590, 7.7957, 50.4715 };
        return Stream.of(
                Arguments.of(0, validExtent, "driving-car", 0.0, "Number of routes must be positive"),
                Arguments.of(1, null, "driving-car", 0.0, "Extent must contain 4 coordinates"),
                Arguments.of(1, new double[3], "driving-car", 0.0, "Extent must contain 4 coordinates"),
                Arguments.of(1, validExtent, "", 0.0, "Profile must not be empty"),
                Arguments.of(1, validExtent, null, 0.0, "Profile must not be empty"),
                Arguments.of(1, validExtent, "driving-car", -1.0, "Minimum distance must be non-negative"));
    }

    @ParameterizedTest
    @MethodSource("invalidConstructorParameters")
    void testInvalidConstructorParameters(int numRoutes, double[] extent, String profile, double minDistance,
            String expectedMessage) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TestCoordinateGeneratorRoute(numRoutes, extent, profile, null, minDistance));
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testUniquePairGeneration() throws Exception {
        String mockJsonResponse = """
                {
                    "distances": [[0,100.2,200.3],[100.0,0,300.6],[200.5,300.3,0]],
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
                                "Found duplicate route pair at indices " + i + " and " + j);
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
            super(numRoutes, extent, profile, baseUrl, 0);
        }

        public TestCoordinateGeneratorRoute(int numRoutes, double[] extent, String profile, String baseUrl,
                double minDistance) {
            super(numRoutes, extent, profile, baseUrl, minDistance);
        }

        void setHttpClient(CloseableHttpClient client) {
            this.testClient = client;
        }

        @Override
        protected CloseableHttpClient createHttpClient() {
            return testClient != null ? testClient : super.createHttpClient();
        }
    }
}
