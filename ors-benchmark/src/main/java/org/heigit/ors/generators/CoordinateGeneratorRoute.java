package org.heigit.ors.generators;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
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
import org.heigit.ors.util.CoordinateGeneratorHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import me.tongfei.progressbar.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CoordinateGeneratorRoute extends AbstractCoordinateGenerator {
    private static final int DEFAULT_MATRIX_SIZE = 2;
    private static final double DEFAULT_SNAP_RADIUS = 350; // 350 meters radius for snapping
    private static final String LOCATION_KEY = "location";
    private static final String LOCATIONS_KEY = "locations";
    private static final int DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private final int numRoutes;
    private final double minDistance;
    private final Map<String, Double> maxDistanceByProfile; // New field for max distances by profile
    private final Map<String, List<Route>> resultsByProfile;
    private final Map<String, Set<RoutePair>> uniqueRoutesByProfile;
    private final ReadWriteLock resultsLock = new ReentrantReadWriteLock();
    private final int numThreads;

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
            double minDistance, Map<String, Double> maxDistanceByProfile) {
        this(numRoutes, extent, profiles, baseUrl, minDistance, maxDistanceByProfile, DEFAULT_THREAD_COUNT);
    }

    protected CoordinateGeneratorRoute(int numRoutes, double[] extent, String[] profiles, String baseUrl,
            double minDistance, Map<String, Double> maxDistanceByProfile, int numThreads) {
        super(extent, profiles, baseUrl, "matrix");
        if (numRoutes <= 0)
            throw new IllegalArgumentException("Number of routes must be positive");
        if (minDistance < 0)
            throw new IllegalArgumentException("Minimum distance must be non-negative");
        if (numThreads <= 0)
            throw new IllegalArgumentException("Number of threads must be positive");

        this.numRoutes = numRoutes;
        this.minDistance = minDistance;
        this.maxDistanceByProfile = new HashMap<>(maxDistanceByProfile); // Copy to prevent external modification
        this.resultsByProfile = new HashMap<>();
        this.uniqueRoutesByProfile = new HashMap<>();
        this.numThreads = numThreads;

        for (String userProfile : profiles) {
            resultsByProfile.put(userProfile, new ArrayList<>());
            uniqueRoutesByProfile.put(userProfile, new HashSet<>());
            // Ensure each profile has a max distance, even if not explicitly provided
            this.maxDistanceByProfile.computeIfAbsent(userProfile, k -> {
                LOGGER.debug("No maximum distance specified for profile {}, using unlimited", k);
                return Double.MAX_VALUE;
            });
        }
    }

    @Override
    protected List<double[]> randomCoordinatesInExtent(int count) {
        return CoordinateGeneratorHelper.randomCoordinatesInExtent(count, extent);
    }

    @Override
    protected String processResponse(ClassicHttpResponse response) throws IOException {
        int status = response.getCode();
        LOGGER.debug("Processing response with status code: {}", status);
        if (status >= HttpStatus.SC_REDIRECTION) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Received error response: {}", new StatusLine(response));
            }
            return null;
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Received empty response entity");
            }
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

    private class RouteWorker implements Callable<Boolean> {
        private final String profile;
        private final CloseableHttpClient client;
        private final AtomicBoolean shouldContinue;

        public RouteWorker(String profile, CloseableHttpClient client, AtomicBoolean shouldContinue) {
            this.profile = profile;
            this.client = client;
            this.shouldContinue = shouldContinue;
        }

        @Override
        public Boolean call() {
            try {
                boolean profileComplete;
                resultsLock.readLock().lock();
                try {
                    profileComplete = uniqueRoutesByProfile.get(profile).size() >= numRoutes;
                } finally {
                    resultsLock.readLock().unlock();
                }

                if (profileComplete || !shouldContinue.get()) {
                    return false;
                }

                processNextBatch(client, profile);

                // Check if we found any new routes
                resultsLock.readLock().lock();
                try {
                    return uniqueRoutesByProfile.get(profile).size() < numRoutes;
                } finally {
                    resultsLock.readLock().unlock();
                }
            } catch (IOException e) {
                LOGGER.error("Error in worker thread for profile {}: {}", profile, e.getMessage());
                return false;
            }
        }
    }

    @Override
    public void generate(int maxAttempts) {
        LOGGER.debug("Starting parallel route generation with max attempts: {} using {} threads",
                maxAttempts, numThreads);
        initializeCollections();

        ExecutorService executor = null;
        AtomicBoolean shouldContinue = new AtomicBoolean(true);
        AtomicInteger attempts = new AtomicInteger(0);

        ProgressBarBuilder pbb = new ProgressBarBuilder()
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .setUpdateIntervalMillis(5000)
                .setTaskName("Generating routes")
                .setInitialMax((long) numRoutes * profiles.length)
                .setConsumer(new DelegatingProgressBarConsumer(ProgressBarLogger.getLogger()::info));

        try (CloseableHttpClient client = createHttpClient();
                ProgressBar pb = pbb.build()) {

            executor = Executors.newFixedThreadPool(numThreads);
            pb.setExtraMessage("Starting with " + numThreads + " threads...");

            while (!isGenerationComplete() && attempts.get() < maxAttempts && shouldContinue.get()) {
                LOGGER.debug("Generation iteration - Attempt {}/{}", attempts.get() + 1, maxAttempts);

                List<Future<Boolean>> futures = new ArrayList<>();

                // Submit tasks for each profile that needs more routes
                for (String profile : profiles) {
                    resultsLock.readLock().lock();
                    boolean profileNeedsMoreRoutes = false;
                    try {
                        profileNeedsMoreRoutes = uniqueRoutesByProfile.get(profile).size() < numRoutes;
                    } finally {
                        resultsLock.readLock().unlock();
                    }

                    if (profileNeedsMoreRoutes) {
                        // Submit multiple worker tasks for each profile for better parallelism
                        int tasksPerProfile = Math.max(1, numThreads / profiles.length);
                        for (int i = 0; i < tasksPerProfile; i++) {
                            futures.add(executor.submit(new RouteWorker(profile, client, shouldContinue)));
                        }
                    }
                }

                // Wait for all tasks to complete
                int newRoutesFound = 0;
                int initialTotalRoutes = getTotalRoutes();

                for (Future<Boolean> future : futures) {
                    try {
                        Boolean hasMoreWork = future.get();
                        if (Boolean.TRUE.equals(hasMoreWork)) {
                            newRoutesFound++;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        shouldContinue.set(false);
                        LOGGER.warn("Route generation interrupted");
                    } catch (ExecutionException e) {
                        LOGGER.error("Error in worker task: {}", e.getCause().getMessage());
                    }
                }

                updateProgress(pb);

                if (newRoutesFound == 0 || getTotalRoutes() == initialTotalRoutes) {
                    attempts.incrementAndGet();
                    pb.setExtraMessage(String.format("Attempt %d/%d - No new routes", attempts.get(), maxAttempts));
                } else {
                    attempts.set(0);
                }
            }

            pb.stepTo(getTotalRoutes());
            if (attempts.get() >= maxAttempts && LOGGER.isWarnEnabled()) {
                LOGGER.warn("Stopped route generation after {} attempts. Routes per profile: {}",
                        maxAttempts, formatProgressMessage());
            }

        } catch (Exception e) {
            LOGGER.error("Error generating routes: ", e);
        } finally {
            if (executor != null) {
                executor.shutdown(); // Initiate orderly shutdown
                try {
                    // Wait for tasks to complete, or force termination after timeout
                    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                        LOGGER.warn("Executor did not terminate in the specified time, forcing shutdown");
                        executor.shutdownNow(); // Force shutdown
                        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                            LOGGER.error("Executor still did not terminate");
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("Termination interrupted, forcing shutdown");
                    executor.shutdownNow();
                }
            }
        }

    }

    private void updateProgress(ProgressBar pb) {
        int totalRoutes = getTotalRoutes();
        pb.stepTo(totalRoutes);
        pb.setExtraMessage(formatProgressMessage());
    }

    private int getTotalRoutes() {
        resultsLock.readLock().lock();
        try {
            return uniqueRoutesByProfile.values().stream()
                    .mapToInt(Set::size)
                    .sum();
        } finally {
            resultsLock.readLock().unlock();
        }
    }

    private String formatProgressMessage() {
        resultsLock.readLock().lock();
        try {
            return uniqueRoutesByProfile.entrySet().stream()
                    .map(e -> String.format("%s: %d/%d", e.getKey(), e.getValue().size(), numRoutes))
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
        } finally {
            resultsLock.readLock().unlock();
        }
    }

    private boolean isGenerationComplete() {
        resultsLock.readLock().lock();
        try {
            return uniqueRoutesByProfile.values().stream()
                    .allMatch(routes -> routes.size() >= numRoutes);
        } finally {
            resultsLock.readLock().unlock();
        }
    }

    @Override
    protected void initializeCollections() {
        resultsLock.writeLock().lock();
        try {
            resultsByProfile.values().forEach(List::clear);
            uniqueRoutesByProfile.values().forEach(Set::clear);
        } finally {
            resultsLock.writeLock().unlock();
        }
    }

    @Override
    protected void processNextBatch(CloseableHttpClient client, String profile) throws IOException {
        // Get max distance for this profile
        Double maxDistance = maxDistanceByProfile.get(profile);
        double effectiveMaxDistance = maxDistance != null ? maxDistance : Double.MAX_VALUE;
        // Generate random coordinates with max distance constraint if applicable
        List<double[]> randomCoordinates = CoordinateGeneratorHelper.randomCoordinatesInExtent(DEFAULT_MATRIX_SIZE,
                extent);
        LOGGER.debug("Generated {} random coordinates for profile: {} with max distance: {}",
                randomCoordinates.size(), profile, maxDistance != null ? maxDistance : "unlimited");

        // Snap the coordinates to the road network
        List<double[]> snappedCoordinates = snapCoordinates(client, randomCoordinates, profile);
        LOGGER.debug("Received {} snapped coordinates for profile: {}", snappedCoordinates.size(), profile);

        // Only proceed if we have enough valid snapped coordinates
        if (snappedCoordinates.size() < 2) {
            LOGGER.debug("Not enough valid snapped coordinates to create matrix (minimum 2 needed)");
            return;
        }

        // Find the second pair coordinates that are within the max distance and extent.
        // Store both pairs and use set to avoid duplicates. Use
        // randomCoordinateInRadiusAndExtent to generate the second pair.
        for (int i = 0; i < snappedCoordinates.size(); i++) {
            double[] start = snappedCoordinates.get(i);
            double[] end = CoordinateGeneratorHelper.randomCoordinateInRadiusAndExtent(start, effectiveMaxDistance,
                    extent);
            if (end.length > 0
                    && CoordinateGeneratorHelper.calculateHaversineDistance(start, end) <= effectiveMaxDistance) {
                String response = sendMatrixRequest(client, List.of(start, end), profile);
                if (response != null) {
                    processMatrixResponse(response, profile);
                }
            }
        }
    }

    /**
     * Sends a request to the snapping API to snap coordinates to the road network.
     * 
     * @param client      HTTP client to use
     * @param coordinates Coordinates to snap
     * @param profile     Profile to use for snapping
     * @return List of snapped coordinates, may be smaller than input if some
     *         coordinates couldn't be snapped
     * @throws IOException If an error occurs during the request
     */
    private List<double[]> snapCoordinates(CloseableHttpClient client, List<double[]> coordinates, String profile)
            throws IOException {
        LOGGER.debug("Snapping {} coordinates for profile: {}", coordinates.size(), profile);

        // Create the snap request
        HttpPost request = createSnapRequest(coordinates, profile);

        // Execute the request and get the response
        String response = client.execute(request, this::processResponse);
        if (response == null) {
            LOGGER.debug("Received null response from snap API");
            return Collections.emptyList();
        }

        // Parse and process the snap response
        return processSnapResponse(response);
    }

    /**
     * Creates an HTTP request for the snap API.
     * 
     * @param coordinates Coordinates to snap
     * @param profile     Profile to use for snapping
     * @return HttpPost object configured for the snap request
     * @throws JsonProcessingException If the JSON serialization fails
     */
    private HttpPost createSnapRequest(List<double[]> coordinates, String profile) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put(LOCATIONS_KEY, coordinates);
        payload.put("radius", DEFAULT_SNAP_RADIUS);

        HttpPost request = new HttpPost(baseUrl + "/v2/snap/" + profile);
        headers.forEach(request::addHeader);
        request.setEntity(new StringEntity(mapper.writeValueAsString(payload), ContentType.APPLICATION_JSON));
        return request;
    }

    /**
     * Processes the response from the snap API.
     * 
     * @param response JSON response string
     * @return List of snapped coordinates
     * @throws JsonProcessingException If the JSON parsing fails
     */
    @SuppressWarnings("unchecked")
    private List<double[]> processSnapResponse(String response) throws JsonProcessingException {
        List<double[]> snappedCoordinates = new ArrayList<>();

        Map<String, Object> responseMap = mapper.readValue(response,
                new TypeReference<Map<String, Object>>() {
                });

        Object locationsObj = responseMap.get(LOCATIONS_KEY);
        if (!(locationsObj instanceof List<?>)) {
            LOGGER.debug("Snap response contained no valid locations array");
            return snappedCoordinates;
        }

        List<Map<String, Object>> locations = (List<Map<String, Object>>) locationsObj;
        for (Map<String, Object> location : locations) {
            if (location == null) {
                LOGGER.debug("Invalid location in snap response");
                continue;
            }
            List<Number> coords = (List<Number>) location.get(LOCATION_KEY);
            if (coords != null && coords.size() >= 2) {
                double[] point = new double[] { coords.get(0).doubleValue(), coords.get(1).doubleValue() };
                snappedCoordinates.add(point);
                LOGGER.debug("Added snapped coordinate: [{}, {}]", point[0], point[1]);
            } else {
                LOGGER.debug("Invalid location in snap response");
            }
        }

        return snappedCoordinates;

    }

    private String sendMatrixRequest(CloseableHttpClient client, List<double[]> coordinates, String profile)
            throws IOException {
        HttpPost request = createMatrixRequest(coordinates, profile);
        return client.execute(request, this::processResponse);
    }

    private HttpPost createMatrixRequest(List<double[]> coordinates, String profile) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put(LOCATIONS_KEY, coordinates);
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
        List<Map<String, Object>> destinationCoordinates = extractLocations(responseMap, "destinations");
        List<Map<String, Object>> sourceCoordinates = extractLocations(responseMap, "sources");
        if (distances == null || destinationCoordinates == null || destinationCoordinates.isEmpty()) {
            LOGGER.debug("Invalid matrix response - missing distances or locations");
            return;
        }

        if (destinationCoordinates.size() != sourceCoordinates.size()) {
            LOGGER.debug("Invalid matrix response - different number of source and destination coordinates");
            return;
        }

        for (int i = 0; i < destinationCoordinates.size(); i++) {
            if (sourceCoordinates.get(i) == null || destinationCoordinates.get(i) == null || distances.get(i) == null
                    || distances.get(i).get(0) == null) {
                LOGGER.debug("Invalid matrix response - source {}, destination {}, distance {}",
                        sourceCoordinates.get(i), destinationCoordinates.get(i), distances.get(i));
                continue;
            }
            addRouteIfUnique(sourceCoordinates.get(i), destinationCoordinates.get(i), distances.get(i).get(0), profile);
        }
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

    @SuppressWarnings("unchecked")
    private void addRouteIfUnique(Map<String, Object> start, Map<String, Object> end, double distance, String profile) {
        List<Number> startCoord = (List<Number>) start.get(LOCATION_KEY);
        List<Number> endCoord = (List<Number>) end.get(LOCATION_KEY);

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

            resultsLock.writeLock().lock();
            try {
                if (uniqueRoutesByProfile.get(profile).add(routePair)) {
                    LOGGER.debug("Added new unique route for profile: {}", profile);
                    resultsByProfile.get(profile).add(new Route(startPoint, endPoint, distance, profile));
                } else {
                    LOGGER.debug("Skipped duplicate route for profile: {}", profile);
                }
            } finally {
                resultsLock.writeLock().unlock();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Route> getResult() {
        resultsLock.readLock().lock();
        try {
            List<Route> combined = new ArrayList<>();
            resultsByProfile.forEach((String userProfile, List<Route> routes) -> routes.stream()
                    .limit(numRoutes)
                    .forEach(combined::add));
            return combined;
        } finally {
            resultsLock.readLock().unlock();
        }
    }

    @Override
    protected void writeToCSV(String filePath) throws FileNotFoundException {
        LOGGER.debug("Writing routes to CSV file: {}", filePath);
        resultsLock.readLock().lock();
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
        } finally {
            resultsLock.readLock().unlock();
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
