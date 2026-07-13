package org.heigit.ors.routing;

import com.graphhopper.routing.ev.HashMapSparseEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Dataset names come from FeatureStore config and are used unchanged as GraphHopper
 * EncodedValue names and as custom_model expression identifiers. FeatureStore already
 * validates dataset keys at its config boundary (Java-identifier-safe, no hyphens), so
 * RoutingProfile no longer sanitizes/rewrites names - it validates and fails loudly on
 * anything invalid, since that would indicate a misconfiguration slipping past FeatureStore
 * rather than something safe to silently patch up. See RoutingProfile#validateDatasetName.
 */
@ExtendWith(MockitoExtension.class)
class RoutingProfileDynamicDataTest {

    @Mock
    private ORSGraphHopper graphHopper;
    @Mock
    private EncodingManager encodingManager;

    private RoutingProfile routingProfile;
    private List<String> dynamicDatasets;

    @BeforeEach
    void setUp() throws Exception {
        // RoutingProfile only exposes a heavy constructor that builds a real graph, so we bypass
        // it (Mockito mocks a concrete class without invoking its constructor) and wire the few
        // fields exercised by the dynamic-data methods via reflection.
        routingProfile = mock(RoutingProfile.class, withSettings().defaultAnswer(org.mockito.Answers.CALLS_REAL_METHODS));

        setField(routingProfile, "mGraphHopper", graphHopper);
        dynamicDatasets = new ArrayList<>();
        setField(routingProfile, "dynamicDatasets", dynamicDatasets);
        setField(routingProfile, "loggedUnregisteredDatasets", java.util.concurrent.ConcurrentHashMap.newKeySet());
        setField(routingProfile, "profileName", "logie_hgv");

        lenient().when(graphHopper.getEncodingManager()).thenReturn(encodingManager);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = RoutingProfile.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void addDynamicDataRegistersValidNameUnchanged() {
        routingProfile.addDynamicData("logie_roads");

        verify(graphHopper).addSparseEncodedValue("logie_roads");
        assertThat(dynamicDatasets).containsExactly("logie_roads");
    }

    @ParameterizedTest(name = "[{index}] invalidName=''{0}''")
    @ValueSource(strings = {
            "logie-roads",       // hyphens
            "Logie_roads",       // uppercase
            "1logie",            // leading digit
            "logie__roads",      // double underscore
            "logie_roads_",      // trailing underscore
            "_logie",            // leading underscore
            "",                  // blank
    })
    void addDynamicDataRejectsInvalidNames(String invalidName) {
        assertThatThrownBy(() -> routingProfile.addDynamicData(invalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(invalidName);

        assertThat(dynamicDatasets).isEmpty();
    }

    @Test
    void updateDynamicDataResolvesRegisteredKeyDirectly() {
        String datasetName = "logie_roads";
        routingProfile.addDynamicData(datasetName);

        HashMapSparseEncodedValue sev = mock(HashMapSparseEncodedValue.class);
        when(encodingManager.getEncodedValue("logie_roads", HashMapSparseEncodedValue.class)).thenReturn(sev);

        routingProfile.updateDynamicData(datasetName, 42, 3.5);

        verify(sev).set(42, 3.5);
    }

    @Test
    void updateDynamicDataIsNoOpWhenDatasetWasNeverRegistered() {
        routingProfile.updateDynamicData("logie_not_registered", 1, 1.0);

        verify(encodingManager, never()).getEncodedValue(anyString(), eq(HashMapSparseEncodedValue.class));
    }

    @Test
    void unsetDynamicDataResolvesRegisteredKeyDirectly() {
        String datasetName = "logie_borders";
        routingProfile.addDynamicData(datasetName);

        HashMapSparseEncodedValue sev = mock(HashMapSparseEncodedValue.class);
        when(encodingManager.getEncodedValue("logie_borders", HashMapSparseEncodedValue.class)).thenReturn(sev);

        routingProfile.unsetDynamicData(datasetName, 7);

        verify(sev).set(7, null);
    }

    @Test
    void unsetDynamicDataDoesNotThrowWhenEncodedValueMissing() {
        when(encodingManager.getEncodedValue(anyString(), eq(HashMapSparseEncodedValue.class))).thenReturn(null);

        assertDoesNotThrow(() -> routingProfile.unsetDynamicData("logie_roads", 1));
    }

    /**
     * Simulates a profile configured with a full dataset list, e.g.
     * "ors.engine.profiles.logie_hgv.service.dynamic_data.datasets=logie_roads,logie_borders,logie_bridges".
     */
    @Test
    void multipleValidDatasetsFromAProfilesConfigListAreEachRegisteredIndependently() {
        List<String> configuredDatasets = List.of("logie_roads", "logie_borders", "logie_bridges");

        configuredDatasets.forEach(routingProfile::addDynamicData);

        for (String datasetName : configuredDatasets) {
            verify(graphHopper).addSparseEncodedValue(datasetName);
        }
        assertThat(dynamicDatasets).containsExactlyInAnyOrderElementsOf(configuredDatasets);
    }
}
