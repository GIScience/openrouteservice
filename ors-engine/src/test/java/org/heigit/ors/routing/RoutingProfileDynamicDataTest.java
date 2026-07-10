package org.heigit.ors.routing;

import com.graphhopper.routing.ev.HashMapSparseEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Covers the hyphen/underscore dataset-name mismatch between config/FeatureStore identifiers
 * (e.g. {@code logie-roads}, as configured in {@code ors.engine.profiles.*.service.dynamic_data.datasets})
 * and GraphHopper's stricter EncodedValue naming rules (lowercase letters, digits, single
 * underscores only - no hyphens). See RoutingProfile#sanitizeEncodedValueKey.
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
        setField(routingProfile, "profileName", "logie-hgv");

        lenient().when(graphHopper.getEncodingManager()).thenReturn(encodingManager);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = RoutingProfile.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Dataset names as they actually appear in .ors.env config (comma-separated
     * "ors.engine.profiles.*.service.dynamic_data.datasets" values), covering the possibilities
     * an operator might configure: hyphenated (FeatureStore's native style), already-underscored,
     * mixed separators, and plain single-word names with no separator at all.
     */
    @ParameterizedTest(name = "[{index}] datasetName=''{0}'' -> encodedValueName=''{1}''")
    @CsvSource({
            "logie-roads,     logie_roads",
            "logie-borders,   logie_borders",
            "logie-bridges,   logie_bridges",
            "logie_roads,     logie_roads",
            "logie-multi-word-name, logie_multi_word_name",
            "plainname,       plainname",
    })
    void addDynamicDataSanitizesHyphensForGraphHopperButKeepsRawNameForBookkeeping(String datasetName, String expectedEncodedValueName) {
        routingProfile.addDynamicData(datasetName);

        verify(graphHopper).addSparseEncodedValue(expectedEncodedValueName);
        assertTrue(dynamicDatasets.contains(datasetName),
                "dynamicDatasets should track the raw config/FeatureStore name, not the sanitized one");
    }

    @Test
    void updateDynamicDataResolvesHyphenatedKeyToSanitizedEncodedValue() {
        String datasetName = "logie-roads";
        routingProfile.addDynamicData(datasetName);

        HashMapSparseEncodedValue sev = mock(HashMapSparseEncodedValue.class);
        when(encodingManager.getEncodedValue("logie_roads", HashMapSparseEncodedValue.class)).thenReturn(sev);

        // FeatureStore's /matches payload sends back the same hyphenated key that was configured.
        routingProfile.updateDynamicData(datasetName, 42, 3.5);

        verify(sev).set(42, 3.5);
    }

    @Test
    void updateDynamicDataIsNoOpWhenDatasetWasNeverRegistered() {
        routingProfile.updateDynamicData("logie-not-registered", 1, 1.0);

        verify(encodingManager, never()).getEncodedValue(anyString(), eq(HashMapSparseEncodedValue.class));
    }

    @Test
    void unsetDynamicDataResolvesHyphenatedKeyToSanitizedEncodedValue() {
        String datasetName = "logie-borders";
        routingProfile.addDynamicData(datasetName);

        HashMapSparseEncodedValue sev = mock(HashMapSparseEncodedValue.class);
        when(encodingManager.getEncodedValue("logie_borders", HashMapSparseEncodedValue.class)).thenReturn(sev);

        routingProfile.unsetDynamicData(datasetName, 7);

        verify(sev).set(7, null);
    }

    @Test
    void unsetDynamicDataDoesNotThrowWhenEncodedValueMissing() {
        when(encodingManager.getEncodedValue(anyString(), eq(HashMapSparseEncodedValue.class))).thenReturn(null);

        assertDoesNotThrow(() -> routingProfile.unsetDynamicData("logie-roads", 1));
    }

    /**
     * Simulates a profile configured with a full dataset list, e.g.
     * "ors.engine.profiles.logie-hgv.service.dynamic_data.datasets=logie-roads,logie-borders,logie-bridges",
     * verifying each hyphenated entry is independently sanitized without cross-contamination.
     */
    @Test
    void multipleHyphenatedDatasetsFromAProfilesConfigListAreEachSanitizedIndependently() {
        List<String> configuredDatasets = List.of("logie-roads", "logie-borders", "logie-bridges");

        configuredDatasets.forEach(routingProfile::addDynamicData);

        for (String datasetName : configuredDatasets) {
            verify(graphHopper).addSparseEncodedValue(datasetName.replace('-', '_'));
        }
        assertEquals(configuredDatasets.size(), dynamicDatasets.size());
        assertTrue(dynamicDatasets.containsAll(configuredDatasets));
    }
}
