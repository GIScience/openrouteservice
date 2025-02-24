package org.heigit.ors.generators;

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
import org.heigit.ors.util.ProgressBarLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import me.tongfei.progressbar.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.*;

public class CoordinateGeneratorRoute {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorRoute.class);
    private static final int DEFAULT_MAX_ATTEMPTS = 10;
    private static final int DEFAULT_MATRIX_SIZE = 4;
    private static final double COORDINATE_PRECISION = 1e-6;
    private static final String DEFAULT_BASE_URL = "http://localhost:8082/ors";
    private final String baseUrl;
    private final double[] extent;
    private final int numRoutes;
    private final String profile;
    private final String url;
    private final Map<String, String> headers;
    private final List<Route> routes;
    private final Random random;
    private final ObjectMapper mapper;
    private final Set<RoutePair> uniqueRoutes;
    private final double minDistance; // Add this field

    protected static class Route {
        final double[] start;
        final double[] end;
        final double distance; // Changed from duration to distance

        Route(double[] start, double[] end, double distance) { // Changed parameter name
            this.start = start;
            this.end = end;
            this.distance = distance; // Changed field name
        }
    }

    public static <T extends Number> double rand(T x) {
        if (x == null) {
            throw new IllegalArgumentException("Input number cannot be null");
        }
        return (x instanceof Double || x instanceof Float) ? x.doubleValue() : x.longValue();
    }

    protected static class RoutePair {
        final double[] start;
        final double[] end;

        RoutePair(double[] start, double[] end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            RoutePair that = (RoutePair) o;
            return coordinatesEqual(start, that.start) && coordinatesEqual(end, that.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    Math.round(start[0] * 1e6) / 1e6,
                    Math.round(start[1] * 1e6) / 1e6,
                            Math.round(end[0] * 1e6) / 1e6,
                    Math.round(end[1] * 1e6) / 1e6);
        }

        private boolean coordinatesEqual(double[] coord1, double[] coord2) {
            return Math.abs(coord1[0] - coord2[0]) < COORDINATE_PRECISION &&
                    Math.abs(coord1[1] - coord2[1]) < COORDINATE_PRECISION;
        }
    }

    protected CoordinateGeneratorRoute(int numRoutes, double[] extent, String profile, String baseUrl,
            double minDistance) {
        this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
        validateInputParameters(numRoutes, extent, profile, minDistance);
        this.extent = extent;
        this.numRoutes = numRoutes;
        this.profile = profile;
        this.minDistance = minDistance;
        this.random = new SecureRandom();
        this.mapper = new ObjectMapper();
        this.routes = new ArrayList<>();
        this.uniqueRoutes = new HashSet<>();

        String apiKey = getApiKey();
        this.url = String.format("%s/v2/matrix/%s", this.baseUrl, this.profile);
        this.headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", apiKey);
    }

    private void validateInputParameters(int numRoutes, double[] extent, String profile, double minDistance) {
        if (numRoutes <= 0)
            throw new IllegalArgumentException("Number of routes must be positive");
        if (extent == null || extent.length != 4)
            throw new IllegalArgumentException("Extent must contain 4 coordinates");
        if (profile == null || profile.isBlank())
            throw new IllegalArgumentException("Profile must not be empty");
        if (minDistance < 0)
            throw new IllegalArgumentException("Minimum distance must be non-negative");
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

    public void generateRoutes() {
        generateRoutes(DEFAULT_MAX_ATTEMPTS);
    }

    public void generateRoutes(int maxAttempts) {
        initializeCollections();
        int attempts = 0;
        int lastSize = 0;

        // Create progress bar builder
        ProgressBarBuilder pbb = new ProgressBarBuilder()
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .setUpdateIntervalMillis(5000)
                .setTaskName("Generating routes")
                .setInitialMax(numRoutes)
                .setConsumer(new DelegatingProgressBarConsumer(ProgressBarLogger.getLogger()::info));
        ProgressBar pb = pbb.build();
        // Use try-with-resources for both client and progress bar to ensure proper
        // closing
        try (CloseableHttpClient client = createHttpClient()) {

            pb.setExtraMessage("Starting...");

            while (uniqueRoutes.size() < numRoutes && attempts < maxAttempts) {
                processNextBatch(client);

                if (uniqueRoutes.size() == lastSize) {
                    attempts++;
                    pb.setExtraMessage(String.format("Attempt %d/%d - No new routes", attempts, maxAttempts));
                    LOGGER.debug("No new routes found in attempt {}/{}", attempts, maxAttempts);
                } else {
                    pb.stepTo(Math.min(uniqueRoutes.size(), numRoutes));
                    pb.setExtraMessage(
                            String.format("Found %d unique routes ", Math.min(uniqueRoutes.size(), numRoutes)));
                    attempts = 0;
                    lastSize = uniqueRoutes.size();
                }
            }

            pb.stepTo(Math.min(uniqueRoutes.size(), numRoutes));
            if (attempts >= maxAttempts) {
                pb.setExtraMessage(String.format("Stopped after %d attempts - Found at least %d/%d routes",
                        maxAttempts, uniqueRoutes.size(), numRoutes));
                LOGGER.warn("Stopped route generation after {} attempts. Found {}/{} routes",
                        maxAttempts, uniqueRoutes.size(), numRoutes);
            }
        } catch (Exception e) {
            LOGGER.error("Error generating routes", e);
        } finally {
            pb.close();
            LOGGER.info("\n");
            LOGGER.info("Generated {} unique routes", Math.min(uniqueRoutes.size(), numRoutes));
        }
    }

    private void initializeCollections() {
        routes.clear();
        uniqueRoutes.clear();
    }

    private void processNextBatch(CloseableHttpClient client) throws IOException {
        List<double[]> coordinates = randomCoordinatesInExtent(DEFAULT_MATRIX_SIZE);
        String response = sendMatrixRequest(client, coordinates);
        if (response != null) {
            processMatrixResponse(response);
        }
    }

    private String sendMatrixRequest(CloseableHttpClient client, List<double[]> coordinates) throws IOException {
        HttpPost request = createMatrixRequest(coordinates);
        return client.execute(request, this::processResponse);
    }

    private HttpPost createMatrixRequest(List<double[]> coordinates) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("locations", coordinates);
        payload.put("metrics", new String[] { "distance" }); // Add metrics parameter to request distance

        HttpPost request = new HttpPost(url);
        headers.forEach(request::addHeader);
        request.setEntity(new StringEntity(mapper.writeValueAsString(payload), ContentType.APPLICATION_JSON));
        return request;
    }

    private void processMatrixResponse(String response) throws JsonProcessingException {
        Map<String, Object> responseMap = mapper.readValue(response, new TypeReference<Map<String, Object>>() {
        });

        List<List<Double>> distances = extractDistances(responseMap); // Changed from durations to distances
        List<Map<String, Object>> locations = extractLocations(responseMap, "destinations");

        if (distances == null || locations == null || locations.isEmpty()) {
            return;
        }

        processMatrixResults(distances, locations); // Updated parameter name
    }

    @SuppressWarnings("unchecked")
    private List<List<Double>> extractDistances(Map<String, Object> responseMap) { // Changed method name
        Object distancesObj = responseMap.get("distances"); // Changed from durations to distances
        if (distancesObj instanceof List<?>) {
            return (List<List<Double>>) distancesObj;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractLocations(Map<String, Object> responseMap, String key) {
        Object locationsObj = responseMap.get(key);
        if (locationsObj instanceof List<?>) {
            return (List<Map<String, Object>>) locationsObj;
        }
        return Collections.emptyList();
    }

    private void processMatrixResults(List<List<Double>> distances, List<Map<String, Object>> locations) { // Updated
                                                                                                           // parameter
                                                                                                           // name
        int size = locations.size();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    Double distance = distances.get(i).get(j); // Changed from duration to distance
                    if (distance > 0) {
                        addRouteIfUnique(locations.get(i), locations.get(j), distance); // Updated parameter name
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addRouteIfUnique(Map<String, Object> start, Map<String, Object> end, double distance) { // Updated
                                                                                                         // parameter
                                                                                                         // name
        List<Number> startCoord = (List<Number>) start.get("location");
        List<Number> endCoord = (List<Number>) end.get("location");

        if (startCoord != null && endCoord != null && startCoord.size() >= 2 && endCoord.size() >= 2) {
            double[] startPoint = new double[] { startCoord.get(0).doubleValue(), startCoord.get(1).doubleValue() };
            double[] endPoint = new double[] { endCoord.get(0).doubleValue(), endCoord.get(1).doubleValue() };

            // Check if distance is greater than minimum (now in meters instead of seconds)
            if (distance < minDistance) {
                LOGGER.debug("Skipping route with distance {} < minimum {} meters", distance, minDistance);
                return;
            }

            RoutePair routePair = new RoutePair(startPoint, endPoint);
            if (uniqueRoutes.add(routePair)) {
                routes.add(new Route(startPoint, endPoint, distance));
            }
        }
    }

    protected List<Route> getResult() {
        return routes.subList(0, Math.min(numRoutes, routes.size()));
    }

    protected void writeToCSV(String filePath) throws FileNotFoundException {
        // Get cleaned up results
        List<Route> cleanedRoutes = getResult();

        File csvOutputFile = new File(filePath);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            pw.println("start_longitude,start_latitude,end_longitude,end_latitude,distance");
            for (Route route : cleanedRoutes) { // Use cleaned routes
                pw.printf("%f,%f,%f,%f,%f%n",
                                route.start[0], route.start[1],
                        route.end[0], route.end[1],
                        route.distance);
            }
        }
    }

    public static void main(String[] args) throws org.apache.commons.cli.ParseException {
        try {
            CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);

            if (cli.hasHelp()) {
                cli.printHelp();
                return;
            }

            LOGGER.info("Creating coordinate generator for routes...");
            CoordinateGeneratorRoute generator = cli.createGenerator();

            LOGGER.info("Generating {} routes...", generator.numRoutes);
            generator.generateRoutes(DEFAULT_MAX_ATTEMPTS);

            List<Route> result = generator.getResult(); // Get cleaned results
            LOGGER.info("Writing {} routes to {}", result.size(), cli.getOutputFile());
            generator.writeToCSV(cli.getOutputFile());

            LOGGER.info("Successfully generated {} route{}",
                    result.size(),
                    result.size() != 1 ? "s" : "");
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
