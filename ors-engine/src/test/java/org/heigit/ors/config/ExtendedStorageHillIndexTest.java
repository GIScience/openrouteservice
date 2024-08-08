package org.heigit.ors.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.storages.ExtendedStorageHillIndex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}