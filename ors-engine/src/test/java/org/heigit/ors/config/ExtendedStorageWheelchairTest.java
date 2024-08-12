package org.heigit.ors.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.storages.ExtendedStorageWheelchair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.heigit.ors.config.utils.PropertyUtils.getAllFields;
import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageWheelchairTest {

    ExtendedStorageWheelchair source;
    ExtendedStorageWheelchair target;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws JsonProcessingException {
        String json1 = "{\"Wheelchair\":{\"enabled\":false,\"KerbsOnCrossings\":true}}";
        String json2 = "{\"Wheelchair\":{\"enabled\":true,\"KerbsOnCrossings\":false}}";

        source = objectMapper.readValue(json1, ExtendedStorageWheelchair.class);
        target = objectMapper.readValue(json2, ExtendedStorageWheelchair.class);
    }

    @Test
    void testDefaultConstructor() {
        ExtendedStorageWheelchair storage = new ExtendedStorageWheelchair();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
        assertTrue(storage.getKerbsOnCrossings(), "Default constructor should initialize 'kerbs_on_crossings' to true");
    }

    @Test
    void testSerializationProducesCorrectJson() throws Exception {
        // Step 1: Create and configure an instance of ExtendedStorageWheelchair
        ExtendedStorageWheelchair storage = new ExtendedStorageWheelchair();

        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(storage);

        // Step 3: Assert JSON structure and values including enabled
        assertTrue(jsonResult.contains("\"Wheelchair\""), "Serialized JSON should have 'Wheelchair' key");
        assertTrue(jsonResult.contains("\"enabled\":true"), "Serialized JSON should have 'enabled' set to true");
        assertTrue(jsonResult.contains("\"KerbsOnCrossings\":true"), "Serialized JSON should have 'KerbsOnCrossings' set to true");
    }

    @Test
    void testDeserializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Step 1: Create a JSON string with 'enabled' set to true and kerbs_on_crossings
        String json = "{\"Wheelchair\":{\"enabled\":true,\"KerbsOnCrossings\":true}}";

        // Step 2: Deserialize the JSON string to an object
        ExtendedStorageWheelchair storage = objectMapper.readValue(json, ExtendedStorageWheelchair.class);

        // Step 3: Assert the object's values
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertTrue(storage.getKerbsOnCrossings(), "Deserialized object should have 'kerbs_on_crossings' set to true");

        // Deserialize with enabled and kerbs_on_crossings set to false
        json = "{\"Wheelchair\":{\"enabled\":false,\"KerbsOnCrossings\":false}}";
        storage = objectMapper.readValue(json, ExtendedStorageWheelchair.class);
        assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to false");
        assertFalse(storage.getKerbsOnCrossings(), "Deserialized object should have 'kerbs_on_crossings' set to false");
    }

    @Test
    void testDeserializationWithEmptyValues() throws Exception {
        String json = "{\"Wheelchair\":\"\"}";
        ExtendedStorageWheelchair storage = new ObjectMapper().readValue(json, ExtendedStorageWheelchair.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertTrue(storage.getKerbsOnCrossings(), "Deserialized object should have 'kerbs_on_crossings' set to true");
    }

    @Test
    void testCopyPropertiesWithOverwrite() {
        ExtendedStorageWheelchair source = new ExtendedStorageWheelchair(false);
        ExtendedStorageWheelchair target = new ExtendedStorageWheelchair(true);

        assertNotEquals(source, target);
        target.copyProperties(source, true);

        assertEquals(source, target, "Source and target should be equal after copying properties");
    }

    @Test
    void testCopyPropertiesWithoutOverwrite() {
        ExtendedStorageWheelchair source = new ExtendedStorageWheelchair(false);
        ExtendedStorageWheelchair target = new ExtendedStorageWheelchair(true);

        target.copyProperties(source, false);

        assertEquals(true, target.getKerbsOnCrossings(), "KerbsOnCrossings should not be copied when overwrite is false");
    }

    @Test
    void testCopyPropertiesWithNullSource() {
        ExtendedStorageWheelchair target = new ExtendedStorageWheelchair(true);
        target.copyProperties(null, true);
        assertTrue(target.getEnabled(), "Enabled should remain unchanged when source is null");
    }

    @Test
    void testCopyPropertiesWithEmptySource() throws JsonProcessingException, IllegalAccessException {
        ExtendedStorageWheelchair source = new ExtendedStorageWheelchair();
        // use reflection to set the kerbs_on_crossings field to null
        List<Field> allFields = getAllFields(source.getClass());
        for (Field field : allFields) {
            if (field.getName().equals("kerbs_on_crossings")) {
                field.setAccessible(true);
                field.set(source, null);
            }
        }
        assertNotEquals(source, target);
        target.copyProperties(source, false);
        assertEquals(source.getEnabled(), target.getEnabled(), "Enabled should be copied from an empty source when overwrite is true");
    }

    @Test
    void testCopyPropertiesWithEmptyTarget() throws JsonProcessingException, IllegalAccessException {
        ExtendedStorageWheelchair target = new ExtendedStorageWheelchair();

        // User reflection to set the kerbs_on_crossings field to null
        List<Field> allFields = getAllFields(target.getClass());
        for (Field field : allFields) {
            if (field.getName().equals("kerbs_on_crossings")) {
                field.setAccessible(true);
                field.set(target, null);
            }
        }
        target.copyProperties(source, true);
        assertEquals(source.getEnabled(), target.getEnabled(), "Enabled should be copied from an empty source when overwrite is true");
    }


}