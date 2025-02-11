package org.heigit.ors.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.util.*;

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

    public CoordinateGenerator(int numPoints, double[] extent, double minDistance,
            double maxDistance, int maxAttempts, double radius,
            String profile, String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl : "http://localhost:8080/ors";
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

        generatePoints();
    }

    private void generatePoints() {
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

    public List<double[]> randomCoordinatesInExtent(int numPoints) {
        List<double[]> points = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            double x = random.nextDouble() * (extent[2] - extent[0]) + extent[0];
            double y = random.nextDouble() * (extent[3] - extent[1]) + extent[1];
            points.add(new double[] { x, y });
        }
        return points;
    }

    private Map<String, List<double[]>> applyMatrix(List<double[]> points) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("locations", points);
        payload.put("destinations", Collections.singletonList(0));
        payload.put("id", "ors_benchmarks");
        payload.put("profile", profile);
        payload.put("metrics", Collections.singletonList("distance"));

        ObjectMapper mapper = new ObjectMapper();
        String jsonPayload = mapper.writeValueAsString(payload);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            headers.forEach(httpPost::setHeader);
            httpPost.setEntity(new StringEntity(jsonPayload));

            // Process response and return results
            // Note: Implementation details omitted for brevity
            // You'll need to process the HTTP response and create appropriate return values
            return new HashMap<>(); // Placeholder
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
