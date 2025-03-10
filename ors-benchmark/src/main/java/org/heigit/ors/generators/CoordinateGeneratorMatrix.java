package org.heigit.ors.generators;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;
import org.heigit.ors.service.RouteSnapper;
import org.heigit.ors.util.CoordinateGeneratorHelper;
import org.heigit.ors.util.ProgressBarLogger;

import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

public class CoordinateGeneratorMatrix extends AbstractCoordinateGenerator {
    private static final int DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    
    private final int numLists;
    private final int pointsPerList;
    private final Map<String, Double> maxDistanceByProfile;
    private final Map<String, List<CoordinateList>> coordinateListsByProfile;
    private final RouteSnapper routeSnapper;
    private final int numThreads;

    public static class CoordinateList {
        private final String profile;
        private final List<double[]> coordinates;
        
        public CoordinateList(String profile, List<double[]> coordinates) {
            this.profile = profile;
            this.coordinates = new ArrayList<>(coordinates);
        }
        
        public String getProfile() {
            return profile;
        }
        
        public List<double[]> getCoordinates() {
            return coordinates;
        }
        
        public int size() {
            return coordinates.size();
        }
    }

    public CoordinateGeneratorMatrix(int numLists, int pointsPerList, double[] extent, 
            String[] profiles, String baseUrl, Map<String, Double> maxDistanceByProfile) {
        this(numLists, pointsPerList, extent, profiles, baseUrl, maxDistanceByProfile, DEFAULT_THREAD_COUNT);
    }

    public CoordinateGeneratorMatrix(int numLists, int pointsPerList, double[] extent, String[] profiles, 
            String baseUrl, Map<String, Double> maxDistanceByProfile, int numThreads) {
        super(extent, profiles, baseUrl, "matrix");
        validateInputs(numLists, pointsPerList, numThreads);

        this.numLists = numLists;
        this.pointsPerList = pointsPerList;
        this.maxDistanceByProfile = normalizeMaxDistances(profiles, maxDistanceByProfile);
        this.numThreads = numThreads;
        this.coordinateListsByProfile = new HashMap<>();
        
        for (String profile : profiles) {
            coordinateListsByProfile.put(profile, new ArrayList<>());
        }

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
    }

    private void validateInputs(int numLists, int pointsPerList, int numThreads) {
        if (numLists <= 0)
            throw new IllegalArgumentException("Number of lists must be positive");
        if (pointsPerList <= 1)
            throw new IllegalArgumentException("Number of points per list must be at least 2");
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

    @Override
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
            return null;
        }

        try {
            return EntityUtils.toString(entity);
        } catch (ParseException | IOException e) {
            throw new IOException("Failed to parse response entity", e);
        }
    }

    @Override
    public void generate() {
        generate(DEFAULT_MAX_ATTEMPTS);
    }

    @Override
    public void generate(int maxAttempts) {
        LOGGER.info("Starting coordinate list generation with {} threads and max attempts: {}", numThreads, maxAttempts);
        initializeCollections();

        ExecutorService executor = null;
        AtomicBoolean shouldContinue = new AtomicBoolean(true);

        try {
            executor = Executors.newFixedThreadPool(numThreads);
            executeCoordinateListGeneration(executor, maxAttempts, shouldContinue);
        } catch (Exception e) {
            LOGGER.error("Error generating coordinate lists: ", e);
        } finally {
            shutdownExecutor(executor);
        }
    }

    private void executeCoordinateListGeneration(ExecutorService executor, int maxAttempts,
            AtomicBoolean shouldContinue) {

        AtomicInteger consecutiveFailedAttempts = new AtomicInteger(0);

        try (ProgressBar pb = createProgressBar()) {
            while (!areAllProfilesComplete() && 
                    consecutiveFailedAttempts.get() < maxAttempts &&
                    shouldContinue.get()) {

                int initialTotalLists = getTotalListCount();
                List<Future<Boolean>> futures = submitTasks(executor, shouldContinue);

                // Wait for all tasks to complete
                for (Future<Boolean> future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        shouldContinue.set(false);
                        LOGGER.warn("Coordinate list generation interrupted");
                    } catch (ExecutionException e) {
                        LOGGER.error("Error in worker task: {}", e.getCause().getMessage());
                    }
                }

                updateProgressBar(pb);

                // Check if we made progress in this iteration
                if (getTotalListCount() == initialTotalLists) {
                    int attempts = consecutiveFailedAttempts.incrementAndGet();
                    pb.setExtraMessage(String.format("Attempt %d/%d - No new lists", attempts, maxAttempts));
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
                .setTaskName("Generating coordinate lists")
                .setInitialMax((long) numLists * profiles.length)
                .setConsumer(new DelegatingProgressBarConsumer(ProgressBarLogger.getLogger()::info))
                .build();
    }

    private List<Future<Boolean>> submitTasks(ExecutorService executor, AtomicBoolean shouldContinue) {
        List<Future<Boolean>> futures = new ArrayList<>();
        int tasksPerProfile = Math.max(1, numThreads / profiles.length);

        for (String profile : profiles) {
            if (!isProfileComplete(profile)) {
                for (int i = 0; i < tasksPerProfile && shouldContinue.get(); i++) {
                    futures.add(executor.submit(new CoordinateListGenerationTask(profile, shouldContinue)));
                }
            }
        }

        return futures;
    }

    private void updateProgressBar(ProgressBar pb) {
        int totalLists = getTotalListCount();
        pb.stepTo(totalLists);
        pb.setExtraMessage(getProgressMessage());
    }

    private void finalizeProgress(ProgressBar pb, int maxAttempts, int attempts) {
        pb.stepTo(getTotalListCount());
        if (attempts >= maxAttempts) {
            LOGGER.warn("Stopped coordinate list generation after {} attempts. Lists per profile: {}",
                    maxAttempts, getProgressMessage());
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

    private class CoordinateListGenerationTask implements Callable<Boolean> {
        private final String profile;
        private final AtomicBoolean shouldContinue;

        public CoordinateListGenerationTask(String profile, AtomicBoolean shouldContinue) {
            this.profile = profile;
            this.shouldContinue = shouldContinue;
        }

        @Override
        public Boolean call() {
            if (!shouldContinue.get() || isProfileComplete(profile)) {
                return false;
            }

            try {
                return generateCoordinateListForProfile();
            } catch (Exception e) {
                LOGGER.error("Error generating coordinate list for profile {}: {}", profile, e.getMessage());
                return false;
            }
        }

        private boolean generateCoordinateListForProfile() {
            // Step 1: Generate random center point within extent
            List<double[]> centerPoints = CoordinateGeneratorHelper.randomCoordinatesInExtent(1, extent);
            if (centerPoints.isEmpty()) {
                return false;
            }

            // Step 2: Snap the center point to the road network
            List<double[]> snappedCenterPoints = routeSnapper.snapCoordinates(centerPoints, profile);
            if (snappedCenterPoints.isEmpty()) {
                return false;
            }
            
            double[] snappedCenter = snappedCenterPoints.get(0);
            double maxDistance = maxDistanceByProfile.get(profile);
            
            // Step 3: Generate additional points within the radius
            List<double[]> additionalPoints = new ArrayList<>();
            for (int i = 0; i < pointsPerList - 1; i++) {
                double[] point = CoordinateGeneratorHelper.randomCoordinateInRadiusAndExtent(
                        snappedCenter, maxDistance, extent);
                if (point.length > 0) {
                    additionalPoints.add(point);
                }
            }
            
            if (additionalPoints.isEmpty()) {
                return false;
            }
            
            // Step 4: Snap additional points
            List<double[]> snappedAdditionalPoints = routeSnapper.snapCoordinates(additionalPoints, profile);
            if (snappedAdditionalPoints.isEmpty()) {
                return false;
            }
            
            // Step 5: Create and store the coordinate list
            List<double[]> allPoints = new ArrayList<>();
            allPoints.add(snappedCenter);
            allPoints.addAll(snappedAdditionalPoints);
            
            // Only add the list if we have enough points
            if (allPoints.size() == pointsPerList) {
                synchronized (coordinateListsByProfile) {
                    List<CoordinateList> lists = coordinateListsByProfile.get(profile);
                    if (lists.size() < numLists) {
                        lists.add(new CoordinateList(profile, allPoints));
                        LOGGER.debug("Added new coordinate list for profile {}: {} points", 
                                profile, allPoints.size());
                        return true;
                    }
                }
            }
            
            return false;
        }
    }

    private boolean isProfileComplete(String profile) {
        return coordinateListsByProfile.get(profile).size() >= numLists;
    }
    
    private boolean areAllProfilesComplete() {
        for (String profile : profiles) {
            if (!isProfileComplete(profile)) {
                return false;
            }
        }
        return true;
    }
    
    private int getTotalListCount() {
        int total = 0;
        for (List<CoordinateList> lists : coordinateListsByProfile.values()) {
            total += lists.size();
        }
        return total;
    }
    
    private String getProgressMessage() {
        StringBuilder sb = new StringBuilder();
        for (String profile : profiles) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(profile).append(": ").append(coordinateListsByProfile.get(profile).size())
                .append("/").append(numLists);
        }
        return sb.toString();
    }

    @Override
    protected void initializeCollections() {
        for (String profile : profiles) {
            coordinateListsByProfile.put(profile, new ArrayList<>());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<CoordinateList> getResult() {
        List<CoordinateList> allLists = new ArrayList<>();
        for (List<CoordinateList> lists : coordinateListsByProfile.values()) {
            allLists.addAll(lists);
        }
        return allLists;
    }

    @Override
    protected void writeToCSV(String filePath) throws FileNotFoundException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header with new distance column
            writer.write("profile,distance,coordinates\n");
    
            // Write coordinate lists
            for (String profile : profiles) {
                List<CoordinateList> lists = coordinateListsByProfile.get(profile);
                double maxDistance = maxDistanceByProfile.get(profile);
                
                for (CoordinateList list : lists) {
                    StringBuilder coordsBuilder = new StringBuilder();
                    coordsBuilder.append("[");

                    // Append each coordinate as [lon,lat]
                    boolean isFirst = true;
                    for (double[] coord : list.getCoordinates()) {
                        if (!isFirst) {
                            coordsBuilder.append(",");
                        }
                        coordsBuilder.append("[").append(String.format("%.6f", coord[0]))
                                .append(",").append(String.format("%.6f", coord[1])).append("]");
                        isFirst = false;
                    }

                    coordsBuilder.append("]");

                    // Write the line with profile, distance, and coordinate array
                    writer.write(String.format("%s,%.1f,\"%s\"%n", profile, maxDistance, coordsBuilder.toString()));
                }
            }
        } catch (IOException e) {
            throw new FileNotFoundException("Failed to write to file: " + e.getMessage());
        }
    }
}
