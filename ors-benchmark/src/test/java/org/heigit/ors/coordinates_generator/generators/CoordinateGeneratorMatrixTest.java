package org.heigit.ors.coordinates_generator.generators;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.heigit.ors.coordinates_generator.model.Matrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CoordinateGeneratorMatrixTest {
    private static final double[] TEST_EXTENT = { 8.573179, 49.352003, 8.793986, 49.459693 };
    private static final String[] DEFAULT_PROFILES = new String[] { "driving-car" };
    private static final int NUM_MATRICES = 4;
    private static final int NUM_ROWS = 4;
    private static final int NUM_COLS = 4;


    @Mock
    private CloseableHttpClient mockHttpClient;

    @Captor
    private ArgumentCaptor<HttpClientResponseHandler<String>> handlerCaptor;

    private TestCoordinateGeneratorMatrix testGenerator;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Map<String, Double> maxDistanceMap = new HashMap<>();
        maxDistanceMap.put("driving-car", 10000.0);
        testGenerator = new TestCoordinateGeneratorMatrix(NUM_MATRICES, TEST_EXTENT, DEFAULT_PROFILES, "http://localhost:8080/ors",
                maxDistanceMap, new CoordinateGeneratorMatrix.MatrixDimensions(NUM_ROWS, NUM_COLS));
        testGenerator.setHttpClient(mockHttpClient);
    }

    @Test
    void testCreateHeaders() {
        Map<String, String> headers = testGenerator.createHeaders();
        assertEquals(2, headers.size(), "Should have created 1 header");
        assertTrue(headers.containsKey("Content-Type"), "Should have Content-Type header");
        assertTrue(headers.containsKey("Accept"), "Should have Accept header");
        assertFalse(headers.containsKey("Authorization"), "Should not have Authorization header");
        assertEquals("application/json", headers.get("Content-Type"), "Content-Type should be 'application/json'");
        assertEquals("application/json", headers.get("Accept"), "Accept should be 'application/json'");
    }

    @Test
    void testCreateHeadersWithAuthorization() {
        // Set properties for authorization
        System.setProperty("ORS_API_KEY", "test-key");
        TestCoordinateGeneratorMatrix routeCoordinateGenerator = new TestCoordinateGeneratorMatrix(4, TEST_EXTENT,
                DEFAULT_PROFILES,
                "https://openrouteservice.org/", new HashMap<>(), new CoordinateGeneratorMatrix.MatrixDimensions(NUM_ROWS, NUM_COLS));
        Map<String, String> headers = routeCoordinateGenerator.createHeaders();
        assertTrue(headers.containsKey("Authorization"), "Should have Authorization header");
        assertEquals("test-key", headers.get("Authorization"),
                "Authorization header should contain value from ORS_API_KEY");
    }

    @Test
    void testGenerateMatricesSuccessful() throws Exception {
        // Create mock responses for snap and matrix
        String snapResponse = """
                {
                    "locations": [
                        {"location": [8.669629,49.413025], "snapped_distance": 200.94},
                        {"location": [8.675841,49.418532], "snapped_distance": 19.11},
                        {"location": [8.68278,49.41985], "snapped_distance": 25.11},
                        {"location": [8.68289,49.41457], "snapped_distance": 18.11}
                    ]
                }
                """;
        String firstMatrixResponse = """
                {
                    "distances": [[1500.2]],
                    "destinations": [
                        {"location": [8.665144,49.415594]}
                    ],
                    "sources": [
                        {"location": [8.669629,49.413025]}
                    ]
                }
                """;
        String secondMatrixResponse = """
                {
                    "distances": [[500.0]],
                    "destinations": [
                        {"location": [8.665144,49.415594]}
                    ],
                    "sources": [
                        {"location": [8.675841,49.418532]}
                    ]
                }
                """;

        String thirdMatrixResponse = """
                {
                    "distances": [[600.0]],
                    "destinations": [
                        {"location": [8.665144,49.415594]}
                    ],
                    "sources": [
                        {"location": [8.68278,49.41985]}
                    ]
                }
                """;
        String fourthMatrixResponse = """
                {
                    "distances": [[600.0]],
                    "destinations": [
                        {"location": [8.665144,49.415594]}
                    ],
                    "sources": [
                        {"location": [8.68289,49.41457]}
                    ]
                }
                """;

        // Set up mock for both snap and matrix requests
        ClassicHttpResponse mockSnapResponse = mock(ClassicHttpResponse.class);
        when(mockSnapResponse.getCode()).thenReturn(HttpStatus.SC_OK);
        when(mockSnapResponse.getEntity()).thenReturn(new StringEntity(
                snapResponse, ContentType.APPLICATION_JSON));
        // First Matrix response
        ClassicHttpResponse mockMatrixResponse = mock(ClassicHttpResponse.class);
        when(mockMatrixResponse.getCode()).thenReturn(HttpStatus.SC_OK);
        when(mockMatrixResponse.getEntity()).thenReturn(new StringEntity(
                firstMatrixResponse, ContentType.APPLICATION_JSON));

        // Second Matrix response
        ClassicHttpResponse mockMatrixResponse2 = mock(ClassicHttpResponse.class);
        when(mockMatrixResponse2.getCode()).thenReturn(HttpStatus.SC_OK);
        when(mockMatrixResponse2.getEntity()).thenReturn(new StringEntity(
                secondMatrixResponse, ContentType.APPLICATION_JSON));

        ClassicHttpResponse mockMatrixResponse3 = mock(ClassicHttpResponse.class);
        when(mockMatrixResponse3.getCode()).thenReturn(HttpStatus.SC_OK);
        when(mockMatrixResponse3.getEntity()).thenReturn(new StringEntity(
                thirdMatrixResponse, ContentType.APPLICATION_JSON));

        ClassicHttpResponse mockMatrixResponse4 = mock(ClassicHttpResponse.class);
        when(mockMatrixResponse4.getCode()).thenReturn(HttpStatus.SC_OK);
        when(mockMatrixResponse4.getEntity()).thenReturn(new StringEntity(
                fourthMatrixResponse, ContentType.APPLICATION_JSON));

        // Mock the execute calls to return appropriate responses based on URL path
        AtomicInteger matrixCallCount = new AtomicInteger(0);
        when(mockHttpClient.execute(any(HttpPost.class), handlerCaptor.capture())).thenAnswer(invocation -> {
            HttpPost request = invocation.getArgument(0);
            HttpClientResponseHandler<String> handler = handlerCaptor.getValue();
            String path = request.getPath();

            if (path.contains("snap")) {
                return handler.handleResponse(mockSnapResponse);
            } else if (path.contains("matrix")) {
                int callIndex = matrixCallCount.getAndIncrement() % 4; // Cycles 0,1,2,3,0,1...
                switch (callIndex) {
                    case 0:
                        return handler.handleResponse(mockMatrixResponse);
                    case 1:
                        return handler.handleResponse(mockMatrixResponse2);
                    case 2:
                        return handler.handleResponse(mockMatrixResponse3);
                    case 3:
                        return handler.handleResponse(mockMatrixResponse4);
                    default:
                        return handler.handleResponse(mockMatrixResponse);
                }
            }
            return null;
        });

        // Execute test
        testGenerator.generateMatrices();

        // Verify results
        List<Matrix> result = testGenerator.getResult();
        assertEquals(NUM_MATRICES, result.size(), "Should have generated " + NUM_MATRICES + " matrices");
        verify(mockHttpClient, atLeast(3)).execute(any(), handlerCaptor.capture());
    }

    @Test
    void testMultipleProfiles() throws Exception {
        // Set up generator with multiple profiles
        String[] profiles = new String[] { "driving-car", "cycling-regular" };
        Map<String, Double> maxDistanceMap = new HashMap<>();
        maxDistanceMap.put("driving-car", 10000.0);
        maxDistanceMap.put("cycling-regular", 5000.0);
        testGenerator = new TestCoordinateGeneratorMatrix(NUM_MATRICES, TEST_EXTENT, profiles, "http://localhost:8080/ors",
                maxDistanceMap, new CoordinateGeneratorMatrix.MatrixDimensions(NUM_ROWS, NUM_COLS));
        testGenerator.setHttpClient(mockHttpClient);

        // Create mock responses
        String snapResponse = """
                {
                    "locations": [
                        {"location": [8.666862, 49.413181]},
                        {"location": [8.676105, 49.41853]}
                    ]
                }
                """;

        String matrixResponse = """
                {
                    "distances": [[0,1500.2],[1500.3,0]],
                    "destinations": [
                        {"location": [8.666862, 49.413181]},
                        {"location": [8.676105, 49.41853]}
                    ],
                    "sources": [
                        {"location": [8.666862, 49.413181]},
                        {"location": [8.676105, 49.41853]}
                    ]
                }
                """;

        // Mock HTTP responses
        ClassicHttpResponse mockResponse = mock(ClassicHttpResponse.class);
        when(mockResponse.getCode()).thenReturn(HttpStatus.SC_OK);
        when(mockResponse.getEntity()).thenReturn(new StringEntity(snapResponse, ContentType.APPLICATION_JSON),
                new StringEntity(matrixResponse, ContentType.APPLICATION_JSON));

        when(mockHttpClient.execute(any(HttpPost.class), handlerCaptor.capture())).thenAnswer(invocation -> {
            HttpClientResponseHandler<String> handler = handlerCaptor.getValue();
            return handler.handleResponse(mockResponse);
        });

        // Execute test
        testGenerator.generateMatrices();

        // Verify results
        List<Matrix> result = testGenerator.getResult();
        assertTrue(result.size() <= 4, "Should have generated up to 4 routes (2 per profile)");
    }

    @Test
    void testWriteCSVToFile(@TempDir Path tempDir) throws Exception {
        // Set up mock responses
        String snapResponse = """
                {
                    "locations": [
                        {"location": [8.666862, 49.413181]},
                        {"location": [8.676105, 49.41853]}
                    ]
                }
                """;

        String matrixResponse = """
                {
                    "distances": [[0,1500.3],[1500.1,0]],
                    "destinations": [
                        {"location": [8.666862, 49.413181]},
                        {"location": [8.676105, 49.41853]}
                    ],
                    "sources": [
                        {"location": [8.666862, 49.413181]},
                        {"location": [8.676105, 49.41853]}
                    ]
                }
                """;

        // Mock HTTP responses
        ClassicHttpResponse mockResponse = mock(ClassicHttpResponse.class);
        when(mockResponse.getCode()).thenReturn(HttpStatus.SC_OK);
        when(mockResponse.getEntity()).thenReturn(new StringEntity(snapResponse, ContentType.APPLICATION_JSON),
                new StringEntity(matrixResponse, ContentType.APPLICATION_JSON));

        when(mockHttpClient.execute(any(HttpPost.class), handlerCaptor.capture())).thenAnswer(invocation -> {
            HttpClientResponseHandler<String> handler = handlerCaptor.getValue();
            return handler.handleResponse(mockResponse);
        });

        // Execute test
        testGenerator.generateMatrices();

        // Write results to CSV
        String filename = tempDir.resolve("test_routes.csv").toString();
        testGenerator.writeToCSV(filename);

        // Verify file was created with expected content
        List<String> lines = Files.readAllLines(Path.of(filename));
        assertFalse(lines.isEmpty(), "CSV file should not be empty");
        assertEquals("coordinates,sources,destinations,distances,profile", lines.get(0));
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> invalidConstructorParameters() {
        Map<String, Double> validMaxDistances = new HashMap<>();
        validMaxDistances.put("driving-car", 10000.0);

        return Stream.of(
                Arguments.of(0, TEST_EXTENT, DEFAULT_PROFILES, validMaxDistances, 0.0,
                        "Number of routes must be positive"),
                Arguments.of(1, null, DEFAULT_PROFILES, validMaxDistances, 0.0,
                        "Extent must contain 4 coordinates"),
                Arguments.of(1, new double[3], DEFAULT_PROFILES, validMaxDistances, 0.0,
                        "Extent must contain 4 coordinates"),
                Arguments.of(1, TEST_EXTENT, new String[0], validMaxDistances, 0.0,
                        "Profiles must not be empty"),
                Arguments.of(1, TEST_EXTENT, null, validMaxDistances, 0.0,
                        "Profiles must not be empty"),
                Arguments.of(1, TEST_EXTENT, DEFAULT_PROFILES, validMaxDistances, -1.0,
                        "Minimum distance must be non-negative"));
    }

    /**
     * A testable extension of CoordinateGeneratorRoute that allows injection of
     * dependencies
     */
    private static class TestCoordinateGeneratorMatrix extends CoordinateGeneratorMatrix {
        private CloseableHttpClient testClient;

        public TestCoordinateGeneratorMatrix(int numRoutes, double[] extent, String[] profiles,
                                             String baseUrl, Map<String, Double> maxDistanceByProfile, MatrixDimensions matrixDimensions) {
            super(numRoutes, extent, profiles, baseUrl, maxDistanceByProfile, matrixDimensions, 1, 1);
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
