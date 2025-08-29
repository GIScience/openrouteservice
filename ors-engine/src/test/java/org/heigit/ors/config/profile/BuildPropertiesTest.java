package org.heigit.ors.config.profile;

import com.graphhopper.routing.ev.WaySurface;
import com.graphhopper.routing.ev.WayType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuildPropertiesTest {
    private BuildProperties buildProperties;

    @BeforeEach
    void setUp() {
        buildProperties = new BuildProperties();
    }

    @Test
    void testGetEncodedValuesString() {
        buildProperties.getEncodedValues().setWaySurface(true);
        assertEquals(WaySurface.KEY, buildProperties.getEncodedValuesString());

        buildProperties.getEncodedValues().setWayType(true);
        assertEquals(WaySurface.KEY + "," + WayType.KEY, buildProperties.getEncodedValuesString());

        buildProperties.getEncodedValues().setWaySurface(false);
        assertEquals(WayType.KEY, buildProperties.getEncodedValuesString());

        buildProperties.getEncodedValues().setWayType(false);
        assertEquals("", buildProperties.getEncodedValuesString());

        buildProperties.getEncodedValues().setWaySurface(null);
        buildProperties.getEncodedValues().setWayType(null);
        assertEquals("", buildProperties.getEncoderOptionsString());
    }

    @Test
    void testInitializationOfWaySurfaceTypeExtendedStorageSetsCorrespondingEncodedValues() {
        buildProperties.getExtStorages().put("WaySurfaceType", new ExtendedStorageProperties());
        buildProperties.initExtStorages();

        assertTrue(buildProperties.getEncodedValues().getWaySurface());
        assertTrue(buildProperties.getEncodedValues().getWayType());
    }

    @Test
    void testEncodedValuesHavePrecedenceOverExternalStorages() {
        buildProperties.getEncodedValues().setWayType(false);
        buildProperties.getExtStorages().put("WaySurfaceType", new ExtendedStorageProperties());
        buildProperties.initExtStorages();

        assertFalse(buildProperties.getEncodedValues().getWayType());
        assertTrue(buildProperties.getEncodedValues().getWaySurface());
    }
}