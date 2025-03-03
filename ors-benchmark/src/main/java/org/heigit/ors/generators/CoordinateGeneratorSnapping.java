package org.heigit.ors.generators;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.heigit.ors.util.ProgressBarLogger;

import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CoordinateGeneratorSnapping extends AbstractCoordinateGenerator {
    private static final int DEFAULT_BATCH_SIZE = 100;

    private final int numPoints;
    private final double radius;
    private final Map<String, List<double[]>> resultsByProfile;
    private final Map<String, Set<Point>> uniquePointsByProfile;

    protected static class Point {
        final double[] coordinates;

        Point(double[] coordinates) {
            this.coordinates = coordinates;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Point point = (Point) o;
            return CoordinateHash.equals(coordinates, point.coordinates);
        }

        @Override
        public int hashCode() {
            return CoordinateHash.hash(coordinates);
        }
    }

    protected CoordinateGeneratorSnapping(int numPoints, double[] extent, double radius, String[] profiles,
            String baseUrl) {
        super(extent, profiles, baseUrl, "snap"); // Use first profile as default
        if (numPoints <= 0)
            throw new IllegalArgumentException("Number of points must be positive");
        if (radius <= 0)
            throw new IllegalArgumentException("Radius must be positive");

        this.numPoints = numPoints;
        this.radius = radius;
        this.resultsByProfile = new HashMap<>();
        this.uniquePointsByProfile = new HashMap<>();
        for (String userProfile : profiles) {
            resultsByProfile.put(userProfile, new ArrayList<>());
            uniquePointsByProfile.put(userProfile, new HashSet<>());
        }
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

        try (CloseableHttpClient client = createHttpClient();
                ProgressBar pb = pbb.build()) {

            pb.setExtraMessage("Starting...");
            generatePoints(client, pb, lastSizes, maxAttempts);

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

    private void generatePoints(CloseableHttpClient client, ProgressBar pb, Map<String, Integer> lastSizes,
            int maxAttempts) throws IOException {
        int attempts = 0;
        while (!isGenerationComplete() && attempts < maxAttempts) {
            boolean newPointsFound = processProfiles(client, lastSizes);

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

    private boolean processProfiles(CloseableHttpClient client, Map<String, Integer> lastSizes) throws IOException {
        boolean newPointsFound = false;
        for (String userProfile : profiles) {
            if (uniquePointsByProfile.get(userProfile).size() < numPoints) {
                processNextBatch(client, userProfile);
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

    @Override
    protected void processNextBatch(CloseableHttpClient client, String profile) throws IOException {
        int remainingPoints = numPoints - uniquePointsByProfile.get(profile).size();
        int currentBatchSize = Math.min(DEFAULT_BATCH_SIZE, remainingPoints);
        List<double[]> rawPoints = randomCoordinatesInExtent(currentBatchSize);

        String response = sendSnappingRequest(client, rawPoints, profile);
        if (response != null) {
            processSnappingResponse(response, profile);
        }
    }

    private String sendSnappingRequest(CloseableHttpClient client, List<double[]> points, String profile)
            throws IOException {
        HttpPost request = createSnappingRequest(points, profile);
        return client.execute(request, this::processResponse);
    }

    private HttpPost createSnappingRequest(List<double[]> points, String profile) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("locations", points);
        payload.put("radius", radius);

        HttpPost request = new HttpPost(baseUrl + "/v2/snap/" + profile);
        headers.forEach(request::addHeader);
        request.setEntity(new StringEntity(mapper.writeValueAsString(payload), ContentType.APPLICATION_JSON));
        return request;
    }

    @SuppressWarnings("unchecked")
    private void processSnappingResponse(String response, String profile) throws IOException {
        Map<String, Object> responseMap = mapper.readValue(response,
                new TypeReference<Map<String, Object>>() {
                });

        Object locationsObj = responseMap.get("locations");
        List<Map<String, Object>> locations;

        if (locationsObj instanceof List<?> list) {
            locations = list.stream()
                    .filter(item -> item instanceof Map<?, ?>)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        } else {
            locations = Collections.emptyList();
            LOGGER.warn("Response contained no valid locations array");
        }

        for (Map<String, Object> location : locations) {
            processLocation(location, profile);
            if (uniquePointsByProfile.get(profile).size() >= numPoints)
                break;
        }
    }

    private void processLocation(Map<String, Object> location, String profile) {
        if (location == null)
            return;

        @SuppressWarnings("unchecked")
        List<Number> coords = (List<Number>) location.get("location");
        if (coords != null && coords.size() >= 2) {
            double[] point = new double[] { coords.get(0).doubleValue(), coords.get(1).doubleValue() };
            Point snappedPoint = new Point(point);
            if (uniquePointsByProfile.get(profile).add(snappedPoint)) {
                resultsByProfile.get(profile).add(point);
            }
        }
    }

    @Override
    protected void writeToCSV(String filePath) throws IOException {
        try (PrintWriter pw = new PrintWriter(filePath)) {
            pw.println("longitude,latitude,profile");
            for (Map.Entry<String, List<double[]>> entry : resultsByProfile.entrySet()) {
                String userProfile = entry.getKey();
                for (double[] point : entry.getValue()) {
                    pw.printf("%f,%f,%s%n", point[0], point[1], userProfile);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> List<T> getResult() {
        List<Object[]> combined = new ArrayList<>();
        resultsByProfile.forEach((userProfile, points) -> points
                .forEach(point -> combined.add(new Object[] { point[0], point[1], userProfile })));
        return (List<T>) combined;
    }

    public static void main(String[] args) {
        try {
            CoordinateGeneratorSnappingCLI cli = new CoordinateGeneratorSnappingCLI(args);

            if (cli.hasHelp()) {
                cli.printHelp();
                return;
            }

            LOGGER.info("Creating coordinate generator for snapping...");
            CoordinateGeneratorSnapping generator = cli.createGenerator();

            LOGGER.info("Generating and snapping {} points...", generator.numPoints);
            generator.generate(DEFAULT_MAX_ATTEMPTS);

            LOGGER.info("\n");
            LOGGER.info("Writing {} snapped points to {}", generator.getResult().size(), cli.getOutputFile());
            generator.writeToCSV(cli.getOutputFile());

            LOGGER.info("Successfully snapped {} coordinate{}",
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
        System.exit(0);
    }
}
