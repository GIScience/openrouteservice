package org.heigit.ors.apitests.common;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

/**
 * Abstract base test class that uses Spring's MockRestServiceServer to mock the FeatureStore API
 * for testing dynamic data integration.
 */
public abstract class MockRestBaseTest extends ServiceTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MockRestBaseTest.class);
    
    protected MockRestServiceServer mockServer;

    @Autowired(required = false)
    protected RestClient.Builder restClientBuilder;

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        String apiUrl = "http://mock-feature-store/api/v1";
        System.setProperty("ors.engine.dynamic_data.feature_store_api_url", apiUrl);
        registry.add("ors.engine.dynamic_data.feature_store_api_url", () -> apiUrl);
    }

    @BeforeEach
    void configureMockServer() {
        if (restClientBuilder != null) {
             mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
             setupMocks();
        } else {
             LOGGER.warn("Could not setup MockRestServiceServer: restClientBuilder not autowired.");
        }
    }

    private void setupMocks() {
        String ndjsonMatches = """
                {"feature_id":1,"dataset_key":"logie_borders","edge_id":3239,"value":"CLOSED","timestamp":"2024-09-08T20:21:00Z","is_deleted":false}
                {"feature_id":2,"dataset_key":"logie_bridges","edge_id":3239,"value":"RESTRICTED","timestamp":"2024-09-08T20:21:00Z","is_deleted":false}
                {"feature_id":3,"dataset_key":"logie_roads","edge_id":3239,"value":"RESTRICTED","timestamp":"2024-09-08T20:21:00Z","is_deleted":false}
                {"feature_id":3,"dataset_key":"logie_roads","edge_id":14409,"value":"RESTRICTED","timestamp":"2024-09-08T20:21:00Z","is_deleted":false}
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
}
