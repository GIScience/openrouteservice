package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedStorageGreenIndexTest {

    @Test
    void testDeSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Path path = Paths.get("src/test/resources/index.csv");
        String json = "{\"GreenIndex\":{\"enabled\":true,\"filepath\":\"" + path + "\"}}";
        ExtendedStorageGreenIndex storage = objectMapper.readValue(json, ExtendedStorageGreenIndex.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertEquals(path.toAbsolutePath(), storage.getFilepath(), "Deserialized object should have 'filepath' set to an absolute path");
    }

    @Test
    void testSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ExtendedStorageGreenIndex storage = new ExtendedStorageGreenIndex();
        String expectedJson = "{\"GreenIndex\":{\"enabled\":true,\"filepath\":\"\"}}";
        String json = objectMapper.writeValueAsString(storage);
        assertEquals(expectedJson, json, "Serialized JSON should have 'enabled' set to true and 'filepath' set to an absolute path");
    }
}