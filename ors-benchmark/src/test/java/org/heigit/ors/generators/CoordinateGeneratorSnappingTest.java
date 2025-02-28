package org.heigit.ors.generators;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoordinateGeneratorSnappingTest extends AbstractCoordinateGeneratorTest {
    private TestCoordinateGeneratorSnapping testGenerator;

    @Override
    @BeforeEach
    void setUpBase() {
        super.setUpBase();
        testGenerator = new TestCoordinateGeneratorSnapping(2, extent, 350, "driving-car", null);
    }

    @Override
    protected AbstractCoordinateGenerator createTestGenerator() {
        return new TestCoordinateGeneratorSnapping(2, extent, 350, "driving-car", null);
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
        testGenerator.generate();

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
        testGenerator.generate();

        String filename = "test_snap.csv";
        String filePath = tempDir.resolve(filename).toString();
        testGenerator.writeToCSV(filePath);

        String expected = "longitude,latitude\n8.666862,49.413181\n8.676105,49.418530\n";
        String result = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
        assertEquals(expected, result);
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
        testGenerator.generate();

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
