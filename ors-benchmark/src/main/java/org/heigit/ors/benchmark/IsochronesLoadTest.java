package org.heigit.ors.benchmark;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.heigit.ors.exceptions.RequestBodyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class IsochronesLoadTest extends Simulation {
    private static final Logger logger = LoggerFactory.getLogger(IsochronesLoadTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final TestConfig config = new TestConfig();

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(config.getBaseUrl())
            .acceptHeader("application/geo+json; charset=utf-8")
            .contentTypeHeader("application/json; charset=utf-8")
            .userAgentHeader("Gatling")
            .header("Authorization", config.getApiKey());

    static ScenarioBuilder createScenario(String name, int locationCount, TestConfig config) {
        FeederBuilder<String> feeder = initCsvFeeder(config.getSourceFile());
        return scenario(name)
                .feed(feeder)
                .exec(http("Isochrones " + name)
                        .post("/v2/isochrones/" + config.getTargetProfile())
                        .body(StringBody(session -> createRequestBody(
                                session,
                                locationCount,
                                config.getFieldLon(),
                                config.getFieldLat(),
                                config.getRange())))
                        .asJson()
                        .check(status().is(200)));
    }

    static String createRequestBody(Session session, int locationCount, String fieldLon, String fieldLat,
            String range) {
        List<List<Double>> locations = IntStream.range(0, locationCount)
                .mapToObj(i -> List.of(
                        session.getDouble(fieldLon),
                        session.getDouble(fieldLat)))
                .toList();

        Map<String, Object> requestBody = Map.of(
                "locations", locations,
                "range", Collections.singletonList(Integer.parseInt(range)));
        try {
            logger.debug("Created request body with {} locations", locationCount);
            return objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            logger.error("Failed to create request body for {} locations: {}", locationCount, e.getMessage(), e);
            throw new RequestBodyCreationException("Failed to create request body for " + locationCount + " locations",
                    e);
        }
    }

    /**
     * Initialize feeder with source file. Cannot only be instantiated by Gatling.
     * 
     * @param sourceFile Source file to read from
     * @return FeederBuilder with random access
     */
    private static FeederBuilder<String> initCsvFeeder(String sourceFile) {
        logger.info("Initializing feeder with source file: {}", sourceFile);
        return csv(sourceFile).random();
    }

    public IsochronesLoadTest() {
        logger.info("Initializing IsochronesLoadTest with {} users ramping up over {} seconds",
                config.getNumCalls(), config.getRampTime());

        ScenarioBuilder singleLocationScenario = createScenario("Single Location", 1, config);
        ScenarioBuilder multiLocationScenario = createScenario("Multiple Locations", config.getQuerySize(), config);

        setUp(
                singleLocationScenario
                        .injectOpen(
                                rampUsers(config.getNumCalls()).during(config.getRampTime())),
                multiLocationScenario
                        .injectOpen(
                                rampUsers(config.getNumCalls()).during(config.getRampTime())))
                .protocols(httpProtocol);
    }
}
