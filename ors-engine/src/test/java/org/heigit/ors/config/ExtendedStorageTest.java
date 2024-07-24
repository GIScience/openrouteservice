package org.heigit.ors.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
