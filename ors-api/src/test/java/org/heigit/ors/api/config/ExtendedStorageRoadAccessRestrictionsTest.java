package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageRoadAccessRestrictionsTest {

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
}