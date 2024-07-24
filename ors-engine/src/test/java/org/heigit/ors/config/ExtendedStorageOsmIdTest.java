package org.heigit.ors.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.storages.ExtendedStorageOsmId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageOsmIdTest {

    @Test
    void testDefaultConstructor() {
        ExtendedStorageOsmId storage = new ExtendedStorageOsmId();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
    }

    @Test
    void testSerializationProducesCorrectJson() throws Exception {
        // Step 1: Create and configure an instance of ExtendedStorageOsmId
        ExtendedStorageOsmId storage = new ExtendedStorageOsmId();

        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(storage);

        // Step 3: Assert JSON structure and values including enabled
        assertTrue(jsonResult.contains("\"OsmId\""), "Serialized JSON should have 'OsmId' key");
        assertTrue(jsonResult.contains("\"enabled\":true"), "Serialized JSON should have 'enabled' set to true");
    }

    @Test
    void testDeserializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Step 1: Create a JSON string with 'enabled' set to true
        String json = "{\"OsmId\":{\"enabled\":true}}";

        // Step 2: Deserialize the JSON string to an object
        ExtendedStorageOsmId storage = objectMapper.readValue(json, ExtendedStorageOsmId.class);

        // Step 3: Assert the object's values
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");

        // Deserialize with enabled set to false
        json = "{\"OsmId\":{\"enabled\":false}}";
        storage = objectMapper.readValue(json, ExtendedStorageOsmId.class);
        assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to false");
    }

    @Test
    void testDeserializationWithEmptyValues() throws Exception {
        String json = "{\"OsmId\":\"\"}";
        ExtendedStorageOsmId storage = new ObjectMapper().readValue(json, ExtendedStorageOsmId.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
    }
}