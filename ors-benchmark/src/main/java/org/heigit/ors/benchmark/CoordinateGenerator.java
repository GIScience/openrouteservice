package org.heigit.ors.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import java.io.File;
import java.io.PrintWriter;

public class CoordinateGenerator {
    private final String baseUrl;
    private final double[] extent;
    private final int numPoints;
    private final double minDistance;
    private final double maxDistance;
    private final int maxAttempts;
    private final String profile;
    private final String url;
    private final Map<String, String> headers;
    private final Map<String, Object> result;
    private final Random random;
    ObjectMapper mapper = new ObjectMapper();
    // logger
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CoordinateGenerator.class);

    protected CoordinateGenerator(int numPoints, double[] extent, double minDistance,
            double maxDistance, int maxAttempts,
                    String profile, String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl : "http://localhost:8082/ors";
        this.extent = extent;
        this.numPoints = numPoints;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.maxAttempts = maxAttempts;
        this.profile = profile;
        this.random = new Random();

        String apiKey = "";
        if (this.baseUrl.contains("openrouteservice.org")) {
            apiKey = System.getenv("ORS_API_KEY");
            if (apiKey == null) {
                apiKey = System.getProperty("ORS_API_KEY");
            }
            if (apiKey == null) {
                throw new RuntimeException("ORS_API_KEY environment variable is not set.");
            }
        }

        this.url = String.format("%s/v2/matrix/%s", this.baseUrl, this.profile);
        this.headers = new HashMap<>();
        headers.put("accept", "application/json");
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", apiKey);

        this.result = new HashMap<>();
        result.put("to_points", new ArrayList<double[]>());
        result.put("from_points", new ArrayList<double[]>());
        result.put("existing_pairs", new HashSet<String>());
    }

    protected boolean isNewCoordinatePair(double[] fromPoint, double[] toPoint) {
        String pairKey = String.format("%.6f,%.6f,%.6f,%.6f",
                fromPoint[0], fromPoint[1], toPoint[0], toPoint[1]);
        return ((Set<String>) result.get("existing_pairs")).add(pairKey);
    }

    @SuppressWarnings("unchecked")
    protected void generatePoints() {
        int attempts = 0;
        final int batchSize = 30;

        List<double[]> fromPoints = (List<double[]>) result.get("from_points");
        List<double[]> toPoints = (List<double[]>) result.get("to_points");

        while (attempts < maxAttempts && toPoints.size() < numPoints) {
            LOGGER.debug("Processing batch with attempt {}", attempts);
            try {
                List<double[]> rawPoints = randomCoordinatesInExtent(batchSize);
                Map<String, List<double[]>> points = applyMatrix(rawPoints);

                if (points.get("to_points") != null && points.get("from_points") != null) {
                    boolean foundNewPair = false;

                    // Try to add new coordinate combinations
                    for (int i = 0; i < points.get("to_points").size(); i++) {
                        double[] fromPoint = points.get("from_points").get(i);
                        double[] toPoint = points.get("to_points").get(i);

                        if (isNewCoordinatePair(fromPoint, toPoint)) {
                            fromPoints.add(fromPoint);
                            toPoints.add(toPoint);
                            foundNewPair = true;

                            if (toPoints.size() >= numPoints) {
                                break;
                            }
                        }
                    }

                    // If no new pairs were found in this batch, consider it a failed attempt
                    if (!foundNewPair) {
                        LOGGER.warn("No new coordinate pairs found in this batch, counting as failed attempt");
                        attempts++;
                    }
                } else {
                    // Invalid response counts as a failed attempt
                    attempts++;
                }
            } catch (Exception e) {
                LOGGER.error("Error in attempt {}: {}", attempts, e.getMessage());
                attempts++;
            }

            if (attempts >= maxAttempts) {
                LOGGER.warn("Max attempts ({}) reached", maxAttempts);
            }
        }

        // Trim excess points if we somehow got more than requested
        if (toPoints.size() > numPoints) {
            toPoints.subList(numPoints, toPoints.size()).clear();
            fromPoints.subList(numPoints, fromPoints.size()).clear();
        }
    }

    protected List<double[]> randomCoordinatesInExtent(int numPoints) {
        List<double[]> points = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            double x = random.nextDouble() * (extent[2] - extent[0]) + extent[0];
            double y = random.nextDouble() * (extent[3] - extent[1]) + extent[1];
            points.add(new double[] { x, y });
        }
        return points;
    }

    protected CloseableHttpClient createHttpClient() {
        return HttpClientBuilder.create().build();
    }

    protected String processResponse(ClassicHttpResponse response) throws IOException {
        int status = response.getCode();
        if (status != HttpStatus.SC_OK) {
            throw new IOException("Request failed with status code: " + status);
        }
        HttpEntity entity = response.getEntity();
        try {
            return EntityUtils.toString(entity);
        } catch (ParseException | IOException e) {
            throw new IOException("Failed to parse response entity", e);
        } catch (NullPointerException e) {
            throw new IOException("Response entity is null", e);
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, List<double[]>> applyMatrix(List<double[]> points) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("locations", points);
        payload.put("destinations", Collections.singletonList(0));
        payload.put("id", "ors_benchmarks");
        payload.put("profile", profile);
        payload.put("metrics", Collections.singletonList("distance"));

        String jsonPayload = mapper.writeValueAsString(payload);

        Map<String, List<double[]>> matrixResults = new HashMap<>();
        matrixResults.put("from_points", new ArrayList<>());
        matrixResults.put("to_points", new ArrayList<>());

        try (CloseableHttpClient client = createHttpClient()) {
            final HttpPost httpPost = new HttpPost(url);
            headers.forEach(httpPost::addHeader);
            httpPost.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));
            LOGGER.debug("Execute request");
            String executeResults = client.execute(httpPost, this::processResponse);
            if (executeResults == null) {
                LOGGER.warn("No results from request");
                return matrixResults;
            }
            LOGGER.debug("Read values");
            Map<String, Object> responseMap = mapper.readValue(executeResults, Map.class);

            // Check for empty or invalid destinations
            LOGGER.debug("Check for empty or invalid destinations");
            List<Map<String, Object>> destinations = (List<Map<String, Object>>) responseMap.get("destinations");
            if (destinations == null || destinations.isEmpty()) {
                LOGGER.warn("No destinations found in response");
                return matrixResults;
            }

            // Check for valid location in first destination
            LOGGER.debug("Check for valid location in first destination");
            Map<String, Object> firstDestination = destinations.get(0);
            if (firstDestination == null || !firstDestination.containsKey("location")) {
                LOGGER.warn("Invalid location in first destination: {}", firstDestination);
                return matrixResults;
            }

            LOGGER.debug("Get start point");
            double[] startPoint = ((List<Number>) firstDestination.get("location")).stream()
                    .mapToDouble(Number::doubleValue)
                    .toArray();

            // Get all source points
            LOGGER.debug("Get all source points");
            List<Map<String, Object>> sources = (List<Map<String, Object>>) responseMap.get("sources");
            LOGGER.debug("Sources: {}", sources);

            if (sources == null || sources.isEmpty()) {
                LOGGER.warn("No sources found in response");
                return matrixResults;
            }

            List<double[]> sourcePoints = sources.stream()
                    .filter(Objects::nonNull) // Filter out null sources
                    .filter(source -> source.get("location") != null) // Filter out sources without location
                    .map(source -> {
                        try {
                            List<Number> location = (List<Number>) source.get("location");
                            if (location != null && location.size() >= 2) {
                                return new double[] {
                                        location.get(0).doubleValue(),
                                        location.get(1).doubleValue()
                                };
                            }
                        } catch (ClassCastException e) {
                            LOGGER.warn("Invalid location format in source: {}", source);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull) // Filter out null results
                    .collect(Collectors.toList());

            if (sourcePoints.isEmpty()) {
                LOGGER.warn("No valid source points found");
                return matrixResults;
            }

            // Get distances matrix
            LOGGER.debug("Get distances matrix");
            List<List<Number>> distances = (List<List<Number>>) responseMap.get("distances");

            if (distances == null || distances.isEmpty()) {
                LOGGER.warn("No distances found in response");
                return matrixResults;
            }

            // Filter points based on distance constraints
            List<double[]> filteredDestPoints = new ArrayList<>();
            List<double[]> filteredStartPoints = new ArrayList<>();
            LOGGER.debug("Filter points based on distance constraints");

            for (int i = 0; i < Math.min(distances.size(), sourcePoints.size()); i++) {
                List<Number> distanceRow = distances.get(i);
                if (distanceRow != null && !distanceRow.isEmpty()) {
                    // Handle null values in the distance matrix
                    Number distanceValue = distanceRow.get(0);
                    if (distanceValue != null) {
                        double distance = distanceValue.doubleValue();
                        LOGGER.debug("Distance at index {}: {}", i, distance);
                        if (distance > minDistance && distance < maxDistance) {
                            filteredDestPoints.add(sourcePoints.get(i));
                            filteredStartPoints.add(startPoint);
                        }
                    } else {
                        LOGGER.debug("Null distance value at index {}", i);
                    }
                } else {
                    LOGGER.warn("Invalid distance data at index {}", i);
                }
            }
            LOGGER.debug("Filtered points: {}", filteredDestPoints.size());
            matrixResults.put("from_points", filteredStartPoints);
            matrixResults.put("to_points", filteredDestPoints);
            return matrixResults;
        } catch (HttpHostConnectException e) {
            String message = String.format("Failed to connect to ORS instance at URL: %s", url);
            LOGGER.error(message, e);
            throw new IOException(message, e);
        } catch (IOException e) {
            LOGGER.error("Failed to execute request", e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, List<double[]>> getResult() {
        Map<String, List<double[]>> cleanResult = new HashMap<>();
        cleanResult.put("from_points", (List<double[]>) result.get("from_points"));
        cleanResult.put("to_points", (List<double[]>) result.get("to_points"));
        return cleanResult;
    }

    @SuppressWarnings("unchecked")
    protected String printToCSV(Map<String, ?> result) throws IOException {
        final CsvMapper CSV_MAPPER = new CsvMapper();

        try (StringWriter stringWriter = new StringWriter()){
            SequenceWriter sequenceWriter = CSV_MAPPER.writer().writeValues(stringWriter);
            sequenceWriter.write(Arrays.asList("from_lon", "from_lat", "to_lon", "to_lat"));

            List<double[]> fromPoints = (List<double[]>) result.get("from_points");
            List<double[]> toPoints = (List<double[]>) result.get("to_points");

            for (int i = 0; i < fromPoints.size(); i++) {
                double[] fromPoint = fromPoints.get(i);
                double[] toPoint = toPoints.get(i);
                sequenceWriter.write(Arrays.asList(fromPoint[0], fromPoint[1], toPoint[0], toPoint[1]));
            }
            sequenceWriter.close();
            return stringWriter.toString();
        }
    }

    protected void writeToCSV(String filePath) throws IOException {
        String csv = printToCSV(result);
        File csvOutputFile = new File(filePath);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            pw.print(csv);
        }
    }

    public static void main(String[] args) throws org.apache.commons.cli.ParseException {
        try {
            GeneratorCLI cli = new GeneratorCLI(args);

            if (cli.hasHelp()) {
                cli.printHelp();
                return;
            }

            LOGGER.debug("Create generator");
            CoordinateGenerator generator = cli.createGenerator();

            LOGGER.debug("Generate points");
            generator.generatePoints();
            LOGGER.debug("Write to CSV");
            generator.writeToCSV(cli.getOutputFile());

            System.out.println("Generated " + generator.getResult().get("to_points").size() + " coordinate pairs");
            System.out.println("Results written to: " + cli.getOutputFile());

        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric arguments: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error writing to output file: " + e.getMessage());
            System.exit(1);
        }
    }
}
