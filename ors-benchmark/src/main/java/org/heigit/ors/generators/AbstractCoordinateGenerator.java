package org.heigit.ors.generators;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractCoordinateGenerator {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractCoordinateGenerator.class);
    protected static final int DEFAULT_MAX_ATTEMPTS = 100;
    protected static final double COORDINATE_PRECISION = 1e-6;
    protected static final String DEFAULT_BASE_URL = "http://localhost:8082/ors";

    protected final String baseUrl;
    protected final double[] extent;
    protected final String endpoint;
    protected final String profile;
    protected final String url;
    protected final Map<String, String> headers;
    protected final Random random;
    protected final ObjectMapper mapper;

    protected AbstractCoordinateGenerator(double[] extent, String profile, String baseUrl, String endpoint) {
        this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
        validateBaseInputParameters(extent, profile, endpoint);
        this.extent = extent;
        this.profile = profile;
        this.endpoint = endpoint;
        this.random = new SecureRandom();
        this.mapper = new ObjectMapper();
        
        String apiKey = getApiKey();
        this.url = buildUrl();
        this.headers = createHeaders(apiKey);
    }

    private String buildUrl() {
        return String.format("%s/v2/%s/%s", baseUrl, endpoint, profile);
    }

    private void validateBaseInputParameters(double[] extent, String profile, String endpoint) {
        if (extent == null || extent.length != 4)
            throw new IllegalArgumentException("Extent must contain 4 coordinates");
        if (profile == null || profile.isBlank())
            throw new IllegalArgumentException("Profile must not be empty");
        if (endpoint == null || endpoint.isBlank())
            throw new IllegalArgumentException("Endpoint must not be empty");
    }

    protected final String getApiKey() {
        if (!baseUrl.contains("openrouteservice.org")) {
            return "";
        }

        String apiKey = System.getenv("ORS_API_KEY");
        if (apiKey == null) {
            apiKey = System.getProperty("ORS_API_KEY");
        }
        if (apiKey == null) {
            throw new IllegalStateException("ORS_API_KEY environment variable is not set.");
        }
        return apiKey;
    }

    protected final Map<String, String> createHeaders(String apiKey) {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept", "application/json");
        requestHeaders.put("Content-Type", "application/json");
        requestHeaders.put("Authorization", apiKey);
        return requestHeaders;
    }

    protected List<double[]> randomCoordinatesInExtent(int count) {
        List<double[]> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double x = random.nextDouble() * (extent[2] - extent[0]) + extent[0];
            double y = random.nextDouble() * (extent[3] - extent[1]) + extent[1];
            points.add(new double[] { x, y });
        }
        return points;
    }

    protected CloseableHttpClient createHttpClient() {
        return HttpClientBuilder.create().build();
    }

    protected String processResponse(ClassicHttpResponse response) throws IOException {
        int status = response.getCode();
        if (status >= HttpStatus.SC_REDIRECTION) {
            throw new ClientProtocolException(new StatusLine(response).toString());
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        try {
            return EntityUtils.toString(entity);
        } catch (ParseException | IOException e) {
            throw new IOException("Failed to parse response entity", e);
        }
    }

    protected abstract void writeToCSV(String filePath) throws IOException;
    protected abstract void generate(int maxAttempts);
    protected abstract <T> List<T> getResult();
    protected abstract void initializeCollections();
    protected abstract void processNextBatch(CloseableHttpClient client) throws IOException;


    protected void generate() {
        generate(DEFAULT_MAX_ATTEMPTS);
    }

    protected static class CoordinateHash {
        private CoordinateHash() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }

        protected static int hash(double[] coord) {
            return Objects.hash(
                Math.round(coord[0] * 1e6) / 1e6,
                Math.round(coord[1] * 1e6) / 1e6
            );
        }

        protected static boolean equals(double[] coord1, double[] coord2) {
            return Math.abs(coord1[0] - coord2[0]) < COORDINATE_PRECISION &&
                   Math.abs(coord1[1] - coord2[1]) < COORDINATE_PRECISION;
        }
    }
}
