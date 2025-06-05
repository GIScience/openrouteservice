package org.heigit.ors.benchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import org.heigit.ors.benchmark.BenchmarkEnums.MatrixModes;
import org.heigit.ors.benchmark.exceptions.RequestBodyCreationException;
import org.heigit.ors.util.SourceUtils;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
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
                .header("Accept", "application/json;charset=UTF-8")
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
     * @throws RequestBodyCreationException if JSON serialization fails or data is missing
     */
    static String createRequestBody(Session session, MatrixModes mode) {
        try {
            // 1) Retrieve the raw feeder values. Gatling will give us a List<String> for each column.
            @SuppressWarnings("unchecked")
            List<String> coordsList = (List<String>) session.get("coordinates");
            @SuppressWarnings("unchecked")
            List<String> sourcesList = (List<String>) session.get("sources");
            @SuppressWarnings("unchecked")
            List<String> destsList = (List<String>) session.get("destinations");

            // 2) Fail fast if any column is missing or empty
            if (coordsList == null || coordsList.isEmpty()) {
                throw new RequestBodyCreationException("'coordinates' field is missing or empty in session");
            }
            if (sourcesList == null || sourcesList.isEmpty()) {
                throw new RequestBodyCreationException("'sources' field is missing or empty in session");
            }
            if (destsList == null || destsList.isEmpty()) {
                throw new RequestBodyCreationException("'destinations' field is missing or empty in session");
            }

            // 3) The first element of each List<String> is the actual JSON‐style value.
            String coordinatesJson = coordsList.get(0);
            String sourcesJson     = sourcesList.get(0);
            String destsJson       = destsList.get(0);

            logger.debug(
                    "Raw CSV values → coordinatesJson: {}, sourcesJson: {}, destsJson: {}",
                    coordinatesJson, sourcesJson, destsJson
            );

            // 4) Let Jackson parse "[[lon, lat], [lon, lat], …]" into List<List<Double>>
            List<List<Double>> locations = objectMapper.readValue(
                    coordinatesJson, new TypeReference<List<List<Double>>>() {}
            );

            // 5) Similarly parse "[0, 1, 2]" into List<Integer>
            List<Integer> sources = objectMapper.readValue(
                    sourcesJson, new TypeReference<List<Integer>>() {}
            );
            List<Integer> destinations = objectMapper.readValue(
                    destsJson, new TypeReference<List<Integer>>() {}
            );

            // 6) Build the request body map and merge in any extra params from MatrixModes
            Map<String, Object> requestBody = new HashMap<>(Map.of(
                    "locations",    locations,
                    "sources",      sources,
                    "destinations", destinations
            ));
            requestBody.putAll(mode.getRequestParams());

            // 7) Serialize to JSON and return
            return objectMapper.writeValueAsString(requestBody);

        } catch (JsonProcessingException e) {
            // Jackson failed to parse or serialize
            throw new RequestBodyCreationException("Failed to serialize request body to JSON", e);
        }
    }
}
