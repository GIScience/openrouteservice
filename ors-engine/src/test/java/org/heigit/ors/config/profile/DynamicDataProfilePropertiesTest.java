package org.heigit.ors.config.profile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicDataProfilePropertiesTest {

    @Test
    public void testIsEmpty() {
        DynamicDataProfileProperties props = new DynamicDataProfileProperties();
        assertTrue(props.isEmpty());

        props.setDatasets(Collections.singletonList("logie_borders"));
        assertFalse(props.isEmpty());
    }

    @Test
    public void testMerge() {
        DynamicDataProfileProperties props1 = new DynamicDataProfileProperties();
        props1.setDatasets(new ArrayList<>(Arrays.asList("dataset1")));

        DynamicDataProfileProperties props2 = new DynamicDataProfileProperties();
        props2.setDatasets(new ArrayList<>(Arrays.asList("dataset2", "dataset1")));

        props1.merge(props2);

        assertEquals(2, props1.getDatasets().size());
        assertTrue(props1.getDatasets().contains("dataset1"));
        assertTrue(props1.getDatasets().contains("dataset2"));
    }

    @Test
    public void testGetEnabledDynamicDatasets() {
        DynamicDataProfileProperties props = new DynamicDataProfileProperties();
        props.setDatasets(Arrays.asList("logie_borders", "logie_bridges"));

        assertEquals(2, props.getEnabledDynamicDatasets().size());
        assertTrue(props.getEnabledDynamicDatasets().contains("logie_borders"));
    }

    /**
     * Config values like "ors.engine.profiles.logie_hgv.service.dynamic_data.datasets=..." are
     * comma-separated lists bound by Spring's relaxed binding into this List<String> as-is - this
     * class does no format validation of its own, it just preserves whatever was configured
     * unmodified. Actual identifier validation happens later, at the RoutingProfile/EncodedValue
     * boundary (RoutingProfile#validateDatasetName), which rejects anything but Java-identifier-safe
     * names (see RoutingProfileDynamicDataTest) - this test only covers that this passthrough layer
     * doesn't itself mangle or reject any particular separator style.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "logie-roads,logie-borders,logie-bridges",
            "logie_roads,logie_borders,logie_bridges",
            "logie-roads,logie_borders,logie-bridges",
            "single-dataset",
            "single_dataset",
    })
    public void testGetEnabledDynamicDatasetsPreservesConfiguredNamesRegardlessOfSeparatorStyle(String commaSeparatedDatasets) {
        List<String> expected = Arrays.asList(commaSeparatedDatasets.split(","));

        DynamicDataProfileProperties props = new DynamicDataProfileProperties();
        props.setDatasets(expected);

        assertEquals(expected, props.getEnabledDynamicDatasets());
    }
}
