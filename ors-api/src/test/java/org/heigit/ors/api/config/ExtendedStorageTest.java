package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageTest {

    @Test
    void testDefaultConstructor() {
        ExtendedStorage storage = new HelperClass();
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
    }

    @Test
    void testStringConstructor() {
        ExtendedStorage storage = new HelperClass("");
        assertTrue(storage.getEnabled(), "String constructor should not alter 'enabled' state");
    }

    @Test
    void testLinkedHashMapConstructor() {
        ExtendedStorage storage = new HelperClass(new LinkedHashMap<>());
        assertTrue(storage.getEnabled(), "LinkedHashMap constructor should not alter 'enabled' state");
    }

    @Test
    void testGetterAndSetter() {
        ExtendedStorage storage = new HelperClass();
        assertTrue(storage.getEnabled(), "setEnabled(true) should result in 'enabled' being true");
    }

    @Test
    void testSerializationProducesCorrectJson() throws Exception {
        // Step 1: Create and configure an instance of ExtendedStorage
        ExtendedStorage storage = new HelperClass();

        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(storage);

        // Step 3: Assert JSON structure and values including enabled
        assertTrue(jsonResult.contains("\"enabled\":true"), "Serialized JSON should have 'enabled' set to true");
        // Assert that jsonResult doesn't contain the default path parameter
        assertFalse(jsonResult.contains("\"filepath\":"), "Serialized JSON should not contain 'filepath'");
    }

    class HelperClass extends ExtendedStorage {
        public HelperClass() {
            super();
        }

        public HelperClass(String ignoredEmpty) {
            super();
        }

        public HelperClass(LinkedHashMap<String, Object> ignoredMap) {
            super();
        }
    }
}

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