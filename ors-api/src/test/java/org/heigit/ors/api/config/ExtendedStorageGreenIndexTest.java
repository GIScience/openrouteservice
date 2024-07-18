package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageGreenIndexTest {
    @Test
    void testDefaultConstructor() {
        ExtendedStorageGreenIndex storage = new ExtendedStorageGreenIndex();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
        assertEquals("", storage.getFilepath().toString(), "Default constructor should initialize 'filepath' to an empty string");
    }

    @Test
    void testDeSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"filepath\":\"src/test/resources/index.csv\",\"enabled\":true}";
        ExtendedStorageGreenIndex storage = objectMapper.readValue(json, ExtendedStorageGreenIndex.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertEquals(Paths.get("src/test/resources/index.csv").toAbsolutePath(), storage.getFilepath(), "Deserialized object should have 'filepath' set to an absolute path");
    }

    @Test
    void testSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ExtendedStorageGreenIndex storage = new ExtendedStorageGreenIndex();
        String path = "src/test/resources/index.csv";
        storage.setFilepath(path);
        String json = objectMapper.writeValueAsString(storage);
        assertEquals("{\"enabled\":true,\"filepath\":\"" + Paths.get(path).toAbsolutePath() + "}", json, "Serialized object should have 'filepath' set to an absolute path");
    }


}