package org.heigit.ors.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.heigit.ors.config.profile.storages.ExtendedStorageWayCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtendedStorageWayCategoryTest {

    @Test
    void testDefaultConstructor() {
        ExtendedStorageWayCategory storage = new ExtendedStorageWayCategory();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
    }

    @Test
    void testDeSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"WayCategory\":{\"enabled\":true}}";
        ExtendedStorageWayCategory storage = (ExtendedStorageWayCategory) objectMapper.readValue(json, ExtendedStorage.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
    }

    @Test
    void testDeSerializationCorrectJsonWithoutEnabled() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"WayCategory\":\"\"}";
        ExtendedStorage storage = objectMapper.readValue(json, ExtendedStorage.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
    }

}