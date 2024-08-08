package org.heigit.ors.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.storages.ExtendedStorageTrailDifficulty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageTrailDifficultyTest {

    @Test
    void testDefaultConstructor() {
        ExtendedStorageTrailDifficulty storage = new ExtendedStorageTrailDifficulty();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
    }

    @Test
    void testSerializationProducesCorrectJson() throws Exception {
        // Step 1: Create and configure an instance of ExtendedStorageTrailDifficulty
        ExtendedStorageTrailDifficulty storage = new ExtendedStorageTrailDifficulty();

        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(storage);

        // Step 3: Assert JSON structure and values including enabled
        assertTrue(jsonResult.contains("\"TrailDifficulty\""), "Serialized JSON should have 'TrailDifficulty' key");
        assertTrue(jsonResult.contains("\"enabled\":true"), "Serialized JSON should have 'enabled' set to true");
    }

    @Test
    void testDeserializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Step 1: Create a JSON string with 'enabled' set to true
        String json = "{\"TrailDifficulty\":{\"enabled\":true}}";

        // Step 2: Deserialize the JSON string to an object
        ExtendedStorageTrailDifficulty storage = objectMapper.readValue(json, ExtendedStorageTrailDifficulty.class);

        // Step 3: Assert the object's values
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");

        // Deserialize with enabled set to false
        json = "{\"TrailDifficulty\":{\"enabled\":false}}";
        storage = objectMapper.readValue(json, ExtendedStorageTrailDifficulty.class);
        assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to false");
    }


    @Test
    void testDeserializationWithEmptyValues() throws Exception {
        String json = "{\"TrailDifficulty\":\"\"}";
        ExtendedStorageTrailDifficulty storage = new ObjectMapper().readValue(json, ExtendedStorageTrailDifficulty.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
    }
}