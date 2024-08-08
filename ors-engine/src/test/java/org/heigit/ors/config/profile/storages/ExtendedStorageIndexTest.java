package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;


class ExtendedStorageIndexTest {

    @Test
    void defaultConstructorInitializesObject() {
        ExtendedStorageIndex storage = new HelperClass();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
        assertEquals("", storage.getFilepath().toString(), "Default constructor should initialize 'filepath' to an empty string");
    }

    @Test
    void deserializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"GreenIndex\":{\"enabled\":false,\"filepath\":\"" + Paths.get("src/test/resources/index.csv") + "\"}}";
        ExtendedStorageIndex storage = objectMapper.readValue(json, ExtendedStorageIndex.class);
        assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to false");
        assertEquals(Paths.get("src/test/resources/index.csv").toAbsolutePath(), storage.getFilepath(), "Deserialized object should have 'filepath' set to an absolute path");
    }

    @Test
    void deserializeNullPath() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"GreenIndex\":{\"enabled\":false,\"filepath\":null}}";
        ExtendedStorageIndex storage = objectMapper.readValue(json, ExtendedStorageIndex.class);
        assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to false");
        assertEquals(Paths.get(""), storage.getFilepath(), "Deserialized object should have 'filepath' set to an empty string");
    }

    class HelperClass extends ExtendedStorageIndex {
        public HelperClass() {
            super();
        }
    }
}