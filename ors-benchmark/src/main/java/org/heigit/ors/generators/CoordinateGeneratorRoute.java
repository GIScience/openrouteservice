package org.heigit.ors.generators;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.ClientProtocolException;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import me.tongfei.progressbar.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CoordinateGeneratorRoute extends AbstractCoordinateGenerator {
    private static final int DEFAULT_MATRIX_SIZE = 4;

    private final int numRoutes;
    private final double minDistance;
    private final Map<String, List<Route>> resultsByProfile;
    private final Map<String, Set<RoutePair>> uniqueRoutesByProfile;

    protected static class Route {
        final double[] start;
        final double[] end;
        final double distance;
        final String profile;

        Route(double[] start, double[] end, double distance, String profile) {
            this.start = start;
            this.end = end;
            this.distance = distance;
            this.profile = profile;
        }
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

    protected CoordinateGeneratorRoute(int numRoutes, double[] extent, String[] profiles, String baseUrl,
            double minDistance) {
        super(extent, profiles, baseUrl, "matrix"); // Use first profile as default
        if (numRoutes <= 0)
            throw new IllegalArgumentException("Number of routes must be positive");
        if (minDistance < 0)
            throw new IllegalArgumentException("Minimum distance must be non-negative");

        this.numRoutes = numRoutes;
        this.minDistance = minDistance;
        this.resultsByProfile = new HashMap<>();
        this.uniqueRoutesByProfile = new HashMap<>();

        for (String userProfile : profiles) {
            resultsByProfile.put(userProfile, new ArrayList<>());
            uniqueRoutesByProfile.put(userProfile, new HashSet<>());
        }
    }

    @Override
    protected List<double[]> randomCoordinatesInExtent(int count) {
        LOGGER.debug("Generating {} random coordinates within extent [{}, {}, {}, {}]",
                count, extent[0], extent[1], extent[2], extent[3]);
        List<double[]> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double x = random.nextDouble() * (extent[2] - extent[0]) + extent[0];
            double y = random.nextDouble() * (extent[3] - extent[1]) + extent[1];
            points.add(new double[] { x, y });
        }
        return points;
    }

    @Override
    protected String processResponse(ClassicHttpResponse response) throws IOException {
        int status = response.getCode();
        LOGGER.debug("Processing response with status code: {}", status);
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
        generate(DEFAULT_MAX_ATTEMPTS);
    }

    @Override
    public void generate(int maxAttempts) {
        LOGGER.debug("Starting route generation with max attempts: {}", maxAttempts);
        initializeCollections();
        int attempts = 0;
        Map<String, Integer> lastSizes = initializeLastSizes();

        ProgressBarBuilder pbb = new ProgressBarBuilder()
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .setUpdateIntervalMillis(5000)
                .setTaskName("Generating routes")
                .setInitialMax((long) numRoutes * profiles.length)
                .setConsumer(new DelegatingProgressBarConsumer(ProgressBarLogger.getLogger()::info));

        try (CloseableHttpClient client = createHttpClient();
                ProgressBar pb = pbb.build()) {

            pb.setExtraMessage("Starting...");

            while (!isGenerationComplete() && attempts < maxAttempts) {
                LOGGER.debug("Generation iteration - Attempt {}/{}", attempts + 1, maxAttempts);
                boolean newRoutesFound = processProfiles(client, lastSizes);

                if (!newRoutesFound) {
                    attempts++;
                    pb.setExtraMessage(String.format("Attempt %d/%d - No new routes", attempts, maxAttempts));
                } else {
                    updateProgress(pb);
                    attempts = 0;
                }
            }

            pb.stepTo(getTotalRoutes());
            if (attempts >= maxAttempts && LOGGER.isWarnEnabled()) {
                LOGGER.warn("Stopped route generation after {} attempts. Routes per profile: {}",
                        maxAttempts, formatProgressMessage());
            }

        } catch (Exception e) {
            LOGGER.error("Error generating routes: ", e);
        }
    }

    private Map<String, Integer> initializeLastSizes() {
        Map<String, Integer> lastSizes = new HashMap<>();
        for (String userProfile : profiles) {
            lastSizes.put(userProfile, 0);
        }
        return lastSizes;
    }

    private boolean processProfiles(CloseableHttpClient client, Map<String, Integer> lastSizes) throws IOException {
        LOGGER.debug("Processing profiles. Current sizes: {}", lastSizes);
        boolean newRoutesFound = false;
        for (String userProfile : profiles) {
            if (uniqueRoutesByProfile.get(userProfile).size() < numRoutes) {
                processNextBatch(client, userProfile);
                int currentSize = uniqueRoutesByProfile.get(userProfile).size();

                if (currentSize > lastSizes.get(userProfile)) {
                    newRoutesFound = true;
                    lastSizes.put(userProfile, currentSize);
                }
            }
        }
        return newRoutesFound;
    }

    private void updateProgress(ProgressBar pb) {
        int totalRoutes = getTotalRoutes();
        pb.stepTo(totalRoutes);
        pb.setExtraMessage(formatProgressMessage());
    }

    private int getTotalRoutes() {
        return uniqueRoutesByProfile.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    private String formatProgressMessage() {
        return uniqueRoutesByProfile.entrySet().stream()
                .map(e -> String.format("%s: %d/%d", e.getKey(), e.getValue().size(), numRoutes))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private boolean isGenerationComplete() {
        return uniqueRoutesByProfile.values().stream()
                .allMatch(routes -> routes.size() >= numRoutes);
    }

    @Override
    protected void initializeCollections() {
        resultsByProfile.values().forEach(List::clear);
        uniqueRoutesByProfile.values().forEach(Set::clear);
    }

    @Override
    protected void processNextBatch(CloseableHttpClient client, String profile) throws IOException {
        List<double[]> coordinates = randomCoordinatesInExtent(DEFAULT_MATRIX_SIZE);
        LOGGER.debug("Processing next batch for profile: {}", profile);
        String response = sendMatrixRequest(client, coordinates, profile);
        LOGGER.debug("Received matrix response for profile: {}", profile);
        if (response != null) {
            processMatrixResponse(response, profile);
        }
    }

    private String sendMatrixRequest(CloseableHttpClient client, List<double[]> coordinates, String profile) throws IOException {
        HttpPost request = createMatrixRequest(coordinates, profile);
        return client.execute(request, this::processResponse);
    }

    private HttpPost createMatrixRequest(List<double[]> coordinates, String profile) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("locations", coordinates);
        payload.put("metrics", new String[] { "distance" });

        HttpPost request = new HttpPost(baseUrl + "/v2/matrix/" + profile);
        headers.forEach(request::addHeader);
        request.setEntity(new StringEntity(mapper.writeValueAsString(payload), ContentType.APPLICATION_JSON));
        return request;
    }

    private void processMatrixResponse(String response, String profile) throws JsonProcessingException {
        LOGGER.debug("Processing matrix response for profile: {}", profile);
        Map<String, Object> responseMap = mapper.readValue(response, new TypeReference<Map<String, Object>>() {
        });

        List<List<Double>> distances = extractDistances(responseMap);
        List<Map<String, Object>> locations = extractLocations(responseMap, "destinations");

        if (distances == null || locations == null || locations.isEmpty()) {
            LOGGER.debug("Invalid matrix response - missing distances or locations");
            return;
        }

        processMatrixResults(distances, locations, profile);
    }

    @SuppressWarnings("unchecked")
    private List<List<Double>> extractDistances(Map<String, Object> responseMap) {
        Object distancesObj = responseMap.get("distances");
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

    private void processMatrixResults(List<List<Double>> distances, List<Map<String, Object>> locations,
            String profile) {
        LOGGER.debug("Processing matrix results for profile {} with {} locations", profile, locations.size());
        int size = locations.size();

        for (int i = 0; i < size; i++) {
            if (locations.get(i) == null)
                continue;

            processLocationPairs(distances, locations, i, size, profile);
        }
    }

    private void processLocationPairs(List<List<Double>> distances, List<Map<String, Object>> locations,
            int sourceIndex, int size, String profile) {
        for (int j = 0; j < size; j++) {
            if (sourceIndex == j || locations.get(j) == null)
                continue;

            processDistanceIfValid(distances, locations, sourceIndex, j, profile);
        }
    }

    private void processDistanceIfValid(List<List<Double>> distances, List<Map<String, Object>> locations,
            int i, int j, String profile) {
        // Check if the row exists
        if (i >= distances.size() || distances.get(i) == null)
            return;

        List<Double> row = distances.get(i);

        // Check if the distance value is valid
        if (j >= row.size() || row.get(j) == null)
            return;

        Double distance = row.get(j);
        if (distance > 0) {
            addRouteIfUnique(locations.get(i), locations.get(j), distance, profile);
        }
    }

    @SuppressWarnings("unchecked")
    private void addRouteIfUnique(Map<String, Object> start, Map<String, Object> end, double distance, String profile) {
        List<Number> startCoord = (List<Number>) start.get("location");
        List<Number> endCoord = (List<Number>) end.get("location");

        if (startCoord != null && endCoord != null && startCoord.size() >= 2 && endCoord.size() >= 2) {
            double[] startPoint = new double[] { startCoord.get(0).doubleValue(), startCoord.get(1).doubleValue() };
            double[] endPoint = new double[] { endCoord.get(0).doubleValue(), endCoord.get(1).doubleValue() };

            LOGGER.debug("Evaluating route: [{}, {}] -> [{}, {}], distance: {}, profile: {}",
                    startPoint[0], startPoint[1], endPoint[0], endPoint[1], distance, profile);

            if (distance < minDistance) {
                LOGGER.debug("Skipping route with distance {} < minimum {} meters", distance, minDistance);
                return;
            }

            RoutePair routePair = new RoutePair(startPoint, endPoint);
            if (uniqueRoutesByProfile.get(profile).add(routePair)) {
                LOGGER.debug("Added new unique route for profile: {}", profile);
                resultsByProfile.get(profile).add(new Route(startPoint, endPoint, distance, profile));
            } else {
                LOGGER.debug("Skipped duplicate route for profile: {}", profile);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Route> getResult() {
        List<Route> combined = new ArrayList<>();
        resultsByProfile.forEach((String userProfile, List<Route> routes) -> routes.stream()
                .limit(numRoutes)
                .forEach(combined::add));
        return combined;
    }

    @Override
    protected void writeToCSV(String filePath) throws FileNotFoundException {
        LOGGER.debug("Writing routes to CSV file: {}", filePath);
        try (PrintWriter pw = new PrintWriter(filePath)) {
                    pw.println("start_longitude,start_latitude,end_longitude,end_latitude,distance,profile");
            for (Map.Entry<String, List<Route>> entry : resultsByProfile.entrySet()) {
                entry.getValue().stream()
                        .limit(numRoutes)
                        .forEach(route -> pw.printf("%f,%f,%f,%f,%.2f,%s%n",
                                route.start[0], route.start[1],
                                route.end[0], route.end[1],
                                route.distance,
                                route.profile));
            }
        }
    }

    public static void main(String[] args) {
        try {
            CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);

            if (cli.hasHelp()) {
                cli.printHelp();
                return;
            }

            LOGGER.info("Creating coordinate generator for routes...");
            CoordinateGeneratorRoute generator = cli.createGenerator();

            LOGGER.info("Generating {} routes...", generator.numRoutes);
            generator.generateRoutes();
            LOGGER.info("\n");

            List<Route> result = generator.getResult();
            LOGGER.info("Writing {} routes to {}", result.size(), cli.getOutputFile());
            generator.writeToCSV(cli.getOutputFile());

            LOGGER.info("\n");
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
        System.exit(0);
    }
}
