package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageGreenIndexTest {

    @Test
    void testDeSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"GreenIndex\":{\"enabled\":true,\"filepath\":\"" + Paths.get("src/test/resources/index.csv") + "\"}}";
        ExtendedStorageGreenIndex storage = objectMapper.readValue(json, ExtendedStorageGreenIndex.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertEquals(Paths.get("src/test/resources/index.csv").toAbsolutePath(), storage.getFilepath(), "Deserialized object should have 'filepath' set to an absolute path");
    }

    @Test
    void testSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ExtendedStorageGreenIndex storage = new ExtendedStorageGreenIndex();
        String path = "src/test/resources/index.csv";
        String actualJson = "{\"GreenIndex\":{\"enabled\":true,\"filepath\":\"" + Paths.get(path).toAbsolutePath() + "\"}}";
        storage.setFilepath(Paths.get(path));
        String json = objectMapper.writeValueAsString(storage);
        assertEquals(actualJson, json, "Serialized JSON should have 'enabled' set to true and 'filepath' set to an absolute path");
    }
}