package org.heigit.ors.coordinates_generator.generators;

import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.heigit.ors.coordinates_generator.model.Route;
import org.heigit.ors.coordinates_generator.model.RouteRepository;
import org.heigit.ors.coordinates_generator.service.MatrixCalculator;
import org.heigit.ors.coordinates_generator.service.CoordinateSnapper;
import org.heigit.ors.util.CoordinateGeneratorHelper;
import org.heigit.ors.util.ProgressBarLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class CoordinateGeneratorRoute extends AbstractCoordinateGenerator {
    protected static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorRoute.class);

    private static final int DEFAULT_MATRIX_SIZE = 2;
    private static final int DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final String LOCATION_KEY = "location";

    private final int numRoutes;
    private final double minDistance;
    private final Map<String, Double> maxDistanceByProfile;
    private final RouteRepository routeRepository;
    private final CoordinateSnapper coordinateSnapper;
    private final MatrixCalculator matrixCalculator;
    private final int numThreads;

    protected CoordinateGeneratorRoute(int numRoutes, double[] extent, String[] profiles, String baseUrl,
            double minDistance, Map<String, Double> maxDistanceByProfile) {
        this(numRoutes, extent, profiles, baseUrl, minDistance, maxDistanceByProfile, DEFAULT_THREAD_COUNT);
    }

    public CoordinateGeneratorRoute(int numRoutes, double[] extent, String[] profiles, String baseUrl,
                                    double minDistance, Map<String, Double> maxDistanceByProfile, int numThreads) {
        super(extent, profiles, baseUrl, "matrix");
        validateInputs(numRoutes, minDistance, numThreads);

        this.numRoutes = numRoutes;
        this.minDistance = minDistance;
        this.maxDistanceByProfile = normalizeMaxDistances(profiles, maxDistanceByProfile);
        this.numThreads = numThreads;
        this.routeRepository = new RouteRepository(Set.of(profiles));

        Function<HttpPost, String> requestExecutor = request -> {
            LOGGER.debug("RequestExecutor: Preparing to execute request URI: {}", request.getRequestUri());
            try (CloseableHttpClient client = createHttpClient()) {
                String response = client.execute(request, this::processResponse);
                LOGGER.debug("RequestExecutor: Raw response: {}", response);
                return response;
            } catch (IOException e) {
                LOGGER.debug("Error executing request: {}", e.getMessage());
                return null;
            }
        };

        Map<String, String> headers = createHeaders();
        this.coordinateSnapper = new CoordinateSnapper(baseUrl, headers, mapper, requestExecutor);
        this.matrixCalculator = new MatrixCalculator(baseUrl, headers, mapper, requestExecutor);
    }

    private void validateInputs(int numRoutes, double minDistance, int numThreads) {
        if (numRoutes <= 0)
            throw new IllegalArgumentException("Number of routes must be positive");
        if (minDistance < 0)
            throw new IllegalArgumentException("Minimum distance must be non-negative");
        if (numThreads <= 0)
            throw new IllegalArgumentException("Number of threads must be positive");
    }

    private Map<String, Double> normalizeMaxDistances(String[] profiles, Map<String, Double> maxDistanceByProfile) {
        Map<String, Double> normalized = new HashMap<>();
        for (String profile : profiles) {
            normalized.put(profile, maxDistanceByProfile.getOrDefault(profile, Double.MAX_VALUE));
        }
        return normalized;
    }

    public void generateRoutes() {
        generate(DEFAULT_MAX_ATTEMPTS);
    }

    @Override
    public void generate(int maxAttempts) {
        LOGGER.info("Starting route generation with {} threads and max attempts: {}", numThreads, maxAttempts);
        initializeCollections();
        LOGGER.debug("Collections initialized.");

        ExecutorService executor = null;
        AtomicBoolean shouldContinue = new AtomicBoolean(true);
        AtomicInteger consecutiveFailedAttempts = new AtomicInteger(0);

        try {
            executor = Executors.newFixedThreadPool(numThreads);
            executeRouteGeneration(executor, maxAttempts, shouldContinue, consecutiveFailedAttempts);
        } catch (Exception e) {
            LOGGER.error("Error generating routes: ", e);
        } finally {
            shutdownExecutor(executor);
        }
    }

    private void executeRouteGeneration(ExecutorService executor, int maxAttempts,
            AtomicBoolean shouldContinue, AtomicInteger consecutiveFailedAttempts) {
        LOGGER.debug("executeRouteGeneration: Starting. Max attempts: {}, Should continue: {}, Consecutive Fails: {}",
                maxAttempts, shouldContinue.get(), consecutiveFailedAttempts.get());
        try (ProgressBar pb = createProgressBar()) {
            while (!routeRepository.areAllProfilesComplete(numRoutes) &&
                    consecutiveFailedAttempts.get() < maxAttempts &&
                    shouldContinue.get()) {
                LOGGER.debug(
                        "executeRouteGeneration: Loop iteration. Profiles complete: {}, Consecutive Fails: {}, Should continue: {}",
                        routeRepository.areAllProfilesComplete(numRoutes), consecutiveFailedAttempts.get(),
                        shouldContinue.get());

                int initialTotalRoutes = routeRepository.getTotalRouteCount();
                LOGGER.debug("executeRouteGeneration: Initial total routes: {}", initialTotalRoutes);
                List<Future<Boolean>> futures = submitTasks(executor, shouldContinue);
                LOGGER.debug("executeRouteGeneration: Submitted {} tasks.", futures.size());

                // Wait for all tasks to complete
                for (Future<Boolean> future : futures) {
                    try {
                        Boolean result = future.get();
                        LOGGER.debug("executeRouteGeneration: Task completed with result: {}", result);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        shouldContinue.set(false);
                        LOGGER.warn("Route generation interrupted");
                    } catch (ExecutionException e) {
                        LOGGER.error("Error in worker task: {}", e.getCause().getMessage(), e.getCause());
                    }
                }

                updateProgressBar(pb);
                LOGGER.debug("executeRouteGeneration: Progress bar updated.");

                // Check if we made progress in this iteration
                if (routeRepository.getTotalRouteCount() == initialTotalRoutes) {
                    int attempts = consecutiveFailedAttempts.incrementAndGet();
                    LOGGER.debug("executeRouteGeneration: No new routes. Consecutive failed attempts: {}", attempts);
                    pb.setExtraMessage(String.format("Attempt %d/%d - No new routes", attempts, maxAttempts));
                } else {
                    LOGGER.debug("executeRouteGeneration: New routes added. Resetting consecutive failed attempts.");
                    consecutiveFailedAttempts.set(0);
                }
            }

            finalizeProgress(pb, maxAttempts, consecutiveFailedAttempts.get());
            LOGGER.debug("executeRouteGeneration: Finalized progress.");
        }
    }

    private ProgressBar createProgressBar() {
        return new ProgressBarBuilder()
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .setUpdateIntervalMillis(2000)
                .setTaskName("Generating routes")
                .setInitialMax((long) numRoutes * profiles.length)
                .setConsumer(new DelegatingProgressBarConsumer(ProgressBarLogger.getLogger()::info))
                .build();
    }

    private List<Future<Boolean>> submitTasks(ExecutorService executor, AtomicBoolean shouldContinue) {
        List<Future<Boolean>> futures = new ArrayList<>();
        LOGGER.debug("submitTasks: Starting. Should continue: {}", shouldContinue.get());

        for (String profile : profiles) {
            LOGGER.debug("submitTasks: Checking profile: {}. Is complete: {}", profile,
                    routeRepository.isProfileComplete(profile, numRoutes));
            if (!routeRepository.isProfileComplete(profile, numRoutes)) {
                int tasksPerProfile = Math.max(1, numThreads / profiles.length);
                LOGGER.debug("submitTasks: Submitting {} tasks for profile: {}", tasksPerProfile, profile);
                for (int i = 0; i < tasksPerProfile && shouldContinue.get(); i++) {
                    futures.add(executor.submit(new RouteGenerationTask(profile, shouldContinue)));
                }
            }
        }
        LOGGER.debug("submitTasks: Finished. Total tasks submitted: {}", futures.size());
        return futures;
    }

    private void updateProgressBar(ProgressBar pb) {
        int totalRoutes = routeRepository.getTotalRouteCount();
        pb.stepTo(totalRoutes);
        pb.setExtraMessage(routeRepository.getProgressMessage());
    }

    private void finalizeProgress(ProgressBar pb, int maxAttempts, int attempts) {
        pb.stepTo(routeRepository.getTotalRouteCount());
        if (attempts >= maxAttempts) {
            LOGGER.warn("Stopped route generation after {} attempts. Routes per profile: {}",
                    maxAttempts, routeRepository.getProgressMessage());
        }
    }

    private void shutdownExecutor(ExecutorService executor) {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
        }
    }

    private class RouteGenerationTask implements Callable<Boolean> {
        private final String profile;
        private final AtomicBoolean shouldContinue;

        public RouteGenerationTask(String profile, AtomicBoolean shouldContinue) {
            this.profile = profile;
            this.shouldContinue = shouldContinue;
        }

        @Override
        public Boolean call() {
            LOGGER.debug(
                    "RouteGenerationTask.call: Starting for profile: {}. Should continue: {}, Profile complete: {}",
                    profile, shouldContinue.get(), routeRepository.isProfileComplete(profile, numRoutes));
            if (!shouldContinue.get() || routeRepository.isProfileComplete(profile, numRoutes)) {
                LOGGER.debug("RouteGenerationTask.call: Exiting early for profile: {}", profile);
                return false;
            }

            try {
                boolean result = generateRoutesForProfile();
                LOGGER.debug("RouteGenerationTask.call: generateRoutesForProfile returned {} for profile: {}", result,
                        profile);
                return result;
            } catch (Exception e) {
                LOGGER.error("Error generating routes for profile {}: {}", profile, e.getMessage(), e);
                return false;
            }
        }

        private boolean generateRoutesForProfile() {
            LOGGER.debug("generateRoutesForProfile: Starting for profile: {}", profile);
            // Get max distance for this profile
            double effectiveMaxDistance = maxDistanceByProfile.getOrDefault(profile, Double.MAX_VALUE);
            LOGGER.debug("generateRoutesForProfile: Effective max distance: {} for profile: {}", effectiveMaxDistance,
                    profile);

            // Generate random coordinates
            List<double[]> randomCoordinates = CoordinateGeneratorHelper.randomCoordinatesInExtent(DEFAULT_MATRIX_SIZE,
                    extent);
            LOGGER.debug("generateRoutesForProfile: Generated random coordinates: {} for profile: {}",
                    Arrays.deepToString(randomCoordinates.toArray()), profile);

            // Snap the coordinates to the road network
            LOGGER.debug("generateRoutesForProfile: Snapping coordinates for profile: {}", profile);
            List<double[]> snappedCoordinates = coordinateSnapper.snapCoordinates(randomCoordinates, profile);
            LOGGER.debug("generateRoutesForProfile: Snapped coordinates: {} for profile: {}",
                    Arrays.deepToString(snappedCoordinates.toArray()), profile);

            if (snappedCoordinates.size() < 2) {
                LOGGER.debug("generateRoutesForProfile: Not enough snapped coordinates ({}) for profile: {}. Skipping.",
                        snappedCoordinates.size(), profile);
                return false;
            }

            boolean result = processSnappedCoordinates(snappedCoordinates, effectiveMaxDistance);
            LOGGER.debug("generateRoutesForProfile: processSnappedCoordinates returned {} for profile: {}", result,
                    profile);
            return result;
        }

        private boolean processSnappedCoordinates(List<double[]> snappedCoordinates, double maxDistance) {
            LOGGER.debug(
                    "processSnappedCoordinates: Starting for profile: {}. Snapped coordinates: {}, Max distance: {}",
                    profile, Arrays.deepToString(snappedCoordinates.toArray()), maxDistance);
            boolean addedNewRoute = false;
            try {
                addedNewRoute = processCoordinatePairs(snappedCoordinates, maxDistance);
                LOGGER.debug("processSnappedCoordinates: processCoordinatePairs returned {} for profile: {}",
                        addedNewRoute, profile);
            } catch (Exception e) {
                LOGGER.error("Error processing snapped coordinates for profile {}: {}", profile, e.getMessage(), e);
            }
            LOGGER.debug("processSnappedCoordinates: Finished for profile: {}. Added new route: {}", profile,
                    addedNewRoute);
            return addedNewRoute;
        }

        private boolean processCoordinatePairs(List<double[]> snappedCoordinates, double maxDistance) {
            LOGGER.debug("processCoordinatePairs: Starting for profile: {}. Snapped coordinates: {}, Max distance: {}",
                    profile, Arrays.deepToString(snappedCoordinates.toArray()), maxDistance);
            boolean addedNewRoute = false;
            // Process all pairs of coordinates
            for (double[] start : snappedCoordinates) {
                LOGGER.debug("processCoordinatePairs: Processing start coordinate: {} for profile: {}",
                        Arrays.toString(start), profile);
                // Generate a destination point within max distance
                double[] end = CoordinateGeneratorHelper.randomCoordinateInRadiusAndExtent(
                        start, maxDistance, extent);
                LOGGER.debug("processCoordinatePairs: Generated end coordinate: {} for profile: {}",
                        Arrays.toString(end), profile);

                if (end.length > 0 &&
                        CoordinateGeneratorHelper.calculateHaversineDistance(start, end) <= maxDistance) {
                    LOGGER.debug("processCoordinatePairs: End coordinate is valid and within distance for profile: {}",
                            profile);

                    LOGGER.debug(
                            "processCoordinatePairs: Calculating matrix for profile: {} with start: {} and end: {}",
                            profile, Arrays.toString(start), Arrays.toString(end));
                    Optional<MatrixCalculator.MatrixResult> matrixResultOpt = matrixCalculator.calculateMatrix(
                            List.of(start, end), profile);
                    LOGGER.debug("processCoordinatePairs: Matrix calculation result present: {} for profile: {}",
                            matrixResultOpt.isPresent(), profile);

                    if (matrixResultOpt.isPresent()) {
                        boolean processed = processMatrixResult(matrixResultOpt.get());
                        LOGGER.debug("processCoordinatePairs: processMatrixResult returned {} for profile: {}",
                                processed, profile);
                        addedNewRoute |= processed;
                    }
                } else {
                    LOGGER.debug(
                            "processCoordinatePairs: End coordinate invalid or too far for profile: {}. End: {}, Haversine: {}",
                            profile, Arrays.toString(end),
                            (end.length > 0 ? CoordinateGeneratorHelper.calculateHaversineDistance(start, end)
                                    : "N/A"));
                }
            }
            LOGGER.debug("processCoordinatePairs: Finished for profile: {}. Added new route: {}", profile,
                    addedNewRoute);
            return addedNewRoute;
        }

        private boolean processMatrixResult(MatrixCalculator.MatrixResult result) {
            LOGGER.debug("processMatrixResult: Starting for profile: {}", profile);

            if (!result.isValid()) {
                LOGGER.info("Matrix result is invalid, skipping processing for profile: {}", profile);
                return false;
            }
            LOGGER.debug("processMatrixResult: Matrix result is valid for profile: {}", profile);

            List<Map<String, Object>> sources = result.getSources();
            List<Map<String, Object>> destinations = result.getDestinations();
            List<List<Double>> distances = result.getDistances();
            LOGGER.debug("processMatrixResult: Sources: {}, Destinations: {}, Distances: {} for profile: {}", sources,
                    destinations, distances, profile);

            boolean addedNewRoute = false;

            for (int i = 0; i < destinations.size(); i++) {
                LOGGER.debug("processMatrixResult: Processing destination index {} for profile: {}", i, profile);
                if (sources.get(i) == null || destinations.get(i) == null ||
                        distances.get(i) == null || distances.get(i).isEmpty()) {
                    LOGGER.debug("processMatrixResult: Skipping null or empty data at index {} for profile: {}", i,
                            profile);
                    continue;
                }

                if (distances.get(i).get(0) != null) {
                    double distanceValue = distances.get(i).get(0);
                    LOGGER.debug("processMatrixResult: Distance value: {} for profile: {}", distanceValue, profile);
                    boolean added = addRouteIfUnique(sources.get(i), destinations.get(i),
                            distanceValue, profile);
                    LOGGER.debug("processMatrixResult: addRouteIfUnique returned {} for profile: {}", added, profile);

                    if (added) {
                        addedNewRoute = true;
                        LOGGER.debug("processMatrixResult: New route added for profile: {}", profile);
                    }
                } else {
                    LOGGER.debug("processMatrixResult: Distance at index {} is null for profile: {}", i, profile);
                }
            }
            LOGGER.debug("processMatrixResult: Finished for profile: {}. Added new route: {}", profile, addedNewRoute);
            return addedNewRoute;
        }

        @SuppressWarnings("unchecked")
        private boolean addRouteIfUnique(Map<String, Object> start, Map<String, Object> end, double distance,
                String profile) {
            LOGGER.debug("addRouteIfUnique: Starting for profile: {}. Start: {}, End: {}, Distance: {}", profile, start,
                    end, distance);
            try {
                List<Number> startCoord = (List<Number>) start.get(LOCATION_KEY);
                List<Number> endCoord = (List<Number>) end.get(LOCATION_KEY);

                if (startCoord != null && endCoord != null && startCoord.size() >= 2 && endCoord.size() >= 2) {
                    LOGGER.debug("addRouteIfUnique: Coordinates are valid for profile: {}", profile);

                    double[] startPoint = new double[] { startCoord.get(0).doubleValue(),
                            startCoord.get(1).doubleValue() };
                    double[] endPoint = new double[] { endCoord.get(0).doubleValue(), endCoord.get(1).doubleValue() };
                    LOGGER.debug("addRouteIfUnique: Start point: {}, End point: {} for profile: {}",
                            Arrays.toString(startPoint), Arrays.toString(endPoint), profile);

                    if (distance < minDistance) {
                        LOGGER.debug(
                                "addRouteIfUnique: Distance {} is less than minDistance {} for profile: {}. Skipping.",
                                distance, minDistance, profile);
                        return false;
                    }

                    Route route = new Route(startPoint, endPoint, distance, profile);
                    LOGGER.debug("addRouteIfUnique: Created route object: {} for profile: {}", route, profile);
                    boolean added = routeRepository.addRouteIfUnique(route);
                    LOGGER.debug("addRouteIfUnique: routeRepository.addRouteIfUnique returned {} for profile: {}",
                            added, profile);

                    if (added) {
                        LOGGER.debug("Added new route for profile {}: {} -> {} ({}m)", profile,
                                        Arrays.toString(startPoint), Arrays.toString(endPoint), distance);
                        return true;
                    } else {
                        LOGGER.debug("Route already exists or not added for profile {}: {} -> {} ({}m)", profile,
                                Arrays.toString(startPoint), Arrays.toString(endPoint), distance);
                        return false;
                    }
                } else {
                    LOGGER.debug(
                            "addRouteIfUnique: Invalid coordinates (null or insufficient size) for profile: {}. Start: {}, End: {}",
                            profile, startCoord, endCoord);
                }
            } catch (Exception e) {
                LOGGER.error("Error adding route for profile {}: {}", profile, e.getMessage(), e);
            }
            LOGGER.debug("addRouteIfUnique: Finished for profile: {}. Returning false.", profile);
            return false;
        }
    }

    @Override
    protected void initializeCollections() {
        routeRepository.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Route> getResult() {
        return routeRepository.getAllRoutes(numRoutes);
    }

    @Override
    public void writeToCSV(String filePath) throws FileNotFoundException {
        routeRepository.writeToCSV(filePath, numRoutes);
    }

    public int getNumRoutes() {
        return numRoutes;
    }
}
