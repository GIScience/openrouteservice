package org.heigit.ors.coordinates_generator.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Abstract base class for coordinate generation implementations
 */
public abstract class AbstractCoordinateGenerator {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractCoordinateGenerator.class);
    protected static final int DEFAULT_MAX_ATTEMPTS = 10000;
    protected static final double COORDINATE_PRECISION = 1e-6;
    protected static final String DEFAULT_BASE_URL = "http://localhost:8082/ors";

    // Common configuration
    protected final String baseUrl;
    protected final double[] extent;
    protected final String[] profiles;
    protected final Random random;
    protected final ObjectMapper mapper;
    protected final String apiKey;

    /**
     * Creates a new coordinate generator
     * 
     * @param extent   Bounding box [minX, minY, maxX, maxY]
     * @param profiles List of routing profiles to use
     * @param baseUrl  API base URL
     * @param endpoint API endpoint name (for logging)
     */
    protected AbstractCoordinateGenerator(double[] extent, String[] profiles, String baseUrl, String endpoint) {
        this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
        validateBaseInputParameters(extent, profiles, endpoint);
        this.extent = extent;
        this.profiles = profiles.clone();
        this.random = new SecureRandom();
        this.mapper = new ObjectMapper();
        this.apiKey = getApiKey();
    }

    private void validateBaseInputParameters(double[] extent, String[] profiles, String endpoint) {
        if (extent == null || extent.length != 4)
            throw new IllegalArgumentException("Extent must contain 4 coordinates");
        if (profiles == null || profiles.length == 0)
            throw new IllegalArgumentException("Profiles must not be empty");
        if (endpoint == null || endpoint.isBlank())
            throw new IllegalArgumentException("Endpoint must not be empty");
    }

    /**
     * Gets the API key from environment or system properties
     */
    private String getApiKey() {
        if (!baseUrl.contains("openrouteservice.org")) {
            return "";
        }

        String orsApiAccessKey = System.getenv("ORS_API_KEY");
        if (orsApiAccessKey == null) {
            orsApiAccessKey = System.getProperty("ORS_API_KEY");
        }
        if (orsApiAccessKey == null) {
            throw new IllegalStateException("ORS_API_KEY environment variable is not set.");
        }
        return orsApiAccessKey;
    }

    /**
     * Creates HTTP request headers
     */
    public final Map<String, String> createHeaders() {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept", "application/json");
        requestHeaders.put("Content-Type", "application/json");
        if (!apiKey.isEmpty()) {
            requestHeaders.put("Authorization", apiKey);
        }
        return requestHeaders;
    }

    /**
     * Creates a new HTTP client
     */
    protected CloseableHttpClient createHttpClient() {
        return HttpClientBuilder.create().build();
    }

    /**
     * Processes an HTTP response
     */
    protected String processResponse(ClassicHttpResponse response) throws IOException {
        int status = response.getCode();
        if (status >= HttpStatus.SC_REDIRECTION) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Received error response: {}", new StatusLine(response));
            }
            return null;
        }

        HttpEntity entity = response.getEntity();
        if (entity == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Received empty response");
            }
            return null;
        }

        try {
            return EntityUtils.toString(entity);
        } catch (ParseException | IOException e) {
            throw new IOException("Failed to parse response entity", e);
        }
    }

    /**
     * Writes generation results to a CSV file
     */
    public abstract void writeToCSV(String filePath) throws IOException;

    /**
     * Main generation method with specified maximum attempts
     */
    protected abstract void generate(int maxAttempts);

    /**
     * Gets the generated results
     */
    public abstract <T> List<T> getResult();

    /**
     * Initializes or clears collections before generation
     */
    protected abstract void initializeCollections();

    /**
     * Main generation method with default maximum attempts
     */
    public void generate() {
        generate(DEFAULT_MAX_ATTEMPTS);
    }
}
