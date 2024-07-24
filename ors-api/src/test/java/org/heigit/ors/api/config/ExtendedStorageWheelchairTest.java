package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.api.config.profile.storages.ExtendedStorageWheelchair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageWheelchairTest {

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

}