package org.heigit.ors.benchmark;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.heigit.ors.benchmark.BenchmarkEnums.DirectionsModes;
import static org.heigit.ors.benchmark.util.NameUtils.getFileNameWithoutExtension;
import org.heigit.ors.benchmark.util.SourceUtils;
import org.heigit.ors.exceptions.RequestBodyCreationException;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

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
        String scenarioName = formatScenarioName(mode, profile);
        return createDirectionScenario(scenarioName, sourceFile, config, isParallel, mode, profile)
                .injectClosed(constantConcurrentUsers(config.getNumConcurrentUsers()).during(Duration.ofSeconds(1)));
    }

    private String formatScenarioName(DirectionsModes mode, String profile) {
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

            logger.info("Scenario {}: Processing {} coordinates for profile {}", name, remainingRecords.get(),
                    profile);

            return scenario(name)
                    .asLongAs(session -> remainingRecords.get() >= 1)
                    .on(
                            feed(recordFeeder, 1)
                                    .exec(session -> {
                                        remainingRecords.getAndAdd(-1);
                                        return session;
                                    })

                                    .exec(group(groupName).on(createRequest(name, config, mode, profile))));

        } catch (IllegalStateException e) {
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
