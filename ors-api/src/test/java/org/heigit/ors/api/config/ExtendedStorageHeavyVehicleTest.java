package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExtendedStorageHeavyVehicleTest {

    @Test
    void testDefaultConstructor() {
        ExtendedStorageHeavyVehicle storage = new ExtendedStorageHeavyVehicle();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
        assertTrue(storage.getRestrictions(), "Default constructor should initialize 'restrictions' to true");
    }

    @Test
    void testSerializationProducesCorrectJson() throws Exception {
        // Step 1: Create and configure an instance of ExtendedStorageHeavyVehicle
        ExtendedStorageHeavyVehicle storage = new ExtendedStorageHeavyVehicle();

        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(storage);

        // Step 3: Assert JSON structure and values including enabled
        assertTrue(jsonResult.contains("\"HeavyVehicle\""), "Serialized JSON should have 'HeavyVehicle' key");
        assertTrue(jsonResult.contains("\"enabled\":true"), "Serialized JSON should have 'enabled' set to true");
        assertTrue(jsonResult.contains("\"restrictions\":true"), "Serialized JSON should have 'restrictions' set to true");
    }

    @Test
    void testDeserializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Step 1: Create a JSON string with 'enabled' set to true and restrictions
        String json = "{\"HeavyVehicle\":{\"enabled\":true,\"restrictions\":true}}";

        // Step 2: Deserialize the JSON string to an object
        ExtendedStorageHeavyVehicle storage = objectMapper.readValue(json, ExtendedStorageHeavyVehicle.class);

        // Step 3: Assert the object's values
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertTrue(storage.getRestrictions(), "Deserialized object should have 'restrictions' set to true");

        // Deserialize with enabled and restrictions set to false
        json = "{\"HeavyVehicle\":{\"enabled\":false,\"restrictions\":false}}";
        storage = objectMapper.readValue(json, ExtendedStorageHeavyVehicle.class);
        assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to false");
        assertFalse(storage.getRestrictions(), "Deserialized object should have 'restrictions' set to false");
    }
}