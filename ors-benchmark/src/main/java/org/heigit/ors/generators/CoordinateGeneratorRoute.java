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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CoordinateGeneratorRoute extends AbstractCoordinateGenerator {
    private static final int DEFAULT_MATRIX_SIZE = 4;

    private final int numRoutes;
    private final double minDistance;
    private final List<Route> result;
    private final Set<RoutePair> uniqueRoutes;

    protected static class Route {
        final double[] start;
        final double[] end;
        final double distance;

        Route(double[] start, double[] end, double distance) {
            this.start = start;
            this.end = end;
            this.distance = distance;
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

    protected CoordinateGeneratorRoute(int numRoutes, double[] extent, String profile, String baseUrl,
            double minDistance) {
        super(extent, profile, baseUrl, "matrix");
        if (numRoutes <= 0)
            throw new IllegalArgumentException("Number of routes must be positive");
        if (minDistance < 0)
            throw new IllegalArgumentException("Minimum distance must be non-negative");

        this.numRoutes = numRoutes;
        this.minDistance = minDistance;
        this.result = new ArrayList<>();
        this.uniqueRoutes = new HashSet<>();
    }

    @Override
    protected List<double[]> randomCoordinatesInExtent(int count) {
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
        initializeCollections();
        int attempts = 0;
        int lastSize = 0;

        ProgressBarBuilder pbb = new ProgressBarBuilder()
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .setUpdateIntervalMillis(5000)
                .setTaskName("Generating routes")
                .setInitialMax(numRoutes)
                .setConsumer(new DelegatingProgressBarConsumer(ProgressBarLogger.getLogger()::info));

        try (CloseableHttpClient client = createHttpClient();
                ProgressBar pb = pbb.build()) {

            pb.setExtraMessage("Starting...");

            while (uniqueRoutes.size() < numRoutes && attempts < maxAttempts) {
                processNextBatch(client, "");

                if (uniqueRoutes.size() == lastSize) {
                    attempts++;
                    pb.setExtraMessage(String.format("Attempt %d/%d - No new routes", attempts, maxAttempts));
                    LOGGER.debug("No new routes found in attempt {}/{}", attempts, maxAttempts);
                } else {
                    pb.stepTo(Math.min(uniqueRoutes.size(), numRoutes));
                    pb.setExtraMessage(
                            String.format("Found %d unique routes", Math.min(uniqueRoutes.size(), numRoutes)));
                    attempts = 0;
                    lastSize = uniqueRoutes.size();
                }
            }

            pb.stepTo(Math.min(uniqueRoutes.size(), numRoutes));
            if (attempts >= maxAttempts) {
                LOGGER.info("\n");
                pb.setExtraMessage(String.format("Stopped after %d attempts - Found at least %d/%d routes",
                        maxAttempts, uniqueRoutes.size(), numRoutes));
                LOGGER.warn("Stopped route generation after {} attempts. Found {}/{} routes",
                        maxAttempts, uniqueRoutes.size(), numRoutes);
            }
        } catch (Exception e) {
            LOGGER.error("Error generating routes", e);
        }
    }

    @Override
    protected void initializeCollections() {
        result.clear();
        uniqueRoutes.clear();
    }

    @Override
    protected void processNextBatch(CloseableHttpClient client, String profile) throws IOException {
        List<double[]> coordinates = randomCoordinatesInExtent(DEFAULT_MATRIX_SIZE);
        String response = sendMatrixRequest(client, coordinates);
        if (response != null) {
            processMatrixResponse(response);
        }
    }

    private String sendMatrixRequest(CloseableHttpClient client, List<double[]> coordinates) throws IOException {
        HttpPost request = createMatrixRequest(coordinates);
        return client.execute(request, this::processResponse);
    }

    private HttpPost createMatrixRequest(List<double[]> coordinates) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("locations", coordinates);
        payload.put("metrics", new String[] { "distance" });

        HttpPost request = new HttpPost(url);
        headers.forEach(request::addHeader);
        request.setEntity(new StringEntity(mapper.writeValueAsString(payload), ContentType.APPLICATION_JSON));
        return request;
    }

    private void processMatrixResponse(String response) throws JsonProcessingException {
        Map<String, Object> responseMap = mapper.readValue(response, new TypeReference<Map<String, Object>>() {
        });

        List<List<Double>> distances = extractDistances(responseMap);
        List<Map<String, Object>> locations = extractLocations(responseMap, "destinations");

        if (distances == null || locations == null || locations.isEmpty()) {
            return;
        }

        processMatrixResults(distances, locations);
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

    private void processMatrixResults(List<List<Double>> distances, List<Map<String, Object>> locations) {
        int size = locations.size();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    Double distance = distances.get(i).get(j);
                    if (distance > 0) {
                        addRouteIfUnique(locations.get(i), locations.get(j), distance);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addRouteIfUnique(Map<String, Object> start, Map<String, Object> end, double distance) {
        List<Number> startCoord = (List<Number>) start.get("location");
        List<Number> endCoord = (List<Number>) end.get("location");

        if (startCoord != null && endCoord != null && startCoord.size() >= 2 && endCoord.size() >= 2) {
            double[] startPoint = new double[] { startCoord.get(0).doubleValue(), startCoord.get(1).doubleValue() };
            double[] endPoint = new double[] { endCoord.get(0).doubleValue(), endCoord.get(1).doubleValue() };

            if (distance < minDistance) {
                LOGGER.debug("Skipping route with distance {} < minimum {} meters", distance, minDistance);
                return;
            }

            RoutePair routePair = new RoutePair(startPoint, endPoint);
            if (uniqueRoutes.add(routePair)) {
                result.add(new Route(startPoint, endPoint, distance));
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> List<T> getResult() {
        return (List<T>) result.subList(0, Math.min(numRoutes, result.size()));
    }

    @Override
    protected void writeToCSV(String filePath) throws FileNotFoundException {
        // Get cleaned up results
        List<Route> cleanedRoutes = getResult();

        File csvOutputFile = new File(filePath);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            pw.println("start_longitude,start_latitude,end_longitude,end_latitude,distance");
            for (Route route : cleanedRoutes) {
                pw.printf("%f,%f,%f,%f,%.2f%n",
                                route.start[0], route.start[1],
                        route.end[0], route.end[1],
                        route.distance);
            }
        }
    }

    public static void main(String[] args) throws org.apache.commons.cli.ParseException {
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
