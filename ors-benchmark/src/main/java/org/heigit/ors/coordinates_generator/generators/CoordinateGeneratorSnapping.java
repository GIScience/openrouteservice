package org.heigit.ors.coordinates_generator.generators;

import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.heigit.ors.coordinates_generator.model.Point;
import org.heigit.ors.coordinates_generator.service.CoordinateSnapper;
import org.heigit.ors.util.CoordinateGeneratorHelper;
import org.heigit.ors.util.ProgressBarLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;

public class CoordinateGeneratorSnapping extends AbstractCoordinateGenerator {
    protected static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorSnapping.class);

    private static final int DEFAULT_BATCH_SIZE = 100;

    private final int numPoints;
    private final CoordinateSnapper coordinateSnapper;
    private final Map<String, List<double[]>> resultsByProfile;
    private final Map<String, Set<Point>> uniquePointsByProfile;

    public CoordinateGeneratorSnapping(int numPoints, double[] extent, double radius, String[] profiles,
            String baseUrl) {
        super(extent, profiles, baseUrl, "snap");
        if (numPoints <= 0)
            throw new IllegalArgumentException("Number of points must be positive");
        if (radius <= 0)
            throw new IllegalArgumentException("Radius must be positive");

        this.numPoints = numPoints;
        this.resultsByProfile = new HashMap<>();
        this.uniquePointsByProfile = new HashMap<>();
        for (String userProfile : profiles) {
            resultsByProfile.put(userProfile, new ArrayList<>());
            uniquePointsByProfile.put(userProfile, new HashSet<>());
        }

        Function<HttpPost, String> requestExecutor = request -> {
            try (CloseableHttpClient client = createHttpClient()) {
                return client.execute(request, this::processResponse);
            } catch (IOException e) {
                LOGGER.debug("Error executing request: {}", e.getMessage());
                return null;
            }
        };
        Map<String, String> headers = createHeaders();
        this.coordinateSnapper = new CoordinateSnapper(baseUrl, headers, mapper, requestExecutor);
    }

    @Override
    protected void generate(int maxAttempts) {
        initializeCollections();
        Map<String, Integer> lastSizes = initializeLastSizes();

        final ProgressBarBuilder pbb = new ProgressBarBuilder()
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .setUpdateIntervalMillis(5000)
                .setTaskName("Generating snapped points")
                .setInitialMax((long) numPoints * profiles.length)
                .setConsumer(new DelegatingProgressBarConsumer(ProgressBarLogger.getLogger()::info));

        try (ProgressBar pb = pbb.build()) {

            pb.setExtraMessage("Starting...");
            generatePoints(pb, lastSizes, maxAttempts);

        } catch (Exception e) {
            LOGGER.error("Error generating points", e);
        }
    }

    private Map<String, Integer> initializeLastSizes() {
        Map<String, Integer> lastSizes = new HashMap<>();
        for (String userProfile : profiles) {
            lastSizes.put(userProfile, 0);
        }
        return lastSizes;
    }

    private void generatePoints(ProgressBar pb, Map<String, Integer> lastSizes,
            int maxAttempts) {
        int attempts = 0;
        while (!isGenerationComplete() && attempts < maxAttempts) {
            boolean newPointsFound = processProfiles(lastSizes);

            if (!newPointsFound) {
                attempts++;
                pb.setExtraMessage(String.format("Attempt %d/%d - No new points", attempts, maxAttempts));
            } else {
                updateProgress(pb);
                attempts = 0;
            }
        }

        pb.stepTo(getTotalPoints());
        if (attempts >= maxAttempts && LOGGER.isWarnEnabled()) {
            LOGGER.warn("Stopped point generation after {} attempts. Points per profile: {}",
                    maxAttempts, formatProgressMessage());
        }
    }

    private boolean processProfiles(Map<String, Integer> lastSizes) {
        boolean newPointsFound = false;
        for (String userProfile : profiles) {
            if (uniquePointsByProfile.get(userProfile).size() < numPoints) {
                processNextBatch(userProfile);
                int currentSize = uniquePointsByProfile.get(userProfile).size();

                if (currentSize > lastSizes.get(userProfile)) {
                    newPointsFound = true;
                    lastSizes.put(userProfile, currentSize);
                }
            }
        }
        return newPointsFound;
    }

    private void updateProgress(ProgressBar pb) {
        int totalPoints = getTotalPoints();
        pb.stepTo(totalPoints);
        pb.setExtraMessage(formatProgressMessage());
    }

    private int getTotalPoints() {
        return uniquePointsByProfile.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    private String formatProgressMessage() {
        return uniquePointsByProfile.entrySet().stream()
                .map(e -> String.format("%s: %d/%d", e.getKey(), e.getValue().size(), numPoints))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private boolean isGenerationComplete() {
        return uniquePointsByProfile.values().stream()
                .allMatch(points -> points.size() >= numPoints);
    }

    @Override
    protected void initializeCollections() {
        resultsByProfile.values().forEach(List::clear);
        uniquePointsByProfile.values().forEach(Set::clear);
    }

    protected void processNextBatch(String profile) {
        int remainingPoints = numPoints - uniquePointsByProfile.get(profile).size();
        int currentBatchSize = Math.min(DEFAULT_BATCH_SIZE, remainingPoints);
        List<double[]> rawPoints = CoordinateGeneratorHelper.randomCoordinatesInExtent(currentBatchSize, extent);

        List<double[]> snappedPoints = coordinateSnapper.snapCoordinates(rawPoints, profile);
        processSnappedPoints(snappedPoints, profile);
    }

    private void processSnappedPoints(List<double[]> snappedPoints, String profile) {
        for (double[] point : snappedPoints) {
            Point snappedPoint = new Point(point, profile);
            if (uniquePointsByProfile.get(profile).add(snappedPoint)) {
                resultsByProfile.get(profile).add(point);
                LOGGER.debug("Added snapped point: [{}, {}] for profile {}", point[0], point[1], profile);
            } else {
                LOGGER.debug("Skipped duplicate point: [{}, {}] for profile {}", point[0], point[1], profile);
            }

            if (uniquePointsByProfile.get(profile).size() >= numPoints) {
                break;
            }
        }
    }

    @Override
    public void writeToCSV(String filePath) throws IOException {
        try (PrintWriter pw = new PrintWriter(filePath)) {
            pw.println("longitude,latitude,profile");
            for (Map.Entry<String, List<double[]>> entry : resultsByProfile.entrySet()) {
                String userProfile = entry.getKey();
                List<double[]> points = entry.getValue();

                points.stream()
                        .limit(numPoints)
                        .forEach(point -> pw.printf(Locale.US, "%f,%f,%s%n", point[0], point[1], userProfile));
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getResult() {
        List<Object[]> combined = new ArrayList<>();
        resultsByProfile.forEach((userProfile, points) -> points.stream()
                .limit(numPoints)
                .forEach(point -> combined.add(new Object[] { point[0], point[1], userProfile })));
        return (List<T>) combined;
    }

    public int getNumPoints() {
        return numPoints;
    }
}
