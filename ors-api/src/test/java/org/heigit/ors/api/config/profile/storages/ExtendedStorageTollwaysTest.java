package org.heigit.ors.api.config.profile.storages;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedStorageTollwaysTest {

    @Test
    void testExtendedStorageTollways() {
        ExtendedStorageTollways extendedStorageTollways = new ExtendedStorageTollways();
        assertNotNull(extendedStorageTollways);
        assertTrue(extendedStorageTollways.getEnabled());
    }

    @Test
    void testDeSerializationCorrectJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"Tollways\":{\"enabled\":true}}";
        ExtendedStorageTollways storage = objectMapper.readValue(json, ExtendedStorageTollways.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
    }

    @Test
    void testDeSerializationCorrectJsonWithoutEnabled() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"Tollways\":{}}";
        ExtendedStorageTollways storage = objectMapper.readValue(json, ExtendedStorageTollways.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
    }


    @Test
    void testDeSerializationCorrectJsonWithEmptyValues() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"Tollways\":\"\"}";
        ExtendedStorageTollways storage = objectMapper.readValue(json, ExtendedStorageTollways.class);
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
    }

}