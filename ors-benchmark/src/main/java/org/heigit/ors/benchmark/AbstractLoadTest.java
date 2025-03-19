package org.heigit.ors.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.heigit.ors.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

import static io.gatling.javaapi.http.HttpDsl.http;

public abstract class AbstractLoadTest extends Simulation {
    protected static Logger logger = LoggerFactory.getLogger(AbstractLoadTest.class);
    protected static final ObjectMapper objectMapper = new ObjectMapper();

    protected final Config config = new Config();
    protected final HttpProtocolBuilder httpProtocol;

    protected AbstractLoadTest() {
        this.httpProtocol = createHttpProtocol();
        localLogConfigInfo();
        
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

    private HttpProtocolBuilder createHttpProtocol() {
        return http.baseUrl(config.getBaseUrl())
                .acceptHeader("application/geo+json; charset=utf-8")
                .contentTypeHeader("application/json; charset=utf-8")
                .userAgentHeader("Gatling")
                .header("Authorization", config.getApiKey());
    }

    protected final void localLogConfigInfo() {
        logConfigInfo();
    }

    protected abstract void logConfigInfo();

    protected abstract Stream<PopulationBuilder> createScenarios(boolean isParallel);

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

    @Override
    public void before() {
        logger.info("Starting Gatling simulation...");
        logTestTypeInfo();
    }

    protected abstract void logTestTypeInfo();

    @Override
    public void after() {
        logger.info("Gatling simulation completed.");
    }
}
