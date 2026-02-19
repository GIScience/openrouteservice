package org.heigit.ors.coordinates_generator.generators;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CoordinateGeneratorSnappingTest {

    @Mock
    protected CloseableHttpClient closeableHttpClient;

    @Captor
    protected ArgumentCaptor<HttpClientResponseHandler<String>> handlerCaptor;

    private TestCoordinateGeneratorSnapping testGenerator;
    private double[] extent;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        extent = new double[] { 7.6286, 50.3590, 7.7957, 50.4715 };
        testGenerator = new TestCoordinateGeneratorSnapping(2, extent, 350,
                new String[] { "driving-car", "cycling-regular" }, null);
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
        testGenerator.generate();

        List<Object[]> result = testGenerator.getResult();
        assertEquals(4, result.size()); // 2 points * 2 profiles

        // Check format of results (lon, lat, profile)
        for (Object[] point : result) {
            assertEquals(3, point.length);
            assertTrue(point[2] instanceof String);
            String profile = (String) point[2];
            assertTrue(profile.equals("driving-car") || profile.equals("cycling-regular"));
        }
    }

    @Test
    void testGeneratePointsWithInvalidResponse() throws Exception {
        String invalidResponse = "{ invalid json }";
        when(closeableHttpClient.execute(any(HttpPost.class), handlerCaptor.capture())).thenReturn(invalidResponse);

        testGenerator.setHttpClient(closeableHttpClient);
        testGenerator.generate();

        List<Object[]> result = testGenerator.getResult();
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
        testGenerator.generate();

        String filename = "test_snap.csv";
        String filePath = tempDir.resolve(filename).toString();
        testGenerator.writeToCSV(filePath);

        String expected = """
                longitude,latitude,profile
                8.666862,49.413181,driving-car
                8.676105,49.418530,driving-car
                8.666862,49.413181,cycling-regular
                8.676105,49.418530,cycling-regular
                """;
        String result = new String(Files.readAllBytes(Paths.get(filePath)));
        assertEquals(expected, result);
    }

    @Test
    void testGeneratePointsWithDuplicates() throws Exception {
        String mockJsonResponse = """
                {
                    "locations": [
                        {"location": [8.666862, 49.413181], "snapped_distance": 200.94},
                        {"location": [8.666862, 49.413181], "snapped_distance": 200.94},
                        {"location": [8.676105, 49.41853], "name": "Berliner Straße", "snapped_distance": 19.11}
                    ]
                }
                """;

        when(closeableHttpClient.execute(any(HttpPost.class), handlerCaptor.capture())).thenReturn(mockJsonResponse);
        testGenerator = new TestCoordinateGeneratorSnapping(2, extent, 350,
                new String[] { "driving-car", "cycling-regular" }, null);

        testGenerator.setHttpClient(closeableHttpClient);
        testGenerator.generate();

        List<Object[]> result = testGenerator.getResult();

        // Should have 2 points per profile = 4 total
        assertEquals(4, result.size());

        // Verify unique points per profile
        Map<String, Set<String>> pointsByProfile = new HashMap<>();
        for (Object[] point : result) {
            String profile = (String) point[2];
            String coordKey = String.format("%.6f,%.6f", point[0], point[1]);
            pointsByProfile.computeIfAbsent(profile, k -> new HashSet<>()).add(coordKey);
        }

        assertEquals(2, pointsByProfile.get("driving-car").size());
        assertEquals(2, pointsByProfile.get("cycling-regular").size());
    }

    private class TestCoordinateGeneratorSnapping extends CoordinateGeneratorSnapping {
        private CloseableHttpClient testClient;

        public TestCoordinateGeneratorSnapping(int numPoints, double[] extent, double radius, String[] profiles,
                String baseUrl) {
            super(numPoints, extent, radius, profiles, baseUrl, 1);
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
