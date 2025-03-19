package org.heigit.ors.generators;

import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.heigit.ors.model.Route;
import org.heigit.ors.model.RouteRepository;
import org.heigit.ors.service.MatrixCalculator;
import org.heigit.ors.service.RouteSnapper;
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
    private final RouteSnapper routeSnapper;
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
            try (CloseableHttpClient client = createHttpClient()) {
                return client.execute(request, this::processResponse);
            } catch (IOException e) {
                LOGGER.error("Error executing request: {}", e.getMessage());
                return null;
            }
        };

        Map<String, String> headers = createHeaders();
        this.routeSnapper = new RouteSnapper(baseUrl, headers, mapper, requestExecutor);
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
        try (ProgressBar pb = createProgressBar()) {
            while (!routeRepository.areAllProfilesComplete(numRoutes) &&
                    consecutiveFailedAttempts.get() < maxAttempts &&
                    shouldContinue.get()) {

                int initialTotalRoutes = routeRepository.getTotalRouteCount();
                List<Future<Boolean>> futures = submitTasks(executor, shouldContinue);

                // Wait for all tasks to complete
                for (Future<Boolean> future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        shouldContinue.set(false);
                        LOGGER.warn("Route generation interrupted");
                    } catch (ExecutionException e) {
                        LOGGER.error("Error in worker task: {}", e.getCause().getMessage());
                    }
                }

                updateProgressBar(pb);

                // Check if we made progress in this iteration
                if (routeRepository.getTotalRouteCount() == initialTotalRoutes) {
                    int attempts = consecutiveFailedAttempts.incrementAndGet();
                    pb.setExtraMessage(String.format("Attempt %d/%d - No new routes", attempts, maxAttempts));
                } else {
                    consecutiveFailedAttempts.set(0);
                }
            }

            finalizeProgress(pb, maxAttempts, consecutiveFailedAttempts.get());
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

        for (String profile : profiles) {
            if (!routeRepository.isProfileComplete(profile, numRoutes)) {
                int tasksPerProfile = Math.max(1, numThreads / profiles.length);
                for (int i = 0; i < tasksPerProfile && shouldContinue.get(); i++) {
                    futures.add(executor.submit(new RouteGenerationTask(profile, shouldContinue)));
                }
            }
        }

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
            if (!shouldContinue.get() || routeRepository.isProfileComplete(profile, numRoutes)) {
                return false;
            }

            try {
                return generateRoutesForProfile();
            } catch (Exception e) {
                LOGGER.error("Error generating routes for profile {}: {}", profile, e.getMessage());
                return false;
            }
        }

        private boolean generateRoutesForProfile() {
            // Get max distance for this profile
            double effectiveMaxDistance = maxDistanceByProfile.getOrDefault(profile, Double.MAX_VALUE);

            // Generate random coordinates
            List<double[]> randomCoordinates = CoordinateGeneratorHelper.randomCoordinatesInExtent(DEFAULT_MATRIX_SIZE,
                    extent);

            // Snap the coordinates to the road network
            List<double[]> snappedCoordinates = routeSnapper.snapCoordinates(randomCoordinates, profile);

            if (snappedCoordinates.size() < 2) {
                return false;
            }

            return processSnappedCoordinates(snappedCoordinates, effectiveMaxDistance);
        }

        private boolean processSnappedCoordinates(List<double[]> snappedCoordinates, double maxDistance) {
            boolean addedNewRoute = false;
            try {
                addedNewRoute = processCoordinatePairs(snappedCoordinates, maxDistance);
            } catch (Exception e) {
                LOGGER.error("Error processing snapped coordinates: {}", e.getMessage());
            }
            return addedNewRoute;
        }

        private boolean processCoordinatePairs(List<double[]> snappedCoordinates, double maxDistance) {
            boolean addedNewRoute = false;
            // Process all pairs of coordinates
            for (double[] start : snappedCoordinates) {
                // Generate a destination point within max distance
                double[] end = CoordinateGeneratorHelper.randomCoordinateInRadiusAndExtent(
                        start, maxDistance, extent);

                if (end.length > 0 &&
                        CoordinateGeneratorHelper.calculateHaversineDistance(start, end) <= maxDistance) {

                    Optional<MatrixCalculator.MatrixResult> matrixResultOpt = matrixCalculator.calculateMatrix(
                            List.of(start, end), profile);

                    if (matrixResultOpt.isPresent()) {
                        addedNewRoute |= processMatrixResult(matrixResultOpt.get());
                    }
                }
            }
            return addedNewRoute;
        }

        private boolean processMatrixResult(MatrixCalculator.MatrixResult result) {

            if (!result.isValid()) {
                LOGGER.info("Matrix result is invalid, skipping processing");
                return false;
            }

            List<Map<String, Object>> sources = result.getSources();
            List<Map<String, Object>> destinations = result.getDestinations();
            List<List<Double>> distances = result.getDistances();

            boolean addedNewRoute = false;

            for (int i = 0; i < destinations.size(); i++) {
                if (sources.get(i) == null || destinations.get(i) == null ||
                        distances.get(i) == null || distances.get(i).isEmpty()) {
                    continue;
                }

                if (distances.get(i).get(0) != null) {
                    boolean added = addRouteIfUnique(sources.get(i), destinations.get(i),
                            distances.get(i).get(0), profile);

                    if (added) {
                        addedNewRoute = true;
                    }
                }
            }
            return addedNewRoute;
        }

        @SuppressWarnings("unchecked")
        private boolean addRouteIfUnique(Map<String, Object> start, Map<String, Object> end, double distance,
                String profile) {
            try {
                List<Number> startCoord = (List<Number>) start.get(LOCATION_KEY);
                List<Number> endCoord = (List<Number>) end.get(LOCATION_KEY);

                if (startCoord != null && endCoord != null && startCoord.size() >= 2 && endCoord.size() >= 2) {

                    double[] startPoint = new double[] { startCoord.get(0).doubleValue(),
                            startCoord.get(1).doubleValue() };
                    double[] endPoint = new double[] { endCoord.get(0).doubleValue(), endCoord.get(1).doubleValue() };

                    if (distance < minDistance) {
                        LOGGER.debug("Skipping route with distance {} < minimum {} meters", distance, minDistance);
                        return false;
                    }

                    Route route = new Route(startPoint, endPoint, distance, profile);
                    boolean added = routeRepository.addRouteIfUnique(route);

                    if (added) {
                        LOGGER.debug("Added new unique route for profile: {}", profile);
                    } else {
                        LOGGER.debug("Skipped duplicate route for profile: {}", profile);
                    }

                    return added;
                }
            } catch (Exception e) {
                LOGGER.error("Error adding route: {}", e.getMessage());
            }
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
