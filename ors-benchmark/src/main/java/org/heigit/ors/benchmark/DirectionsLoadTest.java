package org.heigit.ors.benchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import org.heigit.ors.benchmark.BenchmarkEnums.DirectionsModes;
import org.heigit.ors.config.Config;
import org.heigit.ors.exceptions.RequestBodyCreationException;
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

public class DirectionsLoadTest extends AbstractLoadTest {

    static {
        logger = LoggerFactory.getLogger(DirectionsLoadTest.class);
    }

    public DirectionsLoadTest() {
        super();
    }

    @Override
    protected void logConfigInfo() {
        logger.info("Initializing DirectionsLoadTest:");
        logger.info("- Source files: {}", config.getSourceFiles());
        logger.info("- Concurrent users: {}", config.getNumConcurrentUsers());
        logger.info("- Execution mode: {}", config.isParallelExecution() ? "parallel" : "sequential");
    }

    @Override
    protected void logTestTypeInfo() {
        logger.info("Testing directions");
    }

    @Override
    protected Stream<PopulationBuilder> createScenarios(boolean isParallel) {
        return config.getDirectionsModes().stream()
                .flatMap(mode -> config.getSourceFiles().stream()
                        .flatMap(sourceFile -> mode.getProfiles().stream()
                                .map(profile -> createScenarioWithInjection(sourceFile, isParallel, mode, profile))));
    }

    private PopulationBuilder createScenarioWithInjection(String sourceFile, boolean isParallel, DirectionsModes mode, String profile) {
        String scenarioName = formatScenarioName(mode, profile, isParallel);
        return createDirectionScenario(scenarioName, sourceFile, config, mode, profile)
                .injectOpen(atOnceUsers(config.getNumConcurrentUsers()));
    }

    private String formatScenarioName(DirectionsModes mode, String profile, boolean isParallel) {
        return String.format("%s - %s - %s", isParallel ? "Parallel" : "Sequential", mode, profile);
    }

    private static ScenarioBuilder createDirectionScenario(String name, String sourceFile, Config config,
            DirectionsModes mode, String profile) {

        try {
            List<Map<String, Object>> records = csv(sourceFile).readRecords();
            List<Map<String, Object>> targetRecords = SourceUtils.getRecordsByProfile(records, profile);

            AtomicInteger remainingRecords = new AtomicInteger(targetRecords.size());

            logger.info("Scenario {}: Processing {} coordinates for profile {}", name, remainingRecords.get(),
                    profile);

            return scenario(name)
                    .feed(targetRecords.iterator(), 1)
                    .asLongAs(session -> remainingRecords.decrementAndGet() >= 0)
                    .on(exec(createRequest(name, config, mode, profile)));

        } catch (IllegalStateException e) {
            logger.error("Error building scenario: ", e);
            System.exit(1);
            return null;
        }
    }

    private static HttpRequestActionBuilder createRequest(String name, Config config, DirectionsModes mode, String profile) {
        return http(name)
                .post("/v2/directions/" + profile)
                .body(StringBody(session -> createRequestBody(session, config, mode)))
                .asJson()
                .check(status().is(200));
    }

    static String createRequestBody(Session session, Config config, DirectionsModes mode) {
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

    static List<List<Double>> createLocationsListFromArrays(Session session, Config config) {
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
