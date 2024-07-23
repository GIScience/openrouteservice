package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedStorageShadowIndexTest {

    @Test
    void testDeSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Path path = Paths.get("src/test/resources/index.csv");
        String json = "{\"ShadowIndex\":{\"enabled\":true,\"filepath\":\"" + path + "\"}}";
        ExtendedStorageShadowIndex storage = objectMapper.readValue(json, ExtendedStorageShadowIndex.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertEquals(path.toAbsolutePath(), storage.getFilepath(), "Deserialized object should have 'filepath' set to an absolute path");
    }

    @Test
    void testSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ExtendedStorageShadowIndex storage = new ExtendedStorageShadowIndex();
        String expectedJson = "{\"ShadowIndex\":{\"enabled\":true,\"filepath\":\"\"}}";
        String json = objectMapper.writeValueAsString(storage);
        assertEquals(expectedJson, json, "Serialized JSON should have 'enabled' set to true and 'filepath' set to an absolute path");
    }

    @Test
    void testDeserializationWithEmptyValues() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"ShadowIndex\":\"\"}";
        ExtendedStorageShadowIndex storage = objectMapper.readValue(json, ExtendedStorageShadowIndex.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertEquals("", storage.getFilepath().toString(), "Deserialized object should have 'filepath' set to an empty string");
    }

}