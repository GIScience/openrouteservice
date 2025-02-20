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

    protected static class Route {
        final double[] start;
        final double[] end;
        final double duration;

        Route(double[] start, double[] end, double duration) {
            this.start = start;
            this.end = end;
            this.duration = duration;
        }
    }

    public static <t extends Number> double rand(t x) {
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RoutePair that = (RoutePair) o;
            return coordinatesEqual(start, that.start) && coordinatesEqual(end, that.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                Math.round(start[0] * 1e6) / 1e6,
                Math.round(start[1] * 1e6) / 1e6,
                Math.round(end[0] * 1e6) / 1e6,
                Math.round(end[1] * 1e6) / 1e6
            );
        }

        private boolean coordinatesEqual(double[] coord1, double[] coord2) {
            return Math.abs(coord1[0] - coord2[0]) < COORDINATE_PRECISION &&
                   Math.abs(coord1[1] - coord2[1]) < COORDINATE_PRECISION;
        }
    }

    protected CoordinateGeneratorRoute(int numRoutes, double[] extent, String profile, String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
        validateInputParameters(numRoutes, extent, profile);
        this.extent = extent;
        this.numRoutes = numRoutes;
        this.profile = profile;
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

    private void validateInputParameters(int numRoutes, double[] extent, String profile) {
        if (numRoutes <= 0)
            throw new IllegalArgumentException("Number of routes must be positive");
        if (extent == null || extent.length != 4)
            throw new IllegalArgumentException("Extent must contain 4 coordinates");
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

        try (CloseableHttpClient client = createHttpClient()) {
            while (uniqueRoutes.size() < numRoutes && attempts < maxAttempts) {
                processNextBatch(client);

                if (uniqueRoutes.size() == lastSize) {
                    attempts++;
                    LOGGER.debug("No new routes found in attempt {}/{}", attempts, maxAttempts);
                } else {
                    attempts = 0;
                    lastSize = uniqueRoutes.size();
                }
            }

            if (attempts >= maxAttempts) {
                LOGGER.warn("Stopped route generation after {} attempts. Found {}/{} routes",
                        maxAttempts, uniqueRoutes.size(), numRoutes);
            }
        } catch (Exception e) {
            LOGGER.error("Error generating routes", e);
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
            processMatrixResponse(response, coordinates);
        }
    }

    private String sendMatrixRequest(CloseableHttpClient client, List<double[]> coordinates) throws IOException {
        HttpPost request = createMatrixRequest(coordinates);
        return client.execute(request, this::processResponse);
    }

    private HttpPost createMatrixRequest(List<double[]> coordinates) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("locations", coordinates);

        HttpPost request = new HttpPost(url);
        headers.forEach(request::addHeader);
        request.setEntity(new StringEntity(mapper.writeValueAsString(payload), ContentType.APPLICATION_JSON));
        return request;
    }

    private void processMatrixResponse(String response, List<double[]> inputCoordinates) throws IOException {
        Map<String, Object> responseMap = mapper.readValue(response, new TypeReference<Map<String, Object>>() {});
        
        List<List<Double>> durations = extractDurations(responseMap);
        List<Map<String, Object>> locations = extractLocations(responseMap, "destinations");
        
        if (durations == null || locations == null || locations.isEmpty()) {
            return;
        }

        processMatrixResults(durations, locations);
    }

    @SuppressWarnings("unchecked")
    private List<List<Double>> extractDurations(Map<String, Object> responseMap) {
        Object durationsObj = responseMap.get("durations");
        if (durationsObj instanceof List<?>) {
            return (List<List<Double>>) durationsObj;
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
    

    private void processMatrixResults(List<List<Double>> durations, List<Map<String, Object>> locations) {
        int size = locations.size();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    Double duration = durations.get(i).get(j);
                    if (duration > 0) {
                        addRouteIfUnique(locations.get(i), locations.get(j), duration);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addRouteIfUnique(Map<String, Object> start, Map<String, Object> end, double duration) {
        List<Number> startCoord = (List<Number>) start.get("location");
        List<Number> endCoord = (List<Number>) end.get("location");

        if (startCoord != null && endCoord != null && startCoord.size() >= 2 && endCoord.size() >= 2) {
            double[] startPoint = new double[] { startCoord.get(0).doubleValue(), startCoord.get(1).doubleValue() };
            double[] endPoint = new double[] { endCoord.get(0).doubleValue(), endCoord.get(1).doubleValue() };

            RoutePair routePair = new RoutePair(startPoint, endPoint);
            if (uniqueRoutes.add(routePair)) {
                routes.add(new Route(startPoint, endPoint, duration));
            }
        }
    }

    protected List<Route> getResult() {
        return new ArrayList<>(routes);
    }

    protected void writeToCSV(String filePath) throws IOException {
        File csvOutputFile = new File(filePath);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            pw.println("start_longitude,start_latitude,end_longitude,end_latitude,duration");
            for (Route route : routes) {
                pw.printf("%f,%f,%f,%f,%f%n", 
                    route.start[0], route.start[1],
                    route.end[0], route.end[1],
                    route.duration);
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

            LOGGER.info("Writing {} routes to {}", generator.getResult().size(), cli.getOutputFile());
            generator.writeToCSV(cli.getOutputFile());

            LOGGER.info("Successfully generated {} route{}",
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
