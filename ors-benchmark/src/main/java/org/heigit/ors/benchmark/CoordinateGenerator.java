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
    private Map<String, List<double[]>> result;
    private final Random random;
    ObjectMapper mapper = new ObjectMapper();
    // logger
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CoordinateGenerator.class);

    protected CoordinateGenerator(int numPoints, double[] extent, double minDistance,
                    double maxDistance, int maxAttempts, double radius,
            String profile, String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl : "http://localhost:8080/ors";
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
        result.put("to_points", new ArrayList<>());
        result.put("from_points", new ArrayList<>());
    }

    protected void generatePoints() {
        for (int i = 0; i < maxAttempts; i++) {
            LOGGER.info("Attempt {}", i);
            try {
                if (result.get("to_points").size() < numPoints) {
                    LOGGER.info("In attempt {}", i);
                    List<double[]> rawPoints = randomCoordinatesInExtent(numPoints);
                    Map<String, List<double[]>> points = applyMatrix(rawPoints);
                    LOGGER.info("Points: {}", points);
                    if (points.get("to_points") != null && points.get("from_points") != null) {
                        result.get("from_points").addAll(points.get("from_points"));
                        result.get("to_points").addAll(points.get("to_points"));
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to connect to ORS instance");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result.get("to_points").size() > numPoints) {
            result.get("to_points").subList(numPoints, result.get("to_points").size()).clear();
            result.get("from_points").subList(numPoints, result.get("from_points").size()).clear();
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
            LOGGER.info("Execute request");
            String executeResults = client.execute(httpPost, this::processResponse);
            if (executeResults == null) {
                return matrixResults;
            }
            LOGGER.info("Read values");
            Map<String, Object> responseMap = mapper.readValue(executeResults, Map.class);

            // Check for empty or invalid destinations
            LOGGER.info("Check for empty or invalid destinations");
            List<Map<String, Object>> destinations = (List<Map<String, Object>>) responseMap.get("destinations");
            if (destinations == null || destinations.isEmpty()) {
                return matrixResults;
            }

            // Check for valid location in first destination
            LOGGER.info("Check for valid location in first destination");
            Map<String, Object> firstDestination = destinations.get(0);
            if (firstDestination == null || !firstDestination.containsKey("location")) {
                return matrixResults;
            }

            LOGGER.info("Get start point");
            double[] startPoint = ((List<Number>) firstDestination.get("location")).stream()
                    .mapToDouble(Number::doubleValue)
                    .toArray();

            // Get all source points
            LOGGER.info("Get all source points");
            List<Map<String, Object>> sources = (List<Map<String, Object>>) responseMap.get("sources");
            LOGGER.info("Sources: {}", sources);

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
            LOGGER.info("Get distances matrix");
            List<List<Number>> distances = (List<List<Number>>) responseMap.get("distances");

            if (distances == null || distances.isEmpty()) {
                LOGGER.warn("No distances found in response");
                return matrixResults;
            }

            // Filter points based on distance constraints
            List<double[]> filteredDestPoints = new ArrayList<>();
            List<double[]> filteredStartPoints = new ArrayList<>();
            LOGGER.info("Filter points based on distance constraints");

            for (int i = 0; i < Math.min(distances.size(), sourcePoints.size()); i++) {
                List<Number> distanceRow = distances.get(i);
                if (distanceRow != null && !distanceRow.isEmpty() && distanceRow.get(0) != null) {
                    double distance = distanceRow.get(0).doubleValue();
                    LOGGER.debug("Distance at index {}: {}", i, distance);
                    if (distance > minDistance && distance < maxDistance) {
                        filteredDestPoints.add(sourcePoints.get(i));
                        filteredStartPoints.add(startPoint);
                    }
                } else {
                    LOGGER.warn("Invalid distance data at index {}", i);
                }
            }
            LOGGER.info("Filtered points: {}", filteredDestPoints.size());
            matrixResults.put("from_points", filteredStartPoints);
            matrixResults.put("to_points", filteredDestPoints);
            return matrixResults;
        }
    }

    protected Map<String, List<double[]>> getResult() {
        return result;
    }

    protected String printToCSV(Map<String, List<double[]>> result) throws IOException {
        final CsvMapper CSV_MAPPER = new CsvMapper();

        try (StringWriter stringWriter = new StringWriter()){
            SequenceWriter sequenceWriter = CSV_MAPPER.writer().writeValues(stringWriter);
            sequenceWriter.write(Arrays.asList("from_lon", "from_lat", "to_lon", "to_lat"));
            for (int i = 0; i < result.get("from_points").size(); i ++) {
                double[] fromPoint = result.get("from_points").get(i);
                double[] toPoint = result.get("to_points").get(i);
                sequenceWriter.write(Arrays.asList(fromPoint[0], fromPoint[1], toPoint[0], toPoint[1]));
            }
            sequenceWriter.close();
            String csv = stringWriter.toString();
            return csv;
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

            LOGGER.info("Create generator");
            CoordinateGenerator generator = cli.createGenerator();

            LOGGER.info("Generate points");
            generator.generatePoints();
            LOGGER.info("Write to CSV");
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
