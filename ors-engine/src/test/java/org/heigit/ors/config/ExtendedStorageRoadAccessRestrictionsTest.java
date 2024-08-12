package org.heigit.ors.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.storages.ExtendedStorageRoadAccessRestrictions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.heigit.ors.config.utils.PropertyUtils.getAllFields;
import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageRoadAccessRestrictionsTest {

    ExtendedStorageRoadAccessRestrictions source;
    ExtendedStorageRoadAccessRestrictions target;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws JsonProcessingException {
        String json1 = "{\"RoadAccessRestrictions\":{\"enabled\":false,\"use_for_warnings\":false}}";
        String json2 = "{\"RoadAccessRestrictions\":{\"enabled\":true,\"use_for_warnings\":true}}";
        source = objectMapper.readValue(json1, ExtendedStorageRoadAccessRestrictions.class);
        target = objectMapper.readValue(json2, ExtendedStorageRoadAccessRestrictions.class);
    }


    @Test
    void testDefaultConstructor() {
        ExtendedStorageRoadAccessRestrictions storage = new ExtendedStorageRoadAccessRestrictions();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
        assertTrue(storage.getUseForWarnings(), "Default constructor should initialize 'use_for_warnings' to true");
    }

    @Test
    void serializationProducesCorrectJson() throws Exception {
        // Step 1: Create and configure an instance of ExtendedStorageRoadAccessRestrictions
        ExtendedStorageRoadAccessRestrictions storage = new ExtendedStorageRoadAccessRestrictions();

        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(storage);

        // Step 3: Assert JSON structure and values including enabled
        assertTrue(jsonResult.contains("\"RoadAccessRestrictions\""), "Serialized JSON should have 'RoadAccessRestrictions' key");
        assertTrue(jsonResult.contains("\"enabled\":true"), "Serialized JSON should have 'enabled' set to true");
        assertTrue(jsonResult.contains("\"use_for_warnings\":true"), "Serialized JSON should have 'use_for_warnings' set to true");
    }

    @Test
    void deSerializationDisabledCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Step 1: Create a JSON string with 'enabled' set to false
        String json = "{\"RoadAccessRestrictions\":{\"enabled\":false,\"use_for_warnings\":false}}";

        // Step 2: Deserialize the JSON string to an object
        ExtendedStorageRoadAccessRestrictions storage = objectMapper.readValue(json, ExtendedStorageRoadAccessRestrictions.class);

        // Step 3: Assert the object's values
        assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to false");
        assertFalse(storage.getUseForWarnings(), "Deserialized object should have 'use_for_warnings' set to true");
    }

    @Test
    void testDeserializationWithEmptyValues() throws Exception {
        String json = "{\"RoadAccessRestrictions\":\"\"}";
        ExtendedStorageRoadAccessRestrictions storage = new ObjectMapper().readValue(json, ExtendedStorageRoadAccessRestrictions.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertTrue(storage.getUseForWarnings(), "Deserialized object should have 'use_for_warnings' set to true");
    }

    @Test
    void testCopyPropertiesWithOverwrite() {
        assertNotEquals(source, target);
        target.copyProperties(source, true);

        assertEquals(source, target, "Source and target should be equal after copying properties with overwrite");
    }

    @Test
    void testCopyPropertiesWithoutOverwrite() {
        assertNotEquals(source, target);
        target.copyProperties(source, false);
        assertNotEquals(source, target, "Source and target should not be equal after copying properties without overwrite");
    }

    @Test
    void testCopyPropertiesWithNullSource() {
        ExtendedStorageRoadAccessRestrictions target = new ExtendedStorageRoadAccessRestrictions();
        target.copyProperties(null, true);
        assertTrue(target.getEnabled(), "Enabled should remain unchanged when source is null");
        assertTrue(target.getUseForWarnings(), "UseForWarnings should remain unchanged when source is null");
    }

    @Test
    void testCopyPropertiesWithEmptySource() throws JsonProcessingException, IllegalAccessException {
        // User reflection to set use_for_warnings to null
        List<Field> allFields = getAllFields(source.getClass());
        for (Field field : allFields) {
            if (field.getName().equals("use_for_warnings")) {
                field.setAccessible(true);
                field.set(source, null);
            }
        }
        assertNotEquals(source, target);
        target.copyProperties(source, true);
        assertNotEquals(source, target, "Source and target should be equal after copying properties from an empty source with overwrite");
    }

    @Test
    void testCopyPropertiesWithEmptyTarget() throws IllegalAccessException {
        // User reflection to set use_for_warnings to null
        List<Field> allFields = getAllFields(target.getClass());
        for (Field field : allFields) {
            if (field.getName().equals("use_for_warnings")) {
                field.setAccessible(true);
                field.set(target, null);
            }
        }
        assertNotEquals(source, target);
        target.copyProperties(source, true);
        assertEquals(source, target, "Source and target should be equal after copying properties to an empty target with overwrite");
    }


}