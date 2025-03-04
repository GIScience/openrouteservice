package org.heigit.ors.benchmark;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.heigit.ors.benchmark.BenchmarkEnums.DirectionsModes;
import org.heigit.ors.benchmark.util.SourceUtils;
import org.heigit.ors.exceptions.RequestBodyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static org.heigit.ors.benchmark.util.NameUtils.getFileNameWithoutExtension;

import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

public class DirectionsLoadTest extends Simulation {
    private static final Logger logger = LoggerFactory.getLogger(DirectionsLoadTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final TestConfig config = new TestConfig();
    private final HttpProtocolBuilder httpProtocol = http.baseUrl(config.getBaseUrl())
            .acceptHeader("application/geo+json; charset=utf-8")
            .contentTypeHeader("application/json; charset=utf-8")
            .userAgentHeader("Gatling")
            .header("Authorization", config.getApiKey());

    public DirectionsLoadTest() {
        logger.info("Initializing DirectionsLoadTest:");
        logger.info("- Source files: {}", config.getSourceFiles());
        logger.info("- Concurrent users: {}", config.getNumConcurrentUsers());
        logger.info("- Execution mode: {}", config.isParallelExecution() ? "parallel" : "sequential");

        try {
            if (config.isParallelExecution()) {
                executeParallelScenarios();
            } else {
                executeSequentialScenarios();
            }
        } catch (Exception e) {
            logger.error("Failed to initialize Gatling simulation: {}", e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void before() {
        logger.info("Starting Gatling simulation...");
        logger.info("Testing directions");
    }

    @Override
    public void after() {
        logger.info("Gatling simulation completed.");
    }

    private void executeParallelScenarios() {
        List<PopulationBuilder> scenarios = createScenarios(true).toList();
        if (scenarios.isEmpty()) throw new IllegalStateException("No scenarios to run");
        setUp(scenarios.toArray(PopulationBuilder[]::new)).protocols(httpProtocol);
    }

    private void executeSequentialScenarios() {
        PopulationBuilder chainedScenario = createScenarios(false)
                .reduce(PopulationBuilder::andThen)
                .orElseThrow(() -> new IllegalStateException("No scenarios to run"));
        setUp(chainedScenario).protocols(httpProtocol);
    }

    private Stream<PopulationBuilder> createScenarios(boolean isParallel) {
        return config.getDirectionsModes().stream()
                .flatMap(mode -> config.getSourceFiles().stream()
                        .flatMap(sourceFile -> mode.getProfiles().stream()
                                .map(profile -> createScenarioWithInjection(sourceFile, isParallel, mode, profile))));
    }

    private PopulationBuilder createScenarioWithInjection(String sourceFile, boolean isParallel, DirectionsModes mode, String profile) {
        String scenarioName = formatScenarioName(sourceFile, mode, profile);
        return createDirectionScenario(scenarioName, sourceFile, config, isParallel, mode, profile)
                .injectClosed(constantConcurrentUsers(config.getNumConcurrentUsers()).during(Duration.ofSeconds(1)));
    }

    private String formatScenarioName(String sourceFile, DirectionsModes mode, String profile) {
        return String.format("%s - %s", mode.name(), profile);
    }

    private static ScenarioBuilder createDirectionScenario(String name, String sourceFile, TestConfig config,
                                                           boolean isParallel, DirectionsModes mode, String profile) {
        String parallelOrSequential = isParallel ? "parallel" : "sequential";
        String groupName = String.format("Directions %s %s - %s - Users %s",
                parallelOrSequential, mode.name(), getFileNameWithoutExtension(sourceFile),
                config.getNumConcurrentUsers());

        try {
            List<Map<String, Object>> targetRecords = SourceUtils.getRecordsByProfile(sourceFile, profile);

            Iterator<Map<String, Object>> recordFeeder = SourceUtils.getRecordFeeder(targetRecords, config, profile);

            AtomicInteger remainingRecords = new AtomicInteger(targetRecords.size());

            logger.info("Processing {} coordinates for profile {}", remainingRecords.get(), profile);

            return scenario(name)
                    .asLongAs(session -> remainingRecords.get() >= 1)
                    .on(
                            feed(recordFeeder, 1)
                                    .exec(session -> {
                                        remainingRecords.getAndAdd(-1);
                                        return session;
                                    })

                                    .exec(group(groupName).on(createRequest(name, config, mode, profile))));

        } catch (Exception e) {
            logger.error("Error building scenario: ", e);
            System.exit(1);
            return null;
        }
    }

    private static HttpRequestActionBuilder createRequest(String name, TestConfig config, DirectionsModes mode, String profile) {
        return http(name)
                .post("/v2/directions/" + profile)
                .body(StringBody(session -> createRequestBody(session, config, mode)))
                .asJson()
                .check(status().is(200));
    }

    static String createRequestBody(Session session, TestConfig config, DirectionsModes mode) {
        try {
            Map<String, Object> requestBody = new java.util.HashMap<>(Map.of(
                    "coordinates", createLocationsListFromArrays(session, config)
            ));
            requestBody.putAll(mode.getRequestParams());
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RequestBodyCreationException("Failed to create request body", e);
        }
    }

    static List<List<Double>> createLocationsListFromArrays(Session session, TestConfig config) {
        List<List<Double>> locations = new ArrayList<>();
        try {
            Double startLon = Double.valueOf((String) session.getList(config.getFieldStartLon()).get(0));
            Double startLat = Double.valueOf((String) session.getList(config.getFieldStartLat()).get(0));
            locations.add(List.of(startLon, startLat));
            Double endLon = Double.valueOf((String) session.getList(config.getFieldEndLon()).get(0));
            Double endLat = Double.valueOf((String) session.getList(config.getFieldEndLat()).get(0));
            locations.add(List.of(endLon, endLat));
        } catch (NumberFormatException e) {
            String errorMessage = String.format("Failed to parse coordinate values in locations list at index %d. Original value could not be converted to double", locations.size());
            throw new RequestBodyCreationException("Error processing coordinates: " + errorMessage, e);
        }
        return locations;
    }
}
