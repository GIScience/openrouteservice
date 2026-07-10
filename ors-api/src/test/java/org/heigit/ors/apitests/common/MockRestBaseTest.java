package org.heigit.ors.apitests.common;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.MockServerRestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.UnorderedRequestExpectationManager;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

/**
 * Abstract base test class that uses Spring's MockRestServiceServer to mock the FeatureStore API
 * for testing dynamic data integration.
 * <p>
 * Uses {@link MockServerRestClientCustomizer} rather than autowiring/binding to a
 * {@code RestClient.Builder} directly: Spring Boot's auto-configured {@code RestClient.Builder}
 * bean is prototype-scoped, so an {@code @Autowired} builder in a test class is a different
 * instance than the one injected into {@code DynamicDataService}'s constructor. The customizer
 * is applied by {@code RestClientBuilderConfigurer} to every prototype instance as it's created,
 * so it reliably intercepts the exact builder {@code DynamicDataService} uses.
 * <p>
 * Stubs are registered from within the customizer's {@code customize()} callback (overridden
 * below), not in {@code @BeforeEach}: {@code DynamicDataService.onContextRefreshed()} fires a
 * real HTTP call synchronously during Spring context startup (via {@code ContextRefreshedEvent}),
 * which happens before any {@code @BeforeEach} method runs. Registering stubs at customize-time
 * guarantees they exist before that first call, which is also what let this setup fail loudly
 * (rather than silently returning an empty result) once the mock binding was fixed.
 */
@Import(MockRestBaseTest.MockRestClientTestConfig.class)
public abstract class MockRestBaseTest extends ServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockRestBaseTest.class);

    @Autowired(required = false)
    protected MockServerRestClientCustomizer mockServerRestClientCustomizer;

    protected MockRestServiceServer mockServer;

    @TestConfiguration
    static class MockRestClientTestConfig {
        @Bean
        MockServerRestClientCustomizer mockServerRestClientCustomizer() {
            // DynamicDataService calls /datasets/stats before /matches, but stub registration
            // order below doesn't otherwise matter - avoid coupling the test to call order.
            return new MockServerRestClientCustomizer(UnorderedRequestExpectationManager::new) {
                @Override
                public void customize(RestClient.Builder restClientBuilder) {
                    super.customize(restClientBuilder);
                    setupMocks(getServer(restClientBuilder));
                }
            };
        }
    }

    private static void setupMocks(MockRestServiceServer mockServer) {
        String ndjsonMatches = """
                {"feature_id":1,"dataset_key":"logie_borders","edge_id":3239,"value":1.0,"timestamp":"2024-09-08T20:21:00Z","is_deleted":false}
                {"feature_id":2,"dataset_key":"logie_bridges","edge_id":3239,"value":1.0,"timestamp":"2024-09-08T20:21:00Z","is_deleted":false}
                {"feature_id":3,"dataset_key":"logie_roads","edge_id":3239,"value":1.0,"timestamp":"2024-09-08T20:21:00Z","is_deleted":false}
                {"feature_id":3,"dataset_key":"logie_roads","edge_id":14409,"value":1.0,"timestamp":"2024-09-08T20:21:00Z","is_deleted":false}
                """.trim();

        // Support pagination via updated_after and profile parameters
        mockServer.expect(ExpectedCount.manyTimes(),
                MockRestRequestMatchers.requestTo(org.hamcrest.Matchers.matchesPattern("http://mock\\-feature\\-store/api/v1/matches.*")))
                .andRespond(MockRestResponseCreators.withSuccess(ndjsonMatches, MediaType.TEXT_PLAIN));

        String statsResponse = """
            [
              {
                "datasetId": "logie_borders",
                "totalFeatures": 1000,
                "matchedFeatures": 1000,
                "unmatchedFeatures": 0,
                "deletedFeatures": 0
                  },
              {
                "datasetId": "logie_bridges",
                "totalFeatures": 100,
                "matchedFeatures": 100,
                "unmatchedFeatures": 0,
                "deletedFeatures": 0
                  },
              {
                "datasetId": "logie_roads",
                "totalFeatures": 500,
                "matchedFeatures": 450,
                "unmatchedFeatures": 50,
                "deletedFeatures": 0
                  }
            ]
                """;

        mockServer.expect(ExpectedCount.manyTimes(),
                MockRestRequestMatchers.requestTo("http://mock-feature-store/api/v1/datasets/stats"))
                .andRespond(MockRestResponseCreators.withSuccess(statsResponse, MediaType.APPLICATION_JSON));

        LOGGER.info("MockRestServiceServer stubs configured successfully");
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        // Deliberately a bare host with no "/api/v1" suffix, matching real .ors.env config
        // (ors.engine.dynamic_data.api_url=http://featurestoreservice:8080). The "/api/v1"
        // prefix must come from DynamicDataService's own .uri()/.fromPath() calls - if it were
        // baked into this base URL instead, a missing prefix in the production code would go
        // undetected here (as previously happened).
        String apiUrl = "http://mock-feature-store";
        System.setProperty("ors.engine.dynamic_data.feature_store_api_url", apiUrl);
        registry.add("ors.engine.dynamic_data.feature_store_api_url", () -> apiUrl);
    }

    @BeforeEach
    void resolveMockServer() {
        if (mockServerRestClientCustomizer != null) {
            mockServer = mockServerRestClientCustomizer.getServer();
        } else {
            LOGGER.warn("Could not resolve MockRestServiceServer: MockServerRestClientCustomizer not autowired.");
        }
    }
}
