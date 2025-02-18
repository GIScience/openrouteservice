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
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CoordinateGeneratorSnappingTest {
    private double[] extent;
    private TestCoordinateGeneratorSnapping testGenerator;

    @Mock
    CloseableHttpClient closeableHttpClient;

    @Captor
    private ArgumentCaptor<HttpClientResponseHandler<String>> handlerCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        extent = new double[] { 8.6286, 49.3590, 8.7957, 49.4715 };
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
    void testProcessResponseParseError() throws IOException {
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

    private class TestCoordinateGeneratorSnapping extends CoordinateGeneratorSnapping {
        private CloseableHttpClient testClient;

        public TestCoordinateGeneratorSnapping(int numPoints, double[] extent, double radius, String profile, String baseUrl) {
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
