package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageCsvIndexTest {

    @Test
    void testDeSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"csv\":{\"enabled\":true,\"filepath\":\"" + Paths.get("src/test/resources/index.csv") + "\"}}";
        ExtendedStorageCsvIndex storage = objectMapper.readValue(json, ExtendedStorageCsvIndex.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertEquals(Paths.get("src/test/resources/index.csv").toAbsolutePath(), storage.getFilepath(), "Deserialized object should have 'filepath' set to an absolute path");
    }

    @Test
    void testSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ExtendedStorageCsvIndex storage = new ExtendedStorageCsvIndex();
        String path = "src/test/resources/index.csv";
        String actualJson = "{\"csv\":{\"enabled\":true,\"filepath\":\"" + Paths.get(path).toAbsolutePath() + "\"}}";
        storage.setFilepath(Paths.get(path));
        String json = objectMapper.writeValueAsString(storage);
        assertEquals(actualJson, json, "Serialized JSON should have 'enabled' set to true and 'filepath' set to an absolute path");
    }

}