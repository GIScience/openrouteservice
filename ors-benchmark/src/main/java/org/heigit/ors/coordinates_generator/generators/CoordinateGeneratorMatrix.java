package org.heigit.ors.coordinates_generator.generators;

import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.heigit.ors.coordinates_generator.model.Matrix;
import org.heigit.ors.coordinates_generator.model.MatrixRepository;
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

public class CoordinateGeneratorMatrix extends AbstractCoordinateGenerator {
    public record MatrixDimensions(int numRows, int numCols) {
    }

    protected static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorMatrix.class);

    private static final int DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final String LOCATION_KEY = "location";

    private final int numMatrices;
    private final Map<String, Double> maxDistanceByProfile;
    private final int numRows;
    private final int numCols;
    private final MatrixRepository matrixRepository;
    private final CoordinateSnapper coordinateSnapper;
    private final MatrixCalculator matrixCalculator;
    private final int numThreads;
    private final CloseableHttpClient httpClient;

    protected CoordinateGeneratorMatrix(int numMatrices, double[] extent, String[] profiles, String baseUrl,
            Map<String, Double> maxDistanceByProfile,
            MatrixDimensions matrixDimensions) {
        this(numMatrices, extent, profiles, baseUrl, maxDistanceByProfile, matrixDimensions, DEFAULT_THREAD_COUNT);
    }

    public CoordinateGeneratorMatrix(int numMatrices, double[] extent, String[] profiles, String baseUrl,
            Map<String, Double> maxDistanceByProfile,
            MatrixDimensions matrixDimensions, int numThreads) {
        super(extent, profiles, baseUrl, "matrix");
        validateInputs(numMatrices, numThreads);

        this.numMatrices = numMatrices;
        this.maxDistanceByProfile = normalizeMaxDistances(profiles, maxDistanceByProfile);
        this.numRows = matrixDimensions.numRows;
        this.numCols = matrixDimensions.numCols;
        this.numThreads = numThreads;
        this.matrixRepository = new MatrixRepository(Set.of(profiles));
        this.httpClient = createHttpClient();

        Function<HttpPost, String> requestExecutor = request -> {
            try {
                return httpClient.execute(request, this::processResponse);
            } catch (IOException e) {
                LOGGER.debug("Error executing request: {}", e.getMessage());
                return null;
            }
        };

        Map<String, String> headers = createHeaders();
        this.coordinateSnapper = new CoordinateSnapper(baseUrl, headers, mapper, requestExecutor);
        this.matrixCalculator = new MatrixCalculator(baseUrl, headers, mapper, requestExecutor);
    }

    @Override
    protected CloseableHttpClient createHttpClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(numThreads * 2); // Allow 2 connections per thread
        cm.setDefaultMaxPerRoute(numThreads); // Limit connections per host
        ConnectionConfig connConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(10))
                .build();
        cm.setDefaultConnectionConfig(connConfig);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(5))
                .setResponseTimeout(Timeout.ofSeconds(30))
                .build();

        return HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    private void validateInputs(int numRoutes, int numThreads) {
        if (numRoutes <= 0)
            throw new IllegalArgumentException("Number of routes must be positive");
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

    public void generateMatrices() {
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
            executeMatrixGeneration(executor, maxAttempts, shouldContinue, consecutiveFailedAttempts);
        } catch (Exception e) {
            LOGGER.error("Error generating routes: ", e);
        } finally {
            shutdownExecutor(executor);
        }
    }

    private void executeMatrixGeneration(ExecutorService executor, int maxAttempts,
            AtomicBoolean shouldContinue, AtomicInteger consecutiveFailedAttempts) {
        try (ProgressBar pb = createProgressBar()) {
            while (!matrixRepository.areAllProfilesComplete(numMatrices) &&
                    consecutiveFailedAttempts.get() < maxAttempts &&
                    shouldContinue.get()) {

                int initialTotalMatrices = matrixRepository.getTotalMatrixCount();
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
                if (matrixRepository.getTotalMatrixCount() == initialTotalMatrices) {
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
        int totalRoutes = matrixRepository.getTotalMatrixCount();
        pb.stepTo(totalRoutes);
        pb.setExtraMessage(matrixRepository.getProgressMessage());
    }

    private void finalizeProgress(ProgressBar pb, int maxAttempts, int attempts) {
        pb.stepTo(matrixRepository.getTotalMatrixCount());
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

        // Close HTTP client
        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.error("Error closing HTTP client", e);
        }
    }

    /**
     * We want a generated matrix to be fully connected,
     * meaning all (X/Y) pairs are reachable forwards and backwards.
     * Exemplary table below:
     *
     * <table border="1" cellpadding="4">
     *   <tr>
     *     <th></th><th>A</th><th>B</th><th>C</th><th>D</th>
     *   </tr>
     *   <tr>
     *     <th>1</th>
     *     <td>1-&gt;A<br/>A-&gt;1</td>
     *     <td>1-&gt;B<br/>B-&gt;1</td>
     *     <td>1-&gt;C<br/>C-&gt;1</td>
     *     <td>1-&gt;D<br/>D-&gt;1</td>
     *   </tr>
     *   <tr>
     *     <th>2</th>
     *     <td>2-&gt;A<br/>A-&gt;2</td>
     *     <td>2-&gt;B<br/>B-&gt;2</td>
     *     <td>2-&gt;C<br/>C-&gt;2</td>
     *     <td>2-&gt;D<br/>D-&gt;2</td>
     *   </tr>
     *   <tr>
     *     <th>3</th>
     *     <td>3-&gt;A<br/>A-&gt;3</td>
     *     <td>3-&gt;B<br/>B-&gt;3</td>
     *     <td>3-&gt;C<br/>C-&gt;3</td>
     *     <td>3-&gt;D<br/>D-&gt;3</td>
     *   </tr>
     *   <tr>
     *     <th>4</th>
     *     <td>4-&gt;A<br/>A-&gt;4</td>
     *     <td>4-&gt;B<br/>B-&gt;4</td>
     *     <td>4-&gt;C<br/>C-&gt;4</td>
     *     <td>4-&gt;D<br/>D-&gt;4</td>
     *   </tr>
     * </table>
     *
     * In a brute force approach, this would mean O(2 * X * Y) routing checks.
     * We use a chaining approach instead, which can reduce complexity to O(X + Y):
     * If all column points are reachable (A-&gt;B-&gt;C-&gt;D)
     * and all row points are reachable in reverse (4-&gt;3-&gt;2-&gt;1)
     * and we find a connection (1-&gt;A),
     * then by transitivity every combination is covered.
     * For the backwards case, we just connect the last column to the last row item.
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
         * 
         * @return Whether a matrix was successfully added to the repository
         */
        private boolean generateMatricesForProfile() {
            // Get max distance for this profile
            double effectiveMaxDistance = maxDistanceByProfile.getOrDefault(profile, Double.MAX_VALUE);

            // We have a double statistics gate here
            // 1. We need to find numRows + numCols snappable coordinates
            // 2. We need to make sure that the chains as described above for these
            // coordinates are reachable
            // We solve problem 1 here within one task, and we leave problem 2 to the
            // repeated execution of the task

            int targetNum = numCols + numRows;
            int maxRetries = 20 * targetNum; // This is a random limit; Could use improvement
            int currRetry = 0;
            List<double[]> snappedCoordinates = new ArrayList<>();

            while (snappedCoordinates.size() < targetNum && currRetry < maxRetries) {
                List<double[]> randomCoordinate = CoordinateGeneratorHelper.randomCoordinatesInExtent(1,
                        extent);

                // Snap the coordinates to the road network
                List<double[]> snappedCoordinate = coordinateSnapper.snapCoordinates(randomCoordinate, profile);
                snappedCoordinates.addAll(snappedCoordinate);
                currRetry++;
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

        /**
         * Processes a set of snapped coordinates by verifying distance and routeability
         * constraints,
         * and optionally calculating and storing the resulting distance matrix.
         * <p>
         * The method checks for:
         * <ul>
         * <li>Maximum allowed distance between row and column coordinates</li>
         * <li>Routeability in forward direction between row elements, and column
         * elements</li>
         * <li>Connectors between row and column to create circular routeability</li>
         * <li>Computes an asymmetric matrix if all conditions are satisfied</li>
         * </ul>
         *
         * @param snappedCoordinates a list of coordinates, where the first
         *                           {@code numRows} entries are treated as rows
         *                           and the next {@code numCols} as columns
         * @param maxDistance        the minimum distance required between each
         *                           row-column coordinate pair
         * @return {@code true} if a matrix was successfully computed and processed;
         *         {@code false} otherwise
         */
        private boolean processCoordinatePairs(List<double[]> snappedCoordinates, double maxDistance) {
            List<double[]> row = snappedCoordinates.subList(0, numRows);
            List<double[]> col = snappedCoordinates.subList(numRows, numRows + numCols);

            if (!hasNearbyPoints(row, col, maxDistance))
                return false;
            if (!rowsColsRouteable(row, col))
                return false;
            if (!isRouteable(row.get(0), col.get(0)))
                return false;
            if (!isRouteable(col.get(numCols - 1), row.get(numRows - 1)))
                return false;

            return computeAndProcessMatrix(snappedCoordinates);
        }

        /**
         * Checks whether any pair of coordinates between rows and columns is within the
         * specified maximum distance.
         *
         * @param row         list of row coordinates
         * @param col         list of column coordinates
         * @param maxDistance the maximum allowed proximity in meters
         * @return {@code true} if any row-column coordinate pair is too close;
         *         {@code false} otherwise
         */
        private boolean hasNearbyPoints(List<double[]> row, List<double[]> col, double maxDistance) {
            for (double[] rowCoord : row) {
                for (double[] colCoord : col) {
                    double distance = CoordinateGeneratorHelper.calculateHaversineDistance(rowCoord, colCoord);
                    if (distance <= maxDistance)
                        return true;
                }
            }
            return false;
        }

        /**
         * Verifies routeability between row elements and column elements in the forward
         * direction:
         * <ul>
         * <li>Backward traversal through row coordinates (i.e., last to first)</li>
         * <li>Forward traversal through column coordinates</li>
         * </ul>
         *
         * @param row list of row coordinates
         * @param col list of column coordinates
         * @return {@code true} if all routeability conditions are satisfied in the
         *         forward direction
         */
        private boolean rowsColsRouteable(List<double[]> row, List<double[]> col) {
            for (int i = numRows - 1; i > 0; i--) {
                if (!isRouteable(row.get(i), row.get(i - 1)))
                    return false;
            }

            for (int i = 0; i < numCols - 1; i++) {
                if (!isRouteable(col.get(i), col.get(i + 1)))
                    return false;
            }

            return true;
        }

        /**
         * Calculates the asymmetric distance matrix using the provided coordinates,
         * and processes the result if available.
         *
         * @param snappedCoordinates a combined list of row and column coordinates
         * @return {@code true} if the matrix was successfully computed and passed to
         *         {@code processMatrixResult}; {@code false} otherwise
         */
        private boolean computeAndProcessMatrix(List<double[]> snappedCoordinates) {
            int[] sources = java.util.stream.IntStream.range(0, numRows).toArray();
            int[] destinations = java.util.stream.IntStream.range(0, numCols).toArray();

            Optional<MatrixCalculator.MatrixResult> fullMatrixResult = matrixCalculator.calculateAsymmetricMatrix(
                    snappedCoordinates, sources, destinations, profile);

            return fullMatrixResult.map(this::processMatrixResult).orElse(false);
        }

        /**
         * Check whether two points are connected in a forward way
         * 
         * @param from coordinate pair from
         * @param to   coordinate pair to
         * @return true if matrix was successfully calculated and points are routeable,
         *         false otherwise
         */
        private boolean isRouteable(double[] from, double[] to) {
            Optional<MatrixCalculator.MatrixResult> matrixResultOpt = matrixCalculator.calculateMatrix(
                    List.of(from, to), profile);
            if (matrixResultOpt.isEmpty()) {
                return false;
            }
            MatrixCalculator.MatrixResult result = matrixResultOpt.get();
            return result.getSources().get(0) != null && result.getDestinations().get(0) != null;
        }

        /**
         * Processes a MatrixResult by transforming the source and destination locations
         * into the required matrix format and attempting to add it to the matrix
         * repository.
         *
         * <p>
         * It flattens source and destination coordinates into a single coordinate
         * array,
         * generates index arrays for sources and destinations, converts distance data
         * to a double[][],
         * and constructs a Matrix object. If the matrix is unique, it is added to the
         * repository.
         * </p>
         *
         * @param result the result from the matrix calculation
         * @return true if the matrix was successfully added (i.e. is unique), false
         *         otherwise
         */
        private boolean processMatrixResult(MatrixCalculator.MatrixResult result) {
            List<Map<String, Object>> rawSources = result.getSources();
            List<Map<String, Object>> rawDestinations = result.getDestinations();
            List<List<Double>> rawDistances = result.getDistances();

            int sourceCount = rawSources.size();
            int destCount = rawDestinations.size();
            int totalCount = sourceCount + destCount;

            double[][] coordinates = new double[totalCount][2];
            int[] sourceIndices = new int[sourceCount];
            int[] destinationIndices = new int[destCount];

            // Populate source coordinates and indices
            for (int i = 0; i < sourceCount; i++) {
                List<Double> loc = (List<Double>) rawSources.get(i).get(LOCATION_KEY);
                coordinates[i][0] = loc.get(0);
                coordinates[i][1] = loc.get(1);
                sourceIndices[i] = i;
            }

            // Populate destination coordinates and indices
            for (int i = 0; i < destCount; i++) {
                List<Double> loc = (List<Double>) rawDestinations.get(i).get(LOCATION_KEY);
                int index = sourceCount + i;
                coordinates[index][0] = loc.get(0);
                coordinates[index][1] = loc.get(1);
                destinationIndices[i] = index;
            }

            // Convert distances list to 2D array
            double[][] distances = rawDistances.stream()
                    .map(row -> row.stream().mapToDouble(Double::doubleValue).toArray())
                    .toArray(double[][]::new);

            try {
                Matrix matrix = new Matrix(coordinates, sourceIndices, destinationIndices, distances, profile);
                boolean added = matrixRepository.addMatrixIfUnique(matrix);

                if (added) {
                    LOGGER.debug("Added new unique route for profile: {}", profile);
                } else {
                    LOGGER.debug("Skipped duplicate route for profile: {}", profile);
                }

                return added;

            } catch (Exception e) {
                LOGGER.error("Error adding route: {}", e.getMessage(), e);
                return false;
            }
        }
    }

    @Override
    protected void initializeCollections() {
        matrixRepository.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Matrix> getResult() {
        return matrixRepository.getAllMatrices(numMatrices);
    }

    @Override
    public void writeToCSV(String filePath) throws FileNotFoundException {
        matrixRepository.writeToCSV(filePath, numMatrices);
    }

    public int getNumMatrices() {
        return numMatrices;
    }
}
