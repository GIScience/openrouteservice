package org.heigit.ors.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CoordinateGeneratorSnapping {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorSnapping.class);
    private final String baseUrl;
    private final double[] extent;
    private final int numPoints;
    private final double radius;
    private final String profile;
    private final String url;
    private final Map<String, String> headers;
    private final List<double[]> result;
    private final Random random;
    private final ObjectMapper mapper;

    protected CoordinateGeneratorSnapping(int numPoints, double[] extent, double radius, String profile, String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl : "http://localhost:8082/ors";
        this.extent = extent;
        this.numPoints = numPoints;
        this.radius = radius;
        this.profile = profile;
        this.random = new Random();
        this.mapper = new ObjectMapper();
        this.result = new ArrayList<>();

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

        this.url = String.format("%s/v2/snap/%s", this.baseUrl, this.profile);
        this.headers = new HashMap<>();
        headers.put("accept", "application/json");
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", apiKey);
    }

    protected List<double[]> randomCoordinatesInExtent(int batchSize) {
        List<double[]> points = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
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
        } catch (NullPointerException e) {
            throw new IOException("Response entity is null", e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void generatePoints() {
        final int batchSize = 100;
        result.clear();

        try (CloseableHttpClient client = createHttpClient()) {
            while (result.size() < numPoints) {
                List<double[]> rawPoints = randomCoordinatesInExtent(Math.min(batchSize, numPoints - result.size()));
                
                Map<String, Object> payload = new HashMap<>();
                payload.put("locations", rawPoints);
                payload.put("radius", radius);

                HttpPost httpPost = new HttpPost(url);
                headers.forEach(httpPost::addHeader);
                httpPost.setEntity(new StringEntity(mapper.writeValueAsString(payload), ContentType.APPLICATION_JSON));

                String response = client.execute(httpPost, this::processResponse);

                if (response != null) {
                    Map<String, Object> responseMap = mapper.readValue(response, Map.class);
                    List<Map<String, Object>> locations = (List<Map<String, Object>>) responseMap.get("locations");
                    
                    if (locations != null) {
                        for (Map<String, Object> location : locations) {
                            List<Number> coords = (List<Number>) location.get("location");
                            if (coords != null && coords.size() >= 2) {
                                result.add(new double[]{
                                    coords.get(0).doubleValue(),
                                    coords.get(1).doubleValue()
                                });
                                if (result.size() >= numPoints) break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error generating points", e);
        }
    }

    protected List<double[]> getResult() {
        return new ArrayList<>(result);
    }

    protected void writeToCSV(String filePath) throws IOException {
        File csvOutputFile = new File(filePath);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            pw.println("longitude,latitude");
            for (double[] point : result) {
                pw.printf("%f,%f%n", point[0], point[1]);
            }
        }
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
            generator.generatePoints();

            LOGGER.info("Writing {} snapped points to {}", generator.getResult().size(), cli.getOutputFile());
            generator.writeToCSV(cli.getOutputFile());

            int generatedPoints = generator.getResult().size();
            System.out.printf("%nSuccessfully snapped %d coordinate%s%n",
                    generatedPoints, generatedPoints != 1 ? "s" : "");
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
