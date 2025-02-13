package org.heigit.ors.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
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
            if (result.get("to_points").size() < numPoints) {
                List<double[]> rawPoints = randomCoordinatesInExtent(5);
                try {
                    Map<String, List<double[]>> points = applyMatrix(rawPoints);
                    if (points.get("to_points") != null && points.get("from_points") != null) {
                        result.get("from_points").addAll(points.get("from_points"));
                        result.get("to_points").addAll(points.get("to_points"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
        return HttpClients.createDefault();
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

        // Create empty result for invalid responses
        Map<String, List<double[]>> emptyResult = new HashMap<>();
        emptyResult.put("from_points", new ArrayList<>());
        emptyResult.put("to_points", new ArrayList<>());

        try (CloseableHttpClient client = createHttpClient()) {
            HttpPost httpPost = new HttpPost(url);
            headers.forEach(httpPost::addHeader);
            httpPost.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                if (response == null || response.getEntity() == null) {
                    return emptyResult;
                }

                String responseContent = new String(response.getEntity().getContent().readAllBytes());
                if (responseContent == null || responseContent.isEmpty()) {
                    return emptyResult;
                }

                Map<String, Object> responseMap = mapper.readValue(responseContent, Map.class);
                if (responseMap == null) {
                    return emptyResult;
                }

                // Check for empty or invalid destinations
                List<Map<String, Object>> destinations = (List<Map<String, Object>>) responseMap.get("destinations");
                if (destinations == null || destinations.isEmpty()) {
                    return emptyResult;
                }

                // Check for valid location in first destination
                Map<String, Object> firstDestination = destinations.get(0);
                if (firstDestination == null || !firstDestination.containsKey("location")) {
                    return emptyResult;
                }

                double[] startPoint = ((List<Number>) firstDestination.get("location")).stream()
                        .mapToDouble(Number::doubleValue)
                        .toArray();

                // Get all source points
                List<Map<String, Object>> sources = (List<Map<String, Object>>) responseMap.get("sources");
                List<double[]> sourcePoints = sources.stream()
                        .map(source -> ((List<Number>) source.get("location")).stream()
                                .mapToDouble(Number::doubleValue)
                                .toArray())
                        .collect(Collectors.toList());

                // Get distances matrix
                List<List<Number>> distances = (List<List<Number>>) responseMap.get("distances");

                // Filter points based on distance constraints
                List<double[]> filteredDestPoints = new ArrayList<>();
                List<double[]> filteredStartPoints = new ArrayList<>();

                for (int i = 0; i < distances.size(); i++) {
                    double distance = distances.get(i).get(0).doubleValue();
                    if (distance > minDistance && distance < maxDistance) {
                        filteredDestPoints.add(sourcePoints.get(i));
                        filteredStartPoints.add(startPoint);
                    }
                }

                result.put("from_points", filteredStartPoints);
                result.put("to_points", filteredDestPoints);
                return result;
            }
        }
    }

    protected Map<String, List<double[]>> getResult() {
        return result;
    }

    protected String printToCSV(Map<String, List<double[]>> result) throws IOException {
        final CsvMapper CSV_MAPPER = new CsvMapper();

        try (StringWriter stringWriter = new StringWriter()){
            SequenceWriter sequenceWriter = CSV_MAPPER.writer().writeValues(stringWriter);
            sequenceWriter.write(Arrays.asList("from_lat", "from_lon", "to_lat", "to_lon"));
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

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: CoordinateGenerator <output_filepath>");
            System.exit(1);
        }
        String outputFilePath = args[0];

        double[] extent = { 8.6286, 49.3590, 8.7957, 49.4715 };
        int n = 100;
        double minDist = 10000;
        double maxDist = 1000000;
        double snapRadius = 10;
        int maxAttempts = 1000000;
        String profile = "driving-car";

        CoordinateGenerator generator = new CoordinateGenerator(
                n, extent, minDist, maxDist, maxAttempts, snapRadius, profile, null);

        generator.generatePoints();
        try {
            generator.writeToCSV(outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
