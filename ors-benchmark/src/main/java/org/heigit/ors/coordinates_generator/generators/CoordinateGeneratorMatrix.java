package org.heigit.ors.coordinates_generator.generators;

import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.heigit.ors.coordinates_generator.model.MatrixRepository;
import org.heigit.ors.coordinates_generator.model.Route;
import org.heigit.ors.coordinates_generator.service.MatrixCalculator;
import org.heigit.ors.coordinates_generator.service.RouteSnapper;
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

public class CoordinateGeneratorMatrix extends AbstractCoordinateGenerator {
    protected static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorMatrix.class);

    private static final int DEFAULT_MATRIX_SIZE = 2;
    private static final int DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final String LOCATION_KEY = "location";

    private final int numMatrices;
    private final double minDistance;
    private final Map<String, Double> maxDistanceByProfile;
    private final int numRows;
    private final int numCols;
    private final MatrixRepository matrixRepository;
    private final RouteSnapper routeSnapper;
    private final MatrixCalculator matrixCalculator;
    private final int numThreads;

    protected CoordinateGeneratorMatrix(int numRoutes, double[] extent, String[] profiles, String baseUrl,
                                        double minDistance, Map<String, Double> maxDistanceByProfile) {
        this(numRoutes, extent, profiles, baseUrl, minDistance, maxDistanceByProfile, DEFAULT_THREAD_COUNT);
    }

    public CoordinateGeneratorMatrix(int numMatrices, double[] extent, String[] profiles, String baseUrl,
                                     double minDistance, Map<String, Double> maxDistanceByProfile, int numThreads) {
        super(extent, profiles, baseUrl, "matrix");
        validateInputs(numMatrices, minDistance, numThreads);

        this.numMatrices = numMatrices;
        this.minDistance = minDistance;
        this.maxDistanceByProfile = normalizeMaxDistances(profiles, maxDistanceByProfile);
        this.numRows = 5; //TODO
        this.numCols = 5; //TODO
        this.numThreads = numThreads;
        this.matrixRepository = new MatrixRepository(Set.of(profiles));

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
            while (!matrixRepository.areAllProfilesComplete(numMatrices) &&
                    consecutiveFailedAttempts.get() < maxAttempts &&
                    shouldContinue.get()) {

                int initialTotalRoutes = matrixRepository.getTotalRouteCount();
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
                if (matrixRepository.getTotalRouteCount() == initialTotalRoutes) {
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
                .setInitialMax((long) numMatrices * profiles.length)
                .setConsumer(new DelegatingProgressBarConsumer(ProgressBarLogger.getLogger()::info))
                .build();
    }

    private List<Future<Boolean>> submitTasks(ExecutorService executor, AtomicBoolean shouldContinue) {
        List<Future<Boolean>> futures = new ArrayList<>();

        for (String profile : profiles) {
            if (!matrixRepository.isProfileComplete(profile, numMatrices)) {
                int tasksPerProfile = Math.max(1, numThreads / profiles.length);
                for (int i = 0; i < tasksPerProfile && shouldContinue.get(); i++) {
                    futures.add(executor.submit(new MatrixGenerationTask(profile, shouldContinue)));
                }
            }
        }

        return futures;
    }

    private void updateProgressBar(ProgressBar pb) {
        int totalRoutes = matrixRepository.getTotalRouteCount();
        pb.stepTo(totalRoutes);
        pb.setExtraMessage(matrixRepository.getProgressMessage());
    }

    private void finalizeProgress(ProgressBar pb, int maxAttempts, int attempts) {
        pb.stepTo(matrixRepository.getTotalRouteCount());
        if (attempts >= maxAttempts) {
            LOGGER.warn("Stopped route generation after {} attempts. Routes per profile: {}",
                    maxAttempts, matrixRepository.getProgressMessage());
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

    /**
     * We want a generated matrix to be fully connected,
     * meaning all (X/Y) pairs are reachable forwards and backwards.
     * Exemplary table below.
     *
     *     A           B           C           D
     *   +-----------+-----------+-----------+-----------+
     * 1 | 1->A      | 1->B      | 1->C      | 1->D      |
     *   | A->1      | B->1      | C->1      | D->1      |
     *   +-----------+-----------+-----------+-----------+
     * 2 | 2->A      | 2->B      | 2->C      | 2->D      |
     *   | A->2      | B->2      | C->2      | D->2      |
     *   +-----------+-----------+-----------+-----------+
     * 3 | 3->A      | 3->B      | 3->C      | 3->D      |
     *   | A->3      | B->3      | C->3      | D->3      |
     *   +-----------+-----------+-----------+-----------+
     * 4 | 4->A      | 4->B      | 4->C      | 4->D      |
     *   | A->4      | B->4      | C->4      | D->4      |
     *   +-----------+-----------+-----------+-----------+
     *
     * In a brute force approach, this would mean O(2 * X * Y) routing checks.
     * We use a chaining approach instead, which can reduce complexity to O(2 * (X + Y) + 2):
     *  If all column points are reachable (A->B->C->D)
     *  and all row points are reachable in reverse (4->3->2->1)
     *  and we find a connection (1->A)
     *  then we ensure all combinations have at least one connection by transitivity.
     *  This guarantees full bidirectional connectivity across the matrix with linear effort,
     *  leveraging structure over exhaustive search.
     *  For the backwards case, we invert the chains.
     */
    private class MatrixGenerationTask implements Callable<Boolean> {
        private final String profile;
        private final AtomicBoolean shouldContinue;

        public MatrixGenerationTask(String profile, AtomicBoolean shouldContinue) {
            this.profile = profile;
            this.shouldContinue = shouldContinue;
        }

        @Override
        public Boolean call() {
            if (!shouldContinue.get() || matrixRepository.isProfileComplete(profile, numMatrices)) {
                return false;
            }

            try {
                return generateMatricesForProfile();
            } catch (Exception e) {
                LOGGER.error("Error generating routes for profile {}: {}", profile, e.getMessage());
                return false;
            }
        }

        /**
         * Main method generating a number of matrices
         * @return Whether a matrix was successfully added to the repository
         */
        private boolean generateMatricesForProfile() {
            // Get max distance for this profile
            double effectiveMaxDistance = maxDistanceByProfile.getOrDefault(profile, Double.MAX_VALUE);
            
            //We have a double statistics gate here
            //1. We need to find numRows + numCols snappable coordinates
            //2. We need to make sure that the chains as described above for these coordinates are reachable
            //We solve problem 1 here, and we leave problem 2 to the repeated execution of the task

            int targetNum = numCols + numRows;
            int maxRetries = 10 * targetNum; //TODO this is randomly chosen
            int currRetry = 0;
            List<double[]> snappedCoordinates = new ArrayList<>();

            while (snappedCoordinates.size() < targetNum && currRetry < maxRetries) {
                List<double[]> randomCoordinate = CoordinateGeneratorHelper.randomCoordinatesInExtent(1,
                        extent);

                // Snap the coordinates to the road network
                List<double[]> snappedCoordinate = routeSnapper.snapCoordinates(randomCoordinate, profile);
                snappedCoordinates.addAll(snappedCoordinate);
            }

            if (snappedCoordinates.size() < targetNum) {
                // We did not manage to find enough snapped coordinates
                return false;
            }

            return processSnappedCoordinates(snappedCoordinates, effectiveMaxDistance);
        }

        private boolean processSnappedCoordinates(List<double[]> snappedCoordinates, double maxDistance) {
            boolean addedNewMatrix = false;
            try {
                addedNewMatrix = processCoordinatePairs(snappedCoordinates, maxDistance);
            } catch (Exception e) {
                LOGGER.error("Error processing snapped coordinates: {}", e.getMessage());
            }
            return addedNewMatrix;
        }

        private boolean processCoordinatePairs(List<double[]> snappedCoordinates, double maxDistance) {
            //TODO Add reverse

            // Get columns and rows from all (unstructured) coordinates
            List<double[]> row = snappedCoordinates.subList(0, numRows);
            List<double[]> col = snappedCoordinates.subList(numRows, numRows + numCols);

            // We check distances first as this is cheaper, but we have to ensure for all coordinates
            for (double[] rowCoord : row) {
                for (double[] colCoord : col) {
                    double distance = CoordinateGeneratorHelper.calculateHaversineDistance(rowCoord, colCoord);
                    if (distance <= maxDistance) {
                        return false;
                    }
                }
            }

            // We try building the row chain, if it fails, we cannot ensure routeability
            // We do not actually need the results
            // We route backwards
            for (int i = numRows; i > 0; i--) {
                Optional<MatrixCalculator.MatrixResult> matrixResultOpt = matrixCalculator.calculateMatrix(
                        List.of(row.get(i), row.get(i-1)), profile);
                if (matrixResultOpt.isEmpty()) {
                    return false;
                }
            }

            // Same process for column chain, but forward
            for (int i = 0; i < numCols - 1; i++) {
                Optional<MatrixCalculator.MatrixResult> matrixResultOpt = matrixCalculator.calculateMatrix(
                        List.of(col.get(i), col.get(i+1)), profile);
                if (matrixResultOpt.isEmpty()) {
                    return false;
                }
            }

            // Same process for connector col -> row
            Optional<MatrixCalculator.MatrixResult> matrixResultOpt = matrixCalculator.calculateMatrix(
                    List.of(row.get(0), col.get(0)), profile);
            if (matrixResultOpt.isEmpty()) {
                return false;
            }

            // Calculate the actual matrix
            int[] sources = java.util.stream.IntStream.range(0, numRows).toArray();
            int[] destinations = java.util.stream.IntStream.range(0, numCols).toArray();

            Optional<MatrixCalculator.MatrixResult> fullMatrixResult = matrixCalculator.calculateAsymmetricMatrix(
                    snappedCoordinates, sources, destinations, profile);

            boolean addedNewMatrix = false;
            if (fullMatrixResult.isPresent()) {
                addedNewMatrix = processMatrixResult(matrixResultOpt.get());
            }

            return addedNewMatrix;
        }


        //TODO adapt from route to matrix
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
                    boolean added = addMatrixIfUnique(sources.get(i), destinations.get(i),
                            distances.get(i).get(0), profile);

                    if (added) {
                        addedNewRoute = true;
                    }
                }
            }
            return addedNewRoute;
        }

        //TODO adapt from route to matrix
        @SuppressWarnings("unchecked")
        private boolean addMatrixIfUnique(Map<String, Object> start, Map<String, Object> end, double distance,
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
                    boolean added = matrixRepository.addRouteIfUnique(route);

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
        matrixRepository.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Route> getResult() {
        return matrixRepository.getAllRoutes(numMatrices);
    }

    @Override
    public void writeToCSV(String filePath) throws FileNotFoundException {
        matrixRepository.writeToCSV(filePath, numMatrices);
    }

    public int getNumMatrices() {
        return numMatrices;
    }
}
