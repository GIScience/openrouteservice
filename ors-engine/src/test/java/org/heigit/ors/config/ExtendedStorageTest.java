package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;

import static org.heigit.ors.config.utils.PropertyUtils.getAllFields;
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
    void testCopyProperties() throws IllegalAccessException {
        ExtendedStorage source = new HelperClass();
        ExtendedStorage target = new HelperClass();

        // Use reflection to set enabled to false
        List<Field> fields = getAllFields(source.getClass());
        for (Field field : fields) {
            if (field.getName().equals("enabled")) {
                field.trySetAccessible();
                field.set(source, false);
            }
        }

        assertFalse(source.getEnabled(), "Source should have 'enabled' set to false");
        assertTrue(target.getEnabled(), "Target should have 'enabled' set to true");

        target.copyProperties(source, true);

        assertFalse(target.getEnabled(), "Target should have 'enabled' set to false after copying from source");
    }

    @Test
    void testEmptyTargetEnabled() throws IllegalAccessException {
        ExtendedStorage source = new HelperClass();
        ExtendedStorage target = new HelperClass();
        // Use reflection to set enabled to false
        List<Field> fields = getAllFields(target.getClass());
        for (Field field : fields) {
            if (field.getName().equals("enabled")) {
                field.trySetAccessible();
                field.set(target, null);
            }
        }
        assertNull(target.getEnabled(), "Source should have 'enabled' set to null");

        target.copyProperties(source, false);

        assertTrue(target.getEnabled(), "Target should have 'enabled' set to true after copying from target with null enabled");
    }

    @Test
    void testCopyNullSource() {
        ExtendedStorage target = new HelperClass();
        target.copyProperties(null, true);
        assertTrue(target.getEnabled(), "Target should have 'enabled' set to true after copying from null source");
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

    @JsonTypeName("HelperClass")
    class HelperClass extends ExtendedStorage {

        @JsonCreator
        public HelperClass() {
            super();
        }

        @JsonCreator
        public HelperClass(String ignoredEmpty) {
            super();
        }

        public HelperClass(LinkedHashMap<String, Object> ignoredMap) {
            super();
        }
    }
}
