package org.heigit.ors.api.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtendedStorageBordersTest {

    @Test
    void testInit() {
        ExtendedStorageBorders extendedStorageBorders = new ExtendedStorageBorders();
        assertEquals("", extendedStorageBorders.getBoundaries().toString());
        assertEquals("", extendedStorageBorders.getOpenborders().toString());
        assertEquals("", extendedStorageBorders.getIds().toString());
    }

    @Test
    void testSerialization() throws JsonProcessingException {
        ExtendedStorageBorders storage = new ExtendedStorageBorders();
        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(storage);
        String expectedJson = "{\"Borders\":{\"enabled\":true,\"boundaries\":\"\",\"ids\":\"\",\"openborders\":\"\"}}";
        // Step 3: Assert JSON structure and values including enabled
        assertEquals(expectedJson, jsonResult);
    }

    @Test
    void testDeserialization() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Path path_boundaries = Path.of("/path/to/boundaries");
        Path path_ids = Path.of("/path/to/ids");
        Path path_openborders = Path.of("/path/to/openborders");
        String json = "{\"Borders\":{\"enabled\":true,\"boundaries\":\"" +
                path_boundaries + "\",\"ids\":\"" +
                path_ids + "\",\"openborders\":\"" +
                path_openborders + "\"}}";
        ExtendedStorageBorders storage = objectMapper.readValue(json, ExtendedStorageBorders.class);
        assertEquals(path_boundaries.toAbsolutePath(), storage.getBoundaries());
        assertEquals(path_ids.toAbsolutePath(), storage.getIds());
        assertEquals(path_openborders.toAbsolutePath(), storage.getOpenborders());
    }
}