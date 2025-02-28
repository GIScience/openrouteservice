package org.heigit.ors.generators;

import java.io.IOException;
import java.util.List;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

abstract class AbstractCoordinateGeneratorTest {
    protected double[] extent;

    @Mock
    protected CloseableHttpClient closeableHttpClient;

    @Captor
    protected ArgumentCaptor<HttpClientResponseHandler<String>> handlerCaptor;

    @BeforeEach
    void setUpBase() {
        MockitoAnnotations.openMocks(this);
        extent = new double[] { 8.6286, 49.3590, 8.7957, 49.4715 };
    }

    protected abstract AbstractCoordinateGenerator createTestGenerator();

    @Test
    void testProcessResponseSuccess() throws IOException {
        AbstractCoordinateGenerator generator = createTestGenerator();
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        HttpEntity entity = new StringEntity("test content");
        when(response.getCode()).thenReturn(HttpStatus.SC_OK);
        when(response.getEntity()).thenReturn(entity);

        String result = generator.processResponse(response);
        assertEquals("test content", result);
    }

    @Test
    void testProcessResponseNonOkStatus() {
        AbstractCoordinateGenerator generator = createTestGenerator();
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        when(response.getCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);

        Exception exception = assertThrows(IOException.class, () -> generator.processResponse(response));
        assertTrue(exception.getMessage().contains("400"));
    }

    @Test
    void testProcessResponseNullEntity() {
        AbstractCoordinateGenerator generator = createTestGenerator();
        ClassicHttpResponse response = mock(ClassicHttpResponse.class);
        when(response.getCode()).thenReturn(HttpStatus.SC_OK);
        when(response.getEntity()).thenReturn(null);

        assertDoesNotThrow(() -> {
            String result = generator.processResponse(response);
            assertNull(result);
        });
    }

    @Test
    void testRandomCoordinatesInExtent() {
        AbstractCoordinateGenerator generator = createTestGenerator();
        int count = 10;
        List<double[]> coordinates = generator.randomCoordinatesInExtent(count);

        assertEquals(count, coordinates.size());
        for (double[] coord : coordinates) {
            assertEquals(2, coord.length);
            assertTrue(coord[0] >= extent[0] && coord[0] <= extent[2]);
            assertTrue(coord[1] >= extent[1] && coord[1] <= extent[3]);
        }
    }
}
