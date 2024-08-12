package org.heigit.ors.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.storages.ExtendedStorageBorders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageBordersTest {

    ExtendedStorageBorders storage1;
    ExtendedStorageBorders storage2;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws JsonProcessingException {
        storage1 = objectMapper.readValue("{\"Borders\":{\"enabled\":true,\"boundaries\":\"/path/to/boundaries\",\"ids\":\"/path/to/ids\",\"openborders\":\"/path/to/openborders\"}}", ExtendedStorageBorders.class);
        storage2 = objectMapper.readValue("{\"Borders\":{\"enabled\":false,\"boundaries\":\"/second/path/to/boundaries\",\"ids\":\"/second/path/to/ids\",\"openborders\":\"/second/path/to/openborders\"}}", ExtendedStorageBorders.class);
    }

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

    @Test
    void testDeserializationWithEmptyValues() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"Borders\":\"\"}";
        ExtendedStorageBorders storage = objectMapper.readValue(json, ExtendedStorageBorders.class);
        assertEquals("", storage.getBoundaries().toString());
        assertEquals("", storage.getIds().toString());
        assertEquals("", storage.getOpenborders().toString());
        assertFalse(storage.getEnabled());
    }

    @Test
    void testCopyProperties() {
        assertNotEquals(storage1, storage2);

        storage1.copyProperties(storage2, true);

        assertEquals(storage1, storage2);
    }

    @Test
    void testCopyPropertiesWithNull() {
        assertTrue(storage1.getEnabled());

        storage1.copyProperties(null, true);
        assertTrue(storage1.getEnabled());
    }

    @Test
    void testCopyPropertiesWithEmptyTargetValues() {
        ExtendedStorageBorders target = new ExtendedStorageBorders();
        Path emptyPath = Path.of("");
        assertEquals(emptyPath, target.getBoundaries());
        assertEquals(emptyPath, target.getIds());
        assertEquals(emptyPath, target.getOpenborders());

        assertNotEquals(target, storage1);

        target.copyProperties(storage1, true);

        assertEquals(storage1, target);
    }

    @Test
    void testCopyPropertiesWithEmptySourceValues() {
        ExtendedStorageBorders source = new ExtendedStorageBorders();
        Path emptyPath = Path.of("");
        assertEquals(emptyPath, source.getBoundaries());
        assertEquals(emptyPath, source.getIds());
        assertEquals(emptyPath, source.getOpenborders());

        assertNotEquals(storage1, source);

        storage1.copyProperties(source, true);

        assertNotEquals(storage1, source);
    }
}