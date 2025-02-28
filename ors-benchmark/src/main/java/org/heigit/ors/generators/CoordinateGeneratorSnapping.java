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
    private final List<double[]> result;
    private final Set<Point> uniquePoints;

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

    protected CoordinateGeneratorSnapping(int numPoints, double[] extent, double radius, String profile,
            String baseUrl) {
        super(extent, profile, baseUrl, "snap");
        if (numPoints <= 0)
            throw new IllegalArgumentException("Number of points must be positive");
        if (radius <= 0)
            throw new IllegalArgumentException("Radius must be positive");

        this.numPoints = numPoints;
        this.radius = radius;
        this.result = new ArrayList<>();
        this.uniquePoints = new HashSet<>();
    }

    @Override
    protected void generate(int maxAttempts) {
        initializeCollections();
        int attempts = 0;
        int lastSize = 0;

        ProgressBarBuilder pbb = new ProgressBarBuilder()
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .setUpdateIntervalMillis(5000)
                .setTaskName("Generating snapped points")
                .setInitialMax(numPoints)
                .setConsumer(new DelegatingProgressBarConsumer(ProgressBarLogger.getLogger()::info));

        try (CloseableHttpClient client = createHttpClient();
                ProgressBar pb = pbb.build()) {

            pb.setExtraMessage("Starting...");

            while (uniquePoints.size() < numPoints && attempts < maxAttempts) {
                processNextBatch(client);

                if (uniquePoints.size() == lastSize) {
                    attempts++;
                    pb.setExtraMessage(String.format("Attempt %d/%d - No new points", attempts, maxAttempts));
                    LOGGER.debug("No new points found in attempt {}/{}", attempts, maxAttempts);
                } else {
                    pb.stepTo(uniquePoints.size());
                    pb.setExtraMessage(String.format("Found %d unique points", uniquePoints.size()));
                    attempts = 0;
                    lastSize = uniquePoints.size();
                }
            }

            pb.stepTo(uniquePoints.size());
            if (attempts >= maxAttempts) {
                pb.setExtraMessage(String.format("Stopped after %d attempts - Found %d/%d points",
                        maxAttempts, uniquePoints.size(), numPoints));
                LOGGER.warn("Stopped point generation after {} attempts. Found {}/{} points",
                        maxAttempts, uniquePoints.size(), numPoints);
            }
        } catch (Exception e) {
            LOGGER.error("Error generating points", e);
        }
    }

    @Override
    protected void initializeCollections() {
        result.clear();
        uniquePoints.clear();
    }

    @Override
    protected void processNextBatch(CloseableHttpClient client) throws IOException {
        int remainingPoints = numPoints - uniquePoints.size();
        int currentBatchSize = Math.min(DEFAULT_BATCH_SIZE, remainingPoints);
        List<double[]> rawPoints = randomCoordinatesInExtent(currentBatchSize);

        String response = sendSnappingRequest(client, rawPoints);
        if (response != null) {
            processSnappingResponse(response);
        }
    }

    private String sendSnappingRequest(CloseableHttpClient client, List<double[]> points) throws IOException {
        HttpPost request = createSnappingRequest(points);
        return client.execute(request, this::processResponse);
    }

    private HttpPost createSnappingRequest(List<double[]> points) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("locations", points);
        payload.put("radius", radius);

        HttpPost request = new HttpPost(url);
        headers.forEach(request::addHeader);
        request.setEntity(new StringEntity(mapper.writeValueAsString(payload), ContentType.APPLICATION_JSON));
        return request;
    }

    @SuppressWarnings("unchecked")
    private void processSnappingResponse(String response) throws IOException {
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
            processLocation(location);
            if (uniquePoints.size() >= numPoints)
                break;
        }
    }

    private void processLocation(Map<String, Object> location) {
        if (location == null)
            return;

        @SuppressWarnings("unchecked")
        List<Number> coords = (List<Number>) location.get("location");
        if (coords != null && coords.size() >= 2) {
            double[] point = new double[] { coords.get(0).doubleValue(), coords.get(1).doubleValue() };
            Point snappedPoint = new Point(point);
            if (uniquePoints.add(snappedPoint)) {
                result.add(point);
            }
        }
    }

    @Override
    protected void writeToCSV(String filePath) throws IOException {
        try (PrintWriter pw = new PrintWriter(filePath)) {
            pw.println("longitude,latitude");
            for (double[] point : result) {
                pw.printf("%f,%f%n", point[0], point[1]);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> List<T> getResult() {
        return (List<T>) new ArrayList<>(result);
    }

    public static void main(String[] args) throws org.apache.commons.cli.ParseException {
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
