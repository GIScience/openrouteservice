package org.heigit.ors.benchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import org.heigit.ors.benchmark.BenchmarkEnums.MatrixModes;
import org.heigit.ors.benchmark.exceptions.RequestBodyCreationException;
import org.heigit.ors.util.SourceUtils;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Load test implementation for OpenRouteService Matrix API using Gatling
 * framework.
 * 
 * This class performs load testing on the matrix endpoint by:
 * - Reading matrix test data from CSV files containing coordinates, sources,
 * and destinations
 * - Creating HTTP requests to the /v2/matrix/{profile} endpoint
 * - Testing different matrix calculation modes and routing profiles
 * - Measuring response times and throughput under concurrent load
 * 
 * The test data is expected to be in CSV format with columns:
 * coordinates, sources, destinations, distances, profile
 */
public class MatrixAlgorithmLoadTest extends AbstractLoadTest {

    static {
        logger = LoggerFactory.getLogger(MatrixAlgorithmLoadTest.class);
    }

    /**
     * Constructs a new MatrixAlgorithmLoadTest instance.
     * Initializes the load test with configuration from the parent class.
     */
    public MatrixAlgorithmLoadTest() {
        super();
    }

    /**
     * Logs configuration information specific to matrix load testing.
     * Displays source files, concurrent users, and execution mode.
     */
    @Override
    protected void logConfigInfo() {
        logger.info("Initializing MatrixAlgorithmLoadTest:");
        logger.info("- Source files: {}", config.getSourceFiles());
        logger.info("- Concurrent users: {}", config.getNumConcurrentUsers());
        logger.info("- Execution mode: {}", config.isParallelExecution() ? "parallel" : "sequential");
    }

    /**
     * Logs the type of test being performed.
     */
    @Override
    protected void logTestTypeInfo() {
        logger.info("Testing matrix");
    }

    /**
     * Creates test scenarios for all combinations of matrix modes, source files,
     * and profiles.
     * 
     * @param isParallel whether scenarios should be executed in parallel
     * @return stream of PopulationBuilder instances for each test scenario
     */
    @Override
    protected Stream<PopulationBuilder> createScenarios(boolean isParallel) {
        return config.getMatrixModes().stream()
                .flatMap(mode -> config.getSourceFiles().stream()
                        .flatMap(sourceFile -> mode.getProfiles().stream()
                                .map(profile -> createScenarioWithInjection(sourceFile, isParallel, mode, profile))));
    }

    /**
     * Creates a single test scenario with user injection configuration.
     * 
     * @param sourceFile path to the CSV file containing test data
     * @param isParallel whether the scenario runs in parallel mode
     * @param mode       the matrix calculation mode to test
     * @param profile    the routing profile to test
     * @return PopulationBuilder configured with the specified parameters
     */
    private PopulationBuilder createScenarioWithInjection(String sourceFile, boolean isParallel, MatrixModes mode,
            String profile) {
        String scenarioName = formatScenarioName(mode, profile, isParallel);
        return createMatrixScenario(scenarioName, sourceFile, mode, profile)
                .injectOpen(atOnceUsers(config.getNumConcurrentUsers()));
    }

    /**
     * Formats a descriptive name for the test scenario.
     * 
     * @param mode       the matrix calculation mode
     * @param profile    the routing profile
     * @param isParallel whether the scenario runs in parallel
     * @return formatted scenario name string
     */
    private String formatScenarioName(MatrixModes mode, String profile, boolean isParallel) {
        return String.format("%s - %s - %s", isParallel ? "Parallel" : "Sequential", mode, profile);
    }

    /**
     * Creates a Gatling scenario for matrix load testing.
     * 
     * @param name       descriptive name for the scenario
     * @param sourceFile path to CSV file containing test coordinates
     * @param config     test configuration parameters
     * @param mode       matrix calculation mode to test
     * @param profile    routing profile to test
     * @return ScenarioBuilder configured for matrix testing
     */
    private static ScenarioBuilder createMatrixScenario(String name, String sourceFile,
            MatrixModes mode, String profile) {
        try {
            List<Map<String, Object>> records = csv(sourceFile).readRecords();
            List<Map<String, Object>> targetRecords = SourceUtils.getRecordsByProfile(records, profile);

            AtomicInteger remainingRecords = new AtomicInteger(targetRecords.size());

            logger.info("Scenario {}: Processing {} coordinates for profile {}", name, remainingRecords.get(),
                    profile);

            return scenario(name)
                    .feed(targetRecords.iterator(), 1)
                    .asLongAs(session -> remainingRecords.decrementAndGet() >= 0)
                    .on(exec(createRequest(name, mode, profile)));

        } catch (IllegalStateException e) {
            logger.error("Error building scenario: ", e);
            System.exit(1);
            return null;
        }
    }

    /**
     * Creates an HTTP request action for the matrix API endpoint.
     * 
     * @param name    request name for identification in test results
     * @param mode    matrix calculation mode
     * @param profile routing profile
     * @return HttpRequestActionBuilder configured for matrix API calls
     */
    private static HttpRequestActionBuilder createRequest(String name, MatrixModes mode,
            String profile) {
        return http(name)
                .post("/v2/matrix/" + profile)
                .body(StringBody(session -> createRequestBody(session, mode)))
                .asJson()
                .check(status().is(200));
    }

    /**
     * Creates the JSON request body for matrix API calls from CSV session data.
     * 
     * @param session Gatling session containing CSV row data
     * @param mode    matrix calculation mode providing additional parameters
     * @return JSON string representation of the request body
     * @throws RequestBodyCreationException if JSON serialization fails
     */
    static String createRequestBody(Session session, MatrixModes mode) {
        try {
            // Get the data from the CSV row
            String coordinatesStr = (String) session.get("coordinates");
            String sourcesStr = (String) session.get("sources");
            String destinationsStr = (String) session.get("destinations");

            Map<String, Object> requestBody = new java.util.HashMap<>(Map.of(
                    "locations", parseCoordinatesFromString(coordinatesStr),
                    "sources", parseIntegerArrayFromString(sourcesStr),
                    "destinations", parseIntegerArrayFromString(destinationsStr)));

            requestBody.putAll(mode.getRequestParams());
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RequestBodyCreationException("Failed to create request body", e);
        }
    }

    /**
     * Parses coordinate pairs from CSV string format to nested list structure.
     * 
     * Converts strings like "[[8.695556, 49.392701], [8.684623, 49.398284]]"
     * into List<List<Double>> format expected by the matrix API.
     * 
     * @param coordinatesStr string representation of coordinate array from CSV
     * @return list of coordinate pairs as [longitude, latitude] arrays
     * @throws RequestBodyCreationException if parsing fails or format is invalid
     */
    static List<List<Double>> parseCoordinatesFromString(String coordinatesStr) {
        try {
            if (coordinatesStr == null || coordinatesStr.trim().isEmpty()) {
                throw new RequestBodyCreationException("Coordinates string is null or empty");
            }

            // Remove quotes if present
            String cleaned = coordinatesStr.trim();
            if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
                cleaned = cleaned.substring(1, cleaned.length() - 1);
            }

            // Remove outer brackets
            cleaned = cleaned.substring(2, cleaned.length() - 2);
            String[] coordinatePairs = cleaned.split("\\], \\[");

            List<List<Double>> locations = new ArrayList<>();
            for (String pair : coordinatePairs) {
                String[] values = pair.split(", ");
                if (values.length != 2) {
                    throw new RequestBodyCreationException("Invalid coordinate pair: " + pair);
                }
                double lon = Double.parseDouble(values[0]);
                double lat = Double.parseDouble(values[1]);
                locations.add(List.of(lon, lat));
            }
            return locations;
        } catch (Exception e) {
            throw new RequestBodyCreationException("Failed to parse coordinates: " + coordinatesStr, e);
        }
    }

    /**
     * Parses integer arrays from CSV string format.
     * 
     * Converts strings like "[0, 1, 2]" into List<Integer> format
     * for sources and destinations parameters.
     * 
     * @param arrayStr string representation of integer array from CSV
     * @return list of integers
     * @throws RequestBodyCreationException if parsing fails or format is invalid
     */
    static List<Integer> parseIntegerArrayFromString(String arrayStr) {
        try {
            if (arrayStr == null || arrayStr.trim().isEmpty()) {
                throw new RequestBodyCreationException("Array string is null or empty");
            }

            // Remove quotes if present
            String cleaned = arrayStr.trim();
            if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
                cleaned = cleaned.substring(1, cleaned.length() - 1);
            }

            // Remove brackets
            cleaned = cleaned.substring(1, cleaned.length() - 1);

            if (cleaned.trim().isEmpty()) {
                return new ArrayList<>();
            }

            String[] values = cleaned.split(", ");
            List<Integer> result = new ArrayList<>();
            for (String value : values) {
                result.add(Integer.parseInt(value.trim()));
            }
            return result;
        } catch (Exception e) {
            throw new RequestBodyCreationException("Failed to parse integer array: " + arrayStr, e);
        }
    }
}
