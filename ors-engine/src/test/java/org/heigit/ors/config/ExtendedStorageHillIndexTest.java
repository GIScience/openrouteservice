package org.heigit.ors.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.storages.ExtendedStorageHillIndex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageHillIndexTest {

    @Test
    void testDefaultConstructor() {
        ExtendedStorageHillIndex storage = new ExtendedStorageHillIndex();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
    }

    @Test
    void testDeserializationWithEmptyValues() throws Exception {
        String json = "{\"HillIndex\":\"\"}";
        ExtendedStorageHillIndex storage = new ObjectMapper().readValue(json, ExtendedStorageHillIndex.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
    }

    @Test
    void testCopyPropertiesWithOverwrite() {
        ExtendedStorageHillIndex source = new ExtendedStorageHillIndex();
        source.setMaximumSlope(15);
        ExtendedStorageHillIndex target = new ExtendedStorageHillIndex();
        target.setMaximumSlope(10);

        target.copyProperties(source, true);

        assertEquals(source.getMaximumSlope(), target.getMaximumSlope(), "MaximumSlope should be copied when overwrite is true");
    }

    @Test
    void testCopyPropertiesWithoutOverwrite() {
        ExtendedStorageHillIndex source = new ExtendedStorageHillIndex();
        source.setMaximumSlope(15);
        ExtendedStorageHillIndex target = new ExtendedStorageHillIndex();
        target.setMaximumSlope(10);

        target.copyProperties(source, false);

        assertEquals(10, target.getMaximumSlope(), "MaximumSlope should not be copied when overwrite is false");
    }

    @Test
    void testCopyPropertiesWithNullSource() {
        ExtendedStorageHillIndex target = new ExtendedStorageHillIndex();
        target.setMaximumSlope(10);

        target.copyProperties(null, true);

        assertEquals(10, target.getMaximumSlope(), "MaximumSlope should remain unchanged when source is null");
    }

    @Test
    void testCopyPropertiesWithEmptySource() {
        ExtendedStorageHillIndex source = new ExtendedStorageHillIndex();
        ExtendedStorageHillIndex target = new ExtendedStorageHillIndex();
        target.setMaximumSlope(10);

        target.copyProperties(source, true);

        assertEquals(10, target.getMaximumSlope(), "MaximumSlope should remain unchanged when source is empty");
    }

    @Test
    void testCopyPropertiesWithEmptyTarget() {
        ExtendedStorageHillIndex target = new ExtendedStorageHillIndex();
        ExtendedStorageHillIndex source = new ExtendedStorageHillIndex();
        source.setMaximumSlope(15);

        target.copyProperties(source, false);

        assertEquals(15, target.getMaximumSlope(), "MaximumSlope should be copied from a non-empty source when overwrite is false");
    }
}