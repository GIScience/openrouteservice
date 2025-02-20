package org.heigit.ors.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.*;

public class CoordinateGeneratorSnapping {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorSnapping.class);
    private static final int DEFAULT_MAX_ATTEMPTS = 10;
    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final double COORDINATE_PRECISION = 1e-6;
    private static final String DEFAULT_BASE_URL = "http://localhost:8082/ors";
    private final String baseUrl;
    private final double[] extent;
    private final int numPoints;
    private final double radius;
    private final String profile;
    private final String url;
    private final Map<String, String> headers;
    private final List<double[]> result;
    private final Random random;
    private final ObjectMapper mapper;
    private final Set<Point> uniquePoints;

    protected static class Point {
        final double[] coordinates;

        Point(double[] coordinates) {
            this.coordinates = coordinates;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Point point = (Point) o;
            return Math.abs(coordinates[0] - point.coordinates[0]) < COORDINATE_PRECISION &&
                    Math.abs(coordinates[1] - point.coordinates[1]) < COORDINATE_PRECISION;
        }

        @Override
        public int hashCode() {
            // Use 6 decimal places precision for hash code
            return Objects.hash(
                    Math.round(coordinates[0] * 1e6) / 1e6,
                    Math.round(coordinates[1] * 1e6) / 1e6);
        }
    }

    protected CoordinateGeneratorSnapping(int numPoints, double[] extent, double radius, String profile,
            String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
        validateInputParameters(numPoints, extent, radius, profile);
        this.extent = extent;
        this.numPoints = numPoints;
        this.radius = radius;
        this.profile = profile;
        this.random = new SecureRandom();
        byte[] bytes = new byte[20];
        this.random.nextBytes(bytes);
        this.mapper = new ObjectMapper();
        this.result = new ArrayList<>();
        this.uniquePoints = new HashSet<>();

        String apiKey = getApiKey();
        this.url = String.format("%s/v2/snap/%s", this.baseUrl, this.profile);
        this.headers = new HashMap<>();
        headers.put("accept", "application/json");
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", apiKey);
    }

    private void validateInputParameters(int numPoints, double[] extent, double radius, String profile) {
        if (numPoints <= 0)
            throw new IllegalArgumentException("Number of points must be positive");
        if (extent == null || extent.length != 4)
            throw new IllegalArgumentException("Extent must contain 4 coordinates");
        if (radius <= 0)
            throw new IllegalArgumentException("Radius must be positive");
        if (profile == null || profile.isBlank())
            throw new IllegalArgumentException("Profile must not be empty");
    }

    private String getApiKey() {
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

    protected List<double[]> randomCoordinatesInExtent(int batchSize) {
        List<double[]> points = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
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
        } catch (NullPointerException e) {
            throw new IOException("Response entity is null", e);
        }
    }

    public void generatePoints() {
        generatePoints(DEFAULT_MAX_ATTEMPTS);
    }

    public void generatePoints(int maxAttempts) {
        initializeCollections();
        int attempts = 0;
        int lastSize = 0;

        try (CloseableHttpClient client = createHttpClient()) {
            while (uniquePoints.size() < numPoints && attempts < maxAttempts) {
                processNextBatch(client);

                if (uniquePoints.size() == lastSize) {
                    attempts++;
                    LOGGER.debug("No new points found in attempt {}/{}", attempts, maxAttempts);
                } else {
                    attempts = 0;
                    lastSize = uniquePoints.size();
                }
            }

            if (attempts >= maxAttempts) {
                LOGGER.warn("Stopped point generation after {} attempts without finding new points. Found {}/{} points",
                        maxAttempts, uniquePoints.size(), numPoints);
            }
        } catch (Exception e) {
            LOGGER.error("Error generating points", e);
        }
    }

    private void initializeCollections() {
        result.clear();
        uniquePoints.clear();
    }

    private void processNextBatch(CloseableHttpClient client) throws IOException {
        int remainingPoints = numPoints - uniquePoints.size();
        int currentBatchSize = Math.min(DEFAULT_BATCH_SIZE, remainingPoints);
        List<double[]> rawPoints = randomCoordinatesInExtent(currentBatchSize);

        String response = sendSnappingRequest(client, rawPoints);
        if (response != null) {
            processSnappingResponse(response);
        }
    }

    private String sendSnappingRequest(CloseableHttpClient client, List<double[]> points) throws IOException {
        HttpPost request = createSnappingRequest(points);
        return client.execute(request, this::processResponse);
    }

    private HttpPost createSnappingRequest(List<double[]> points) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("locations", points);
        payload.put("radius", radius);

        HttpPost request = new HttpPost(url);
        headers.forEach(request::addHeader);
        request.setEntity(new StringEntity(mapper.writeValueAsString(payload), ContentType.APPLICATION_JSON));
        return request;
    }

    private void processSnappingResponse(String response) throws IOException {
        Map<String, Object> responseMap = mapper.readValue(response,
                new TypeReference<Map<String, Object>>() {
                });

        Object locationsObj = responseMap.get("locations");
        List<Map<String, Object>> locations;

        if (locationsObj instanceof List<?> list) {
            locations = list.stream()
                    .filter(item -> item instanceof Map<?, ?>)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        } else {
            locations = Collections.emptyList();
            LOGGER.warn("Response contained no valid locations array");
        }

        for (Map<String, Object> location : locations) {
            processLocation(location);
            if (uniquePoints.size() >= numPoints)
                break;
        }
    }

    private void processLocation(Map<String, Object> location) {
        if (location == null)
            return;

        @SuppressWarnings("unchecked")
        List<Number> coords = (List<Number>) location.get("location");
        if (coords != null && coords.size() >= 2) {
            addPointIfUnique(coords);
        }
    }

    private void addPointIfUnique(List<Number> coords) {
        double[] point = new double[] {
                coords.get(0).doubleValue(),
                coords.get(1).doubleValue()
        };

        if (uniquePoints.add(new Point(point))) {
            result.add(point);
        } else {
            LOGGER.debug("Skipping duplicate point: [{}, {}]", point[0], point[1]);
        }
    }

    protected List<double[]> getResult() {
        return new ArrayList<>(result);
    }

    protected void writeToCSV(String filePath) throws IOException {
        File csvOutputFile = new File(filePath);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            pw.println("longitude,latitude");
            for (double[] point : result) {
                pw.printf("%f,%f%n", point[0], point[1]);
            }
        }
    }

    public static void main(String[] args) throws org.apache.commons.cli.ParseException {
        try {
            CoordinateGeneratorSnappingCLI cli = new CoordinateGeneratorSnappingCLI(args);

            if (cli.hasHelp()) {
                cli.printHelp();
                return;
            }

            LOGGER.info("Creating coordinate generator for snapping...");
            CoordinateGeneratorSnapping generator = cli.createGenerator();

            LOGGER.info("Generating and snapping {} points...", generator.numPoints);
            generator.generatePoints(DEFAULT_MAX_ATTEMPTS);

            LOGGER.info("Writing {} snapped points to {}", generator.getResult().size(), cli.getOutputFile());
            generator.writeToCSV(cli.getOutputFile());

            LOGGER.info("Successfully snapped {} coordinate{}",
                    generator.getResult().size(),
                    generator.getResult().size() != 1 ? "s" : "");
            LOGGER.info("Results written to: {}", cli.getOutputFile());

        } catch (NumberFormatException e) {
            LOGGER.error("Error parsing numeric arguments: {}", e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            LOGGER.error("Error writing to output file: {}", e.getMessage());
            System.exit(1);
        }
    }
}
