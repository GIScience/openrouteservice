package org.heigit.ors.config.profile;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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
}
