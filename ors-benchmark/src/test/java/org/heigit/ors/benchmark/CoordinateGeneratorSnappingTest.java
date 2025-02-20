package org.heigit.ors.benchmark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.heigit.ors.benchmark.CoordinateGeneratorSnapping.Point;
import org.mockito.MockedStatic;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.Map;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CoordinateGeneratorSnappingTest {
    private double[] extent;
    private TestCoordinateGeneratorSnapping testGenerator;
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorSnapping.class);

    @Mock
    CloseableHttpClient closeableHttpClient;

    @Captor
    private ArgumentCaptor<HttpClientResponseHandler<String>> handlerCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        extent = new double[] { 7.6286, 50.3590, 7.7957, 50.4715 };
        testGenerator = new TestCoordinateGeneratorSnapping(
                2, extent, 350, "driving-car", null);
    }

    @Test
    void testGeneratePointsSuccessful() throws Exception {
        String mockJsonResponse = """
                {
                    "locations": [
                        {"location": [8.666862, 49.413181], "snapped_distance": 200.94},
                        {"location": [8.676105, 49.41853], "name": "Berliner Straße", "snapped_distance": 19.11}
                    ]
                }
                """;

        when(closeableHttpClient.execute(any(HttpPost.class), handlerCaptor.capture())).thenReturn(mockJsonResponse);

        testGenerator.setHttpClient(closeableHttpClient);
        testGenerator.generatePoints();

        List<double[]> result = testGenerator.getResult();
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).length);
        verify(closeableHttpClient, atLeast(1)).execute(any(), handlerCaptor.capture());
    }

    @Test
    void testGeneratePointsWithInvalidResponse() throws Exception {
        String invalidResponse = "{ invalid json }";
        when(closeableHttpClient.execute(any(HttpPost.class), handlerCaptor.capture())).thenReturn(invalidResponse);

        testGenerator.setHttpClient(closeableHttpClient);
        testGenerator.generatePoints();

        List<double[]> result = testGenerator.getResult();
        assertTrue(result.isEmpty());
        verify(closeableHttpClient, atLeast(1)).execute(any(), handlerCaptor.capture());
    }

    @Test
    void testWriteCSVToFile(@TempDir Path tempDir) throws Exception {
        String mockJsonResponse = """
                {
                    "locations": [
                        {"location": [8.666862, 49.413181], "snapped_distance": 200.94},
                        {"location": [8.676105, 49.418530], "name": "Berliner Straße", "snapped_distance": 19.11}
                    ]
                }
                """;

        when(closeableHttpClient.execute(any(HttpPost.class), handlerCaptor.capture())).thenReturn(mockJsonResponse);

        testGenerator.setHttpClient(closeableHttpClient);
        testGenerator.generatePoints();

        String filename = "test_snap.csv";
        String filePath = tempDir.resolve(filename).toString();
        testGenerator.writeToCSV(filePath);

        String expected = "longitude,latitude\n8.666862,49.413181\n8.676105,49.418530\n";
        String result = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
        assertEquals(expected, result);
    }

    @Test
    void testProcessResponseSuccess() throws IOException {
        // Mock response with successful status and valid content
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        HttpEntity entity = new StringEntity("test content");
        when(response.getCode()).thenReturn(HttpStatus.SC_OK);
        when(response.getEntity()).thenReturn(entity);

        String result = testGenerator.processResponse(response);
        assertEquals("test content", result);
    }

    @Test
    void testProcessResponseNonOkStatus() {
        // Mock response with non-OK status
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        when(response.getCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);

        assertThrows(ClientProtocolException.class, () -> testGenerator.processResponse(response));
    }

    @Test
    void testProcessResponseNullEntity() {
        // Mock response with null entity
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        when(response.getCode()).thenReturn(HttpStatus.SC_OK);
        when(response.getEntity()).thenReturn(null);

        assertDoesNotThrow(() -> testGenerator.processResponse(response));
    }

    @Test
    void testProcessResponseParseError() {
        // Mock response and entity
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(response.getCode()).thenReturn(HttpStatus.SC_OK);
        when(response.getEntity()).thenReturn(entity);

        // Use MockedStatic to mock the static EntityUtils.toString method
        try (MockedStatic<EntityUtils> entityUtils = mockStatic(EntityUtils.class)) {
            entityUtils.when(() -> EntityUtils.toString(any(HttpEntity.class)))
                    .thenThrow(new ParseException("Failed to parse response entity"));

            IOException exception = assertThrows(IOException.class, () -> testGenerator.processResponse(response));
            assertEquals("Failed to parse response entity", exception.getMessage());
        }
    }

    @Test
    void testGeneratePointsWithDuplicates() throws Exception {
        // Mock response with duplicate points
        String mockJsonResponse = """
                {
                    "locations": [
                        {"location": [8.666862, 49.413181], "snapped_distance": 200.94},
                        {"location": [8.666862, 49.413181], "snapped_distance": 200.94},
                        {"location": [8.676105, 49.41853], "name": "Berliner Straße", "snapped_distance": 19.11},
                        {"location": [8.676105, 49.41853], "name": "Berliner Straße", "snapped_distance": 19.11},
                        {"location": [8.677000, 49.41900], "name": "Another Street", "snapped_distance": 25.00}
                    ]
                }
                """;

        when(closeableHttpClient.execute(any(HttpPost.class), handlerCaptor.capture())).thenReturn(mockJsonResponse);
        testGenerator = new TestCoordinateGeneratorSnapping(
                3, extent, 350, "driving-car", null);

        testGenerator.setHttpClient(closeableHttpClient);
        testGenerator.generatePoints();

        List<double[]> result = testGenerator.getResult();

        // Should only contain unique points (3 instead of 5)
        assertEquals(3, result.size());

        // Verify the points are actually unique by checking coordinates
        Set<String> uniqueCoords = new HashSet<>();
        for (double[] point : result) {
            String coordKey = String.format("%.6f,%.6f", point[0], point[1]);
            assertTrue(uniqueCoords.add(coordKey),
                    "Point " + coordKey + " should not already exist in results");
        }
        assertTrue(uniqueCoords.contains("8.666862,49.413181"));
        assertTrue(uniqueCoords.contains("8.676105,49.418530"));
        assertTrue(uniqueCoords.contains("8.677000,49.419000"));
    }

    @Test
    void testInvalidInputParameters() {
        assertThrows(IllegalArgumentException.class,
                () -> new TestCoordinateGeneratorSnapping(-1, extent, 350, "driving-car", null));

        assertThrows(IllegalArgumentException.class,
                () -> new TestCoordinateGeneratorSnapping(2, new double[3], 350, "driving-car", null));

        assertThrows(IllegalArgumentException.class,
                () -> new TestCoordinateGeneratorSnapping(2, extent, -1, "driving-car", null));

        assertThrows(IllegalArgumentException.class,
                () -> new TestCoordinateGeneratorSnapping(2, extent, 350, "", null));
    }

    @Test
    void testEmptyResponseHandling() throws Exception {
        when(closeableHttpClient.execute(any(HttpPost.class), handlerCaptor.capture())).thenReturn("{}");

        testGenerator.setHttpClient(closeableHttpClient);
        testGenerator.generatePoints();

        assertTrue(testGenerator.getResult().isEmpty());
    }

    @Test
    void testBatchProcessing() throws Exception {
        int totalPoints = 100;
        int numBatches = 3;
        List<String> responses = createDistributedMockResponses(totalPoints, numBatches);

        // Setup sequential responses
        when(closeableHttpClient.execute(any(HttpPost.class), handlerCaptor.capture()))
                .thenReturn(responses.get(0))
                .thenReturn(responses.get(1))
                .thenReturn(responses.get(2));

        TestCoordinateGeneratorSnapping largeGenerator = new TestCoordinateGeneratorSnapping(
                totalPoints, extent, 350, "driving-car", null);
        largeGenerator.setHttpClient(closeableHttpClient);

        largeGenerator.generatePoints();

        List<double[]> result = largeGenerator.getResult();
        assertEquals(totalPoints, result.size());
        verify(closeableHttpClient, times(3)).execute(any(), handlerCaptor.capture());

        // Verify all points are unique across batches
        Set<String> allCoords = new HashSet<>();
        for (double[] point : result) {
            String coordKey = String.format("%.6f,%.6f", point[0], point[1]);
            assertTrue(allCoords.add(coordKey),
                    "Point " + coordKey + " should not already exist in results");
        }
    }

    private List<String> createDistributedMockResponses(int totalPoints, int numBatches) {
        Set<String> allUsedCoords = new HashSet<>();
        List<String> responses = new ArrayList<>();

        int basePointsPerBatch = totalPoints / numBatches;
        int remainingPoints = totalPoints % numBatches;

        for (int batch = 0; batch < numBatches; batch++) {
            int pointsForThisBatch = basePointsPerBatch + (batch < remainingPoints ? 1 : 0);
            responses.add(createMockResponseWithNPoints(pointsForThisBatch, allUsedCoords));
        }

        return responses;
    }

    private String createMockResponseWithNPoints(int n) {
        return createMockResponseWithNPoints(n, new HashSet<>());
    }

    private String createMockResponseWithNPoints(int n, Set<String> existingCoords) {
        Random random = new Random(42); // Fixed seed for reproducibility
        StringBuilder responseBuilder = new StringBuilder("{\"locations\":[");
        int added = 0;

        while (added < n) {
            double lon = random.nextDouble() * (extent[2] - extent[0]) + extent[0];
            double lat = random.nextDouble() * (extent[3] - extent[1]) + extent[1];
            String coord = String.format("%.6f,%.6f", lon, lat);

            if (!existingCoords.contains(coord)) {
                if (added > 0) {
                    responseBuilder.append(",");
                }
                String[] parts = coord.split(",");
                responseBuilder.append(String.format(
                        "{\"location\":[%s,%s],\"snapped_distance\":20.0}",
                        parts[0], parts[1]));
                added++;
                existingCoords.add(coord);
            }
        }

        responseBuilder.append("]}");
        return responseBuilder.toString();
    }

    @Test
    void testGeneratePointsMaxAttemptsReached() throws Exception {
        // Mock response that always returns the same point
        String mockJsonResponse = """
                {
                    "locations": [
                        {"location": [8.666862, 49.413181], "snapped_distance": 200.94}
                    ]
                }
                """;

        when(closeableHttpClient.execute(any(HttpPost.class), handlerCaptor.capture()))
                .thenReturn(mockJsonResponse);

        testGenerator = new TestCoordinateGeneratorSnapping(
                3, extent, 350, "driving-car", null);
        testGenerator.setHttpClient(closeableHttpClient);

        testGenerator.generatePoints(2); // Set max attempts to 2

        List<double[]> result = testGenerator.getResult();
        assertEquals(1, result.size()); // Should only contain one point as others were duplicates
        verify(closeableHttpClient, atLeast(2)).execute(any(), handlerCaptor.capture());
    }

    @Test
    void testGeneratePointsSuccessBeforeMaxAttempts() throws Exception {
        // First response has unique points, should succeed before max attempts
        String successResponse = createMockResponseWithNPoints(2);

        when(closeableHttpClient.execute(any(HttpPost.class), handlerCaptor.capture()))
                .thenReturn(successResponse);

        testGenerator = new TestCoordinateGeneratorSnapping(
                2, extent, 350, "driving-car", null);
        testGenerator.setHttpClient(closeableHttpClient);

        testGenerator.generatePoints(5);

        List<double[]> result = testGenerator.getResult();
        assertEquals(2, result.size());
        verify(closeableHttpClient, times(1)).execute(any(), handlerCaptor.capture());
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    void testPointEquals() {
        double coordinatePrecision = 1e-6;
        Point point1 = new Point(new double[] { 8.123456, 49.123456 });

        // Test same object reference
        assertEquals(point1, point1, "Point should equal itself");

        // Test null comparison
        assertEquals(false, point1.equals(null), "Point should not equal null");

        // Test different class
        assertEquals(false, point1.equals("not a point"), "Point should not equal different class");

        // Test exact same coordinates
        Point point2 = new Point(new double[] { 8.123456, 49.123456 });
        assertEquals(point1, point2, "Points with same coordinates should be equal");

        // Test coordinates within precision
        Point point3 = new Point(new double[] {
                8.123456 + (coordinatePrecision / 2),
                49.123456 + (coordinatePrecision / 2)
        });
        assertEquals(point1, point3, "Points within precision should be equal");

        // Test coordinates just outside precision
        Point point4 = new Point(new double[] {
                8.123456 + (coordinatePrecision * 2),
                49.123456 + (coordinatePrecision * 2)
        });
        assertEquals(false, point1.equals(point4), "Points outside precision should not be equal");

        // Test only longitude differs
        Point point5 = new Point(new double[] { 8.123457, 49.123456 });
        assertEquals(false, point1.equals(point5), "Points with different longitude should not be equal");

        // Test only latitude differs
        Point point6 = new Point(new double[] { 8.123456, 49.123457 });
        assertEquals(false, point1.equals(point6), "Points with different latitude should not be equal");
    }

    @Test
    void testPointEqualsSymmetry() {
        Point point1 = new Point(new double[] { 8.123456, 49.123456 });
        Point point2 = new Point(new double[] { 8.123456, 49.123456 });

        // Test symmetry of equals
        boolean symmetric = point1.equals(point2) && point2.equals(point1);
        assertEquals(true, symmetric, "Equals should be symmetric");
    }

    @Test
    void testPointEqualsTransitivity() {
        Point point1 = new Point(new double[] { 8.123456, 49.123456 });
        Point point2 = new Point(new double[] { 8.123456, 49.123456 });
        Point point3 = new Point(new double[] { 8.123456, 49.123456 });

        // Test transitivity of equals
        boolean transitive = point1.equals(point2) && point2.equals(point3) && point1.equals(point3);
        assertEquals(true, transitive, "Equals should be transitive");
    }

    @Test
    void testPointEqualsConsistency() {
        Point point1 = new Point(new double[] { 8.123456, 49.123456 });
        Point point2 = new Point(new double[] { 8.123456, 49.123456 });

        // Test consistency of equals
        boolean firstCall = point1.equals(point2);
        for (int i = 0; i < 10; i++) {
            assertEquals(firstCall, point1.equals(point2),
                    "Equals should be consistent across multiple calls");
        }
    }

    private static Stream<Arguments> invalidConstructorParameters() {
        double[] validExtent = new double[] { 7.6286, 50.3590, 7.7957, 50.4715 };
        return Stream.of(
                // Test invalid numPoints
                Arguments.of(
                        0, validExtent, 350.0, "driving-car",
                        "Number of points must be positive"),
                Arguments.of(
                        -1, validExtent, 350.0, "driving-car",
                        "Number of points must be positive"),

                // Test invalid extent
                Arguments.of(
                        1, null, 350.0, "driving-car",
                        "Extent must contain 4 coordinates"),
                Arguments.of(
                        1, new double[3], 350.0, "driving-car",
                        "Extent must contain 4 coordinates"),
                Arguments.of(
                        1, new double[5], 350.0, "driving-car",
                        "Extent must contain 4 coordinates"),

                // Test invalid radius
                Arguments.of(
                        1, validExtent, 0.0, "driving-car",
                        "Radius must be positive"),
                Arguments.of(
                        1, validExtent, -1.0, "driving-car",
                        "Radius must be positive"),

                // Test invalid profile
                Arguments.of(
                        1, validExtent, 350.0, null,
                        "Profile must not be empty"),
                Arguments.of(
                        1, validExtent, 350.0, "",
                        "Profile must not be empty"),
                Arguments.of(
                        1, validExtent, 350.0, "   ",
                        "Profile must not be empty"));
    }

    @ParameterizedTest
    @MethodSource("invalidConstructorParameters")
    void testInvalidConstructorParameters(
            int numPoints,
            double[] extent,
            double radius,
            String profile,
            String expectedMessage) {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TestCoordinateGeneratorSnapping(numPoints, extent, radius, profile, null),
                "Constructor should throw IllegalArgumentException for invalid parameters");

        assertEquals(expectedMessage, exception.getMessage(),
                "Exception message should match expected message");
    }

    @Test
    void testValidConstructorParameters() {
        double[] validExtent = new double[] { 7.6286, 50.3590, 7.7957, 50.4715 };
        assertDoesNotThrow(() -> new TestCoordinateGeneratorSnapping(1, validExtent, 350.0, "driving-car", null),
                "Constructor should not throw exception for valid parameters");
    }

    private static Stream<Arguments> apiKeyTestParameters() {
        return Stream.of(
                // Test local URL (should not require API key)
                Arguments.of(
                        "http://localhost:8082/ors",
                        null,
                        null,
                        true,
                        "Local URL should not require API key"),
                // Test openrouteservice.org URL with system property
                Arguments.of(
                        "https://api.openrouteservice.org",
                        null,
                        "test-api-key",
                        true,
                        "Should accept API key from system property"),

                // Test openrouteservice.org URL without API key
                Arguments.of(
                        "https://api.openrouteservice.org",
                        null,
                        null,
                        false,
                        "Should fail without API key for openrouteservice.org"));
    }

    @ParameterizedTest
    @MethodSource("apiKeyTestParameters")
    void testApiKeyValidation(
            String baseUrl,
            String envApiKey,
            String systemPropertyApiKey,
            boolean shouldSucceed,
            String message) {

        // Store original environment
        String originalPropertyKey = System.getProperty("ORS_API_KEY");

        try {
            // Setup environment for test
            if (envApiKey != null) {
                withEnvironmentVariable("ORS_API_KEY", envApiKey);
            }
            if (systemPropertyApiKey != null) {
                System.setProperty("ORS_API_KEY", systemPropertyApiKey);
            }

            if (shouldSucceed) {
                assertDoesNotThrow(() -> new TestCoordinateGeneratorSnapping(
                        1, extent, 350, "driving-car", baseUrl),
                        message);
            } else {
                assertThrows(IllegalStateException.class, () -> new TestCoordinateGeneratorSnapping(
                        1, extent, 350, "driving-car", baseUrl),
                        message);
            }

        } finally {
            if (originalPropertyKey != null) {
                System.setProperty("ORS_API_KEY", originalPropertyKey);
            } else {
                System.clearProperty("ORS_API_KEY");
            }
        }
    }

    // Helper method to simulate environment variable
    private void withEnvironmentVariable(String name, String value) {
        try {
            setEnv(name, value);
        } catch (Exception e) {
            LOGGER.error("Failed to set environment variable: {}", e.getMessage());
        }
    }

    // Utility method to set environment variables (requires security permissions)
    @SuppressWarnings({ "unchecked" })
    private void setEnv(String name, String value) throws Exception {
        Map<String, String> env = System.getenv();
        Field field = env.getClass().getDeclaredField("m");
        field.setAccessible(true);
        ((Map<String, String>) field.get(env)).put(name, value);
    }

    private class TestCoordinateGeneratorSnapping extends CoordinateGeneratorSnapping {
        private CloseableHttpClient testClient;

        public TestCoordinateGeneratorSnapping(int numPoints, double[] extent, double radius, String profile,
                String baseUrl) {
            super(numPoints, extent, radius, profile, baseUrl);
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
