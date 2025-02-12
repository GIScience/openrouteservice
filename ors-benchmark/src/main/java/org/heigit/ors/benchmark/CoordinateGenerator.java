package org.heigit.ors.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class CoordinateGenerator {
    private final String baseUrl;
    private final double[] extent;
    private final int numPoints;
    private final double minDistance;
    private final double maxDistance;
    private final int maxAttempts;
    private final double radius;
    private final String profile;
    private final String url;
    private final Map<String, String> headers;
    private final Map<String, List<double[]>> result;
    private final Random random;
    ObjectMapper mapper = new ObjectMapper();

    protected CoordinateGenerator(int numPoints, double[] extent, double minDistance,
                    double maxDistance, int maxAttempts, double radius,
            String profile, String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl : "http://localhost:8082/ors";
        this.extent = extent;
        this.numPoints = numPoints;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.maxAttempts = maxAttempts;
        this.radius = radius;
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
            while (result.get("to_points").size() < numPoints) {
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

        try (CloseableHttpClient client = createHttpClient()) {
            HttpPost httpPost = new HttpPost(url);
            headers.forEach(httpPost::addHeader);
            httpPost.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                String responseContent = new String(response.getEntity().getContent().readAllBytes());
                Map<String, Object> responseMap = mapper.readValue(responseContent, Map.class);

                // Get the start point (first destination)
                List<Map<String, Object>> destinations = (List<Map<String, Object>>) responseMap.get("destinations");
                double[] startPoint = ((List<Number>) destinations.get(0).get("location")).stream()
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

                Map<String, List<double[]>> result = new HashMap<>();
                result.put("from_points", filteredStartPoints);
                result.put("to_points", filteredDestPoints);
                return result;
            }
        }
    }

    public Map<String, List<double[]>> getResult() {
        return result;
    }

    public static void main(String[] args) {
        double[] extent = { 8.6286, 49.3590, 8.7957, 49.4715 };
        int n = 100;
        double minDist = 10000;
        double maxDist = 1000000;
        double snapRadius = 10;
        int maxAttempts = 1000000;
        String profile = "driving-car";

        CoordinateGenerator generator = new CoordinateGenerator(
                n, extent, minDist, maxDist, maxAttempts, snapRadius, profile, null);

        // Note: CSV writing implementation needed here
    }
}
