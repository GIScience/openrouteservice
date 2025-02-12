package org.heigit.ors.benchmark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.StatusLine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CoordinateGeneratorTest {
    private CoordinateGenerator generator;
    private double[] extent;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        extent = new double[] { 8.6286, 49.3590, 8.7957, 49.4715 };
        generator = new TestCoordinateGenerator(
                100, extent, 1, 100, 100, 350, "driving-car", null);
    }

    // Set the base url to openrouteservice.org and catch the runtime exception
    @Test
    void testCoordinateGeneratorWithMissingApiKey() {
        assertThrows(RuntimeException.class, () -> {
            new CoordinateGenerator(
                    100, extent, 1, 100, 100, 350, "driving-car", "https://openrouteservice.org");
        });
    }

    static Stream<Arguments> pointSizeProvider() {
        return Stream.of(
                Arguments.of(1),
                Arguments.of(5),
                Arguments.of(10),
                Arguments.of(20),
                Arguments.of(50),
                Arguments.of(1000));
    }

    @ParameterizedTest
    @MethodSource("pointSizeProvider")
    void testRandomCoordinatesInExtentWithDifferentSizes(int numPoints) {
        List<double[]> points = generator.randomCoordinatesInExtent(numPoints);

        // Test correct number of points
        assertEquals(numPoints, points.size());

        // Test each point is within extent
        for (double[] point : points) {
            assertEquals(2, point.length);
            assertTrue(point[0] >= extent[0] && point[0] <= extent[2],
                    "X coordinate should be within extent");
            assertTrue(point[1] >= extent[1] && point[1] <= extent[3],
                    "Y coordinate should be within extent");
        }
    }

    @Test
    void testRandomCoordinatesInExtentZeroPoints() {
        List<double[]> points = generator.randomCoordinatesInExtent(0);
        assertTrue(points.isEmpty());
    }

    @Test
    void testRandomCoordinatesInExtentNegativePoints() {
        List<double[]> points = generator.randomCoordinatesInExtent(-1);
        assertTrue(points.isEmpty());
    }

    @Test
    void testApplyMatrixWithMockedResponse() throws Exception {

        HttpPost httpPost = mock(HttpPost.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
        // Prepare test data
        /**
         */
        String mockJsonResponse = """
                {
                  "distances": [[0], [99]],
                  "destinations": [
                    { "location": [8.681009, 49.409929], "snapped_distance": 7.93 }
                  ],
                  "sources": [
                    { "location": [8.681009, 49.409929], "snapped_distance": 7.93 },
                    { "location": [8.687026, 49.420002], "snapped_distance": 1.86 }
                  ]
                }
                """;

        // Create a proper StringEntity
        StringEntity entity = new StringEntity(mockJsonResponse, StandardCharsets.UTF_8);

        // Define the response content
        when(statusLine.getStatusCode()).thenReturn(200);
        when(closeableHttpResponse.getEntity()).thenReturn(entity);
        when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);

        // Create test generator with mocked client
        TestCoordinateGenerator testGenerator = new TestCoordinateGenerator(
                100, extent, 1, 100, 100, 350, "driving-car", null);
        testGenerator.setHttpClient(closeableHttpClient);

        // Test with sample points
        List<double[]> testPoints = List.of(
                new double[] { 8.681009, 49.409929 });
        Map<String, List<double[]>> result = testGenerator.applyMatrix(testPoints);

        // Verify
        verify(closeableHttpClient, times(1)).execute(any());
        assertNotNull(result);
        assertEquals(8.681009, result.get("from_points").get(0)[0], 0.0001);
        assertEquals(49.409929, result.get("from_points").get(0)[1], 0.0001);
        assertEquals(8.687026, result.get("to_points").get(0)[0], 0.0001);
        assertEquals(49.420002, result.get("to_points").get(0)[1], 0.0001);

    }

    @Test
    void testApplyMatrixWithEmptyResponse() throws Exception {
        // Mock empty response
        String mockJsonResponse = "{\"distances\":[],\"destinations\":[],\"sources\":[]}";
        StringEntity entity = new StringEntity(mockJsonResponse, StandardCharsets.UTF_8);

        CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
        when(closeableHttpResponse.getEntity()).thenReturn(entity);
        when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);

        TestCoordinateGenerator testGenerator = new TestCoordinateGenerator(
                100, extent, 1, 100, 100, 350, "driving-car", null);
        testGenerator.setHttpClient(closeableHttpClient);

        Map<String, List<double[]>> result = testGenerator.applyMatrix(List.of(new double[] { 8.681, 49.41 }));

        assertTrue(result.get("from_points").isEmpty());
        assertTrue(result.get("to_points").isEmpty());
    }

    @Test
    void testApplyMatrixWithDistanceFiltering() throws Exception {
        // Mock response with distances outside and inside the valid range
        String mockJsonResponse = """
                {
                  "distances": [[0], [50], [150]],
                  "destinations": [{ "location": [8.681, 49.41] }],
                  "sources": [
                    { "location": [8.681, 49.41] },
                    { "location": [8.682, 49.42] },
                    { "location": [8.683, 49.43] }
                  ]
                }

                """;

        StringEntity entity = new StringEntity(mockJsonResponse, StandardCharsets.UTF_8);

        CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
        when(closeableHttpResponse.getEntity()).thenReturn(entity);
        when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);

        TestCoordinateGenerator testGenerator = new TestCoordinateGenerator(
                100, extent, 40, 125, 100, 350, "driving-car", null);
        testGenerator.setHttpClient(closeableHttpClient);

        Map<String, List<double[]>> result = testGenerator.applyMatrix(
                List.of(new double[] { 8.681, 49.41 }));

        assertEquals(1, result.get("from_points").size());
        assertEquals(1, result.get("to_points").size());
        assertEquals(8.682, result.get("to_points").get(0)[0], 0.0001);
        assertEquals(49.42, result.get("to_points").get(0)[1], 0.0001);
    }

    @Test
    void testApplyMatrixWithMalformedResponse() {
        String mockJsonResponse = "{ invalid json }";
        StringEntity entity = new StringEntity(mockJsonResponse, StandardCharsets.UTF_8);

        CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
        when(closeableHttpResponse.getEntity()).thenReturn(entity);
        try {
            when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);

            TestCoordinateGenerator testGenerator = new TestCoordinateGenerator(
                    100, extent, 1, 100, 100, 350, "driving-car", null);
            testGenerator.setHttpClient(closeableHttpClient);

            assertThrows(Exception.class, () -> testGenerator.applyMatrix(List.of(new double[] { 8.681, 49.41 })));
        } catch (Exception e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    @Test
    void testApplyMatrixWithHttpError() throws Exception {
        CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);
        when(closeableHttpClient.execute(any())).thenThrow(new IOException("Network error"));

        TestCoordinateGenerator testGenerator = new TestCoordinateGenerator(
                100, extent, 1, 100, 100, 350, "driving-car", null);
        testGenerator.setHttpClient(closeableHttpClient);

        assertThrows(IOException.class, () -> testGenerator.applyMatrix(List.of(new double[] { 8.681, 49.41 })));
    }

    // Test helper class
    private class TestCoordinateGenerator extends CoordinateGenerator {
        private CloseableHttpClient testClient;

        public TestCoordinateGenerator(int numPoints, double[] extent, double minDistance,
                double maxDistance, int maxAttempts, double radius,
                String profile, String baseUrl) {
            super(numPoints, extent, minDistance, maxDistance, maxAttempts, radius, profile, baseUrl);
        }

        void setHttpClient(CloseableHttpClient client) {
            this.testClient = client;
        }

        @Override
        protected CloseableHttpClient createHttpClient() {
            // if testClient is null fail
            if (testClient == null) {
                fail("Test client not set properly");
            }
            return testClient;
        }
    }
}
