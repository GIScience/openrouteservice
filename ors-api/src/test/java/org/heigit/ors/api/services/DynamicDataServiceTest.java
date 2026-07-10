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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;

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
        lenient().when(restClientBuilder.baseUrl(any(String.class))).thenReturn(restClientBuilder);
        lenient().when(restClientBuilder.build()).thenReturn(restClient);

        // Mock EngineService and RoutingProfileManager for successful initialization
        lenient().when(engineService.waitForInitializedRoutingProfileManager())
                .thenReturn(routingProfileManager);
        lenient().when(routingProfileManager.isShutdown()).thenReturn(false);
        lenient().when(routingProfileManager.hasFailed()).thenReturn(false);
        lenient().when(routingProfileManager.getUniqueProfiles()).thenReturn(new ArrayList<>());

        dynamicDataService = new DynamicDataService(engineService, engineProperties, restClientBuilder, Runnable::run);
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
        lenient().when(stubBuilder.baseUrl(any(String.class))).thenReturn(stubBuilder);
        lenient().when(stubBuilder.build()).thenReturn(mock(RestClient.class));

        DynamicDataService disabledService = new DynamicDataService(engineService, disabledProperties, stubBuilder,
                Runnable::run);

        assertFalse(disabledService.isEnabled(), "Service should be disabled when configured to be disabled");
    }

    @Test
    @DisplayName("Should handle null feature store URL by disabling service")
    void testNullFeatureStoreUrlDisablesService() {
        EngineProperties properties = new EngineProperties();
        properties.getDynamicData().setEnabled(true);
        properties.getDynamicData().setFeatureStoreApiUrl(null);

        RestClient.Builder stubBuilder = mock(RestClient.Builder.class);
        lenient().when(stubBuilder.build()).thenReturn(mock(RestClient.class));

        DynamicDataService service = new DynamicDataService(engineService, properties, stubBuilder, Runnable::run);

        assertFalse(service.isEnabled(), "Service should be disabled when feature store URL is null");
    }

    @Test
    @DisplayName("Should handle empty feature store URL by disabling service")
    void testEmptyFeatureStoreUrlDisablesService() {
        EngineProperties properties = new EngineProperties();
        properties.getDynamicData().setEnabled(true);
        properties.getDynamicData().setFeatureStoreApiUrl("");

        RestClient.Builder stubBuilder = mock(RestClient.Builder.class);
        lenient().when(stubBuilder.build()).thenReturn(mock(RestClient.class));

        DynamicDataService service = new DynamicDataService(engineService, properties, stubBuilder, Runnable::run);

        assertFalse(service.isEnabled(), "Service should be disabled when feature store URL is empty");
    }

    @Test
    @DisplayName("Should guard each profile independently instead of serializing all profiles behind one lock")
    void testPerProfileGuardsAreIndependent() throws Exception {
        Method guardForMethod = DynamicDataService.class.getDeclaredMethod("guardFor", String.class);
        guardForMethod.setAccessible(true);

        AtomicBoolean hgvGuard = (AtomicBoolean) guardForMethod.invoke(dynamicDataService, "logie-hgv");
        AtomicBoolean carGuard = (AtomicBoolean) guardForMethod.invoke(dynamicDataService, "logie-car");

        assertNotSame(hgvGuard, carGuard, "Different profiles must not share a reentrancy guard");

        assertTrue(hgvGuard.compareAndSet(false, true), "Should acquire the hgv guard");

        assertTrue(carGuard.compareAndSet(false, true),
                "car profile must be able to update while hgv is still in progress");

        assertFalse(hgvGuard.compareAndSet(false, true), "A profile must not overlap with its own in-progress update");

        assertSame(hgvGuard, guardForMethod.invoke(dynamicDataService, "logie-hgv"));
    }
}
