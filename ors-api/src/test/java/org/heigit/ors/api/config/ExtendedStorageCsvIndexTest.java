package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.api.config.profile.storages.ExtendedStorageCsvIndex;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedStorageCsvIndexTest {

    @Test
    void testDeSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Path path = Paths.get("src/test/resources/index.csv");
        String json = "{\"csv\":{\"enabled\":true,\"filepath\":\"" + path + "\"}}";
        ExtendedStorageCsvIndex storage = objectMapper.readValue(json, ExtendedStorageCsvIndex.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertEquals(path.toAbsolutePath(), storage.getFilepath(), "Deserialized object should have 'filepath' set to an absolute path");
    }

    @Test
    void testSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ExtendedStorageCsvIndex storage = new ExtendedStorageCsvIndex();
        String actualJson = "{\"csv\":{\"enabled\":true,\"filepath\":\"\"}}";
        String json = objectMapper.writeValueAsString(storage);
        assertEquals(actualJson, json, "Serialized JSON should have 'enabled' set to true and 'filepath' set to an absolute path");
    }

    @Test
    void testDeserializationWithEmptyValues() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"csv\":\"\"}";
        ExtendedStorageCsvIndex storage = objectMapper.readValue(json, ExtendedStorageCsvIndex.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertEquals("", storage.getFilepath().toString(), "Deserialized object should have 'filepath' set to an empty string");
    }


}