package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.api.config.profile.storages.ExtendedStorage;
import org.heigit.ors.api.config.profile.storages.ExtendedStorageWaySurfaceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedStorageWaySurfaceTypeTest {
    @Test
    void testDefaultConstructor() {
        ExtendedStorageWaySurfaceType storage = new ExtendedStorageWaySurfaceType();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
    }

    @Test
    void testSerializationProducesCorrectJson() throws Exception {
        ExtendedStorageWaySurfaceType storage = new ExtendedStorageWaySurfaceType();
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(storage);
        assertTrue(jsonResult.contains("\"WaySurfaceType\""), "Serialized JSON should have 'WaySurfaceType' key");
        assertTrue(jsonResult.contains("\"enabled\":true"), "Serialized JSON should have 'enabled' set to true");
    }

    @Test
    void testDeSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        String json = "{\"WaySurfaceType\":{\"enabled\":true}}";
        ExtendedStorageWaySurfaceType storage = (ExtendedStorageWaySurfaceType) objectMapper.readValue(json, ExtendedStorage.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
    }

    @Test
    void testDeserializationWithEmptyValues() throws Exception {
        String json = "{\"WaySurfaceType\":\"\"}";
        ExtendedStorageWaySurfaceType storage = new ObjectMapper().readValue(json, ExtendedStorageWaySurfaceType.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
    }


}