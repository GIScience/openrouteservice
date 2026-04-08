package org.heigit.ors.api.services;

import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.routing.RoutingProfileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DynamicDataService focusing on configuration and service state.
 * Tests mocked RestClient behavior and configuration validation. 
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DynamicDataService Unit Tests")
class DynamicDataServiceTest {

    @Mock
    private EngineService engineService;

    @Mock
    private RoutingProfileManager routingProfileManager;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    private EngineProperties engineProperties;
    private DynamicDataService dynamicDataService;

    @BeforeEach
    void setUp() {
        engineProperties = new EngineProperties();
        engineProperties.getDynamicData().setEnabled(true);
        engineProperties.getDynamicData().setFeatureStoreApiUrl("http://localhost:8080/api/v1");

        // Mock RestClient.Builder chain
        when(restClientBuilder.baseUrl(any(String.class))).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);

        // Mock EngineService and RoutingProfileManager for successful initialization
        when(engineService.waitForInitializedRoutingProfileManager()).thenReturn(routingProfileManager);
        when(routingProfileManager.isShutdown()).thenReturn(false);
        when(routingProfileManager.hasFailed()).thenReturn(false);
        when(routingProfileManager.getUniqueProfiles()).thenReturn(new ArrayList<>());

        dynamicDataService = new DynamicDataService(engineService, engineProperties, restClientBuilder);
    }

    @Test
    @DisplayName("Should indicate service is enabled when configured")
    void testIsEnabledReturnsTrue() {
        assertTrue(dynamicDataService.isEnabled(), "Service should be enabled in this test setup");
    }

    @Test
    @DisplayName("Should indicate service is disabled when not configured")
    void testIsEnabledReturnsFalseWhenDisabled() {
        EngineProperties disabledProperties = new EngineProperties();
        disabledProperties.getDynamicData().setEnabled(false);
        disabledProperties.getDynamicData().setFeatureStoreApiUrl("http://localhost:8080/api/v1");

        RestClient.Builder stubBuilder = mock(RestClient.Builder.class);
        when(stubBuilder.baseUrl(any(String.class))).thenReturn(stubBuilder);
        when(stubBuilder.build()).thenReturn(mock(RestClient.class));

        DynamicDataService disabledService = new DynamicDataService(engineService, disabledProperties, stubBuilder);

        assertFalse(disabledService.isEnabled(), "Service should be disabled when configured to be disabled");
    }

    @Test
    @DisplayName("Should handle null feature store URL by disabling service")
    void testNullFeatureStoreUrlDisablesService() {
        EngineProperties properties = new EngineProperties();
        properties.getDynamicData().setEnabled(true);
        properties.getDynamicData().setFeatureStoreApiUrl(null);

        RestClient.Builder stubBuilder = mock(RestClient.Builder.class);
        when(stubBuilder.build()).thenReturn(mock(RestClient.class));

        DynamicDataService service = new DynamicDataService(engineService, properties, stubBuilder);

        assertFalse(service.isEnabled(), "Service should be disabled when feature store URL is null");
    }

    @Test
    @DisplayName("Should handle empty feature store URL by disabling service")
    void testEmptyFeatureStoreUrlDisablesService() {
        EngineProperties properties = new EngineProperties();
        properties.getDynamicData().setEnabled(true);
        properties.getDynamicData().setFeatureStoreApiUrl("");

        RestClient.Builder stubBuilder = mock(RestClient.Builder.class);
        when(stubBuilder.build()).thenReturn(mock(RestClient.class));

        DynamicDataService service = new DynamicDataService(engineService, properties, stubBuilder);

        assertFalse(service.isEnabled(), "Service should be disabled when feature store URL is empty");
    }
}
