package org.heigit.ors.benchmark;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.heigit.ors.exceptions.RequestBodyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

public class IsochronesLoadTest extends Simulation {
    private static final Logger logger = LoggerFactory.getLogger(IsochronesLoadTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final TestConfig config = new TestConfig();
    private final HttpProtocolBuilder httpProtocol = createHttpProtocol();

    private HttpProtocolBuilder createHttpProtocol() {
        return http.baseUrl(config.getBaseUrl())
                .acceptHeader("application/geo+json; charset=utf-8")
                .contentTypeHeader("application/json; charset=utf-8")
                .userAgentHeader("Gatling")
                .header("Authorization", config.getApiKey());
    }

    private static ScenarioBuilder createIsochroneScenario(String name, int locationCount, TestConfig config) {
        return scenario(name)
                .feed(initCsvFeeder(config.getSourceFile()))
                .during(Duration.ofSeconds(config.getRunTime()))
                .on(
                        group("Time Isochrones").on(
                                createIsochroneRequest(name + " (Time)", locationCount, config, RangeType.TIME)),
                        group("Distance Isochrones").on(
                                createIsochroneRequest(name + " (Distance)", locationCount, config,
                                        RangeType.DISTANCE)));
    }

    private static HttpRequestActionBuilder createIsochroneRequest(String name, int locationCount, TestConfig config,
            RangeType rangeType) {
        return http("Isochrones " + name)
                .post("/v2/isochrones/" + config.getTargetProfile())
                .body(StringBody(session -> createRequestBody(session, locationCount, config, rangeType)))
                .asJson()
                .check(status().is(200));
    }

    static String createRequestBody(Session session, int locationCount, TestConfig config, RangeType rangeType) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "locations", createLocationsList(session, locationCount, config),
                    "range_type", rangeType.getValue(),
                    "range", Collections.singletonList(Integer.valueOf(config.getRange())));

            logger.debug("Created request body with {} locations and range_type {}", locationCount, rangeType);
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RequestBodyCreationException("Failed to create request body", e);
        }
    }

    static List<List<Double>> createLocationsList(Session session, int locationCount, TestConfig config) {
        return IntStream.range(0, locationCount)
                .mapToObj(i -> List.of(
                        session.getDouble(config.getFieldLon()),
                        session.getDouble(config.getFieldLat())))
                .toList();
    }

    private static FeederBuilder<String> initCsvFeeder(String sourceFile) {
        logger.info("Initializing feeder with source file: {}", sourceFile);
        return csv(sourceFile).shuffle();
    }

    private String formatScenarioName(int querySize, boolean isParallel) {
        String executionMode = isParallel ? "Parallel" : "Sequential";
        return String.format("%s | Users (%d) | Locations (%d)",
                executionMode, config.getNumConcurrentUsers(), querySize);
    }

    private PopulationBuilder createScenarioWithInjection(int querySize, boolean isParallel) {
        String scenarioName = formatScenarioName(querySize, isParallel);
        return createIsochroneScenario(scenarioName, querySize, config)
                .injectClosed(constantConcurrentUsers(config.getNumConcurrentUsers())
                        .during(config.getRunTime()));
    }

    private void executeParallelScenarios() {
        List<PopulationBuilder> scenarios = config.getQuerySizes().stream()
                .map(querySize -> createScenarioWithInjection(querySize, true))
                .toList();

        setUp(scenarios.toArray(PopulationBuilder[]::new))
                .protocols(httpProtocol);
    }

    private void executeSequentialScenarios() {
        PopulationBuilder chainedScenario = config.getQuerySizes().stream()
                .map(querySize -> createScenarioWithInjection(querySize, false))
                .reduce(PopulationBuilder::andThen)
                .orElseThrow(() -> new IllegalStateException("No scenarios to run"));

        setUp(chainedScenario).protocols(httpProtocol);
    }

    public IsochronesLoadTest() {
        logger.info("Initializing IsochronesLoadTest:");
        logger.info("- Concurrent users: {}", config.getNumConcurrentUsers());
        logger.info("- Query sizes: {}", config.getQuerySizes());
        logger.info("- Runtime: {} seconds", config.getRunTime());
        logger.info("- Execution mode: {}", config.isParallelExecution() ? "parallel" : "sequential");

        if (config.isParallelExecution()) {
            executeParallelScenarios();
        } else {
            executeSequentialScenarios();
        }
    }

    @Override
    public void before() {
        logger.info("Starting Gatling simulation...");
        logger.info("Testing both time and distance isochrones");
    }

    @Override
    public void after() {
        logger.info("Gatling simulation completed.");
    }
}
