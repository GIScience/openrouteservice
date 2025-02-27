package org.heigit.ors.benchmark;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.heigit.ors.exceptions.RequestBodyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.feed;
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

    private static ScenarioBuilder createIsochroneScenario(String name, int locationCount, String sourceFile,
            TestConfig config,
            RangeType rangeType, boolean isParallel) {
        String parallelOrSequential = isParallel ? "parallel" : "sequential";
        String groupName = String.format("Isochrones %s %s - %s - Users %s - Ranges %s",
                        parallelOrSequential, rangeType.getValue(), getFileNameWithoutExtension(sourceFile),
                config.getNumConcurrentUsers(), config.getRanges());

        FeederBuilder<String> feeder = initCsvFeeder(sourceFile);
        AtomicInteger remainingRecords = new AtomicInteger(feeder.recordsCount());

        return scenario(name)
                .asLongAs(session -> remainingRecords.get() > 0)
                .on(
                        feed(feeder, locationCount)
                                .exec(session -> {
                                    remainingRecords.getAndAdd(-locationCount);
                                    return session;
                                })
                                .exec(
                        group(groupName).on(
                                                createIsochroneRequest(name, locationCount, config, rangeType))));
    }

    private static HttpRequestActionBuilder createIsochroneRequest(String name, int locationCount, TestConfig config,
            RangeType rangeType) {
        return http(name)
                .post("/v2/isochrones/" + config.getTargetProfile())
                .body(StringBody(session -> createRequestBody(session, locationCount, config, rangeType)))
                .asJson()
                .check(status().is(200));
    }

    static String createRequestBody(Session session, int locationCount, TestConfig config, RangeType rangeType) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "locations", createLocationsListFromArrays(session, locationCount, config),
                            "range_type", rangeType.getValue(),
                    "range", config.getRanges());

            logger.debug("Created request body with {} locations, {} ranges and range_type {}",
                    locationCount, config.getRanges().size(), rangeType);
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RequestBodyCreationException("Failed to create request body", e);
        }
    }

    static List<List<Double>> createLocationsListFromArrays(Session session, int locationCount, TestConfig config) {
        List<List<Double>> locations = new ArrayList<>();
        
        try {
            List<?> lons = session.get(config.getFieldLon());
            List<?> lats = session.get(config.getFieldLat());

            if (lons == null || lats == null) {
                logger.error("Longitude or latitude values are null in session");
                return locations;
            }

            int size = Math.min(Math.min(locationCount, lons.size()), lats.size());
            
            for (int i = 0; i < size; i++) {
                Object lon = lons.get(i);
                Object lat = lats.get(i);
                
                if (lon != null && lat != null) {
                    locations.add(List.of(Double.valueOf(lon.toString()),
                            Double.valueOf(lat.toString())));
                }
            }
            
            logger.debug("Created location list with {} coordinates", locations.size());
            
        } catch (NumberFormatException e) {
            String errorMessage = String.format("Failed to parse coordinate values in locations list at index %d. Original value could not be converted to double", locations.size());
            throw new RequestBodyCreationException("Error processing coordinates: " + errorMessage, e);
        }
        
        return locations;
    }

    private static FeederBuilder<String> initCsvFeeder(String sourceFile) {
        logger.info("Initializing feeder with source file: {}", sourceFile);
        return csv(sourceFile).circular();
    }

    private static String getFileNameWithoutExtension(String filePath) {
        String fileName = new File(filePath).getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }

    private String formatScenarioName(String sourceFile, int querySize) {

        String fileName = getFileNameWithoutExtension(sourceFile);
        return String.format("Locations (%d) | %s", querySize, fileName);
    }

    private PopulationBuilder createScenarioWithInjection(String sourceFile, int querySize, boolean isParallel,
            RangeType rangeType) {
        String scenarioName = formatScenarioName(sourceFile, querySize);
        return createIsochroneScenario(scenarioName, querySize, sourceFile, config, rangeType, isParallel)
                .injectClosed(constantConcurrentUsers(config.getNumConcurrentUsers()).during(Duration.ofSeconds(1)));
    }

    private void executeParallelScenarios() {
        List<PopulationBuilder> scenarios = createScenariosForTestUnit().stream()
                .flatMap(rangeType -> config.getSourceFiles().stream()
                        .flatMap(sourceFile -> config.getQuerySizes().stream()
                                .map(querySize -> createScenarioWithInjection(sourceFile, querySize, true, rangeType))))
                .toList();

        setUp(scenarios.toArray(PopulationBuilder[]::new))
                .protocols(httpProtocol);
    }

    private void executeSequentialScenarios() {
        PopulationBuilder chainedScenario = createScenariosForTestUnit().stream()
                .flatMap(rangeType -> config.getSourceFiles().stream()
                        .flatMap(sourceFile -> config.getQuerySizes().stream()
                                .map(querySize -> createScenarioWithInjection(sourceFile, querySize, false,
                                        rangeType))))
                .reduce(PopulationBuilder::andThen)
                .orElseThrow(() -> new IllegalStateException("No scenarios to run"));

        setUp(chainedScenario).protocols(httpProtocol);
    }

    private List<RangeType> createScenariosForTestUnit() {
        return switch (config.getTestUnit()) {
            case DISTANCE -> List.of(RangeType.DISTANCE);
            case TIME -> List.of(RangeType.TIME);
        };
    }

    public IsochronesLoadTest() {
        logger.info("Initializing IsochronesLoadTest:");
        logger.info("- Source files: {}", config.getSourceFiles());
        logger.info("- Concurrent users: {}", config.getNumConcurrentUsers());
        logger.info("- Query sizes: {}", config.getQuerySizes());
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
        logger.info("Testing {} isochrones", config.getTestUnit());
    }

    @Override
    public void after() {
        logger.info("Gatling simulation completed.");
    }
}
