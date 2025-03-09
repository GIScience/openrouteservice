package org.heigit.ors.benchmark;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.heigit.ors.benchmark.BenchmarkEnums.RangeType;
import static org.heigit.ors.benchmark.util.NameUtils.getFileNameWithoutExtension;
import org.heigit.ors.benchmark.util.SourceUtils;
import org.heigit.ors.exceptions.RequestBodyCreationException;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
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

public class IsochronesLoadTest extends AbstractLoadTest {

    static {
        logger = LoggerFactory.getLogger(IsochronesLoadTest.class);
    }

    public IsochronesLoadTest() {
        super();
    }

    @Override
    protected void logConfigInfo() {
        logger.info("Initializing IsochronesLoadTest with configuration:");
        logger.info("- Source files: {}", config.getSourceFiles());
        logger.info("- Target profile: {}", config.getTargetProfile());
        logger.info("- Concurrent users: {}", config.getNumConcurrentUsers());
        logger.info("- Query sizes: {}", config.getQuerySizes());
        logger.info("- Ranges: {}", config.getRanges());
        logger.info("- Test unit: {}", config.getTestUnit());
        logger.info("- Base URL: {}", config.getBaseUrl());
        logger.info("- Execution mode: {}", config.isParallelExecution() ? "parallel" : "sequential");
    }

    @Override
    protected void logTestTypeInfo() {
        logger.info("Testing {} isochrones", config.getTestUnit());
    }

    @Override
    protected Stream<PopulationBuilder> createScenarios(boolean isParallel) {
        return createScenariosForTestUnit().stream()
                .flatMap(rangeType -> config.getSourceFiles().stream()
                        .flatMap(sourceFile -> config.getQuerySizes().stream()
                                .map(querySize -> createScenarioWithInjection(sourceFile, querySize, isParallel,
                                        rangeType))));
    }

    private List<RangeType> createScenariosForTestUnit() {
        return switch (config.getTestUnit()) {
            case DISTANCE -> List.of(RangeType.DISTANCE);
            case TIME -> List.of(RangeType.TIME);
        };
    }

    private PopulationBuilder createScenarioWithInjection(String sourceFile, int querySize, boolean isParallel,
            RangeType rangeType) {
        String scenarioName = formatScenarioName(sourceFile, querySize);
        return createIsochroneScenario(scenarioName, querySize, sourceFile, config, rangeType, isParallel)
                .injectOpen(atOnceUsers(config.getNumConcurrentUsers()));
    }

    private String formatScenarioName(String sourceFile, int querySize) {
        String fileName = getFileNameWithoutExtension(sourceFile);
        return String.format("Locations (%d) | %s", querySize, fileName);
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

        // Get records for target profile or all records if no profile column

        try {
            List<Map<String, Object>> targetRecords = SourceUtils.getRecordsByProfile(sourceFile, config.getTargetProfile());


            AtomicInteger remainingRecords = new AtomicInteger(targetRecords.size());

            logger.info("Processing {} coordinates for profile {}", remainingRecords.get(),
                    config.getTargetProfile());

            return scenario(name)
                    .group(groupName)
                    .on(
                            feed(targetRecords.iterator(), locationCount)
                                    .exec(createIsochroneRequest(name, locationCount, config, rangeType)));
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
            return scenario(name);
        }
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
            throw new RequestBodyCreationException("Failed to create request body", e);
        }
    }

    private static void processCoordinates(List<List<Double>> locations, List<?> lons, List<?> lats, int size) {
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
                throw new RequestBodyCreationException("Failed to parse coordinate values", e);
            }
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

            processCoordinates(locations, lons, lats, size);

            logger.debug("Created location list with {} coordinate pairs", locations.size());
            if (!locations.isEmpty()) {
                logger.debug("Sample coordinate: {}", locations.get(0));
            }

        } catch (NumberFormatException e) {
            throw new RequestBodyCreationException("Error processing coordinates", e);
        }

        return locations;
    }
}
