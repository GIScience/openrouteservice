package org.heigit.ors.benchmark;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.heigit.ors.benchmark.BenchmarkEnums.RangeType;
import org.heigit.ors.benchmark.util.IteratorUtils;
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
import static io.gatling.javaapi.jdbc.JdbcDsl.jdbcFeeder;

public class IsochronesLoadTest extends Simulation {
    private static final Logger logger = LoggerFactory.getLogger(IsochronesLoadTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String PROFILE_COLUMN = "profile";

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
            TestConfig config, RangeType rangeType, boolean isParallel) {

        logger.info(
                "Creating scenario: name={}, locationCount={}, sourceFile={}, profile={}, rangeType={}, isParallel={}",
                name, locationCount, sourceFile, config.getTargetProfile(), rangeType, isParallel);

        String parallelOrSequential = isParallel ? "parallel" : "sequential";
        String groupName = String.format("Isochrones %s %s - %s - Users %s - Ranges %s",
                parallelOrSequential, rangeType.getValue(), getFileNameWithoutExtension(sourceFile),
                        config.getNumConcurrentUsers(), config.getRanges());

        // Read all records from CSV
        List<Map<String, Object>> records = csv(sourceFile).readRecords();
        logger.info("Read {} records from CSV file", records.size());

        if (records.isEmpty()) {
            logger.error("No records found in CSV file: {}", sourceFile);
            return scenario(name);
        }

        // Sample log of first record for debugging
        if (!records.isEmpty()) {
            logger.info("Sample record structure: {}", records.get(0).keySet());
        }

        // Group records by profile if profile column exists, otherwise use all records
        Map<String, List<Map<String, Object>>> recordsByProfile;
        if (records.isEmpty() || !records.get(2).containsKey(PROFILE_COLUMN)) {
            // If no profile column exists, put all records under a default key
            recordsByProfile = Map.of("all", records);
            logger.info("No profile column found in CSV, using all {} coordinates", records.size());
        } else {
            // Group records by profile
            recordsByProfile = records.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            coordinateRecord -> (String) coordinateRecord.getOrDefault(PROFILE_COLUMN,
                                    config.getTargetProfile())));
            logger.info("Found coordinates for profiles: {}", recordsByProfile.keySet());
        }

        // Get records for target profile or all records if no profile column
        List<Map<String, Object>> targetRecords = recordsByProfile.getOrDefault(
                config.getTargetProfile(), recordsByProfile.get("all"));

        if (targetRecords == null || targetRecords.isEmpty()) {
            logger.error("No records found for profile '{}' in file {}", config.getTargetProfile(), sourceFile);
            return scenario(name);
        }

        logger.info("Selected {} records for profile '{}'", targetRecords.size(), config.getTargetProfile());

        // Transform records to coordinates and shuffle
        List<Map<String, Object>> mappedRecords = targetRecords.stream()
                .map(targetRecord -> Map.of(
                        config.getFieldLon(), targetRecord.get(config.getFieldLon()),
                        config.getFieldLat(), targetRecord.get(config.getFieldLat())))
                .collect(Collectors.toList());
        Collections.shuffle(mappedRecords);

        // Create infinite circular iterator
        Iterator<Map<String, Object>> recordFeeder = IteratorUtils.infiniteCircularIterator(mappedRecords);

        logger.info("Created circular feeder with {} coordinates for profile {}",
                mappedRecords.size(), config.getTargetProfile());

        AtomicInteger remainingRecords = new AtomicInteger(targetRecords.size());

        logger.info("Processing {} coordinates for profile {}", remainingRecords.get(),
                config.getTargetProfile());

        return scenario(name)
                .asLongAs(session -> remainingRecords.get() >= 1)
                .on(
                        feed(recordFeeder, locationCount)
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
            List<List<Double>> locations = createLocationsListFromArrays(session, locationCount, config);

            Map<String, Object> requestBody = Map.of(
                    "locations", locations,
                            "range_type", rangeType.getValue(),
                    "range", config.getRanges());

            String body = objectMapper.writeValueAsString(requestBody);
            logger.debug("Created request body: {}", body);
            return body;
        } catch (JsonProcessingException e) {
            logger.error("Failed to create request body: {}", e.getMessage());
            throw new RequestBodyCreationException("Failed to create request body", e);
        }
    }

    static List<List<Double>> createLocationsListFromArrays(Session session, int locationCount, TestConfig config) {
        logger.debug("Creating locations list for batch size: {}", locationCount);
        List<List<Double>> locations = new ArrayList<>();

        try {
            logger.debug("Reading session data for fields: lon={}, lat={}", config.getFieldLon(), config.getFieldLat());
            logger.debug("Session {}", session);
            List<?> lons = session.get(config.getFieldLon());
            List<?> lats = session.get(config.getFieldLat());

            if (lons == null || lats == null) {
                logger.error("Session values are null - lon: {}, lat: {}", lons, lats);
                return locations;
            }

            logger.debug("Session data - lon size: {}, lat size: {}", lons.size(), lats.size());

            int size = Math.min(Math.min(locationCount, lons.size()), lats.size());
            logger.debug("Processing {} coordinates", size);

            for (int i = 0; i < size; i++) {
                Object lon = lons.get(i);
                Object lat = lats.get(i);

                if (lon == null || lat == null) {
                    logger.warn("Null coordinate at index {}: lon={}, lat={}", i, lon, lat);
                    continue;
                }

                try {
                    double lonValue = Double.parseDouble(lon.toString());
                    double latValue = Double.parseDouble(lat.toString());
                    locations.add(List.of(lonValue, latValue));
                } catch (NumberFormatException e) {
                    logger.error("Failed to parse coordinate at index {}: lon='{}', lat='{}'", i, lon, lat);
                    throw e;
                }
            }

            logger.debug("Created location list with {} coordinate pairs", locations.size());
            if (!locations.isEmpty()) {
                logger.debug("Sample coordinate: {}", locations.get(0));
            }

        } catch (Exception e) {
            logger.error("Error creating locations list: {}", e.getMessage(), e);
            throw new RequestBodyCreationException("Error processing coordinates", e);
        }

        return locations;
    }

    private static FeederBuilder<String> initCsvFeeder(String sourceFile) {
        logger.info("Initializing feeder with source file: {}", sourceFile);
        jdbcFeeder("databaseUrl", "username", "password", "SELECT * FROM users");
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
        logger.info("Initializing IsochronesLoadTest with configuration:");
        logger.info("- Source files: {}", config.getSourceFiles());
        logger.info("- Target profile: {}", config.getTargetProfile());
        logger.info("- Concurrent users: {}", config.getNumConcurrentUsers());
        logger.info("- Query sizes: {}", config.getQuerySizes());
        logger.info("- Ranges: {}", config.getRanges());
        logger.info("- Test unit: {}", config.getTestUnit());
        logger.info("- Base URL: {}", config.getBaseUrl());
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
