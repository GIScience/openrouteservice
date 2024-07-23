package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.api.config.profile.storages.ExtendedStorageHereTraffic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageHereTrafficTest {

    String streets = "./src/test/files/here-traffic-streets.csv";
    String patterns = "./src/test/files/here-traffic-patterns.csv";
    String ref_patterns = "/some/absolute/path/src/test/files/here-traffic-ref-patterns.csv";

    private static Stream<Arguments> provideJsonStrings() {
        return Stream.of(Arguments.of("{\"HereTraffic\":{\"enabled\":true,\"streets\":\"./src/test/files/here-traffic-streets.csv\",\"ref_pattern\":\"./src/test/files/here-traffic-ref-patterns.csv\",\"pattern\":\"./src/test/files/here-traffic-patterns.csv\"}}"), Arguments.of("{\"HereTraffic\":{\"enabled\":false,\"streets\":\"./src/test/files/here-traffic-streets.csv\",\"ref_pattern\":\"./src/test/files/here-traffic-ref-patterns.csv\",\"pattern\":\"./src/test/files/here-traffic-patterns.csv\"}}"), Arguments.of("{\"HereTraffic\":{\"streets\":\"./src/test/files/here-traffic-streets.csv\",\"ref_pattern\":\"./src/test/files/here-traffic-ref-patterns.csv\",\"pattern\":\"./src/test/files/here-traffic-patterns.csv\"}}"));
    }

    @Test
    void testDefaultConstructor() {
        ExtendedStorageHereTraffic storage = new ExtendedStorageHereTraffic();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
        assertEquals("", storage.getStreets().toString(), "Default constructor should initialize 'streets' to \"\"");
        assertEquals("", storage.getRefPattern().toString(), "Default constructor should initialize 'ref_pattern' to \"\"");
        assertEquals("", storage.getPattern().toString(), "Default constructor should initialize 'pattern' to \"\"");
        assertEquals(250, storage.getRadius(), "Default constructor should initialize 'radius' to 250");
        assertFalse(storage.getOutputLog(), "Default constructor should initialize 'output_log' to false");
        assertEquals("", storage.getLogLocation().toString(), "Default constructor should initialize 'log_location' to \"\"");
    }

    @Test
    void serializationProducesCorrectJson() throws Exception {
        // Step 1: Create and configure an instance of ExtendedStorageHereTraffic
        ExtendedStorageHereTraffic storage = new ExtendedStorageHereTraffic();

        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(storage);

        // Step 3: Assert JSON structure and values including enabled
        assertTrue(jsonResult.contains("\"HereTraffic\""), "Serialized JSON should have 'HereTraffic' key");
        assertTrue(jsonResult.contains("\"enabled\":true"), "Serialized JSON should have 'enabled' set to true");
        assertTrue(jsonResult.contains("\"streets\":\"\""), "Serialized JSON should have 'streets' set to \"\"");
        assertTrue(jsonResult.contains("\"ref_pattern\":\"\""), "Serialized JSON should have 'ref_pattern' set to \"\"");
        assertTrue(jsonResult.contains("\"pattern\":\"\""), "Serialized JSON should have 'pattern' set to \"\"");


    }

    @Test
    void deSerializationDisabledCorrectJson() throws Exception {
        Path streets_path = Paths.get(streets);
        Path patterns_path = Paths.get(patterns);
        Path ref_patterns_path = Paths.get(ref_patterns);
        Integer radius = 500;
        boolean output_log = true;
        Path log_location = Paths.get("/some/absolute/path/src/test/files/here-traffic-log.txt");

        // Step 1: Create and configure an instance of ExtendedStorageHereTraffic
        String json = "{\"HereTraffic\":{\"enabled\":false,\"streets\":\"" + streets_path + "\",\"ref_pattern\":\"" + ref_patterns_path + "\",\"pattern\":\"" + patterns_path + "\",\"radius\":" + radius + ",\"output_log\":" + output_log + ",\"log_location\":\"" + log_location + "\"}}";

        // Step 2: Deserialize the JSON to an object
        ObjectMapper mapper = new ObjectMapper();
        ExtendedStorageHereTraffic storage = mapper.readValue(json, ExtendedStorageHereTraffic.class);
        // Step 3: Assert JSON structure and values including enabled
        assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to false");
        assertEquals(streets_path.toAbsolutePath(), storage.getStreets(), "Deserialized object should have 'streets' set to the absolute path.");
        assertEquals(ref_patterns_path.toAbsolutePath(), storage.getRefPattern(), "Deserialized object should have 'ref_pattern' set to the absolute path.");
        assertEquals(patterns_path.toAbsolutePath(), storage.getPattern(), "Deserialized object should have 'pattern' set to the absolute path.");
        assertEquals(radius, storage.getRadius(), "Deserialized object should have 'radius' set to 500");
        assertTrue(storage.getOutputLog(), "Deserialized object should have 'output_log' set to true");
        assertEquals(log_location.toAbsolutePath(), storage.getLogLocation(), "Deserialized object should have 'log_location' set to the absolute path.");
    }

    @Test
    void deSerializationWithoutEnabledCorrectJson() throws Exception {

        // Step 1: Create and configure an instance of ExtendedStorageHereTraffic
        String json = "{\"HereTraffic\":{\"streets\":\"" + streets + "\",\"ref_pattern\":\"" + ref_patterns + "\",\"pattern\":\"" + patterns + "\"}}";

        // Step 2: Deserialize the JSON to an object
        ObjectMapper mapper = new ObjectMapper();
        ExtendedStorageHereTraffic storage = mapper.readValue(json, ExtendedStorageHereTraffic.class);

        // Step 3: Assert JSON structure and values including enabled
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertEquals(Paths.get(streets).toAbsolutePath(), storage.getStreets(), "Deserialized object should have 'streets' set to the absolute path.");
        assertEquals(Paths.get(ref_patterns).toAbsolutePath(), storage.getRefPattern(), "Deserialized object should have 'ref_pattern' set to the absolute path.");
        assertEquals(Paths.get(patterns).toAbsolutePath(), storage.getPattern(), "Deserialized object should have 'pattern' set to the absolute path.");
        assertEquals(250, storage.getRadius(), "Deserialized object should have 'radius' set to 250");
        assertFalse(storage.getOutputLog(), "Deserialized object should have 'output_log' set to false");
        assertEquals("", storage.getLogLocation().toString(), "Deserialized object should have 'log_location' set to \"\"");
    }

    @ParameterizedTest
    @MethodSource("provideJsonStrings")
    void deSerializationCorrectJson(String json) throws Exception {
        // Step 1: Deserialize the JSON to an object
        ObjectMapper mapper = new ObjectMapper();
        ExtendedStorageHereTraffic storage = mapper.readValue(json, ExtendedStorageHereTraffic.class);

        // Step 2: Assert JSON structure and values including enabled
        if (json.contains("enabled\":false")) {
            assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to false");
        } else {
            assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        }
        assertEquals(Paths.get("./src/test/files/here-traffic-streets.csv").toAbsolutePath(), storage.getStreets(), "Deserialized object should have 'streets' set to the absolute path.");
        assertEquals(Paths.get("./src/test/files/here-traffic-ref-patterns.csv").toAbsolutePath(), storage.getRefPattern(), "Deserialized object should have 'ref_pattern' set to the absolute path.");
        assertEquals(Paths.get("./src/test/files/here-traffic-patterns.csv").toAbsolutePath(), storage.getPattern(), "Deserialized object should have 'pattern' set to the absolute path.");
    }

    @Test
    void testDeserializationWithEmptyValues() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"HereTraffic\":\"\"}";
        ExtendedStorageHereTraffic storage = objectMapper.readValue(json, ExtendedStorageHereTraffic.class);
        assertEquals("", storage.getStreets().toString(), "Deserialized object should have 'streets' set to \"\"");
        assertEquals("", storage.getRefPattern().toString(), "Deserialized object should have 'ref_pattern' set to \"\"");
        assertEquals("", storage.getPattern().toString(), "Deserialized object should have 'pattern' set to \"\"");
        assertEquals(250, storage.getRadius(), "Deserialized object should have 'radius' set to 250");
        assertFalse(storage.getOutputLog(), "Deserialized object should have 'output_log' set to false");
        assertEquals("", storage.getLogLocation().toString(), "Deserialized object should have 'log_location' set to \"\"");
        assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
    }
}